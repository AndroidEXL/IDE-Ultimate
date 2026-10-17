package com.androidexl.ideultimate.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.ui.components.GlassCard
import com.androidexl.ideultimate.ui.components.FolderNavIcon
import com.androidexl.ideultimate.ui.components.GlassIconButton
import com.androidexl.ideultimate.ui.components.GradientButton
import com.androidexl.ideultimate.ui.components.LogoMark
import com.androidexl.ideultimate.ui.components.Starfield
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Lilac
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft
import androidx.compose.runtime.CompositionLocalProvider

private val HomeEnterEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

/** Dashboard shown after the splash screen. */
@Composable
fun HomeScreen(
    onReplaySplash: () -> Unit,
    onOpenWorkspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val enter = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        enter.animateTo(1f, tween(durationMillis = 850, easing = HomeEnterEasing))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy),
    ) {
        Image(
            painter = painterResource(R.drawable.splash_nebula),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.5f,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to DeepNavy.copy(alpha = 0.72f),
                        0.45f to DeepNavy.copy(alpha = 0.9f),
                        1f to DeepNavy,
                    ),
                ),
        )
        Starfield(
            modifier = Modifier.fillMaxSize(),
            starCount = 55,
            seed = 21,
            comet = false,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .safeDrawingPadding()
                .padding(horizontal = 22.dp)
                .graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * 48.dp.toPx()
                },
        ) {
            Spacer(Modifier.height(8.dp))

            Header(
                onSettings = { toast(context, R.string.home_soon) },
                onOpenWorkspace = onOpenWorkspace,
            )

            Spacer(Modifier.height(28.dp))

            Text(
                text = stringResource(R.string.home_greeting).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    letterSpacing = 2.5.sp,
                    fontWeight = FontWeight.Medium,
                ),
                color = Cyan,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.displayLarge.copy(
                    brush = Brush.linearGradient(listOf(Color.White, Lilac, Cyan)),
                ),
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted,
            )

            Spacer(Modifier.height(26.dp))

            WorkspaceCard(
                onStart = onOpenWorkspace,
                onReplaySplash = onReplaySplash,
            )

            Spacer(Modifier.height(30.dp))

            Text(
                text = stringResource(R.string.home_tools),
                style = MaterialTheme.typography.titleLarge,
                color = Ink,
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FeatureCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    icon = Icons.Filled.Create,
                    tint = Cyan,
                    titleRes = R.string.feature_editor,
                    descriptionRes = R.string.feature_editor_desc,
                )
                FeatureCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    icon = Icons.Filled.PlayArrow,
                    tint = VioletSoft,
                    titleRes = R.string.feature_preview,
                    descriptionRes = R.string.feature_preview_desc,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FeatureCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    icon = Icons.Filled.Star,
                    tint = Fuchsia,
                    titleRes = R.string.feature_plugins,
                    descriptionRes = R.string.feature_plugins_desc,
                )
                FeatureCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    icon = Icons.Filled.Lock,
                    tint = Teal,
                    titleRes = R.string.feature_build,
                    descriptionRes = R.string.feature_build_desc,
                )
            }

            Spacer(Modifier.height(34.dp))

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color(0x33FFFFFF), Color.Transparent),
                        ),
                    ),
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.home_footer),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Muted.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onReplaySplash) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = Cyan,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.home_replay_splash),
                        style = MaterialTheme.typography.labelMedium,
                        color = Cyan,
                    )
                }
            }

            Spacer(Modifier.height(104.dp))
        }
    }

}

@Composable
private fun Header(
    onSettings: () -> Unit,
    onOpenWorkspace: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogoMark(
            modifier = Modifier.size(42.dp),
            animated = true,
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
            )
            Text(
                text = stringResource(R.string.home_version),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Muted,
            )
        }
        WorkspaceShortcut(onClick = onOpenWorkspace)
        Spacer(Modifier.width(8.dp))
        GlassIconButton(
            icon = Icons.Filled.Settings,
            contentDescription = stringResource(R.string.home_settings),
            onClick = onSettings,
            modifier = Modifier.size(44.dp),
        )
    }
}

@Composable
private fun WorkspaceShortcut(onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(shape)
            .background(Cyan.copy(alpha = 0.12f))
            .border(1.dp, Cyan.copy(alpha = 0.32f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        FolderNavIcon(
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun WorkspaceCard(
    onStart: () -> Unit,
    onReplaySplash: () -> Unit,
) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(listOf(Violet, Color(0xFF4C1D95)))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.home_new_project),
                    style = MaterialTheme.typography.titleMedium,
                    color = Ink,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Kotlin • Compose • 1.16.0",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = Muted,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Cyan.copy(alpha = 0.12f))
                    .border(1.dp, Cyan.copy(alpha = 0.35f), RoundedCornerShape(percent = 50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "NEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 8.sp,
                        letterSpacing = 1.sp,
                    ),
                    color = Cyan,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.home_new_project_desc),
            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
            color = Muted,
        )

        Spacer(Modifier.height(14.dp))

        CodePreview(Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            GradientButton(
                text = stringResource(R.string.home_start),
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.PlayArrow,
                onClick = onStart,
            )
            Spacer(Modifier.width(10.dp))
            GlassIconButton(
                icon = Icons.Filled.Refresh,
                contentDescription = stringResource(R.string.home_replay_splash),
                onClick = onReplaySplash,
            )
        }
    }
}

@Composable
private fun CodePreview(modifier: Modifier = Modifier) {
    val code = buildAnnotatedString {
        withStyle(SpanStyle(color = Fuchsia)) { append("package") }
        append(" com.ideultimate.app\n\n")
        withStyle(SpanStyle(color = VioletSoft)) { append("@Composable") }
        append("\n")
        withStyle(SpanStyle(color = Fuchsia)) { append("fun") }
        append(" ")
        withStyle(SpanStyle(color = Cyan)) { append("App") }
        append("() {\n    ")
        withStyle(SpanStyle(color = Cyan)) { append("Text") }
        append("(")
        withStyle(SpanStyle(color = Teal)) { append("\"Hello, Kotlin!\"") }
        append(")\n}")
    }

    val shape = RoundedCornerShape(16.dp)
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier
                .clip(shape)
                .background(Color(0xB30A0F24))
                .border(1.dp, Color(0x1AFFFFFF), shape)
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color(0xFFFF5F57)),
                )
                Spacer(Modifier.width(5.dp))
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color(0xFFFEBC2E)),
                )
                Spacer(Modifier.width(5.dp))
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color(0xFF28C840)),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "MainActivity.kt",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                    color = Muted,
                )
                Spacer(Modifier.width(4.dp))
            }
            Spacer(Modifier.height(9.dp))
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = JetBrainsMono,
                    fontSize = 9.5.sp,
                    lineHeight = 15.sp,
                ),
                color = Ink,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun FeatureCard(
    modifier: Modifier,
    icon: ImageVector,
    tint: Color,
    titleRes: Int,
    descriptionRes: Int,
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(15.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.14f))
                .border(1.dp, tint.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
            color = Ink,
            maxLines = 1,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(descriptionRes),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 15.sp),
            color = Muted,
            minLines = 3,
        )
    }
}

private fun toast(context: Context, messageRes: Int) {
    Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
}
