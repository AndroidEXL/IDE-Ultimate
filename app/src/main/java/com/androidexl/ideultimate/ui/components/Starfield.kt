package com.androidexl.ideultimate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.androidexl.ideultimate.ui.theme.Cyan
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private data class Star(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val phase: Float,
    val glow: Boolean,
)

private fun generateStars(count: Int, seed: Int): List<Star> {
    val random = Random(seed)
    return List(count) {
        Star(
            x = random.nextFloat(),
            y = random.nextFloat(),
            radius = 0.4f + random.nextFloat() * 1.3f,
            alpha = 0.25f + random.nextFloat() * 0.65f,
            phase = random.nextFloat() * (2f * PI.toFloat()),
            glow = random.nextFloat() > 0.84f,
        )
    }
}

/**
 * Lightweight, deterministic star field with a gentle twinkle and an
 * occasional shooting star. All animation happens in the draw phase.
 */
@Composable
fun Starfield(
    modifier: Modifier = Modifier,
    starCount: Int = 80,
    seed: Int = 11,
    comet: Boolean = true,
) {
    val stars = remember(starCount, seed) { generateStars(starCount, seed) }
    val transition = rememberInfiniteTransition(label = "starfield")

    val twinkle by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI.toFloat()),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "twinkle",
    )

    val cometPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 9000
                0f at 0
                0f at 4800
                1f at 6400
                1f at 9000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "comet",
    )

    Canvas(modifier = modifier) {
        stars.forEach { star ->
            val sparkle = 0.55f + 0.45f * sin(twinkle + star.phase)
            val alpha = (star.alpha * sparkle).coerceIn(0f, 1f)
            val point = Offset(star.x * size.width, star.y * size.height)
            val radius = star.radius.dp.toPx()

            if (star.glow) {
                val glowRadius = radius * 6f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Cyan.copy(alpha = alpha * 0.28f), Color.Transparent),
                        center = point,
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                    center = point,
                )
            }
            drawCircle(color = Color.White.copy(alpha = alpha), radius = radius, center = point)
        }

        if (comet && cometPhase > 0f && cometPhase < 1f) {
            val t = cometPhase
            val start = Offset(size.width * 0.88f, size.height * 0.10f)
            val end = Offset(size.width * 0.42f, size.height * 0.34f)
            val head = Offset(
                start.x + (end.x - start.x) * t,
                start.y + (end.y - start.y) * t,
            )
            val dx = end.x - start.x
            val dy = end.y - start.y
            val length = hypot(dx, dy).coerceAtLeast(1f)
            val tailLength = size.minDimension * 0.22f
            val tail = Offset(head.x - dx / length * tailLength, head.y - dy / length * tailLength)
            val alpha = sin(t * PI).toFloat().coerceIn(0f, 1f)

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color.Transparent, Color.White.copy(alpha = alpha * 0.9f)),
                    start = tail,
                    end = head,
                ),
                start = tail,
                end = head,
                strokeWidth = 1.6.dp.toPx(),
                cap = StrokeCap.Round,
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = alpha * 0.7f), Color.Transparent),
                    center = head,
                    radius = 14.dp.toPx(),
                ),
                radius = 14.dp.toPx(),
                center = head,
            )
        }
    }
}
