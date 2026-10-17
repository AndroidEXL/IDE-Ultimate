package com.androidexl.ideultimate.data

import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds a ready-to-open Android Studio project (Gradle KTS + Compose) from a
 * workspace project name and returns it as an in-memory ZIP archive.
 */
object ProjectZipExporter {

    fun suggestedFileName(projectName: String): String = "${safeDirectoryName(projectName)}.zip"

    fun buildZip(projectName: String): ByteArray {
        val displayName = projectName.trim().ifBlank { "Project" }
        val directory = safeDirectoryName(displayName)
        val packageName = "com.ideultimate.projects.${packageSlug(displayName)}"

        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            zip.add("$directory/settings.gradle.kts", settingsGradle(displayName))
            zip.add("$directory/build.gradle.kts", rootBuildGradle)
            zip.add("$directory/gradle.properties", gradleProperties)
            zip.add("$directory/.gitignore", gitignore)
            zip.add("$directory/app/build.gradle.kts", appBuildGradle(packageName))
            zip.add("$directory/app/src/main/AndroidManifest.xml", manifest())
            zip.add(
                "$directory/app/src/main/java/${packageName.replace('.', '/')}/MainActivity.kt",
                mainActivity(packageName, displayName),
            )
            zip.add("$directory/app/src/main/res/values/strings.xml", stringsXml(displayName))
            zip.add("$directory/app/src/main/res/values/themes.xml", themesXml)
            zip.add("$directory/README.md", readme(displayName))
        }
        return output.toByteArray()
    }

    private fun ZipOutputStream.add(path: String, content: String) {
        putNextEntry(ZipEntry(path))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun packageSlug(name: String): String {
        val builder = StringBuilder()
        name.lowercase(Locale.ROOT).forEach { character ->
            if (character in 'a'..'z' || character in '0'..'9') builder.append(character)
        }
        val slug = builder.toString()
        return when {
            slug.isEmpty() -> "app"
            slug.first().isDigit() -> "app$slug"
            else -> slug
        }
    }

    private fun safeDirectoryName(name: String): String {
        val cleaned = buildString {
            name.trim().forEach { character ->
                append(
                    when {
                        character.isLetterOrDigit() -> character
                        character == '-' || character == '_' -> character
                        character == ' ' -> ' '
                        else -> '_'
                    },
                )
            }
        }
        val collapsed = cleaned.split(' ')
            .filter { it.isNotBlank() }
            .joinToString("-")
        return if (collapsed.any { it.isLetterOrDigit() }) collapsed else "project"
    }

    private fun escapeXml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun settingsGradle(name: String) = """
        pluginManagement {
            repositories {
                google()
                mavenCentral()
                gradlePluginPortal()
            }
        }

        dependencyResolutionManagement {
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
            }
        }

        rootProject.name = "${escapeKotlin(name)}"
        include(":app")
    """.trimIndent()

    private val rootBuildGradle = """
        plugins {
            id("com.android.application") version "8.9.2" apply false
            id("org.jetbrains.kotlin.android") version "2.1.20" apply false
            id("org.jetbrains.kotlin.plugin.compose") version "2.1.20" apply false
        }
    """.trimIndent()

    private val gradleProperties = """
        org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
        android.useAndroidX=true
        kotlin.code.style=official
    """.trimIndent()

    private val gitignore = """
        .gradle/
        build/
        local.properties
        .idea/
        *.iml
        captures/
    """.trimIndent()

    private fun appBuildGradle(packageName: String) = """
        plugins {
            id("com.android.application")
            id("org.jetbrains.kotlin.android")
            id("org.jetbrains.kotlin.plugin.compose")
        }

        android {
            namespace = "$packageName"
            compileSdk = 35

            defaultConfig {
                applicationId = "$packageName"
                minSdk = 24
                targetSdk = 35
                versionCode = 1
                versionName = "1.0"
            }

            buildFeatures {
                compose = true
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            kotlinOptions {
                jvmTarget = "17"
            }
        }

        dependencies {
            val composeBom = platform("androidx.compose:compose-bom:2025.04.01")
            implementation(composeBom)
            implementation("androidx.core:core-ktx:1.16.0")
            implementation("androidx.activity:activity-compose:1.10.1")
            implementation("androidx.compose.ui:ui")
            implementation("androidx.compose.material3:material3")
            debugImplementation("androidx.compose.ui:ui-tooling")
        }
    """.trimIndent()

    private fun manifest() = """
        <?xml version="1.0" encoding="utf-8"?>
        <manifest xmlns:android="http://schemas.android.com/apk/res/android">

            <application
                android:allowBackup="true"
                android:label="@string/app_name"
                android:supportsRtl="true"
                android:theme="@style/Theme.ExportedProject">

                <activity
                    android:name=".MainActivity"
                    android:exported="true">
                    <intent-filter>
                        <action android:name="android.intent.action.MAIN" />
                        <category android:name="android.intent.category.LAUNCHER" />
                    </intent-filter>
                </activity>
            </application>

        </manifest>
    """.trimIndent()

    private fun mainActivity(packageName: String, displayName: String) = """
        package $packageName

        import android.os.Bundle
        import androidx.activity.ComponentActivity
        import androidx.activity.compose.setContent
        import androidx.compose.foundation.layout.Box
        import androidx.compose.foundation.layout.fillMaxSize
        import androidx.compose.material3.MaterialTheme
        import androidx.compose.material3.Surface
        import androidx.compose.material3.Text
        import androidx.compose.ui.Alignment
        import androidx.compose.ui.Modifier

        class MainActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContent {
                    MaterialTheme {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(text = "Hello from ${escapeKotlin(displayName)}")
                            }
                        }
                    }
                }
            }
        }
    """.trimIndent()

    private fun stringsXml(name: String) = """
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="app_name">${escapeXml(name)}</string>
        </resources>
    """.trimIndent()

    private val themesXml = """
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <style name="Theme.ExportedProject" parent="android:Theme.Material.Light.NoActionBar" />
        </resources>
    """.trimIndent()

    private fun readme(name: String) = """
        # $name

        تم تصدير هذا المشروع من **IDE Ultimate**.

        ## التشغيل

        1. افتح المجلد في Android Studio (Ladybug أو أحدث).
        2. اترك Gradle يقوم بمزامنة المشروع (Sync).
        3. شغّل التطبيق على محاكي أو جهاز حقيقي.

        ## البنية

        - `app/src/main/java/` — كود Kotlin (نقطة البداية: `MainActivity.kt`).
        - `app/src/main/res/` — الموارد (النصوص والثيمات).

        Built with Kotlin and Jetpack Compose.
    """.trimIndent()

    private fun escapeKotlin(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\$", "\\\$")
}
