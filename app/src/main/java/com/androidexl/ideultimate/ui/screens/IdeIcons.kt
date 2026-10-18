package com.androidexl.ideultimate.ui.screens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object IdeIcons {
    val Folder: ImageVector by lazy {
        ImageVector.Builder(
            name = "IdeIcons.Folder",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(10f, 4f)
                lineTo(4f, 4f)
                curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
                lineTo(2f, 18f)
                curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                lineTo(20f, 20f)
                curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                lineTo(22f, 8f)
                curveTo(22f, 6.9f, 21.1f, 6f, 20f, 6f)
                lineTo(12f, 6f)
                lineTo(10f, 4f)
                close()
            }
        }.build()
    }

    val Upload: ImageVector by lazy {
        ImageVector.Builder(
            name = "IdeIcons.Upload",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(9f, 16f)
                horizontalLineTo(15f)
                verticalLineTo(10f)
                horizontalLineTo(19f)
                lineTo(12f, 3f)
                lineTo(5f, 10f)
                horizontalLineTo(9f)
                close()
                moveTo(5f, 18f)
                horizontalLineTo(19f)
                verticalLineTo(20f)
                horizontalLineTo(5f)
                close()
            }
        }.build()
    }

    val Download: ImageVector by lazy {
        ImageVector.Builder(
            name = "IdeIcons.Download",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(19f, 9f)
                horizontalLineTo(15f)
                verticalLineTo(3f)
                horizontalLineTo(9f)
                verticalLineTo(9f)
                horizontalLineTo(5f)
                lineTo(12f, 16f)
                lineTo(19f, 9f)
                close()
                moveTo(5f, 18f)
                verticalLineTo(20f)
                horizontalLineTo(19f)
                verticalLineTo(18f)
                horizontalLineTo(5f)
                close()
            }
        }.build()
    }

    val Undo: ImageVector by lazy {
        ImageVector.Builder(
            name = "IdeIcons.Undo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12.5f, 8f)
                curveTo(8.85f, 8f, 5.56f, 9.47f, 3.17f, 11.86f)
                lineTo(1f, 9.69f)
                verticalLineTo(16f)
                horizontalLineTo(7.31f)
                lineTo(4.68f, 13.37f)
                curveTo(6.65f, 11.4f, 9.42f, 10.19f, 12.5f, 10.19f)
                curveTo(18.23f, 10.19f, 22.88f, 14.84f, 22.88f, 20.57f)
                horizontalLineTo(25.07f)
                curveTo(25.07f, 13.63f, 19.44f, 8f, 12.5f, 8f)
                close()
            }
        }.build()
    }

    val Redo: ImageVector by lazy {
        ImageVector.Builder(
            name = "IdeIcons.Redo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.White),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(18.4f, 10.6f)
                curveTo(16.5f, 9f, 14f, 8f, 11.5f, 8f)
                curveTo(5.8f, 8f, 1.2f, 12.6f, 1.2f, 18.3f)
                horizontalLineTo(3.4f)
                curveTo(3.4f, 13.8f, 7f, 10.2f, 11.5f, 10.2f)
                curveTo(13.6f, 10.2f, 15.6f, 11f, 17.1f, 12.4f)
                lineTo(14.5f, 15f)
                horizontalLineTo(21f)
                verticalLineTo(8.5f)
                lineTo(18.4f, 10.6f)
                close()
            }
        }.build()
    }
}
