package com.androidexl.ideultimate.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.data.ProjectLanguage
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.JetBrainsMono
import com.androidexl.ideultimate.ui.theme.Muted
import com.androidexl.ideultimate.ui.theme.Teal
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

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

private val RemoteBitmapCache = ConcurrentHashMap<String, ImageBitmap>()

fun languageColor(language: ProjectLanguage): Color = when (language) {
    ProjectLanguage.FLUTTER -> Color(0xFF38BDF8)
    ProjectLanguage.KOTLIN -> VioletSoft
    ProjectLanguage.JAVA -> Color(0xFFFBBF24)
}

fun savePickedProjectIcon(context: Context, uri: Uri): String? = runCatching {
    val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream)
    } ?: return null
    val size = 192
    val scaled = Bitmap.createScaledBitmap(rawBitmap, size, size, true)
    val dir = File(context.filesDir, "project_icons").apply { mkdirs() }
    val outFile = File(dir, "icon_${System.currentTimeMillis()}.png")
    FileOutputStream(outFile).use { out ->
        scaled.compress(Bitmap.CompressFormat.PNG, 90, out)
    }
    outFile.absolutePath
}.getOrNull()

fun savePickedProjectScreenshot(context: Context, uri: Uri): String? = runCatching {
    val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream)
    } ?: return null
    val maxDim = 720
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
    val outFile = File(dir, "shot_${System.currentTimeMillis()}_${(100..999).random()}.jpg")
    FileOutputStream(outFile).use { out ->
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
    }
    outFile.absolutePath
}.getOrNull()

/**
 * Normalizes user-entered image URLs (adds https:// if missing, converts GitHub blob URLs
 * or Google Drive sharing URLs into direct image URLs).
 */
fun normalizeImageUrl(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty() ||
        trimmed.startsWith("data:image", ignoreCase = true) ||
        trimmed.startsWith("/") ||
        trimmed.startsWith("file://", ignoreCase = true)
    ) {
        return trimmed
    }
    val withScheme = if (trimmed.startsWith("http://", ignoreCase = true) ||
        trimmed.startsWith("https://", ignoreCase = true)
    ) {
        trimmed
    } else {
        "https://$trimmed"
    }

    // Convert GitHub blob links to raw image links automatically
    if (withScheme.contains("github.com/") && withScheme.contains("/blob/")) {
        return withScheme
            .replace("https://github.com/", "https://raw.githubusercontent.com/")
            .replace("http://github.com/", "https://raw.githubusercontent.com/")
            .replace("/blob/", "/")
    }

    // Convert Google Drive /file/d/<id>/view links to direct download links
    val driveRegex = Regex("""drive\.google\.com/file/d/([a-zA-Z0-9_-]+)""")
    val driveMatch = driveRegex.find(withScheme)
    if (driveMatch != null) {
        val id = driveMatch.groupValues[1]
        return "https://drive.google.com/uc?export=download&id=$id"
    }

    return withScheme
}

/**
 * Converts a local image file path into a compact Base64 data URI so it can be stored directly
 * in Firebase Realtime Database when uploading user avatars, project icons, or screenshots.
 */
fun encodeImageForFirebase(pathOrData: String?, maxDim: Int = 160): String? {
    val value = pathOrData?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    if (value.startsWith("data:image", ignoreCase = true) ||
        value.startsWith("http://", ignoreCase = true) ||
        value.startsWith("https://", ignoreCase = true)
    ) {
        return normalizeImageUrl(value)
    }
    return runCatching {
        val raw = BitmapFactory.decodeFile(value) ?: return normalizeImageUrl(value)
        val w = raw.width.coerceAtLeast(1)
        val h = raw.height.coerceAtLeast(1)
        val scale = (maxDim.toFloat() / maxOf(w, h)).coerceAtMost(1f)
        val bmp = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                raw,
                (w * scale).toInt().coerceAtLeast(1),
                (h * scale).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            raw
        }
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 78, baos)
        val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
        "data:image/jpeg;base64,$b64"
    }.getOrDefault(value)
}

fun decodePathOrDataUri(pathOrData: String?): ImageBitmap? {
    val value = pathOrData?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    RemoteBitmapCache[value]?.let { return it }
    return runCatching {
        when {
            value.startsWith("data:image", ignoreCase = true) -> {
                val comma = value.indexOf(',')
                if (comma > 0) {
                    val bytes = Base64.decode(value.substring(comma + 1), Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                } else {
                    null
                }
            }
            !value.startsWith("http://", ignoreCase = true) &&
                !value.startsWith("https://", ignoreCase = true) -> {
                BitmapFactory.decodeFile(value)?.asImageBitmap()
            }
            else -> null
        }
    }.getOrNull()?.also { bmp ->
        RemoteBitmapCache[value] = bmp
    }
}

private fun downloadRemoteBitmapBlocking(rawUrl: String, maxDim: Int = 1080): ImageBitmap? {
    val normalized = normalizeImageUrl(rawUrl)
    RemoteBitmapCache[normalized]?.let { return it }
    return runCatching {
        var currentUrl = normalized
        var redirects = 0
        var bytes: ByteArray? = null
        while (redirects < 5) {
            val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 12_000
                readTimeout = 15_000
                setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/124.0 Mobile Safari/537.36",
                )
                setRequestProperty("Accept", "image/*,*/*;q=0.8")
            }
            val code = conn.responseCode
            if (code in 300..399) {
                val location = conn.getHeaderField("Location")
                conn.disconnect()
                if (location.isNullOrBlank()) break
                currentUrl = URL(URL(currentUrl), location).toString()
                redirects++
                continue
            }
            if (code in 200..299) {
                bytes = conn.inputStream.use { it.readBytes() }
            }
            conn.disconnect()
            break
        }
        val data = bytes ?: return null
        val raw = BitmapFactory.decodeByteArray(data, 0, data.size) ?: return null
        val w = raw.width.coerceAtLeast(1)
        val h = raw.height.coerceAtLeast(1)
        val scale = (maxDim.toFloat() / maxOf(w, h)).coerceAtMost(1f)
        val finalBmp = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                raw,
                (w * scale).toInt().coerceAtLeast(1),
                (h * scale).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            raw
        }
        finalBmp.asImageBitmap().also { img ->
            RemoteBitmapCache[rawUrl] = img
            RemoteBitmapCache[normalized] = img
        }
    }.getOrNull()
}

/**
 * Loads an [ImageBitmap] from a local file path, a Base64 data URI, or an HTTP/HTTPS image URL.
 */
@Composable
fun rememberLoadedImageBitmap(pathOrUrl: String?): Pair<ImageBitmap?, Boolean> {
    val clean = pathOrUrl?.trim().orEmpty()
    var bitmap by remember(clean) {
        mutableStateOf(decodePathOrDataUri(clean))
    }
    var isLoading by remember(clean) {
        val needsNetwork = bitmap == null && clean.isNotEmpty() &&
            (clean.startsWith("http://", ignoreCase = true) ||
                clean.startsWith("https://", ignoreCase = true) ||
                !clean.startsWith("/"))
        mutableStateOf(needsNetwork)
    }

    LaunchedEffect(clean) {
        if (clean.isEmpty()) {
            bitmap = null
            isLoading = false
            return@LaunchedEffect
        }
        val immediate = decodePathOrDataUri(clean)
        if (immediate != null) {
            bitmap = immediate
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        val downloaded = withContext(Dispatchers.IO) {
            downloadRemoteBitmapBlocking(clean)
        }
        bitmap = downloaded
        isLoading = false
    }

    return bitmap to isLoading
}

@Composable
fun UserAvatarView(
    avatarPath: String?,
    isLoggedIn: Boolean = true,
    size: Dp = 48.dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val (customBitmap, _) = rememberLoadedImageBitmap(avatarPath)
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
    contentScale: ContentScale = ContentScale.Crop,
) {
    val (loadedBitmap, isLoading) = rememberLoadedImageBitmap(pathOrUrl)
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF0A1028))
            .border(1.dp, Cyan.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center,
    ) {
        when {
            loadedBitmap != null -> {
                Image(
                    bitmap = loadedBitmap,
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            isLoading -> {
                CircularProgressIndicator(
                    color = Cyan,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(28.dp),
                )
            }
            else -> {
                LogoMark(modifier = Modifier.size(44.dp), animated = false)
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

    val (customBitmap, _) = rememberLoadedImageBitmap(customIconPath)

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
