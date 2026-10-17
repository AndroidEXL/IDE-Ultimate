package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.ProjectLanguage
import com.androidexl.ideultimate.data.ProjectZipExporter
import com.androidexl.ideultimate.data.WorkspaceItem
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.GlassIconButton
import com.androidexl.ideultimate.ui.components.GradientButton
import com.androidexl.ideultimate.ui.components.LogoMark
import com.androidexl.ideultimate.ui.components.ProjectIconPresets
import com.androidexl.ideultimate.ui.components.ProjectIconView
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

private val EnterCurve = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

data class ProjectFormResult(
    val name: String,
    val packageName: String,
    val language: ProjectLanguage,
    val versionCode: Int,
    val versionName: String,
    val iconPreset: Int,
    val customIconPath: String?,
)

/**
 * Full-screen Create / Edit Project page with:
 * - Custom icon picker (device gallery + 6 glowing presets)
 * - Project name
 * - Package name (applicationId)
 * - Programming language Spinner (Flutter, Kotlin, Java) — selectable once upon creation, locked on edit
 * - Version Code & Version Name
 * - Live animated preview card & staggered entrance animations
 */
@Composable
fun CreateProjectScreen(
    folderName: String,
    editingProject: WorkspaceItem? = null,
    initialLanguage: ProjectLanguage = ProjectLanguage.KOTLIN,
    initialName: String = "",
    validateName: (String) -> String?,
    onBack: () -> Unit,
    onSubmit: (ProjectFormResult) -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val isEdit = editingProject != null

    var name by rememberSaveable { mutableStateOf(editingProject?.name ?: initialName) }
    var packageTouched by rememberSaveable { mutableStateOf(editingProject != null) }
    var packageName by rememberSaveable {
        mutableStateOf(
            editingProject?.packageName
                ?: if (initialName.isNotBlank()) ProjectZipExporter.packageName(initialName) else "com.ideultimate.projects.myapp",
        )
    }
    var languageName by rememberSaveable {
        mutableStateOf((editingProject?.language ?: initialLanguage).name)
    }
    val language = remember(languageName) {
        runCatching { ProjectLanguage.valueOf(languageName) }.getOrDefault(ProjectLanguage.KOTLIN)
    }
    var versionCodeText by rememberSaveable {
        mutableStateOf((editingProject?.versionCode ?: 1).toString())
    }
    var versionName by rememberSaveable {
        mutableStateOf(editingProject?.versionName ?: "1.0.0")
    }
    var iconPreset by rememberSaveable {
        mutableIntStateOf(
            editingProject?.iconPreset ?: when (initialLanguage) {
                ProjectLanguage.FLUTTER -> 1
                ProjectLanguage.JAVA -> 2
                ProjectLanguage.KOTLIN -> 0
            },
        )
    }
    var customIconPath by rememberSaveable {
        mutableStateOf(editingProject?.customIconPath)
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var spinnerExpanded by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            val savedPath = savePickedProjectIcon(context, uri)
            if (savedPath != null) {
                customIconPath = savedPath
            }
        }
    }

    val enterProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enterProgress.animateTo(1f, tween(durationMillis = 720, easing = EnterCurve))
    }

    val infinite = rememberInfiniteTransition(label = "create-project-orbit")
    val orbitAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbit-angle",
    )
    val glowPulse by infinite.animateFloat(
        initialValue = 0.78f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow-pulse",
    )

    BackHandler(onBack = onBack)

    fun submitForm() {
        val nameErr = validateName(name)
        if (nameErr != null) {
            errorMessage = nameErr
            return
        }
        val cleanPkg = packageName.trim()
        if (!ProjectZipExporter.isValidPackageName(cleanPkg)) {
            errorMessage = context.getString(R.string.error_package_invalid)
            return
        }
        val parsedVersionCode = versionCodeText.trim().toIntOrNull()
        if (parsedVersionCode == null || parsedVersionCode < 1) {
            errorMessage = context.getString(R.string.error_version_code_invalid)
            return
        }
        if (versionName.trim().isEmpty()) {
            errorMessage = context.getString(R.string.error_version_name_empty)
            return
        }
        errorMessage = null
        onSubmit(
            ProjectFormResult(
                name = name.trim(),
                packageName = cleanPkg,
                language = language,
                versionCode = parsedVersionCode,
                versionName = versionName.trim(),
                iconPreset = iconPreset,
                customIconPath = customIconPath,
            ),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .graphicsLayer {
                alpha = enterProgress.value
                translationY = (1f - enterProgress.value) * 42.dp.toPx()
            },
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.workspace_back),
                onClick = onBack,
                modifier = Modifier.size(44.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (isEdit) R.string.create_project_edit_title else R.string.create_project_title,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.workspace_root_inside, folderName),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Cyan,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            LogoMark(modifier = Modifier.size(40.dp), animated = true)
        }

        Spacer(Modifier.height(14.dp))

        // Live Animated Preview Card
        LiveProjectPreviewCard(
            name = name.ifBlank { stringResource(R.string.create_project_preview_default_name) },
            packageName = packageName.ifBlank { "com.ideultimate.projects.app" },
            language = language,
            versionCode = versionCodeText.toIntOrNull()?.coerceAtLeast(1) ?: 1,
            versionName = versionName.ifBlank { "1.0.0" },
            iconPreset = iconPreset,
            customIconPath = customIconPath,
            orbitAngle = orbitAngle,
            glowPulse = glowPulse,
        )

        Spacer(Modifier.height(16.dp))

        // 1. Project Icon Selector Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            SectionTitle(
                step = "01",
                title = stringResource(R.string.field_icon_title),
                subtitle = stringResource(R.string.field_icon_subtitle),
                accent = Cyan,
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProjectIconView(
                    iconPreset = iconPreset,
                    customIconPath = customIconPath,
                    modifier = Modifier.size(64.dp),
                    cornerRadius = 20.dp,
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Cyan.copy(alpha = 0.20f), Violet.copy(alpha = 0.25f)),
                                ),
                            )
                            .border(1.dp, Cyan.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .clickable { imagePicker.launch("image/*") }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.field_icon_pick_gallery),
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                            color = Color.White,
                        )
                    }
                    if (customIconPath != null) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.field_icon_reset_preset),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Cyan,
                            modifier = Modifier.clickable { customIconPath = null },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProjectIconPresets.forEach { preset ->
                    val selected = customIconPath == null && iconPreset == preset.id
                    val borderTint by animateColorAsState(
                        targetValue = if (selected) Cyan else Color(0x22FFFFFF),
                        animationSpec = tween(220),
                        label = "preset-border",
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) Cyan.copy(alpha = 0.14f) else Color(0x0AFFFFFF))
                            .border(1.2.dp, borderTint, RoundedCornerShape(16.dp))
                            .clickable {
                                customIconPath = null
                                iconPreset = preset.id
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        ProjectIconView(
                            iconPreset = preset.id,
                            customIconPath = null,
                            modifier = Modifier.size(40.dp),
                            cornerRadius = 13.dp,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = preset.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (selected) Cyan else Muted,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 2. Project Name & Package Name Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            SectionTitle(
                step = "02",
                title = stringResource(R.string.field_identity_title),
                subtitle = stringResource(R.string.field_identity_subtitle),
                accent = VioletSoft,
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { value ->
                    if (value.length <= 40) {
                        name = value
                        errorMessage = null
                        if (!packageTouched) {
                            packageName = ProjectZipExporter.packageName(value.ifBlank { "myapp" })
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.field_project_name_label)) },
                placeholder = { Text(stringResource(R.string.field_project_name_hint)) },
                shape = RoundedCornerShape(15.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = glassFieldColors(),
            )

            Spacer(Modifier.height(12.dp))

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                OutlinedTextField(
                    value = packageName,
                    onValueChange = { value ->
                        packageTouched = true
                        packageName = value.lowercase().filter {
                            it in 'a'..'z' || it in '0'..'9' || it == '.' || it == '_'
                        }
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(stringResource(R.string.field_package_label)) },
                    placeholder = { Text("com.ideultimate.projects.myapp") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 12.5.sp,
                    ),
                    shape = RoundedCornerShape(15.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        imeAction = ImeAction.Next,
                    ),
                    colors = glassFieldColors(),
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // 3. Programming Language Spinner (Flutter / Kotlin / Java) — set once on creation
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f)) {
                    SectionTitle(
                        step = "03",
                        title = stringResource(R.string.field_language_title),
                        subtitle = stringResource(
                            if (isEdit) R.string.field_language_locked_note else R.string.field_language_once_note,
                        ),
                        accent = languageColor(language),
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            if (isEdit) Color(0x26F59E0B) else Cyan.copy(alpha = 0.14f),
                        )
                        .border(
                            1.dp,
                            if (isEdit) Color(0x66F59E0B) else Cyan.copy(alpha = 0.40f),
                            RoundedCornerShape(percent = 50),
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEdit) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(12.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            text = stringResource(
                                if (isEdit) R.string.badge_locked else R.string.badge_once_only,
                            ),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = if (isEdit) Color(0xFFFBBF24) else Cyan,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            val arrowRotation by animateFloatAsState(
                targetValue = if (spinnerExpanded) 180f else 0f,
                animationSpec = tween(220),
                label = "spinner-arrow",
            )
            val langTint = languageColor(language)

            Box(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    langTint.copy(alpha = if (isEdit) 0.10f else 0.18f),
                                    Color(0x14FFFFFF),
                                ),
                            ),
                        )
                        .border(
                            1.2.dp,
                            langTint.copy(alpha = if (isEdit) 0.35f else 0.65f),
                            RoundedCornerShape(16.dp),
                        )
                        .clickable(enabled = !isEdit) { spinnerExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(langTint.copy(alpha = 0.20f))
                            .border(1.dp, langTint.copy(alpha = 0.50f), RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = when (language) {
                                ProjectLanguage.FLUTTER -> "FL"
                                ProjectLanguage.KOTLIN -> "KT"
                                ProjectLanguage.JAVA -> "JV"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMono,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = langTint,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = language.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            color = Ink,
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = languageDescription(language),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Muted,
                        )
                    }
                    if (isEdit) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Muted,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = langTint,
                            modifier = Modifier
                                .size(26.dp)
                                .rotate(arrowRotation),
                        )
                    }
                }

                DropdownMenu(
                    expanded = spinnerExpanded && !isEdit,
                    onDismissRequest = { spinnerExpanded = false },
                    modifier = Modifier
                        .background(Color(0xFF0E1535))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(14.dp)),
                ) {
                    ProjectLanguage.entries.forEach { option ->
                        val selected = option == language
                        val optionColor = languageColor(option)
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(optionColor),
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = option.displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                            color = if (selected) optionColor else Ink,
                                        )
                                        Text(
                                            text = languageDescription(option),
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = Muted,
                                        )
                                    }
                                    if (selected) {
                                        Spacer(Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = optionColor,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                            },
                            onClick = {
                                languageName = option.name
                                if (customIconPath == null) {
                                    iconPreset = when (option) {
                                        ProjectLanguage.FLUTTER -> 1
                                        ProjectLanguage.JAVA -> 2
                                        ProjectLanguage.KOTLIN -> 0
                                    }
                                }
                                spinnerExpanded = false
                            },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 4. Version Code & Version Name Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            SectionTitle(
                step = "04",
                title = stringResource(R.string.field_version_title),
                subtitle = stringResource(R.string.field_version_subtitle),
                accent = Teal,
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                // Version Code with +/- stepper
                Column(Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = versionCodeText,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(6)
                            versionCodeText = digits
                            errorMessage = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text(stringResource(R.string.field_version_code_label)) },
                        placeholder = { Text("1") },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = JetBrainsMono,
                        ),
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 6.dp),
                            ) {
                                StepperPill(
                                    label = "−",
                                    onClick = {
                                        val current = versionCodeText.toIntOrNull() ?: 1
                                        versionCodeText = (current - 1).coerceAtLeast(1).toString()
                                    },
                                )
                                Spacer(Modifier.width(4.dp))
                                StepperPill(
                                    label = "+",
                                    onClick = {
                                        val current = versionCodeText.toIntOrNull() ?: 1
                                        versionCodeText = (current + 1).toString()
                                    },
                                )
                            }
                        },
                        shape = RoundedCornerShape(15.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                        colors = glassFieldColors(),
                    )
                }

                // Version Name
                Column(Modifier.weight(1f)) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        OutlinedTextField(
                            value = versionName,
                            onValueChange = { value ->
                                if (value.length <= 20) {
                                    versionName = value
                                    errorMessage = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text(stringResource(R.string.field_version_name_label)) },
                            placeholder = { Text("1.0.0") },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = JetBrainsMono,
                            ),
                            shape = RoundedCornerShape(15.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Done,
                            ),
                            colors = glassFieldColors(),
                        )
                    }
                }
            }
        }

        if (errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x26FF5C5C))
                    .border(1.dp, Color(0x55FF5C5C), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                    color = Color(0xFFFFB4B4),
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        GradientButton(
            text = stringResource(
                if (isEdit) R.string.create_project_save_btn else R.string.create_project_submit_btn,
            ),
            icon = if (isEdit) Icons.Filled.Check else Icons.Filled.Add,
            onClick = { submitForm() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(32.dp + bottomBarPadding))
    }
}

@Composable
private fun LiveProjectPreviewCard(
    name: String,
    packageName: String,
    language: ProjectLanguage,
    versionCode: Int,
    versionName: String,
    iconPreset: Int,
    customIconPath: String?,
    orbitAngle: Float,
    glowPulse: Float,
) {
    val langAccent = languageColor(language)
    val shape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        langAccent.copy(alpha = 0.20f * glowPulse),
                        Violet.copy(alpha = 0.18f),
                        Color(0xFF0A0F26),
                    ),
                ),
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(langAccent.copy(alpha = 0.65f), VioletSoft.copy(alpha = 0.25f)),
                ),
                shape = shape,
            )
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    val radius = size.minDimension * 0.48f
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                langAccent.copy(alpha = 0.75f),
                                Color.Transparent,
                                VioletSoft.copy(alpha = 0.55f),
                                Color.Transparent,
                            ),
                        ),
                        radius = radius,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
                    )
                    val rad = Math.toRadians(orbitAngle.toDouble())
                    val dotX = center.x + (radius * kotlin.math.cos(rad)).toFloat()
                    val dotY = center.y + (radius * kotlin.math.sin(rad)).toFloat()
                    drawCircle(
                        color = langAccent,
                        radius = 3.5.dp.toPx(),
                        center = Offset(dotX, dotY),
                    )
                }
                ProjectIconView(
                    iconPreset = iconPreset,
                    customIconPath = customIconPath,
                    modifier = Modifier.size(54.dp),
                    cornerRadius = 17.dp,
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(langAccent.copy(alpha = 0.18f))
                            .border(1.dp, langAccent.copy(alpha = 0.48f), RoundedCornerShape(percent = 50))
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = language.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = langAccent,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(percent = 50))
                            .background(Color(0x19FFFFFF))
                            .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(percent = 50))
                            .padding(horizontal = 9.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "v$versionName ($versionCode)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 9.5.sp,
                            ),
                            color = Ink,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = packageName,
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
    }
}

@Composable
private fun SectionTitle(
    step: String,
    title: String,
    subtitle: String,
    accent: Color,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(accent.copy(alpha = 0.16f))
                .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                ),
                color = accent,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = Ink,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = Muted,
            )
        }
    }
}

@Composable
private fun StepperPill(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Cyan.copy(alpha = 0.16f))
            .border(1.dp, Cyan.copy(alpha = 0.38f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
            ),
            color = Cyan,
        )
    }
}

@Composable
private fun languageDescription(language: ProjectLanguage): String = when (language) {
    ProjectLanguage.FLUTTER -> stringResource(R.string.lang_desc_flutter)
    ProjectLanguage.KOTLIN -> stringResource(R.string.lang_desc_kotlin)
    ProjectLanguage.JAVA -> stringResource(R.string.lang_desc_java)
}

@Composable
private fun glassFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Ink,
    unfocusedTextColor = Ink,
    cursorColor = Cyan,
    focusedBorderColor = Cyan.copy(alpha = 0.85f),
    unfocusedBorderColor = Color(0x33FFFFFF),
    focusedLabelColor = Cyan,
    unfocusedLabelColor = Muted,
    focusedContainerColor = Color(0x16FFFFFF),
    unfocusedContainerColor = Color(0x0BFFFFFF),
    focusedPlaceholderColor = Muted.copy(alpha = 0.55f),
    unfocusedPlaceholderColor = Muted.copy(alpha = 0.55f),
)
