package com.androidexl.ideultimate.ui.screens

import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.IndigoDeep
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.Muted
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.CodeEditorFlavor
import com.androidexl.ideultimate.data.IdeLogicItem
import com.androidexl.ideultimate.data.IdeLogicSection
import com.androidexl.ideultimate.data.ProjectResourceEntry
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IdeFilesManagerScreen(
    project: WorkspaceItem,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentPath by rememberSaveable { mutableStateOf("") }
    var fileEditingEntry by remember { mutableStateOf<ProjectResourceEntry?>(null) }

    if (fileEditingEntry != null) {
        val editingEntry = fileEditingEntry!!
        IdeProjectSingleFileEditor(
            project = project,
            repository = repository,
            entry = editingEntry,
            onBack = {
                fileEditingEntry = null
            },
            modifier = modifier,
        )
        return
    }

    BackHandler {
        if (currentPath.isNotEmpty()) {
            val parent = currentPath.substringBeforeLast('/', "")
            currentPath = parent
        } else {
            onBack()
        }
    }

    var entries by remember(project.id, currentPath) {
        mutableStateOf(repository.listProjectFiles(project, currentPath))
    }

    fun refreshList() {
        entries = repository.listProjectFiles(project, currentPath)
    }

    var longPressedEntry by remember { mutableStateOf<ProjectResourceEntry?>(null) }
    var showRenameDialog by remember { mutableStateOf<ProjectResourceEntry?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ProjectResourceEntry?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Midnight)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        if (currentPath.isNotEmpty()) {
                            currentPath = currentPath.substringBeforeLast('/', "")
                        } else {
                            onBack()
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                    )
                }

                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Files Manager",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        ),
                        color = Ink,
                    )
                    Text(
                        text = if (currentPath.isEmpty()) "${project.name} (Root)" else "${project.name}/$currentPath",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Cyan,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Add File/Folder button in header
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New",
                        tint = Cyan,
                    )
                }
            }

            // Path Breadcrumbs Bar
            if (currentPath.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IndigoDeep)
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "root",
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        color = Cyan,
                        modifier = Modifier.clickable { currentPath = "" },
                    )
                    val segments = currentPath.split('/')
                    var accumulated = ""
                    segments.forEach { segment ->
                        accumulated = if (accumulated.isEmpty()) segment else "$accumulated/$segment"
                        val thisPath = accumulated
                        Text(text = "/", color = Muted, fontSize = 10.sp)
                        Text(
                            text = segment,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = if (thisPath == currentPath) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (thisPath == currentPath) Ink else Cyan,
                            modifier = Modifier.clickable { currentPath = thisPath },
                        )
                    }
                }
            }

            // Entries List
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "المجلد فارغ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(entries, key = { it.relativePath }) { item ->
                        val badge = when {
                            item.isDirectory -> "DIR"
                            item.name.endsWith(".xml", ignoreCase = true) -> "XML"
                            item.name.endsWith(".kt", ignoreCase = true) -> "KT"
                            item.name.endsWith(".java", ignoreCase = true) -> "JAVA"
                            item.name.endsWith(".gradle", ignoreCase = true) || item.name.endsWith(".kts", ignoreCase = true) -> "GRADLE"
                            item.name.endsWith(".json", ignoreCase = true) -> "JSON"
                            else -> "FILE"
                        }
                        val badgeColor = when {
                            item.isDirectory -> Color(0xFF38BDF8)
                            item.name.endsWith(".xml", ignoreCase = true) -> Color(0xFFFB923C)
                            item.name.endsWith(".kt", ignoreCase = true) -> Color(0xFFA78BFA)
                            item.name.endsWith(".java", ignoreCase = true) -> Color(0xFFF472B6)
                            else -> Muted
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Midnight)
                                .border(1.dp, GlassStrokeTop, RoundedCornerShape(14.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (item.isDirectory) {
                                            currentPath = item.relativePath
                                        } else {
                                            fileEditingEntry = item
                                        }
                                    },
                                    onLongClick = {
                                        longPressedEntry = item
                                    },
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(badgeColor.copy(alpha = 0.16f))
                                    .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = badgeColor,
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                    ),
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = if (item.isDirectory) "${item.childCount} عنصر" else "${item.sizeBytes} بايت",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Muted,
                                )
                            }
                        }
                    }
                }
            }
        }

        // FAB to add new file/folder
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = Cyan,
            contentColor = Color(0xFF050816),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add File/Folder",
                modifier = Modifier.size(24.dp),
            )
        }
    }

    // Long Press Bottom Sheet
    val entryToManage = longPressedEntry
    if (entryToManage != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { longPressedEntry = null },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            scrimColor = Color(0xCC03060F),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = entryToManage.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )
                Text(
                    text = entryToManage.relativePath,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                    color = Cyan,
                )

                Spacer(Modifier.height(4.dp))

                // Rename
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(IndigoDeep)
                        .clickable {
                            longPressedEntry = null
                            showRenameDialog = entryToManage
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = "تعديل الاسم",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                        Text(
                            text = "تغيير اسم الملف أو المجلد",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }

                // Delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF38101E))
                        .clickable {
                            longPressedEntry = null
                            showDeleteConfirmDialog = entryToManage
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(20.dp),
                    )
                    Column {
                        Text(
                            text = "حذف العنصر",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFF87171),
                        )
                        Text(
                            text = "حذف الملف أو المجلد بجميع محتوياته نهائياً",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }
            }
        }
    }

    // Rename Dialog
    val itemToRename = showRenameDialog
    if (itemToRename != null) {
        var inputName by rememberSaveable { mutableStateOf(itemToRename.name) }
        var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "تعديل الاسم",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = {
                            inputName = it
                            errorMsg = null
                        },
                        label = { Text("الاسم الجديد") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                    if (errorMsg != null) {
                        Text(
                            text = errorMsg.orEmpty(),
                            color = Color(0xFFF87171),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ok = repository.renameProjectFileOrFolder(project, itemToRename.relativePath, inputName)
                        if (ok) {
                            refreshList()
                            showRenameDialog = null
                        } else {
                            errorMsg = "الاسم موجود مسبقاً أو غير صالح"
                        }
                    },
                ) {
                    Text("حفظ", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }

    // Delete Confirmation Dialog
    val itemToDelete = showDeleteConfirmDialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "حذف العنصر",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF87171),
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف ${itemToDelete.name}؟ لا يمكن التراجع عن هذه الخطوة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.deleteProjectFileOrFolder(project, itemToDelete.relativePath)
                        refreshList()
                        showDeleteConfirmDialog = null
                    },
                ) {
                    Text("حذف", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }

    // Create File/Folder Dialog
    if (showCreateDialog) {
        var inputName by rememberSaveable { mutableStateOf("") }
        var isDir by rememberSaveable { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = if (isDir) "إنشاء مجلد جديد" else "إنشاء ملف جديد",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isDir) Cyan.copy(alpha = 0.2f) else GlassStrokeTop)
                                .border(1.dp, if (!isDir) Cyan else GlassStrokeTop, RoundedCornerShape(10.dp))
                                .clickable { isDir = false }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "ملف (File)",
                                color = if (!isDir) Cyan else Muted,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isDir) Cyan.copy(alpha = 0.2f) else GlassStrokeTop)
                                .border(1.dp, if (isDir) Cyan else GlassStrokeTop, RoundedCornerShape(10.dp))
                                .clickable { isDir = true }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "مجلد (Folder)",
                                color = if (isDir) Cyan else Muted,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text(if (isDir) "اسم المجلد (e.g. models)" else "اسم الملف (e.g. User.kt / layout.xml)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = GlassStrokeTop,
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                        ),
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            repository.createProjectFileOrFolder(project, currentPath, inputName, isDir)
                            refreshList()
                        }
                        showCreateDialog = false
                    },
                ) {
                    Text("إنشاء", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }
}

@Composable
private fun IdeProjectSingleFileEditor(
    project: WorkspaceItem,
    repository: WorkspaceRepository,
    entry: ProjectResourceEntry,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    val initialText = remember(entry.relativePath) {
        repository.readProjectFileText(project, entry.relativePath)
    }

    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(initialText, selection = TextRange(initialText.length)))
    }

    val undoStack = remember { mutableStateListOf<TextFieldValue>() }
    val redoStack = remember { mutableStateListOf<TextFieldValue>() }
    var showAutocomplete by rememberSaveable { mutableStateOf(false) }

    fun updateContent(newValue: TextFieldValue, recordHistory: Boolean = true) {
        if (recordHistory && newValue.text != textFieldValue.text) {
            undoStack.add(textFieldValue)
            redoStack.clear()
        }
        textFieldValue = newValue
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(textFieldValue)
            textFieldValue = prev
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(textFieldValue)
            textFieldValue = next
        }
    }

    val isXml = entry.name.endsWith(".xml", ignoreCase = true)
    val isKotlin = entry.name.endsWith(".kt", ignoreCase = true)
    val isJava = entry.name.endsWith(".java", ignoreCase = true)

    val flavor = when {
        isXml -> CodeEditorFlavor.XML_ANDROID
        isJava -> CodeEditorFlavor.JAVA_ANDROID
        else -> CodeEditorFlavor.KOTLIN_ANDROID
    }

    val beforeCursor = remember(textFieldValue.text, textFieldValue.selection.start) {
        val cursor = textFieldValue.selection.start.coerceIn(0, textFieldValue.text.length)
        textFieldValue.text.substring(0, cursor)
    }

    val dummyLogicItem = remember(entry.name) {
        IdeLogicItem(
            name = entry.name.substringBeforeLast('.'),
            subtitle = entry.name,
            section = IdeLogicSection.ACTIVITY,
        )
    }

    val suggestions = remember(beforeCursor, isXml) {
        if (beforeCursor.isBlank()) {
            emptyList()
        } else if (isXml) {
            AndroidXmlCompletions.computeXmlCompletions(beforeCursor, isManifest = entry.name.equals("AndroidManifest.xml", ignoreCase = true))
        } else if (isKotlin || isJava) {
            computeAndroidCodeCompletions(
                textBeforeCursor = beforeCursor,
                allWidgets = emptyList(),
                userFunctions = emptyList(),
                currentItem = dummyLogicItem,
            )
        } else {
            emptyList()
        }
    }

    fun applyCompletion(sug: IdeCodeCompletionSuggestion) {
        val curText = textFieldValue.text
        val cursor = textFieldValue.selection.start
        val before = curText.substring(0, cursor)
        val after = curText.substring(cursor)

        val tokenMatch = Regex("([<a-zA-Z0-9_.:\"-]+)$").find(before)
        val (prefix, insert) = if (tokenMatch != null) {
            val tok = tokenMatch.value
            val isTag = tok.startsWith("<")
            val p = before.dropLast(tok.length)
            val ins = if (isTag && sug.insertText.startsWith("<")) {
                sug.insertText
            } else if (isTag) {
                "<" + sug.insertText
            } else {
                sug.insertText
            }
            p to ins
        } else {
            before to sug.insertText
        }

        val newText = prefix + insert + after
        val newCursor = (prefix.length + insert.length - sug.cursorBackOffset).coerceIn(0, newText.length)
        updateContent(TextFieldValue(newText, TextRange(newCursor)))
        showAutocomplete = false
    }

    val linesCount = remember(textFieldValue.text) {
        textFieldValue.text.count { it == '\n' } + 1
    }
    val verticalScroll = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .imePadding(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Midnight)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Ink,
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                    ),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.relativePath,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                    ),
                    color = Cyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Undo
            IconButton(
                onClick = { handleUndo() },
                enabled = undoStack.isNotEmpty(),
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = IdeIcons.Undo,
                    contentDescription = "Undo",
                    tint = if (undoStack.isNotEmpty()) Cyan else Muted,
                    modifier = Modifier.size(18.dp),
                )
            }

            // Redo
            IconButton(
                onClick = { handleRedo() },
                enabled = redoStack.isNotEmpty(),
                modifier = Modifier.size(34.dp),
            ) {
                Icon(
                    imageVector = IdeIcons.Redo,
                    contentDescription = "Redo",
                    tint = if (redoStack.isNotEmpty()) Cyan else Muted,
                    modifier = Modifier.size(18.dp),
                )
            }

            // Save
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF22C55E).copy(alpha = 0.2f))
                    .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .clickable {
                        repository.writeProjectFileText(project, entry.relativePath, textFieldValue.text)
                        Toast.makeText(context, "تم حفظ ${entry.name} بنجاح", Toast.LENGTH_SHORT).show()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Save",
                        tint = Color(0xFF22C55E),
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "حفظ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                        ),
                        color = Color(0xFF22C55E),
                    )
                }
            }
        }

        // Editor Body (LTR for code and line numbers)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(DeepNavy),
            ) {
                val minEditorHeight = (linesCount * 20).coerceAtLeast(300).dp
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScroll)
                        .padding(vertical = 8.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 6.dp)
                            .defaultMinSize(minHeight = minEditorHeight),
                        horizontalAlignment = Alignment.End,
                    ) {
                        for (line in 1..linesCount) {
                            Text(
                                text = line.toString(),
                                style = TextStyle(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.5.sp,
                                    lineHeight = 20.sp,
                                    color = Muted,
                                ),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(minEditorHeight)
                            .background(GlassStrokeTop),
                    )

                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newVal ->
                            updateContent(newVal)
                            showAutocomplete = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = minEditorHeight)
                            .padding(start = 10.dp, end = 12.dp),
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            lineHeight = 20.sp,
                            color = Ink,
                        ),
                        cursorBrush = SolidColor(Cyan),
                        visualTransformation = AndroidCodeSyntaxTransformation(flavor),
                    )
                }

                if (showAutocomplete && suggestions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Midnight)
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0x2638BDF8))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = "مكمل النصوص الذكي (${suggestions.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                    ),
                                    color = Cyan,
                                )
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = Muted,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clickable { showAutocomplete = false },
                                )
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 140.dp),
                            ) {
                                items(suggestions) { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { applyCompletion(item) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(item.category.color.copy(alpha = 0.18f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp),
                                        ) {
                                            Text(
                                                text = item.category.badge.substringBefore(" •"),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = item.category.color,
                                            )
                                        }

                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontFamily = JetBrainsMono,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                ),
                                                color = Ink,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            if (item.detail.isNotEmpty()) {
                                                Text(
                                                    text = item.detail,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                                    color = Muted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Symbols bar
        val symbols = if (isXml) {
            listOf("<", ">", "\"", "=", "/", ":", "android:", "LinearLayout", "TextView", "Button")
        } else {
            listOf("(", ")", "{", "}", ";", ".", ",", "\"", "=", "fun", "val", "var")
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepNavy)
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            symbols.forEach { sym ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(IndigoDeep)
                        .border(1.dp, GlassStrokeTop, RoundedCornerShape(6.dp))
                        .clickable {
                            val cur = textFieldValue.text
                            val c = textFieldValue.selection.start
                            val updated = cur.substring(0, c) + sym + cur.substring(c)
                            updateContent(TextFieldValue(updated, TextRange(c + sym.length)))
                            showAutocomplete = true
                        }
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = sym,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Cyan,
                    )
                }
            }
        }
    }
}
