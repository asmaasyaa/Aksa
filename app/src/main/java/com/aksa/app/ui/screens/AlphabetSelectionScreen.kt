package com.aksa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.MascotStanding
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

private data class AlphabetData(
    val letter: String,
    val displayLabel: String,
    val wordHint: String
)

@Composable
fun AlphabetSelectionScreen(
    onNavigateBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onNavigateToPronunciation: (String) -> Unit = {},
    onNavigateToLetterLearning: (String) -> Unit = {},
    onNavigateToHurufMirip: () -> Unit = {}
) {
    val alphabetList = remember {
        listOf(
            "A" to "Apel",
            "B" to "Bola",
            "C" to "Cacing",
            "D" to "Daun",
            "E" to "Ekor",
            "F" to "Foto",
            "G" to "Gajah",
            "H" to "Hujan",
            "I" to "Ikan",
            "J" to "Jeruk",
            "K" to "Kuda",
            "L" to "Lilin",
            "M" to "Mata",
            "N" to "Nasi",
            "O" to "Obat",
            "P" to "Paku",
            "Q" to "Quran",
            "R" to "Roda",
            "S" to "Sapi",
            "T" to "Topi",
            "U" to "Ular",
            "V" to "Vas",
            "W" to "Wortel",
            "X" to "Xilofon",
            "Y" to "Yoyo",
            "Z" to "Zebra"
        ).map { (upper, hint) ->
            AlphabetData(
                letter = upper,
                displayLabel = "$upper${upper.lowercase()}",
                wordHint = hint
            )
        }
    }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
                            painter = AksaIcons.arrowBack(),
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

                MascotStanding(
                    modifier = Modifier.size(46.dp),
                    animated = false
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
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

            Spacer(modifier = Modifier.height(10.dp))

            val featureCardShape = RoundedCornerShape(26.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(elevation = 6.dp, shape = featureCardShape, spotColor = Color(0x33441100))
                    .clip(featureCardShape)
                    .background(AksaWhite)
                    .clickable(onClick = onNavigateToHurufMirip)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(AksaYellow)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Latihan Khusus",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = AksaDarkBrown
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "b vs d • p vs q",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = AksaOrange
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Huruf Mirip",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AksaDarkBrown
                        )

                        Text(
                            text = "Yuk belajar membedakan huruf yang bentuknya hampir sama!",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = AksaDarkBrown.copy(alpha = 0.75f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(AksaYellow.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        MascotStanding(size = 56.dp, animated = false)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(alphabetList) { item ->
                    ContrastAlphabetCard(
                        displayLabel = item.displayLabel,
                        wordHint = item.wordHint,
                        onClick = {
                            onNavigateToLetterLearning(item.letter)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContrastAlphabetCard(
    displayLabel: String,
    wordHint: String,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(26.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(138.dp)
            .shadow(6.dp, cardShape, spotColor = Color(0x33441100))
            .clip(cardShape)
            .background(AksaWhite)
            .border(2.dp, AksaYellow, cardShape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .clip(CircleShape)
                .background(AksaPeachLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = AksaIcons.volumeUp(),
                contentDescription = null,
                tint = AksaOrange,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = displayLabel,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 38.sp,
                color = AksaDarkBrown
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = wordHint,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = AksaOrange
            )
        }
    }
}