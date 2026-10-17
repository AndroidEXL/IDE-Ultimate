package com.androidexl.ideultimate.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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
 * Persistent workspace model backed by SharedPreferences + disk synchronization
 * under `/storage/emulated/0/.IDE Ultimate`.
 */
class WorkspaceRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("ide_ultimate_workspace", Context.MODE_PRIVATE)

    val items: SnapshotStateList<WorkspaceItem> = mutableStateListOf()

    init {
        val raw = prefs.getString(KEY_ITEMS, null)
        if (raw == null) {
            seedDemoContent()
        } else {
            items.addAll(runCatching { parseItems(raw) }.getOrDefault(emptyList()))
            syncToFileSystem()
        }
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
        items[index] = current.copy(
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
        persist()
    }

    fun rename(id: String, newName: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        items[index] = items[index].copy(
            name = newName.trim(),
            updatedAt = System.currentTimeMillis(),
        )
        persist()
    }

    fun descendantCount(id: String): Int = (collectSubtreeIds(id).size - 1).coerceAtLeast(0)

    fun delete(id: String) {
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

    /**
     * Synchronizes folders and project metadata onto `/storage/emulated/0/.IDE Ultimate`
     * (and app-scoped `.IDE Ultimate` fallback if raw external storage isn't granted yet).
     */
    private fun syncToFileSystem() {
        val roots = listOfNotNull(
            File(WORKSPACE_DISK_PATH),
            appContext.getExternalFilesDir(null)?.let { File(it, ".IDE Ultimate") },
        )
        roots.forEach { rootDir ->
            runCatching {
                if (!rootDir.exists()) rootDir.mkdirs()
                items.forEach { item ->
                    val relPath = relativeSegmentsOf(item).joinToString(File.separator)
                    val targetDir = File(rootDir, relPath)
                    if (!targetDir.exists()) targetDir.mkdirs()
                    if (item.type == WorkspaceItemType.PROJECT) {
                        val metaFile = File(targetDir, "ide-project.json")
                        val metaJson = JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("packageName", item.packageName)
                            put("language", item.language.name)
                            put("versionCode", item.versionCode)
                            put("versionName", item.versionName)
                            put("path", "$WORKSPACE_DISK_PATH/${relativeSegmentsOf(item).joinToString("/")}")
                        }
                        metaFile.writeText(metaJson.toString(2), Charsets.UTF_8)
                    }
                }
            }
        }
    }

    private fun persist() {
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

    private fun seedDemoContent() {
        val android = WorkspaceItem(name = "مشاريع Android", type = WorkspaceItemType.FOLDER, parentId = null)
        val flutter = WorkspaceItem(name = "تطبيقات Flutter", type = WorkspaceItemType.FOLDER, parentId = null)
        val templates = WorkspaceItem(name = "قوالب جاهزة", type = WorkspaceItemType.FOLDER, parentId = null)

        items.addAll(listOf(android, flutter, templates))
        // Root project so the user immediately sees projects can live directly in the root too
        items.add(
            WorkspaceItem(
                name = "IDE Starter",
                type = WorkspaceItemType.PROJECT,
                parentId = null,
                packageName = "com.ideultimate.projects.idestarter",
                language = ProjectLanguage.KOTLIN,
                versionCode = 1,
                versionName = "1.0.0",
                iconPreset = 0,
            ),
        )
        items.add(
            WorkspaceItem(
                name = "HelloCompose",
                type = WorkspaceItemType.PROJECT,
                parentId = android.id,
                packageName = "com.ideultimate.projects.hellocompose",
                language = ProjectLanguage.KOTLIN,
                versionCode = 2,
                versionName = "1.2.0",
                iconPreset = 0,
            ),
        )
        items.add(
            WorkspaceItem(
                name = "NotesApp",
                type = WorkspaceItemType.PROJECT,
                parentId = android.id,
                packageName = "com.ideultimate.projects.notesapp",
                language = ProjectLanguage.JAVA,
                versionCode = 1,
                versionName = "1.0.0",
                iconPreset = 2,
            ),
        )
        items.add(
            WorkspaceItem(
                name = "ShopFlutter",
                type = WorkspaceItemType.PROJECT,
                parentId = flutter.id,
                packageName = "com.ideultimate.projects.shopflutter",
                language = ProjectLanguage.FLUTTER,
                versionCode = 3,
                versionName = "2.0.0",
                iconPreset = 1,
            ),
        )
        items.add(
            WorkspaceItem(
                name = "Starter Template",
                type = WorkspaceItemType.PROJECT,
                parentId = templates.id,
                packageName = "com.ideultimate.projects.startertemplate",
                language = ProjectLanguage.KOTLIN,
                versionCode = 1,
                versionName = "1.0.0",
                iconPreset = 3,
            ),
        )
        persist()
    }

    companion object {
        const val WORKSPACE_DISK_PATH = "/storage/emulated/0/.IDE Ultimate"
        private const val KEY_ITEMS = "items_v4"
    }
}
