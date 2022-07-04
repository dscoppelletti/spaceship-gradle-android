package it.scoppelletti.spaceship.gradle.android.tasks

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import it.scoppelletti.spaceship.gradle.reflect.getResourceAsStream
import java.io.File
import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.io.TempDir

private const val DATABASE_NAME =
    "it/scoppelletti/spaceship/gradle/android/tasks/credits.xml"
private const val TEMPLATE_NAME =
    "it/scoppelletti/spaceship/gradle/android/tasks/credits_test.ftl"

class CreditsTaskTest {

    @Test
    fun testCredits(@TempDir tmpDir: File) {
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

        // GradleRunner spawns another process that cannot access to the
        // embedded resources in the test project.
        val templFile = File(tmpDir, "credits.ftl")
        templFile.outputStream().use { file ->
            getResourceAsStream(TEMPLATE_NAME)?.use { res ->
                var b = res.read()
                while (b > -1) {
                    file.write(b)
                    b = res.read()
                }
            } ?: run {
                assert(false) { "Fail to get resource $TEMPLATE_NAME" }
            }
        }

        File(tmpDir, "build.gradle").run {
            writeText("""
                plugins {
                    id 'com.android.application' version '7.2.1'
                    id 'it.scoppelletti.spaceship.android-app'
                }

                android {
                    namespace 'it.scoppelletti.sample'
                    compileSdk 31
                    defaultConfig {
                        applicationId 'it.scoppelletti.sample'
                        minSdk 21
                        targetSdk 31
                        versionCode 1
                        versionName '1.0.0'
                    }
                }
                
                dependencies {
                    implementation 'androidx.activity:activity:1.4.0'
                    implementation 'org.apache.commons:commons-lang3:3.12.0'
                    implementation 'com.google.android.exoplayer:exoplayer-core:2.16.1'
                    implementation 'com.google.android.exoplayer:exoplayer-ui:2.16.1'
                }
                
                tasks.create("testTask", ${CreditsTask::class.java.name}) {
                    variantName = "release"
                    databaseUrl = "${dbFile.toURI()}"
                    templateName = "${templFile.canonicalPath}"
                    outputDir = file("${tmpDir.path}")
                    creditsName = "credits.txt"
                }
                """.trimIndent()
            )
        }

        val result = GradleRunner.create()
            .withProjectDir(tmpDir)
            .withPluginClasspath()
            .withArguments("testTask")
            .build()
        result.task(":testTask")!!.outcome.shouldBe(TaskOutcome.SUCCESS)

        File(tmpDir, "credits.txt").bufferedReader().use { credits ->
            credits.readLine() shouldBe "Android Jetpack"
            credits.readLine() shouldBe "The Android Open Source Project"
            credits.readLine() shouldBe "Apache License, Version 2.0"
            credits.readLine() shouldBe "Android Platform"
            credits.readLine() shouldBe "The Android Open Source Project"
            credits.readLine() shouldBe
                    "Android Software Development Kit License"
            credits.readLine() shouldBe "Commons Lang"
            credits.readLine() shouldBe "Commons Lang Team"
            credits.readLine() shouldBe "Apache License, Version 2.0"
            credits.readLine() shouldBe "Exo Player"
            credits.readLine() shouldBe "Google Open Source"
            credits.readLine() shouldBe "Apache License, Version 2.0"
            credits.readLine() shouldBe "OpenJDK"
            credits.readLine() shouldBe "OpenJDK Community"
            credits.readLine() shouldBe "GPLv2 + Classpath Exception"
            credits.readLine().shouldBeNull()
        }
    }
}