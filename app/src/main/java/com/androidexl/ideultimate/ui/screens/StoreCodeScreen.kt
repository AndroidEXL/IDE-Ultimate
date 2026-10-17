package com.androidexl.ideultimate.ui.screens

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.androidexl.ideultimate.data.CommunityStoreRepository
import com.androidexl.ideultimate.data.ProjectLanguage
import com.androidexl.ideultimate.data.StoreProjectItem
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
 * - Top Search Bar + User Profile Avatar / Login Button next to it
 * - Section 1: قسم المشاريع الجديدة (New Projects — tapping opens StoreProjectViewScreen)
 * - Section 2: قسم الأكواد البرمجية (Code Snippets — tapping opens StoreCodeSnippetViewScreen)
 * - Section 3: قسم المكتبات الخاصة (Custom Libraries — tapping opens StoreLibraryViewScreen)
 */
@Composable
fun StoreCodeScreen(
    storeRepository: CommunityStoreRepository,
    onAvatarOrAuthClick: () -> Unit,
    onOpenStoreItem: (StoreProjectItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedFilter by rememberSaveable { mutableStateOf("ALL") }

    val q = searchQuery.trim().lowercase()

    val filteredProjects = storeRepository.newProjects().filter { item ->
        (selectedFilter == "ALL" || item.language.name == selectedFilter) &&
            (q.isEmpty() || item.name.lowercase().contains(q) || item.details.lowercase().contains(q))
    }

    val filteredSnippets = storeRepository.codeSnippetItems().filter { item ->
        (selectedFilter == "ALL" || item.language.name == selectedFilter) &&
            (q.isEmpty() || item.name.lowercase().contains(q) || item.details.lowercase().contains(q))
    }

    val filteredLibraries = storeRepository.customLibraryItems().filter { item ->
        (selectedFilter == "ALL" || item.language.name == selectedFilter) &&
            (q.isEmpty() || item.name.lowercase().contains(q) || item.details.lowercase().contains(q))
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

        // Top Search Bar + User Avatar / Login trigger next to it
        SearchAndUserTopBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            avatarPath = storeRepository.userAvatarPath,
            isLoggedIn = storeRepository.isLoggedIn,
            onAvatarClick = onAvatarOrAuthClick,
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
                        onClick = { onOpenStoreItem(project) },
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

        if (filteredSnippets.isEmpty()) {
            EmptySectionNotice(stringResource(R.string.workspace_empty_title))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredSnippets.forEach { snippet ->
                    CodeSnippetStoreCard(
                        item = snippet,
                        onClick = { onOpenStoreItem(snippet) },
                        onCopy = { copyCodeToClipboard(context, snippet.name, snippet.codeContent) },
                    )
                }
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

        if (filteredLibraries.isEmpty()) {
            EmptySectionNotice(stringResource(R.string.workspace_empty_title))
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredLibraries.forEach { library ->
                    CustomLibraryStoreCard(
                        item = library,
                        onClick = { onOpenStoreItem(library) },
                        onCopy = { copyCodeToClipboard(context, library.name, library.codeContent) },
                    )
                }
            }
        }

        Spacer(Modifier.height(124.dp + bottomBarPadding))
    }
}

/**
 * Shared Top Search Bar + User Avatar / Login button next to it.
 */
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
            placeholder = {
                Text(
                    text = stringResource(R.string.search_placeholder),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
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
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Cyan,
                unfocusedBorderColor = Color(0x30FFFFFF),
                cursorColor = Cyan,
                focusedTextColor = Ink,
                unfocusedTextColor = Ink,
                focusedContainerColor = Color(0x18070B1E),
                unfocusedContainerColor = Color(0x18070B1E),
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.width(10.dp))

        if (isLoggedIn) {
            UserAvatarView(
                avatarPath = avatarPath,
                size = 48.dp,
                onClick = onAvatarClick,
            )
        } else {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Brush.linearGradient(listOf(Violet, Cyan)))
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp))
                    .clickable(onClick = onAvatarClick)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.auth_tab_login),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                    ),
                    color = Ink,
                )
            }
        }
    }
}

@Composable
private fun StoreSectionHeader(
    title: String,
    subtitle: String,
    count: Int,
    accent: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(accent),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Muted,
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.16f))
                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
                color = accent,
            )
        }
    }
}

@Composable
private fun StoreProjectCard(
    project: StoreProjectItem,
    onClick: () -> Unit,
) {
    val langColor = languageColor(project.language)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xD811183A), Color(0xD80A0F28)),
                ),
            )
            .border(1.2.dp, langColor.copy(alpha = 0.44f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProjectIconView(
                iconPreset = project.iconPreset,
                customIconPath = project.customIconPath,
                modifier = Modifier.size(60.dp),
                cornerRadius = 18.dp,
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        ),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .background(langColor.copy(alpha = 0.18f))
                            .border(1.dp, langColor.copy(alpha = 0.48f), RoundedCornerShape(7.dp))
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
                Spacer(Modifier.height(8.dp))
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
                    if (project.authorName.isNotBlank()) {
                        Text(
                            text = "👤 ${project.authorName}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Cyan,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CodeSnippetStoreCard(
    item: StoreProjectItem,
    onClick: () -> Unit,
    onCopy: () -> Unit,
) {
    val langColor = languageColor(item.language)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xCC11183A), Color(0xCC0A0E24)),
                ),
            )
            .border(1.dp, VioletSoft.copy(alpha = 0.42f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProjectIconView(
                    iconPreset = item.iconPreset,
                    customIconPath = item.customIconPath,
                    modifier = Modifier.size(48.dp),
                    cornerRadius = 14.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(langColor.copy(alpha = 0.18f))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = item.language.shortBadge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 9.sp,
                                ),
                                color = langColor,
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
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
                        color = Fuchsia,
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x16FFFFFF))
                        .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(10.dp))
                        .clickable(onClick = onCopy)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.store_copy_code),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                        color = Cyan,
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomLibraryStoreCard(
    item: StoreProjectItem,
    onClick: () -> Unit,
    onCopy: () -> Unit,
) {
    val langColor = languageColor(item.language)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xCC0D1834), Color(0xCC091024)),
                ),
            )
            .border(1.dp, Teal.copy(alpha = 0.40f), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProjectIconView(
                    iconPreset = item.iconPreset,
                    customIconPath = item.customIconPath,
                    modifier = Modifier.size(48.dp),
                    cornerRadius = 14.dp,
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(7.dp))
                                .background(langColor.copy(alpha = 0.18f))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = item.language.shortBadge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 9.sp,
                                ),
                                color = langColor,
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF06091A))
                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(12.dp))
                        .clickable(onClick = onCopy)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.codeContent,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Teal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.store_copy_code),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                        ),
                        color = Cyan,
                    )
                }
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
