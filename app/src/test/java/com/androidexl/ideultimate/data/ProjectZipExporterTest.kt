package com.androidexl.ideultimate.data

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectZipExporterTest {

    /** Reads every ZIP entry (name -> UTF-8 content) so the archive can be asserted. */
    private fun entries(bytes: ByteArray): Map<String, String> {
        val result = linkedMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                result[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return result
    }

    @Test
    fun exportsACompleteGradleProject() {
        val files = entries(ProjectZipExporter.buildZip("Notes App"))

        assertTrue(files.keys.any { it.endsWith("settings.gradle.kts") }, "settings.gradle.kts")
        assertTrue(files.keys.any { it.endsWith("build.gradle.kts") }, "root build.gradle.kts")
        assertTrue(files.keys.any { it.endsWith("app/build.gradle.kts") }, "app build.gradle.kts")
        assertTrue(files.keys.any { it.endsWith("AndroidManifest.xml") }, "AndroidManifest.xml")
        assertTrue(files.keys.any { it.endsWith("MainActivity.kt") }, "MainActivity.kt")

        val main = files.entries.first { it.key.endsWith("MainActivity.kt") }.value
        assertTrue(main.contains("package com.ideultimate.projects.notesapp"))
        assertTrue(main.contains("Hello from Notes App"))
    }

    @Test
    fun exportsJavaAndFlutterProjectsWithMetadata() {
        val javaItem = WorkspaceItem(
            name = "JavaCalc",
            type = WorkspaceItemType.PROJECT,
            parentId = null,
            packageName = "com.custom.javacalc",
            language = ProjectLanguage.JAVA,
            versionCode = 7,
            versionName = "2.3.1",
        )
        val javaFiles = entries(ProjectZipExporter.buildZip(javaItem))
        assertTrue(javaFiles.keys.any { it.endsWith("com/custom/javacalc/MainActivity.java") })
        val javaGradle = javaFiles.entries.first { it.key.endsWith("app/build.gradle.kts") }.value
        assertTrue(javaGradle.contains("versionCode = 7"))
        assertTrue(javaGradle.contains("versionName = \"2.3.1\""))

        val flutterItem = WorkspaceItem(
            name = "FlutterShop",
            type = WorkspaceItemType.PROJECT,
            parentId = null,
            packageName = "com.custom.fluttershop",
            language = ProjectLanguage.FLUTTER,
            versionCode = 4,
            versionName = "1.5.0",
        )
        val flutterFiles = entries(ProjectZipExporter.buildZip(flutterItem))
        assertTrue(flutterFiles.keys.any { it.endsWith("pubspec.yaml") })
        assertTrue(flutterFiles.keys.any { it.endsWith("lib/main.dart") })
        val pubspec = flutterFiles.entries.first { it.key.endsWith("pubspec.yaml") }.value
        assertTrue(pubspec.contains("version: 1.5.0+4"))
    }

    @Test
    fun packageAndDirectoryAreDerivedFromTheName() {
        assertEquals("com.ideultimate.projects.notesapp", ProjectZipExporter.packageName("Notes App"))
        assertEquals("Notes-App", ProjectZipExporter.directoryName("Notes App"))
        assertEquals("com.ideultimate.projects.app", ProjectZipExporter.packageName("***"))
        assertEquals("com.ideultimate.projects.app2cool", ProjectZipExporter.packageName("2cool"))
        assertTrue(ProjectZipExporter.isValidPackageName("com.ideultimate.app"))
        assertFalse(ProjectZipExporter.isValidPackageName("invalid_package"))
    }

    @Test
    fun exportFileNameIsSafe() {
        assertEquals("Notes-App.zip", ProjectZipExporter.suggestedFileName("Notes App"))
        assertEquals("project.zip", ProjectZipExporter.suggestedFileName("***"))
    }

    @Test
    fun arabicProjectNamesStillExport() {
        val files = entries(ProjectZipExporter.buildZip("مشروعي"))
        assertTrue(files.isNotEmpty())
        assertTrue(files.keys.any { it.endsWith("MainActivity.kt") })
        assertTrue(files.keys.any { it.endsWith("strings.xml") })
    }
}
