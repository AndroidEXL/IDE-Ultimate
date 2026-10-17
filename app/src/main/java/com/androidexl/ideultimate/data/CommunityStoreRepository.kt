package com.androidexl.ideultimate.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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
 * Persistent repository for:
 * 1. New Store Projects (قسم المشاريع الجديدة)
 * 2. Code Snippets (قسم الأكواد البرمجية)
 * 3. Custom Libraries (قسم المكتبات الخاصة)
 * 4. User Profile (Projects tab, Collection tab, Likes/Dislikes/Subscribers stats)
 */
class CommunityStoreRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("ide_ultimate_community_v1", Context.MODE_PRIVATE)

    var userName by mutableStateOf(prefs.getString(KEY_USER_NAME, "AndroidEXL") ?: "AndroidEXL")
        private set

    var userAvatarPath by mutableStateOf(prefs.getString(KEY_USER_AVATAR, null))
        private set

    var isLoggedIn by mutableStateOf(prefs.getBoolean(KEY_LOGGED_IN, true))
        private set

    var subscribersCount by mutableIntStateOf(prefs.getInt(KEY_SUBSCRIBERS, 1280))
        private set

    val projects: SnapshotStateList<StoreProjectItem> = mutableStateListOf()

    val codeSnippets: List<CodeSnippetItem> = listOf(
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

    val customLibraries: List<CustomLibraryItem> = listOf(
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

    init {
        val raw = prefs.getString(KEY_PROJECTS, null)
        if (raw == null) {
            seedDefaultStoreProjects()
        } else {
            projects.addAll(runCatching { parseProjects(raw) }.getOrDefault(emptyList()))
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
    }

    fun toggleLogin() {
        isLoggedIn = !isLoggedIn
        prefs.edit().putBoolean(KEY_LOGGED_IN, isLoggedIn).apply()
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
        projects[index] = existing.copy(
            name = name.trim(),
            details = details.trim(),
            customIconPath = customIconPath,
            iconPreset = iconPreset,
            downloadLinks = downloadLinks.filter { it.isNotBlank() },
            imagePaths = imagePaths.filter { it.isNotBlank() },
        )
        persistProjects()
    }

    fun deleteStoreProject(id: String) {
        if (projects.removeAll { it.id == id }) {
            persistProjects()
        }
    }

    fun toggleSaveInCollection(id: String): Boolean {
        val index = projects.indexOfFirst { it.id == id }
        if (index < 0) return false
        val current = projects[index]
        val nextSaved = !current.savedInCollection
        projects[index] = current.copy(savedInCollection = nextSaved)
        persistProjects()
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

    private fun seedDefaultStoreProjects() {
        projects.addAll(
            listOf(
                StoreProjectItem(
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
        const val KEY_USER_NAME = "user_name_v1"
        const val KEY_USER_AVATAR = "user_avatar_v1"
        const val KEY_LOGGED_IN = "user_logged_in_v1"
        const val KEY_SUBSCRIBERS = "user_subscribers_v1"
    }
}
