/*
 * Copyright (C) 2020 Dario Scoppelletti, <http://www.scoppelletti.it/>.
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

package it.scoppelletti.spaceship.gradle.android.tasks

import freemarker.template.TemplateException
import it.scoppelletti.spaceship.gradle.android.model.ArtifactElement
import it.scoppelletti.spaceship.gradle.android.model.CreditElement
import it.scoppelletti.spaceship.gradle.android.model.CreditsModel
import it.scoppelletti.spaceship.gradle.freemarker.FreemarkerExt
import java.io.IOException
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.BuildException

private const val CONFIG_RUNTIME = "RuntimeElements"
private const val TEMPLATE_NAME =
    "/it/scoppelletti/spaceship/gradle/android/tasks/credits.ftl"

/**
 * Generates the credits file.
 *
 * @since 1.0.0
 *
 * @property variantName  Variant name.
 * @property databaseUrl  Credits database URL.
 * @property templateName Template name.
 * @property outputDir    Output folder.
 * @property creditsName  Name of the credits file.
 */
public abstract class CreditsTask @Inject constructor(
    objects: ObjectFactory,
    providers: ProviderFactory
): DefaultTask() {

    @get:Input
    public abstract val variantName: Property<String>

    @get:Input
    public val databaseUrl: Property<String> =
        objects.property(String::class.java).apply {
            convention(providers.gradleProperty(PROP_DATABASE))
        }

    @get:Input
    public val templateName: Property<String> =
        objects.property(String::class.java).apply {
            convention(providers.gradleProperty(PROP_TEMPLATE)
                .orElse(TEMPLATE_NAME))
        }

    @get:OutputDirectory
    public abstract val outputDir: DirectoryProperty

    @get:Input
    public val creditsName: Property<String> =
        objects.property(String::class.java).apply {
            convention(CREDITS_NAME)
        }

    @TaskAction
    public fun action() {
        val templName = templateName.get()
        val outDir = outputDir.get().asFile
        val fileName = creditsName.get()
        val database = CreditDatabase.load(databaseUrl.get())
        val configName = variantName.get() + CONFIG_RUNTIME
        val config = project.configurations.getByName(configName)

        val credits = mutableSetOf<CreditElement>()
        database.credits
            .filter { it.force }
            .toCollection(credits)

        config.allDependencies.withType(ExternalDependency::class.java)
            .forEach {
                addDependency(credits, it, database)
            }

        val model = CreditsModel(credits.sortedBy { it.key })

        val templ = try {
            FreemarkerExt.config.getTemplate(templName)
        } catch (ex: IOException) {
            logger.error("Fail to get $templName.", ex)
            throw BuildException("Fail to get $templName.", ex)
        }

        outDir.mkdirs()
        outDir.resolve(fileName).writer().use { writer ->
            try {
                templ.process(model, writer)
            } catch (ex: IOException) {
                logger.error("Fail to process $templName.", ex)
                throw BuildException("Fail to process $templName.", ex)
            } catch (ex: TemplateException) {
                logger.error("Fail to process $templName.", ex)
                throw BuildException("Fail to process $templName.", ex)
            }
        }
    }

    private fun addDependency(credits: MutableSet<CreditElement>,
        dep: ExternalDependency, database: CreditDatabase) {
        val artifact = ArtifactElement(
            requireNotNull(dep.group) {
                "Argument dep.group is null."
            },
            dep.name
        )
        logger.debug("Detect artifact {}", artifact)

        database[artifact]?.let {
            credits.add(it)
        } ?: run {
            logger.warn("No credit found for artifact {}.", artifact)
        }
    }

    public companion object {

        /**
         * Name of the credits file.
         */
        public const val CREDITS_NAME: String = "credits.html"

        /**
         * Property containing the URL of the credits database.
         */
        public const val PROP_DATABASE: String =
            "it.scoppelletti.spaceship.credits.databaseUrl"

        /**
         * Property containing the URL of the credits template.
         */
        public const val PROP_TEMPLATE: String =
            "it.scoppelletti.spaceship.credits.templateName"

        /**
         * Base of the name of the schema.
         */
        public const val SCHEMA: String =
            "spaceship/gradle/android/tasks/credits"

        /**
         * Version of the schema.
         */
        public const val VERSION: Int = 1
    }
}