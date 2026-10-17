package com.androidexl.ideultimate.data

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
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
