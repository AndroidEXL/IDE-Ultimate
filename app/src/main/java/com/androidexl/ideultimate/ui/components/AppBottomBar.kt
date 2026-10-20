package com.androidexl.ideultimate.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Midnight
import com.androidexl.ideultimate.ui.theme.IndigoDeep
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.GlassStrokeTop
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

enum class AppTab { ProjectManager, StoreCode }

/** Height reserved at the bottom of scrollable content so the bar never covers it. */
val AppBottomBarSpace: Dp = 88.dp

/**
 * Floating glass bottom navigation bar with 2 tabs:
 * - Project Manager
 * - Store Code
 */
@Composable
fun AppBottomBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(26.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .clip(shape)
            .background(Midnight)
            .border(
                width = 1.2.dp,
                color = GlassStrokeTop,
                shape = shape,
            )
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BottomBarItem(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.nav_project_manager),
            selected = selected == AppTab.ProjectManager,
            accent = Cyan,
            onClick = { onSelect(AppTab.ProjectManager) },
        ) {
            FolderNavIcon(modifier = Modifier.size(19.dp))
        }
        BottomBarItem(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.nav_store_code),
            selected = selected == AppTab.StoreCode,
            accent = VioletSoft,
            onClick = { onSelect(AppTab.StoreCode) },
        ) {
            StoreCodeNavIcon(modifier = Modifier.size(19.dp))
        }
    }
}

@Composable
private fun BottomBarItem(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) Ink else Muted,
        animationSpec = tween(220),
        label = "tab-color",
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.0f else 0.96f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tab-scale",
    )
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .height(50.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(Violet.copy(alpha = 0.65f), accent.copy(alpha = 0.45f)),
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                },
            )
            .border(
                width = 1.dp,
                color = if (selected) accent.copy(alpha = 0.65f) else Color.Transparent,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            icon()
        }
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMono,
                fontSize = 11.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            ),
            color = contentColor,
            maxLines = 1,
        )
    }
}

/** Compact folder glyph used for Project Manager tab. */
@Composable
fun FolderNavIcon(modifier: Modifier = Modifier) {
    val tint = LocalContentColor.current
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.02f, h * 0.16f),
            size = Size(w * 0.42f, h * 0.30f),
            cornerRadius = CornerRadius(w * 0.09f, w * 0.09f),
        )
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.02f, h * 0.32f),
            size = Size(w * 0.96f, h * 0.56f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(w * 0.12f, h * 0.44f),
            size = Size(w * 0.42f, h * 0.07f),
            cornerRadius = CornerRadius(h * 0.04f, h * 0.04f),
        )
    }
}

/** Code brackets `</>` glyph used for Store Code tab. */
@Composable
fun StoreCodeNavIcon(modifier: Modifier = Modifier) {
    val tint = LocalContentColor.current
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = w * 0.11f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val leftBracket = Path().apply {
            moveTo(w * 0.34f, h * 0.24f)
            lineTo(w * 0.10f, h * 0.50f)
            lineTo(w * 0.34f, h * 0.76f)
        }
        val rightBracket = Path().apply {
            moveTo(w * 0.66f, h * 0.24f)
            lineTo(w * 0.90f, h * 0.50f)
            lineTo(w * 0.66f, h * 0.76f)
        }
        drawPath(leftBracket, color = tint, style = stroke)
        drawPath(rightBracket, color = tint, style = stroke)
        drawLine(
            color = tint,
            start = Offset(w * 0.56f, h * 0.20f),
            end = Offset(w * 0.44f, h * 0.80f),
            strokeWidth = w * 0.10f,
            cap = StrokeCap.Round,
        )
    }
}
