package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.CodeEditorFlavor
import com.androidexl.ideultimate.data.IdeWidgetCategory
import com.androidexl.ideultimate.data.IdeWidgetNode
import com.androidexl.ideultimate.data.IdeWidgetType
import com.androidexl.ideultimate.data.ProjectLanguage
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
import com.androidexl.ideultimate.ui.theme.VioletSoft
import kotlin.math.roundToInt

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

    // Default Code Editor flavor based on project language (user can switch between Java Android, Kotlin Android, xml Android)
    var activeFlavorName by rememberSaveable(project.id) {
        mutableStateOf(
            if (project.language == ProjectLanguage.JAVA) {
                CodeEditorFlavor.JAVA_ANDROID.name
            } else {
                CodeEditorFlavor.KOTLIN_ANDROID.name
            },
        )
    }
    val activeFlavor = remember(activeFlavorName) {
        runCatching { CodeEditorFlavor.valueOf(activeFlavorName) }
            .getOrDefault(CodeEditorFlavor.KOTLIN_ANDROID)
    }

    // Visual widget nodes placed inside the white phone frame for the current XML page
    var widgetNodes by remember(project.id, selectedXmlPage.xmlFileName) {
        mutableStateOf(repository.loadWidgetsForXmlPage(project, selectedXmlPage))
    }

    // Code text for the currently selected XML page & editor flavor (Java Android / Kotlin Android / xml Android)
    var codeText by remember(project.id, selectedXmlPage.xmlFileName, activeFlavor) {
        mutableStateOf(repository.readCodeForFlavor(project, selectedXmlPage, activeFlavor))
    }

    LaunchedEffect(project.id, selectedXmlPage.xmlFileName, activeFlavor, activeTab) {
        widgetNodes = repository.loadWidgetsForXmlPage(project, selectedXmlPage)
        codeText = repository.readCodeForFlavor(project, selectedXmlPage, activeFlavor)
    }

    fun updateWidgetNodes(newNodes: List<IdeWidgetNode>) {
        widgetNodes = newNodes
        repository.saveWidgetsForXmlPage(project, selectedXmlPage, newNodes)
        if (activeFlavor == CodeEditorFlavor.XML_ANDROID) {
            codeText = repository.readCodeForFlavor(project, selectedXmlPage, CodeEditorFlavor.XML_ANDROID)
        }
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
            widgetNodes = widgetNodes,
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
                .padding(horizontal = 10.dp, vertical = 4.dp),
        ) {
            when (activeTab) {
                IdeProjectTab.VIEW -> {
                    // View tab: Widgets palette on the side + Phone Frame with WHITE screen & drag-and-drop
                    IdeViewDesigner(
                        project = project,
                        xmlPage = selectedXmlPage,
                        widgetNodes = widgetNodes,
                        onUpdateWidgets = { updateWidgetNodes(it) },
                    )
                }

                IdeProjectTab.LOGIC -> {
                    // Logic tab: Code Editor supporting Java Android, Kotlin Android, and xml Android
                    IdeLogicCodeEditor(
                        project = project,
                        xmlPage = selectedXmlPage,
                        activeFlavor = activeFlavor,
                        onSelectFlavor = { flavor ->
                            activeFlavorName = flavor.name
                            codeText = repository.readCodeForFlavor(project, selectedXmlPage, flavor)
                        },
                        code = codeText,
                        onCodeChange = { updated ->
                            codeText = updated
                            repository.saveCodeForFlavor(project, selectedXmlPage, activeFlavor, updated)
                        },
                    )
                }

                IdeProjectTab.EMULATOR -> {
                    // Emulator tab: empty for now
                    IdeEmptyWorkspaceCanvas()
                }
            }
        }

        // 4. Compact Bottom XML / Activity Selector Pill (shown below View & Logic)
        if (activeTab == IdeProjectTab.VIEW || activeTab == IdeProjectTab.LOGIC) {
            IdeBottomPageSelectorBar(
                activeTab = activeTab,
                xmlPage = selectedXmlPage,
                activeFlavor = activeFlavor,
                onClick = { showXmlBottomSheet = true },
                bottomBarPadding = bottomBarPadding,
            )
        } else {
            Spacer(Modifier.height(8.dp + bottomBarPadding))
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

// ============================================================================
// 1. Top Bar & 3-Tabs Bar
// ============================================================================

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
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlassIconButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.workspace_back),
            onClick = onBack,
            modifier = Modifier.size(40.dp),
        )

        Spacer(Modifier.width(10.dp))

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
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = "IDE Project • ${project.language.displayName}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 9.sp,
                    ),
                    color = langAccent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(Teal, Cyan)))
                .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(12.dp))
                .clickable(onClick = onRunClick)
                .padding(horizontal = 13.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = stringResource(R.string.ide_run_button),
                tint = Color(0xFF04101E),
                modifier = Modifier.size(17.dp),
            )
            Spacer(Modifier.width(3.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    text = stringResource(R.string.ide_run_button),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 11.5.sp,
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
                .padding(horizontal = 14.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0x99070B1E))
                .border(1.dp, Color(0x24FFFFFF), RoundedCornerShape(14.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
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
                    animationSpec = tween(200),
                    label = "ide-tab-bg",
                )
                val border by animateColorAsState(
                    targetValue = if (selected) tabAccent.copy(alpha = 0.65f) else Color.Transparent,
                    animationSpec = tween(200),
                    label = "ide-tab-border",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(11.dp))
                        .background(bg)
                        .border(1.dp, border, RoundedCornerShape(11.dp))
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (selected) tabAccent else Muted,
                    )
                }
            }
        }
    }
}

// ============================================================================
// 2. VIEW Tab: Widgets Palette + White Phone Frame with Drag & Drop
// ============================================================================

@Composable
private fun IdeViewDesigner(
    project: WorkspaceItem,
    xmlPage: ProjectXmlPage,
    widgetNodes: List<IdeWidgetNode>,
    onUpdateWidgets: (List<IdeWidgetNode>) -> Unit,
) {
    var rootBounds by remember { mutableStateOf(Rect.Zero) }
    var phoneCanvasBounds by remember { mutableStateOf(Rect.Zero) }
    val containerBoundsMap = remember { mutableStateMapOf<String, Rect>() }

    var draggingType by remember { mutableStateOf<IdeWidgetType?>(null) }
    var dragFingerInRoot by remember { mutableStateOf(Offset.Zero) }
    var selectedWidgetUid by remember { mutableStateOf<String?>(null) }
    var editingTextViewNode by remember { mutableStateOf<IdeWidgetNode?>(null) }

    // Determine which container (or phone root) the dragged item is currently hovering over
    val hoveredContainerUid: String? = remember(draggingType, dragFingerInRoot, containerBoundsMap.toMap()) {
        if (draggingType == null) {
            null
        } else {
            // Pick the smallest (deepest nested) container whose bounds contain the finger
            containerBoundsMap.entries
                .filter { (_, rect) -> !rect.isEmpty && rect.contains(dragFingerInRoot) }
                .minByOrNull { (_, rect) -> rect.width * rect.height }
                ?.key
        }
    }

    val isHoveringPhoneCanvas = remember(draggingType, dragFingerInRoot, phoneCanvasBounds) {
        draggingType != null && !phoneCanvasBounds.isEmpty && phoneCanvasBounds.contains(dragFingerInRoot)
    }

    fun nextViewIdFor(type: IdeWidgetType): String {
        val total = countNodesOfType(widgetNodes, type) + 1
        return "${type.idPrefix}_$total"
    }

    fun addWidgetToTarget(type: IdeWidgetType, targetContainerUid: String?) {
        val newNode = IdeWidgetNode(
            viewId = nextViewIdFor(type),
            type = type,
            text = if (type == IdeWidgetType.TEXT_VIEW) "TextView" else type.displayName,
        )
        val updated = if (targetContainerUid != null) {
            insertChildIntoContainer(widgetNodes, targetContainerUid, newNode)
        } else {
            widgetNodes + newNode
        }
        onUpdateWidgets(updated)
        selectedWidgetUid = newNode.uid
    }

    val selectedNode = remember(widgetNodes, selectedWidgetUid) {
        selectedWidgetUid?.let { findNodeByUid(widgetNodes, it) }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    rootBounds = coords.boundsInRoot()
                },
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Left Panel: Widgets Palette ("widget ... قسم layout ... قسم text")
                WidgetsPaletteColumn(
                    onStartDrag = { type, startRootPos ->
                        draggingType = type
                        dragFingerInRoot = startRootPos
                    },
                    onDragBy = { delta ->
                        dragFingerInRoot += delta
                    },
                    onEndDrag = {
                        val droppedType = draggingType
                        if (droppedType != null) {
                            when {
                                hoveredContainerUid != null -> {
                                    addWidgetToTarget(droppedType, hoveredContainerUid)
                                }
                                isHoveringPhoneCanvas || dragFingerInRoot.x >= phoneCanvasBounds.left - 24f -> {
                                    // Dropped onto the phone frame (or into a selected container if it is a container)
                                    val targetContainer = selectedNode?.takeIf { it.type.isContainer }?.uid
                                    addWidgetToTarget(droppedType, targetContainer)
                                }
                            }
                        }
                        draggingType = null
                    },
                    onCancelDrag = {
                        draggingType = null
                    },
                    onTapAdd = { type ->
                        val targetContainer = selectedNode?.takeIf { it.type.isContainer }?.uid
                        addWidgetToTarget(type, targetContainer)
                    },
                    modifier = Modifier
                        .width(126.dp)
                        .fillMaxHeight(),
                )

                // Right Area: Phone Frame with White Interior ("وحط جانببها فريم هاتف و جوه الفريم ابيض")
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Selected widget quick bar (Edit text / Delete widget / Clear)
                    if (selectedNode != null) {
                        SelectedWidgetBar(
                            node = selectedNode,
                            onEditText = {
                                if (selectedNode.type == IdeWidgetType.TEXT_VIEW) {
                                    editingTextViewNode = selectedNode
                                }
                            },
                            onDelete = {
                                onUpdateWidgets(removeNodeByUid(widgetNodes, selectedNode.uid))
                                containerBoundsMap.remove(selectedNode.uid)
                                selectedWidgetUid = null
                            },
                            onDeselect = { selectedWidgetUid = null },
                        )
                        Spacer(Modifier.height(6.dp))
                    }

                    // The Phone Frame ("فريم هاتف و جوه الفريم ابيض")
                    PhoneFrameWhiteCanvas(
                        project = project,
                        xmlPage = xmlPage,
                        widgetNodes = widgetNodes,
                        selectedWidgetUid = selectedWidgetUid,
                        hoveredContainerUid = hoveredContainerUid,
                        isPhoneHighlighted = isHoveringPhoneCanvas && hoveredContainerUid == null,
                        onSelectWidget = { node -> selectedWidgetUid = node.uid },
                        onDoubleTapTextView = { node -> editingTextViewNode = node },
                        onPhoneCanvasPositioned = { rect -> phoneCanvasBounds = rect },
                        onContainerPositioned = { uid, rect -> containerBoundsMap[uid] = rect },
                        onTapEmptyWhiteArea = { selectedWidgetUid = null },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    )
                }
            }

            // Floating Drag Preview Badge while dragging a widget from Widgets to Phone Frame
            val activeDrag = draggingType
            if (activeDrag != null && !rootBounds.isEmpty) {
                val localX = (dragFingerInRoot.x - rootBounds.left).roundToInt() - 55
                val localY = (dragFingerInRoot.y - rootBounds.top).roundToInt() - 22
                Box(
                    modifier = Modifier
                        .offset { IntOffset(localX, localY) }
                        .shadow(10.dp, RoundedCornerShape(10.dp))
                        .clip(RoundedCornerShape(10.dp))
                        .background( Color(0xF20D1536))
                        .border(1.5.dp, Cyan, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        WidgetTagIcon(type = activeDrag)
                        Text(
                            text = activeDrag.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }

    val editingNode = editingTextViewNode
    if (editingNode != null) {
        EditTextViewDialog(
            initialText = editingNode.text,
            viewId = editingNode.viewId,
            onDismiss = { editingTextViewNode = null },
            onConfirm = { newText ->
                onUpdateWidgets(updateNodeText(widgetNodes, editingNode.uid, newText))
                editingTextViewNode = null
            },
        )
    }
}

@Composable
private fun WidgetsPaletteColumn(
    onStartDrag: (IdeWidgetType, Offset) -> Unit,
    onDragBy: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onCancelDrag: () -> Unit,
    onTapAdd: (IdeWidgetType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xD9080D24))
            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(18.dp))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Palette Title: Widgets
        Text(
            text = "Widgets",
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = JetBrainsMono,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
            color = Cyan,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        )

        // Section 1: layout ("قسم layout: Linear Layout H, Linear Layout V, Scroll V, Scroll H")
        WidgetCategoryHeader(title = "layout", accent = Cyan)

        IdeWidgetType.entries
            .filter { it.category == IdeWidgetCategory.LAYOUT }
            .forEach { widgetType ->
                DraggableWidgetPaletteItem(
                    type = widgetType,
                    onStartDrag = onStartDrag,
                    onDragBy = onDragBy,
                    onEndDrag = onEndDrag,
                    onCancelDrag = onCancelDrag,
                    onTap = { onTapAdd(widgetType) },
                )
            }

        Spacer(Modifier.height(4.dp))

        // Section 2: text ("قسم text: Text View")
        WidgetCategoryHeader(title = "text", accent = VioletSoft)

        IdeWidgetType.entries
            .filter { it.category == IdeWidgetCategory.TEXT }
            .forEach { widgetType ->
                DraggableWidgetPaletteItem(
                    type = widgetType,
                    onStartDrag = onStartDrag,
                    onDragBy = onDragBy,
                    onEndDrag = onEndDrag,
                    onCancelDrag = onCancelDrag,
                    onTap = { onTapAdd(widgetType) },
                )
            }
    }
}

@Composable
private fun WidgetCategoryHeader(
    title: String,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 7.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMono,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = accent,
        )
    }
}

@Composable
private fun DraggableWidgetPaletteItem(
    type: IdeWidgetType,
    onStartDrag: (IdeWidgetType, Offset) -> Unit,
    onDragBy: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onCancelDrag: () -> Unit,
    onTap: () -> Unit,
) {
    var itemBoundsInRoot by remember { mutableStateOf(Rect.Zero) }
    val shape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                itemBoundsInRoot = coords.boundsInRoot()
            }
            .clip(shape)
            .background(Color(0x16FFFFFF))
            .border(1.dp, Color(0x28FFFFFF), shape)
            .clickable(onClick = onTap)
            .pointerInput(type) {
                detectDragGestures(
                    onDragStart = { localOffset ->
                        val startInRoot = Offset(
                            x = itemBoundsInRoot.left + localOffset.x,
                            y = itemBoundsInRoot.top + localOffset.y,
                        )
                        onStartDrag(type, startInRoot)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragBy(dragAmount)
                    },
                    onDragEnd = { onEndDrag() },
                    onDragCancel = { onCancelDrag() },
                )
            }
            .padding(horizontal = 7.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        WidgetTagIcon(type = type)
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMono,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 12.sp,
            ),
            color = Ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WidgetTagIcon(type: IdeWidgetType) {
    val accent = when (type) {
        IdeWidgetType.LINEAR_H -> Cyan
        IdeWidgetType.LINEAR_V -> Color(0xFF60A5FA)
        IdeWidgetType.SCROLL_V -> Teal
        IdeWidgetType.SCROLL_H -> Color(0xFF34D399)
        IdeWidgetType.TEXT_VIEW -> Fuchsia
    }
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(accent.copy(alpha = 0.20f))
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = type.shortTag,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMono,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
            ),
            color = accent,
        )
    }
}

@Composable
private fun SelectedWidgetBar(
    node: IdeWidgetNode,
    onEditText: () -> Unit,
    onDelete: () -> Unit,
    onDeselect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xD90B1230))
            .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f),
        ) {
            WidgetTagIcon(type = node.type)
            Column {
                Text(
                    text = "@+id/${node.viewId}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Cyan,
                    maxLines = 1,
                )
                Text(
                    text = node.type.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 8.5.sp,
                    ),
                    color = Muted,
                    maxLines = 1,
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (node.type == IdeWidgetType.TEXT_VIEW) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Cyan.copy(alpha = 0.18f))
                        .clickable(onClick = onEditText)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Create,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            text = "Text",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Cyan,
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x28FF5C5C))
                    .clickable(onClick = onDelete)
                    .padding(horizontal = 7.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFFFF8080),
                    modifier = Modifier.size(14.dp),
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x1FFFFFFF))
                    .clickable(onClick = onDeselect)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

@Composable
private fun PhoneFrameWhiteCanvas(
    project: WorkspaceItem,
    xmlPage: ProjectXmlPage,
    widgetNodes: List<IdeWidgetNode>,
    selectedWidgetUid: String?,
    hoveredContainerUid: String?,
    isPhoneHighlighted: Boolean,
    onSelectWidget: (IdeWidgetNode) -> Unit,
    onDoubleTapTextView: (IdeWidgetNode) -> Unit,
    onPhoneCanvasPositioned: (Rect) -> Unit,
    onContainerPositioned: (String, Rect) -> Unit,
    onTapEmptyWhiteArea: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bezelShape = RoundedCornerShape(26.dp)
    val screenShape = RoundedCornerShape(19.dp)
    val frameBorderColor = if (isPhoneHighlighted) Cyan else Color(0xFF2B3558)

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Outer Phone Bezel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(bezelShape)
                .background(Color(0xFF0E1328))
                .border(2.2.dp, frameBorderColor, bezelShape)
                .padding(7.dp),
        ) {
            // Top Speaker Notch inside Phone Bezel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color(0xFF2D3656)),
                )
            }

            // Inner Phone Screen — WHITE BACKGROUND ("جوه الفريم ابيض")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(screenShape)
                    .background(Color.White)
                    .border(
                        width = if (isPhoneHighlighted) 2.dp else 1.dp,
                        color = if (isPhoneHighlighted) Cyan else Color(0xFFD1D5DB),
                        shape = screenShape,
                    )
                    .onGloballyPositioned { coords ->
                        onPhoneCanvasPositioned(coords.boundsInRoot())
                    }
                    .clickable(onClick = onTapEmptyWhiteArea),
            ) {
                // Phone Status Bar + App Toolbar preview header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "9:41",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFF374151),
                    )
                    Text(
                        text = xmlPage.xmlFileName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 8.5.sp,
                        ),
                        color = Color(0xFF6B7280),
                    )
                    Text(
                        text = "100%",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFF374151),
                    )
                }

                // Pure WHITE canvas where dragged widgets are placed and rendered
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (widgetNodes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.ide_view_drag_hint),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.5.sp,
                                ),
                                color = Color(0xFF9CA3AF),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp),
                            )
                        }
                    } else {
                        widgetNodes.forEach { node ->
                            RenderWidgetNodeOnPhone(
                                node = node,
                                selectedWidgetUid = selectedWidgetUid,
                                hoveredContainerUid = hoveredContainerUid,
                                onSelectWidget = onSelectWidget,
                                onDoubleTapTextView = onDoubleTapTextView,
                                onContainerPositioned = onContainerPositioned,
                            )
                        }
                    }
                }

                // Bottom Android Home Gesture Bar inside Phone Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .height(3.5.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color(0xFFCBD5E1)),
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderWidgetNodeOnPhone(
    node: IdeWidgetNode,
    selectedWidgetUid: String?,
    hoveredContainerUid: String?,
    onSelectWidget: (IdeWidgetNode) -> Unit,
    onDoubleTapTextView: (IdeWidgetNode) -> Unit,
    onContainerPositioned: (String, Rect) -> Unit,
) {
    val isSelected = node.uid == selectedWidgetUid
    val isHoveredDropTarget = node.uid == hoveredContainerUid

    when (node.type) {
        IdeWidgetType.LINEAR_H -> {
            val borderColor = when {
                isHoveredDropTarget -> Color(0xFF00D4FF)
                isSelected -> Color(0xFF2563EB)
                else -> Color(0xFF93C5FD)
            }
            val bgColor = when {
                isHoveredDropTarget -> Color(0xFFE0F2FE)
                isSelected -> Color(0xFFEFF6FF)
                else -> Color(0xFFF8FAFC)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .onGloballyPositioned { coords ->
                        onContainerPositioned(node.uid, coords.boundsInRoot())
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected || isHoveredDropTarget) 1.8.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable { onSelectWidget(node) }
                    .padding(6.dp),
            ) {
                ContainerBadgeLabel(
                    label = "LinearLayout H • ${node.viewId}",
                    color = Color(0xFF1D4ED8),
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (node.children.isEmpty()) {
                        EmptyContainerHint("Linear Layout H (أفقي)")
                    } else {
                        node.children.forEach { child ->
                            Box(Modifier.widthIn(min = 64.dp)) {
                                RenderWidgetNodeOnPhone(
                                    node = child,
                                    selectedWidgetUid = selectedWidgetUid,
                                    hoveredContainerUid = hoveredContainerUid,
                                    onSelectWidget = onSelectWidget,
                                    onDoubleTapTextView = onDoubleTapTextView,
                                    onContainerPositioned = onContainerPositioned,
                                )
                            }
                        }
                    }
                }
            }
        }

        IdeWidgetType.LINEAR_V -> {
            val borderColor = when {
                isHoveredDropTarget -> Color(0xFF00D4FF)
                isSelected -> Color(0xFF4F46E5)
                else -> Color(0xFFA5B4FC)
            }
            val bgColor = when {
                isHoveredDropTarget -> Color(0xFFE0F2FE)
                isSelected -> Color(0xFFEEF2FF)
                else -> Color(0xFFF8FAFC)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp)
                    .onGloballyPositioned { coords ->
                        onContainerPositioned(node.uid, coords.boundsInRoot())
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected || isHoveredDropTarget) 1.8.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable { onSelectWidget(node) }
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                ContainerBadgeLabel(
                    label = "LinearLayout V • ${node.viewId}",
                    color = Color(0xFF4338CA),
                )
                if (node.children.isEmpty()) {
                    EmptyContainerHint("Linear Layout V (عمودي)")
                } else {
                    node.children.forEach { child ->
                        RenderWidgetNodeOnPhone(
                            node = child,
                            selectedWidgetUid = selectedWidgetUid,
                            hoveredContainerUid = hoveredContainerUid,
                            onSelectWidget = onSelectWidget,
                            onDoubleTapTextView = onDoubleTapTextView,
                            onContainerPositioned = onContainerPositioned,
                        )
                    }
                }
            }
        }

        IdeWidgetType.SCROLL_V -> {
            val borderColor = when {
                isHoveredDropTarget -> Color(0xFF00D4FF)
                isSelected -> Color(0xFF0284C7)
                else -> Color(0xFF7DD3FC)
            }
            val bgColor = when {
                isHoveredDropTarget -> Color(0xFFE0F2FE)
                isSelected -> Color(0xFFF0F9FF)
                else -> Color(0xFFF8FAFC)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .onGloballyPositioned { coords ->
                        onContainerPositioned(node.uid, coords.boundsInRoot())
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected || isHoveredDropTarget) 1.8.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable { onSelectWidget(node) }
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                ContainerBadgeLabel(
                    label = "Scroll V (↕) • ${node.viewId}",
                    color = Color(0xFF0369A1),
                )
                if (node.children.isEmpty()) {
                    EmptyContainerHint("Scroll V (تمرير عمودي)")
                } else {
                    node.children.forEach { child ->
                        RenderWidgetNodeOnPhone(
                            node = child,
                            selectedWidgetUid = selectedWidgetUid,
                            hoveredContainerUid = hoveredContainerUid,
                            onSelectWidget = onSelectWidget,
                            onDoubleTapTextView = onDoubleTapTextView,
                            onContainerPositioned = onContainerPositioned,
                        )
                    }
                }
            }
        }

        IdeWidgetType.SCROLL_H -> {
            val borderColor = when {
                isHoveredDropTarget -> Color(0xFF00D4FF)
                isSelected -> Color(0xFF059669)
                else -> Color(0xFF6EE7B7)
            }
            val bgColor = when {
                isHoveredDropTarget -> Color(0xFFE0F2FE)
                isSelected -> Color(0xFFECFDF5)
                else -> Color(0xFFF8FAFC)
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .onGloballyPositioned { coords ->
                        onContainerPositioned(node.uid, coords.boundsInRoot())
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected || isHoveredDropTarget) 1.8.dp else 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clickable { onSelectWidget(node) }
                    .padding(6.dp),
            ) {
                ContainerBadgeLabel(
                    label = "Scroll H (↔) • ${node.viewId}",
                    color = Color(0xFF047857),
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (node.children.isEmpty()) {
                        EmptyContainerHint("Scroll H (تمرير أفقي)")
                    } else {
                        node.children.forEach { child ->
                            Box(Modifier.widthIn(min = 64.dp)) {
                                RenderWidgetNodeOnPhone(
                                    node = child,
                                    selectedWidgetUid = selectedWidgetUid,
                                    hoveredContainerUid = hoveredContainerUid,
                                    onSelectWidget = onSelectWidget,
                                    onDoubleTapTextView = onDoubleTapTextView,
                                    onContainerPositioned = onContainerPositioned,
                                )
                            }
                        }
                    }
                }
            }
        }

        IdeWidgetType.TEXT_VIEW -> {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF3F4F6))
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) Color(0xFF2563EB) else Color(0xFFD1D5DB),
                        shape = RoundedCornerShape(6.dp),
                    )
                    .clickable {
                        if (isSelected) {
                            onDoubleTapTextView(node)
                        } else {
                            onSelectWidget(node)
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
                Text(
                    text = node.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color(0xFF111827),
                )
            }
        }
    }
}

@Composable
private fun ContainerBadgeLabel(
    label: String,
    color: Color,
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = JetBrainsMono,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
        ),
        color = color,
    )
}

@Composable
private fun EmptyContainerHint(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = JetBrainsMono,
            fontSize = 9.sp,
        ),
        color = Color(0xFF94A3B8),
        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
    )
}

// ============================================================================
// 3. LOGIC Tab: Android Code Editor (Java Android | Kotlin Android | xml Android)
// ============================================================================

@Composable
private fun IdeLogicCodeEditor(
    project: WorkspaceItem,
    xmlPage: ProjectXmlPage,
    activeFlavor: CodeEditorFlavor,
    onSelectFlavor: (CodeEditorFlavor) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val lineCount = remember(code) { code.lines().size.coerceAtLeast(1) }
    val syntaxTransformation = remember(activeFlavor) {
        AndroidCodeSyntaxTransformation(activeFlavor)
    }

    val currentFileLabel = remember(xmlPage, activeFlavor) {
        when (activeFlavor) {
            CodeEditorFlavor.KOTLIN_ANDROID -> "${xmlPage.activityName}.kt"
            CodeEditorFlavor.JAVA_ANDROID -> "${xmlPage.activityName}.java"
            CodeEditorFlavor.XML_ANDROID -> xmlPage.xmlFileName
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xE606091A))
            .border(1.dp, VioletSoft.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
    ) {
        // 1. Code Editor Mode Switcher: Java Android | Kotlin Android | xml Android
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xCC0B1028))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CodeEditorFlavor.entries.forEach { flavor ->
                        val selected = flavor == activeFlavor
                        val accent = when (flavor) {
                            CodeEditorFlavor.JAVA_ANDROID -> Color(0xFFFBBF24)
                            CodeEditorFlavor.KOTLIN_ANDROID -> VioletSoft
                            CodeEditorFlavor.XML_ANDROID -> Cyan
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (selected) accent.copy(alpha = 0.22f) else Color(0x14FFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (selected) accent.copy(alpha = 0.70f) else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(9.dp),
                                )
                                .clickable { onSelectFlavor(flavor) }
                                .padding(vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = flavor.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = if (selected) accent else Muted,
                                maxLines = 1,
                            )
                        }
                    }
                }

                // Active file & linked XML indicator row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(Teal),
                        )
                        Text(
                            text = currentFileLabel,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                    }

                    Text(
                        text = "${xmlPage.xmlFileName} ➔ ${xmlPage.activityName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 9.5.sp,
                        ),
                        color = Cyan,
                    )
                }
            }
        }

        // 2. Quick Symbol Bar for fast Android coding on mobile
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            val symbols = remember(activeFlavor) {
                when (activeFlavor) {
                    CodeEditorFlavor.XML_ANDROID ->
                        listOf("<", ">", "/>", "=", "\"\"", "android:", "@+id/", "match_parent", "wrap_content")
                    CodeEditorFlavor.JAVA_ANDROID, CodeEditorFlavor.KOTLIN_ANDROID ->
                        listOf("    ", "{", "}", "(", ")", ";", ".", "=", "\"\"", "R.id.", "R.layout.")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x99080C20))
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                symbols.forEach { sym ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1FFFFFFF))
                            .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(6.dp))
                            .clickable { onCodeChange(code + sym) }
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = if (sym == "    ") "Tab" else sym,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Cyan,
                        )
                    }
                }
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x1FFFFFFF)),
        )

        // 3. Syntax-Highlighted Code Area with Line Numbers
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
                    .padding(vertical = 10.dp),
            ) {
                Column(
                    modifier = Modifier.padding(start = 10.dp, end = 8.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    for (lineIdx in 1..lineCount) {
                        Text(
                            text = lineIdx.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 11.5.sp,
                                lineHeight = 19.sp,
                            ),
                            color = Muted.copy(alpha = 0.50f),
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
                    visualTransformation = syntaxTransformation,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 11.5.sp,
                        lineHeight = 19.sp,
                        color = Ink,
                    ),
                    cursorBrush = SolidColor(Cyan),
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScroll)
                        .padding(horizontal = 10.dp),
                )
            }
        }
    }
}

private class AndroidCodeSyntaxTransformation(
    private val flavor: CodeEditorFlavor,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = highlightAndroidCode(text.text, flavor),
            offsetMapping = OffsetMapping.Identity,
        )
    }
}

private fun highlightAndroidCode(raw: String, flavor: CodeEditorFlavor): AnnotatedString {
    val builder = AnnotatedString.Builder(raw)
    if (raw.isEmpty()) return builder.toAnnotatedString()

    when (flavor) {
        CodeEditorFlavor.XML_ANDROID -> {
            // XML tags
            Regex("</?[A-Za-z0-9_.:-]+|/?>").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            // XML attributes (e.g. android:layout_width)
            Regex("\\b[A-Za-z0-9_]+:[A-Za-z0-9_]+").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFFC084FC)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            // XML strings
            Regex("\"[^\"\n]*\"").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF34D399)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            // XML comments
            Regex("<!--[\\s\\S]*?-->").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF64748B)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
        }

        CodeEditorFlavor.JAVA_ANDROID, CodeEditorFlavor.KOTLIN_ANDROID -> {
            val keywords = if (flavor == CodeEditorFlavor.JAVA_ANDROID) {
                Regex("\\b(package|import|public|private|protected|class|interface|extends|implements|void|int|long|boolean|float|double|return|if|else|for|while|new|super|this|null|true|false|static|final)\\b")
            } else {
                Regex("\\b(package|import|class|interface|object|fun|val|var|override|private|public|protected|internal|return|if|else|when|for|while|super|this|null|true|false)\\b")
            }
            keywords.findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFFF472B6), fontWeight = FontWeight.Bold),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            // Android classes & R references
            Regex("\\b(AppCompatActivity|Activity|Bundle|TextView|LinearLayout|ScrollView|HorizontalScrollView|View|R|layout|id)\\b").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            // Annotations (@Override, etc.)
            Regex("@[A-Za-z0-9_]+").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFFFBBF24)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            // Strings
            Regex("\"[^\"\n]*\"").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF34D399)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            // Comments
            Regex("//[^\n]*|/\\*[\\s\\S]*?\\*/").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF64748B)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
        }
    }
    return builder.toAnnotatedString()
}

// ============================================================================
// 4. Compact Bottom XML / Activity Selector Button
// ============================================================================

@Composable
private fun IdeBottomPageSelectorBar(
    activeTab: IdeProjectTab,
    xmlPage: ProjectXmlPage,
    activeFlavor: CodeEditorFlavor,
    onClick: () -> Unit,
    bottomBarPadding: Dp = 0.dp,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    top = 2.dp,
                    bottom = 8.dp + bottomBarPadding,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Compact, sleek pill selector (not wide, not tall!)
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Color(0xE60B112C))
                    .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(percent = 50))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            if (activeTab == IdeProjectTab.VIEW) {
                                Cyan.copy(alpha = 0.20f)
                            } else {
                                VioletSoft.copy(alpha = 0.22f)
                            },
                        )
                        .padding(horizontal = 6.dp, vertical = 1.5.dp),
                ) {
                    Text(
                        text = if (activeTab == IdeProjectTab.VIEW) "XML" else activeFlavor.extension.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (activeTab == IdeProjectTab.VIEW) Cyan else VioletSoft,
                    )
                }

                Text(
                    text = if (activeTab == IdeProjectTab.VIEW) {
                        xmlPage.xmlFileName
                    } else {
                        "${xmlPage.activityName} • ${xmlPage.xmlFileName}"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Ink,
                    maxLines = 1,
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(15.dp),
                )
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
                .padding(horizontal = 14.dp, vertical = 11.dp),
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
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = if (selected) Cyan else Ink,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "${page.activityName} (.kt / .java)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Muted,
                    )
                }
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Cyan.copy(alpha = 0.22f))
                        .border(1.dp, Cyan, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(15.dp),
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
private fun EditTextViewDialog(
    initialText: String,
    viewId: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initialText) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1230),
        titleContentColor = Ink,
        textContentColor = Ink,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "TextView (@+id/$viewId)",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("android:text") },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Ink),
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
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(text.ifBlank { "TextView" }) }) {
                Text(stringResource(R.string.dialog_save), color = Cyan)
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
    widgetNodes: List<IdeWidgetNode>,
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

        Spacer(Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.ide_temp_run_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(6.dp))
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x80070B1E))
                                .border(1.dp, langAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = "${activeXmlPage.activityName}  •  ${activeXmlPage.xmlFileName}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.5.sp,
                                ),
                                color = langAccent,
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Live preview of the white phone canvas inside Temporary Run Page
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp, max = 320.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            if (widgetNodes.isEmpty()) {
                                Text(
                                    text = "${project.name} - ${activeXmlPage.activityName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color(0xFF111827),
                                )
                            } else {
                                widgetNodes.forEach { node ->
                                    RenderWidgetNodeOnPhone(
                                        node = node,
                                        selectedWidgetUid = null,
                                        hoveredContainerUid = null,
                                        onSelectWidget = {},
                                        onDoubleTapTextView = {},
                                        onContainerPositioned = { _, _ -> },
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
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

// ============================================================================
// Widget Tree Helper Functions
// ============================================================================

private fun countNodesOfType(nodes: List<IdeWidgetNode>, type: IdeWidgetType): Int {
    var count = 0
    nodes.forEach { node ->
        if (node.type == type) count += 1
        count += countNodesOfType(node.children, type)
    }
    return count
}

private fun findNodeByUid(nodes: List<IdeWidgetNode>, uid: String): IdeWidgetNode? {
    for (node in nodes) {
        if (node.uid == uid) return node
        val found = findNodeByUid(node.children, uid)
        if (found != null) return found
    }
    return null
}

private fun insertChildIntoContainer(
    nodes: List<IdeWidgetNode>,
    containerUid: String,
    newChild: IdeWidgetNode,
): List<IdeWidgetNode> = nodes.map { node ->
    when {
        node.uid == containerUid && node.type.isContainer ->
            node.copy(children = node.children + newChild)
        node.children.isNotEmpty() ->
            node.copy(children = insertChildIntoContainer(node.children, containerUid, newChild))
        else -> node
    }
}

private fun removeNodeByUid(
    nodes: List<IdeWidgetNode>,
    targetUid: String,
): List<IdeWidgetNode> = nodes
    .filter { it.uid != targetUid }
    .map { node ->
        if (node.children.isEmpty()) {
            node
        } else {
            node.copy(children = removeNodeByUid(node.children, targetUid))
        }
    }

private fun updateNodeText(
    nodes: List<IdeWidgetNode>,
    targetUid: String,
    newText: String,
): List<IdeWidgetNode> = nodes.map { node ->
    when {
        node.uid == targetUid -> node.copy(text = newText)
        node.children.isNotEmpty() -> node.copy(children = updateNodeText(node.children, targetUid, newText))
        else -> node
    }
}
