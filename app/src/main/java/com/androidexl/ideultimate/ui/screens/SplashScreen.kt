package com.androidexl.ideultimate.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.androidexl.ideultimate.R
import com.androidexl.ideultimate.ui.components.GradientProgressBar
import com.androidexl.ideultimate.ui.components.LogoMark
import com.androidexl.ideultimate.ui.components.Starfield
import com.androidexl.ideultimate.ui.theme.Almarai
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.Ink
import com.androidexl.ideultimate.ui.theme.Lilac
import com.androidexl.ideultimate.ui.theme.Muted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val IntroEasing = CubicBezierEasing(0.22f, 0.61f, 0.36f, 1f)
private val ProgressEasing = CubicBezierEasing(0.35f, 0f, 0.2f, 1f)

/**
 * Animated splash screen: nebula backdrop, twinkling star field, a pulsing
 * code mark, staged typography and a gradient loading indicator.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    val exitAlpha = remember { Animatable(1f) }
    val exitScale = remember { Animatable(1f) }
    var finishing by remember { mutableStateOf(false) }

    var showName by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showLoader by remember { mutableStateOf(false) }
    var startZoom by remember { mutableStateOf(false) }

    fun finish() {
        if (finishing) return
        finishing = true
        scope.launch {
            launch { exitScale.animateTo(1.06f, tween(460, easing = FastOutSlowInEasing)) }
            exitAlpha.animateTo(0f, tween(380, easing = LinearEasing))
            onFinished()
        }
    }

    LaunchedEffect(Unit) {
        startZoom = true
        delay(160)
        showName = true
        delay(130)
        showTagline = true
        delay(130)
        showLoader = true
        progress.animateTo(1f, tween(durationMillis = 2300, easing = ProgressEasing))
        delay(340)
        finish()
    }

    BackHandler { finish() }

    val zoom by animateFloatAsState(
        targetValue = if (startZoom) 1.02f else 1.14f,
        animationSpec = tween(durationMillis = 7000, easing = LinearOutSlowInEasing),
        label = "nebula-zoom",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavy)
            .graphicsLayer {
                alpha = exitAlpha.value
                scaleX = exitScale.value
                scaleY = exitScale.value
            },
    ) {
        Image(
            painter = painterResource(R.drawable.splash_nebula),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                },
        )

        // Soft dark scrim so the content always stays readable.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to DeepNavy.copy(alpha = 0.86f),
                        0.28f to DeepNavy.copy(alpha = 0.30f),
                        0.62f to Color.Transparent,
                        1f to DeepNavy.copy(alpha = 0.92f),
                    ),
                ),
        )

        // Vignette.
        val vignetteColor = DeepNavy
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Transparent,
                        vignetteColor.copy(alpha = 0.72f),
                    ),
                    center = center,
                    radius = size.maxDimension * 0.72f,
                ),
            )
        }

        Starfield(
            modifier = Modifier.fillMaxSize(),
            starCount = 90,
            seed = 3,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            LogoMark(
                modifier = Modifier.size(156.dp),
                animated = true,
            )

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = showName,
                enter = fadeIn(tween(650, easing = IntroEasing)) +
                    slideInVertically(tween(650, easing = IntroEasing)) { it / 3 },
            ) {
                Text(
                    text = "IDE ULTIMATE",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = Almarai,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 27.sp,
                        letterSpacing = 3.sp,
                        brush = Brush.linearGradient(listOf(Color.White, Lilac, Cyan)),
                    ),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(10.dp))

            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(tween(650, delayMillis = 80, easing = IntroEasing)) +
                    slideInVertically(tween(650, delayMillis = 80, easing = IntroEasing)) { it / 3 },
            ) {
                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 23.sp),
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(46.dp))

            AnimatedVisibility(
                visible = showLoader,
                enter = fadeIn(tween(700, delayMillis = 80, easing = IntroEasing)),
            ) {
                SplashLoader(progress = { progress.value })
            }
        }

        Surface(
            onClick = { finish() },
            shape = RoundedCornerShape(percent = 50),
            color = Color(0x14FFFFFF),
            contentColor = Ink,
            border = BorderStroke(1.dp, Color(0x2BFFFFFF)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(top = 18.dp, end = 22.dp),
        ) {
            Text(
                text = stringResource(R.string.splash_skip),
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = Ink,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(Cyan),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.splash_powered),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            letterSpacing = 2.sp,
                        ),
                        color = Muted.copy(alpha = 0.9f),
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.splash_version),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = Muted.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun SplashLoader(progress: () -> Float) {
    val value = progress()
    val statusRes = when {
        value < 0.32f -> R.string.splash_status_workspace
        value < 0.64f -> R.string.splash_status_kotlin
        value < 0.95f -> R.string.splash_status_preview
        else -> R.string.splash_status_ready
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GradientProgressBar(
            progress = value,
            modifier = Modifier.width(220.dp),
            height = 5.dp,
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.width(220.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedContent(
                targetState = statusRes,
                transitionSpec = {
                    (fadeIn(tween(260)) + slideInVertically { it / 2 }) togetherWith
                        (fadeOut(tween(160)) + slideOutVertically { -it / 2 })
                },
                modifier = Modifier.weight(1f),
                label = "splash-status",
            ) { res ->
                Text(
                    text = stringResource(res),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Muted,
                    maxLines = 1,
                )
            }
            Text(
                text = "${(value * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Cyan,
            )
        }
    }
}
