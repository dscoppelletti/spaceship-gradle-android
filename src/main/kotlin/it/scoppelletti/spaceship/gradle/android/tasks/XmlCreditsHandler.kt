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
import it.scoppelletti.spaceship.gradle.android.model.LicenseElement
import it.scoppelletti.spaceship.gradle.android.model.OwnerElement
import it.scoppelletti.spaceship.gradle.xml.SchemaEntityResolver
import it.scoppelletti.spaceship.gradle.xml.XmlDefaultHandler
import it.scoppelletti.spaceship.gradle.xml.XmlExt
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.SAXParseException

private const val COMPONENT_ELEMENT = "component"
private const val OWNER_REF = "ownerRef"
private const val LICENSE_REF = "licenseRef"
private const val OWNER_DATABASE = "owners"
private const val LICENSE_DATABASE = "licenses"
private const val KEY_ATTR = "key"
private const val KEYREF_ATTR = "keyref"
private const val FORCE_ATTR = "force"

/**
 * Credit database parser.
 */
internal class XmlCreditsHandler: XmlDefaultHandler(
    SchemaEntityResolver(CreditsTask.SCHEMA, CreditsTask.VERSION,
        CreditsTask.VERSION)
) {

    var creditMap: MutableMap<String, CreditElement>? = null
    var artifactMap: MutableMap<ArtifactElement, String>? = null
    private var currentCreditEl: CreditElement? = null
    private var currentOwnerEl: OwnerElement? = null
    private var currentLicenseEl: LicenseElement? = null
    private var ownerMap: MutableMap<String, OwnerElement>? = null
    private var licenseMap: MutableMap<String, LicenseElement>? = null

    override fun startDocument() {
        creditMap = mutableMapOf()
        artifactMap = mutableMapOf()
        currentCreditEl = null
        currentOwnerEl = null
        currentLicenseEl = null
        ownerMap = null
        licenseMap = null
    }

    override fun endDocument() {
        creditMap?.values?.forEach {
            checkCredit(it)
        }

        ownerMap = null
        licenseMap = null
    }

    override fun startElement(
        uri: String?,
        localName: String?,
        qName: String?,
        attributes: Attributes?
    ) {
        when (localName) {
            CreditElement.ELEMENT -> {
                val key = attributes?.getValue(KEY_ATTR)
                if (key.isNullOrBlank()) {
                    throw SAXParseException("Missing attribute $KEY_ATTR.",
                        locator)
                }

                val force = XmlExt.parseBoolean(attributes.getValue(FORCE_ATTR))
                currentCreditEl = CreditElement(key, force)
            }

            OWNER_DATABASE -> {
                ownerMap = mutableMapOf()
            }

            LICENSE_DATABASE -> {
                licenseMap = mutableMapOf()
            }

            COMPONENT_ELEMENT -> {
                collectContent()
            }

            OwnerElement.ELEMENT -> {
                if (ownerMap != null) {
                    val key = attributes?.getValue(KEY_ATTR)
                    if (key.isNullOrBlank()) {
                        throw SAXParseException("Missing attribute $KEY_ATTR.",
                            locator)
                    }

                    currentOwnerEl = OwnerElement(key)
                }

                collectContent()
            }

            LicenseElement.ELEMENT -> {
                if (licenseMap != null) {
                    val key = attributes?.getValue(KEY_ATTR)
                    if (key.isNullOrBlank()) {
                        throw SAXParseException("Missing attribute $KEY_ATTR.",
                            locator)
                    }

                    currentLicenseEl = LicenseElement(key)
                }

                collectContent()
            }

            OWNER_REF -> {
                val key = attributes?.getValue(KEYREF_ATTR)
                if (key.isNullOrBlank()) {
                    throw SAXParseException("Missing attribute $KEYREF_ATTR.",
                        locator)
                }

                currentCreditEl?.let {
                    it.owner = OwnerElement(key)
                } ?: run {
                    throw SAXParseException("""Element $OWNER_REF not inner
                        |element ${CreditElement.ELEMENT}.""".trimMargin()
                        .replace('\n', ' '), locator)
                }
            }

            LICENSE_REF -> {
                val key = attributes?.getValue(KEYREF_ATTR)
                if (key.isNullOrBlank()) {
                    throw SAXParseException("Missing attribute $KEYREF_ATTR.",
                        locator)
                }

                currentCreditEl?.let {
                    it.license = LicenseElement(key)
                } ?: run {
                    throw SAXParseException("""Element $LICENSE_REF not inner
                        |element ${CreditElement.ELEMENT}.
                        |""".trimMargin().replace('\n', ' '), locator)
                }
            }

            ArtifactElement.ELEMENT -> {
                val groupId = attributes?.getValue(ArtifactElement.GROUPID_ATTR)
                if (groupId.isNullOrBlank()) {
                    throw SAXParseException("""Missing attribute
                        |${ArtifactElement.GROUPID_ATTR}.
                        |""".trimMargin().replace('\n', ' '), locator)
                }

                val artifactId = attributes.getValue(
                    ArtifactElement.ARTIFACTID_ATTR)
                if (artifactId.isNullOrBlank()) {
                    throw SAXParseException("""Missing attribute
                        |${ArtifactElement.ARTIFACTID_ATTR}.
                        |""".trimMargin().replace('\n', ' '), locator)
                }

                currentCreditEl?.let {
                    artifactMap?.set(ArtifactElement(groupId, artifactId),
                        it.key)
                } ?: run {
                    throw SAXParseException("""Element
                        |${ArtifactElement.ELEMENT} not inner element
                        |${CreditElement.ELEMENT}.
                        |""".trimMargin().replace('\n', ' '), locator)
                }
            }
        }
    }

    override fun endElement(uri: String?, localName: String?, qName: String?) {
        when (localName) {
            CreditElement.ELEMENT -> {
                currentCreditEl?.let {
                    creditMap?.set(it.key, it)
                } ?: run {
                    throw SAXParseException("""End element
                        |${CreditElement.ELEMENT} not match start element.
                        |""".trimMargin().replace('\n', ' '), locator)
                }

                currentCreditEl = null
            }

            COMPONENT_ELEMENT -> {
                currentCreditEl?.let {
                    it.component = getCollectedContent()
                } ?: run {
                    throw SAXParseException("""Element $COMPONENT_ELEMENT not
                        |inner element ${CreditElement.ELEMENT}.
                        |""".trimMargin().replace('\n', ' '), locator)
                }
            }

            OwnerElement.ELEMENT -> {
                if (currentCreditEl != null)  {
                    currentCreditEl?.owner = OwnerElement(null,
                        getCollectedContent())
                } else if (ownerMap != null && currentOwnerEl != null) {
                    currentOwnerEl?.let {
                        it.text = getCollectedContent()
                        ownerMap?.set(it.key!!, it)
                    }

                    currentOwnerEl = null
                } else {
                    throw SAXParseException("""Unexepected end element 
                        |${OwnerElement.ELEMENT}.
                        |""".trimMargin().replace('\n', ' '), locator)
                }
            }

            LicenseElement.ELEMENT -> {
                if (currentCreditEl != null)  {
                    currentCreditEl?.license = LicenseElement(null,
                        getCollectedContent())
                } else if (licenseMap != null && currentLicenseEl != null) {
                    currentLicenseEl?.let {
                        it.text = getCollectedContent()
                        licenseMap?.set(it.key!!, it)
                    }

                    currentLicenseEl = null
                } else {
                    throw SAXParseException("""Unexepected end element 
                        |${LicenseElement.ELEMENT}.
                        |""".trimMargin().replace('\n', ' '), locator)
                }
            }
        }
    }

    private fun checkCredit(credit: CreditElement) {
        if (credit.component.isNullOrBlank()) {
            throw SAXException(
                "Component undefined for credit with key ${credit.key}.")
        }

        credit.owner?.let {
            resolveOwnerRef(credit, it)
        } ?: run {
            throw SAXException(
                "Owner undefined for credit with key ${credit.key}.")
        }

        credit.license?.let {
            resolveLicenseRef(credit, it)
        } ?: run {
            throw SAXException(
                "License undefined for credit with key ${credit.key}.")
        }
    }

    private fun resolveOwnerRef(credit: CreditElement, ownerRef: OwnerElement) {
        val key = ownerRef.key
        if (key.isNullOrBlank()) {
            // Inner owner
            return
        }

        val owner = ownerMap?.get(key) ?: throw SAXException(
            "Credit with key ${credit.key} refers to undefined owner key $key.")
        credit.owner = owner
    }

    private fun resolveLicenseRef(
        credit: CreditElement,
        licenseRef: LicenseElement
    ) {
        val key = licenseRef.key
        if (key.isNullOrBlank()) {
            // Inner license
            return
        }

        val license = licenseMap?.get(key) ?: throw SAXException("""Credit with
            |key ${credit.key} refers to undefined license key $key.
            |""".trimMargin().replace('\n', ' '))
        credit.license = license
    }
}