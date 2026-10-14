package com.aksa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaOrangeDark
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.Poppins

enum class SyllableCardColor {
    GREEN,
    YELLOW,
    ORANGE,
    WHITE
}

@Composable
fun AksaSyllableCard(
    syllable: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    colorTheme: SyllableCardColor = SyllableCardColor.YELLOW,
    width: Dp = 80.dp,
    height: Dp = 64.dp
) {
    val shape = RoundedCornerShape(18.dp)

    val (bg, bevel, text, border) = when (colorTheme) {
        SyllableCardColor.GREEN -> QuadSyllableColors(AksaLime, AksaLimeDark, AksaDarkBrown, Color.Transparent)
        SyllableCardColor.YELLOW -> QuadSyllableColors(AksaYellow, AksaYellowDark, AksaDarkBrown, Color.Transparent)
        SyllableCardColor.ORANGE -> QuadSyllableColors(AksaOrange, AksaOrangeDark, AksaWhite, Color.Transparent)
        SyllableCardColor.WHITE -> QuadSyllableColors(AksaWhite, AksaSoftPeach, AksaDarkBrown, AksaSoftPeach)
    }

    val finalBg = if (isSelected) AksaOrange else bg
    val finalBevel = if (isSelected) AksaOrangeDark else bevel
    val finalTextColor = if (isSelected) AksaWhite else text

    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clickable(onClick = onClick)
    ) {
        // 3D bevel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .background(finalBevel, shape)
        )

        // Main tile surface
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
                .clip(shape)
                .background(finalBg)
                .then(
                    if (border != Color.Transparent && !isSelected) {
                        Modifier.border(2.dp, border, shape)
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = syllable,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = finalTextColor
            )
        }
    }
}

private data class QuadSyllableColors(
    val bg: Color,
    val bevel: Color,
    val text: Color,
    val border: Color
)
