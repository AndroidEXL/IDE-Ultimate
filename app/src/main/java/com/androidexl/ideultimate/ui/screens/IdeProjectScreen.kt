package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
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
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.VioletSoft
import kotlin.math.abs
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

    var widgetNodes by remember(project.id, selectedXmlPage.xmlFileName) {
        mutableStateOf(repository.loadWidgetsForXmlPage(project, selectedXmlPage))
    }
    val undoStack = remember(project.id, selectedXmlPage.xmlFileName) {
        mutableStateListOf<List<IdeWidgetNode>>()
    }
    val redoStack = remember(project.id, selectedXmlPage.xmlFileName) {
        mutableStateListOf<List<IdeWidgetNode>>()
    }

    var codeText by remember(project.id, selectedXmlPage.xmlFileName, activeFlavor) {
        mutableStateOf(repository.readCodeForFlavor(project, selectedXmlPage, activeFlavor))
    }

    LaunchedEffect(project.id, selectedXmlPage.xmlFileName, activeFlavor, activeTab) {
        widgetNodes = repository.loadWidgetsForXmlPage(project, selectedXmlPage)
        codeText = repository.readCodeForFlavor(project, selectedXmlPage, activeFlavor)
    }

    fun updateWidgetNodes(newNodes: List<IdeWidgetNode>, recordHistory: Boolean = true) {
        if (recordHistory) {
            undoStack.add(widgetNodes)
            redoStack.clear()
        }
        widgetNodes = newNodes
        repository.saveWidgetsForXmlPage(project, selectedXmlPage, newNodes)
        if (activeFlavor == CodeEditorFlavor.XML_ANDROID) {
            codeText = repository.readCodeForFlavor(project, selectedXmlPage, CodeEditorFlavor.XML_ANDROID)
        }
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val previous = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(widgetNodes)
            updateWidgetNodes(previous, recordHistory = false)
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(widgetNodes)
            updateWidgetNodes(next, recordHistory = false)
        }
    }

    var showXmlBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showAddXmlDialog by rememberSaveable { mutableStateOf(false) }
    var showTempRunScreen by rememberSaveable { mutableStateOf(false) }

    var selectedWidgetUid by remember(project.id, selectedXmlPage.xmlFileName) {
        mutableStateOf<String?>(null)
    }
    var isDraggingWidget by remember { mutableStateOf(false) }
    var editingTextViewNode by remember { mutableStateOf<IdeWidgetNode?>(null) }
    var editingIdNode by remember { mutableStateOf<IdeWidgetNode?>(null) }
    var editingSizeNode by remember { mutableStateOf<Pair<IdeWidgetNode, Boolean>?>(null) } // true = width, false = height

    val selectedNode = remember(widgetNodes, selectedWidgetUid) {
        selectedWidgetUid?.let { findNodeByUid(widgetNodes, it) }
    }

    BackHandler {
        when {
            showTempRunScreen -> showTempRunScreen = false
            selectedWidgetUid != null -> selectedWidgetUid = null
            else -> onBack()
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(DeepNavy)
                .safeDrawingPadding(),
        ) {
            // 1. Top Toolbar: Back Arrow + Project Icon + Project Name/Version + Undo / Redo / Save / More
            IdeSketchTopBar(
                project = project,
                canUndo = undoStack.isNotEmpty(),
                canRedo = redoStack.isNotEmpty(),
                onBack = onBack,
                onUndo = { handleUndo() },
                onRedo = { handleRedo() },
                onSave = {
                    repository.saveWidgetsForXmlPage(project, selectedXmlPage, widgetNodes)
                    repository.saveCodeForFlavor(project, selectedXmlPage, activeFlavor, codeText)
                },
            )

            // 2. 3-Tabs Bar (View | Logic | Emulator) with active bottom indicator line
            IdeSketchTabsBar(
                activeTab = activeTab,
                onSelectTab = { tab ->
                    selectedWidgetUid = null
                    activeTabName = tab.name
                },
            )

            // 3. Main Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                when (activeTab) {
                    IdeProjectTab.VIEW -> {
                        IdeViewDesigner(
                            project = project,
                            xmlPage = selectedXmlPage,
                            widgetNodes = widgetNodes,
                            selectedWidgetUid = selectedWidgetUid,
                            onSelectWidgetUid = { selectedWidgetUid = it },
                            onDraggingStateChange = { isDraggingWidget = it },
                            onRequestEditTextView = { editingTextViewNode = it },
                            onUpdateWidgets = { updateWidgetNodes(it) },
                        )
                    }

                    IdeProjectTab.LOGIC -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                        ) {
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
                    }

                    IdeProjectTab.EMULATOR -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                        ) {
                            IdeEmptyWorkspaceCanvas()
                        }
                    }
                }
            }

            // 4. Bottom Bar:
            // When a widget is dropped or selected in View tab (and not actively dragging),
            // show the Sketchware-style Bottom Widget Property Panel (matching video frames 1.3s-1.7s & 5.4s-6.2s).
            // Otherwise show the XML/Activity Selector Pill + Split Run Button.
            if (activeTab == IdeProjectTab.VIEW && selectedNode != null && !isDraggingWidget) {
                IdeWidgetPropertyBottomPanel(
                    node = selectedNode,
                    allWidgets = flattenAllNodes(widgetNodes),
                    onSelectOtherWidget = { selectedWidgetUid = it.uid },
                    onMoveUp = {
                        updateWidgetNodes(moveNodeInParent(widgetNodes, selectedNode.uid, direction = -1))
                    },
                    onCollapse = { selectedWidgetUid = null },
                    onDelete = {
                        updateWidgetNodes(removeNodeByUid(widgetNodes, selectedNode.uid))
                        selectedWidgetUid = null
                    },
                    onSave = {
                        repository.saveWidgetsForXmlPage(project, selectedXmlPage, widgetNodes)
                    },
                    onEditIdOrCustomAttr = { editingIdNode = selectedNode },
                    onConvertWidget = {
                        val convertedType = when (selectedNode.type) {
                            IdeWidgetType.LINEAR_H -> IdeWidgetType.LINEAR_V
                            IdeWidgetType.LINEAR_V -> IdeWidgetType.LINEAR_H
                            IdeWidgetType.SCROLL_H -> IdeWidgetType.SCROLL_V
                            IdeWidgetType.SCROLL_V -> IdeWidgetType.SCROLL_H
                            IdeWidgetType.TEXT_VIEW -> IdeWidgetType.TEXT_VIEW
                        }
                        if (convertedType != selectedNode.type) {
                            updateWidgetNodes(
                                updateNodeByUid(widgetNodes, selectedNode.uid) { it.copy(type = convertedType) },
                            )
                        } else {
                            editingTextViewNode = selectedNode
                        }
                    },
                    onEditWidth = { editingSizeNode = selectedNode to true },
                    onEditHeight = { editingSizeNode = selectedNode to false },
                    onEditText = { editingTextViewNode = selectedNode },
                    onCycleTextSize = {
                        val sizes = listOf(12, 14, 16, 18, 20, 24)
                        val nextSize = sizes[(sizes.indexOf(selectedNode.textSizeSp) + 1) % sizes.size]
                        updateWidgetNodes(
                            updateNodeByUid(widgetNodes, selectedNode.uid) { it.copy(textSizeSp = nextSize) },
                        )
                    },
                    onCyclePadding = {
                        val pads = listOf(0, 4, 8, 12, 16)
                        val nextPad = pads[(pads.indexOf(selectedNode.paddingDp) + 1) % pads.size]
                        updateWidgetNodes(
                            updateNodeByUid(widgetNodes, selectedNode.uid) { it.copy(paddingDp = nextPad) },
                        )
                    },
                    onOpenLogicEvent = {
                        selectedWidgetUid = null
                        activeTabName = IdeProjectTab.LOGIC.name
                    },
                    bottomBarPadding = bottomBarPadding,
                )
            } else {
                IdeSketchBottomBar(
                    activeTab = activeTab,
                    xmlPage = selectedXmlPage,
                    onOpenPageSelector = { showXmlBottomSheet = true },
                    onRunClick = { showTempRunScreen = true },
                    bottomBarPadding = bottomBarPadding,
                )
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
                updateWidgetNodes(updateNodeText(widgetNodes, editingNode.uid, newText))
                editingTextViewNode = null
            },
        )
    }

    val idNode = editingIdNode
    if (idNode != null) {
        EditWidgetIdDialog(
            initialId = idNode.viewId,
            widgetType = idNode.type,
            onDismiss = { editingIdNode = null },
            onConfirm = { newId ->
                updateWidgetNodes(
                    updateNodeByUid(widgetNodes, idNode.uid) { it.copy(viewId = newId) },
                )
                editingIdNode = null
            },
        )
    }

    val sizeDialogPair = editingSizeNode
    if (sizeDialogPair != null) {
        val (targetNode, isWidth) = sizeDialogPair
        EditWidgetDimensionDialog(
            viewId = targetNode.viewId,
            isWidth = isWidth,
            currentValue = if (isWidth) targetNode.layoutWidth else targetNode.layoutHeight,
            onDismiss = { editingSizeNode = null },
            onSelectValue = { chosen ->
                updateWidgetNodes(
                    updateNodeByUid(widgetNodes, targetNode.uid) {
                        if (isWidth) it.copy(layoutWidth = chosen) else it.copy(layoutHeight = chosen)
                    },
                )
                editingSizeNode = null
            },
        )
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
// 1. Top Bar & 3-Tabs Bar (Screenshot proportions, IDE Ultimate Dark Colors)
// ============================================================================

@Composable
private fun IdeSketchTopBar(
    project: WorkspaceItem,
    canUndo: Boolean,
    canRedo: Boolean,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0F24))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.workspace_back),
            tint = Ink,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack)
                .padding(5.dp),
        )

        Spacer(Modifier.width(10.dp))

        // Project Icon next to Program Name
        ProjectIconView(
            iconPreset = project.iconPreset,
            customIconPath = project.customIconPath,
            modifier = Modifier.size(34.dp),
            cornerRadius = 10.dp,
        )

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(
                text = project.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${project.versionCode} • v${project.versionName}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.5.sp,
                ),
                color = Muted,
                maxLines = 1,
            )
        }

        // Undo Icon
        TopBarVectorAction(
            enabled = canUndo,
            onClick = onUndo,
        ) { tint ->
            Canvas(Modifier.size(18.dp)) {
                val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val path = Path().apply {
                    moveTo(size.width * 0.25f, size.height * 0.42f)
                    lineTo(size.width * 0.65f, size.height * 0.42f)
                    cubicTo(
                        size.width * 0.88f, size.height * 0.42f,
                        size.width * 0.88f, size.height * 0.80f,
                        size.width * 0.62f, size.height * 0.80f,
                    )
                    lineTo(size.width * 0.35f, size.height * 0.80f)
                }
                drawPath(path, color = tint, style = stroke)
                val arrow = Path().apply {
                    moveTo(size.width * 0.42f, size.height * 0.24f)
                    lineTo(size.width * 0.22f, size.height * 0.42f)
                    lineTo(size.width * 0.42f, size.height * 0.60f)
                }
                drawPath(arrow, color = tint, style = stroke)
            }
        }

        // Redo Icon
        TopBarVectorAction(
            enabled = canRedo,
            onClick = onRedo,
        ) { tint ->
            Canvas(Modifier.size(18.dp)) {
                val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val path = Path().apply {
                    moveTo(size.width * 0.75f, size.height * 0.42f)
                    lineTo(size.width * 0.35f, size.height * 0.42f)
                    cubicTo(
                        size.width * 0.12f, size.height * 0.42f,
                        size.width * 0.12f, size.height * 0.80f,
                        size.width * 0.38f, size.height * 0.80f,
                    )
                    lineTo(size.width * 0.65f, size.height * 0.80f)
                }
                drawPath(path, color = tint, style = stroke)
                val arrow = Path().apply {
                    moveTo(size.width * 0.58f, size.height * 0.24f)
                    lineTo(size.width * 0.78f, size.height * 0.42f)
                    lineTo(size.width * 0.58f, size.height * 0.60f)
                }
                drawPath(arrow, color = tint, style = stroke)
            }
        }

        // Save Floppy Icon
        TopBarVectorAction(
            enabled = true,
            onClick = onSave,
        ) { tint ->
            Canvas(Modifier.size(18.dp)) {
                val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
                    size = Size(size.width * 0.72f, size.height * 0.72f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = stroke,
                )
                drawRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.28f, size.height * 0.14f),
                    size = Size(size.width * 0.36f, size.height * 0.24f),
                    style = stroke,
                )
                drawCircle(
                    color = tint,
                    radius = size.width * 0.12f,
                    center = Offset(size.width * 0.50f, size.height * 0.62f),
                )
            }
        }

        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = null,
            tint = Ink.copy(alpha = 0.85f),
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .clickable(onClick = onSave)
                .padding(5.dp),
        )
    }
}

@Composable
private fun TopBarVectorAction(
    enabled: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val tint = if (enabled) Ink.copy(alpha = 0.88f) else Muted.copy(alpha = 0.38f)
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        icon(tint)
    }
}

@Composable
private fun IdeSketchTabsBar(
    activeTab: IdeProjectTab,
    onSelectTab: (IdeProjectTab) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0F24)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            IdeProjectTab.entries.forEach { tab ->
                val selected = tab == activeTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(tab) }
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        ),
                        color = if (selected) Cyan else Muted,
                    )
                    Spacer(Modifier.height(9.dp))
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(2.5.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(if (selected) Cyan else Color.Transparent),
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x24FFFFFF)),
        )
    }
}

// ============================================================================
// 2. VIEW Tab: Left Compact Widgets Column + Right Rectangular White Phone Frame
//    with Sketchware-exact Drag-and-Drop (#D6D6D6 live placeholder, floating pill,
//    #C2E4E8 cyan-teal selection fill, and Bottom Property Panel)
// ============================================================================

@Composable
private fun IdeViewDesigner(
    project: WorkspaceItem,
    xmlPage: ProjectXmlPage,
    widgetNodes: List<IdeWidgetNode>,
    selectedWidgetUid: String?,
    onSelectWidgetUid: (String?) -> Unit,
    onDraggingStateChange: (Boolean) -> Unit,
    onRequestEditTextView: (IdeWidgetNode) -> Unit,
    onUpdateWidgets: (List<IdeWidgetNode>) -> Unit,
) {
    val density = LocalDensity.current
    var rootBounds by remember { mutableStateOf(Rect.Zero) }
    var phoneCanvasBounds by remember { mutableStateOf(Rect.Zero) }
    val containerBoundsMap = remember { mutableStateMapOf<String, Rect>() }
    val widgetBoundsMap = remember { mutableStateMapOf<String, Rect>() }

    var draggingType by remember { mutableStateOf<IdeWidgetType?>(null) }
    var draggingExistingNode by remember { mutableStateOf<IdeWidgetNode?>(null) }
    var dragFingerInRoot by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(draggingType) {
        onDraggingStateChange(draggingType != null)
    }

    // Active when dragging and finger has entered or is approaching the phone canvas
    val isDragOverPhone = remember(draggingType, dragFingerInRoot, phoneCanvasBounds) {
        draggingType != null &&
            !phoneCanvasBounds.isEmpty &&
            dragFingerInRoot.x >= phoneCanvasBounds.left - 28f &&
            dragFingerInRoot.x <= phoneCanvasBounds.right + 28f &&
            dragFingerInRoot.y >= phoneCanvasBounds.top - 16f &&
            dragFingerInRoot.y <= phoneCanvasBounds.bottom + 24f
    }

    // Innermost container under the user's finger (excluding any container currently being moved)
    val hoveredContainerUid: String? = remember(
        draggingType,
        draggingExistingNode,
        dragFingerInRoot,
        isDragOverPhone,
        containerBoundsMap.toMap(),
        widgetNodes,
    ) {
        if (!isDragOverPhone || draggingType == null) {
            null
        } else {
            val movingUid = draggingExistingNode?.uid
            containerBoundsMap.entries
                .filter { (uid, rect) ->
                    uid != movingUid &&
                        findNodeByUid(widgetNodes, uid)?.type?.isContainer == true &&
                        (movingUid == null || !containsDescendantUid(findNodeByUid(widgetNodes, movingUid), uid)) &&
                        !rect.isEmpty &&
                        rect.contains(dragFingerInRoot)
                }
                .minByOrNull { (_, rect) -> rect.width * rect.height }
                ?.key
        }
    }

    // Compute exact insertion index inside the target container (or root canvas)
    val dropInsertIndex: Int = remember(
        draggingType,
        draggingExistingNode,
        dragFingerInRoot,
        isDragOverPhone,
        hoveredContainerUid,
        widgetBoundsMap.toMap(),
        widgetNodes,
    ) {
        if (!isDragOverPhone || draggingType == null) {
            -1
        } else {
            val movingUid = draggingExistingNode?.uid
            if (hoveredContainerUid != null) {
                val containerNode = findNodeByUid(widgetNodes, hoveredContainerUid)
                val children = containerNode?.children?.filter { it.uid != movingUid }.orEmpty()
                val isHorizontal = containerNode?.type == IdeWidgetType.LINEAR_H ||
                    containerNode?.type == IdeWidgetType.SCROLL_H
                computeInsertIndex(children, widgetBoundsMap, dragFingerInRoot, isHorizontal)
            } else {
                val rootChildren = widgetNodes.filter { it.uid != movingUid }
                computeInsertIndex(rootChildren, widgetBoundsMap, dragFingerInRoot, isHorizontal = false)
            }
        }
    }

    fun nextViewIdFor(type: IdeWidgetType): String {
        val existingIds = flattenAllNodes(widgetNodes).map { it.viewId }.toSet()
        var index = countNodesOfPrefix(widgetNodes, type.idPrefix) + 1
        while ("${type.idPrefix}$index" in existingIds) {
            index++
        }
        return "${type.idPrefix}$index"
    }

    fun commitDrop(type: IdeWidgetType) {
        val existingMoving = draggingExistingNode
        if (!isDragOverPhone) {
            draggingType = null
            draggingExistingNode = null
            return
        }

        val nodeToInsert = existingMoving ?: IdeWidgetNode(
            viewId = nextViewIdFor(type),
            type = type,
            text = if (type == IdeWidgetType.TEXT_VIEW) "TextView" else type.displayName,
        )

        val baseTree = if (existingMoving != null) {
            removeNodeByUid(widgetNodes, existingMoving.uid)
        } else {
            widgetNodes
        }

        val targetContainer = hoveredContainerUid
        val updated = if (targetContainer != null) {
            insertChildIntoContainerAt(
                nodes = baseTree,
                containerUid = targetContainer,
                newChild = nodeToInsert,
                insertIndex = dropInsertIndex,
            )
        } else {
            val safeIndex = if (dropInsertIndex < 0) baseTree.size else dropInsertIndex.coerceIn(0, baseTree.size)
            baseTree.toMutableList().apply { add(safeIndex, nodeToInsert) }
        }

        onUpdateWidgets(updated)
        onSelectWidgetUid(nodeToInsert.uid)
        draggingType = null
        draggingExistingNode = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootBounds = coords.boundsInRoot()
            },
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Left Palette Column: exact width & spacing as video (~120.dp)
            WidgetsPaletteColumn(
                onStartDrag = { type, startRootPos ->
                    onSelectWidgetUid(null)
                    draggingExistingNode = null
                    draggingType = type
                    dragFingerInRoot = startRootPos
                },
                onUpdateDragRootPos = { currentRootPos ->
                    dragFingerInRoot = currentRootPos
                },
                onEndDrag = {
                    val droppedType = draggingType
                    if (droppedType != null) {
                        commitDrop(droppedType)
                    }
                    draggingType = null
                    draggingExistingNode = null
                },
                onCancelDrag = {
                    draggingType = null
                    draggingExistingNode = null
                },
                onPaletteTouchOrScroll = {
                    if (selectedWidgetUid != null) {
                        onSelectWidgetUid(null)
                    }
                },
                onTapAdd = { type ->
                    val selectedNode = selectedWidgetUid?.let { findNodeByUid(widgetNodes, it) }
                    val targetContainer = selectedNode?.takeIf { it.type.isContainer }?.uid
                    val newNode = IdeWidgetNode(
                        viewId = nextViewIdFor(type),
                        type = type,
                        text = if (type == IdeWidgetType.TEXT_VIEW) "TextView" else type.displayName,
                    )
                    val updated = if (targetContainer != null) {
                        insertChildIntoContainer(widgetNodes, targetContainer, newNode)
                    } else {
                        widgetNodes + newNode
                    }
                    onUpdateWidgets(updated)
                    onSelectWidgetUid(newNode.uid)
                },
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
            )

            // Right Phone Frame Area: stays in fixed position (no top bar pushing it down)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 8.dp, end = 14.dp, top = 12.dp, bottom = 14.dp),
            ) {
                PhoneFrameWhiteCanvas(
                    project = project,
                    xmlPage = xmlPage,
                    widgetNodes = widgetNodes,
                    selectedWidgetUid = selectedWidgetUid,
                    draggingType = draggingType,
                    draggingExistingUid = draggingExistingNode?.uid,
                    isDragOverPhone = isDragOverPhone,
                    hoveredContainerUid = hoveredContainerUid,
                    dropInsertIndex = dropInsertIndex,
                    onSelectWidget = { node -> onSelectWidgetUid(node.uid) },
                    onDoubleTapTextView = { node -> onRequestEditTextView(node) },
                    onStartMoveExistingWidget = { node, startRootPos ->
                        onSelectWidgetUid(null)
                        draggingExistingNode = node
                        draggingType = node.type
                        dragFingerInRoot = startRootPos
                    },
                    onUpdateMoveDragPos = { currentRootPos ->
                        dragFingerInRoot = currentRootPos
                    },
                    onEndMoveExistingWidget = {
                        val droppedType = draggingType
                        if (droppedType != null) {
                            commitDrop(droppedType)
                        }
                        draggingType = null
                        draggingExistingNode = null
                    },
                    onCancelMoveExistingWidget = {
                        draggingType = null
                        draggingExistingNode = null
                    },
                    onPhoneCanvasPositioned = { rect -> phoneCanvasBounds = rect },
                    onContainerPositioned = { uid, rect -> containerBoundsMap[uid] = rect },
                    onWidgetPositioned = { uid, rect -> widgetBoundsMap[uid] = rect },
                    onTapEmptyWhiteArea = { onSelectWidgetUid(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )
            }
        }

        // Semi-transparent Floating Drag Pill following the user's finger (exact match to video)
        val activeDrag = draggingType
        if (activeDrag != null && !rootBounds.isEmpty) {
            val offsetX = with(density) {
                (dragFingerInRoot.x - rootBounds.left - 78.dp.toPx()).roundToInt()
            }
            val offsetY = with(density) {
                (dragFingerInRoot.y - rootBounds.top - 14.dp.toPx()).roundToInt()
            }
            Row(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .width(108.dp)
                    .height(28.dp)
                    .graphicsLayer(alpha = 0.62f)
                    .shadow(4.dp, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF1A2448))
                    .border(1.dp, Cyan.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SketchWidgetIcon(type = activeDrag, tint = Ink)
                Text(
                    text = sketchWidgetLabel(activeDrag),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun WidgetsPaletteColumn(
    onStartDrag: (IdeWidgetType, Offset) -> Unit,
    onUpdateDragRootPos: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onCancelDrag: () -> Unit,
    onPaletteTouchOrScroll: () -> Unit,
    onTapAdd: (IdeWidgetType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            onPaletteTouchOrScroll()
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 12.dp),
    ) {
        // Top mini-switcher strip matching screenshot (Palette active tab + Bookmark icon)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(bottomEnd = 8.dp, bottomStart = 4.dp))
                    .background(Color(0xFF1E2952))
                    .border(
                        width = 1.dp,
                        color = Cyan.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(bottomEnd = 8.dp, bottomStart = 4.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(14.dp)) {
                    val stroke = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(Cyan, Offset(2f, 3f), Offset(size.width - 2f, 3f), strokeWidth = stroke.width)
                    drawRoundRect(
                        color = Cyan,
                        topLeft = Offset(3f, size.height * 0.36f),
                        size = Size(size.width - 6f, size.height * 0.50f),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                        style = stroke,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(13.dp)) {
                    val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    val path = Path().apply {
                        moveTo(size.width * 0.22f, size.height * 0.12f)
                        lineTo(size.width * 0.78f, size.height * 0.12f)
                        lineTo(size.width * 0.78f, size.height * 0.88f)
                        lineTo(size.width * 0.50f, size.height * 0.68f)
                        lineTo(size.width * 0.22f, size.height * 0.88f)
                        close()
                    }
                    drawPath(path, color = Muted, style = stroke)
                }
            }
        }

        // Section 1: Layouts ("قسم layout")
        Text(
            text = "Layouts",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = Muted,
            modifier = Modifier.padding(start = 2.dp, top = 4.dp, bottom = 6.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            listOf(
                IdeWidgetType.LINEAR_H,
                IdeWidgetType.LINEAR_V,
                IdeWidgetType.SCROLL_H,
                IdeWidgetType.SCROLL_V,
            ).forEach { widgetType ->
                DraggableWidgetPaletteItem(
                    type = widgetType,
                    onStartDrag = onStartDrag,
                    onUpdateDragRootPos = onUpdateDragRootPos,
                    onEndDrag = onEndDrag,
                    onCancelDrag = onCancelDrag,
                    onTap = { onTapAdd(widgetType) },
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // Section 2: Widgets / Text ("قسم text")
        Text(
            text = "Widgets",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = Muted,
            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp),
        )

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            DraggableWidgetPaletteItem(
                type = IdeWidgetType.TEXT_VIEW,
                onStartDrag = onStartDrag,
                onUpdateDragRootPos = onUpdateDragRootPos,
                onEndDrag = onEndDrag,
                onCancelDrag = onCancelDrag,
                onTap = { onTapAdd(IdeWidgetType.TEXT_VIEW) },
            )
        }
    }
}

private fun sketchWidgetLabel(type: IdeWidgetType): String = when (type) {
    IdeWidgetType.LINEAR_H -> "Linear(H)"
    IdeWidgetType.LINEAR_V -> "Linear(V)"
    IdeWidgetType.SCROLL_H -> "Scroll(H)"
    IdeWidgetType.SCROLL_V -> "Scroll(V)"
    IdeWidgetType.TEXT_VIEW -> "TextView"
}

@Composable
private fun DraggableWidgetPaletteItem(
    type: IdeWidgetType,
    onStartDrag: (IdeWidgetType, Offset) -> Unit,
    onUpdateDragRootPos: (Offset) -> Unit,
    onEndDrag: () -> Unit,
    onCancelDrag: () -> Unit,
    onTap: () -> Unit,
) {
    var itemBoundsInRoot by remember { mutableStateOf(Rect.Zero) }
    val shape = RoundedCornerShape(6.dp)

    // Supports BOTH hold-to-drag (~140ms) AND immediate horizontal drag toward the phone canvas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .onGloballyPositioned { coords ->
                itemBoundsInRoot = coords.boundsInRoot()
            }
            .clip(shape)
            .background(Color(0xFF161E3C))
            .border(1.dp, Color(0x2EFFFFFF), shape)
            .pointerInput(type) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val downPos = down.position
                    val downTime = System.currentTimeMillis()
                    var dragging = false
                    var latestPos = downPos

                    // Phase 1: Wait until hold threshold (140ms) or finger movement
                    while (!dragging) {
                        val event = withTimeoutOrNull(140L) {
                            awaitPointerEvent(PointerEventPass.Main)
                        }
                        if (event == null) {
                            // Finger held for 140ms -> start drag immediately like Sketchware!
                            dragging = true
                            val rootPos = Offset(
                                x = itemBoundsInRoot.left + latestPos.x,
                                y = itemBoundsInRoot.top + latestPos.y,
                            )
                            onStartDrag(type, rootPos)
                            break
                        }
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change == null || !change.pressed) {
                            // Released quickly -> treat as a tap if within tap slop
                            if (System.currentTimeMillis() - downTime < 220L &&
                                (latestPos - downPos).getDistance() < viewConfiguration.touchSlop
                            ) {
                                onTap()
                            }
                            return@awaitEachGesture
                        }
                        latestPos = change.position
                        val dx = latestPos.x - downPos.x
                        val dy = latestPos.y - downPos.y
                        if (abs(dx) > 6f && abs(dx) >= abs(dy) * 0.65f) {
                            // Immediate horizontal drag toward the canvas!
                            change.consume()
                            dragging = true
                            val rootPos = Offset(
                                x = itemBoundsInRoot.left + latestPos.x,
                                y = itemBoundsInRoot.top + latestPos.y,
                            )
                            onStartDrag(type, rootPos)
                        } else if (abs(dy) > viewConfiguration.touchSlop && abs(dy) > abs(dx) * 1.4f) {
                            // Vertical scroll of the palette -> let parent verticalScroll handle it
                            return@awaitEachGesture
                        } else if (System.currentTimeMillis() - downTime >= 140L) {
                            change.consume()
                            dragging = true
                            val rootPos = Offset(
                                x = itemBoundsInRoot.left + latestPos.x,
                                y = itemBoundsInRoot.top + latestPos.y,
                            )
                            onStartDrag(type, rootPos)
                        }
                    }

                    // Phase 2: Track drag on Initial pass so parent verticalScroll never steals the gesture
                    while (dragging) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                        if (change == null) {
                            onCancelDrag()
                            break
                        }
                        if (!change.pressed) {
                            change.consume()
                            onEndDrag()
                            break
                        }
                        change.consume()
                        val currentRootPos = Offset(
                            x = itemBoundsInRoot.left + change.position.x,
                            y = itemBoundsInRoot.top + change.position.y,
                        )
                        onUpdateDragRootPos(currentRootPos)
                    }
                }
            }
            .padding(horizontal = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        SketchWidgetIcon(type = type, tint = Ink.copy(alpha = 0.88f))
        Text(
            text = sketchWidgetLabel(type),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SketchWidgetIcon(
    type: IdeWidgetType,
    tint: Color,
) {
    Canvas(Modifier.size(14.dp)) {
        val stroke = Stroke(width = 1.4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (type) {
            IdeWidgetType.LINEAR_H -> {
                // 3 vertical columns inside a box [|||]
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, 2f),
                    size = Size(size.width - 2f, size.height - 4f),
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
                    style = stroke,
                )
                val third = size.width / 3f
                drawLine(tint, Offset(third, 2f), Offset(third, size.height - 2f), strokeWidth = stroke.width)
                drawLine(tint, Offset(third * 2f, 2f), Offset(third * 2f, size.height - 2f), strokeWidth = stroke.width)
            }

            IdeWidgetType.LINEAR_V -> {
                // 3 horizontal rows inside a box [≡]
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(2f, 1f),
                    size = Size(size.width - 4f, size.height - 2f),
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()),
                    style = stroke,
                )
                val third = size.height / 3f
                drawLine(tint, Offset(2f, third), Offset(size.width - 2f, third), strokeWidth = stroke.width)
                drawLine(tint, Offset(2f, third * 2f), Offset(size.width - 2f, third * 2f), strokeWidth = stroke.width)
            }

            IdeWidgetType.SCROLL_H -> {
                // Horizontal double arrow + hand indicator
                val y = size.height * 0.35f
                drawLine(tint, Offset(1.5f, y), Offset(size.width - 1.5f, y), strokeWidth = stroke.width)
                drawLine(tint, Offset(1.5f, y), Offset(4.5f, y - 3f), strokeWidth = stroke.width)
                drawLine(tint, Offset(1.5f, y), Offset(4.5f, y + 3f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width - 1.5f, y), Offset(size.width - 4.5f, y - 3f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width - 1.5f, y), Offset(size.width - 4.5f, y + 3f), strokeWidth = stroke.width)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.28f, size.height * 0.55f),
                    size = Size(size.width * 0.44f, size.height * 0.36f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
            }

            IdeWidgetType.SCROLL_V -> {
                // Vertical double arrow + hand indicator
                val x = size.width * 0.32f
                drawLine(tint, Offset(x, 1.5f), Offset(x, size.height - 1.5f), strokeWidth = stroke.width)
                drawLine(tint, Offset(x, 1.5f), Offset(x - 3f, 4.5f), strokeWidth = stroke.width)
                drawLine(tint, Offset(x, 1.5f), Offset(x + 3f, 4.5f), strokeWidth = stroke.width)
                drawLine(tint, Offset(x, size.height - 1.5f), Offset(x - 3f, size.height - 4.5f), strokeWidth = stroke.width)
                drawLine(tint, Offset(x, size.height - 1.5f), Offset(x + 3f, size.height - 4.5f), strokeWidth = stroke.width)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.52f, size.height * 0.35f),
                    size = Size(size.width * 0.36f, size.height * 0.48f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
            }

            IdeWidgetType.TEXT_VIEW -> {
                // "TT" icon matching screenshot
                val sw = 1.5.dp.toPx()
                // Small T on left
                drawLine(tint, Offset(1f, size.height * 0.36f), Offset(size.width * 0.46f, size.height * 0.36f), strokeWidth = sw)
                drawLine(tint, Offset(size.width * 0.23f, size.height * 0.36f), Offset(size.width * 0.23f, size.height * 0.86f), strokeWidth = sw)
                // Tall T on right
                drawLine(tint, Offset(size.width * 0.44f, size.height * 0.16f), Offset(size.width - 1f, size.height * 0.16f), strokeWidth = sw)
                drawLine(tint, Offset(size.width * 0.72f, size.height * 0.16f), Offset(size.width * 0.72f, size.height * 0.86f), strokeWidth = sw)
            }
        }
    }
}

/**
 * Solid `#D6D6D6` gray drop-target placeholder bar/box shown inside the Phone Frame
 * while dragging a widget (matches video frames 1.28s-1.45s & 3.52s-4.10s).
 */
@Composable
private fun SketchDropPlaceholder(
    type: IdeWidgetType,
    inHorizontalParent: Boolean,
) {
    val placeholderModifier = if (type.isContainer && !inHorizontalParent) {
        Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(Color(0xFFD6D6D6))
    } else {
        Modifier
            .width(44.dp)
            .height(22.dp)
            .background(Color(0xFFD6D6D6))
    }
    Box(modifier = placeholderModifier)
}

/**
 * Clean rectangular Phone Screen Frame matching the video's exact proportions & behavior:
 * - Top Status Bar (`xmlPage.xmlFileName` on left, signal bars + battery + `1:37` on right)
 * - Toolbar strip (`Toolbar`) below status bar
 * - Pure WHITE (`#FFFFFF`) interior screen below Toolbar with edge-to-edge layout rendering
 *   and live `#D6D6D6` drop placeholder during drag.
 */
@Composable
private fun PhoneFrameWhiteCanvas(
    project: WorkspaceItem,
    xmlPage: ProjectXmlPage,
    widgetNodes: List<IdeWidgetNode>,
    selectedWidgetUid: String?,
    draggingType: IdeWidgetType?,
    draggingExistingUid: String?,
    isDragOverPhone: Boolean,
    hoveredContainerUid: String?,
    dropInsertIndex: Int,
    onSelectWidget: (IdeWidgetNode) -> Unit,
    onDoubleTapTextView: (IdeWidgetNode) -> Unit,
    onStartMoveExistingWidget: (IdeWidgetNode, Offset) -> Unit,
    onUpdateMoveDragPos: (Offset) -> Unit,
    onEndMoveExistingWidget: () -> Unit,
    onCancelMoveExistingWidget: () -> Unit,
    onPhoneCanvasPositioned: (Rect) -> Unit,
    onContainerPositioned: (String, Rect) -> Unit,
    onWidgetPositioned: (String, Rect) -> Unit,
    onTapEmptyWhiteArea: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val frameShape = RoundedCornerShape(2.dp)
    Column(
        modifier = modifier
            .shadow(6.dp, frameShape)
            .clip(frameShape)
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0x33FFFFFF),
                shape = frameShape,
            )
            .onGloballyPositioned { coords ->
                onPhoneCanvasPositioned(coords.boundsInRoot())
            }
            .clickable(onClick = onTapEmptyWhiteArea),
    ) {
        // 1. Phone Status Bar (matches screenshot: xml filename on left, signal + battery + time on right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF283566))
                .padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = xmlPage.xmlFileName,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Normal,
                ),
                color = Color.White.copy(alpha = 0.92f),
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                // Signal bars icon
                Canvas(Modifier.size(width = 13.dp, height = 10.dp)) {
                    val barW = size.width / 5.5f
                    for (i in 0..3) {
                        val barH = size.height * (0.32f + i * 0.22f)
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(i * (barW + 1.5f), size.height - barH),
                            size = Size(barW, barH),
                        )
                    }
                }
                // Battery icon
                Canvas(Modifier.size(width = 8.dp, height = 10.dp)) {
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(size.width * 0.28f, 0f),
                        size = Size(size.width * 0.44f, size.height * 0.16f),
                    )
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(0f, size.height * 0.16f),
                        size = Size(size.width, size.height * 0.84f),
                    )
                }
                Text(
                    text = "1:37",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Color.White,
                )
            }
        }

        // 2. App Toolbar inside Phone Frame ("Toolbar" strip just like screenshot)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF33437A))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = "Toolbar",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
            )
        }

        // 3. Pure WHITE Screen Interior ("جوه الفريم ابيض")
        val visibleRootNodes = remember(widgetNodes, draggingExistingUid) {
            widgetNodes.filter { it.uid != draggingExistingUid }
        }
        val showRootPlaceholder = isDragOverPhone && draggingType != null && hoveredContainerUid == null
        val safeRootInsertIndex = if (showRootPlaceholder) {
            dropInsertIndex.coerceIn(0, visibleRootNodes.size)
        } else {
            -1
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
        ) {
            visibleRootNodes.forEachIndexed { index, node ->
                if (showRootPlaceholder && index == safeRootInsertIndex && draggingType != null) {
                    SketchDropPlaceholder(
                        type = draggingType,
                        inHorizontalParent = false,
                    )
                }
                RenderWidgetNodeOnPhone(
                    node = node,
                    selectedWidgetUid = selectedWidgetUid,
                    draggingType = draggingType,
                    draggingExistingUid = draggingExistingUid,
                    hoveredContainerUid = hoveredContainerUid,
                    dropInsertIndex = dropInsertIndex,
                    onSelectWidget = onSelectWidget,
                    onDoubleTapTextView = onDoubleTapTextView,
                    onStartMoveExistingWidget = onStartMoveExistingWidget,
                    onUpdateMoveDragPos = onUpdateMoveDragPos,
                    onEndMoveExistingWidget = onEndMoveExistingWidget,
                    onCancelMoveExistingWidget = onCancelMoveExistingWidget,
                    onContainerPositioned = onContainerPositioned,
                    onWidgetPositioned = onWidgetPositioned,
                )
            }
            if (showRootPlaceholder && safeRootInsertIndex == visibleRootNodes.size && draggingType != null) {
                SketchDropPlaceholder(
                    type = draggingType,
                    inHorizontalParent = false,
                )
            }
        }
    }
}

@Composable
private fun RenderWidgetNodeOnPhone(
    node: IdeWidgetNode,
    selectedWidgetUid: String?,
    draggingType: IdeWidgetType?,
    draggingExistingUid: String?,
    hoveredContainerUid: String?,
    dropInsertIndex: Int,
    onSelectWidget: (IdeWidgetNode) -> Unit,
    onDoubleTapTextView: (IdeWidgetNode) -> Unit,
    onStartMoveExistingWidget: (IdeWidgetNode, Offset) -> Unit,
    onUpdateMoveDragPos: (Offset) -> Unit,
    onEndMoveExistingWidget: () -> Unit,
    onCancelMoveExistingWidget: () -> Unit,
    onContainerPositioned: (String, Rect) -> Unit,
    onWidgetPositioned: (String, Rect) -> Unit,
) {
    val isSelected = node.uid == selectedWidgetUid
    val isHoveredDropTarget = node.uid == hoveredContainerUid
    var nodeBoundsInRoot by remember(node.uid) { mutableStateOf(Rect.Zero) }

    // Sketchware's signature light cyan-teal selection fill (#C2E4E8) and #757575 outline border
    val selectedFill = Color(0xFFC2E4E8)
    val layoutOutlineColor = Color(0xFF757575)

    val visibleChildren = remember(node.children, draggingExistingUid) {
        node.children.filter { it.uid != draggingExistingUid }
    }
    val safeChildInsertIndex = if (isHoveredDropTarget && draggingType != null) {
        dropInsertIndex.coerceIn(0, visibleChildren.size)
    } else {
        -1
    }

    val widthModifier = if (node.layoutWidth == "match_parent") {
        Modifier.fillMaxWidth()
    } else {
        Modifier.wrapContentWidth()
    }
    val visualPad = (node.paddingDp / 2).coerceIn(2, 12).dp

    when (node.type) {
        IdeWidgetType.LINEAR_H, IdeWidgetType.SCROLL_H -> {
            val isEmpty = visibleChildren.isEmpty() && !isHoveredDropTarget
            Row(
                modifier = widthModifier
                    .then(
                        if (isEmpty) {
                            Modifier.height(28.dp)
                        } else {
                            Modifier.heightIn(min = 28.dp)
                        },
                    )
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        onContainerPositioned(node.uid, rect)
                        onWidgetPositioned(node.uid, rect)
                    }
                    .background(if (isSelected) selectedFill else Color.Transparent)
                    .border(0.8.dp, layoutOutlineColor, RectangleShape)
                    .clickable { onSelectWidget(node) }
                    .pointerInput(node.uid) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { localOffset ->
                                onStartMoveExistingWidget(
                                    node,
                                    Offset(
                                        x = nodeBoundsInRoot.left + localOffset.x,
                                        y = nodeBoundsInRoot.top + localOffset.y,
                                    ),
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                onUpdateMoveDragPos(
                                    Offset(
                                        x = nodeBoundsInRoot.left + change.position.x,
                                        y = nodeBoundsInRoot.top + change.position.y,
                                    ),
                                )
                            },
                            onDragEnd = onEndMoveExistingWidget,
                            onDragCancel = onCancelMoveExistingWidget,
                        )
                    }
                    .padding(if (isEmpty) 0.dp else visualPad),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
            ) {
                visibleChildren.forEachIndexed { index, child ->
                    if (isHoveredDropTarget && index == safeChildInsertIndex && draggingType != null) {
                        SketchDropPlaceholder(
                            type = draggingType,
                            inHorizontalParent = true,
                        )
                    }
                    RenderWidgetNodeOnPhone(
                        node = child,
                        selectedWidgetUid = selectedWidgetUid,
                        draggingType = draggingType,
                        draggingExistingUid = draggingExistingUid,
                        hoveredContainerUid = hoveredContainerUid,
                        dropInsertIndex = dropInsertIndex,
                        onSelectWidget = onSelectWidget,
                        onDoubleTapTextView = onDoubleTapTextView,
                        onStartMoveExistingWidget = onStartMoveExistingWidget,
                        onUpdateMoveDragPos = onUpdateMoveDragPos,
                        onEndMoveExistingWidget = onEndMoveExistingWidget,
                        onCancelMoveExistingWidget = onCancelMoveExistingWidget,
                        onContainerPositioned = onContainerPositioned,
                        onWidgetPositioned = onWidgetPositioned,
                    )
                }
                if (isHoveredDropTarget && safeChildInsertIndex == visibleChildren.size && draggingType != null) {
                    SketchDropPlaceholder(
                        type = draggingType,
                        inHorizontalParent = true,
                    )
                }
            }
        }

        IdeWidgetType.LINEAR_V, IdeWidgetType.SCROLL_V -> {
            val isEmpty = visibleChildren.isEmpty() && !isHoveredDropTarget
            Column(
                modifier = widthModifier
                    .then(
                        if (isEmpty) {
                            Modifier.height(28.dp)
                        } else {
                            Modifier.heightIn(min = 28.dp)
                        },
                    )
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        onContainerPositioned(node.uid, rect)
                        onWidgetPositioned(node.uid, rect)
                    }
                    .background(if (isSelected) selectedFill else Color.Transparent)
                    .border(0.8.dp, layoutOutlineColor, RectangleShape)
                    .clickable { onSelectWidget(node) }
                    .pointerInput(node.uid) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { localOffset ->
                                onStartMoveExistingWidget(
                                    node,
                                    Offset(
                                        x = nodeBoundsInRoot.left + localOffset.x,
                                        y = nodeBoundsInRoot.top + localOffset.y,
                                    ),
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                onUpdateMoveDragPos(
                                    Offset(
                                        x = nodeBoundsInRoot.left + change.position.x,
                                        y = nodeBoundsInRoot.top + change.position.y,
                                    ),
                                )
                            },
                            onDragEnd = onEndMoveExistingWidget,
                            onDragCancel = onCancelMoveExistingWidget,
                        )
                    }
                    .padding(if (isEmpty) 0.dp else visualPad),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                visibleChildren.forEachIndexed { index, child ->
                    if (isHoveredDropTarget && index == safeChildInsertIndex && draggingType != null) {
                        SketchDropPlaceholder(
                            type = draggingType,
                            inHorizontalParent = false,
                        )
                    }
                    RenderWidgetNodeOnPhone(
                        node = child,
                        selectedWidgetUid = selectedWidgetUid,
                        draggingType = draggingType,
                        draggingExistingUid = draggingExistingUid,
                        hoveredContainerUid = hoveredContainerUid,
                        dropInsertIndex = dropInsertIndex,
                        onSelectWidget = onSelectWidget,
                        onDoubleTapTextView = onDoubleTapTextView,
                        onStartMoveExistingWidget = onStartMoveExistingWidget,
                        onUpdateMoveDragPos = onUpdateMoveDragPos,
                        onEndMoveExistingWidget = onEndMoveExistingWidget,
                        onCancelMoveExistingWidget = onCancelMoveExistingWidget,
                        onContainerPositioned = onContainerPositioned,
                        onWidgetPositioned = onWidgetPositioned,
                    )
                }
                if (isHoveredDropTarget && safeChildInsertIndex == visibleChildren.size && draggingType != null) {
                    SketchDropPlaceholder(
                        type = draggingType,
                        inHorizontalParent = false,
                    )
                }
            }
        }

        IdeWidgetType.TEXT_VIEW -> {
            Box(
                modifier = widthModifier
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        onWidgetPositioned(node.uid, rect)
                    }
                    .background(if (isSelected) selectedFill else Color.Transparent)
                    .clickable {
                        if (isSelected) {
                            onDoubleTapTextView(node)
                        } else {
                            onSelectWidget(node)
                        }
                    }
                    .pointerInput(node.uid) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { localOffset ->
                                onStartMoveExistingWidget(
                                    node,
                                    Offset(
                                        x = nodeBoundsInRoot.left + localOffset.x,
                                        y = nodeBoundsInRoot.top + localOffset.y,
                                    ),
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                onUpdateMoveDragPos(
                                    Offset(
                                        x = nodeBoundsInRoot.left + change.position.x,
                                        y = nodeBoundsInRoot.top + change.position.y,
                                    ),
                                )
                            },
                            onDragEnd = onEndMoveExistingWidget,
                            onDragCancel = onCancelMoveExistingWidget,
                        )
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                Text(
                    text = node.text,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = (node.textSizeSp - 1).coerceIn(10, 22).sp,
                        fontWeight = FontWeight.Normal,
                    ),
                    color = Color(0xFF222222),
                )
            }
        }
    }
}

// ============================================================================
// 2B. Bottom Widget Property Panel (Slides up when a widget is dropped/selected
//     matching video frames 1.3s-1.7s & 5.4s-6.2s)
// ============================================================================

private enum class WidgetPropTab(val label: String) {
    BASIC("Basic"),
    RECENT("Recent"),
    EVENT("Event"),
}

@Composable
private fun IdeWidgetPropertyBottomPanel(
    node: IdeWidgetNode,
    allWidgets: List<IdeWidgetNode>,
    onSelectOtherWidget: (IdeWidgetNode) -> Unit,
    onMoveUp: () -> Unit,
    onCollapse: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onEditIdOrCustomAttr: () -> Unit,
    onConvertWidget: () -> Unit,
    onEditWidth: () -> Unit,
    onEditHeight: () -> Unit,
    onEditText: () -> Unit,
    onCycleTextSize: () -> Unit,
    onCyclePadding: () -> Unit,
    onOpenLogicEvent: () -> Unit,
    bottomBarPadding: Dp = 0.dp,
) {
    var activePropTab by remember { mutableStateOf(WidgetPropTab.BASIC) }
    var showWidgetPickerMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D142E))
            .border(width = 1.dp, color = Color(0x28FFFFFF))
            .navigationBarsPadding()
            .padding(bottom = 8.dp + bottomBarPadding),
    ) {
        // 1. Header Row: [Icon] viewId ▼   ...   ^   v   Trash   Save
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        if (allWidgets.size > 1) {
                            showWidgetPickerMenu = !showWidgetPickerMenu
                        } else {
                            onEditIdOrCustomAttr()
                        }
                    }
                    .padding(vertical = 4.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SketchWidgetIcon(type = node.type, tint = Ink)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = node.viewId,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = Ink,
                )
                Spacer(Modifier.width(24.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier.size(20.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Up chevron (Move widget up)
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onMoveUp)
                        .padding(2.dp),
                )

                // Down chevron (Collapse bottom property sheet)
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onCollapse)
                        .padding(2.dp),
                )

                // Trash icon (Delete widget)
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Ink,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onDelete)
                        .padding(2.dp),
                )

                // Floppy Save icon
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onSave),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.size(18.dp)) {
                        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        drawRoundRect(
                            color = Ink,
                            topLeft = Offset(size.width * 0.12f, size.height * 0.12f),
                            size = Size(size.width * 0.76f, size.height * 0.76f),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                            style = stroke,
                        )
                        drawRect(
                            color = Ink,
                            topLeft = Offset(size.width * 0.28f, size.height * 0.12f),
                            size = Size(size.width * 0.36f, size.height * 0.24f),
                            style = stroke,
                        )
                        drawCircle(
                            color = Ink,
                            radius = size.width * 0.12f,
                            center = Offset(size.width * 0.50f, size.height * 0.62f),
                        )
                    }
                }
            }
        }

        if (showWidgetPickerMenu && allWidgets.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                allWidgets.forEach { item ->
                    val isCurrent = item.uid == node.uid
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (isCurrent) Cyan.copy(alpha = 0.22f) else Color(0xFF161E3C))
                            .border(
                                1.dp,
                                if (isCurrent) Cyan else Color(0x28FFFFFF),
                                RoundedCornerShape(50),
                            )
                            .clickable {
                                onSelectOtherWidget(item)
                                showWidgetPickerMenu = false
                            }
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        SketchWidgetIcon(type = item.type, tint = if (isCurrent) Cyan else Ink)
                        Text(
                            text = item.viewId,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = if (isCurrent) Cyan else Ink,
                        )
                    }
                }
            }
        }

        // 2. Category Pills Row: Basic | Recent | Event
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WidgetPropTab.entries.forEach { tab ->
                val selected = tab == activePropTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            if (selected) Color(0xFF213163) else Color(0xFF172042),
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) Cyan.copy(alpha = 0.55f) else Color(0x22FFFFFF),
                            shape = RoundedCornerShape(percent = 50),
                        )
                        .clickable { activePropTab = tab }
                        .padding(horizontal = 15.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.5.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        ),
                        color = if (selected) Cyan else Ink.copy(alpha = 0.85f),
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // 3. Horizontal Scrollable Row of Rounded Property Cards (matches video: Custom attributes, Convert, Width, See All, etc.)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (activePropTab == WidgetPropTab.EVENT) {
                SketchPropertySquareCard(
                    label = "onClick",
                    highlighted = false,
                    onClick = onOpenLogicEvent,
                ) { tint ->
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                // Card 1: Custom attributes (< >)
                SketchPropertySquareCard(
                    label = "Custom\nattributes",
                    highlighted = false,
                    onClick = onEditIdOrCustomAttr,
                ) { tint ->
                    Canvas(Modifier.size(18.dp)) {
                        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        val left = Path().apply {
                            moveTo(size.width * 0.36f, size.height * 0.25f)
                            lineTo(size.width * 0.14f, size.height * 0.50f)
                            lineTo(size.width * 0.36f, size.height * 0.75f)
                        }
                        val right = Path().apply {
                            moveTo(size.width * 0.64f, size.height * 0.25f)
                            lineTo(size.width * 0.86f, size.height * 0.50f)
                            lineTo(size.width * 0.64f, size.height * 0.75f)
                        }
                        drawPath(left, tint, style = stroke)
                        drawPath(right, tint, style = stroke)
                    }
                }

                // Card 2: Convert
                SketchPropertySquareCard(
                    label = "Convert",
                    highlighted = false,
                    onClick = onConvertWidget,
                ) { tint ->
                    Canvas(Modifier.size(18.dp)) {
                        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        drawLine(tint, Offset(size.width * 0.18f, size.height * 0.34f), Offset(size.width * 0.82f, size.height * 0.34f), stroke.width)
                        drawLine(tint, Offset(size.width * 0.64f, size.height * 0.18f), Offset(size.width * 0.82f, size.height * 0.34f), stroke.width)
                        drawLine(tint, Offset(size.width * 0.64f, size.height * 0.50f), Offset(size.width * 0.82f, size.height * 0.34f), stroke.width)

                        drawLine(tint, Offset(size.width * 0.82f, size.height * 0.68f), Offset(size.width * 0.18f, size.height * 0.68f), stroke.width)
                        drawLine(tint, Offset(size.width * 0.36f, size.height * 0.52f), Offset(size.width * 0.18f, size.height * 0.68f), stroke.width)
                        drawLine(tint, Offset(size.width * 0.36f, size.height * 0.84f), Offset(size.width * 0.18f, size.height * 0.68f), stroke.width)
                    }
                }

                // Card 3: Width ([<->])
                SketchPropertySquareCard(
                    label = "Width",
                    highlighted = false,
                    onClick = onEditWidth,
                ) { tint ->
                    Canvas(Modifier.size(18.dp)) {
                        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        drawRoundRect(
                            color = tint,
                            topLeft = Offset(1.5f, 2.5f),
                            size = Size(size.width - 3f, size.height - 5f),
                            cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                            style = stroke,
                        )
                        val cy = size.height * 0.50f
                        drawLine(tint, Offset(size.width * 0.24f, cy), Offset(size.width * 0.42f, cy - 3f), stroke.width)
                        drawLine(tint, Offset(size.width * 0.24f, cy), Offset(size.width * 0.42f, cy + 3f), stroke.width)
                        drawLine(tint, Offset(size.width * 0.76f, cy), Offset(size.width * 0.58f, cy - 3f), stroke.width)
                        drawLine(tint, Offset(size.width * 0.76f, cy), Offset(size.width * 0.58f, cy + 3f), stroke.width)
                    }
                }

                // Card 4: See All (••• highlighted in soft blue-cyan like video)
                SketchPropertySquareCard(
                    label = "See All",
                    highlighted = true,
                    onClick = {
                        if (node.type == IdeWidgetType.TEXT_VIEW) {
                            onEditText()
                        } else {
                            onEditWidth()
                        }
                    },
                ) { tint ->
                    Canvas(Modifier.size(18.dp)) {
                        val r = 2.dp.toPx()
                        val cy = size.height * 0.50f
                        drawCircle(tint, r, Offset(size.width * 0.22f, cy))
                        drawCircle(tint, r, Offset(size.width * 0.50f, cy))
                        drawCircle(tint, r, Offset(size.width * 0.78f, cy))
                    }
                }

                // Extra cards when scrolling right: Height, Text, Text size, Padding
                SketchPropertySquareCard(
                    label = "Height",
                    highlighted = false,
                    onClick = onEditHeight,
                ) { tint ->
                    Canvas(Modifier.size(18.dp)) {
                        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        drawRoundRect(
                            color = tint,
                            topLeft = Offset(2.5f, 1.5f),
                            size = Size(size.width - 5f, size.height - 3f),
                            cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                            style = stroke,
                        )
                        val cx = size.width * 0.50f
                        drawLine(tint, Offset(cx, size.height * 0.24f), Offset(cx - 3f, size.height * 0.42f), stroke.width)
                        drawLine(tint, Offset(cx, size.height * 0.24f), Offset(cx + 3f, size.height * 0.42f), stroke.width)
                        drawLine(tint, Offset(cx, size.height * 0.76f), Offset(cx - 3f, size.height * 0.58f), stroke.width)
                        drawLine(tint, Offset(cx, size.height * 0.76f), Offset(cx + 3f, size.height * 0.58f), stroke.width)
                    }
                }

                if (node.type == IdeWidgetType.TEXT_VIEW) {
                    SketchPropertySquareCard(
                        label = "Text",
                        highlighted = false,
                        onClick = onEditText,
                    ) { tint ->
                        SketchWidgetIcon(type = IdeWidgetType.TEXT_VIEW, tint = tint)
                    }

                    SketchPropertySquareCard(
                        label = "Text size\n(${node.textSizeSp}sp)",
                        highlighted = false,
                        onClick = onCycleTextSize,
                    ) { tint ->
                        SketchWidgetIcon(type = IdeWidgetType.TEXT_VIEW, tint = tint)
                    }
                }

                SketchPropertySquareCard(
                    label = "Padding\n(${node.paddingDp}dp)",
                    highlighted = false,
                    onClick = onCyclePadding,
                ) { tint ->
                    SketchWidgetIcon(type = IdeWidgetType.LINEAR_H, tint = tint)
                }
            }
        }
    }
}

@Composable
private fun SketchPropertySquareCard(
    label: String,
    highlighted: Boolean,
    onClick: () -> Unit,
    icon: @Composable (Color) -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val bgColor = if (highlighted) Color(0xFF1F3166) else Color(0xFF161F40)
    val borderColor = if (highlighted) Cyan.copy(alpha = 0.55f) else Color(0x24FFFFFF)
    val contentTint = if (highlighted) Cyan else Ink.copy(alpha = 0.90f)

    Column(
        modifier = Modifier
            .width(80.dp)
            .height(74.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            icon(contentTint)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.5.sp,
                lineHeight = 12.sp,
                fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = contentTint,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ============================================================================
// 3. Bottom Bar Matching Screenshot Layout (Left XML Pill + Right Run Split Pill)
// ============================================================================

@Composable
private fun IdeSketchBottomBar(
    activeTab: IdeProjectTab,
    xmlPage: ProjectXmlPage,
    onOpenPageSelector: () -> Unit,
    onRunClick: () -> Unit,
    bottomBarPadding: Dp = 0.dp,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0F24))
            .navigationBarsPadding()
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 6.dp,
                bottom = 8.dp + bottomBarPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Left Selector Pill (matches screenshot's `[📱] main.xml  ▾` button size & spacing)
        Row(
            modifier = Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Color(0xFF151D3B))
                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(percent = 50))
                .clickable(onClick = onOpenPageSelector)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Small phone rectangle icon like screenshot
            Canvas(Modifier.size(width = 12.dp, height = 15.dp)) {
                drawRoundRect(
                    color = Cyan,
                    topLeft = Offset(1f, 1f),
                    size = Size(size.width - 2f, size.height - 2f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text = if (activeTab == IdeProjectTab.LOGIC) {
                    "${xmlPage.activityName} (${xmlPage.xmlFileName})"
                } else {
                    xmlPage.xmlFileName
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = Ink.copy(alpha = 0.85f),
                modifier = Modifier.size(20.dp),
            )
        }

        // Right Split Run Button (`▷ Run` + `⌄`) matching screenshot proportions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .width(108.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            bottomStart = 20.dp,
                            topEnd = 4.dp,
                            bottomEnd = 4.dp,
                        ),
                    )
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF283A70), Color(0xFF364F96)),
                        ),
                    )
                    .border(
                        width = 1.dp,
                        color = Cyan.copy(alpha = 0.40f),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            bottomStart = 20.dp,
                            topEnd = 4.dp,
                            bottomEnd = 4.dp,
                        ),
                    )
                    .clickable(onClick = onRunClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = stringResource(R.string.ide_run_button),
                    tint = Color.White,
                    modifier = Modifier.size(17.dp),
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.ide_run_button),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(38.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 4.dp,
                            bottomStart = 4.dp,
                            topEnd = 10.dp,
                            bottomEnd = 10.dp,
                        ),
                    )
                    .background(Color(0xFF2B3E78))
                    .border(
                        width = 1.dp,
                        color = Cyan.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(
                            topStart = 4.dp,
                            bottomStart = 4.dp,
                            topEnd = 10.dp,
                            bottomEnd = 10.dp,
                        ),
                    )
                    .clickable(onClick = onOpenPageSelector),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

// ============================================================================
// 4. LOGIC Tab: Android Code Editor (Java Android | Kotlin Android | xml Android)
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xE606091A))
            .border(1.dp, VioletSoft.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
    ) {
        // 1. Code Editor Mode Switcher: Java Android | Kotlin Android | xml Android
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
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) accent.copy(alpha = 0.22f) else Color(0x14FFFFFF))
                            .border(
                                width = 1.dp,
                                color = if (selected) accent.copy(alpha = 0.70f) else Color(0x22FFFFFF),
                                shape = RoundedCornerShape(8.dp),
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

        // 2. Quick Symbol Bar
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

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0x1FFFFFFF)),
        )

        // 3. Syntax-Highlighted Code Area with Line Numbers
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
            Regex("</?[A-Za-z0-9_.:-]+|/?>").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            Regex("\\b[A-Za-z0-9_]+:[A-Za-z0-9_]+").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFFC084FC)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            Regex("\"[^\"\n]*\"").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF34D399)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
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
            Regex("\\b(AppCompatActivity|Activity|Bundle|TextView|LinearLayout|ScrollView|HorizontalScrollView|View|R|layout|id)\\b").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            Regex("@[A-Za-z0-9_]+").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFFFBBF24)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
            Regex("\"[^\"\n]*\"").findAll(raw).forEach { m ->
                builder.addStyle(
                    SpanStyle(color = Color(0xFF34D399)),
                    m.range.first,
                    m.range.last + 1,
                )
            }
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

@Composable
private fun IdeEmptyWorkspaceCanvas() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x66060A1C))
            .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(18.dp)),
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
private fun EditWidgetIdDialog(
    initialId: String,
    widgetType: IdeWidgetType,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var rawId by remember { mutableStateOf(initialId) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1230),
        titleContentColor = Ink,
        textContentColor = Ink,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "${sketchWidgetLabel(widgetType)} (@+id/$initialId)",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
            )
        },
        text = {
            OutlinedTextField(
                value = rawId,
                onValueChange = { rawId = it },
                singleLine = true,
                label = { Text("android:id") },
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cleaned = rawId.trim()
                        .replace(Regex("[^a-zA-Z0-9_]+"), "_")
                        .ifBlank { initialId }
                    onConfirm(cleaned)
                },
            ) {
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
private fun EditWidgetDimensionDialog(
    viewId: String,
    isWidth: Boolean,
    currentValue: String,
    onDismiss: () -> Unit,
    onSelectValue: (String) -> Unit,
) {
    val options = listOf("match_parent", "wrap_content")
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1230),
        titleContentColor = Ink,
        textContentColor = Ink,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "${if (isWidth) "Width" else "Height"} (@+id/$viewId)",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = JetBrainsMono),
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { opt ->
                    val selected = opt == currentValue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Cyan.copy(alpha = 0.18f) else Color(0x14FFFFFF))
                            .border(
                                width = 1.dp,
                                color = if (selected) Cyan else Color(0x28FFFFFF),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable { onSelectValue(opt) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = if (selected) Cyan else Ink,
                        )
                        if (selected) {
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
        },
        confirmButton = {},
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

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 180.dp, max = 320.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Top,
                        ) {
                            if (widgetNodes.isEmpty()) {
                                Text(
                                    text = "${project.name} - ${activeXmlPage.activityName}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color(0xFF111827),
                                    modifier = Modifier.padding(12.dp),
                                )
                            } else {
                                widgetNodes.forEach { node ->
                                    RenderWidgetNodeOnPhone(
                                        node = node,
                                        selectedWidgetUid = null,
                                        draggingType = null,
                                        draggingExistingUid = null,
                                        hoveredContainerUid = null,
                                        dropInsertIndex = -1,
                                        onSelectWidget = {},
                                        onDoubleTapTextView = {},
                                        onStartMoveExistingWidget = { _, _ -> },
                                        onUpdateMoveDragPos = {},
                                        onEndMoveExistingWidget = {},
                                        onCancelMoveExistingWidget = {},
                                        onContainerPositioned = { _, _ -> },
                                        onWidgetPositioned = { _, _ -> },
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

private fun computeInsertIndex(
    siblings: List<IdeWidgetNode>,
    widgetBoundsMap: Map<String, Rect>,
    fingerInRoot: Offset,
    isHorizontal: Boolean,
): Int {
    if (siblings.isEmpty()) return 0
    for (i in siblings.indices) {
        val rect = widgetBoundsMap[siblings[i].uid] ?: continue
        if (rect.isEmpty) continue
        val midpoint = if (isHorizontal) rect.center.x else rect.center.y
        val fingerCoord = if (isHorizontal) fingerInRoot.x else fingerInRoot.y
        if (fingerCoord < midpoint) {
            return i
        }
    }
    return siblings.size
}

private fun containsDescendantUid(node: IdeWidgetNode?, targetUid: String): Boolean {
    if (node == null) return false
    if (node.uid == targetUid) return true
    return node.children.any { containsDescendantUid(it, targetUid) }
}

private fun flattenAllNodes(nodes: List<IdeWidgetNode>): List<IdeWidgetNode> {
    val result = mutableListOf<IdeWidgetNode>()
    nodes.forEach { node ->
        result.add(node)
        result.addAll(flattenAllNodes(node.children))
    }
    return result
}

private fun countNodesOfPrefix(nodes: List<IdeWidgetNode>, idPrefix: String): Int {
    var count = 0
    nodes.forEach { node ->
        if (node.type.idPrefix == idPrefix) count += 1
        count += countNodesOfPrefix(node.children, idPrefix)
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
): List<IdeWidgetNode> = insertChildIntoContainerAt(nodes, containerUid, newChild, -1)

private fun insertChildIntoContainerAt(
    nodes: List<IdeWidgetNode>,
    containerUid: String,
    newChild: IdeWidgetNode,
    insertIndex: Int,
): List<IdeWidgetNode> = nodes.map { node ->
    when {
        node.uid == containerUid && node.type.isContainer -> {
            val idx = if (insertIndex < 0) node.children.size else insertIndex.coerceIn(0, node.children.size)
            val updatedChildren = node.children.toMutableList().apply { add(idx, newChild) }
            node.copy(children = updatedChildren)
        }
        node.children.isNotEmpty() ->
            node.copy(children = insertChildIntoContainerAt(node.children, containerUid, newChild, insertIndex))
        else -> node
    }
}

private fun moveNodeInParent(
    nodes: List<IdeWidgetNode>,
    targetUid: String,
    direction: Int,
): List<IdeWidgetNode> {
    val index = nodes.indexOfFirst { it.uid == targetUid }
    if (index >= 0) {
        val newIndex = (index + direction).coerceIn(0, nodes.lastIndex)
        if (newIndex == index) return nodes
        val mutable = nodes.toMutableList()
        val item = mutable.removeAt(index)
        mutable.add(newIndex, item)
        return mutable
    }
    return nodes.map { node ->
        if (node.children.isEmpty()) {
            node
        } else {
            node.copy(children = moveNodeInParent(node.children, targetUid, direction))
        }
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

private fun updateNodeByUid(
    nodes: List<IdeWidgetNode>,
    targetUid: String,
    transform: (IdeWidgetNode) -> IdeWidgetNode,
): List<IdeWidgetNode> = nodes.map { node ->
    when {
        node.uid == targetUid -> transform(node)
        node.children.isNotEmpty() -> node.copy(children = updateNodeByUid(node.children, targetUid, transform))
        else -> node
    }
}

private fun updateNodeText(
    nodes: List<IdeWidgetNode>,
    targetUid: String,
    newText: String,
): List<IdeWidgetNode> = updateNodeByUid(nodes, targetUid) { it.copy(text = newText) }
