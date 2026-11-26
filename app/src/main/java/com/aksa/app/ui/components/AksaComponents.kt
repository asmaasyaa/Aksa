package com.aksa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

@Composable
fun EarnedStarsRow(
    accuracy: Float,
    modifier: Modifier = Modifier
) {
    val earnedStars = when {
        accuracy >= 0.8f -> 3
        accuracy >= 0.5f -> 2
        accuracy > 0f -> 1
        else -> 0
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..3) {
            val isEarned = i <= earnedStars
            Icon(
                painter = AksaIcons.star(),
                contentDescription = "Bintang $i",
                tint = if (isEarned) Color(0xFFFFB300) else Color(0xFFD6DCE2),
                modifier = Modifier.size(if (i == 2) 44.dp else 36.dp)
            )
        }
    }
}

/**
 * Standardized exit confirmation dialog for mini-games and learning sessions.
 * Enforces centered text alignment for all title, description, and button elements.
 */
@Composable
fun AksaExitConfirmationDialog(
    isMiniGame: Boolean,
    onContinue: () -> Unit,
    onExit: () -> Unit
) {
    val title = "Yakin ingin keluar?"
    val description = if (isMiniGame) {
        "Permainanmu belum selesai. Apakah kamu yakin ingin keluar?"
    } else {
        "Belajarmu belum selesai. Apakah kamu yakin ingin keluar dari sesi belajar?"
    }
    val continueText = if (isMiniGame) "Lanjut Bermain" else "Lanjut Belajar"
    val exitText = "Ya, Keluar"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        val dialogShape = RoundedCornerShape(32.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .shadow(16.dp, dialogShape)
                .background(AksaWhite, dialogShape)
                .padding(26.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                MascotStanding(size = 110.dp)

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = title,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = AksaDarkBrown,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = AksaDarkBrown.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                AksaButton(
                    text = continueText,
                    style = AksaButtonStyle.PRIMARY_ORANGE,
                    onClick = onContinue,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .clickable(onClick = onExit)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = exitText,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = AksaOrange,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
