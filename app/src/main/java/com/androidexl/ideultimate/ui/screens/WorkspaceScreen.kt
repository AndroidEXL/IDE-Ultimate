package com.androidexl.ideultimate.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.CommunityStoreRepository
import com.androidexl.ideultimate.data.ProjectLanguage
import com.androidexl.ideultimate.data.ProjectZipExporter
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceItemType
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.components.AppTab
import com.androidexl.ideultimate.ui.components.FolderNavIcon
import com.androidexl.ideultimate.ui.components.GlassIconButton
import com.androidexl.ideultimate.ui.components.LogoMark
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.components.Starfield
import com.androidexl.ideultimate.ui.components.languageColor
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
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

private sealed interface FolderDialog {
    data class CreateFolder(val parentId: String?) : FolderDialog
    data class RenameFolder(val item: WorkspaceItem) : FolderDialog
}

private sealed interface ProjectPageMode {
    data class Create(
        val parentId: String?,
        val initialName: String = "",
        val initialLanguage: ProjectLanguage = ProjectLanguage.KOTLIN,
    ) : ProjectPageMode

    data class Edit(val projectId: String) : ProjectPageMode
}

private sealed interface StoreFormMode {
    data object Create : StoreFormMode
    data class Edit(val projectId: String) : StoreFormMode
}

private val FolderPalette = listOf(Cyan, VioletSoft, Fuchsia, Teal)
private val TileEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    activeTab: AppTab,
    onSelectTab: (AppTab) -> Unit,
    onReplaySplash: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val repository = remember { WorkspaceRepository(context) }
    val storeRepository = remember { CommunityStoreRepository(context) }

    var pathCsv by rememberSaveable { mutableStateOf("") }
    val pathIds = remember(pathCsv) {
        if (pathCsv.isBlank()) emptyList() else pathCsv.split("|").filter { it.isNotBlank() }
    }
    val currentParentId = pathIds.lastOrNull()
    val currentFolder = currentParentId?.let { repository.itemById(it) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var languageFilter by rememberSaveable { mutableStateOf("ALL") }
    val rawChildren = repository.childrenOf(currentParentId)
    val children = remember(rawChildren, languageFilter, searchQuery) {
        val q = searchQuery.trim().lowercase()
        rawChildren.filter { item ->
            val matchesLang = languageFilter == "ALL" ||
                item.type == WorkspaceItemType.FOLDER ||
                item.language.name == languageFilter
            val matchesQuery = q.isEmpty() ||
                item.name.lowercase().contains(q) ||
                item.packageName.lowercase().contains(q)
            matchesLang && matchesQuery
        }
    }

    var openProjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var projectPageMode by remember { mutableStateOf<ProjectPageMode?>(null) }

    // Community / Store & Profile overlay states
    var showUserProfile by rememberSaveable { mutableStateOf(false) }
    var openStoreProjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var storeFormMode by remember { mutableStateOf<StoreFormMode?>(null) }

    var sheet by remember { mutableStateOf<WorkspaceSheet?>(null) }
    var folderDialog by remember { mutableStateOf<FolderDialog?>(null) }
    var deleteTarget by remember { mutableStateOf<WorkspaceItem?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var lastMessage by remember { mutableStateOf("") }
    var pendingExport by remember { mutableStateOf<WorkspaceItem?>(null) }
    var pendingStoreZipName by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        val target = pendingExport
        val storeName = pendingStoreZipName
        pendingExport = null
        pendingStoreZipName = null
        if (uri != null) {
            if (target != null) {
                val success = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(ProjectZipExporter.buildZip(target))
                    } ?: error("Unable to open destination")
                }.isSuccess
                message = if (success) {
                    context.getString(R.string.export_success, target.name)
                } else {
                    context.getString(R.string.export_failed)
                }
            } else if (storeName != null) {
                val success = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(ProjectZipExporter.buildZip(storeName))
                    } ?: error("Unable to open destination")
                }.isSuccess
                message = if (success) {
                    context.getString(R.string.export_success, storeName)
                } else {
                    context.getString(R.string.export_failed)
                }
            }
        }
    }

    fun showMessage(text: String) {
        lastMessage = text
        message = text
    }

    fun openFolder(item: WorkspaceItem) {
        languageFilter = "ALL"
        pathCsv = (pathIds + item.id).joinToString("|")
    }

    fun navigateUp() {
        when {
            storeFormMode != null -> storeFormMode = null
            openStoreProjectId != null -> openStoreProjectId = null
            showUserProfile -> showUserProfile = false
            projectPageMode != null -> projectPageMode = null
            openProjectId != null -> openProjectId = null
            activeTab == AppTab.StoreCode -> onSelectTab(AppTab.ProjectManager)
            pathIds.isNotEmpty() -> {
                languageFilter = "ALL"
                pathCsv = pathIds.dropLast(1).joinToString("|")
            }
        }
    }

    BackHandler(
        enabled = storeFormMode != null ||
            openStoreProjectId != null ||
            showUserProfile ||
            projectPageMode != null ||
            openProjectId != null ||
            activeTab == AppTab.StoreCode ||
            pathIds.isNotEmpty(),
    ) {
        navigateUp()
    }

    LaunchedEffect(message) {
        if (message != null) {
            delay(2500)
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
            alpha = 0.30f,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DeepNavy.copy(alpha = 0.58f),
                            DeepNavy.copy(alpha = 0.92f),
                            DeepNavy,
                        ),
                    ),
                ),
        )
        Starfield(
            modifier = Modifier.fillMaxSize(),
            starCount = 46,
            seed = 27,
            comet = false,
        )

        val currentStoreForm = storeFormMode
        val activeStoreProject = openStoreProjectId?.let { storeRepository.projectById(it) }
        val currentMode = projectPageMode
        val openProject = openProjectId?.let { id -> repository.itemById(id) }

        when {
            // 1. Store Project Create / Edit Page (from Profile -> Projects FAB or Edit)
            currentStoreForm != null -> {
                val editingTarget = (currentStoreForm as? StoreFormMode.Edit)
                    ?.let { storeRepository.projectById(it.projectId) }
                StoreProjectFormScreen(
                    editingItem = editingTarget,
                    onBack = { storeFormMode = null },
                    onSave = { formData ->
                        if (editingTarget != null) {
                            storeRepository.updateStoreProject(
                                id = editingTarget.id,
                                name = formData.name,
                                details = formData.details,
                                customIconPath = formData.customIconPath,
                                iconPreset = formData.iconPreset,
                                downloadLinks = formData.downloadLinks,
                                imagePaths = formData.imagePaths,
                            )
                            showMessage(context.getString(R.string.msg_project_updated))
                        } else {
                            storeRepository.createStoreProject(
                                name = formData.name,
                                details = formData.details,
                                customIconPath = formData.customIconPath,
                                iconPreset = formData.iconPreset,
                                downloadLinks = formData.downloadLinks,
                                imagePaths = formData.imagePaths,
                            )
                            showMessage(context.getString(R.string.msg_project_created))
                        }
                        storeFormMode = null
                    },
                    bottomBarPadding = bottomBarPadding,
                )
            }

            // 2. Read-only Store Project View Page (opened from Store, Profile Projects, or Collection)
            activeStoreProject != null -> {
                StoreProjectViewScreen(
                    project = activeStoreProject,
                    onBack = { openStoreProjectId = null },
                    onToggleSaveLater = {
                        val saved = storeRepository.toggleSaveInCollection(activeStoreProject.id)
                        showMessage(
                            context.getString(
                                if (saved) R.string.msg_saved_to_collection else R.string.msg_removed_from_collection,
                            ),
                        )
                    },
                    onLike = { storeRepository.toggleLike(activeStoreProject.id) },
                    onDislike = { storeRepository.toggleDislike(activeStoreProject.id) },
                    onDownloadZip = {
                        pendingStoreZipName = activeStoreProject.name
                        exportLauncher.launch(
                            ProjectZipExporter.suggestedFileName(activeStoreProject.name),
                        )
                    },
                    bottomBarPadding = bottomBarPadding,
                )
            }

            // 3. User Profile Page (opened by tapping the User Avatar next to the Search bar)
            showUserProfile -> {
                UserProfileScreen(
                    storeRepository = storeRepository,
                    onBack = { showUserProfile = false },
                    onOpenStoreProject = { item -> openStoreProjectId = item.id },
                    onCreateStoreProject = { storeFormMode = StoreFormMode.Create },
                    onEditStoreProject = { item -> storeFormMode = StoreFormMode.Edit(item.id) },
                    bottomBarPadding = bottomBarPadding,
                )
            }

            // 4. Store Code Tab (New Projects + Code Snippets + Custom Libraries + Search + Avatar)
            activeTab == AppTab.StoreCode -> {
                StoreCodeScreen(
                    storeRepository = storeRepository,
                    onOpenProfile = { showUserProfile = true },
                    onOpenStoreProject = { item -> openStoreProjectId = item.id },
                    onUseSnippetAsProject = { snippet ->
                        onSelectTab(AppTab.ProjectManager)
                        projectPageMode = ProjectPageMode.Create(
                            parentId = currentParentId,
                            initialName = snippet.title,
                            initialLanguage = snippet.language,
                        )
                    },
                    bottomBarPadding = bottomBarPadding,
                )
            }

            // 5. Local IDE Project Create / Edit Page
            currentMode != null -> {
                when (currentMode) {
                    is ProjectPageMode.Create -> {
                        val folderLabel = currentMode.parentId
                            ?.let { repository.itemById(it)?.name }
                            ?: stringResource(R.string.workspace_root)
                        CreateProjectScreen(
                            folderName = folderLabel,
                            editingProject = null,
                            initialLanguage = currentMode.initialLanguage,
                            initialName = currentMode.initialName,
                            validateName = { candidate ->
                                validateName(
                                    context = context,
                                    repository = repository,
                                    rawName = candidate,
                                    parentId = currentMode.parentId,
                                    excludeId = null,
                                )
                            },
                            onBack = { projectPageMode = null },
                            onSubmit = { form ->
                                repository.createProject(
                                    name = form.name,
                                    parentId = currentMode.parentId,
                                    packageName = form.packageName,
                                    language = form.language,
                                    versionCode = form.versionCode,
                                    versionName = form.versionName,
                                    iconPreset = form.iconPreset,
                                    customIconPath = form.customIconPath,
                                )
                                projectPageMode = null
                                showMessage(context.getString(R.string.msg_project_created))
                            },
                            bottomBarPadding = bottomBarPadding,
                        )
                    }

                    is ProjectPageMode.Edit -> {
                        val target = repository.itemById(currentMode.projectId)
                        if (target == null) {
                            projectPageMode = null
                        } else {
                            val folderLabel = target.parentId
                                ?.let { repository.itemById(it)?.name }
                                ?: stringResource(R.string.workspace_root)
                            CreateProjectScreen(
                                folderName = folderLabel,
                                editingProject = target,
                                validateName = { candidate ->
                                    validateName(
                                        context = context,
                                        repository = repository,
                                        rawName = candidate,
                                        parentId = target.parentId,
                                        excludeId = target.id,
                                    )
                                },
                                onBack = { projectPageMode = null },
                                onSubmit = { form ->
                                    repository.updateProject(
                                        id = target.id,
                                        newName = form.name,
                                        newPackageName = form.packageName,
                                        newVersionCode = form.versionCode,
                                        newVersionName = form.versionName,
                                        newIconPreset = form.iconPreset,
                                        newCustomIconPath = form.customIconPath,
                                    )
                                    projectPageMode = null
                                    showMessage(context.getString(R.string.msg_project_updated))
                                },
                                bottomBarPadding = bottomBarPadding,
                            )
                        }
                    }
                }
            }

            // 6. Local Project Detail Page
            openProject != null -> {
                ProjectDetailScreen(
                    project = openProject,
                    onBack = { openProjectId = null },
                    onExport = {
                        pendingExport = openProject
                        exportLauncher.launch(ProjectZipExporter.suggestedFileName(openProject.name))
                    },
                    onRename = {
                        projectPageMode = ProjectPageMode.Edit(openProject.id)
                    },
                    onDelete = { deleteTarget = openProject },
                    onNewProject = {
                        projectPageMode = ProjectPageMode.Create(parentId = openProject.parentId)
                    },
                    bottomBarPadding = bottomBarPadding,
                )
            }

            // 7. Project Manager Grid View (Root & Folders — supports creating both Folders and Projects in Root!)
            else -> {
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
                            stringResource(
                                R.string.workspace_count,
                                repository.childCount(currentFolder.id),
                            )
                        },
                        showBack = pathIds.isNotEmpty(),
                        onBack = { navigateUp() },
                        onReplaySplash = onReplaySplash,
                    )

                    // Top Search Bar + User Avatar (opens UserProfileScreen on tap)
                    Box(Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                        SearchAndUserTopBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            avatarPath = storeRepository.userAvatarPath,
                            isLoggedIn = storeRepository.isLoggedIn,
                            onAvatarClick = { showUserProfile = true },
                        )
                    }

                    // Disk Storage Path Banner: /storage/emulated/0/.IDE Ultimate
                    DiskStoragePathBanner(
                        diskPath = repository.displayDiskPath(pathIds),
                        context = context,
                    )

                    WorkspaceStatsStrip(
                        folderCount = if (currentFolder == null) {
                            repository.folderCount(null)
                        } else {
                            repository.folderCount(currentFolder.id)
                        },
                        projectCount = if (currentFolder == null) {
                            repository.totalProjectCount()
                        } else {
                            repository.projectCount(currentFolder.id)
                        },
                    )

                    if (pathIds.isNotEmpty()) {
                        Breadcrumbs(
                            pathIds = pathIds,
                            nameOf = { id -> repository.itemById(id)?.name },
                            onRoot = {
                                languageFilter = "ALL"
                                pathCsv = ""
                            },
                            onCrumb = { index ->
                                languageFilter = "ALL"
                                pathCsv = pathIds.take(index + 1).joinToString("|")
                            },
                        )
                    }

                    LanguageFilterRow(
                        selected = languageFilter,
                        onSelect = { languageFilter = it },
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    ) {
                        if (children.isEmpty()) {
                            EmptyWorkspace(Modifier.fillMaxSize())
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 152.dp),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 8.dp,
                                    bottom = 136.dp + bottomBarPadding,
                                ),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                itemsIndexed(children, key = { _, item -> item.id }) { index, item ->
                                    when (item.type) {
                                        WorkspaceItemType.FOLDER -> FolderTile(
                                            item = item,
                                            index = index,
                                            childCount = repository.childCount(item.id),
                                            onClick = { openFolder(item) },
                                            onLongClick = { sheet = WorkspaceSheet.FolderActions(item) },
                                        )
                                        WorkspaceItemType.PROJECT -> ProjectTile(
                                            item = item,
                                            index = index,
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
                        .padding(end = 16.dp, bottom = 18.dp + bottomBarPadding),
                    onClick = { sheet = WorkspaceSheet.Create },
                )
            }
        }

        SnackbarOverlay(
            visible = message != null,
            text = lastMessage,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 108.dp + bottomBarPadding, start = 24.dp, end = 24.dp),
        )
    }

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
                        // User requested: allow creating projects in Root as well as inside folders!
                        allowProject = true,
                        onNewFolder = {
                            sheet = null
                            folderDialog = FolderDialog.CreateFolder(currentParentId)
                        },
                        onNewProject = {
                            sheet = null
                            projectPageMode = ProjectPageMode.Create(parentId = currentParentId)
                        },
                    )

                    is WorkspaceSheet.FolderActions -> FolderActionsSheetContent(
                        item = currentSheet.item,
                        onRename = {
                            sheet = null
                            folderDialog = FolderDialog.RenameFolder(currentSheet.item)
                        },
                        onDelete = {
                            sheet = null
                            deleteTarget = currentSheet.item
                        },
                    )

                    is WorkspaceSheet.ProjectActions -> ProjectActionsSheetContent(
                        item = currentSheet.item,
                        onEditProject = {
                            val target = currentSheet.item
                            sheet = null
                            projectPageMode = ProjectPageMode.Edit(target.id)
                        },
                        onExport = {
                            val target = currentSheet.item
                            sheet = null
                            pendingExport = target
                            exportLauncher.launch(
                                ProjectZipExporter.suggestedFileName(target.name),
                            )
                        },
                        onDelete = {
                            val target = currentSheet.item
                            sheet = null
                            deleteTarget = target
                        },
                        onNewProject = {
                            val target = currentSheet.item
                            sheet = null
                            projectPageMode = ProjectPageMode.Create(parentId = target.parentId)
                        },
                    )
                }
            }
        }
    }

    val currentFolderDialog = folderDialog
    if (currentFolderDialog != null) {
        when (currentFolderDialog) {
            is FolderDialog.CreateFolder -> NameInputDialog(
                title = stringResource(R.string.dialog_new_folder),
                initialName = "",
                confirmLabel = stringResource(R.string.dialog_create),
                validate = { name ->
                    validateName(context, repository, name, currentFolderDialog.parentId, null)
                },
                onDismiss = { folderDialog = null },
                onConfirm = { name ->
                    repository.createFolder(name, currentFolderDialog.parentId)
                    showMessage(context.getString(R.string.msg_folder_created))
                    folderDialog = null
                },
            )

            is FolderDialog.RenameFolder -> NameInputDialog(
                title = stringResource(R.string.dialog_rename),
                initialName = currentFolderDialog.item.name,
                confirmLabel = stringResource(R.string.dialog_save),
                validate = { name ->
                    validateName(
                        context,
                        repository,
                        name,
                        currentFolderDialog.item.parentId,
                        currentFolderDialog.item.id,
                    )
                },
                onDismiss = { folderDialog = null },
                onConfirm = { name ->
                    repository.rename(currentFolderDialog.item.id, name)
                    showMessage(context.getString(R.string.msg_renamed))
                    folderDialog = null
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

@Composable
private fun DiskStoragePathBanner(
    diskPath: String,
    context: Context,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(Color(0x99060919))
                .border(1.dp, Cyan.copy(alpha = 0.32f), RoundedCornerShape(13.dp))
                .clickable {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
                        runCatching {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                    Uri.parse("package:${context.packageName}"),
                                ),
                            )
                        }
                    }
                }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FolderNavIcon(modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = diskPath,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                ),
                color = Cyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
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
    showBack: Boolean,
    onBack: () -> Unit,
    onReplaySplash: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBack) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.workspace_back),
                onClick = onBack,
                modifier = Modifier.size(42.dp),
            )
        } else {
            LogoMark(
                modifier = Modifier.size(40.dp),
                animated = true,
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        GlassIconButton(
            icon = Icons.Filled.Refresh,
            contentDescription = stringResource(R.string.home_replay_splash),
            onClick = onReplaySplash,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
private fun WorkspaceStatsStrip(
    folderCount: Int,
    projectCount: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatChip(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.stat_folders),
            value = folderCount.toString(),
            accent = Cyan,
        )
        StatChip(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.stat_projects),
            value = projectCount.toString(),
            accent = VioletSoft,
        )
    }
}

@Composable
private fun StatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    accent: Color,
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = 0.16f), Color(0x0DFFFFFF)),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.35f), shape)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.5.sp),
            color = Muted,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMono,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = accent,
        )
    }
}

@Composable
private fun LanguageFilterRow(
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LanguageChip(
            label = stringResource(R.string.filter_all),
            selected = selected == "ALL",
            accent = Cyan,
            onClick = { onSelect("ALL") },
        )
        ProjectLanguage.entries.forEach { lang ->
            LanguageChip(
                label = lang.displayName,
                selected = selected == lang.name,
                accent = languageColor(lang),
                onClick = { onSelect(lang.name) },
            )
        }
    }
}

@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.20f) else Color(0x10FFFFFF),
        animationSpec = tween(200),
        label = "lang-chip-bg",
    )
    val border by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.60f) else Color(0x1FFFFFFF),
        animationSpec = tween(200),
        label = "lang-chip-border",
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 5.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            ),
            color = if (selected) accent else Muted,
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
            .padding(horizontal = 20.dp, vertical = 3.dp),
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
            .padding(horizontal = 12.dp, vertical = 4.dp),
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
    index: Int,
    childCount: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val accent = folderAccent(item.name)
    val shape = RoundedCornerShape(24.dp)
    val appear = remember(item.id) { Animatable(0f) }

    LaunchedEffect(item.id) {
        delay((index.coerceAtMost(8) * 45).toLong())
        appear.animateTo(1f, tween(durationMillis = 480, easing = TileEase))
    }

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                alpha = appear.value
                scaleX = 0.88f + 0.12f * appear.value
                scaleY = 0.88f + 0.12f * appear.value
                translationY = (1f - appear.value) * 26.dp.toPx()
            }
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = 0.14f),
                        Color(0x18FFFFFF),
                        Color(0x08FFFFFF),
                    ),
                ),
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(accent.copy(alpha = 0.58f), Color(0x16FFFFFF)),
                ),
                shape = shape,
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            FolderGlyph(
                modifier = Modifier.size(width = 74.dp, height = 56.dp),
                accent = accent,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(accent.copy(alpha = 0.16f))
                    .border(1.dp, accent.copy(alpha = 0.40f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = childCount.toString(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = accent,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.5.sp),
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
    index: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val langAccent = languageColor(item.language)
    val shape = RoundedCornerShape(24.dp)
    val appear = remember(item.id) { Animatable(0f) }

    LaunchedEffect(item.id) {
        delay((index.coerceAtMost(8) * 45).toLong())
        appear.animateTo(1f, tween(durationMillis = 500, easing = TileEase))
    }

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .graphicsLayer {
                alpha = appear.value
                scaleX = 0.88f + 0.12f * appear.value
                scaleY = 0.88f + 0.12f * appear.value
                translationY = (1f - appear.value) * 26.dp.toPx()
            }
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        langAccent.copy(alpha = 0.15f),
                        Color(0x1DFFFFFF),
                        Color(0x0A0E24),
                    ),
                ),
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(langAccent.copy(alpha = 0.62f), VioletSoft.copy(alpha = 0.20f)),
                ),
                shape = shape,
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(13.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ProjectIconView(
                iconPreset = item.iconPreset,
                customIconPath = item.customIconPath,
                modifier = Modifier.size(48.dp),
                cornerRadius = 15.dp,
            )

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(langAccent.copy(alpha = 0.18f))
                        .border(1.dp, langAccent.copy(alpha = 0.48f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = item.language.displayName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = langAccent,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "v${item.versionName} (#${item.versionCode})",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 8.5.sp,
                    ),
                    color = Muted,
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Text(
                text = item.packageName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 8.5.sp,
                ),
                color = Cyan.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
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
    val infinite = rememberInfiniteTransition(label = "fab-pulse")
    val haloScale by infinite.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fab-halo",
    )
    val shape = RoundedCornerShape(21.dp)
    Box(modifier.size(78.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(76.dp)
                .graphicsLayer {
                    scaleX = haloScale
                    scaleY = haloScale
                }
                .background(
                    brush = Brush.radialGradient(
                        listOf(Cyan.copy(alpha = 0.42f), Violet.copy(alpha = 0.25f), Color.Transparent),
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
                        listOf(Violet, Color(0xFF7C3AED), Cyan),
                    ),
                )
                .border(1.2.dp, Color(0x66FFFFFF), shape)
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
    onEditProject: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onNewProject: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SheetHeader(
            badge = "${item.language.displayName} • v${item.versionName}",
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
            onClick = onEditProject,
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
