package com.androidexl.ideultimate.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.data.ProjectLanguage
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft
import java.io.File
import java.io.FileOutputStream

data class IconPresetStyle(
    val id: Int,
    val label: String,
    val primary: Color,
    val secondary: Color,
    val glyph: String,
)

val ProjectIconPresets = listOf(
    IconPresetStyle(0, "Code", Cyan, Violet, "</>"),
    IconPresetStyle(1, "Flutter", Color(0xFF38BDF8), Color(0xFF0284C7), "FL"),
    IconPresetStyle(2, "Java", Color(0xFFF59E0B), Color(0xFFD97706), "JV"),
    IconPresetStyle(3, "Compose", VioletSoft, Fuchsia, "UI"),
    IconPresetStyle(4, "Android", Teal, Color(0xFF059669), "APK"),
    IconPresetStyle(5, "Core", Fuchsia, Color(0xFF7C3AED), "IDE"),
)

val PROJECT_ICON_PRESETS = ProjectIconPresets

fun languageColor(language: ProjectLanguage): Color = when (language) {
    ProjectLanguage.FLUTTER -> Color(0xFF38BDF8)
    ProjectLanguage.KOTLIN -> VioletSoft
    ProjectLanguage.JAVA -> Color(0xFFFBBF24)
}

fun savePickedProjectIcon(context: Context, uri: Uri): String? = runCatching {
    val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream)
    } ?: return null
    val size = 256
    val scaled = Bitmap.createScaledBitmap(rawBitmap, size, size, true)
    val dir = File(context.filesDir, "project_icons").apply { mkdirs() }
    val outFile = File(dir, "icon_${System.currentTimeMillis()}.png")
    FileOutputStream(outFile).use { out ->
        scaled.compress(Bitmap.CompressFormat.PNG, 92, out)
    }
    outFile.absolutePath
}.getOrNull()

fun savePickedProjectScreenshot(context: Context, uri: Uri): String? = runCatching {
    val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream)
    } ?: return null
    val maxDim = 960
    val w = rawBitmap.width.coerceAtLeast(1)
    val h = rawBitmap.height.coerceAtLeast(1)
    val scale = (maxDim.toFloat() / maxOf(w, h)).coerceAtMost(1f)
    val scaled = if (scale < 1f) {
        Bitmap.createScaledBitmap(
            rawBitmap,
            (w * scale).toInt().coerceAtLeast(1),
            (h * scale).toInt().coerceAtLeast(1),
            true,
        )
    } else {
        rawBitmap
    }
    val dir = File(context.filesDir, "project_screenshots").apply { mkdirs() }
    val outFile = File(dir, "shot_${System.currentTimeMillis()}_${(100..999).random()}.png")
    FileOutputStream(outFile).use { out ->
        scaled.compress(Bitmap.CompressFormat.PNG, 90, out)
    }
    outFile.absolutePath
}.getOrNull()

@Composable
fun UserAvatarView(
    avatarPath: String?,
    isLoggedIn: Boolean = true,
    size: Dp = 48.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val customBitmap = remember(avatarPath) {
        avatarPath?.takeIf { it.isNotBlank() }?.let { path ->
            runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
        }
    }

    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    if (isLoggedIn) {
                        listOf(Cyan.copy(alpha = 0.35f), Violet.copy(alpha = 0.55f))
                    } else {
                        listOf(Color(0x26FFFFFF), Color(0x14FFFFFF))
                    },
                ),
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    if (isLoggedIn) {
                        listOf(Cyan, Fuchsia)
                    } else {
                        listOf(Color(0x40FFFFFF), Color(0x20FFFFFF))
                    },
                ),
                shape = CircleShape,
            )
            .then(clickMod),
        contentAlignment = Alignment.Center,
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = if (isLoggedIn) Color.White else Muted,
                modifier = Modifier.fillMaxSize(0.58f),
            )
        }
    }
}

@Composable
fun ProjectScreenshotImage(
    pathOrUrl: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
) {
    val localBitmap = remember(pathOrUrl) {
        if (!pathOrUrl.startsWith("http", ignoreCase = true)) {
            runCatching { BitmapFactory.decodeFile(pathOrUrl)?.asImageBitmap() }.getOrNull()
        } else {
            null
        }
    }
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF0A1028))
            .border(1.dp, Cyan.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (localBitmap != null) {
            Image(
                bitmap = localBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.splash_nebula),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.55f,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0x40050816), Color(0xCC050816)),
                        ),
                    )
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.weight(1f))
                LogoMark(modifier = Modifier.height(42.dp), animated = false)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = pathOrUrl.substringAfterLast('/'),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMono,
                        fontSize = 9.5.sp,
                    ),
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ProjectIconView(
    iconPreset: Int,
    customIconPath: String?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
) {
    val preset = ProjectIconPresets.getOrElse(
        ((iconPreset % ProjectIconPresets.size) + ProjectIconPresets.size) % ProjectIconPresets.size,
    ) { ProjectIconPresets.first() }

    val customBitmap = remember(customIconPath) {
        customIconPath?.takeIf { it.isNotBlank() }?.let { path ->
            runCatching { BitmapFactory.decodeFile(path)?.asImageBitmap() }.getOrNull()
        }
    }

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        preset.primary.copy(alpha = 0.28f),
                        preset.secondary.copy(alpha = 0.36f),
                        Color(0xFF0B1028),
                    ),
                ),
            )
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(preset.primary.copy(alpha = 0.75f), preset.secondary.copy(alpha = 0.35f)),
                ),
                shape = shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Canvas(Modifier.fillMaxSize().padding(6.dp)) {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(preset.primary.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(w * 0.5f, h * 0.5f),
                        radius = w * 0.55f,
                    ),
                )
                drawRoundRect(
                    color = preset.primary.copy(alpha = 0.22f),
                    topLeft = Offset(w * 0.14f, h * 0.14f),
                    size = Size(w * 0.72f, h * 0.72f),
                    cornerRadius = CornerRadius(w * 0.22f, w * 0.22f),
                    style = Stroke(width = w * 0.035f, cap = StrokeCap.Round),
                )
                if (preset.id == 1) {
                    val wing = Path().apply {
                        moveTo(w * 0.30f, h * 0.52f)
                        lineTo(w * 0.58f, h * 0.24f)
                        lineTo(w * 0.72f, h * 0.24f)
                        lineTo(w * 0.44f, h * 0.52f)
                        close()
                    }
                    drawPath(wing, preset.primary.copy(alpha = 0.30f))
                }
            }
            Text(
                text = preset.glyph,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = JetBrainsMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                ),
                color = Color.White,
            )
        }
    }
}
