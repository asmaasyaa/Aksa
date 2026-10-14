package com.aksa.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeLight
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaOrangeLight
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape

enum class ProgressBarColor {
    LIME,
    ORANGE,
    YELLOW
}

@Composable
fun AksaProgressBar(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    colorTheme: ProgressBarColor = ProgressBarColor.LIME,
    backgroundColor: Color = AksaPeachLight
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    val gradientBrush = when (colorTheme) {
        ProgressBarColor.LIME -> Brush.horizontalGradient(
            colors = listOf(AksaLime, AksaLimeLight)
        )
        ProgressBarColor.ORANGE -> Brush.horizontalGradient(
            colors = listOf(AksaOrange, AksaOrangeLight)
        )
        ProgressBarColor.YELLOW -> Brush.horizontalGradient(
            colors = listOf(AksaYellow, Color(0xFFFFE082))
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(PillShape)
            .background(backgroundColor)
            .border(1.5.dp, Color(0x1F6B240C), PillShape)
            .padding(2.5.dp)
    ) {
        if (animatedProgress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(PillShape)
                    .background(gradientBrush)
            )
        }
    }
}
