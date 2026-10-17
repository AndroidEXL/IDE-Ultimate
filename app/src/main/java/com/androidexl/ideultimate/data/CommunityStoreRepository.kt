package com.androidexl.ideultimate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

enum class StoreItemType {
    PROJECT,
    CODE_SNIPPET,
    LIBRARY,
}

data class StoreProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val type: StoreItemType = StoreItemType.PROJECT,
    val name: String,
    val details: String,
    val codeContent: String = "",
    val iconPreset: Int = 0,
    val customIconPath: String? = null,
    val language: ProjectLanguage = ProjectLanguage.KOTLIN,
    val downloadLinks: List<String> = emptyList(),
    val imagePaths: List<String> = emptyList(),
    val likesCount: Int = 0,
    val dislikesCount: Int = 0,
    val userReaction: Int = 0, // 1 = liked, -1 = disliked, 0 = none
    val savedInCollection: Boolean = false,
    val createdByUserId: String = "",
    val createdByCurrentUser: Boolean = false,
    val authorName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

data class UserAccountRecord(
    val userId: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val avatarPath: String? = null,
    val subscribersCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
)

/**
 * Real-time Firebase-backed repository (`https://sketchware-f2a24-default-rtdb.firebaseio.com`)
 * with:
 * - Zero fake user & zero demo/sample items
 * - Full user authentication (Sign In & Sign Up with Name, Icon, Email, Password, Confirm Password)
 * - 3 item types: PROJECT (قسم المشاريع الجديدة), CODE_SNIPPET (قسم الأكواد البرمجية), LIBRARY (قسم المكتبات الخاصة)
 */
class CommunityStoreRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext
        .getSharedPreferences("ide_ultimate_community_v2", Context.MODE_PRIVATE)

    var currentUserId by mutableStateOf(prefs.getString(KEY_USER_ID, "") ?: "")
        private set

    var userName by mutableStateOf(prefs.getString(KEY_USER_NAME, "") ?: "")
        private set

    var userEmail by mutableStateOf(prefs.getString(KEY_USER_EMAIL, "") ?: "")
        private set

    var userAvatarPath by mutableStateOf(prefs.getString(KEY_USER_AVATAR, null))
        private set

    var isLoggedIn by mutableStateOf(prefs.getBoolean(KEY_LOGGED_IN, false) && currentUserId.isNotBlank())
        private set

    var subscribersCount by mutableIntStateOf(prefs.getInt(KEY_SUBSCRIBERS, 0))
        private set

    var isFirebaseConnected by mutableStateOf(false)
        private set

    val firebaseProjectId: String = FirebaseConfig.PROJECT_ID
    val firebaseDatabaseUrl: String = FirebaseConfig.FIREBASE_URL

    /** All store items across the 3 types (PROJECT, CODE_SNIPPET, LIBRARY) — starts empty with 0 demo items. */
    val items: SnapshotStateList<StoreProjectItem> = mutableStateListOf()

    private val accounts: SnapshotStateList<UserAccountRecord> = mutableStateListOf()
    private var firebaseDb: FirebaseDatabase? = null

    init {
        val rawItems = prefs.getString(KEY_ITEMS, null)
        if (rawItems != null) {
            items.addAll(runCatching { parseItems(rawItems) }.getOrDefault(emptyList()))
        }
        val rawAccounts = prefs.getString(KEY_ACCOUNTS, null)
        if (rawAccounts != null) {
            accounts.addAll(runCatching { parseAccounts(rawAccounts) }.getOrDefault(emptyList()))
        }
        initFirebaseRealtimeSync()
    }

    private fun initFirebaseRealtimeSync() {
        val db = FirebaseConfig.ensureInitialized(appContext) ?: return
        firebaseDb = db

        // Remove any legacy demo items that existed in earlier versions
        runCatching {
            FirebaseConfig.LEGACY_DEMO_NODES.forEach { legacyPath ->
                db.getReference(legacyPath).removeValue()
            }
        }

        // 1. Monitor Firebase connection state (.info/connected)
        runCatching {
            db.getReference(".info/connected")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        isFirebaseConnected = snapshot.getValue(Boolean::class.java) == true
                    }

                    override fun onCancelled(error: DatabaseError) {
                        isFirebaseConnected = false
                    }
                })
        }

        // 2. Sync registered accounts (ide_ultimate/accounts)
        runCatching {
            val accountsRef = db.getReference(FirebaseConfig.NODE_ACCOUNTS)
            accountsRef.keepSynced(true)
            accountsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val loaded = ArrayList<UserAccountRecord>()
                    for (child in snapshot.children) {
                        val userId = child.child("userId").getValue(String::class.java)
                            ?: child.key
                            ?: continue
                        val name = child.child("name").getValue(String::class.java) ?: continue
                        val email = child.child("email").getValue(String::class.java) ?: continue
                        val passwordHash = child.child("passwordHash").getValue(String::class.java) ?: continue
                        val avatarPath = child.child("avatarPath").getValue(String::class.java)
                            ?.takeIf { it.isNotBlank() }
                        val subs = child.child("subscribersCount").getValue(Int::class.java) ?: 0
                        val createdAt = child.child("createdAt").getValue(Long::class.java)
                            ?: System.currentTimeMillis()
                        loaded.add(
                            UserAccountRecord(
                                userId = userId,
                                name = name,
                                email = email,
                                passwordHash = passwordHash,
                                avatarPath = avatarPath,
                                subscribersCount = subs,
                                createdAt = createdAt,
                            ),
                        )
                    }
                    accounts.clear()
                    accounts.addAll(loaded)
                    persistAccounts()

                    if (isLoggedIn) {
                        val currentAccount = accounts.firstOrNull { it.userId == currentUserId }
                        if (currentAccount != null) {
                            subscribersCount = currentAccount.subscribersCount
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
        }

        // 3. Sync Store Items (ide_ultimate/store_items) — no demo seeding
        runCatching {
            val itemsRef = db.getReference(FirebaseConfig.NODE_STORE_ITEMS)
            itemsRef.keepSynced(true)
            itemsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val localById = items.associateBy { it.id }
                    val remoteList = ArrayList<StoreProjectItem>()
                    for (child in snapshot.children) {
                        val parsed = parseItemSnapshot(child, localById[child.key])
                        if (parsed != null) {
                            remoteList.add(parsed)
                        }
                    }
                    remoteList.sortByDescending { it.createdAt }
                    items.clear()
                    items.addAll(remoteList)
                    persistItems()
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
        }
    }

    private fun parseItemSnapshot(
        snapshot: DataSnapshot,
        localExisting: StoreProjectItem?,
    ): StoreProjectItem? {
        return runCatching {
            val id = snapshot.child("id").getValue(String::class.java)
                ?: snapshot.key
                ?: return null
            val typeStr = snapshot.child("type").getValue(String::class.java)
                ?: StoreItemType.PROJECT.name
            val type = runCatching { StoreItemType.valueOf(typeStr) }
                .getOrDefault(StoreItemType.PROJECT)
            val name = snapshot.child("name").getValue(String::class.java) ?: return null
            val details = snapshot.child("details").getValue(String::class.java) ?: ""
            val codeContent = snapshot.child("codeContent").getValue(String::class.java) ?: ""
            val iconPreset = snapshot.child("iconPreset").getValue(Int::class.java) ?: 0
            val customIconPath = snapshot.child("customIconPath").getValue(String::class.java)
                ?.takeIf { it.isNotBlank() }
                ?: localExisting?.customIconPath
            val langName = snapshot.child("language").getValue(String::class.java) ?: "KOTLIN"
            val language = runCatching { ProjectLanguage.valueOf(langName) }
                .getOrDefault(ProjectLanguage.KOTLIN)
            val downloadLinks = snapshot.child("downloadLinks").children
                .mapNotNull { it.getValue(String::class.java)?.takeIf { s -> s.isNotBlank() } }
            val imagePaths = snapshot.child("imagePaths").children
                .mapNotNull { it.getValue(String::class.java)?.takeIf { s -> s.isNotBlank() } }
            val likesCount = snapshot.child("likesCount").getValue(Int::class.java) ?: 0
            val dislikesCount = snapshot.child("dislikesCount").getValue(Int::class.java) ?: 0
            val authorName = snapshot.child("authorName").getValue(String::class.java) ?: ""
            val createdByUserId = snapshot.child("createdByUserId").getValue(String::class.java) ?: ""
            val isMine = isLoggedIn && currentUserId.isNotBlank() && createdByUserId == currentUserId
            val createdAt = snapshot.child("createdAt").getValue(Long::class.java)
                ?: localExisting?.createdAt
                ?: System.currentTimeMillis()

            StoreProjectItem(
                id = id,
                type = type,
                name = name,
                details = details,
                codeContent = codeContent,
                iconPreset = iconPreset,
                customIconPath = customIconPath,
                language = language,
                downloadLinks = downloadLinks,
                imagePaths = imagePaths,
                likesCount = likesCount,
                dislikesCount = dislikesCount,
                userReaction = localExisting?.userReaction ?: 0,
                savedInCollection = localExisting?.savedInCollection ?: false,
                createdByUserId = createdByUserId,
                createdByCurrentUser = isMine,
                authorName = authorName,
                createdAt = createdAt,
            )
        }.getOrNull()
    }

    private fun pushItemToFirebase(item: StoreProjectItem) {
        val db = firebaseDb ?: return
        runCatching {
            val payload = mapOf(
                "id" to item.id,
                "type" to item.type.name,
                "name" to item.name,
                "details" to item.details,
                "codeContent" to item.codeContent,
                "iconPreset" to item.iconPreset,
                "customIconPath" to (item.customIconPath ?: ""),
                "language" to item.language.name,
                "downloadLinks" to item.downloadLinks,
                "imagePaths" to item.imagePaths,
                "likesCount" to item.likesCount,
                "dislikesCount" to item.dislikesCount,
                "authorName" to item.authorName,
                "createdByUserId" to item.createdByUserId,
                "createdAt" to item.createdAt,
            )
            db.getReference(FirebaseConfig.NODE_STORE_ITEMS)
                .child(item.id)
                .setValue(payload)
        }
    }

    private fun pushAccountToFirebase(account: UserAccountRecord) {
        val db = firebaseDb ?: return
        runCatching {
            val payload = mapOf(
                "userId" to account.userId,
                "name" to account.name,
                "email" to account.email,
                "passwordHash" to account.passwordHash,
                "avatarPath" to (account.avatarPath ?: ""),
                "subscribersCount" to account.subscribersCount,
                "createdAt" to account.createdAt,
            )
            db.getReference(FirebaseConfig.NODE_ACCOUNTS)
                .child(account.userId)
                .setValue(payload)
            db.getReference(FirebaseConfig.NODE_USERS)
                .child(account.userId)
                .setValue(
                    payload + mapOf(
                        "projectsCount" to userPublishedProjects().size,
                        "likesCount" to totalUserLikes(),
                        "dislikesCount" to totalUserDislikes(),
                    ),
                )
        }
    }

    /**
     * Sign up a new user with Name, Avatar, Email, Password, and Confirm Password.
     */
    fun registerNewAccount(
        name: String,
        email: String,
        password: String,
        avatarPath: String?,
    ): Result<UserAccountRecord> {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()
        val existing = accounts.firstOrNull { it.email.equals(cleanEmail, ignoreCase = true) }
        if (existing != null) {
            return Result.failure(IllegalArgumentException("EMAIL_TAKEN"))
        }

        val newId = "usr_" + UUID.randomUUID().toString().replace("-", "").take(12)
        val record = UserAccountRecord(
            userId = newId,
            name = cleanName,
            email = cleanEmail,
            passwordHash = hashPassword(password),
            avatarPath = avatarPath,
            subscribersCount = 0,
        )
        accounts.add(record)
        persistAccounts()
        pushAccountToFirebase(record)
        applyActiveSession(record)
        return Result.success(record)
    }

    /**
     * Sign in an existing user with Email and Password.
     */
    fun signInWithEmailPassword(
        email: String,
        password: String,
    ): Result<UserAccountRecord> {
        val cleanEmail = email.trim().lowercase()
        val hashed = hashPassword(password)
        val matched = accounts.firstOrNull {
            it.email.equals(cleanEmail, ignoreCase = true) && it.passwordHash == hashed
        } ?: return Result.failure(IllegalArgumentException("INVALID_CREDENTIALS"))

        applyActiveSession(matched)
        return Result.success(matched)
    }

    private fun applyActiveSession(account: UserAccountRecord) {
        currentUserId = account.userId
        userName = account.name
        userEmail = account.email
        userAvatarPath = account.avatarPath
        subscribersCount = account.subscribersCount
        isLoggedIn = true

        prefs.edit()
            .putString(KEY_USER_ID, currentUserId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, userEmail)
            .putString(KEY_USER_AVATAR, userAvatarPath)
            .putInt(KEY_SUBSCRIBERS, subscribersCount)
            .putBoolean(KEY_LOGGED_IN, true)
            .apply()

        refreshOwnershipFlags()
    }

    fun logout() {
        isLoggedIn = false
        currentUserId = ""
        userName = ""
        userEmail = ""
        userAvatarPath = null
        subscribersCount = 0

        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_AVATAR)
            .putInt(KEY_SUBSCRIBERS, 0)
            .apply()

        refreshOwnershipFlags()
    }

    private fun refreshOwnershipFlags() {
        for (i in items.indices) {
            val current = items[i]
            val mine = isLoggedIn && currentUserId.isNotBlank() && current.createdByUserId == currentUserId
            if (current.createdByCurrentUser != mine) {
                items[i] = current.copy(createdByCurrentUser = mine)
            }
        }
        persistItems()
    }

    fun updateProfile(newName: String, newAvatarPath: String?) {
        if (!isLoggedIn) return
        val trimmed = newName.trim().ifBlank { userName }
        userName = trimmed
        userAvatarPath = newAvatarPath
        prefs.edit()
            .putString(KEY_USER_NAME, trimmed)
            .putString(KEY_USER_AVATAR, newAvatarPath)
            .apply()

        val idx = accounts.indexOfFirst { it.userId == currentUserId }
        if (idx >= 0) {
            val updatedAccount = accounts[idx].copy(
                name = trimmed,
                avatarPath = newAvatarPath,
            )
            accounts[idx] = updatedAccount
            persistAccounts()
            pushAccountToFirebase(updatedAccount)
        }
    }

    fun itemById(id: String): StoreProjectItem? = items.firstOrNull { it.id == id }

    fun newProjects(): List<StoreProjectItem> =
        items.filter { it.type == StoreItemType.PROJECT }.sortedByDescending { it.createdAt }

    fun codeSnippetItems(): List<StoreProjectItem> =
        items.filter { it.type == StoreItemType.CODE_SNIPPET }.sortedByDescending { it.createdAt }

    fun customLibraryItems(): List<StoreProjectItem> =
        items.filter { it.type == StoreItemType.LIBRARY }.sortedByDescending { it.createdAt }

    fun userPublishedProjects(): List<StoreProjectItem> =
        if (!isLoggedIn) {
            emptyList()
        } else {
            items.filter { it.createdByUserId == currentUserId || it.createdByCurrentUser }
                .sortedByDescending { it.createdAt }
        }

    fun savedCollectionProjects(): List<StoreProjectItem> =
        items.filter { it.savedInCollection }.sortedByDescending { it.createdAt }

    fun totalUserLikes(): Int =
        userPublishedProjects().sumOf { it.likesCount }

    fun totalUserDislikes(): Int =
        userPublishedProjects().sumOf { it.dislikesCount }

    fun createStoreItem(
        type: StoreItemType,
        name: String,
        details: String,
        codeContent: String,
        customIconPath: String?,
        iconPreset: Int = 0,
        language: ProjectLanguage = ProjectLanguage.KOTLIN,
        downloadLinks: List<String>,
        imagePaths: List<String>,
    ): StoreProjectItem {
        val item = StoreProjectItem(
            type = type,
            name = name.trim(),
            details = details.trim(),
            codeContent = if (type == StoreItemType.PROJECT) "" else codeContent.trim(),
            iconPreset = iconPreset,
            customIconPath = customIconPath,
            language = language,
            downloadLinks = if (type == StoreItemType.PROJECT) {
                downloadLinks.filter { it.isNotBlank() }
            } else {
                emptyList()
            },
            imagePaths = if (type == StoreItemType.PROJECT) {
                imagePaths.filter { it.isNotBlank() }
            } else {
                emptyList()
            },
            likesCount = 0,
            dislikesCount = 0,
            userReaction = 0,
            savedInCollection = false,
            createdByUserId = currentUserId,
            createdByCurrentUser = true,
            authorName = userName.ifBlank { "User" },
        )
        items.add(0, item)
        persistItems()
        pushItemToFirebase(item)
        return item
    }

    fun updateStoreItem(
        id: String,
        type: StoreItemType,
        name: String,
        details: String,
        codeContent: String,
        customIconPath: String?,
        iconPreset: Int,
        language: ProjectLanguage,
        downloadLinks: List<String>,
        imagePaths: List<String>,
    ) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val existing = items[index]
        val updated = existing.copy(
            type = type,
            name = name.trim(),
            details = details.trim(),
            codeContent = if (type == StoreItemType.PROJECT) "" else codeContent.trim(),
            customIconPath = customIconPath,
            iconPreset = iconPreset,
            language = language,
            downloadLinks = if (type == StoreItemType.PROJECT) {
                downloadLinks.filter { it.isNotBlank() }
            } else {
                emptyList()
            },
            imagePaths = if (type == StoreItemType.PROJECT) {
                imagePaths.filter { it.isNotBlank() }
            } else {
                emptyList()
            },
        )
        items[index] = updated
        persistItems()
        pushItemToFirebase(updated)
    }

    fun deleteStoreItem(id: String) {
        if (items.removeAll { it.id == id }) {
            persistItems()
            runCatching {
                firebaseDb?.getReference(FirebaseConfig.NODE_STORE_ITEMS)
                    ?.child(id)
                    ?.removeValue()
            }
        }
    }

    fun toggleSaveInCollection(id: String): Boolean {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return false
        val current = items[index]
        val nextSaved = !current.savedInCollection
        items[index] = current.copy(savedInCollection = nextSaved)
        persistItems()
        if (currentUserId.isNotBlank()) {
            runCatching {
                val ref = firebaseDb?.getReference(FirebaseConfig.NODE_COLLECTIONS)
                    ?.child(currentUserId)
                    ?.child(id)
                if (nextSaved) {
                    ref?.setValue(true)
                } else {
                    ref?.removeValue()
                }
            }
        }
        return nextSaved
    }

    fun toggleLike(id: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val item = items[index]
        val updated = when (item.userReaction) {
            1 -> item.copy(
                likesCount = (item.likesCount - 1).coerceAtLeast(0),
                userReaction = 0,
            )
            -1 -> item.copy(
                likesCount = item.likesCount + 1,
                dislikesCount = (item.dislikesCount - 1).coerceAtLeast(0),
                userReaction = 1,
            )
            else -> item.copy(
                likesCount = item.likesCount + 1,
                userReaction = 1,
            )
        }
        items[index] = updated
        persistItems()
        pushItemToFirebase(updated)
    }

    fun toggleDislike(id: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val item = items[index]
        val updated = when (item.userReaction) {
            -1 -> item.copy(
                dislikesCount = (item.dislikesCount - 1).coerceAtLeast(0),
                userReaction = 0,
            )
            1 -> item.copy(
                dislikesCount = item.dislikesCount + 1,
                likesCount = (item.likesCount - 1).coerceAtLeast(0),
                userReaction = -1,
            )
            else -> item.copy(
                dislikesCount = item.dislikesCount + 1,
                userReaction = -1,
            )
        }
        items[index] = updated
        persistItems()
        pushItemToFirebase(updated)
    }

    private fun hashPassword(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(raw.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun persistItems() {
        val array = JSONArray()
        items.forEach { item ->
            val json = JSONObject().apply {
                put("id", item.id)
                put("type", item.type.name)
                put("name", item.name)
                put("details", item.details)
                put("codeContent", item.codeContent)
                put("iconPreset", item.iconPreset)
                put("customIconPath", item.customIconPath ?: JSONObject.NULL)
                put("language", item.language.name)
                put("downloadLinks", JSONArray(item.downloadLinks))
                put("imagePaths", JSONArray(item.imagePaths))
                put("likesCount", item.likesCount)
                put("dislikesCount", item.dislikesCount)
                put("userReaction", item.userReaction)
                put("savedInCollection", item.savedInCollection)
                put("createdByUserId", item.createdByUserId)
                put("createdByCurrentUser", item.createdByCurrentUser)
                put("authorName", item.authorName)
                put("createdAt", item.createdAt)
            }
            array.put(json)
        }
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private fun parseItems(raw: String): List<StoreProjectItem> {
        val array = JSONArray(raw)
        val list = ArrayList<StoreProjectItem>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val linksArr = obj.optJSONArray("downloadLinks") ?: JSONArray()
            val links = (0 until linksArr.length()).mapNotNull { idx ->
                linksArr.optString(idx).takeIf { it.isNotBlank() }
            }
            val imagesArr = obj.optJSONArray("imagePaths") ?: JSONArray()
            val images = (0 until imagesArr.length()).mapNotNull { idx ->
                imagesArr.optString(idx).takeIf { it.isNotBlank() }
            }
            val type = runCatching {
                StoreItemType.valueOf(obj.optString("type", StoreItemType.PROJECT.name))
            }.getOrDefault(StoreItemType.PROJECT)
            val lang = runCatching {
                ProjectLanguage.valueOf(obj.optString("language", ProjectLanguage.KOTLIN.name))
            }.getOrDefault(ProjectLanguage.KOTLIN)
            val customIcon = if (obj.isNull("customIconPath")) {
                null
            } else {
                obj.optString("customIconPath").ifBlank { null }
            }
            val ownerId = obj.optString("createdByUserId", "")
            val isMine = isLoggedIn && currentUserId.isNotBlank() && ownerId == currentUserId
            list.add(
                StoreProjectItem(
                    id = obj.getString("id"),
                    type = type,
                    name = obj.getString("name"),
                    details = obj.optString("details"),
                    codeContent = obj.optString("codeContent", ""),
                    iconPreset = obj.optInt("iconPreset", 0),
                    customIconPath = customIcon,
                    language = lang,
                    downloadLinks = links,
                    imagePaths = images,
                    likesCount = obj.optInt("likesCount", 0),
                    dislikesCount = obj.optInt("dislikesCount", 0),
                    userReaction = obj.optInt("userReaction", 0),
                    savedInCollection = obj.optBoolean("savedInCollection", false),
                    createdByUserId = ownerId,
                    createdByCurrentUser = isMine,
                    authorName = obj.optString("authorName", ""),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                ),
            )
        }
        return list
    }

    private fun persistAccounts() {
        val array = JSONArray()
        accounts.forEach { acc ->
            val json = JSONObject().apply {
                put("userId", acc.userId)
                put("name", acc.name)
                put("email", acc.email)
                put("passwordHash", acc.passwordHash)
                put("avatarPath", acc.avatarPath ?: JSONObject.NULL)
                put("subscribersCount", acc.subscribersCount)
                put("createdAt", acc.createdAt)
            }
            array.put(json)
        }
        prefs.edit().putString(KEY_ACCOUNTS, array.toString()).apply()
    }

    private fun parseAccounts(raw: String): List<UserAccountRecord> {
        val array = JSONArray(raw)
        val list = ArrayList<UserAccountRecord>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val avatar = if (obj.isNull("avatarPath")) {
                null
            } else {
                obj.optString("avatarPath").ifBlank { null }
            }
            list.add(
                UserAccountRecord(
                    userId = obj.getString("userId"),
                    name = obj.getString("name"),
                    email = obj.getString("email"),
                    passwordHash = obj.getString("passwordHash"),
                    avatarPath = avatar,
                    subscribersCount = obj.optInt("subscribersCount", 0),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                ),
            )
        }
        return list
    }

    private companion object {
        const val KEY_ITEMS = "store_items_v2"
        const val KEY_ACCOUNTS = "store_accounts_v2"
        const val KEY_USER_ID = "user_id_v2"
        const val KEY_USER_NAME = "user_name_v2"
        const val KEY_USER_EMAIL = "user_email_v2"
        const val KEY_USER_AVATAR = "user_avatar_v2"
        const val KEY_LOGGED_IN = "user_logged_in_v2"
        const val KEY_SUBSCRIBERS = "user_subscribers_v2"
    }
}
