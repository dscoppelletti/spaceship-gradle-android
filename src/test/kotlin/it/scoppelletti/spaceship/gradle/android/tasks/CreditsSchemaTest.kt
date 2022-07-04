package it.scoppelletti.spaceship.gradle.android.tasks

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import it.scoppelletti.spaceship.gradle.reflect.getResourceAsStream
import it.scoppelletti.spaceship.gradle.xml.SchemaEntityResolver
import it.scoppelletti.spaceship.gradle.xml.XmlDefaultHandler
import it.scoppelletti.spaceship.gradle.xml.newSAXParser
import kotlin.test.Test
import org.gradle.tooling.BuildException
import org.xml.sax.SAXParseException

const val PACKAGE = "it/scoppelletti/spaceship/gradle/android/tasks/"

class CreditsSchemaTest {

    @Test
    fun duplicateComponentKey() {
        val saxParser = newSAXParser()
        val ex = shouldThrow<BuildException> {
            getResourceAsStream("$PACKAGE/credits_duplicateComponentKey.xml")
                .use {
                    saxParser.parse(it, TestHandler)
                }
        }

        val innerEx = ex.cause
        innerEx.shouldBeInstanceOf<SAXParseException>()
        innerEx.message.shouldContain("""Duplicate key value [component1]
            |declared for identity constraint "creditKey" of element
            |"credits".""".trimMargin().replace('\n', ' '))
    }

    @Test
    fun duplicateArtifactKey() {
        val saxParser = newSAXParser()
        val ex = shouldThrow<BuildException> {
            getResourceAsStream("$PACKAGE/credits_duplicateArtifactKey.xml")
                .use {
                    saxParser.parse(it, TestHandler)
                }
        }

        val innerEx = ex.cause
        innerEx.shouldBeInstanceOf<SAXParseException>()
        innerEx.message.shouldContain("""Duplicate key value [group1,name1]
            |declared for identity constraint "artifactKey" of element
            |"credits".""".trimMargin().replace('\n', ' '))
    }

    @Test
    fun duplicateOwnerKey() {
        val saxParser = newSAXParser()
        val ex = shouldThrow<BuildException> {
            getResourceAsStream("$PACKAGE/credits_duplicateOwnerKey.xml")
                .use {
                    saxParser.parse(it, TestHandler)
                }
        }

        val innerEx = ex.cause
        innerEx.shouldBeInstanceOf<SAXParseException>()
        innerEx.message.shouldContain("""Duplicate key value [owner1]
            |declared for identity constraint "ownerKey" of element
            |"owners".""".trimMargin().replace('\n', ' '))
    }

    @Test
    fun duplicateLicenseKey() {
        val saxParser = newSAXParser()
        val ex = shouldThrow<BuildException> {
            getResourceAsStream("$PACKAGE/credits_duplicateLicenseKey.xml")
                .use {
                    saxParser.parse(it, TestHandler)
                }
        }

        val innerEx = ex.cause
        innerEx.shouldBeInstanceOf<SAXParseException>()
        innerEx.message.shouldContain("""Duplicate key value [license1]
            |declared for identity constraint "licenseKey" of element
            |"licenses".""".trimMargin().replace('\n', ' '))
    }

    @Test
    fun unknownOwnerRef() {
        val saxParser = newSAXParser()
        val ex = shouldThrow<BuildException> {
            getResourceAsStream("$PACKAGE/credits_unknownOwnerRef.xml")
                .use {
                    saxParser.parse(it, TestHandler)
                }
        }

        val innerEx = ex.cause
        innerEx.shouldBeInstanceOf<SAXParseException>()
        innerEx.message.shouldContain("""Key 'ownerKeyRef' with value 'owner2'
            |not found for identity constraint of element 'credits'."""
            .trimMargin().replace('\n', ' '))
    }

    @Test
    fun unknownLicenseRef() {
        val saxParser = newSAXParser()
        val ex = shouldThrow<BuildException> {
            getResourceAsStream("$PACKAGE/credits_unknownLicenseRef.xml")
                .use {
                    saxParser.parse(it, TestHandler)
                }
        }

        val innerEx = ex.cause
        innerEx.shouldBeInstanceOf<SAXParseException>()
        innerEx.message.shouldContain("""Key 'licenseKeyRef' with value
            |'license2' not found for identity constraint of element
            |'credits'.""".trimMargin().replace('\n', ' '))
    }

    @Test
    fun licenseKeyInOwnerRef() {
        val saxParser = newSAXParser()
        val ex = shouldThrow<BuildException> {
            getResourceAsStream("$PACKAGE/credits_licenseKeyInOwnerRef.xml")
                .use {
                    saxParser.parse(it, TestHandler)
                }
        }

        val innerEx = ex.cause
        innerEx.shouldBeInstanceOf<SAXParseException>()
        innerEx.message.shouldContain("""Key 'ownerKeyRef' with value 'license1'
            |not found for identity constraint of element 'credits'."""
            .trimMargin().replace('\n', ' '))
    }

    @Test
    fun ownerKeyInLicenseRef() {
        val saxParser = newSAXParser()
        val ex = shouldThrow<BuildException> {
            getResourceAsStream("$PACKAGE/credits_ownerKeyInLicenseRef.xml")
                .use {
                    saxParser.parse(it, TestHandler)
                }
        }

        val innerEx = ex.cause
        innerEx.shouldBeInstanceOf<SAXParseException>()
        innerEx.message.shouldContain("""Key 'licenseKeyRef' with value 'owner2'
            |not found for identity constraint of element 'credits'."""
            .trimMargin().replace('\n', ' '))
    }
}

private object TestHandler: XmlDefaultHandler(
    SchemaEntityResolver(CreditsTask.SCHEMA, CreditsTask.VERSION,
        CreditsTask.VERSION)
)