/*
 * Copyright (C) 2020-2023 Dario Scoppelletti, <http://www.scoppelletti.it/>.
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

import org.apache.commons.lang3.StringUtils

/**
 * Task names.
 */
internal class TaskNames private constructor(
    private val varName: String
) {
    val copyLicense
        get() = "copy${varName}License"

    val copyLicenseIntoSourceArchive
        get() = "copy${varName}LicenseIntoSourceArchive"

    val copyMetainf
        get() = "copy${varName}Metainf"

    val generateCredits
        get() = "generate${varName}Credits"

    val generateMetadataFile
        get() = "generateMetadataFileFor${varName}Publication"

    val generateNotice
        get() = "generate${varName}Notice"

    val rezipSourceArchive
        get() = "rezip${varName}SourceArchive"

    val unzipSourceArchive
        get() = "unzip${varName}SourceArchive"

    val updateLibraryArchive
        get() = "update${varName}LibraryArchive"

    val zipSourceArchive
        get() = "source${varName}Jar"

    companion object {

        /**
         * Creates a new instance.
         *
         * @param  varName Variant name.
         * @return         The new object.
         */
        fun create(varName: String) =
            TaskNames(StringUtils.capitalize(varName))
    }
}