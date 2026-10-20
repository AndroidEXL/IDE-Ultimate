package com.androidexl.ideultimate.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.IndigoDeep
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.Muted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Locale
import javax.xml.parsers.DocumentBuilderFactory

data class ValueEntryItem(
    val tagName: String, // e.g. "color", "string", "dimen", "style", "bool", "integer"
    val name: String,
    val value: String,
)

enum class ValuesXmlFile(val fileName: String, val displayName: String, val defaultTag: String) {
    COLORS("colors.xml", "الألوان (colors.xml)", "color"),
    STRINGS("strings.xml", "النصوص (strings.xml)", "string"),
    DIMENS("dimens.xml", "الأبعاد (dimens.xml)", "dimen"),
    STYLES("styles.xml", "الأنماط (styles.xml)", "style"),
    BOOLS("bools.xml", "القيم المنطقية (bools.xml)", "bool"),
    INTEGERS("integers.xml", "الأرقام (integers.xml)", "integer"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IdeValuesManagerScreen(
    project: WorkspaceItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val repository = remember { WorkspaceRepository(context) }
    val scope = rememberCoroutineScope()

    var selectedFile by remember { mutableStateOf(ValuesXmlFile.COLORS) }
    var fileDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val entries = remember { mutableStateListOf<ValueEntryItem>() }
    var isLoading by remember { mutableStateOf(false) }

    // Dialog & BottomSheet state
    var activeBottomSheetItem by remember { mutableStateOf<ValueEntryItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<ValueEntryItem?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    fun loadValuesForCurrentFile() {
        isLoading = true
        scope.launch(Dispatchers.IO) {
            val root = repository.ensureProjectResRoot(project)
            val file = File(root, "values/${selectedFile.fileName}")
            val loaded = mutableListOf<ValueEntryItem>()
            if (file.exists()) {
                runCatching {
                    val content = file.readText(Charsets.UTF_8)
                    val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    val doc = db.parse(ByteArrayInputStream(content.toByteArray(Charsets.UTF_8)))
                    val rootEl = doc.documentElement
                    val children = rootEl.childNodes
                    for (i in 0 until children.length) {
                        val node = children.item(i)
                        if (node is Element) {
                            val name = node.getAttribute("name")
                            val text = node.textContent?.trim() ?: ""
                            if (name.isNotBlank()) {
                                loaded.add(
                                    ValueEntryItem(
                                        tagName = node.tagName,
                                        name = name,
                                        value = text,
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // If missing, initialize standard templates
                file.parentFile?.mkdirs()
                val initialContent = when (selectedFile) {
                    ValuesXmlFile.COLORS -> """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="primary">#6200EE</color>
    <color name="primary_variant">#3700B3</color>
    <color name="secondary">#03DAC6</color>
    <color name="white">#FFFFFFFF</color>
    <color name="black">#FF000000</color>
</resources>"""
                    ValuesXmlFile.STRINGS -> """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">${project.name}</string>
</resources>"""
                    ValuesXmlFile.DIMENS -> """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <dimen name="margin_small">8dp</dimen>
    <dimen name="margin_normal">16dp</dimen>
    <dimen name="text_size_normal">14sp</dimen>
</resources>"""
                    ValuesXmlFile.STYLES -> """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="AppTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
    </style>
</resources>"""
                    ValuesXmlFile.BOOLS -> """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <bool name="is_tablet">false</bool>
</resources>"""
                    ValuesXmlFile.INTEGERS -> """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <integer name="max_count">100</integer>
</resources>"""
                }
                runCatching { file.writeText(initialContent, Charsets.UTF_8) }
                return@launch loadValuesForCurrentFile()
            }

            // Sort keys alphabetically
            val sorted = loaded.sortedBy { it.name.lowercase(Locale.ROOT) }

            withContext(Dispatchers.Main) {
                entries.clear()
                entries.addAll(sorted)
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedFile) {
        loadValuesForCurrentFile()
    }

    fun saveCurrentValuesToFile(updatedList: List<ValueEntryItem>) {
        scope.launch(Dispatchers.IO) {
            val root = repository.ensureProjectResRoot(project)
            val file = File(root, "values/${selectedFile.fileName}")
            file.parentFile?.mkdirs()

            val sb = StringBuilder()
            sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
            sb.append("<resources>\n")
            updatedList.sortedBy { it.name.lowercase(Locale.ROOT) }.forEach { item ->
                sb.append("    <${item.tagName} name=\"${item.name}\">${item.value}</${item.tagName}>\n")
            }
            sb.append("</resources>\n")

            runCatching {
                file.writeText(sb.toString(), Charsets.UTF_8)
                repository.writeProjectResourceFile(project, "values/${selectedFile.fileName}", sb.toString())
            }

            withContext(Dispatchers.Main) {
                loadValuesForCurrentFile()
            }
        }
    }

    val filteredEntries = entries.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.value.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Midnight),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Values Manager",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                    ),
                    color = Ink,
                )
                Text(
                    text = "إدارة ملفات الموارد والقيم (${selectedFile.fileName})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Muted,
                )
            }

            // Spinner / Selector for values files
            Box {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(IndigoDeep)
                        .border(1.dp, Cyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { fileDropdownExpanded = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = selectedFile.fileName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono,
                        ),
                        color = Cyan,
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = Cyan,
                        modifier = Modifier.size(18.dp),
                    )
                }

                DropdownMenu(
                    expanded = fileDropdownExpanded,
                    onDismissRequest = { fileDropdownExpanded = false },
                    modifier = Modifier.background(Midnight),
                ) {
                    ValuesXmlFile.entries.forEach { opt ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = opt.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = if (opt == selectedFile) FontWeight.Bold else FontWeight.Normal,
                                    ),
                                    color = if (opt == selectedFile) Cyan else Ink,
                                )
                            },
                            onClick = {
                                selectedFile = opt
                                fileDropdownExpanded = false
                            },
                        )
                    }
                }
            }
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("بحث عن اسم أو قيمة...", style = MaterialTheme.typography.bodySmall, color = Muted)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Muted,
                        modifier = Modifier.size(18.dp),
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Muted, modifier = Modifier.size(18.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Cyan,
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedContainerColor = Midnight,
                    unfocusedContainerColor = Midnight,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink,
                ),
            )
        }

        // Entries List or Empty Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (filteredEntries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (searchQuery.isBlank()) "لا توجد عناصر في ${selectedFile.fileName}\nاضغط زر (+) لإضافة عنصر جديد" else "لم يتم العثور على نتائج تطابق البحث",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredEntries, key = { it.name }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Midnight)
                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                                .combinedClickable(
                                    onClick = {
                                        activeBottomSheetItem = item
                                    },
                                    onLongClick = {
                                        activeBottomSheetItem = item
                                    },
                                )
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            // Tag badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(IndigoDeep)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = item.tagName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Cyan,
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = JetBrainsMono,
                                    ),
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    text = item.value,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMono,
                                    ),
                                    color = Muted,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            // If color, preview colored box
                            if (item.tagName == "color" && (item.value.startsWith("#") && (item.value.length == 7 || item.value.length == 9))) {
                                val parsedColor = runCatching {
                                    Color(android.graphics.Color.parseColor(item.value))
                                }.getOrNull()
                                if (parsedColor != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(parsedColor)
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // FAB to Add new Value
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                containerColor = Cyan,
                contentColor = DeepNavy,
                shape = CircleShape,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Value")
            }
        }
    }

    // Bottom Sheet on Long Press or Click
    val sheetItem = activeBottomSheetItem
    if (sheetItem != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { activeBottomSheetItem = null },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0x44FFFFFF)),
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = sheetItem.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMono,
                    ),
                    color = Ink,
                )
                Text(
                    text = "القيمة: ${sheetItem.value}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                    ),
                    color = Muted,
                )

                Spacer(Modifier.height(4.dp))

                // Edit Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(IndigoDeep)
                        .clickable {
                            editingItem = sheetItem
                            activeBottomSheetItem = null
                            showEditDialog = true
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, tint = Cyan)
                    Text(
                        text = "تعديل الاسم أو القيمة",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                }

                // Delete Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x33EF4444))
                        .clickable {
                            val updated = entries.filterNot { it.name == sheetItem.name }
                            saveCurrentValuesToFile(updated)
                            activeBottomSheetItem = null
                            Toast.makeText(context, "تم حذف ${sheetItem.name}", Toast.LENGTH_SHORT).show()
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                    Text(
                        text = "حذف العنصر نهائياً",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFEF4444),
                    )
                }
            }
        }
    }

    // Create New Value Dialog
    if (showCreateDialog) {
        var newTagName by remember { mutableStateOf(selectedFile.defaultTag) }
        var newName by remember { mutableStateOf("") }
        var newValue by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            title = {
                Text("إضافة قيمة جديدة (${selectedFile.fileName})", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it.trim() },
                        label = { Text("نوع الوسم (Tag Name)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it.trim() },
                        label = { Text("اسم المفتاح (name)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("القيمة (value)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanName = newName.trim()
                        if (cleanName.isNotBlank()) {
                            val updated = entries.filterNot { it.name == cleanName } + ValueEntryItem(
                                tagName = newTagName.ifBlank { selectedFile.defaultTag },
                                name = cleanName,
                                value = newValue,
                            )
                            saveCurrentValuesToFile(updated)
                            showCreateDialog = false
                            Toast.makeText(context, "تمت الإضافة بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text("إضافة", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }

    // Edit Item Dialog
    val itemToEdit = editingItem
    if (showEditDialog && itemToEdit != null) {
        var editName by remember { mutableStateOf(itemToEdit.name) }
        var editVal by remember { mutableStateOf(itemToEdit.value) }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            title = {
                Text("تعديل العنصر", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it.trim() },
                        label = { Text("اسم المفتاح (name)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = editVal,
                        onValueChange = { editVal = it },
                        label = { Text("القيمة (value)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cleanName = editName.trim()
                        if (cleanName.isNotBlank()) {
                            val updated = entries.filterNot { it.name == itemToEdit.name } + ValueEntryItem(
                                tagName = itemToEdit.tagName,
                                name = cleanName,
                                value = editVal,
                            )
                            saveCurrentValuesToFile(updated)
                            showEditDialog = false
                            Toast.makeText(context, "تم حفظ التعديل بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    },
                ) {
                    Text("حفظ", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }
}
