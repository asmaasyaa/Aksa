package com.aksa.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aksa.app.R

enum class MascotVariant(@DrawableRes val drawableResId: Int) {
    PEEKING(R.drawable.mascot_peeking),
    STANDING(R.drawable.mascot_standing),
    LEARNING(R.drawable.mascot_learning),
    CHEERING(R.drawable.mascot_cheering),
    HEADBAND(R.drawable.mascot_headband)
}

@Composable
fun AksaMascot(
    modifier: Modifier = Modifier,
    variant: MascotVariant = MascotVariant.STANDING,
    animated: Boolean = true
) {
    val bobOffset by if (animated) {
        val infiniteTransition = rememberInfiniteTransition(label = "MascotAnimation")
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "MascotBob"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    Image(
        painter = painterResource(id = variant.drawableResId),
        contentDescription = "Maskot Aksa",
        contentScale = ContentScale.Fit,
        modifier = modifier.graphicsLayer {
            translationY = bobOffset
        }
    )
}

@Composable
fun MascotPeeking(
    modifier: Modifier = Modifier,
    width: Dp = 160.dp,
    height: Dp = 100.dp,
    animated: Boolean = true
) {
    AksaMascot(
        modifier = modifier
            .width(width)
            .height(height),
        variant = MascotVariant.PEEKING,
        animated = animated
    )
}

@Composable
fun MascotStanding(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    animated: Boolean = true
) {
    AksaMascot(
        modifier = modifier.size(size),
        variant = MascotVariant.STANDING,
        animated = animated
    )
}

@Composable
fun MascotLearning(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    animated: Boolean = true
) {
    AksaMascot(
        modifier = modifier.size(size),
        variant = MascotVariant.LEARNING,
        animated = animated
    )
}

@Composable
fun MascotCheering(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    animated: Boolean = true
) {
    AksaMascot(
        modifier = modifier.size(size),
        variant = MascotVariant.CHEERING,
        animated = animated
    )
}

@Composable
fun MascotHeadband(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    animated: Boolean = true
) {
    AksaMascot(
        modifier = modifier.size(size),
        variant = MascotVariant.HEADBAND,
        animated = animated
    )
}
