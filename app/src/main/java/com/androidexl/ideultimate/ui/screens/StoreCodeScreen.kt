package com.androidexl.ideultimate.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.CodeSnippetItem
import com.androidexl.ideultimate.data.CommunityStoreRepository
import com.androidexl.ideultimate.data.CustomLibraryItem
import com.androidexl.ideultimate.data.ProjectLanguage
import com.androidexl.ideultimate.data.StoreProjectItem
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.GradientButton
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.components.UserAvatarView
import com.androidexl.ideultimate.ui.components.languageColor
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

private val StoreCurve = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * Store Code screen featuring:
 * - Top Search Bar + User Profile Avatar next to it (opens UserProfileScreen on tap)
 * - Section 1: قسم المشاريع الجديدة (New Projects — tapping opens StoreProjectViewScreen)
 * - Section 2: قسم الأكواد البرمجية (Code Snippets — below New Projects)
 * - Section 3: قسم المكتبات الخاصة (Custom Libraries — below Code Snippets)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreCodeScreen(
    storeRepository: CommunityStoreRepository,
    onOpenProfile: () -> Unit,
    onOpenStoreProject: (StoreProjectItem) -> Unit,
    onUseSnippetAsProject: (CodeSnippetItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("ALL") }
    var previewSnippet by remember { mutableStateOf<CodeSnippetItem?>(null) }

    val q = searchQuery.trim().lowercase()

    val filteredProjects = storeRepository.projects.filter { item ->
        (selectedFilter == "ALL" || item.language.name == selectedFilter) &&
            (q.isEmpty() || item.name.lowercase().contains(q) || item.details.lowercase().contains(q))
    }

    val filteredSnippets = storeRepository.codeSnippets.filter { item ->
        (selectedFilter == "ALL" || item.language.name == selectedFilter) &&
            (q.isEmpty() || item.title.lowercase().contains(q) || item.description.lowercase().contains(q))
    }

    val filteredLibraries = storeRepository.customLibraries.filter { item ->
        (selectedFilter == "ALL" || item.language.name == selectedFilter) &&
            (q.isEmpty() || item.name.lowercase().contains(q) || item.summary.lowercase().contains(q))
    }

    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 640, easing = StoreCurve))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .graphicsLayer {
                alpha = enter.value
                translationY = (1f - enter.value) * 32.dp.toPx()
            },
    ) {
        Spacer(Modifier.height(10.dp))

        // Top Search Bar + User Avatar next to it
        SearchAndUserTopBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            avatarPath = storeRepository.userAvatarPath,
            isLoggedIn = storeRepository.isLoggedIn,
            onAvatarClick = onOpenProfile,
        )

        Spacer(Modifier.height(10.dp))

        FirebaseRealtimeStatusBar(
            projectId = storeRepository.firebaseProjectId,
            databaseUrl = storeRepository.firebaseDatabaseUrl,
            isConnected = storeRepository.isFirebaseConnected,
        )

        Spacer(Modifier.height(10.dp))

        // Language Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChipPill(
                label = stringResource(R.string.filter_all),
                selected = selectedFilter == "ALL",
                accent = Cyan,
                onClick = { selectedFilter = "ALL" },
            )
            ProjectLanguage.entries.forEach { lang ->
                FilterChipPill(
                    label = lang.displayName,
                    selected = selectedFilter == lang.name,
                    accent = languageColor(lang),
                    onClick = { selectedFilter = lang.name },
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // 1. قسم المشاريع الجديدة (New Projects Section)
        StoreSectionHeader(
            title = stringResource(R.string.section_new_projects),
            subtitle = stringResource(R.string.section_new_projects_desc),
            count = filteredProjects.size,
            accent = Cyan,
        )

        Spacer(Modifier.height(10.dp))

        if (filteredProjects.isEmpty()) {
            EmptySectionNotice(stringResource(R.string.workspace_empty_title))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredProjects.forEach { project ->
                    StoreProjectCard(
                        project = project,
                        onClick = { onOpenStoreProject(project) },
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // 2. قسم الأكواد البرمجية (Code Snippets Section — below New Projects)
        StoreSectionHeader(
            title = stringResource(R.string.section_code_snippets),
            subtitle = stringResource(R.string.section_code_snippets_desc),
            count = filteredSnippets.size,
            accent = VioletSoft,
        )

        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            filteredSnippets.forEach { snippet ->
                CodeSnippetCard(
                    snippet = snippet,
                    onPreview = { previewSnippet = snippet },
                    onCopy = { copyText(context, snippet.title, snippet.code) },
                    onUse = { onUseSnippetAsProject(snippet) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // 3. قسم المكتبات الخاصة (Custom Libraries Section — below Code Snippets)
        StoreSectionHeader(
            title = stringResource(R.string.section_custom_libraries),
            subtitle = stringResource(R.string.section_custom_libraries_desc),
            count = filteredLibraries.size,
            accent = Teal,
        )

        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            filteredLibraries.forEach { library ->
                CustomLibraryCard(
                    library = library,
                    onCopyDependency = {
                        copyText(context, library.name, library.dependencyLine)
                    },
                )
            }
        }

        Spacer(Modifier.height(124.dp + bottomBarPadding))
    }

    val currentPreview = previewSnippet
    if (currentPreview != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { previewSnippet = null },
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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProjectIconView(
                        iconPreset = currentPreview.iconPreset,
                        customIconPath = null,
                        modifier = Modifier.size(46.dp),
                        cornerRadius = 14.dp,
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = currentPreview.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                        )
                        Text(
                            text = currentPreview.description,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Muted,
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF060919))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(14.dp),
                    ) {
                        Text(
                            text = currentPreview.code,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                            ),
                            color = Ink,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GradientButton(
                        text = stringResource(R.string.store_use_as_project),
                        icon = Icons.Filled.Add,
                        onClick = {
                            val chosen = currentPreview
                            previewSnippet = null
                            onUseSnippetAsProject(chosen)
                        },
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x19FFFFFF))
                            .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                            .clickable { copyText(context, currentPreview.title, currentPreview.code) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.store_copy_code),
                            style = MaterialTheme.typography.labelLarge,
                            color = Cyan,
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun SearchAndUserTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    avatarPath: String?,
    isLoggedIn: Boolean,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = {
                Text(
                    text = stringResource(R.string.search_placeholder),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onQueryChange("") },
                    )
                }
            },
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                cursorColor = Cyan,
                focusedBorderColor = Cyan.copy(alpha = 0.85f),
                unfocusedBorderColor = Color(0x30FFFFFF),
                focusedContainerColor = Color(0x18FFFFFF),
                unfocusedContainerColor = Color(0x10FFFFFF),
                focusedPlaceholderColor = Muted,
                unfocusedPlaceholderColor = Muted,
            ),
        )

        Spacer(Modifier.width(10.dp))

        UserAvatarView(
            avatarPath = avatarPath,
            isLoggedIn = isLoggedIn,
            modifier = Modifier
                .size(50.dp)
                .clickable(onClick = onAvatarClick),
        )
    }
}

@Composable
fun StoreSectionHeader(
    title: String,
    subtitle: String,
    count: Int,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(accent),
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.5.sp),
                color = Ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = Muted,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(accent.copy(alpha = 0.16f))
                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                .padding(horizontal = 10.dp, vertical = 3.dp),
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                ),
                color = accent,
            )
        }
    }
}

@Composable
fun StoreProjectCard(
    project: StoreProjectItem,
    onClick: () -> Unit,
) {
    val accent = languageColor(project.language)
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProjectIconView(
                iconPreset = project.iconPreset,
                customIconPath = project.customIconPath,
                modifier = Modifier.size(54.dp),
                cornerRadius = 17.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(accent.copy(alpha = 0.16f))
                            .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = project.language.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = accent,
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
                        color = Color(0xFFFF8A8A),
                    )
                    if (project.savedInCollection) {
                        Text(
                            text = "★ Collection",
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

@Composable
fun CodeSnippetCard(
    snippet: CodeSnippetItem,
    onPreview: () -> Unit,
    onCopy: () -> Unit,
    onUse: () -> Unit,
) {
    val accent = languageColor(snippet.language)
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPreview),
        contentPadding = PaddingValues(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProjectIconView(
                iconPreset = snippet.iconPreset,
                customIconPath = null,
                modifier = Modifier.size(48.dp),
                cornerRadius = 15.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = snippet.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp),
                    color = Ink,
                )
                Text(
                    text = snippet.description,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(accent.copy(alpha = 0.16f))
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = snippet.language.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = accent,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x99060919))
                    .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                Text(
                    text = snippet.code.lines().take(3).joinToString("\n"),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 9.5.sp,
                        lineHeight = 14.sp,
                    ),
                    color = Ink.copy(alpha = 0.9f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Violet.copy(alpha = 0.65f), Cyan.copy(alpha = 0.50f)),
                        ),
                    )
                    .clickable(onClick = onUse)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.store_use_as_project),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = Color.White,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(11.dp))
                    .clickable(onClick = onCopy)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.store_copy_code),
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                    color = Cyan,
                )
            }
        }
    }
}

@Composable
fun CustomLibraryCard(
    library: CustomLibraryItem,
    onCopyDependency: () -> Unit,
) {
    val accent = languageColor(library.language)
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.5.sp),
                        color = Ink,
                    )
                    Text(
                        text = "v${library.version}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 9.5.sp,
                        ),
                        color = Teal,
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = library.summary,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = Muted,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(accent.copy(alpha = 0.16f))
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text(
                    text = library.language.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = accent,
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(11.dp))
                    .background(Color(0x99060919))
                    .border(1.dp, Teal.copy(alpha = 0.35f), RoundedCornerShape(11.dp))
                    .clickable(onClick = onCopyDependency)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = library.dependencyLine,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 9.5.sp,
                    ),
                    color = Teal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "COPY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                    ),
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun FirebaseRealtimeStatusBar(
    projectId: String,
    databaseUrl: String,
    isConnected: Boolean,
    modifier: Modifier = Modifier,
) {
    val dotColor = if (isConnected) Teal else Cyan
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x16090F26))
                .border(1.dp, dotColor.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(dotColor),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Firebase RTDB • $projectId",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = Ink,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = databaseUrl.removePrefix("https://"),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                ),
                color = dotColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun EmptySectionNotice(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x10FFFFFF))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}

@Composable
private fun FilterChipPill(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.22f) else Color(0x12FFFFFF),
        animationSpec = tween(200),
        label = "chip-bg",
    )
    val border by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.65f) else Color(0x22FFFFFF),
        animationSpec = tween(200),
        label = "chip-border",
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 7.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            color = if (selected) accent else Muted,
        )
    }
}

private fun copyText(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(
        context,
        context.getString(R.string.store_copied_msg, label),
        Toast.LENGTH_SHORT,
    ).show()
}
