package com.androidexl.ideultimate.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.androidexl.ideultimate.ui.theme.Cyan
import com.androidexl.ideultimate.ui.theme.Fuchsia
import com.androidexl.ideultimate.ui.theme.Lilac
import com.androidexl.ideultimate.ui.theme.Violet
import com.androidexl.ideultimate.ui.theme.VioletSoft
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The IDE Ultimate mark, drawn entirely with Compose primitives:
 * a glowing hexagon, a pair of code brackets and a slowly rotating orbit.
 */
@Composable
fun LogoMark(
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    glow: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "logo-transition")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbit",
    )

    Canvas(modifier = modifier) {
        val side = size.minDimension
        val middle = Offset(size.width / 2f, size.height / 2f)
        val radius = side * 0.355f

        if (glow) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Violet.copy(alpha = 0.40f),
                        Violet.copy(alpha = 0.10f),
                        Color.Transparent,
                    ),
                    center = middle,
                    radius = side * 0.52f,
                ),
                radius = side * 0.52f,
                center = middle,
            )
        }

        // Rotating orbit arcs.
        val orbitTopLeft = Offset(middle.x - radius * 1.18f, middle.y - radius * 1.18f)
        val orbitSize = Size(radius * 2.36f, radius * 2.36f)
        rotate(degrees = if (animated) spin else 0f, pivot = middle) {
            drawArc(
                color = Cyan.copy(alpha = 0.55f),
                startAngle = -95f,
                sweepAngle = 76f,
                useCenter = false,
                topLeft = orbitTopLeft,
                size = orbitSize,
                style = Stroke(width = side * 0.012f, cap = StrokeCap.Round),
            )
            drawArc(
                color = Fuchsia.copy(alpha = 0.45f),
                startAngle = 85f,
                sweepAngle = 76f,
                useCenter = false,
                topLeft = orbitTopLeft,
                size = orbitSize,
                style = Stroke(width = side * 0.012f, cap = StrokeCap.Round),
            )
        }

        // Hexagon.
        val hexagon = Path().apply {
            for (i in 0 until 6) {
                val angle = (PI / 3.0) * i - PI / 2.0
                val x = middle.x + radius * cos(angle).toFloat()
                val y = middle.y + radius * sin(angle).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(
            path = hexagon,
            color = Violet.copy(alpha = 0.20f),
            style = Stroke(width = side * 0.078f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawPath(
            path = hexagon,
            brush = Brush.linearGradient(
                colors = listOf(Cyan, VioletSoft, Fuchsia),
                start = Offset(middle.x - radius, middle.y - radius),
                end = Offset(middle.x + radius, middle.y + radius),
            ),
            style = Stroke(width = side * 0.034f, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )

        // Code brackets and slash.
        val bracketX = radius * 0.44f
        val bracketY = radius * 0.42f
        val tipX = radius * 0.74f
        val strokeWidth = side * 0.034f

        val leftBracket = Path().apply {
            moveTo(middle.x - bracketX, middle.y - bracketY)
            lineTo(middle.x - tipX, middle.y)
            lineTo(middle.x - bracketX, middle.y + bracketY)
        }
        val rightBracket = Path().apply {
            moveTo(middle.x + bracketX, middle.y - bracketY)
            lineTo(middle.x + tipX, middle.y)
            lineTo(middle.x + bracketX, middle.y + bracketY)
        }
        drawPath(
            path = leftBracket,
            color = Cyan,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawPath(
            path = rightBracket,
            color = Lilac,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
        )
        drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(middle.x + radius * 0.14f, middle.y - radius * 0.46f),
            end = Offset(middle.x - radius * 0.14f, middle.y + radius * 0.46f),
            strokeWidth = strokeWidth * 0.72f,
            cap = StrokeCap.Round,
        )
    }
}
