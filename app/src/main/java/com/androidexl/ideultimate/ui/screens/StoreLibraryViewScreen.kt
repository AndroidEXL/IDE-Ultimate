package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.StoreProjectItem
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.ProjectIconView
import com.androidexl.ideultimate.ui.components.languageColor
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet

private val LibraryViewEnterCurve = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/**
 * Dedicated Read-Only View Screen for a Custom Library (`StoreItemType.LIBRARY`):
 * - Top: Back button + Save for Later (حفظ لاحقًا)
 * - Header card (LTR): Icon on the left + Library Name & Language badge on the right
 * - Centered above Details: Like / Dislike buttons (أعجبني / لم يعجبني)
 * - Read-only Details card
 * - Below Details: Read-only Library Code / Dependency viewer card
 * - Bottom FAB: Copy Library Code
 */
@Composable
fun StoreLibraryViewScreen(
    item: StoreProjectItem,
    onBack: () -> Unit,
    onToggleSaveLater: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleDislike: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBarPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)

    val enter = remember { Animatable(0f) }
    LaunchedEffect(item.id) {
        enter.animateTo(1f, tween(durationMillis = 560, easing = LibraryViewEnterCurve))
    }

    val saveBtnBg by animateColorAsState(
        targetValue = if (item.savedInCollection) Teal.copy(alpha = 0.25f) else Color(0x1EFFFFFF),
        animationSpec = tween(220),
        label = "lib-save-later-bg",
    )
    val saveBtnBorder by animateColorAsState(
        targetValue = if (item.savedInCollection) Teal.copy(alpha = 0.70f) else Color(0x36FFFFFF),
        animationSpec = tween(220),
        label = "lib-save-later-border",
    )

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
                    translationY = (1f - enter.value) * 28.dp.toPx()
                },
        ) {
            Spacer(Modifier.height(10.dp))

            // Top Bar: Back button + Title + "حفظ لاحقًا"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = stringResource(R.string.library_view_screen_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = Ink,
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(saveBtnBg)
                        .border(1.dp, saveBtnBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onToggleSaveLater)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (item.savedInCollection) {
                            stringResource(R.string.store_saved_in_collection)
                        } else {
                            stringResource(R.string.store_save_later)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        ),
                        color = if (item.savedInCollection) Teal else Ink,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Header Card (LTR): Icon on LEFT + Library Name on RIGHT
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProjectIconView(
                            iconPreset = item.iconPreset,
                            customIconPath = item.customIconPath,
                            modifier = Modifier.size(76.dp),
                            cornerRadius = 22.dp,
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                                color = Ink,
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                val langColor = languageColor(item.language)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(langColor.copy(alpha = 0.20f))
                                        .border(1.dp, langColor.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = item.language.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = langColor,
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Teal.copy(alpha = 0.18f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.store_type_library),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Teal,
                                    )
                                }
                            }
                            if (item.authorName.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "by ${item.authorName}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Muted,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Centered above Details: Like / Dislike buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReactionChipButton(
                    label = stringResource(R.string.store_btn_like),
                    count = item.likesCount,
                    selected = item.userReaction == 1,
                    accent = Teal,
                    symbol = "👍",
                    onClick = onToggleLike,
                )
                Spacer(Modifier.width(12.dp))
                ReactionChipButton(
                    label = stringResource(R.string.store_btn_dislike),
                    count = item.dislikesCount,
                    selected = item.userReaction == -1,
                    accent = Fuchsia,
                    symbol = "👎",
                    onClick = onToggleDislike,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Read-Only Library Details Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.store_form_library_details_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = Cyan,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.details,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = Ink,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Below Details: Read-Only Library Code / Dependency Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.store_form_library_box_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = Teal,
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Teal.copy(alpha = 0.18f))
                            .border(1.dp, Teal.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                            .clickable {
                                copyCodeToClipboard(context, item.name, item.codeContent)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.store_copy_code),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Teal,
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF06091A))
                            .border(1.dp, Color(0x28FFFFFF), RoundedCornerShape(14.dp))
                            .horizontalScroll(rememberScrollState())
                            .padding(14.dp),
                    ) {
                        Text(
                            text = item.codeContent,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = JetBrainsMono,
                                fontSize = 12.sp,
                                lineHeight = 19.sp,
                            ),
                            color = Teal,
                        )
                    }
                }
            }

            Spacer(Modifier.height(120.dp + bottomBarPadding))
        }

        // Bottom FAB: Copy Library Code
        FloatingActionButton(
            onClick = { copyCodeToClipboard(context, item.name, item.codeContent) },
            containerColor = Color.Transparent,
            contentColor = Ink,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 22.dp + bottomBarPadding),
        ) {
            Box(
                modifier = Modifier
                    .height(58.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Teal, Violet, Cyan)))
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(20.dp))
                    .padding(horizontal = 22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "📦  " + stringResource(R.string.store_copy_code),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    color = Ink,
                )
            }
        }
    }
}
