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
import java.util.UUID

data class StoreProjectItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val details: String,
    val iconPreset: Int = 0,
    val customIconPath: String? = null,
    val language: ProjectLanguage = ProjectLanguage.KOTLIN,
    val downloadLinks: List<String> = emptyList(),
    val imagePaths: List<String> = emptyList(),
    val likesCount: Int = 24,
    val dislikesCount: Int = 1,
    val userReaction: Int = 0, // 1 = liked, -1 = disliked, 0 = none
    val savedInCollection: Boolean = false,
    val createdByCurrentUser: Boolean = true,
    val authorName: String = "AndroidEXL",
    val createdAt: Long = System.currentTimeMillis(),
)

data class CodeSnippetItem(
    val id: String,
    val title: String,
    val description: String,
    val language: ProjectLanguage,
    val iconPreset: Int,
    val code: String,
)

data class CustomLibraryItem(
    val id: String,
    val name: String,
    val summary: String,
    val language: ProjectLanguage,
    val version: String,
    val dependencyLine: String,
    val docsUrl: String,
)

/**
 * Persistent & Firebase-synced repository (`https://sketchware-f2a24-default-rtdb.firebaseio.com`) for:
 * 1. New Store Projects (قسم المشاريع الجديدة)
 * 2. Code Snippets (قسم الأكواد البرمجية)
 * 3. Custom Libraries (قسم المكتبات الخاصة)
 * 4. User Profile (Projects tab, Collection tab, Likes/Dislikes/Subscribers stats)
 */
class CommunityStoreRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext
        .getSharedPreferences("ide_ultimate_community_v1", Context.MODE_PRIVATE)

    val currentUserId: String = prefs.getString(KEY_USER_ID, null) ?: run {
        val generated = "user_" + UUID.randomUUID().toString().take(8)
        prefs.edit().putString(KEY_USER_ID, generated).apply()
        generated
    }

    var userName by mutableStateOf(prefs.getString(KEY_USER_NAME, "AndroidEXL") ?: "AndroidEXL")
        private set

    var userAvatarPath by mutableStateOf(prefs.getString(KEY_USER_AVATAR, null))
        private set

    var isLoggedIn by mutableStateOf(prefs.getBoolean(KEY_LOGGED_IN, true))
        private set

    var subscribersCount by mutableIntStateOf(prefs.getInt(KEY_SUBSCRIBERS, 1280))
        private set

    var isFirebaseConnected by mutableStateOf(false)
        private set

    val firebaseProjectId: String = FirebaseConfig.PROJECT_ID
    val firebaseDatabaseUrl: String = FirebaseConfig.FIREBASE_URL

    val projects: SnapshotStateList<StoreProjectItem> = mutableStateListOf()
    val codeSnippets: SnapshotStateList<CodeSnippetItem> = mutableStateListOf()
    val customLibraries: SnapshotStateList<CustomLibraryItem> = mutableStateListOf()

    private var firebaseDb: FirebaseDatabase? = null

    init {
        val raw = prefs.getString(KEY_PROJECTS, null)
        if (raw == null) {
            seedDefaultStoreProjects()
        } else {
            projects.addAll(runCatching { parseProjects(raw) }.getOrDefault(emptyList()))
        }
        codeSnippets.addAll(defaultCodeSnippets())
        customLibraries.addAll(defaultCustomLibraries())

        initFirebaseRealtimeSync()
    }

    private fun initFirebaseRealtimeSync() {
        val db = FirebaseConfig.ensureInitialized(appContext) ?: return
        firebaseDb = db

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

        // 2. Sync Store Projects (ide_ultimate/store_projects)
        runCatching {
            val projectsRef = db.getReference(FirebaseConfig.NODE_STORE_PROJECTS)
            projectsRef.keepSynced(true)
            projectsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                        // Push initial projects to Firebase Realtime Database
                        projects.forEach { pushProjectToFirebase(it) }
                        return
                    }
                    val localById = projects.associateBy { it.id }
                    val remoteList = ArrayList<StoreProjectItem>()
                    for (child in snapshot.children) {
                        val parsed = parseProjectSnapshot(child, localById[child.key])
                        if (parsed != null) {
                            remoteList.add(parsed)
                        }
                    }
                    if (remoteList.isNotEmpty()) {
                        remoteList.sortByDescending { it.createdAt }
                        projects.clear()
                        projects.addAll(remoteList)
                        persistProjects()
                    }
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
        }

        // 3. Sync Code Snippets (ide_ultimate/code_snippets)
        runCatching {
            val snippetsRef = db.getReference(FirebaseConfig.NODE_CODE_SNIPPETS)
            snippetsRef.keepSynced(true)
            snippetsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                        codeSnippets.forEach { snippet ->
                            snippetsRef.child(snippet.id).setValue(
                                mapOf(
                                    "id" to snippet.id,
                                    "title" to snippet.title,
                                    "description" to snippet.description,
                                    "language" to snippet.language.name,
                                    "iconPreset" to snippet.iconPreset,
                                    "code" to snippet.code,
                                ),
                            )
                        }
                        return
                    }
                    val loaded = ArrayList<CodeSnippetItem>()
                    for (child in snapshot.children) {
                        val id = child.child("id").getValue(String::class.java) ?: child.key ?: continue
                        val title = child.child("title").getValue(String::class.java) ?: continue
                        val description = child.child("description").getValue(String::class.java) ?: ""
                        val langStr = child.child("language").getValue(String::class.java) ?: "KOTLIN"
                        val language = runCatching { ProjectLanguage.valueOf(langStr) }
                            .getOrDefault(ProjectLanguage.KOTLIN)
                        val iconPreset = child.child("iconPreset").getValue(Int::class.java) ?: 0
                        val code = child.child("code").getValue(String::class.java) ?: ""
                        loaded.add(
                            CodeSnippetItem(
                                id = id,
                                title = title,
                                description = description,
                                language = language,
                                iconPreset = iconPreset,
                                code = code,
                            ),
                        )
                    }
                    if (loaded.isNotEmpty()) {
                        codeSnippets.clear()
                        codeSnippets.addAll(loaded)
                    }
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
        }

        // 4. Sync Custom Libraries (ide_ultimate/custom_libraries)
        runCatching {
            val libsRef = db.getReference(FirebaseConfig.NODE_CUSTOM_LIBRARIES)
            libsRef.keepSynced(true)
            libsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                        customLibraries.forEach { lib ->
                            libsRef.child(lib.id).setValue(
                                mapOf(
                                    "id" to lib.id,
                                    "name" to lib.name,
                                    "summary" to lib.summary,
                                    "language" to lib.language.name,
                                    "version" to lib.version,
                                    "dependencyLine" to lib.dependencyLine,
                                    "docsUrl" to lib.docsUrl,
                                ),
                            )
                        }
                        return
                    }
                    val loaded = ArrayList<CustomLibraryItem>()
                    for (child in snapshot.children) {
                        val id = child.child("id").getValue(String::class.java) ?: child.key ?: continue
                        val name = child.child("name").getValue(String::class.java) ?: continue
                        val summary = child.child("summary").getValue(String::class.java) ?: ""
                        val langStr = child.child("language").getValue(String::class.java) ?: "KOTLIN"
                        val language = runCatching { ProjectLanguage.valueOf(langStr) }
                            .getOrDefault(ProjectLanguage.KOTLIN)
                        val version = child.child("version").getValue(String::class.java) ?: "1.0.0"
                        val dep = child.child("dependencyLine").getValue(String::class.java) ?: ""
                        val docs = child.child("docsUrl").getValue(String::class.java) ?: ""
                        loaded.add(
                            CustomLibraryItem(
                                id = id,
                                name = name,
                                summary = summary,
                                language = language,
                                version = version,
                                dependencyLine = dep,
                                docsUrl = docs,
                            ),
                        )
                    }
                    if (loaded.isNotEmpty()) {
                        customLibraries.clear()
                        customLibraries.addAll(loaded)
                    }
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
        }

        // 5. Push/sync current user profile to Firebase (ide_ultimate/users/<userId>)
        syncUserProfileToFirebase()
    }

    private fun parseProjectSnapshot(
        snapshot: DataSnapshot,
        localExisting: StoreProjectItem?,
    ): StoreProjectItem? {
        return runCatching {
            val id = snapshot.child("id").getValue(String::class.java)
                ?: snapshot.key
                ?: return null
            val name = snapshot.child("name").getValue(String::class.java) ?: return null
            val details = snapshot.child("details").getValue(String::class.java) ?: ""
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
            val authorName = snapshot.child("authorName").getValue(String::class.java) ?: "AndroidEXL"
            val createdByUserId = snapshot.child("createdByUserId").getValue(String::class.java)
            val createdByCurrentUser = when {
                createdByUserId != null -> createdByUserId == currentUserId || authorName == userName
                localExisting != null -> localExisting.createdByCurrentUser
                else -> authorName == userName
            }
            val createdAt = snapshot.child("createdAt").getValue(Long::class.java)
                ?: localExisting?.createdAt
                ?: System.currentTimeMillis()

            StoreProjectItem(
                id = id,
                name = name,
                details = details,
                iconPreset = iconPreset,
                customIconPath = customIconPath,
                language = language,
                downloadLinks = downloadLinks,
                imagePaths = imagePaths,
                likesCount = likesCount,
                dislikesCount = dislikesCount,
                userReaction = localExisting?.userReaction ?: 0,
                savedInCollection = localExisting?.savedInCollection ?: false,
                createdByCurrentUser = createdByCurrentUser,
                authorName = authorName,
                createdAt = createdAt,
            )
        }.getOrNull()
    }

    private fun pushProjectToFirebase(item: StoreProjectItem) {
        val db = firebaseDb ?: return
        runCatching {
            val payload = mapOf(
                "id" to item.id,
                "name" to item.name,
                "details" to item.details,
                "iconPreset" to item.iconPreset,
                "customIconPath" to (item.customIconPath ?: ""),
                "language" to item.language.name,
                "downloadLinks" to item.downloadLinks,
                "imagePaths" to item.imagePaths,
                "likesCount" to item.likesCount,
                "dislikesCount" to item.dislikesCount,
                "authorName" to item.authorName,
                "createdByUserId" to currentUserId,
                "createdAt" to item.createdAt,
            )
            db.getReference(FirebaseConfig.NODE_STORE_PROJECTS)
                .child(item.id)
                .setValue(payload)
        }
    }

    private fun syncUserProfileToFirebase() {
        val db = firebaseDb ?: return
        runCatching {
            val profileData = mapOf(
                "userId" to currentUserId,
                "userName" to userName,
                "userAvatarPath" to (userAvatarPath ?: ""),
                "isLoggedIn" to isLoggedIn,
                "subscribersCount" to subscribersCount,
                "projectsCount" to userPublishedProjects().size,
                "likesCount" to totalUserLikes(),
                "dislikesCount" to totalUserDislikes(),
                "updatedAt" to System.currentTimeMillis(),
            )
            db.getReference(FirebaseConfig.NODE_USERS)
                .child(currentUserId)
                .setValue(profileData)
        }
    }

    fun updateProfile(newName: String, newAvatarPath: String?) {
        val trimmed = newName.trim().ifBlank { "AndroidEXL" }
        userName = trimmed
        userAvatarPath = newAvatarPath
        prefs.edit()
            .putString(KEY_USER_NAME, trimmed)
            .putString(KEY_USER_AVATAR, newAvatarPath)
            .apply()
        syncUserProfileToFirebase()
    }

    fun toggleLogin() {
        isLoggedIn = !isLoggedIn
        prefs.edit().putBoolean(KEY_LOGGED_IN, isLoggedIn).apply()
        syncUserProfileToFirebase()
    }

    fun projectById(id: String): StoreProjectItem? = projects.firstOrNull { it.id == id }

    fun userPublishedProjects(): List<StoreProjectItem> =
        projects.filter { it.createdByCurrentUser }.sortedByDescending { it.createdAt }

    fun savedCollectionProjects(): List<StoreProjectItem> =
        projects.filter { it.savedInCollection }.sortedByDescending { it.createdAt }

    fun totalUserLikes(): Int =
        projects.filter { it.createdByCurrentUser }.sumOf { it.likesCount }

    fun totalUserDislikes(): Int =
        projects.filter { it.createdByCurrentUser }.sumOf { it.dislikesCount }

    fun createStoreProject(
        name: String,
        details: String,
        customIconPath: String?,
        iconPreset: Int = 0,
        language: ProjectLanguage = ProjectLanguage.KOTLIN,
        downloadLinks: List<String>,
        imagePaths: List<String>,
    ): StoreProjectItem {
        val item = StoreProjectItem(
            name = name.trim(),
            details = details.trim(),
            iconPreset = iconPreset,
            customIconPath = customIconPath,
            language = language,
            downloadLinks = downloadLinks.filter { it.isNotBlank() },
            imagePaths = imagePaths.filter { it.isNotBlank() },
            likesCount = 1,
            dislikesCount = 0,
            userReaction = 0,
            savedInCollection = false,
            createdByCurrentUser = true,
            authorName = userName,
        )
        projects.add(0, item)
        persistProjects()
        pushProjectToFirebase(item)
        syncUserProfileToFirebase()
        return item
    }

    fun updateStoreProject(
        id: String,
        name: String,
        details: String,
        customIconPath: String?,
        iconPreset: Int,
        downloadLinks: List<String>,
        imagePaths: List<String>,
    ) {
        val index = projects.indexOfFirst { it.id == id }
        if (index < 0) return
        val existing = projects[index]
        val updated = existing.copy(
            name = name.trim(),
            details = details.trim(),
            customIconPath = customIconPath,
            iconPreset = iconPreset,
            downloadLinks = downloadLinks.filter { it.isNotBlank() },
            imagePaths = imagePaths.filter { it.isNotBlank() },
        )
        projects[index] = updated
        persistProjects()
        pushProjectToFirebase(updated)
        syncUserProfileToFirebase()
    }

    fun deleteStoreProject(id: String) {
        if (projects.removeAll { it.id == id }) {
            persistProjects()
            runCatching {
                firebaseDb?.getReference(FirebaseConfig.NODE_STORE_PROJECTS)
                    ?.child(id)
                    ?.removeValue()
            }
            syncUserProfileToFirebase()
        }
    }

    fun toggleSaveInCollection(id: String): Boolean {
        val index = projects.indexOfFirst { it.id == id }
        if (index < 0) return false
        val current = projects[index]
        val nextSaved = !current.savedInCollection
        projects[index] = current.copy(savedInCollection = nextSaved)
        persistProjects()
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
        return nextSaved
    }

    fun toggleLike(id: String) {
        val index = projects.indexOfFirst { it.id == id }
        if (index < 0) return
        val item = projects[index]
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
        projects[index] = updated
        persistProjects()
        pushProjectToFirebase(updated)
        runCatching {
            firebaseDb?.getReference(FirebaseConfig.NODE_REACTIONS)
                ?.child(currentUserId)
                ?.child(id)
                ?.setValue(updated.userReaction)
        }
        syncUserProfileToFirebase()
    }

    fun toggleDislike(id: String) {
        val index = projects.indexOfFirst { it.id == id }
        if (index < 0) return
        val item = projects[index]
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
        projects[index] = updated
        persistProjects()
        pushProjectToFirebase(updated)
        runCatching {
            firebaseDb?.getReference(FirebaseConfig.NODE_REACTIONS)
                ?.child(currentUserId)
                ?.child(id)
                ?.setValue(updated.userReaction)
        }
        syncUserProfileToFirebase()
    }

    private fun persistProjects() {
        val array = JSONArray()
        projects.forEach { item ->
            val json = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("details", item.details)
                put("iconPreset", item.iconPreset)
                put("customIconPath", item.customIconPath ?: JSONObject.NULL)
                put("language", item.language.name)
                put("downloadLinks", JSONArray(item.downloadLinks))
                put("imagePaths", JSONArray(item.imagePaths))
                put("likesCount", item.likesCount)
                put("dislikesCount", item.dislikesCount)
                put("userReaction", item.userReaction)
                put("savedInCollection", item.savedInCollection)
                put("createdByCurrentUser", item.createdByCurrentUser)
                put("authorName", item.authorName)
                put("createdAt", item.createdAt)
            }
            array.put(json)
        }
        prefs.edit().putString(KEY_PROJECTS, array.toString()).apply()
    }

    private fun parseProjects(raw: String): List<StoreProjectItem> {
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
            val lang = runCatching {
                ProjectLanguage.valueOf(obj.optString("language", ProjectLanguage.KOTLIN.name))
            }.getOrDefault(ProjectLanguage.KOTLIN)
            val customIcon = if (obj.isNull("customIconPath")) {
                null
            } else {
                obj.optString("customIconPath").ifBlank { null }
            }
            list.add(
                StoreProjectItem(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    details = obj.optString("details"),
                    iconPreset = obj.optInt("iconPreset", 0),
                    customIconPath = customIcon,
                    language = lang,
                    downloadLinks = links,
                    imagePaths = images,
                    likesCount = obj.optInt("likesCount", 0),
                    dislikesCount = obj.optInt("dislikesCount", 0),
                    userReaction = obj.optInt("userReaction", 0),
                    savedInCollection = obj.optBoolean("savedInCollection", false),
                    createdByCurrentUser = obj.optBoolean("createdByCurrentUser", true),
                    authorName = obj.optString("authorName", "AndroidEXL"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                ),
            )
        }
        return list
    }

    private fun defaultCodeSnippets(): List<CodeSnippetItem> = listOf(
        CodeSnippetItem(
            id = "snip_compose_auth",
            title = "NeonAuthUI",
            description = "شاشة تسجيل دخول زجاجية متحركة بـ Jetpack Compose",
            language = ProjectLanguage.KOTLIN,
            iconPreset = 3,
            code = """
                @Composable
                fun NeonAuthScreen(onLogin: (String) -> Unit) {
                    var email by remember { mutableStateOf("") }
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text("Welcome Back", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(value = email, onValueChange = { email = it })
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { onLogin(email) }) { Text("Sign In") }
                    }
                }
            """.trimIndent(),
        ),
        CodeSnippetItem(
            id = "snip_flutter_cart",
            title = "FlutterShopCart",
            description = "واجهة متجر إلكتروني وسلة مشتريات متكاملة بـ Flutter",
            language = ProjectLanguage.FLUTTER,
            iconPreset = 1,
            code = """
                class ShopCartPage extends StatefulWidget {
                  const ShopCartPage({super.key});
                  @override
                  State<ShopCartPage> createState() => _ShopCartPageState();
                }

                class _ShopCartPageState extends State<ShopCartPage> {
                  int count = 1;
                  @override
                  Widget build(BuildContext context) {
                    return Scaffold(
                      appBar: AppBar(title: const Text('Flutter Shop')),
                      floatingActionButton: FloatingActionButton(
                        onPressed: () => setState(() => count++),
                        child: Text('${'$'}count'),
                      ),
                    );
                  }
                }
            """.trimIndent(),
        ),
        CodeSnippetItem(
            id = "snip_java_notes",
            title = "JavaNotesHelper",
            description = "إدارة الملاحظات وحفظ البيانات محليًا بلغة Java",
            language = ProjectLanguage.JAVA,
            iconPreset = 2,
            code = """
                public class NotesRepository {
                    private final List<String> notes = new ArrayList<>();

                    public void addNote(String text) {
                        if (text != null && !text.trim().isEmpty()) {
                            notes.add(text.trim());
                        }
                    }

                    public List<String> getAllNotes() {
                        return Collections.unmodifiableList(notes);
                    }
                }
            """.trimIndent(),
        ),
        CodeSnippetItem(
            id = "snip_kotlin_api",
            title = "KotlinRestFetcher",
            description = "جلب بيانات JSON غير متزامن عبر Kotlin Coroutines",
            language = ProjectLanguage.KOTLIN,
            iconPreset = 0,
            code = """
                suspend fun fetchJson(url: String): Result<String> =
                    withContext(Dispatchers.IO) {
                        runCatching {
                            URL(url).openStream().bufferedReader().use { it.readText() }
                        }
                    }
            """.trimIndent(),
        ),
    )

    private fun defaultCustomLibraries(): List<CustomLibraryItem> = listOf(
        CustomLibraryItem(
            id = "lib_neon_ui",
            name = "IDE-NeonGlassKit",
            summary = "مكتبة بطاقات زجاجية وأنيميشن نيون جاهزة لمشاريع Jetpack Compose",
            language = ProjectLanguage.KOTLIN,
            version = "2.4.0",
            dependencyLine = "implementation(\"com.androidexl.libs:neon-glass-kit:2.4.0\")",
            docsUrl = "https://github.com/AndroidEXL/IDE-Ultimate",
        ),
        CustomLibraryItem(
            id = "lib_flutter_motion",
            name = "flutter_space_motion",
            summary = "حزمة أنيميشن وانتقالات ثلاثية الأبعاد لتطبيقات Flutter",
            language = ProjectLanguage.FLUTTER,
            version = "1.8.2",
            dependencyLine = "flutter_space_motion: ^1.8.2",
            docsUrl = "https://github.com/AndroidEXL/IDE-Ultimate",
        ),
        CustomLibraryItem(
            id = "lib_java_zip",
            name = "AndroidZipEngine",
            summary = "مكتبة Java سريعة لضغط وتصدير ملفات المشاريع بصيغة ZIP",
            language = ProjectLanguage.JAVA,
            version = "1.3.0",
            dependencyLine = "implementation(\"com.androidexl.libs:android-zip-engine:1.3.0\")",
            docsUrl = "https://github.com/AndroidEXL/IDE-Ultimate",
        ),
    )

    private fun seedDefaultStoreProjects() {
        projects.addAll(
            listOf(
                StoreProjectItem(
                    id = "proj_ide_ultimate_studio",
                    name = "IDE Ultimate Studio",
                    details = "مشروع بيئة تطوير متكاملة لنظام أندرويد مبني بـ Kotlin و Jetpack Compose مع شاشة Splash سينمائية، إدارة الفولدرات والمشاريع، وتصدير الكود إلى ZIP.",
                    iconPreset = 0,
                    language = ProjectLanguage.KOTLIN,
                    downloadLinks = listOf(
                        "https://github.com/AndroidEXL/IDE-Ultimate/releases/latest",
                    ),
                    imagePaths = listOf(
                        "https://raw.githubusercontent.com/AndroidEXL/IDE-Ultimate/arena/45f97ffd-ide-ultimate/docs/splash_preview.png",
                        "https://raw.githubusercontent.com/AndroidEXL/IDE-Ultimate/arena/45f97ffd-ide-ultimate/docs/workspace_preview.png",
                    ),
                    likesCount = 148,
                    dislikesCount = 3,
                    savedInCollection = true,
                    createdByCurrentUser = true,
                    authorName = "AndroidEXL",
                ),
                StoreProjectItem(
                    id = "proj_flutter_commerce_pro",
                    name = "Flutter Commerce Pro",
                    details = "تطبيق متجر إلكتروني كامل بـ Flutter و Material 3 يشمل سلة المشتريات، بوابات الدفع، وصفحة تفاصيل المنتجات مع أنيميشن سلس.",
                    iconPreset = 1,
                    language = ProjectLanguage.FLUTTER,
                    downloadLinks = listOf(
                        "https://github.com/AndroidEXL/IDE-Ultimate/releases",
                    ),
                    imagePaths = listOf(
                        "https://raw.githubusercontent.com/AndroidEXL/IDE-Ultimate/arena/45f97ffd-ide-ultimate/docs/project_detail_preview.png",
                    ),
                    likesCount = 92,
                    dislikesCount = 2,
                    savedInCollection = true,
                    createdByCurrentUser = true,
                    authorName = "AndroidEXL",
                ),
                StoreProjectItem(
                    id = "proj_java_file_explorer",
                    name = "Java File Explorer",
                    details = "مدير ملفات أندرويد سريع مبني بلغة Java يدعم ضغط وفك ضغط ملفات ZIP وتصفح الذاكرة الداخلية.",
                    iconPreset = 2,
                    language = ProjectLanguage.JAVA,
                    downloadLinks = listOf(
                        "https://github.com/AndroidEXL/IDE-Ultimate",
                    ),
                    imagePaths = listOf(
                        "https://raw.githubusercontent.com/AndroidEXL/IDE-Ultimate/arena/45f97ffd-ide-ultimate/docs/workspace_sheet_preview.png",
                    ),
                    likesCount = 64,
                    dislikesCount = 1,
                    savedInCollection = false,
                    createdByCurrentUser = false,
                    authorName = "CommunityDev",
                ),
            ),
        )
        persistProjects()
    }

    private companion object {
        const val KEY_PROJECTS = "store_projects_v1"
        const val KEY_USER_ID = "user_id_v1"
        const val KEY_USER_NAME = "user_name_v1"
        const val KEY_USER_AVATAR = "user_avatar_v1"
        const val KEY_LOGGED_IN = "user_logged_in_v1"
        const val KEY_SUBSCRIBERS = "user_subscribers_v1"
    }
}
