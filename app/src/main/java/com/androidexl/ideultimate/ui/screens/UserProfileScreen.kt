package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.CommunityStoreRepository
import com.androidexl.ideultimate.data.StoreItemType
import com.androidexl.ideultimate.data.StoreProjectItem
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.components.UserAvatarView
import com.androidexl.ideultimate.ui.components.languageColor
import com.androidexl.ideultimate.ui.components.savePickedProjectIcon
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.GlassStrokeTop
import com.androidexl.ideultimate.ui.theme.VioletSoft

private val ProfileEnterCurve = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * User Profile Screen:
 * - Centered user avatar + user name & email + Logout button
 * - 4 counters: المشاريع, اللايكات, عدم الإعجاب, المشتركين
 * - 2 Tabs: `Projects` and `Collection`
 * - In `Projects`: long-press opens bottom sheet (تعديل البيانات / حذف المشروع), FAB opens create form
 * - In `Collection`: shows items saved via "حفظ لاحقًا"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    storeRepository: CommunityStoreRepository,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onOpenCreateStoreProject: () -> Unit,
    onOpenEditStoreProject: (StoreProjectItem) -> Unit,
    onOpenViewStoreItem: (StoreProjectItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)

    // 0 = Projects tab, 1 = Collection tab
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var actionTarget by remember { mutableStateOf<StoreProjectItem?>(null) }
    var deleteTarget by remember { mutableStateOf<StoreProjectItem?>(null) }
    var showEditNameDialog by remember { mutableStateOf(false) }

    val pickAvatarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val saved = savePickedProjectIcon(context, uri)
            if (saved != null) {
                storeRepository.updateProfile(storeRepository.userName, saved)
            }
        }
    }

    val userProjects = storeRepository.userPublishedProjects()
    val collectionProjects = storeRepository.savedCollectionProjects()

    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 560, easing = ProfileEnterCurve))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 28.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(10.dp))

            // Top Back Bar + Edit Profile + Logout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x1EFFFFFF))
                        .border(1.dp, Color(0x36FFFFFF), RoundedCornerShape(14.dp))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.workspace_back),
                        tint = Ink,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x1EFFFFFF))
                            .border(1.dp, Color(0x36FFFFFF), RoundedCornerShape(12.dp))
                            .clickable { showEditNameDialog = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(17.dp),
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Fuchsia.copy(alpha = 0.16f))
                            .border(1.dp, Fuchsia.copy(alpha = 0.48f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onLogout)
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.auth_btn_logout),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Fuchsia,
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Profile Card: Centered Avatar + Name + 4 Stats
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // 1. User Icon Centered
                    Box(contentAlignment = Alignment.BottomEnd) {
                        UserAvatarView(
                            avatarPath = storeRepository.userAvatarPath,
                            size = 86.dp,
                            onClick = {
                                pickAvatarLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            },
                        )
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Violet, Cyan)))
                                .border(1.5.dp, Color(0xFF06091A), CircleShape)
                                .clickable {
                                    pickAvatarLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly,
                                        ),
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = Ink,
                                modifier = Modifier.size(15.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // 2. User Name below it
                    Text(
                        text = storeRepository.userName,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Ink,
                        textAlign = TextAlign.Center,
                    )
                    if (storeRepository.userEmail.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = storeRepository.userEmail,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 11.sp,
                            ),
                            color = Muted,
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // 3. Below it: Projects count, Likes count, Dislikes count, Subscribers count
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ProfileStatTile(
                            value = "${userProjects.size}",
                            label = stringResource(R.string.profile_stat_projects),
                            accent = Cyan,
                            modifier = Modifier.weight(1f),
                        )
                        ProfileStatTile(
                            value = "${storeRepository.totalUserLikes()}",
                            label = stringResource(R.string.profile_stat_likes),
                            accent = Teal,
                            modifier = Modifier.weight(1f),
                        )
                        ProfileStatTile(
                            value = "${storeRepository.totalUserDislikes()}",
                            label = stringResource(R.string.profile_stat_dislikes),
                            accent = Fuchsia,
                            modifier = Modifier.weight(1f),
                        )
                        ProfileStatTile(
                            value = "${storeRepository.subscribersCount}",
                            label = stringResource(R.string.profile_stat_subscribers),
                            accent = VioletSoft,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 4. Below it: 2 Tabs — Projects and Collection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x16070B1E))
                    .border(1.dp, Color(0x2AFFFFFF), RoundedCornerShape(18.dp))
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ProfileTabButton(
                    title = "Projects",
                    count = userProjects.size,
                    selected = selectedTab == 0,
                    accent = Cyan,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f),
                )
                ProfileTabButton(
                    title = "Collection",
                    count = collectionProjects.size,
                    selected = selectedTab == 1,
                    accent = VioletSoft,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            val displayedList = if (selectedTab == 0) userProjects else collectionProjects

            if (displayedList.isEmpty()) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = if (selectedTab == 0) {
                                stringResource(R.string.profile_empty_projects)
                            } else {
                                stringResource(R.string.profile_empty_collection)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    displayedList.forEach { project ->
                        ProfileStoreProjectCard(
                            project = project,
                            onClick = { onOpenViewStoreItem(project) },
                            onLongClick = {
                                if (selectedTab == 0) {
                                    actionTarget = project
                                } else {
                                    onOpenViewStoreItem(project)
                                }
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(124.dp + bottomBarPadding))
        }

        // FAB in Projects Tab -> Opens Create Store Item page
        if (selectedTab == 0) {
            FloatingActionButton(
                onClick = onOpenCreateStoreProject,
                containerColor = Color.Transparent,
                contentColor = Ink,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 22.dp, bottom = 22.dp + bottomBarPadding),
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Violet, Cyan)))
                        .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.store_form_create_title),
                        tint = Ink,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }

    // Long-press Bottom Sheet: تعديل البيانات or حذف المشروع
    val currentAction = actionTarget
    if (currentAction != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { actionTarget = null },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            scrimColor = Color(0xD903060F),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProjectIconView(
                        iconPreset = currentAction.iconPreset,
                        customIconPath = currentAction.customIconPath,
                        modifier = Modifier.size(48.dp),
                        cornerRadius = 14.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = currentAction.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                        )
                        Text(
                            text = currentAction.details,
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Option 1: تعديل البيانات (Edit data)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Cyan.copy(alpha = 0.14f))
                        .border(1.dp, Cyan.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                        .clickable {
                            val item = currentAction
                            actionTarget = null
                            onOpenEditStoreProject(item)
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.profile_action_edit_data),
                            style = MaterialTheme.typography.titleMedium,
                            color = Ink,
                        )
                        Text(
                            text = stringResource(R.string.profile_action_edit_data_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Option 2: حذف المشروع (Delete project)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Fuchsia.copy(alpha = 0.14f))
                        .border(1.dp, Fuchsia.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                        .clickable {
                            val item = currentAction
                            actionTarget = null
                            deleteTarget = item
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Fuchsia,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.action_delete_project),
                            style = MaterialTheme.typography.titleMedium,
                            color = Fuchsia,
                        )
                        Text(
                            text = stringResource(R.string.action_delete_project_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
            }
        }
    }

    val pendingDelete = deleteTarget
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Muted,
            title = {
                Text(
                    text = stringResource(R.string.delete_project_title, pendingDelete.name),
                    style = MaterialTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.delete_project_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        storeRepository.deleteStoreItem(pendingDelete.id)
                        deleteTarget = null
                    },
                ) {
                    Text(stringResource(R.string.delete_confirm), color = Fuchsia)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.dialog_cancel), color = Muted)
                }
            },
        )
    }

    if (showEditNameDialog) {
        var newName by remember { mutableStateOf(storeRepository.userName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = Midnight,
            titleContentColor = Ink,
            textContentColor = Ink,
            title = { Text(stringResource(R.string.dialog_rename)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text(stringResource(R.string.dialog_name_label)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = GlassStrokeTop,
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        storeRepository.updateProfile(newName, storeRepository.userAvatarPath)
                        showEditNameDialog = false
                    },
                ) {
                    Text(stringResource(R.string.dialog_save), color = Cyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel), color = Muted)
                }
            },
        )
    }
}

@Composable
private fun ProfileStatTile(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            ),
            color = accent,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProfileTabButton(
    title: String,
    count: Int,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(220),
        label = "prof-tab-bg",
    )
    val border by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.65f) else Color.Transparent,
        animationSpec = tween(220),
        label = "prof-tab-border",
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            ),
            color = if (selected) accent else Muted,
        )
        Spacer(Modifier.width(6.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(if (selected) accent.copy(alpha = 0.28f) else Color(0x18FFFFFF))
                .padding(horizontal = 7.dp, vertical = 2.dp),
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                ),
                color = if (selected) Ink else Muted,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileStoreProjectCard(
    project: StoreProjectItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val langColor = languageColor(project.language)
    val typeAccent = when (project.type) {
        StoreItemType.PROJECT -> Cyan
        StoreItemType.CODE_SNIPPET -> VioletSoft
        StoreItemType.LIBRARY -> Teal
    }
    val typeLabel = when (project.type) {
        StoreItemType.PROJECT -> stringResource(R.string.store_type_project)
        StoreItemType.CODE_SNIPPET -> stringResource(R.string.store_type_code)
        StoreItemType.LIBRARY -> stringResource(R.string.store_type_library)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xCC11183A), Color(0xCC0B1028)),
                ),
            )
            .border(1.dp, typeAccent.copy(alpha = 0.40f), RoundedCornerShape(22.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProjectIconView(
                iconPreset = project.iconPreset,
                customIconPath = project.customIconPath,
                modifier = Modifier.size(56.dp),
                cornerRadius = 16.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(typeAccent.copy(alpha = 0.18f))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = typeAccent,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(langColor.copy(alpha = 0.18f))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = project.language.shortBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = langColor,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = project.details,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "👍 ${project.likesCount}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Teal,
                    )
                    Text(
                        text = "👎 ${project.dislikesCount}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Fuchsia,
                    )
                    if (project.type == StoreItemType.PROJECT) {
                        Text(
                            text = "🔗 ${project.downloadLinks.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                            ),
                            color = Cyan,
                        )
                    }
                }
            }
        }
    }
}
