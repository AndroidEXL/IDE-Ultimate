package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.ProjectXmlPage
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IdeActivityManagerScreen(
    project: WorkspaceItem,
    repository: WorkspaceRepository,
    xmlPages: SnapshotStateList<ProjectXmlPage>,
    selectedXmlFileName: String,
    onSelectPage: (ProjectXmlPage) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onBack)

    var longPressedPage by remember { mutableStateOf<ProjectXmlPage?>(null) }
    var showRenameDialog by remember { mutableStateOf<ProjectXmlPage?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf<ProjectXmlPage?>(null) }
    var showAddPageDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF070B1E)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0C1330))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ink,
                    )
                }
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Activity Manager",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMono,
                        ),
                        color = Ink,
                    )
                    Text(
                        text = "إدارة صفحات XML و Java/Kotlin Activity في المشروع (${xmlPages.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = Cyan,
                    )
                }
            }

            // List of Pages
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(xmlPages, key = { it.xmlFileName }) { page ->
                    val isCurrent = page.xmlFileName == selectedXmlFileName
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isCurrent) Color(0xFF162552) else Color(0xFF101838))
                            .border(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) Cyan else Color(0x24FFFFFF),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .combinedClickable(
                                onClick = { onSelectPage(page) },
                                onLongClick = { longPressedPage = page },
                            )
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // XML Badge Box
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFB923C).copy(alpha = 0.16f))
                                .border(1.dp, Color(0xFFFB923C).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "XML",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                ),
                                color = Color(0xFFFB923C),
                            )
                        }

                        // Names: XML filename on top, Java/Kotlin Activity file below
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = page.xmlFileName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                    ),
                                    color = Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Cyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            text = "الحالية",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = Cyan,
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF38BDF8)),
                                )
                                Text(
                                    text = page.codeFileName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 11.5.sp,
                                    ),
                                    color = Color(0xFF93C5FD),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        if (isCurrent) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Cyan,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add New Activity / XML page
        FloatingActionButton(
            onClick = { showAddPageDialog = true },
            containerColor = Cyan,
            contentColor = Color(0xFF050816),
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Activity",
                modifier = Modifier.size(24.dp),
            )
        }
    }

    // Long Press Bottom Sheet: Rename or Delete
    val pageToManage = longPressedPage
    if (pageToManage != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { longPressedPage = null },
            sheetState = sheetState,
            containerColor = Color(0xFF0E1430),
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
                    text = pageToManage.xmlFileName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )
                Text(
                    text = "Activity: ${pageToManage.codeFileName}",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                    color = Cyan,
                )

                Spacer(Modifier.height(4.dp))

                // Rename Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF172044))
                        .clickable {
                            longPressedPage = null
                            showRenameDialog = pageToManage
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
                            text = "تغيير اسم ملف XML و Activity المرتبط به",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }

                // Delete Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF38101E))
                        .clickable {
                            longPressedPage = null
                            showDeleteConfirmDialog = pageToManage
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
                            text = "حذف صفحة XML وملف الكود المرتبط من المشروع نهائياً",
                            style = MaterialTheme.typography.labelSmall,
                            color = Muted,
                        )
                    }
                }
            }
        }
    }

    // Rename Dialog
    val pageToRename = showRenameDialog
    if (pageToRename != null) {
        var inputName by rememberSaveable { mutableStateOf(pageToRename.layoutResourceName) }
        var errorMsg by rememberSaveable { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "تعديل اسم الصفحة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "الملف الحالي: ${pageToRename.xmlFileName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMono),
                        color = Muted,
                    )
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = {
                            inputName = it
                            errorMsg = null
                        },
                        label = { Text("الاسم الجديد (e.g. activity_profile)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = Color(0x44FFFFFF),
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
                        val renamed = repository.renameProjectXmlPage(project, pageToRename.xmlFileName, inputName)
                        if (renamed != null) {
                            val idx = xmlPages.indexOfFirst { it.xmlFileName == pageToRename.xmlFileName }
                            if (idx >= 0) {
                                xmlPages[idx] = renamed
                            }
                            if (selectedXmlFileName == pageToRename.xmlFileName) {
                                onSelectPage(renamed)
                            }
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
    val pageToDelete = showDeleteConfirmDialog
    if (pageToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "حذف الصفحة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFF87171),
                )
            },
            text = {
                Text(
                    text = "هل أنت متأكد من حذف ${pageToDelete.xmlFileName} و ${pageToDelete.codeFileName}؟ لا يمكن التراجع عن هذه الخطوة.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val ok = repository.deleteProjectXmlPage(project, pageToDelete.xmlFileName)
                        if (ok) {
                            xmlPages.removeAll { it.xmlFileName == pageToDelete.xmlFileName }
                            if (selectedXmlFileName == pageToDelete.xmlFileName) {
                                xmlPages.firstOrNull()?.let { onSelectPage(it) }
                            }
                        }
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

    // Add XML Layout Dialog
    if (showAddPageDialog) {
        var inputName by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddPageDialog = false },
            containerColor = Color(0xFF0F172A),
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "إضافة صفحة Activity جديدة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("اسم الصفحة (e.g. settings)") },
                    placeholder = { Text("activity_settings") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = Color(0x44FFFFFF),
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            val created = repository.addProjectXmlPage(project, inputName)
                            if (xmlPages.none { it.xmlFileName == created.xmlFileName }) {
                                xmlPages.add(created)
                            }
                            onSelectPage(created)
                        }
                        showAddPageDialog = false
                    },
                ) {
                    Text("إضافة", color = Cyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPageDialog = false }) {
                    Text("إلغاء", color = Muted)
                }
            },
        )
    }
}
