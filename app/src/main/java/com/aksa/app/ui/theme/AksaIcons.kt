package com.aksa.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * 100% self-contained AKSA Vector Icons.
 * Pure vector paths with zero external dependencies.
 */
object AksaIcons {

    val Star: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaStar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 17.27f)
                lineTo(18.18f, 21f)
                lineTo(16.54f, 13.97f)
                lineTo(22f, 9.24f)
                lineTo(14.81f, 8.63f)
                lineTo(12f, 2f)
                lineTo(9.19f, 8.63f)
                lineTo(2f, 9.24f)
                lineTo(7.46f, 13.97f)
                lineTo(5.82f, 21f)
                close()
            }
        }.build()
    }

    val Check: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaCheck",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9f, 16.17f)
                lineTo(4.83f, 12f)
                lineTo(3.41f, 13.41f)
                lineTo(9f, 19f)
                lineTo(21f, 7f)
                lineTo(19.59f, 5.59f)
                close()
            }
        }.build()
    }

    val Lock: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaLock",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(18f, 8f)
                horizontalLineTo(17f)
                verticalLineTo(6f)
                curveTo(17f, 3.24f, 14.76f, 1f, 12f, 1f)
                curveTo(9.24f, 1f, 7f, 3.24f, 7f, 6f)
                verticalLineTo(8f)
                horizontalLineTo(6f)
                curveTo(4.9f, 8f, 4f, 8.9f, 4f, 10f)
                verticalLineTo(20f)
                curveTo(4f, 21.1f, 4.9f, 22f, 6f, 22f)
                horizontalLineTo(18f)
                curveTo(19.1f, 22f, 20f, 21.1f, 20f, 20f)
                verticalLineTo(10f)
                curveTo(20f, 8.9f, 19.1f, 8f, 18f, 8f)
                close()
                moveTo(9f, 6f)
                curveTo(9f, 4.34f, 10.34f, 3f, 12f, 3f)
                curveTo(13.66f, 3f, 15f, 4.34f, 15f, 6f)
                verticalLineTo(8f)
                horizontalLineTo(9f)
                verticalLineTo(6f)
                close()
                moveTo(12f, 17f)
                curveTo(10.9f, 17f, 10f, 16.1f, 10f, 15f)
                curveTo(10f, 13.9f, 10.9f, 13f, 12f, 13f)
                curveTo(13.1f, 13f, 14f, 13.9f, 14f, 15f)
                curveTo(14f, 16.1f, 13.1f, 17f, 12f, 17f)
                close()
            }
        }.build()
    }

    val Face: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaFace",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(8.5f, 9.5f)
                curveTo(9.33f, 9.5f, 10f, 10.17f, 10f, 11f)
                curveTo(10f, 11.83f, 9.33f, 12.5f, 8.5f, 12.5f)
                curveTo(7.67f, 12.5f, 7f, 11.83f, 7f, 11f)
                curveTo(7f, 10.17f, 7.67f, 9.5f, 8.5f, 9.5f)
                close()
                moveTo(15.5f, 9.5f)
                curveTo(16.33f, 9.5f, 17f, 10.17f, 17f, 11f)
                curveTo(17f, 11.83f, 16.33f, 12.5f, 15.5f, 12.5f)
                curveTo(14.67f, 12.5f, 14f, 11.83f, 14f, 11f)
                curveTo(14f, 10.17f, 14.67f, 9.5f, 15.5f, 9.5f)
                close()
                moveTo(12f, 17.5f)
                curveTo(9.67f, 17.5f, 7.69f, 16.04f, 6.89f, 14f)
                horizontalLineTo(17.11f)
                curveTo(16.31f, 16.04f, 14.33f, 17.5f, 12f, 17.5f)
                close()
            }
        }.build()
    }

    val Person: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaPerson",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 12f)
                curveTo(14.21f, 12f, 16f, 10.21f, 16f, 8f)
                curveTo(16f, 5.79f, 14.21f, 4f, 12f, 4f)
                curveTo(9.79f, 4f, 8f, 5.79f, 8f, 8f)
                curveTo(8f, 10.21f, 9.79f, 12f, 12f, 12f)
                close()
                moveTo(12f, 14f)
                curveTo(9.33f, 14f, 4f, 15.34f, 4f, 18f)
                verticalLineTo(20f)
                horizontalLineTo(20f)
                verticalLineTo(18f)
                curveTo(20f, 15.34f, 14.67f, 14f, 12f, 14f)
                close()
            }
        }.build()
    }

    val Info: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaInfo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 2f)
                curveTo(6.48f, 2f, 2f, 6.48f, 2f, 12f)
                curveTo(2f, 17.52f, 6.48f, 22f, 12f, 22f)
                curveTo(17.52f, 22f, 22f, 17.52f, 22f, 12f)
                curveTo(22f, 6.48f, 17.52f, 2f, 12f, 2f)
                close()
                moveTo(13f, 17f)
                horizontalLineTo(11f)
                verticalLineTo(11f)
                horizontalLineTo(13f)
                verticalLineTo(17f)
                close()
                moveTo(13f, 9f)
                horizontalLineTo(11f)
                verticalLineTo(7f)
                horizontalLineTo(13f)
                verticalLineTo(9f)
                close()
            }
        }.build()
    }

    val Favorite: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaFavorite",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 21.35f)
                lineTo(10.55f, 20.03f)
                curveTo(5.4f, 15.36f, 2f, 12.28f, 2f, 8.5f)
                curveTo(2f, 5.42f, 4.42f, 3f, 7.5f, 3f)
                curveTo(9.24f, 3f, 10.91f, 3.81f, 12f, 5.09f)
                curveTo(13.09f, 3.81f, 14.76f, 3f, 16.5f, 3f)
                curveTo(19.58f, 3f, 22f, 5.42f, 22f, 8.5f)
                curveTo(22f, 12.28f, 18.6f, 15.36f, 13.45f, 20.04f)
                lineTo(12f, 21.35f)
                close()
            }
        }.build()
    }

    val PlayArrow: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaPlayArrow",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(8f, 5f)
                verticalLineTo(19f)
                lineTo(19f, 12f)
                close()
            }
        }.build()
    }

    val ArrowForward: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaArrowForward",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 4f)
                lineTo(10.59f, 5.41f)
                lineTo(16.17f, 11f)
                horizontalLineTo(4f)
                verticalLineTo(13f)
                horizontalLineTo(16.17f)
                lineTo(10.59f, 18.59f)
                lineTo(12f, 20f)
                lineTo(20f, 12f)
                close()
            }
        }.build()
    }

    val Email: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaEmail",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 4f)
                horizontalLineTo(4f)
                curveTo(2.9f, 4f, 2.01f, 4.9f, 2.01f, 6f)
                lineTo(2f, 18f)
                curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                horizontalLineTo(20f)
                curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                verticalLineTo(6f)
                curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
                close()
                moveTo(20f, 8f)
                lineTo(12f, 13f)
                lineTo(4f, 8f)
                verticalLineTo(6f)
                lineTo(12f, 11f)
                lineTo(20f, 6f)
                verticalLineTo(8f)
                close()
            }
        }.build()
    }

    val Eye: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaEye",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 4.5f)
                curveTo(7f, 4.5f, 2.73f, 7.61f, 1f, 12f)
                curveTo(2.73f, 16.39f, 7f, 19.5f, 12f, 19.5f)
                curveTo(17f, 19.5f, 21.27f, 16.39f, 23f, 12f)
                curveTo(21.27f, 7.61f, 17f, 4.5f, 12f, 4.5f)
                close()
                moveTo(12f, 17f)
                curveTo(9.24f, 17f, 7f, 14.76f, 7f, 12f)
                curveTo(7f, 9.24f, 9.24f, 7f, 12f, 7f)
                curveTo(14.76f, 7f, 17f, 9.24f, 17f, 12f)
                curveTo(17f, 14.76f, 14.76f, 17f, 12f, 17f)
                close()
                moveTo(12f, 9f)
                curveTo(10.34f, 9f, 9f, 10.34f, 9f, 12f)
                curveTo(9f, 13.66f, 10.34f, 15f, 12f, 15f)
                curveTo(13.66f, 15f, 15f, 13.66f, 15f, 12f)
                curveTo(15f, 10.34f, 13.66f, 9f, 12f, 9f)
                close()
            }
        }.build()
    }

    val EyeOff: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaEyeOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 7f)
                curveTo(13.56f, 7f, 14.97f, 7.61f, 16f, 8.6f)
                lineTo(18.39f, 6.21f)
                curveTo(16.66f, 4.88f, 14.47f, 4.14f, 12f, 4.14f)
                curveTo(7.28f, 4.14f, 3.25f, 7.02f, 1.54f, 11.14f)
                curveTo(2.32f, 13.01f, 3.59f, 14.65f, 5.21f, 15.86f)
                lineTo(7.53f, 13.54f)
                curveTo(7.19f, 12.87f, 7f, 12.11f, 7f, 11.31f)
                curveTo(7f, 8.93f, 8.93f, 7f, 11.31f, 7f)
                horizontalLineTo(12f)
                close()
                moveTo(2.81f, 2.81f)
                lineTo(1.39f, 4.22f)
                lineTo(4.62f, 7.45f)
                lineTo(5.1f, 7.93f)
                curveTo(3.47f, 9.21f, 2.22f, 10.88f, 1.54f, 12.86f)
                curveTo(3.25f, 16.98f, 7.28f, 19.86f, 12f, 19.86f)
                curveTo(13.82f, 19.86f, 15.54f, 19.38f, 17.04f, 18.52f)
                lineTo(17.51f, 19f)
                lineTo(19.78f, 21.26f)
                lineTo(21.19f, 19.85f)
                lineTo(2.81f, 2.81f)
                close()
            }
        }.build()
    }

    val Flame: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaFlame",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(13.5f, 0.67f)
                curveTo(13.5f, 0.67f, 13.34f, 1.25f, 13.34f, 1.67f)
                curveTo(13.34f, 3.23f, 14.59f, 4.48f, 16.15f, 4.48f)
                curveTo(17.71f, 4.48f, 19f, 5.77f, 19f, 7.33f)
                curveTo(19f, 10.66f, 16.4f, 13.57f, 13f, 13.95f)
                verticalLineTo(13f)
                curveTo(13f, 10.79f, 11.21f, 9f, 9f, 9f)
                curveTo(8.53f, 9f, 8.08f, 9.08f, 7.67f, 9.23f)
                curveTo(8.37f, 6.72f, 10.72f, 4.88f, 13.5f, 4.88f)
                verticalLineTo(0.67f)
                close()
                moveTo(12f, 2f)
                curveTo(6.48f, 6.84f, 4f, 10.96f, 4f, 15f)
                curveTo(4f, 19.42f, 7.58f, 23f, 12f, 23f)
                curveTo(16.42f, 23f, 20f, 19.42f, 20f, 15f)
                curveTo(20f, 9.8f, 16.5f, 5.5f, 12f, 2f)
                close()
                moveTo(12f, 21f)
                curveTo(8.69f, 21f, 6f, 18.31f, 6f, 15f)
                curveTo(6f, 12.5f, 7.82f, 9.2f, 12f, 5.5f)
                curveTo(16.18f, 9.2f, 18f, 12.5f, 18f, 15f)
                curveTo(18f, 18.31f, 15.31f, 21f, 12f, 21f)
                close()
            }
        }.build()
    }

    val Map: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaMap",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.5f, 3f)
                lineTo(15f, 5.1f)
                lineTo(9f, 3f)
                lineTo(3.36f, 4.9f)
                curveTo(3.14f, 4.97f, 3f, 5.18f, 3f, 5.42f)
                verticalLineTo(20.5f)
                curveTo(3f, 20.78f, 3.22f, 21f, 3.5f, 21f)
                lineTo(9f, 18.9f)
                lineTo(15f, 21f)
                lineTo(20.64f, 19.1f)
                curveTo(20.86f, 19.03f, 21f, 18.82f, 21f, 18.58f)
                verticalLineTo(3.5f)
                curveTo(21f, 3.22f, 20.78f, 3f, 20.5f, 3f)
                close()
                moveTo(15f, 19f)
                lineTo(9f, 16.9f)
                verticalLineTo(5f)
                lineTo(15f, 7.1f)
                verticalLineTo(19f)
                close()
            }
        }.build()
    }

    val Calendar: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaCalendar",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 3f)
                horizontalLineTo(18f)
                verticalLineTo(1f)
                horizontalLineTo(16f)
                verticalLineTo(3f)
                horizontalLineTo(8f)
                verticalLineTo(1f)
                horizontalLineTo(6f)
                verticalLineTo(3f)
                horizontalLineTo(5f)
                curveTo(3.89f, 3f, 3.01f, 3.9f, 3.01f, 5f)
                lineTo(3f, 19f)
                curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                verticalLineTo(5f)
                curveTo(21f, 3.9f, 20.1f, 3f, 19f, 3f)
                close()
                moveTo(19f, 19f)
                horizontalLineTo(5f)
                verticalLineTo(8f)
                horizontalLineTo(19f)
                verticalLineTo(19f)
                close()
            }
        }.build()
    }

    val Badge: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaBadge",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 4f)
                horizontalLineTo(4f)
                curveTo(2.9f, 4f, 2f, 4.9f, 2f, 6f)
                verticalLineTo(18f)
                curveTo(2f, 19.1f, 2.9f, 20f, 4f, 20f)
                horizontalLineTo(20f)
                curveTo(21.1f, 20f, 22f, 19.1f, 22f, 18f)
                verticalLineTo(6f)
                curveTo(22f, 4.9f, 21.1f, 4f, 20f, 4f)
                close()
                moveTo(12f, 8f)
                curveTo(13.1f, 8f, 14f, 8.9f, 14f, 10f)
                curveTo(14f, 11.1f, 13.1f, 12f, 12f, 12f)
                curveTo(10.9f, 12f, 10f, 11.1f, 10f, 10f)
                curveTo(10f, 8.9f, 10.9f, 8f, 12f, 8f)
                close()
                moveTo(16f, 16f)
                horizontalLineTo(8f)
                verticalLineTo(15f)
                curveTo(8f, 13.67f, 10.67f, 13f, 12f, 13f)
                curveTo(13.33f, 13f, 16f, 13.67f, 16f, 15f)
                verticalLineTo(16f)
                close()
            }
        }.build()
    }

    val AutoAwesome: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaAutoAwesome",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 9f)
                lineTo(20.25f, 6.25f)
                lineTo(23f, 5f)
                lineTo(20.25f, 3.75f)
                lineTo(19f, 1f)
                lineTo(17.75f, 3.75f)
                lineTo(15f, 5f)
                lineTo(17.75f, 6.25f)
                close()
                moveTo(11.5f, 9.5f)
                lineTo(9f, 4f)
                lineTo(6.5f, 9.5f)
                lineTo(1f, 12f)
                lineTo(6.5f, 14.5f)
                lineTo(9f, 20f)
                lineTo(11.5f, 14.5f)
                lineTo(17f, 12f)
                close()
                moveTo(19f, 15f)
                lineTo(17.75f, 17.75f)
                lineTo(15f, 19f)
                lineTo(17.75f, 20.25f)
                lineTo(19f, 23f)
                lineTo(20.25f, 20.25f)
                lineTo(23f, 19f)
                lineTo(20.25f, 17.75f)
                close()
            }
        }.build()
    }

    val MenuBook: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaMenuBook",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(21f, 5f)
                curveTo(19.89f, 4.65f, 18.67f, 4.5f, 17.5f, 4.5f)
                curveTo(15.55f, 4.5f, 13.45f, 4.9f, 12f, 6f)
                curveTo(10.55f, 4.9f, 8.45f, 4.5f, 6.5f, 4.5f)
                curveTo(4.55f, 4.5f, 2.45f, 4.9f, 1f, 6f)
                verticalLineTo(20.65f)
                curveTo(1f, 20.9f, 1.25f, 21.15f, 1.5f, 21.15f)
                curveTo(1.6f, 21.15f, 1.65f, 21.1f, 1.75f, 21.1f)
                curveTo(3.1f, 20.45f, 5.05f, 20f, 6.5f, 20f)
                curveTo(8.45f, 20f, 10.55f, 20.4f, 12f, 21.5f)
                curveTo(13.35f, 20.65f, 15.8f, 20f, 17.5f, 20f)
                curveTo(19.15f, 20f, 20.85f, 20.3f, 22.25f, 21.1f)
                curveTo(22.35f, 21.15f, 22.4f, 21.15f, 22.5f, 21.15f)
                curveTo(22.75f, 21.15f, 23f, 20.9f, 23f, 20.65f)
                verticalLineTo(6f)
                curveTo(22.45f, 5.55f, 21.75f, 5.25f, 21f, 5f)
                close()
                moveTo(21f, 18.5f)
                curveTo(19.9f, 18.15f, 18.7f, 18f, 17.5f, 18f)
                curveTo(15.8f, 18f, 13.35f, 18.65f, 12f, 19.5f)
                verticalLineTo(8f)
                curveTo(13.35f, 7.15f, 15.8f, 6.5f, 17.5f, 6.5f)
                curveTo(18.7f, 6.5f, 19.9f, 6.65f, 21f, 7f)
                verticalLineTo(18.5f)
                close()
            }
        }.build()
    }

    val EmojiEvents: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaEmojiEvents",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19f, 5f)
                horizontalLineTo(17f)
                verticalLineTo(3f)
                horizontalLineTo(7f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                curveTo(3.9f, 5f, 3f, 5.9f, 3f, 7f)
                verticalLineTo(8f)
                curveTo(3f, 10.55f, 4.92f, 12.64f, 7.39f, 12.94f)
                curveTo(8.02f, 14.12f, 9.13f, 15f, 10.45f, 15.35f)
                verticalLineTo(19f)
                horizontalLineTo(8f)
                verticalLineTo(21f)
                horizontalLineTo(16f)
                verticalLineTo(19f)
                horizontalLineTo(13.55f)
                verticalLineTo(15.35f)
                curveTo(14.87f, 15f, 15.98f, 14.12f, 16.61f, 12.94f)
                curveTo(19.08f, 12.64f, 21f, 10.55f, 21f, 8f)
                verticalLineTo(7f)
                curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
                close()
                moveTo(5f, 8f)
                verticalLineTo(7f)
                horizontalLineTo(7f)
                verticalLineTo(10.82f)
                curveTo(5.84f, 10.4f, 5f, 9.3f, 5f, 8f)
                close()
                moveTo(19f, 8f)
                curveTo(19f, 9.3f, 18.16f, 10.4f, 17f, 10.82f)
                verticalLineTo(7f)
                horizontalLineTo(19f)
                verticalLineTo(8f)
                close()
            }
        }.build()
    }

    val VolumeUp: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaVolumeUp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 9f)
                verticalLineTo(15f)
                horizontalLineTo(7f)
                lineTo(12f, 20f)
                verticalLineTo(4f)
                lineTo(7f, 9f)
                horizontalLineTo(3f)
                close()
                moveTo(16.5f, 12f)
                curveTo(16.5f, 10.23f, 15.48f, 8.71f, 14f, 7.97f)
                verticalLineTo(16.02f)
                curveTo(15.48f, 15.29f, 16.5f, 13.77f, 16.5f, 12f)
                close()
                moveTo(14f, 3.23f)
                verticalLineTo(5.29f)
                curveTo(16.89f, 6.15f, 19f, 8.83f, 19f, 12f)
                curveTo(19f, 15.17f, 16.89f, 17.85f, 14f, 18.71f)
                verticalLineTo(20.77f)
                curveTo(18.01f, 19.86f, 21f, 16.28f, 21f, 12f)
                curveTo(21f, 7.72f, 18.01f, 4.14f, 14f, 3.23f)
                close()
            }
        }.build()
    }

    val ArrowBack: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaArrowBack",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(20f, 11f)
                horizontalLineTo(7.83f)
                lineTo(13.42f, 5.41f)
                lineTo(12f, 4f)
                lineTo(4f, 12f)
                lineTo(12f, 20f)
                lineTo(13.41f, 18.59f)
                lineTo(7.83f, 13f)
                horizontalLineTo(20f)
                verticalLineTo(11f)
                close()
            }
        }.build()
    }

    val Mic: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaMic",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12f, 14f)
                curveTo(13.66f, 14f, 15f, 12.66f, 15f, 11f)
                verticalLineTo(5f)
                curveTo(15f, 3.34f, 13.66f, 2f, 12f, 2f)
                curveTo(10.34f, 2f, 9f, 3.34f, 9f, 5f)
                verticalLineTo(11f)
                curveTo(9f, 12.66f, 10.34f, 14f, 12f, 14f)
                close()
                moveTo(17f, 11f)
                curveTo(17f, 13.76f, 14.76f, 16f, 12f, 16f)
                curveTo(9.24f, 16f, 7f, 13.76f, 7f, 11f)
                horizontalLineTo(5f)
                curveTo(5f, 14.53f, 7.72f, 17.44f, 11f, 17.93f)
                verticalLineTo(21f)
                horizontalLineTo(13f)
                verticalLineTo(17.93f)
                curveTo(16.28f, 17.44f, 19f, 14.53f, 19f, 11f)
                horizontalLineTo(17f)
                close()
            }
        }.build()
    }

    val Refresh: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaRefresh",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(17.65f, 6.35f)
                curveTo(16.2f, 4.9f, 14.21f, 4f, 12f, 4f)
                curveTo(7.58f, 4f, 4.01f, 7.58f, 4.01f, 12f)
                curveTo(4.01f, 16.42f, 7.58f, 20f, 12f, 20f)
                curveTo(15.73f, 20f, 18.84f, 17.45f, 19.73f, 14f)
                horizontalLineTo(17.65f)
                curveTo(16.83f, 16.33f, 14.61f, 18f, 12f, 18f)
                curveTo(8.69f, 18f, 6f, 15.31f, 6f, 12f)
                curveTo(6f, 8.69f, 8.69f, 6f, 12f, 6f)
                curveTo(13.66f, 6f, 15.14f, 6.69f, 16.22f, 7.78f)
                lineTo(13f, 11f)
                horizontalLineTo(20f)
                verticalLineTo(4f)
                lineTo(17.65f, 6.35f)
                close()
            }
        }.build()
    }

    val Edit: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaEdit",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(3f, 17.25f)
                verticalLineTo(21f)
                horizontalLineTo(6.75f)
                lineTo(17.81f, 9.94f)
                lineTo(14.06f, 6.19f)
                lineTo(3f, 17.25f)
                close()
                moveTo(20.71f, 7.04f)
                curveTo(21.1f, 6.65f, 21.1f, 6.02f, 20.71f, 5.63f)
                lineTo(18.37f, 3.29f)
                curveTo(17.98f, 2.9f, 17.35f, 2.9f, 16.96f, 3.29f)
                lineTo(15.13f, 5.12f)
                lineTo(18.88f, 8.87f)
                lineTo(20.71f, 7.04f)
                close()
            }
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaSettings",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(19.14f, 12.94f)
                curveTo(19.18f, 12.63f, 19.2f, 12.32f, 19.2f, 12f)
                curveTo(19.2f, 11.68f, 19.18f, 11.37f, 19.14f, 11.06f)
                lineTo(21.16f, 9.48f)
                curveTo(21.34f, 9.34f, 21.39f, 9.08f, 21.28f, 8.88f)
                lineTo(19.36f, 5.56f)
                curveTo(19.24f, 5.36f, 19f, 5.27f, 18.78f, 5.36f)
                lineTo(16.4f, 6.32f)
                curveTo(15.91f, 5.94f, 15.37f, 5.63f, 14.79f, 5.4f)
                lineTo(14.43f, 2.87f)
                curveTo(14.39f, 2.64f, 14.19f, 2.47f, 13.95f, 2.47f)
                horizontalLineTo(10.05f)
                curveTo(9.81f, 2.47f, 9.61f, 2.64f, 9.57f, 2.87f)
                lineTo(9.21f, 5.4f)
                curveTo(8.63f, 5.63f, 8.09f, 5.94f, 7.6f, 6.32f)
                lineTo(5.22f, 5.36f)
                curveTo(5f, 5.27f, 4.76f, 5.36f, 4.64f, 5.56f)
                lineTo(2.72f, 8.88f)
                curveTo(2.61f, 9.08f, 2.66f, 9.34f, 2.84f, 9.48f)
                lineTo(4.86f, 11.06f)
                curveTo(4.82f, 11.37f, 4.8f, 11.69f, 4.8f, 12f)
                curveTo(4.8f, 12.31f, 4.82f, 12.63f, 4.86f, 12.94f)
                lineTo(2.84f, 14.52f)
                curveTo(2.66f, 14.66f, 2.61f, 14.92f, 2.72f, 15.12f)
                lineTo(4.64f, 18.44f)
                curveTo(4.76f, 18.64f, 5f, 18.73f, 5.22f, 18.64f)
                lineTo(7.6f, 17.68f)
                curveTo(8.09f, 18.06f, 8.63f, 18.37f, 9.21f, 18.6f)
                lineTo(9.57f, 21.13f)
                curveTo(9.61f, 21.36f, 9.81f, 21.53f, 10.05f, 21.53f)
                horizontalLineTo(13.95f)
                curveTo(14.19f, 21.53f, 14.39f, 21.36f, 14.43f, 21.13f)
                lineTo(14.79f, 18.6f)
                curveTo(15.37f, 18.37f, 15.91f, 18.06f, 16.4f, 17.68f)
                lineTo(18.78f, 18.64f)
                curveTo(19f, 18.73f, 19.24f, 18.64f, 19.36f, 18.44f)
                lineTo(21.28f, 15.12f)
                curveTo(21.39f, 14.92f, 21.34f, 14.66f, 21.16f, 14.52f)
                lineTo(19.14f, 12.94f)
                close()
                moveTo(12f, 15.5f)
                curveTo(10.07f, 15.5f, 8.5f, 13.93f, 8.5f, 12f)
                curveTo(8.5f, 10.07f, 10.07f, 8.5f, 12f, 8.5f)
                curveTo(13.93f, 8.5f, 15.5f, 10.07f, 15.5f, 12f)
                curveTo(15.5f, 13.93f, 13.93f, 15.5f, 12f, 15.5f)
                close()
            }
        }.build()
    }

    val ExitToApp: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaExitToApp",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10.09f, 15.59f)
                lineTo(11.5f, 17f)
                lineTo(16.5f, 12f)
                lineTo(11.5f, 7f)
                lineTo(10.09f, 8.41f)
                lineTo(12.67f, 11f)
                horizontalLineTo(3f)
                verticalLineTo(13f)
                horizontalLineTo(12.67f)
                lineTo(10.09f, 15.59f)
                close()
                moveTo(19f, 3f)
                horizontalLineTo(5f)
                curveTo(3.89f, 3f, 3f, 3.89f, 3f, 5f)
                verticalLineTo(9f)
                horizontalLineTo(5f)
                verticalLineTo(5f)
                horizontalLineTo(19f)
                verticalLineTo(19f)
                horizontalLineTo(5f)
                verticalLineTo(15f)
                horizontalLineTo(3f)
                verticalLineTo(19f)
                curveTo(3f, 20.1f, 3.89f, 21f, 5f, 21f)
                horizontalLineTo(19f)
                curveTo(20.1f, 21f, 21f, 20.1f, 21f, 19f)
                verticalLineTo(5f)
                curveTo(21f, 3.89f, 20.1f, 3f, 19f, 3f)
                close()
            }
        }.build()
    }

    val ChevronRight: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaChevronRight",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(10f, 6f)
                lineTo(8.59f, 7.41f)
                lineTo(13.17f, 12f)
                lineTo(8.59f, 16.59f)
                lineTo(10f, 18f)
                lineTo(16f, 12f)
                close()
            }
        }.build()
    }
}
