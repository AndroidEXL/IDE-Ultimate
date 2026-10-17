package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.StoreProjectItem
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.GlassIconButton
import com.androidexl.ideultimate.ui.components.GradientButton
import com.androidexl.ideultimate.ui.components.LogoMark
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.components.ProjectScreenshotImage
import com.androidexl.ideultimate.ui.components.savePickedProjectIcon
import com.androidexl.ideultimate.ui.components.savePickedProjectScreenshot
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

private val FormEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

data class StoreProjectFormData(
    val name: String,
    val details: String,
    val customIconPath: String?,
    val iconPreset: Int,
    val downloadLinks: List<String>,
    val imagePaths: List<String>,
)

/**
 * Create / Edit Store Project page opened from User Profile -> Projects FAB (or long-press -> Edit):
 * 1. Top row (LTR): Icon from device on the LEFT + Program Name input on the RIGHT
 * 2. Project Details below
 * 3. Button to add download links below
 * 4. Button to add project images below
 * 5. Save button at the bottom
 */
@Composable
fun StoreProjectFormScreen(
    editingItem: StoreProjectItem? = null,
    onBack: () -> Unit,
    onSave: (StoreProjectFormData) -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val isEdit = editingItem != null

    var name by rememberSaveable { mutableStateOf(editingItem?.name ?: "") }
    var details by rememberSaveable { mutableStateOf(editingItem?.details ?: "") }
    var customIconPath by rememberSaveable { mutableStateOf(editingItem?.customIconPath) }
    var iconPreset by rememberSaveable { mutableIntStateOf(editingItem?.iconPreset ?: 0) }

    val downloadLinks = remember {
        mutableStateListOf<String>().apply {
            editingItem?.downloadLinks?.let { addAll(it) }
        }
    }
    val imagePaths = remember {
        mutableStateListOf<String>().apply {
            editingItem?.imagePaths?.let { addAll(it) }
        }
    }

    var showAddLinkDialog by remember { mutableStateOf(false) }
    var showAddImageUrlDialog by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val iconPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri != null) {
            savePickedProjectIcon(context, uri)?.let { saved ->
                customIconPath = saved
            }
        }
    }

    val imagesPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
    ) { uris ->
        uris.forEach { uri ->
            savePickedProjectScreenshot(context, uri)?.let { saved ->
                imagePaths.add(saved)
            }
        }
    }

    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 620, easing = FormEase))
    }

    BackHandler(onBack = onBack)

    fun handleSave() {
        if (name.trim().isEmpty()) {
            errorText = context.getString(R.string.error_name_empty)
            return
        }
        if (details.trim().isEmpty()) {
            errorText = context.getString(R.string.store_form_error_details)
            return
        }
        errorText = null
        onSave(
            StoreProjectFormData(
                name = name.trim(),
                details = details.trim(),
                customIconPath = customIconPath,
                iconPreset = iconPreset,
                downloadLinks = downloadLinks.toList(),
                imagePaths = imagePaths.toList(),
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
                alpha = enter.value
                translationY = (1f - enter.value) * 36.dp.toPx()
            },
    ) {
        // Header
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
                        if (isEdit) R.string.store_form_edit_title else R.string.store_form_create_title,
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.store_form_subtitle),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                    color = Cyan,
                )
            }
            LogoMark(modifier = Modifier.size(40.dp), animated = false)
        }

        Spacer(Modifier.height(14.dp))

        // 1. Top Row: Icon from device on the LEFT + Program Name on the RIGHT
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // LEFT: Icon from device
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { iconPicker.launch("image/*") },
                    ) {
                        ProjectIconView(
                            iconPreset = iconPreset,
                            customIconPath = customIconPath,
                            modifier = Modifier.size(74.dp),
                            cornerRadius = 22.dp,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.store_form_pick_icon),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = Cyan,
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    // RIGHT: Write Program Name
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Column(Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = {
                                    if (it.length <= 50) {
                                        name = it
                                        errorText = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text(stringResource(R.string.store_form_program_name)) },
                                placeholder = { Text(stringResource(R.string.store_form_program_name_hint)) },
                                shape = RoundedCornerShape(16.dp),
                                colors = storeFieldColors(),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 2. Below: Project Details
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Text(
                text = stringResource(R.string.store_form_details_label),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                color = Ink,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = details,
                onValueChange = {
                    details = it
                    errorText = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
                label = { Text(stringResource(R.string.store_form_details_label)) },
                placeholder = { Text(stringResource(R.string.store_form_details_hint)) },
                shape = RoundedCornerShape(16.dp),
                colors = storeFieldColors(),
            )
        }

        Spacer(Modifier.height(14.dp))

        // 3. Below: Button to add Download Links
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.store_form_links_section),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = Ink,
                )
                Text(
                    text = "${downloadLinks.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        color = Cyan,
                    ),
                )
            }
            Spacer(Modifier.height(10.dp))

            ActionOutlineButton(
                text = stringResource(R.string.store_form_add_link_btn),
                accent = Cyan,
                onClick = { showAddLinkDialog = true },
            )

            if (downloadLinks.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    downloadLinks.forEachIndexed { idx, link ->
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x14FFFFFF))
                                    .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = link,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 10.sp,
                                    ),
                                    color = Cyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Spacer(Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFFF8A8A),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { downloadLinks.removeAt(idx) },
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // 4. Below: Button to add Project Images
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.store_form_images_section),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = Ink,
                )
                Text(
                    text = "${imagePaths.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        color = VioletSoft,
                    ),
                )
            }
            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionOutlineButton(
                    text = stringResource(R.string.store_form_add_images_btn),
                    accent = VioletSoft,
                    modifier = Modifier.weight(1f),
                    onClick = { imagesPicker.launch("image/*") },
                )
                ActionOutlineButton(
                    text = stringResource(R.string.store_form_add_image_url_btn),
                    accent = Teal,
                    onClick = { showAddImageUrlDialog = true },
                )
            }

            if (imagePaths.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    imagePaths.forEachIndexed { idx, imgPath ->
                        Box(modifier = Modifier.size(width = 120.dp, height = 165.dp)) {
                            ProjectScreenshotImage(
                                pathOrUrl = imgPath,
                                modifier = Modifier.fillMaxSize(),
                                cornerRadius = 16.dp,
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC060918))
                                    .clickable { imagePaths.removeAt(idx) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFFF8A8A),
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (errorText != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = errorText.orEmpty(),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = Color(0xFFFF8A8A),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(18.dp))

        // 5. Below: Save Button
        GradientButton(
            text = stringResource(R.string.store_form_save_btn),
            icon = Icons.Filled.Check,
            onClick = { handleSave() },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(34.dp + bottomBarPadding))
    }

    if (showAddLinkDialog) {
        UrlInputDialog(
            title = stringResource(R.string.store_form_add_link_btn),
            hint = "https://github.com/AndroidEXL/IDE-Ultimate/releases/latest",
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
private fun ActionOutlineButton(
    text: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.48f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            color = Color.White,
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
        textContentColor = Muted,
        shape = RoundedCornerShape(24.dp),
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    placeholder = { Text(hint) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = storeFieldColors(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clean = value.trim()
                    if (clean.isNotEmpty()) onConfirm(clean)
                },
            ) {
                Text(
                    text = stringResource(R.string.dialog_create),
                    color = Cyan,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.dialog_cancel),
                    color = Muted,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
    )
}

@Composable
private fun storeFieldColors() = OutlinedTextFieldDefaults.colors(
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
