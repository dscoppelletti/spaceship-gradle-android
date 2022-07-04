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
 * Credit
 *
 * @since 1.0.0
 *
 * @property key       Key.
 * @property force     Whether this component must be cited regardless the using
 *                     of its artifacts.
 * @property component Component.
 * @property owner     Owner.
 * @property license   License.
 */
public data class CreditElement(
    public val key: String,
    public val force: Boolean,
    public var component: String? = null,
    public var owner: OwnerElement? = null,
    public var license: LicenseElement? = null
) {

    override fun equals(other: Any?): Boolean {
        val op = other as? CreditElement ?: return false

        return (key == op.key)
    }

    override fun hashCode(): Int = key.hashCode()

    public companion object {

        /**
         * Name of the XML element.
         */
        public const val ELEMENT: String = "credit"
    }
}