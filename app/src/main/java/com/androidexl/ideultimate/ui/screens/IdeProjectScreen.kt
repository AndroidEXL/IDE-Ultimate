package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.ProjectXmlPage
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.data.WorkspaceRepository
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.GlassIconButton
import com.androidexl.ideultimate.ui.components.GradientButton
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.components.languageColor
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

private enum class IdeProjectTab(val title: String) {
    VIEW("View"),
    LOGIC("Logic"),
    EMULATOR("Emulator"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeProjectScreen(
    project: WorkspaceItem,
    repository: WorkspaceRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val xmlPages = remember(project.id) {
        mutableStateListOf<ProjectXmlPage>().apply {
            addAll(repository.listProjectXmlPages(project))
        }
    }

    var selectedXmlFileName by rememberSaveable(project.id) {
        mutableStateOf(xmlPages.firstOrNull()?.xmlFileName ?: "activity_main.xml")
    }
    val selectedXmlPage = remember(selectedXmlFileName, xmlPages.size, project) {
        xmlPages.firstOrNull { it.xmlFileName == selectedXmlFileName }
            ?: xmlPages.firstOrNull()
            ?: repository.xmlPageForName(project, "activity_main.xml")
    }

    var activeTabName by rememberSaveable(project.id) { mutableStateOf(IdeProjectTab.VIEW.name) }
    val activeTab = remember(activeTabName) {
        runCatching { IdeProjectTab.valueOf(activeTabName) }.getOrDefault(IdeProjectTab.VIEW)
    }

    var codeText by remember(project.id, selectedXmlPage.xmlFileName) {
        mutableStateOf(repository.readActivityCode(project, selectedXmlPage))
    }

    LaunchedEffect(project.id, selectedXmlPage.xmlFileName) {
        codeText = repository.readActivityCode(project, selectedXmlPage)
    }

    var showXmlBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showAddXmlDialog by rememberSaveable { mutableStateOf(false) }
    var showTempRunScreen by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        if (showTempRunScreen) {
            showTempRunScreen = false
        } else {
            onBack()
        }
    }

    if (showTempRunScreen) {
        TemporaryRunScreen(
            project = project,
            activeXmlPage = selectedXmlPage,
            onClose = { showTempRunScreen = false },
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        // 1. Top Section: Back + Project Icon next to Program Name + Run Button
        IdeProjectTopBar(
            project = project,
            onBack = onBack,
            onRunClick = { showTempRunScreen = true },
        )

        // 2. 3 Tabs below Top Section: View | Logic | Emulator
        IdeProjectTabsBar(
            activeTab = activeTab,
            onSelectTab = { tab -> activeTabName = tab.name },
        )

        // 3. Tab Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            when (activeTab) {
                IdeProjectTab.VIEW -> {
                    // View tab: empty canvas inside
                    IdeEmptyWorkspaceCanvas()
                }

                IdeProjectTab.LOGIC -> {
                    // Logic tab: displays code text for the Activity paired with the selected XML page
                    IdeLogicCodeEditor(
                        project = project,
                        xmlPage = selectedXmlPage,
                        code = codeText,
                        onCodeChange = { updated ->
                            codeText = updated
                            repository.saveActivityCode(project, selectedXmlPage, updated)
                        },
                    )
                }

                IdeProjectTab.EMULATOR -> {
                    // Emulator tab: empty for now
                    IdeEmptyWorkspaceCanvas()
                }
            }
        }

        // 4. Bottom XML / Activity Selector Bar (shown below View & Logic)
        if (activeTab == IdeProjectTab.VIEW || activeTab == IdeProjectTab.LOGIC) {
            IdeBottomPageSelectorBar(
                activeTab = activeTab,
                xmlPage = selectedXmlPage,
                onClick = { showXmlBottomSheet = true },
                bottomBarPadding = bottomBarPadding,
            )
        } else {
            Spacer(Modifier.height(12.dp + bottomBarPadding))
        }
    }

    // Bottom Sheet showing all XML layout pages & their corresponding Activities
    if (showXmlBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showXmlBottomSheet = false },
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
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.ide_xml_sheet_title),
                            style = MaterialTheme.typography.titleLarge,
                            color = Ink,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.ide_xml_sheet_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Muted,
                        )
                    }

                    // Button to add a new XML Layout page
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Cyan.copy(alpha = 0.16f))
                            .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                            .clickable {
                                showXmlBottomSheet = false
                                showAddXmlDialog = true
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.ide_xml_add_btn),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Cyan,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                xmlPages.forEach { page ->
                    val isSelected = page.xmlFileName == selectedXmlPage.xmlFileName
                    XmlLayoutSheetRow(
                        page = page,
                        selected = isSelected,
                        onClick = {
                            selectedXmlFileName = page.xmlFileName
                            showXmlBottomSheet = false
                        },
                    )
                }

                Spacer(Modifier.height(10.dp))
            }
        }
    }

    if (showAddXmlDialog) {
        AddXmlLayoutDialog(
            onDismiss = { showAddXmlDialog = false },
            onConfirm = { rawName ->
                val created = repository.addProjectXmlPage(project, rawName)
                if (xmlPages.none { it.xmlFileName == created.xmlFileName }) {
                    xmlPages.add(created)
                }
                selectedXmlFileName = created.xmlFileName
                showAddXmlDialog = false
            },
        )
    }
}

@Composable
private fun IdeProjectTopBar(
    project: WorkspaceItem,
    onBack: () -> Unit,
    onRunClick: () -> Unit,
) {
    val langAccent = languageColor(project.language)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.workspace_back),
            onClick = onBack,
            modifier = Modifier.size(42.dp),
        )

        Spacer(Modifier.width(12.dp))

        // Project Icon right next to Program Name ("القسم اللي فوق حط الايقونة بجانبها اسم البرنامج")
        ProjectIconView(
            iconPreset = project.iconPreset,
            customIconPath = project.customIconPath,
            modifier = Modifier.size(42.dp),
            cornerRadius = 13.dp,
        )

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = "IDE Project • ${project.language.displayName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 9.5.sp,
                    ),
                    color = langAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // Run Button ("وزر Run يفتح صفحة موقتة")
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Teal, Cyan),
                    ),
                )
                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(14.dp))
                .clickable(onClick = onRunClick)
                .padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(R.string.ide_run_button),
                tint = Color(0xFF04101E),
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(4.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = stringResource(R.string.ide_run_button),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                    color = Color(0xFF04101E),
                )
            }
        }
    }
}

@Composable
private fun IdeProjectTabsBar(
    activeTab: IdeProjectTab,
    onSelectTab: (IdeProjectTab) -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x99070B1E))
                .border(1.dp, Color(0x24FFFFFF), RoundedCornerShape(16.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            IdeProjectTab.entries.forEach { tab ->
                val selected = tab == activeTab
                val tabAccent = when (tab) {
                    IdeProjectTab.VIEW -> Cyan
                    IdeProjectTab.LOGIC -> VioletSoft
                    IdeProjectTab.EMULATOR -> Teal
                }
                val bg by animateColorAsState(
                    targetValue = if (selected) tabAccent.copy(alpha = 0.22f) else Color.Transparent,
                    animationSpec = tween(220),
                    label = "ide-tab-bg",
                )
                val border by animateColorAsState(
                    targetValue = if (selected) tabAccent.copy(alpha = 0.65f) else Color.Transparent,
                    animationSpec = tween(220),
                    label = "ide-tab-border",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(12.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 12.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (selected) tabAccent else Muted,
                    )
                }
            }
        }
    }
}

@Composable
private fun IdeEmptyWorkspaceCanvas() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x66060A1C))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(22.dp)),
    )
}

@Composable
private fun IdeLogicCodeEditor(
    project: WorkspaceItem,
    xmlPage: ProjectXmlPage,
    code: String,
    onCodeChange: (String) -> Unit,
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val lineCount = remember(code) { code.lines().size.coerceAtLeast(1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xCC06091A))
            .border(1.dp, VioletSoft.copy(alpha = 0.35f), RoundedCornerShape(22.dp)),
    ) {
        // Top header bar inside Logic Editor showing the active Activity class & XML layout
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x990B1028))
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Teal),
                    )
                    Text(
                        text = xmlPage.codeFileName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Ink,
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Cyan.copy(alpha = 0.14f))
                        .border(1.dp, Cyan.copy(alpha = 0.40f), RoundedCornerShape(percent = 50))
                        .padding(horizontal = 10.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = "${xmlPage.xmlFileName}  •  ${xmlPage.activityName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Cyan,
                    )
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x1FFFFFFF)),
        )

        // Code area with line numbers and editable code text
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
                    .padding(vertical = 12.dp),
            ) {
                // Line numbers column
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 10.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    for (lineIdx in 1..lineCount) {
                        Text(
                            text = lineIdx.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                lineHeight = 20.sp,
                            ),
                            color = Muted.copy(alpha = 0.55f),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0x1FFFFFFF)),
                )

                BasicTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        color = Ink,
                    ),
                    cursorBrush = SolidColor(Cyan),
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScroll)
                        .padding(horizontal = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun IdeBottomPageSelectorBar(
    activeTab: IdeProjectTab,
    xmlPage: ProjectXmlPage,
    onClick: () -> Unit,
    bottomBarPadding: Dp = 0.dp,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = 12.dp + bottomBarPadding,
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xE60B112C),
                            Color(0xE611193E),
                        ),
                    ),
                )
                .border(1.2.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (activeTab == IdeProjectTab.VIEW) {
                                Cyan.copy(alpha = 0.18f)
                            } else {
                                VioletSoft.copy(alpha = 0.20f)
                            },
                        )
                        .border(
                            1.dp,
                            if (activeTab == IdeProjectTab.VIEW) Cyan.copy(alpha = 0.50f) else VioletSoft.copy(alpha = 0.50f),
                            RoundedCornerShape(8.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text = if (activeTab == IdeProjectTab.VIEW) "XML" else "ACTIVITY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (activeTab == IdeProjectTab.VIEW) Cyan else VioletSoft,
                    )
                }

                Column {
                    Text(
                        text = if (activeTab == IdeProjectTab.VIEW) {
                            xmlPage.xmlFileName
                        } else {
                            "${xmlPage.activityName} (${xmlPage.codeFileName})"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Ink,
                    )
                    Text(
                        text = if (activeTab == IdeProjectTab.VIEW) {
                            "Activity: ${xmlPage.activityName}"
                        } else {
                            "Layout: ${xmlPage.xmlFileName}"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Muted,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = xmlPage.xmlFileName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Cyan,
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun XmlLayoutSheetRow(
    page: ProjectXmlPage,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (selected) Cyan.copy(alpha = 0.16f) else Color(0x12FFFFFF))
                .border(
                    width = 1.dp,
                    color = if (selected) Cyan.copy(alpha = 0.55f) else Color(0x22FFFFFF),
                    shape = shape,
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Fuchsia.copy(alpha = 0.16f))
                        .border(1.dp, Fuchsia.copy(alpha = 0.40f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = "XML",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Fuchsia,
                    )
                }

                Column {
                    Text(
                        text = page.xmlFileName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (selected) Cyan else Ink,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${page.activityName} • ${page.codeFileName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.5.sp,
                        ),
                        color = Muted,
                    )
                }
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Cyan.copy(alpha = 0.22f))
                        .border(1.dp, Cyan, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun AddXmlLayoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1230),
        titleContentColor = Ink,
        textContentColor = Ink,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = stringResource(R.string.ide_xml_add_dialog_title),
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.ide_xml_add_dialog_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        placeholder = { Text("activity_profile.xml") },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMono,
                            color = Ink,
                        ),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Ink,
                            unfocusedTextColor = Ink,
                            cursorColor = Cyan,
                            focusedBorderColor = Cyan,
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedContainerColor = Color(0x14FFFFFF),
                            unfocusedContainerColor = Color(0x0AFFFFFF),
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.trim().isNotEmpty()) {
                        onConfirm(name.trim())
                    }
                },
            ) {
                Text(stringResource(R.string.dialog_create), color = Cyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel), color = Muted)
            }
        },
    )
}

@Composable
private fun TemporaryRunScreen(
    project: WorkspaceItem,
    activeXmlPage: ProjectXmlPage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val langAccent = languageColor(project.language)
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        // Top bar of Temporary Run Page
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.workspace_back),
                onClick = onClose,
                modifier = Modifier.size(42.dp),
            )
            Spacer(Modifier.width(12.dp))
            ProjectIconView(
                iconPreset = project.iconPreset,
                customIconPath = project.customIconPath,
                modifier = Modifier.size(38.dp),
                cornerRadius = 12.dp,
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(R.string.ide_temp_run_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = Teal,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(24.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    ProjectIconView(
                        iconPreset = project.iconPreset,
                        customIconPath = project.customIconPath,
                        modifier = Modifier.size(76.dp),
                        cornerRadius = 22.dp,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.ide_temp_run_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium.copy(color = Cyan),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x80070B1E))
                                .border(1.dp, langAccent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "${activeXmlPage.activityName}  •  ${activeXmlPage.xmlFileName}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.5.sp,
                                ),
                                color = langAccent,
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.ide_temp_run_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(22.dp))
                    GradientButton(
                        text = stringResource(R.string.ide_temp_run_close),
                        onClick = onClose,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
