package com.androidexl.ideultimate.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.UUID

enum class WorkspaceItemType { FOLDER, PROJECT }

enum class ProjectLanguage(
    val displayName: String,
    val shortBadge: String,
) {
    FLUTTER(displayName = "Flutter", shortBadge = "FLUTTER"),
    KOTLIN(displayName = "Kotlin", shortBadge = "KOTLIN"),
    JAVA(displayName = "Java", shortBadge = "JAVA"),
}

data class WorkspaceItem(
    val name: String,
    val type: WorkspaceItemType,
    val parentId: String?,
    val id: String = UUID.randomUUID().toString(),
    val packageName: String = ProjectZipExporter.packageName(name),
    val language: ProjectLanguage = ProjectLanguage.KOTLIN,
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val iconPreset: Int = 0,
    val customIconPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * Represents an XML layout page (e.g. `activity_main.xml`) and its paired
 * Activity class (e.g. `MainActivity` / `MainActivity.kt`) inside an IDE Project.
 */
data class ProjectXmlPage(
    val xmlFileName: String,
    val activityName: String,
    val codeFileName: String,
) {
    val layoutResourceName: String
        get() = xmlFileName.removeSuffix(".xml")
}

/**
 * Persistent workspace model backed by SharedPreferences + full two-way disk synchronization
 * under `/storage/emulated/0/.IDE Ultimate` (restores existing projects and folders from disk).
 */
class WorkspaceRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("ide_ultimate_workspace", Context.MODE_PRIVATE)

    val items: SnapshotStateList<WorkspaceItem> = mutableStateListOf()

    init {
        val raw = prefs.getString(KEY_ITEMS, null)
        if (raw != null) {
            items.addAll(runCatching { parseItems(raw) }.getOrDefault(emptyList()))
        }
        restoreFromFileSystem()
        syncToFileSystem()
    }

    fun itemById(id: String): WorkspaceItem? = items.firstOrNull { it.id == id }

    /**
     * Returns the full `/storage/emulated/0/.IDE Ultimate/...` path for the current breadcrumb trail.
     */
    fun displayDiskPath(pathIds: List<String>): String {
        val segments = pathIds.mapNotNull { id -> itemById(id)?.name }
        return if (segments.isEmpty()) {
            WORKSPACE_DISK_PATH
        } else {
            "$WORKSPACE_DISK_PATH/${segments.joinToString("/")}"
        }
    }

    /** Folders first, then projects, each group sorted alphabetically. */
    fun childrenOf(parentId: String?): List<WorkspaceItem> = items
        .filter { it.parentId == parentId }
        .sortedWith(
            compareBy<WorkspaceItem> { it.type != WorkspaceItemType.FOLDER }
                .thenBy { it.name.lowercase() },
        )

    fun childCount(folderId: String): Int = items.count { it.parentId == folderId }

    fun folderCount(parentId: String?): Int =
        items.count { it.parentId == parentId && it.type == WorkspaceItemType.FOLDER }

    fun projectCount(parentId: String?): Int =
        items.count { it.parentId == parentId && it.type == WorkspaceItemType.PROJECT }

    fun totalFolderCount(): Int = items.count { it.type == WorkspaceItemType.FOLDER }

    fun totalProjectCount(): Int = items.count { it.type == WorkspaceItemType.PROJECT }

    fun allFolders(): List<WorkspaceItem> =
        items.filter { it.type == WorkspaceItemType.FOLDER }.sortedBy { it.name.lowercase() }

    fun isNameTaken(name: String, parentId: String?, excludeId: String? = null): Boolean {
        val candidate = name.trim()
        return items.any { item ->
            item.id != excludeId &&
                item.parentId == parentId &&
                item.name.equals(candidate, ignoreCase = true)
        }
    }

    fun createFolder(name: String, parentId: String?): WorkspaceItem {
        val item = WorkspaceItem(
            name = name.trim(),
            type = WorkspaceItemType.FOLDER,
            parentId = parentId,
        )
        items.add(item)
        persist()
        return item
    }

    fun createProject(
        name: String,
        parentId: String?,
        packageName: String = ProjectZipExporter.packageName(name),
        language: ProjectLanguage = ProjectLanguage.KOTLIN,
        versionCode: Int = 1,
        versionName: String = "1.0.0",
        iconPreset: Int = 0,
        customIconPath: String? = null,
    ): WorkspaceItem {
        val trimmedName = name.trim()
        val cleanPackage = packageName.trim().ifBlank {
            ProjectZipExporter.packageName(trimmedName)
        }
        val item = WorkspaceItem(
            name = trimmedName,
            type = WorkspaceItemType.PROJECT,
            parentId = parentId,
            packageName = cleanPackage,
            language = language,
            versionCode = versionCode.coerceAtLeast(1),
            versionName = versionName.trim().ifBlank { "1.0.0" },
            iconPreset = iconPreset,
            customIconPath = customIconPath,
        )
        items.add(item)
        persist()
        return item
    }

    /**
     * Updates an existing project's editable metadata.
     * Note: [WorkspaceItem.language] is intentionally immutable after creation.
     */
    fun updateProject(
        id: String,
        newName: String,
        newPackageName: String,
        newVersionCode: Int,
        newVersionName: String,
        newIconPreset: Int,
        newCustomIconPath: String?,
    ) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val current = items[index]
        val trimmedName = newName.trim()
        val oldRelSegments = relativeSegmentsOf(current)
        val updated = current.copy(
            name = trimmedName,
            packageName = newPackageName.trim().ifBlank {
                ProjectZipExporter.packageName(trimmedName)
            },
            language = current.language,
            versionCode = newVersionCode.coerceAtLeast(1),
            versionName = newVersionName.trim().ifBlank { "1.0.0" },
            iconPreset = newIconPreset,
            customIconPath = newCustomIconPath,
            updatedAt = System.currentTimeMillis(),
        )
        items[index] = updated
        val newRelSegments = relativeSegmentsOf(updated)
        if (oldRelSegments != newRelSegments) {
            renameOnDisk(oldRelSegments, newRelSegments)
        }
        persist()
    }

    fun rename(id: String, newName: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val current = items[index]
        val oldRelSegments = relativeSegmentsOf(current)
        val updated = current.copy(
            name = newName.trim(),
            updatedAt = System.currentTimeMillis(),
        )
        items[index] = updated
        val newRelSegments = relativeSegmentsOf(updated)
        if (oldRelSegments != newRelSegments) {
            renameOnDisk(oldRelSegments, newRelSegments)
        }
        persist()
    }

    fun descendantCount(id: String): Int = (collectSubtreeIds(id).size - 1).coerceAtLeast(0)

    fun delete(id: String) {
        val target = itemById(id)
        if (target != null) {
            val relSegments = relativeSegmentsOf(target)
            deleteFromDisk(relSegments)
        }
        val ids = collectSubtreeIds(id)
        if (items.removeAll { it.id in ids }) persist()
    }

    private fun collectSubtreeIds(id: String): Set<String> {
        val ids = mutableSetOf(id)
        var changed = true
        while (changed) {
            changed = false
            items.forEach { item ->
                val parent = item.parentId
                if (parent != null && parent in ids && ids.add(item.id)) changed = true
            }
        }
        return ids
    }

    private fun relativeSegmentsOf(item: WorkspaceItem): List<String> {
        val segments = mutableListOf(item.name)
        var parentId = item.parentId
        var guard = 0
        while (parentId != null && guard < 32) {
            val parent = itemById(parentId) ?: break
            segments.add(0, parent.name)
            parentId = parent.parentId
            guard += 1
        }
        return segments
    }

    private fun workspaceRoots(): List<File> = listOfNotNull(
        File(WORKSPACE_DISK_PATH),
        appContext.getExternalFilesDir(null)?.let { File(it, ".IDE Ultimate") },
    )

    private fun renameOnDisk(oldSegments: List<String>, newSegments: List<String>) {
        val oldRel = oldSegments.joinToString(File.separator)
        val newRel = newSegments.joinToString(File.separator)
        workspaceRoots().forEach { root ->
            runCatching {
                val oldDir = File(root, oldRel)
                val newDir = File(root, newRel)
                if (oldDir.exists() && !newDir.exists()) {
                    oldDir.renameTo(newDir)
                }
            }
        }
    }

    private fun deleteFromDisk(segments: List<String>) {
        val rel = segments.joinToString(File.separator)
        workspaceRoots().forEach { root ->
            runCatching {
                val targetDir = File(root, rel)
                if (targetDir.exists()) {
                    targetDir.deleteRecursively()
                }
            }
        }
    }

    /**
     * Scans `/storage/emulated/0/.IDE Ultimate/` (and fallback root) and restores any
     * folders and projects found on disk that are not yet in [items].
     * Returns the number of newly restored projects/folders.
     */
    fun restoreFromFileSystem(): Int {
        var addedCount = 0
        workspaceRoots().forEach { rootDir ->
            runCatching {
                if (rootDir.exists() && rootDir.isDirectory && rootDir.canRead()) {
                    addedCount += scanDiskDirectory(rootDir, parentId = null)
                }
            }
        }
        if (addedCount > 0) {
            savePrefsOnly()
        }
        return addedCount
    }

    private fun scanDiskDirectory(dir: File, parentId: String?): Int {
        var added = 0
        val childDirs = dir.listFiles()
            ?.filter { it.isDirectory && !it.name.startsWith(".") && it.name !in INTERNAL_PROJECT_DIRS }
            ?.sortedBy { it.name.lowercase(Locale.ROOT) }
            ?: return 0

        for (childDir in childDirs) {
            val metaFile = File(childDir, "ide-project.json")
            val folderMarker = File(childDir, ".ide-folder.json")

            val isProject = when {
                metaFile.exists() -> true
                folderMarker.exists() -> false
                hasProjectStructure(childDir) -> true
                hasNestedWorkspaceItems(childDir) -> false
                else -> true
            }

            if (isProject) {
                val parsed = runCatching {
                    if (metaFile.exists()) {
                        parseProjectMetaFile(metaFile, childDir, parentId)
                    } else {
                        inferProjectFromDirectory(childDir, parentId)
                    }
                }.getOrNull()

                if (parsed != null) {
                    val existingIndex = items.indexOfFirst { existing ->
                        existing.id == parsed.id ||
                            (existing.parentId == parentId &&
                                existing.name.equals(parsed.name, ignoreCase = true))
                    }
                    if (existingIndex < 0) {
                        items.add(parsed)
                        added += 1
                    } else {
                        val current = items[existingIndex]
                        if (current.type != WorkspaceItemType.PROJECT) {
                            items[existingIndex] = parsed.copy(id = current.id)
                            added += 1
                        }
                    }
                }
            } else {
                var folderItem = items.firstOrNull { existing ->
                    existing.type == WorkspaceItemType.FOLDER &&
                        existing.parentId == parentId &&
                        existing.name.equals(childDir.name, ignoreCase = true)
                }
                if (folderItem == null) {
                    folderItem = WorkspaceItem(
                        name = childDir.name,
                        type = WorkspaceItemType.FOLDER,
                        parentId = parentId,
                    )
                    items.add(folderItem)
                    added += 1
                }
                added += scanDiskDirectory(childDir, parentId = folderItem.id)
            }
        }
        return added
    }

    private fun hasProjectStructure(dir: File): Boolean {
        val markers = listOf(
            "ide-project.json",
            "build.gradle.kts",
            "build.gradle",
            "settings.gradle.kts",
            "settings.gradle",
            "pubspec.yaml",
            "AndroidManifest.xml",
            "app",
            "src",
            "res",
            "lib",
        )
        if (markers.any { File(dir, it).exists() }) return true
        val files = dir.listFiles() ?: return false
        return files.any { file ->
            file.isFile && (
                file.name.endsWith(".kt", ignoreCase = true) ||
                    file.name.endsWith(".java", ignoreCase = true) ||
                    file.name.endsWith(".dart", ignoreCase = true) ||
                    file.name.endsWith(".xml", ignoreCase = true)
                )
        }
    }

    private fun hasNestedWorkspaceItems(dir: File): Boolean {
        val subDirs = dir.listFiles()?.filter {
            it.isDirectory && !it.name.startsWith(".") && it.name !in INTERNAL_PROJECT_DIRS
        } ?: return false
        return subDirs.isNotEmpty()
    }

    private fun parseProjectMetaFile(
        metaFile: File,
        projectDir: File,
        parentId: String?,
    ): WorkspaceItem {
        val json = JSONObject(metaFile.readText(Charsets.UTF_8))
        val name = json.optString("name", projectDir.name).trim().ifBlank { projectDir.name }
        val id = json.optString("id").trim().ifBlank {
            UUID.nameUUIDFromBytes("project:${parentId.orEmpty()}:$name".toByteArray()).toString()
        }
        val language = runCatching {
            ProjectLanguage.valueOf(json.optString("language", ProjectLanguage.KOTLIN.name))
        }.getOrDefault(ProjectLanguage.KOTLIN)
        val pkg = json.optString("packageName").trim().ifBlank {
            ProjectZipExporter.packageName(name)
        }
        val customIcon = if (json.isNull("customIconPath")) {
            null
        } else {
            json.optString("customIconPath").trim().ifBlank { null }
        }
        return WorkspaceItem(
            id = id,
            name = name,
            type = WorkspaceItemType.PROJECT,
            parentId = parentId,
            packageName = pkg,
            language = language,
            versionCode = json.optInt("versionCode", 1).coerceAtLeast(1),
            versionName = json.optString("versionName", "1.0.0").ifBlank { "1.0.0" },
            iconPreset = json.optInt("iconPreset", 0),
            customIconPath = customIcon,
            createdAt = json.optLong("createdAt", projectDir.lastModified().takeIf { it > 0 } ?: System.currentTimeMillis()),
            updatedAt = json.optLong("updatedAt", projectDir.lastModified().takeIf { it > 0 } ?: System.currentTimeMillis()),
        )
    }

    private fun inferProjectFromDirectory(
        projectDir: File,
        parentId: String?,
    ): WorkspaceItem {
        val name = projectDir.name.trim().ifBlank { "Project" }
        val id = UUID.nameUUIDFromBytes("disk-project:${parentId.orEmpty()}:$name".toByteArray()).toString()
        val language = when {
            File(projectDir, "pubspec.yaml").exists() || File(projectDir, "lib/main.dart").exists() ->
                ProjectLanguage.FLUTTER
            projectDir.walkTopDown().maxDepth(6).any { it.isFile && it.name.endsWith(".java", true) } ->
                ProjectLanguage.JAVA
            else -> ProjectLanguage.KOTLIN
        }
        val pkg = ProjectZipExporter.packageName(name)
        val lastMod = projectDir.lastModified().takeIf { it > 0 } ?: System.currentTimeMillis()
        return WorkspaceItem(
            id = id,
            name = name,
            type = WorkspaceItemType.PROJECT,
            parentId = parentId,
            packageName = pkg,
            language = language,
            versionCode = 1,
            versionName = "1.0.0",
            iconPreset = 0,
            customIconPath = null,
            createdAt = lastMod,
            updatedAt = lastMod,
        )
    }

    // =========================================================================
    // Project XML Layout Pages & Activity Code Management
    // =========================================================================

    private fun projectDirectoriesFor(project: WorkspaceItem): List<File> {
        val relPath = relativeSegmentsOf(project).joinToString(File.separator)
        return workspaceRoots().map { root -> File(root, relPath) }
    }

    /**
     * Converts any XML layout filename (e.g. `activity_main.xml`, `activity_splash.xml`)
     * into its corresponding [ProjectXmlPage] (`MainActivity`, `SplashActivity`, etc.).
     */
    fun xmlPageForName(project: WorkspaceItem, rawXmlName: String): ProjectXmlPage {
        val cleanXml = normalizeXmlFileName(rawXmlName)
        val activityName = xmlNameToActivityName(cleanXml)
        val ext = when (project.language) {
            ProjectLanguage.KOTLIN -> "kt"
            ProjectLanguage.JAVA -> "java"
            ProjectLanguage.FLUTTER -> "dart"
        }
        return ProjectXmlPage(
            xmlFileName = cleanXml,
            activityName = activityName,
            codeFileName = "$activityName.$ext",
        )
    }

    /**
     * Returns all XML layout pages for [project], reading from
     * `/storage/emulated/0/.IDE Ultimate/<project>/app/src/main/res/layout/`
     * and initializing the default XML layout pages if none exist yet.
     */
    fun listProjectXmlPages(project: WorkspaceItem): List<ProjectXmlPage> {
        ensureDefaultProjectFiles(project)
        val foundXmlNames = linkedSetOf<String>()

        projectDirectoriesFor(project).forEach { projDir ->
            val layoutDirs = listOf(
                File(projDir, "app/src/main/res/layout"),
                File(projDir, "res/layout"),
                File(projDir, "layout"),
            )
            layoutDirs.forEach { layoutDir ->
                if (layoutDir.exists() && layoutDir.isDirectory) {
                    layoutDir.listFiles()
                        ?.filter { it.isFile && it.name.endsWith(".xml", ignoreCase = true) }
                        ?.sortedBy { it.name.lowercase(Locale.ROOT) }
                        ?.forEach { file ->
                            foundXmlNames.add(file.name.lowercase(Locale.ROOT))
                        }
                }
            }
        }

        val savedCustom = prefs.getStringSet("xml_pages_${project.id}", null).orEmpty()
        foundXmlNames.addAll(savedCustom.map { normalizeXmlFileName(it) })

        if (foundXmlNames.isEmpty()) {
            DEFAULT_XML_LAYOUTS.forEach { foundXmlNames.add(it) }
        }

        // Keep activity_main.xml first, followed by the rest sorted naturally
        val ordered = foundXmlNames.sortedWith(
            compareBy<String> { it != "activity_main.xml" }
                .thenBy { DEFAULT_XML_LAYOUTS.indexOf(it).let { idx -> if (idx < 0) 999 else idx } }
                .thenBy { it },
        )
        return ordered.map { xmlName -> xmlPageForName(project, xmlName) }
    }

    /**
     * Creates a new XML layout page (e.g. `activity_profile.xml`) and its paired
     * Activity class (`ProfileActivity`) on disk and returns the created [ProjectXmlPage].
     */
    fun addProjectXmlPage(project: WorkspaceItem, rawName: String): ProjectXmlPage {
        val page = xmlPageForName(project, rawName)
        val currentSet = prefs.getStringSet("xml_pages_${project.id}", emptySet()).orEmpty().toMutableSet()
        currentSet.add(page.xmlFileName)
        prefs.edit().putStringSet("xml_pages_${project.id}", currentSet).apply()

        val defaultXml = defaultXmlLayoutContent(project, page)
        val defaultCode = defaultActivityCodeContent(project, page)

        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val layoutFile = File(projDir, "app/src/main/res/layout/${page.xmlFileName}")
                layoutFile.parentFile?.mkdirs()
                if (!layoutFile.exists()) {
                    layoutFile.writeText(defaultXml, Charsets.UTF_8)
                }
                val codeFile = activityFileInDir(projDir, project, page)
                codeFile.parentFile?.mkdirs()
                if (!codeFile.exists()) {
                    codeFile.writeText(defaultCode, Charsets.UTF_8)
                }
            }
        }
        return page
    }

    /**
     * Reads the Activity code for the selected [xmlPage] from disk (or preferences/default template).
     */
    fun readActivityCode(project: WorkspaceItem, xmlPage: ProjectXmlPage): String {
        ensureDefaultProjectFiles(project)
        for (projDir in projectDirectoriesFor(project)) {
            val codeFile = activityFileInDir(projDir, project, xmlPage)
            if (codeFile.exists() && codeFile.isFile && codeFile.canRead()) {
                val text = runCatching { codeFile.readText(Charsets.UTF_8) }.getOrNull()
                if (!text.isNullOrBlank()) return text
            }
        }
        val prefKey = "code_${project.id}_${xmlPage.xmlFileName}"
        val saved = prefs.getString(prefKey, null)
        if (!saved.isNullOrBlank()) return saved
        return defaultActivityCodeContent(project, xmlPage)
    }

    /**
     * Saves the edited Activity code for [xmlPage] to disk in `/storage/emulated/0/.IDE Ultimate/...`
     * and local preferences.
     */
    fun saveActivityCode(project: WorkspaceItem, xmlPage: ProjectXmlPage, newCode: String) {
        val prefKey = "code_${project.id}_${xmlPage.xmlFileName}"
        prefs.edit().putString(prefKey, newCode).apply()
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val codeFile = activityFileInDir(projDir, project, xmlPage)
                codeFile.parentFile?.mkdirs()
                codeFile.writeText(newCode, Charsets.UTF_8)
            }
        }
    }

    private fun activityFileInDir(
        projDir: File,
        project: WorkspaceItem,
        xmlPage: ProjectXmlPage,
    ): File {
        val pkgPath = project.packageName.replace('.', '/')
        return when (project.language) {
            ProjectLanguage.FLUTTER -> File(projDir, "lib/${xmlPage.codeFileName}")
            ProjectLanguage.KOTLIN, ProjectLanguage.JAVA ->
                File(projDir, "app/src/main/java/$pkgPath/${xmlPage.codeFileName}")
        }
    }

    private fun ensureDefaultProjectFiles(project: WorkspaceItem) {
        val defaultPages = DEFAULT_XML_LAYOUTS.map { xmlPageForName(project, it) }
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                if (!projDir.exists()) projDir.mkdirs()
                defaultPages.forEach { page ->
                    val layoutFile = File(projDir, "app/src/main/res/layout/${page.xmlFileName}")
                    layoutFile.parentFile?.mkdirs()
                    if (!layoutFile.exists()) {
                        layoutFile.writeText(defaultXmlLayoutContent(project, page), Charsets.UTF_8)
                    }
                    val codeFile = activityFileInDir(projDir, project, page)
                    codeFile.parentFile?.mkdirs()
                    if (!codeFile.exists()) {
                        codeFile.writeText(defaultActivityCodeContent(project, page), Charsets.UTF_8)
                    }
                }
            }
        }
    }

    private fun normalizeXmlFileName(raw: String): String {
        val cleaned = raw.trim()
            .lowercase(Locale.ROOT)
            .removeSuffix(".xml")
            .replace(Regex("[^a-z0-9_]+"), "_")
            .trim('_')
            .ifBlank { "activity_main" }
        val withPrefix = if (
            cleaned.startsWith("activity_") ||
            cleaned.startsWith("fragment_") ||
            cleaned.startsWith("dialog_") ||
            cleaned.startsWith("item_")
        ) {
            cleaned
        } else {
            "activity_$cleaned"
        }
        return "$withPrefix.xml"
    }

    private fun xmlNameToActivityName(xmlFileName: String): String {
        val base = xmlFileName.removeSuffix(".xml")
            .removePrefix("activity_")
            .ifBlank { "main" }
        val pascal = base.split('_', '-')
            .filter { it.isNotBlank() }
            .joinToString("") { part ->
                part.replaceFirstChar { ch -> ch.titlecase(Locale.ROOT) }
            }
            .ifBlank { "Main" }
        return if (pascal.endsWith("Activity")) pascal else "${pascal}Activity"
    }

    private fun defaultXmlLayoutContent(
        project: WorkspaceItem,
        page: ProjectXmlPage,
    ): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:tools="http://schemas.android.com/tools"
            android:id="@+id/root_${page.layoutResourceName}"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="24dp"
            tools:context=".${page.activityName}">

            <TextView
                android:id="@+id/tv_${page.layoutResourceName}_title"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="${project.name} - ${page.activityName}"
                android:textSize="20sp"
                android:textStyle="bold" />

        </LinearLayout>
    """.trimIndent()

    private fun defaultActivityCodeContent(
        project: WorkspaceItem,
        page: ProjectXmlPage,
    ): String = when (project.language) {
        ProjectLanguage.KOTLIN -> """
            package ${project.packageName}

            import android.os.Bundle
            import androidx.appcompat.app.AppCompatActivity

            /**
             * Activity linked to layout: res/layout/${page.xmlFileName}
             */
            class ${page.activityName} : AppCompatActivity() {

                override fun onCreate(savedInstanceState: Bundle?) {
                    super.onCreate(savedInstanceState)
                    setContentView(R.layout.${page.layoutResourceName})
                }
            }
        """.trimIndent()

        ProjectLanguage.JAVA -> """
            package ${project.packageName};

            import android.app.Activity;
            import android.os.Bundle;

            /**
             * Activity linked to layout: res/layout/${page.xmlFileName}
             */
            public class ${page.activityName} extends Activity {

                @Override
                protected void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    setContentView(R.layout.${page.layoutResourceName});
                }
            }
        """.trimIndent()

        ProjectLanguage.FLUTTER -> """
            import 'package:flutter/material.dart';

            /// Screen linked to XML page: ${page.xmlFileName}
            class ${page.activityName} extends StatelessWidget {
              const ${page.activityName}({super.key});

              @override
              Widget build(BuildContext context) {
                return Scaffold(
                  appBar: AppBar(
                    title: const Text('${project.name} - ${page.activityName}'),
                  ),
                  body: const Center(
                    child: Text('Layout: ${page.xmlFileName} (${page.activityName})'),
                  ),
                );
              }
            }
        """.trimIndent()
    }

    /**
     * Synchronizes folders and project metadata onto `/storage/emulated/0/.IDE Ultimate`
     * (and app-scoped `.IDE Ultimate` fallback if raw external storage isn't granted yet).
     */
    fun syncToFileSystem() {
        workspaceRoots().forEach { rootDir ->
            runCatching {
                if (!rootDir.exists()) rootDir.mkdirs()
                items.forEach { item ->
                    val relPath = relativeSegmentsOf(item).joinToString(File.separator)
                    val targetDir = File(rootDir, relPath)
                    if (!targetDir.exists()) targetDir.mkdirs()
                    if (item.type == WorkspaceItemType.FOLDER) {
                        val folderMarker = File(targetDir, ".ide-folder.json")
                        val folderJson = JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("type", "FOLDER")
                        }
                        folderMarker.writeText(folderJson.toString(2), Charsets.UTF_8)
                    } else if (item.type == WorkspaceItemType.PROJECT) {
                        val metaFile = File(targetDir, "ide-project.json")
                        val metaJson = JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("packageName", item.packageName)
                            put("language", item.language.name)
                            put("versionCode", item.versionCode)
                            put("versionName", item.versionName)
                            put("iconPreset", item.iconPreset)
                            put("customIconPath", item.customIconPath ?: JSONObject.NULL)
                            put("createdAt", item.createdAt)
                            put("updatedAt", item.updatedAt)
                            put("path", "$WORKSPACE_DISK_PATH/${relativeSegmentsOf(item).joinToString("/")}")
                        }
                        metaFile.writeText(metaJson.toString(2), Charsets.UTF_8)
                        ensureDefaultProjectFiles(item)
                    }
                }
            }
        }
    }

    private fun savePrefsOnly() {
        val array = JSONArray()
        items.forEach { item ->
            val json = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("type", item.type.name)
                put("parentId", item.parentId ?: JSONObject.NULL)
                put("packageName", item.packageName)
                put("language", item.language.name)
                put("versionCode", item.versionCode)
                put("versionName", item.versionName)
                put("iconPreset", item.iconPreset)
                put("customIconPath", item.customIconPath ?: JSONObject.NULL)
                put("createdAt", item.createdAt)
                put("updatedAt", item.updatedAt)
            }
            array.put(json)
        }
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private fun persist() {
        savePrefsOnly()
        syncToFileSystem()
    }

    private fun parseItems(raw: String): List<WorkspaceItem> {
        val array = JSONArray(raw)
        val result = ArrayList<WorkspaceItem>(array.length())
        for (index in 0 until array.length()) {
            val json = array.getJSONObject(index)
            val name = json.getString("name")
            val type = runCatching { WorkspaceItemType.valueOf(json.getString("type")) }
                .getOrDefault(WorkspaceItemType.PROJECT)
            val language = runCatching {
                ProjectLanguage.valueOf(json.optString("language", ProjectLanguage.KOTLIN.name))
            }.getOrDefault(ProjectLanguage.KOTLIN)
            val pkg = json.optString("packageName").ifBlank {
                ProjectZipExporter.packageName(name)
            }
            val customIcon = if (json.isNull("customIconPath")) {
                null
            } else {
                json.optString("customIconPath").ifBlank { null }
            }
            result.add(
                WorkspaceItem(
                    id = json.getString("id"),
                    name = name,
                    type = type,
                    parentId = if (json.isNull("parentId")) null else json.optString("parentId").ifBlank { null },
                    packageName = pkg,
                    language = language,
                    versionCode = json.optInt("versionCode", 1).coerceAtLeast(1),
                    versionName = json.optString("versionName", "1.0.0").ifBlank { "1.0.0" },
                    iconPreset = json.optInt("iconPreset", 0),
                    customIconPath = customIcon,
                    createdAt = json.optLong("createdAt"),
                    updatedAt = json.optLong("updatedAt"),
                ),
            )
        }
        return result
    }

    companion object {
        const val WORKSPACE_DISK_PATH = "/storage/emulated/0/.IDE Ultimate"
        private const val KEY_ITEMS = "items_v5"

        private val DEFAULT_XML_LAYOUTS = listOf(
            "activity_main.xml",
            "activity_splash.xml",
            "activity_home.xml",
        )

        private val INTERNAL_PROJECT_DIRS = setOf(
            "app",
            "src",
            "res",
            "lib",
            "gradle",
            "android",
            "ios",
            "build",
            "layout",
            "java",
            "kotlin",
            "values",
            "assets",
            "web",
            "test",
        )
    }
}
