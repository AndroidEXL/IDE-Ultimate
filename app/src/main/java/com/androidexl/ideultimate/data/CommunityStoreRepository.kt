package com.androidexl.ideultimate.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.androidexl.ideultimate.ui.components.encodeImageForFirebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.Executors

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
 * using BOTH the official Firebase Realtime Database / Auth SDKs AND direct HTTPS REST calls
 * so every registration, login, project upload, code snippet, library, like/dislike, and collection
 * is guaranteed to write to and read from Realtime Database immediately.
 */
class CommunityStoreRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext
        .getSharedPreferences("ide_ultimate_community_v3", Context.MODE_PRIVATE)
    private val ioExecutor = Executors.newCachedThreadPool()
    private val mainHandler = Handler(Looper.getMainLooper())

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

    var lastFirebaseStatus by mutableStateOf("")
        private set

    val firebaseProjectId: String = FirebaseConfig.PROJECT_ID
    val firebaseDatabaseUrl: String = FirebaseConfig.FIREBASE_URL

    /** All store items across the 3 types (PROJECT, CODE_SNIPPET, LIBRARY) — 0 demo items. */
    val items: SnapshotStateList<StoreProjectItem> = mutableStateListOf()

    private val accounts: SnapshotStateList<UserAccountRecord> = mutableStateListOf()
    private var firebaseDb: FirebaseDatabase? = null

    init {
        FirebaseConfig.currentIdToken = prefs.getString(KEY_ID_TOKEN, null)
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
        val db = FirebaseConfig.ensureInitialized(appContext)
        firebaseDb = db

        if (db != null) {
            // 1. Monitor Firebase connection state (.info/connected)
            runCatching {
                db.getReference(".info/connected")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val connected = snapshot.getValue(Boolean::class.java) == true
                            if (connected) isFirebaseConnected = true
                        }

                        override fun onCancelled(error: DatabaseError) = Unit
                    })
            }

            // 2. Listen to /users in Realtime Database
            runCatching {
                db.getReference(FirebaseConfig.NODE_USERS)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            isFirebaseConnected = true
                            val loaded = ArrayList<UserAccountRecord>()
                            for (child in snapshot.children) {
                                val rec = parseAccountSnapshot(child)
                                if (rec != null) loaded.add(rec)
                            }
                            if (loaded.isNotEmpty()) {
                                mergeAccountsOnMain(loaded)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            lastFirebaseStatus = error.message
                        }
                    })
            }

            // 3. Listen to /store_items in Realtime Database
            runCatching {
                db.getReference(FirebaseConfig.NODE_STORE_ITEMS)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            isFirebaseConnected = true
                            val localById = items.associateBy { it.id }
                            val remoteList = ArrayList<StoreProjectItem>()
                            for (child in snapshot.children) {
                                val parsed = parseItemSnapshot(child, localById[child.key])
                                if (parsed != null) remoteList.add(parsed)
                            }
                            remoteList.sortByDescending { it.createdAt }
                            items.clear()
                            items.addAll(remoteList)
                            persistItems()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            lastFirebaseStatus = error.message
                        }
                    })
            }
        }

        // 4. Immediately fetch latest /users and /store_items over HTTPS REST
        refreshFromFirebaseRest()
    }

    fun refreshFromFirebaseRest() {
        ioExecutor.execute {
            val usersRes = FirebaseConfig.getRtdbJson(FirebaseConfig.NODE_USERS)
            if (usersRes.isSuccess) {
                val parsedUsers = parseUsersFromRtdbJson(usersRes.body)
                mainHandler.post {
                    isFirebaseConnected = true
                    if (parsedUsers.isNotEmpty()) {
                        mergeAccountsOnMain(parsedUsers)
                    }
                }
            }
            val itemsRes = FirebaseConfig.getRtdbJson(FirebaseConfig.NODE_STORE_ITEMS)
            if (itemsRes.isSuccess) {
                mainHandler.post {
                    isFirebaseConnected = true
                    val localById = items.associateBy { it.id }
                    val parsedItems = parseItemsFromRtdbJson(itemsRes.body, localById)
                    parsedItems.sortByDescending { it.createdAt }
                    items.clear()
                    items.addAll(parsedItems)
                    persistItems()
                }
            }
        }
    }

    private fun mergeAccountsOnMain(remoteAccounts: List<UserAccountRecord>) {
        val byEmail = LinkedHashMap<String, UserAccountRecord>()
        accounts.forEach { byEmail[it.email.lowercase()] = it }
        remoteAccounts.forEach { byEmail[it.email.lowercase()] = it }
        accounts.clear()
        accounts.addAll(byEmail.values)
        persistAccounts()

        if (isLoggedIn) {
            val currentAccount = accounts.firstOrNull {
                it.userId == currentUserId || it.email.equals(userEmail, ignoreCase = true)
            }
            if (currentAccount != null) {
                subscribersCount = currentAccount.subscribersCount
            }
        }
    }

    private fun parseAccountSnapshot(child: DataSnapshot): UserAccountRecord? {
        return runCatching {
            val userId = child.child("userId").value?.toString()
                ?: child.key
                ?: return null
            val name = child.child("name").value?.toString() ?: return null
            val email = child.child("email").value?.toString() ?: return null
            val passwordHash = child.child("passwordHash").value?.toString() ?: ""
            val avatarPath = child.child("avatarPath").value?.toString()?.takeIf { it.isNotBlank() }
            val subs = child.child("subscribersCount").value?.toString()?.toIntOrNull() ?: 0
            val createdAt = child.child("createdAt").value?.toString()?.toLongOrNull()
                ?: System.currentTimeMillis()
            UserAccountRecord(
                userId = userId,
                name = name,
                email = email,
                passwordHash = passwordHash,
                avatarPath = avatarPath,
                subscribersCount = subs,
                createdAt = createdAt,
            )
        }.getOrNull()
    }

    private fun parseUsersFromRtdbJson(body: String): List<UserAccountRecord> {
        val trimmed = body.trim()
        if (trimmed.isEmpty() || trimmed == "null" || !trimmed.startsWith("{")) return emptyList()
        return runCatching {
            val root = JSONObject(trimmed)
            val result = ArrayList<UserAccountRecord>()
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = root.optJSONObject(key) ?: continue
                val userId = obj.optString("userId", key)
                val name = obj.optString("name", "")
                val email = obj.optString("email", "")
                if (email.isBlank()) continue
                val passwordHash = obj.optString("passwordHash", "")
                val avatarPath = obj.optString("avatarPath", "").ifBlank { null }
                val subs = obj.optInt("subscribersCount", 0)
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                result.add(
                    UserAccountRecord(
                        userId = userId,
                        name = name.ifBlank { email.substringBefore('@') },
                        email = email,
                        passwordHash = passwordHash,
                        avatarPath = avatarPath,
                        subscribersCount = subs,
                        createdAt = createdAt,
                    ),
                )
            }
            result
        }.getOrDefault(emptyList())
    }

    private fun parseItemSnapshot(
        snapshot: DataSnapshot,
        localExisting: StoreProjectItem?,
    ): StoreProjectItem? {
        return runCatching {
            val id = snapshot.child("id").value?.toString()
                ?: snapshot.key
                ?: return null
            val typeStr = snapshot.child("type").value?.toString()
                ?: StoreItemType.PROJECT.name
            val type = runCatching { StoreItemType.valueOf(typeStr) }
                .getOrDefault(StoreItemType.PROJECT)
            val name = snapshot.child("name").value?.toString() ?: return null
            val details = snapshot.child("details").value?.toString() ?: ""
            val codeContent = snapshot.child("codeContent").value?.toString() ?: ""
            val iconPreset = snapshot.child("iconPreset").value?.toString()?.toIntOrNull() ?: 0
            val customIconPath = snapshot.child("customIconPath").value?.toString()
                ?.takeIf { it.isNotBlank() }
                ?: localExisting?.customIconPath
            val langName = snapshot.child("language").value?.toString() ?: "KOTLIN"
            val language = runCatching { ProjectLanguage.valueOf(langName) }
                .getOrDefault(ProjectLanguage.KOTLIN)
            val downloadLinks = snapshot.child("downloadLinks").children
                .mapNotNull { it.value?.toString()?.takeIf { s -> s.isNotBlank() } }
            val imagePaths = snapshot.child("imagePaths").children
                .mapNotNull { it.value?.toString()?.takeIf { s -> s.isNotBlank() } }
            val likesCount = snapshot.child("likesCount").value?.toString()?.toIntOrNull() ?: 0
            val dislikesCount = snapshot.child("dislikesCount").value?.toString()?.toIntOrNull() ?: 0
            val authorName = snapshot.child("authorName").value?.toString() ?: ""
            val createdByUserId = snapshot.child("createdByUserId").value?.toString() ?: ""
            val isMine = isLoggedIn && currentUserId.isNotBlank() && createdByUserId == currentUserId
            val createdAt = snapshot.child("createdAt").value?.toString()?.toLongOrNull()
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

    private fun parseItemsFromRtdbJson(
        body: String,
        localById: Map<String, StoreProjectItem>,
    ): ArrayList<StoreProjectItem> {
        val list = ArrayList<StoreProjectItem>()
        val trimmed = body.trim()
        if (trimmed.isEmpty() || trimmed == "null" || !trimmed.startsWith("{")) return list
        runCatching {
            val root = JSONObject(trimmed)
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val obj = root.optJSONObject(key) ?: continue
                val id = obj.optString("id", key)
                val name = obj.optString("name", "")
                if (name.isBlank()) continue
                val localExisting = localById[id]
                val type = runCatching {
                    StoreItemType.valueOf(obj.optString("type", StoreItemType.PROJECT.name))
                }.getOrDefault(StoreItemType.PROJECT)
                val lang = runCatching {
                    ProjectLanguage.valueOf(obj.optString("language", ProjectLanguage.KOTLIN.name))
                }.getOrDefault(ProjectLanguage.KOTLIN)
                val linksArr = obj.optJSONArray("downloadLinks") ?: JSONArray()
                val links = (0 until linksArr.length()).mapNotNull { i ->
                    linksArr.optString(i).takeIf { it.isNotBlank() }
                }
                val imagesArr = obj.optJSONArray("imagePaths") ?: JSONArray()
                val images = (0 until imagesArr.length()).mapNotNull { i ->
                    imagesArr.optString(i).takeIf { it.isNotBlank() }
                }
                val customIcon = obj.optString("customIconPath", "").ifBlank {
                    localExisting?.customIconPath
                }
                val createdByUserId = obj.optString("createdByUserId", "")
                val isMine = isLoggedIn && currentUserId.isNotBlank() && createdByUserId == currentUserId
                list.add(
                    StoreProjectItem(
                        id = id,
                        type = type,
                        name = name,
                        details = obj.optString("details", ""),
                        codeContent = obj.optString("codeContent", ""),
                        iconPreset = obj.optInt("iconPreset", 0),
                        customIconPath = customIcon,
                        language = lang,
                        downloadLinks = links,
                        imagePaths = images,
                        likesCount = obj.optInt("likesCount", 0),
                        dislikesCount = obj.optInt("dislikesCount", 0),
                        userReaction = localExisting?.userReaction ?: 0,
                        savedInCollection = localExisting?.savedInCollection ?: false,
                        createdByUserId = createdByUserId,
                        createdByCurrentUser = isMine,
                        authorName = obj.optString("authorName", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    ),
                )
            }
        }
        return list
    }

    private fun itemToJsonObject(item: StoreProjectItem): JSONObject {
        val encodedIcon = encodeImageForFirebase(item.customIconPath, maxDim = 160) ?: ""
        val encodedImages = item.imagePaths.mapNotNull {
            encodeImageForFirebase(it, maxDim = 480)
        }
        return JSONObject().apply {
            put("id", item.id)
            put("type", item.type.name)
            put("name", item.name)
            put("details", item.details)
            put("codeContent", item.codeContent)
            put("iconPreset", item.iconPreset)
            put("customIconPath", encodedIcon)
            put("language", item.language.name)
            put("downloadLinks", JSONArray(item.downloadLinks))
            put("imagePaths", JSONArray(encodedImages))
            put("likesCount", item.likesCount)
            put("dislikesCount", item.dislikesCount)
            put("authorName", item.authorName)
            put("createdByUserId", item.createdByUserId)
            put("createdAt", item.createdAt)
        }
    }

    private fun categoryNodeFor(type: StoreItemType): String = when (type) {
        StoreItemType.PROJECT -> FirebaseConfig.NODE_PROJECTS
        StoreItemType.CODE_SNIPPET -> FirebaseConfig.NODE_CODES
        StoreItemType.LIBRARY -> FirebaseConfig.NODE_LIBRARIES
    }

    private fun pushItemToFirebaseBlocking(item: StoreProjectItem): RtdbHttpResponse {
        val jsonObj = itemToJsonObject(item)
        val jsonString = jsonObj.toString()

        // 1. Write via official FirebaseDatabase SDK
        runCatching {
            val mapPayload = mapOf(
                "id" to item.id,
                "type" to item.type.name,
                "name" to item.name,
                "details" to item.details,
                "codeContent" to item.codeContent,
                "iconPreset" to item.iconPreset,
                "customIconPath" to jsonObj.optString("customIconPath", ""),
                "language" to item.language.name,
                "downloadLinks" to item.downloadLinks,
                "imagePaths" to (0 until (jsonObj.optJSONArray("imagePaths")?.length() ?: 0)).map {
                    jsonObj.getJSONArray("imagePaths").getString(it)
                },
                "likesCount" to item.likesCount,
                "dislikesCount" to item.dislikesCount,
                "authorName" to item.authorName,
                "createdByUserId" to item.createdByUserId,
                "createdAt" to item.createdAt,
            )
            firebaseDb?.getReference(FirebaseConfig.NODE_STORE_ITEMS)
                ?.child(item.id)
                ?.setValue(mapPayload)
            firebaseDb?.getReference(categoryNodeFor(item.type))
                ?.child(item.id)
                ?.setValue(mapPayload)
        }

        // 2. Write directly via HTTPS PUT to Realtime Database REST API
        val mainWrite = FirebaseConfig.putRtdbJson(
            "${FirebaseConfig.NODE_STORE_ITEMS}/${item.id}",
            jsonString,
        )
        FirebaseConfig.putRtdbJson(
            "${categoryNodeFor(item.type)}/${item.id}",
            jsonString,
        )
        return mainWrite
    }

    private fun pushAccountToFirebaseBlocking(account: UserAccountRecord): RtdbHttpResponse {
        val encodedAvatar = encodeImageForFirebase(account.avatarPath, maxDim = 160) ?: ""
        val userProjectsCount = items.count { it.createdByUserId == account.userId }
        val userLikesCount = items.filter { it.createdByUserId == account.userId }.sumOf { it.likesCount }
        val userDislikesCount = items.filter { it.createdByUserId == account.userId }.sumOf { it.dislikesCount }

        val jsonObj = JSONObject().apply {
            put("userId", account.userId)
            put("name", account.name)
            put("email", account.email)
            put("passwordHash", account.passwordHash)
            put("avatarPath", encodedAvatar)
            put("subscribersCount", account.subscribersCount)
            put("projectsCount", userProjectsCount)
            put("likesCount", userLikesCount)
            put("dislikesCount", userDislikesCount)
            put("createdAt", account.createdAt)
            put("updatedAt", System.currentTimeMillis())
        }

        // 1. Write via official FirebaseDatabase SDK
        runCatching {
            val payload = mapOf(
                "userId" to account.userId,
                "name" to account.name,
                "email" to account.email,
                "passwordHash" to account.passwordHash,
                "avatarPath" to encodedAvatar,
                "subscribersCount" to account.subscribersCount,
                "projectsCount" to userProjectsCount,
                "likesCount" to userLikesCount,
                "dislikesCount" to userDislikesCount,
                "createdAt" to account.createdAt,
                "updatedAt" to System.currentTimeMillis(),
            )
            firebaseDb?.getReference(FirebaseConfig.NODE_USERS)
                ?.child(account.userId)
                ?.setValue(payload)
        }

        // 2. Write directly via HTTPS PUT to /users/<userId>.json in Realtime Database
        return FirebaseConfig.putRtdbJson(
            "${FirebaseConfig.NODE_USERS}/${account.userId}",
            jsonObj.toString(),
        )
    }

    /**
     * Asynchronously registers a new account in Firebase Auth + Realtime Database (`/users/<userId>`)
     * and invokes [onComplete] on the Main Thread once saved in Realtime Database.
     */
    fun registerNewAccountAsync(
        name: String,
        email: String,
        password: String,
        avatarPath: String?,
        onComplete: (Boolean, String?) -> Unit,
    ) {
        val cleanName = name.trim()
        val cleanEmail = email.trim().lowercase()

        ioExecutor.execute {
            // 1. Fetch latest /users from Realtime Database to check if email already exists
            val latestUsersRes = FirebaseConfig.getRtdbJson(FirebaseConfig.NODE_USERS)
            val remoteAccounts = if (latestUsersRes.isSuccess) {
                parseUsersFromRtdbJson(latestUsersRes.body)
            } else {
                emptyList()
            }

            val alreadyExists = remoteAccounts.any { it.email.equals(cleanEmail, ignoreCase = true) } ||
                accounts.any { it.email.equals(cleanEmail, ignoreCase = true) }
            if (alreadyExists) {
                mainHandler.post {
                    onComplete(false, "EMAIL_TAKEN")
                }
                return@execute
            }

            // 2. Register in Firebase Authentication (IdentityToolkit + FirebaseAuth SDK)
            val authRes = FirebaseConfig.signUpWithIdentityToolkit(cleanEmail, password)
            runCatching {
                FirebaseConfig.authInstance(appContext)
                    ?.createUserWithEmailAndPassword(cleanEmail, password)
            }
            if (authRes.idToken.isNotBlank()) {
                prefs.edit().putString(KEY_ID_TOKEN, authRes.idToken).apply()
            }

            val newId = authRes.localId.takeIf { it.isNotBlank() }
                ?: ("usr_" + UUID.randomUUID().toString().replace("-", "").take(12))

            val encodedAvatar = encodeImageForFirebase(avatarPath, maxDim = 160)
            val record = UserAccountRecord(
                userId = newId,
                name = cleanName,
                email = cleanEmail,
                passwordHash = hashPassword(password),
                avatarPath = encodedAvatar ?: avatarPath,
                subscribersCount = 0,
            )

            // 3. Push user data directly into Realtime Database (/users/<userId>)
            val rtdbWrite = pushAccountToFirebaseBlocking(record)

            mainHandler.post {
                isFirebaseConnected = rtdbWrite.isSuccess
                if (!rtdbWrite.isSuccess) {
                    lastFirebaseStatus = "HTTP ${rtdbWrite.statusCode}: ${rtdbWrite.body}"
                }
                accounts.removeAll { it.email.equals(cleanEmail, ignoreCase = true) }
                accounts.add(record)
                persistAccounts()
                applyActiveSession(record)
                onComplete(true, if (rtdbWrite.isSuccess) null else rtdbWrite.body)
            }
        }
    }

    /**
     * Asynchronously signs in an existing user against Firebase Auth + Realtime Database (`/users`)
     * and updates their record in Realtime Database.
     */
    fun signInWithEmailPasswordAsync(
        email: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit,
    ) {
        val cleanEmail = email.trim().lowercase()
        val hashed = hashPassword(password)

        ioExecutor.execute {
            // 1. Try Firebase Auth IdentityToolkit Sign In
            val authRes = FirebaseConfig.signInWithIdentityToolkit(cleanEmail, password)
            runCatching {
                FirebaseConfig.authInstance(appContext)
                    ?.signInWithEmailAndPassword(cleanEmail, password)
            }
            if (authRes.idToken.isNotBlank()) {
                prefs.edit().putString(KEY_ID_TOKEN, authRes.idToken).apply()
            }

            // 2. Fetch latest /users from Realtime Database
            val usersRes = FirebaseConfig.getRtdbJson(FirebaseConfig.NODE_USERS)
            val remoteUsers = if (usersRes.isSuccess) {
                parseUsersFromRtdbJson(usersRes.body)
            } else {
                emptyList()
            }

            val allKnown = LinkedHashMap<String, UserAccountRecord>()
            accounts.forEach { allKnown[it.email.lowercase()] = it }
            remoteUsers.forEach { allKnown[it.email.lowercase()] = it }

            val matchedInDb = allKnown[cleanEmail]?.takeIf {
                it.passwordHash == hashed || authRes.isSuccess
            }

            val finalAccount = matchedInDb ?: if (authRes.isSuccess) {
                // User existed in Firebase Authentication; create/sync their /users/<uid> node in Realtime DB
                UserAccountRecord(
                    userId = authRes.localId.ifBlank {
                        "usr_" + UUID.randomUUID().toString().replace("-", "").take(12)
                    },
                    name = cleanEmail.substringBefore('@'),
                    email = cleanEmail,
                    passwordHash = hashed,
                    avatarPath = null,
                    subscribersCount = 0,
                )
            } else {
                null
            }

            if (finalAccount == null) {
                mainHandler.post {
                    onComplete(false, "INVALID_CREDENTIALS")
                }
                return@execute
            }

            // 3. Ensure user record is written/updated in Realtime Database (/users/<userId>)
            val rtdbWrite = pushAccountToFirebaseBlocking(finalAccount)

            mainHandler.post {
                isFirebaseConnected = rtdbWrite.isSuccess || usersRes.isSuccess
                mergeAccountsOnMain(allKnown.values.toList() + listOf(finalAccount))
                applyActiveSession(finalAccount)
                onComplete(true, null)
            }
        }
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
        FirebaseConfig.currentIdToken = null
        runCatching { FirebaseConfig.authInstance(appContext)?.signOut() }

        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_AVATAR)
            .remove(KEY_ID_TOKEN)
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
        val encodedAvatar = encodeImageForFirebase(newAvatarPath, maxDim = 160) ?: newAvatarPath
        userName = trimmed
        userAvatarPath = encodedAvatar
        prefs.edit()
            .putString(KEY_USER_NAME, trimmed)
            .putString(KEY_USER_AVATAR, encodedAvatar)
            .apply()

        val idx = accounts.indexOfFirst { it.userId == currentUserId }
        val updatedAccount = if (idx >= 0) {
            accounts[idx].copy(name = trimmed, avatarPath = encodedAvatar).also { accounts[idx] = it }
        } else {
            UserAccountRecord(
                userId = currentUserId,
                name = trimmed,
                email = userEmail,
                passwordHash = "",
                avatarPath = encodedAvatar,
                subscribersCount = subscribersCount,
            ).also { accounts.add(it) }
        }
        persistAccounts()
        ioExecutor.execute {
            val res = pushAccountToFirebaseBlocking(updatedAccount)
            mainHandler.post {
                if (res.isSuccess) isFirebaseConnected = true
            }
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

    /**
     * Creates and uploads a new Store Item (Project, Code Snippet, or Library) directly to
     * Firebase Realtime Database (`/store_items/<id>` and `/projects/<id>` | `/codes/<id>` | `/libraries/<id>`).
     */
    fun createStoreItemAsync(
        type: StoreItemType,
        name: String,
        details: String,
        codeContent: String,
        customIconPath: String?,
        iconPreset: Int = 0,
        language: ProjectLanguage = ProjectLanguage.KOTLIN,
        downloadLinks: List<String>,
        imagePaths: List<String>,
        onComplete: (StoreProjectItem, Boolean) -> Unit,
    ) {
        ioExecutor.execute {
            val encodedIcon = encodeImageForFirebase(customIconPath, maxDim = 160) ?: customIconPath
            val encodedShots = if (type == StoreItemType.PROJECT) {
                imagePaths.filter { it.isNotBlank() }.mapNotNull {
                    encodeImageForFirebase(it, maxDim = 480)
                }
            } else {
                emptyList()
            }

            val item = StoreProjectItem(
                type = type,
                name = name.trim(),
                details = details.trim(),
                codeContent = if (type == StoreItemType.PROJECT) "" else codeContent.trim(),
                iconPreset = iconPreset,
                customIconPath = encodedIcon,
                language = language,
                downloadLinks = if (type == StoreItemType.PROJECT) {
                    downloadLinks.filter { it.isNotBlank() }
                } else {
                    emptyList()
                },
                imagePaths = encodedShots,
                likesCount = 0,
                dislikesCount = 0,
                userReaction = 0,
                savedInCollection = false,
                createdByUserId = currentUserId,
                createdByCurrentUser = true,
                authorName = userName.ifBlank { "User" },
            )

            val rtdbRes = pushItemToFirebaseBlocking(item)
            val currentAcc = accounts.firstOrNull { it.userId == currentUserId }
            if (currentAcc != null) {
                pushAccountToFirebaseBlocking(currentAcc)
            }

            mainHandler.post {
                isFirebaseConnected = rtdbRes.isSuccess
                items.removeAll { it.id == item.id }
                items.add(0, item)
                persistItems()
                onComplete(item, rtdbRes.isSuccess)
            }
        }
    }

    /**
     * Updates an existing Store Item in Firebase Realtime Database.
     */
    fun updateStoreItemAsync(
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
        onComplete: (Boolean) -> Unit,
    ) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) {
            onComplete(false)
            return
        }
        val existing = items[index]
        ioExecutor.execute {
            val encodedIcon = encodeImageForFirebase(customIconPath, maxDim = 160) ?: customIconPath
            val encodedShots = if (type == StoreItemType.PROJECT) {
                imagePaths.filter { it.isNotBlank() }.mapNotNull {
                    encodeImageForFirebase(it, maxDim = 480)
                }
            } else {
                emptyList()
            }
            val updated = existing.copy(
                type = type,
                name = name.trim(),
                details = details.trim(),
                codeContent = if (type == StoreItemType.PROJECT) "" else codeContent.trim(),
                customIconPath = encodedIcon,
                iconPreset = iconPreset,
                language = language,
                downloadLinks = if (type == StoreItemType.PROJECT) {
                    downloadLinks.filter { it.isNotBlank() }
                } else {
                    emptyList()
                },
                imagePaths = encodedShots,
            )

            val rtdbRes = pushItemToFirebaseBlocking(updated)
            mainHandler.post {
                isFirebaseConnected = rtdbRes.isSuccess
                val idxNow = items.indexOfFirst { it.id == id }
                if (idxNow >= 0) {
                    items[idxNow] = updated
                }
                persistItems()
                onComplete(rtdbRes.isSuccess)
            }
        }
    }

    fun deleteStoreItem(id: String) {
        val target = items.firstOrNull { it.id == id }
        if (items.removeAll { it.id == id }) {
            persistItems()
            ioExecutor.execute {
                runCatching {
                    firebaseDb?.getReference(FirebaseConfig.NODE_STORE_ITEMS)
                        ?.child(id)
                        ?.removeValue()
                    if (target != null) {
                        firebaseDb?.getReference(categoryNodeFor(target.type))
                            ?.child(id)
                            ?.removeValue()
                    }
                }
                FirebaseConfig.deleteRtdbJson("${FirebaseConfig.NODE_STORE_ITEMS}/$id")
                if (target != null) {
                    FirebaseConfig.deleteRtdbJson("${categoryNodeFor(target.type)}/$id")
                }
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
            ioExecutor.execute {
                val path = "${FirebaseConfig.NODE_COLLECTIONS}/$currentUserId/$id"
                if (nextSaved) {
                    FirebaseConfig.putRtdbJson(path, "true")
                } else {
                    FirebaseConfig.deleteRtdbJson(path)
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
        ioExecutor.execute {
            pushItemToFirebaseBlocking(updated)
            if (currentUserId.isNotBlank()) {
                FirebaseConfig.putRtdbJson(
                    "${FirebaseConfig.NODE_REACTIONS}/$currentUserId/$id",
                    "${updated.userReaction}",
                )
            }
        }
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
        ioExecutor.execute {
            pushItemToFirebaseBlocking(updated)
            if (currentUserId.isNotBlank()) {
                FirebaseConfig.putRtdbJson(
                    "${FirebaseConfig.NODE_REACTIONS}/$currentUserId/$id",
                    "${updated.userReaction}",
                )
            }
        }
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
        const val KEY_ITEMS = "store_items_v3"
        const val KEY_ACCOUNTS = "store_accounts_v3"
        const val KEY_USER_ID = "user_id_v3"
        const val KEY_USER_NAME = "user_name_v3"
        const val KEY_USER_EMAIL = "user_email_v3"
        const val KEY_USER_AVATAR = "user_avatar_v3"
        const val KEY_LOGGED_IN = "user_logged_in_v3"
        const val KEY_SUBSCRIBERS = "user_subscribers_v3"
        const val KEY_ID_TOKEN = "user_id_token_v3"
    }
}
