package com.androidexl.ideultimate.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.ProjectZipExporter
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceItemType
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.components.GlassIconButton
import com.androidexl.ideultimate.ui.components.LogoMark
import com.androidexl.ideultimate.ui.components.Starfield
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft
import kotlinx.coroutines.delay

private sealed interface WorkspaceSheet {
    data object Create : WorkspaceSheet
    data class FolderActions(val item: WorkspaceItem) : WorkspaceSheet
    data class ProjectActions(val item: WorkspaceItem) : WorkspaceSheet
}

private sealed interface NameDialog {
    data class Create(val parentId: String?, val type: WorkspaceItemType) : NameDialog
    data class Rename(val item: WorkspaceItem) : NameDialog
}

private val FolderPalette = listOf(Cyan, VioletSoft, Fuchsia, Teal)

/**
 * File-style workspace browser:
 * folders open on tap and expose rename/delete on long press, projects expose
 * rename / export as ZIP / new project / delete, and a FAB opens the create sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val repository = remember { WorkspaceRepository(context) }

    var pathCsv by rememberSaveable { mutableStateOf("") }
    val pathIds = remember(pathCsv) {
        if (pathCsv.isBlank()) emptyList() else pathCsv.split("|").filter { it.isNotBlank() }
    }
    val currentParentId = pathIds.lastOrNull()
    val currentFolder = currentParentId?.let { repository.itemById(it) }
    val children = repository.childrenOf(currentParentId)

    var openProjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var sheet by remember { mutableStateOf<WorkspaceSheet?>(null) }
    var dialog by remember { mutableStateOf<NameDialog?>(null) }
    var deleteTarget by remember { mutableStateOf<WorkspaceItem?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var lastMessage by remember { mutableStateOf("") }
    var pendingExport by remember { mutableStateOf<WorkspaceItem?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        val target = pendingExport
        pendingExport = null
        if (uri != null && target != null) {
            val success = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(ProjectZipExporter.buildZip(target.name))
                } ?: error("Unable to open the selected destination")
            }.isSuccess
            message = if (success) {
                context.getString(R.string.export_success, target.name)
            } else {
                context.getString(R.string.export_failed)
            }
        }
    }

    fun showMessage(text: String) {
        lastMessage = text
        message = text
    }

    fun openFolder(item: WorkspaceItem) {
        pathCsv = (pathIds + item.id).joinToString("|")
    }

    fun navigateUp() {
        if (pathIds.isEmpty()) onBack() else pathCsv = pathIds.dropLast(1).joinToString("|")
    }

    BackHandler { navigateUp() }

    LaunchedEffect(message) {
        if (message != null) {
            delay(2400)
            message = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy),
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(R.drawable.splash_nebula),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.28f,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DeepNavy.copy(alpha = 0.60f),
                            DeepNavy.copy(alpha = 0.92f),
                            DeepNavy,
                        ),
                    ),
                ),
        )
        Starfield(
            modifier = Modifier.fillMaxSize(),
            starCount = 42,
            seed = 27,
            comet = false,
        )

        val openProject = openProjectId?.let { id -> repository.itemById(id) }
        if (openProject != null) {
            ProjectDetailScreen(
                project = openProject,
                onBack = { openProjectId = null },
                onExport = {
                    pendingExport = openProject
                    exportLauncher.launch(ProjectZipExporter.suggestedFileName(openProject.name))
                },
                onRename = { dialog = NameDialog.Rename(openProject) },
                onDelete = { deleteTarget = openProject },
                onNewProject = {
                    dialog = NameDialog.Create(openProject.parentId, WorkspaceItemType.PROJECT)
                },
                bottomBarPadding = bottomBarPadding,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding(),
            ) {
                WorkspaceTopBar(
                    title = currentFolder?.name ?: stringResource(R.string.workspace_title),
                    subtitle = if (currentFolder == null) {
                        stringResource(R.string.workspace_subtitle)
                    } else {
                        stringResource(R.string.workspace_count, repository.childCount(currentFolder.id))
                    },
                    onBack = { navigateUp() },
                )

                if (pathIds.isNotEmpty()) {
                    Breadcrumbs(
                        pathIds = pathIds,
                        nameOf = { id -> repository.itemById(id)?.name },
                        onRoot = { pathCsv = "" },
                        onCrumb = { index -> pathCsv = pathIds.take(index + 1).joinToString("|") },
                    )
                }

                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) {
                    if (children.isEmpty()) {
                        EmptyWorkspace(Modifier.fillMaxSize())
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 148.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 20.dp,
                                end = 20.dp,
                                top = 10.dp,
                                bottom = 130.dp + bottomBarPadding,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(children, key = { it.id }) { item ->
                                when (item.type) {
                                    WorkspaceItemType.FOLDER -> FolderTile(
                                        item = item,
                                        childCount = repository.childCount(item.id),
                                        onClick = { openFolder(item) },
                                        onLongClick = { sheet = WorkspaceSheet.FolderActions(item) },
                                    )
                                    WorkspaceItemType.PROJECT -> ProjectTile(
                                        item = item,
                                        onClick = { openProjectId = item.id },
                                        onLongClick = { sheet = WorkspaceSheet.ProjectActions(item) },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            WorkspaceFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 14.dp, bottom = 16.dp + bottomBarPadding),
                onClick = { sheet = WorkspaceSheet.Create },
            )
        }

        SnackbarOverlay(
            visible = message != null,
            text = lastMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 104.dp + bottomBarPadding, start = 24.dp, end = 24.dp),
        )
    }

    if (sheet != null) {
        val currentSheet = sheet
        if (currentSheet != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { sheet = null },
                sheetState = sheetState,
                containerColor = Color(0xFF0B1028),
                contentColor = Ink,
                scrimColor = Color(0xD903060F),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                dragHandle = { SheetHandle() },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp, bottom = 26.dp),
                ) {
                    when (currentSheet) {
                        is WorkspaceSheet.Create -> CreateSheetContent(
                            locationName = currentFolder?.name ?: stringResource(R.string.workspace_root),
                            allowProject = currentFolder != null,
                            onNewFolder = {
                                sheet = null
                                dialog = NameDialog.Create(currentParentId, WorkspaceItemType.FOLDER)
                            },
                            onNewProject = {
                                sheet = null
                                dialog = NameDialog.Create(currentParentId, WorkspaceItemType.PROJECT)
                            },
                        )
                        is WorkspaceSheet.FolderActions -> FolderActionsSheetContent(
                            item = currentSheet.item,
                            onRename = {
                                sheet = null
                                dialog = NameDialog.Rename(currentSheet.item)
                            },
                            onDelete = {
                                sheet = null
                                deleteTarget = currentSheet.item
                            },
                        )
                        is WorkspaceSheet.ProjectActions -> ProjectActionsSheetContent(
                            item = currentSheet.item,
                            onRename = {
                                sheet = null
                                dialog = NameDialog.Rename(currentSheet.item)
                            },
                            onExport = {
                                sheet = null
                                pendingExport = currentSheet.item
                                exportLauncher.launch(
                                    ProjectZipExporter.suggestedFileName(currentSheet.item.name),
                                )
                            },
                            onNewProject = {
                                sheet = null
                                dialog = NameDialog.Create(
                                    currentSheet.item.parentId,
                                    WorkspaceItemType.PROJECT,
                                )
                            },
                            onDelete = {
                                sheet = null
                                deleteTarget = currentSheet.item
                            },
                        )
                    }
                }
            }
        }
    }

    val currentDialog = dialog
    if (currentDialog != null) {
        when (currentDialog) {
            is NameDialog.Create -> {
                val isFolder = currentDialog.type == WorkspaceItemType.FOLDER
                NameInputDialog(
                    title = stringResource(
                        if (isFolder) R.string.dialog_new_folder else R.string.dialog_new_project,
                    ),
                    initialName = "",
                    confirmLabel = stringResource(R.string.dialog_create),
                    validate = { name ->
                        validateName(context, repository, name, currentDialog.parentId, null)
                    },
                    onDismiss = { dialog = null },
                    onConfirm = { name ->
                        if (isFolder) {
                            repository.createFolder(name, currentDialog.parentId)
                        } else {
                            repository.createProject(name, currentDialog.parentId)
                        }
                        showMessage(
                            context.getString(
                                if (isFolder) R.string.msg_folder_created else R.string.msg_project_created,
                            ),
                        )
                        dialog = null
                    },
                )
            }
            is NameDialog.Rename -> NameInputDialog(
                title = stringResource(
                    if (currentDialog.item.type == WorkspaceItemType.PROJECT) {
                        R.string.dialog_edit_project
                    } else {
                        R.string.dialog_rename
                    },
                ),
                initialName = currentDialog.item.name,
                confirmLabel = stringResource(R.string.dialog_save),
                validate = { name ->
                    validateName(
                        context,
                        repository,
                        name,
                        currentDialog.item.parentId,
                        currentDialog.item.id,
                    )
                },
                onDismiss = { dialog = null },
                onConfirm = { name ->
                    repository.rename(currentDialog.item.id, name)
                    showMessage(context.getString(R.string.msg_renamed))
                    dialog = null
                },
            )
        }
    }

    val currentDelete = deleteTarget
    if (currentDelete != null) {
        val isFolder = currentDelete.type == WorkspaceItemType.FOLDER
        val descendants = if (isFolder) repository.descendantCount(currentDelete.id) else 0
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = Color(0xFF0D1230),
            titleContentColor = Ink,
            textContentColor = Muted,
            shape = RoundedCornerShape(26.dp),
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFFFF8A8A),
                )
            },
            title = {
                Text(
                    text = stringResource(
                        if (isFolder) R.string.delete_folder_title else R.string.delete_project_title,
                        currentDelete.name,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Column {
                    Text(
                        text = stringResource(
                            if (isFolder) R.string.delete_folder_message else R.string.delete_project_message,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                    )
                    if (isFolder && descendants > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.delete_folder_count, descendants),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFFFFB4B4),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        repository.delete(currentDelete.id)
                        if (openProjectId == currentDelete.id) openProjectId = null
                        deleteTarget = null
                        showMessage(context.getString(R.string.msg_deleted))
                    },
                ) {
                    Text(
                        text = stringResource(R.string.delete_confirm),
                        color = Color(0xFFFF8A8A),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(
                        text = stringResource(R.string.dialog_cancel),
                        color = Muted,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            },
        )
    }
}

private fun validateName(
    context: Context,
    repository: WorkspaceRepository,
    rawName: String,
    parentId: String?,
    excludeId: String?,
): String? {
    val name = rawName.trim()
    return when {
        name.isEmpty() -> context.getString(R.string.error_name_empty)
        name.length > 40 -> context.getString(R.string.error_name_too_long)
        repository.isNameTaken(name, parentId, excludeId) -> context.getString(R.string.error_name_taken)
        else -> null
    }
}

@Composable
private fun WorkspaceTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.workspace_back),
            onClick = onBack,
            modifier = Modifier.size(44.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        LogoMark(
            modifier = Modifier.size(40.dp),
            animated = false,
        )
    }
}

@Composable
private fun Breadcrumbs(
    pathIds: List<String>,
    nameOf: (String) -> String?,
    onRoot: () -> Unit,
    onCrumb: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Crumb(
            text = stringResource(R.string.workspace_root),
            selected = false,
            onClick = onRoot,
        )
        pathIds.forEachIndexed { index, id ->
            val name = nameOf(id) ?: return@forEachIndexed
            Text(
                text = "›",
                style = MaterialTheme.typography.bodySmall,
                color = Muted.copy(alpha = 0.55f),
                modifier = Modifier.padding(horizontal = 7.dp),
            )
            Crumb(
                text = name,
                selected = index == pathIds.lastIndex,
                onClick = { onCrumb(index) },
            )
        }
    }
}

@Composable
private fun Crumb(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(percent = 50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) Cyan.copy(alpha = 0.16f) else Color(0x0FFFFFFF))
            .border(1.dp, if (selected) Cyan.copy(alpha = 0.45f) else Color(0x14FFFFFF), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = if (selected) Cyan else Muted,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderTile(
    item: WorkspaceItem,
    childCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val accent = folderAccent(item.name)
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(Color(0x1FFFFFFF), Color(0x08FFFFFF))),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.45f), Color(0x12FFFFFF)),
                ),
                shape = shape,
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(14.dp),
    ) {
        FolderGlyph(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            accent = accent,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = if (childCount == 0) {
                stringResource(R.string.workspace_empty_folder)
            } else {
                stringResource(R.string.workspace_count, childCount)
            },
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = Muted,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProjectTile(
    item: WorkspaceItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(Color(0x1FFFFFFF), Color(0x08FFFFFF))),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(VioletSoft.copy(alpha = 0.45f), Color(0x12FFFFFF)),
                ),
                shape = shape,
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xB3070B1C))
                .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(15.dp))
                .padding(horizontal = 10.dp, vertical = 9.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Dot(Color(0xFFFF5F57))
                Spacer(Modifier.width(4.dp))
                Dot(Color(0xFFFEBC2E))
                Spacer(Modifier.width(4.dp))
                Dot(Color(0xFF28C840))
                Spacer(Modifier.weight(1f))
                Text(
                    text = "kt",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                    color = Muted.copy(alpha = 0.8f),
                )
            }
            Spacer(Modifier.height(10.dp))
            CodeLine(0.78f, Fuchsia)
            Spacer(Modifier.height(5.dp))
            CodeLine(0.52f, Cyan)
            Spacer(Modifier.height(5.dp))
            CodeLine(0.88f, VioletSoft)
            Spacer(Modifier.height(5.dp))
            CodeLine(0.38f, Teal)
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.project_label),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = Muted,
            maxLines = 1,
        )
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
private fun CodeLine(fraction: Float, color: Color) {
    Box(
        Modifier
            .fillMaxWidth(fraction)
            .height(4.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(color.copy(alpha = 0.75f)),
    )
}

@Composable
private fun FolderGlyph(modifier: Modifier = Modifier, accent: Color = Cyan) {
    Canvas(modifier) {
        val width = size.width
        val height = size.height
        val bodyTopLeft = Offset(width * 0.04f, height * 0.30f)
        val bodySize = Size(width * 0.92f, height * 0.60f)
        val bodyRadius = CornerRadius(width * 0.11f, width * 0.11f)

        drawRoundRect(
            color = accent.copy(alpha = 0.40f),
            topLeft = Offset(width * 0.07f, height * 0.16f),
            size = Size(width * 0.40f, height * 0.26f),
            cornerRadius = CornerRadius(width * 0.07f, width * 0.07f),
        )
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(accent.copy(alpha = 0.90f), accent.copy(alpha = 0.42f)),
                startY = bodyTopLeft.y,
                endY = bodyTopLeft.y + bodySize.height,
            ),
            topLeft = bodyTopLeft,
            size = bodySize,
            cornerRadius = bodyRadius,
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.22f),
            topLeft = Offset(bodyTopLeft.x + width * 0.07f, bodyTopLeft.y + height * 0.08f),
            size = Size(bodySize.width * 0.50f, height * 0.035f),
            cornerRadius = CornerRadius(height * 0.02f, height * 0.02f),
        )
        drawRoundRect(
            color = accent.copy(alpha = 0.55f),
            topLeft = bodyTopLeft,
            size = bodySize,
            cornerRadius = bodyRadius,
            style = Stroke(width = width * 0.018f),
        )
    }
}

@Composable
private fun EmptyWorkspace(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FolderGlyph(
            modifier = Modifier.size(92.dp),
            accent = VioletSoft,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.workspace_empty_title),
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.workspace_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WorkspaceFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(21.dp)
    Box(modifier.size(76.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(76.dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(Violet.copy(alpha = 0.45f), Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )
        Box(
            Modifier
                .size(60.dp)
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(Violet, Color(0xFF8B5CF6), Color(0xFFD946EF)),
                    ),
                )
                .border(1.dp, Color(0x4DFFFFFF), shape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(R.string.workspace_fab),
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun SnackbarOverlay(
    visible: Boolean,
    text: String,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xF20B1028))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Cyan,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Ink,
            )
        }
    }
}

@Composable
private fun SheetHandle() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .width(46.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0x40FFFFFF)),
        )
    }
}

@Composable
private fun SheetHeader(title: String, subtitle: String? = null, badge: String? = null) {
    Column(Modifier.fillMaxWidth()) {
        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp,
                ),
                color = Cyan,
            )
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Muted,
            )
        }
    }
}

@Composable
private fun SheetAction(
    title: String,
    description: String,
    tint: Color,
    destructive: Boolean = false,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (destructive) Color(0x1AFF5C5C) else Color(0x0FFFFFFF))
            .border(
                1.dp,
                if (destructive) Color(0x33FF5C5C) else Color(0x14FFFFFF),
                shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(tint.copy(alpha = 0.14f))
                .border(1.dp, tint.copy(alpha = 0.28f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = if (destructive) Color(0xFFFF9B9B) else Ink,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 15.sp),
                color = Muted,
            )
        }
    }
}

@Composable
private fun CreateSheetContent(
    locationName: String,
    allowProject: Boolean,
    onNewFolder: () -> Unit,
    onNewProject: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SheetHeader(
            badge = stringResource(R.string.workspace_root_inside, locationName),
            title = stringResource(R.string.sheet_create_title),
            subtitle = stringResource(
                if (allowProject) R.string.sheet_create_subtitle else R.string.sheet_create_folder_only_subtitle,
            ),
        )
        Spacer(Modifier.height(2.dp))
        SheetAction(
            title = stringResource(R.string.sheet_new_folder),
            description = stringResource(R.string.sheet_new_folder_desc),
            tint = Cyan,
            icon = { FolderGlyph(Modifier.size(22.dp), accent = Cyan) },
            onClick = onNewFolder,
        )
        if (allowProject) {
            SheetAction(
                title = stringResource(R.string.sheet_new_project),
                description = stringResource(R.string.sheet_new_project_desc),
                tint = VioletSoft,
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = VioletSoft,
                        modifier = Modifier.size(20.dp),
                    )
                },
                onClick = onNewProject,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.sheet_folder_hint),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            color = Muted.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FolderActionsSheetContent(
    item: WorkspaceItem,
    onRename: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SheetHeader(
            badge = stringResource(R.string.folder_label),
            title = item.name,
            subtitle = stringResource(R.string.sheet_folder_actions),
        )
        Spacer(Modifier.height(2.dp))
        SheetAction(
            title = stringResource(R.string.action_rename),
            description = stringResource(R.string.action_rename_desc),
            tint = Cyan,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Create,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = onRename,
        )
        SheetAction(
            title = stringResource(R.string.action_delete),
            description = stringResource(R.string.action_delete_folder_desc),
            tint = Color(0xFFFF6B6B),
            destructive = true,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFFFF8A8A),
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = onDelete,
        )
    }
}

@Composable
private fun ProjectActionsSheetContent(
    item: WorkspaceItem,
    onRename: () -> Unit,
    onExport: () -> Unit,
    onNewProject: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SheetHeader(
            badge = stringResource(R.string.project_label),
            title = item.name,
            subtitle = stringResource(R.string.sheet_project_actions),
        )
        Spacer(Modifier.height(2.dp))
        SheetAction(
            title = stringResource(R.string.action_edit_project),
            description = stringResource(R.string.action_edit_project_desc),
            tint = Cyan,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Create,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = onRename,
        )
        SheetAction(
            title = stringResource(R.string.action_export),
            description = stringResource(R.string.action_export_desc),
            tint = VioletSoft,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = null,
                    tint = VioletSoft,
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = onExport,
        )
        SheetAction(
            title = stringResource(R.string.action_delete_project),
            description = stringResource(R.string.action_delete_project_desc),
            tint = Color(0xFFFF6B6B),
            destructive = true,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFFFF8A8A),
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = onDelete,
        )
        SheetAction(
            title = stringResource(R.string.action_new_project_here),
            description = stringResource(R.string.action_new_project_here_desc),
            tint = Teal,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Teal,
                    modifier = Modifier.size(20.dp),
                )
            },
            onClick = onNewProject,
        )
    }
}

@Composable
private fun NameInputDialog(
    title: String,
    initialName: String,
    confirmLabel: String,
    validate: (String) -> String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var error by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    fun submit() {
        val validationError = validate(name)
        if (validationError != null) {
            error = validationError
        } else {
            onConfirm(name.trim())
        }
    }

    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1230),
        titleContentColor = Ink,
        textContentColor = Muted,
        shape = RoundedCornerShape(26.dp),
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { value ->
                        if (value.length <= 40) {
                            name = value
                            error = null
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    label = { Text(stringResource(R.string.dialog_name_label)) },
                    placeholder = { Text(stringResource(R.string.dialog_name_hint)) },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                        cursorColor = Cyan,
                        focusedBorderColor = Cyan.copy(alpha = 0.8f),
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedLabelColor = Cyan,
                        unfocusedLabelColor = Muted,
                        focusedContainerColor = Color(0x14FFFFFF),
                        unfocusedContainerColor = Color(0x0AFFFFFF),
                        focusedPlaceholderColor = Muted.copy(alpha = 0.6f),
                        unfocusedPlaceholderColor = Muted.copy(alpha = 0.6f),
                    ),
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = error.orEmpty(),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color(0xFFFF8A8A),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { submit() }) {
                Text(
                    text = confirmLabel,
                    color = Cyan,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.dialog_cancel),
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

private fun folderAccent(name: String): Color {
    val index = ((name.hashCode() % FolderPalette.size) + FolderPalette.size) % FolderPalette.size
    return FolderPalette[index]
}
