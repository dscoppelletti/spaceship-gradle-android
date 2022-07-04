/*
 * Copyright (C) 2019-2022 Dario Scoppelletti, <http://www.scoppelletti.it/>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("RemoveRedundantQualifierName")

package it.scoppelletti.spaceship.gradle.android

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.api.variant.LibraryVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import it.scoppelletti.spaceship.gradle.DokkaTools
import it.scoppelletti.spaceship.gradle.JarTools
import it.scoppelletti.spaceship.gradle.LicenseTools
import it.scoppelletti.spaceship.gradle.PublishTools
import it.scoppelletti.spaceship.gradle.model.DokkaConfigModel
import it.scoppelletti.spaceship.gradle.model.LibraryExtension
import it.scoppelletti.spaceship.gradle.tasks.DokkaLogoStylesTask
import it.scoppelletti.spaceship.gradle.tasks.NoticeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider

private const val AAR_EXTENSION = "aar"

/**
 * Plugin for Android libraries.
 *
 * @since 1.0.0
 */
public abstract class LibraryPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create(LibraryExtension.NAME,
            LibraryExtension::class.java
        )

        val androidTools = AndroidTools.create(project)

        project.extensions.findByType(
            LibraryAndroidComponentsExtension::class.java)?.let { ext ->
            ext.onVariants(
                ext.selector().withBuildType(LibraryPlugin.PUBL_NAME)
            ) { variant ->
                onVariant(project, androidTools, variant)
            }
        } ?: run {
            project.logger.error("""Extension
                |${LibraryAndroidComponentsExtension::class.java} not
                |found.""".trimMargin().replace('\n', ' '))
        }

        project.afterEvaluate { prj ->
            onProjectAfterEvaluate(prj, androidTools)
        }
    }

    /**
     * Completes the project.
     *
     * @param project      Project.
     * @param androidTools Tools for Android compilation.
     */
    private fun onProjectAfterEvaluate(
        project: Project,
        androidTools: AndroidTools?
    ) {
        androidTools?.connectTasks(LibraryPlugin.PUBL_NAME)
        val publTools = PublishTools.create(project) ?: return

        val publ = publTools.createPublication(LibraryPlugin.PUBL_NAME,
            LibraryPlugin.PUBL_NAME).apply {
            pom.packaging = AAR_EXTENSION
        }

        val taskNames = TaskNames.create(LibraryPlugin.PUBL_NAME)
        val dokkaTools = DokkaTools.create(project)

        val logoStylesFile = project.buildDir
            .resolve(DokkaLogoStylesTask::class.java.canonicalName)
            .resolve(DokkaLogoStylesTask.STYLES_NAME)
        val logoStylesTask = dokkaTools.createLogoStylesTask(
            "generateDokkaLogoStyles", logoStylesFile)

        val dokkaConfigModel = DokkaConfigModel(
            footerMessage = dokkaTools.buildCopyright(),
            customStyleSheets = if (logoStylesTask != null) {
                listOf(logoStylesFile.path)
            } else {
                null
            }
        )

        dokkaTools.createDokkaTask(dokkaConfigModel)?.apply {
            moduleName.set(project.rootProject.name)
            dokkaSourceSets.configureEach { sourceSet ->
                sourceSet.includes.from(project.rootDir.resolve(
                    DokkaTools.README_NAME))
            }
        }?.let { dokkaTask ->
            logoStylesTask?.let {
                dokkaTask.dependsOn(it)
            }

            (project.tasks.findByName(
                taskNames.copyMetainf
            ) as? Copy)?.let { metainfTask ->
                publ.artifact(
                    publTools.createJavadocPackageTask(
                        dokkaTask.outputDirectory,
                        metainfTask.destinationDir
                    ).apply {
                        configure { task ->
                            task.dependsOn(metainfTask)
                        }
                    })
            } ?: run {
                project.logger.error("Task ${taskNames.copyMetainf} not found.")
            }
        }

        publTools.createPublishingRepo()
    }

    /**
     * Completes a variant.
     *
     * @param project      Project.
     * @param androidTools Tools for Android compilation.
     * @param variant      Variant.
     */
    private fun onVariant(
        project: Project,
        androidTools: AndroidTools?,
        variant: LibraryVariant
    ) {
        val metainfDeps = mutableListOf<TaskProvider<out Task>>()
        val metainfSources = project.files()
        val taskNames = TaskNames.create(variant.name)

        val licenseTools = LicenseTools.create(project)
        val licenseDir = project.buildDir
            .resolve("intermediates")
            .resolve(LicenseTools.LICENSE_TASK)
            .resolve(variant.name)
            .resolve("META-INF")
        metainfDeps.add(licenseTools.createLicenseTask(taskNames.copyLicense,
            licenseDir))
        metainfSources.from(licenseDir)

        val noticeDir = project.buildDir
            .resolve("intermediates")
            .resolve(NoticeTask::class.java.canonicalName)
            .resolve(variant.name)
            .resolve("META-INF")
        metainfDeps.add(licenseTools.createNoticeTask(taskNames.generateNotice,
            noticeDir.resolve(NoticeTask.NOTICE_NAME)))
        metainfSources.from(noticeDir)

        val jarTools = JarTools.create(project)
        val metainfDir = project.buildDir
            .resolve("intermediates")
            .resolve(JarTools.METAINF_TASK)
            .resolve(variant.name)
            .resolve("META-INF")
        val metainfTask = jarTools.createMetainfTask(taskNames.copyMetainf,
            metainfSources, metainfDir).apply {
            configure { task ->
                task.setDependsOn(metainfDeps)
            }
        }

        androidTools?.let {
            it.createLibraryArchiveTransform(variant, metainfDir).apply {
                dependsOn(metainfTask)
            }

            it.createSourceArchiveTransform(variant, metainfDir).apply {
                dependsOn(metainfTask)
            }
        }
    }

    public companion object {
        
        /**
         * Name of the Maven publication.
         */
        public const val PUBL_NAME: String = "release"
    }
}