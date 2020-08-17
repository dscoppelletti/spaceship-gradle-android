package it.scoppelletti.spaceship.gradle.android.tasks;

import java.io.InputStream;
import javax.xml.parsers.SAXParser;
import it.scoppelletti.spaceship.gradle.reflect.ReflectionExt;
import it.scoppelletti.spaceship.gradle.xml.SchemaEntityResolver;
import it.scoppelletti.spaceship.gradle.xml.XmlDefaultHandler;
import it.scoppelletti.spaceship.gradle.xml.XmlExt;
import org.gradle.tooling.BuildException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXParseException;

@SuppressWarnings("RedundantThrows")
public class CreditsSchemaTest {
    private static final String PACKAGE =
            "it/scoppelletti/spaceship/gradle/android/tasks/";
    private SAXParser saxParser;
    private XmlDefaultHandler xmlHandler;

    @Before
    public void setUp() {
        saxParser = XmlExt.newSAXParser();
        xmlHandler = new CreditsSchemaTest.TestHandler();
        xmlHandler.setEntityResolver(new SchemaEntityResolver(
                CreditDatabase.SCHEMA, CreditDatabase.VERSION,
                CreditDatabase.VERSION));
    }

    @Test
    public void duplicateComponentKey() throws Exception {
        Exception ex;
        Throwable innerEx;

        ex = Assert.assertThrows("exception", BuildException.class, () -> {
            try (InputStream stream = ReflectionExt.getResourceAsStream(
                    CreditsSchemaTest.PACKAGE.concat(
                            "/credits_duplicateComponentKey.xml"))) {
                saxParser.parse(stream, xmlHandler);
            }
        });

        innerEx = ex.getCause();
        Assert.assertTrue("cause", innerEx instanceof SAXParseException);
        Assert.assertTrue("message", innerEx.getMessage().contains(
                "Duplicate key value [component1] declared for identity " +
                        "constraint \"creditKey\" of element \"credits\""));
    }

    @Test
    public void duplicateArtifactKey() throws Exception {
        Exception ex;
        Throwable innerEx;

        ex = Assert.assertThrows("exception", BuildException.class, () -> {
            try (InputStream stream = ReflectionExt.getResourceAsStream(
                    CreditsSchemaTest.PACKAGE.concat(
                            "/credits_duplicateArtifactKey.xml"))) {
                saxParser.parse(stream, xmlHandler);
            }
        });

        innerEx = ex.getCause();
        Assert.assertTrue("cause", innerEx instanceof SAXParseException);
        Assert.assertTrue("message", innerEx.getMessage().contains(
                "Duplicate key value [group1,name1] declared for identity " +
                        "constraint \"artifactKey\" of element \"credits\""));
    }

    @Test
    public void duplicateOwnerKey() throws Exception {
        Exception ex;
        Throwable innerEx;

        ex = Assert.assertThrows("exception", BuildException.class, () -> {
            try (InputStream stream = ReflectionExt.getResourceAsStream(
                    CreditsSchemaTest.PACKAGE.concat(
                            "/credits_duplicateOwnerKey.xml"))) {
                saxParser.parse(stream, xmlHandler);
            }
        });

        innerEx = ex.getCause();
        Assert.assertTrue("cause", innerEx instanceof SAXParseException);
        Assert.assertTrue("message", innerEx.getMessage().contains(
                "Duplicate key value [owner1] declared for identity " +
                        "constraint \"ownerKey\" of element \"owners\""));
    }

    @Test
    public void duplicateLicenseKey() throws Exception {
        Exception ex;
        Throwable innerEx;

        ex = Assert.assertThrows("exception", BuildException.class, () -> {
            try (InputStream stream = ReflectionExt.getResourceAsStream(
                    CreditsSchemaTest.PACKAGE.concat(
                            "/credits_duplicateLicenseKey.xml"))) {
                saxParser.parse(stream, xmlHandler);
            }
        });

        innerEx = ex.getCause();
        Assert.assertTrue("cause", innerEx instanceof SAXParseException);
        Assert.assertTrue("message", innerEx.getMessage().contains(
                "Duplicate key value [license1] declared for identity " +
                        "constraint \"licenseKey\" of element \"licenses\""));
    }

    @Test
    public void unknownOwnerRef() throws Exception {
        Exception ex;
        Throwable innerEx;

        ex = Assert.assertThrows("exception", BuildException.class, () -> {
            try (InputStream stream = ReflectionExt.getResourceAsStream(
                    CreditsSchemaTest.PACKAGE.concat(
                            "/credits_unknownOwnerRef.xml"))) {
                saxParser.parse(stream, xmlHandler);
            }
        });

        innerEx = ex.getCause();
        Assert.assertTrue("cause", innerEx instanceof SAXParseException);
        Assert.assertTrue("message", innerEx.getMessage().contains(
                "Key 'ownerKeyRef' with value 'owner2' not found for " +
                        "identity constraint of element 'credits'"));
    }

    @Test
    public void unknownLicenseRef() throws Exception {
        Exception ex;
        Throwable innerEx;

        ex = Assert.assertThrows("exception", BuildException.class, () -> {
            try (InputStream stream = ReflectionExt.getResourceAsStream(
                    CreditsSchemaTest.PACKAGE.concat(
                            "/credits_unknownLicenseRef.xml"))) {
                saxParser.parse(stream, xmlHandler);
            }
        });

        innerEx = ex.getCause();
        Assert.assertTrue("cause", innerEx instanceof SAXParseException);
        Assert.assertTrue("message", innerEx.getMessage().contains(
                "Key 'licenseKeyRef' with value 'license2' not found for " +
                        "identity constraint of element 'credits'"));
    }

    @Test
    public void licenseKeyInOwnerRef() throws Exception {
        Exception ex;
        Throwable innerEx;

        ex = Assert.assertThrows("exception", BuildException.class, () -> {
            try (InputStream stream = ReflectionExt.getResourceAsStream(
                    CreditsSchemaTest.PACKAGE.concat(
                            "/credits_licenseKeyInOwnerRef.xml"))) {
                saxParser.parse(stream, xmlHandler);
            }
        });

        innerEx = ex.getCause();
        Assert.assertTrue("cause", innerEx instanceof SAXParseException);
        Assert.assertTrue("message", innerEx.getMessage().contains(
                "Key 'ownerKeyRef' with value 'license1' not found for " +
                        "identity constraint of element 'credits'"));
    }

    @Test
    public void ownerKeyInLicenseRef() throws Exception {
        Exception ex;
        Throwable innerEx;

        ex = Assert.assertThrows("exception", BuildException.class, () -> {
            try (InputStream stream = ReflectionExt.getResourceAsStream(
                    CreditsSchemaTest.PACKAGE.concat(
                            "/credits_ownerKeyInLicenseRef.xml"))) {
                saxParser.parse(stream, xmlHandler);
            }
        });

        innerEx = ex.getCause();
        Assert.assertTrue("cause", innerEx instanceof SAXParseException);
        Assert.assertTrue("message", innerEx.getMessage().contains(
                "Key 'licenseKeyRef' with value 'owner2' not found for " +
                        "identity constraint of element 'credits'"));
    }

    private static class TestHandler extends XmlDefaultHandler {

        TestHandler() {
        }
    }
}
