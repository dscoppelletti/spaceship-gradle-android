/*
 * Copyright (C) 2020-2022 Dario Scoppelletti, <http://www.scoppelletti.it/>.
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

import com.android.build.api.artifact.MultipleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import it.scoppelletti.spaceship.gradle.android.tasks.CreditsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for Android Apps.
 *
 * @since 1.0.0
 */
public abstract class AppPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.findByType(
            ApplicationAndroidComponentsExtension::class.java)?.let { ext ->
            ext.onVariants(ext.selector().all()) { variant ->
                onVariant(project, variant)
            }
        } ?: run {
            project.logger.error("""Extension
                |${ApplicationAndroidComponentsExtension::class.java} not
                |found.""".trimMargin().replace('\n', ' '))
        }
    }

    /**
     * Completes a variant.
     *
     * @param project Project.
     * @param variant Variant.
     */
    @Suppress("UnstableApiUsage")
    private fun onVariant(project: Project, variant: ApplicationVariant) {
        val tools = CreditsTools.create(project)
        val creditsTask = tools.createCreditsTask(variant)
        variant.artifacts.use(creditsTask)
            .wiredWith(CreditsTask::outputDir)
            .toAppendTo(MultipleArtifact.ASSETS)
    }
}