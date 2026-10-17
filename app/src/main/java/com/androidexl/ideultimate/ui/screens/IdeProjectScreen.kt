package com.androidexl.ideultimate.ui.screens

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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
import com.androidexl.ideultimate.data.IdeFunctionParam
import com.androidexl.ideultimate.data.IdeLogicItem
import com.androidexl.ideultimate.data.IdeLogicSection
import com.androidexl.ideultimate.data.IdeWidgetCategory
import com.androidexl.ideultimate.data.IdeWidgetNode
import com.androidexl.ideultimate.data.IdeWidgetType
import com.androidexl.ideultimate.data.ProjectImageItem
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
    IMAGE_MANAGER(
        title = "Image Manager",
        subtitle = "استيراد وإدارة صور المشروع داخل res/drawable",
        badge = "IMG",
        accentColor = Color(0xFF38BDF8),
    ),
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

    var logicItems by remember(project.id, selectedXmlPage.xmlFileName) {
        mutableStateOf(repository.loadLogicItemsForXmlPage(project, selectedXmlPage))
    }

    LaunchedEffect(project.id, selectedXmlPage.xmlFileName, activeFlavor, activeTab) {
        widgetNodes = repository.loadWidgetsForXmlPage(project, selectedXmlPage)
        logicItems = repository.loadLogicItemsForXmlPage(project, selectedXmlPage)
        codeText = repository.readCodeForFlavor(project, selectedXmlPage, activeFlavor)
    }

    fun updateLogicItems(newItems: List<IdeLogicItem>) {
        logicItems = newItems
        repository.saveLogicItemsForXmlPage(project, selectedXmlPage, newItems)
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
    var showImageManagerScreen by rememberSaveable { mutableStateOf(false) }
    var activeSideMenuItem by remember { mutableStateOf<IdeSideMenuItem?>(null) }

    var projectImages by remember(project.id) {
        mutableStateOf(repository.getProjectImages(project))
    }
    fun refreshProjectImages() {
        projectImages = repository.getProjectImages(project)
    }

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
    var isDraggingExistingWidget by remember { mutableStateOf(false) }
    var isHoveringTrashZone by remember { mutableStateOf(false) }
    var bottomTrashBounds by remember { mutableStateOf(Rect.Zero) }

    var editingPropertyRequest by remember { mutableStateOf<Pair<String, IdeWidgetProperty>?>(null) }
    var imagePickerWidgetUid by remember { mutableStateOf<String?>(null) }
    val recentProperties = remember { mutableStateListOf<IdeWidgetProperty>() }

    val selectedNode = remember(widgetNodes, selectedWidgetUid) {
        selectedWidgetUid?.let { findNodeByUid(widgetNodes, it) }
    }

    fun openPropertyDialog(node: IdeWidgetNode, prop: IdeWidgetProperty) {
        recentProperties.remove(prop)
        recentProperties.add(0, prop)
        if (prop == IdeWidgetProperty.IMAGE) {
            refreshProjectImages()
            imagePickerWidgetUid = node.uid
        } else {
            editingPropertyRequest = node.uid to prop
        }
    }

    BackHandler {
        when {
            imagePickerWidgetUid != null -> imagePickerWidgetUid = null
            editingPropertyRequest != null -> editingPropertyRequest = null
            showImageManagerScreen -> showImageManagerScreen = false
            activeSideMenuItem != null -> activeSideMenuItem = null
            showSideMenuDrawer -> showSideMenuDrawer = false
            showBuildMethodSheet -> showBuildMethodSheet = false
            showTempRunScreen -> showTempRunScreen = false
            selectedWidgetUid != null -> selectedWidgetUid = null
            else -> onBack()
        }
    }

    if (showImageManagerScreen) {
        IdeImageManagerScreen(
            project = project,
            images = projectImages,
            onImportUris = { uris ->
                uris.forEach { uri ->
                    repository.importProjectImage(project, uri)
                }
                refreshProjectImages()
            },
            onRenameImage = { oldName, newName ->
                val renamed = repository.renameProjectImage(project, oldName, newName)
                refreshProjectImages()
                if (renamed != null && renamed != oldName) {
                    updateWidgetNodes(renameImageResInTree(widgetNodes, oldName, renamed), recordHistory = false)
                }
            },
            onDeleteImages = { namesToDelete ->
                repository.deleteProjectImages(project, namesToDelete)
                refreshProjectImages()
            },
            onBack = { showImageManagerScreen = false },
            modifier = modifier,
        )
        return
    }

    if (showTempRunScreen) {
        TemporaryRunScreen(
            project = project,
            activeXmlPage = selectedXmlPage,
            widgetNodes = widgetNodes,
            projectImages = projectImages,
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
                                projectImages = projectImages,
                                selectedWidgetUid = selectedWidgetUid,
                                bottomTrashBounds = bottomTrashBounds,
                                onSelectWidgetUid = { selectedWidgetUid = it },
                                onDraggingStateChange = { draggingAny, draggingExisting, hoveringTrash ->
                                    isDraggingWidget = draggingAny
                                    isDraggingExistingWidget = draggingExisting
                                    isHoveringTrashZone = hoveringTrash
                                    if (draggingAny || draggingExisting) {
                                        editingPropertyRequest = null
                                        imagePickerWidgetUid = null
                                    }
                                },
                                onRequestEditWidgetPrimary = { node ->
                                    when {
                                        node.type.isContainer ->
                                            openPropertyDialog(node, IdeWidgetProperty.ORIENTATION)
                                        node.type.isImageWidget || node.type == IdeWidgetType.FAB_BUTTON ->
                                            openPropertyDialog(node, IdeWidgetProperty.IMAGE)
                                        node.type.isWebWidget ->
                                            openPropertyDialog(node, IdeWidgetProperty.WEB_URL)
                                        node.type.isListWidget ->
                                            openPropertyDialog(node, IdeWidgetProperty.CUSTOM_VIEW)
                                        node.type == IdeWidgetType.SPINNER ->
                                            openPropertyDialog(node, IdeWidgetProperty.SPINNER_ENTRIES)
                                        node.type == IdeWidgetType.TIME_PICKER ||
                                            node.type == IdeWidgetType.DATE_PICKER ||
                                            node.type == IdeWidgetType.DATE_RANGE_PICKER ->
                                            openPropertyDialog(node, IdeWidgetProperty.PICKER_MODE)
                                        node.type == IdeWidgetType.CALENDAR_VIEW ->
                                            openPropertyDialog(node, IdeWidgetProperty.FIRST_DAY_OF_WEEK)
                                        node.type == IdeWidgetType.ANALOG_CLOCK ->
                                            openPropertyDialog(node, IdeWidgetProperty.TIME_ZONE)
                                        node.type.isInputTextWidget ->
                                            openPropertyDialog(node, IdeWidgetProperty.HINT)
                                        node.type.hasTextLabel ->
                                            openPropertyDialog(node, IdeWidgetProperty.TEXT)
                                        else ->
                                            openPropertyDialog(node, IdeWidgetProperty.ID)
                                    }
                                },
                                onUpdateWidgets = { updateWidgetNodes(it) },
                            )
                        }

                        IdeProjectTab.LOGIC -> {
                            IdeLogicEventWorkspace(
                                project = project,
                                xmlPage = selectedXmlPage,
                                widgetNodes = widgetNodes,
                                logicItems = logicItems,
                                onUpdateLogicItems = { updateLogicItems(it) },
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

                // 4. Base Bottom Bar stays at a constant height in the Column so the View Canvas
                // NEVER resizes or jumps when the Property Panel or Trash Drop Bar appears/disappears!
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

            // 4B. Overlay Bottom Panels (Trash Drop Zone during long-press drag, or Property Panel when selected)
            // Overlaid at Alignment.BottomCenter so opening/closing NEVER shifts canvas coordinates during long-press!
            if (activeTab == IdeProjectTab.VIEW && isDraggingExistingWidget) {
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    IdeBottomTrashDropBar(
                        hovered = isHoveringTrashZone,
                        bottomBarPadding = bottomBarPadding,
                        onTrashBoundsChanged = { rect -> bottomTrashBounds = rect },
                    )
                }
            } else if (activeTab == IdeProjectTab.VIEW && selectedNode != null && !isDraggingWidget) {
                val parentContainerNode = remember(widgetNodes, selectedNode.uid) {
                    findParentNodeOf(widgetNodes, selectedNode.uid)
                }
                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    IdeWidgetPropertyBottomPanel(
                        node = selectedNode,
                        parentNode = parentContainerNode,
                        allWidgets = flattenAllNodes(widgetNodes),
                        recentProperties = recentProperties,
                        existingViewLogicItems = logicItems.filter { it.section == IdeLogicSection.VIEW },
                        onSelectOtherWidget = { selectedWidgetUid = it.uid },
                        onSelectParentLayout = {
                            if (parentContainerNode != null) {
                                selectedWidgetUid = parentContainerNode.uid
                            } else {
                                updateWidgetNodes(moveNodeInParent(widgetNodes, selectedNode.uid, direction = -1))
                            }
                        },
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
                        onToggleWidgetEvent = { eventOpt ->
                            val eventName = "${selectedNode.viewId}: ${eventOpt.eventKey}"
                            val existing = logicItems.firstOrNull {
                                it.section == IdeLogicSection.VIEW &&
                                    it.targetViewId == selectedNode.viewId &&
                                    it.name == eventName
                            }
                            if (existing != null) {
                                updateLogicItems(logicItems.filterNot { it.id == existing.id })
                            } else {
                                val newItem = IdeLogicItem(
                                    section = IdeLogicSection.VIEW,
                                    name = eventName,
                                    subtitle = "${selectedNode.type.displayName} (@+id/${selectedNode.viewId}) • ${eventOpt.subtitle}",
                                    targetViewId = selectedNode.viewId,
                                    bodyCode = "// ${selectedNode.viewId} ${eventOpt.eventKey}\n",
                                )
                                updateLogicItems(logicItems + newItem)
                            }
                        },
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
                    if (item == IdeSideMenuItem.IMAGE_MANAGER) {
                        refreshProjectImages()
                        showImageManagerScreen = true
                    } else {
                        activeSideMenuItem = item
                    }
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

    val pickerTargetUid = imagePickerWidgetUid
    if (pickerTargetUid != null) {
        val targetNode = findNodeByUid(widgetNodes, pickerTargetUid)
        if (targetNode != null) {
            ImageViewSelectImageBottomSheet(
                node = targetNode,
                projectImages = projectImages,
                onSelectImageRes = { chosenName ->
                    updateWidgetNodes(
                        updateNodeByUid(widgetNodes, targetNode.uid) {
                            it.copy(imageResName = chosenName)
                        },
                    )
                    imagePickerWidgetUid = null
                },
                onOpenImageManager = {
                    imagePickerWidgetUid = null
                    refreshProjectImages()
                    showImageManagerScreen = true
                },
                onDismiss = { imagePickerWidgetUid = null },
            )
        } else {
            imagePickerWidgetUid = null
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
    projectImages: List<ProjectImageItem>,
    selectedWidgetUid: String?,
    bottomTrashBounds: Rect,
    onSelectWidgetUid: (String?) -> Unit,
    onDraggingStateChange: (Boolean, Boolean, Boolean) -> Unit,
    onRequestEditWidgetPrimary: (IdeWidgetNode) -> Unit,
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
    var showAddWidgetBottomSheet by remember { mutableStateOf(false) }

    val currentWidgetNodes by rememberUpdatedState(widgetNodes)
    val currentSelectedWidgetUid by rememberUpdatedState(selectedWidgetUid)
    val currentBottomTrashBounds by rememberUpdatedState(bottomTrashBounds)
    val currentOnSelectWidgetUid by rememberUpdatedState(onSelectWidgetUid)
    val currentOnUpdateWidgets by rememberUpdatedState(onUpdateWidgets)

    fun evalIsOverTrashZone(): Boolean {
        if (draggingExistingNode == null || draggingType == null) return false
        val trashRect = currentBottomTrashBounds
        if (!trashRect.isEmpty && (trashRect.inflate(24f).contains(dragFingerInRoot) || dragFingerInRoot.y >= trashRect.top - 18f)) {
            return true
        }
        if (!rootBounds.isEmpty && dragFingerInRoot.y >= rootBounds.bottom - with(density) { 36.dp.toPx() }) {
            return true
        }
        return false
    }

    val isOverTrashZone = evalIsOverTrashZone()

    LaunchedEffect(draggingType, draggingExistingNode, isOverTrashZone) {
        onDraggingStateChange(
            draggingType != null,
            draggingExistingNode != null,
            isOverTrashZone,
        )
    }

    // Live evaluation functions that always read current MutableState values (never stale!)
    fun evalIsDragOverPhone(): Boolean {
        if (draggingType == null || evalIsOverTrashZone()) return false
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
                    rect.inflate(10f).contains(dragFingerInRoot)
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
            // Scroll(V) is horizontal linear (Row) & Scroll(H) is vertical linear (Column)
            val isHorizontal = containerNode?.type == IdeWidgetType.LINEAR_H ||
                containerNode?.type == IdeWidgetType.SCROLL_V
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

    fun createDefaultWidgetNode(type: IdeWidgetType, customViewId: String? = null): IdeWidgetNode {
        val defaultText = when (type) {
            IdeWidgetType.BUTTON -> "Button"
            IdeWidgetType.MATERIAL_BUTTON -> "MaterialButton"
            IdeWidgetType.OUTLINED_BUTTON -> "Outlined"
            IdeWidgetType.TEXT_BUTTON -> "TextButton"
            IdeWidgetType.FAB_BUTTON -> "+"
            IdeWidgetType.EXTENDED_FAB -> "+ Action"
            IdeWidgetType.TOGGLE_BUTTON -> "Toggle"
            IdeWidgetType.CHIP_BUTTON -> "Chip"
            IdeWidgetType.CHECK_BOX -> "CheckBox"
            IdeWidgetType.RADIO_BUTTON -> "RadioButton"
            IdeWidgetType.SWITCH -> "Switch"
            IdeWidgetType.SPINNER -> "Spinner"
            IdeWidgetType.DIGITAL_CLOCK,
            IdeWidgetType.TEXT_CLOCK -> "12:30:45 PM"
            IdeWidgetType.CHRONOMETER,
            IdeWidgetType.STOPWATCH_VIEW -> "00:00"
            IdeWidgetType.COUNTDOWN_VIEW -> "05:00"
            IdeWidgetType.WORLD_CLOCK_VIEW -> "Cairo 12:30 PM"
            else -> "TextView"
        }
        val cleanedCustomId = customViewId
            ?.trim()
            ?.removePrefix("@+id/")
            ?.replace(Regex("[^a-zA-Z0-9_]+"), "_")
            ?.takeIf { it.isNotEmpty() }
        return IdeWidgetNode(
            viewId = cleanedCustomId ?: nextViewIdFor(type),
            type = type,
            text = defaultText,
            layoutWidth = when {
                type.isContainer -> "match_parent"
                type.isWebWidget -> "match_parent"
                type.isListWidget -> "match_parent"
                type == IdeWidgetType.CALENDAR_VIEW ||
                    type == IdeWidgetType.DATE_PICKER ||
                    type == IdeWidgetType.TIME_PICKER ||
                    type == IdeWidgetType.DATE_RANGE_PICKER -> "match_parent"
                else -> "wrap_content"
            },
            layoutHeight = when {
                type.isWebWidget -> "120dp"
                type.isListWidget -> "130dp"
                else -> "wrap_content"
            },
        )
    }

    fun commitDrop(type: IdeWidgetType) {
        val existingMoving = draggingExistingNode
        val nodes = currentWidgetNodes

        // If user dragged an existing widget into the bottom Trash zone, delete it!
        if (existingMoving != null && evalIsOverTrashZone()) {
            currentOnUpdateWidgets(removeNodeByUid(nodes, existingMoving.uid))
            currentOnSelectWidgetUid(null)
            draggingType = null
            draggingExistingNode = null
            return
        }

        val overPhone = evalIsDragOverPhone()
        if (!overPhone) {
            draggingType = null
            draggingExistingNode = null
            return
        }

        val targetContainer = evalHoveredContainerUid()
        val insertIdx = evalDropInsertIndex(targetContainer)

        val nodeToInsert = existingMoving ?: createDefaultWidgetNode(type)

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
                    if (droppedType != null && (evalIsDragOverPhone() || evalIsOverTrashZone())) {
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
                    val newNode = createDefaultWidgetNode(type)
                    val updated = if (targetContainer != null) {
                        insertChildIntoContainer(nodes, targetContainer, newNode)
                    } else {
                        nodes + newNode
                    }
                    currentOnUpdateWidgets(updated)
                    currentOnSelectWidgetUid(newNode.uid)
                },
                onOpenAddWidgetSheet = {
                    showAddWidgetBottomSheet = true
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
                    projectImages = projectImages,
                    selectedWidgetUid = selectedWidgetUid,
                    draggingType = draggingType,
                    draggingExistingUid = draggingExistingNode?.uid,
                    isDragOverPhone = isDragOverPhone,
                    hoveredContainerUid = hoveredContainerUid,
                    dropInsertIndex = dropInsertIndex,
                    onSelectWidget = { node -> currentOnSelectWidgetUid(node.uid) },
                    onDoubleTapWidget = { node -> onRequestEditWidgetPrimary(node) },
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
                        if (droppedType != null && (evalIsDragOverPhone() || evalIsOverTrashZone())) {
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
            val pillBorderColor = if (isOverTrashZone) Color(0xFFF43F5E) else Cyan.copy(alpha = 0.65f)
            val pillBgColor = if (isOverTrashZone) Color(0xFF4C0519) else Color(0xFF1A2448)
            Row(
                modifier = Modifier
                    .offset { IntOffset(offsetX, offsetY) }
                    .width(108.dp)
                    .height(28.dp)
                    .graphicsLayer(alpha = 0.72f)
                    .shadow(4.dp, RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp))
                    .background(pillBgColor)
                    .border(1.dp, pillBorderColor, RoundedCornerShape(6.dp))
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

        if (showAddWidgetBottomSheet) {
            val containerList = remember(widgetNodes) {
                flattenAllNodes(widgetNodes).filter { it.type.isContainer }
            }
            val initialContainerUid = remember(selectedWidgetUid, widgetNodes) {
                selectedWidgetUid?.let { uid ->
                    val n = findNodeByUid(widgetNodes, uid)
                    if (n?.type?.isContainer == true) n.uid else findParentNodeOf(widgetNodes, uid)?.uid
                }
            }
            AddWidgetModalBottomSheet(
                containers = containerList,
                initialParentContainerUid = initialContainerUid,
                suggestViewIdForType = { type -> nextViewIdFor(type) },
                onDismiss = { showAddWidgetBottomSheet = false },
                onConfirmAdd = { chosenType, customId, parentContainerUid ->
                    val nodes = currentWidgetNodes
                    val newNode = createDefaultWidgetNode(chosenType, customId)
                    val updated = if (parentContainerUid != null) {
                        insertChildIntoContainer(nodes, parentContainerUid, newNode)
                    } else {
                        nodes + newNode
                    }
                    currentOnUpdateWidgets(updated)
                    currentOnSelectWidgetUid(newNode.uid)
                    showAddWidgetBottomSheet = false
                },
            )
        }
    }
}

@Composable
private fun IdeBottomTrashDropBar(
    hovered: Boolean,
    bottomBarPadding: Dp = 0.dp,
    onTrashBoundsChanged: (Rect) -> Unit,
) {
    val bgColor = if (hovered) Color(0xFF881337) else Color(0xFF2A0E1D)
    val borderColor = if (hovered) Color(0xFFFB7185) else Color(0xFFF43F5E).copy(alpha = 0.6f)
    val contentTint = if (hovered) Color.White else Color(0xFFFDA4AF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                onTrashBoundsChanged(coords.boundsInRoot())
            }
            .background(bgColor)
            .border(width = 1.5.dp, color = borderColor)
            .navigationBarsPadding()
            .padding(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 12.dp + bottomBarPadding,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Delete widget",
            tint = contentTint,
            modifier = Modifier.size(if (hovered) 26.dp else 22.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = if (hovered) "أفلت الآن لحذف العنصر • Release to Delete" else "اسحب العنصر إلى السلة لحذفه • Drop here to Delete",
            style = MaterialTheme.typography.titleSmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = contentTint,
        )
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
    onOpenAddWidgetSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(scrollState.isScrollInProgress) {
        if (scrollState.isScrollInProgress) {
            onPaletteTouchOrScroll()
        }
    }

    val sections = remember {
        listOf(
            "Layouts" to listOf(
                IdeWidgetType.LINEAR_H,
                IdeWidgetType.LINEAR_V,
                IdeWidgetType.SCROLL_H,
                IdeWidgetType.SCROLL_V,
            ),
            "Text" to listOf(
                IdeWidgetType.TEXT_VIEW,
                IdeWidgetType.EDIT_TEXT,
                IdeWidgetType.AUTO_COMPLETE_TEXT,
                IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT,
                IdeWidgetType.CHECKED_TEXT_VIEW,
                IdeWidgetType.TEXT_INPUT_EDIT_TEXT,
            ),
            "Image" to listOf(
                IdeWidgetType.IMAGE_VIEW,
                IdeWidgetType.CIRCLE_IMAGE_VIEW,
                IdeWidgetType.IMAGE_BUTTON,
                IdeWidgetType.ROUNDED_IMAGE_VIEW,
                IdeWidgetType.SHAPEABLE_IMAGE_VIEW,
                IdeWidgetType.ZOOM_IMAGE_VIEW,
                IdeWidgetType.ASPECT_IMAGE_VIEW,
                IdeWidgetType.IMAGE_FILTER_VIEW,
            ),
            "Buttons" to listOf(
                IdeWidgetType.BUTTON,
                IdeWidgetType.MATERIAL_BUTTON,
                IdeWidgetType.OUTLINED_BUTTON,
                IdeWidgetType.TEXT_BUTTON,
                IdeWidgetType.FAB_BUTTON,
                IdeWidgetType.EXTENDED_FAB,
                IdeWidgetType.TOGGLE_BUTTON,
                IdeWidgetType.CHIP_BUTTON,
            ),
            "Choose" to listOf(
                IdeWidgetType.CHECK_BOX,
                IdeWidgetType.RADIO_BUTTON,
                IdeWidgetType.SWITCH,
                IdeWidgetType.SPINNER,
            ),
            "Web" to listOf(
                IdeWidgetType.WEB_VIEW,
                IdeWidgetType.CUSTOM_TAB_VIEW,
                IdeWidgetType.GECKO_WEB_VIEW,
                IdeWidgetType.AD_WEB_VIEW,
                IdeWidgetType.SWIPE_WEB_VIEW,
            ),
            "List" to listOf(
                IdeWidgetType.GRID_VIEW,
                IdeWidgetType.LIST_VIEW,
                IdeWidgetType.TREE_VIEW,
                IdeWidgetType.RECYCLER_VIEW,
                IdeWidgetType.VIEW_PAGER,
                IdeWidgetType.EXPANDABLE_LIST_VIEW,
                IdeWidgetType.STAGGERED_GRID_VIEW,
                IdeWidgetType.HORIZONTAL_LIST_VIEW,
                IdeWidgetType.CAROUSEL_VIEW,
                IdeWidgetType.SWIPE_LIST_VIEW,
            ),
            "Date" to listOf(
                IdeWidgetType.ANALOG_CLOCK,
                IdeWidgetType.DIGITAL_CLOCK,
                IdeWidgetType.TIME_PICKER,
                IdeWidgetType.DATE_PICKER,
                IdeWidgetType.CALENDAR_VIEW,
                IdeWidgetType.TEXT_CLOCK,
                IdeWidgetType.CHRONOMETER,
                IdeWidgetType.COUNTDOWN_VIEW,
                IdeWidgetType.STOPWATCH_VIEW,
                IdeWidgetType.DATE_RANGE_PICKER,
                IdeWidgetType.WORLD_CLOCK_VIEW,
            ),
        )
    }

    val cleanQuery = searchQuery.trim().lowercase()

    Column(
        modifier = modifier
            .padding(start = 6.dp, end = 4.dp, top = 4.dp, bottom = 6.dp),
    ) {
        // 1. Above Search & Layouts: "+ Add Widget" Button (opens Modal Bottom Sheet with Widget Type + Name/ID)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Cyan.copy(alpha = 0.18f))
                .border(1.dp, Cyan.copy(alpha = 0.65f), RoundedCornerShape(8.dp))
                .clickable(onClick = onOpenAddWidgetSheet)
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Widget",
                tint = Cyan,
                modifier = Modifier.size(15.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Add Widget",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = Cyan,
                maxLines = 1,
            )
        }

        Spacer(Modifier.height(6.dp))

        // 2. Above Layout: Live Widget Search Bar ("وفوق Layout شريط بحث للعناصر")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF141C3A))
                .border(
                    width = 1.dp,
                    color = if (cleanQuery.isNotEmpty()) Cyan.copy(alpha = 0.65f) else Color(0x2EFFFFFF),
                    shape = RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search Widgets",
                    tint = if (cleanQuery.isNotEmpty()) Cyan else Muted,
                    modifier = Modifier.size(13.dp),
                )
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        color = Ink,
                    ),
                    cursorBrush = SolidColor(Cyan),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search...",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                color = Muted.copy(alpha = 0.75f),
                                maxLines = 1,
                            )
                        }
                        innerTextField()
                    },
                )
                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Clear search",
                        tint = Muted,
                        modifier = Modifier
                            .size(13.dp)
                            .clip(CircleShape)
                            .clickable { searchQuery = "" },
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // 3. Scrollable Palette Sections (Layouts, Text, Image, Buttons, Choose, Web, List, Date)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState, enabled = !isDragging)
                .padding(bottom = 8.dp),
        ) {
            sections.forEach { (sectionTitle, sectionWidgets) ->
                val filteredWidgets = if (cleanQuery.isEmpty()) {
                    sectionWidgets
                } else {
                    sectionWidgets.filter { w ->
                        sketchWidgetLabel(w).lowercase().contains(cleanQuery) ||
                            w.displayName.lowercase().contains(cleanQuery) ||
                            w.idPrefix.lowercase().contains(cleanQuery) ||
                            sectionTitle.lowercase().contains(cleanQuery)
                    }
                }
                if (filteredWidgets.isNotEmpty()) {
                    Text(
                        text = sectionTitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Muted,
                        modifier = Modifier.padding(start = 2.dp, top = 4.dp, bottom = 6.dp),
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        filteredWidgets.forEach { widgetType ->
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

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWidgetModalBottomSheet(
    containers: List<IdeWidgetNode>,
    initialParentContainerUid: String?,
    suggestViewIdForType: (IdeWidgetType) -> String,
    onDismiss: () -> Unit,
    onConfirmAdd: (IdeWidgetType, String, String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCategory by remember { mutableStateOf<IdeWidgetCategory?>(null) }
    var selectedType by remember { mutableStateOf(IdeWidgetType.LINEAR_V) }
    var widgetNameInput by remember { mutableStateOf(suggestViewIdForType(IdeWidgetType.LINEAR_V)) }
    var selectedParentUid by remember { mutableStateOf(initialParentContainerUid) }
    var typeSearchQuery by remember { mutableStateOf("") }

    val filteredTypes = remember(selectedCategory, typeSearchQuery) {
        val q = typeSearchQuery.trim().lowercase()
        IdeWidgetType.entries.filter { t ->
            (selectedCategory == null || t.category == selectedCategory) &&
                (q.isEmpty() ||
                    sketchWidgetLabel(t).lowercase().contains(q) ||
                    t.displayName.lowercase().contains(q) ||
                    t.idPrefix.lowercase().contains(q))
        }
    }

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
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Add Widget • إضافة عنصر جديد",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                        Text(
                            text = "اختر نوع الـ Widget وحدد الاسم (ID) لإضافته مباشرةً إلى التصميم",
                            style = MaterialTheme.typography.labelSmall,
                            color = Cyan,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = null, tint = Muted)
                    }
                }

                // 1. Widget Name / ID Input
                OutlinedTextField(
                    value = widgetNameInput,
                    onValueChange = { widgetNameInput = it },
                    singleLine = true,
                    label = { Text("Widget Name / ID (الاسم)") },
                    placeholder = { Text(suggestViewIdForType(selectedType)) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier.padding(start = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            SketchWidgetIcon(type = selectedType, tint = Cyan)
                        }
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        color = Ink,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                        cursorColor = Cyan,
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedLabelColor = Cyan,
                        unfocusedLabelColor = Muted,
                        focusedContainerColor = Color(0x14FFFFFF),
                        unfocusedContainerColor = Color(0x0AFFFFFF),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                // 2. Optional Parent Layout Selector (if containers exist on canvas)
                if (containers.isNotEmpty()) {
                    Text(
                        text = "Parent Layout (الحاوية الأب):",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Muted,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val isRoot = selectedParentUid == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isRoot) Cyan.copy(alpha = 0.22f) else Color(0xFF151E3F))
                                .border(1.dp, if (isRoot) Cyan else Color(0x28FFFFFF), RoundedCornerShape(50))
                                .clickable { selectedParentUid = null }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "Root Screen",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = if (isRoot) Cyan else Ink,
                            )
                        }
                        containers.forEach { container ->
                            val selected = selectedParentUid == container.uid
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) Cyan.copy(alpha = 0.22f) else Color(0xFF151E3F))
                                    .border(1.dp, if (selected) Cyan else Color(0x28FFFFFF), RoundedCornerShape(50))
                                    .clickable { selectedParentUid = container.uid }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                SketchWidgetIcon(type = container.type, tint = if (selected) Cyan else Ink)
                                Text(
                                    text = container.viewId,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = if (selected) Cyan else Ink,
                                )
                            }
                        }
                    }
                }

                // 3. Category Filter Pills + Search for Widget Type
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val allSelected = selectedCategory == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (allSelected) Color(0xFF213163) else Color(0xFF151E3F))
                            .border(1.dp, if (allSelected) Cyan else Color(0x24FFFFFF), RoundedCornerShape(50))
                            .clickable { selectedCategory = null }
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(
                            text = "All (${IdeWidgetType.entries.size})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (allSelected) Cyan else Ink,
                        )
                    }
                    IdeWidgetCategory.entries.forEach { cat ->
                        val catSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (catSelected) Color(0xFF213163) else Color(0xFF151E3F))
                                .border(1.dp, if (catSelected) Cyan else Color(0x24FFFFFF), RoundedCornerShape(50))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 12.dp, vertical = 5.dp),
                        ) {
                            Text(
                                text = cat.title.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (catSelected) Cyan else Ink,
                            )
                        }
                    }
                }

                // 4. Scrollable Widget Types List ("نوع الـ Widget")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 260.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    filteredTypes.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            rowItems.forEach { type ->
                                val isSelected = selectedType == type
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Cyan.copy(alpha = 0.18f) else Color(0xFF141D3D))
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) Cyan else Color(0x26FFFFFF),
                                            shape = RoundedCornerShape(12.dp),
                                        )
                                        .clickable {
                                            selectedType = type
                                            widgetNameInput = suggestViewIdForType(type)
                                        }
                                        .padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    SketchWidgetIcon(type = type, tint = if (isSelected) Cyan else Ink)
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = sketchWidgetLabel(type),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.5.sp,
                                            ),
                                            color = if (isSelected) Cyan else Ink,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = type.category.title.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMono,
                                                fontSize = 8.5.sp,
                                            ),
                                            color = Muted,
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }

                // 5. Confirm Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dialog_cancel), color = Muted)
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Cyan)
                            .clickable {
                                onConfirmAdd(
                                    selectedType,
                                    widgetNameInput.ifBlank { suggestViewIdForType(selectedType) },
                                    selectedParentUid,
                                )
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color(0xFF050816),
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "إضافة • Add Widget",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF050816),
                        )
                    }
                }
            }
        }
    }
}

private fun sketchWidgetLabel(type: IdeWidgetType): String = when (type) {
    IdeWidgetType.LINEAR_H -> "Linear(H)"
    IdeWidgetType.LINEAR_V -> "Linear(V)"
    IdeWidgetType.SCROLL_H -> "Scroll(H)"
    IdeWidgetType.SCROLL_V -> "Scroll(V)"
    IdeWidgetType.TEXT_VIEW -> "TextView"
    IdeWidgetType.EDIT_TEXT -> "EditText"
    IdeWidgetType.AUTO_COMPLETE_TEXT -> "AutoComplete"
    IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT -> "MultiAutoText"
    IdeWidgetType.CHECKED_TEXT_VIEW -> "CheckedText"
    IdeWidgetType.TEXT_INPUT_EDIT_TEXT -> "TextInputEdit"
    IdeWidgetType.IMAGE_VIEW -> "ImageView"
    IdeWidgetType.CIRCLE_IMAGE_VIEW -> "CircleImageView"
    IdeWidgetType.IMAGE_BUTTON -> "ImageButton"
    IdeWidgetType.ROUNDED_IMAGE_VIEW -> "RoundedImage"
    IdeWidgetType.SHAPEABLE_IMAGE_VIEW -> "ShapeableImage"
    IdeWidgetType.ZOOM_IMAGE_VIEW -> "ZoomImageView"
    IdeWidgetType.ASPECT_IMAGE_VIEW -> "AspectImageView"
    IdeWidgetType.IMAGE_FILTER_VIEW -> "ImageFilterView"
    IdeWidgetType.BUTTON -> "Button"
    IdeWidgetType.MATERIAL_BUTTON -> "MaterialButton"
    IdeWidgetType.OUTLINED_BUTTON -> "OutlinedButton"
    IdeWidgetType.TEXT_BUTTON -> "TextButton"
    IdeWidgetType.FAB_BUTTON -> "FAB"
    IdeWidgetType.EXTENDED_FAB -> "ExtendedFAB"
    IdeWidgetType.TOGGLE_BUTTON -> "ToggleButton"
    IdeWidgetType.CHIP_BUTTON -> "ChipButton"
    IdeWidgetType.CHECK_BOX -> "Check Box"
    IdeWidgetType.RADIO_BUTTON -> "Radio Button"
    IdeWidgetType.SWITCH -> "Switch"
    IdeWidgetType.SPINNER -> "Spinner"
    IdeWidgetType.WEB_VIEW -> "WebView"
    IdeWidgetType.CUSTOM_TAB_VIEW -> "CustomTabView"
    IdeWidgetType.GECKO_WEB_VIEW -> "GeckoWebView"
    IdeWidgetType.AD_WEB_VIEW -> "AdBlockWeb"
    IdeWidgetType.SWIPE_WEB_VIEW -> "SwipeWebView"
    IdeWidgetType.GRID_VIEW -> "Grid View"
    IdeWidgetType.LIST_VIEW -> "List View"
    IdeWidgetType.TREE_VIEW -> "TreeView"
    IdeWidgetType.RECYCLER_VIEW -> "RecyclerView"
    IdeWidgetType.VIEW_PAGER -> "ViewPager"
    IdeWidgetType.EXPANDABLE_LIST_VIEW -> "ExpandableList"
    IdeWidgetType.STAGGERED_GRID_VIEW -> "StaggeredGrid"
    IdeWidgetType.HORIZONTAL_LIST_VIEW -> "HorizontalList"
    IdeWidgetType.CAROUSEL_VIEW -> "CarouselView"
    IdeWidgetType.SWIPE_LIST_VIEW -> "SwipeListView"
    IdeWidgetType.ANALOG_CLOCK -> "AnalogClock"
    IdeWidgetType.DIGITAL_CLOCK -> "DigitalClock"
    IdeWidgetType.TIME_PICKER -> "TimePicker"
    IdeWidgetType.DATE_PICKER -> "DatePicker"
    IdeWidgetType.CALENDAR_VIEW -> "CalendarView"
    IdeWidgetType.TEXT_CLOCK -> "TextClock"
    IdeWidgetType.CHRONOMETER -> "Chronometer"
    IdeWidgetType.COUNTDOWN_VIEW -> "CountDownView"
    IdeWidgetType.STOPWATCH_VIEW -> "StopwatchView"
    IdeWidgetType.DATE_RANGE_PICKER -> "DateRangePicker"
    IdeWidgetType.WORLD_CLOCK_VIEW -> "WorldClockView"
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
                            // If finger moves predominantly vertically, allow the palette column to scroll!
                            if (abs(dy) > 9f && abs(dy) > abs(dx) * 1.4f) {
                                return@awaitEachGesture
                            }
                            if (abs(dx) > 4f || (abs(dx) >= abs(dy) && (latestPos - downPos).getDistance() > 6f)) {
                                change.consume()
                                dragging = true
                                val rootPos = Offset(
                                    x = itemBoundsInRoot.left + latestPos.x,
                                    y = itemBoundsInRoot.top + latestPos.y,
                                )
                                currentOnStartDrag(type, rootPos)
                            } else if (System.currentTimeMillis() - downTime >= 150L) {
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

            IdeWidgetType.EDIT_TEXT -> {
                // Input box with I-beam cursor & bottom underline
                val sw = 1.4.dp.toPx()
                drawLine(tint, Offset(1f, size.height * 0.86f), Offset(size.width - 1f, size.height * 0.86f), strokeWidth = sw)
                // I-beam cursor on left
                val cx = size.width * 0.26f
                drawLine(tint, Offset(cx, size.height * 0.18f), Offset(cx, size.height * 0.72f), strokeWidth = sw)
                drawLine(tint, Offset(cx - 2.2f, size.height * 0.18f), Offset(cx + 2.2f, size.height * 0.18f), strokeWidth = sw)
                drawLine(tint, Offset(cx - 2.2f, size.height * 0.72f), Offset(cx + 2.2f, size.height * 0.72f), strokeWidth = sw)
                // Text line
                drawLine(tint, Offset(size.width * 0.48f, size.height * 0.48f), Offset(size.width * 0.88f, size.height * 0.48f), strokeWidth = sw)
            }

            IdeWidgetType.AUTO_COMPLETE_TEXT -> {
                // Input field with dropdown arrow ▾
                val sw = 1.4.dp.toPx()
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, size.height * 0.18f),
                    size = Size(size.width - 2f, size.height * 0.64f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.20f, size.height * 0.50f), Offset(size.width * 0.52f, size.height * 0.50f), strokeWidth = sw)
                val arrow = Path().apply {
                    moveTo(size.width * 0.64f, size.height * 0.42f)
                    lineTo(size.width * 0.74f, size.height * 0.58f)
                    lineTo(size.width * 0.84f, size.height * 0.42f)
                }
                drawPath(arrow, color = tint, style = stroke)
            }

            IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT -> {
                // Input field with two tag pills + dropdown dot
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, size.height * 0.16f),
                    size = Size(size.width - 2f, size.height * 0.68f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.34f),
                    size = Size(size.width * 0.26f, size.height * 0.32f),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()),
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.52f, size.height * 0.34f),
                    size = Size(size.width * 0.26f, size.height * 0.32f),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx()),
                )
            }

            IdeWidgetType.CHECKED_TEXT_VIEW -> {
                // Checkmark inside a box ☑
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1.2f, 1.2f),
                    size = Size(size.width - 2.4f, size.height - 2.4f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                val check = Path().apply {
                    moveTo(size.width * 0.26f, size.height * 0.52f)
                    lineTo(size.width * 0.44f, size.height * 0.70f)
                    lineTo(size.width * 0.76f, size.height * 0.32f)
                }
                drawPath(check, color = tint, style = stroke)
            }

            IdeWidgetType.TEXT_INPUT_EDIT_TEXT -> {
                // Material Outlined TextField with top floating label bar
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, size.height * 0.24f),
                    size = Size(size.width - 2f, size.height * 0.62f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.22f, size.height * 0.12f), Offset(size.width * 0.58f, size.height * 0.12f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width * 0.24f, size.height * 0.55f), Offset(size.width * 0.74f, size.height * 0.55f), strokeWidth = stroke.width)
            }

            IdeWidgetType.IMAGE_VIEW -> {
                // Picture frame with mountain peak + sun dot
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1.2f, 1.5f),
                    size = Size(size.width - 2.4f, size.height - 3f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                drawCircle(
                    color = tint,
                    radius = 1.3.dp.toPx(),
                    center = Offset(size.width * 0.34f, size.height * 0.38f),
                )
                val mountain = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.78f)
                    lineTo(size.width * 0.46f, size.height * 0.48f)
                    lineTo(size.width * 0.64f, size.height * 0.66f)
                    lineTo(size.width * 0.76f, size.height * 0.54f)
                    lineTo(size.width * 0.86f, size.height * 0.78f)
                }
                drawPath(mountain, color = tint, style = stroke)
            }

            IdeWidgetType.CIRCLE_IMAGE_VIEW -> {
                // Circular avatar frame with person/mountain inside
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.44f,
                    center = c,
                    style = stroke,
                )
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.15f,
                    center = Offset(c.x, size.height * 0.38f),
                )
                drawArc(
                    color = tint,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.24f, size.height * 0.54f),
                    size = Size(size.width * 0.52f, size.height * 0.40f),
                    style = stroke,
                )
            }

            IdeWidgetType.IMAGE_BUTTON -> {
                // Button pill with camera/image dot inside
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, 2f),
                    size = Size(size.width - 2f, size.height - 4f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = stroke,
                )
                drawCircle(
                    color = tint,
                    radius = 2.dp.toPx(),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    style = stroke,
                )
            }

            IdeWidgetType.ROUNDED_IMAGE_VIEW -> {
                // Extra-rounded image frame with mountain
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1.2f, 1.2f),
                    size = Size(size.width - 2.4f, size.height - 2.4f),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                    style = stroke,
                )
                val mountain = Path().apply {
                    moveTo(size.width * 0.24f, size.height * 0.74f)
                    lineTo(size.width * 0.48f, size.height * 0.44f)
                    lineTo(size.width * 0.76f, size.height * 0.74f)
                }
                drawPath(mountain, color = tint, style = stroke)
            }

            IdeWidgetType.SHAPEABLE_IMAGE_VIEW -> {
                // Cut-corner hexagon/diamond image frame
                val hex = Path().apply {
                    moveTo(size.width * 0.30f, 1.2f)
                    lineTo(size.width * 0.70f, 1.2f)
                    lineTo(size.width - 1.2f, size.height * 0.30f)
                    lineTo(size.width - 1.2f, size.height * 0.70f)
                    lineTo(size.width * 0.70f, size.height - 1.2f)
                    lineTo(size.width * 0.30f, size.height - 1.2f)
                    lineTo(1.2f, size.height * 0.70f)
                    lineTo(1.2f, size.height * 0.30f)
                    close()
                }
                drawPath(hex, color = tint, style = stroke)
                drawCircle(tint, radius = 1.4.dp.toPx(), center = Offset(size.width * 0.5f, size.height * 0.5f))
            }

            IdeWidgetType.ZOOM_IMAGE_VIEW -> {
                // Magnifying glass + image
                val r = size.minDimension * 0.32f
                val center = Offset(size.width * 0.44f, size.height * 0.44f)
                drawCircle(tint, radius = r, center = center, style = stroke)
                drawLine(tint, Offset(size.width * 0.66f, size.height * 0.66f), Offset(size.width * 0.88f, size.height * 0.88f), strokeWidth = stroke.width)
                drawLine(tint, Offset(center.x - r * 0.45f, center.y), Offset(center.x + r * 0.45f, center.y), strokeWidth = stroke.width)
                drawLine(tint, Offset(center.x, center.y - r * 0.45f), Offset(center.x, center.y + r * 0.45f), strokeWidth = stroke.width)
            }

            IdeWidgetType.ASPECT_IMAGE_VIEW -> {
                // 16:9 widescreen frame with diagonal arrows
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, size.height * 0.22f),
                    size = Size(size.width - 2f, size.height * 0.56f),
                    cornerRadius = CornerRadius(1.8.dp.toPx(), 1.8.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.26f, size.height * 0.64f), Offset(size.width * 0.74f, size.height * 0.36f), strokeWidth = stroke.width)
            }

            IdeWidgetType.IMAGE_FILTER_VIEW -> {
                // Image frame with filter sliders/sparkle
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1.2f, 1.5f),
                    size = Size(size.width - 2.4f, size.height - 3f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.25f, size.height * 0.38f), Offset(size.width * 0.75f, size.height * 0.38f), strokeWidth = stroke.width)
                drawCircle(tint, radius = 1.4.dp.toPx(), center = Offset(size.width * 0.60f, size.height * 0.38f))
                drawLine(tint, Offset(size.width * 0.25f, size.height * 0.64f), Offset(size.width * 0.75f, size.height * 0.64f), strokeWidth = stroke.width)
                drawCircle(tint, radius = 1.4.dp.toPx(), center = Offset(size.width * 0.38f, size.height * 0.64f))
            }

            IdeWidgetType.BUTTON,
            IdeWidgetType.MATERIAL_BUTTON -> {
                // Raised button pill with inner label bar
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, size.height * 0.20f),
                    size = Size(size.width - 2f, size.height * 0.60f),
                    cornerRadius = CornerRadius(if (type == IdeWidgetType.MATERIAL_BUTTON) 4.dp.toPx() else 2.dp.toPx()),
                    style = stroke,
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.26f, size.height * 0.50f),
                    end = Offset(size.width * 0.74f, size.height * 0.50f),
                    strokeWidth = stroke.width,
                )
            }

            IdeWidgetType.OUTLINED_BUTTON -> {
                val dash = PathEffect.dashPathEffect(floatArrayOf(2.5f, 2f), 0f)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, size.height * 0.20f),
                    size = Size(size.width - 2f, size.height * 0.60f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = Stroke(width = stroke.width, pathEffect = dash),
                )
                drawLine(tint, Offset(size.width * 0.28f, size.height * 0.50f), Offset(size.width * 0.72f, size.height * 0.50f), strokeWidth = stroke.width)
            }

            IdeWidgetType.TEXT_BUTTON -> {
                drawLine(tint, Offset(size.width * 0.18f, size.height * 0.44f), Offset(size.width * 0.82f, size.height * 0.44f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width * 0.28f, size.height * 0.68f), Offset(size.width * 0.72f, size.height * 0.68f), strokeWidth = stroke.width)
            }

            IdeWidgetType.FAB_BUTTON -> {
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(tint, radius = size.minDimension * 0.43f, center = c, style = stroke)
                drawLine(tint, Offset(c.x - 3f, c.y), Offset(c.x + 3f, c.y), strokeWidth = stroke.width)
                drawLine(tint, Offset(c.x, c.y - 3f), Offset(c.x, c.y + 3f), strokeWidth = stroke.width)
            }

            IdeWidgetType.EXTENDED_FAB -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(0.8f, size.height * 0.20f),
                    size = Size(size.width - 1.6f, size.height * 0.60f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = stroke,
                )
                val cx = size.width * 0.30f
                val cy = size.height * 0.50f
                drawLine(tint, Offset(cx - 2f, cy), Offset(cx + 2f, cy), strokeWidth = stroke.width)
                drawLine(tint, Offset(cx, cy - 2f), Offset(cx, cy + 2f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width * 0.50f, cy), Offset(size.width * 0.78f, cy), strokeWidth = stroke.width)
            }

            IdeWidgetType.TOGGLE_BUTTON -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, size.height * 0.18f),
                    size = Size(size.width - 2f, size.height * 0.64f),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.26f, size.height * 0.64f), Offset(size.width * 0.74f, size.height * 0.64f), strokeWidth = 1.8.dp.toPx())
            }

            IdeWidgetType.CHIP_BUTTON -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(0.8f, size.height * 0.22f),
                    size = Size(size.width - 1.6f, size.height * 0.56f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = stroke,
                )
                drawCircle(tint, radius = 1.4.dp.toPx(), center = Offset(size.width * 0.28f, size.height * 0.50f))
                drawLine(tint, Offset(size.width * 0.44f, size.height * 0.50f), Offset(size.width * 0.76f, size.height * 0.50f), strokeWidth = stroke.width)
            }

            IdeWidgetType.CHECK_BOX -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1.4f, 1.4f),
                    size = Size(size.width - 2.8f, size.height - 2.8f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                val check = Path().apply {
                    moveTo(size.width * 0.26f, size.height * 0.52f)
                    lineTo(size.width * 0.44f, size.height * 0.70f)
                    lineTo(size.width * 0.76f, size.height * 0.30f)
                }
                drawPath(check, color = tint, style = stroke)
            }

            IdeWidgetType.RADIO_BUTTON -> {
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(tint, radius = size.minDimension * 0.42f, center = c, style = stroke)
                drawCircle(tint, radius = size.minDimension * 0.19f, center = c)
            }

            IdeWidgetType.SWITCH -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(0.8f, size.height * 0.24f),
                    size = Size(size.width - 1.6f, size.height * 0.52f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = stroke,
                )
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.16f,
                    center = Offset(size.width * 0.68f, size.height * 0.50f),
                )
            }

            IdeWidgetType.SPINNER -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, size.height * 0.20f),
                    size = Size(size.width - 2f, size.height * 0.60f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                val tri = Path().apply {
                    moveTo(size.width * 0.58f, size.height * 0.42f)
                    lineTo(size.width * 0.78f, size.height * 0.42f)
                    lineTo(size.width * 0.68f, size.height * 0.60f)
                    close()
                }
                drawPath(tri, color = tint)
            }

            IdeWidgetType.WEB_VIEW,
            IdeWidgetType.CUSTOM_TAB_VIEW,
            IdeWidgetType.GECKO_WEB_VIEW,
            IdeWidgetType.AD_WEB_VIEW,
            IdeWidgetType.SWIPE_WEB_VIEW -> {
                // Globe / Browser icon
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.minDimension * 0.43f
                drawCircle(tint, radius = r, center = c, style = stroke)
                drawLine(tint, Offset(c.x - r, c.y), Offset(c.x + r, c.y), strokeWidth = stroke.width * 0.85f)
                drawLine(tint, Offset(c.x, c.y - r), Offset(c.x, c.y + r), strokeWidth = stroke.width * 0.85f)
                drawOval(
                    color = tint,
                    topLeft = Offset(c.x - r * 0.52f, c.y - r),
                    size = Size(r * 1.04f, r * 2f),
                    style = stroke,
                )
            }

            IdeWidgetType.GRID_VIEW,
            IdeWidgetType.STAGGERED_GRID_VIEW -> {
                // 2x2 Grid cells
                val gap = 1.6f
                val cellW = (size.width - 3.6f - gap) / 2f
                val cellH = (size.height - 3.6f - gap) / 2f
                for (r in 0..1) {
                    for (c in 0..1) {
                        drawRoundRect(
                            color = tint,
                            topLeft = Offset(1.8f + c * (cellW + gap), 1.8f + r * (cellH + gap)),
                            size = Size(cellW, cellH),
                            cornerRadius = CornerRadius(1.2.dp.toPx(), 1.2.dp.toPx()),
                            style = stroke,
                        )
                    }
                }
            }

            IdeWidgetType.LIST_VIEW,
            IdeWidgetType.RECYCLER_VIEW,
            IdeWidgetType.EXPANDABLE_LIST_VIEW,
            IdeWidgetType.SWIPE_LIST_VIEW -> {
                // 3 horizontal list rows with bullet dots
                for (i in 0..2) {
                    val y = size.height * (0.24f + i * 0.26f)
                    drawCircle(tint, radius = 1.1.dp.toPx(), center = Offset(size.width * 0.20f, y))
                    drawLine(tint, Offset(size.width * 0.36f, y), Offset(size.width * 0.86f, y), strokeWidth = stroke.width)
                }
            }

            IdeWidgetType.TREE_VIEW -> {
                // Hierarchical tree branch lines
                drawLine(tint, Offset(size.width * 0.22f, size.height * 0.18f), Offset(size.width * 0.22f, size.height * 0.78f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width * 0.22f, size.height * 0.24f), Offset(size.width * 0.78f, size.height * 0.24f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width * 0.22f, size.height * 0.52f), Offset(size.width * 0.84f, size.height * 0.52f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width * 0.22f, size.height * 0.78f), Offset(size.width * 0.84f, size.height * 0.78f), strokeWidth = stroke.width)
            }

            IdeWidgetType.VIEW_PAGER,
            IdeWidgetType.HORIZONTAL_LIST_VIEW,
            IdeWidgetType.CAROUSEL_VIEW -> {
                // Center page card + side peeks
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.24f, size.height * 0.16f),
                    size = Size(size.width * 0.52f, size.height * 0.68f),
                    cornerRadius = CornerRadius(1.8.dp.toPx(), 1.8.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.10f, size.height * 0.26f), Offset(size.width * 0.10f, size.height * 0.74f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width * 0.90f, size.height * 0.26f), Offset(size.width * 0.90f, size.height * 0.74f), strokeWidth = stroke.width)
            }

            IdeWidgetType.ANALOG_CLOCK,
            IdeWidgetType.TIME_PICKER,
            IdeWidgetType.CHRONOMETER,
            IdeWidgetType.COUNTDOWN_VIEW,
            IdeWidgetType.STOPWATCH_VIEW,
            IdeWidgetType.WORLD_CLOCK_VIEW -> {
                // Clock dial + hands
                val c = Offset(size.width / 2f, size.height / 2f)
                val r = size.minDimension * 0.42f
                drawCircle(tint, radius = r, center = c, style = stroke)
                drawLine(tint, c, Offset(c.x, c.y - r * 0.58f), strokeWidth = stroke.width)
                drawLine(tint, c, Offset(c.x + r * 0.48f, c.y), strokeWidth = stroke.width)
            }

            IdeWidgetType.DIGITAL_CLOCK,
            IdeWidgetType.TEXT_CLOCK -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1f, size.height * 0.22f),
                    size = Size(size.width - 2f, size.height * 0.56f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                drawCircle(tint, radius = 0.9.dp.toPx(), center = Offset(size.width * 0.50f, size.height * 0.42f))
                drawCircle(tint, radius = 0.9.dp.toPx(), center = Offset(size.width * 0.50f, size.height * 0.58f))
            }

            IdeWidgetType.DATE_PICKER,
            IdeWidgetType.CALENDAR_VIEW,
            IdeWidgetType.DATE_RANGE_PICKER -> {
                // Calendar box with top binding rings & grid
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(1.4f, size.height * 0.20f),
                    size = Size(size.width - 2.8f, size.height * 0.68f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(1.4f, size.height * 0.38f), Offset(size.width - 1.4f, size.height * 0.38f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width * 0.32f, size.height * 0.10f), Offset(size.width * 0.32f, size.height * 0.26f), strokeWidth = stroke.width)
                drawLine(tint, Offset(size.width * 0.68f, size.height * 0.10f), Offset(size.width * 0.68f, size.height * 0.26f), strokeWidth = stroke.width)
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
    val placeholderModifier = if ((type.isContainer || type.isInputTextWidget || type.isWebWidget || type.isListWidget || type == IdeWidgetType.SPINNER || type == IdeWidgetType.CALENDAR_VIEW || type == IdeWidgetType.DATE_PICKER || type == IdeWidgetType.TIME_PICKER || type == IdeWidgetType.DATE_RANGE_PICKER) && !inHorizontalParent) {
        Modifier
            .fillMaxWidth()
            .height(if (type.isWebWidget || type.isListWidget) 48.dp else 24.dp)
            .background(Color(0xFFD6D6D6))
    } else if (type == IdeWidgetType.CIRCLE_IMAGE_VIEW || type == IdeWidgetType.FAB_BUTTON || type == IdeWidgetType.ANALOG_CLOCK) {
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFD6D6D6))
    } else {
        Modifier
            .width(52.dp)
            .height(24.dp)
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
    projectImages: List<ProjectImageItem>,
    selectedWidgetUid: String?,
    draggingType: IdeWidgetType?,
    draggingExistingUid: String?,
    isDragOverPhone: Boolean,
    hoveredContainerUid: String?,
    dropInsertIndex: Int,
    onSelectWidget: (IdeWidgetNode) -> Unit,
    onDoubleTapWidget: (IdeWidgetNode) -> Unit,
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
    var interiorBounds by remember { mutableStateOf(Rect.Zero) }

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
            },
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
                .clipToBounds()
                .background(Color.White)
                .onGloballyPositioned { coords ->
                    interiorBounds = coords.boundsInRoot()
                }
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
                    projectImages = projectImages,
                    parentBounds = interiorBounds,
                    selectedWidgetUid = selectedWidgetUid,
                    draggingType = draggingType,
                    draggingExistingUid = draggingExistingUid,
                    hoveredContainerUid = hoveredContainerUid,
                    dropInsertIndex = dropInsertIndex,
                    onSelectWidget = onSelectWidget,
                    onDoubleTapWidget = onDoubleTapWidget,
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
    projectImages: List<ProjectImageItem>,
    parentBounds: Rect = Rect.Zero,
    selectedWidgetUid: String?,
    draggingType: IdeWidgetType?,
    draggingExistingUid: String?,
    hoveredContainerUid: String?,
    dropInsertIndex: Int,
    onSelectWidget: (IdeWidgetNode) -> Unit,
    onDoubleTapWidget: (IdeWidgetNode) -> Unit,
    onStartMoveExistingWidget: (IdeWidgetNode, Offset) -> Unit,
    onUpdateMoveDragPos: (Offset) -> Unit,
    onEndMoveExistingWidget: () -> Unit,
    onCancelMoveExistingWidget: () -> Unit,
    onContainerPositioned: (String, Rect) -> Unit,
    onWidgetPositioned: (String, Rect) -> Unit,
) {
    val density = LocalDensity.current
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
        customWidthDp != null && customWidthDp > 0 -> Modifier.width(customWidthDp.coerceIn(16, 320).dp)
        node.layoutWidth == "match_parent" -> Modifier.fillMaxWidth()
        else -> Modifier.wrapContentWidth()
    }

    val visualPad = (node.paddingDp / 2).coerceIn(2, 16).dp
    val containerVisualPad = (node.paddingDp / 2).coerceAtLeast(if (node.children.isNotEmpty()) 8 else 3).coerceAtMost(18).dp
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

    // Clamp translationX & translationY so the widget can NEVER exit outside its parent layout!
    val requestedTxPx = with(density) { node.translationX.dp.toPx() }
    val requestedTyPx = with(density) { node.translationY.dp.toPx() }
    val clampedTxPx = if (!parentBounds.isEmpty && !nodeBoundsInRoot.isEmpty && parentBounds.width > 1f) {
        val minTx = (parentBounds.left - nodeBoundsInRoot.left).coerceAtMost(0f)
        val maxTx = (parentBounds.right - nodeBoundsInRoot.right).coerceAtLeast(0f)
        requestedTxPx.coerceIn(minTx, maxTx)
    } else {
        requestedTxPx
    }
    val clampedTyPx = if (!parentBounds.isEmpty && !nodeBoundsInRoot.isEmpty && parentBounds.height > 1f) {
        val minTy = (parentBounds.top - nodeBoundsInRoot.top).coerceAtMost(0f)
        val maxTy = (parentBounds.bottom - nodeBoundsInRoot.bottom).coerceAtLeast(0f)
        requestedTyPx.coerceIn(minTy, maxTy)
    } else {
        requestedTyPx
    }

    // Keep real layout dimensions while dragging (`isBeingMoved`) so `pointerInput` coordinates NEVER shift!
    val transformModifier = Modifier.graphicsLayer(
        rotationZ = if (isBeingMoved) 0f else node.rotation,
        alpha = if (isBeingMoved) 0.28f else (if (node.enabled) node.alpha.coerceIn(0.08f, 1f) else 0.45f),
        translationX = if (isBeingMoved) 0f else clampedTxPx,
        translationY = if (isBeingMoved) 0f else clampedTyPx,
        scaleX = if (isBeingMoved) 1f else node.scaleX.coerceIn(0.25f, 2.5f),
        scaleY = if (isBeingMoved) 1f else node.scaleY.coerceIn(0.25f, 2.5f),
    )

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
        // Linear(H) and Scroll(V) both behave as Horizontal Linear layouts; Scroll(V) scrolls horizontally!
        IdeWidgetType.LINEAR_H, IdeWidgetType.SCROLL_V -> {
            val isEmpty = nonMovingChildCount == 0 && !isHoveredDropTarget
            val hScrollState = rememberScrollState()
            val scrollMod = if (node.type == IdeWidgetType.SCROLL_V) {
                Modifier.horizontalScroll(hScrollState, enabled = draggingType == null)
            } else {
                Modifier
            }
            Column(
                modifier = Modifier
                    .padding(visualMargin)
                    .then(widthModifier)
                    .then(
                        when {
                            customHeightDp != null && customHeightDp > 0 -> Modifier.height(customHeightDp.coerceIn(24, 260).dp)
                            isEmpty -> Modifier.height(28.dp)
                            else -> Modifier.heightIn(min = 32.dp)
                        },
                    )
                    .then(transformModifier)
                    .clipToBounds()
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        if (!isBeingMoved) {
                            onContainerPositioned(node.uid, rect)
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(baseFill)
                    .border(if (isSelected) 1.3.dp else 0.8.dp, if (isSelected) Color(0xFF0284C7) else layoutOutlineColor, RectangleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelectWidget(node) }
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
                    },
            ) {
                // Dedicated clickable Layout Header Strip when the layout contains children,
                // so the user can ALWAYS reach/select/drag even a tiny layout filled with match_parent children!
                if (node.children.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.22f) else Color(0xFFE2E8F0).copy(alpha = 0.85f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onSelectWidget(node) }
                            .padding(horizontal = 5.dp, vertical = 1.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "[${node.type.shortTag}] ${node.viewId}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = if (isSelected) Color(0xFF0369A1) else Color(0xFF475569),
                            maxLines = 1,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(scrollMod)
                        .padding(if (isEmpty) 0.dp else containerVisualPad),
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
                            projectImages = projectImages,
                            parentBounds = nodeBoundsInRoot,
                            selectedWidgetUid = selectedWidgetUid,
                            draggingType = draggingType,
                            draggingExistingUid = draggingExistingUid,
                            hoveredContainerUid = hoveredContainerUid,
                            dropInsertIndex = dropInsertIndex,
                            onSelectWidget = onSelectWidget,
                            onDoubleTapWidget = onDoubleTapWidget,
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
        }

        // Linear(V) and Scroll(H) both behave as Vertical Linear layouts; Scroll(H) scrolls vertically!
        IdeWidgetType.LINEAR_V, IdeWidgetType.SCROLL_H -> {
            val isEmpty = nonMovingChildCount == 0 && !isHoveredDropTarget
            val vScrollState = rememberScrollState()
            val scrollMod = if (node.type == IdeWidgetType.SCROLL_H) {
                Modifier.verticalScroll(vScrollState, enabled = draggingType == null)
            } else {
                Modifier
            }
            Column(
                modifier = Modifier
                    .padding(visualMargin)
                    .then(widthModifier)
                    .then(
                        when {
                            customHeightDp != null && customHeightDp > 0 -> Modifier.height(customHeightDp.coerceIn(24, 260).dp)
                            isEmpty -> Modifier.height(28.dp)
                            node.type == IdeWidgetType.SCROLL_H -> Modifier.heightIn(min = 38.dp, max = 180.dp)
                            else -> Modifier.heightIn(min = 32.dp)
                        },
                    )
                    .then(transformModifier)
                    .clipToBounds()
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        if (!isBeingMoved) {
                            onContainerPositioned(node.uid, rect)
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(baseFill)
                    .border(if (isSelected) 1.3.dp else 0.8.dp, if (isSelected) Color(0xFF0284C7) else layoutOutlineColor, RectangleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelectWidget(node) }
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
                    .then(scrollMod),
                verticalArrangement = verticalArr,
                horizontalAlignment = horizontalAlign,
            ) {
                // Dedicated clickable Layout Header Strip when the layout contains children,
                // so the user can ALWAYS reach/select/drag even a tiny layout filled with match_parent children!
                if (node.children.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) Color(0xFF0284C7).copy(alpha = 0.22f) else Color(0xFFE2E8F0).copy(alpha = 0.85f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onSelectWidget(node) }
                            .padding(horizontal = 5.dp, vertical = 1.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "[${node.type.shortTag}] ${node.viewId}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = if (isSelected) Color(0xFF0369A1) else Color(0xFF475569),
                            maxLines = 1,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isEmpty) 0.dp else containerVisualPad),
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
                            projectImages = projectImages,
                            parentBounds = nodeBoundsInRoot,
                            selectedWidgetUid = selectedWidgetUid,
                            draggingType = draggingType,
                            draggingExistingUid = draggingExistingUid,
                            hoveredContainerUid = hoveredContainerUid,
                            dropInsertIndex = dropInsertIndex,
                            onSelectWidget = onSelectWidget,
                            onDoubleTapWidget = onDoubleTapWidget,
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
        }

        IdeWidgetType.TEXT_VIEW,
        IdeWidgetType.EDIT_TEXT,
        IdeWidgetType.AUTO_COMPLETE_TEXT,
        IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT,
        IdeWidgetType.CHECKED_TEXT_VIEW,
        IdeWidgetType.TEXT_INPUT_EDIT_TEXT -> {
            val parsedTxtColor = remember(node.textColor) {
                parseHexColorOrNull(node.textColor) ?: Color(0xFF222222)
            }
            val parsedHintColor = remember(node.hintColor) {
                parseHexColorOrNull(node.hintColor) ?: Color(0xFF94A3B8)
            }
            val fontWeight = if (node.textStyle.contains("bold")) FontWeight.Bold else FontWeight.Normal
            val fontStyle = if (node.textStyle.contains("italic")) FontStyle.Italic else FontStyle.Normal
            val boxAlignment = when {
                node.gravity.contains("center") -> Alignment.Center
                node.gravity.contains("right") -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }
            val showingHint = node.type.isInputTextWidget && node.text.isBlank()
            val rawDisplayedText = if (showingHint) {
                node.hint.ifBlank { node.type.displayName }
            } else {
                node.text.ifBlank { node.type.displayName }
            }
            val displayedText = if (!showingHint && node.inputType.contains("Password", ignoreCase = true)) {
                "•".repeat(rawDisplayedText.length.coerceAtLeast(6))
            } else {
                rawDisplayedText
            }
            val maxLinesCount = when {
                node.singleLine -> 1
                node.lines > 0 -> node.lines
                else -> Int.MAX_VALUE
            }

            Box(
                modifier = Modifier
                    .padding(visualMargin)
                    .then(widthModifier)
                    .then(
                        if (customHeightDp != null && customHeightDp > 0) {
                            Modifier.height(customHeightDp.coerceIn(18, 220).dp)
                        } else {
                            Modifier
                        },
                    )
                    .then(transformModifier)
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        if (!isBeingMoved) {
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(baseFill)
                    .then(
                        if (node.type == IdeWidgetType.TEXT_INPUT_EDIT_TEXT) {
                            Modifier.border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF0284C7) else Color(0xFF94A3B8),
                                shape = RoundedCornerShape(6.dp),
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (isSelected) {
                            onDoubleTapWidget(node)
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
                        horizontal = (node.paddingDp.coerceIn(4, 24)).dp,
                        vertical = (node.paddingDp / 2).coerceIn(3, 16).dp,
                    ),
                contentAlignment = boxAlignment,
            ) {
                Column(
                    modifier = if (node.layoutWidth == "match_parent") Modifier.fillMaxWidth() else Modifier.wrapContentWidth(),
                ) {
                    if (node.type == IdeWidgetType.TEXT_INPUT_EDIT_TEXT) {
                        Text(
                            text = node.hint.ifBlank { "Label" },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = Color(0xFF0284C7),
                            maxLines = 1,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = displayedText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (node.textSizeSp - 1).coerceIn(9, 28).sp,
                                fontWeight = fontWeight,
                                fontStyle = fontStyle,
                            ),
                            color = if (showingHint) parsedHintColor else parsedTxtColor,
                            maxLines = maxLinesCount,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (node.layoutWidth == "match_parent") Modifier.weight(1f, fill = true) else Modifier,
                        )

                        when (node.type) {
                            IdeWidgetType.AUTO_COMPLETE_TEXT -> {
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp),
                                )
                            }

                            IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFE2E8F0))
                                        .padding(horizontal = 4.dp, vertical = 1.dp),
                                ) {
                                    Text(
                                        text = ",▾",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = Color(0xFF475569),
                                    )
                                }
                            }

                            IdeWidgetType.CHECKED_TEXT_VIEW -> {
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (node.checked) Color(0xFF0284C7) else Color.Transparent)
                                        .border(
                                            width = 1.3.dp,
                                            color = if (node.checked) Color(0xFF0284C7) else Color(0xFF64748B),
                                            shape = RoundedCornerShape(3.dp),
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (node.checked) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp),
                                        )
                                    }
                                }
                            }

                            else -> {}
                        }
                    }

                    // Standard Android EditText / AutoComplete bottom underline bar
                    if (node.type == IdeWidgetType.EDIT_TEXT ||
                        node.type == IdeWidgetType.AUTO_COMPLETE_TEXT ||
                        node.type == IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT
                    ) {
                        Spacer(Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .then(
                                    if (node.layoutWidth == "match_parent") Modifier.fillMaxWidth() else Modifier.width(88.dp),
                                )
                                .height(1.4.dp)
                                .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B)),
                        )
                    }
                }
            }
        }

        IdeWidgetType.IMAGE_VIEW,
        IdeWidgetType.CIRCLE_IMAGE_VIEW,
        IdeWidgetType.IMAGE_BUTTON,
        IdeWidgetType.ROUNDED_IMAGE_VIEW,
        IdeWidgetType.SHAPEABLE_IMAGE_VIEW,
        IdeWidgetType.ZOOM_IMAGE_VIEW,
        IdeWidgetType.ASPECT_IMAGE_VIEW,
        IdeWidgetType.IMAGE_FILTER_VIEW -> {
            val matchedImage = remember(projectImages, node.imageResName) {
                projectImages.firstOrNull { it.resName == node.imageResName }
            }
            val decodedBitmap = remember(matchedImage?.filePath) {
                matchedImage?.filePath?.let { path ->
                    runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
                }
            }
            val contentScale = remember(node.scaleType) {
                when (node.scaleType.uppercase()) {
                    "CENTER_CROP" -> ContentScale.Crop
                    "FIT_XY" -> ContentScale.FillBounds
                    "CENTER_INSIDE" -> ContentScale.Inside
                    "CENTER" -> ContentScale.None
                    else -> ContentScale.Fit
                }
            }
            val parsedTint = remember(node.imageTint) {
                parseHexColorOrNull(node.imageTint)
            }
            val parsedBorderColor = remember(node.civBorderColor) {
                parseHexColorOrNull(node.civBorderColor) ?: Color(0xFF38BDF8)
            }
            val parsedCircleBg = remember(node.civCircleBgColor) {
                parseHexColorOrNull(node.civCircleBgColor)
            }
            val isCircle = node.type == IdeWidgetType.CIRCLE_IMAGE_VIEW
            val isAspect = node.type == IdeWidgetType.ASPECT_IMAGE_VIEW
            val defaultW = if (isAspect) 84.dp else 56.dp
            val defaultH = if (isAspect) 48.dp else 56.dp

            val imgWidthMod = when {
                customWidthDp != null && customWidthDp > 0 -> Modifier.width(customWidthDp.coerceIn(24, 300).dp)
                node.layoutWidth == "match_parent" -> Modifier.fillMaxWidth()
                else -> Modifier.width(defaultW)
            }
            val imgHeightMod = when {
                customHeightDp != null && customHeightDp > 0 -> Modifier.height(customHeightDp.coerceIn(24, 260).dp)
                node.layoutHeight == "match_parent" -> Modifier.height(140.dp)
                else -> Modifier.height(defaultH)
            }

            val widgetShape = when (node.type) {
                IdeWidgetType.CIRCLE_IMAGE_VIEW -> CircleShape
                IdeWidgetType.ROUNDED_IMAGE_VIEW -> RoundedCornerShape(node.cornerRadiusDp.coerceAtLeast(12).dp)
                IdeWidgetType.SHAPEABLE_IMAGE_VIEW -> RoundedCornerShape(node.cornerRadiusDp.coerceAtLeast(16).dp)
                IdeWidgetType.IMAGE_BUTTON -> RoundedCornerShape(node.cornerRadiusDp.coerceAtLeast(8).dp)
                IdeWidgetType.IMAGE_FILTER_VIEW -> RoundedCornerShape(node.cornerRadiusDp.coerceAtLeast(6).dp)
                else -> RoundedCornerShape(2.dp)
            }

            val effectiveFill = when {
                isSelected -> selectedFill
                isCircle && parsedCircleBg != null -> parsedCircleBg
                node.type == IdeWidgetType.IMAGE_BUTTON && parsedBgColor == null -> Color(0xFFE2E8F0)
                else -> baseFill
            }

            val borderWidth = when {
                node.civBorderWidthDp > 0 && (isCircle || node.type == IdeWidgetType.ROUNDED_IMAGE_VIEW || node.type == IdeWidgetType.SHAPEABLE_IMAGE_VIEW) ->
                    node.civBorderWidthDp.coerceIn(1, 12).dp
                isSelected -> 1.2.dp
                else -> 0.8.dp
            }
            val borderColor = when {
                isSelected -> Color(0xFF0284C7)
                node.civBorderWidthDp > 0 && (isCircle || node.type == IdeWidgetType.ROUNDED_IMAGE_VIEW || node.type == IdeWidgetType.SHAPEABLE_IMAGE_VIEW) ->
                    parsedBorderColor
                else -> Color(0xFF94A3B8)
            }

            Box(
                modifier = Modifier
                    .padding(visualMargin)
                    .then(imgWidthMod)
                    .then(imgHeightMod)
                    .then(transformModifier)
                    .clip(widgetShape)
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        if (!isBeingMoved) {
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(effectiveFill, widgetShape)
                    .border(
                        width = borderWidth,
                        color = borderColor,
                        shape = widgetShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (isSelected) {
                            onDoubleTapWidget(node)
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
                    .padding((node.paddingDp / 2).coerceIn(2, 12).dp),
                contentAlignment = Alignment.Center,
            ) {
                if (decodedBitmap != null) {
                    Image(
                        bitmap = decodedBitmap.asImageBitmap(),
                        contentDescription = node.viewId,
                        contentScale = contentScale,
                        colorFilter = parsedTint?.let { ColorFilter.tint(it) },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(widgetShape),
                    )
                } else {
                    // Distinct placeholder icon for CircleImageView vs ImageView / specialized image views
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        val iconTint = parsedTint ?: Color(0xFF475569)
                        Canvas(Modifier.size(26.dp)) {
                            val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                            if (isCircle) {
                                val c = Offset(size.width / 2f, size.height / 2f)
                                drawCircle(
                                    color = iconTint,
                                    radius = size.minDimension * 0.44f,
                                    center = c,
                                    style = stroke,
                                )
                                drawCircle(
                                    color = iconTint,
                                    radius = size.minDimension * 0.16f,
                                    center = Offset(c.x, size.height * 0.38f),
                                )
                                drawArc(
                                    color = iconTint,
                                    startAngle = 200f,
                                    sweepAngle = 140f,
                                    useCenter = false,
                                    topLeft = Offset(size.width * 0.22f, size.height * 0.54f),
                                    size = Size(size.width * 0.56f, size.height * 0.40f),
                                    style = stroke,
                                )
                            } else {
                                drawRoundRect(
                                    color = iconTint,
                                    topLeft = Offset(2f, 2f),
                                    size = Size(size.width - 4f, size.height - 4f),
                                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                                    style = stroke,
                                )
                                drawCircle(
                                    color = iconTint,
                                    radius = 2.4.dp.toPx(),
                                    center = Offset(size.width * 0.34f, size.height * 0.36f),
                                )
                                val mountain = Path().apply {
                                    moveTo(size.width * 0.16f, size.height * 0.78f)
                                    lineTo(size.width * 0.45f, size.height * 0.46f)
                                    lineTo(size.width * 0.64f, size.height * 0.66f)
                                    lineTo(size.width * 0.76f, size.height * 0.54f)
                                    lineTo(size.width * 0.86f, size.height * 0.78f)
                                }
                                drawPath(mountain, color = iconTint, style = stroke)
                            }
                        }
                    }
                }
            }
        }

        // 4. Buttons Widgets (Button, MaterialButton, OutlinedButton, TextButton, FAB, ExtendedFAB, ToggleButton, ChipButton)
        IdeWidgetType.BUTTON,
        IdeWidgetType.MATERIAL_BUTTON,
        IdeWidgetType.OUTLINED_BUTTON,
        IdeWidgetType.TEXT_BUTTON,
        IdeWidgetType.FAB_BUTTON,
        IdeWidgetType.EXTENDED_FAB,
        IdeWidgetType.TOGGLE_BUTTON,
        IdeWidgetType.CHIP_BUTTON -> {
            val isFab = node.type == IdeWidgetType.FAB_BUTTON
            val btnShape = when (node.type) {
                IdeWidgetType.FAB_BUTTON -> CircleShape
                IdeWidgetType.EXTENDED_FAB, IdeWidgetType.CHIP_BUTTON -> RoundedCornerShape(node.cornerRadiusDp.coerceAtLeast(18).dp)
                IdeWidgetType.TEXT_BUTTON -> RoundedCornerShape(6.dp)
                else -> RoundedCornerShape(node.cornerRadiusDp.coerceAtLeast(6).dp)
            }
            val defaultBtnBg = when (node.type) {
                IdeWidgetType.OUTLINED_BUTTON, IdeWidgetType.TEXT_BUTTON -> Color.Transparent
                IdeWidgetType.TOGGLE_BUTTON -> if (node.checked) Color(0xFFDBEAFE) else Color(0xFFE2E8F0)
                IdeWidgetType.CHIP_BUTTON -> if (node.checked) Color(0xFFBAE6FD) else Color(0xFFF1F5F9)
                else -> Color(0xFF2563EB)
            }
            val btnFill = when {
                isSelected -> selectedFill
                parsedBgColor != null -> parsedBgColor
                else -> defaultBtnBg
            }
            val parsedTxtColor = remember(node.textColor, node.type, node.checked) {
                parseHexColorOrNull(node.textColor) ?: when (node.type) {
                    IdeWidgetType.OUTLINED_BUTTON, IdeWidgetType.TEXT_BUTTON -> Color(0xFF2563EB)
                    IdeWidgetType.TOGGLE_BUTTON, IdeWidgetType.CHIP_BUTTON -> Color(0xFF0F172A)
                    else -> Color.White
                }
            }
            val parsedStrokeColor = remember(node.civBorderColor) {
                parseHexColorOrNull(node.civBorderColor) ?: Color(0xFF2563EB)
            }
            val strokeW = when {
                isSelected -> 1.4.dp
                node.civBorderWidthDp > 0 -> node.civBorderWidthDp.coerceIn(1, 8).dp
                node.type == IdeWidgetType.OUTLINED_BUTTON -> 1.2.dp
                else -> 0.dp
            }
            val rawBtnText = when (node.type) {
                IdeWidgetType.TOGGLE_BUTTON -> if (node.checked) node.textOn.ifBlank { "ON" } else node.textOff.ifBlank { "OFF" }
                else -> node.text.ifBlank { node.type.displayName }
            }
            val displayBtnText = if (node.allCaps) rawBtnText.uppercase() else rawBtnText

            Box(
                modifier = Modifier
                    .padding(visualMargin)
                    .then(
                        if (isFab) {
                            Modifier.size((customWidthDp ?: 44).coerceIn(32, 96).dp)
                        } else {
                            widthModifier.then(
                                if (customHeightDp != null && customHeightDp > 0) {
                                    Modifier.height(customHeightDp.coerceIn(24, 160).dp)
                                } else {
                                    Modifier.heightIn(min = 32.dp)
                                },
                            )
                        },
                    )
                    .then(transformModifier)
                    .shadow(if (node.type == IdeWidgetType.TEXT_BUTTON || node.type == IdeWidgetType.OUTLINED_BUTTON) 0.dp else node.elevationDp.coerceIn(0, 12).dp, btnShape)
                    .clip(btnShape)
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        if (!isBeingMoved) {
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(btnFill, btnShape)
                    .then(
                        if (strokeW > 0.dp) {
                            Modifier.border(
                                width = strokeW,
                                color = if (isSelected) Color(0xFF0284C7) else parsedStrokeColor,
                                shape = btnShape,
                            )
                        } else {
                            Modifier
                        },
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (isSelected) {
                            onDoubleTapWidget(node)
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
                    .padding(horizontal = if (isFab) 0.dp else 12.dp, vertical = if (isFab) 0.dp else 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (isFab) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = node.viewId,
                        tint = if (isSelected) Color(0xFF0369A1) else parsedTxtColor,
                        modifier = Modifier.size(20.dp),
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        if (node.type == IdeWidgetType.EXTENDED_FAB) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFF0369A1) else parsedTxtColor,
                                modifier = Modifier.size(14.dp),
                            )
                        } else if (node.type == IdeWidgetType.CHIP_BUTTON && node.checked) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(13.dp),
                            )
                        }
                        Text(
                            text = displayBtnText,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = (node.textSizeSp - 1).coerceIn(9, 24).sp,
                                fontWeight = if (node.textStyle.contains("bold")) FontWeight.Bold else FontWeight.Medium,
                                fontStyle = if (node.textStyle.contains("italic")) FontStyle.Italic else FontStyle.Normal,
                            ),
                            color = if (isSelected) Color(0xFF0F172A) else parsedTxtColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // 5. Choose Widgets (CheckBox, RadioButton, Switch, Spinner)
        IdeWidgetType.CHECK_BOX,
        IdeWidgetType.RADIO_BUTTON,
        IdeWidgetType.SWITCH,
        IdeWidgetType.SPINNER -> {
            val parsedTxtColor = remember(node.textColor) {
                parseHexColorOrNull(node.textColor) ?: Color(0xFF1E293B)
            }
            val parsedBtnTint = remember(node.buttonTint) {
                parseHexColorOrNull(node.buttonTint) ?: Color(0xFF0284C7)
            }
            val parsedTrackTint = remember(node.trackTint) {
                parseHexColorOrNull(node.trackTint) ?: Color(0xFF93C5FD)
            }

            Row(
                modifier = Modifier
                    .padding(visualMargin)
                    .then(widthModifier)
                    .then(
                        if (customHeightDp != null && customHeightDp > 0) {
                            Modifier.height(customHeightDp.coerceIn(22, 160).dp)
                        } else {
                            Modifier.heightIn(min = 28.dp)
                        },
                    )
                    .then(transformModifier)
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        if (!isBeingMoved) {
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(baseFill, RoundedCornerShape(4.dp))
                    .then(
                        if (node.type == IdeWidgetType.SPINNER) {
                            Modifier.border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF0284C7) else Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(6.dp),
                            )
                        } else if (isSelected) {
                            Modifier.border(1.dp, Color(0xFF0284C7), RoundedCornerShape(4.dp))
                        } else {
                            Modifier
                        },
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (isSelected) {
                            onDoubleTapWidget(node)
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
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                when (node.type) {
                    IdeWidgetType.CHECK_BOX -> {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (node.checked) parsedBtnTint else Color.Transparent)
                                .border(
                                    width = 1.5.dp,
                                    color = if (node.checked) parsedBtnTint else Color(0xFF64748B),
                                    shape = RoundedCornerShape(3.dp),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (node.checked) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp),
                                )
                            }
                        }
                        Text(
                            text = node.text.ifBlank { "CheckBox" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (node.textSizeSp - 1).coerceIn(9, 24).sp,
                                fontWeight = if (node.textStyle.contains("bold")) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = parsedTxtColor,
                            maxLines = 1,
                        )
                    }

                    IdeWidgetType.RADIO_BUTTON -> {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 1.6.dp,
                                    color = if (node.checked) parsedBtnTint else Color(0xFF64748B),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (node.checked) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(parsedBtnTint),
                                )
                            }
                        }
                        Text(
                            text = node.text.ifBlank { "RadioButton" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (node.textSizeSp - 1).coerceIn(9, 24).sp,
                                fontWeight = if (node.textStyle.contains("bold")) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = parsedTxtColor,
                            maxLines = 1,
                        )
                    }

                    IdeWidgetType.SWITCH -> {
                        Text(
                            text = node.text.ifBlank { "Switch" },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (node.textSizeSp - 1).coerceIn(9, 24).sp,
                                fontWeight = if (node.textStyle.contains("bold")) FontWeight.Bold else FontWeight.Normal,
                            ),
                            color = parsedTxtColor,
                            maxLines = 1,
                        )
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(17.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(if (node.checked) parsedTrackTint else Color(0xFFCBD5E1))
                                .padding(2.dp),
                            contentAlignment = if (node.checked) Alignment.CenterEnd else Alignment.CenterStart,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(CircleShape)
                                    .background(if (node.checked) parsedBtnTint else Color.White),
                            )
                        }
                    }

                    IdeWidgetType.SPINNER -> {
                        val firstEntry = remember(node.spinnerEntries) {
                            node.spinnerEntries.split(",").map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: "Select item"
                        }
                        Text(
                            text = firstEntry,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = (node.textSizeSp - 1).coerceIn(9, 22).sp,
                                fontWeight = FontWeight.Medium,
                            ),
                            color = parsedTxtColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (node.layoutWidth == "match_parent") Modifier.weight(1f) else Modifier,
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    else -> {}
                }
            }
        }

        // 6. Web Widgets (WebView + 4 Web display widgets: CustomTabView, GeckoWebView, AdBlockWebView, SwipeWebView)
        IdeWidgetType.WEB_VIEW,
        IdeWidgetType.CUSTOM_TAB_VIEW,
        IdeWidgetType.GECKO_WEB_VIEW,
        IdeWidgetType.AD_WEB_VIEW,
        IdeWidgetType.SWIPE_WEB_VIEW -> {
            val webHeightDp = customHeightDp?.coerceIn(48, 280)?.dp ?: 110.dp
            val webShape = RoundedCornerShape(6.dp)
            Column(
                modifier = Modifier
                    .padding(visualMargin)
                    .then(
                        when {
                            customWidthDp != null && customWidthDp > 0 -> Modifier.width(customWidthDp.coerceIn(60, 320).dp)
                            node.layoutWidth == "match_parent" -> Modifier.fillMaxWidth()
                            else -> Modifier.width(180.dp)
                        },
                    )
                    .height(webHeightDp)
                    .then(transformModifier)
                    .clip(webShape)
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        if (!isBeingMoved) {
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(if (isSelected) selectedFill else Color(0xFFF8FAFC), webShape)
                    .border(
                        width = if (isSelected) 1.4.dp else 1.dp,
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF94A3B8),
                        shape = webShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (isSelected) {
                            onDoubleTapWidget(node)
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
                    },
            ) {
                // Browser URL address bar header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    SketchWidgetIcon(type = node.type, tint = Color(0xFF0284C7))
                    Text(
                        text = node.webUrl.ifBlank { "https://google.com" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 8.5.sp,
                        ),
                        color = Color(0xFF334155),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (node.webJavaScript) {
                        Text(
                            text = "JS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Color(0xFF0284C7),
                        )
                    }
                }

                // Web content body preview
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        SketchWidgetIcon(type = node.type, tint = Color(0xFF475569))
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = "${node.type.displayName} (${node.viewId})",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Color(0xFF334155),
                        )
                        Text(
                            text = node.webUrl,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }

        // 7. List Widgets (GridView, ListView, TreeView, RecyclerView, ViewPager + 5 specialized List widgets)
        IdeWidgetType.GRID_VIEW,
        IdeWidgetType.LIST_VIEW,
        IdeWidgetType.TREE_VIEW,
        IdeWidgetType.RECYCLER_VIEW,
        IdeWidgetType.VIEW_PAGER,
        IdeWidgetType.EXPANDABLE_LIST_VIEW,
        IdeWidgetType.STAGGERED_GRID_VIEW,
        IdeWidgetType.HORIZONTAL_LIST_VIEW,
        IdeWidgetType.CAROUSEL_VIEW,
        IdeWidgetType.SWIPE_LIST_VIEW -> {
            val listHeightDp = customHeightDp?.coerceIn(56, 280)?.dp ?: 110.dp
            val listShape = RoundedCornerShape(6.dp)
            Column(
                modifier = Modifier
                    .padding(visualMargin)
                    .then(
                        when {
                            customWidthDp != null && customWidthDp > 0 -> Modifier.width(customWidthDp.coerceIn(60, 320).dp)
                            node.layoutWidth == "match_parent" -> Modifier.fillMaxWidth()
                            else -> Modifier.width(180.dp)
                        },
                    )
                    .height(listHeightDp)
                    .then(transformModifier)
                    .clip(listShape)
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        if (!isBeingMoved) {
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(if (isSelected) selectedFill else Color(0xFFF8FAFC), listShape)
                    .border(
                        width = if (isSelected) 1.4.dp else 1.dp,
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF94A3B8),
                        shape = listShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (isSelected) {
                            onDoubleTapWidget(node)
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
                    },
            ) {
                // Header strip showing widget type, viewId, and Custom View / Columns info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE2E8F0))
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    SketchWidgetIcon(type = node.type, tint = Color(0xFF0284C7))
                    Text(
                        text = "${node.type.displayName} (${node.viewId})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (node.customView != "none" && node.customView.isNotBlank()) {
                        Text(
                            text = node.customView,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 7.5.sp,
                            ),
                            color = Color(0xFF0284C7),
                            maxLines = 1,
                        )
                    }
                }

                // Body preview of list items / grid cells / pager cards
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                ) {
                    when (node.type) {
                        IdeWidgetType.GRID_VIEW,
                        IdeWidgetType.STAGGERED_GRID_VIEW -> {
                            val cols = node.numColumns.coerceIn(1, 4)
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                for (r in 0..1) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        for (c in 1..cols) {
                                            val idx = r * cols + c
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .fillMaxHeight()
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFFE2E8F0))
                                                    .border(0.8.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp)),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Text(
                                                    text = "Item $idx",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontFamily = JetBrainsMono,
                                                        fontSize = 8.sp,
                                                    ),
                                                    color = Color(0xFF475569),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        IdeWidgetType.VIEW_PAGER,
                        IdeWidgetType.HORIZONTAL_LIST_VIEW,
                        IdeWidgetType.CAROUSEL_VIEW -> {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                for (p in 1..3) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(if (p == 1) Color(0xFFDBEAFE) else Color(0xFFE2E8F0))
                                            .border(
                                                0.8.dp,
                                                if (p == 1) Color(0xFF3B82F6) else Color(0xFFCBD5E1),
                                                RoundedCornerShape(5.dp),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "Page $p",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMono,
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                            ),
                                            color = Color(0xFF1E3A8A),
                                        )
                                    }
                                }
                            }
                        }

                        IdeWidgetType.TREE_VIEW -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                listOf("▾ Root Folder" to 0.dp, "  ├─ Child Node 1" to 10.dp, "  └─ Child Node 2" to 10.dp).forEach { (label, indentDp) ->
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontSize = 8.5.sp,
                                        ),
                                        color = Color(0xFF334155),
                                        modifier = Modifier.padding(start = indentDp),
                                    )
                                }
                            }
                        }

                        else -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                for (idx in 1..3) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFE2E8F0).copy(alpha = 0.65f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = if (node.type == IdeWidgetType.EXPANDABLE_LIST_VIEW) "▾ Group Item $idx" else "List Item $idx",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMono,
                                                fontSize = 8.5.sp,
                                            ),
                                            color = Color(0xFF334155),
                                        )
                                        Text(
                                            text = "#$idx",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMono,
                                                fontSize = 7.5.sp,
                                            ),
                                            color = Color(0xFF64748B),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Date & Time Widgets (AnalogClock, DigitalClock, TimePicker, DatePicker, CalendarView + 6 Date/Time widgets)
        IdeWidgetType.ANALOG_CLOCK,
        IdeWidgetType.DIGITAL_CLOCK,
        IdeWidgetType.TIME_PICKER,
        IdeWidgetType.DATE_PICKER,
        IdeWidgetType.CALENDAR_VIEW,
        IdeWidgetType.TEXT_CLOCK,
        IdeWidgetType.CHRONOMETER,
        IdeWidgetType.COUNTDOWN_VIEW,
        IdeWidgetType.STOPWATCH_VIEW,
        IdeWidgetType.DATE_RANGE_PICKER,
        IdeWidgetType.WORLD_CLOCK_VIEW -> {
            val dateShape = if (node.type == IdeWidgetType.ANALOG_CLOCK) CircleShape else RoundedCornerShape(8.dp)
            val parsedTxtColor = parseHexColorOrNull(node.textColor) ?: Color(0xFF0F172A)
            Box(
                modifier = Modifier
                    .padding(visualMargin)
                    .then(
                        when {
                            node.type == IdeWidgetType.ANALOG_CLOCK -> {
                                val d = (customWidthDp ?: customHeightDp ?: 68).coerceIn(44, 180).dp
                                Modifier.size(d)
                            }
                            customWidthDp != null && customWidthDp > 0 -> Modifier.width(customWidthDp.coerceIn(48, 320).dp)
                            node.layoutWidth == "match_parent" -> Modifier.fillMaxWidth()
                            else -> Modifier.wrapContentWidth()
                        },
                    )
                    .then(
                        when {
                            node.type == IdeWidgetType.ANALOG_CLOCK -> Modifier
                            customHeightDp != null && customHeightDp > 0 -> Modifier.height(customHeightDp.coerceIn(32, 260).dp)
                            else -> Modifier
                        },
                    )
                    .then(transformModifier)
                    .clip(dateShape)
                    .onGloballyPositioned { coords ->
                        val rect = coords.boundsInRoot()
                        nodeBoundsInRoot = rect
                        if (!isBeingMoved) {
                            onWidgetPositioned(node.uid, rect)
                        }
                    }
                    .background(
                        color = if (isSelected) selectedFill else (parsedBgColor ?: Color(0xFFF8FAFC)),
                        shape = dateShape,
                    )
                    .border(
                        width = if (isSelected) 1.4.dp else 1.dp,
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFFCBD5E1),
                        shape = dateShape,
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        if (isSelected) {
                            onDoubleTapWidget(node)
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
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                when (node.type) {
                    IdeWidgetType.ANALOG_CLOCK -> {
                        Canvas(Modifier.fillMaxSize()) {
                            val c = Offset(size.width / 2f, size.height / 2f)
                            val r = size.minDimension * 0.44f
                            drawCircle(Color(0xFF0284C7), radius = r, center = c, style = Stroke(width = 2.dp.toPx()))
                            for (tick in 0 until 12) {
                                val angle = Math.toRadians((tick * 30).toDouble())
                                val startR = r * 0.80f
                                drawLine(
                                    color = Color(0xFF475569),
                                    start = Offset(c.x + (startR * kotlin.math.sin(angle)).toFloat(), c.y - (startR * kotlin.math.cos(angle)).toFloat()),
                                    end = Offset(c.x + (r * 0.94f * kotlin.math.sin(angle)).toFloat(), c.y - (r * 0.94f * kotlin.math.cos(angle)).toFloat()),
                                    strokeWidth = 1.2.dp.toPx(),
                                )
                            }
                            // Hour & Minute hands
                            drawLine(Color(0xFF0F172A), c, Offset(c.x, c.y - r * 0.52f), strokeWidth = 2.2.dp.toPx(), cap = StrokeCap.Round)
                            drawLine(Color(0xFF0284C7), c, Offset(c.x + r * 0.62f, c.y), strokeWidth = 1.8.dp.toPx(), cap = StrokeCap.Round)
                            drawCircle(Color(0xFF0284C7), radius = 2.5.dp.toPx(), center = c)
                        }
                    }

                    IdeWidgetType.DIGITAL_CLOCK,
                    IdeWidgetType.TEXT_CLOCK,
                    IdeWidgetType.CHRONOMETER,
                    IdeWidgetType.COUNTDOWN_VIEW,
                    IdeWidgetType.STOPWATCH_VIEW,
                    IdeWidgetType.WORLD_CLOCK_VIEW -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            SketchWidgetIcon(type = node.type, tint = Color(0xFF0284C7))
                            Text(
                                text = node.text.ifBlank {
                                    when (node.type) {
                                        IdeWidgetType.CHRONOMETER, IdeWidgetType.STOPWATCH_VIEW -> "00:00"
                                        IdeWidgetType.COUNTDOWN_VIEW -> "05:00"
                                        IdeWidgetType.WORLD_CLOCK_VIEW -> "${node.timeZone}: 12:30"
                                        else -> if (node.is24HourView) "14:30:45" else "02:30:45 PM"
                                    }
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = node.textSizeSp.coerceIn(10, 26).sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = parsedTxtColor,
                            )
                        }
                    }

                    IdeWidgetType.TIME_PICKER -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                SketchWidgetIcon(type = node.type, tint = Color(0xFF0284C7))
                                Text(
                                    text = if (node.is24HourView) "14 : 30" else "02 : 30 PM",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color(0xFF0F172A),
                                )
                            }
                            Text(
                                text = "TimePicker (${node.pickerMode} • ${node.viewId})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 8.5.sp,
                                ),
                                color = Color(0xFF64748B),
                            )
                        }
                    }

                    IdeWidgetType.DATE_PICKER,
                    IdeWidgetType.CALENDAR_VIEW,
                    IdeWidgetType.DATE_RANGE_PICKER -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF0284C7))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = if (node.type == IdeWidgetType.DATE_RANGE_PICKER) "Oct 17 – Oct 24, 2026" else "October 2026",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Color.White,
                                )
                                Text(
                                    text = node.viewId,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 8.sp,
                                    ),
                                    color = Color.White.copy(alpha = 0.85f),
                                )
                            }
                            // Mini calendar days row preview
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { idx, d ->
                                    Text(
                                        text = d,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = if (idx == 6) Color(0xFF0284C7) else Color(0xFF64748B),
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                (11..17).forEach { dayNum ->
                                    val highlight = dayNum == 17
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(if (highlight) Color(0xFF0284C7) else Color.Transparent),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = dayNum.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = JetBrainsMono,
                                                fontSize = 8.sp,
                                                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                                            ),
                                            color = if (highlight) Color.White else Color(0xFF1E293B),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> {}
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
) {
    IMAGE(title = "Image", cardLabel = "Image"),
    SCALE_TYPE(title = "Scale type", cardLabel = "Scale type"),
    TINT(title = "Tint", cardLabel = "Tint"),
    BORDER_WIDTH(title = "Border / Stroke width", cardLabel = "Border\nwidth"),
    BORDER_COLOR(title = "Border / Stroke color", cardLabel = "Border\ncolor"),
    CIRCLE_BG_COLOR(title = "Circle background", cardLabel = "Circle\nbackground"),
    CORNER_RADIUS(title = "Corner radius", cardLabel = "Corner\nradius"),
    ELEVATION(title = "Elevation", cardLabel = "Elevation"),
    RIPPLE_COLOR(title = "Ripple color", cardLabel = "Ripple\ncolor"),
    ALL_CAPS(title = "All caps", cardLabel = "All caps"),
    ENABLED(title = "Enabled", cardLabel = "Enabled"),
    BUTTON_TINT(title = "Button / Thumb tint", cardLabel = "Button\ntint"),
    TRACK_TINT(title = "Track tint", cardLabel = "Track\ntint"),
    TEXT_ON(title = "Text On", cardLabel = "Text On"),
    TEXT_OFF(title = "Text Off", cardLabel = "Text Off"),
    SPINNER_ENTRIES(title = "Spinner entries", cardLabel = "Entries"),
    SPINNER_MODE(title = "Spinner mode", cardLabel = "Spinner\nmode"),
    SPINNER_PROMPT(title = "Spinner prompt", cardLabel = "Prompt"),
    WEB_URL(title = "Web URL", cardLabel = "Web URL"),
    WEB_JAVASCRIPT(title = "JavaScript", cardLabel = "JavaScript"),
    WEB_DOM_STORAGE(title = "DOM storage", cardLabel = "DOM\nstorage"),
    WEB_ZOOM(title = "Support zoom", cardLabel = "Zoom\ncontrols"),
    WEB_DESKTOP_MODE(title = "Desktop mode", cardLabel = "Desktop\nmode"),
    WEB_FILE_ACCESS(title = "File access", cardLabel = "File\naccess"),
    WEB_CACHE_MODE(title = "Cache mode", cardLabel = "Cache\nmode"),
    WEB_USER_AGENT(title = "User agent", cardLabel = "User\nagent"),
    ORIENTATION(title = "Orientation", cardLabel = "Orientation"),
    CUSTOM_VIEW(title = "Custom View", cardLabel = "Custom\nView"),
    DIVIDER_HEIGHT(title = "Divider height", cardLabel = "Divider\nheight"),
    NUM_COLUMNS(title = "Columns / Span count", cardLabel = "Columns"),
    HORIZONTAL_SPACING(title = "Horizontal spacing", cardLabel = "Horiz\nspacing"),
    VERTICAL_SPACING(title = "Vertical spacing", cardLabel = "Vert\nspacing"),
    CHOICE_MODE(title = "Choice mode", cardLabel = "Choice\nmode"),
    LAYOUT_MANAGER(title = "Layout manager", cardLabel = "Layout\nmanager"),
    STACK_FROM_BOTTOM(title = "Stack from bottom", cardLabel = "Stack\nbottom"),
    FAST_SCROLL(title = "Fast scroll", cardLabel = "Fast\nscroll"),
    PAGE_LIMIT(title = "Offscreen page limit", cardLabel = "Page\nlimit"),
    TIME_FORMAT_12H(title = "Format 12h", cardLabel = "Format\n12h"),
    TIME_FORMAT_24H(title = "Format 24h", cardLabel = "Format\n24h"),
    IS_24_HOUR_VIEW(title = "24-Hour view", cardLabel = "24-Hour\nview"),
    PICKER_MODE(title = "Picker mode", cardLabel = "Picker\nmode"),
    FIRST_DAY_OF_WEEK(title = "First day of week", cardLabel = "First\nday"),
    MIN_DATE(title = "Min date", cardLabel = "Min date"),
    MAX_DATE(title = "Max date", cardLabel = "Max date"),
    SHOW_WEEK_NUMBER(title = "Show week number", cardLabel = "Week\nnumber"),
    TIME_ZONE(title = "Time zone", cardLabel = "Time\nzone"),
    COUNT_DOWN(title = "Count down", cardLabel = "Count\ndown"),
    CUSTOM_ATTRIBUTES(title = "Custom attributes", cardLabel = "Custom\nattributes"),
    CONVERT(title = "Convert", cardLabel = "Convert"),
    WIDTH(title = "Width", cardLabel = "Width"),
    HEIGHT(title = "Height", cardLabel = "Height"),
    PADDING(title = "Padding", cardLabel = "Padding"),
    MARGIN(title = "Margin", cardLabel = "Margin"),
    GRAVITY(title = "Gravity", cardLabel = "Gravity"),
    LAYOUT_GRAVITY(title = "Layout gravity", cardLabel = "Layout gravity"),
    WEIGHT(title = "Weight", cardLabel = "Weight"),
    TEXT(title = "Text", cardLabel = "Text"),
    HINT(title = "Hint", cardLabel = "Hint"),
    HINT_COLOR(title = "Hint color", cardLabel = "Hint color"),
    INPUT_TYPE(title = "Input type", cardLabel = "Input type"),
    IME_OPTIONS(title = "IME options", cardLabel = "IME\noptions"),
    SINGLE_LINE(title = "Single line", cardLabel = "Single line"),
    CHECKED(title = "Checked", cardLabel = "Checked"),
    COMPLETION_THRESHOLD(title = "Completion threshold", cardLabel = "Threshold"),
    TEXT_SIZE(title = "Text size", cardLabel = "Text size"),
    TEXT_STYLE(title = "Text style", cardLabel = "Text style"),
    TEXT_COLOR(title = "Text color", cardLabel = "Text color"),
    LINES(title = "Lines", cardLabel = "Lines"),
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

private fun isPropertyAvailableForWidget(prop: IdeWidgetProperty, type: IdeWidgetType): Boolean = when (prop) {
    IdeWidgetProperty.IMAGE -> type.isImageWidget || type == IdeWidgetType.FAB_BUTTON
    IdeWidgetProperty.SCALE_TYPE,
    IdeWidgetProperty.TINT -> type.isImageWidget

    IdeWidgetProperty.BORDER_WIDTH,
    IdeWidgetProperty.BORDER_COLOR ->
        type == IdeWidgetType.CIRCLE_IMAGE_VIEW ||
            type == IdeWidgetType.ROUNDED_IMAGE_VIEW ||
            type == IdeWidgetType.SHAPEABLE_IMAGE_VIEW ||
            type.isButtonWidget

    IdeWidgetProperty.CIRCLE_BG_COLOR -> type == IdeWidgetType.CIRCLE_IMAGE_VIEW

    IdeWidgetProperty.CORNER_RADIUS ->
        type == IdeWidgetType.ROUNDED_IMAGE_VIEW ||
            type == IdeWidgetType.SHAPEABLE_IMAGE_VIEW ||
            type == IdeWidgetType.IMAGE_BUTTON ||
            type == IdeWidgetType.IMAGE_FILTER_VIEW ||
            (type.isButtonWidget && type != IdeWidgetType.FAB_BUTTON)

    IdeWidgetProperty.ELEVATION,
    IdeWidgetProperty.RIPPLE_COLOR -> type.isButtonWidget

    IdeWidgetProperty.ALL_CAPS -> type.isButtonWidget && type != IdeWidgetType.FAB_BUTTON

    IdeWidgetProperty.ENABLED -> type.isButtonWidget || type.isChooseWidget || type.isInputTextWidget

    IdeWidgetProperty.BUTTON_TINT ->
        type == IdeWidgetType.CHECK_BOX ||
            type == IdeWidgetType.RADIO_BUTTON ||
            type == IdeWidgetType.SWITCH

    IdeWidgetProperty.TRACK_TINT -> type == IdeWidgetType.SWITCH

    IdeWidgetProperty.TEXT_ON,
    IdeWidgetProperty.TEXT_OFF ->
        type == IdeWidgetType.SWITCH ||
            type == IdeWidgetType.TOGGLE_BUTTON

    IdeWidgetProperty.SPINNER_ENTRIES,
    IdeWidgetProperty.SPINNER_MODE,
    IdeWidgetProperty.SPINNER_PROMPT -> type == IdeWidgetType.SPINNER

    IdeWidgetProperty.WEB_URL,
    IdeWidgetProperty.WEB_JAVASCRIPT,
    IdeWidgetProperty.WEB_DOM_STORAGE,
    IdeWidgetProperty.WEB_ZOOM,
    IdeWidgetProperty.WEB_DESKTOP_MODE,
    IdeWidgetProperty.WEB_FILE_ACCESS,
    IdeWidgetProperty.WEB_CACHE_MODE,
    IdeWidgetProperty.WEB_USER_AGENT -> type.isWebWidget

    IdeWidgetProperty.ORIENTATION ->
        type.isContainer ||
            type == IdeWidgetType.RECYCLER_VIEW ||
            type == IdeWidgetType.VIEW_PAGER ||
            type == IdeWidgetType.CAROUSEL_VIEW ||
            type == IdeWidgetType.STAGGERED_GRID_VIEW ||
            type == IdeWidgetType.HORIZONTAL_LIST_VIEW

    IdeWidgetProperty.CUSTOM_VIEW -> type.isListWidget

    IdeWidgetProperty.DIVIDER_HEIGHT ->
        type == IdeWidgetType.LIST_VIEW ||
            type == IdeWidgetType.EXPANDABLE_LIST_VIEW ||
            type == IdeWidgetType.SWIPE_LIST_VIEW ||
            type == IdeWidgetType.TREE_VIEW

    IdeWidgetProperty.NUM_COLUMNS ->
        type == IdeWidgetType.GRID_VIEW ||
            type == IdeWidgetType.RECYCLER_VIEW ||
            type == IdeWidgetType.STAGGERED_GRID_VIEW

    IdeWidgetProperty.HORIZONTAL_SPACING,
    IdeWidgetProperty.VERTICAL_SPACING ->
        type == IdeWidgetType.GRID_VIEW ||
            type == IdeWidgetType.STAGGERED_GRID_VIEW

    IdeWidgetProperty.CHOICE_MODE ->
        type == IdeWidgetType.LIST_VIEW ||
            type == IdeWidgetType.GRID_VIEW ||
            type == IdeWidgetType.EXPANDABLE_LIST_VIEW ||
            type == IdeWidgetType.SWIPE_LIST_VIEW

    IdeWidgetProperty.LAYOUT_MANAGER ->
        type == IdeWidgetType.RECYCLER_VIEW ||
            type == IdeWidgetType.STAGGERED_GRID_VIEW ||
            type == IdeWidgetType.HORIZONTAL_LIST_VIEW ||
            type == IdeWidgetType.TREE_VIEW

    IdeWidgetProperty.STACK_FROM_BOTTOM,
    IdeWidgetProperty.FAST_SCROLL ->
        type == IdeWidgetType.LIST_VIEW ||
            type == IdeWidgetType.RECYCLER_VIEW ||
            type == IdeWidgetType.EXPANDABLE_LIST_VIEW ||
            type == IdeWidgetType.SWIPE_LIST_VIEW

    IdeWidgetProperty.PAGE_LIMIT ->
        type == IdeWidgetType.VIEW_PAGER ||
            type == IdeWidgetType.CAROUSEL_VIEW

    IdeWidgetProperty.TIME_FORMAT_12H,
    IdeWidgetProperty.TIME_FORMAT_24H ->
        type == IdeWidgetType.DIGITAL_CLOCK ||
            type == IdeWidgetType.TEXT_CLOCK ||
            type == IdeWidgetType.WORLD_CLOCK_VIEW

    IdeWidgetProperty.IS_24_HOUR_VIEW ->
        type == IdeWidgetType.TIME_PICKER ||
            type == IdeWidgetType.DIGITAL_CLOCK ||
            type == IdeWidgetType.TEXT_CLOCK ||
            type == IdeWidgetType.WORLD_CLOCK_VIEW

    IdeWidgetProperty.PICKER_MODE ->
        type == IdeWidgetType.TIME_PICKER ||
            type == IdeWidgetType.DATE_PICKER ||
            type == IdeWidgetType.DATE_RANGE_PICKER

    IdeWidgetProperty.FIRST_DAY_OF_WEEK,
    IdeWidgetProperty.MIN_DATE,
    IdeWidgetProperty.MAX_DATE ->
        type == IdeWidgetType.CALENDAR_VIEW ||
            type == IdeWidgetType.DATE_PICKER ||
            type == IdeWidgetType.DATE_RANGE_PICKER

    IdeWidgetProperty.SHOW_WEEK_NUMBER -> type == IdeWidgetType.CALENDAR_VIEW

    IdeWidgetProperty.TIME_ZONE ->
        type == IdeWidgetType.ANALOG_CLOCK ||
            type == IdeWidgetType.DIGITAL_CLOCK ||
            type == IdeWidgetType.TEXT_CLOCK ||
            type == IdeWidgetType.WORLD_CLOCK_VIEW

    IdeWidgetProperty.COUNT_DOWN ->
        type == IdeWidgetType.CHRONOMETER ||
            type == IdeWidgetType.COUNTDOWN_VIEW ||
            type == IdeWidgetType.STOPWATCH_VIEW

    IdeWidgetProperty.TEXT,
    IdeWidgetProperty.TEXT_SIZE,
    IdeWidgetProperty.TEXT_STYLE,
    IdeWidgetProperty.TEXT_COLOR -> type.hasTextLabel

    IdeWidgetProperty.LINES,
    IdeWidgetProperty.SINGLE_LINE -> type.isTextWidget

    IdeWidgetProperty.HINT,
    IdeWidgetProperty.HINT_COLOR,
    IdeWidgetProperty.INPUT_TYPE,
    IdeWidgetProperty.IME_OPTIONS -> type.isInputTextWidget

    IdeWidgetProperty.CHECKED -> type.isCheckableWidget

    IdeWidgetProperty.COMPLETION_THRESHOLD ->
        type == IdeWidgetType.AUTO_COMPLETE_TEXT ||
            type == IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT

    else -> true
}

@Composable
private fun IdeWidgetPropertyBottomPanel(
    node: IdeWidgetNode,
    parentNode: IdeWidgetNode? = null,
    allWidgets: List<IdeWidgetNode>,
    recentProperties: List<IdeWidgetProperty>,
    existingViewLogicItems: List<IdeLogicItem> = emptyList(),
    onSelectOtherWidget: (IdeWidgetNode) -> Unit,
    onSelectParentLayout: () -> Unit = {},
    onMoveUp: () -> Unit,
    onCollapse: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onClickProperty: (IdeWidgetProperty) -> Unit,
    onToggleWidgetEvent: (IdeViewEventOption) -> Unit = {},
    bottomBarPadding: Dp = 0.dp,
) {
    var activePropTab by remember { mutableStateOf(WidgetPropTab.BASIC) }
    var showWidgetPickerMenu by remember { mutableStateOf(false) }

    // Filter properties based on selected widget type (Layouts vs Text vs Image vs Buttons vs Choose vs Web)
    val availableProperties = remember(node.type) {
        IdeWidgetProperty.entries.filter { prop ->
            isPropertyAvailableForWidget(prop, node.type)
        }
    }
    val availableEvents = remember(node.type) {
        getEventsForWidgetType(node.type)
    }
    val displayedProperties = remember(node.type, activePropTab, recentProperties.size) {
        when (activePropTab) {
            WidgetPropTab.BASIC -> availableProperties
            WidgetPropTab.RECENT -> {
                val filteredRecent = recentProperties.filter { prop ->
                    isPropertyAvailableForWidget(prop, node.type)
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
        // 1. Header Row: [Icon] viewId ▼   [↑ Parent] ...   ^   v   Trash   Save
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f, fill = false),
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
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = node.viewId,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                        color = Ink,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = Ink,
                        modifier = Modifier.size(20.dp),
                    )
                }

                // Direct 1-tap Parent Layout selector pill whenever the selected widget is inside a Layout!
                if (parentNode != null) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Cyan.copy(alpha = 0.16f))
                            .border(1.dp, Cyan.copy(alpha = 0.50f), RoundedCornerShape(50))
                            .clickable(onClick = onSelectParentLayout)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        SketchWidgetIcon(type = parentNode.type, tint = Cyan)
                        Text(
                            text = "↑ ${parentNode.viewId}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Cyan,
                            maxLines = 1,
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Up chevron (Select Parent Layout if inside a container, or move widget up)
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = if (parentNode != null) Cyan else Muted,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onSelectParentLayout)
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

        // 3. Horizontal Scrollable Row of Rounded Property / Event Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (activePropTab == WidgetPropTab.EVENT) {
                availableEvents.forEach { eventOpt ->
                    val isEnabled = existingViewLogicItems.any {
                        it.section == IdeLogicSection.VIEW &&
                            it.targetViewId == node.viewId &&
                            it.name == "${node.viewId}: ${eventOpt.eventKey}"
                    }
                    SketchEventSquareCard(
                        label = eventOpt.cardLabel,
                        isEnabled = isEnabled,
                        onClick = { onToggleWidgetEvent(eventOpt) },
                    )
                }
            } else {
                displayedProperties.forEach { prop ->
                    SketchPropertySquareCard(
                        label = prop.cardLabel,
                        highlighted = prop == IdeWidgetProperty.IMAGE,
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
        IdeWidgetProperty.IMAGE -> {
            SketchWidgetIcon(type = IdeWidgetType.IMAGE_VIEW, tint = tint)
        }

        IdeWidgetProperty.SCALE_TYPE -> {
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.12f, size.height * 0.14f),
                    size = Size(size.width * 0.76f, size.height * 0.72f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = stroke,
                )
                // Diagonal expand arrows
                drawLine(tint, Offset(size.width * 0.28f, size.height * 0.70f), Offset(size.width * 0.72f, size.height * 0.28f), stroke.width)
                drawLine(tint, Offset(size.width * 0.54f, size.height * 0.28f), Offset(size.width * 0.72f, size.height * 0.28f), stroke.width)
                drawLine(tint, Offset(size.width * 0.72f, size.height * 0.46f), Offset(size.width * 0.72f, size.height * 0.28f), stroke.width)
            }
        }

        IdeWidgetProperty.TINT -> {
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawCircle(
                    color = tint,
                    radius = size.width * 0.36f,
                    center = Offset(size.width * 0.50f, size.height * 0.50f),
                    style = stroke,
                )
                drawCircle(tint, 2.5.dp.toPx(), Offset(size.width * 0.50f, size.height * 0.50f))
            }
        }

        IdeWidgetProperty.BORDER_WIDTH -> {
            Canvas(Modifier.size(20.dp)) {
                val c = Offset(size.width * 0.50f, size.height * 0.50f)
                drawCircle(
                    color = tint,
                    radius = size.width * 0.38f,
                    center = c,
                    style = Stroke(width = 2.4.dp.toPx()),
                )
                drawCircle(
                    color = tint,
                    radius = size.width * 0.22f,
                    center = c,
                    style = Stroke(width = 1.1.dp.toPx()),
                )
            }
        }

        IdeWidgetProperty.BORDER_COLOR,
        IdeWidgetProperty.CIRCLE_BG_COLOR,
        IdeWidgetProperty.HINT_COLOR,
        IdeWidgetProperty.RIPPLE_COLOR,
        IdeWidgetProperty.BUTTON_TINT,
        IdeWidgetProperty.TRACK_TINT -> {
            Canvas(Modifier.size(20.dp)) {
                val c = Offset(size.width * 0.50f, size.height * 0.50f)
                drawCircle(
                    color = tint,
                    radius = size.width * 0.36f,
                    center = c,
                    style = Stroke(width = 1.7.dp.toPx()),
                )
                drawCircle(
                    color = tint.copy(alpha = 0.45f),
                    radius = size.width * 0.22f,
                    center = c,
                )
            }
        }

        IdeWidgetProperty.CORNER_RADIUS -> {
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.16f),
                    size = Size(size.width * 0.68f, size.height * 0.68f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = stroke,
                )
            }
        }

        IdeWidgetProperty.ELEVATION -> {
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.18f),
                    size = Size(size.width * 0.62f, size.height * 0.46f),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.24f, size.height * 0.78f), Offset(size.width * 0.84f, size.height * 0.78f), stroke.width)
            }
        }

        IdeWidgetProperty.ALL_CAPS -> {
            Text(
                text = "AA",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tint,
            )
        }

        IdeWidgetProperty.ENABLED,
        IdeWidgetProperty.WEB_JAVASCRIPT,
        IdeWidgetProperty.WEB_DOM_STORAGE,
        IdeWidgetProperty.WEB_ZOOM,
        IdeWidgetProperty.WEB_DESKTOP_MODE,
        IdeWidgetProperty.WEB_FILE_ACCESS -> {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(19.dp),
            )
        }

        IdeWidgetProperty.TEXT_ON,
        IdeWidgetProperty.TEXT_OFF,
        IdeWidgetProperty.SPINNER_PROMPT,
        IdeWidgetProperty.WEB_USER_AGENT -> {
            Text(
                text = "Ab",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tint,
            )
        }

        IdeWidgetProperty.SPINNER_ENTRIES,
        IdeWidgetProperty.SPINNER_MODE,
        IdeWidgetProperty.WEB_CACHE_MODE -> {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }

        IdeWidgetProperty.WEB_URL -> {
            SketchWidgetIcon(type = IdeWidgetType.WEB_VIEW, tint = tint)
        }

        IdeWidgetProperty.ORIENTATION -> {
            Canvas(Modifier.size(20.dp)) {
                val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                drawLine(tint, Offset(size.width * 0.16f, size.height * 0.50f), Offset(size.width * 0.84f, size.height * 0.50f), stroke.width)
                drawLine(tint, Offset(size.width * 0.50f, size.height * 0.16f), Offset(size.width * 0.50f, size.height * 0.84f), stroke.width)
            }
        }

        IdeWidgetProperty.CUSTOM_VIEW -> {
            SketchWidgetIcon(type = IdeWidgetType.LIST_VIEW, tint = tint)
        }

        IdeWidgetProperty.NUM_COLUMNS,
        IdeWidgetProperty.LAYOUT_MANAGER -> {
            SketchWidgetIcon(type = IdeWidgetType.GRID_VIEW, tint = tint)
        }

        IdeWidgetProperty.DIVIDER_HEIGHT,
        IdeWidgetProperty.HORIZONTAL_SPACING,
        IdeWidgetProperty.VERTICAL_SPACING,
        IdeWidgetProperty.PAGE_LIMIT,
        IdeWidgetProperty.FIRST_DAY_OF_WEEK -> {
            Text(
                text = "123",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tint,
            )
        }

        IdeWidgetProperty.CHOICE_MODE,
        IdeWidgetProperty.PICKER_MODE,
        IdeWidgetProperty.TIME_ZONE -> {
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
        }

        IdeWidgetProperty.STACK_FROM_BOTTOM,
        IdeWidgetProperty.FAST_SCROLL,
        IdeWidgetProperty.IS_24_HOUR_VIEW,
        IdeWidgetProperty.SHOW_WEEK_NUMBER,
        IdeWidgetProperty.COUNT_DOWN -> {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(19.dp),
            )
        }

        IdeWidgetProperty.TIME_FORMAT_12H,
        IdeWidgetProperty.TIME_FORMAT_24H -> {
            SketchWidgetIcon(type = IdeWidgetType.ANALOG_CLOCK, tint = tint)
        }

        IdeWidgetProperty.MIN_DATE,
        IdeWidgetProperty.MAX_DATE -> {
            SketchWidgetIcon(type = IdeWidgetType.CALENDAR_VIEW, tint = tint)
        }

        IdeWidgetProperty.HINT -> {
            Text(
                text = "Abc",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tint,
            )
        }

        IdeWidgetProperty.INPUT_TYPE -> {
            Text(
                text = "A/1",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tint,
            )
        }

        IdeWidgetProperty.IME_OPTIONS -> {
            Text(
                text = "⏎",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tint,
            )
        }

        IdeWidgetProperty.SINGLE_LINE -> {
            Canvas(Modifier.size(20.dp)) {
                val s = 1.8.dp.toPx()
                drawLine(tint, Offset(size.width * 0.14f, size.height * 0.50f), Offset(size.width * 0.86f, size.height * 0.50f), s, StrokeCap.Round)
            }
        }

        IdeWidgetProperty.CHECKED -> {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(19.dp),
            )
        }

        IdeWidgetProperty.COMPLETION_THRESHOLD -> {
            Text(
                text = "1..n",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = tint,
            )
        }

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

@Composable
private fun SketchEventSquareCard(
    label: String,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    val dotColor = if (isEnabled) Color(0xFF22C55E) else Color(0xFFEF4444)
    val bgColor = if (isEnabled) Color(0xFF132A26) else Color(0xFF161F40)
    val borderColor = if (isEnabled) Color(0xFF22C55E).copy(alpha = 0.65f) else Color(0x28FFFFFF)
    val contentTint = if (isEnabled) Color(0xFF4ADE80) else Ink.copy(alpha = 0.92f)

    Box(
        modifier = Modifier
            .width(88.dp)
            .height(74.dp)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
    ) {
        // Status Dot at Top-End: Green when enabled (مفعل), Red when disabled (غير مفعل)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(12.dp)
                .clip(CircleShape)
                .background(dotColor.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(7.5.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.size(18.dp)) {
                    val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    val bolt = Path().apply {
                        moveTo(size.width * 0.56f, size.height * 0.10f)
                        lineTo(size.width * 0.24f, size.height * 0.54f)
                        lineTo(size.width * 0.50f, size.height * 0.54f)
                        lineTo(size.width * 0.44f, size.height * 0.90f)
                        lineTo(size.width * 0.76f, size.height * 0.46f)
                        lineTo(size.width * 0.50f, size.height * 0.46f)
                        close()
                    }
                    drawPath(bolt, color = contentTint, style = stroke)
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    lineHeight = 11.5.sp,
                    fontWeight = if (isEnabled) FontWeight.Bold else FontWeight.Medium,
                ),
                color = contentTint,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
// 4. LOGIC Tab: Event & Functions Manager (Activity | View | Component | Drawer | Functions)
//    Matches Screenshot Layout + All Activity Events + View (OnClick / OnClickLong / OnTouch)
//    + Functions (Name, Return Type, Typed Parameters, Private Variable)
//    + Long-Press Bottom Sheet (Reset / Rename / Delete)
// ============================================================================

private data class IdeActivityEventTemplate(
    val name: String,
    val subtitle: String,
    val defaultCode: String,
)

private val ALL_ACTIVITY_EVENT_TEMPLATES = listOf(
    IdeActivityEventTemplate("onCreate", "On activity create", "// On activity create\nsetContentView(R.layout.activity_main)\ninitializeLogic()"),
    IdeActivityEventTemplate("Import", "Custom imports (import ...)", "import android.view.View\nimport android.widget.Toast"),
    IdeActivityEventTemplate("OnRename", "On activity rename / state update", "// OnRename logic"),
    IdeActivityEventTemplate("initializeLogic", "On activity create", "// Initialize logic here"),
    IdeActivityEventTemplate("onStart", "On activity start", "// On activity start"),
    IdeActivityEventTemplate("onResume", "On activity resume", "// On activity resume"),
    IdeActivityEventTemplate("onPause", "On activity pause", "// On activity pause"),
    IdeActivityEventTemplate("onStop", "On activity stop", "// On activity stop"),
    IdeActivityEventTemplate("onDestroy", "On activity destroy", "// On activity destroy"),
    IdeActivityEventTemplate("onRestart", "On activity restart", "// On activity restart"),
    IdeActivityEventTemplate("onBackPressed", "On back button pressed", "// On back button pressed"),
    IdeActivityEventTemplate("onPostCreate", "On activity post create", "// On activity post create"),
    IdeActivityEventTemplate("onSaveInstanceState", "On save instance state", "// Save instance state"),
    IdeActivityEventTemplate("onRestoreInstanceState", "On restore instance state", "// Restore instance state"),
    IdeActivityEventTemplate("onActivityResult", "On activity result", "// Handle activity result"),
    IdeActivityEventTemplate("onRequestPermissionsResult", "On request permissions result", "// Handle permission result"),
    IdeActivityEventTemplate("onConfigurationChanged", "On configuration changed", "// Handle configuration change"),
    IdeActivityEventTemplate("onCreateOptionsMenu", "On create options menu", "// Inflate options menu"),
    IdeActivityEventTemplate("onOptionsItemSelected", "On options item selected", "// Handle menu item click"),
    IdeActivityEventTemplate("onUserLeaveHint", "On user leave hint", "// On user leave hint"),
    IdeActivityEventTemplate("onWindowFocusChanged", "On window focus changed", "// On window focus changed"),
    IdeActivityEventTemplate("onTrimMemory", "On trim memory", "// On trim memory"),
    IdeActivityEventTemplate("onLowMemory", "On low memory", "// On low memory"),
    IdeActivityEventTemplate("onNewIntent", "On new intent received", "// On new intent received"),
)

private data class IdeViewEventOption(
    val eventKey: String,
    val cardLabel: String,
    val title: String,
    val subtitle: String,
)

private fun getEventsForWidgetType(type: IdeWidgetType): List<IdeViewEventOption> = when (type) {
    // 1. Text Widgets — Every Text widget has its own distinct events!
    IdeWidgetType.TEXT_VIEW -> listOf(
        IdeViewEventOption("onTextClick", "OnText\nClick", "onTextClick", "On TextView clicked • عند الضغط على النص"),
        IdeViewEventOption("onTextLongClick", "OnText\nLongClick", "onTextLongClick", "On TextView long clicked • عند الضغط المطوّل"),
        IdeViewEventOption("onTextLinkClick", "OnLink\nClick", "onTextLinkClick", "On link/URL clicked inside TextView"),
        IdeViewEventOption("onTextCopied", "OnText\nCopied", "onTextCopied", "On TextView text copied"),
    )
    IdeWidgetType.EDIT_TEXT -> listOf(
        IdeViewEventOption("onTextChanged", "OnText\nChanged", "onTextChanged", "On EditText text changed (charSeq, start, before, count)"),
        IdeViewEventOption("beforeTextChanged", "Before\nText", "beforeTextChanged", "Before EditText text changes"),
        IdeViewEventOption("afterTextChanged", "After\nText", "afterTextChanged", "After EditText editable text changed"),
        IdeViewEventOption("onFocusChange", "OnFocus\nChange", "onFocusChange", "On EditText focus gained or lost"),
        IdeViewEventOption("onEditorAction", "OnEditor\nAction", "onEditorAction", "On keyboard IME action (Done/Search/Send)"),
    )
    IdeWidgetType.AUTO_COMPLETE_TEXT -> listOf(
        IdeViewEventOption("onSuggestionClick", "OnItem\nSelect", "onSuggestionClick", "On autocomplete suggestion item selected"),
        IdeViewEventOption("onQueryFilter", "OnQuery\nFilter", "onQueryFilter", "On AutoComplete filter query updated"),
        IdeViewEventOption("onDropdownShow", "OnPopup\nShow", "onDropdownShow", "On suggestions dropdown shown"),
        IdeViewEventOption("onDropdownDismiss", "OnPopup\nDismiss", "onDropdownDismiss", "On suggestions dropdown dismissed"),
        IdeViewEventOption("onThresholdReached", "OnFilter\nReady", "onThresholdReached", "On completion threshold reached"),
    )
    IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT -> listOf(
        IdeViewEventOption("onTokenAdded", "OnToken\nAdded", "onTokenAdded", "On comma-separated token added"),
        IdeViewEventOption("onTokenRemoved", "OnToken\nRemoved", "onTokenRemoved", "On token removed from field"),
        IdeViewEventOption("onTokenizerSplit", "OnToken\nSplit", "onTokenizerSplit", "On MultiAutoComplete tokenizer split"),
        IdeViewEventOption("onMultiPopupDismiss", "OnMulti\nDismiss", "onMultiPopupDismiss", "On MultiAutoComplete popup dismissed"),
    )
    IdeWidgetType.CHECKED_TEXT_VIEW -> listOf(
        IdeViewEventOption("onCheckedChanged", "OnCheck\nToggle", "onCheckedChanged", "On CheckedTextView check state toggled"),
        IdeViewEventOption("onCheckMarkTap", "OnMark\nTap", "onCheckMarkTap", "On CheckedTextView checkmark icon tapped"),
        IdeViewEventOption("onCheckedStateRead", "OnCheck\nRead", "onCheckedStateRead", "On CheckedTextView checked state read"),
    )
    IdeWidgetType.TEXT_INPUT_EDIT_TEXT -> listOf(
        IdeViewEventOption("onInputTextChanged", "OnInput\nChanged", "onInputTextChanged", "On TextInputEditText input changed"),
        IdeViewEventOption("onEndIconClick", "OnEndIcon\nClick", "onEndIconClick", "On clear/password toggle icon clicked"),
        IdeViewEventOption("onStartIconClick", "OnStartIcon\nClick", "onStartIconClick", "On leading start icon clicked"),
        IdeViewEventOption("onErrorChanged", "OnError\nState", "onErrorChanged", "On validation error state changed"),
        IdeViewEventOption("onCounterOverflow", "OnMax\nLength", "onCounterOverflow", "On character counter max length exceeded"),
    )

    // 2. Layouts — Each Layout has its own completely distinct events!
    IdeWidgetType.LINEAR_H -> listOf(
        IdeViewEventOption("onLinearHClick", "OnRow\nClick", "onLinearHClick", "On Horizontal LinearLayout row clicked"),
        IdeViewEventOption("onChildAddedH", "OnChild\nAdded", "onChildAddedH", "On child view added to Linear(H)"),
        IdeViewEventOption("onChildRemovedH", "OnChild\nRemoved", "onChildRemovedH", "On child view removed from Linear(H)"),
        IdeViewEventOption("onWeightSumChange", "OnWeight\nChange", "onWeightSumChange", "On horizontal weight distribution updated"),
    )
    IdeWidgetType.LINEAR_V -> listOf(
        IdeViewEventOption("onLinearVClick", "OnColumn\nClick", "onLinearVClick", "On Vertical LinearLayout column clicked"),
        IdeViewEventOption("onChildAddedV", "OnItem\nAdded", "onChildAddedV", "On child view added to Linear(V)"),
        IdeViewEventOption("onLayoutBoundsChange", "OnBounds\nChange", "onLayoutBoundsChange", "On Linear(V) layout bounds changed"),
        IdeViewEventOption("onDividerDraw", "OnDivider\nChanged", "onDividerDraw", "On vertical divider state updated"),
    )
    IdeWidgetType.SCROLL_H -> listOf(
        IdeViewEventOption("onScrollYChange", "OnScrollY\nChange", "onScrollYChange", "On vertical ScrollView scroll Y changed"),
        IdeViewEventOption("onReachBottom", "OnReach\nBottom", "onReachBottom", "On ScrollView scrolled to bottom"),
        IdeViewEventOption("onReachTop", "OnReach\nTop", "onReachTop", "On ScrollView scrolled to top"),
        IdeViewEventOption("onFlingVertical", "OnFling\nScroll", "onFlingVertical", "On ScrollView vertical fling gesture"),
    )
    IdeWidgetType.SCROLL_V -> listOf(
        IdeViewEventOption("onScrollXChange", "OnScrollX\nChange", "onScrollXChange", "On HorizontalScrollView scroll X changed"),
        IdeViewEventOption("onReachRightEnd", "OnReach\nRight", "onReachRightEnd", "On HorizontalScrollView reached right end"),
        IdeViewEventOption("onReachLeftStart", "OnReach\nLeft", "onReachLeftStart", "On HorizontalScrollView reached left start"),
        IdeViewEventOption("onFlingHorizontal", "OnHoriz\nFling", "onFlingHorizontal", "On HorizontalScrollView fling gesture"),
    )

    // 3. Image Widgets — Every single Image widget has its own 100% distinct events (كل عنصر غير أحداث الآخر)!
    IdeWidgetType.IMAGE_VIEW -> listOf(
        IdeViewEventOption("onImageClick", "OnImage\nClick", "onImageClick", "On ImageView clicked • عند الضغط على الصورة"),
        IdeViewEventOption("onImageLongClick", "OnImage\nLongClick", "onImageLongClick", "On ImageView long clicked • عند الضغط المطوّل على الصورة"),
        IdeViewEventOption("onImageLoaded", "OnImage\nLoaded", "onImageLoaded", "On bitmap/drawable loaded into ImageView"),
        IdeViewEventOption("onImageLoadError", "OnLoad\nError", "onImageLoadError", "On ImageView resource load failed"),
        IdeViewEventOption("onScaleTypeChange", "OnScale\nType", "onScaleTypeChange", "On ImageView scaleType changed"),
    )
    IdeWidgetType.CIRCLE_IMAGE_VIEW -> listOf(
        IdeViewEventOption("onAvatarTap", "OnAvatar\nTap", "onAvatarTap", "On CircleImageView avatar tapped"),
        IdeViewEventOption("onAvatarDoubleTap", "OnAvatar\nDoubleTap", "onAvatarDoubleTap", "On CircleImageView avatar double tapped"),
        IdeViewEventOption("onBorderColorChange", "OnBorder\nColor", "onBorderColorChange", "On CircleImageView border color changed"),
        IdeViewEventOption("onBorderWidthChange", "OnBorder\nWidth", "onBorderWidthChange", "On CircleImageView border width changed"),
        IdeViewEventOption("onCircleCropReady", "OnCircle\nReady", "onCircleCropReady", "On circular bitmap crop completed"),
    )
    IdeWidgetType.IMAGE_BUTTON -> listOf(
        IdeViewEventOption("onBtnPressDown", "OnBtn\nPress", "onBtnPressDown", "On ImageButton pressed down"),
        IdeViewEventOption("onBtnReleaseUp", "OnBtn\nRelease", "onBtnReleaseUp", "On ImageButton released"),
        IdeViewEventOption("onRippleTrigger", "OnRipple\nEffect", "onRippleTrigger", "On ImageButton ripple animation triggered"),
        IdeViewEventOption("onIconStateChange", "OnIcon\nState", "onIconStateChange", "On ImageButton active/disabled icon changed"),
        IdeViewEventOption("onHoldRepeat", "OnHold\nRepeat", "onHoldRepeat", "On ImageButton held down repeat action"),
    )
    IdeWidgetType.ROUNDED_IMAGE_VIEW -> listOf(
        IdeViewEventOption("onCornerRadiusChange", "OnCorner\nRadius", "onCornerRadiusChange", "On RoundedImageView corner radius changed"),
        IdeViewEventOption("onRoundedCardTap", "OnRounded\nTap", "onRoundedCardTap", "On RoundedImageView card tapped"),
        IdeViewEventOption("onTopCornersClip", "OnTop\nCorners", "onTopCornersClip", "On top corners rounded clip applied"),
        IdeViewEventOption("onBottomCornersClip", "OnBottom\nCorners", "onBottomCornersClip", "On bottom corners rounded clip applied"),
        IdeViewEventOption("onShadowElevation", "OnShadow\nElevation", "onShadowElevation", "On RoundedImageView shadow elevation changed"),
    )
    IdeWidgetType.SHAPEABLE_IMAGE_VIEW -> listOf(
        IdeViewEventOption("onShapeModelChange", "OnShape\nModel", "onShapeModelChange", "On ShapeAppearanceModel changed (Cut/Rounded/Diamond)"),
        IdeViewEventOption("onStrokeOutlineTap", "OnStroke\nOutline", "onStrokeOutlineTap", "On ShapeableImageView stroke outline updated"),
        IdeViewEventOption("onCornerFamilySwitch", "OnCorner\nFamily", "onCornerFamilySwitch", "On corner family switched (Rounded vs Cut)"),
        IdeViewEventOption("onMaskPathUpdate", "OnMask\nPath", "onMaskPathUpdate", "On custom shape mask path recalculated"),
    )
    IdeWidgetType.ZOOM_IMAGE_VIEW -> listOf(
        IdeViewEventOption("onPinchZoom", "OnPinch\nZoom", "onPinchZoom", "On pinch-to-zoom scale factor changed (scaleFactor)"),
        IdeViewEventOption("onDoubleTapZoom", "OnDouble\nTapZoom", "onDoubleTapZoom", "On double-tap zoom in/out toggled"),
        IdeViewEventOption("onPanDrag", "OnPan\nDrag", "onPanDrag", "On zoomed image panned/dragged (dx, dy)"),
        IdeViewEventOption("onZoomReset", "OnZoom\nReset", "onZoomReset", "On zoom returned to 1.0x original scale"),
        IdeViewEventOption("onMaxZoomReached", "OnMax\nZoom", "onMaxZoomReached", "On maximum zoom limit reached"),
    )
    IdeWidgetType.ASPECT_IMAGE_VIEW -> listOf(
        IdeViewEventOption("onAspectRatioChange", "OnRatio\nChanged", "onAspectRatioChange", "On aspect ratio changed (16:9 / 4:3 / 1:1)"),
        IdeViewEventOption("onMeasureBounds", "OnBounds\nMeasure", "onMeasureBounds", "On AspectImageView width/height measured"),
        IdeViewEventOption("onCropFrameUpdate", "OnCrop\nFrame", "onCropFrameUpdate", "On aspect crop viewport updated"),
        IdeViewEventOption("onRatioFlip", "OnRatio\nFlip", "onRatioFlip", "On aspect ratio flipped portrait/landscape"),
    )
    IdeWidgetType.IMAGE_FILTER_VIEW -> listOf(
        IdeViewEventOption("onSaturationChange", "OnSaturate\nChange", "onSaturationChange", "On image saturation filter changed"),
        IdeViewEventOption("onContrastChange", "OnContrast\nChange", "onContrastChange", "On image contrast filter changed"),
        IdeViewEventOption("onWarmthChange", "OnWarmth\nChange", "onWarmthChange", "On image warmth/temperature changed"),
        IdeViewEventOption("onCrossfadeProgress", "OnCross\nFade", "onCrossfadeProgress", "On altSrc crossfade progress (0.0..1.0)"),
        IdeViewEventOption("onBrightnessChange", "OnBright\nChange", "onBrightnessChange", "On image brightness filter updated"),
    )

    // 4. Button Widgets — Distinct events per Button widget
    IdeWidgetType.BUTTON -> listOf(
        IdeViewEventOption("onClick", "OnClick", "onClick", "On Button clicked"),
        IdeViewEventOption("onLongClick", "OnLong\nClick", "onLongClick", "On Button long clicked"),
        IdeViewEventOption("onTouch", "OnTouch", "onTouch", "On Button touch event"),
    )
    IdeWidgetType.MATERIAL_BUTTON -> listOf(
        IdeViewEventOption("onMaterialClick", "OnMaterial\nClick", "onMaterialClick", "On MaterialButton clicked"),
        IdeViewEventOption("onIconGravityTap", "OnIcon\nTap", "onIconGravityTap", "On MaterialButton icon tapped"),
        IdeViewEventOption("onCornerMorph", "OnCorner\nMorph", "onCornerMorph", "On MaterialButton corner radius morphed"),
        IdeViewEventOption("onStrokeWidthChange", "OnStroke\nChange", "onStrokeWidthChange", "On MaterialButton stroke updated"),
    )
    IdeWidgetType.OUTLINED_BUTTON -> listOf(
        IdeViewEventOption("onOutlineClick", "OnOutline\nClick", "onOutlineClick", "On OutlinedButton clicked"),
        IdeViewEventOption("onBorderPress", "OnBorder\nPress", "onBorderPress", "On OutlinedButton border pressed"),
        IdeViewEventOption("onHoverChange", "OnHover\nState", "onHoverChange", "On OutlinedButton hover state changed"),
    )
    IdeWidgetType.TEXT_BUTTON -> listOf(
        IdeViewEventOption("onTextBtnTap", "OnTextBtn\nTap", "onTextBtnTap", "On TextButton tapped"),
        IdeViewEventOption("onRippleSpread", "OnRipple\nSpread", "onRippleSpread", "On TextButton ripple spread finished"),
        IdeViewEventOption("onLinkAction", "OnLink\nAction", "onLinkAction", "On TextButton secondary action triggered"),
    )
    IdeWidgetType.FAB_BUTTON -> listOf(
        IdeViewEventOption("onFabClick", "OnFab\nClick", "onFabClick", "On FloatingActionButton clicked"),
        IdeViewEventOption("onFabShown", "OnFab\nShown", "onFabShown", "On FAB show animation finished"),
        IdeViewEventOption("onFabHidden", "OnFab\nHidden", "onFabHidden", "On FAB hide animation finished"),
        IdeViewEventOption("onFabRotate", "OnFab\nRotate", "onFabRotate", "On FAB icon rotated"),
    )
    IdeWidgetType.EXTENDED_FAB -> listOf(
        IdeViewEventOption("onExtFabClick", "OnExtFab\nClick", "onExtFabClick", "On ExtendedFAB clicked"),
        IdeViewEventOption("onExtend", "OnExtend", "onExtend", "On ExtendedFAB expanded with text"),
        IdeViewEventOption("onShrink", "OnShrink", "onShrink", "On ExtendedFAB collapsed to icon only"),
        IdeViewEventOption("onSlideBehavior", "OnScroll\nSlide", "onSlideBehavior", "On ExtendedFAB auto-shrink on scroll"),
    )
    IdeWidgetType.TOGGLE_BUTTON -> listOf(
        IdeViewEventOption("onToggleChange", "OnToggle\nChange", "onToggleChange", "On ToggleButton switched ON/OFF"),
        IdeViewEventOption("onToggleOn", "OnTurn\nON", "onToggleOn", "When ToggleButton becomes ON"),
        IdeViewEventOption("onToggleOff", "OnTurn\nOFF", "onToggleOff", "When ToggleButton becomes OFF"),
    )
    IdeWidgetType.CHIP_BUTTON -> listOf(
        IdeViewEventOption("onChipClick", "OnChip\nClick", "onChipClick", "On Chip clicked"),
        IdeViewEventOption("onCloseIconClick", "OnClose\nIcon", "onCloseIconClick", "On Chip close (X) icon clicked"),
        IdeViewEventOption("onChipChecked", "OnChip\nCheck", "onChipChecked", "On Chip filter selection changed"),
    )

    // 5. Choose Widgets — Distinct events per Choose widget
    IdeWidgetType.CHECK_BOX -> listOf(
        IdeViewEventOption("onCheckBoxChange", "OnBox\nChecked", "onCheckBoxChange", "On CheckBox checked/unchecked (isChecked)"),
        IdeViewEventOption("onIndeterminate", "OnTri\nState", "onIndeterminate", "On CheckBox indeterminate state changed"),
        IdeViewEventOption("onCheckBoxTap", "OnBox\nTap", "onCheckBoxTap", "On CheckBox label tapped"),
    )
    IdeWidgetType.RADIO_BUTTON -> listOf(
        IdeViewEventOption("onRadioSelected", "OnRadio\nSelect", "onRadioSelected", "On RadioButton selected"),
        IdeViewEventOption("onRadioDeselected", "OnRadio\nUnselect", "onRadioDeselected", "On RadioButton unselected in group"),
        IdeViewEventOption("onGroupChange", "OnGroup\nChange", "onGroupChange", "On RadioGroup checkedId changed"),
    )
    IdeWidgetType.SWITCH -> listOf(
        IdeViewEventOption("onSwitchToggle", "OnSwitch\nToggle", "onSwitchToggle", "On Switch toggled ON/OFF (isChecked)"),
        IdeViewEventOption("onSlideThumb", "OnSlide\nThumb", "onSlideThumb", "On Switch thumb dragged"),
        IdeViewEventOption("onTrackColorChange", "OnTrack\nChange", "onTrackColorChange", "On Switch track state updated"),
    )
    IdeWidgetType.SPINNER -> listOf(
        IdeViewEventOption("onItemSelected", "OnSpinner\nSelect", "onItemSelected", "On Spinner option selected (position)"),
        IdeViewEventOption("onNothingSelected", "OnNothing\nSelected", "onNothingSelected", "When Spinner selection cleared"),
        IdeViewEventOption("onSpinnerPopupOpen", "OnMenu\nOpen", "onSpinnerPopupOpen", "On Spinner dropdown menu opened"),
    )

    // 6. Web Widgets — Distinct events per Web widget
    IdeWidgetType.WEB_VIEW -> listOf(
        IdeViewEventOption("onPageStarted", "OnPage\nStarted", "onPageStarted", "On WebView page loading started (url)"),
        IdeViewEventOption("onPageFinished", "OnPage\nFinished", "onPageFinished", "On WebView page loading finished (url)"),
        IdeViewEventOption("onReceivedError", "OnPage\nError", "onReceivedError", "On WebView loading error"),
        IdeViewEventOption("onUrlOverride", "OnUrl\nOverride", "onUrlOverride", "On shouldOverrideUrlLoading"),
    )
    IdeWidgetType.CUSTOM_TAB_VIEW -> listOf(
        IdeViewEventOption("onTabShown", "OnTab\nShown", "onTabShown", "On Chrome Custom Tab opened"),
        IdeViewEventOption("onTabHidden", "OnTab\nClosed", "onTabHidden", "On Chrome Custom Tab closed"),
        IdeViewEventOption("onNavigationEvent", "OnNav\nEvent", "onNavigationEvent", "On Custom Tab navigation event"),
    )
    IdeWidgetType.GECKO_WEB_VIEW -> listOf(
        IdeViewEventOption("onGeckoPageStart", "OnGecko\nStart", "onGeckoPageStart", "On GeckoSession page start"),
        IdeViewEventOption("onGeckoPageStop", "OnGecko\nStop", "onGeckoPageStop", "On GeckoSession page stop"),
        IdeViewEventOption("onGeckoSecurity", "OnSecurity\nChange", "onGeckoSecurity", "On SSL security status changed"),
    )
    IdeWidgetType.AD_WEB_VIEW -> listOf(
        IdeViewEventOption("onAdBlocked", "OnAd\nBlocked", "onAdBlocked", "When an ad request is blocked (blockedUrl)"),
        IdeViewEventOption("onTrackerBlocked", "OnTracker\nBlocked", "onTrackerBlocked", "When a tracking script is blocked"),
        IdeViewEventOption("onPopupBlocked", "OnPopup\nBlocked", "onPopupBlocked", "When an ad popup window is blocked"),
    )
    IdeWidgetType.SWIPE_WEB_VIEW -> listOf(
        IdeViewEventOption("onSwipeRefresh", "OnPull\nRefresh", "onSwipeRefresh", "On pull-to-refresh gesture triggered"),
        IdeViewEventOption("onReloadComplete", "OnReload\nDone", "onReloadComplete", "On SwipeWebView refresh completed"),
        IdeViewEventOption("onProgressChanged", "OnLoad\nProgress", "onProgressChanged", "On page loading progress (0..100)"),
    )

    // 7. List Widgets — Distinct events per List widget
    IdeWidgetType.GRID_VIEW -> listOf(
        IdeViewEventOption("onGridCellClick", "OnCell\nClick", "onGridCellClick", "On GridView cell clicked (position)"),
        IdeViewEventOption("onGridCellLongClick", "OnCell\nLongClick", "onGridCellLongClick", "On GridView cell long clicked (position)"),
        IdeViewEventOption("onGridScroll", "OnGrid\nScroll", "onGridScroll", "On GridView scrolled"),
    )
    IdeWidgetType.LIST_VIEW -> listOf(
        IdeViewEventOption("onListRowClick", "OnRow\nTap", "onListRowClick", "On ListView row clicked (position)"),
        IdeViewEventOption("onListRowLongClick", "OnRow\nLongTap", "onListRowLongClick", "On ListView row long clicked (position)"),
        IdeViewEventOption("onListScrollChange", "OnList\nScroll", "onListScrollChange", "On ListView scroll state changed"),
    )
    IdeWidgetType.TREE_VIEW -> listOf(
        IdeViewEventOption("onNodeClick", "OnNode\nClick", "onNodeClick", "On TreeView node clicked"),
        IdeViewEventOption("onNodeExpand", "OnNode\nExpand", "onNodeExpand", "On TreeView branch expanded"),
        IdeViewEventOption("onNodeCollapse", "OnNode\nCollapse", "onNodeCollapse", "On TreeView branch collapsed"),
    )
    IdeWidgetType.RECYCLER_VIEW -> listOf(
        IdeViewEventOption("onBindCustomView", "OnBind\nHolder", "onBindCustomView", "On RecyclerView ViewHolder bound (position)"),
        IdeViewEventOption("onHolderRecycled", "OnView\nRecycled", "onHolderRecycled", "On RecyclerView ViewHolder recycled"),
        IdeViewEventOption("onScrollStateChanged", "OnRecycler\nScroll", "onScrollStateChanged", "On RecyclerView scroll state changed"),
    )
    IdeWidgetType.VIEW_PAGER -> listOf(
        IdeViewEventOption("onPageSelected", "OnPage\nSelected", "onPageSelected", "On ViewPager page selected (position)"),
        IdeViewEventOption("onPageScrolled", "OnPage\nScrolled", "onPageScrolled", "On ViewPager page scrolling"),
        IdeViewEventOption("onPageScrollState", "OnPager\nState", "onPageScrollState", "On ViewPager scroll state changed"),
    )
    IdeWidgetType.EXPANDABLE_LIST_VIEW -> listOf(
        IdeViewEventOption("onGroupClick", "OnGroup\nClick", "onGroupClick", "On ExpandableList group header clicked"),
        IdeViewEventOption("onChildClick", "OnChild\nClick", "onChildClick", "On ExpandableList child item clicked"),
        IdeViewEventOption("onGroupExpand", "OnGroup\nExpand", "onGroupExpand", "On ExpandableList group expanded"),
        IdeViewEventOption("onGroupCollapse", "OnGroup\nCollapse", "onGroupCollapse", "On ExpandableList group collapsed"),
    )
    IdeWidgetType.STAGGERED_GRID_VIEW -> listOf(
        IdeViewEventOption("onStaggeredItemClick", "OnTile\nClick", "onStaggeredItemClick", "On StaggeredGrid tile clicked"),
        IdeViewEventOption("onBindStaggeredView", "OnBind\nTile", "onBindStaggeredView", "On StaggeredGrid tile bound"),
        IdeViewEventOption("onSpanChanged", "OnSpan\nChange", "onSpanChanged", "On staggered span layout changed"),
    )
    IdeWidgetType.HORIZONTAL_LIST_VIEW -> listOf(
        IdeViewEventOption("onHorizItemClick", "OnHoriz\nItemTap", "onHorizItemClick", "On HorizontalList item clicked"),
        IdeViewEventOption("onHorizScrollEnd", "OnHoriz\nEnd", "onHorizScrollEnd", "On HorizontalList scrolled to end"),
    )
    IdeWidgetType.CAROUSEL_VIEW -> listOf(
        IdeViewEventOption("onCarouselSnap", "OnCard\nSnap", "onCarouselSnap", "On CarouselView card snapped to center"),
        IdeViewEventOption("onCarouselClick", "OnCenter\nTap", "onCarouselClick", "On CarouselView active card clicked"),
    )
    IdeWidgetType.SWIPE_LIST_VIEW -> listOf(
        IdeViewEventOption("onSwipeDismissLeft", "OnSwipe\nLeft", "onSwipeDismissLeft", "On row swiped left"),
        IdeViewEventOption("onSwipeDismissRight", "OnSwipe\nRight", "onSwipeDismissRight", "On row swiped right"),
        IdeViewEventOption("onUndoSwipe", "OnSwipe\nUndo", "onUndoSwipe", "On swipe dismissal undone"),
    )

    // 8. Date & Time Widgets — Distinct events per Date/Time widget
    IdeWidgetType.ANALOG_CLOCK -> listOf(
        IdeViewEventOption("onMinuteHandTick", "OnMinute\nTick", "onMinuteHandTick", "On AnalogClock minute hand tick"),
        IdeViewEventOption("onHourChime", "OnHour\nChime", "onHourChime", "On AnalogClock full hour reached"),
        IdeViewEventOption("onDialFaceTap", "OnDial\nTap", "onDialFaceTap", "On AnalogClock dial face tapped"),
    )
    IdeWidgetType.DIGITAL_CLOCK -> listOf(
        IdeViewEventOption("onSecondTick", "OnSecond\nTick", "onSecondTick", "On DigitalClock second updated"),
        IdeViewEventOption("onFormatChange", "OnFormat\n24h/12h", "onFormatChange", "On 12h/24h format switched"),
    )
    IdeWidgetType.TIME_PICKER -> listOf(
        IdeViewEventOption("onTimeChanged", "OnTime\nChanged", "onTimeChanged", "On TimePicker time changed (hourOfDay, minute)"),
        IdeViewEventOption("onAmPmChanged", "OnAmPm\nChanged", "onAmPmChanged", "On AM/PM period toggled"),
    )
    IdeWidgetType.DATE_PICKER -> listOf(
        IdeViewEventOption("onDateChanged", "OnDate\nChanged", "onDateChanged", "On DatePicker date changed (year, month, day)"),
        IdeViewEventOption("onYearSelected", "OnYear\nSelected", "onYearSelected", "On DatePicker year selected"),
    )
    IdeWidgetType.CALENDAR_VIEW -> listOf(
        IdeViewEventOption("onSelectedDayChange", "OnDay\nSelected", "onSelectedDayChange", "On CalendarView day selected (year, month, dayOfMonth)"),
        IdeViewEventOption("onMonthChanged", "OnMonth\nChanged", "onMonthChanged", "On CalendarView month swiped"),
    )
    IdeWidgetType.TEXT_CLOCK -> listOf(
        IdeViewEventOption("onClockPatternTick", "OnClock\nTick", "onClockPatternTick", "On TextClock formatted time updated"),
        IdeViewEventOption("onTimeZoneChanged", "OnZone\nChange", "onTimeZoneChanged", "On TextClock timeZone changed"),
    )
    IdeWidgetType.CHRONOMETER -> listOf(
        IdeViewEventOption("onChronometerTick", "OnTimer\nTick", "onChronometerTick", "On Chronometer tick every second"),
        IdeViewEventOption("onChronometerStart", "OnStart", "onChronometerStart", "On Chronometer started"),
        IdeViewEventOption("onChronometerStop", "OnStop", "onChronometerStop", "On Chronometer stopped"),
    )
    IdeWidgetType.COUNTDOWN_VIEW -> listOf(
        IdeViewEventOption("onCountDownTick", "OnCount\nTick", "onCountDownTick", "On CountDown millisUntilFinished tick"),
        IdeViewEventOption("onCountDownFinish", "OnCount\nFinish", "onCountDownFinish", "When CountDown reaches 00:00"),
    )
    IdeWidgetType.STOPWATCH_VIEW -> listOf(
        IdeViewEventOption("onLapRecorded", "OnLap\nRecord", "onLapRecorded", "On Stopwatch lap time recorded"),
        IdeViewEventOption("onStopwatchReset", "OnReset", "onStopwatchReset", "On Stopwatch reset to 00:00"),
    )
    IdeWidgetType.DATE_RANGE_PICKER -> listOf(
        IdeViewEventOption("onRangeSelected", "OnRange\nSelected", "onRangeSelected", "On start & end date range selected"),
        IdeViewEventOption("onRangeCleared", "OnRange\nCleared", "onRangeCleared", "On date range selection cleared"),
    )
    IdeWidgetType.WORLD_CLOCK_VIEW -> listOf(
        IdeViewEventOption("onCityChanged", "OnCity\nChange", "onCityChanged", "On WorldClock city/timezone changed"),
        IdeViewEventOption("onDayNightSwitch", "OnDay\nNight", "onDayNightSwitch", "On WorldClock day/night transition"),
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun IdeLogicEventWorkspace(
    project: WorkspaceItem,
    xmlPage: ProjectXmlPage,
    widgetNodes: List<IdeWidgetNode>,
    logicItems: List<IdeLogicItem>,
    onUpdateLogicItems: (List<IdeLogicItem>) -> Unit,
    activeFlavor: CodeEditorFlavor,
    onSelectFlavor: (CodeEditorFlavor) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
) {
    var activeSection by rememberSaveable { mutableStateOf(IdeLogicSection.ACTIVITY) }
    var showAddActivitySheet by remember { mutableStateOf(false) }
    var showAddViewSheet by remember { mutableStateOf(false) }
    var preselectedViewNodeForSheet by remember { mutableStateOf<IdeWidgetNode?>(null) }
    var showAddComponentOrDrawerSheet by remember { mutableStateOf(false) }
    var showFunctionSheet by remember { mutableStateOf(false) }
    var editingFunctionItem by remember { mutableStateOf<IdeLogicItem?>(null) }

    var longPressedItem by remember { mutableStateOf<IdeLogicItem?>(null) }
    var renameDialogItem by remember { mutableStateOf<IdeLogicItem?>(null) }
    var renameDialogText by remember { mutableStateOf("") }

    val allViewWidgets = remember(widgetNodes) { flattenAllNodes(widgetNodes) }
    val filteredItems = remember(logicItems, activeSection) {
        logicItems.filter { it.section == activeSection }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
        ) {
            // 1. Left Vertical Category Rail (Activity | View | Component | Drawer | Functions)
            // Clicking any section ONLY switches activeSection — never opens a bottom sheet!
            Column(
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0A1026))
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 18.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                IdeLogicSection.entries.forEach { section ->
                    val selected = section == activeSection
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                activeSection = section
                            }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // Pill highlight around active icon (exact match to screenshot's Activity icon pill!)
                        Box(
                            modifier = Modifier
                                .width(56.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(percent = 50))
                                .background(
                                    if (selected) Color(0xFF23326A) else Color.Transparent,
                                )
                                .then(
                                    if (selected) {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = Cyan.copy(alpha = 0.55f),
                                            shape = RoundedCornerShape(percent = 50),
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            IdeLogicRailIcon(
                                section = section,
                                tint = if (selected) Cyan else Ink.copy(alpha = 0.78f),
                            )
                        }

                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (selected) Cyan else Ink.copy(alpha = 0.80f),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                        )
                    }
                }
            }

            // Subtle vertical divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(Color(0x1FFFFFFF)),
            )

            // 2. Right Content Area (Import Code button in Activity + Event/Function cards — no popup sheets on click!)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 12.dp, end = 14.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (activeSection == IdeLogicSection.ACTIVITY) {
                    // Standalone "Import Code" button in Activity (kept as a clean button without opening an explanatory bottom sheet)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color(0xFF151F44))
                            .border(
                                width = 1.dp,
                                color = Color(0x38FFFFFF),
                                shape = RoundedCornerShape(percent = 50),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Import Code",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                    }
                }

                // Event / Function Cards List (clicking an item does NOT open any bottom sheet!)
                filteredItems.forEach { item ->
                    IdeLogicEventCard(
                        item = item,
                        onClick = {},
                        onLongClick = { longPressedItem = item },
                    )
                }
            }
        }

        // 3. Rounded-Square Floating Action Button (+) at Bottom-End
        // Hidden in VIEW section because View events are toggled directly from each widget's Event tab in View!
        if (activeSection != IdeLogicSection.VIEW) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 18.dp, bottom = 18.dp)
                    .size(58.dp)
                    .shadow(10.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF283A75), Color(0xFF364F9C)),
                        ),
                    )
                    .border(1.2.dp, Cyan.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
                    .clickable {
                        when (activeSection) {
                            IdeLogicSection.ACTIVITY -> showAddActivitySheet = true
                            IdeLogicSection.FUNCTIONS -> {
                                editingFunctionItem = null
                                showFunctionSheet = true
                            }
                            IdeLogicSection.COMPONENT, IdeLogicSection.DRAWER -> {
                                showAddComponentOrDrawerSheet = true
                            }
                            IdeLogicSection.VIEW -> {}
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Event or Function",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }

    // ========================================================================
    // Bottom Sheet 1: Add Activity Event (All Activity Events: OnCreate, Import, OnRename, etc.)
    // ========================================================================
    if (showAddActivitySheet) {
        AddActivityEventBottomSheet(
            existingItems = logicItems.filter { it.section == IdeLogicSection.ACTIVITY },
            onAddEvent = { template ->
                val alreadyExists = logicItems.any {
                    it.section == IdeLogicSection.ACTIVITY && it.name.equals(template.name, ignoreCase = true)
                }
                if (!alreadyExists) {
                    val newItem = IdeLogicItem(
                        section = IdeLogicSection.ACTIVITY,
                        name = template.name,
                        subtitle = template.subtitle,
                        bodyCode = template.defaultCode,
                    )
                    onUpdateLogicItems(logicItems + newItem)
                }
                showAddActivitySheet = false
            },
            onAddAllMissing = {
                val existingNames = logicItems
                    .filter { it.section == IdeLogicSection.ACTIVITY }
                    .map { it.name.lowercase() }
                    .toSet()
                val newOnes = ALL_ACTIVITY_EVENT_TEMPLATES
                    .filter { it.name.lowercase() !in existingNames }
                    .map { tpl ->
                        IdeLogicItem(
                            section = IdeLogicSection.ACTIVITY,
                            name = tpl.name,
                            subtitle = tpl.subtitle,
                            bodyCode = tpl.defaultCode,
                        )
                    }
                if (newOnes.isNotEmpty()) {
                    onUpdateLogicItems(logicItems + newOnes)
                }
                showAddActivitySheet = false
            },
            onDismiss = { showAddActivitySheet = false },
        )
    }

    // ========================================================================
    // Bottom Sheet 2: View Events Bottom Sheet (Widget-specific Events with Red/Green dots)
    // ========================================================================
    if (showAddViewSheet) {
        AddViewEventBottomSheet(
            allViewWidgets = allViewWidgets,
            preselectedWidget = preselectedViewNodeForSheet,
            existingViewItems = logicItems.filter { it.section == IdeLogicSection.VIEW },
            onToggleOrOpenViewEvent = { widgetNode, eventOpt ->
                val eventName = "${widgetNode.viewId}: ${eventOpt.eventKey}"
                val existing = logicItems.firstOrNull {
                    it.section == IdeLogicSection.VIEW &&
                        it.targetViewId == widgetNode.viewId &&
                        it.name == eventName
                }
                if (existing != null) {
                    onUpdateLogicItems(logicItems.filterNot { it.id == existing.id })
                } else {
                    val newItem = IdeLogicItem(
                        section = IdeLogicSection.VIEW,
                        name = eventName,
                        subtitle = "${widgetNode.type.displayName} (@+id/${widgetNode.viewId}) • ${eventOpt.subtitle}",
                        targetViewId = widgetNode.viewId,
                        bodyCode = "// ${widgetNode.viewId} ${eventOpt.eventKey}\n",
                    )
                    onUpdateLogicItems(logicItems + newItem)
                }
            },
            onDismiss = {
                showAddViewSheet = false
                preselectedViewNodeForSheet = null
            },
        )
    }

    // ========================================================================
    // Bottom Sheet 3: Functions (MoreBlock renamed to Functions) Bottom Sheet
    //   1. الاسم (Function Name)
    //   2. نوع الدالة (Return Type)
    //   3. متغيرات الدالة مع تحديد نوعها (Parameters + Type)
    //   4. متغير خاص (Private Variable + Type + Initial Value)
    // ========================================================================
    if (showFunctionSheet) {
        CreateOrEditFunctionBottomSheet(
            initialItem = editingFunctionItem,
            onSaveFunction = { funcName, returnType, params, privName, privType, privVal ->
                val paramsSummary = if (params.isEmpty()) {
                    "No params"
                } else {
                    params.joinToString(", ") { "${it.name}: ${it.type}" }
                }
                val privSummary = if (privName.isBlank()) {
                    ""
                } else {
                    " • متغير خاص: $privName ($privType${if (privVal.isNotBlank()) " = $privVal" else ""})"
                }
                val sub = "نوع الدالة: $returnType • ($paramsSummary)$privSummary"

                val existingEditing = editingFunctionItem
                if (existingEditing != null) {
                    val updatedList = logicItems.map { item ->
                        if (item.id == existingEditing.id) {
                            item.copy(
                                name = funcName,
                                subtitle = sub,
                                returnType = returnType,
                                parameters = params,
                                privateVarName = privName,
                                privateVarType = privType,
                                privateVarValue = privVal,
                            )
                        } else {
                            item
                        }
                    }
                    onUpdateLogicItems(updatedList)
                } else {
                    val defaultPrivLine = if (privName.isNotBlank()) {
                        val defaultInit = privVal.ifBlank {
                            when (privType) {
                                "Int", "Long" -> "0"
                                "Double", "Float" -> "0.0"
                                "Boolean" -> "false"
                                "String" -> "\"\""
                                else -> "null"
                            }
                        }
                        "var $privName: $privType = $defaultInit\n"
                    } else {
                        ""
                    }
                    val newFunc = IdeLogicItem(
                        section = IdeLogicSection.FUNCTIONS,
                        name = funcName,
                        subtitle = sub,
                        returnType = returnType,
                        parameters = params,
                        privateVarName = privName,
                        privateVarType = privType,
                        privateVarValue = privVal,
                        bodyCode = "${defaultPrivLine}// كود الدالة $funcName هنا",
                    )
                    onUpdateLogicItems(logicItems + newFunc)
                }
                showFunctionSheet = false
                editingFunctionItem = null
            },
            onDismiss = {
                showFunctionSheet = false
                editingFunctionItem = null
            },
        )
    }

    // ========================================================================
    // Bottom Sheet 4: Component / Drawer Event Picker
    // ========================================================================
    if (showAddComponentOrDrawerSheet) {
        AddComponentOrDrawerEventBottomSheet(
            section = activeSection,
            onAddItem = { title, sub, body ->
                val newItem = IdeLogicItem(
                    section = activeSection,
                    name = title,
                    subtitle = sub,
                    bodyCode = body,
                )
                onUpdateLogicItems(logicItems + newItem)
                showAddComponentOrDrawerSheet = false
            },
            onDismiss = { showAddComponentOrDrawerSheet = false },
        )
    }

    // ========================================================================
    // Bottom Sheet 5: Long-Press on ANY Item in Logic:
    //   - Reset (مسح محتوى الدالة كاملة)
    //   - تعديل الاسم (Edit Name / Edit Function in Functions)
    //   - حذف (Delete)
    // ========================================================================
    val activeLongPress = longPressedItem
    if (activeLongPress != null) {
        IdeLogicItemLongPressBottomSheet(
            item = activeLongPress,
            onResetBody = {
                // Reset: مسح محتوى الدالة كاملة
                val updated = logicItems.map {
                    if (it.id == activeLongPress.id) it.copy(bodyCode = "") else it
                }
                onUpdateLogicItems(updated)
                longPressedItem = null
            },
            onEditName = {
                val target = activeLongPress
                longPressedItem = null
                if (target.section == IdeLogicSection.FUNCTIONS) {
                    editingFunctionItem = target
                    showFunctionSheet = true
                } else {
                    renameDialogText = target.name
                    renameDialogItem = target
                }
            },
            onDelete = {
                onUpdateLogicItems(logicItems.filterNot { it.id == activeLongPress.id })
                longPressedItem = null
            },
            onDismiss = { longPressedItem = null },
        )
    }

    // Rename Dialog for non-Function Logic items
    val currentRenameItem = renameDialogItem
    if (currentRenameItem != null) {
        AlertDialog(
            onDismissRequest = { renameDialogItem = null },
            containerColor = Color(0xFF0D1230),
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "تعديل الاسم • Rename",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                OutlinedTextField(
                    value = renameDialogText,
                    onValueChange = { renameDialogText = it },
                    singleLine = true,
                    label = { Text("Name / الاسم") },
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
                        val newName = renameDialogText.trim()
                        if (newName.isNotEmpty()) {
                            onUpdateLogicItems(
                                logicItems.map {
                                    if (it.id == currentRenameItem.id) it.copy(name = newName) else it
                                },
                            )
                        }
                        renameDialogItem = null
                    },
                ) {
                    Text(stringResource(R.string.dialog_save), color = Cyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDialogItem = null }) {
                    Text(stringResource(R.string.dialog_cancel), color = Muted)
                }
            },
        )
    }
}

@Composable
private fun IdeLogicRailIcon(
    section: IdeLogicSection,
    tint: Color,
) {
    Canvas(Modifier.size(20.dp)) {
        val stroke = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (section) {
            IdeLogicSection.ACTIVITY -> {
                // Recycle / 3-arrow lifecycle icon matching screenshot
                val cx = size.width * 0.50f
                val cy = size.height * 0.50f
                val r = size.width * 0.36f
                drawArc(
                    color = tint,
                    startAngle = -75f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = stroke,
                )
                drawArc(
                    color = tint,
                    startAngle = 45f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = stroke,
                )
                drawArc(
                    color = tint,
                    startAngle = 165f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    style = stroke,
                )
            }

            IdeLogicSection.VIEW -> {
                // Stacked 3-row rectangle matching screenshot
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.20f, size.height * 0.14f),
                    size = Size(size.width * 0.60f, size.height * 0.72f),
                    cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.20f, size.height * 0.38f), Offset(size.width * 0.80f, size.height * 0.38f), stroke.width)
                drawLine(tint, Offset(size.width * 0.20f, size.height * 0.62f), Offset(size.width * 0.80f, size.height * 0.62f), stroke.width)
            }

            IdeLogicSection.COMPONENT -> {
                // Box with top lid & slot matching screenshot
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.14f, size.height * 0.16f),
                    size = Size(size.width * 0.72f, size.height * 0.20f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.20f, size.height * 0.36f),
                    size = Size(size.width * 0.60f, size.height * 0.48f),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                    style = stroke,
                )
                drawLine(tint, Offset(size.width * 0.38f, size.height * 0.54f), Offset(size.width * 0.62f, size.height * 0.54f), stroke.width)
            }

            IdeLogicSection.DRAWER -> {
                // Split drawer rectangle matching screenshot
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.16f),
                    size = Size(size.width * 0.68f, size.height * 0.68f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                    style = stroke,
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.16f, size.height * 0.16f),
                    size = Size(size.width * 0.32f, size.height * 0.68f),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
                )
            }

            IdeLogicSection.FUNCTIONS -> {
                // Puzzle block icon matching screenshot
                val path = Path().apply {
                    moveTo(size.width * 0.14f, size.height * 0.36f)
                    lineTo(size.width * 0.36f, size.height * 0.36f)
                    lineTo(size.width * 0.42f, size.height * 0.44f)
                    lineTo(size.width * 0.58f, size.height * 0.44f)
                    lineTo(size.width * 0.64f, size.height * 0.36f)
                    lineTo(size.width * 0.86f, size.height * 0.36f)
                    lineTo(size.width * 0.86f, size.height * 0.68f)
                    lineTo(size.width * 0.14f, size.height * 0.68f)
                    close()
                }
                drawPath(path, color = tint)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IdeLogicEventCard(
    item: IdeLogicItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val shape = RoundedCornerShape(18.dp)
    val titleDisplay = if (item.section == IdeLogicSection.FUNCTIONS) {
        val paramsStr = item.parameters.joinToString(", ") { "${it.name}: ${it.type}" }
        "${item.name}($paramsStr): ${item.returnType}"
    } else {
        item.name
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF151E42))
            .border(1.dp, Color(0x2EFFFFFF), shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 16.dp, vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Left "< >" Code Brackets Icon matching screenshot
        Canvas(Modifier.size(22.dp)) {
            val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            val left = Path().apply {
                moveTo(size.width * 0.36f, size.height * 0.26f)
                lineTo(size.width * 0.12f, size.height * 0.50f)
                lineTo(size.width * 0.36f, size.height * 0.74f)
            }
            val right = Path().apply {
                moveTo(size.width * 0.64f, size.height * 0.26f)
                lineTo(size.width * 0.88f, size.height * 0.50f)
                lineTo(size.width * 0.64f, size.height * 0.74f)
            }
            drawPath(left, color = Cyan, style = stroke)
            drawPath(right, color = Cyan, style = stroke)
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = titleDisplay,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (item.bodyCode.isBlank()) {
                    "${item.subtitle} • (فارغ / Reset)"
                } else {
                    item.subtitle
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                ),
                color = Muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = Ink.copy(alpha = 0.80f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddActivityEventBottomSheet(
    existingItems: List<IdeLogicItem>,
    onAddEvent: (IdeActivityEventTemplate) -> Unit,
    onAddAllMissing: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val existingNames = remember(existingItems) {
        existingItems.map { it.name.lowercase() }.toSet()
    }

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
                            text = "Add Activity Event • كل أحداث Activity",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                        Text(
                            text = "OnCreate, Import, OnRename & all lifecycle events",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = Cyan,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Cyan.copy(alpha = 0.16f))
                            .border(1.dp, Cyan.copy(alpha = 0.50f), RoundedCornerShape(50))
                            .clickable(onClick = onAddAllMissing)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = "+ إضافة الكل",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Cyan,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ALL_ACTIVITY_EVENT_TEMPLATES.forEach { tpl ->
                        val isAdded = tpl.name.lowercase() in existingNames
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isAdded) Cyan.copy(alpha = 0.14f) else Color(0xFF141E42),
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isAdded) Cyan.copy(alpha = 0.55f) else Color(0x26FFFFFF),
                                    shape = RoundedCornerShape(14.dp),
                                )
                                .clickable { onAddEvent(tpl) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = tpl.name,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = if (isAdded) Cyan else Ink,
                                )
                                Text(
                                    text = tpl.subtitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = Muted,
                                )
                            }

                            if (isAdded) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Cyan,
                                    modifier = Modifier.size(18.dp),
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = Ink,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddViewEventBottomSheet(
    allViewWidgets: List<IdeWidgetNode>,
    preselectedWidget: IdeWidgetNode?,
    existingViewItems: List<IdeLogicItem>,
    onToggleOrOpenViewEvent: (IdeWidgetNode, IdeViewEventOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val widgetsToShow = remember(allViewWidgets, preselectedWidget) {
        if (preselectedWidget != null) {
            listOf(preselectedWidget) + allViewWidgets.filter { it.uid != preselectedWidget.uid }
        } else {
            allViewWidgets
        }
    }

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
                Text(
                    text = "View Events • أحداث عناصر View",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )
                Text(
                    text = "نقطة خضراء = الحدث مفعل • نقطة حمراء = الحدث غير مفعل",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = Cyan,
                )

                if (widgetsToShow.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF141E42))
                            .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
                            .padding(20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "لا توجد عناصر في صفحة View حالياً — أضف عناصر في تبويب View أولاً لتظهر هنا مع أحداثها الخاصة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Ink,
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        widgetsToShow.forEach { wNode ->
                            val widgetEvents = remember(wNode.type) {
                                getEventsForWidgetType(wNode.type)
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF131C3E))
                                    .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    SketchWidgetIcon(type = wNode.type, tint = Cyan)
                                    Text(
                                        text = "${wNode.viewId} (${wNode.type.displayName})",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = Cyan,
                                    )
                                }

                                widgetEvents.forEach { evOpt ->
                                    val isAdded = existingViewItems.any {
                                        it.targetViewId == wNode.viewId &&
                                            it.name == "${wNode.viewId}: ${evOpt.eventKey}"
                                    }
                                    val dotColor = if (isAdded) Color(0xFF22C55E) else Color(0xFFEF4444)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isAdded) Color(0xFF132A26) else Color(0xFF1B2752),
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isAdded) Color(0xFF22C55E).copy(alpha = 0.65f) else Color(0x26FFFFFF),
                                                shape = RoundedCornerShape(12.dp),
                                            )
                                            .clickable { onToggleOrOpenViewEvent(wNode, evOpt) }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Text(
                                                text = evOpt.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = JetBrainsMono,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = if (isAdded) Color(0xFF4ADE80) else Ink,
                                            )
                                            Text(
                                                text = evOpt.subtitle,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                                color = Muted,
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(dotColor.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(9.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateOrEditFunctionBottomSheet(
    initialItem: IdeLogicItem?,
    onSaveFunction: (
        funcName: String,
        returnType: String,
        params: List<IdeFunctionParam>,
        privName: String,
        privType: String,
        privVal: String,
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var funcName by remember(initialItem?.id) {
        mutableStateOf(initialItem?.name ?: "")
    }
    var returnType by remember(initialItem?.id) {
        mutableStateOf(initialItem?.returnType ?: "void")
    }
    val params = remember(initialItem?.id) {
        mutableStateListOf<IdeFunctionParam>().apply {
            if (initialItem != null) {
                addAll(initialItem.parameters)
            }
        }
    }
    var newParamName by remember { mutableStateOf("") }
    var newParamType by remember { mutableStateOf("String") }

    var privateVarName by remember(initialItem?.id) {
        mutableStateOf(initialItem?.privateVarName ?: "")
    }
    var privateVarType by remember(initialItem?.id) {
        mutableStateOf(initialItem?.privateVarType ?: "String")
    }
    var privateVarValue by remember(initialItem?.id) {
        mutableStateOf(initialItem?.privateVarValue ?: "")
    }

    val returnTypes = listOf(
        "void",
        "String",
        "Int",
        "Boolean",
        "Double",
        "Float",
        "Long",
        "List<String>",
        "Map<String, Any>",
        "Any",
    )
    val varTypes = listOf(
        "String",
        "Int",
        "Boolean",
        "Double",
        "Float",
        "Long",
        "View",
        "Any",
    )

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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (initialItem == null) {
                        "Functions • إضافة دالة جديدة للمشروع"
                    } else {
                        "Functions • تعديل الدالة (${initialItem.name})"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )

                // 1. الاسم (Function Name)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "1. الاسم (Function Name):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Cyan,
                    )
                    DialogTextField(
                        value = funcName,
                        onValueChange = { funcName = it },
                        label = "اسم الدالة (مثال: calculateTotal)",
                    )
                }

                // 2. نوع الدالة (Function Return Type)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "2. نوع الدالة (Function Return Type):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Cyan,
                    )
                    DialogPresetChipsGrid(
                        presets = returnTypes,
                        selectedValue = returnType,
                        suffix = "",
                        onSelect = { returnType = it },
                    )
                }

                // 3. متغيرات الدالة مع تحديد نوعها (Function Variables / Parameters + Type)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131C3E))
                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "3. متغيرات الدالة مع تحديد نوعها (Parameters):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Cyan,
                    )

                    if (params.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            params.forEachIndexed { idx, p ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1C2959))
                                        .border(1.dp, Cyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "${p.name} : ${p.type}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = Ink,
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Remove Parameter",
                                        tint = Color(0xFFFDA4AF),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { params.removeAt(idx) },
                                    )
                                }
                            }
                        }
                    }

                    DialogTextField(
                        value = newParamName,
                        onValueChange = { newParamName = it },
                        label = "اسم متغير الدالة (Parameter Name)",
                    )

                    Text(
                        text = "نوع متغير الدالة (Parameter Type):",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    DialogPresetChipsGrid(
                        presets = varTypes,
                        selectedValue = newParamType,
                        suffix = "",
                        onSelect = { newParamType = it },
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Cyan.copy(alpha = 0.18f))
                            .border(1.dp, Cyan, RoundedCornerShape(12.dp))
                            .clickable {
                                val cleanParam = newParamName.trim().replace(Regex("[^a-zA-Z0-9_]+"), "_")
                                if (cleanParam.isNotEmpty()) {
                                    params.add(IdeFunctionParam(name = cleanParam, type = newParamType))
                                    newParamName = ""
                                }
                            }
                            .padding(vertical = 9.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "إضافة متغير للدالة • Add Parameter",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Cyan,
                        )
                    }
                }

                // 4. متغير خاص (Private / Local Variable)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131C3E))
                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "4. متغير خاص بالدالة (Private Variable):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Cyan,
                    )
                    DialogTextField(
                        value = privateVarName,
                        onValueChange = { privateVarName = it },
                        label = "اسم المتغير الخاص (مثال: localResult)",
                    )
                    Text(
                        text = "نوع المتغير الخاص:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                    DialogPresetChipsGrid(
                        presets = varTypes,
                        selectedValue = privateVarType,
                        suffix = "",
                        onSelect = { privateVarType = it },
                    )
                    DialogTextField(
                        value = privateVarValue,
                        onValueChange = { privateVarValue = it },
                        label = "القيمة الابتدائية للمتغير الخاص (اختياري)",
                    )
                }

                GradientButton(
                    text = if (initialItem == null) "إضافة الدالة • Create Function" else "حفظ التعديلات • Save Function",
                    onClick = {
                        // If the user typed a parameter name without pressing "+ Add Parameter", include it automatically!
                        val finalParams = params.toMutableList()
                        val pendingParam = newParamName.trim().replace(Regex("[^a-zA-Z0-9_]+"), "_")
                        if (pendingParam.isNotEmpty() && finalParams.none { it.name == pendingParam }) {
                            finalParams.add(IdeFunctionParam(name = pendingParam, type = newParamType))
                        }
                        val cleanFuncName = funcName.trim()
                            .replace(Regex("[^a-zA-Z0-9_]+"), "_")
                            .ifBlank { "customFunction" }
                        onSaveFunction(
                            cleanFuncName,
                            returnType,
                            finalParams,
                            privateVarName.trim(),
                            privateVarType,
                            privateVarValue.trim(),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddComponentOrDrawerEventBottomSheet(
    section: IdeLogicSection,
    onAddItem: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val options = remember(section) {
        if (section == IdeLogicSection.COMPONENT) {
            listOf(
                Triple("Timer: onTick", "On timer tick event", "// Timer tick logic"),
                Triple("Intent: onNavigate", "Start activity intent", "// Intent navigation"),
                Triple("SharedPreferences: onLoad", "Read/Write local data", "// SharedPreferences logic"),
                Triple("Dialog: onConfirm", "AlertDialog confirm button clicked", "// Dialog confirm logic"),
                Triple("FirebaseDB: onChildAdded", "On Firebase Realtime DB child added", "// FirebaseDB child added"),
                Triple("RequestNetwork: onResponse", "On HTTP network response", "// Network response logic"),
            )
        } else {
            listOf(
                Triple("onDrawerOpened", "On navigation drawer opened", "// Drawer opened"),
                Triple("onDrawerClosed", "On navigation drawer closed", "// Drawer closed"),
                Triple("onDrawerSlide", "On navigation drawer sliding", "// Drawer sliding"),
                Triple("onDrawerStateChanged", "On drawer state changed", "// Drawer state changed"),
            )
        }
    }

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
                Text(
                    text = "Add ${section.title} Event",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )
                options.forEach { (title, sub, body) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF141E42))
                            .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(14.dp))
                            .clickable { onAddItem(title, sub, body) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = Ink,
                            )
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.labelSmall,
                                color = Muted,
                            )
                        }
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdeLogicItemLongPressBottomSheet(
    item: IdeLogicItem,
    onResetBody: () -> Unit,
    onEditName: () -> Unit,
    onDelete: () -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "${item.section.title} • ${item.name}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                ),
                color = Cyan,
            )

            // 1. تعديل الاسم (Edit Name / Edit Function in Functions)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF141E42))
                    .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(14.dp))
                    .clickable(onClick = onEditName)
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = if (item.section == IdeLogicSection.FUNCTIONS) {
                        "تعديل الاسم وخصائص الدالة • Edit Function"
                    } else {
                        "تعديل الاسم • Rename"
                    },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Ink,
                )
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    tint = Cyan,
                    modifier = Modifier.size(18.dp),
                )
            }

            // 2. Reset (مسح محتوى الدالة كاملة)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1E2952))
                    .border(1.dp, Color(0xFFFBBF24).copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onResetBody)
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Reset • مسح محتوى الدالة كاملة",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFBBF24),
                    )
                    Text(
                        text = "تفريغ الكود الداخلي بالكامل مع الإبقاء على الدالة/الحدث",
                        style = MaterialTheme.typography.labelSmall,
                        color = Muted,
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(18.dp),
                )
            }

            // 3. حذف (Delete)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF3B0D1E))
                    .border(1.dp, Color(0xFFF43F5E).copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .clickable(onClick = onDelete)
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "حذف • Delete",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFDA4AF),
                )
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFFFDA4AF),
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.height(6.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdeLogicItemEditorBottomSheet(
    item: IdeLogicItem,
    onSaveBodyCode: (String) -> Unit,
    onResetBodyCode: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var bodyText by remember(item.id) { mutableStateOf(item.bodyCode) }

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
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = Cyan,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF3B0D1E))
                                .border(1.dp, Color(0xFFF43F5E), RoundedCornerShape(50))
                                .clickable {
                                    bodyText = ""
                                    onResetBodyCode()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = Color(0xFFFDA4AF),
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Cyan)
                                .clickable { onSaveBodyCode(bodyText) }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = "Save",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = Color(0xFF050816),
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    singleLine = false,
                    placeholder = { Text("// Write event or function logic here...") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 12.5.sp,
                        color = Ink,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Ink,
                        unfocusedTextColor = Ink,
                        cursorColor = Cyan,
                        focusedBorderColor = Cyan,
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedContainerColor = Color(0xFF060A1C),
                        unfocusedContainerColor = Color(0xFF060A1C),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                )

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

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
    onUpdateNode: (IdeWidgetNode) -> Unit,
) {
    var textInput by remember(node.uid, property) {
        mutableStateOf(
            when (property) {
                IdeWidgetProperty.IMAGE -> node.imageResName
                IdeWidgetProperty.SCALE_TYPE -> node.scaleType
                IdeWidgetProperty.TINT -> node.imageTint
                IdeWidgetProperty.BORDER_WIDTH -> node.civBorderWidthDp.toString()
                IdeWidgetProperty.BORDER_COLOR -> node.civBorderColor
                IdeWidgetProperty.CIRCLE_BG_COLOR -> node.civCircleBgColor
                IdeWidgetProperty.CORNER_RADIUS -> node.cornerRadiusDp.toString()
                IdeWidgetProperty.ELEVATION -> node.elevationDp.toString()
                IdeWidgetProperty.RIPPLE_COLOR -> node.rippleColor
                IdeWidgetProperty.ALL_CAPS -> node.allCaps.toString()
                IdeWidgetProperty.ENABLED -> node.enabled.toString()
                IdeWidgetProperty.BUTTON_TINT -> node.buttonTint
                IdeWidgetProperty.TRACK_TINT -> node.trackTint
                IdeWidgetProperty.TEXT_ON -> node.textOn
                IdeWidgetProperty.TEXT_OFF -> node.textOff
                IdeWidgetProperty.SPINNER_ENTRIES -> node.spinnerEntries
                IdeWidgetProperty.SPINNER_MODE -> node.spinnerMode
                IdeWidgetProperty.SPINNER_PROMPT -> node.spinnerPrompt
                IdeWidgetProperty.WEB_URL -> node.webUrl
                IdeWidgetProperty.WEB_JAVASCRIPT -> node.webJavaScript.toString()
                IdeWidgetProperty.WEB_DOM_STORAGE -> node.webDomStorage.toString()
                IdeWidgetProperty.WEB_ZOOM -> node.webSupportZoom.toString()
                IdeWidgetProperty.WEB_DESKTOP_MODE -> node.webDesktopMode.toString()
                IdeWidgetProperty.WEB_FILE_ACCESS -> node.webAllowFileAccess.toString()
                IdeWidgetProperty.WEB_CACHE_MODE -> node.webCacheMode
                IdeWidgetProperty.WEB_USER_AGENT -> node.webUserAgent
                IdeWidgetProperty.ORIENTATION -> node.orientation
                IdeWidgetProperty.CUSTOM_VIEW -> node.customView
                IdeWidgetProperty.DIVIDER_HEIGHT -> node.dividerHeightDp.toString()
                IdeWidgetProperty.NUM_COLUMNS -> node.numColumns.toString()
                IdeWidgetProperty.HORIZONTAL_SPACING -> node.horizontalSpacingDp.toString()
                IdeWidgetProperty.VERTICAL_SPACING -> node.verticalSpacingDp.toString()
                IdeWidgetProperty.CHOICE_MODE -> node.choiceMode
                IdeWidgetProperty.LAYOUT_MANAGER -> node.layoutManager
                IdeWidgetProperty.STACK_FROM_BOTTOM -> node.stackFromBottom.toString()
                IdeWidgetProperty.FAST_SCROLL -> node.fastScroll.toString()
                IdeWidgetProperty.PAGE_LIMIT -> node.pageLimit.toString()
                IdeWidgetProperty.TIME_FORMAT_12H -> node.format12Hour
                IdeWidgetProperty.TIME_FORMAT_24H -> node.format24Hour
                IdeWidgetProperty.IS_24_HOUR_VIEW -> node.is24HourView.toString()
                IdeWidgetProperty.PICKER_MODE -> node.pickerMode
                IdeWidgetProperty.FIRST_DAY_OF_WEEK -> node.firstDayOfWeek.toString()
                IdeWidgetProperty.MIN_DATE -> node.minDate
                IdeWidgetProperty.MAX_DATE -> node.maxDate
                IdeWidgetProperty.SHOW_WEEK_NUMBER -> node.showWeekNumber.toString()
                IdeWidgetProperty.TIME_ZONE -> node.timeZone
                IdeWidgetProperty.COUNT_DOWN -> node.countDown.toString()
                IdeWidgetProperty.CUSTOM_ATTRIBUTES -> node.customAttributes
                IdeWidgetProperty.CONVERT -> node.type.name
                IdeWidgetProperty.WIDTH -> node.layoutWidth
                IdeWidgetProperty.HEIGHT -> node.layoutHeight
                IdeWidgetProperty.PADDING -> node.paddingDp.toString()
                IdeWidgetProperty.MARGIN -> node.marginDp.toString()
                IdeWidgetProperty.GRAVITY -> node.gravity
                IdeWidgetProperty.LAYOUT_GRAVITY -> node.layoutGravity
                IdeWidgetProperty.WEIGHT -> node.weight.toString()
                IdeWidgetProperty.TEXT -> node.text
                IdeWidgetProperty.HINT -> node.hint
                IdeWidgetProperty.HINT_COLOR -> node.hintColor
                IdeWidgetProperty.INPUT_TYPE -> node.inputType
                IdeWidgetProperty.IME_OPTIONS -> node.imeOptions
                IdeWidgetProperty.SINGLE_LINE -> node.singleLine.toString()
                IdeWidgetProperty.CHECKED -> node.checked.toString()
                IdeWidgetProperty.COMPLETION_THRESHOLD -> node.completionThreshold.toString()
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
            IdeWidgetProperty.IMAGE -> node.copy(imageResName = trimmed.removePrefix("@drawable/").ifBlank { "default_image" })
            IdeWidgetProperty.SCALE_TYPE -> node.copy(scaleType = trimmed.ifBlank { "FIT_CENTER" })
            IdeWidgetProperty.TINT -> {
                val clean = if (trimmed.equals("none", ignoreCase = true) || trimmed.isBlank()) {
                    "none"
                } else {
                    val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                    if (parseHexColorOrNull(hex) != null) hex.uppercase() else node.imageTint
                }
                node.copy(imageTint = clean)
            }
            IdeWidgetProperty.BORDER_WIDTH -> {
                val bw = trimmed.removeSuffix("dp").trim().toIntOrNull()?.coerceIn(0, 24) ?: node.civBorderWidthDp
                node.copy(civBorderWidthDp = bw)
            }
            IdeWidgetProperty.BORDER_COLOR -> {
                val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                node.copy(civBorderColor = if (parseHexColorOrNull(hex) != null) hex.uppercase() else node.civBorderColor)
            }
            IdeWidgetProperty.CIRCLE_BG_COLOR -> {
                val clean = if (trimmed.equals("transparent", ignoreCase = true) || trimmed.isBlank()) {
                    "transparent"
                } else {
                    val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                    if (parseHexColorOrNull(hex) != null) hex.uppercase() else node.civCircleBgColor
                }
                node.copy(civCircleBgColor = clean)
            }
            IdeWidgetProperty.CORNER_RADIUS -> {
                val cr = trimmed.removeSuffix("dp").trim().toIntOrNull()?.coerceIn(0, 64) ?: node.cornerRadiusDp
                node.copy(cornerRadiusDp = cr)
            }
            IdeWidgetProperty.ELEVATION -> {
                val el = trimmed.removeSuffix("dp").trim().toIntOrNull()?.coerceIn(0, 24) ?: node.elevationDp
                node.copy(elevationDp = el)
            }
            IdeWidgetProperty.RIPPLE_COLOR -> {
                val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                node.copy(rippleColor = if (parseHexColorOrNull(hex) != null) hex.uppercase() else node.rippleColor)
            }
            IdeWidgetProperty.ALL_CAPS -> node.copy(allCaps = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.ENABLED -> node.copy(enabled = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.BUTTON_TINT -> {
                val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                node.copy(buttonTint = if (parseHexColorOrNull(hex) != null) hex.uppercase() else node.buttonTint)
            }
            IdeWidgetProperty.TRACK_TINT -> {
                val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                node.copy(trackTint = if (parseHexColorOrNull(hex) != null) hex.uppercase() else node.trackTint)
            }
            IdeWidgetProperty.TEXT_ON -> node.copy(textOn = raw.ifBlank { "ON" })
            IdeWidgetProperty.TEXT_OFF -> node.copy(textOff = raw.ifBlank { "OFF" })
            IdeWidgetProperty.SPINNER_ENTRIES -> node.copy(spinnerEntries = raw.ifBlank { "Item 1, Item 2, Item 3" })
            IdeWidgetProperty.SPINNER_MODE -> node.copy(spinnerMode = trimmed.ifBlank { "dropdown" })
            IdeWidgetProperty.SPINNER_PROMPT -> node.copy(spinnerPrompt = raw)
            IdeWidgetProperty.WEB_URL -> node.copy(webUrl = trimmed.ifBlank { "https://google.com" })
            IdeWidgetProperty.WEB_JAVASCRIPT -> node.copy(webJavaScript = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.WEB_DOM_STORAGE -> node.copy(webDomStorage = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.WEB_ZOOM -> node.copy(webSupportZoom = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.WEB_DESKTOP_MODE -> node.copy(webDesktopMode = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.WEB_FILE_ACCESS -> node.copy(webAllowFileAccess = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.WEB_CACHE_MODE -> node.copy(webCacheMode = trimmed.ifBlank { "LOAD_DEFAULT" })
            IdeWidgetProperty.WEB_USER_AGENT -> node.copy(webUserAgent = raw.ifBlank { "default" })
            IdeWidgetProperty.ORIENTATION -> {
                val cleanOrient = if (trimmed.equals("horizontal", ignoreCase = true)) "horizontal" else "vertical"
                val syncedType = when (node.type) {
                    IdeWidgetType.LINEAR_H, IdeWidgetType.LINEAR_V ->
                        if (cleanOrient == "horizontal") IdeWidgetType.LINEAR_H else IdeWidgetType.LINEAR_V
                    IdeWidgetType.SCROLL_V, IdeWidgetType.SCROLL_H ->
                        if (cleanOrient == "horizontal") IdeWidgetType.SCROLL_V else IdeWidgetType.SCROLL_H
                    else -> node.type
                }
                node.copy(orientation = cleanOrient, type = syncedType)
            }
            IdeWidgetProperty.CUSTOM_VIEW -> node.copy(customView = trimmed.ifBlank { "none" })
            IdeWidgetProperty.DIVIDER_HEIGHT -> {
                val dh = trimmed.removeSuffix("dp").trim().toIntOrNull()?.coerceIn(0, 32) ?: node.dividerHeightDp
                node.copy(dividerHeightDp = dh)
            }
            IdeWidgetProperty.NUM_COLUMNS -> {
                val nc = trimmed.toIntOrNull()?.coerceIn(1, 8) ?: node.numColumns
                node.copy(numColumns = nc)
            }
            IdeWidgetProperty.HORIZONTAL_SPACING -> {
                val hs = trimmed.removeSuffix("dp").trim().toIntOrNull()?.coerceIn(0, 48) ?: node.horizontalSpacingDp
                node.copy(horizontalSpacingDp = hs)
            }
            IdeWidgetProperty.VERTICAL_SPACING -> {
                val vs = trimmed.removeSuffix("dp").trim().toIntOrNull()?.coerceIn(0, 48) ?: node.verticalSpacingDp
                node.copy(verticalSpacingDp = vs)
            }
            IdeWidgetProperty.CHOICE_MODE -> node.copy(choiceMode = trimmed.ifBlank { "none" })
            IdeWidgetProperty.LAYOUT_MANAGER -> node.copy(layoutManager = trimmed.ifBlank { "LinearVertical" })
            IdeWidgetProperty.STACK_FROM_BOTTOM -> node.copy(stackFromBottom = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.FAST_SCROLL -> node.copy(fastScroll = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.PAGE_LIMIT -> {
                val pl = trimmed.toIntOrNull()?.coerceIn(1, 10) ?: node.pageLimit
                node.copy(pageLimit = pl)
            }
            IdeWidgetProperty.TIME_FORMAT_12H -> node.copy(format12Hour = raw.ifBlank { "hh:mm:ss a" })
            IdeWidgetProperty.TIME_FORMAT_24H -> node.copy(format24Hour = raw.ifBlank { "HH:mm:ss" })
            IdeWidgetProperty.IS_24_HOUR_VIEW -> node.copy(is24HourView = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.PICKER_MODE -> node.copy(pickerMode = trimmed.ifBlank { "clock" })
            IdeWidgetProperty.FIRST_DAY_OF_WEEK -> {
                val fd = trimmed.toIntOrNull()?.coerceIn(1, 7) ?: node.firstDayOfWeek
                node.copy(firstDayOfWeek = fd)
            }
            IdeWidgetProperty.MIN_DATE -> node.copy(minDate = trimmed.ifBlank { "01/01/2000" })
            IdeWidgetProperty.MAX_DATE -> node.copy(maxDate = trimmed.ifBlank { "12/31/2035" })
            IdeWidgetProperty.SHOW_WEEK_NUMBER -> node.copy(showWeekNumber = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.TIME_ZONE -> node.copy(timeZone = trimmed.ifBlank { "Default" })
            IdeWidgetProperty.COUNT_DOWN -> node.copy(countDown = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.CUSTOM_ATTRIBUTES -> node.copy(customAttributes = trimmed)
            IdeWidgetProperty.CONVERT -> {
                val chosenType = runCatching { IdeWidgetType.valueOf(trimmed) }.getOrDefault(node.type)
                val syncedOrient = when (chosenType) {
                    IdeWidgetType.LINEAR_H, IdeWidgetType.SCROLL_V -> "horizontal"
                    IdeWidgetType.LINEAR_V, IdeWidgetType.SCROLL_H -> "vertical"
                    else -> node.orientation
                }
                node.copy(type = chosenType, orientation = syncedOrient)
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
            IdeWidgetProperty.TEXT -> node.copy(text = if (node.type.isInputTextWidget) raw else raw.ifBlank { node.type.displayName })
            IdeWidgetProperty.HINT -> node.copy(hint = raw.ifBlank { "Hint" })
            IdeWidgetProperty.HINT_COLOR -> {
                val hex = if (trimmed.startsWith("#")) trimmed else "#$trimmed"
                node.copy(hintColor = if (parseHexColorOrNull(hex) != null) hex.uppercase() else node.hintColor)
            }
            IdeWidgetProperty.INPUT_TYPE -> node.copy(inputType = trimmed.ifBlank { "text" })
            IdeWidgetProperty.IME_OPTIONS -> node.copy(imeOptions = trimmed.ifBlank { "actionUnspecified" })
            IdeWidgetProperty.SINGLE_LINE -> node.copy(singleLine = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.CHECKED -> node.copy(checked = trimmed.equals("true", ignoreCase = true))
            IdeWidgetProperty.COMPLETION_THRESHOLD -> {
                val th = trimmed.toIntOrNull()?.coerceIn(1, 10) ?: node.completionThreshold
                node.copy(completionThreshold = th)
            }
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
                val tx = trimmed.removeSuffix("dp").trim().toFloatOrNull()?.coerceIn(-160f, 160f) ?: node.translationX
                node.copy(translationX = tx)
            }
            IdeWidgetProperty.TRANSLATION_Y -> {
                val ty = trimmed.removeSuffix("dp").trim().toFloatOrNull()?.coerceIn(-160f, 160f) ?: node.translationY
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
                        IdeWidgetProperty.IMAGE -> {
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:src (@drawable/...)",
                            )
                        }

                        IdeWidgetProperty.SCALE_TYPE -> {
                            val scaleTypes = listOf(
                                "FIT_CENTER" to "Fit Center (Default)",
                                "CENTER_CROP" to "Center Crop (Fill & Crop)",
                                "FIT_XY" to "Fit XY (Stretch to Bounds)",
                                "CENTER_INSIDE" to "Center Inside",
                                "CENTER" to "Center (Original Size)",
                            )
                            scaleTypes.forEach { (value, subtitle) ->
                                DialogOptionRow(
                                    label = value,
                                    subtitle = subtitle,
                                    selected = textInput.equals(value, ignoreCase = true),
                                    onClick = {
                                        textInput = value
                                        applyAndSave(value)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.TINT -> {
                            val tintColors = listOf(
                                "none",
                                "#000000",
                                "#FFFFFF",
                                "#38BDF8",
                                "#2563EB",
                                "#E11D48",
                                "#16A34A",
                                "#D97706",
                                "#7C3AED",
                            )
                            DialogColorSwatches(
                                colors = tintColors,
                                selectedHex = textInput,
                                onSelectHex = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:tint (#RRGGBB or none)",
                            )
                        }

                        IdeWidgetProperty.BORDER_WIDTH -> {
                            val presets = listOf("0", "1", "2", "3", "4", "6", "8")
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
                                label = "app:civ_border_width (dp)",
                            )
                        }

                        IdeWidgetProperty.BORDER_COLOR -> {
                            val borderColors = listOf(
                                "#38BDF8",
                                "#2563EB",
                                "#7C3AED",
                                "#E11D48",
                                "#16A34A",
                                "#D97706",
                                "#000000",
                                "#FFFFFF",
                            )
                            DialogColorSwatches(
                                colors = borderColors,
                                selectedHex = textInput,
                                onSelectHex = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "app:civ_border_color (#RRGGBB)",
                            )
                        }

                        IdeWidgetProperty.CIRCLE_BG_COLOR -> {
                            val bgColors = listOf(
                                "transparent",
                                "#E0F2FE",
                                "#EFF6FF",
                                "#F1F5F9",
                                "#FEF3C7",
                                "#FCE7F3",
                                "#DCFCE7",
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
                                label = "app:civ_circle_background_color (#RRGGBB or transparent)",
                            )
                        }

                        IdeWidgetProperty.CORNER_RADIUS -> {
                            val presets = listOf("0", "4", "8", "12", "16", "20", "28")
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
                                label = "Corner radius (dp)",
                            )
                        }

                        IdeWidgetProperty.ELEVATION -> {
                            val presets = listOf("0", "2", "4", "6", "8", "12")
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
                                label = "android:elevation (dp)",
                            )
                        }

                        IdeWidgetProperty.RIPPLE_COLOR,
                        IdeWidgetProperty.BUTTON_TINT,
                        IdeWidgetProperty.TRACK_TINT -> {
                            val swatchColors = listOf(
                                "#0284C7",
                                "#38BDF8",
                                "#93C5FD",
                                "#2563EB",
                                "#7C3AED",
                                "#E11D48",
                                "#16A34A",
                                "#D97706",
                                "#000000",
                            )
                            DialogColorSwatches(
                                colors = swatchColors,
                                selectedHex = textInput,
                                onSelectHex = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "${property.title} (#RRGGBB)",
                            )
                        }

                        IdeWidgetProperty.TEXT_ON,
                        IdeWidgetProperty.TEXT_OFF,
                        IdeWidgetProperty.SPINNER_PROMPT -> {
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = property.title,
                            )
                        }

                        IdeWidgetProperty.SPINNER_ENTRIES -> {
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "Spinner entries (comma-separated, e.g. Item 1, Item 2, Item 3)",
                                singleLine = false,
                            )
                        }

                        IdeWidgetProperty.SPINNER_MODE -> {
                            listOf(
                                "dropdown" to "Dropdown Menu (Default)",
                                "dialog" to "Modal Dialog List",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.WEB_URL -> {
                            val urlPresets = listOf(
                                "https://google.com",
                                "https://github.com",
                                "https://developer.android.com",
                                "file:///android_asset/index.html",
                            )
                            urlPresets.forEach { presetUrl ->
                                DialogOptionRow(
                                    label = presetUrl,
                                    selected = textInput.equals(presetUrl, ignoreCase = true),
                                    onClick = {
                                        textInput = presetUrl
                                        applyAndSave(presetUrl)
                                    },
                                )
                            }
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "Web URL (https://... or file:///...)",
                            )
                        }

                        IdeWidgetProperty.WEB_CACHE_MODE -> {
                            listOf(
                                "LOAD_DEFAULT" to "Default Cache Policy",
                                "LOAD_NO_CACHE" to "Always Load from Network (No Cache)",
                                "LOAD_CACHE_ELSE_NETWORK" to "Use Cache Whenever Available",
                                "LOAD_CACHE_ONLY" to "Offline Cache Only",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.WEB_USER_AGENT -> {
                            listOf(
                                "default" to "System WebView Default User-Agent",
                                "Chrome Desktop" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/128.0",
                                "Mobile Android" to "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome Mobile",
                                "Mobile Safari" to "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0) Mobile/15E148 Safari",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "Custom User-Agent String",
                            )
                        }

                        IdeWidgetProperty.ORIENTATION -> {
                            listOf(
                                "vertical" to "Vertical (عمودي — العناصر تحت بعضها)",
                                "horizontal" to "Horizontal (أفقي — العناصر بجانب بعضها)",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.CUSTOM_VIEW -> {
                            listOf(
                                "none" to "Default List Row Layout (بدون Custom View)",
                                "custom_item.xml" to "Custom List Row (@layout/custom_item)",
                                "item_card.xml" to "Card List Row (@layout/item_card)",
                                "grid_cell.xml" to "Grid Cell Layout (@layout/grid_cell)",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "Custom View XML file name (e.g. custom_item.xml)",
                            )
                        }

                        IdeWidgetProperty.DIVIDER_HEIGHT,
                        IdeWidgetProperty.HORIZONTAL_SPACING,
                        IdeWidgetProperty.VERTICAL_SPACING -> {
                            val presets = listOf("0", "1", "2", "4", "8", "12", "16")
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
                                label = "${property.title} (dp)",
                            )
                        }

                        IdeWidgetProperty.NUM_COLUMNS,
                        IdeWidgetProperty.PAGE_LIMIT -> {
                            val presets = listOf("1", "2", "3", "4", "5", "6")
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
                                label = property.title,
                            )
                        }

                        IdeWidgetProperty.CHOICE_MODE -> {
                            listOf(
                                "none" to "No Selection (Default)",
                                "singleChoice" to "Single Choice (اختيار عنصر واحد)",
                                "multipleChoice" to "Multiple Choice (اختيار متعدد)",
                                "multipleChoiceModal" to "Multiple Choice Modal Action Mode",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.LAYOUT_MANAGER -> {
                            listOf(
                                "LinearVertical" to "LinearLayoutManager (Vertical)",
                                "LinearHorizontal" to "LinearLayoutManager (Horizontal)",
                                "Grid" to "GridLayoutManager (Columns)",
                                "StaggeredGrid" to "StaggeredGridLayoutManager",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.TIME_FORMAT_12H -> {
                            listOf(
                                "hh:mm:ss a" to "12:30:45 PM (Hours, Minutes, Seconds)",
                                "hh:mm a" to "12:30 PM (Hours & Minutes)",
                                "EEE, MMM d, hh:mm a" to "Sat, Oct 17, 12:30 PM",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput == valStr,
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:format12Hour",
                            )
                        }

                        IdeWidgetProperty.TIME_FORMAT_24H -> {
                            listOf(
                                "HH:mm:ss" to "14:30:45 (24-Hour with Seconds)",
                                "HH:mm" to "14:30 (24-Hour Short)",
                                "yyyy-MM-dd HH:mm" to "2026-10-17 14:30",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput == valStr,
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:format24Hour",
                            )
                        }

                        IdeWidgetProperty.PICKER_MODE -> {
                            val modes = if (node.type == IdeWidgetType.TIME_PICKER) {
                                listOf(
                                    "clock" to "Clock Dial Mode (ساعة دائرية)",
                                    "spinner" to "Spinner Wheel Mode (بكرة تمرير)",
                                )
                            } else {
                                listOf(
                                    "calendar" to "Calendar Grid Mode (تقويم شهري)",
                                    "spinner" to "Spinner Wheel Mode (بكرة تمرير)",
                                )
                            }
                            modes.forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.FIRST_DAY_OF_WEEK -> {
                            listOf(
                                "1" to "Sunday (الأحد)",
                                "2" to "Monday (الإثنين)",
                                "7" to "Saturday (السبت)",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput == valStr,
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.MIN_DATE,
                        IdeWidgetProperty.MAX_DATE -> {
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "${property.title} (MM/DD/YYYY)",
                            )
                        }

                        IdeWidgetProperty.TIME_ZONE -> {
                            listOf(
                                "Default" to "Device Local Time Zone",
                                "UTC" to "Coordinated Universal Time (UTC)",
                                "Africa/Cairo" to "Cairo (EET / GMT+2)",
                                "Asia/Riyadh" to "Riyadh (AST / GMT+3)",
                                "Europe/London" to "London (GMT / BST)",
                                "America/New_York" to "New York (EST / EDT)",
                            ).forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:timeZone",
                            )
                        }

                        IdeWidgetProperty.CONVERT -> {
                            val candidateTypes = when {
                                node.type.isContainer -> listOf(
                                    IdeWidgetType.LINEAR_H,
                                    IdeWidgetType.LINEAR_V,
                                    IdeWidgetType.SCROLL_H,
                                    IdeWidgetType.SCROLL_V,
                                )
                                node.type.isImageWidget -> listOf(
                                    IdeWidgetType.IMAGE_VIEW,
                                    IdeWidgetType.CIRCLE_IMAGE_VIEW,
                                    IdeWidgetType.IMAGE_BUTTON,
                                    IdeWidgetType.ROUNDED_IMAGE_VIEW,
                                    IdeWidgetType.SHAPEABLE_IMAGE_VIEW,
                                    IdeWidgetType.ZOOM_IMAGE_VIEW,
                                    IdeWidgetType.ASPECT_IMAGE_VIEW,
                                    IdeWidgetType.IMAGE_FILTER_VIEW,
                                )
                                node.type.isButtonWidget -> listOf(
                                    IdeWidgetType.BUTTON,
                                    IdeWidgetType.MATERIAL_BUTTON,
                                    IdeWidgetType.OUTLINED_BUTTON,
                                    IdeWidgetType.TEXT_BUTTON,
                                    IdeWidgetType.FAB_BUTTON,
                                    IdeWidgetType.EXTENDED_FAB,
                                    IdeWidgetType.TOGGLE_BUTTON,
                                    IdeWidgetType.CHIP_BUTTON,
                                )
                                node.type.isChooseWidget -> listOf(
                                    IdeWidgetType.CHECK_BOX,
                                    IdeWidgetType.RADIO_BUTTON,
                                    IdeWidgetType.SWITCH,
                                    IdeWidgetType.SPINNER,
                                )
                                node.type.isWebWidget -> listOf(
                                    IdeWidgetType.WEB_VIEW,
                                    IdeWidgetType.CUSTOM_TAB_VIEW,
                                    IdeWidgetType.GECKO_WEB_VIEW,
                                    IdeWidgetType.AD_WEB_VIEW,
                                    IdeWidgetType.SWIPE_WEB_VIEW,
                                )
                                node.type.isListWidget -> listOf(
                                    IdeWidgetType.GRID_VIEW,
                                    IdeWidgetType.LIST_VIEW,
                                    IdeWidgetType.TREE_VIEW,
                                    IdeWidgetType.RECYCLER_VIEW,
                                    IdeWidgetType.VIEW_PAGER,
                                    IdeWidgetType.EXPANDABLE_LIST_VIEW,
                                    IdeWidgetType.STAGGERED_GRID_VIEW,
                                    IdeWidgetType.HORIZONTAL_LIST_VIEW,
                                    IdeWidgetType.CAROUSEL_VIEW,
                                    IdeWidgetType.SWIPE_LIST_VIEW,
                                )
                                node.type.isDateWidget -> listOf(
                                    IdeWidgetType.ANALOG_CLOCK,
                                    IdeWidgetType.DIGITAL_CLOCK,
                                    IdeWidgetType.TIME_PICKER,
                                    IdeWidgetType.DATE_PICKER,
                                    IdeWidgetType.CALENDAR_VIEW,
                                    IdeWidgetType.TEXT_CLOCK,
                                    IdeWidgetType.CHRONOMETER,
                                    IdeWidgetType.COUNTDOWN_VIEW,
                                    IdeWidgetType.STOPWATCH_VIEW,
                                    IdeWidgetType.DATE_RANGE_PICKER,
                                    IdeWidgetType.WORLD_CLOCK_VIEW,
                                )
                                else -> listOf(
                                    IdeWidgetType.TEXT_VIEW,
                                    IdeWidgetType.EDIT_TEXT,
                                    IdeWidgetType.AUTO_COMPLETE_TEXT,
                                    IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT,
                                    IdeWidgetType.CHECKED_TEXT_VIEW,
                                    IdeWidgetType.TEXT_INPUT_EDIT_TEXT,
                                )
                            }
                            candidateTypes.forEach { t ->
                                val selected = textInput == t.name
                                DialogOptionRow(
                                    label = t.displayName,
                                    subtitle = when {
                                        t.isContainer -> "Layout Container"
                                        t.isImageWidget -> "Image Widget"
                                        t.isButtonWidget -> "Button Widget"
                                        t.isChooseWidget -> "Choose Widget"
                                        t.isWebWidget -> "Web Widget"
                                        t.isListWidget -> "List Widget"
                                        t.isDateWidget -> "Date / Time Widget"
                                        else -> "Text Widget"
                                    },
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

                        IdeWidgetProperty.HINT -> {
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:hint",
                            )
                        }

                        IdeWidgetProperty.HINT_COLOR -> {
                            val hintColors = listOf(
                                "#94A3B8",
                                "#64748B",
                                "#475569",
                                "#0284C7",
                                "#2563EB",
                                "#E11D48",
                                "#16A34A",
                                "#D97706",
                                "#7C3AED",
                            )
                            DialogColorSwatches(
                                colors = hintColors,
                                selectedHex = textInput,
                                onSelectHex = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:textColorHint (#RRGGBB)",
                            )
                        }

                        IdeWidgetProperty.INPUT_TYPE -> {
                            val inputTypes = listOf(
                                "text" to "Plain Text (Default)",
                                "textPassword" to "Password (Hidden Dots)",
                                "number" to "Integer Number",
                                "numberDecimal" to "Decimal Number",
                                "phone" to "Phone Number",
                                "textEmailAddress" to "Email Address",
                                "textMultiLine" to "Multi-Line Text",
                                "textPersonName" to "Person Name",
                                "textUri" to "Web URL / URI",
                            )
                            inputTypes.forEach { (value, subtitle) ->
                                DialogOptionRow(
                                    label = value,
                                    subtitle = subtitle,
                                    selected = textInput.equals(value, ignoreCase = true),
                                    onClick = {
                                        textInput = value
                                        applyAndSave(value)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.IME_OPTIONS -> {
                            val imeList = listOf(
                                "actionUnspecified" to "Default (Unspecified)",
                                "actionDone" to "Done (✓)",
                                "actionSearch" to "Search (🔍)",
                                "actionSend" to "Send (➤)",
                                "actionNext" to "Next (→)",
                                "actionGo" to "Go (↵)",
                            )
                            imeList.forEach { (value, subtitle) ->
                                DialogOptionRow(
                                    label = value,
                                    subtitle = subtitle,
                                    selected = textInput.equals(value, ignoreCase = true),
                                    onClick = {
                                        textInput = value
                                        applyAndSave(value)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.SINGLE_LINE,
                        IdeWidgetProperty.CHECKED,
                        IdeWidgetProperty.ALL_CAPS,
                        IdeWidgetProperty.ENABLED,
                        IdeWidgetProperty.WEB_JAVASCRIPT,
                        IdeWidgetProperty.WEB_DOM_STORAGE,
                        IdeWidgetProperty.WEB_ZOOM,
                        IdeWidgetProperty.WEB_DESKTOP_MODE,
                        IdeWidgetProperty.WEB_FILE_ACCESS,
                        IdeWidgetProperty.STACK_FROM_BOTTOM,
                        IdeWidgetProperty.FAST_SCROLL,
                        IdeWidgetProperty.IS_24_HOUR_VIEW,
                        IdeWidgetProperty.SHOW_WEEK_NUMBER,
                        IdeWidgetProperty.COUNT_DOWN -> {
                            listOf("true" to "Enabled (True)", "false" to "Disabled (False)").forEach { (valStr, sub) ->
                                DialogOptionRow(
                                    label = valStr,
                                    subtitle = sub,
                                    selected = textInput.equals(valStr, ignoreCase = true),
                                    onClick = {
                                        textInput = valStr
                                        applyAndSave(valStr)
                                    },
                                )
                            }
                        }

                        IdeWidgetProperty.COMPLETION_THRESHOLD -> {
                            val presets = listOf("1", "2", "3", "4", "5")
                            DialogPresetChipsGrid(
                                presets = presets,
                                selectedValue = textInput,
                                suffix = " chars",
                                onSelect = { chosen ->
                                    textInput = chosen
                                    applyAndSave(chosen)
                                },
                            )
                            DialogTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = "android:completionThreshold (1-10)",
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
                                label = "android:${property.title.replaceFirstChar { it.lowercase() }} (dp — stays inside parent layout)",
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
                TextButton(onClick = { applyAndSave() }) {
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
    projectImages: List<ProjectImageItem>,
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
                                .clipToBounds()
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
                                        projectImages = projectImages,
                                        selectedWidgetUid = null,
                                        draggingType = null,
                                        draggingExistingUid = null,
                                        hoveredContainerUid = null,
                                        dropInsertIndex = -1,
                                        onSelectWidget = {},
                                        onDoubleTapWidget = {},
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

private fun findParentNodeOf(nodes: List<IdeWidgetNode>, childUid: String): IdeWidgetNode? {
    for (node in nodes) {
        if (node.children.any { it.uid == childUid }) return node
        val found = findParentNodeOf(node.children, childUid)
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

private fun renameImageResInTree(
    nodes: List<IdeWidgetNode>,
    oldResName: String,
    newResName: String,
): List<IdeWidgetNode> = nodes.map { node ->
    val updatedChildren = if (node.children.isEmpty()) {
        emptyList()
    } else {
        renameImageResInTree(node.children, oldResName, newResName)
    }
    node.copy(
        imageResName = if (node.imageResName == oldResName) newResName else node.imageResName,
        backgroundResource = if (node.backgroundResource == "@drawable/$oldResName") {
            "@drawable/$newResName"
        } else {
            node.backgroundResource
        },
        children = updatedChildren,
    )
}

// ============================================================================
// 7. ImageView Select Image Bottom Sheet (Lists all images from Image Manager)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImageViewSelectImageBottomSheet(
    node: IdeWidgetNode,
    projectImages: List<ProjectImageItem>,
    onSelectImageRes: (String) -> Unit,
    onOpenImageManager: () -> Unit,
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Select Image • تحديد الصورة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                        )
                        Text(
                            text = "@+id/${node.viewId} • Choose from Image Manager",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 11.sp,
                            ),
                            color = Cyan,
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Cyan.copy(alpha = 0.16f))
                            .border(1.dp, Cyan.copy(alpha = 0.50f), RoundedCornerShape(50))
                            .clickable(onClick = onOpenImageManager)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "Image Manager",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Cyan,
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    // Default placeholder image row
                    val isDefaultSelected = node.imageResName == "default_image" || node.imageResName.isBlank()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDefaultSelected) Cyan.copy(alpha = 0.16f) else Color(0xFF141D3F))
                            .border(
                                width = if (isDefaultSelected) 1.5.dp else 1.dp,
                                color = if (isDefaultSelected) Cyan else Color(0x26FFFFFF),
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { onSelectImageRes("default_image") }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E2952))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            SketchWidgetIcon(type = node.type, tint = Cyan)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "default_image",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontFamily = JetBrainsMono,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = if (isDefaultSelected) Cyan else Ink,
                            )
                            Text(
                                text = "Default ${node.type.displayName} placeholder icon",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                color = Muted,
                            )
                        }
                        if (isDefaultSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Cyan,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    if (projectImages.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF121A3A))
                                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(16.dp))
                                .clickable(onClick = onOpenImageManager)
                                .padding(horizontal = 16.dp, vertical = 22.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = Cyan,
                                    modifier = Modifier.size(28.dp),
                                )
                                Text(
                                    text = "لا توجد صور مضافة في Image Manager بعد",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Ink,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = "اضغط هنا لفتح صفحة Image Manager واستيراد صور من جهازك",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Cyan,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        projectImages.forEach { img ->
                            val selected = node.imageResName == img.resName
                            val bmp = remember(img.filePath) {
                                runCatching { BitmapFactory.decodeFile(img.filePath) }.getOrNull()
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (selected) Cyan.copy(alpha = 0.16f) else Color(0xFF141D3F))
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) Cyan else Color(0x26FFFFFF),
                                        shape = RoundedCornerShape(14.dp),
                                    )
                                    .clickable { onSelectImageRes(img.resName) }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (bmp != null) {
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = img.resName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                    } else {
                                        SketchWidgetIcon(type = IdeWidgetType.IMAGE_VIEW, tint = Color(0xFF475569))
                                    }
                                }

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = img.resName,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = if (selected) Cyan else Ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "@drawable/${img.resName} • ${(img.sizeBytes / 1024L).coerceAtLeast(1L)} KB",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontSize = 10.5.sp,
                                        ),
                                        color = Muted,
                                    )
                                }

                                if (selected) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(Cyan),
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
                    }
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

// ============================================================================
// 8. Image Manager Screen (Opened from Side Menu -> Image Manager)
//    - FAB (+) imports images from device into app/src/main/res/drawable/
//    - Long-press in normal mode opens bottom sheet:
//        1. تعديل الاسم (Rename)
//        2. حذف العنصر (Delete item)
//        3. تحديد متعدد (Multi-select)
//    - In Multi-select mode:
//        - Tap items to select/deselect
//        - Long-press opens bottom sheet with: حذف الملفات (Delete files)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun IdeImageManagerScreen(
    project: WorkspaceItem,
    images: List<ProjectImageItem>,
    onImportUris: (List<android.net.Uri>) -> Unit,
    onRenameImage: (String, String) -> Unit,
    onDeleteImages: (Set<String>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isMultiSelectMode by remember { mutableStateOf(false) }
    var selectedResNames by remember { mutableStateOf<Set<String>>(emptySet()) }
    var activeSheetImage by remember { mutableStateOf<ProjectImageItem?>(null) }
    var showMultiSelectSheet by remember { mutableStateOf(false) }
    var renameTargetImage by remember { mutableStateOf<ProjectImageItem?>(null) }
    var renameInput by remember { mutableStateOf("") }
    var previewImage by remember { mutableStateOf<ProjectImageItem?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportUris(uris)
        }
    }

    BackHandler {
        if (isMultiSelectMode) {
            isMultiSelectMode = false
            selectedResNames = emptySet()
        } else {
            onBack()
        }
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
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0F24))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GlassIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        onClick = {
                            if (isMultiSelectMode) {
                                isMultiSelectMode = false
                                selectedResNames = emptySet()
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.size(40.dp),
                    )

                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (isMultiSelectMode) {
                                "تحديد متعدد (${selectedResNames.size}) • Multi-Select"
                            } else {
                                "Image Manager • مدير الصور"
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (isMultiSelectMode) {
                                "اضغط لتحديد الصور أو اضغط مطولاً لحذف الملفات"
                            } else {
                                "${project.name} • ${images.size} drawable images"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 10.5.sp,
                            ),
                            color = Cyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (isMultiSelectMode) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Cyan.copy(alpha = 0.16f))
                                    .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(50))
                                    .clickable {
                                        selectedResNames = if (selectedResNames.size == images.size) {
                                            emptySet()
                                        } else {
                                            images.map { it.resName }.toSet()
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text = if (selectedResNames.size == images.size) "إلغاء الكل" else "تحديد الكل",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Cyan,
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0x22FFFFFF))
                                    .clickable {
                                        isMultiSelectMode = false
                                        selectedResNames = emptySet()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                            ) {
                                Text(
                                    text = "إلغاء",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = Ink,
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x24FFFFFF)),
                )

                if (images.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Cyan.copy(alpha = 0.14f))
                                    .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                                    .clickable { imagePickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = Cyan,
                                    modifier = Modifier.size(34.dp),
                                )
                            }
                            Text(
                                text = "لا توجد صور في المشروع حالياً",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Ink,
                            )
                            Text(
                                text = "اضغط على زر الإضافة العائم (+) بالأسفل لاستيراد صور من جهازك إلى المشروع",
                                style = MaterialTheme.typography.bodySmall,
                                color = Muted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        images.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                rowItems.forEach { item ->
                                    val isSelected = item.resName in selectedResNames
                                    val bmp = remember(item.filePath) {
                                        runCatching { BitmapFactory.decodeFile(item.filePath) }.getOrNull()
                                    }
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isSelected) Cyan.copy(alpha = 0.18f) else Color(0xFF121A3A),
                                            )
                                            .border(
                                                width = if (isSelected) 1.8.dp else 1.dp,
                                                color = if (isSelected) Cyan else Color(0x28FFFFFF),
                                                shape = RoundedCornerShape(16.dp),
                                            )
                                            .combinedClickable(
                                                onClick = {
                                                    if (isMultiSelectMode) {
                                                        selectedResNames = if (isSelected) {
                                                            selectedResNames - item.resName
                                                        } else {
                                                            selectedResNames + item.resName
                                                        }
                                                    } else {
                                                        previewImage = item
                                                    }
                                                },
                                                onLongClick = {
                                                    if (isMultiSelectMode) {
                                                        if (!isSelected) {
                                                            selectedResNames = selectedResNames + item.resName
                                                        }
                                                        showMultiSelectSheet = true
                                                    } else {
                                                        activeSheetImage = item
                                                    }
                                                },
                                            )
                                            .padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(112.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White)
                                                .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (bmp != null) {
                                                Image(
                                                    bitmap = bmp.asImageBitmap(),
                                                    contentDescription = item.resName,
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(4.dp),
                                                )
                                            } else {
                                                SketchWidgetIcon(type = IdeWidgetType.IMAGE_VIEW, tint = Color(0xFF475569))
                                            }

                                            if (isMultiSelectMode) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopEnd)
                                                        .padding(6.dp)
                                                        .size(24.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            if (isSelected) Cyan else Color(0x990B1028),
                                                        )
                                                        .border(
                                                            width = 1.5.dp,
                                                            color = if (isSelected) Cyan else Color.White,
                                                            shape = CircleShape,
                                                        ),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    if (isSelected) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Check,
                                                            contentDescription = null,
                                                            tint = Color(0xFF050816),
                                                            modifier = Modifier.size(15.dp),
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Text(
                                                text = item.resName,
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontFamily = JetBrainsMono,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                ),
                                                color = if (isSelected) Cyan else Ink,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                            Text(
                                                text = "@drawable/${item.resName} • ${(item.sizeBytes / 1024L).coerceAtLeast(1L)} KB",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontFamily = JetBrainsMono,
                                                    fontSize = 10.sp,
                                                ),
                                                color = Muted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                        Spacer(Modifier.height(84.dp))
                    }
                }
            }

            // Floating Action Button (+) to import images from phone into project
            FloatingActionButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                containerColor = Cyan,
                contentColor = Color(0xFF050816),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = if (isMultiSelectMode && selectedResNames.isNotEmpty()) 76.dp else 20.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Import Image",
                    modifier = Modifier.size(26.dp),
                )
            }

            // Bottom Action Bar when in Multi-select mode with selected items
            if (isMultiSelectMode && selectedResNames.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color(0xFF131B3A))
                        .border(1.dp, Color(0xFFF43F5E).copy(alpha = 0.5f))
                        .navigationBarsPadding()
                        .clickable { showMultiSelectSheet = true }
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "تم تحديد ${selectedResNames.size} ملفات",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF43F5E))
                            .clickable { showMultiSelectSheet = true }
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "حذف الملفات (${selectedResNames.size})",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }

    // 1. Long-press Bottom Sheet in Normal Mode:
    //    - تعديل الاسم (Rename)
    //    - حذف العنصر (Delete item)
    //    - تحديد متعدد (Multi-select)
    val sheetItem = activeSheetImage
    if (sheetItem != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { activeSheetImage = null },
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
                Text(
                    text = "@drawable/${sheetItem.resName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Cyan,
                )

                // Option 1: تعديل الاسم (Rename)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141E42))
                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(14.dp))
                        .clickable {
                            val target = sheetItem
                            activeSheetImage = null
                            renameInput = target.resName
                            renameTargetImage = target
                        }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "تعديل الاسم • Rename",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                    Text(
                        text = sheetItem.resName,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMono),
                        color = Cyan,
                    )
                }

                // Option 2: تحديد متعدد (Multi-select)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141E42))
                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(14.dp))
                        .clickable {
                            val target = sheetItem
                            activeSheetImage = null
                            isMultiSelectMode = true
                            selectedResNames = setOf(target.resName)
                        }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "تحديد متعدد • Multi-select",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Ink,
                    )
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(18.dp),
                    )
                }

                // Option 3: حذف العنصر (Delete item)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF3B0D1E))
                        .border(1.dp, Color(0xFFF43F5E).copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        .clickable {
                            val target = sheetItem
                            activeSheetImage = null
                            onDeleteImages(setOf(target.resName))
                        }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "حذف العنصر • Delete Item",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFDA4AF),
                    )
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color(0xFFFDA4AF),
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }

    // 2. Long-press Bottom Sheet in Multi-Select Mode:
    //    - حذف الملفات (Delete files)
    if (showMultiSelectSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showMultiSelectSheet = false },
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
                Text(
                    text = "تم تحديد ${selectedResNames.size} ملفات • Selected Files",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Ink,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF4C0519))
                        .border(1.dp, Color(0xFFF43F5E), RoundedCornerShape(14.dp))
                        .clickable {
                            val targets = selectedResNames
                            showMultiSelectSheet = false
                            if (targets.isNotEmpty()) {
                                onDeleteImages(targets)
                            }
                            isMultiSelectMode = false
                            selectedResNames = emptySet()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "حذف الملفات (${selectedResNames.size}) • Delete Files",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }

    // 3. Rename Image Dialog
    val renameTarget = renameTargetImage
    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTargetImage = null },
            containerColor = Color(0xFF0D1230),
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "تعديل الاسم • Rename Image",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "أدخل الاسم الجديد للصورة (أحرف إنجليزية صغيرة وأرقام و _):",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        OutlinedTextField(
                            value = renameInput,
                            onValueChange = { renameInput = it },
                            singleLine = true,
                            label = { Text("@drawable/...") },
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
                        val newName = renameInput.trim()
                        if (newName.isNotEmpty()) {
                            onRenameImage(renameTarget.resName, newName)
                        }
                        renameTargetImage = null
                    },
                ) {
                    Text(stringResource(R.string.dialog_save), color = Cyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTargetImage = null }) {
                    Text(stringResource(R.string.dialog_cancel), color = Muted)
                }
            },
        )
    }

    // 4. Quick Image Preview Dialog on single tap in normal mode
    val currentPreview = previewImage
    if (currentPreview != null) {
        val bmp = remember(currentPreview.filePath) {
            runCatching { BitmapFactory.decodeFile(currentPreview.filePath) }.getOrNull()
        }
        AlertDialog(
            onDismissRequest = { previewImage = null },
            containerColor = Color(0xFF0D1230),
            titleContentColor = Ink,
            textContentColor = Ink,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "@drawable/${currentPreview.resName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    if (bmp != null) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = currentPreview.resName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { previewImage = null }) {
                    Text(stringResource(R.string.ide_temp_run_close), color = Cyan)
                }
            },
        )
    }
}

