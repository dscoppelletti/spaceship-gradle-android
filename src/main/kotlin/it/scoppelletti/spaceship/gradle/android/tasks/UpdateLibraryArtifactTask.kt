/*
 * Copyright (C) 2023 Dario Scoppelletti, <http://www.scoppelletti.it/>.
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

import it.scoppelletti.spaceship.gradle.zip.zip
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

/**
 * Insert license files in the library archive.
 *
 * @since 1.1.0
 *
 * @property initialArtifact Initial artifact.
 * @property metainfDir      Folder of the license files.
 * @property updatedArtifact Updated artifact.
 */
public abstract class UpdateLibraryArtifactTask: DefaultTask() {

    @get:InputFiles
    public abstract val initialArtifact: RegularFileProperty

    @get:InputDirectory
    public abstract val metainfDir: DirectoryProperty

    @get:OutputFile
    public abstract val updatedArtifact: RegularFileProperty

    @TaskAction
    public fun taskAction() {
        val workDir = project.buildDir
            .resolve("intermediates")
            .resolve(name).apply {
                mkdirs()
            }

        val unzipResult = project.copy {
            it.from(project.zipTree(initialArtifact))
            it.into(workDir)
        }

        if (!unzipResult.didWork) {
            logger.error("unzipResult.didWork=false")
            didWork = false
            return
        }

        val licenseResult = project.copy {
            it.from(metainfDir)
            it.into(workDir.resolve("META-INF").apply {
                mkdir()
            })
        }

        if (!licenseResult.didWork) {
            logger.error("licenseResult.didWork=false")
            didWork = false
            return
        }

        val zipResult = project.zip(updatedArtifact.get().asFile, workDir)
        if (!zipResult.didWork) {
            logger.error("zipResult.didWork=false")
            didWork = false
            return
        }

        didWork = true
    }
}