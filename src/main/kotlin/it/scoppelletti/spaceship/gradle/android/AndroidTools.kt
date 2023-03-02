/*
 * Copyright (C) 2019-2023 Dario Scoppelletti, <http://www.scoppelletti.it/>.
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

package it.scoppelletti.spaceship.gradle.android

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.LibraryVariant
import it.scoppelletti.spaceship.gradle.android.tasks.UpdateLibraryArtifactTask
import java.io.File
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.Zip

/**
 * Tools for Android compilation.
 */
internal class AndroidTools(
    private val project: Project
) {

    /**
     * Creates the library archive trasformation.
     *
     * @param  variant Variant.
     * @param  metaInf Folder of the license files.
     * @return         The task provider.
     */
    fun createLibraryArchiveTransform(
        variant: LibraryVariant,
        metaInf: File
    ): TaskProvider<UpdateLibraryArtifactTask> {
        val taskNames = TaskNames.create(variant.name)

        val transformTask = project.tasks.register(
            taskNames.updateLibraryArchive,
            UpdateLibraryArtifactTask::class.java
        ) { task ->
            task.metainfDir.set(metaInf)
        }

        variant.artifacts.use(transformTask)
            .wiredWithFiles(
                UpdateLibraryArtifactTask::initialArtifact,
                UpdateLibraryArtifactTask::updatedArtifact
            )
            .toTransform(SingleArtifact.AAR)

        return transformTask
    }

    /**
     * Creates the source archive trasformation.
     *
     * @param  variant Variant.
     * @param  metaInf META-INF resource folder.
     * @return         The task provider.
     */
    fun createSourceArchiveTransform(
        variant: LibraryVariant,
        metaInf: File
    ): TaskProvider<Copy> {
        val taskNames = TaskNames.create(variant.name)

        val sourceArchive = project.buildDir
            .resolve("intermediates")
            .resolve("source_jar")
            .resolve(variant.name)
            .resolve("${variant.name}-sources.jar")

        val workDir = project.buildDir
            .resolve("intermediates")
            .resolve(AndroidTools::class.java.canonicalName)
            .resolve("sourceArchiveTransform")
            .resolve(variant.name)

        val unzipTask = project.tasks.register(taskNames.unzipSourceArchive,
            Copy::class.java) { task ->
            task.description = "Unzip ${variant.name} source archive"
            task.group = PublishingPlugin.PUBLISH_TASK_GROUP
            task.from(project.zipTree(sourceArchive))
            task.destinationDir = workDir
            task.outputs.upToDateWhen { false }
        }

        val copyTask = project.tasks.register(
            taskNames.copyLicenseIntoSourceArchive, Copy::class.java) { task ->
            task.description = "Copies the license files"
            task.group = PublishingPlugin.PUBLISH_TASK_GROUP
            task.dependsOn(unzipTask)
            task.from(metaInf)
            task.destinationDir = workDir.resolve("META-INF")
        }

        project.tasks.register(taskNames.rezipSourceArchive,
            Zip::class.java) { task ->
            task.description = "Rezip ${variant.name} source archive"
            task.group = BasePlugin.BUILD_GROUP
            task.dependsOn(copyTask)
            task.from(workDir)
            task.include("**/**")
            task.archiveFileName.set(sourceArchive.name)
            task.destinationDirectory.set(sourceArchive.parentFile)
            task.outputs.upToDateWhen { false }
        }

        return unzipTask
    }

    /**
     * Connects the tasks.
     *
     * @param varName Variant name.
     */
    fun connectTasks(varName: String) {
        val taskNames = TaskNames.create(varName)

        connectLibraryArchiveTasks(taskNames)
        connectSourceArchiveTasks(taskNames)
    }

    /**
     * Connects the tasks.
     *
     * @param taskNames Task names.
     */
    private fun connectLibraryArchiveTasks(taskNames: TaskNames) {
        val bundleTask = project.tasks.findByName(taskNames.bundleAar)
        if (bundleTask == null) {
            project.logger.error("Task ${taskNames.bundleAar} not found.")
            return
        }

        val rezipTask = project.tasks.findByName(taskNames.updateLibraryArchive)
        if (rezipTask == null) {
            project.logger.error(
                "Task ${taskNames.updateLibraryArchive} not found.")
            return
        }

        bundleTask.finalizedBy(rezipTask)
    }

    /**
     * Connects the tasks.
     *
     * @param taskNames Task names.
     */
    private fun connectSourceArchiveTasks(taskNames: TaskNames) {
        val bundleTask = project.tasks.findByName(taskNames.zipSourceArchive)
        if (bundleTask == null) {
            project.logger.error(
                "Task ${taskNames.zipSourceArchive} not found.")
            return
        }

        val rezipTask = project.tasks.findByName(taskNames.rezipSourceArchive)
        if (rezipTask == null) {
            project.logger.error(
                "Task ${taskNames.rezipSourceArchive} not found.")
            return
        }

        bundleTask.finalizedBy(rezipTask)
    }

    companion object {

        /**
         * Creates a new instance.
         *
         * @param  project Project.
         * @return         The new object.
         */
        fun create(project: Project) = AndroidTools(project)
    }
}