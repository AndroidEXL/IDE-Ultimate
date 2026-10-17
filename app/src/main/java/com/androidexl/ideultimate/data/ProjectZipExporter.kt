package com.androidexl.ideultimate.data

import java.io.ByteArrayOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Builds a ready-to-open project archive (Kotlin/Compose, Java Android, or Flutter)
 * from a workspace project and returns it as an in-memory ZIP archive.
 */
object ProjectZipExporter {

    fun suggestedFileName(projectName: String): String = "${directoryName(projectName)}.zip"

    /** Folder name used at the root of the exported ZIP. */
    fun directoryName(projectName: String): String =
        safeDirectoryName(projectName.trim().ifBlank { "Project" })

    /** Default application id / package of a generated project. */
    fun packageName(projectName: String): String =
        "com.ideultimate.projects.${packageSlug(projectName.trim().ifBlank { "Project" })}"

    /** Validates whether a user-entered package name is syntactically valid. */
    fun isValidPackageName(raw: String): Boolean {
        val trimmed = raw.trim()
        if (trimmed.length < 3 || !trimmed.contains('.')) return false
        val parts = trimmed.split('.')
        if (parts.size < 2) return false
        return parts.all { part ->
            part.isNotEmpty() &&
                part.first() in 'a'..'z' &&
                part.all { ch -> ch in 'a'..'z' || ch in '0'..'9' || ch == '_' }
        }
    }

    fun buildZip(item: WorkspaceItem): ByteArray = buildZip(
        projectName = item.name,
        customPackageName = item.packageName,
        language = item.language,
        versionCode = item.versionCode,
        versionName = item.versionName,
    )

    fun buildZip(
        projectName: String,
        customPackageName: String? = null,
        language: ProjectLanguage = ProjectLanguage.KOTLIN,
        versionCode: Int = 1,
        versionName: String = "1.0",
    ): ByteArray {
        val displayName = projectName.trim().ifBlank { "Project" }
        val directory = directoryName(displayName)
        val appPackage = customPackageName?.trim()?.takeIf { isValidPackageName(it) }
            ?: packageName(displayName)
        val safeVersionCode = versionCode.coerceAtLeast(1)
        val safeVersionName = versionName.trim().ifBlank { "1.0.0" }

        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            when (language) {
                ProjectLanguage.FLUTTER -> {
                    zip.add("$directory/pubspec.yaml", flutterPubspec(directory, displayName, safeVersionName, safeVersionCode))
                    zip.add("$directory/lib/main.dart", flutterMainDart(displayName, appPackage, safeVersionName))
                    zip.add("$directory/android/app/build.gradle.kts", flutterAndroidBuildGradle(appPackage, safeVersionCode, safeVersionName))
                    zip.add("$directory/android/app/src/main/AndroidManifest.xml", manifest())
                    zip.add("$directory/.gitignore", flutterGitignore)
                    zip.add("$directory/README.md", readme(displayName, language, appPackage, safeVersionName, safeVersionCode))
                }
                ProjectLanguage.JAVA -> {
                    zip.add("$directory/settings.gradle.kts", settingsGradle(displayName))
                    zip.add("$directory/build.gradle.kts", rootJavaBuildGradle)
                    zip.add("$directory/gradle.properties", gradleProperties)
                    zip.add("$directory/.gitignore", gitignore)
                    zip.add("$directory/app/build.gradle.kts", javaAppBuildGradle(appPackage, safeVersionCode, safeVersionName))
                    zip.add("$directory/app/src/main/AndroidManifest.xml", manifest())
                    zip.add(
                        "$directory/app/src/main/java/${appPackage.replace('.', '/')}/MainActivity.java",
                        javaMainActivity(appPackage, displayName),
                    )
                    zip.add("$directory/app/src/main/res/values/strings.xml", stringsXml(displayName))
                    zip.add("$directory/app/src/main/res/values/themes.xml", themesXml)
                    zip.add("$directory/README.md", readme(displayName, language, appPackage, safeVersionName, safeVersionCode))
                }
                ProjectLanguage.KOTLIN -> {
                    zip.add("$directory/settings.gradle.kts", settingsGradle(displayName))
                    zip.add("$directory/build.gradle.kts", rootBuildGradle)
                    zip.add("$directory/gradle.properties", gradleProperties)
                    zip.add("$directory/.gitignore", gitignore)
                    zip.add("$directory/app/build.gradle.kts", appBuildGradle(appPackage, safeVersionCode, safeVersionName))
                    zip.add("$directory/app/src/main/AndroidManifest.xml", manifest())
                    zip.add(
                        "$directory/app/src/main/java/${appPackage.replace('.', '/')}/MainActivity.kt",
                        mainActivity(appPackage, displayName),
                    )
                    zip.add("$directory/app/src/main/res/values/strings.xml", stringsXml(displayName))
                    zip.add("$directory/app/src/main/res/values/themes.xml", themesXml)
                    zip.add("$directory/README.md", readme(displayName, language, appPackage, safeVersionName, safeVersionCode))
                }
            }
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

    private val rootJavaBuildGradle = """
        plugins {
            id("com.android.application") version "8.9.2" apply false
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

    private val flutterGitignore = """
        .dart_tool/
        .packages
        build/
        .idea/
        *.iml
    """.trimIndent()

    private fun appBuildGradle(packageName: String, versionCode: Int, versionName: String) = """
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
                versionCode = $versionCode
                versionName = "${escapeKotlin(versionName)}"
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

    private fun javaAppBuildGradle(packageName: String, versionCode: Int, versionName: String) = """
        plugins {
            id("com.android.application")
        }

        android {
            namespace = "$packageName"
            compileSdk = 35

            defaultConfig {
                applicationId = "$packageName"
                minSdk = 24
                targetSdk = 35
                versionCode = $versionCode
                versionName = "${escapeKotlin(versionName)}"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }

        dependencies {
            implementation("androidx.appcompat:appcompat:1.7.0")
        }
    """.trimIndent()

    private fun flutterPubspec(slug: String, displayName: String, versionName: String, versionCode: Int): String {
        val dartSlug = packageSlug(slug)
        return """
            name: $dartSlug
            description: "${escapeKotlin(displayName)} — generated by IDE Ultimate"
            publish_to: "none"
            version: $versionName+$versionCode

            environment:
              sdk: ">=3.4.0 <4.0.0"

            dependencies:
              flutter:
                sdk: flutter

            flutter:
              uses-material-design: true
        """.trimIndent()
    }

    private fun flutterMainDart(displayName: String, packageName: String, versionName: String) = """
        import 'package:flutter/material.dart';

        void main() {
          runApp(const IdeUltimateApp());
        }

        class IdeUltimateApp extends StatelessWidget {
          const IdeUltimateApp({super.key});

          @override
          Widget build(BuildContext context) {
            return MaterialApp(
              debugShowCheckedModeBanner: false,
              title: '${escapeKotlin(displayName)}',
              theme: ThemeData.dark(useMaterial3: true),
              home: Scaffold(
                body: Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: const [
                      Text(
                        'Hello from ${escapeKotlin(displayName)}',
                        style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
                      ),
                      SizedBox(height: 8),
                      Text('$packageName • v${escapeKotlin(versionName)}'),
                    ],
                  ),
                ),
              ),
            );
          }
        }
    """.trimIndent()

    private fun flutterAndroidBuildGradle(packageName: String, versionCode: Int, versionName: String) = """
        android {
            namespace = "$packageName"
            compileSdk = 35

            defaultConfig {
                applicationId = "$packageName"
                minSdk = 24
                targetSdk = 35
                versionCode = $versionCode
                versionName = "${escapeKotlin(versionName)}"
            }
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

    private fun javaMainActivity(packageName: String, displayName: String) = """
        package $packageName;

        import android.app.Activity;
        import android.os.Bundle;
        import android.view.Gravity;
        import android.widget.TextView;

        public class MainActivity extends Activity {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                TextView tv = new TextView(this);
                tv.setText("Hello from ${escapeKotlin(displayName)}");
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(20f);
                setContentView(tv);
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

    private fun readme(
        name: String,
        language: ProjectLanguage,
        packageName: String,
        versionName: String,
        versionCode: Int,
    ) = """
        # $name

        تم تصدير هذا المشروع من **IDE Ultimate**.

        - **اللغة البرمجية**: ${language.displayName}
        - **الحزمة (Package)**: `$packageName`
        - **الإصدار**: `$versionName` (`versionCode = $versionCode`)

        ## التشغيل

        1. افتح المجلد في Android Studio أو VS Code.
        2. شغّل المشروع على محاكي أو جهاز حقيقي.
    """.trimIndent()

    private fun escapeKotlin(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\$", "\\\$")
}
