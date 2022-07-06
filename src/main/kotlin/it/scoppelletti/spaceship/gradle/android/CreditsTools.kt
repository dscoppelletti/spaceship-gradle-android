/*
 * Copyright (C) 2022 Dario Scoppelletti, <http://www.scoppelletti.it/>.
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

import com.android.build.api.variant.ApplicationVariant
import groovy.lang.MissingPropertyException
import it.scoppelletti.spaceship.gradle.android.tasks.CreditsTask
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.TaskProvider

/**
 * Tools for credits.
 */
internal class CreditsTools private constructor(
    private val project: Project
) {

    /**
     * Creates the credits task.
     *
     * @param variant Variant.
     */
    fun createCreditsTask(
        variant: ApplicationVariant
    ): TaskProvider<CreditsTask> {
        val taskNames = TaskNames.create(variant.name)

        val databaseUrl = try {
            project.property(CreditsTask.PROP_DATABASE) as String?
        } catch (ex: MissingPropertyException) {
            null
        }
        project.logger.info(
            "Property ${CreditsTask.PROP_DATABASE}=$databaseUrl")

        val templateName = try {
            project.property(CreditsTask.PROP_TEMPLATE) as String?
        } catch (ex: MissingPropertyException) {
            null
        }
        project.logger.info(
            "Property ${CreditsTask.PROP_TEMPLATE}=$templateName")

        val outDir = project.buildDir
            .resolve("intermediates")
            .resolve(CreditsTask::class.java.canonicalName)
            .resolve(variant.name)
            .resolve("assets")

        return project.tasks.register(taskNames.generateCredits,
            CreditsTask::class.java) { task ->
            task.description = "Creates ${variant.name} credits file"
            task.group = BasePlugin.BUILD_GROUP
            task.variantName.set(variant.name)
            task.outputDir.set(outDir)
        }
    }

    companion object {

        /**
         * Creates a new instance.
         *
         * @param  project Project.
         * @return         The new object.
         */
        fun create(project: Project) = CreditsTools(project)
    }
}