package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
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
import com.androidexl.ideultimate.data.StoreProjectItem
import com.androidexl.ideultimate.ui.components.GlassIconButton
import com.androidexl.ideultimate.ui.components.LogoMark
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
import com.androidexl.ideultimate.ui.theme.VioletSoft

private enum class ProfileTab { Projects, Collection }
private val ProfileEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * User Profile screen opened by tapping the user avatar next to the search bar:
 * - Centered user icon
 * - Below it: User Name
 * - Below it: Projects count, Likes count, Dislikes count, Subscribers count
 * - Below it: 2 Tabs (Collection & Projects)
 *   - Projects tab: user's added projects, long-press bottom sheet (تعديل البيانات / حذف المشروع),
 *     and a FAB button to create a new Store project.
 *   - Collection tab: saved-for-later projects; tapping opens the same StoreProjectViewScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    storeRepository: CommunityStoreRepository,
    onBack: () -> Unit,
    onOpenStoreProject: (StoreProjectItem) -> Unit,
    onCreateStoreProject: () -> Unit,
    onEditStoreProject: (StoreProjectItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    var activeTabName by rememberSaveable { mutableStateOf(ProfileTab.Projects.name) }
    val activeTab = runCatching { ProfileTab.valueOf(activeTabName) }.getOrDefault(ProfileTab.Projects)

    var actionTarget by remember { mutableStateOf<StoreProjectItem?>(null) }
    var showEditNameDialog by remember { mutableStateOf(false) }

    val userProjects = storeRepository.userPublishedProjects()
    val collectionProjects = storeRepository.savedCollectionProjects()

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            savePickedProjectIcon(context, uri)?.let { savedPath ->
                storeRepository.updateProfile(storeRepository.userName, savedPath)
            }
        }
    }

    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 620, easing = ProfileEase))
    }

    BackHandler(onBack = onBack)

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 34.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                GlassIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.workspace_back),
                    onClick = onBack,
                    modifier = Modifier.size(44.dp),
                )
                Text(
                    text = stringResource(R.string.profile_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                LogoMark(modifier = Modifier.size(40.dp), animated = false)
            }

            Spacer(Modifier.height(10.dp))

            // 1. Centered User Icon ("أيقونة المستخدم في النص")
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.clickable { avatarPicker.launch("image/*") },
            ) {
                UserAvatarView(
                    avatarPath = storeRepository.userAvatarPath,
                    isLoggedIn = storeRepository.isLoggedIn,
                    modifier = Modifier.size(90.dp),
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Cyan)
                        .border(2.dp, Color(0xFF060919), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = null,
                        tint = Color(0xFF060919),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // 2. Centered User Name below Icon ("وتحتها الاسم")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { showEditNameDialog = true },
            ) {
                Text(
                    text = storeRepository.userName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                    color = Ink,
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.Create,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(15.dp),
                )
            }

            Spacer(Modifier.height(14.dp))

            // 3. Stats Row: Projects, Likes, Dislikes, Subscribers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProfileStatBox(
                    modifier = Modifier.weight(1f),
                    value = userProjects.size.toString(),
                    label = stringResource(R.string.profile_stat_projects),
                    accent = Cyan,
                )
                ProfileStatBox(
                    modifier = Modifier.weight(1f),
                    value = storeRepository.totalUserLikes().toString(),
                    label = stringResource(R.string.profile_stat_likes),
                    accent = Teal,
                )
                ProfileStatBox(
                    modifier = Modifier.weight(1f),
                    value = storeRepository.totalUserDislikes().toString(),
                    label = stringResource(R.string.profile_stat_dislikes),
                    accent = Color(0xFFFF8A8A),
                )
                ProfileStatBox(
                    modifier = Modifier.weight(1f),
                    value = storeRepository.subscribersCount.toString(),
                    label = stringResource(R.string.profile_stat_subscribers),
                    accent = VioletSoft,
                )
            }

            Spacer(Modifier.height(16.dp))

            // 4. Below stats: 2 Tabs — Collection and Projects
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x16FFFFFF))
                    .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(20.dp))
                    .padding(5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ProfileTabButton(
                    modifier = Modifier.weight(1f),
                    label = "Projects (${userProjects.size})",
                    selected = activeTab == ProfileTab.Projects,
                    accent = Cyan,
                    onClick = { activeTabName = ProfileTab.Projects.name },
                )
                ProfileTabButton(
                    modifier = Modifier.weight(1f),
                    label = "Collection (${collectionProjects.size})",
                    selected = activeTab == ProfileTab.Collection,
                    accent = VioletSoft,
                    onClick = { activeTabName = ProfileTab.Collection.name },
                )
            }

            Spacer(Modifier.height(12.dp))

            val displayedList = if (activeTab == ProfileTab.Projects) userProjects else collectionProjects

            if (displayedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(
                            if (activeTab == ProfileTab.Projects) {
                                R.string.profile_empty_projects
                            } else {
                                R.string.profile_empty_collection
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        top = 4.dp,
                        bottom = 128.dp + bottomBarPadding,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(displayedList, key = { it.id }) { item ->
                        ProfileProjectCard(
                            item = item,
                            allowLongPress = activeTab == ProfileTab.Projects,
                            onClick = { onOpenStoreProject(item) },
                            onLongClick = {
                                if (activeTab == ProfileTab.Projects) {
                                    actionTarget = item
                                }
                            },
                        )
                    }
                }
            }
        }

        // FAB in Projects tab to create a new project
        if (activeTab == ProfileTab.Projects) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 18.dp, bottom = 18.dp + bottomBarPadding)
                    .size(60.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Violet, Cyan),
                        ),
                    )
                    .border(1.2.dp, Color(0x66FFFFFF), RoundedCornerShape(20.dp))
                    .clickable(onClick = onCreateStoreProject),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.store_form_create_title),
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }

    // Long-press bottom sheet on Projects tab: تعديل البيانات / حذف المشروع
    val currentTarget = actionTarget
    if (currentTarget != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { actionTarget = null },
            sheetState = sheetState,
            containerColor = Color(0xFF0B1028),
            contentColor = Ink,
            scrimColor = Color(0xD903060F),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = currentTarget.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.sheet_project_actions),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )

                // 1. تعديل البيانات
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Cyan.copy(alpha = 0.14f))
                        .border(1.dp, Cyan.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                        .clickable {
                            val item = currentTarget
                            actionTarget = null
                            onEditStoreProject(item)
                        }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Create,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.profile_action_edit_data),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                            color = Ink,
                        )
                        Text(
                            text = stringResource(R.string.profile_action_edit_data_desc),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Muted,
                        )
                    }
                }

                // 2. حذف المشروع
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1AFF5C5C))
                        .border(1.dp, Color(0x40FF5C5C), RoundedCornerShape(16.dp))
                        .clickable {
                            val id = currentTarget.id
                            actionTarget = null
                            storeRepository.deleteStoreProject(id)
                        }
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFF8A8A),
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.action_delete_project),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                            color = Color(0xFFFF9B9B),
                        )
                        Text(
                            text = stringResource(R.string.action_delete_project_desc),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Muted,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showEditNameDialog) {
        var newName by remember { mutableStateOf(storeRepository.userName) }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = Color(0xFF0D1230),
            titleContentColor = Ink,
            textContentColor = Muted,
            shape = RoundedCornerShape(24.dp),
            title = { Text(stringResource(R.string.dialog_rename)) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { if (it.length <= 30) newName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                        cursorColor = Cyan,
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                    ),
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
private fun ProfileStatBox(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.16f), Color(0x0EFFFFFF)),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.38f), RoundedCornerShape(16.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMono,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = accent,
            maxLines = 1,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
            color = Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ProfileTabButton(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.24f) else Color.Transparent,
        animationSpec = tween(200),
        label = "prof-tab-bg",
    )
    val border by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.65f) else Color.Transparent,
        animationSpec = tween(200),
        label = "prof-tab-border",
    )
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(15.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMono,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            ),
            color = if (selected) Color.White else Muted,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfileProjectCard(
    item: StoreProjectItem,
    allowLongPress: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val accent = languageColor(item.language)
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = 0.14f), Color(0x14FFFFFF)),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.42f), shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (allowLongPress) onLongClick else null,
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProjectIconView(
            iconPreset = item.iconPreset,
            customIconPath = item.customIconPath,
            modifier = Modifier.size(54.dp),
            cornerRadius = 16.dp,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = item.details,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "👍 ${item.likesCount}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                    ),
                    color = Teal,
                )
                Text(
                    text = "👎 ${item.dislikesCount}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                    ),
                    color = Color(0xFFFF8A8A),
                )
            }
        }
    }
}
