package com.androidexl.ideultimate.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.StoreProjectItem
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.GlassIconButton
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.components.ProjectScreenshotImage
import com.androidexl.ideultimate.ui.components.languageColor
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

private val ViewEase = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * Read-only Store Project view screen with the same layout structure as Create Store Project:
 * - Top bar with "حفظ لاحقًا" (Save for later to Collection) button
 * - Top card (LTR): Icon on the LEFT + Program Name on the RIGHT (read-only)
 * - Centered above Details: "أعجبني" (Like) and "لم يعجبني" (Dislike) buttons
 * - Read-only Project Details card
 * - Below Details: Project Images gallery; tapping any image opens it full-screen + link opener
 * - Bottom FAB: Download button ("زر التحميل")
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreProjectViewScreen(
    project: StoreProjectItem,
    onBack: () -> Unit,
    onToggleSaveLater: () -> Unit,
    onLike: () -> Unit,
    onDislike: () -> Unit,
    onDownloadZip: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    var fullScreenImage by remember { mutableStateOf<String?>(null) }
    var showDownloadSheet by remember { mutableStateOf(false) }

    val enter = remember { Animatable(0f) }
    LaunchedEffect(project.id) {
        enter.snapTo(0f)
        enter.animateTo(1f, tween(durationMillis = 600, easing = ViewEase))
    }

    BackHandler {
        if (fullScreenImage != null) {
            fullScreenImage = null
        } else {
            onBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 32.dp.toPx()
                },
        ) {
            // Top bar with "حفظ لاحقًا" button
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
                        text = project.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${project.authorName} • ${project.language.displayName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = languageColor(project.language),
                    )
                }

                // "حفظ لاحقًا" (Save for later) button at the top
                SaveLaterTopButton(
                    saved = project.savedInCollection,
                    onClick = onToggleSaveLater,
                )
            }

            Spacer(Modifier.height(14.dp))

            // 1. Top Card (same layout as Create page: Icon on the LEFT, Program Name on the RIGHT, read-only)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProjectIconView(
                            iconPreset = project.iconPreset,
                            customIconPath = project.customIconPath,
                            modifier = Modifier.size(74.dp),
                            cornerRadius = 22.dp,
                        )
                        Spacer(Modifier.width(14.dp))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.store_form_program_name),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Muted,
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0x14FFFFFF))
                                        .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(14.dp))
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                ) {
                                    Text(
                                        text = project.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                        color = Ink,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 2. Centered Like / Dislike buttons right ABOVE the Details element
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReactionPillButton(
                    label = stringResource(R.string.store_btn_like),
                    count = project.likesCount,
                    selected = project.userReaction == 1,
                    accent = Teal,
                    isLike = true,
                    onClick = onLike,
                )
                Spacer(Modifier.width(12.dp))
                ReactionPillButton(
                    label = stringResource(R.string.store_btn_dislike),
                    count = project.dislikesCount,
                    selected = project.userReaction == -1,
                    accent = Color(0xFFFF6B6B),
                    isLike = false,
                    onClick = onDislike,
                )
            }

            Spacer(Modifier.height(12.dp))

            // 3. Project Details (Read-Only)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.store_form_details_label),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = Cyan,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = project.details,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = Ink,
                )
            }

            Spacer(Modifier.height(14.dp))

            // 4. Below Details: Image Display Elements (displays actual images; tap opens full-screen)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.store_view_images_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                    color = Ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.store_view_images_hint),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Muted,
                )
                Spacer(Modifier.height(12.dp))

                if (project.imagePaths.isEmpty()) {
                    Text(
                        text = stringResource(R.string.store_view_no_images),
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        project.imagePaths.forEach { imgPath ->
                            ProjectScreenshotImage(
                                pathOrUrl = imgPath,
                                modifier = Modifier
                                    .width(168.dp)
                                    .height(220.dp)
                                    .clickable { fullScreenImage = imgPath },
                                cornerRadius = 18.dp,
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(120.dp + bottomBarPadding))
        }

        // 5. Bottom FAB: Download Button ("وتحت في زر fab زر التحميل")
        DownloadFabButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 18.dp, bottom = 18.dp + bottomBarPadding),
            onClick = { showDownloadSheet = true },
        )
    }

    // Full-screen image viewer dialog when any project image is tapped
    val activeImage = fullScreenImage
    if (activeImage != null) {
        Dialog(
            onDismissRequest = { fullScreenImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xF2040714))
                    .padding(20.dp),
            ) {
                ProjectScreenshotImage(
                    pathOrUrl = activeImage,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 48.dp),
                    cornerRadius = 24.dp,
                    contentScale = ContentScale.Fit,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GlassIconButton(
                        icon = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.dialog_cancel),
                        onClick = { fullScreenImage = null },
                        modifier = Modifier.size(44.dp),
                    )
                }
            }
        }
    }

    if (showDownloadSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showDownloadSheet = false },
            sheetState = sheetState,
            containerColor = Midnight,
            contentColor = Ink,
            scrimColor = Color(0xD903060F),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.store_download_sheet_title, project.name),
                    style = MaterialTheme.typography.titleLarge,
                    color = Ink,
                )
                Text(
                    text = stringResource(R.string.store_download_sheet_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted,
                )

                project.downloadLinks.forEachIndexed { idx, link ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Cyan.copy(alpha = 0.14f))
                            .border(1.dp, Cyan.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                            .clickable {
                                showDownloadSheet = false
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                                }.onFailure {
                                    Toast.makeText(context, link, Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DownloadArrowGlyph(Modifier.size(22.dp), tint = Cyan)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.store_download_link_item, idx + 1),
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.5.sp),
                                color = Ink,
                            )
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Text(
                                    text = link,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = JetBrainsMono,
                                        fontSize = 9.5.sp,
                                    ),
                                    color = Cyan,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                // Direct ZIP export / download option (always available!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Violet.copy(alpha = 0.22f))
                        .border(1.dp, VioletSoft.copy(alpha = 0.50f), RoundedCornerShape(16.dp))
                        .clickable {
                            showDownloadSheet = false
                            onDownloadZip()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DownloadArrowGlyph(Modifier.size(22.dp), tint = VioletSoft)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.store_download_zip_direct),
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.5.sp),
                            color = Color.White,
                        )
                        Text(
                            text = stringResource(R.string.action_export_desc),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Muted,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun SaveLaterTopButton(
    saved: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (saved) Cyan.copy(alpha = 0.24f) else Color(0x16FFFFFF),
        animationSpec = tween(200),
        label = "save-later-bg",
    )
    val border by animateColorAsState(
        targetValue = if (saved) Cyan else Color(0x33FFFFFF),
        animationSpec = tween(200),
        label = "save-later-border",
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = if (saved) Cyan else Muted,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(
                if (saved) R.string.store_saved_in_collection else R.string.store_save_later,
            ),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
            color = if (saved) Color.White else Ink,
        )
    }
}

@Composable
private fun ReactionPillButton(
    label: String,
    count: Int,
    selected: Boolean,
    accent: Color,
    isLike: Boolean,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent.copy(alpha = 0.24f) else Color(0x14FFFFFF),
        animationSpec = tween(200),
        label = "react-bg",
    )
    val border by animateColorAsState(
        targetValue = if (selected) accent else Color(0x28FFFFFF),
        animationSpec = tween(200),
        label = "react-border",
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(bg)
            .border(1.2.dp, border, RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (isLike) "👍" else "👎",
            fontSize = 13.sp,
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
            color = if (selected) Color.White else Ink,
        )
        Spacer(Modifier.width(7.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = JetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            ),
            color = if (selected) accent else Muted,
        )
    }
}

@Composable
private fun DownloadFabButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .height(58.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(Cyan, Violet, Color(0xFFD946EF)),
                ),
            )
            .border(1.2.dp, Color(0x66FFFFFF), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DownloadArrowGlyph(Modifier.size(22.dp), tint = Color.White)
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.store_fab_download),
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = Color.White,
        )
    }
}

@Composable
fun DownloadArrowGlyph(modifier: Modifier = Modifier, tint: Color = Color.White) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.11f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Vertical shaft
        drawLine(
            color = tint,
            start = Offset(w * 0.50f, h * 0.16f),
            end = Offset(w * 0.50f, h * 0.66f),
            strokeWidth = w * 0.11f,
            cap = StrokeCap.Round,
        )
        // Arrow head
        val head = Path().apply {
            moveTo(w * 0.28f, h * 0.46f)
            lineTo(w * 0.50f, h * 0.68f)
            lineTo(w * 0.72f, h * 0.46f)
        }
        drawPath(head, color = tint, style = stroke)
        // Tray
        val tray = Path().apply {
            moveTo(w * 0.20f, h * 0.72f)
            lineTo(w * 0.20f, h * 0.84f)
            lineTo(w * 0.80f, h * 0.84f)
            lineTo(w * 0.80f, h * 0.72f)
        }
        drawPath(tray, color = tint, style = stroke)
    }
}
