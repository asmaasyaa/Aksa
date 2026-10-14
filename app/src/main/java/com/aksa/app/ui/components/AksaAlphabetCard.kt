package com.aksa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaGrayBorder
import com.aksa.app.ui.theme.AksaGrayDisabled
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaOrangeDark
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.Poppins

enum class AlphabetCardStatus {
    ACTIVE,
    COMPLETED,
    LOCKED
}

@Composable
fun AksaAlphabetCard(
    letter: String,
    status: AlphabetCardStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 110.dp,
    wordHint: String? = null
) {
    val shape = RoundedCornerShape(22.dp)

    val (bgColor, bottomBevel, textColor) = when (status) {
        AlphabetCardStatus.ACTIVE -> Triple(AksaOrange, AksaOrangeDark, AksaWhite)
        AlphabetCardStatus.COMPLETED -> Triple(AksaWhite, AksaSoftPeach, AksaDarkBrown)
        AlphabetCardStatus.LOCKED -> Triple(Color(0xFFF2F2F5), Color(0xFFE0E0E6), AksaGrayDisabled)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clickable(enabled = status != AlphabetCardStatus.LOCKED, onClick = onClick)
    ) {
        // 3D bottom bevel
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 4.dp)
                .background(bottomBevel, shape)
        )

        // Main surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height - 4.dp)
                .clip(shape)
                .background(bgColor)
                .then(
                    if (status == AlphabetCardStatus.COMPLETED) {
                        Modifier.border(2.dp, AksaOrange.copy(alpha = 0.3f), shape)
                    } else Modifier
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = letter,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    color = textColor
                )

                if (!wordHint.isNullOrEmpty() && status != AlphabetCardStatus.LOCKED) {
                    Text(
                        text = wordHint,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.85f)
                    )
                }
            }

            if (status == AlphabetCardStatus.LOCKED) {
                Icon(
                    imageVector = AksaIcons.Lock,
                    contentDescription = "Terkunci",
                    tint = AksaGrayText,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                )
            } else if (status == AlphabetCardStatus.ACTIVE) {
                Icon(
                    imageVector = AksaIcons.VolumeUp,
                    contentDescription = "Bunyikan",
                    tint = AksaYellow,
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}
