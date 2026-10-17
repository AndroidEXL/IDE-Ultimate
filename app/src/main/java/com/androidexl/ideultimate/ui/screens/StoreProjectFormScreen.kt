package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.androidexl.ideultimate.data.ProjectLanguage
import com.androidexl.ideultimate.data.StoreItemType
import com.androidexl.ideultimate.data.StoreProjectItem
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.GradientButton
import com.androidexl.ideultimate.ui.components.PROJECT_ICON_PRESETS
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.components.ProjectScreenshotImage
import com.androidexl.ideultimate.ui.components.languageColor
import com.androidexl.ideultimate.ui.components.savePickedProjectIcon
import com.androidexl.ideultimate.ui.components.savePickedProjectScreenshot
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

private val FormEnterCurve = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * Create / Edit Store Item Screen:
 * - Top row (LTR): Device icon picker on the LEFT, Name input on the RIGHT
 * - Item Type Selector:
 *   1. PROJECT (مشروع) -> shows Details + Add Download Links + Add Project Images + Save
 *   2. CODE_SNIPPET (كود برمجي) -> no links & no images; shows Details + Code input below Details + Save
 *   3. LIBRARY (مكتبة خاصة) -> no links & no images; shows Details + Library Code input below Details + Save
 */
@Composable
fun StoreProjectFormScreen(
    editingItem: StoreProjectItem?,
    onBack: () -> Unit,
    onSave: (
        type: StoreItemType,
        name: String,
        details: String,
        codeContent: String,
        customIconPath: String?,
        iconPreset: Int,
        language: ProjectLanguage,
        downloadLinks: List<String>,
        imagePaths: List<String>,
    ) -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val isEditMode = editingItem != null

    BackHandler(onBack = onBack)

    var selectedTypeName by rememberSaveable(editingItem?.id) {
        mutableStateOf((editingItem?.type ?: StoreItemType.PROJECT).name)
    }
    val selectedType = runCatching { StoreItemType.valueOf(selectedTypeName) }
        .getOrDefault(StoreItemType.PROJECT)

    var selectedLanguageName by rememberSaveable(editingItem?.id) {
        mutableStateOf((editingItem?.language ?: ProjectLanguage.KOTLIN).name)
    }
    val selectedLanguage = runCatching { ProjectLanguage.valueOf(selectedLanguageName) }
        .getOrDefault(ProjectLanguage.KOTLIN)

    var programName by rememberSaveable(editingItem?.id) {
        mutableStateOf(editingItem?.name.orEmpty())
    }
    var projectDetails by rememberSaveable(editingItem?.id) {
        mutableStateOf(editingItem?.details.orEmpty())
    }
    var codeContent by rememberSaveable(editingItem?.id) {
        mutableStateOf(editingItem?.codeContent.orEmpty())
    }
    var iconPreset by rememberSaveable(editingItem?.id) {
        mutableIntStateOf(editingItem?.iconPreset ?: 0)
    }
    var customIconPath by rememberSaveable(editingItem?.id) {
        mutableStateOf(editingItem?.customIconPath)
    }

    val downloadLinks = remember(editingItem?.id) {
        mutableStateListOf<String>().apply {
            addAll(editingItem?.downloadLinks ?: emptyList())
        }
    }
    val imagePaths = remember(editingItem?.id) {
        mutableStateListOf<String>().apply {
            addAll(editingItem?.imagePaths ?: emptyList())
        }
    }

    var showAddLinkDialog by remember { mutableStateOf(false) }
    var showAddImageUrlDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val errNameEmpty = stringResource(R.string.error_name_empty)
    val errDetailsEmpty = stringResource(R.string.store_form_error_details)
    val errCodeEmpty = stringResource(R.string.store_form_error_code)

    val pickIconLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val savedPath = savePickedProjectIcon(context, uri)
            if (savedPath != null) {
                customIconPath = savedPath
            }
        }
    }

    val pickScreenshotLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8),
    ) { uris ->
        uris.forEach { uri ->
            val savedPath = savePickedProjectScreenshot(context, uri)
            if (savedPath != null) {
                imagePaths.add(savedPath)
            }
        }
    }

    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 560, easing = FormEnterCurve))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .graphicsLayer {
                alpha = enter.value
                translationY = (1f - enter.value) * 28.dp.toPx()
            },
    ) {
        Spacer(Modifier.height(10.dp))

        // Top Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
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
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (isEditMode) {
                        stringResource(R.string.store_form_edit_title)
                    } else {
                        stringResource(R.string.store_form_create_title)
                    },
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 21.sp),
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.store_form_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 0. Item Type Selector (مشروع | كود برمجي | مكتبة) + Language Selector
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.store_form_type_label),
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.store_form_type_hint),
                style = MaterialTheme.typography.bodySmall,
                color = Muted,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StoreTypeOptionButton(
                    label = stringResource(R.string.store_type_project),
                    selected = selectedType == StoreItemType.PROJECT,
                    accent = Cyan,
                    onClick = {
                        selectedTypeName = StoreItemType.PROJECT.name
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f),
                )
                StoreTypeOptionButton(
                    label = stringResource(R.string.store_type_code),
                    selected = selectedType == StoreItemType.CODE_SNIPPET,
                    accent = VioletSoft,
                    onClick = {
                        selectedTypeName = StoreItemType.CODE_SNIPPET.name
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f),
                )
                StoreTypeOptionButton(
                    label = stringResource(R.string.store_type_library),
                    selected = selectedType == StoreItemType.LIBRARY,
                    accent = Teal,
                    onClick = {
                        selectedTypeName = StoreItemType.LIBRARY.name
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.field_language_title),
                style = MaterialTheme.typography.labelLarge,
                color = Muted,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ProjectLanguage.entries.forEach { lang ->
                    StoreTypeOptionButton(
                        label = lang.displayName,
                        selected = selectedLanguage == lang,
                        accent = languageColor(lang),
                        onClick = { selectedLanguageName = lang.name },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 1. Top Card: Device Icon on the LEFT + Program/Code/Library Name on the RIGHT
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // LEFT: Icon from device
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            ProjectIconView(
                                iconPreset = iconPreset,
                                customIconPath = customIconPath,
                                modifier = Modifier
                                    .size(78.dp)
                                    .clickable {
                                        pickIconLauncher.launch(
                                            PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly,
                                            ),
                                        )
                                    },
                                cornerRadius = 22.dp,
                            )
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Violet, Cyan)))
                                    .border(1.5.dp, Color(0xFF06091A), CircleShape)
                                    .clickable {
                                        pickIconLauncher.launch(
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
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.store_form_pick_icon),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Cyan,
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // RIGHT: Program / Code / Library Name input
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = programName,
                            onValueChange = {
                                programName = it
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    when (selectedType) {
                                        StoreItemType.PROJECT -> stringResource(R.string.store_form_program_name)
                                        StoreItemType.CODE_SNIPPET -> stringResource(R.string.store_form_code_name)
                                        StoreItemType.LIBRARY -> stringResource(R.string.store_form_library_name)
                                    },
                                )
                            },
                            placeholder = { Text(stringResource(R.string.store_form_program_name_hint)) },
                            singleLine = true,
                            colors = storeFormFieldColors(),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Preset icons strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PROJECT_ICON_PRESETS.forEach { preset ->
                    val isSelected = customIconPath == null && iconPreset == preset.id
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) Cyan else Color(0x28FFFFFF),
                                shape = RoundedCornerShape(12.dp),
                            )
                            .clickable {
                                customIconPath = null
                                iconPreset = preset.id
                            }
                            .padding(3.dp),
                    ) {
                        ProjectIconView(
                            iconPreset = preset.id,
                            customIconPath = null,
                            modifier = Modifier.fillMaxSize(),
                            cornerRadius = 9.dp,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 2. Below: Details (تفاصيل المشروع / الكود / المكتبة)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = when (selectedType) {
                    StoreItemType.PROJECT -> stringResource(R.string.store_form_details_label)
                    StoreItemType.CODE_SNIPPET -> stringResource(R.string.store_form_code_details_label)
                    StoreItemType.LIBRARY -> stringResource(R.string.store_form_library_details_label)
                },
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = projectDetails,
                onValueChange = {
                    projectDetails = it
                    errorMessage = null
                },
                placeholder = { Text(stringResource(R.string.store_form_details_hint)) },
                minLines = 4,
                maxLines = 8,
                colors = storeFormFieldColors(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(14.dp))

        if (selectedType == StoreItemType.PROJECT) {
            // 3A. For PROJECT: Button to Add Download Links + Links list
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.store_form_links_section),
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                    )
                    Text(
                        text = "${downloadLinks.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = Cyan,
                    )
                }

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(Violet.copy(alpha = 0.25f), Cyan.copy(alpha = 0.22f))))
                        .border(1.dp, Cyan.copy(alpha = 0.50f), RoundedCornerShape(14.dp))
                        .clickable { showAddLinkDialog = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Cyan,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.store_form_add_link_btn),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Ink,
                        )
                    }
                }

                if (downloadLinks.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        downloadLinks.forEachIndexed { idx, link ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x16070B1E))
                                    .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = link,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 11.sp,
                                    ),
                                    color = Cyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = Fuchsia,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { downloadLinks.removeAt(idx) },
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 4A. For PROJECT: Button to Add Project Images + Image Previews
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.store_form_images_section),
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                    )
                    Text(
                        text = "${imagePaths.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = VioletSoft,
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Violet.copy(alpha = 0.24f))
                            .border(1.dp, VioletSoft.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                            .clickable {
                                pickScreenshotLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = VioletSoft,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.store_form_add_images_btn),
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                                color = Ink,
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Teal.copy(alpha = 0.20f))
                            .border(1.dp, Teal.copy(alpha = 0.50f), RoundedCornerShape(14.dp))
                            .clickable { showAddImageUrlDialog = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.store_form_add_image_url_btn),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = Teal,
                        )
                    }
                }

                if (imagePaths.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        imagePaths.forEachIndexed { idx, path ->
                            Box(
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(100.dp),
                            ) {
                                ProjectScreenshotImage(
                                    pathOrUrl = path,
                                    modifier = Modifier.fillMaxSize(),
                                    cornerRadius = 14.dp,
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xCC06091A))
                                        .clickable { imagePaths.removeAt(idx) },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        tint = Fuchsia,
                                        modifier = Modifier.size(14.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // 3B. For CODE_SNIPPET or LIBRARY: NO links & NO images — Code box directly under Details!
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (selectedType == StoreItemType.CODE_SNIPPET) {
                        stringResource(R.string.store_form_code_box_label)
                    } else {
                        stringResource(R.string.store_form_library_box_label)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (selectedType == StoreItemType.CODE_SNIPPET) {
                        stringResource(R.string.store_form_code_box_hint)
                    } else {
                        stringResource(R.string.store_form_library_box_hint)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )
                Spacer(Modifier.height(10.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    OutlinedTextField(
                        value = codeContent,
                        onValueChange = {
                            codeContent = it
                            errorMessage = null
                        },
                        placeholder = {
                            Text(
                                text = if (selectedType == StoreItemType.CODE_SNIPPET) {
                                    "// Write your code here..."
                                } else {
                                    "implementation(\"com.example:library:1.0.0\")"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = JetBrainsMono,
                                    color = Muted,
                                ),
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            color = Ink,
                        ),
                        minLines = 7,
                        maxLines = 16,
                        colors = storeFormFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Fuchsia.copy(alpha = 0.16f))
                    .border(1.dp, Fuchsia.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Fuchsia,
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        // 5. Bottom: Save Button (زر الحفظ)
        GradientButton(
            text = if (isUploading) "Firebase..." else stringResource(R.string.store_form_save_btn),
            onClick = {
                if (isUploading) return@GradientButton
                val cleanName = programName.trim()
                val cleanDetails = projectDetails.trim()
                val cleanCode = codeContent.trim()
                if (cleanName.isEmpty()) {
                    errorMessage = errNameEmpty
                    return@GradientButton
                }
                if (cleanDetails.isEmpty()) {
                    errorMessage = errDetailsEmpty
                    return@GradientButton
                }
                if (selectedType != StoreItemType.PROJECT && cleanCode.isEmpty()) {
                    errorMessage = errCodeEmpty
                    return@GradientButton
                }
                isUploading = true
                onSave(
                    selectedType,
                    cleanName,
                    cleanDetails,
                    cleanCode,
                    customIconPath,
                    iconPreset,
                    selectedLanguage,
                    downloadLinks.toList(),
                    imagePaths.toList(),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(110.dp + bottomBarPadding))
    }

    if (showAddLinkDialog) {
        UrlInputDialog(
            title = stringResource(R.string.store_form_add_link_btn),
            hint = "https://github.com/...",
            onDismiss = { showAddLinkDialog = false },
            onConfirm = { url ->
                downloadLinks.add(url)
                showAddLinkDialog = false
            },
        )
    }

    if (showAddImageUrlDialog) {
        UrlInputDialog(
            title = stringResource(R.string.store_form_add_image_url_btn),
            hint = "https://example.com/screenshot.png",
            onDismiss = { showAddImageUrlDialog = false },
            onConfirm = { url ->
                imagePaths.add(url)
                showAddImageUrlDialog = false
            },
        )
    }
}

@Composable
private fun StoreTypeOptionButton(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.22f) else Color(0x14070B1E),
        animationSpec = tween(200),
        label = "store-type-bg",
    )
    val border by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.65f) else Color(0x28FFFFFF),
        animationSpec = tween(200),
        label = "store-type-border",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = if (selected) accent else Muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun UrlInputDialog(
    title: String,
    hint: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0D1230),
        titleContentColor = Ink,
        textContentColor = Ink,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                placeholder = { Text(hint) },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMono,
                    color = Ink,
                ),
                colors = storeFormFieldColors(),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = value.trim()
                    if (trimmed.isNotEmpty()) {
                        onConfirm(trimmed)
                    }
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
private fun storeFormFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Cyan,
    unfocusedBorderColor = Color(0x33FFFFFF),
    focusedLabelColor = Cyan,
    unfocusedLabelColor = Muted,
    cursorColor = Cyan,
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    focusedContainerColor = Color(0x16070B1E),
    unfocusedContainerColor = Color(0x16070B1E),
)
