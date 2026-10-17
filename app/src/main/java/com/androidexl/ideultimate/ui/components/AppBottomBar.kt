package com.androidexl.ideultimate.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft

enum class AppTab { Home, Workspace }

/** Height reserved at the bottom of scrollable content so the bar never covers it. */
val AppBottomBarSpace: Dp = 84.dp

/**
 * Floating glass navigation bar with two destinations: Home and Workspace.
 * The selected destination gets a violet gradient pill.
 */
@Composable
fun AppBottomBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(24.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(listOf(Color(0xF20C1230), Color(0xF2060A1B))),
            )
            .border(1.dp, Color(0x26FFFFFF), shape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        BottomBarItem(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.nav_workspace),
            selected = selected == AppTab.Workspace,
            onClick = { onSelect(AppTab.Workspace) },
        ) {
            FolderNavIcon(modifier = Modifier.size(19.dp))
        }
        BottomBarItem(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.nav_home),
            selected = selected == AppTab.Home,
            onClick = { onSelect(AppTab.Home) },
        ) {
            Icon(
                imageVector = Icons.Filled.Home,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(19.dp),
            )
        }
    }
}

@Composable
private fun BottomBarItem(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Muted,
        animationSpec = tween(220),
        label = "tab-color",
    )
    val background by animateColorAsState(
        targetValue = if (selected) Violet.copy(alpha = 0.55f) else Color.Transparent,
        animationSpec = tween(220),
        label = "tab-bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) VioletSoft.copy(alpha = 0.55f) else Color.Transparent,
        animationSpec = tween(220),
        label = "tab-border",
    )
    val shape = RoundedCornerShape(19.dp)

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(background)
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        androidx.compose.runtime.CompositionLocalProvider(LocalContentColor provides contentColor) {
            icon()
        }
        Spacer(Modifier.width(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.5.sp),
            color = contentColor,
            maxLines = 1,
        )
    }
}

/** Compact folder glyph used for Workspace entries (material-icons-core has no folder). */
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
