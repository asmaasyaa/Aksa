package com.aksa.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaSyllableCard
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.SyllableCardColor
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

@Composable
fun SyllableQuizScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit
) {
    var slot1 by remember { mutableStateOf<String?>(null) }
    var slot2 by remember { mutableStateOf<String?>(null) }

    val choices = listOf("KU", "CA", "MA", "GA", "SO", "TA")
    val isCorrect = slot1 == "MA" && slot2 == "TA"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaOrange)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // "Kembali" pill
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

                // Progress Badge: Soal 1 dari 5
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AksaWhite)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Soal 1 dari 5",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large White Learning Card with Eyes Illustration
            val cardShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, cardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, cardShape)
                    .padding(22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Illustration Card (Mata)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(AksaPeachLight),
                        contentAlignment = Alignment.Center
                    ) {
                        EyesIllustration()
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Susun kata sesuai gambar!",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AksaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // TWO ANSWER SLOTS
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnswerSlot(
                            value = slot1,
                            onClear = { slot1 = null }
                        )

                        Text(
                            text = "|",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = AksaDarkBrown.copy(alpha = 0.4f)
                        )

                        AnswerSlot(
                            value = slot2,
                            onClear = { slot2 = null }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Syllable Choices Title
                    Text(
                        text = "Pilih suku kata di bawah:",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = AksaDarkBrown.copy(alpha = 0.65f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2x3 Grid of Yellow Syllable Choices (KU, CA, MA, GA, SO, TA)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (i in 0..2) {
                                val s = choices[i]
                                AksaSyllableCard(
                                    syllable = s,
                                    colorTheme = SyllableCardColor.YELLOW,
                                    isSelected = slot1 == s || slot2 == s,
                                    width = 84.dp,
                                    height = 58.dp,
                                    onClick = {
                                        if (slot1 == null) {
                                            slot1 = s
                                        } else if (slot2 == null && slot1 != s) {
                                            slot2 = s
                                        }
                                    }
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (i in 3..5) {
                                val s = choices[i]
                                AksaSyllableCard(
                                    syllable = s,
                                    colorTheme = SyllableCardColor.YELLOW,
                                    isSelected = slot1 == s || slot2 == s,
                                    width = 84.dp,
                                    height = 58.dp,
                                    onClick = {
                                        if (slot1 == null) {
                                            slot1 = s
                                        } else if (slot2 == null && slot1 != s) {
                                            slot2 = s
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Green Success Feedback Banner when correct
                    AnimatedVisibility(
                        visible = isCorrect,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 22.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(AksaLime)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = AksaIcons.Check,
                                        contentDescription = null,
                                        tint = AksaDarkBrown,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Benar! Ini adalah MATA.",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AksaDarkBrown
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Primary Button: Selanjutnya
                            AksaButton(
                                text = "Selanjutnya",
                                style = AksaButtonStyle.PRIMARY_ORANGE,
                                onClick = onNavigateToLevelMap
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerSlot(
    value: String?,
    onClear: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val isFilled = !value.isNullOrEmpty()

    Box(
        modifier = Modifier
            .size(width = 86.dp, height = 62.dp)
            .clip(shape)
            .background(if (isFilled) AksaLime else AksaPeachLight)
            .border(
                width = 2.dp,
                color = if (isFilled) AksaLime else AksaOrange.copy(alpha = 0.5f),
                shape = shape
            )
            .clickable(enabled = isFilled, onClick = onClear),
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            Text(
                text = value ?: "",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = AksaDarkBrown
            )
        } else {
            Text(
                text = "—",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = AksaDarkBrown.copy(alpha = 0.35f)
            )
        }
    }
}
