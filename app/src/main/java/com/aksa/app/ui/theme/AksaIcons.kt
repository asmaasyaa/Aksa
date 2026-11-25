package com.aksa.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.aksa.app.R

object AksaIcons {
    @Composable fun star(): Painter = painterResource(R.drawable.ic_star)
    @Composable fun check(): Painter = painterResource(R.drawable.ic_check)
    @Composable fun lock(): Painter = painterResource(R.drawable.ic_lock)
    @Composable fun face(): Painter = painterResource(R.drawable.ic_face)
    @Composable fun person(): Painter = painterResource(R.drawable.ic_person)
    @Composable fun info(): Painter = painterResource(R.drawable.ic_info)
    @Composable fun favorite(): Painter = painterResource(R.drawable.ic_favorite)
    @Composable fun playArrow(): Painter = painterResource(R.drawable.ic_play_arrow)
    @Composable fun arrowForward(): Painter = painterResource(R.drawable.ic_arrow_forward)
    @Composable fun arrowBack(): Painter = painterResource(R.drawable.ic_arrow_back)
    @Composable fun email(): Painter = painterResource(R.drawable.ic_email)
    @Composable fun visibility(): Painter = painterResource(R.drawable.ic_visibility)
    @Composable fun visibilityOff(): Painter = painterResource(R.drawable.ic_visibility_off)
    @Composable fun flame(): Painter = painterResource(R.drawable.ic_local_fire_department)
    @Composable fun map(): Painter = painterResource(R.drawable.ic_map)
    @Composable fun calendar(): Painter = painterResource(R.drawable.ic_calendar_month)
    @Composable fun badge(): Painter = painterResource(R.drawable.ic_badge)
    @Composable fun autoAwesome(): Painter = painterResource(R.drawable.ic_auto_awesome)
    @Composable fun menuBook(): Painter = painterResource(R.drawable.ic_menu_book)
    @Composable fun emojiEvents(): Painter = painterResource(R.drawable.ic_emoji_events)
    @Composable fun volumeUp(): Painter = painterResource(R.drawable.ic_volume_up)
    @Composable fun mic(): Painter = painterResource(R.drawable.ic_mic)
    @Composable fun refresh(): Painter = painterResource(R.drawable.ic_refresh)
    @Composable fun edit(): Painter = painterResource(R.drawable.ic_edit)
    @Composable fun settings(): Painter = painterResource(R.drawable.ic_settings)
    @Composable fun logout(): Painter = painterResource(R.drawable.ic_logout)
    @Composable fun chevronRight(): Painter = painterResource(R.drawable.ic_chevron_right)
    @Composable fun delete(): Painter = painterResource(R.drawable.ic_delete)
    @Composable fun warning(): Painter = painterResource(R.drawable.ic_warning)


    val Google: ImageVector by lazy {
        ImageVector.Builder(
            name = "AksaGoogle",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color(0xFF4285F4))) {
                moveTo(21.805f, 10.023f)
                horizontalLineTo(21f)
                verticalLineTo(10f)
                horizontalLineTo(12f)
                verticalLineTo(14f)
                horizontalLineTo(17.651f)
                curveTo(16.827f, 16.337f, 14.611f, 18f, 12f, 18f)
                curveTo(8.686f, 18f, 6f, 15.314f, 6f, 12f)
                curveTo(6f, 8.686f, 8.686f, 6f, 12f, 6f)
                curveTo(13.519f, 6f, 14.901f, 6.577f, 15.955f, 7.521f)
                lineTo(18.769f, 4.707f)
                curveTo(17.023f, 3.072f, 14.63f, 2f, 12f, 2f)
                curveTo(6.477f, 2f, 2f, 6.477f, 2f, 12f)
                curveTo(2f, 17.523f, 6.477f, 22f, 12f, 22f)
                curveTo(17.523f, 22f, 22f, 17.523f, 22f, 12f)
                curveTo(22f, 11.327f, 21.931f, 10.67f, 21.805f, 10.023f)
                close()
            }
            path(fill = SolidColor(Color(0xFF34A853))) {
                moveTo(3.153f, 7.345f)
                lineTo(6.439f, 9.755f)
                curveTo(7.325f, 7.554f, 9.481f, 6f, 12f, 6f)
                curveTo(13.519f, 6f, 14.901f, 6.577f, 15.956f, 7.521f)
                lineTo(18.769f, 4.707f)
                curveTo(17.023f, 3.072f, 14.631f, 2f, 12f, 2f)
                curveTo(8.159f, 2f, 4.828f, 4.168f, 3.153f, 7.345f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFBBC05))) {
                moveTo(12f, 22f)
                curveTo(14.583f, 22f, 16.93f, 21.012f, 18.665f, 19.418f)
                lineTo(15.609f, 16.808f)
                curveTo(14.572f, 17.574f, 13.339f, 18.001f, 12f, 18f)
                curveTo(9.4f, 18f, 7.19f, 16.35f, 6.359f, 14.027f)
                lineTo(3.097f, 16.54f)
                curveTo(4.752f, 19.778f, 8.111f, 22f, 12f, 22f)
                close()
            }
            path(fill = SolidColor(Color(0xFFEB4335))) {
                moveTo(21.805f, 10.023f)
                horizontalLineTo(21f)
                verticalLineTo(10f)
                horizontalLineTo(12f)
                verticalLineTo(14f)
                horizontalLineTo(17.651f)
                curveTo(17.257f, 15.092f, 16.549f, 16.036f, 15.608f, 16.809f)
                lineTo(15.609f, 16.808f)
                lineTo(18.665f, 19.418f)
                curveTo(18.455f, 19.61f, 22f, 17f, 22f, 12f)
                curveTo(22f, 11.327f, 21.931f, 10.67f, 21.805f, 10.023f)
                close()
            }
        }.build()
    }
}
