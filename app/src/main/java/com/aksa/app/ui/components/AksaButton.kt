package com.aksa.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayBorder
import com.aksa.app.ui.theme.AksaGrayDisabled
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaOrangeDark
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.Poppins

enum class AksaButtonStyle {
    PRIMARY_ORANGE,
    SECONDARY_YELLOW,
    SECONDARY_LIME,
    WHITE_CARD,
    OUTLINE,
    SOCIAL
}

/**
 * High-fidelity, child-friendly tactile button with 3D press effect.
 */
@Composable
fun AksaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AksaButtonStyle = AksaButtonStyle.PRIMARY_ORANGE,
    icon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 56.dp,
    fontSize: TextUnit = 17.sp,
    cornerRadius: Dp = 26.dp
) {
    var isPressed by remember { mutableStateOf(false) }
    val pressTranslation by animateFloatAsState(
        targetValue = if (isPressed) 4f else 0f,
        label = "ButtonPress"
    )

    val (bgColor, bottomBevelColor, textColor, borderColor) = when (style) {
        AksaButtonStyle.PRIMARY_ORANGE -> QuadColor(
            AksaOrange,
            AksaOrangeDark,
            AksaWhite,
            Color.Transparent
        )
        AksaButtonStyle.SECONDARY_YELLOW -> QuadColor(
            AksaYellow,
            AksaYellowDark,
            AksaDarkBrown,
            Color.Transparent
        )
        AksaButtonStyle.SECONDARY_LIME -> QuadColor(
            AksaLime,
            AksaLimeDark,
            AksaDarkBrown,
            Color.Transparent
        )
        AksaButtonStyle.WHITE_CARD -> QuadColor(
            AksaWhite,
            AksaGrayBorder,
            AksaDarkBrown,
            AksaGrayBorder
        )
        AksaButtonStyle.OUTLINE -> QuadColor(
            Color.Transparent,
            Color.Transparent,
            AksaWhite,
            AksaWhite
        )
        AksaButtonStyle.SOCIAL -> QuadColor(
            AksaWhite,
            AksaGrayBorder,
            AksaDarkBrown,
            AksaGrayBorder
        )
    }

    val finalBg = if (enabled) bgColor else AksaGrayDisabled
    val finalBevel = if (enabled) bottomBevelColor else AksaGrayDisabled
    val finalTextColor = if (enabled) textColor else AksaGrayText

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .graphicsLayer {
                translationY = pressTranslation
            }
            .pointerInput(enabled) {
                if (enabled) {
                    awaitEachGesture {
                        awaitFirstDown()
                        isPressed = true
                        val up = waitForUpOrCancellation()
                        isPressed = false
                        if (up != null) {
                            onClick()
                        }
                    }
                }
            }
    ) {
        // Bottom bevel shadow (3D effect)
        if (style != AksaButtonStyle.OUTLINE && enabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .padding(top = 4.dp)
                    .background(finalBevel, shape)
            )
        }

        // Top button surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height - 4.dp)
                .background(finalBg, shape)
                .then(
                    if (borderColor != Color.Transparent) {
                        Modifier.clip(shape).background(finalBg, shape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = finalTextColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = text,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    color = finalTextColor
                )

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = finalTextColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

private data class QuadColor(
    val bg: Color,
    val bevel: Color,
    val text: Color,
    val border: Color
)
