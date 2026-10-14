package com.aksa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaAlphabetCard
import com.aksa.app.ui.components.AlphabetCardStatus
import com.aksa.app.ui.components.MascotPeeking
import com.aksa.app.ui.components.MascotStanding
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

private data class AlphabetData(
    val letter: String,
    val wordHint: String,
    val status: AlphabetCardStatus
)

@Composable
fun AlphabetSelectionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPronunciation: (String) -> Unit
) {
    val alphabetList = listOf(
        AlphabetData("A", "Apel", AlphabetCardStatus.ACTIVE),
        AlphabetData("B", "Bola", AlphabetCardStatus.COMPLETED),
        AlphabetData("C", "Cacing", AlphabetCardStatus.COMPLETED),
        AlphabetData("D", "Daun", AlphabetCardStatus.COMPLETED),
        AlphabetData("E", "Ekor", AlphabetCardStatus.COMPLETED),
        AlphabetData("F", "Foto", AlphabetCardStatus.COMPLETED),
        AlphabetData("G", "Gajah", AlphabetCardStatus.LOCKED),
        AlphabetData("H", "Hujan", AlphabetCardStatus.LOCKED),
        AlphabetData("I", "Ikan", AlphabetCardStatus.LOCKED),
        AlphabetData("J", "Jeruk", AlphabetCardStatus.LOCKED),
        AlphabetData("K", "Kuda", AlphabetCardStatus.LOCKED),
        AlphabetData("L", "Lilin", AlphabetCardStatus.LOCKED),
        AlphabetData("M", "Mata", AlphabetCardStatus.LOCKED),
        AlphabetData("N", "Nasi", AlphabetCardStatus.LOCKED)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaOrange)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // "Kembali" pill button
                Box(
                    modifier = Modifier
                        .shadow(4.dp, PillShape)
                        .clip(PillShape)
                        .background(AksaWhite)
                        .clickable(onClick = onNavigateBack)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = AksaIcons.ArrowBack,
                            contentDescription = "Kembali",
                            tint = AksaDarkBrown,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kembali",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AksaDarkBrown
                        )
                    }
                }

                // Mascot mini badge
                MascotStanding(
                    modifier = Modifier.size(46.dp),
                    animated = false
                )
            }

            // Header Titles
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Pulau Alfabet",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = AksaWhite
                )

                Text(
                    text = "Pilih huruf yang ingin kamu pelajari!",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = AksaWhite.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Two-column Alphabet Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(alphabetList) { item ->
                    AksaAlphabetCard(
                        letter = item.letter,
                        wordHint = item.wordHint,
                        status = item.status,
                        onClick = {
                            onNavigateToPronunciation(item.letter)
                        }
                    )
                }
            }
        }
    }
}
