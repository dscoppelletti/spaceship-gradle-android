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

package it.scoppelletti.spaceship.gradle.android.tasks;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.SAXParser;
import it.scoppelletti.spaceship.gradle.android.model.CreditItem;
import it.scoppelletti.spaceship.gradle.android.model.LicenseItem;
import it.scoppelletti.spaceship.gradle.android.model.OwnerItem;
import it.scoppelletti.spaceship.gradle.xml.SchemaEntityResolver;
import it.scoppelletti.spaceship.gradle.xml.XmlDefaultHandler;
import it.scoppelletti.spaceship.gradle.xml.XmlExt;
import org.apache.commons.lang3.StringUtils;
import org.gradle.tooling.BuildException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Credit database.
 */
final class CreditDatabase {
    static final String SCHEMA = "spaceship/gradle/android/tasks/credits";
    static final int VERSION = 1;

    private final Map<String, CreditItem> myCreditMap;
    private final Map<ArtifactItem, String> myArtifactMap;

    /**
     * Constructor.
     *
     * @param creditMap   Credit collection.
     * @param artifactMap Artifact collection.
     */
    private CreditDatabase(Map<String, CreditItem> creditMap,
            Map<ArtifactItem, String> artifactMap) {
        myCreditMap = Objects.requireNonNull(creditMap,
                "Argument creditMap is null.");
        myArtifactMap = Objects.requireNonNull(artifactMap,
                "Argument artifactMap is null.");
    }

    /**
     * Gets the credit corresponding to an artifact.
     *
     * @param  artifact Artifact.
     * @return          Credit. If no credit corresponds to the artifact,
     *                  returns {@code null}.
     */
    @Nullable
    CreditItem getCreditByArtifact(ArtifactItem artifact) {
        String key;
        CreditItem credit;

        Objects.requireNonNull(artifact, "Argument artifact is null.");

        key = myArtifactMap.get(artifact);
        if (key == null) {
            return null;
        }

        credit = Objects.requireNonNull(myCreditMap.get(key), () ->
                String.format("No credit for key %1$s.", key));
        return credit;
    }

    /**
     * Loads a database.
     *
     * @param  url Database URL.
     * @return     The new object.
     */
    static CreditDatabase load(@Nonnull URI url) {
        SAXParser saxParser;
        CreditDatabase.XmlHandler xmlHandler;

        Objects.requireNonNull(url, "Argument url is null.");

        saxParser = XmlExt.newSAXParser();
        xmlHandler = new CreditDatabase.XmlHandler();
        xmlHandler.setEntityResolver(new SchemaEntityResolver(
                CreditDatabase.SCHEMA, CreditDatabase.VERSION,
                CreditDatabase.VERSION));

        try {
            saxParser.parse(url.toString(), xmlHandler);
        } catch (SAXParseException ex) {
            throw new BuildException(XmlExt.toString(ex), ex);
        } catch (IOException|SAXException ex) {
            throw new BuildException(ex.getMessage(), ex);
        }

        return new CreditDatabase(xmlHandler.creditMap,
                xmlHandler.artifactMap);
    }

    private static class XmlHandler extends XmlDefaultHandler {
        private static final String COMPONENT_ELEMENT = "component";
        private static final String OWNER_REF = "ownerRef";
        private static final String LICENSE_REF = "licenseRef";
        private static final String OWNER_DATABASE = "owners";
        private static final String LICENSE_DATABASE = "licenses";
        private static final String KEY_ATTR = "key";
        private static final String KEYREF_ATTR = "keyref";

        Map<String, CreditItem> creditMap;
        Map<ArtifactItem, String> artifactMap;
        private CreditItem currentCreditItem;
        private OwnerItem currentOwnerItem;
        private LicenseItem currentLicenseItem;
        private Map<String, OwnerItem> ownerMap;
        private Map<String, LicenseItem> licenseMap;

        /**
         * Sole constructor.
         */
        XmlHandler() {
        }

        @Override
        @SuppressWarnings("RedundantThrows")
        public void startDocument() throws SAXException {
            creditMap = new HashMap<>();
            artifactMap = new HashMap<>();
            currentCreditItem = null;
            currentOwnerItem = null;
            currentLicenseItem = null;
            ownerMap = null;
            licenseMap = null;
        }

        @Override
        public void endDocument() throws SAXException {
            for (CreditItem credit : creditMap.values()) {
                checkCredit(credit);
            }

            ownerMap = null;
            licenseMap = null;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            String artifactId, groupId, key;
            ArtifactItem artifact;
            LicenseItem license;
            OwnerItem owner;

            switch (localName) {
            case CreditItem.ELEMENT:
                key = attributes.getValue(XmlHandler.KEY_ATTR);
                if (StringUtils.isBlank(key)) {
                    throw new SAXParseException(String.format(
                            "Missing attribute %1$s.", XmlHandler.KEY_ATTR),
                            getDocumentLocator());
                }

                currentCreditItem = new CreditItem(key);
                break;

            case XmlHandler.OWNER_DATABASE:
                ownerMap = new HashMap<>();
                break;

            case XmlHandler.LICENSE_DATABASE:
                licenseMap = new HashMap<>();
                break;

            case XmlHandler.COMPONENT_ELEMENT:
                collectContent();
                break;

            case OwnerItem.ELEMENT:
                if (ownerMap != null) {
                    // Owner database
                    key = attributes.getValue(XmlHandler.KEY_ATTR);
                    if (StringUtils.isBlank(key)) {
                        throw new SAXParseException(String.format(
                                "Missing attribute %1$s.", XmlHandler.KEY_ATTR),
                                getDocumentLocator());
                    }

                    currentOwnerItem = new OwnerItem(key);
                }

                collectContent();
                break;

            case LicenseItem.ELEMENT:
                if (licenseMap != null) {
                    // License database
                    key = attributes.getValue(XmlHandler.KEY_ATTR);
                    if (StringUtils.isBlank(key)) {
                        throw new SAXParseException(String.format(
                                "Missing attribute %1$s.", XmlHandler.KEY_ATTR),
                                getDocumentLocator());
                    }

                    currentLicenseItem = new LicenseItem(key);
                }

                collectContent();
                break;

            case XmlHandler.OWNER_REF:
                if (currentCreditItem == null) {
                    throw new SAXParseException(String.format(
                            "Element %2$s not inner element %1$s.",
                            CreditItem.ELEMENT, XmlHandler.OWNER_REF),
                            getDocumentLocator());
                }

                key = attributes.getValue(XmlHandler.KEYREF_ATTR);
                if (StringUtils.isBlank(key)) {
                    throw new SAXParseException(String.format(
                            "Missing attribute %1$s.", XmlHandler.KEYREF_ATTR),
                            getDocumentLocator());
                }

                owner = new OwnerItem(key);
                currentCreditItem.setOwner(owner);
                break;

            case XmlHandler.LICENSE_REF:
                if (currentCreditItem == null) {
                    throw new SAXParseException(String.format(
                            "Element %2$s not inner element %1$s.",
                            CreditItem.ELEMENT, XmlHandler.LICENSE_REF),
                            getDocumentLocator());
                }

                key = attributes.getValue(XmlHandler.KEYREF_ATTR);
                if (StringUtils.isBlank(key)) {
                    throw new SAXParseException(String.format(
                            "Missing attribute %1$s.", XmlHandler.KEYREF_ATTR),
                            getDocumentLocator());
                }

                license = new LicenseItem(key);
                currentCreditItem.setLicense(license);
                break;

            case ArtifactItem.ELEMENT:
                if (currentCreditItem == null) {
                    throw new SAXParseException(String.format(
                            "Element %2$s not inner element %1$s.",
                            CreditItem.ELEMENT, ArtifactItem.ELEMENT),
                            getDocumentLocator());
                }

                groupId = attributes.getValue(ArtifactItem.GROUPID_ATTR);
                if (StringUtils.isBlank(groupId)) {
                    throw new SAXParseException(String.format(
                            "Missing attribute %1$s.",
                            ArtifactItem.GROUPID_ATTR), getDocumentLocator());
                }

                artifactId = attributes.getValue(ArtifactItem.ARTIFACTID_ATTR);
                if (StringUtils.isBlank(groupId)) {
                    throw new SAXParseException(String.format(
                            "Missing attribute %1$s.",
                            ArtifactItem.ARTIFACTID_ATTR),
                            getDocumentLocator());
                }

                artifact = new ArtifactItem(groupId, artifactId);
                artifactMap.put(artifact, currentCreditItem.getKey());
                break;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            LicenseItem license;
            OwnerItem owner;

            switch (localName) {
            case CreditItem.ELEMENT:
                if (currentCreditItem == null) {
                    throw new SAXParseException(String.format(
                            "End element %1$s not match start element.",
                            CreditItem.ELEMENT), getDocumentLocator());
                }

                creditMap.put(currentCreditItem.getKey(), currentCreditItem);
                currentCreditItem = null;
                break;

            case XmlHandler.COMPONENT_ELEMENT:
                if (currentCreditItem == null) {
                    throw new SAXParseException(String.format(
                            "Element %2$s not inner element %1$s.",
                            CreditItem.ELEMENT, XmlHandler.COMPONENT_ELEMENT),
                            getDocumentLocator());
                }

                currentCreditItem.setComponent(getCollectedContent());
                break;

            case OwnerItem.ELEMENT:
                if (currentCreditItem != null) {
                    // Credit database
                    owner = new OwnerItem(null);
                    owner.setText(getCollectedContent());
                    currentCreditItem.setOwner(owner);
                } else if (ownerMap != null && currentOwnerItem != null) {
                    // Owner database
                    currentOwnerItem.setText(getCollectedContent());
                    ownerMap.put(currentOwnerItem.getKey(), currentOwnerItem);
                    currentOwnerItem = null;
                } else {
                    throw new SAXParseException(String.format(
                            "Unexpected end element %1$s.", OwnerItem.ELEMENT),
                            getDocumentLocator());
                }
                break;

            case LicenseItem.ELEMENT:
                if (currentCreditItem != null) {
                    license = new LicenseItem(null);
                    license.setText(getCollectedContent());
                    currentCreditItem.setLicense(license);
                } else if (licenseMap != null && currentLicenseItem != null) {
                    currentLicenseItem.setText(getCollectedContent());
                    licenseMap.put(currentLicenseItem.getKey(),
                            currentLicenseItem);
                    currentLicenseItem = null;
                } else {
                    throw new SAXParseException(String.format(
                            "Unexpected end element %1$s.",
                            LicenseItem.ELEMENT), getDocumentLocator());
                }
                break;
            }
        }

        private void checkCredit(CreditItem credit) throws SAXException {
            LicenseItem license;
            OwnerItem owner;

            if (StringUtils.isBlank(credit.getComponent())) {
                throw new SAXException(String.format(
                        "Component undefined for credit with key %1$s.",
                        credit.getKey()));
            }

            owner = credit.getOwner();
            if (owner == null) {
                throw new SAXException(String.format(
                        "Owner undefined for credit with key %1$s.",
                        credit.getKey()));
            }

            if (ownerMap != null) {
                resolveOwnerRef(credit, owner);
            }

            license = credit.getLicense();
            if (license == null) {
                throw new SAXException(String.format(
                        "License undefined for credit with key %1$s.",
                        credit.getKey()));
            }

            if (licenseMap != null) {
                resolveLicenseRef(credit, license);
            }
        }

        private void resolveOwnerRef(CreditItem credit, OwnerItem owner) throws
                SAXException {
            String key;

            key = owner.getKey();
            if (key == null) {
                // Inner owner
                return;
            }

            // Owner reference
            owner = ownerMap.get(key);
            if (owner == null) {
                throw new SAXException(String.format("Credit with key %1$s "+
                                "refers to undefined owner key %2$s.",
                        credit.getKey(), key));
            }

            credit.setOwner(owner);
        }

        private void resolveLicenseRef(CreditItem credit, LicenseItem license)
                throws SAXException {
            String key;

            key = license.getKey();
            if (key == null) {
                // Inner license
                return;
            }

            // License reference
            license = licenseMap.get(key);
            if (license == null) {
                throw new SAXException(String.format("Credit with key %1$s" +
                        "refers to undefined license key %2$s.",
                        credit.getKey(), key));
            }

            credit.setLicense(license);
        }
    }
}
