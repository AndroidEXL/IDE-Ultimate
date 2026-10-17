package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.graphics.PathEffect
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
import androidx.compose.ui.text.font.FontStyle
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

private enum class IdeBuildMethod(
    val title: String,
    val subtitle: String,
    val badge: String,
    val badgeColor: Color,
) {
    GRADLE(
        title = "Gradle",
        subtitle = "Android Gradle Wrapper (:app:assembleDebug) • بناء كامل عبر محرك Gradle",
        badge = "./gradlew assembleDebug",
        badgeColor = Color(0xFF34D399),
    ),
    GITHUB_ACTIONS(
        title = "GitHub Actions",
        subtitle = "Cloud CI/CD Workflow (build-apk.yml) • بناء سحابي عبر GitHub Actions",
        badge = "Cloud CI/CD",
        badgeColor = Color(0xFF38BDF8),
    ),
    SIMPLE(
        title = "Simple",
        subtitle = "Fast Local Compiler & Instant Preview • بناء سريع ومباشر داخل IDE",
        badge = "Fast Build",
        badgeColor = Color(0xFFA78BFA),
    ),
    ;

    companion object {
        fun fromName(raw: String): IdeBuildMethod =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: GRADLE
    }
}

private enum class IdeSideMenuItem(
    val title: String,
    val subtitle: String,
    val badge: String,
    val accentColor: Color,
) {
    LIBRARY_MANAGER(
        title = "Library Manager",
        subtitle = "إدارة مكتبات AndroidX و Material و AppCompat",
        badge = "LIB",
        accentColor = Color(0xFF38BDF8),
    ),
    ICON_MANAGER(
        title = "Icon Manager",
        subtitle = "إدارة أيقونات التطبيق و Vector Drawables",
        badge = "ICO",
        accentColor = Color(0xFFF472B6),
    ),
    SOUND_MANAGER(
        title = "Sound Manager",
        subtitle = "إدارة الملفات الصوتية والمؤثرات داخل res/raw",
        badge = "SND",
        accentColor = Color(0xFFFBBF24),
    ),
    FONT_MANAGER(
        title = "Font Manager",
        subtitle = "إدارة خطوط التطبيق المخصصة داخل res/font",
        badge = "FNT",
        accentColor = Color(0xFFA78BFA),
    ),
    RESOURCES_MANAGER(
        title = "Resources Manager",
        subtitle = "إدارة كافة موارد المشروع (drawable / raw / mipmap)",
        badge = "RES",
        accentColor = Color(0xFF34D399),
    ),
    TERMINAL_ANDROID(
        title = "Terminal Android",
        subtitle = "طرفية أوامر Android Shell و Gradle المدمجة",
        badge = "SH",
        accentColor = Color(0xFF2DD4BF),
    ),
    ACTIVITY_MANAGER(
        title = "Activity Manager",
        subtitle = "إدارة صفحات النشاطات (Activities) وملفات XML",
        badge = "ACT",
        accentColor = Color(0xFF60A5FA),
    ),
    ANDROID_MANIFEST_MANAGER(
        title = "Android Menifaset Manager",
        subtitle = "تحرير ومعاينة ملف AndroidManifest.xml للمشروع",
        badge = "XML",
        accentColor = Color(0xFFFB923C),
    ),
    PERMISSION_MANAGER(
        title = "Permission Manager",
        subtitle = "إدارة أذونات وصلاحيات النظام (uses-permission)",
        badge = "PRM",
        accentColor = Color(0xFFF87171),
    ),
    SECURITY_MANAGER(
        title = "Security Manager",
        subtitle = "إدارة التوقيع الرقمي Keystore وقواعد الحماية ProGuard",
        badge = "SEC",
        accentColor = Color(0xFF10B981),
    ),
    FILES_MANAGER(
        title = "Files Manager",
        subtitle = "متصفح ملفات ومجلدات المشروع على الذاكرة",
        badge = "DIR",
        accentColor = Color(0xFF38BDF8),
    ),
    STRINGS_TRANSLATION(
        title = "Strings Translation",
        subtitle = "إدارة النصوص والترجمات متعدّدة اللغات strings.xml",
        badge = "STR",
        accentColor = Color(0xFFE879F9),
    ),
    VALUES_MANAGER(
        title = "Values Manager",
        subtitle = "إدارة الألوان والأبعاد والأنماط (colors / styles / dimens)",
        badge = "VAL",
        accentColor = Color(0xFF818CF8),
    ),
    LOGCAT_READER_APP(
        title = "Logcat Reder App",
        subtitle = "قارئ سجلات التطبيق المباشر (Application Logcat)",
        badge = "LOG",
        accentColor = Color(0xFF22D3EE),
    ),
    LOGCAT_SYSTEM(
        title = "Logcat System",
        subtitle = "سجلات النظام الكاملة وتتبع الأعطال (System Logcat)",
        badge = "SYS",
        accentColor = Color(0xFFF59E0B),
    ),
    COLLECTION_MANAGER(
        title = "Collection Manager",
        subtitle = "مجموعات العناصر والبلوكات والأكواد المحفوظة",
        badge = "COL",
        accentColor = Color(0xFFC084FC),
    ),
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
    var showBuildMethodSheet by rememberSaveable { mutableStateOf(false) }
    var showSideMenuDrawer by rememberSaveable { mutableStateOf(false) }
    var activeSideMenuItem by remember { mutableStateOf<IdeSideMenuItem?>(null) }

    var selectedBuildMethodName by rememberSaveable(project.id) {
        mutableStateOf(repository.getBuildMethodForProject(project.id))
    }
    val selectedBuildMethod = remember(selectedBuildMethodName) {
        IdeBuildMethod.fromName(selectedBuildMethodName)
    }

    var selectedWidgetUid by remember(project.id, selectedXmlPage.xmlFileName) {
        mutableStateOf<String?>(null)
    }
    var isDraggingWidget by remember { mutableStateOf(false) }
    var editingPropertyRequest by remember { mutableStateOf<Pair<String, IdeWidgetProperty>?>(null) }
    val recentProperties = remember { mutableStateListOf<IdeWidgetProperty>() }

    val selectedNode = remember(widgetNodes, selectedWidgetUid) {
        selectedWidgetUid?.let { findNodeByUid(widgetNodes, it) }
    }

    fun openPropertyDialog(node: IdeWidgetNode, prop: IdeWidgetProperty) {
        if (prop != IdeWidgetProperty.SEE_ALL) {
            recentProperties.remove(prop)
            recentProperties.add(0, prop)
        }
        editingPropertyRequest = node.uid to prop
    }

    BackHandler {
        when {
            editingPropertyRequest != null -> editingPropertyRequest = null
            activeSideMenuItem != null -> activeSideMenuItem = null
            showSideMenuDrawer -> showSideMenuDrawer = false
            showBuildMethodSheet -> showBuildMethodSheet = false
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
            buildMethod = selectedBuildMethod,
            onChangeBuildMethod = { method ->
                selectedBuildMethodName = method.name
                repository.setBuildMethodForProject(project.id, method.name)
            },
            onClose = { showTempRunScreen = false },
            modifier = modifier,
        )
        return
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DeepNavy)
                .safeDrawingPadding(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // 1. Top Toolbar: Back Arrow + Project Icon + Project Name/Version + Undo / Redo / Save / More (Side Menu)
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
                    onMoreClick = {
                        showSideMenuDrawer = true
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
                                onRequestEditTextView = { node ->
                                    openPropertyDialog(node, IdeWidgetProperty.TEXT)
                                },
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
                // show the Sketchware-style Bottom Widget Property Panel.
                // Otherwise show the XML/Activity Selector Pill + Split Run Button.
                if (activeTab == IdeProjectTab.VIEW && selectedNode != null && !isDraggingWidget) {
                    IdeWidgetPropertyBottomPanel(
                        node = selectedNode,
                        allWidgets = flattenAllNodes(widgetNodes),
                        recentProperties = recentProperties,
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
                        onClickProperty = { prop ->
                            openPropertyDialog(selectedNode, prop)
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
                        selectedBuildMethod = selectedBuildMethod,
                        onOpenPageSelector = { showXmlBottomSheet = true },
                        onRunClick = { showTempRunScreen = true },
                        onRunLongClick = { showBuildMethodSheet = true },
                        bottomBarPadding = bottomBarPadding,
                    )
                }
            }

            // 5. Side Drawer Menu opened via 3-dots (⋮) button in Top Bar
            IdeProjectSideDrawer(
                visible = showSideMenuDrawer,
                project = project,
                onDismiss = { showSideMenuDrawer = false },
                onSelectItem = { item ->
                    showSideMenuDrawer = false
                    activeSideMenuItem = item
                },
            )
        }
    }

    val activePropReq = editingPropertyRequest
    if (activePropReq != null) {
        val (targetUid, targetProp) = activePropReq
        val targetNode = findNodeByUid(widgetNodes, targetUid)
        if (targetNode != null) {
            IdeWidgetPropertyEditDialog(
                node = targetNode,
                property = targetProp,
                onDismiss = { editingPropertyRequest = null },
                onSelectFromSeeAll = { chosenProp ->
                    openPropertyDialog(targetNode, chosenProp)
                },
                onUpdateNode = { updatedNode ->
                    updateWidgetNodes(
                        updateNodeByUid(widgetNodes, targetNode.uid) { updatedNode },
                    )
                    editingPropertyRequest = null
                },
            )
        } else {
            editingPropertyRequest = null
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

    // Bottom Sheet for Run button Long-Press: Select Build Method (Gradle, GitHub Actions, Simple)
    if (showBuildMethodSheet) {
        BuildMethodBottomSheet(
            selectedMethod = selectedBuildMethod,
            onSelectMethod = { chosen ->
                selectedBuildMethodName = chosen.name
                repository.setBuildMethodForProject(project.id, chosen.name)
                showBuildMethodSheet = false
            },
            onDismiss = { showBuildMethodSheet = false },
        )
    }

    // Dialog / Manager view when a Side Menu item is tapped
    val currentMenuItem = activeSideMenuItem
    if (currentMenuItem != null) {
        IdeSideMenuManagerDialog(
            project = project,
            menuItem = currentMenuItem,
            onDismiss = { activeSideMenuItem = null },
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
    onMoreClick: () -> Unit,
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

        // 3-Dots Icon (MoreVert) -> Opens the Side Menu Drawer
        Icon(
            imageVector = Icons.Filled.MoreVert,
            contentDescription = "Side Menu",
            tint = Ink.copy(alpha = 0.88f),
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .clickable(onClick = onMoreClick)
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

    val currentWidgetNodes by rememberUpdatedState(widgetNodes)
    val currentSelectedWidgetUid by rememberUpdatedState(selectedWidgetUid)
    val currentOnSelectWidgetUid by rememberUpdatedState(onSelectWidgetUid)
    val currentOnUpdateWidgets by rememberUpdatedState(onUpdateWidgets)

    LaunchedEffect(draggingType) {
        onDraggingStateChange(draggingType != null)
    }

    // Live evaluation functions that always read current MutableState values (never stale!)
    fun evalIsDragOverPhone(): Boolean {
        if (draggingType == null) return false
        if (!phoneCanvasBounds.isEmpty) {
            return dragFingerInRoot.x >= phoneCanvasBounds.left - 48f
        }
        if (!rootBounds.isEmpty) {
            val paletteRightPx = rootBounds.left + with(density) { 108.dp.toPx() }
            return dragFingerInRoot.x >= paletteRightPx
        }
        return true
    }

    fun evalHoveredContainerUid(): String? {
        if (!evalIsDragOverPhone() || draggingType == null) return null
        val nodes = currentWidgetNodes
        val movingUid = draggingExistingNode?.uid
        return containerBoundsMap.entries
            .filter { (uid, rect) ->
                uid != movingUid &&
                    findNodeByUid(nodes, uid)?.type?.isContainer == true &&
                    (movingUid == null || !containsDescendantUid(findNodeByUid(nodes, movingUid), uid)) &&
                    !rect.isEmpty &&
                    rect.contains(dragFingerInRoot)
            }
            .minByOrNull { (_, rect) -> rect.width * rect.height }
            ?.key
    }

    fun evalDropInsertIndex(targetContainerUid: String?): Int {
        if (!evalIsDragOverPhone() || draggingType == null) return -1
        val nodes = currentWidgetNodes
        val movingUid = draggingExistingNode?.uid
        return if (targetContainerUid != null) {
            val containerNode = findNodeByUid(nodes, targetContainerUid)
            val children = containerNode?.children?.filter { it.uid != movingUid }.orEmpty()
            val isHorizontal = containerNode?.type == IdeWidgetType.LINEAR_H ||
                containerNode?.type == IdeWidgetType.SCROLL_H
            computeInsertIndex(children, widgetBoundsMap, dragFingerInRoot, isHorizontal)
        } else {
            val rootChildren = nodes.filter { it.uid != movingUid }
            computeInsertIndex(rootChildren, widgetBoundsMap, dragFingerInRoot, isHorizontal = false)
        }
    }

    val isDragOverPhone = evalIsDragOverPhone()
    val hoveredContainerUid = evalHoveredContainerUid()
    val dropInsertIndex = evalDropInsertIndex(hoveredContainerUid)

    fun nextViewIdFor(type: IdeWidgetType): String {
        val nodes = currentWidgetNodes
        val existingIds = flattenAllNodes(nodes).map { it.viewId }.toSet()
        var index = countNodesOfPrefix(nodes, type.idPrefix) + 1
        while ("${type.idPrefix}$index" in existingIds) {
            index++
        }
        return "${type.idPrefix}$index"
    }

    fun commitDrop(type: IdeWidgetType) {
        val existingMoving = draggingExistingNode
        val overPhone = evalIsDragOverPhone()
        if (!overPhone) {
            draggingType = null
            draggingExistingNode = null
            return
        }

        val nodes = currentWidgetNodes
        val targetContainer = evalHoveredContainerUid()
        val insertIdx = evalDropInsertIndex(targetContainer)

        val nodeToInsert = existingMoving ?: IdeWidgetNode(
            viewId = nextViewIdFor(type),
            type = type,
            text = if (type == IdeWidgetType.TEXT_VIEW) "TextView" else type.displayName,
        )

        val baseTree = if (existingMoving != null) {
            removeNodeByUid(nodes, existingMoving.uid)
        } else {
            nodes
        }

        val updated = if (targetContainer != null) {
            insertChildIntoContainerAt(
                nodes = baseTree,
                containerUid = targetContainer,
                newChild = nodeToInsert,
                insertIndex = insertIdx,
            )
        } else {
            val safeIndex = if (insertIdx < 0) baseTree.size else insertIdx.coerceIn(0, baseTree.size)
            baseTree.toMutableList().apply { add(safeIndex, nodeToInsert) }
        }

        currentOnUpdateWidgets(updated)
        currentOnSelectWidgetUid(nodeToInsert.uid)
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
                isDragging = draggingType != null,
                onStartDrag = { type, startRootPos ->
                    currentOnSelectWidgetUid(null)
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
                    val droppedType = draggingType
                    if (droppedType != null && evalIsDragOverPhone()) {
                        commitDrop(droppedType)
                    }
                    draggingType = null
                    draggingExistingNode = null
                },
                onPaletteTouchOrScroll = {
                    if (currentSelectedWidgetUid != null) {
                        currentOnSelectWidgetUid(null)
                    }
                },
                onTapAdd = { type ->
                    val nodes = currentWidgetNodes
                    val selectedNode = currentSelectedWidgetUid?.let { findNodeByUid(nodes, it) }
                    val targetContainer = selectedNode?.takeIf { it.type.isContainer }?.uid
                    val newNode = IdeWidgetNode(
                        viewId = nextViewIdFor(type),
                        type = type,
                        text = if (type == IdeWidgetType.TEXT_VIEW) "TextView" else type.displayName,
                    )
                    val updated = if (targetContainer != null) {
                        insertChildIntoContainer(nodes, targetContainer, newNode)
                    } else {
                        nodes + newNode
                    }
                    currentOnUpdateWidgets(updated)
                    currentOnSelectWidgetUid(newNode.uid)
                },
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
            )

            // Right Phone Frame Area: shortened vertical height (طول الفريم أقصر ومتناسق)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 8.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
                contentAlignment = Alignment.TopCenter,
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
                    onSelectWidget = { node -> currentOnSelectWidgetUid(node.uid) },
                    onDoubleTapTextView = { node -> onRequestEditTextView(node) },
                    onStartMoveExistingWidget = { node, startRootPos ->
                        currentOnSelectWidgetUid(null)
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
                        val droppedType = draggingType
                        if (droppedType != null && evalIsDragOverPhone()) {
                            commitDrop(droppedType)
                        }
                        draggingType = null
                        draggingExistingNode = null
                    },
                    onPhoneCanvasPositioned = { rect -> phoneCanvasBounds = rect },
                    onContainerPositioned = { uid, rect -> containerBoundsMap[uid] = rect },
                    onWidgetPositioned = { uid, rect -> widgetBoundsMap[uid] = rect },
                    onTapEmptyWhiteArea = { currentOnSelectWidgetUid(null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.76f),
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
    isDragging: Boolean,
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
            .verticalScroll(scrollState, enabled = !isDragging)
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
    val currentOnStartDrag by rememberUpdatedState(onStartDrag)
    val currentOnUpdateDragRootPos by rememberUpdatedState(onUpdateDragRootPos)
    val currentOnEndDrag by rememberUpdatedState(onEndDrag)
    val currentOnCancelDrag by rememberUpdatedState(onCancelDrag)
    val currentOnTap by rememberUpdatedState(onTap)

    val shape = RoundedCornerShape(6.dp)

    // Supports BOTH hold-to-drag (~120ms) AND immediate drag toward the phone canvas,
    // using rememberUpdatedState + try/finally so releasing the finger ALWAYS commits the drop.
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
                    var endHandled = false
                    var latestPos = downPos

                    try {
                        // Phase 1: Wait until hold threshold (120ms) or finger movement
                        while (!dragging) {
                            val event = withTimeoutOrNull(120L) {
                                awaitPointerEvent(PointerEventPass.Initial)
                            }
                            if (event == null) {
                                // Finger held for 120ms -> start drag immediately like Sketchware!
                                dragging = true
                                val rootPos = Offset(
                                    x = itemBoundsInRoot.left + latestPos.x,
                                    y = itemBoundsInRoot.top + latestPos.y,
                                )
                                currentOnStartDrag(type, rootPos)
                                break
                            }
                            val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                            if (change == null || !change.pressed) {
                                // Released quickly -> treat as a tap if within tap slop
                                if (System.currentTimeMillis() - downTime < 220L &&
                                    (latestPos - downPos).getDistance() < viewConfiguration.touchSlop
                                ) {
                                    endHandled = true
                                    currentOnTap()
                                }
                                return@awaitEachGesture
                            }
                            latestPos = change.position
                            val dx = latestPos.x - downPos.x
                            val dy = latestPos.y - downPos.y
                            if (abs(dx) > 4f || (latestPos - downPos).getDistance() > 6f) {
                                change.consume()
                                dragging = true
                                val rootPos = Offset(
                                    x = itemBoundsInRoot.left + latestPos.x,
                                    y = itemBoundsInRoot.top + latestPos.y,
                                )
                                currentOnStartDrag(type, rootPos)
                            } else if (System.currentTimeMillis() - downTime >= 120L) {
                                change.consume()
                                dragging = true
                                val rootPos = Offset(
                                    x = itemBoundsInRoot.left + latestPos.x,
                                    y = itemBoundsInRoot.top + latestPos.y,
                                )
                                currentOnStartDrag(type, rootPos)
                            }
                        }

                        // Phase 2: Track drag on Initial pass and commit drop when finger lifts
                        while (dragging) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val change = event.changes.firstOrNull { it.id == down.id } ?: event.changes.firstOrNull()
                            if (change == null) {
                                endHandled = true
                                currentOnEndDrag()
                                break
                            }
                            latestPos = change.position
                            val currentRootPos = Offset(
                                x = itemBoundsInRoot.left + latestPos.x,
                                y = itemBoundsInRoot.top + latestPos.y,
                            )
                            currentOnUpdateDragRootPos(currentRootPos)
                            change.consume()
                            if (!change.pressed) {
                                endHandled = true
                                currentOnEndDrag()
                                break
                            }
                        }
                    } finally {
                        if (dragging && !endHandled) {
                            currentOnEndDrag()
                        } else if (!dragging && !endHandled) {
                            currentOnCancelDrag()
                        }
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
        val showRootPlaceholder = isDragOverPhone && draggingType != null && hoveredContainerUid == null
        val nonMovingRootCount = widgetNodes.count { it.uid != draggingExistingUid }
        val safeRootInsertIndex = if (showRootPlaceholder) {
            dropInsertIndex.coerceIn(0, nonMovingRootCount)
        } else {
            -1
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
                .verticalScroll(rememberScrollState(), enabled = draggingType == null),
            verticalArrangement = Arrangement.Top,
        ) {
            var visibleSlot = 0
            widgetNodes.forEach { node ->
                val isMovingThis = node.uid == draggingExistingUid
                if (!isMovingThis && showRootPlaceholder && visibleSlot == safeRootInsertIndex && draggingType != null) {
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
                if (!isMovingThis) {
                    visibleSlot++
                }
            }
            if (showRootPlaceholder && safeRootInsertIndex == nonMovingRootCount && draggingType != null) {
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
    val isBeingMoved = node.uid == draggingExistingUid
    var nodeBoundsInRoot by remember(node.uid) { mutableStateOf(Rect.Zero) }

    val currentNode by rememberUpdatedState(node)
    val currentOnStartMove by rememberUpdatedState(onStartMoveExistingWidget)
    val currentOnUpdateMove by rememberUpdatedState(onUpdateMoveDragPos)
    val currentOnEndMove by rememberUpdatedState(onEndMoveExistingWidget)
    val currentOnCancelMove by rememberUpdatedState(onCancelMoveExistingWidget)

    // Sketchware's signature light cyan-teal selection fill (#C2E4E8) and #757575 outline border
    val selectedFill = Color(0xFFC2E4E8)
    val layoutOutlineColor = Color(0xFF757575)

    val nonMovingChildCount = node.children.count { it.uid != draggingExistingUid }
    val safeChildInsertIndex = if (isHoveredDropTarget && draggingType != null) {
        dropInsertIndex.coerceIn(0, nonMovingChildCount)
    } else {
        -1
    }

    val customWidthDp = node.layoutWidth.removeSuffix("dp").trim().toIntOrNull()
    val customHeightDp = node.layoutHeight.removeSuffix("dp").trim().toIntOrNull()

    val widthModifier = when {
        isBeingMoved -> Modifier.width(0.dp).height(0.dp).graphicsLayer(alpha = 0f)
        customWidthDp != null && customWidthDp > 0 -> Modifier.width(customWidthDp.coerceIn(16, 320).dp)
        node.layoutWidth == "match_parent" -> Modifier.fillMaxWidth()
        else -> Modifier.wrapContentWidth()
    }

    val visualPad = (node.paddingDp / 2).coerceIn(2, 16).dp
    val visualMargin = (node.marginDp / 2).coerceIn(0, 16).dp
    val parsedBgColor = remember(node.backgroundColor) {
        parseHexColorOrNull(node.backgroundColor)
    }
    val baseFill = when {
        isSelected -> selectedFill
        parsedBgColor != null -> parsedBgColor
        node.backgroundResource != "none" && node.backgroundResource.isNotBlank() -> Color(0xFFEFF6FF)
        else -> Color.Transparent
    }
    val transformModifier = if (isBeingMoved) {
        Modifier
    } else {
        Modifier.graphicsLayer(
            rotationZ = node.rotation,
            alpha = node.alpha.coerceIn(0.08f, 1f),
            translationX = node.translationX,
            translationY = node.translationY,
            scaleX = node.scaleX.coerceIn(0.25f, 2.5f),
            scaleY = node.scaleY.coerceIn(0.25f, 2.5f),
        )
    }

    val horizontalAlign = when {
        node.gravity.contains("right") -> Alignment.End
        node.gravity.contains("center") -> Alignment.CenterHorizontally
        else -> Alignment.Start
    }
    val verticalAlign = when {
        node.gravity.contains("bottom") -> Alignment.Bottom
        node.gravity.contains("center") -> Alignment.CenterVertically
        else -> Alignment.Top
    }
    val horizontalArr = when {
        node.gravity.contains("right") -> Arrangement.End
        node.gravity.contains("center") -> Arrangement.Center
        else -> Arrangement.Start
    }
    val verticalArr = when {
        node.gravity.contains("bottom") -> Arrangement.Bottom
        node.gravity.contains("center") -> Arrangement.Center
        else -> Arrangement.Top
    }

    when (node.type) {
        IdeWidgetType.LINEAR_H, IdeWidgetType.SCROLL_H -> {
            val isEmpty = nonMovingChildCount == 0 && !isHoveredDropTarget
            Row(
                modifier = Modifier
                    .padding(if (isBeingMoved) 0.dp else visualMargin)
                    .then(widthModifier)
                    .then(
                        when {
                            isBeingMoved -> Modifier.height(0.dp)
                            customHeightDp != null && customHeightDp > 0 -> Modifier.height(customHeightDp.coerceIn(20, 260).dp)
                            isEmpty -> Modifier.height(28.dp)
                            else -> Modifier.heightIn(min = 28.dp)
                        },
                    )
                    .then(transformModifier)
                    .onGloballyPositioned { coords ->
                        if (!isBeingMoved) {
                            val rect = coords.boundsInRoot()
                            nodeBoundsInRoot = rect
                            onContainerPositioned(node.uid, rect)
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(baseFill)
                    .border(if (isBeingMoved) 0.dp else 0.8.dp, layoutOutlineColor, RectangleShape)
                    .clickable { onSelectWidget(node) }
                    .pointerInput(node.uid) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { localOffset ->
                                currentOnStartMove(
                                    currentNode,
                                    Offset(
                                        x = nodeBoundsInRoot.left + localOffset.x,
                                        y = nodeBoundsInRoot.top + localOffset.y,
                                    ),
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentOnUpdateMove(
                                    Offset(
                                        x = nodeBoundsInRoot.left + change.position.x,
                                        y = nodeBoundsInRoot.top + change.position.y,
                                    ),
                                )
                            },
                            onDragEnd = { currentOnEndMove() },
                            onDragCancel = { currentOnCancelMove() },
                        )
                    }
                    .padding(if (isEmpty || isBeingMoved) 0.dp else visualPad),
                verticalAlignment = verticalAlign,
                horizontalArrangement = horizontalArr,
            ) {
                var childSlot = 0
                node.children.forEach { child ->
                    val childMoving = child.uid == draggingExistingUid
                    if (!childMoving && isHoveredDropTarget && childSlot == safeChildInsertIndex && draggingType != null) {
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
                    if (!childMoving) {
                        childSlot++
                    }
                }
                if (isHoveredDropTarget && safeChildInsertIndex == nonMovingChildCount && draggingType != null) {
                    SketchDropPlaceholder(
                        type = draggingType,
                        inHorizontalParent = true,
                    )
                }
            }
        }

        IdeWidgetType.LINEAR_V, IdeWidgetType.SCROLL_V -> {
            val isEmpty = nonMovingChildCount == 0 && !isHoveredDropTarget
            Column(
                modifier = Modifier
                    .padding(if (isBeingMoved) 0.dp else visualMargin)
                    .then(widthModifier)
                    .then(
                        when {
                            isBeingMoved -> Modifier.height(0.dp)
                            customHeightDp != null && customHeightDp > 0 -> Modifier.height(customHeightDp.coerceIn(20, 260).dp)
                            isEmpty -> Modifier.height(28.dp)
                            else -> Modifier.heightIn(min = 28.dp)
                        },
                    )
                    .then(transformModifier)
                    .onGloballyPositioned { coords ->
                        if (!isBeingMoved) {
                            val rect = coords.boundsInRoot()
                            nodeBoundsInRoot = rect
                            onContainerPositioned(node.uid, rect)
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(baseFill)
                    .border(if (isBeingMoved) 0.dp else 0.8.dp, layoutOutlineColor, RectangleShape)
                    .clickable { onSelectWidget(node) }
                    .pointerInput(node.uid) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { localOffset ->
                                currentOnStartMove(
                                    currentNode,
                                    Offset(
                                        x = nodeBoundsInRoot.left + localOffset.x,
                                        y = nodeBoundsInRoot.top + localOffset.y,
                                    ),
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentOnUpdateMove(
                                    Offset(
                                        x = nodeBoundsInRoot.left + change.position.x,
                                        y = nodeBoundsInRoot.top + change.position.y,
                                    ),
                                )
                            },
                            onDragEnd = { currentOnEndMove() },
                            onDragCancel = { currentOnCancelMove() },
                        )
                    }
                    .padding(if (isEmpty || isBeingMoved) 0.dp else visualPad),
                verticalArrangement = verticalArr,
                horizontalAlignment = horizontalAlign,
            ) {
                var childSlot = 0
                node.children.forEach { child ->
                    val childMoving = child.uid == draggingExistingUid
                    if (!childMoving && isHoveredDropTarget && childSlot == safeChildInsertIndex && draggingType != null) {
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
                    if (!childMoving) {
                        childSlot++
                    }
                }
                if (isHoveredDropTarget && safeChildInsertIndex == nonMovingChildCount && draggingType != null) {
                    SketchDropPlaceholder(
                        type = draggingType,
                        inHorizontalParent = false,
                    )
                }
            }
        }

        IdeWidgetType.TEXT_VIEW -> {
            val parsedTxtColor = remember(node.textColor) {
                parseHexColorOrNull(node.textColor) ?: Color(0xFF222222)
            }
            val fontWeight = if (node.textStyle.contains("bold")) FontWeight.Bold else FontWeight.Normal
            val fontStyle = if (node.textStyle.contains("italic")) FontStyle.Italic else FontStyle.Normal
            val boxAlignment = when {
                node.gravity.contains("center") -> Alignment.Center
                node.gravity.contains("right") -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }
            Box(
                modifier = Modifier
                    .padding(if (isBeingMoved) 0.dp else visualMargin)
                    .then(widthModifier)
                    .then(
                        if (!isBeingMoved && customHeightDp != null && customHeightDp > 0) {
                            Modifier.height(customHeightDp.coerceIn(18, 220).dp)
                        } else {
                            Modifier
                        },
                    )
                    .then(transformModifier)
                    .onGloballyPositioned { coords ->
                        if (!isBeingMoved) {
                            val rect = coords.boundsInRoot()
                            nodeBoundsInRoot = rect
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(baseFill)
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
                                currentOnStartMove(
                                    currentNode,
                                    Offset(
                                        x = nodeBoundsInRoot.left + localOffset.x,
                                        y = nodeBoundsInRoot.top + localOffset.y,
                                    ),
                                )
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentOnUpdateMove(
                                    Offset(
                                        x = nodeBoundsInRoot.left + change.position.x,
                                        y = nodeBoundsInRoot.top + change.position.y,
                                    ),
                                )
                            },
                            onDragEnd = { currentOnEndMove() },
                            onDragCancel = { currentOnCancelMove() },
                        )
                    }
                    .padding(
                        horizontal = if (isBeingMoved) 0.dp else (node.paddingDp.coerceIn(2, 24)).dp,
                        vertical = if (isBeingMoved) 0.dp else (node.paddingDp / 2).coerceIn(2, 16).dp,
                    ),
                contentAlignment = boxAlignment,
            ) {
                if (!isBeingMoved) {
                    Text(
                        text = node.text,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = (node.textSizeSp - 1).coerceIn(9, 28).sp,
                            fontWeight = fontWeight,
                            fontStyle = fontStyle,
                        ),
                        color = parsedTxtColor,
                        maxLines = if (node.lines > 0) node.lines else Int.MAX_VALUE,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun parseHexColorOrNull(raw: String): Color? {
    val clean = raw.trim().removePrefix("#")
    if (clean.equals("transparent", ignoreCase = true) || clean.equals("none", ignoreCase = true) || clean.isBlank()) {
        return null
    }
    return runCatching {
        when (clean.length) {
            6 -> Color(("FF$clean").toLong(16))
            8 -> Color(clean.toLong(16))
            else -> null
        }
    }.getOrNull()
}

// ============================================================================
// 2B. Bottom Widget Property Panel (All 24 Properties from User Screenshots)
//     Layouts exclude Text-specific properties; every card opens a value dialog
// ============================================================================

private enum class WidgetPropTab(val label: String) {
    BASIC("Basic"),
    RECENT("Recent"),
    EVENT("Event"),
}

private enum class IdeWidgetProperty(
    val title: String,
    val cardLabel: String,
    val textViewOnly: Boolean = false,
) {
    CUSTOM_ATTRIBUTES(title = "Custom attributes", cardLabel = "Custom\nattributes"),
    CONVERT(title = "Convert", cardLabel = "Convert"),
    WIDTH(title = "Width", cardLabel = "Width"),
    SEE_ALL(title = "See All", cardLabel = "See All"),
    HEIGHT(title = "Height", cardLabel = "Height"),
    PADDING(title = "Padding", cardLabel = "Padding"),
    MARGIN(title = "Margin", cardLabel = "Margin"),
    GRAVITY(title = "Gravity", cardLabel = "Gravity"),
    LAYOUT_GRAVITY(title = "Layout gravity", cardLabel = "Layout gravity"),
    WEIGHT(title = "Weight", cardLabel = "Weight"),
    TEXT(title = "Text", cardLabel = "Text", textViewOnly = true),
    TEXT_SIZE(title = "Text size", cardLabel = "Text size", textViewOnly = true),
    TEXT_STYLE(title = "Text style", cardLabel = "Text style", textViewOnly = true),
    TEXT_COLOR(title = "Text color", cardLabel = "Text color", textViewOnly = true),
    LINES(title = "Lines", cardLabel = "Lines", textViewOnly = true),
    BACKGROUND_RESOURCE(title = "Background resource", cardLabel = "Background\nresource"),
    BACKGROUND_COLOR(title = "Background color", cardLabel = "Background\ncolor"),
    ROTATE(title = "Rotate", cardLabel = "Rotate"),
    ALPHA(title = "Alpha", cardLabel = "Alpha"),
    TRANSLATION_X(title = "TranslationX", cardLabel = "TranslationX"),
    TRANSLATION_Y(title = "TranslationY", cardLabel = "TranslationY"),
    SCALE_X(title = "ScaleX", cardLabel = "ScaleX"),
    SCALE_Y(title = "ScaleY", cardLabel = "ScaleY"),
    ID(title = "ID", cardLabel = "ID"),
}

@Composable
private fun IdeWidgetPropertyBottomPanel(
    node: IdeWidgetNode,
    allWidgets: List<IdeWidgetNode>,
    recentProperties: List<IdeWidgetProperty>,
    onSelectOtherWidget: (IdeWidgetNode) -> Unit,
    onMoveUp: () -> Unit,
    onCollapse: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onClickProperty: (IdeWidgetProperty) -> Unit,
    onOpenLogicEvent: () -> Unit,
    bottomBarPadding: Dp = 0.dp,
) {
    var activePropTab by remember { mutableStateOf(WidgetPropTab.BASIC) }
    var showWidgetPickerMenu by remember { mutableStateOf(false) }

    // Filter out Text-specific properties when a Layout widget (Linear/Scroll) is selected!
    val availableProperties = remember(node.type) {
        IdeWidgetProperty.entries.filter { prop ->
            !prop.textViewOnly || node.type == IdeWidgetType.TEXT_VIEW
        }
    }
    val displayedProperties = remember(node.type, activePropTab, recentProperties.size) {
        when (activePropTab) {
            WidgetPropTab.BASIC -> availableProperties
            WidgetPropTab.RECENT -> {
                val filteredRecent = recentProperties.filter { prop ->
                    !prop.textViewOnly || node.type == IdeWidgetType.TEXT_VIEW
                }
                filteredRecent.ifEmpty { availableProperties.take(6) }
            }
            WidgetPropTab.EVENT -> emptyList()
        }
    }

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
                            onClickProperty(IdeWidgetProperty.ID)
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

        // 3. Horizontal Scrollable Row of Rounded Property Cards (all 24 items from screenshots)
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
                displayedProperties.forEach { prop ->
                    SketchPropertySquareCard(
                        label = prop.cardLabel,
                        highlighted = prop == IdeWidgetProperty.SEE_ALL,
                        onClick = { onClickProperty(prop) },
                    ) { tint ->
                        SketchPropertyVectorIcon(property = prop, tint = tint)
                    }
                }
            }
        }
    }
}

@Composable
private fun SketchPropertyVectorIcon(
    property: IdeWidgetProperty,
    tint: Color,
) {
    when (property) {
        IdeWidgetProperty.CUSTOM_ATTRIBUTES -> {
            Canvas(Modifier.size(20.dp)) {
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

        IdeWidgetProperty.CONVERT -> {
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawCircle(tint, 1.3.dp.toPx(), Offset(size.width * 0.20f, size.height * 0.34f))
                drawCircle(tint, 1.3.dp.toPx(), Offset(size.width * 0.34f, size.height * 0.34f))
                drawLine(tint, Offset(size.width * 0.46f, size.height * 0.34f), Offset(size.width * 0.82f, size.height * 0.34f), stroke.width)
                drawLine(tint, Offset(size.width * 0.66f, size.height * 0.18f), Offset(size.width * 0.82f, size.height * 0.34f), stroke.width)
                drawLine(tint, Offset(size.width * 0.66f, size.height * 0.50f), Offset(size.width * 0.82f, size.height * 0.34f), stroke.width)

                drawCircle(tint, 1.3.dp.toPx(), Offset(size.width * 0.80f, size.height * 0.68f))
                drawCircle(tint, 1.3.dp.toPx(), Offset(size.width * 0.66f, size.height * 0.68f))
                drawLine(tint, Offset(size.width * 0.54f, size.height * 0.68f), Offset(size.width * 0.18f, size.height * 0.68f), stroke.width)
                drawLine(tint, Offset(size.width * 0.34f, size.height * 0.52f), Offset(size.width * 0.18f, size.height * 0.68f), stroke.width)
                drawLine(tint, Offset(size.width * 0.34f, size.height * 0.84f), Offset(size.width * 0.18f, size.height * 0.68f), stroke.width)
            }
        }

        IdeWidgetProperty.WIDTH -> {
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.10f, size.height * 0.20f),
                    size = Size(size.width * 0.80f, size.height * 0.60f),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                    style = stroke,
                )
                val cy = size.height * 0.50f
                drawLine(tint, Offset(size.width * 0.24f, cy), Offset(size.width * 0.38f, cy - 3.2f), stroke.width)
                drawLine(tint, Offset(size.width * 0.24f, cy), Offset(size.width * 0.38f, cy + 3.2f), stroke.width)
                drawLine(tint, Offset(size.width * 0.76f, cy), Offset(size.width * 0.62f, cy - 3.2f), stroke.width)
                drawLine(tint, Offset(size.width * 0.76f, cy), Offset(size.width * 0.62f, cy + 3.2f), stroke.width)
            }
        }

        IdeWidgetProperty.SEE_ALL -> {
            Canvas(Modifier.size(20.dp)) {
                val r = 2.dp.toPx()
                val cy = size.height * 0.50f
                drawCircle(tint, r, Offset(size.width * 0.22f, cy))
                drawCircle(tint, r, Offset(size.width * 0.50f, cy))
                drawCircle(tint, r, Offset(size.width * 0.78f, cy))
            }
        }

        IdeWidgetProperty.HEIGHT -> {
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.10f),
                    size = Size(size.width * 0.56f, size.height * 0.80f),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                    style = stroke,
                )
                val cx = size.width * 0.50f
                drawLine(tint, Offset(cx, size.height * 0.25f), Offset(cx - 3.2f, size.height * 0.38f), stroke.width)
                drawLine(tint, Offset(cx, size.height * 0.25f), Offset(cx + 3.2f, size.height * 0.38f), stroke.width)
                drawLine(tint, Offset(cx, size.height * 0.75f), Offset(cx - 3.2f, size.height * 0.62f), stroke.width)
                drawLine(tint, Offset(cx, size.height * 0.75f), Offset(cx + 3.2f, size.height * 0.62f), stroke.width)
            }
        }

        IdeWidgetProperty.PADDING -> {
            // 4 arrows pointing inward toward a center dot (exact match to screenshot)
            Canvas(Modifier.size(20.dp)) {
                val s = 1.6.dp.toPx()
                val cx = size.width * 0.50f
                val cy = size.height * 0.50f
                drawCircle(tint, 1.5.dp.toPx(), Offset(cx, cy))
                // Top arrow down
                drawLine(tint, Offset(cx, size.height * 0.08f), Offset(cx, size.height * 0.34f), s, StrokeCap.Round)
                drawLine(tint, Offset(cx - 3f, size.height * 0.24f), Offset(cx, size.height * 0.34f), s, StrokeCap.Round)
                drawLine(tint, Offset(cx + 3f, size.height * 0.24f), Offset(cx, size.height * 0.34f), s, StrokeCap.Round)
                // Bottom arrow up
                drawLine(tint, Offset(cx, size.height * 0.92f), Offset(cx, size.height * 0.66f), s, StrokeCap.Round)
                drawLine(tint, Offset(cx - 3f, size.height * 0.76f), Offset(cx, size.height * 0.66f), s, StrokeCap.Round)
                drawLine(tint, Offset(cx + 3f, size.height * 0.76f), Offset(cx, size.height * 0.66f), s, StrokeCap.Round)
                // Left arrow right
                drawLine(tint, Offset(size.width * 0.08f, cy), Offset(size.width * 0.34f, cy), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.24f, cy - 3f), Offset(size.width * 0.34f, cy), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.24f, cy + 3f), Offset(size.width * 0.34f, cy), s, StrokeCap.Round)
                // Right arrow left
                drawLine(tint, Offset(size.width * 0.92f, cy), Offset(size.width * 0.66f, cy), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.76f, cy - 3f), Offset(size.width * 0.66f, cy), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.76f, cy + 3f), Offset(size.width * 0.66f, cy), s, StrokeCap.Round)
            }
        }

        IdeWidgetProperty.MARGIN -> {
            // |<->| icon matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val s = 1.7.dp.toPx()
                val cy = size.height * 0.50f
                drawLine(tint, Offset(size.width * 0.14f, size.height * 0.20f), Offset(size.width * 0.14f, size.height * 0.80f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.86f, size.height * 0.20f), Offset(size.width * 0.86f, size.height * 0.80f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.28f, cy), Offset(size.width * 0.72f, cy), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.28f, cy), Offset(size.width * 0.42f, cy - 3.5f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.28f, cy), Offset(size.width * 0.42f, cy + 3.5f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.72f, cy), Offset(size.width * 0.58f, cy - 3.5f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.72f, cy), Offset(size.width * 0.58f, cy + 3.5f), s, StrokeCap.Round)
            }
        }

        IdeWidgetProperty.GRAVITY, IdeWidgetProperty.LAYOUT_GRAVITY -> {
            // 4 corner brackets + center circle matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val s = 1.7.dp.toPx()
                val stroke = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawCircle(tint, radius = size.width * 0.16f, center = Offset(size.width * 0.50f, size.height * 0.50f), style = stroke)
                // Top-left corner
                drawLine(tint, Offset(size.width * 0.16f, size.height * 0.34f), Offset(size.width * 0.16f, size.height * 0.16f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.16f, size.height * 0.16f), Offset(size.width * 0.34f, size.height * 0.16f), s, StrokeCap.Round)
                // Top-right corner
                drawLine(tint, Offset(size.width * 0.66f, size.height * 0.16f), Offset(size.width * 0.84f, size.height * 0.16f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.84f, size.height * 0.16f), Offset(size.width * 0.84f, size.height * 0.34f), s, StrokeCap.Round)
                // Bottom-left corner
                drawLine(tint, Offset(size.width * 0.16f, size.height * 0.66f), Offset(size.width * 0.16f, size.height * 0.84f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.16f, size.height * 0.84f), Offset(size.width * 0.34f, size.height * 0.84f), s, StrokeCap.Round)
                // Bottom-right corner
                drawLine(tint, Offset(size.width * 0.66f, size.height * 0.84f), Offset(size.width * 0.84f, size.height * 0.84f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.84f, size.height * 0.66f), Offset(size.width * 0.84f, size.height * 0.84f), s, StrokeCap.Round)
            }
        }

        IdeWidgetProperty.WEIGHT -> {
            // Split rectangle [ |  ] matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.12f, size.height * 0.22f),
                    size = Size(size.width * 0.76f, size.height * 0.56f),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                    style = stroke,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.42f, size.height * 0.22f),
                    end = Offset(size.width * 0.42f, size.height * 0.78f),
                    strokeWidth = stroke.width,
                )
            }
        }

        IdeWidgetProperty.TEXT -> {
            // I-beam cursor + dotted rectangle matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val s = 1.6.dp.toPx()
                drawLine(tint, Offset(size.width * 0.22f, size.height * 0.20f), Offset(size.width * 0.22f, size.height * 0.80f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.12f, size.height * 0.20f), Offset(size.width * 0.32f, size.height * 0.20f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.12f, size.height * 0.80f), Offset(size.width * 0.32f, size.height * 0.80f), s, StrokeCap.Round)
                val dash = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.36f, size.height * 0.24f),
                    size = Size(size.width * 0.50f, size.height * 0.52f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = 1.4.dp.toPx(), pathEffect = dash),
                )
            }
        }

        IdeWidgetProperty.TEXT_SIZE -> {
            SketchWidgetIcon(type = IdeWidgetType.TEXT_VIEW, tint = tint)
        }

        IdeWidgetProperty.TEXT_STYLE -> {
            // Paint roller / brush icon matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.14f),
                    size = Size(size.width * 0.64f, size.height * 0.34f),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.18f, size.height * 0.32f), Offset(size.width * 0.82f, size.height * 0.32f), stroke.width)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.42f, size.height * 0.52f),
                    size = Size(size.width * 0.16f, size.height * 0.34f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                )
            }
        }

        IdeWidgetProperty.TEXT_COLOR, IdeWidgetProperty.BACKGROUND_COLOR -> {
            // Palette icon matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawCircle(
                    color = tint,
                    radius = size.width * 0.38f,
                    center = Offset(size.width * 0.50f, size.height * 0.50f),
                    style = stroke,
                )
                drawCircle(tint, 1.5.dp.toPx(), Offset(size.width * 0.35f, size.height * 0.38f))
                drawCircle(tint, 1.5.dp.toPx(), Offset(size.width * 0.50f, size.height * 0.30f))
                drawCircle(tint, 1.5.dp.toPx(), Offset(size.width * 0.65f, size.height * 0.40f))
                drawCircle(tint, 2.dp.toPx(), Offset(size.width * 0.60f, size.height * 0.62f))
            }
        }

        IdeWidgetProperty.LINES -> {
            Text(
                text = "123",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tint,
            )
        }

        IdeWidgetProperty.BACKGROUND_RESOURCE -> {
            // Rounded square with 3x3 dots inside (matching screenshot)
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.14f),
                    size = Size(size.width * 0.72f, size.height * 0.72f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = stroke,
                )
                for (r in 0..2) {
                    for (c in 0..2) {
                        drawCircle(
                            color = tint,
                            radius = 1.1.dp.toPx(),
                            center = Offset(
                                x = size.width * (0.32f + c * 0.18f),
                                y = size.height * (0.32f + r * 0.18f),
                            ),
                        )
                    }
                }
            }
        }

        IdeWidgetProperty.ROTATE -> {
            // Rotating diamond arrows matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val s = 1.6.dp.toPx()
                val path = Path().apply {
                    moveTo(size.width * 0.50f, size.height * 0.16f)
                    lineTo(size.width * 0.84f, size.height * 0.50f)
                    lineTo(size.width * 0.50f, size.height * 0.84f)
                    lineTo(size.width * 0.16f, size.height * 0.50f)
                    close()
                }
                drawPath(path, tint, style = Stroke(width = s, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }

        IdeWidgetProperty.ALPHA -> {
            // Droplet icon matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                val drop = Path().apply {
                    moveTo(size.width * 0.50f, size.height * 0.14f)
                    cubicTo(
                        size.width * 0.82f, size.height * 0.48f,
                        size.width * 0.78f, size.height * 0.84f,
                        size.width * 0.50f, size.height * 0.84f,
                    )
                    cubicTo(
                        size.width * 0.22f, size.height * 0.84f,
                        size.width * 0.18f, size.height * 0.48f,
                        size.width * 0.50f, size.height * 0.14f,
                    )
                }
                drawPath(drop, tint, style = stroke)
                drawArc(
                    color = tint,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(size.width * 0.27f, size.height * 0.40f),
                    size = Size(size.width * 0.46f, size.height * 0.42f),
                )
            }
        }

        IdeWidgetProperty.TRANSLATION_X -> {
            // <-.. icon matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val s = 1.7.dp.toPx()
                val cy = size.height * 0.50f
                drawLine(tint, Offset(size.width * 0.16f, cy), Offset(size.width * 0.52f, cy), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.16f, cy), Offset(size.width * 0.30f, cy - 3.5f), s, StrokeCap.Round)
                drawLine(tint, Offset(size.width * 0.16f, cy), Offset(size.width * 0.30f, cy + 3.5f), s, StrokeCap.Round)
                drawCircle(tint, 1.4.dp.toPx(), Offset(size.width * 0.66f, cy))
                drawCircle(tint, 1.4.dp.toPx(), Offset(size.width * 0.80f, cy))
            }
        }

        IdeWidgetProperty.TRANSLATION_Y -> {
            // Vertical dots + down arrow matching screenshot
            Canvas(Modifier.size(20.dp)) {
                val s = 1.7.dp.toPx()
                val cx = size.width * 0.50f
                drawCircle(tint, 1.4.dp.toPx(), Offset(cx, size.height * 0.20f))
                drawCircle(tint, 1.4.dp.toPx(), Offset(cx, size.height * 0.34f))
                drawLine(tint, Offset(cx, size.height * 0.48f), Offset(cx, size.height * 0.84f), s, StrokeCap.Round)
                drawLine(tint, Offset(cx - 3.5f, size.height * 0.70f), Offset(cx, size.height * 0.84f), s, StrokeCap.Round)
                drawLine(tint, Offset(cx + 3.5f, size.height * 0.70f), Offset(cx, size.height * 0.84f), s, StrokeCap.Round)
            }
        }

        IdeWidgetProperty.SCALE_X -> {
            // Filled left half + dotted right half matching screenshot
            Canvas(Modifier.size(20.dp)) {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.18f),
                    size = Size(size.width * 0.38f, size.height * 0.64f),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                )
                val dash = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.48f, size.height * 0.22f),
                    size = Size(size.width * 0.36f, size.height * 0.56f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = 1.4.dp.toPx(), pathEffect = dash),
                )
            }
        }

        IdeWidgetProperty.SCALE_Y -> {
            // Filled bottom half + dotted top half matching screenshot
            Canvas(Modifier.size(20.dp)) {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.48f),
                    size = Size(size.width * 0.64f, size.height * 0.36f),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                )
                val dash = PathEffect.dashPathEffect(floatArrayOf(3f, 3f), 0f)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.16f),
                    size = Size(size.width * 0.56f, size.height * 0.34f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = Stroke(width = 1.4.dp.toPx(), pathEffect = dash),
                )
            }
        }

        IdeWidgetProperty.ID -> {
            Box(
                modifier = Modifier
                    .border(1.5.dp, tint, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "ID",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = tint,
                )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IdeSketchBottomBar(
    activeTab: IdeProjectTab,
    xmlPage: ProjectXmlPage,
    selectedBuildMethod: IdeBuildMethod,
    onOpenPageSelector: () -> Unit,
    onRunClick: () -> Unit,
    onRunLongClick: () -> Unit,
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

        // Right Split Run Button (`▷ Run` + `⌄`)
        // Tap = Run with active build method; Long-press = open Build Method BottomSheet (Gradle / GitHub Actions / Simple)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                modifier = Modifier
                    .height(40.dp)
                    .widthIn(min = 114.dp)
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
                        color = Cyan.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            bottomStart = 20.dp,
                            topEnd = 4.dp,
                            bottomEnd = 4.dp,
                        ),
                    )
                    .combinedClickable(
                        onClick = onRunClick,
                        onLongClick = onRunLongClick,
                    )
                    .padding(horizontal = 12.dp),
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
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.ide_run_button),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 12.5.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color.White,
                    )
                    Text(
                        text = selectedBuildMethod.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 8.5.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Cyan,
                        maxLines = 1,
                    )
                }
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
                    .combinedClickable(
                        onClick = onRunLongClick,
                        onLongClick = onRunLongClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Build Method",
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
private fun IdeWidgetPropertyEditDialog(
    node: IdeWidgetNode,
    property: IdeWidgetProperty,
    onDismiss: () -> Unit,
    onSelectFromSeeAll: (IdeWidgetProperty) -> Unit,
    onUpdateNode: (IdeWidgetNode) -> Unit,
) {
    var textInput by remember(node.uid, property) {
        mutableStateOf(
            when (property) {
                IdeWidgetProperty.CUSTOM_ATTRIBUTES -> node.customAttributes
                IdeWidgetProperty.CONVERT -> node.type.name
                IdeWidgetProperty.WIDTH -> node.layoutWidth
                IdeWidgetProperty.SEE_ALL -> ""
                IdeWidgetProperty.HEIGHT -> node.layoutHeight
                IdeWidgetProperty.PADDING -> node.paddingDp.toString()
                IdeWidgetProperty.MARGIN -> node.marginDp.toString()
                IdeWidgetProperty.GRAVITY -> node.gravity
                IdeWidgetProperty.LAYOUT_GRAVITY -> node.layoutGravity
                IdeWidgetProperty.WEIGHT -> node.weight.toString()
                IdeWidgetProperty.TEXT -> node.text
                IdeWidgetProperty.TEXT_SIZE -> node.textSizeSp.toString()
                IdeWidgetProperty.TEXT_STYLE -> node.textStyle
                IdeWidgetProperty.TEXT_COLOR -> node.textColor
                IdeWidgetProperty.LINES -> node.lines.toString()
                IdeWidgetProperty.BACKGROUND_RESOURCE -> node.backgroundResource
                IdeWidgetProperty.BACKGROUND_COLOR -> node.backgroundColor
                IdeWidgetProperty.ROTATE -> node.rotation.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }
                IdeWidgetProperty.ALPHA -> node.alpha.toString()
                IdeWidgetProperty.TRANSLATION_X -> node.translationX.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }
                IdeWidgetProperty.TRANSLATION_Y -> node.translationY.let { if (it % 1f == 0f) it.toInt().toString() else it.toString() }
                IdeWidgetProperty.SCALE_X -> node.scaleX.toString()
                IdeWidgetProperty.SCALE_Y -> node.scaleY.toString()
                IdeWidgetProperty.ID -> node.viewId
            },
        )
    }

    fun applyAndSave(raw: String = textInput) {
        val trimmed = raw.trim()
        val updated = when (property) {
            IdeWidgetProperty.CUSTOM_ATTRIBUTES -> node.copy(customAttributes = trimmed)
            IdeWidgetProperty.CONVERT -> {
                val chosenType = runCatching { IdeWidgetType.valueOf(trimmed) }.getOrDefault(node.type)
                node.copy(type = chosenType)
            }
            IdeWidgetProperty.WIDTH -> {
                val clean = when {
                    trimmed.equals("match_parent", ignoreCase = true) -> "match_parent"
                    trimmed.equals("wrap_content", ignoreCase = true) -> "wrap_content"
                    trimmed.endsWith("dp", ignoreCase = true) -> trimmed.lowercase()
                    trimmed.toIntOrNull() != null -> "${trimmed}dp"
                    else -> node.layoutWidth
                }
                node.copy(layoutWidth = clean)
            }
            IdeWidgetProperty.SEE_ALL -> node
            IdeWidgetProperty.HEIGHT -> {
                val clean = when {
                    trimmed.equals("match_parent", ignoreCase = true) -> "match_parent"
                    trimmed.equals("wrap_content", ignoreCase = true) -> "wrap_content"
                    trimmed.endsWith("dp", ignoreCase = true) -> trimmed.lowercase()
                    trimmed.toIntOrNull() != null -> "${trimmed}dp"
                    else -> node.layoutHeight
                }
                node.copy(layoutHeight = clean)
            }
            IdeWidgetProperty.PADDING -> {
                val dpVal = trimmed.removeSuffix("dp").trim().toIntOrNull()?.coerceIn(0, 200) ?: node.paddingDp
                node.copy(paddingDp = dpVal)
            }
            IdeWidgetProperty.MARGIN -> {
                val dpVal = trimmed.removeSuffix("dp").trim().toIntOrNull()?.coerceIn(0, 200) ?: node.marginDp
                node.copy(marginDp = dpVal)
            }
            IdeWidgetProperty.GRAVITY -> node.copy(gravity = trimmed.ifBlank { "left|top" })
            IdeWidgetProperty.LAYOUT_GRAVITY -> node.copy(layoutGravity = trimmed.ifBlank { "left|top" })
            IdeWidgetProperty.WEIGHT -> {
                val w = trimmed.toIntOrNull()?.coerceIn(0, 100) ?: node.weight
                node.copy(weight = w)
            }
            IdeWidgetProperty.TEXT -> node.copy(text = raw.ifBlank { "TextView" })
            IdeWidgetProperty.TEXT_SIZE -> {
                val spVal = trimmed.removeSuffix("sp").trim().toIntOrNull()?.coerceIn(8, 96) ?: node.textSizeSp
                node.copy(textSizeSp = spVal)
            }
            IdeWidgetProperty.TEXT_STYLE -> node.copy(textStyle = trimmed.ifBlank { "normal" })
            IdeWidgetProperty.TEXT_COLOR -> {
                val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                node.copy(textColor = if (parseHexColorOrNull(hex) != null) hex.uppercase() else node.textColor)
            }
            IdeWidgetProperty.LINES -> {
                val l = trimmed.toIntOrNull()?.coerceIn(0, 100) ?: node.lines
                node.copy(lines = l)
            }
            IdeWidgetProperty.BACKGROUND_RESOURCE -> node.copy(backgroundResource = trimmed.ifBlank { "none" })
            IdeWidgetProperty.BACKGROUND_COLOR -> {
                val clean = if (trimmed.equals("transparent", ignoreCase = true) || trimmed.isBlank()) {
                    "transparent"
                } else {
                    val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                    if (parseHexColorOrNull(hex) != null) hex.uppercase() else node.backgroundColor
                }
                node.copy(backgroundColor = clean)
            }
            IdeWidgetProperty.ROTATE -> {
                val deg = trimmed.removeSuffix("°").trim().toFloatOrNull()?.coerceIn(-360f, 360f) ?: node.rotation
                node.copy(rotation = deg)
            }
            IdeWidgetProperty.ALPHA -> {
                val a = trimmed.toFloatOrNull()?.coerceIn(0f, 1f) ?: node.alpha
                node.copy(alpha = a)
            }
            IdeWidgetProperty.TRANSLATION_X -> {
                val tx = trimmed.removeSuffix("dp").trim().toFloatOrNull()?.coerceIn(-500f, 500f) ?: node.translationX
                node.copy(translationX = tx)
            }
            IdeWidgetProperty.TRANSLATION_Y -> {
                val ty = trimmed.removeSuffix("dp").trim().toFloatOrNull()?.coerceIn(-500f, 500f) ?: node.translationY
                node.copy(translationY = ty)
            }
            IdeWidgetProperty.SCALE_X -> {
                val sx = trimmed.toFloatOrNull()?.coerceIn(0.1f, 5f) ?: node.scaleX
                node.copy(scaleX = sx)
            }
            IdeWidgetProperty.SCALE_Y -> {
                val sy = trimmed.toFloatOrNull()?.coerceIn(0.1f, 5f) ?: node.scaleY
                node.copy(scaleY = sy)
            }
            IdeWidgetProperty.ID -> {
                val cleanedId = trimmed.removePrefix("@+id/").replace(Regex("[^a-zA-Z0-9_]+"), "_").ifBlank { node.viewId }
                node.copy(viewId = cleanedId)
            }
        }
        onUpdateNode(updated)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Color(0xFF0D1230),
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Cyan.copy(alpha = 0.16f))
                            .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        SketchPropertyVectorIcon(property = property, tint = Cyan)
                    }
                    Column {
                        Text(
                            text = property.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                        Text(
                            text = "${sketchWidgetLabel(node.type)} • @+id/${node.viewId}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.5.sp,
                            ),
                            color = Cyan,
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    when (property) {
                        IdeWidgetProperty.SEE_ALL -> {
                            val allPropsForWidget = IdeWidgetProperty.entries.filter { prop ->
                                prop != IdeWidgetProperty.SEE_ALL &&
                                    (!prop.textViewOnly || node.type == IdeWidgetType.TEXT_VIEW)
                            }
                            allPropsForWidget.forEach { prop ->
                                val valSummary = propertyValueSummary(node, prop)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0x14FFFFFF))
                                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(12.dp))
                                        .clickable { onSelectFromSeeAll(prop) }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        SketchPropertyVectorIcon(property = prop, tint = Cyan)
                                        Text(
                                            text = prop.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontFamily = JetBrainsMono,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = Ink,
                                        )
                                    }
                                    Text(
                                        text = valSummary,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontSize = 11.sp,
                                        ),
                                        color = Cyan,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }

                        IdeWidgetProperty.CONVERT -> {
                            val candidateTypes = if (node.type.isContainer) {
                                listOf(
                                    IdeWidgetType.LINEAR_H,
                                    IdeWidgetType.LINEAR_V,
                                    IdeWidgetType.SCROLL_H,
                                    IdeWidgetType.SCROLL_V,
                                )
                            } else {
                                IdeWidgetType.entries
                            }
                            candidateTypes.forEach { t ->
                                val selected = textInput == t.name
                                DialogOptionRow(
                                    label = t.displayName,
                                    subtitle = if (t.isContainer) "Layout Container" else "View Widget",
                                    selected = selected,
                                    onClick = {
                                        textInput = t.name
                                        applyAndSave(t.name)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.WIDTH, IdeWidgetProperty.HEIGHT -> {
                            val presets = listOf("match_parent", "wrap_content", "48dp", "80dp", "120dp", "160dp", "220dp")
                            presets.forEach { opt ->
                                DialogOptionRow(
                                    label = opt,
                                    selected = textInput.equals(opt, ignoreCase = true),
                                    onClick = {
                                        textInput = opt
                                        applyAndSave(opt)
                                    },
                                )
                            }
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = if (property == IdeWidgetProperty.WIDTH) "Custom Width (e.g. 140dp)" else "Custom Height (e.g. 90dp)",
                            )
                        }

                        IdeWidgetProperty.PADDING, IdeWidgetProperty.MARGIN -> {
                            val presets = listOf("0", "4", "8", "12", "16", "24", "32")
                            DialogPresetChipsGrid(
                                presets = presets,
                                selectedValue = textInput,
                                suffix = "dp",
                                onSelect = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "${property.title} in dp",
                            )
                        }

                        IdeWidgetProperty.GRAVITY, IdeWidgetProperty.LAYOUT_GRAVITY -> {
                            val gravities = listOf(
                                "left|top",
                                "center",
                                "center_horizontal",
                                "center_vertical",
                                "left|center_vertical",
                                "right|center_vertical",
                                "right|top",
                                "left|bottom",
                                "right|bottom",
                            )
                            gravities.forEach { g ->
                                DialogOptionRow(
                                    label = g,
                                    selected = textInput == g,
                                    onClick = {
                                        textInput = g
                                        applyAndSave(g)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.WEIGHT -> {
                            val presets = listOf("0", "1", "2", "3", "4", "5")
                            DialogPresetChipsGrid(
                                presets = presets,
                                selectedValue = textInput,
                                suffix = "",
                                onSelect = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:layout_weight",
                            )
                        }

                        IdeWidgetProperty.TEXT -> {
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:text",
                                singleLine = false,
                            )
                        }

                        IdeWidgetProperty.TEXT_SIZE -> {
                            val sizes = listOf("10", "12", "14", "16", "18", "20", "24", "30", "36")
                            DialogPresetChipsGrid(
                                presets = sizes,
                                selectedValue = textInput,
                                suffix = "sp",
                                onSelect = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:textSize (sp)",
                            )
                        }

                        IdeWidgetProperty.TEXT_STYLE -> {
                            val styles = listOf("normal", "bold", "italic", "bold|italic")
                            styles.forEach { st ->
                                DialogOptionRow(
                                    label = st,
                                    selected = textInput == st,
                                    onClick = {
                                        textInput = st
                                        applyAndSave(st)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.TEXT_COLOR -> {
                            val colors = listOf(
                                "#000000",
                                "#1E293B",
                                "#E11D48",
                                "#2563EB",
                                "#0284C7",
                                "#16A34A",
                                "#D97706",
                                "#7C3AED",
                                "#DB2777",
                                "#FFFFFF",
                            )
                            DialogColorSwatches(
                                colors = colors,
                                selectedHex = textInput,
                                onSelectHex = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "Hex Color (#RRGGBB)",
                            )
                        }

                        IdeWidgetProperty.LINES -> {
                            val linePresets = listOf("0", "1", "2", "3", "4", "5", "8")
                            DialogPresetChipsGrid(
                                presets = linePresets,
                                selectedValue = textInput,
                                suffix = "",
                                onSelect = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:lines (0 = default)",
                            )
                        }

                        IdeWidgetProperty.BACKGROUND_RESOURCE -> {
                            val resList = listOf(
                                "none",
                                "@drawable/rounded_card",
                                "@drawable/outline_border",
                                "@drawable/pill_button",
                                "@android:drawable/dialog_holo_light_frame",
                            )
                            resList.forEach { res ->
                                DialogOptionRow(
                                    label = res,
                                    selected = textInput == res,
                                    onClick = {
                                        textInput = res
                                        applyAndSave(res)
                                    },
                                )
                            }
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:background (@drawable/...)",
                            )
                        }

                        IdeWidgetProperty.BACKGROUND_COLOR -> {
                            val bgColors = listOf(
                                "transparent",
                                "#FFFFFF",
                                "#F1F5F9",
                                "#DBEAFE",
                                "#DCFCE7",
                                "#FEF3C7",
                                "#FEE2E2",
                                "#F3E8FF",
                                "#E0F2FE",
                                "#1E293B",
                            )
                            DialogColorSwatches(
                                colors = bgColors,
                                selectedHex = textInput,
                                onSelectHex = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "Background Color (#RRGGBB or transparent)",
                            )
                        }

                        IdeWidgetProperty.ROTATE -> {
                            val angles = listOf("0", "45", "90", "180", "270", "-45", "-90")
                            DialogPresetChipsGrid(
                                presets = angles,
                                selectedValue = textInput,
                                suffix = "°",
                                onSelect = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:rotation (degrees)",
                            )
                        }

                        IdeWidgetProperty.ALPHA -> {
                            val alphas = listOf("1.0", "0.85", "0.7", "0.5", "0.3", "0.1")
                            DialogPresetChipsGrid(
                                presets = alphas,
                                selectedValue = textInput,
                                suffix = "",
                                onSelect = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:alpha (0.0 - 1.0)",
                            )
                        }

                        IdeWidgetProperty.TRANSLATION_X, IdeWidgetProperty.TRANSLATION_Y -> {
                            val offsets = listOf("-40", "-20", "-10", "0", "10", "20", "40")
                            DialogPresetChipsGrid(
                                presets = offsets,
                                selectedValue = textInput,
                                suffix = "dp",
                                onSelect = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:${property.title.replaceFirstChar { it.lowercase() }} (dp)",
                            )
                        }

                        IdeWidgetProperty.SCALE_X, IdeWidgetProperty.SCALE_Y -> {
                            val scales = listOf("0.5", "0.75", "1.0", "1.25", "1.5", "2.0")
                            DialogPresetChipsGrid(
                                presets = scales,
                                selectedValue = textInput,
                                suffix = "x",
                                onSelect = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:${property.title.replaceFirstChar { it.lowercase() }}",
                            )
                        }

                        IdeWidgetProperty.CUSTOM_ATTRIBUTES -> {
                            val sampleAttrs = listOf(
                                """android:visibility="visible"""",
                                """android:clickable="true"""",
                                """android:elevation="4dp"""",
                            )
                            sampleAttrs.forEach { sample ->
                                DialogOptionRow(
                                    label = sample,
                                    selected = textInput.contains(sample),
                                    onClick = {
                                        textInput = if (textInput.isBlank()) sample else "$textInput\n$sample"
                                    },
                                )
                            }
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "Custom XML attributes",
                                singleLine = false,
                            )
                        }

                        IdeWidgetProperty.ID -> {
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:id (@+id/...)",
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (property != IdeWidgetProperty.SEE_ALL) {
                    TextButton(onClick = { applyAndSave() }) {
                        Text(stringResource(R.string.dialog_save), color = Cyan)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dialog_cancel), color = Muted)
                }
            },
        )
    }
}

private fun propertyValueSummary(node: IdeWidgetNode, prop: IdeWidgetProperty): String = when (prop) {
    IdeWidgetProperty.CUSTOM_ATTRIBUTES -> if (node.customAttributes.isBlank()) "none" else "custom"
    IdeWidgetProperty.CONVERT -> node.type.displayName
    IdeWidgetProperty.WIDTH -> node.layoutWidth
    IdeWidgetProperty.SEE_ALL -> ""
    IdeWidgetProperty.HEIGHT -> node.layoutHeight
    IdeWidgetProperty.PADDING -> "${node.paddingDp}dp"
    IdeWidgetProperty.MARGIN -> "${node.marginDp}dp"
    IdeWidgetProperty.GRAVITY -> node.gravity
    IdeWidgetProperty.LAYOUT_GRAVITY -> node.layoutGravity
    IdeWidgetProperty.WEIGHT -> node.weight.toString()
    IdeWidgetProperty.TEXT -> node.text
    IdeWidgetProperty.TEXT_SIZE -> "${node.textSizeSp}sp"
    IdeWidgetProperty.TEXT_STYLE -> node.textStyle
    IdeWidgetProperty.TEXT_COLOR -> node.textColor
    IdeWidgetProperty.LINES -> if (node.lines <= 0) "default" else node.lines.toString()
    IdeWidgetProperty.BACKGROUND_RESOURCE -> node.backgroundResource
    IdeWidgetProperty.BACKGROUND_COLOR -> node.backgroundColor
    IdeWidgetProperty.ROTATE -> "${node.rotation.toInt()}°"
    IdeWidgetProperty.ALPHA -> node.alpha.toString()
    IdeWidgetProperty.TRANSLATION_X -> "${node.translationX.toInt()}dp"
    IdeWidgetProperty.TRANSLATION_Y -> "${node.translationY.toInt()}dp"
    IdeWidgetProperty.SCALE_X -> "${node.scaleX}x"
    IdeWidgetProperty.SCALE_Y -> "${node.scaleY}x"
    IdeWidgetProperty.ID -> node.viewId
}

@Composable
private fun DialogOptionRow(
    label: String,
    subtitle: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
) {
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
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 12.5.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                ),
                color = if (selected) Cyan else Ink,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Muted,
                )
            }
        }
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

@Composable
private fun DialogPresetChipsGrid(
    presets: List<String>,
    selectedValue: String,
    suffix: String,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        presets.forEach { preset ->
            val selected = selectedValue.trim() == preset
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Cyan.copy(alpha = 0.22f) else Color(0x16FFFFFF))
                    .border(
                        width = 1.dp,
                        color = if (selected) Cyan else Color(0x2EFFFFFF),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clickable { onSelect(preset) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$preset$suffix",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (selected) Cyan else Ink,
                )
            }
        }
    }
}

@Composable
private fun DialogColorSwatches(
    colors: List<String>,
    selectedHex: String,
    onSelectHex: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        colors.forEach { hex ->
            val selected = selectedHex.equals(hex, ignoreCase = true)
            val swatchColor = parseHexColorOrNull(hex) ?: Color.Transparent
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) Cyan.copy(alpha = 0.20f) else Color(0x16FFFFFF))
                    .border(
                        width = 1.dp,
                        color = if (selected) Cyan else Color(0x2EFFFFFF),
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clickable { onSelectHex(hex) }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(swatchColor)
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                )
                Text(
                    text = hex,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.5.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (selected) Cyan else Ink,
                )
            }
        }
    }
}

@Composable
private fun DialogTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        label = { Text(label) },
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

@Composable
private fun TemporaryRunScreen(
    project: WorkspaceItem,
    activeXmlPage: ProjectXmlPage,
    widgetNodes: List<IdeWidgetNode>,
    buildMethod: IdeBuildMethod,
    onChangeBuildMethod: (IdeBuildMethod) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val langAccent = languageColor(project.language)
    val methodAccent = buildMethod.badgeColor

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
                    text = "Running via ${buildMethod.title} • ${buildMethod.badge}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = methodAccent,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Build Method Status Banner (clearly tells the user which build method is active)
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0E1636))
                    .border(1.dp, methodAccent.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(methodAccent),
                        )
                        Text(
                            text = "Build Method: ${buildMethod.title}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(methodAccent.copy(alpha = 0.16f))
                            .border(1.dp, methodAccent.copy(alpha = 0.45f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = buildMethod.badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = methodAccent,
                        )
                    }
                }

                Text(
                    text = buildMethod.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                    ),
                    color = Muted,
                )

                // Quick switcher pills inside Run screen as well
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    IdeBuildMethod.entries.forEach { method ->
                        val active = method == buildMethod
                        val chipColor = method.badgeColor
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) chipColor.copy(alpha = 0.20f) else Color(0x14FFFFFF))
                                .border(
                                    width = 1.dp,
                                    color = if (active) chipColor else Color(0x26FFFFFF),
                                    shape = RoundedCornerShape(10.dp),
                                )
                                .clickable { onChangeBuildMethod(method) }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = method.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 10.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = if (active) chipColor else Ink.copy(alpha = 0.78f),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

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
                        text = "${stringResource(R.string.ide_temp_run_title)} (${buildMethod.title})",
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
                                text = "${activeXmlPage.activityName}  •  ${activeXmlPage.xmlFileName}  •  [${buildMethod.title}]",
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
                                .heightIn(min = 180.dp, max = 300.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Top,
                        ) {
                            if (widgetNodes.isEmpty()) {
                                Text(
                                    text = "${project.name} - ${activeXmlPage.activityName} (${buildMethod.title})",
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
// 5. Build Method Bottom Sheet (Opened on Long-Press of Run Button)
//    Options: Gradle | GitHub Actions | Simple
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuildMethodBottomSheet(
    selectedMethod: IdeBuildMethod,
    onSelectMethod: (IdeBuildMethod) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0B1028),
        contentColor = Ink,
        scrimColor = Color(0xD903060F),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
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
                            text = "Select Build Method",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                        Text(
                            text = "Choose how Run compiles & builds your Android project",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = Muted,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Cyan.copy(alpha = 0.14f))
                            .border(1.dp, Cyan.copy(alpha = 0.40f), RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = selectedMethod.title,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Cyan,
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                IdeBuildMethod.entries.forEach { method ->
                    val isSelected = method == selectedMethod
                    val methodAccent = method.badgeColor

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) methodAccent.copy(alpha = 0.15f) else Color(0xFF131B3A),
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) methodAccent else Color(0x26FFFFFF),
                                shape = RoundedCornerShape(16.dp),
                            )
                            .clickable { onSelectMethod(method) }
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        // Method Badge Icon
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(methodAccent.copy(alpha = 0.18f))
                                .border(1.dp, methodAccent.copy(alpha = 0.45f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = methodAccent,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = method.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = if (isSelected) methodAccent else Ink,
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x24FFFFFF))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = method.badge,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontSize = 9.sp,
                                        ),
                                        color = Ink.copy(alpha = 0.85f),
                                    )
                                }
                            }
                            Text(
                                text = method.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                ),
                                color = Muted,
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(methodAccent),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF050816),
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

// ============================================================================
// 6. Side Menu Drawer (Opened via 3-Dots Icon in Top Bar)
//    Contains all 16 requested Managers & Tools
// ============================================================================

@Composable
private fun IdeProjectSideDrawer(
    visible: Boolean,
    project: WorkspaceItem,
    onDismiss: () -> Unit,
    onSelectItem: (IdeSideMenuItem) -> Unit,
) {
    if (!visible) return

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Scrim background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xB803060F))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )

        // Right-side Drawer Panel
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(304.dp)
                .shadow(20.dp, RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0E1636), Color(0xFF090E24)),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = Cyan.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                ),
        ) {
            // Drawer Header with Project Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131D46))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProjectIconView(
                    iconPreset = project.iconPreset,
                    customIconPath = project.customIconPath,
                    modifier = Modifier.size(38.dp),
                    cornerRadius = 11.dp,
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Project Managers & Tools (${IdeSideMenuItem.entries.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                        ),
                        color = Cyan,
                        maxLines = 1,
                    )
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color(0x26FFFFFF)),
            )

            // Scrollable List of all 16 Side Menu Managers
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                IdeSideMenuItem.entries.forEach { item ->
                    val accent = item.accentColor
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color(0xFF141E42))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(13.dp))
                            .clickable { onSelectItem(item) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(11.dp),
                    ) {
                        // Badge / Icon Box
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(accent.copy(alpha = 0.16f))
                                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(9.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = item.badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = accent,
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(1.dp),
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = Ink,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = item.subtitle,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                ),
                                color = Muted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdeSideMenuManagerDialog(
    project: WorkspaceItem,
    menuItem: IdeSideMenuItem,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1230),
        titleContentColor = Ink,
        textContentColor = Ink,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Cyan.copy(alpha = 0.18f))
                        .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = menuItem.badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Cyan,
                    )
                }
                Text(
                    text = menuItem.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = menuItem.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x14FFFFFF))
                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "Project: ${project.name}\nPackage: ${project.packageName}\nTool: ${menuItem.title}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 11.5.sp,
                        ),
                        color = Cyan,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ide_temp_run_close), color = Cyan)
            }
        },
    )
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
