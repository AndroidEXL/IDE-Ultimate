package com.androidexl.ideultimate.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class WorkspaceItemType { FOLDER, PROJECT }

data class WorkspaceItem(
    val name: String,
    val type: WorkspaceItemType,
    val parentId: String?,
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * Small persistent workspace model backed by SharedPreferences + org.json.
 * Folders and projects form a tree through [WorkspaceItem.parentId].
 */
class WorkspaceRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("ide_ultimate_workspace", Context.MODE_PRIVATE)

    val items: SnapshotStateList<WorkspaceItem> = mutableStateListOf()

    init {
        val raw = prefs.getString(KEY_ITEMS, null)
        if (raw == null) {
            seedDemoContent()
        } else {
            items.addAll(runCatching { parseItems(raw) }.getOrDefault(emptyList()))
        }
    }

    fun itemById(id: String): WorkspaceItem? = items.firstOrNull { it.id == id }

    /** Folders first, then projects, each group sorted alphabetically. */
    fun childrenOf(parentId: String?): List<WorkspaceItem> = items
        .filter { it.parentId == parentId }
        .sortedWith(
            compareBy<WorkspaceItem> { it.type != WorkspaceItemType.FOLDER }
                .thenBy { it.name.lowercase() },
        )

    fun childCount(folderId: String): Int = items.count { it.parentId == folderId }

    fun isNameTaken(name: String, parentId: String?, excludeId: String? = null): Boolean {
        val candidate = name.trim()
        return items.any { item ->
            item.id != excludeId &&
                item.parentId == parentId &&
                item.name.equals(candidate, ignoreCase = true)
        }
    }

    fun createFolder(name: String, parentId: String?): WorkspaceItem =
        create(name, WorkspaceItemType.FOLDER, parentId)

    fun createProject(name: String, parentId: String?): WorkspaceItem =
        create(name, WorkspaceItemType.PROJECT, parentId)

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

    private fun create(name: String, type: WorkspaceItemType, parentId: String?): WorkspaceItem {
        val item = WorkspaceItem(name = name.trim(), type = type, parentId = parentId)
        items.add(item)
        persist()
        return item
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

    private fun persist() {
        val array = JSONArray()
        items.forEach { item ->
            val json = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("type", item.type.name)
                put("parentId", item.parentId ?: JSONObject.NULL)
                put("createdAt", item.createdAt)
                put("updatedAt", item.updatedAt)
            }
            array.put(json)
        }
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private fun parseItems(raw: String): List<WorkspaceItem> {
        val array = JSONArray(raw)
        val result = ArrayList<WorkspaceItem>(array.length())
        for (index in 0 until array.length()) {
            val json = array.getJSONObject(index)
            val type = runCatching { WorkspaceItemType.valueOf(json.getString("type")) }
                .getOrDefault(WorkspaceItemType.PROJECT)
            result.add(
                WorkspaceItem(
                    id = json.getString("id"),
                    name = json.getString("name"),
                    type = type,
                    parentId = if (json.isNull("parentId")) null else json.optString("parentId").ifBlank { null },
                    createdAt = json.optLong("createdAt"),
                    updatedAt = json.optLong("updatedAt"),
                ),
            )
        }
        return result
    }

    private fun seedDemoContent() {
        val android = WorkspaceItem(name = "مشاريع Android", type = WorkspaceItemType.FOLDER, parentId = null)
        val templates = WorkspaceItem(name = "قوالب جاهزة", type = WorkspaceItemType.FOLDER, parentId = null)
        val experiments = WorkspaceItem(name = "تجارب Kotlin", type = WorkspaceItemType.FOLDER, parentId = null)

        items.addAll(listOf(android, templates, experiments))
        items.add(WorkspaceItem(name = "HelloCompose", type = WorkspaceItemType.PROJECT, parentId = android.id))
        items.add(WorkspaceItem(name = "NotesApp", type = WorkspaceItemType.PROJECT, parentId = android.id))
        items.add(WorkspaceItem(name = "Starter Template", type = WorkspaceItemType.PROJECT, parentId = templates.id))
        items.add(WorkspaceItem(name = "Playground", type = WorkspaceItemType.PROJECT, parentId = null))
        persist()
    }

    private companion object {
        const val KEY_ITEMS = "items_v1"
    }
}
