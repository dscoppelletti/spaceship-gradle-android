package it.scoppelletti.spaceship.gradle.android.tasks

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import it.scoppelletti.spaceship.gradle.reflect.getResourceAsStream
import java.io.File
import kotlin.test.Test
import org.junit.jupiter.api.io.TempDir

private const val DATABASE_NAME =
    "it/scoppelletti/spaceship/gradle/android/tasks/credits.xml"

class CreditDatabaseTest {

    @Test
    fun testLoad(@TempDir tmpDir: File) {
        val dbFile = File(tmpDir, "credits.xml")

        dbFile.outputStream().use { file ->
            getResourceAsStream(DATABASE_NAME)?.use { res ->
                var b = res.read()
                while (b > -1) {
                    file.write(b)
                    b = res.read()
                }
            } ?: run {
                assert(false) { "Fail to get resource $DATABASE_NAME" }
            }
        }

        val database = CreditDatabase.load(dbFile.toURI().toString())
        val list = database.credits.sortedBy { it.key }
        list.size shouldBe 7

        list[0].let { credit ->
            credit.key shouldBe "androidJetpack"
            credit.force.shouldBeFalse()
            credit.component shouldBe "Android Jetpack"
            credit.owner.shouldNotBeNull().let { owner ->
                owner.key.shouldNotBeNull() shouldBe "android"
                owner.text.shouldNotBeNull() shouldBe
                        "The Android Open Source Project"
            }
            credit.license.shouldNotBeNull().let { license ->
                license.key.shouldNotBeNull() shouldBe "apache"
                license.text.shouldNotBeNull() shouldBe
                        "Apache License, Version 2.0"
            }
        }

        list[1].let { credit ->
            credit.key shouldBe "androidPlatform"
            credit.force.shouldBeTrue()
            credit.component shouldBe "Android Platform"
            credit.owner.shouldNotBeNull().let { owner ->
                owner.key.shouldNotBeNull() shouldBe "android"
                owner.text.shouldNotBeNull() shouldBe
                        "The Android Open Source Project"
            }
            credit.license.shouldNotBeNull().let { license ->
                license.key.shouldNotBeNull() shouldBe "android"
                license.text.shouldNotBeNull() shouldBe
                        "Android Software Development Kit License"
            }
        }

        list[2].let { credit ->
            credit.key shouldBe "commonsLang"
            credit.force.shouldBeFalse()
            credit.component shouldBe "Commons Lang"
            credit.owner.shouldNotBeNull().let { owner ->
                owner.key.shouldBeNull()
                owner.text.shouldNotBeNull() shouldBe "Commons Lang Team"
            }
            credit.license.shouldNotBeNull().let { license ->
                license.key.shouldNotBeNull() shouldBe "apache"
                license.text.shouldNotBeNull() shouldBe
                        "Apache License, Version 2.0"
            }
        }

        list[3].let { credit ->
            credit.key shouldBe "exoplayer"
            credit.force.shouldBeFalse()
            credit.component shouldBe "Exo Player"
            credit.owner.shouldNotBeNull().let { owner ->
                owner.key.shouldNotBeNull() shouldBe "googleOS"
                owner.text.shouldNotBeNull() shouldBe "Google Open Source"
            }
            credit.license.shouldNotBeNull().let { license ->
                license.key.shouldNotBeNull() shouldBe "apache"
                license.text.shouldNotBeNull() shouldBe
                        "Apache License, Version 2.0"
            }
        }

        list[4].let { credit ->
            credit.key shouldBe "glide"
            credit.force.shouldBeFalse()
            credit.component shouldBe "Glide"
            credit.owner.shouldNotBeNull().let { owner ->
                owner.key.shouldBeNull()
                owner.text.shouldNotBeNull() shouldBe "Bump Technologies"
            }
            credit.license.shouldNotBeNull().let { license ->
                license.key.shouldBeNull()
                license.text.shouldNotBeNull() shouldBe "BSD, MIT, Apache 2.0"
            }
        }

        list[5].let { credit ->
            credit.key shouldBe "materialComponents"
            credit.force.shouldBeFalse()
            credit.component shouldBe "Material Design Components for Android"
            credit.owner.shouldNotBeNull().let { owner ->
                owner.key.shouldNotBeNull() shouldBe "android"
                owner.text.shouldNotBeNull() shouldBe
                        "The Android Open Source Project"
            }
            credit.license.shouldNotBeNull().let { license ->
                license.key.shouldNotBeNull() shouldBe "apache"
                license.text.shouldNotBeNull() shouldBe
                        "Apache License, Version 2.0"
            }
        }

        list[6].let { credit ->
            credit.key shouldBe "openJDK"
            credit.force.shouldBeTrue()
            credit.component shouldBe "OpenJDK"
            credit.owner.shouldNotBeNull().let { owner ->
                owner.key.shouldBeNull()
                owner.text.shouldNotBeNull() shouldBe "OpenJDK Community"
            }
            credit.license.shouldNotBeNull().let { license ->
                license.key.shouldBeNull()
                license.text.shouldNotBeNull() shouldBe
                        "GPLv2 + Classpath Exception"
            }
        }
    }
}
