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

import it.scoppelletti.spaceship.gradle.android.model.ArtifactElement
import it.scoppelletti.spaceship.gradle.android.model.CreditElement
import it.scoppelletti.spaceship.gradle.xml.newSAXParser
import it.scoppelletti.spaceship.gradle.xml.toMessage
import java.io.IOException
import org.gradle.tooling.BuildException
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException

/**
 * Credit database.
 *
 * @property credits Credit collection.
 */
internal class CreditDatabase private constructor(
    private val creditMap: Map<String, CreditElement>,
    private val artifactMap: Map<ArtifactElement, String>
) {

    val credits: Collection<CreditElement>
        get() = creditMap.values

    /**
     * Gets the credit corresponding to an artifact.
     *
     * @param  artifact Artifact.
     * @return          Credit.
     */
    operator fun get(artifact: ArtifactElement): CreditElement? {
        val key = artifactMap[artifact] ?: return null

        return creditMap[key] ?: throw NoSuchElementException(
            "No credit for key $key.")
    }

    companion object {

        /**
         * Loads a database.
         *
         * @param  url URL.
         * @return     The new object.
         */
        fun load(url: String): CreditDatabase {
            val saxParser = newSAXParser()
            val xmlHandler = XmlCreditsHandler()

            try {
                saxParser.parse(url, xmlHandler)
            } catch (ex: SAXParseException) {
                throw BuildException(ex.toMessage(), ex)
            } catch (ex: IOException) {
                throw BuildException(ex.message, ex)
            } catch (ex: SAXException) {
                throw BuildException(ex.message, ex)
            }

            return CreditDatabase(xmlHandler.creditMap ?: emptyMap(),
                xmlHandler.artifactMap ?: emptyMap())
        }
    }
}