package com.androidexl.ideultimate.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale
import java.util.UUID

enum class WorkspaceItemType { FOLDER, PROJECT }

enum class ProjectLanguage(
    val displayName: String,
    val shortBadge: String,
) {
    FLUTTER(displayName = "Flutter", shortBadge = "FLUTTER"),
    KOTLIN(displayName = "Kotlin", shortBadge = "KOTLIN"),
    JAVA(displayName = "Java", shortBadge = "JAVA"),
}

data class WorkspaceItem(
    val name: String,
    val type: WorkspaceItemType,
    val parentId: String?,
    val id: String = UUID.randomUUID().toString(),
    val packageName: String = ProjectZipExporter.packageName(name),
    val language: ProjectLanguage = ProjectLanguage.KOTLIN,
    val versionCode: Int = 1,
    val versionName: String = "1.0.0",
    val iconPreset: Int = 0,
    val customIconPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * Represents an XML layout page (e.g. `activity_main.xml`) and its paired
 * Activity class (e.g. `MainActivity` / `MainActivity.kt`) inside an IDE Project.
 */
data class ProjectXmlPage(
    val xmlFileName: String,
    val activityName: String,
    val codeFileName: String,
) {
    val layoutResourceName: String
        get() = xmlFileName.removeSuffix(".xml")
}

enum class CodeEditorFlavor(
    val label: String,
    val extension: String,
) {
    KOTLIN_ANDROID("Kotlin Android", "kt"),
    JAVA_ANDROID("Java Android", "java"),
    XML_ANDROID("xml Android", "xml"),
}

enum class IdeWidgetCategory(val title: String) {
    LAYOUT("layout"),
    TEXT("text"),
    IMAGE("image"),
    BUTTONS("buttons"),
    CHOOSE("choose"),
    WEB("web"),
    LIST("list"),
    DATE("date"),
}

enum class IdeWidgetType(
    val displayName: String,
    val shortTag: String,
    val idPrefix: String,
    val category: IdeWidgetCategory,
    val isContainer: Boolean,
) {
    // 1. Layouts
    LINEAR_H(
        displayName = "Linear(H)",
        shortTag = "H",
        idPrefix = "linear",
        category = IdeWidgetCategory.LAYOUT,
        isContainer = true,
    ),
    LINEAR_V(
        displayName = "Linear(V)",
        shortTag = "V",
        idPrefix = "linear",
        category = IdeWidgetCategory.LAYOUT,
        isContainer = true,
    ),
    SCROLL_V(
        displayName = "Scroll(V)",
        shortTag = "SV",
        idPrefix = "vscroll",
        category = IdeWidgetCategory.LAYOUT,
        isContainer = true,
    ),
    SCROLL_H(
        displayName = "Scroll(H)",
        shortTag = "SH",
        idPrefix = "hscroll",
        category = IdeWidgetCategory.LAYOUT,
        isContainer = true,
    ),

    // 2. Text Widgets (TextView, EditText, AutoCompleteText + 3 additional Text widgets)
    TEXT_VIEW(
        displayName = "TextView",
        shortTag = "Ab",
        idPrefix = "textview",
        category = IdeWidgetCategory.TEXT,
        isContainer = false,
    ),
    EDIT_TEXT(
        displayName = "EditText",
        shortTag = "Ed",
        idPrefix = "edittext",
        category = IdeWidgetCategory.TEXT,
        isContainer = false,
    ),
    AUTO_COMPLETE_TEXT(
        displayName = "AutoCompleteText",
        shortTag = "AC",
        idPrefix = "autocompletetextview",
        category = IdeWidgetCategory.TEXT,
        isContainer = false,
    ),
    MULTI_AUTO_COMPLETE_TEXT(
        displayName = "MultiAutoComplete",
        shortTag = "MAC",
        idPrefix = "multiautocompletetextview",
        category = IdeWidgetCategory.TEXT,
        isContainer = false,
    ),
    CHECKED_TEXT_VIEW(
        displayName = "CheckedTextView",
        shortTag = "CTV",
        idPrefix = "checkedtextview",
        category = IdeWidgetCategory.TEXT,
        isContainer = false,
    ),
    TEXT_INPUT_EDIT_TEXT(
        displayName = "TextInputEdit",
        shortTag = "TIE",
        idPrefix = "textinputedittext",
        category = IdeWidgetCategory.TEXT,
        isContainer = false,
    ),

    // 3. Image Widgets (ImageView, CircleImageView + 6 specialized Image widgets)
    IMAGE_VIEW(
        displayName = "ImageView",
        shortTag = "IMG",
        idPrefix = "imageview",
        category = IdeWidgetCategory.IMAGE,
        isContainer = false,
    ),
    CIRCLE_IMAGE_VIEW(
        displayName = "CircleImageView",
        shortTag = "CIV",
        idPrefix = "circleimageview",
        category = IdeWidgetCategory.IMAGE,
        isContainer = false,
    ),
    IMAGE_BUTTON(
        displayName = "ImageButton",
        shortTag = "IB",
        idPrefix = "imagebutton",
        category = IdeWidgetCategory.IMAGE,
        isContainer = false,
    ),
    ROUNDED_IMAGE_VIEW(
        displayName = "RoundedImageView",
        shortTag = "RIV",
        idPrefix = "roundedimageview",
        category = IdeWidgetCategory.IMAGE,
        isContainer = false,
    ),
    SHAPEABLE_IMAGE_VIEW(
        displayName = "ShapeableImage",
        shortTag = "SIV",
        idPrefix = "shapeableimageview",
        category = IdeWidgetCategory.IMAGE,
        isContainer = false,
    ),
    ZOOM_IMAGE_VIEW(
        displayName = "ZoomImageView",
        shortTag = "ZIV",
        idPrefix = "zoomimageview",
        category = IdeWidgetCategory.IMAGE,
        isContainer = false,
    ),
    ASPECT_IMAGE_VIEW(
        displayName = "AspectImageView",
        shortTag = "AIV",
        idPrefix = "aspectimageview",
        category = IdeWidgetCategory.IMAGE,
        isContainer = false,
    ),
    IMAGE_FILTER_VIEW(
        displayName = "ImageFilterView",
        shortTag = "IFV",
        idPrefix = "imagefilterview",
        category = IdeWidgetCategory.IMAGE,
        isContainer = false,
    ),

    // 4. Buttons Widgets (Button, MaterialButton + 6 additional Button widgets)
    BUTTON(
        displayName = "Button",
        shortTag = "BTN",
        idPrefix = "button",
        category = IdeWidgetCategory.BUTTONS,
        isContainer = false,
    ),
    MATERIAL_BUTTON(
        displayName = "MaterialButton",
        shortTag = "MB",
        idPrefix = "materialbutton",
        category = IdeWidgetCategory.BUTTONS,
        isContainer = false,
    ),
    OUTLINED_BUTTON(
        displayName = "OutlinedButton",
        shortTag = "OB",
        idPrefix = "outlinedbutton",
        category = IdeWidgetCategory.BUTTONS,
        isContainer = false,
    ),
    TEXT_BUTTON(
        displayName = "TextButton",
        shortTag = "TB",
        idPrefix = "textbutton",
        category = IdeWidgetCategory.BUTTONS,
        isContainer = false,
    ),
    FAB_BUTTON(
        displayName = "FAB",
        shortTag = "FAB",
        idPrefix = "fab",
        category = IdeWidgetCategory.BUTTONS,
        isContainer = false,
    ),
    EXTENDED_FAB(
        displayName = "ExtendedFAB",
        shortTag = "EFAB",
        idPrefix = "extendedfab",
        category = IdeWidgetCategory.BUTTONS,
        isContainer = false,
    ),
    TOGGLE_BUTTON(
        displayName = "ToggleButton",
        shortTag = "TGB",
        idPrefix = "togglebutton",
        category = IdeWidgetCategory.BUTTONS,
        isContainer = false,
    ),
    CHIP_BUTTON(
        displayName = "ChipButton",
        shortTag = "CHP",
        idPrefix = "chipbutton",
        category = IdeWidgetCategory.BUTTONS,
        isContainer = false,
    ),

    // 5. Choose Widgets (CheckBox, RadioButton, Switch, Spinner)
    CHECK_BOX(
        displayName = "CheckBox",
        shortTag = "CB",
        idPrefix = "checkbox",
        category = IdeWidgetCategory.CHOOSE,
        isContainer = false,
    ),
    RADIO_BUTTON(
        displayName = "RadioButton",
        shortTag = "RB",
        idPrefix = "radiobutton",
        category = IdeWidgetCategory.CHOOSE,
        isContainer = false,
    ),
    SWITCH(
        displayName = "Switch",
        shortTag = "SW",
        idPrefix = "switch",
        category = IdeWidgetCategory.CHOOSE,
        isContainer = false,
    ),
    SPINNER(
        displayName = "Spinner",
        shortTag = "SP",
        idPrefix = "spinner",
        category = IdeWidgetCategory.CHOOSE,
        isContainer = false,
    ),

    // 6. Web Widgets (WebView + 4 specialized Web display widgets)
    WEB_VIEW(
        displayName = "WebView",
        shortTag = "WEB",
        idPrefix = "webview",
        category = IdeWidgetCategory.WEB,
        isContainer = false,
    ),
    CUSTOM_TAB_VIEW(
        displayName = "CustomTabView",
        shortTag = "CTB",
        idPrefix = "customtabview",
        category = IdeWidgetCategory.WEB,
        isContainer = false,
    ),
    GECKO_WEB_VIEW(
        displayName = "GeckoWebView",
        shortTag = "GWB",
        idPrefix = "geckowebview",
        category = IdeWidgetCategory.WEB,
        isContainer = false,
    ),
    AD_WEB_VIEW(
        displayName = "AdBlockWebView",
        shortTag = "AWB",
        idPrefix = "adwebview",
        category = IdeWidgetCategory.WEB,
        isContainer = false,
    ),
    SWIPE_WEB_VIEW(
        displayName = "SwipeWebView",
        shortTag = "SWB",
        idPrefix = "swipewebview",
        category = IdeWidgetCategory.WEB,
        isContainer = false,
    ),

    // 7. List Widgets (GridView, ListView, TreeView, RecyclerView, ViewPager + 5 specialized List widgets)
    GRID_VIEW(
        displayName = "GridView",
        shortTag = "GRD",
        idPrefix = "gridview",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),
    LIST_VIEW(
        displayName = "ListView",
        shortTag = "LST",
        idPrefix = "listview",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),
    TREE_VIEW(
        displayName = "TreeView",
        shortTag = "TRE",
        idPrefix = "treeview",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),
    RECYCLER_VIEW(
        displayName = "RecyclerView",
        shortTag = "RCV",
        idPrefix = "recyclerview",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),
    VIEW_PAGER(
        displayName = "ViewPager",
        shortTag = "VP",
        idPrefix = "viewpager",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),
    EXPANDABLE_LIST_VIEW(
        displayName = "ExpandableList",
        shortTag = "EXL",
        idPrefix = "expandablelistview",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),
    STAGGERED_GRID_VIEW(
        displayName = "StaggeredGrid",
        shortTag = "STG",
        idPrefix = "staggeredgrid",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),
    HORIZONTAL_LIST_VIEW(
        displayName = "HorizontalList",
        shortTag = "HLS",
        idPrefix = "hlistview",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),
    CAROUSEL_VIEW(
        displayName = "CarouselView",
        shortTag = "CRS",
        idPrefix = "carouselview",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),
    SWIPE_LIST_VIEW(
        displayName = "SwipeListView",
        shortTag = "SWL",
        idPrefix = "swipelistview",
        category = IdeWidgetCategory.LIST,
        isContainer = false,
    ),

    // 8. Date & Time Widgets (AnalogClock, DigitalClock, TimePicker, DatePicker, CalendarView + 6 Date/Time widgets)
    ANALOG_CLOCK(
        displayName = "AnalogClock",
        shortTag = "ACL",
        idPrefix = "analogclock",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    DIGITAL_CLOCK(
        displayName = "DigitalClock",
        shortTag = "DCL",
        idPrefix = "digitalclock",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    TIME_PICKER(
        displayName = "TimePicker",
        shortTag = "TPK",
        idPrefix = "timepicker",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    DATE_PICKER(
        displayName = "DatePicker",
        shortTag = "DPK",
        idPrefix = "datepicker",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    CALENDAR_VIEW(
        displayName = "CalendarView",
        shortTag = "CAL",
        idPrefix = "calendarview",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    TEXT_CLOCK(
        displayName = "TextClock",
        shortTag = "TCL",
        idPrefix = "textclock",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    CHRONOMETER(
        displayName = "Chronometer",
        shortTag = "CHR",
        idPrefix = "chronometer",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    COUNTDOWN_VIEW(
        displayName = "CountDownView",
        shortTag = "CDN",
        idPrefix = "countdownview",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    STOPWATCH_VIEW(
        displayName = "StopwatchView",
        shortTag = "STW",
        idPrefix = "stopwatchview",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    DATE_RANGE_PICKER(
        displayName = "DateRangePicker",
        shortTag = "DRP",
        idPrefix = "daterangepicker",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    ),
    WORLD_CLOCK_VIEW(
        displayName = "WorldClockView",
        shortTag = "WCL",
        idPrefix = "worldclock",
        category = IdeWidgetCategory.DATE,
        isContainer = false,
    );

    val isTextWidget: Boolean
        get() = category == IdeWidgetCategory.TEXT

    val isImageWidget: Boolean
        get() = category == IdeWidgetCategory.IMAGE

    val isButtonWidget: Boolean
        get() = category == IdeWidgetCategory.BUTTONS

    val isChooseWidget: Boolean
        get() = category == IdeWidgetCategory.CHOOSE

    val isWebWidget: Boolean
        get() = category == IdeWidgetCategory.WEB

    val isListWidget: Boolean
        get() = category == IdeWidgetCategory.LIST

    val isDateWidget: Boolean
        get() = category == IdeWidgetCategory.DATE

    val isClockTextWidget: Boolean
        get() = this == DIGITAL_CLOCK ||
            this == TEXT_CLOCK ||
            this == CHRONOMETER ||
            this == COUNTDOWN_VIEW ||
            this == STOPWATCH_VIEW ||
            this == WORLD_CLOCK_VIEW

    val hasTextLabel: Boolean
        get() = isTextWidget ||
            (isButtonWidget && this != FAB_BUTTON) ||
            (isChooseWidget && this != SPINNER) ||
            isClockTextWidget

    val isCheckableWidget: Boolean
        get() = this == CHECKED_TEXT_VIEW ||
            this == CHECK_BOX ||
            this == RADIO_BUTTON ||
            this == SWITCH ||
            this == TOGGLE_BUTTON ||
            this == CHIP_BUTTON

    val isInputTextWidget: Boolean
        get() = this == EDIT_TEXT ||
            this == AUTO_COMPLETE_TEXT ||
            this == MULTI_AUTO_COMPLETE_TEXT ||
            this == TEXT_INPUT_EDIT_TEXT
}

data class ProjectImageItem(
    val name: String,
    val filePath: String,
    val addedAt: Long = System.currentTimeMillis(),
) {
    val resName: String
        get() = name

    val sizeBytes: Long
        get() = runCatching { File(filePath).length() }.getOrDefault(0L)
}

data class ProjectSoundItem(
    val name: String,
    val extension: String,
    val filePath: String,
    val addedAt: Long = System.currentTimeMillis(),
) {
    val resName: String
        get() = name

    val sizeBytes: Long
        get() = runCatching { File(filePath).length() }.getOrDefault(0L)
}

data class ProjectFontItem(
    val name: String,
    val extension: String,
    val filePath: String,
    val addedAt: Long = System.currentTimeMillis(),
) {
    val resName: String
        get() = name

    val sizeBytes: Long
        get() = runCatching { File(filePath).length() }.getOrDefault(0L)
}

data class ProjectResourceEntry(
    val name: String,
    val relativePath: String,
    val absolutePath: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val childCount: Int,
    val lastModified: Long,
)

enum class IdeLogicSection(val title: String) {
    ACTIVITY("Activity"),
    VIEW("View"),
    COMPONENT("Component"),
    DRAWER("Drawer"),
    FUNCTIONS("Functions"),
}

data class IdeFunctionParam(
    val name: String,
    val type: String,
)

data class IdeLogicItem(
    val id: String = UUID.randomUUID().toString(),
    val section: IdeLogicSection,
    val name: String,
    val subtitle: String,
    val targetViewId: String = "",
    val returnType: String = "void",
    val parameters: List<IdeFunctionParam> = emptyList(),
    val privateVarName: String = "",
    val privateVarType: String = "String",
    val privateVarValue: String = "",
    val bodyCode: String = "",
)

data class IdeWidgetNode(
    val uid: String = UUID.randomUUID().toString(),
    val viewId: String,
    val type: IdeWidgetType,
    val text: String = when {
        type == IdeWidgetType.TEXT_VIEW -> "TextView"
        type == IdeWidgetType.CHECKED_TEXT_VIEW -> "CheckedTextView"
        type.isButtonWidget && type != IdeWidgetType.FAB_BUTTON -> type.displayName
        type.isChooseWidget && type != IdeWidgetType.SPINNER -> type.displayName
        else -> ""
    },
    val hint: String = when {
        type == IdeWidgetType.EDIT_TEXT -> "EditText"
        type == IdeWidgetType.AUTO_COMPLETE_TEXT -> "AutoCompleteTextView"
        type == IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT -> "MultiAutoCompleteTextView"
        type == IdeWidgetType.TEXT_INPUT_EDIT_TEXT -> "TextInputEditText"
        else -> "Hint"
    },
    val hintColor: String = "#94A3B8",
    val inputType: String = "text",
    val imeOptions: String = "actionUnspecified",
    val singleLine: Boolean = false,
    val checked: Boolean = true,
    val completionThreshold: Int = 1,
    val buttonTint: String = "#0284C7",
    val trackTint: String = "#93C5FD",
    val textOn: String = "ON",
    val textOff: String = "OFF",
    val spinnerMode: String = "dropdown",
    val spinnerEntries: String = "Item 1, Item 2, Item 3",
    val spinnerPrompt: String = "Select option",
    val webUrl: String = "https://google.com",
    val webJavaScript: Boolean = true,
    val webDomStorage: Boolean = true,
    val webSupportZoom: Boolean = true,
    val webDesktopMode: Boolean = false,
    val webAllowFileAccess: Boolean = true,
    val webCacheMode: String = "LOAD_DEFAULT",
    val webUserAgent: String = "default",
    val elevationDp: Int = if (type.isButtonWidget) 2 else 0,
    val rippleColor: String = "#38BDF8",
    val allCaps: Boolean = false,
    val enabled: Boolean = true,
    val orientation: String = when (type) {
        IdeWidgetType.LINEAR_H,
        IdeWidgetType.SCROLL_V,
        IdeWidgetType.VIEW_PAGER,
        IdeWidgetType.HORIZONTAL_LIST_VIEW,
        IdeWidgetType.CAROUSEL_VIEW -> "horizontal"
        else -> "vertical"
    },
    val customView: String = "none",
    val dividerHeightDp: Int = 1,
    val numColumns: Int = if (type == IdeWidgetType.GRID_VIEW || type == IdeWidgetType.STAGGERED_GRID_VIEW) 2 else 1,
    val horizontalSpacingDp: Int = 4,
    val verticalSpacingDp: Int = 4,
    val choiceMode: String = "none",
    val layoutManager: String = when (type) {
        IdeWidgetType.GRID_VIEW -> "Grid"
        IdeWidgetType.STAGGERED_GRID_VIEW -> "StaggeredGrid"
        IdeWidgetType.HORIZONTAL_LIST_VIEW, IdeWidgetType.CAROUSEL_VIEW -> "LinearHorizontal"
        else -> "LinearVertical"
    },
    val stackFromBottom: Boolean = false,
    val fastScroll: Boolean = true,
    val pageLimit: Int = 2,
    val format12Hour: String = "hh:mm:ss a",
    val format24Hour: String = "HH:mm:ss",
    val is24HourView: Boolean = false,
    val pickerMode: String = if (type == IdeWidgetType.DATE_PICKER || type == IdeWidgetType.DATE_RANGE_PICKER) "calendar" else "clock",
    val firstDayOfWeek: Int = 1,
    val minDate: String = "01/01/2000",
    val maxDate: String = "12/31/2035",
    val showWeekNumber: Boolean = false,
    val timeZone: String = "Default",
    val countDown: Boolean = type == IdeWidgetType.COUNTDOWN_VIEW,
    val layoutWidth: String = if (
        type.isContainer ||
        type.isInputTextWidget ||
        type.isWebWidget ||
        type.isListWidget ||
        type == IdeWidgetType.SPINNER ||
        type == IdeWidgetType.CALENDAR_VIEW ||
        type == IdeWidgetType.DATE_PICKER ||
        type == IdeWidgetType.TIME_PICKER ||
        type == IdeWidgetType.DATE_RANGE_PICKER
    ) "match_parent" else "wrap_content",
    val layoutHeight: String = when {
        type.isWebWidget -> "180dp"
        type.isListWidget -> "140dp"
        else -> "wrap_content"
    },
    val paddingDp: Int = if (type.isContainer) 8 else 4,
    val marginDp: Int = 0,
    val gravity: String = if (type.isButtonWidget) "center" else "left|top",
    val layoutGravity: String = "left|top",
    val weight: Int = 0,
    val textSizeSp: Int = 12,
    val textStyle: String = if (type.isButtonWidget) "bold" else "normal",
    val textColor: String = when {
        type == IdeWidgetType.BUTTON ||
            type == IdeWidgetType.MATERIAL_BUTTON ||
            type == IdeWidgetType.FAB_BUTTON ||
            type == IdeWidgetType.EXTENDED_FAB -> "#FFFFFF"
        type == IdeWidgetType.OUTLINED_BUTTON ||
            type == IdeWidgetType.TEXT_BUTTON -> "#2563EB"
        else -> "#000000"
    },
    val lines: Int = 0,
    val imageResName: String = "default_image",
    val scaleType: String = if (type == IdeWidgetType.CIRCLE_IMAGE_VIEW) "CENTER_CROP" else "FIT_CENTER",
    val imageTint: String = "none",
    val civBorderWidthDp: Int = when (type) {
        IdeWidgetType.CIRCLE_IMAGE_VIEW -> 2
        IdeWidgetType.OUTLINED_BUTTON, IdeWidgetType.CHIP_BUTTON -> 1
        else -> 0
    },
    val civBorderColor: String = if (type == IdeWidgetType.OUTLINED_BUTTON) "#2563EB" else "#38BDF8",
    val civCircleBgColor: String = "transparent",
    val cornerRadiusDp: Int = when (type) {
        IdeWidgetType.ROUNDED_IMAGE_VIEW, IdeWidgetType.SHAPEABLE_IMAGE_VIEW -> 14
        IdeWidgetType.IMAGE_BUTTON, IdeWidgetType.BUTTON -> 8
        IdeWidgetType.MATERIAL_BUTTON, IdeWidgetType.OUTLINED_BUTTON, IdeWidgetType.TOGGLE_BUTTON -> 12
        IdeWidgetType.EXTENDED_FAB, IdeWidgetType.CHIP_BUTTON -> 20
        IdeWidgetType.FAB_BUTTON -> 28
        else -> 0
    },
    val backgroundResource: String = "none",
    val backgroundColor: String = when (type) {
        IdeWidgetType.BUTTON,
        IdeWidgetType.MATERIAL_BUTTON,
        IdeWidgetType.FAB_BUTTON,
        IdeWidgetType.EXTENDED_FAB -> "#2563EB"
        IdeWidgetType.CHIP_BUTTON -> "#E0F2FE"
        IdeWidgetType.TOGGLE_BUTTON -> "#E2E8F0"
        else -> "transparent"
    },
    val rotation: Float = 0f,
    val alpha: Float = 1f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val customAttributes: String = "",
    val children: List<IdeWidgetNode> = emptyList(),
)

/**
 * Persistent workspace model backed by SharedPreferences + full two-way disk synchronization
 * under `/storage/emulated/0/.IDE Ultimate` (restores existing projects and folders from disk).
 */
class WorkspaceRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("ide_ultimate_workspace", Context.MODE_PRIVATE)

    val items: SnapshotStateList<WorkspaceItem> = mutableStateListOf()

    init {
        val raw = prefs.getString(KEY_ITEMS, null)
        if (raw != null) {
            items.addAll(runCatching { parseItems(raw) }.getOrDefault(emptyList()))
        }
        restoreFromFileSystem()
        syncToFileSystem()
    }

    fun itemById(id: String): WorkspaceItem? = items.firstOrNull { it.id == id }

    fun getBuildMethodForProject(projectId: String): String =
        prefs.getString("build_method_$projectId", "SIMPLE") ?: "SIMPLE"

    fun setBuildMethodForProject(projectId: String, methodName: String) {
        prefs.edit().putString("build_method_$projectId", methodName).apply()
    }

    /**
     * Returns the full `/storage/emulated/0/.IDE Ultimate/...` path for the current breadcrumb trail.
     */
    fun displayDiskPath(pathIds: List<String>): String {
        val segments = pathIds.mapNotNull { id -> itemById(id)?.name }
        return if (segments.isEmpty()) {
            WORKSPACE_DISK_PATH
        } else {
            "$WORKSPACE_DISK_PATH/${segments.joinToString("/")}"
        }
    }

    /** Folders first, then projects, each group sorted alphabetically. */
    fun childrenOf(parentId: String?): List<WorkspaceItem> = items
        .filter { it.parentId == parentId }
        .sortedWith(
            compareBy<WorkspaceItem> { it.type != WorkspaceItemType.FOLDER }
                .thenBy { it.name.lowercase() },
        )

    fun childCount(folderId: String): Int = items.count { it.parentId == folderId }

    fun folderCount(parentId: String?): Int =
        items.count { it.parentId == parentId && it.type == WorkspaceItemType.FOLDER }

    fun projectCount(parentId: String?): Int =
        items.count { it.parentId == parentId && it.type == WorkspaceItemType.PROJECT }

    fun totalFolderCount(): Int = items.count { it.type == WorkspaceItemType.FOLDER }

    fun totalProjectCount(): Int = items.count { it.type == WorkspaceItemType.PROJECT }

    fun allFolders(): List<WorkspaceItem> =
        items.filter { it.type == WorkspaceItemType.FOLDER }.sortedBy { it.name.lowercase() }

    fun isNameTaken(name: String, parentId: String?, excludeId: String? = null): Boolean {
        val candidate = name.trim()
        return items.any { item ->
            item.id != excludeId &&
                item.parentId == parentId &&
                item.name.equals(candidate, ignoreCase = true)
        }
    }

    fun createFolder(name: String, parentId: String?): WorkspaceItem {
        val item = WorkspaceItem(
            name = name.trim(),
            type = WorkspaceItemType.FOLDER,
            parentId = parentId,
        )
        items.add(item)
        persist()
        runCatching {
            ensureDefaultProjectFiles(item)
            ensureProjectFullRoot(item)
        }
        return item
    }

    fun createProject(
        name: String,
        parentId: String?,
        packageName: String = ProjectZipExporter.packageName(name),
        language: ProjectLanguage = ProjectLanguage.KOTLIN,
        versionCode: Int = 1,
        versionName: String = "1.0.0",
        iconPreset: Int = 0,
        customIconPath: String? = null,
    ): WorkspaceItem {
        val trimmedName = name.trim()
        val cleanPackage = packageName.trim().ifBlank {
            ProjectZipExporter.packageName(trimmedName)
        }
        val item = WorkspaceItem(
            name = trimmedName,
            type = WorkspaceItemType.PROJECT,
            parentId = parentId,
            packageName = cleanPackage,
            language = language,
            versionCode = versionCode.coerceAtLeast(1),
            versionName = versionName.trim().ifBlank { "1.0.0" },
            iconPreset = iconPreset,
            customIconPath = customIconPath,
        )
        items.add(item)
        persist()
        return item
    }

    /**
     * Updates an existing project's editable metadata.
     * Note: [WorkspaceItem.language] is intentionally immutable after creation.
     */
    fun updateProject(
        id: String,
        newName: String,
        newPackageName: String,
        newVersionCode: Int,
        newVersionName: String,
        newIconPreset: Int,
        newCustomIconPath: String?,
    ) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val current = items[index]
        val trimmedName = newName.trim()
        val oldRelSegments = relativeSegmentsOf(current)
        val updated = current.copy(
            name = trimmedName,
            packageName = newPackageName.trim().ifBlank {
                ProjectZipExporter.packageName(trimmedName)
            },
            language = current.language,
            versionCode = newVersionCode.coerceAtLeast(1),
            versionName = newVersionName.trim().ifBlank { "1.0.0" },
            iconPreset = newIconPreset,
            customIconPath = newCustomIconPath,
            updatedAt = System.currentTimeMillis(),
        )
        items[index] = updated
        val newRelSegments = relativeSegmentsOf(updated)
        if (oldRelSegments != newRelSegments) {
            renameOnDisk(oldRelSegments, newRelSegments)
        }
        persist()
    }

    fun rename(id: String, newName: String) {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return
        val current = items[index]
        val oldRelSegments = relativeSegmentsOf(current)
        val updated = current.copy(
            name = newName.trim(),
            updatedAt = System.currentTimeMillis(),
        )
        items[index] = updated
        val newRelSegments = relativeSegmentsOf(updated)
        if (oldRelSegments != newRelSegments) {
            renameOnDisk(oldRelSegments, newRelSegments)
        }
        persist()
    }

    fun descendantCount(id: String): Int = (collectSubtreeIds(id).size - 1).coerceAtLeast(0)

    fun delete(id: String) {
        val target = itemById(id)
        if (target != null) {
            val relSegments = relativeSegmentsOf(target)
            deleteFromDisk(relSegments)
        }
        val ids = collectSubtreeIds(id)
        if (items.removeAll { it.id in ids }) persist()
    }

    private fun collectSubtreeIds(id: String): Set<String> {
        val ids = mutableSetOf(id)
        var changed = true
        while (changed) {
            changed = false
            items.forEach { item ->
                val parent = item.parentId
                if (parent != null && parent in ids && ids.add(item.id)) changed = true
            }
        }
        return ids
    }

    private fun relativeSegmentsOf(item: WorkspaceItem): List<String> {
        val segments = mutableListOf(item.name)
        var parentId = item.parentId
        var guard = 0
        while (parentId != null && guard < 32) {
            val parent = itemById(parentId) ?: break
            segments.add(0, parent.name)
            parentId = parent.parentId
            guard += 1
        }
        return segments
    }

    private fun workspaceRoots(): List<File> = listOfNotNull(
        File(WORKSPACE_DISK_PATH),
        appContext.getExternalFilesDir(null)?.let { File(it, ".IDE Ultimate") },
    )

    private fun renameOnDisk(oldSegments: List<String>, newSegments: List<String>) {
        val oldRel = oldSegments.joinToString(File.separator)
        val newRel = newSegments.joinToString(File.separator)
        workspaceRoots().forEach { root ->
            runCatching {
                val oldDir = File(root, oldRel)
                val newDir = File(root, newRel)
                if (oldDir.exists() && !newDir.exists()) {
                    oldDir.renameTo(newDir)
                }
            }
        }
    }

    private fun deleteFromDisk(segments: List<String>) {
        val rel = segments.joinToString(File.separator)
        workspaceRoots().forEach { root ->
            runCatching {
                val targetDir = File(root, rel)
                if (targetDir.exists()) {
                    targetDir.deleteRecursively()
                }
            }
        }
    }

    /**
     * Scans `/storage/emulated/0/.IDE Ultimate/` (and fallback root) and restores any
     * folders and projects found on disk that are not yet in [items].
     * Returns the number of newly restored projects/folders.
     */
    fun restoreFromFileSystem(): Int {
        var addedCount = 0
        workspaceRoots().forEach { rootDir ->
            runCatching {
                if (rootDir.exists() && rootDir.isDirectory && rootDir.canRead()) {
                    addedCount += scanDiskDirectory(rootDir, parentId = null)
                }
            }
        }
        if (addedCount > 0) {
            savePrefsOnly()
        }
        return addedCount
    }

    private fun scanDiskDirectory(dir: File, parentId: String?): Int {
        var added = 0
        val childDirs = dir.listFiles()
            ?.filter { it.isDirectory && !it.name.startsWith(".") && it.name !in INTERNAL_PROJECT_DIRS }
            ?.sortedBy { it.name.lowercase(Locale.ROOT) }
            ?: return 0

        for (childDir in childDirs) {
            val metaFile = File(childDir, "ide-project.json")
            val folderMarker = File(childDir, ".ide-folder.json")

            val isProject = when {
                metaFile.exists() -> true
                folderMarker.exists() -> false
                hasProjectStructure(childDir) -> true
                hasNestedWorkspaceItems(childDir) -> false
                else -> true
            }

            if (isProject) {
                val parsed = runCatching {
                    if (metaFile.exists()) {
                        parseProjectMetaFile(metaFile, childDir, parentId)
                    } else {
                        inferProjectFromDirectory(childDir, parentId)
                    }
                }.getOrNull()

                if (parsed != null) {
                    val existingIndex = items.indexOfFirst { existing ->
                        existing.id == parsed.id ||
                            (existing.parentId == parentId &&
                                existing.name.equals(parsed.name, ignoreCase = true))
                    }
                    if (existingIndex < 0) {
                        items.add(parsed)
                        added += 1
                    } else {
                        val current = items[existingIndex]
                        if (current.type != WorkspaceItemType.PROJECT) {
                            items[existingIndex] = parsed.copy(id = current.id)
                            added += 1
                        }
                    }
                }
            } else {
                var folderItem = items.firstOrNull { existing ->
                    existing.type == WorkspaceItemType.FOLDER &&
                        existing.parentId == parentId &&
                        existing.name.equals(childDir.name, ignoreCase = true)
                }
                if (folderItem == null) {
                    folderItem = WorkspaceItem(
                        name = childDir.name,
                        type = WorkspaceItemType.FOLDER,
                        parentId = parentId,
                    )
                    items.add(folderItem)
                    added += 1
                }
                added += scanDiskDirectory(childDir, parentId = folderItem.id)
            }
        }
        return added
    }

    private fun hasProjectStructure(dir: File): Boolean {
        val markers = listOf(
            "ide-project.json",
            "build.gradle.kts",
            "build.gradle",
            "settings.gradle.kts",
            "settings.gradle",
            "pubspec.yaml",
            "AndroidManifest.xml",
            "app",
            "src",
            "res",
            "lib",
        )
        if (markers.any { File(dir, it).exists() }) return true
        val files = dir.listFiles() ?: return false
        return files.any { file ->
            file.isFile && (
                file.name.endsWith(".kt", ignoreCase = true) ||
                    file.name.endsWith(".java", ignoreCase = true) ||
                    file.name.endsWith(".dart", ignoreCase = true) ||
                    file.name.endsWith(".xml", ignoreCase = true)
                )
        }
    }

    private fun hasNestedWorkspaceItems(dir: File): Boolean {
        val subDirs = dir.listFiles()?.filter {
            it.isDirectory && !it.name.startsWith(".") && it.name !in INTERNAL_PROJECT_DIRS
        } ?: return false
        return subDirs.isNotEmpty()
    }

    private fun parseProjectMetaFile(
        metaFile: File,
        projectDir: File,
        parentId: String?,
    ): WorkspaceItem {
        val json = JSONObject(metaFile.readText(Charsets.UTF_8))
        val name = json.optString("name", projectDir.name).trim().ifBlank { projectDir.name }
        val id = json.optString("id").trim().ifBlank {
            UUID.nameUUIDFromBytes("project:${parentId.orEmpty()}:$name".toByteArray()).toString()
        }
        val language = runCatching {
            ProjectLanguage.valueOf(json.optString("language", ProjectLanguage.KOTLIN.name))
        }.getOrDefault(ProjectLanguage.KOTLIN)
        val pkg = json.optString("packageName").trim().ifBlank {
            ProjectZipExporter.packageName(name)
        }
        val customIcon = if (json.isNull("customIconPath")) {
            null
        } else {
            json.optString("customIconPath").trim().ifBlank { null }
        }
        return WorkspaceItem(
            id = id,
            name = name,
            type = WorkspaceItemType.PROJECT,
            parentId = parentId,
            packageName = pkg,
            language = language,
            versionCode = json.optInt("versionCode", 1).coerceAtLeast(1),
            versionName = json.optString("versionName", "1.0.0").ifBlank { "1.0.0" },
            iconPreset = json.optInt("iconPreset", 0),
            customIconPath = customIcon,
            createdAt = json.optLong("createdAt", projectDir.lastModified().takeIf { it > 0 } ?: System.currentTimeMillis()),
            updatedAt = json.optLong("updatedAt", projectDir.lastModified().takeIf { it > 0 } ?: System.currentTimeMillis()),
        )
    }

    private fun inferProjectFromDirectory(
        projectDir: File,
        parentId: String?,
    ): WorkspaceItem {
        val name = projectDir.name.trim().ifBlank { "Project" }
        val id = UUID.nameUUIDFromBytes("disk-project:${parentId.orEmpty()}:$name".toByteArray()).toString()
        val language = when {
            File(projectDir, "pubspec.yaml").exists() || File(projectDir, "lib/main.dart").exists() ->
                ProjectLanguage.FLUTTER
            projectDir.walkTopDown().maxDepth(6).any { it.isFile && it.name.endsWith(".java", true) } ->
                ProjectLanguage.JAVA
            else -> ProjectLanguage.KOTLIN
        }
        val pkg = ProjectZipExporter.packageName(name)
        val lastMod = projectDir.lastModified().takeIf { it > 0 } ?: System.currentTimeMillis()
        return WorkspaceItem(
            id = id,
            name = name,
            type = WorkspaceItemType.PROJECT,
            parentId = parentId,
            packageName = pkg,
            language = language,
            versionCode = 1,
            versionName = "1.0.0",
            iconPreset = 0,
            customIconPath = null,
            createdAt = lastMod,
            updatedAt = lastMod,
        )
    }

    // =========================================================================
    // Project XML Layout Pages & Activity Code Management
    // =========================================================================

    private fun projectDirectoriesFor(project: WorkspaceItem): List<File> {
        val relPath = relativeSegmentsOf(project).joinToString(File.separator)
        return workspaceRoots().map { root -> File(root, relPath) }
    }

    /**
     * Converts any XML layout filename (e.g. `activity_main.xml`, `activity_splash.xml`)
     * into its corresponding [ProjectXmlPage] (`MainActivity`, `SplashActivity`, etc.).
     */
    fun xmlPageForName(project: WorkspaceItem, rawXmlName: String): ProjectXmlPage {
        val cleanXml = normalizeXmlFileName(rawXmlName)
        val activityName = xmlNameToActivityName(cleanXml)
        val ext = when (project.language) {
            ProjectLanguage.KOTLIN -> "kt"
            ProjectLanguage.JAVA -> "java"
            ProjectLanguage.FLUTTER -> "dart"
        }
        return ProjectXmlPage(
            xmlFileName = cleanXml,
            activityName = activityName,
            codeFileName = "$activityName.$ext",
        )
    }

    /**
     * Returns all XML layout pages for [project], reading from
     * `/storage/emulated/0/.IDE Ultimate/<project>/app/src/main/res/layout/`
     * and initializing the default XML layout pages if none exist yet.
     */
    fun listProjectXmlPages(project: WorkspaceItem): List<ProjectXmlPage> {
        ensureDefaultProjectFiles(project)
        val foundXmlNames = linkedSetOf<String>()

        projectDirectoriesFor(project).forEach { projDir ->
            val layoutDirs = listOf(
                File(projDir, "app/src/main/res/layout"),
                File(projDir, "res/layout"),
                File(projDir, "layout"),
            )
            layoutDirs.forEach { layoutDir ->
                if (layoutDir.exists() && layoutDir.isDirectory) {
                    layoutDir.listFiles()
                        ?.filter { it.isFile && it.name.endsWith(".xml", ignoreCase = true) }
                        ?.sortedBy { it.name.lowercase(Locale.ROOT) }
                        ?.forEach { file ->
                            foundXmlNames.add(file.name.lowercase(Locale.ROOT))
                        }
                }
            }
        }

        val savedCustom = prefs.getStringSet("xml_pages_${project.id}", null).orEmpty()
        foundXmlNames.addAll(savedCustom.map { normalizeXmlFileName(it) })

        val deletedSet = prefs.getStringSet("deleted_xml_pages_${project.id}", emptySet()).orEmpty()
        foundXmlNames.removeAll(deletedSet.map { normalizeXmlFileName(it) }.toSet())

        if (foundXmlNames.isEmpty()) {
            DEFAULT_XML_LAYOUTS.filterNot { it in deletedSet }.forEach { foundXmlNames.add(it) }
            if (foundXmlNames.isEmpty()) {
                foundXmlNames.add("activity_main.xml")
            }
        }

        // Keep activity_main.xml first, followed by the rest sorted naturally
        val ordered = foundXmlNames.sortedWith(
            compareBy<String> { it != "activity_main.xml" }
                .thenBy { DEFAULT_XML_LAYOUTS.indexOf(it).let { idx -> if (idx < 0) 999 else idx } }
                .thenBy { it },
        )
        return ordered.map { xmlName -> xmlPageForName(project, xmlName) }
    }

    /**
     * Creates a new XML layout page (e.g. `activity_profile.xml`) and its paired
     * Activity class (`ProfileActivity`) on disk and returns the created [ProjectXmlPage].
     */
    fun addProjectXmlPage(project: WorkspaceItem, rawName: String): ProjectXmlPage {
        val page = xmlPageForName(project, rawName)
        val currentSet = prefs.getStringSet("xml_pages_${project.id}", emptySet()).orEmpty().toMutableSet()
        currentSet.add(page.xmlFileName)
        prefs.edit().putStringSet("xml_pages_${project.id}", currentSet).apply()

        val defaultXml = defaultXmlLayoutContent(project, page)
        val defaultCode = defaultActivityCodeContent(project, page)

        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val layoutFile = File(projDir, "app/src/main/res/layout/${page.xmlFileName}")
                layoutFile.parentFile?.mkdirs()
                if (!layoutFile.exists()) {
                    layoutFile.writeText(defaultXml, Charsets.UTF_8)
                }
                val pkgPath = project.packageName.replace('.', '/')
                if (project.language == ProjectLanguage.JAVA) {
                    val javaFile = File(projDir, "app/src/main/java/$pkgPath/${page.activityName}.java")
                    javaFile.parentFile?.mkdirs()
                    if (!javaFile.exists()) {
                        javaFile.writeText(defaultJavaActivityContent(project, page), Charsets.UTF_8)
                    }
                } else {
                    val ktFile = File(projDir, "app/src/main/java/$pkgPath/${page.activityName}.kt")
                    ktFile.parentFile?.mkdirs()
                    if (!ktFile.exists()) {
                        ktFile.writeText(defaultKotlinActivityContent(project, page), Charsets.UTF_8)
                    }
                }
            }
        }
        syncActivityIntoManifest(project, page.activityName)
        return page
    }

    /**
     * Renames an existing XML page (e.g. `activity_home.xml` -> `activity_profile.xml`) and its
     * paired Java/Kotlin Activity class (`HomeActivity` -> `ProfileActivity`) across disk, preferences, and AndroidManifest.xml.
     */
    fun renameProjectXmlPage(
        project: WorkspaceItem,
        oldXmlFileName: String,
        rawNewName: String,
    ): ProjectXmlPage? {
        val oldPage = xmlPageForName(project, oldXmlFileName)
        val newPage = xmlPageForName(project, rawNewName)
        if (newPage.xmlFileName.equals(oldPage.xmlFileName, ignoreCase = true)) return oldPage

        val existingPages = listProjectXmlPages(project)
        if (existingPages.any { it.xmlFileName.equals(newPage.xmlFileName, ignoreCase = true) }) {
            return null
        }

        val currentSet = existingPages.map { it.xmlFileName }.toMutableSet()
        currentSet.remove(oldPage.xmlFileName)
        currentSet.add(newPage.xmlFileName)
        val deletedSet = prefs.getStringSet("deleted_xml_pages_${project.id}", emptySet()).orEmpty().toMutableSet()
        deletedSet.add(oldPage.xmlFileName)
        deletedSet.remove(newPage.xmlFileName)
        prefs.edit()
            .putStringSet("xml_pages_${project.id}", currentSet)
            .putStringSet("deleted_xml_pages_${project.id}", deletedSet)
            .apply()

        // Migrate saved widgets & logic items
        val oldWidgets = prefs.getString("widgets_${project.id}_${oldPage.xmlFileName}", null)
        if (oldWidgets != null) {
            prefs.edit()
                .putString("widgets_${project.id}_${newPage.xmlFileName}", oldWidgets)
                .remove("widgets_${project.id}_${oldPage.xmlFileName}")
                .apply()
        }
        val oldLogic = prefs.getString("logic_items_${project.id}_${oldPage.xmlFileName}", null)
        if (oldLogic != null) {
            prefs.edit()
                .putString("logic_items_${project.id}_${newPage.xmlFileName}", oldLogic)
                .remove("logic_items_${project.id}_${oldPage.xmlFileName}")
                .apply()
        }

        val pkgPath = project.packageName.replace('.', '/')
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val oldXmlFile = File(projDir, "app/src/main/res/layout/${oldPage.xmlFileName}")
                val newXmlFile = File(projDir, "app/src/main/res/layout/${newPage.xmlFileName}")
                newXmlFile.parentFile?.mkdirs()
                if (oldXmlFile.exists()) {
                    val xmlText = oldXmlFile.readText(Charsets.UTF_8)
                        .replace(".${oldPage.activityName}", ".${newPage.activityName}")
                        .replace(oldPage.layoutResourceName, newPage.layoutResourceName)
                    newXmlFile.writeText(xmlText, Charsets.UTF_8)
                    oldXmlFile.delete()
                } else {
                    newXmlFile.writeText(defaultXmlLayoutContent(project, newPage), Charsets.UTF_8)
                }

                if (project.language == ProjectLanguage.JAVA) {
                    val oldJavaFile = File(projDir, "app/src/main/java/$pkgPath/${oldPage.activityName}.java")
                    val newJavaFile = File(projDir, "app/src/main/java/$pkgPath/${newPage.activityName}.java")
                    newJavaFile.parentFile?.mkdirs()
                    if (oldJavaFile.exists()) {
                        val javaText = oldJavaFile.readText(Charsets.UTF_8)
                            .replace("class ${oldPage.activityName}", "class ${newPage.activityName}")
                            .replace("R.layout.${oldPage.layoutResourceName}", "R.layout.${newPage.layoutResourceName}")
                        newJavaFile.writeText(javaText, Charsets.UTF_8)
                        oldJavaFile.delete()
                    } else {
                        newJavaFile.writeText(defaultJavaActivityContent(project, newPage), Charsets.UTF_8)
                    }
                } else {
                    val oldKtFile = File(projDir, "app/src/main/java/$pkgPath/${oldPage.activityName}.kt")
                    val newKtFile = File(projDir, "app/src/main/java/$pkgPath/${newPage.activityName}.kt")
                    newKtFile.parentFile?.mkdirs()
                    if (oldKtFile.exists()) {
                        val ktText = oldKtFile.readText(Charsets.UTF_8)
                            .replace("class ${oldPage.activityName}", "class ${newPage.activityName}")
                            .replace("R.layout.${oldPage.layoutResourceName}", "R.layout.${newPage.layoutResourceName}")
                        newKtFile.writeText(ktText, Charsets.UTF_8)
                        oldKtFile.delete()
                    } else {
                        newKtFile.writeText(defaultKotlinActivityContent(project, newPage), Charsets.UTF_8)
                    }
                }
            }
        }

        // Update AndroidManifest.xml
        val manifest = readProjectManifest(project)
        if (manifest.contains(".${oldPage.activityName}")) {
            writeProjectManifest(
                project,
                manifest.replace(".${oldPage.activityName}", ".${newPage.activityName}"),
            )
        } else {
            syncActivityIntoManifest(project, newPage.activityName)
        }

        return newPage
    }

    /**
     * Deletes an XML page and its paired Java/Kotlin Activity class from the project.
     */
    fun deleteProjectXmlPage(
        project: WorkspaceItem,
        xmlFileName: String,
    ): Boolean {
        val page = xmlPageForName(project, xmlFileName)
        val currentPages = listProjectXmlPages(project)
        if (currentPages.size <= 1) return false

        val currentSet = currentPages.map { it.xmlFileName }.toMutableSet()
        currentSet.remove(page.xmlFileName)
        val deletedSet = prefs.getStringSet("deleted_xml_pages_${project.id}", emptySet()).orEmpty().toMutableSet()
        deletedSet.add(page.xmlFileName)
        prefs.edit()
            .putStringSet("xml_pages_${project.id}", currentSet)
            .putStringSet("deleted_xml_pages_${project.id}", deletedSet)
            .remove("widgets_${project.id}_${page.xmlFileName}")
            .remove("logic_items_${project.id}_${page.xmlFileName}")
            .apply()

        val pkgPath = project.packageName.replace('.', '/')
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                File(projDir, "app/src/main/res/layout/${page.xmlFileName}").delete()
                File(projDir, "app/src/main/java/$pkgPath/${page.activityName}.kt").delete()
                File(projDir, "app/src/main/java/$pkgPath/${page.activityName}.java").delete()
                File(projDir, ".ide_widgets_${page.layoutResourceName}.json").delete()
                File(projDir, ".ide_logic_${page.layoutResourceName}.json").delete()
            }
        }

        val manifest = readProjectManifest(project)
        val cleanedManifest = manifest.replace(
            Regex("""\s*<activity[^>]*android:name="\.${Regex.escape(page.activityName)}"[^>]*(?:/>|>[\s\S]*?</activity>)"""),
            "",
        )
        if (cleanedManifest != manifest) {
            writeProjectManifest(project, cleanedManifest)
        }
        return true
    }

    /**
     * Reads the code for [xmlPage] in the requested [flavor] (`Kotlin Android`, `Java Android`, or `xml Android`).
     */
    fun readCodeForFlavor(
        project: WorkspaceItem,
        xmlPage: ProjectXmlPage,
        flavor: CodeEditorFlavor,
    ): String {
        ensureDefaultProjectFiles(project)
        val pkgPath = project.packageName.replace('.', '/')
        for (projDir in projectDirectoriesFor(project)) {
            val targetFile = when (flavor) {
                CodeEditorFlavor.KOTLIN_ANDROID ->
                    File(projDir, "app/src/main/java/$pkgPath/${xmlPage.activityName}.kt")
                CodeEditorFlavor.JAVA_ANDROID ->
                    File(projDir, "app/src/main/java/$pkgPath/${xmlPage.activityName}.java")
                CodeEditorFlavor.XML_ANDROID ->
                    File(projDir, "app/src/main/res/layout/${xmlPage.xmlFileName}")
            }
            if (targetFile.exists() && targetFile.isFile && targetFile.canRead()) {
                val text = runCatching { targetFile.readText(Charsets.UTF_8) }.getOrNull()
                if (!text.isNullOrBlank()) return text
            }
        }
        val prefKey = "code_${project.id}_${xmlPage.xmlFileName}_${flavor.name}"
        val saved = prefs.getString(prefKey, null)
        if (!saved.isNullOrBlank()) return saved
        return when (flavor) {
            CodeEditorFlavor.KOTLIN_ANDROID -> defaultKotlinActivityContent(project, xmlPage)
            CodeEditorFlavor.JAVA_ANDROID -> defaultJavaActivityContent(project, xmlPage)
            CodeEditorFlavor.XML_ANDROID -> {
                val widgets = loadWidgetsForXmlPage(project, xmlPage)
                if (widgets.isNotEmpty()) {
                    generateXmlFromWidgets(project, xmlPage, widgets)
                } else {
                    defaultXmlLayoutContent(project, xmlPage)
                }
            }
        }
    }

    /**
     * Saves the edited code for [xmlPage] in the requested [flavor] (`Kotlin Android`, `Java Android`, or `xml Android`).
     */
    fun saveCodeForFlavor(
        project: WorkspaceItem,
        xmlPage: ProjectXmlPage,
        flavor: CodeEditorFlavor,
        newCode: String,
    ) {
        val prefKey = "code_${project.id}_${xmlPage.xmlFileName}_${flavor.name}"
        prefs.edit().putString(prefKey, newCode).apply()
        val pkgPath = project.packageName.replace('.', '/')
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val targetFile = when (flavor) {
                    CodeEditorFlavor.KOTLIN_ANDROID ->
                        File(projDir, "app/src/main/java/$pkgPath/${xmlPage.activityName}.kt")
                    CodeEditorFlavor.JAVA_ANDROID ->
                        File(projDir, "app/src/main/java/$pkgPath/${xmlPage.activityName}.java")
                    CodeEditorFlavor.XML_ANDROID ->
                        File(projDir, "app/src/main/res/layout/${xmlPage.xmlFileName}")
                }
                targetFile.parentFile?.mkdirs()
                targetFile.writeText(newCode, Charsets.UTF_8)
            }
        }
    }

    /**
     * Loads the visual widget tree placed on the white phone canvas for [xmlPage].
     */
    fun loadWidgetsForXmlPage(
        project: WorkspaceItem,
        xmlPage: ProjectXmlPage,
    ): List<IdeWidgetNode> {
        val key = "widgets_${project.id}_${xmlPage.xmlFileName}"
        val raw = prefs.getString(key, null)
        if (!raw.isNullOrBlank()) {
            return runCatching { parseWidgetNodes(JSONArray(raw)) }.getOrDefault(emptyList())
        }
        for (projDir in projectDirectoriesFor(project)) {
            val file = File(projDir, ".ide-widgets-${xmlPage.layoutResourceName}.json")
            if (file.exists() && file.canRead()) {
                return runCatching {
                    parseWidgetNodes(JSONArray(file.readText(Charsets.UTF_8)))
                }.getOrDefault(emptyList())
            }
        }
        return emptyList()
    }

    /**
     * Saves the visual widget tree placed on the white phone canvas for [xmlPage] and
     * automatically updates the corresponding Android XML file (`res/layout/<xmlFileName>`).
     */
    fun saveWidgetsForXmlPage(
        project: WorkspaceItem,
        xmlPage: ProjectXmlPage,
        nodes: List<IdeWidgetNode>,
    ) {
        val serialized = serializeWidgetNodes(nodes).toString(2)
        val key = "widgets_${project.id}_${xmlPage.xmlFileName}"
        prefs.edit().putString(key, serialized).apply()

        val generatedXml = if (nodes.isEmpty()) {
            defaultXmlLayoutContent(project, xmlPage)
        } else {
            generateXmlFromWidgets(project, xmlPage, nodes)
        }
        saveCodeForFlavor(project, xmlPage, CodeEditorFlavor.XML_ANDROID, generatedXml)

        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val file = File(projDir, ".ide-widgets-${xmlPage.layoutResourceName}.json")
                file.parentFile?.mkdirs()
                file.writeText(serialized, Charsets.UTF_8)
            }
        }
    }

    private fun serializeWidgetNodes(nodes: List<IdeWidgetNode>): JSONArray {
        val arr = JSONArray()
        nodes.forEach { node ->
            val obj = JSONObject().apply {
                put("uid", node.uid)
                put("viewId", node.viewId)
                put("type", node.type.name)
                put("text", node.text)
                put("hint", node.hint)
                put("hintColor", node.hintColor)
                put("inputType", node.inputType)
                put("imeOptions", node.imeOptions)
                put("singleLine", node.singleLine)
                put("checked", node.checked)
                put("completionThreshold", node.completionThreshold)
                put("buttonTint", node.buttonTint)
                put("trackTint", node.trackTint)
                put("textOn", node.textOn)
                put("textOff", node.textOff)
                put("spinnerMode", node.spinnerMode)
                put("spinnerEntries", node.spinnerEntries)
                put("spinnerPrompt", node.spinnerPrompt)
                put("webUrl", node.webUrl)
                put("webJavaScript", node.webJavaScript)
                put("webDomStorage", node.webDomStorage)
                put("webSupportZoom", node.webSupportZoom)
                put("webDesktopMode", node.webDesktopMode)
                put("webAllowFileAccess", node.webAllowFileAccess)
                put("webCacheMode", node.webCacheMode)
                put("webUserAgent", node.webUserAgent)
                put("elevationDp", node.elevationDp)
                put("rippleColor", node.rippleColor)
                put("allCaps", node.allCaps)
                put("enabled", node.enabled)
                put("orientation", node.orientation)
                put("customView", node.customView)
                put("dividerHeightDp", node.dividerHeightDp)
                put("numColumns", node.numColumns)
                put("horizontalSpacingDp", node.horizontalSpacingDp)
                put("verticalSpacingDp", node.verticalSpacingDp)
                put("choiceMode", node.choiceMode)
                put("layoutManager", node.layoutManager)
                put("stackFromBottom", node.stackFromBottom)
                put("fastScroll", node.fastScroll)
                put("pageLimit", node.pageLimit)
                put("format12Hour", node.format12Hour)
                put("format24Hour", node.format24Hour)
                put("is24HourView", node.is24HourView)
                put("pickerMode", node.pickerMode)
                put("firstDayOfWeek", node.firstDayOfWeek)
                put("minDate", node.minDate)
                put("maxDate", node.maxDate)
                put("showWeekNumber", node.showWeekNumber)
                put("timeZone", node.timeZone)
                put("countDown", node.countDown)
                put("layoutWidth", node.layoutWidth)
                put("layoutHeight", node.layoutHeight)
                put("paddingDp", node.paddingDp)
                put("marginDp", node.marginDp)
                put("gravity", node.gravity)
                put("layoutGravity", node.layoutGravity)
                put("weight", node.weight)
                put("textSizeSp", node.textSizeSp)
                put("textStyle", node.textStyle)
                put("textColor", node.textColor)
                put("lines", node.lines)
                put("imageResName", node.imageResName)
                put("scaleType", node.scaleType)
                put("imageTint", node.imageTint)
                put("civBorderWidthDp", node.civBorderWidthDp)
                put("civBorderColor", node.civBorderColor)
                put("civCircleBgColor", node.civCircleBgColor)
                put("cornerRadiusDp", node.cornerRadiusDp)
                put("backgroundResource", node.backgroundResource)
                put("backgroundColor", node.backgroundColor)
                put("rotation", node.rotation.toDouble())
                put("alpha", node.alpha.toDouble())
                put("translationX", node.translationX.toDouble())
                put("translationY", node.translationY.toDouble())
                put("scaleX", node.scaleX.toDouble())
                put("scaleY", node.scaleY.toDouble())
                put("customAttributes", node.customAttributes)
                put("children", serializeWidgetNodes(node.children))
            }
            arr.put(obj)
        }
        return arr
    }

    private fun parseWidgetNodes(arr: JSONArray): List<IdeWidgetNode> {
        val list = ArrayList<IdeWidgetNode>(arr.length())
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val type = runCatching { IdeWidgetType.valueOf(obj.getString("type")) }
                .getOrDefault(IdeWidgetType.TEXT_VIEW)
            val childrenArr = obj.optJSONArray("children") ?: JSONArray()
            val defaultW = if (
                type.isContainer ||
                type.isInputTextWidget ||
                type.isWebWidget ||
                type.isListWidget ||
                type == IdeWidgetType.SPINNER ||
                type == IdeWidgetType.CALENDAR_VIEW ||
                type == IdeWidgetType.DATE_PICKER ||
                type == IdeWidgetType.TIME_PICKER ||
                type == IdeWidgetType.DATE_RANGE_PICKER
            ) "match_parent" else "wrap_content"
            val defaultH = when {
                type.isWebWidget -> "180dp"
                type.isListWidget -> "140dp"
                else -> "wrap_content"
            }
            val defaultPad = if (type.isContainer) 8 else 4
            val defaultOrientation = when (type) {
                IdeWidgetType.LINEAR_H,
                IdeWidgetType.SCROLL_V,
                IdeWidgetType.VIEW_PAGER,
                IdeWidgetType.HORIZONTAL_LIST_VIEW,
                IdeWidgetType.CAROUSEL_VIEW -> "horizontal"
                else -> "vertical"
            }
            val defaultLayoutMgr = when (type) {
                IdeWidgetType.GRID_VIEW -> "Grid"
                IdeWidgetType.STAGGERED_GRID_VIEW -> "StaggeredGrid"
                IdeWidgetType.HORIZONTAL_LIST_VIEW, IdeWidgetType.CAROUSEL_VIEW -> "LinearHorizontal"
                else -> "LinearVertical"
            }
            val defaultText = when {
                type == IdeWidgetType.TEXT_VIEW -> "TextView"
                type == IdeWidgetType.CHECKED_TEXT_VIEW -> "CheckedTextView"
                type.isButtonWidget && type != IdeWidgetType.FAB_BUTTON -> type.displayName
                type.isChooseWidget && type != IdeWidgetType.SPINNER -> type.displayName
                type.isClockTextWidget -> "12:30:45"
                else -> ""
            }
            val defaultHint = when (type) {
                IdeWidgetType.EDIT_TEXT -> "EditText"
                IdeWidgetType.AUTO_COMPLETE_TEXT -> "AutoCompleteTextView"
                IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT -> "MultiAutoCompleteTextView"
                IdeWidgetType.TEXT_INPUT_EDIT_TEXT -> "TextInputEditText"
                else -> "Hint"
            }
            list.add(
                IdeWidgetNode(
                    uid = obj.optString("uid").ifBlank { UUID.randomUUID().toString() },
                    viewId = obj.optString("viewId").ifBlank { "${type.idPrefix}${i + 1}" },
                    type = type,
                    text = obj.optString("text", defaultText),
                    hint = obj.optString("hint", defaultHint).ifBlank { defaultHint },
                    hintColor = obj.optString("hintColor", "#94A3B8").ifBlank { "#94A3B8" },
                    inputType = obj.optString("inputType", "text").ifBlank { "text" },
                    imeOptions = obj.optString("imeOptions", "actionUnspecified").ifBlank { "actionUnspecified" },
                    singleLine = obj.optBoolean("singleLine", false),
                    checked = obj.optBoolean("checked", true),
                    completionThreshold = obj.optInt("completionThreshold", 1).coerceIn(1, 10),
                    buttonTint = obj.optString("buttonTint", "#0284C7").ifBlank { "#0284C7" },
                    trackTint = obj.optString("trackTint", "#93C5FD").ifBlank { "#93C5FD" },
                    textOn = obj.optString("textOn", "ON").ifBlank { "ON" },
                    textOff = obj.optString("textOff", "OFF").ifBlank { "OFF" },
                    spinnerMode = obj.optString("spinnerMode", "dropdown").ifBlank { "dropdown" },
                    spinnerEntries = obj.optString("spinnerEntries", "Item 1, Item 2, Item 3").ifBlank { "Item 1, Item 2, Item 3" },
                    spinnerPrompt = obj.optString("spinnerPrompt", "Select option").ifBlank { "Select option" },
                    webUrl = obj.optString("webUrl", "https://google.com").ifBlank { "https://google.com" },
                    webJavaScript = obj.optBoolean("webJavaScript", true),
                    webDomStorage = obj.optBoolean("webDomStorage", true),
                    webSupportZoom = obj.optBoolean("webSupportZoom", true),
                    webDesktopMode = obj.optBoolean("webDesktopMode", false),
                    webAllowFileAccess = obj.optBoolean("webAllowFileAccess", true),
                    webCacheMode = obj.optString("webCacheMode", "LOAD_DEFAULT").ifBlank { "LOAD_DEFAULT" },
                    webUserAgent = obj.optString("webUserAgent", "default").ifBlank { "default" },
                    elevationDp = obj.optInt("elevationDp", if (type.isButtonWidget) 2 else 0),
                    rippleColor = obj.optString("rippleColor", "#38BDF8").ifBlank { "#38BDF8" },
                    allCaps = obj.optBoolean("allCaps", false),
                    enabled = obj.optBoolean("enabled", true),
                    orientation = obj.optString("orientation", defaultOrientation).ifBlank { defaultOrientation },
                    customView = obj.optString("customView", "none").ifBlank { "none" },
                    dividerHeightDp = obj.optInt("dividerHeightDp", 1),
                    numColumns = obj.optInt("numColumns", if (type == IdeWidgetType.GRID_VIEW || type == IdeWidgetType.STAGGERED_GRID_VIEW) 2 else 1),
                    horizontalSpacingDp = obj.optInt("horizontalSpacingDp", 4),
                    verticalSpacingDp = obj.optInt("verticalSpacingDp", 4),
                    choiceMode = obj.optString("choiceMode", "none").ifBlank { "none" },
                    layoutManager = obj.optString("layoutManager", defaultLayoutMgr).ifBlank { defaultLayoutMgr },
                    stackFromBottom = obj.optBoolean("stackFromBottom", false),
                    fastScroll = obj.optBoolean("fastScroll", true),
                    pageLimit = obj.optInt("pageLimit", 2),
                    format12Hour = obj.optString("format12Hour", "hh:mm:ss a").ifBlank { "hh:mm:ss a" },
                    format24Hour = obj.optString("format24Hour", "HH:mm:ss").ifBlank { "HH:mm:ss" },
                    is24HourView = obj.optBoolean("is24HourView", false),
                    pickerMode = obj.optString("pickerMode", if (type == IdeWidgetType.DATE_PICKER || type == IdeWidgetType.DATE_RANGE_PICKER) "calendar" else "clock").ifBlank { "clock" },
                    firstDayOfWeek = obj.optInt("firstDayOfWeek", 1),
                    minDate = obj.optString("minDate", "01/01/2000").ifBlank { "01/01/2000" },
                    maxDate = obj.optString("maxDate", "12/31/2035").ifBlank { "12/31/2035" },
                    showWeekNumber = obj.optBoolean("showWeekNumber", false),
                    timeZone = obj.optString("timeZone", "Default").ifBlank { "Default" },
                    countDown = obj.optBoolean("countDown", type == IdeWidgetType.COUNTDOWN_VIEW),
                    layoutWidth = obj.optString("layoutWidth", defaultW).ifBlank { defaultW },
                    layoutHeight = obj.optString("layoutHeight", defaultH).ifBlank { defaultH },
                    paddingDp = obj.optInt("paddingDp", defaultPad),
                    marginDp = obj.optInt("marginDp", 0),
                    gravity = obj.optString("gravity", "left|top").ifBlank { "left|top" },
                    layoutGravity = obj.optString("layoutGravity", "left|top").ifBlank { "left|top" },
                    weight = obj.optInt("weight", 0),
                    textSizeSp = obj.optInt("textSizeSp", 12),
                    textStyle = obj.optString("textStyle", "normal").ifBlank { "normal" },
                    textColor = obj.optString("textColor", "#000000").ifBlank { "#000000" },
                    lines = obj.optInt("lines", 0),
                    imageResName = obj.optString("imageResName", "default_image").ifBlank { "default_image" },
                    scaleType = obj.optString(
                        "scaleType",
                        if (type == IdeWidgetType.CIRCLE_IMAGE_VIEW) "CENTER_CROP" else "FIT_CENTER",
                    ).ifBlank { "FIT_CENTER" },
                    imageTint = obj.optString("imageTint", "none").ifBlank { "none" },
                    civBorderWidthDp = obj.optInt(
                        "civBorderWidthDp",
                        if (type == IdeWidgetType.CIRCLE_IMAGE_VIEW) 2 else 0,
                    ),
                    civBorderColor = obj.optString("civBorderColor", "#38BDF8").ifBlank { "#38BDF8" },
                    civCircleBgColor = obj.optString("civCircleBgColor", "transparent").ifBlank { "transparent" },
                    cornerRadiusDp = obj.optInt("cornerRadiusDp", 0),
                    backgroundResource = obj.optString("backgroundResource", "none").ifBlank { "none" },
                    backgroundColor = obj.optString("backgroundColor", "transparent").ifBlank { "transparent" },
                    rotation = obj.optDouble("rotation", 0.0).toFloat(),
                    alpha = obj.optDouble("alpha", 1.0).toFloat(),
                    translationX = obj.optDouble("translationX", 0.0).toFloat(),
                    translationY = obj.optDouble("translationY", 0.0).toFloat(),
                    scaleX = obj.optDouble("scaleX", 1.0).toFloat(),
                    scaleY = obj.optDouble("scaleY", 1.0).toFloat(),
                    customAttributes = obj.optString("customAttributes", ""),
                    children = parseWidgetNodes(childrenArr),
                ),
            )
        }
        return list
    }

    fun generateXmlFromWidgets(
        project: WorkspaceItem,
        page: ProjectXmlPage,
        nodes: List<IdeWidgetNode>,
    ): String {
        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
        sb.appendLine("""<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"""")
        sb.appendLine("""    xmlns:app="http://schemas.android.com/apk/res-auto"""")
        sb.appendLine("""    xmlns:tools="http://schemas.android.com/tools"""")
        sb.appendLine("""    android:id="@+id/root_${page.layoutResourceName}"""")
        sb.appendLine("""    android:layout_width="match_parent"""")
        sb.appendLine("""    android:layout_height="match_parent"""")
        sb.appendLine("""    android:orientation="vertical"""")
        sb.appendLine("""    android:background="#FFFFFF"""")
        sb.appendLine("""    tools:context=".${page.activityName}">""")
        nodes.forEach { node ->
            sb.appendLine()
            appendWidgetXml(sb, node, indent = "    ")
        }
        sb.appendLine()
        sb.append("""</LinearLayout>""")
        return sb.toString()
    }

    private fun appendCommonXmlAttributes(sb: StringBuilder, node: IdeWidgetNode, indent: String) {
        sb.appendLine("""$indent    android:id="@+id/${node.viewId}"""")
        sb.appendLine("""$indent    android:layout_width="${node.layoutWidth}"""")
        sb.appendLine("""$indent    android:layout_height="${node.layoutHeight}"""")
        if (node.marginDp > 0) {
            sb.appendLine("""$indent    android:layout_margin="${node.marginDp}dp"""")
        }
        if (node.gravity.isNotBlank() && node.gravity != "left|top") {
            sb.appendLine("""$indent    android:gravity="${node.gravity}"""")
        }
        if (node.layoutGravity.isNotBlank() && node.layoutGravity != "left|top") {
            sb.appendLine("""$indent    android:layout_gravity="${node.layoutGravity}"""")
        }
        if (node.weight > 0) {
            sb.appendLine("""$indent    android:layout_weight="${node.weight}"""")
        }
        if (node.backgroundResource.isNotBlank() && node.backgroundResource != "none") {
            sb.appendLine("""$indent    android:background="${node.backgroundResource}"""")
        } else if (node.backgroundColor.isNotBlank() && node.backgroundColor != "transparent") {
            sb.appendLine("""$indent    android:background="${node.backgroundColor}"""")
        }
        if (node.rotation != 0f) {
            sb.appendLine("""$indent    android:rotation="${node.rotation}"""")
        }
        if (node.alpha != 1f) {
            sb.appendLine("""$indent    android:alpha="${node.alpha}"""")
        }
        if (node.translationX != 0f) {
            sb.appendLine("""$indent    android:translationX="${node.translationX}dp"""")
        }
        if (node.translationY != 0f) {
            sb.appendLine("""$indent    android:translationY="${node.translationY}dp"""")
        }
        if (node.scaleX != 1f) {
            sb.appendLine("""$indent    android:scaleX="${node.scaleX}"""")
        }
        if (node.scaleY != 1f) {
            sb.appendLine("""$indent    android:scaleY="${node.scaleY}"""")
        }
        if (node.customAttributes.isNotBlank()) {
            node.customAttributes.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach { line ->
                    sb.appendLine("""$indent    $line""")
                }
        }
    }

    private fun appendWidgetXml(sb: StringBuilder, node: IdeWidgetNode, indent: String) {
        when (node.type) {
            IdeWidgetType.LINEAR_H, IdeWidgetType.LINEAR_V -> {
                val orientation = node.orientation.ifBlank {
                    if (node.type == IdeWidgetType.LINEAR_H) "horizontal" else "vertical"
                }
                sb.appendLine("""$indent<LinearLayout""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    android:orientation="$orientation"""")
                if (node.children.isEmpty()) {
                    sb.appendLine("""$indent    android:padding="${node.paddingDp}dp" />""")
                } else {
                    sb.appendLine("""$indent    android:padding="${node.paddingDp}dp">""")
                    node.children.forEach { child ->
                        appendWidgetXml(sb, child, "$indent    ")
                    }
                    sb.appendLine("""$indent</LinearLayout>""")
                }
            }

            IdeWidgetType.SCROLL_V -> {
                sb.appendLine("""$indent<HorizontalScrollView""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    android:orientation="${node.orientation.ifBlank { "horizontal" }}"""")
                if (node.children.isEmpty()) {
                    sb.appendLine("""$indent    android:padding="${node.paddingDp}dp" />""")
                } else {
                    sb.appendLine("""$indent    android:padding="${node.paddingDp}dp">""")
                    node.children.forEach { child ->
                        appendWidgetXml(sb, child, "$indent    ")
                    }
                    sb.appendLine("""$indent</HorizontalScrollView>""")
                }
            }

            IdeWidgetType.SCROLL_H -> {
                sb.appendLine("""$indent<ScrollView""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    android:orientation="${node.orientation.ifBlank { "vertical" }}"""")
                if (node.children.isEmpty()) {
                    sb.appendLine("""$indent    android:padding="${node.paddingDp}dp" />""")
                } else {
                    sb.appendLine("""$indent    android:padding="${node.paddingDp}dp">""")
                    node.children.forEach { child ->
                        appendWidgetXml(sb, child, "$indent    ")
                    }
                    sb.appendLine("""$indent</ScrollView>""")
                }
            }

            IdeWidgetType.TEXT_VIEW,
            IdeWidgetType.EDIT_TEXT,
            IdeWidgetType.AUTO_COMPLETE_TEXT,
            IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT,
            IdeWidgetType.CHECKED_TEXT_VIEW,
            IdeWidgetType.TEXT_INPUT_EDIT_TEXT -> {
                val safeText = node.text
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                val safeHint = node.hint
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                val tagName = when (node.type) {
                    IdeWidgetType.TEXT_VIEW -> "TextView"
                    IdeWidgetType.EDIT_TEXT -> "EditText"
                    IdeWidgetType.AUTO_COMPLETE_TEXT -> "AutoCompleteTextView"
                    IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT -> "MultiAutoCompleteTextView"
                    IdeWidgetType.CHECKED_TEXT_VIEW -> "CheckedTextView"
                    IdeWidgetType.TEXT_INPUT_EDIT_TEXT -> "com.google.android.material.textfield.TextInputEditText"
                    else -> "TextView"
                }
                sb.appendLine("""$indent<$tagName""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    android:padding="${node.paddingDp}dp"""")
                if (safeText.isNotEmpty() || !node.type.isInputTextWidget) {
                    sb.appendLine("""$indent    android:text="$safeText"""")
                }
                if (node.type.isInputTextWidget) {
                    sb.appendLine("""$indent    android:hint="$safeHint"""")
                    sb.appendLine("""$indent    android:textColorHint="${node.hintColor}"""")
                    sb.appendLine("""$indent    android:inputType="${node.inputType}"""")
                    if (node.imeOptions != "actionUnspecified" && node.imeOptions.isNotBlank()) {
                        sb.appendLine("""$indent    android:imeOptions="${node.imeOptions}"""")
                    }
                    if (node.singleLine) {
                        sb.appendLine("""$indent    android:singleLine="true"""")
                    }
                }
                if (node.type == IdeWidgetType.AUTO_COMPLETE_TEXT || node.type == IdeWidgetType.MULTI_AUTO_COMPLETE_TEXT) {
                    sb.appendLine("""$indent    android:completionThreshold="${node.completionThreshold}"""")
                }
                if (node.type == IdeWidgetType.CHECKED_TEXT_VIEW) {
                    sb.appendLine("""$indent    android:checked="${node.checked}"""")
                    sb.appendLine("""$indent    android:checkMark="?android:attr/listChoiceIndicatorMultiple"""")
                }
                if (node.textStyle != "normal" && node.textStyle.isNotBlank()) {
                    sb.appendLine("""$indent    android:textStyle="${node.textStyle}"""")
                }
                if (node.lines > 0) {
                    sb.appendLine("""$indent    android:lines="${node.lines}"""")
                }
                sb.appendLine("""$indent    android:textColor="${node.textColor}"""")
                sb.appendLine("""$indent    android:textSize="${node.textSizeSp}sp" />""")
            }

            IdeWidgetType.IMAGE_VIEW,
            IdeWidgetType.CIRCLE_IMAGE_VIEW,
            IdeWidgetType.IMAGE_BUTTON,
            IdeWidgetType.ROUNDED_IMAGE_VIEW,
            IdeWidgetType.SHAPEABLE_IMAGE_VIEW,
            IdeWidgetType.ZOOM_IMAGE_VIEW,
            IdeWidgetType.ASPECT_IMAGE_VIEW,
            IdeWidgetType.IMAGE_FILTER_VIEW -> {
                val xmlScale = when (node.scaleType) {
                    "CENTER" -> "center"
                    "CENTER_CROP" -> "centerCrop"
                    "CENTER_INSIDE" -> "centerInside"
                    "FIT_START" -> "fitStart"
                    "FIT_END" -> "fitEnd"
                    "FIT_XY" -> "fitXY"
                    else -> "fitCenter"
                }
                val tagName = when (node.type) {
                    IdeWidgetType.IMAGE_VIEW -> "ImageView"
                    IdeWidgetType.CIRCLE_IMAGE_VIEW -> "de.hdodenhof.circleimageview.CircleImageView"
                    IdeWidgetType.IMAGE_BUTTON -> "ImageButton"
                    IdeWidgetType.ROUNDED_IMAGE_VIEW -> "com.makeramen.roundedimageview.RoundedImageView"
                    IdeWidgetType.SHAPEABLE_IMAGE_VIEW -> "com.google.android.material.imageview.ShapeableImageView"
                    IdeWidgetType.ZOOM_IMAGE_VIEW -> "com.ortiz.touchview.TouchImageView"
                    IdeWidgetType.ASPECT_IMAGE_VIEW -> "androidx.appcompat.widget.AppCompatImageView"
                    IdeWidgetType.IMAGE_FILTER_VIEW -> "androidx.constraintlayout.utils.widget.ImageFilterView"
                    else -> "ImageView"
                }
                sb.appendLine("""$indent<$tagName""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    android:padding="${node.paddingDp}dp"""")
                sb.appendLine("""$indent    android:src="@drawable/${node.imageResName}"""")
                if (node.imageTint.isNotBlank() && node.imageTint != "none") {
                    sb.appendLine("""$indent    android:tint="${node.imageTint}"""")
                }
                if (node.type == IdeWidgetType.CIRCLE_IMAGE_VIEW) {
                    sb.appendLine("""$indent    app:civ_border_width="${node.civBorderWidthDp}dp"""")
                    sb.appendLine("""$indent    app:civ_border_color="${node.civBorderColor}"""")
                    if (node.civCircleBgColor != "transparent" && node.civCircleBgColor.isNotBlank()) {
                        sb.appendLine("""$indent    app:civ_circle_background_color="${node.civCircleBgColor}"""")
                    }
                }
                if (node.cornerRadiusDp > 0 && (node.type == IdeWidgetType.ROUNDED_IMAGE_VIEW || node.type == IdeWidgetType.SHAPEABLE_IMAGE_VIEW || node.type == IdeWidgetType.IMAGE_FILTER_VIEW)) {
                    sb.appendLine("""$indent    app:riv_corner_radius="${node.cornerRadiusDp}dp"""")
                    sb.appendLine("""$indent    app:round="${node.cornerRadiusDp}dp"""")
                }
                sb.appendLine("""$indent    android:scaleType="$xmlScale" />""")
            }

            IdeWidgetType.BUTTON,
            IdeWidgetType.MATERIAL_BUTTON,
            IdeWidgetType.OUTLINED_BUTTON,
            IdeWidgetType.TEXT_BUTTON,
            IdeWidgetType.FAB_BUTTON,
            IdeWidgetType.EXTENDED_FAB,
            IdeWidgetType.TOGGLE_BUTTON,
            IdeWidgetType.CHIP_BUTTON -> {
                val safeText = node.text
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                val tagName = when (node.type) {
                    IdeWidgetType.BUTTON -> "Button"
                    IdeWidgetType.MATERIAL_BUTTON,
                    IdeWidgetType.OUTLINED_BUTTON,
                    IdeWidgetType.TEXT_BUTTON -> "com.google.android.material.button.MaterialButton"
                    IdeWidgetType.FAB_BUTTON -> "com.google.android.material.floatingactionbutton.FloatingActionButton"
                    IdeWidgetType.EXTENDED_FAB -> "com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton"
                    IdeWidgetType.TOGGLE_BUTTON -> "ToggleButton"
                    IdeWidgetType.CHIP_BUTTON -> "com.google.android.material.chip.Chip"
                    else -> "Button"
                }
                sb.appendLine("""$indent<$tagName""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    android:padding="${node.paddingDp}dp"""")
                if (!node.enabled) {
                    sb.appendLine("""$indent    android:enabled="false"""")
                }
                if (node.elevationDp > 0) {
                    sb.appendLine("""$indent    android:elevation="${node.elevationDp}dp"""")
                }
                if (node.cornerRadiusDp > 0 && node.type != IdeWidgetType.FAB_BUTTON) {
                    sb.appendLine("""$indent    app:cornerRadius="${node.cornerRadiusDp}dp"""")
                }
                if (node.civBorderWidthDp > 0) {
                    sb.appendLine("""$indent    app:strokeWidth="${node.civBorderWidthDp}dp"""")
                    sb.appendLine("""$indent    app:strokeColor="${node.civBorderColor}"""")
                }
                if (node.rippleColor.isNotBlank() && node.rippleColor != "none") {
                    sb.appendLine("""$indent    app:rippleColor="${node.rippleColor}"""")
                }
                if (node.type == IdeWidgetType.FAB_BUTTON) {
                    sb.appendLine("""$indent    android:src="@drawable/${node.imageResName}" />""")
                } else {
                    if (node.type == IdeWidgetType.TOGGLE_BUTTON) {
                        sb.appendLine("""$indent    android:checked="${node.checked}"""")
                        sb.appendLine("""$indent    android:textOn="${node.textOn}"""")
                        sb.appendLine("""$indent    android:textOff="${node.textOff}"""")
                    } else if (node.type == IdeWidgetType.CHIP_BUTTON) {
                        sb.appendLine("""$indent    android:checkable="true"""")
                        sb.appendLine("""$indent    android:checked="${node.checked}"""")
                    }
                    sb.appendLine("""$indent    android:text="$safeText"""")
                    sb.appendLine("""$indent    android:textAllCaps="${node.allCaps}"""")
                    sb.appendLine("""$indent    android:textColor="${node.textColor}"""")
                    sb.appendLine("""$indent    android:textSize="${node.textSizeSp}sp" />""")
                }
            }

            IdeWidgetType.CHECK_BOX,
            IdeWidgetType.RADIO_BUTTON,
            IdeWidgetType.SWITCH,
            IdeWidgetType.SPINNER -> {
                val safeText = node.text
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                val tagName = when (node.type) {
                    IdeWidgetType.CHECK_BOX -> "CheckBox"
                    IdeWidgetType.RADIO_BUTTON -> "RadioButton"
                    IdeWidgetType.SWITCH -> "Switch"
                    IdeWidgetType.SPINNER -> "Spinner"
                    else -> "CheckBox"
                }
                sb.appendLine("""$indent<$tagName""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    android:padding="${node.paddingDp}dp"""")
                if (!node.enabled) {
                    sb.appendLine("""$indent    android:enabled="false"""")
                }
                if (node.type == IdeWidgetType.SPINNER) {
                    sb.appendLine("""$indent    android:spinnerMode="${node.spinnerMode}"""")
                    if (node.spinnerPrompt.isNotBlank()) {
                        sb.appendLine("""$indent    android:prompt="${node.spinnerPrompt}"""")
                    }
                    sb.appendLine("""$indent    tools:entries="${node.spinnerEntries}" />""")
                } else {
                    sb.appendLine("""$indent    android:checked="${node.checked}"""")
                    if (node.type == IdeWidgetType.SWITCH) {
                        sb.appendLine("""$indent    android:thumbTint="${node.buttonTint}"""")
                        sb.appendLine("""$indent    android:trackTint="${node.trackTint}"""")
                        sb.appendLine("""$indent    android:textOn="${node.textOn}"""")
                        sb.appendLine("""$indent    android:textOff="${node.textOff}"""")
                    } else {
                        sb.appendLine("""$indent    android:buttonTint="${node.buttonTint}"""")
                    }
                    sb.appendLine("""$indent    android:text="$safeText"""")
                    sb.appendLine("""$indent    android:textColor="${node.textColor}"""")
                    sb.appendLine("""$indent    android:textSize="${node.textSizeSp}sp" />""")
                }
            }

            IdeWidgetType.WEB_VIEW,
            IdeWidgetType.CUSTOM_TAB_VIEW,
            IdeWidgetType.GECKO_WEB_VIEW,
            IdeWidgetType.AD_WEB_VIEW,
            IdeWidgetType.SWIPE_WEB_VIEW -> {
                val tagName = when (node.type) {
                    IdeWidgetType.WEB_VIEW -> "WebView"
                    IdeWidgetType.CUSTOM_TAB_VIEW -> "android.webkit.WebView"
                    IdeWidgetType.GECKO_WEB_VIEW -> "org.mozilla.geckoview.GeckoView"
                    IdeWidgetType.AD_WEB_VIEW -> "android.webkit.WebView"
                    IdeWidgetType.SWIPE_WEB_VIEW -> "androidx.swiperefreshlayout.widget.SwipeRefreshLayout"
                    else -> "WebView"
                }
                sb.appendLine("""$indent<$tagName""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    tools:url="${node.webUrl}"""")
                sb.appendLine("""$indent    tools:javaScriptEnabled="${node.webJavaScript}"""")
                sb.appendLine("""$indent    tools:domStorageEnabled="${node.webDomStorage}"""")
                sb.appendLine("""$indent    tools:supportZoom="${node.webSupportZoom}"""")
                sb.appendLine("""$indent    tools:desktopMode="${node.webDesktopMode}"""")
                sb.appendLine("""$indent    tools:cacheMode="${node.webCacheMode}"""")
                sb.appendLine("""$indent    android:padding="${node.paddingDp}dp" />""")
            }

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
                val tagName = when (node.type) {
                    IdeWidgetType.GRID_VIEW -> "GridView"
                    IdeWidgetType.LIST_VIEW -> "ListView"
                    IdeWidgetType.EXPANDABLE_LIST_VIEW -> "ExpandableListView"
                    IdeWidgetType.VIEW_PAGER,
                    IdeWidgetType.CAROUSEL_VIEW -> "androidx.viewpager2.widget.ViewPager2"
                    IdeWidgetType.SWIPE_LIST_VIEW -> "androidx.swiperefreshlayout.widget.SwipeRefreshLayout"
                    else -> "androidx.recyclerview.widget.RecyclerView"
                }
                sb.appendLine("""$indent<$tagName""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    android:padding="${node.paddingDp}dp"""")
                if (node.customView != "none" && node.customView.isNotBlank()) {
                    val cleanCustom = node.customView.removeSuffix(".xml")
                    sb.appendLine("""$indent    tools:listitem="@layout/$cleanCustom"""")
                }
                when (node.type) {
                    IdeWidgetType.GRID_VIEW -> {
                        sb.appendLine("""$indent    android:numColumns="${node.numColumns}"""")
                        sb.appendLine("""$indent    android:horizontalSpacing="${node.horizontalSpacingDp}dp"""")
                        sb.appendLine("""$indent    android:verticalSpacing="${node.verticalSpacingDp}dp"""")
                        sb.appendLine("""$indent    android:choiceMode="${node.choiceMode}" />""")
                    }
                    IdeWidgetType.LIST_VIEW,
                    IdeWidgetType.EXPANDABLE_LIST_VIEW,
                    IdeWidgetType.SWIPE_LIST_VIEW -> {
                        sb.appendLine("""$indent    android:dividerHeight="${node.dividerHeightDp}dp"""")
                        sb.appendLine("""$indent    android:choiceMode="${node.choiceMode}"""")
                        sb.appendLine("""$indent    android:stackFromBottom="${node.stackFromBottom}"""")
                        sb.appendLine("""$indent    android:fastScrollEnabled="${node.fastScroll}" />""")
                    }
                    IdeWidgetType.VIEW_PAGER,
                    IdeWidgetType.CAROUSEL_VIEW -> {
                        sb.appendLine("""$indent    android:orientation="${node.orientation}"""")
                        sb.appendLine("""$indent    tools:offscreenPageLimit="${node.pageLimit}" />""")
                    }
                    else -> {
                        sb.appendLine("""$indent    android:orientation="${node.orientation}"""")
                        sb.appendLine("""$indent    app:spanCount="${node.numColumns}"""")
                        sb.appendLine("""$indent    tools:layoutManager="${node.layoutManager}" />""")
                    }
                }
            }

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
                val tagName = when (node.type) {
                    IdeWidgetType.ANALOG_CLOCK -> "AnalogClock"
                    IdeWidgetType.DIGITAL_CLOCK -> "DigitalClock"
                    IdeWidgetType.TIME_PICKER -> "TimePicker"
                    IdeWidgetType.DATE_PICKER -> "DatePicker"
                    IdeWidgetType.CALENDAR_VIEW,
                    IdeWidgetType.DATE_RANGE_PICKER -> "CalendarView"
                    IdeWidgetType.TEXT_CLOCK,
                    IdeWidgetType.WORLD_CLOCK_VIEW -> "TextClock"
                    IdeWidgetType.CHRONOMETER,
                    IdeWidgetType.COUNTDOWN_VIEW,
                    IdeWidgetType.STOPWATCH_VIEW -> "Chronometer"
                    else -> "TextClock"
                }
                sb.appendLine("""$indent<$tagName""")
                appendCommonXmlAttributes(sb, node, indent)
                sb.appendLine("""$indent    android:padding="${node.paddingDp}dp"""")
                when (node.type) {
                    IdeWidgetType.TIME_PICKER -> {
                        sb.appendLine("""$indent    android:timePickerMode="${node.pickerMode}"""")
                        sb.appendLine("""$indent    tools:is24HourView="${node.is24HourView}" />""")
                    }
                    IdeWidgetType.DATE_PICKER,
                    IdeWidgetType.DATE_RANGE_PICKER -> {
                        sb.appendLine("""$indent    android:datePickerMode="${node.pickerMode}"""")
                        sb.appendLine("""$indent    android:firstDayOfWeek="${node.firstDayOfWeek}"""")
                        sb.appendLine("""$indent    android:minDate="${node.minDate}"""")
                        sb.appendLine("""$indent    android:maxDate="${node.maxDate}" />""")
                    }
                    IdeWidgetType.CALENDAR_VIEW -> {
                        sb.appendLine("""$indent    android:firstDayOfWeek="${node.firstDayOfWeek}"""")
                        sb.appendLine("""$indent    android:minDate="${node.minDate}"""")
                        sb.appendLine("""$indent    android:maxDate="${node.maxDate}"""")
                        sb.appendLine("""$indent    android:showWeekNumber="${node.showWeekNumber}" />""")
                    }
                    IdeWidgetType.TEXT_CLOCK,
                    IdeWidgetType.DIGITAL_CLOCK,
                    IdeWidgetType.WORLD_CLOCK_VIEW -> {
                        sb.appendLine("""$indent    android:format12Hour="${node.format12Hour}"""")
                        sb.appendLine("""$indent    android:format24Hour="${node.format24Hour}"""")
                        if (node.timeZone != "Default" && node.timeZone.isNotBlank()) {
                            sb.appendLine("""$indent    android:timeZone="${node.timeZone}"""")
                        }
                        sb.appendLine("""$indent    android:textColor="${node.textColor}"""")
                        sb.appendLine("""$indent    android:textSize="${node.textSizeSp}sp" />""")
                    }
                    IdeWidgetType.CHRONOMETER,
                    IdeWidgetType.COUNTDOWN_VIEW,
                    IdeWidgetType.STOPWATCH_VIEW -> {
                        sb.appendLine("""$indent    android:countDown="${node.countDown}"""")
                        sb.appendLine("""$indent    android:textColor="${node.textColor}"""")
                        sb.appendLine("""$indent    android:textSize="${node.textSizeSp}sp" />""")
                    }
                    else -> {
                        sb.appendLine("""$indent    tools:timeZone="${node.timeZone}" />""")
                    }
                }
            }
        }
    }

    // =========================================================================
    // Project Image Manager (import from device, list, rename, delete, multi-delete)
    // =========================================================================

    private fun projectImagesKey(projectId: String): String = "project_images_$projectId"

    private fun sanitizeDrawableName(raw: String): String {
        val cleaned = raw.trim()
            .substringBeforeLast('.')
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9_]+"), "_")
            .trim('_')
        val base = cleaned.ifBlank { "img_${System.currentTimeMillis() % 10000}" }
        return if (base.first().isDigit()) "img_$base" else base
    }

    fun getProjectImages(project: WorkspaceItem): List<ProjectImageItem> {
        val list = mutableListOf<ProjectImageItem>()
        val seenNames = mutableSetOf<String>()

        val rawJson = prefs.getString(projectImagesKey(project.id), null)
        if (!rawJson.isNullOrBlank()) {
            runCatching {
                val arr = JSONArray(rawJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val name = obj.optString("name").trim()
                    val path = obj.optString("filePath").trim()
                    val addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                    if (name.isNotEmpty() && path.isNotEmpty() && File(path).exists() && seenNames.add(name)) {
                        list.add(ProjectImageItem(name = name, filePath = path, addedAt = addedAt))
                    }
                }
            }
        }

        // Also scan drawable directories on disk for this project so external/restored images are picked up
        val internalDrawableDir = File(appContext.filesDir, "project_drawables/${project.id}")
        val candidateDirs = buildList {
            add(internalDrawableDir)
            projectDirectoriesFor(project).forEach { projDir ->
                add(File(projDir, "app/src/main/res/drawable"))
            }
        }
        candidateDirs.forEach { dir ->
            runCatching {
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.forEach { file ->
                        val ext = file.extension.lowercase(Locale.ROOT)
                        if (file.isFile && ext in setOf("png", "jpg", "jpeg", "webp")) {
                            val resName = sanitizeDrawableName(file.nameWithoutExtension)
                            if (seenNames.add(resName)) {
                                list.add(
                                    ProjectImageItem(
                                        name = resName,
                                        filePath = file.absolutePath,
                                        addedAt = file.lastModified(),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        return list.sortedByDescending { it.addedAt }
    }

    private fun saveProjectImagesMeta(project: WorkspaceItem, images: List<ProjectImageItem>) {
        val arr = JSONArray()
        images.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("name", item.name)
                    put("filePath", item.filePath)
                    put("addedAt", item.addedAt)
                },
            )
        }
        prefs.edit().putString(projectImagesKey(project.id), arr.toString()).apply()
    }

    fun importProjectImage(
        project: WorkspaceItem,
        uri: Uri,
        customName: String? = null,
    ): ProjectImageItem? {
        return runCatching {
            val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null
            if (bytes.isEmpty()) return null

            var displayFileName = customName
            if (displayFileName.isNullOrBlank()) {
                runCatching {
                    appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0 && cursor.moveToFirst()) {
                            displayFileName = cursor.getString(idx)
                        }
                    }
                }
            }

            val existing = getProjectImages(project).toMutableList()
            val baseName = sanitizeDrawableName(displayFileName ?: "image_${existing.size + 1}")
            var uniqueName = baseName
            var counter = 2
            while (existing.any { it.name.equals(uniqueName, ignoreCase = true) }) {
                uniqueName = "${baseName}_$counter"
                counter++
            }

            val internalDir = File(appContext.filesDir, "project_drawables/${project.id}").apply { mkdirs() }
            val internalFile = File(internalDir, "$uniqueName.png")
            internalFile.writeBytes(bytes)

            // Also copy into `/storage/emulated/0/.IDE Ultimate/.../app/src/main/res/drawable/`
            projectDirectoriesFor(project).forEach { projDir ->
                runCatching {
                    val drawDir = File(projDir, "app/src/main/res/drawable").apply { mkdirs() }
                    File(drawDir, "$uniqueName.png").writeBytes(bytes)
                }
            }

            val created = ProjectImageItem(
                name = uniqueName,
                filePath = internalFile.absolutePath,
                addedAt = System.currentTimeMillis(),
            )
            existing.add(0, created)
            saveProjectImagesMeta(project, existing)
            created
        }.getOrNull()
    }

    fun renameProjectImage(
        project: WorkspaceItem,
        oldName: String,
        rawNewName: String,
    ): String? {
        val cleanNew = sanitizeDrawableName(rawNewName)
        if (cleanNew.isBlank() || cleanNew.equals(oldName, ignoreCase = true)) return oldName
        val currentList = getProjectImages(project).toMutableList()
        val index = currentList.indexOfFirst { it.name == oldName }
        if (index < 0) return null
        if (currentList.any { it.name.equals(cleanNew, ignoreCase = true) && it.name != oldName }) {
            return null
        }

        val currentItem = currentList[index]
        val oldFile = File(currentItem.filePath)
        val internalDir = File(appContext.filesDir, "project_drawables/${project.id}").apply { mkdirs() }
        val newFile = File(internalDir, "$cleanNew.png")
        runCatching {
            if (oldFile.exists()) {
                oldFile.copyTo(newFile, overwrite = true)
                if (oldFile.absolutePath != newFile.absolutePath) {
                    oldFile.delete()
                }
            }
        }

        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val drawDir = File(projDir, "app/src/main/res/drawable")
                val diskOld = File(drawDir, "$oldName.png")
                val diskNew = File(drawDir, "$cleanNew.png")
                if (diskOld.exists()) {
                    diskOld.copyTo(diskNew, overwrite = true)
                    diskOld.delete()
                }
            }
        }

        currentList[index] = currentItem.copy(
            name = cleanNew,
            filePath = if (newFile.exists()) newFile.absolutePath else currentItem.filePath,
        )
        saveProjectImagesMeta(project, currentList)
        return cleanNew
    }

    fun deleteProjectImages(
        project: WorkspaceItem,
        namesToDelete: Set<String>,
    ) {
        if (namesToDelete.isEmpty()) return
        val currentList = getProjectImages(project)
        val remaining = currentList.filter { item ->
            if (item.name in namesToDelete) {
                runCatching { File(item.filePath).delete() }
                false
            } else {
                true
            }
        }
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val drawDir = File(projDir, "app/src/main/res/drawable")
                namesToDelete.forEach { name ->
                    File(drawDir, "$name.png").delete()
                    File(drawDir, "$name.jpg").delete()
                    File(drawDir, "$name.jpeg").delete()
                    File(drawDir, "$name.webp").delete()
                }
            }
        }
        saveProjectImagesMeta(project, remaining)
    }

    // =========================================================================
    // Sound Manager (res/raw), Font Manager (res/font), Icon Manager & Resources Manager (res/*)
    // =========================================================================

    private fun projectSoundsKey(projectId: String) = "project_sounds_$projectId"
    private fun projectFontsKey(projectId: String) = "project_fonts_$projectId"

    fun getProjectSounds(project: WorkspaceItem): List<ProjectSoundItem> {
        val list = mutableListOf<ProjectSoundItem>()
        val seenNames = mutableSetOf<String>()

        val rawJson = prefs.getString(projectSoundsKey(project.id), null)
        if (!rawJson.isNullOrBlank()) {
            runCatching {
                val arr = JSONArray(rawJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val name = obj.optString("name").trim()
                    val ext = obj.optString("extension", "mp3").trim()
                    val path = obj.optString("filePath").trim()
                    val addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                    if (name.isNotEmpty() && path.isNotEmpty() && File(path).exists() && seenNames.add(name)) {
                        list.add(ProjectSoundItem(name = name, extension = ext, filePath = path, addedAt = addedAt))
                    }
                }
            }
        }

        val internalRawDir = File(appContext.filesDir, "project_sounds/${project.id}")
        val candidateDirs = buildList {
            add(internalRawDir)
            projectDirectoriesFor(project).forEach { projDir ->
                add(File(projDir, "app/src/main/res/raw"))
            }
        }
        candidateDirs.forEach { dir ->
            runCatching {
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.forEach { file ->
                        val ext = file.extension.lowercase(Locale.ROOT)
                        if (file.isFile && ext in setOf("mp3", "wav", "ogg", "m4a", "aac", "flac")) {
                            val resName = sanitizeDrawableName(file.nameWithoutExtension)
                            if (seenNames.add(resName)) {
                                list.add(
                                    ProjectSoundItem(
                                        name = resName,
                                        extension = ext,
                                        filePath = file.absolutePath,
                                        addedAt = file.lastModified(),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        return list.sortedByDescending { it.addedAt }
    }

    private fun saveProjectSoundsMeta(project: WorkspaceItem, sounds: List<ProjectSoundItem>) {
        val arr = JSONArray()
        sounds.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("name", item.name)
                    put("extension", item.extension)
                    put("filePath", item.filePath)
                    put("addedAt", item.addedAt)
                },
            )
        }
        prefs.edit().putString(projectSoundsKey(project.id), arr.toString()).apply()
    }

    fun importProjectSound(
        project: WorkspaceItem,
        uri: Uri,
        customName: String? = null,
    ): ProjectSoundItem? {
        return runCatching {
            val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null
            if (bytes.isEmpty()) return null

            var displayFileName = customName
            if (displayFileName.isNullOrBlank()) {
                runCatching {
                    appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0 && cursor.moveToFirst()) {
                            displayFileName = cursor.getString(idx)
                        }
                    }
                }
            }

            val rawExt = displayFileName?.substringAfterLast('.', "mp3")?.lowercase(Locale.ROOT) ?: "mp3"
            val ext = if (rawExt in setOf("mp3", "wav", "ogg", "m4a", "aac", "flac")) rawExt else "mp3"

            val existing = getProjectSounds(project).toMutableList()
            val baseName = sanitizeDrawableName(displayFileName?.substringBeforeLast('.') ?: "sound_${existing.size + 1}")
            var uniqueName = baseName
            var counter = 2
            while (existing.any { it.name.equals(uniqueName, ignoreCase = true) }) {
                uniqueName = "${baseName}_$counter"
                counter++
            }

            val internalDir = File(appContext.filesDir, "project_sounds/${project.id}").apply { mkdirs() }
            val internalFile = File(internalDir, "$uniqueName.$ext")
            internalFile.writeBytes(bytes)

            projectDirectoriesFor(project).forEach { projDir ->
                runCatching {
                    val rawDir = File(projDir, "app/src/main/res/raw").apply { mkdirs() }
                    File(rawDir, "$uniqueName.$ext").writeBytes(bytes)
                }
            }

            val created = ProjectSoundItem(
                name = uniqueName,
                extension = ext,
                filePath = internalFile.absolutePath,
                addedAt = System.currentTimeMillis(),
            )
            existing.add(0, created)
            saveProjectSoundsMeta(project, existing)
            created
        }.getOrNull()
    }

    fun renameProjectSound(
        project: WorkspaceItem,
        oldName: String,
        rawNewName: String,
    ): String? {
        val cleanNew = sanitizeDrawableName(rawNewName)
        if (cleanNew.isBlank() || cleanNew.equals(oldName, ignoreCase = true)) return oldName
        val currentList = getProjectSounds(project).toMutableList()
        val index = currentList.indexOfFirst { it.name == oldName }
        if (index < 0) return null
        if (currentList.any { it.name.equals(cleanNew, ignoreCase = true) && it.name != oldName }) {
            return null
        }

        val currentItem = currentList[index]
        val ext = currentItem.extension
        val oldFile = File(currentItem.filePath)
        val internalDir = File(appContext.filesDir, "project_sounds/${project.id}").apply { mkdirs() }
        val newFile = File(internalDir, "$cleanNew.$ext")
        runCatching {
            if (oldFile.exists()) {
                oldFile.copyTo(newFile, overwrite = true)
                if (oldFile.absolutePath != newFile.absolutePath) {
                    oldFile.delete()
                }
            }
        }

        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val rawDir = File(projDir, "app/src/main/res/raw")
                val diskOld = File(rawDir, "$oldName.$ext")
                val diskNew = File(rawDir, "$cleanNew.$ext")
                if (diskOld.exists()) {
                    diskOld.copyTo(diskNew, overwrite = true)
                    diskOld.delete()
                }
            }
        }

        currentList[index] = currentItem.copy(
            name = cleanNew,
            filePath = if (newFile.exists()) newFile.absolutePath else currentItem.filePath,
        )
        saveProjectSoundsMeta(project, currentList)
        return cleanNew
    }

    fun deleteProjectSounds(
        project: WorkspaceItem,
        namesToDelete: Set<String>,
    ) {
        if (namesToDelete.isEmpty()) return
        val currentList = getProjectSounds(project)
        val remaining = currentList.filter { item ->
            if (item.name in namesToDelete) {
                runCatching { File(item.filePath).delete() }
                false
            } else {
                true
            }
        }
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val rawDir = File(projDir, "app/src/main/res/raw")
                namesToDelete.forEach { name ->
                    setOf("mp3", "wav", "ogg", "m4a", "aac", "flac").forEach { ext ->
                        File(rawDir, "$name.$ext").delete()
                    }
                }
            }
        }
        saveProjectSoundsMeta(project, remaining)
    }

    // -------------------------------------------------------------------------
    // Font Manager (res/font)
    // -------------------------------------------------------------------------

    fun getProjectFonts(project: WorkspaceItem): List<ProjectFontItem> {
        val list = mutableListOf<ProjectFontItem>()
        val seenNames = mutableSetOf<String>()

        val rawJson = prefs.getString(projectFontsKey(project.id), null)
        if (!rawJson.isNullOrBlank()) {
            runCatching {
                val arr = JSONArray(rawJson)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val name = obj.optString("name").trim()
                    val ext = obj.optString("extension", "ttf").trim()
                    val path = obj.optString("filePath").trim()
                    val addedAt = obj.optLong("addedAt", System.currentTimeMillis())
                    if (name.isNotEmpty() && path.isNotEmpty() && File(path).exists() && seenNames.add(name)) {
                        list.add(ProjectFontItem(name = name, extension = ext, filePath = path, addedAt = addedAt))
                    }
                }
            }
        }

        val internalFontDir = File(appContext.filesDir, "project_fonts/${project.id}")
        val candidateDirs = buildList {
            add(internalFontDir)
            projectDirectoriesFor(project).forEach { projDir ->
                add(File(projDir, "app/src/main/res/font"))
                add(File(projDir, "app/src/main/assets/fonts"))
            }
        }
        candidateDirs.forEach { dir ->
            runCatching {
                if (dir.exists() && dir.isDirectory) {
                    dir.listFiles()?.forEach { file ->
                        val ext = file.extension.lowercase(Locale.ROOT)
                        if (file.isFile && ext in setOf("ttf", "otf")) {
                            val resName = sanitizeDrawableName(file.nameWithoutExtension)
                            if (seenNames.add(resName)) {
                                list.add(
                                    ProjectFontItem(
                                        name = resName,
                                        extension = ext,
                                        filePath = file.absolutePath,
                                        addedAt = file.lastModified(),
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        return list.sortedByDescending { it.addedAt }
    }

    private fun saveProjectFontsMeta(project: WorkspaceItem, fonts: List<ProjectFontItem>) {
        val arr = JSONArray()
        fonts.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("name", item.name)
                    put("extension", item.extension)
                    put("filePath", item.filePath)
                    put("addedAt", item.addedAt)
                },
            )
        }
        prefs.edit().putString(projectFontsKey(project.id), arr.toString()).apply()
    }

    fun importProjectFont(
        project: WorkspaceItem,
        uri: Uri,
        customName: String? = null,
    ): ProjectFontItem? {
        return runCatching {
            val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return null
            if (bytes.isEmpty()) return null

            var displayFileName = customName
            if (displayFileName.isNullOrBlank()) {
                runCatching {
                    appContext.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0 && cursor.moveToFirst()) {
                            displayFileName = cursor.getString(idx)
                        }
                    }
                }
            }

            val rawExt = displayFileName?.substringAfterLast('.', "ttf")?.lowercase(Locale.ROOT) ?: "ttf"
            val ext = if (rawExt in setOf("ttf", "otf")) rawExt else "ttf"

            val existing = getProjectFonts(project).toMutableList()
            val baseName = sanitizeDrawableName(displayFileName?.substringBeforeLast('.') ?: "font_${existing.size + 1}")
            var uniqueName = baseName
            var counter = 2
            while (existing.any { it.name.equals(uniqueName, ignoreCase = true) }) {
                uniqueName = "${baseName}_$counter"
                counter++
            }

            val internalDir = File(appContext.filesDir, "project_fonts/${project.id}").apply { mkdirs() }
            val internalFile = File(internalDir, "$uniqueName.$ext")
            internalFile.writeBytes(bytes)

            projectDirectoriesFor(project).forEach { projDir ->
                runCatching {
                    val fontDir = File(projDir, "app/src/main/res/font").apply { mkdirs() }
                    File(fontDir, "$uniqueName.$ext").writeBytes(bytes)
                    val assetsFontDir = File(projDir, "app/src/main/assets/fonts").apply { mkdirs() }
                    File(assetsFontDir, "$uniqueName.$ext").writeBytes(bytes)
                }
            }

            val created = ProjectFontItem(
                name = uniqueName,
                extension = ext,
                filePath = internalFile.absolutePath,
                addedAt = System.currentTimeMillis(),
            )
            existing.add(0, created)
            saveProjectFontsMeta(project, existing)
            created
        }.getOrNull()
    }

    fun renameProjectFont(
        project: WorkspaceItem,
        oldName: String,
        rawNewName: String,
    ): String? {
        val cleanNew = sanitizeDrawableName(rawNewName)
        if (cleanNew.isBlank() || cleanNew.equals(oldName, ignoreCase = true)) return oldName
        val currentList = getProjectFonts(project).toMutableList()
        val index = currentList.indexOfFirst { it.name == oldName }
        if (index < 0) return null
        if (currentList.any { it.name.equals(cleanNew, ignoreCase = true) && it.name != oldName }) {
            return null
        }

        val currentItem = currentList[index]
        val ext = currentItem.extension
        val oldFile = File(currentItem.filePath)
        val internalDir = File(appContext.filesDir, "project_fonts/${project.id}").apply { mkdirs() }
        val newFile = File(internalDir, "$cleanNew.$ext")
        runCatching {
            if (oldFile.exists()) {
                oldFile.copyTo(newFile, overwrite = true)
                if (oldFile.absolutePath != newFile.absolutePath) {
                    oldFile.delete()
                }
            }
        }

        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                listOf("app/src/main/res/font", "app/src/main/assets/fonts").forEach { sub ->
                    val fDir = File(projDir, sub)
                    val diskOld = File(fDir, "$oldName.$ext")
                    val diskNew = File(fDir, "$cleanNew.$ext")
                    if (diskOld.exists()) {
                        diskOld.copyTo(diskNew, overwrite = true)
                        diskOld.delete()
                    }
                }
            }
        }

        currentList[index] = currentItem.copy(
            name = cleanNew,
            filePath = if (newFile.exists()) newFile.absolutePath else currentItem.filePath,
        )
        saveProjectFontsMeta(project, currentList)
        return cleanNew
    }

    fun deleteProjectFonts(
        project: WorkspaceItem,
        namesToDelete: Set<String>,
    ) {
        if (namesToDelete.isEmpty()) return
        val currentList = getProjectFonts(project)
        val remaining = currentList.filter { item ->
            if (item.name in namesToDelete) {
                runCatching { File(item.filePath).delete() }
                false
            } else {
                true
            }
        }
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                listOf("app/src/main/res/font", "app/src/main/assets/fonts").forEach { sub ->
                    val fDir = File(projDir, sub)
                    namesToDelete.forEach { name ->
                        File(fDir, "$name.ttf").delete()
                        File(fDir, "$name.otf").delete()
                    }
                }
            }
        }
        saveProjectFontsMeta(project, remaining)
    }

    // -------------------------------------------------------------------------
    // Icon Manager helper (saves generated PNG + Vector XML icon into drawable)
    // -------------------------------------------------------------------------

    fun saveGeneratedIconToProject(
        project: WorkspaceItem,
        rawName: String,
        pngBytes: ByteArray,
        vectorXml: String? = null,
    ): ProjectImageItem? {
        return runCatching {
            val existing = getProjectImages(project).toMutableList()
            val baseName = sanitizeDrawableName(rawName.ifBlank { "ic_icon_${existing.size + 1}" })
            val finalName = if (baseName.startsWith("ic_")) baseName else "ic_$baseName"

            val internalDir = File(appContext.filesDir, "project_drawables/${project.id}").apply { mkdirs() }
            val internalFile = File(internalDir, "$finalName.png")
            internalFile.writeBytes(pngBytes)

            val resRoot = ensureProjectResRoot(project)
            val resDrawDir = File(resRoot, "drawable").apply { mkdirs() }
            File(resDrawDir, "$finalName.png").writeBytes(pngBytes)
            if (!vectorXml.isNullOrBlank()) {
                File(resDrawDir, "${finalName}_vector.xml").writeText(vectorXml, Charsets.UTF_8)
            }

            projectDirectoriesFor(project).forEach { projDir ->
                runCatching {
                    val drawDir = File(projDir, "app/src/main/res/drawable").apply { mkdirs() }
                    File(drawDir, "$finalName.png").writeBytes(pngBytes)
                    if (!vectorXml.isNullOrBlank()) {
                        File(drawDir, "${finalName}_vector.xml").writeText(vectorXml, Charsets.UTF_8)
                    }
                }
            }

            val created = ProjectImageItem(
                name = finalName,
                filePath = internalFile.absolutePath,
                addedAt = System.currentTimeMillis(),
            )
            existing.removeAll { it.name.equals(finalName, ignoreCase = true) }
            existing.add(0, created)
            saveProjectImagesMeta(project, existing)
            created
        }.getOrNull()
    }

    // -------------------------------------------------------------------------
    // Resources Manager (Full app/src/main/res Folder Browser + Create/Rename/Delete)
    // -------------------------------------------------------------------------

    private fun syncProjectLauncherIcon(project: WorkspaceItem, internalResRoot: File) {
        val customPath = project.customIconPath
        val customFile = if (!customPath.isNullOrBlank()) File(customPath) else null
        val hasCustomIcon = customFile != null && customFile.exists() && customFile.length() > 0L

        val mipmapDir = File(internalResRoot, "mipmap").apply { mkdirs() }
        val mipmapV26Dir = File(internalResRoot, "mipmap-anydpi-v26").apply { mkdirs() }
        val drawableDir = File(internalResRoot, "drawable").apply { mkdirs() }

        if (hasCustomIcon && customFile != null) {
            val densities = listOf(
                "mipmap-mdpi" to 48,
                "mipmap-hdpi" to 72,
                "mipmap-xhdpi" to 96,
                "mipmap-xxhdpi" to 144,
                "mipmap-xxxhdpi" to 192,
            )
            val rawBmp = runCatching { android.graphics.BitmapFactory.decodeFile(customFile.absolutePath) }.getOrNull()
            if (rawBmp != null) {
                densities.forEach { (dirName, size) ->
                    val densityDir = File(internalResRoot, dirName).apply { mkdirs() }
                    val scaled = android.graphics.Bitmap.createScaledBitmap(rawBmp, size, size, true)
                    val launcherPng = File(densityDir, "ic_launcher.png")
                    val roundPng = File(densityDir, "ic_launcher_round.png")
                    java.io.FileOutputStream(launcherPng).use { out -> scaled.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out) }
                    java.io.FileOutputStream(roundPng).use { out -> scaled.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out) }
                }
                customFile.copyTo(File(mipmapDir, "ic_launcher.png"), overwrite = true)
                customFile.copyTo(File(mipmapDir, "ic_launcher_round.png"), overwrite = true)
                val customDrawable = File(drawableDir, "ic_project_launcher.png")
                customFile.copyTo(customDrawable, overwrite = true)

                File(mipmapV26Dir, "ic_launcher.xml").writeText(
                    """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/white" />
    <foreground android:drawable="@drawable/ic_project_launcher" />
</adaptive-icon>
""".trimIndent(),
                    Charsets.UTF_8,
                )
                File(mipmapV26Dir, "ic_launcher_round.xml").writeText(
                    """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/white" />
    <foreground android:drawable="@drawable/ic_project_launcher" />
</adaptive-icon>
""".trimIndent(),
                    Charsets.UTF_8,
                )
                val colorsFile = File(internalResRoot, "values/colors.xml")
                if (colorsFile.exists()) {
                    val colorsContent = colorsFile.readText(Charsets.UTF_8)
                    if (!colorsContent.contains("\"white\"")) {
                        val updated = colorsContent.replace("</resources>", "    <color name=\"white\">#FFFFFFFF</color>\n</resources>")
                        colorsFile.writeText(updated, Charsets.UTF_8)
                    }
                } else {
                    File(internalResRoot, "values").mkdirs()
                    colorsFile.writeText("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    <color name=\"white\">#FFFFFFFF</color>\n</resources>", Charsets.UTF_8)
                }
                return
            }
        }

        // Fallback default launcher icon
        val defaultBgFile = File(internalResRoot, "drawable/ic_launcher_background.xml")
        if (!defaultBgFile.exists()) {
            defaultBgFile.writeText(
                """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FF0F172A"
        android:pathData="M0,0h108v108h-108z" />
</vector>
""",
                Charsets.UTF_8,
            )
        }
        val defaultFgFile = File(internalResRoot, "drawable/ic_launcher_foreground.xml")
        if (!defaultFgFile.exists()) {
            defaultFgFile.writeText(
                """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FF38BDF8"
        android:pathData="M38,34 L26,54 L38,74 M70,34 L82,54 L70,74 M59,30 L49,78"
        android:strokeColor="#FF38BDF8"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:strokeWidth="5" />
</vector>
""",
                Charsets.UTF_8,
            )
        }
        val launcherMipmap = File(mipmapV26Dir, "ic_launcher.xml")
        if (!launcherMipmap.exists()) {
            launcherMipmap.writeText(
                """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
""",
                Charsets.UTF_8,
            )
        }
        val launcherRoundMipmap = File(mipmapV26Dir, "ic_launcher_round.xml")
        if (!launcherRoundMipmap.exists()) {
            launcherRoundMipmap.writeText(
                """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
""",
                Charsets.UTF_8,
            )
        }
    }

    fun ensureProjectResRoot(project: WorkspaceItem): File {
        val internalResRoot = File(appContext.filesDir, "project_res/${project.id}/res").apply { mkdirs() }
        val defaultFolders = listOf("drawable", "layout", "values", "raw", "font", "xml", "mipmap-anydpi-v26", "menu", "anim")
        defaultFolders.forEach { folder ->
            File(internalResRoot, folder).mkdirs()
        }

        // Sync project launcher icon (if custom icon is selected by user, write PNGs & adaptive XML; else fallback)
        syncProjectLauncherIcon(project, internalResRoot)

        // Sync existing drawables, layouts, sounds, fonts into internalResRoot so everything appears in Resources Manager
        getProjectImages(project).forEach { img ->
            val src = File(img.filePath)
            if (src.exists()) {
                val dst = File(internalResRoot, "drawable/${src.name}")
                if (!dst.exists()) runCatching { src.copyTo(dst, overwrite = false) }
            }
        }
        getProjectSounds(project).forEach { snd ->
            val src = File(snd.filePath)
            if (src.exists()) {
                val dst = File(internalResRoot, "raw/${src.name}")
                if (!dst.exists()) runCatching { src.copyTo(dst, overwrite = false) }
            }
        }
        getProjectFonts(project).forEach { fnt ->
            val src = File(fnt.filePath)
            if (src.exists()) {
                val dst = File(internalResRoot, "font/${src.name}")
                if (!dst.exists()) runCatching { src.copyTo(dst, overwrite = false) }
            }
        }

        // Default values files if missing
        val stringsFile = File(internalResRoot, "values/strings.xml")
        if (!stringsFile.exists()) {
            stringsFile.writeText(
                """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">${project.name}</string>
</resources>
""",
                Charsets.UTF_8,
            )
        }
        val colorsFile = File(internalResRoot, "values/colors.xml")
        if (!colorsFile.exists()) {
            colorsFile.writeText(
                """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="colorPrimary">#38BDF8</color>
    <color name="colorPrimaryDark">#050816</color>
    <color name="colorAccent">#22C55E</color>
    <color name="primary">#38BDF8</color>
    <color name="primary_variant">#0284C7</color>
    <color name="secondary">#22C55E</color>
</resources>
""",
                Charsets.UTF_8,
            )
        }
        val stylesFile = File(internalResRoot, "values/styles.xml")
        if (!stylesFile.exists()) {
            stylesFile.writeText(
                """<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="AppTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>
</resources>
""",
                Charsets.UTF_8,
            )
        } else {
            val content = stylesFile.readText(Charsets.UTF_8)
            if (content.contains("android:Theme.Material")) {
                stylesFile.writeText(
                    content.replace("android:Theme.Material.Light.NoActionBar", "Theme.MaterialComponents.DayNight.NoActionBar")
                           .replace("android:Theme.Material.NoActionBar", "Theme.MaterialComponents.DayNight.NoActionBar")
                           .replace("android:Theme.Material", "Theme.MaterialComponents.DayNight.NoActionBar"),
                    Charsets.UTF_8
                )
            }
        }

        // Sync layouts from projectDirectoriesFor(project)
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val extRes = File(projDir, "app/src/main/res")
                if (extRes.exists()) {
                    extRes.walkTopDown().forEach { f ->
                        val rel = f.relativeTo(extRes).path
                        if (rel.isNotEmpty()) {
                            val target = File(internalResRoot, rel)
                            if (f.isDirectory) {
                                target.mkdirs()
                            } else if (!target.exists()) {
                                target.parentFile?.mkdirs()
                                f.copyTo(target, overwrite = false)
                            }
                        }
                    }
                }
            }
        }

        return internalResRoot
    }

    private fun syncResRootToExternal(project: WorkspaceItem, internalResRoot: File) {
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val extRes = File(projDir, "app/src/main/res").apply { mkdirs() }
                internalResRoot.walkTopDown().forEach { f ->
                    val rel = f.relativeTo(internalResRoot).path
                    if (rel.isNotEmpty()) {
                        val target = File(extRes, rel)
                        if (f.isDirectory) {
                            target.mkdirs()
                        } else {
                            target.parentFile?.mkdirs()
                            f.copyTo(target, overwrite = true)
                        }
                    }
                }
            }
        }
    }

    fun listProjectResourceEntries(
        project: WorkspaceItem,
        relativePath: String,
    ): List<ProjectResourceEntry> {
        val root = ensureProjectResRoot(project)
        val cleanRel = relativePath.trim('/')
        val targetDir = if (cleanRel.isEmpty()) root else File(root, cleanRel)
        if (!targetDir.exists() || !targetDir.isDirectory) return emptyList()

        return (targetDir.listFiles() ?: emptyArray())
            .sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase(Locale.ROOT) }))
            .map { f ->
                val rel = f.relativeTo(root).path.replace('\\', '/')
                ProjectResourceEntry(
                    name = f.name,
                    relativePath = rel,
                    absolutePath = f.absolutePath,
                    isDirectory = f.isDirectory,
                    sizeBytes = if (f.isFile) f.length() else 0L,
                    childCount = if (f.isDirectory) (f.listFiles()?.size ?: 0) else 0,
                    lastModified = f.lastModified(),
                )
            }
    }

    fun createProjectResourceEntry(
        project: WorkspaceItem,
        parentRelativePath: String,
        rawName: String,
        isDirectory: Boolean,
        initialContent: String = "",
    ): Boolean {
        val cleanName = rawName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (cleanName.isBlank()) return false
        val root = ensureProjectResRoot(project)
        val cleanRel = parentRelativePath.trim('/')
        val parentDir = if (cleanRel.isEmpty()) root else File(root, cleanRel)
        parentDir.mkdirs()

        val target = File(parentDir, cleanName)
        if (target.exists()) return false
        val ok = runCatching {
            if (isDirectory) {
                target.mkdirs()
            } else {
                target.parentFile?.mkdirs()
                val defaultBody = initialContent.ifBlank {
                    if (cleanName.endsWith(".xml", ignoreCase = true)) {
                        "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    \n</resources>\n"
                    } else {
                        ""
                    }
                }
                target.writeText(defaultBody, Charsets.UTF_8)
                true
            }
        }.getOrDefault(false)
        if (ok) {
            syncResRootToExternal(project, root)
        }
        return ok
    }

    fun renameProjectResourceEntry(
        project: WorkspaceItem,
        entryRelativePath: String,
        rawNewName: String,
    ): Boolean {
        val cleanNew = rawNewName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (cleanNew.isBlank()) return false
        val root = ensureProjectResRoot(project)
        val target = File(root, entryRelativePath.trim('/'))
        if (!target.exists()) return false
        val dest = File(target.parentFile ?: root, cleanNew)
        if (dest.exists()) return false
        val ok = runCatching { target.renameTo(dest) }.getOrDefault(false)
        if (ok) {
            projectDirectoriesFor(project).forEach { projDir ->
                runCatching {
                    val extRes = File(projDir, "app/src/main/res")
                    val extOld = File(extRes, entryRelativePath.trim('/'))
                    val extNew = File(extOld.parentFile ?: extRes, cleanNew)
                    if (extOld.exists()) extOld.renameTo(extNew)
                }
            }
        }
        return ok
    }

    fun deleteProjectResourceEntry(
        project: WorkspaceItem,
        entryRelativePath: String,
    ): Boolean {
        val root = ensureProjectResRoot(project)
        val target = File(root, entryRelativePath.trim('/'))
        if (!target.exists()) return false
        val ok = runCatching { target.deleteRecursively() }.getOrDefault(false)
        if (ok) {
            projectDirectoriesFor(project).forEach { projDir ->
                runCatching {
                    val extRes = File(projDir, "app/src/main/res")
                    File(extRes, entryRelativePath.trim('/')).deleteRecursively()
                }
            }
        }
        return ok
    }

    fun readProjectResourceFile(
        project: WorkspaceItem,
        entryRelativePath: String,
    ): String {
        val root = ensureProjectResRoot(project)
        val target = File(root, entryRelativePath.trim('/'))
        if (!target.exists() || !target.isFile) return ""
        return runCatching { target.readText(Charsets.UTF_8) }.getOrDefault("")
    }

    fun writeProjectResourceFile(
        project: WorkspaceItem,
        entryRelativePath: String,
        content: String,
    ) {
        val root = ensureProjectResRoot(project)
        val target = File(root, entryRelativePath.trim('/'))
        runCatching {
            target.parentFile?.mkdirs()
            target.writeText(content, Charsets.UTF_8)
        }
        syncResRootToExternal(project, root)
    }

    // -------------------------------------------------------------------------
    // AndroidManifest.xml Manager & Permission Manager
    // -------------------------------------------------------------------------

    private fun defaultProjectManifestXml(project: WorkspaceItem): String {
        val pages = listProjectXmlPages(project)
        val activitiesXml = buildString {
            pages.forEachIndexed { idx, p ->
                if (idx == 0 || p.activityName == "MainActivity") {
                    appendLine(
                        """        <activity
            android:name=".${p.activityName}"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>""",
                    )
                } else {
                    appendLine(
                        """        <activity
            android:name=".${p.activityName}"
            android:exported="false" />""",
                    )
                }
            }
        }.trimEnd()

        return """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="${project.packageName}">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme"
        android:usesCleartextTraffic="true">
$activitiesXml
    </application>

</manifest>
"""
    }

    fun readProjectManifest(project: WorkspaceItem): String {
        val prefKey = "manifest_xml_${project.id}"
        val internalFile = File(appContext.filesDir, "project_manifest/${project.id}/AndroidManifest.xml")
        if (internalFile.exists() && internalFile.isFile) {
            val txt = runCatching { internalFile.readText(Charsets.UTF_8) }.getOrNull()
            if (!txt.isNullOrBlank()) return txt
        }
        for (projDir in projectDirectoriesFor(project)) {
            val extFile = File(projDir, "app/src/main/AndroidManifest.xml")
            if (extFile.exists() && extFile.isFile) {
                val txt = runCatching { extFile.readText(Charsets.UTF_8) }.getOrNull()
                if (!txt.isNullOrBlank()) {
                    runCatching {
                        internalFile.parentFile?.mkdirs()
                        internalFile.writeText(txt, Charsets.UTF_8)
                    }
                    return txt
                }
            }
        }
        val savedPref = prefs.getString(prefKey, null)
        if (!savedPref.isNullOrBlank()) return savedPref

        val defaultXml = defaultProjectManifestXml(project)
        writeProjectManifest(project, defaultXml)
        return defaultXml
    }

    fun writeProjectManifest(project: WorkspaceItem, xmlContent: String) {
        val prefKey = "manifest_xml_${project.id}"
        prefs.edit().putString(prefKey, xmlContent).apply()

        val internalFile = File(appContext.filesDir, "project_manifest/${project.id}/AndroidManifest.xml")
        runCatching {
            internalFile.parentFile?.mkdirs()
            internalFile.writeText(xmlContent, Charsets.UTF_8)
        }
        val fullRootManifest = File(appContext.filesDir, "project_full/${project.id}/app/src/main/AndroidManifest.xml")
        runCatching {
            fullRootManifest.parentFile?.mkdirs()
            fullRootManifest.writeText(xmlContent, Charsets.UTF_8)
        }
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                val extFile = File(projDir, "app/src/main/AndroidManifest.xml")
                extFile.parentFile?.mkdirs()
                extFile.writeText(xmlContent, Charsets.UTF_8)
            }
        }
    }

    private fun syncActivityIntoManifest(project: WorkspaceItem, activityName: String) {
        val current = readProjectManifest(project)
        if (current.contains("\".$activityName\"") || current.contains("\"${project.packageName}.$activityName\"")) {
            return
        }
        val activityBlock = """
        <activity
            android:name=".$activityName"
            android:exported="false" />
"""
        val updated = if (current.contains("</application>")) {
            current.replaceFirst("</application>", "$activityBlock    </application>")
        } else {
            current
        }
        writeProjectManifest(project, updated)
    }

    fun getProjectPermissions(project: WorkspaceItem): Set<String> {
        val manifest = readProjectManifest(project)
        val regex = Regex("""<uses-permission\s+[^>]*android:name="([^"]+)"""")
        return regex.findAll(manifest)
            .map { it.groupValues[1].trim() }
            .filter { it.isNotEmpty() }
            .toCollection(linkedSetOf())
    }

    fun toggleProjectPermission(project: WorkspaceItem, fullPermissionName: String): Set<String> {
        val cleanPerm = if (fullPermissionName.contains('.')) {
            fullPermissionName.trim()
        } else {
            "android.permission.${fullPermissionName.trim()}"
        }
        val currentManifest = readProjectManifest(project)
        val existingPerms = getProjectPermissions(project)
        val updatedManifest = if (cleanPerm in existingPerms) {
            currentManifest.replace(
                Regex("""[ \t]*<uses-permission\s+[^>]*android:name="${Regex.escape(cleanPerm)}"[^>]*/>\r?\n?"""),
                "",
            )
        } else {
            val permLine = """    <uses-permission android:name="$cleanPerm" />"""
            when {
                currentManifest.contains("<application") ->
                    currentManifest.replaceFirst("<application", "$permLine\n\n    <application")
                currentManifest.contains("</manifest>") ->
                    currentManifest.replaceFirst("</manifest>", "$permLine\n</manifest>")
                else -> "$currentManifest\n$permLine\n"
            }
        }
        writeProjectManifest(project, updatedManifest)
        return getProjectPermissions(project)
    }

    // -------------------------------------------------------------------------
    // Files Manager (Full Android Project Folder & File Browser + Read/Write/Rename/Delete)
    // -------------------------------------------------------------------------

    fun ensureProjectFullRoot(project: WorkspaceItem): File {
        val root = File(appContext.filesDir, "project_full/${project.id}").apply { mkdirs() }
        val pkgPath = project.packageName.replace('.', '/')

        // 1. Sync AndroidManifest.xml
        val manifestFile = File(root, "app/src/main/AndroidManifest.xml")
        manifestFile.parentFile?.mkdirs()
        if (!manifestFile.exists()) {
            manifestFile.writeText(readProjectManifest(project), Charsets.UTF_8)
        }

        // 2. Sync res/ folder
        val resSource = ensureProjectResRoot(project)
        val resTarget = File(root, "app/src/main/res").apply { mkdirs() }
        runCatching {
            resSource.walkTopDown().forEach { f ->
                val rel = f.relativeTo(resSource).path
                if (rel.isNotEmpty()) {
                    val dst = File(resTarget, rel)
                    if (f.isDirectory) {
                        dst.mkdirs()
                    } else if (!dst.exists()) {
                        dst.parentFile?.mkdirs()
                        f.copyTo(dst, overwrite = false)
                    }
                }
            }
        }

        // 3. Sync all XML Layouts & Java/Kotlin Activities (respect project.language)
        listProjectXmlPages(project).forEach { page ->
            val xmlFile = File(root, "app/src/main/res/layout/${page.xmlFileName}")
            xmlFile.parentFile?.mkdirs()
            if (!xmlFile.exists()) {
                xmlFile.writeText(readCodeForFlavor(project, page, CodeEditorFlavor.XML_ANDROID), Charsets.UTF_8)
            }
            if (project.language == ProjectLanguage.JAVA) {
                val javaFile = File(root, "app/src/main/java/$pkgPath/${page.activityName}.java")
                javaFile.parentFile?.mkdirs()
                if (!javaFile.exists()) {
                    javaFile.writeText(readCodeForFlavor(project, page, CodeEditorFlavor.JAVA_ANDROID), Charsets.UTF_8)
                }
            } else {
                val ktFile = File(root, "app/src/main/java/$pkgPath/${page.activityName}.kt")
                ktFile.parentFile?.mkdirs()
                if (!ktFile.exists()) {
                    ktFile.writeText(readCodeForFlavor(project, page, CodeEditorFlavor.KOTLIN_ANDROID), Charsets.UTF_8)
                }
            }
        }

        // 3.5 Generate client logging class (AppLogger) into project source tree so projects automatically send logs to IDE Ultimate Logcat Reader
        runCatching {
            if (project.language == ProjectLanguage.JAVA) {
                val loggerFile = File(root, "app/src/main/java/$pkgPath/AppLogger.java")
                if (!loggerFile.exists()) {
                    loggerFile.parentFile?.mkdirs()
                    loggerFile.writeText(defaultJavaAppLogger(project), Charsets.UTF_8)
                }
            } else {
                val loggerFile = File(root, "app/src/main/java/$pkgPath/AppLogger.kt")
                if (!loggerFile.exists()) {
                    loggerFile.parentFile?.mkdirs()
                    loggerFile.writeText(defaultKotlinAppLogger(project), Charsets.UTF_8)
                }
            }
        }

        // 4. Default Gradle & Project files
        val appBuildGradle = File(root, "app/build.gradle.kts")
        val isJavaProj = (project.language == ProjectLanguage.JAVA)
        val pluginsBlock = if (isJavaProj) {
            """plugins {
    id("com.android.application")
}"""
        } else {
            """plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}"""
        }
        val kotlinBlock = if (!isJavaProj) {
            """
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
"""
        } else {
            ""
        }
        val depsBlock = if (isJavaProj) {
            """dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}"""
        } else {
            """dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}"""
        }
        if (!appBuildGradle.exists()) {
            appBuildGradle.parentFile?.mkdirs()
            appBuildGradle.writeText(
                """$pluginsBlock

android {
    namespace = "${project.packageName}"
    compileSdk = 35

    defaultConfig {
        applicationId = "${project.packageName}"
        minSdk = 24
        targetSdk = 35
        versionCode = ${project.versionCode}
        versionName = "${project.versionName}"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
$kotlinBlock
$depsBlock
""",
                Charsets.UTF_8,
            )
        }

        // Root build.gradle
        val rootBuildGradleFile = File(root, "build.gradle.kts").takeIf { it.exists() }
            ?: File(root, "build.gradle")
        if (!rootBuildGradleFile.exists()) {
            val defaultRootGradle = File(root, "build.gradle.kts")
            val rootPlugins = if (isJavaProj) {
                """plugins {
    id("com.android.application") version "8.9.2" apply false
}
"""
            } else {
                """plugins {
    id("com.android.application") version "8.9.2" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
}
"""
            }
            defaultRootGradle.writeText(rootPlugins, Charsets.UTF_8)
        }

        val settingsGradle = File(root, "settings.gradle.kts").takeIf { it.exists() }
            ?: File(root, "settings.gradle")
        if (!settingsGradle.exists()) {
            val defaultSettings = File(root, "settings.gradle.kts")
            defaultSettings.writeText(
                """pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "${project.name}"
include(":app")
""",
                Charsets.UTF_8,
            )
        }

        val gradleProps = File(root, "gradle.properties")
        if (!gradleProps.exists()) {
            gradleProps.writeText(
                """org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
kotlin.jvm.target.validation.mode=warning
""",
                Charsets.UTF_8,
            )
        }

        // Vendor standard Gradle Wrapper files if missing
        runCatching {
            val gradlewTarget = File(root, "gradlew")
            if (!gradlewTarget.exists()) {
                appContext.assets.open("gradle-wrapper/gradlew").use { input ->
                    gradlewTarget.outputStream().use { output -> input.copyTo(output) }
                }
                gradlewTarget.setExecutable(true, false)
            }
            val wrapperJarTarget = File(root, "gradle/wrapper/gradle-wrapper.jar")
            if (!wrapperJarTarget.exists()) {
                wrapperJarTarget.parentFile?.mkdirs()
                appContext.assets.open("gradle-wrapper/gradle/wrapper/gradle-wrapper.jar").use { input ->
                    wrapperJarTarget.outputStream().use { output -> input.copyTo(output) }
                }
            }
            val wrapperPropsTarget = File(root, "gradle/wrapper/gradle-wrapper.properties")
            if (!wrapperPropsTarget.exists()) {
                wrapperPropsTarget.parentFile?.mkdirs()
                appContext.assets.open("gradle-wrapper/gradle/wrapper/gradle-wrapper.properties").use { input ->
                    wrapperPropsTarget.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }

        // Sync any existing external files from /storage/emulated/0/.IDE Ultimate/<project>
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                if (projDir.exists() && projDir.isDirectory) {
                    projDir.walkTopDown().forEach { f ->
                        val rel = f.relativeTo(projDir).path
                        if (rel.isNotEmpty() && !rel.startsWith(".ide_")) {
                            val dst = File(root, rel)
                            if (f.isDirectory) {
                                dst.mkdirs()
                            } else if (!dst.exists()) {
                                dst.parentFile?.mkdirs()
                                f.copyTo(dst, overwrite = false)
                            }
                        }
                    }
                }
            }
        }

        return root
    }

    private fun syncFullRootToExternal(project: WorkspaceItem, root: File) {
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                if (!projDir.exists()) projDir.mkdirs()
                root.walkTopDown().forEach { f ->
                    val rel = f.relativeTo(root).path
                    if (rel.isNotEmpty()) {
                        val dst = File(projDir, rel)
                        if (f.isDirectory) {
                            dst.mkdirs()
                        } else {
                            dst.parentFile?.mkdirs()
                            f.copyTo(dst, overwrite = true)
                        }
                    }
                }
            }
        }
    }

    fun listProjectFiles(
        project: WorkspaceItem,
        relativePath: String,
    ): List<ProjectResourceEntry> {
        val root = ensureProjectFullRoot(project)
        val cleanRel = relativePath.trim('/')
        val targetDir = if (cleanRel.isEmpty()) root else File(root, cleanRel)
        if (!targetDir.exists() || !targetDir.isDirectory) return emptyList()

        return (targetDir.listFiles() ?: emptyArray())
            .filterNot { it.name.startsWith(".ide_") }
            .sortedWith(compareBy<File>({ !it.isDirectory }, { it.name.lowercase(Locale.ROOT) }))
            .map { f ->
                val rel = f.relativeTo(root).path.replace('\\', '/')
                ProjectResourceEntry(
                    name = f.name,
                    relativePath = rel,
                    absolutePath = f.absolutePath,
                    isDirectory = f.isDirectory,
                    sizeBytes = if (f.isFile) f.length() else 0L,
                    childCount = if (f.isDirectory) (f.listFiles()?.size ?: 0) else 0,
                    lastModified = f.lastModified(),
                )
            }
    }

    fun createProjectFileOrFolder(
        project: WorkspaceItem,
        parentRelativePath: String,
        rawName: String,
        isDirectory: Boolean,
        initialContent: String = "",
    ): Boolean {
        val cleanName = rawName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (cleanName.isBlank()) return false
        val root = ensureProjectFullRoot(project)
        val cleanRel = parentRelativePath.trim('/')
        val parentDir = if (cleanRel.isEmpty()) root else File(root, cleanRel)
        parentDir.mkdirs()

        val target = File(parentDir, cleanName)
        if (target.exists()) return false
        val ok = runCatching {
            if (isDirectory) {
                target.mkdirs()
            } else {
                target.parentFile?.mkdirs()
                val defaultBody = initialContent.ifBlank {
                    when {
                        cleanName.endsWith(".xml", ignoreCase = true) ->
                            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<LinearLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    android:layout_width=\"match_parent\"\n    android:layout_height=\"match_parent\"\n    android:orientation=\"vertical\">\n\n</LinearLayout>\n"
                        cleanName.endsWith(".kt", ignoreCase = true) ->
                            "package ${project.packageName}\n\nclass ${cleanName.removeSuffix(".kt")} {\n    \n}\n"
                        cleanName.endsWith(".java", ignoreCase = true) ->
                            "package ${project.packageName};\n\npublic class ${cleanName.removeSuffix(".java")} {\n    \n}\n"
                        else -> ""
                    }
                }
                target.writeText(defaultBody, Charsets.UTF_8)
                true
            }
        }.getOrDefault(false)
        if (ok) {
            syncFullRootToExternal(project, root)
        }
        return ok
    }

    fun renameProjectFileOrFolder(
        project: WorkspaceItem,
        entryRelativePath: String,
        rawNewName: String,
    ): Boolean {
        val cleanNew = rawNewName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (cleanNew.isBlank()) return false
        val root = ensureProjectFullRoot(project)
        val target = File(root, entryRelativePath.trim('/'))
        if (!target.exists()) return false
        val dest = File(target.parentFile ?: root, cleanNew)
        if (dest.exists()) return false
        val ok = runCatching { target.renameTo(dest) }.getOrDefault(false)
        if (ok) {
            projectDirectoriesFor(project).forEach { projDir ->
                runCatching {
                    val extOld = File(projDir, entryRelativePath.trim('/'))
                    val extNew = File(extOld.parentFile ?: projDir, cleanNew)
                    if (extOld.exists()) extOld.renameTo(extNew)
                }
            }
        }
        return ok
    }

    fun deleteProjectFileOrFolder(
        project: WorkspaceItem,
        entryRelativePath: String,
    ): Boolean {
        val root = ensureProjectFullRoot(project)
        val target = File(root, entryRelativePath.trim('/'))
        if (!target.exists()) return false
        val ok = runCatching { target.deleteRecursively() }.getOrDefault(false)
        if (ok) {
            projectDirectoriesFor(project).forEach { projDir ->
                runCatching {
                    File(projDir, entryRelativePath.trim('/')).deleteRecursively()
                }
            }
        }
        return ok
    }

    fun readProjectFileText(
        project: WorkspaceItem,
        entryRelativePath: String,
    ): String {
        val cleanRel = entryRelativePath.trim('/').replace('\\', '/')
        if (cleanRel.equals("app/src/main/AndroidManifest.xml", ignoreCase = true)) {
            return readProjectManifest(project)
        }
        val root = ensureProjectFullRoot(project)
        val target = File(root, cleanRel)
        if (!target.exists() || !target.isFile) return ""
        return runCatching { target.readText(Charsets.UTF_8) }.getOrDefault("")
    }

    fun writeProjectFileText(
        project: WorkspaceItem,
        entryRelativePath: String,
        content: String,
    ) {
        val cleanRel = entryRelativePath.trim('/').replace('\\', '/')
        if (cleanRel.equals("app/src/main/AndroidManifest.xml", ignoreCase = true)) {
            writeProjectManifest(project, content)
            return
        }
        val root = ensureProjectFullRoot(project)
        val target = File(root, cleanRel)
        runCatching {
            target.parentFile?.mkdirs()
            target.writeText(content, Charsets.UTF_8)
        }
        if (cleanRel.startsWith("app/src/main/res/")) {
            val resRel = cleanRel.removePrefix("app/src/main/res/")
            writeProjectResourceFile(project, resRel, content)
        }
        syncFullRootToExternal(project, root)
    }

    fun collectAllProjectFilesForUpload(
        project: WorkspaceItem,
    ): List<Pair<String, ByteArray>> {
        val root = ensureProjectFullRoot(project)
        val result = mutableListOf<Pair<String, ByteArray>>()
        runCatching {
            root.walkTopDown().forEach { f ->
                if (f.isFile && !f.name.startsWith(".ide_")) {
                    val rel = f.relativeTo(root).path.replace('\\', '/')
                    val bytes = runCatching { f.readBytes() }.getOrNull()
                    if (rel.isNotEmpty() && bytes != null) {
                        result.add(rel to bytes)
                    }
                }
            }
        }
        return result
    }

    /**
     * Loads the saved Logic Events & Functions for [xmlPage] in [project].
     * Defaults to `onCreate` and `initializeLogic` in the `Activity` section (matching Sketchware/IDE layout).
     */
    fun loadLogicItemsForXmlPage(
        project: WorkspaceItem,
        xmlPage: ProjectXmlPage,
    ): List<IdeLogicItem> {
        val prefKey = "logic_items_${project.id}_${xmlPage.xmlFileName}"
        val raw = prefs.getString(prefKey, null)
        if (!raw.isNullOrBlank()) {
            val parsed = parseLogicItemsJson(raw)
            if (parsed.isNotEmpty()) return parsed
        }
        for (projDir in projectDirectoriesFor(project)) {
            val diskFile = File(projDir, ".ide_logic_${xmlPage.layoutResourceName}.json")
            if (diskFile.exists() && diskFile.isFile) {
                val diskRaw = runCatching { diskFile.readText(Charsets.UTF_8) }.getOrNull()
                if (!diskRaw.isNullOrBlank()) {
                    val parsed = parseLogicItemsJson(diskRaw)
                    if (parsed.isNotEmpty()) {
                        prefs.edit().putString(prefKey, diskRaw).apply()
                        return parsed
                    }
                }
            }
        }
        return listOf(
            IdeLogicItem(
                id = "default_onCreate_${xmlPage.layoutResourceName}",
                section = IdeLogicSection.ACTIVITY,
                name = "onCreate",
                subtitle = "On activity create",
                bodyCode = "// On activity create\nsetContentView(R.layout.${xmlPage.layoutResourceName})\ninitializeLogic()",
            ),
            IdeLogicItem(
                id = "default_initializeLogic_${xmlPage.layoutResourceName}",
                section = IdeLogicSection.ACTIVITY,
                name = "initializeLogic",
                subtitle = "On activity create",
                bodyCode = "// Initialize logic here",
            ),
        )
    }

    /**
     * Saves the Logic Events & Functions for [xmlPage] in [project] to preferences and disk.
     */
    fun saveLogicItemsForXmlPage(
        project: WorkspaceItem,
        xmlPage: ProjectXmlPage,
        logicItems: List<IdeLogicItem>,
    ) {
        val array = JSONArray()
        logicItems.forEach { item ->
            val paramsArray = JSONArray()
            item.parameters.forEach { p ->
                paramsArray.put(
                    JSONObject().apply {
                        put("name", p.name)
                        put("type", p.type)
                    },
                )
            }
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("section", item.section.name)
                    put("name", item.name)
                    put("subtitle", item.subtitle)
                    put("targetViewId", item.targetViewId)
                    put("returnType", item.returnType)
                    put("parameters", paramsArray)
                    put("privateVarName", item.privateVarName)
                    put("privateVarType", item.privateVarType)
                    put("privateVarValue", item.privateVarValue)
                    put("bodyCode", item.bodyCode)
                },
            )
        }
        val jsonText = array.toString(2)
        val prefKey = "logic_items_${project.id}_${xmlPage.xmlFileName}"
        prefs.edit().putString(prefKey, jsonText).apply()
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                if (!projDir.exists()) projDir.mkdirs()
                File(projDir, ".ide_logic_${xmlPage.layoutResourceName}.json")
                    .writeText(jsonText, Charsets.UTF_8)
            }
        }
        // Sync generated Kotlin & Java Activity source code for all enabled events & functions
        val ktSource = generateKotlinActivityFromLogic(project, xmlPage, logicItems)
        val javaSource = generateJavaActivityFromLogic(project, xmlPage, logicItems)
        saveCodeForFlavor(project, xmlPage, CodeEditorFlavor.KOTLIN_ANDROID, ktSource)
        saveCodeForFlavor(project, xmlPage, CodeEditorFlavor.JAVA_ANDROID, javaSource)
    }

    private fun generateKotlinActivityFromLogic(
        project: WorkspaceItem,
        page: ProjectXmlPage,
        logicItems: List<IdeLogicItem>,
    ): String {
        val sb = StringBuilder()
        sb.appendLine("package ${project.packageName}")
        sb.appendLine()
        sb.appendLine("import android.os.Bundle")
        sb.appendLine("import android.view.View")
        sb.appendLine("import android.widget.*")
        sb.appendLine("import androidx.appcompat.app.AppCompatActivity")

        val customImports = logicItems.filter {
            it.section == IdeLogicSection.ACTIVITY && it.name.equals("Import", ignoreCase = true)
        }
        customImports.forEach { imp ->
            if (imp.bodyCode.isNotBlank()) {
                sb.appendLine(imp.bodyCode.trim())
            }
        }
        sb.appendLine()
        sb.appendLine("class ${page.activityName} : AppCompatActivity() {")
        sb.appendLine()

        val onCreateItem = logicItems.firstOrNull {
            it.section == IdeLogicSection.ACTIVITY && it.name.equals("onCreate", ignoreCase = true)
        }
        sb.appendLine("    override fun onCreate(savedInstanceState: Bundle?) {")
        sb.appendLine("        super.onCreate(savedInstanceState)")
        if (onCreateItem != null && onCreateItem.bodyCode.isNotBlank()) {
            onCreateItem.bodyCode.lines().forEach { line ->
                sb.appendLine("        $line")
            }
        } else {
            sb.appendLine("        setContentView(R.layout.${page.layoutResourceName})")
            sb.appendLine("        initializeLogic()")
        }
        sb.appendLine("    }")
        sb.appendLine()

        val initItem = logicItems.firstOrNull {
            it.section == IdeLogicSection.ACTIVITY && it.name.equals("initializeLogic", ignoreCase = true)
        }
        val viewItems = logicItems.filter { it.section == IdeLogicSection.VIEW }
        sb.appendLine("    private fun initializeLogic() {")
        if (initItem != null && initItem.bodyCode.isNotBlank()) {
            initItem.bodyCode.lines().forEach { line ->
                sb.appendLine("        $line")
            }
        }
        viewItems.forEach { vItem ->
            sb.appendLine()
            sb.appendLine("        // Event: ${vItem.name}")
            vItem.bodyCode.lines().forEach { line ->
                sb.appendLine("        $line")
            }
        }
        sb.appendLine("    }")

        // Other Activity lifecycle events
        logicItems.filter {
            it.section == IdeLogicSection.ACTIVITY &&
                !it.name.equals("onCreate", ignoreCase = true) &&
                !it.name.equals("initializeLogic", ignoreCase = true) &&
                !it.name.equals("Import", ignoreCase = true)
        }.forEach { actItem ->
            sb.appendLine()
            sb.appendLine("    // Activity Event: ${actItem.name} (${actItem.subtitle})")
            sb.appendLine("    fun ${actItem.name}() {")
            actItem.bodyCode.lines().forEach { line ->
                sb.appendLine("        $line")
            }
            sb.appendLine("    }")
        }

        // Component & Drawer events
        logicItems.filter {
            it.section == IdeLogicSection.COMPONENT || it.section == IdeLogicSection.DRAWER
        }.forEach { cdItem ->
            val safeMethod = cdItem.name.replace(Regex("[^A-Za-z0-9_]"), "_")
            sb.appendLine()
            sb.appendLine("    // ${cdItem.section.title}: ${cdItem.name}")
            sb.appendLine("    private fun $safeMethod() {")
            cdItem.bodyCode.lines().forEach { line ->
                sb.appendLine("        $line")
            }
            sb.appendLine("    }")
        }

        // Functions
        logicItems.filter { it.section == IdeLogicSection.FUNCTIONS }.forEach { fn ->
            val paramsSig = fn.parameters.joinToString(", ") { "${it.name}: ${it.type}" }
            val retSig = if (fn.returnType.equals("void", ignoreCase = true) || fn.returnType.equals("Unit", ignoreCase = true)) {
                ""
            } else {
                ": ${fn.returnType}"
            }
            sb.appendLine()
            sb.appendLine("    fun ${fn.name}($paramsSig)$retSig {")
            fn.bodyCode.lines().forEach { line ->
                sb.appendLine("        $line")
            }
            sb.appendLine("    }")
        }

        sb.appendLine("}")
        return sb.toString()
    }

    private fun generateJavaActivityFromLogic(
        project: WorkspaceItem,
        page: ProjectXmlPage,
        logicItems: List<IdeLogicItem>,
    ): String {
        val sb = StringBuilder()
        sb.appendLine("package ${project.packageName};")
        sb.appendLine()
        sb.appendLine("import android.os.Bundle;")
        sb.appendLine("import android.view.View;")
        sb.appendLine("import android.widget.*;")
        sb.appendLine("import androidx.appcompat.app.AppCompatActivity;")
        sb.appendLine()
        sb.appendLine("public class ${page.activityName} extends AppCompatActivity {")
        sb.appendLine()
        sb.appendLine("    @Override")
        sb.appendLine("    protected void onCreate(Bundle savedInstanceState) {")
        sb.appendLine("        super.onCreate(savedInstanceState);")
        sb.appendLine("        setContentView(R.layout.${page.layoutResourceName});")
        sb.appendLine("        initializeLogic();")
        sb.appendLine("    }")
        sb.appendLine()
        sb.appendLine("    private void initializeLogic() {")
        logicItems.filter { it.section == IdeLogicSection.VIEW }.forEach { vItem ->
            sb.appendLine("        // Event: ${vItem.name}")
            vItem.bodyCode.lines().forEach { line ->
                sb.appendLine("        $line")
            }
        }
        sb.appendLine("    }")
        sb.appendLine("}")
        return sb.toString()
    }

    private fun parseLogicItemsJson(raw: String): List<IdeLogicItem> = runCatching {
        val array = JSONArray(raw)
        val list = ArrayList<IdeLogicItem>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val section = runCatching {
                IdeLogicSection.valueOf(obj.optString("section", IdeLogicSection.ACTIVITY.name))
            }.getOrDefault(IdeLogicSection.ACTIVITY)
            val paramsArr = obj.optJSONArray("parameters") ?: JSONArray()
            val params = ArrayList<IdeFunctionParam>(paramsArr.length())
            for (j in 0 until paramsArr.length()) {
                val pObj = paramsArr.getJSONObject(j)
                params.add(
                    IdeFunctionParam(
                        name = pObj.optString("name", "param${j + 1}"),
                        type = pObj.optString("type", "String"),
                    ),
                )
            }
            list.add(
                IdeLogicItem(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    section = section,
                    name = obj.optString("name", "onCreate"),
                    subtitle = obj.optString("subtitle", "On activity create"),
                    targetViewId = obj.optString("targetViewId", ""),
                    returnType = obj.optString("returnType", "void"),
                    parameters = params,
                    privateVarName = obj.optString("privateVarName", ""),
                    privateVarType = obj.optString("privateVarType", "String"),
                    privateVarValue = obj.optString("privateVarValue", ""),
                    bodyCode = obj.optString("bodyCode", ""),
                ),
            )
        }
        list
    }.getOrDefault(emptyList())

    /**
     * Reads the Activity code for the selected [xmlPage] from disk (or preferences/default template).
     */
    fun readActivityCode(project: WorkspaceItem, xmlPage: ProjectXmlPage): String {
        val defaultFlavor = if (project.language == ProjectLanguage.JAVA) {
            CodeEditorFlavor.JAVA_ANDROID
        } else {
            CodeEditorFlavor.KOTLIN_ANDROID
        }
        return readCodeForFlavor(project, xmlPage, defaultFlavor)
    }

    /**
     * Saves the edited Activity code for [xmlPage] to disk in `/storage/emulated/0/.IDE Ultimate/...`
     * and local preferences.
     */
    fun saveActivityCode(project: WorkspaceItem, xmlPage: ProjectXmlPage, newCode: String) {
        val defaultFlavor = if (project.language == ProjectLanguage.JAVA) {
            CodeEditorFlavor.JAVA_ANDROID
        } else {
            CodeEditorFlavor.KOTLIN_ANDROID
        }
        saveCodeForFlavor(project, xmlPage, defaultFlavor, newCode)
    }

    private fun activityFileInDir(
        projDir: File,
        project: WorkspaceItem,
        xmlPage: ProjectXmlPage,
    ): File {
        val pkgPath = project.packageName.replace('.', '/')
        return when (project.language) {
            ProjectLanguage.FLUTTER -> File(projDir, "lib/${xmlPage.codeFileName}")
            ProjectLanguage.KOTLIN, ProjectLanguage.JAVA ->
                File(projDir, "app/src/main/java/$pkgPath/${xmlPage.codeFileName}")
        }
    }

    private fun ensureDefaultProjectFiles(project: WorkspaceItem) {
        val defaultPages = DEFAULT_XML_LAYOUTS.map { xmlPageForName(project, it) }
        val pkgPath = project.packageName.replace('.', '/')
        projectDirectoriesFor(project).forEach { projDir ->
            runCatching {
                if (!projDir.exists()) projDir.mkdirs()
                defaultPages.forEach { page ->
                    val layoutFile = File(projDir, "app/src/main/res/layout/${page.xmlFileName}")
                    layoutFile.parentFile?.mkdirs()
                    if (!layoutFile.exists()) {
                        layoutFile.writeText(defaultXmlLayoutContent(project, page), Charsets.UTF_8)
                    }
                    if (project.language == ProjectLanguage.JAVA) {
                        val javaFile = File(projDir, "app/src/main/java/$pkgPath/${page.activityName}.java")
                        javaFile.parentFile?.mkdirs()
                        if (!javaFile.exists()) {
                            javaFile.writeText(defaultJavaActivityContent(project, page), Charsets.UTF_8)
                        }
                    } else {
                        val ktFile = File(projDir, "app/src/main/java/$pkgPath/${page.activityName}.kt")
                        ktFile.parentFile?.mkdirs()
                        if (!ktFile.exists()) {
                            ktFile.writeText(defaultKotlinActivityContent(project, page), Charsets.UTF_8)
                        }
                    }
                }

                // Also generate AppLogger in projDir so it's always available
                if (project.language == ProjectLanguage.JAVA) {
                    val logFile = File(projDir, "app/src/main/java/$pkgPath/AppLogger.java")
                    if (!logFile.exists()) {
                        logFile.parentFile?.mkdirs()
                        logFile.writeText(defaultJavaAppLogger(project), Charsets.UTF_8)
                    }
                } else {
                    val logFile = File(projDir, "app/src/main/java/$pkgPath/AppLogger.kt")
                    if (!logFile.exists()) {
                        logFile.parentFile?.mkdirs()
                        logFile.writeText(defaultKotlinAppLogger(project), Charsets.UTF_8)
                    }
                }
            }
        }
    }

    private fun normalizeXmlFileName(raw: String): String {
        val cleaned = raw.trim()
            .lowercase(Locale.ROOT)
            .removeSuffix(".xml")
            .replace(Regex("[^a-z0-9_]+"), "_")
            .trim('_')
            .ifBlank { "activity_main" }
        val withPrefix = if (
            cleaned.startsWith("activity_") ||
            cleaned.startsWith("fragment_") ||
            cleaned.startsWith("dialog_") ||
            cleaned.startsWith("item_")
        ) {
            cleaned
        } else {
            "activity_$cleaned"
        }
        return "$withPrefix.xml"
    }

    private fun xmlNameToActivityName(xmlFileName: String): String {
        val base = xmlFileName.removeSuffix(".xml")
            .removePrefix("activity_")
            .ifBlank { "main" }
        val pascal = base.split('_', '-')
            .filter { it.isNotBlank() }
            .joinToString("") { part ->
                part.replaceFirstChar { ch -> ch.titlecase(Locale.ROOT) }
            }
            .ifBlank { "Main" }
        return if (pascal.endsWith("Activity")) pascal else "${pascal}Activity"
    }

    fun defaultJavaAppLogger(project: WorkspaceItem): String = """package ${project.packageName};

import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AppLogger for IDE Ultimate Logcat Reader.
 * Automatically broadcasts ALL application logs, crashes, and custom tags
 * directly and live to the IDE Ultimate Logcat Reader screen.
 */
public class AppLogger {
    private static final String ACTION_LOG = "com.androidexl.ideultimate.ACTION_LOG_EVENT";
    private static final String EXTRA_PACKAGE = "extra_package_name";
    private static final String EXTRA_APP_NAME = "extra_app_name";
    private static final String EXTRA_TAG = "extra_tag";
    private static final String EXTRA_LEVEL = "extra_level";
    private static final String EXTRA_MESSAGE = "extra_message";
    private static final String EXTRA_TIMESTAMP = "extra_timestamp";

    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    /**
     * Initialize automatic log capture and crash interception.
     * Call this in your Application class or MainActivity.onCreate()
     */
    public static void init(final Context context) {
        if (context == null || initialized.getAndSet(true)) {
            return;
        }

        final Context appContext = context.getApplicationContext();

        // 1. Intercept uncaught exceptions (crashes) and send immediately to Logcat Reader
        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                try {
                    e(appContext, "AndroidRuntime", "FATAL EXCEPTION: " + t.getName() + "\n" + Log.getStackTraceString(e));
                } catch (Exception ignored) {}
                if (defaultHandler != null) {
                    defaultHandler.uncaughtException(t, e);
                }
            }
        });

        // 2. Start live background logcat reader thread for this process PID
        Thread logThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int myPid = Process.myPid();
                    java.lang.Process process = Runtime.getRuntime().exec(
                        new String[]{"logcat", "-v", "time", "*:V"}
                    );
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                        "^(\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+([VDIWEA])/([^(:|\\s]+)(?:\\(\\s*(\\d+)\\))?:\\s*(.*)$"
                    );

                    while ((line = reader.readLine()) != null) {
                        try {
                            java.util.regex.Matcher matcher = pattern.matcher(line);
                            if (matcher.matches()) {
                                String lvl = matcher.group(2);
                                String tag = matcher.group(3).trim();
                                String linePid = matcher.group(4);
                                String msg = matcher.group(5).trim();

                                if (linePid == null || String.valueOf(myPid).equals(linePid) || msg.contains("${project.packageName}") || tag.contains("${project.packageName}")) {
                                    sendBroadcastDirect(appContext, lvl, tag, msg);
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }
        }, "IDE-Ultimate-AppLogger");
        logThread.setDaemon(true);
        logThread.start();

        i(appContext, "AppLogger", "AppLogger initialized. Live logcat streaming active.");
    }

    public static void v(Context context, String tag, String msg) { send(context, "V", tag, msg); }
    public static void d(Context context, String tag, String msg) { send(context, "D", tag, msg); }
    public static void i(Context context, String tag, String msg) { send(context, "I", tag, msg); }
    public static void w(Context context, String tag, String msg) { send(context, "W", tag, msg); }
    public static void e(Context context, String tag, String msg) { send(context, "E", tag, msg); }
    public static void e(Context context, String tag, String msg, Throwable tr) {
        send(context, "E", tag, msg + "\n" + Log.getStackTraceString(tr));
    }

    public static void send(Context context, String level, String tag, String message) {
        Log.println(levelToPriority(level), tag, message != null ? message : "");
        sendBroadcastDirect(context, level, tag, message);
    }

    private static void sendBroadcastDirect(Context context, String level, String tag, String message) {
        if (context == null) return;
        try {
            Intent intent = new Intent(ACTION_LOG);
            intent.setPackage("com.androidexl.ideultimate");
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.putExtra(EXTRA_PACKAGE, context.getPackageName());
            intent.putExtra(EXTRA_APP_NAME, "${project.name}");
            intent.putExtra(EXTRA_TAG, tag != null ? tag : "App");
            intent.putExtra(EXTRA_LEVEL, level != null ? level : "D");
            intent.putExtra(EXTRA_MESSAGE, message != null ? message : "");
            intent.putExtra(EXTRA_TIMESTAMP, System.currentTimeMillis());
            context.sendBroadcast(intent);
        } catch (Exception ignored) {}
    }

    private static int levelToPriority(String level) {
        if ("V".equals(level)) return Log.VERBOSE;
        if ("D".equals(level)) return Log.DEBUG;
        if ("I".equals(level)) return Log.INFO;
        if ("W".equals(level)) return Log.WARN;
        if ("E".equals(level)) return Log.ERROR;
        return Log.DEBUG;
    }
}
""".trimIndent()

    fun defaultKotlinAppLogger(project: WorkspaceItem): String = """package ${project.packageName}

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AppLogger for IDE Ultimate Logcat Reader.
 * Automatically broadcasts ALL application logs, crashes, and custom tags
 * directly and live to the IDE Ultimate Logcat Reader screen.
 */
object AppLogger {
    private const val ACTION_LOG = "com.androidexl.ideultimate.ACTION_LOG_EVENT"
    private const val EXTRA_PACKAGE = "extra_package_name"
    private const val EXTRA_APP_NAME = "extra_app_name"
    private const val EXTRA_TAG = "extra_tag"
    private const val EXTRA_LEVEL = "extra_level"
    private const val EXTRA_MESSAGE = "extra_message"
    private const val EXTRA_TIMESTAMP = "extra_timestamp"

    private val initialized = AtomicBoolean(false)

    /**
     * Initialize automatic log capture and crash interception.
     * Call this in your Application class or MainActivity.onCreate()
     */
    fun init(context: Context?) {
        if (context == null || initialized.getAndSet(true)) {
            return
        }

        val appContext = context.applicationContext

        // 1. Intercept uncaught exceptions (crashes) and send immediately to Logcat Reader
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                e(appContext, "AndroidRuntime", "FATAL EXCEPTION: " + t.name + "\n" + Log.getStackTraceString(e))
            } catch (ignored: Exception) {}
            defaultHandler?.uncaughtException(t, e)
        }

        // 2. Start live background logcat reader thread for this process PID
        val logThread = Thread({
            try {
                val myPid = Process.myPid()
                val process = Runtime.getRuntime().exec(arrayOf("logcat", "-v", "time", "*:V"))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String? = reader.readLine()
                val pattern = Regex(
                    "^(\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\.\\d{3})\\s+([VDIWEA])/([^(:|\\s]+)(?:\\(\\s*(\\d+)\\))?:\\s*(.*)$"
                )

                while (line != null) {
                    try {
                        val match = pattern.matchEntire(line)
                        if (match != null) {
                            val (_, lvl, tag, linePid, msg) = match.destructured
                            if (linePid.isEmpty() || linePid == myPid.toString() || msg.contains("${project.packageName}") || tag.contains("${project.packageName}")) {
                                sendBroadcastDirect(appContext, lvl, tag.trim(), msg.trim())
                            }
                        }
                    } catch (ignored: Exception) {}
                    line = reader.readLine()
                }
            } catch (ignored: Exception) {}
        }, "IDE-Ultimate-AppLogger")
        logThread.isDaemon = true
        logThread.start()

        i(appContext, "AppLogger", "AppLogger initialized. Live logcat streaming active.")
    }

    fun v(context: Context?, tag: String, msg: String) = send(context, "V", tag, msg)
    fun d(context: Context?, tag: String, msg: String) = send(context, "D", tag, msg)
    fun i(context: Context?, tag: String, msg: String) = send(context, "I", tag, msg)
    fun w(context: Context?, tag: String, msg: String) = send(context, "W", tag, msg)
    fun e(context: Context?, tag: String, msg: String) = send(context, "E", tag, msg)
    fun e(context: Context?, tag: String, msg: String, tr: Throwable?) {
        send(context, "E", tag, msg + "\n" + Log.getStackTraceString(tr))
    }

    fun send(context: Context?, level: String, tag: String, message: String) {
        Log.println(levelToPriority(level), tag, message)
        sendBroadcastDirect(context, level, tag, message)
    }

    private fun sendBroadcastDirect(context: Context?, level: String, tag: String, message: String) {
        if (context == null) return
        runCatching {
            val intent = Intent(ACTION_LOG).apply {
                `package` = "com.androidexl.ideultimate"
                addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                putExtra(EXTRA_PACKAGE, context.packageName)
                putExtra(EXTRA_APP_NAME, "${project.name}")
                putExtra(EXTRA_TAG, tag)
                putExtra(EXTRA_LEVEL, level)
                putExtra(EXTRA_MESSAGE, message)
                putExtra(EXTRA_TIMESTAMP, System.currentTimeMillis())
            }
            context.sendBroadcast(intent)
        }
    }

    private fun levelToPriority(level: String): Int = when (level) {
        "V" -> Log.VERBOSE
        "D" -> Log.DEBUG
        "I" -> Log.INFO
        "W" -> Log.WARN
        "E" -> Log.ERROR
        else -> Log.DEBUG
    }
}
""".trimIndent()

    private fun defaultXmlLayoutContent(
        project: WorkspaceItem,
        page: ProjectXmlPage,
    ): String = """
        <?xml version="1.0" encoding="utf-8"?>
        <LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
            xmlns:tools="http://schemas.android.com/tools"
            android:id="@+id/root_${page.layoutResourceName}"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:orientation="vertical"
            android:gravity="center"
            android:padding="24dp"
            tools:context=".${page.activityName}">

            <TextView
                android:id="@+id/tv_${page.layoutResourceName}_title"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="${project.name} - ${page.activityName}"
                android:textSize="20sp"
                android:textStyle="bold" />

        </LinearLayout>
    """.trimIndent()

    private fun defaultKotlinActivityContent(
        project: WorkspaceItem,
        page: ProjectXmlPage,
    ): String = """
        package ${project.packageName}

        import android.os.Bundle
        import androidx.appcompat.app.AppCompatActivity

        /**
         * Kotlin Android Activity linked to layout: res/layout/${page.xmlFileName}
         */
        class ${page.activityName} : AppCompatActivity() {

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                // Initialize automatic live log streaming & crash interception to IDE Ultimate Logcat Reader
                AppLogger.init(this)

                setContentView(R.layout.${page.layoutResourceName})
            }
        }
    """.trimIndent()

    private fun defaultJavaActivityContent(
        project: WorkspaceItem,
        page: ProjectXmlPage,
    ): String = """
        package ${project.packageName};

        import android.os.Bundle;
        import androidx.appcompat.app.AppCompatActivity;

        /**
         * Java Android Activity linked to layout: res/layout/${page.xmlFileName}
         */
        public class ${page.activityName} extends AppCompatActivity {

            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                // Initialize automatic live log streaming & crash interception to IDE Ultimate Logcat Reader
                AppLogger.init(this);

                setContentView(R.layout.${page.layoutResourceName});
            }
        }
    """.trimIndent()

    private fun defaultActivityCodeContent(
        project: WorkspaceItem,
        page: ProjectXmlPage,
    ): String = when (project.language) {
        ProjectLanguage.KOTLIN -> defaultKotlinActivityContent(project, page)
        ProjectLanguage.JAVA -> defaultJavaActivityContent(project, page)
        ProjectLanguage.FLUTTER -> defaultKotlinActivityContent(project, page)
    }

    /**
     * Synchronizes folders and project metadata onto `/storage/emulated/0/.IDE Ultimate`
     * (and app-scoped `.IDE Ultimate` fallback if raw external storage isn't granted yet).
     */
    fun syncToFileSystem() {
        workspaceRoots().forEach { rootDir ->
            runCatching {
                if (!rootDir.exists()) rootDir.mkdirs()
                items.forEach { item ->
                    val relPath = relativeSegmentsOf(item).joinToString(File.separator)
                    val targetDir = File(rootDir, relPath)
                    if (!targetDir.exists()) targetDir.mkdirs()
                    if (item.type == WorkspaceItemType.FOLDER) {
                        val folderMarker = File(targetDir, ".ide-folder.json")
                        val folderJson = JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("type", "FOLDER")
                        }
                        folderMarker.writeText(folderJson.toString(2), Charsets.UTF_8)
                    } else if (item.type == WorkspaceItemType.PROJECT) {
                        val metaFile = File(targetDir, "ide-project.json")
                        val metaJson = JSONObject().apply {
                            put("id", item.id)
                            put("name", item.name)
                            put("packageName", item.packageName)
                            put("language", item.language.name)
                            put("versionCode", item.versionCode)
                            put("versionName", item.versionName)
                            put("iconPreset", item.iconPreset)
                            put("customIconPath", item.customIconPath ?: JSONObject.NULL)
                            put("createdAt", item.createdAt)
                            put("updatedAt", item.updatedAt)
                            put("path", "$WORKSPACE_DISK_PATH/${relativeSegmentsOf(item).joinToString("/")}")
                        }
                        metaFile.writeText(metaJson.toString(2), Charsets.UTF_8)
                        ensureDefaultProjectFiles(item)
                    }
                }
            }
        }
    }

    private fun savePrefsOnly() {
        val array = JSONArray()
        items.forEach { item ->
            val json = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("type", item.type.name)
                put("parentId", item.parentId ?: JSONObject.NULL)
                put("packageName", item.packageName)
                put("language", item.language.name)
                put("versionCode", item.versionCode)
                put("versionName", item.versionName)
                put("iconPreset", item.iconPreset)
                put("customIconPath", item.customIconPath ?: JSONObject.NULL)
                put("createdAt", item.createdAt)
                put("updatedAt", item.updatedAt)
            }
            array.put(json)
        }
        prefs.edit().putString(KEY_ITEMS, array.toString()).apply()
    }

    private fun persist() {
        savePrefsOnly()
        syncToFileSystem()
    }

    private fun parseItems(raw: String): List<WorkspaceItem> {
        val array = JSONArray(raw)
        val result = ArrayList<WorkspaceItem>(array.length())
        for (index in 0 until array.length()) {
            val json = array.getJSONObject(index)
            val name = json.getString("name")
            val type = runCatching { WorkspaceItemType.valueOf(json.getString("type")) }
                .getOrDefault(WorkspaceItemType.PROJECT)
            val language = runCatching {
                ProjectLanguage.valueOf(json.optString("language", ProjectLanguage.KOTLIN.name))
            }.getOrDefault(ProjectLanguage.KOTLIN)
            val pkg = json.optString("packageName").ifBlank {
                ProjectZipExporter.packageName(name)
            }
            val customIcon = if (json.isNull("customIconPath")) {
                null
            } else {
                json.optString("customIconPath").ifBlank { null }
            }
            result.add(
                WorkspaceItem(
                    id = json.getString("id"),
                    name = name,
                    type = type,
                    parentId = if (json.isNull("parentId")) null else json.optString("parentId").ifBlank { null },
                    packageName = pkg,
                    language = language,
                    versionCode = json.optInt("versionCode", 1).coerceAtLeast(1),
                    versionName = json.optString("versionName", "1.0.0").ifBlank { "1.0.0" },
                    iconPreset = json.optInt("iconPreset", 0),
                    customIconPath = customIcon,
                    createdAt = json.optLong("createdAt"),
                    updatedAt = json.optLong("updatedAt"),
                ),
            )
        }
        return result
    }

    companion object {
        const val WORKSPACE_DISK_PATH = "/storage/emulated/0/.IDE Ultimate"
        private const val KEY_ITEMS = "items_v5"

        private val DEFAULT_XML_LAYOUTS = listOf(
            "activity_main.xml",
            "activity_splash.xml",
            "activity_home.xml",
        )

        private val INTERNAL_PROJECT_DIRS = setOf(
            "app",
            "src",
            "res",
            "lib",
            "gradle",
            "android",
            "ios",
            "build",
            "layout",
            "java",
            "kotlin",
            "values",
            "assets",
            "web",
            "test",
        )
    }
}
