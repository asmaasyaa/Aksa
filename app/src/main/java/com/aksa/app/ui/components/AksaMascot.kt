package com.aksa.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaPeachDark
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark

/**
 * Reusable AKSA Mascot Variants
 */
enum class MascotVariant {
    PEEKING,
    STANDING,
    LEARNING,
    CHEERING,
    HEADBAND
}

/**
 * The official AKSA mascot:
 * A friendly, soft white blob/bear-like creature with round ears,
 * dark brown vertical oval eyes, peach cheeks, and flat vector aesthetic.
 */
@Composable
fun AksaMascot(
    modifier: Modifier = Modifier,
    variant: MascotVariant = MascotVariant.STANDING,
    animated: Boolean = true
) {
    // Gentle breathing / bobbing animation
    val infiniteTransition = rememberInfiniteTransition(label = "MascotAnimation")
    val bobOffset by if (animated) {
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

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        when (variant) {
            MascotVariant.PEEKING -> drawMascotPeeking(w, h, bobOffset)
            MascotVariant.STANDING -> drawMascotStanding(w, h, bobOffset, withHeadband = false, isCheering = false, isLearning = false)
            MascotVariant.LEARNING -> drawMascotStanding(w, h, bobOffset, withHeadband = false, isCheering = false, isLearning = true)
            MascotVariant.CHEERING -> drawMascotStanding(w, h, bobOffset, withHeadband = false, isCheering = true, isLearning = false)
            MascotVariant.HEADBAND -> drawMascotStanding(w, h, bobOffset, withHeadband = true, isCheering = false, isLearning = false)
        }
    }
}

// ==============================================================================
// DRAWING IMPLEMENTATIONS
// ==============================================================================

private fun DrawScope.drawMascotPeeking(w: Float, h: Float, bob: Float) {
    val cx = w / 2f
    val base = h
    val headRadius = w * 0.38f
    val headCenterY = base - headRadius * 0.45f + bob

    // Ears
    val earRadius = headRadius * 0.32f
    val earOffsetY = headCenterY - headRadius * 0.85f
    val earSpacing = headRadius * 0.72f

    // Left ear
    drawCircle(
        color = AksaWhite,
        radius = earRadius,
        center = Offset(cx - earSpacing, earOffsetY)
    )
    drawCircle(
        color = AksaSoftPeach,
        radius = earRadius * 0.55f,
        center = Offset(cx - earSpacing, earOffsetY)
    )

    // Right ear
    drawCircle(
        color = AksaWhite,
        radius = earRadius,
        center = Offset(cx + earSpacing, earOffsetY)
    )
    drawCircle(
        color = AksaSoftPeach,
        radius = earRadius * 0.55f,
        center = Offset(cx + earSpacing, earOffsetY)
    )

    // Head blob (large rounded dome)
    val headPath = Path().apply {
        val rect = Rect(cx - headRadius, headCenterY - headRadius, cx + headRadius, headCenterY + headRadius * 1.2f)
        addRoundRect(RoundRect(rect, CornerRadius(headRadius * 0.95f, headRadius * 0.95f)))
    }
    drawPath(path = headPath, color = AksaWhite)

    // Face elements
    drawFaceFeatures(cx, headCenterY - headRadius * 0.05f, headRadius)

    // Tiny rounded white paws holding the bottom edge
    val pawRadius = headRadius * 0.22f
    val pawY = base - pawRadius * 0.5f

    // Left paw
    drawCircle(
        color = AksaWhite,
        radius = pawRadius,
        center = Offset(cx - headRadius * 0.65f, pawY)
    )
    // Paw subtle outline/shadow line at bottom
    drawArc(
        color = AksaPeachDark.copy(alpha = 0.5f),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx - headRadius * 0.65f - pawRadius, pawY - pawRadius),
        size = Size(pawRadius * 2f, pawRadius * 2f),
        style = Stroke(width = 3f)
    )

    // Right paw
    drawCircle(
        color = AksaWhite,
        radius = pawRadius,
        center = Offset(cx + headRadius * 0.65f, pawY)
    )
    drawArc(
        color = AksaPeachDark.copy(alpha = 0.5f),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx + headRadius * 0.65f - pawRadius, pawY - pawRadius),
        size = Size(pawRadius * 2f, pawRadius * 2f),
        style = Stroke(width = 3f)
    )
}

private fun DrawScope.drawMascotStanding(
    w: Float,
    h: Float,
    bob: Float,
    withHeadband: Boolean,
    isCheering: Boolean,
    isLearning: Boolean
) {
    val cx = w / 2f
    val cy = h / 2f + bob
    val scale = minOf(w, h) / 100f

    // Soft drop shadow beneath mascot
    val shadowW = scale * 50f
    val shadowH = scale * 12f
    val shadowY = h - scale * 6f
    drawOval(
        color = Color(0x18000000),
        topLeft = Offset(cx - shadowW / 2f, shadowY - shadowH / 2f),
        size = Size(shadowW, shadowH)
    )

    // Tiny rounded feet
    val footRadius = scale * 8.5f
    val footY = cy + scale * 34f
    drawCircle(color = AksaWhite, radius = footRadius, center = Offset(cx - scale * 14f, footY))
    drawCircle(color = AksaWhite, radius = footRadius, center = Offset(cx + scale * 14f, footY))

    // Ears
    val earRadius = scale * 11f
    val earOffsetY = cy - scale * 26f
    val earSpacing = scale * 22f

    // Left ear
    drawCircle(color = AksaWhite, radius = earRadius, center = Offset(cx - earSpacing, earOffsetY))
    drawCircle(color = AksaSoftPeach, radius = earRadius * 0.55f, center = Offset(cx - earSpacing, earOffsetY))

    // Right ear
    drawCircle(color = AksaWhite, radius = earRadius, center = Offset(cx + earSpacing, earOffsetY))
    drawCircle(color = AksaSoftPeach, radius = earRadius * 0.55f, center = Offset(cx + earSpacing, earOffsetY))

    // Main Body (soft rounded blob / pear silhouette)
    val bodyPath = Path().apply {
        // Head dome + soft wide belly
        val top = cy - scale * 28f
        val bottom = cy + scale * 35f
        val left = cx - scale * 28f
        val right = cx + scale * 28f

        moveTo(cx, top)
        // Top right head curve
        cubicTo(cx + scale * 22f, top, right, cy - scale * 10f, right, cy + scale * 10f)
        // Bottom right hip / belly
        cubicTo(right + scale * 4f, cy + scale * 25f, cx + scale * 20f, bottom, cx, bottom)
        // Bottom left hip / belly
        cubicTo(cx - scale * 20f, bottom, left - scale * 4f, cy + scale * 25f, left, cy + scale * 10f)
        // Top left head curve
        cubicTo(left, cy - scale * 10f, cx - scale * 22f, top, cx, top)
        close()
    }
    drawPath(path = bodyPath, color = AksaWhite)

    // Headband if active
    if (withHeadband) {
        val bandY = cy - scale * 14f
        val bandHeight = scale * 7.5f
        val bandPath = Path().apply {
            moveTo(cx - scale * 26f, bandY)
            lineTo(cx + scale * 26f, bandY)
            lineTo(cx + scale * 25f, bandY + bandHeight)
            lineTo(cx - scale * 25f, bandY + bandHeight)
            close()
        }
        drawPath(path = bandPath, color = AksaYellow)
        // Headband knot on the right side
        drawCircle(color = AksaYellowDark, radius = scale * 4.5f, center = Offset(cx + scale * 27f, bandY + bandHeight * 0.5f))
        drawCircle(color = AksaYellow, radius = scale * 3.5f, center = Offset(cx + scale * 29f, bandY + bandHeight * 0.2f))
        drawCircle(color = AksaYellow, radius = scale * 3.5f, center = Offset(cx + scale * 29f, bandY + bandHeight * 0.8f))
    }

    // Arms
    val armRadius = scale * 7.5f
    when {
        isCheering -> {
            // Arms raised high in celebration
            val leftArmCenter = Offset(cx - scale * 27f, cy - scale * 12f)
            val rightArmCenter = Offset(cx + scale * 27f, cy - scale * 12f)
            drawCircle(color = AksaWhite, radius = armRadius, center = leftArmCenter)
            drawCircle(color = AksaWhite, radius = armRadius, center = rightArmCenter)
        }
        isLearning -> {
            // Mascot holding a cute book in front
            val leftArmCenter = Offset(cx - scale * 16f, cy + scale * 16f)
            val rightArmCenter = Offset(cx + scale * 16f, cy + scale * 16f)
            // Little green learning book
            val bookW = scale * 26f
            val bookH = scale * 20f
            val bookLeft = cx - bookW / 2f
            val bookTop = cy + scale * 6f
            drawRoundRect(
                color = AksaLime,
                topLeft = Offset(bookLeft, bookTop),
                size = Size(bookW, bookH),
                cornerRadius = CornerRadius(scale * 3f, scale * 3f)
            )
            // Book pages spine
            drawLine(
                color = AksaWhite,
                start = Offset(cx, bookTop),
                end = Offset(cx, bookTop + bookH),
                strokeWidth = scale * 2f
            )
            // Little star or letter 'A' on the book cover
            drawCircle(color = AksaYellow, radius = scale * 3f, center = Offset(cx - scale * 6f, bookTop + bookH * 0.5f))

            // Tiny paws holding book
            drawCircle(color = AksaWhite, radius = armRadius * 0.8f, center = leftArmCenter)
            drawCircle(color = AksaWhite, radius = armRadius * 0.8f, center = rightArmCenter)
        }
        else -> {
            // Normal standing arms at sides
            val leftArmCenter = Offset(cx - scale * 25f, cy + scale * 10f)
            val rightArmCenter = Offset(cx + scale * 25f, cy + scale * 10f)
            drawCircle(color = AksaWhite, radius = armRadius, center = leftArmCenter)
            drawCircle(color = AksaWhite, radius = armRadius, center = rightArmCenter)
        }
    }

    // Face elements
    val faceY = if (withHeadband) cy + scale * 0.5f else cy - scale * 2f
    drawFaceFeatures(cx, faceY, scale * 30f, isCheering = isCheering)
}

/**
 * Universal facial features for AKSA mascot:
 * - Dark brown vertical oval eyes with sparkle dot
 * - Peach circular cheeks
 * - Soft minimal smile
 */
private fun DrawScope.drawFaceFeatures(
    cx: Float,
    cy: Float,
    radius: Float,
    isCheering: Boolean = false
) {
    val eyeW = radius * 0.14f
    val eyeH = radius * 0.24f
    val eyeSpacing = radius * 0.36f
    val eyeY = cy - radius * 0.12f

    // Left Eye (Vertical Oval)
    drawOval(
        color = AksaDarkBrown,
        topLeft = Offset(cx - eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f),
        size = Size(eyeW, eyeH)
    )
    // Left eye sparkle
    drawCircle(
        color = AksaWhite,
        radius = eyeW * 0.28f,
        center = Offset(cx - eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f)
    )

    // Right Eye (Vertical Oval)
    drawOval(
        color = AksaDarkBrown,
        topLeft = Offset(cx + eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f),
        size = Size(eyeW, eyeH)
    )
    // Right eye sparkle
    drawCircle(
        color = AksaWhite,
        radius = eyeW * 0.28f,
        center = Offset(cx + eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f)
    )

    // Peach Circular Cheeks
    val cheekRadius = radius * 0.22f
    val cheekY = cy + radius * 0.14f
    val cheekSpacing = radius * 0.54f

    drawCircle(
        color = AksaSoftPeach,
        radius = cheekRadius,
        center = Offset(cx - cheekSpacing, cheekY)
    )
    drawCircle(
        color = AksaSoftPeach,
        radius = cheekRadius,
        center = Offset(cx + cheekSpacing, cheekY)
    )

    // Tiny cute smile
    val mouthY = cy + radius * 0.16f
    if (isCheering) {
        // Cheerful open mouth
        val mouthPath = Path().apply {
            moveTo(cx - radius * 0.16f, mouthY)
            quadraticTo(cx, mouthY + radius * 0.22f, cx + radius * 0.16f, mouthY)
            close()
        }
        drawPath(path = mouthPath, color = AksaDarkBrown)
        // Cute pink tongue
        drawCircle(color = AksaSoftPeach, radius = radius * 0.08f, center = Offset(cx, mouthY + radius * 0.13f))
    } else {
        // Gentle happy curved line
        val mouthPath = Path().apply {
            moveTo(cx - radius * 0.12f, mouthY)
            quadraticTo(cx, mouthY + radius * 0.10f, cx + radius * 0.12f, mouthY)
        }
        drawPath(
            path = mouthPath,
            color = AksaDarkBrown,
            style = Stroke(width = radius * 0.05f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

// ==============================================================================
// CONVENIENCE COMPOSABLES
// ==============================================================================

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
