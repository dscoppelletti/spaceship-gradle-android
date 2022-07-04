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

package it.scoppelletti.spaceship.gradle.android.model

/**
 * Artifact.
 *
 * @since 1.0.0
 *
 * @property groupId    Group ID.
 * @property artifactId Artifact ID.
 */
public data class ArtifactElement(
    val groupId: String,
    val artifactId: String
) {

    override fun toString(): String = "$groupId:$artifactId"

    public companion object {

        /**
         * Name of the XML element.
         */
        public const val ELEMENT: String = "artifact"

        /**
         * Name of the XML `groupId` attribute.
         */
        public const val GROUPID_ATTR: String = "groupId"

        /**
         * Name of the XML `artifactId` attribute.
         */
        public const val ARTIFACTID_ATTR: String = "artifactId"
    }
}