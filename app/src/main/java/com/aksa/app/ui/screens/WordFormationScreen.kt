package com.aksa.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

@Composable
fun WordFormationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit
) {
    val targetWord = listOf("B", "O", "L", "A")
    val shuffledChoices = remember { listOf("L", "A", "B", "O") }
    val selectedIndices = remember { mutableStateListOf<Int>() }

    val isComplete = selectedIndices.size == 4
    val currentWord = selectedIndices.map { shuffledChoices[it] }
    val isCorrect = currentWord == targetWord

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaYellow)
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

                // Level Badge
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AksaWhite)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "MODUL 3",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaDarkBrown
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Gunung Kata",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = AksaDarkBrown
            )

            Text(
                text = "Susun huruf menjadi kata yang benar!",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = AksaDarkBrown.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            // Large White Illustration & Learning Card
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
                    // Illustration Card (BOLA)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(AksaPeachLight),
                        contentAlignment = Alignment.Center
                    ) {
                        BallIllustration()
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4 EMPTY / FILLED ANSWER SLOTS
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (slotIndex in 0..3) {
                            val letter = if (slotIndex < selectedIndices.size) {
                                shuffledChoices[selectedIndices[slotIndex]]
                            } else null

                            LetterSlot(
                                letter = letter,
                                onClick = {
                                    if (slotIndex < selectedIndices.size) {
                                        selectedIndices.removeAt(slotIndex)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Pilih huruf di bawah:",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = AksaDarkBrown.copy(alpha = 0.65f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Shuffled Letters Row (L, A, B, O)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        shuffledChoices.forEachIndexed { index, letter ->
                            val isSelected = selectedIndices.contains(index)
                            LetterChoiceTile(
                                letter = letter,
                                isSelected = isSelected,
                                onClick = {
                                    if (!isSelected && selectedIndices.size < 4) {
                                        selectedIndices.add(index)
                                    }
                                }
                            )
                        }
                    }

                    // Success Feedback when correct
                    AnimatedVisibility(
                        visible = isComplete && isCorrect,
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
                                        text = "Hebat! Kamu berhasil membentuk kata BOLA.",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = AksaDarkBrown
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Cheering Mascot mini
                            MascotCheering(size = 120.dp)

                            Spacer(modifier = Modifier.height(14.dp))

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
private fun LetterSlot(
    letter: String?,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val isFilled = !letter.isNullOrEmpty()

    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 64.dp)
            .clip(shape)
            .background(if (isFilled) AksaOrange else AksaPeachLight)
            .border(
                width = 2.dp,
                color = if (isFilled) AksaOrange else AksaDarkBrown.copy(alpha = 0.2f),
                shape = shape
            )
            .clickable(enabled = isFilled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter ?: "",
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = if (isFilled) AksaWhite else AksaDarkBrown.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun LetterChoiceTile(
    letter: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val bg = if (isSelected) Color(0xFFE6E6EA) else AksaWhite
    val text = if (isSelected) Color(0xFFA0A0AA) else AksaDarkBrown

    Box(
        modifier = Modifier
            .size(width = 64.dp, height = 64.dp)
            .shadow(if (isSelected) 0.dp else 4.dp, shape)
            .clip(shape)
            .background(bg)
            .border(2.dp, if (isSelected) Color(0xFFD8D8DF) else AksaOrange.copy(alpha = 0.4f), shape)
            .clickable(enabled = !isSelected, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = text
        )
    }
}

/**
 * Flat vector illustration of a colorful play ball (BOLA)
 */
@Composable
private fun BallIllustration() {
    Canvas(modifier = Modifier.size(100.dp)) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)

        // Drop shadow
        drawOval(
            color = Color(0x22000000),
            topLeft = Offset(c.x - r * 0.8f, size.height - 12f),
            size = Size(r * 1.6f, 16f)
        )

        // Ball base circle (White)
        drawCircle(color = AksaWhite, radius = r, center = c)

        // Red/Orange segments
        val p1 = Path().apply {
            moveTo(c.x, c.y - r)
            quadraticTo(c.x - r * 0.45f, c.y, c.x, c.y + r)
            quadraticTo(c.x + r * 0.45f, c.y, c.x, c.y - r)
        }
        drawPath(p1, color = AksaOrange)

        val p2 = Path().apply {
            moveTo(c.x - r, c.y)
            quadraticTo(c.x, c.y - r * 0.45f, c.x + r, c.y)
            quadraticTo(c.x, c.y + r * 0.45f, c.x - r, c.y)
        }
        drawPath(p2, color = AksaYellowDark)

        // Border outline
        drawCircle(color = AksaDarkBrown, radius = r, center = c, style = Stroke(width = 4f))

        // Center seam line
        drawLine(
            color = AksaDarkBrown,
            start = Offset(c.x, c.y - r),
            end = Offset(c.x, c.y + r),
            strokeWidth = 3f
        )
        drawLine(
            color = AksaDarkBrown,
            start = Offset(c.x - r, c.y),
            end = Offset(c.x + r, c.y),
            strokeWidth = 3f
        )

        // Shine reflection
        drawCircle(color = AksaWhite.copy(alpha = 0.6f), radius = 10f, center = Offset(c.x - r * 0.45f, c.y - r * 0.45f))
    }
}
