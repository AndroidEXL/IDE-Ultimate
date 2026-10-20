package com.androidexl.ideultimate.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.IndigoDeep
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.regex.Pattern

data class StringResourceItem(
    val key: String,
    val originalValue: String,
    var translatedValue: String = "",
)

data class LanguageOption(
    val code: String,
    val nameEn: String,
    val nameAr: String,
    val valuesDirSuffix: String, // e.g. "values-ar", "values-es", "values-fr"
)

val SUPPORTED_LANGUAGES = listOf(
    LanguageOption("auto", "Auto Detect", "كشف تلقائي", "values"),
    LanguageOption("en", "English", "الإنجليزية", "values"),
    LanguageOption("ar", "Arabic", "العربية", "values-ar"),
    LanguageOption("fr", "French", "الفرنسية", "values-fr"),
    LanguageOption("es", "Spanish", "الإسبانية", "values-es"),
    LanguageOption("de", "German", "الألمانية", "values-de"),
    LanguageOption("it", "Italian", "الإيطالية", "values-it"),
    LanguageOption("pt", "Portuguese", "البرتغالية", "values-pt"),
    LanguageOption("ru", "Russian", "الروسية", "values-ru"),
    LanguageOption("tr", "Turkish", "التركية", "values-tr"),
    LanguageOption("zh", "Chinese", "الصينية", "values-zh"),
    LanguageOption("ja", "Japanese", "اليابانية", "values-ja"),
    LanguageOption("ko", "Korean", "الكورية", "values-ko"),
    LanguageOption("hi", "Hindi", "الهندية", "values-hi"),
    LanguageOption("ur", "Urdu", "الأردية", "values-ur"),
    LanguageOption("fa", "Persian", "الفارسية", "values-fa"),
    LanguageOption("id", "Indonesian", "الإندونيسية", "values-in"),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IdeStringsTranslationScreen(
    project: WorkspaceItem,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val stringItems = remember { mutableStateListOf<StringResourceItem>() }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var sourceLang by rememberSaveable { mutableStateOf("auto") }
    var targetLang by rememberSaveable { mutableStateOf("ar") }
    var isTranslatingAll by rememberSaveable { mutableStateOf(false) }

    var selectedItemForAction by remember { mutableStateOf<StringResourceItem?>(null) }
    var showEditSheet by rememberSaveable { mutableStateOf(false) }
    var showSaveSheet by rememberSaveable { mutableStateOf(false) }
    var showAddNewSheet by rememberSaveable { mutableStateOf(false) }

    var sourceMenuExpanded by remember { mutableStateOf(false) }
    var targetMenuExpanded by remember { mutableStateOf(false) }

    // Read default res/values/strings.xml
    fun loadStringsFromProject() {
        coroutineScope.launch(Dispatchers.IO) {
            val root = repository.ensureProjectFullRoot(project)
            val resValuesDir = File(root, "app/src/main/res/values").apply { mkdirs() }
            val stringsFile = File(resValuesDir, "strings.xml")
            if (!stringsFile.exists()) {
                stringsFile.writeText(
                    """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">${project.name}</string>
    <string name="welcome_message">مرحباً بك في التطبيق</string>
    <string name="action_save">حفظ</string>
    <string name="action_cancel">إلغاء</string>
    <string name="loading">جارٍ التحميل...</string>
</resources>
""".trimIndent(),
                    Charsets.UTF_8,
                )
            }

            val content = stringsFile.readText(Charsets.UTF_8)
            val items = mutableListOf<StringResourceItem>()
            val matcher = Pattern.compile("<string\\s+name=\"([^\"]+)\"[^>]*>(.*?)</string>", Pattern.DOTALL)
                .matcher(content)
            while (matcher.find()) {
                val key = matcher.group(1)?.trim() ?: ""
                val value = matcher.group(2)?.trim() ?: ""
                if (key.isNotEmpty()) {
                    items.add(StringResourceItem(key = key, originalValue = value, translatedValue = ""))
                }
            }

            withContext(Dispatchers.Main) {
                stringItems.clear()
                stringItems.addAll(items)
            }
        }
    }

    LaunchedEffect(project.id) {
        loadStringsFromProject()
    }

    // Translation function
    suspend fun translateSingleText(text: String, src: String, tgt: String): String {
        if (text.isBlank()) return text
        return withContext(Dispatchers.IO) {
            try {
                val encoded = URLEncoder.encode(text, "UTF-8")
                val s = if (src == "auto") "auto" else src
                val urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=$s&tl=$tgt&dt=t&q=$encoded"
                val url = URL(urlStr)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 12000
                    setRequestProperty("User-Agent", "Mozilla/5.0")
                }
                if (conn.responseCode in 200..299) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                    val jsonStr = reader.readText()
                    reader.close()
                    val jsonArr = JSONArray(jsonStr)
                    val parts = jsonArr.getJSONArray(0)
                    val sb = StringBuilder()
                    for (i in 0 until parts.length()) {
                        val partArr = parts.getJSONArray(i)
                        sb.append(partArr.getString(0))
                    }
                    return@withContext sb.toString()
                }
            } catch (_: Exception) {}

            // Offline fallback / Dictionary common mappings
            val commonDict = mapOf(
                "app_name" to project.name,
                "save" to "حفظ",
                "cancel" to "إلغاء",
                "delete" to "حذف",
                "settings" to "الإعدادات",
                "welcome" to "مرحباً",
                "loading" to "جارٍ التحميل...",
                "search" to "بحث",
                "back" to "رجوع",
                "next" to "التالي",
                "done" to "تم",
                "error" to "خطأ",
                "ok" to "موافق",
            )
            val lower = text.lowercase().trim()
            commonDict[lower] ?: text
        }
    }

    fun translateAllStrings() {
        if (isTranslatingAll) return
        isTranslatingAll = true
        coroutineScope.launch {
            val updated = stringItems.map { item ->
                // Do NOT translate app_name (preserve original application name)
                if (item.key.equals("app_name", ignoreCase = true)) {
                    item.copy(translatedValue = item.originalValue)
                } else {
                    val base = if (item.originalValue.isNotBlank()) item.originalValue else item.key
                    val translated = translateSingleText(base, sourceLang, targetLang)
                    item.copy(translatedValue = translated)
                }
            }
            stringItems.clear()
            stringItems.addAll(updated)
            isTranslatingAll = false
            Toast.makeText(context, "تمت ترجمة جميع النصوص إلى ${targetLang.uppercase()} (مع الحفاظ على اسم التطبيق app_name) ✓", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveStringsToDisk(isNewLanguageFolder: Boolean) {
        coroutineScope.launch(Dispatchers.IO) {
            val root = repository.ensureProjectFullRoot(project)
            val targetFolder = if (isNewLanguageFolder) {
                val targetOption = SUPPORTED_LANGUAGES.firstOrNull { it.code == targetLang }
                val suffix = targetOption?.valuesDirSuffix ?: "values-$targetLang"
                File(root, "app/src/main/res/$suffix").apply { mkdirs() }
            } else {
                File(root, "app/src/main/res/values").apply { mkdirs() }
            }

            val targetFile = File(targetFolder, "strings.xml")
            val sb = StringBuilder()
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            sb.append("<resources>\n")
            stringItems.forEach { item ->
                val valToSave = if (isNewLanguageFolder) {
                    if (item.translatedValue.isNotBlank()) item.translatedValue else item.originalValue
                } else {
                    if (item.translatedValue.isNotBlank()) item.translatedValue else item.originalValue
                }
                val escaped = valToSave.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("'", "\\'")
                sb.append("    <string name=\"${item.key}\">$escaped</string>\n")
            }
            sb.append("</resources>\n")

            targetFile.writeText(sb.toString(), Charsets.UTF_8)

            withContext(Dispatchers.Main) {
                showSaveSheet = false
                val msg = if (isNewLanguageFolder) {
                    "تم إنشاء وحفظ مجلد اللغة الجديد: ${targetFolder.name}/strings.xml ✓"
                } else {
                    "تم تحديث وحفظ الملف الأساسي: res/values/strings.xml ✓"
                }
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    BackHandler(onBack = onBack)

    val filteredList = remember(searchQuery, stringItems) {
        if (searchQuery.isBlank()) {
            stringItems
        } else {
            val q = searchQuery.trim().lowercase()
            stringItems.filter {
                it.key.lowercase().contains(q) ||
                    it.originalValue.lowercase().contains(q) ||
                    it.translatedValue.lowercase().contains(q)
            }
        }
    }

    val sourceLabel = SUPPORTED_LANGUAGES.firstOrNull { it.code == sourceLang }?.nameAr ?: sourceLang
    val targetLabel = SUPPORTED_LANGUAGES.firstOrNull { it.code == targetLang }?.nameAr ?: targetLang

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar with Back & Save Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Midnight)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink,
                        )
                    }
                    Column {
                        Text(
                            text = "Strings Translation • ترجمة النصوص",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                            ),
                            color = Ink,
                        )
                        Text(
                            text = "${project.name} • ${stringItems.size} نص معرف",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.5.sp,
                            ),
                            color = Cyan,
                        )
                    }
                }

                // Top Save Button (Larger, prominent button with 20dp icon & 13.5sp text)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.25f))
                        .border(1.2.dp, Color(0xFF10B981), RoundedCornerShape(10.dp))
                        .clickable { showSaveSheet = true }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Save",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "حفظ النصوص",
                            color = Color(0xFF10B981),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                            ),
                        )
                    }
                }
            }

            // 2 Spinners Choose Bar (From Language -> To Language)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IndigoDeep)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Source Language Spinner
                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(IndigoDeep)
                            .border(1.dp, GlassStrokeTop, RoundedCornerShape(10.dp))
                            .clickable { sourceMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("من لغة:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Muted)
                            Text(sourceLabel, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Ink)
                        }
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Cyan)
                    }

                    DropdownMenu(
                        expanded = sourceMenuExpanded,
                        onDismissRequest = { sourceMenuExpanded = false },
                        modifier = Modifier.background(IndigoDeep),
                    ) {
                        SUPPORTED_LANGUAGES.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text("${lang.nameAr} (${lang.nameEn})", color = Ink, fontSize = 12.sp) },
                                onClick = {
                                    sourceLang = lang.code
                                    sourceMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                Text("➔", color = Cyan, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                // Target Language Spinner
                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(IndigoDeep)
                            .border(1.dp, Cyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { targetMenuExpanded = true }
                            .padding(horizontal = 10.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("إلى لغة:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Cyan)
                            Text(targetLabel, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Ink)
                        }
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Cyan)
                    }

                    DropdownMenu(
                        expanded = targetMenuExpanded,
                        onDismissRequest = { targetMenuExpanded = false },
                        modifier = Modifier.background(IndigoDeep),
                    ) {
                        SUPPORTED_LANGUAGES.filter { it.code != "auto" }.forEach { lang ->
                            DropdownMenuItem(
                                text = { Text("${lang.nameAr} (${lang.nameEn})", color = Ink, fontSize = 12.sp) },
                                onClick = {
                                    targetLang = lang.code
                                    targetMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            // Quick Search filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث في المفاتيح والقيم...", color = Muted, fontSize = 11.5.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Cyan, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = GlassStrokeTop,
                        focusedContainerColor = IndigoDeep,
                        unfocusedContainerColor = IndigoDeep,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                )

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(IndigoDeep)
                        .clickable { showAddNewSheet = true }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Cyan, modifier = Modifier.size(16.dp))
                        Text("إضافة نص", color = Ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Strings List
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "لا توجد نصوص strings.xml في المشروع" else "لم يتم العثور على نصوص مطابقة للبحث",
                        color = Muted,
                        fontSize = 12.sp,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredList, key = { it.key }) { item ->
                        StringResourceCard(
                            item = item,
                            onLongClick = {
                                selectedItemForAction = item
                                showEditSheet = true
                            },
                        )
                    }
                }
            }
        }

        // Floating Action Button (FAB) to translate all strings
        FloatingActionButton(
            onClick = { translateAllStrings() },
            containerColor = Cyan,
            contentColor = DeepNavy,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp),
        ) {
            if (isTranslatingAll) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = DeepNavy,
                    strokeWidth = 2.5.dp,
                )
            } else {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("ترجمة الكل", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("🌐", fontSize = 16.sp)
                }
            }
        }
    }

    // Bottom Sheet: Edit Value or Delete Item on Long Press
    if (showEditSheet && selectedItemForAction != null) {
        val currentItem = selectedItemForAction!!
        EditStringItemBottomSheet(
            item = currentItem,
            onDismiss = {
                showEditSheet = false
                selectedItemForAction = null
            },
            onSaveEdit = { newKey, newOrig, newTrans ->
                val index = stringItems.indexOfFirst { it.key == currentItem.key }
                if (index != -1) {
                    stringItems[index] = StringResourceItem(key = newKey, originalValue = newOrig, translatedValue = newTrans)
                    Toast.makeText(context, "تم تعديل النص بنجاح ✓", Toast.LENGTH_SHORT).show()
                }
                showEditSheet = false
                selectedItemForAction = null
            },
            onDelete = {
                stringItems.removeAll { it.key == currentItem.key }
                Toast.makeText(context, "تم حذف المتغير ${currentItem.key} ✓", Toast.LENGTH_SHORT).show()
                showEditSheet = false
                selectedItemForAction = null
            },
        )
    }

    // Bottom Sheet: Add New String Item
    if (showAddNewSheet) {
        AddNewStringBottomSheet(
            onDismiss = { showAddNewSheet = false },
            onAdd = { key, origVal ->
                if (stringItems.any { it.key == key }) {
                    Toast.makeText(context, "المفتاح $key موجود بالفعل!", Toast.LENGTH_SHORT).show()
                } else {
                    stringItems.add(StringResourceItem(key = key, originalValue = origVal, translatedValue = ""))
                    Toast.makeText(context, "تمت إضافة $key بنجاح ✓", Toast.LENGTH_SHORT).show()
                    showAddNewSheet = false
                }
            },
        )
    }

    // Bottom Sheet: Save Options (Primary values/strings.xml vs New Language values-xx/strings.xml)
    if (showSaveSheet) {
        SaveStringsBottomSheet(
            targetLangCode = targetLang,
            onDismiss = { showSaveSheet = false },
            onSaveToPrimary = {
                saveStringsToDisk(isNewLanguageFolder = false)
            },
            onSaveToNewLanguageFolder = {
                saveStringsToDisk(isNewLanguageFolder = true)
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StringResourceCard(
    item: StringResourceItem,
    onLongClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Midnight)
            .border(1.dp, GlassStrokeTop, RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onLongClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = item.key,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                ),
                color = Cyan,
            )
            Text(
                text = "ضغطة مطولة للتعديل",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Muted,
            )
        }

        Spacer(Modifier.height(4.dp))

        // Original Value
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("الأصل:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Muted)
            Text(
                text = item.originalValue.ifBlank { "(فارغ)" },
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Translated Value
        if (item.translatedValue.isNotBlank()) {
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("المترجم:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Color(0xFF10B981))
                Text(
                    text = item.translatedValue,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Color(0xFF6EE7B7),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditStringItemBottomSheet(
    item: StringResourceItem,
    onDismiss: () -> Unit,
    onSaveEdit: (newKey: String, newOrig: String, newTrans: String) -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var keyText by rememberSaveable { mutableStateOf(item.key) }
    var origText by rememberSaveable { mutableStateOf(item.originalValue) }
    var transText by rememberSaveable { mutableStateOf(item.translatedValue) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Midnight,
        contentColor = Ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .imePadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("تعديل أو حذف المتغير", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = Muted)
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = keyText,
                onValueChange = { keyText = it },
                label = { Text("اسم المتغير (Key)", color = Muted, fontSize = 11.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = GlassStrokeTop,
                    focusedContainerColor = IndigoDeep,
                    unfocusedContainerColor = IndigoDeep,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = origText,
                onValueChange = { origText = it },
                label = { Text("القيمة الأصلية (Original Value)", color = Muted, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = GlassStrokeTop,
                    focusedContainerColor = IndigoDeep,
                    unfocusedContainerColor = IndigoDeep,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = transText,
                onValueChange = { transText = it },
                label = { Text("القيمة المترجمة (Translated Value)", color = Color(0xFF10B981), fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = GlassStrokeTop,
                    focusedContainerColor = IndigoDeep,
                    unfocusedContainerColor = IndigoDeep,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Delete button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                        .clickable { onDelete() },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Text("حذف المتغير", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                    }
                }

                // Save button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Cyan)
                        .clickable(enabled = keyText.isNotBlank()) {
                            onSaveEdit(keyText.trim(), origText.trim(), transText.trim())
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("تأكيد التعديل", color = DeepNavy, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNewStringBottomSheet(
    onDismiss: () -> Unit,
    onAdd: (key: String, origVal: String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var keyText by rememberSaveable { mutableStateOf("") }
    var valText by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Midnight,
        contentColor = Ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .imePadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("إضافة متغير نصي جديد", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = Muted)
                }
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = keyText,
                onValueChange = { keyText = it },
                label = { Text("اسم المتغير (Key)", color = Muted, fontSize = 11.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = GlassStrokeTop,
                    focusedContainerColor = IndigoDeep,
                    unfocusedContainerColor = IndigoDeep,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = valText,
                onValueChange = { valText = it },
                label = { Text("النص / القيمة", color = Muted, fontSize = 11.sp) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = GlassStrokeTop,
                    focusedContainerColor = IndigoDeep,
                    unfocusedContainerColor = IndigoDeep,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (keyText.isNotBlank()) Cyan else IndigoDeep)
                    .clickable(enabled = keyText.isNotBlank()) {
                        onAdd(keyText.trim().replace(' ', '_'), valText.trim())
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("إضافة إلى النصوص", color = if (keyText.isNotBlank()) DeepNavy else Muted, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaveStringsBottomSheet(
    targetLangCode: String,
    onDismiss: () -> Unit,
    onSaveToPrimary: () -> Unit,
    onSaveToNewLanguageFolder: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val targetOption = SUPPORTED_LANGUAGES.firstOrNull { it.code == targetLangCode }
    val folderName = targetOption?.valuesDirSuffix ?: "values-$targetLangCode"
    val langTitle = targetOption?.nameAr ?: targetLangCode

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Midnight,
        contentColor = Ink,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("خيارات حفظ النصوص والترجمة", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                    Text("اختر أين تريد حفظ بيانات النصوص المترجمة", style = MaterialTheme.typography.bodySmall, color = Muted)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = Muted)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Option 1: Save to Primary res/values/strings.xml
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Midnight)
                    .border(1.dp, GlassStrokeTop, RoundedCornerShape(12.dp))
                    .clickable { onSaveToPrimary() }
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📄", fontSize = 20.sp)
                    Text("حفظ في الملف الأساسي", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Ink)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "سيتم تحديث الملف الافتراضي res/values/strings.xml مباشرة مع حفظ التعديلات",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Muted,
                )
            }

            Spacer(Modifier.height(10.dp))

            // Option 2: Create folder for new language res/values-xx/strings.xml
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(IndigoDeep)
                    .border(1.dp, Cyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable { onSaveToNewLanguageFolder() }
                    .padding(14.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📁", fontSize = 20.sp)
                    Text("إنشاء فولدر للغة الجديدة ($langTitle)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Cyan)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "سيتم إنشاء مجلد res/$folderName/ وتوليد ملف strings.xml مترجم يدعم التبديل اللغوي في أندرويد",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Muted,
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
