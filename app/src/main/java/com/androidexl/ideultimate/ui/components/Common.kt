package com.androidexl.ideultimate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.GlassStrokeBottom
import com.androidexl.ideultimate.ui.theme.GlassStrokeTop
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

/** Frosted-glass surface used across the app. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val isDark = LocalIsDarkTheme.current
    val topColor = if (isDark) Color(0x1FFFFFFF) else Color(0xFFFFFFFF)
    val botColor = if (isDark) Color(0x08FFFFFF) else Color(0xF7FFFFFF)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, botColor),
                ),
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(GlassStrokeTop, GlassStrokeBottom)),
                shape = shape,
            )
            .padding(contentPadding),
        content = content,
    )
}

/**
 * Slim gradient progress bar with a glowing head. Fills from the start of the
 * current layout direction, so it grows right-to-left in Arabic.
 */
@Composable
fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = Color(0x1AFFFFFF),
    height: Dp = 5.dp,
) {
    val value = progress.coerceIn(0f, 1f)
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val shape = RoundedCornerShape(percent = 50)

    Box(modifier = modifier.height(height)) {
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .background(trackColor),
        )
        Box(
            Modifier
                .matchParentSize()
                .clip(shape)
                .graphicsLayer {
                    scaleX = value
                    transformOrigin = TransformOrigin(if (rtl) 1f else 0f, 0.5f)
                }
                .background(
                    Brush.horizontalGradient(listOf(Cyan, VioletSoft, Fuchsia)),
                ),
        )
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (value > 0.015f && value < 0.995f) 1f else 0f }
                .drawBehind {
                    val x = size.width * (if (rtl) 1f - value else value)
                    val y = size.height / 2f
                    val halo = size.height * 1.8f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Cyan.copy(alpha = 0.55f),
                                Violet.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                            center = Offset(x, y),
                            radius = halo * 2.4f,
                        ),
                        radius = halo * 2.4f,
                        center = Offset(x, y),
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.95f),
                        radius = size.height * 0.55f,
                        center = Offset(x, y),
                    )
                },
        )
    }
}

/** Primary call-to-action with a violet gradient. */
@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.horizontalGradient(listOf(Violet, Color(0xFF8B5CF6), Color(0xFFD946EF))),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}

/** Compact glass button that only carries an icon. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(shape)
            .background(Color(0x14FFFFFF))
            .border(1.dp, Color(0x26FFFFFF), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Ink,
            modifier = Modifier.size(20.dp),
        )
    }
}
