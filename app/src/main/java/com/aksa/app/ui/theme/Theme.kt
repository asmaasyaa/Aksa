package com.aksa.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val AksaColorScheme = lightColorScheme(
    primary = AksaOrange,
    onPrimary = AksaWhite,
    primaryContainer = AksaSoftPeach,
    onPrimaryContainer = AksaDarkBrown,
    secondary = AksaLime,
    onSecondary = AksaDarkBrown,
    secondaryContainer = AksaLimeLight,
    onSecondaryContainer = AksaDarkBrown,
    tertiary = AksaYellow,
    onTertiary = AksaDarkBrown,
    tertiaryContainer = AksaYellowLight,
    onTertiaryContainer = AksaDarkBrown,
    background = AksaOrange,
    onBackground = AksaWhite,
    surface = AksaWhite,
    onSurface = AksaDarkBrown,
    surfaceVariant = AksaPeachLight,
    onSurfaceVariant = AksaBrownLight,
    outline = AksaGrayBorder
)

@Composable
fun AksaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AksaColorScheme,
        typography = AksaTypography,
        shapes = AksaShapes,
        content = content
    )
}
