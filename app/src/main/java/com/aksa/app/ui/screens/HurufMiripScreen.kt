package com.aksa.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.MascotLearning
import com.aksa.app.ui.components.MascotStanding
import com.aksa.app.ui.components.MascotVariant
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

private data class HurufMiripQuestion(
    val targetLetter: String,
    val questionText: String,
    val choices: List<String>,
    val hintTitle: String,
    val leftHint: Pair<String, String>,  // Letter to rule
    val rightHint: Pair<String, String>  // Letter to rule
)

@Composable
fun HurufMiripScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit
) {
    val questions = listOf(
        HurufMiripQuestion(
            targetLetter = "b",
            questionText = "Mana huruf b?",
            choices = listOf("b", "d"),
            hintTitle = "Perbedaan b dan d:",
            leftHint = "b" to "Bulatannya ada di kanan",
            rightHint = "d" to "Bulatannya ada di kiri"
        ),
        HurufMiripQuestion(
            targetLetter = "d",
            questionText = "Mana huruf d?",
            choices = listOf("b", "d"),
            hintTitle = "Perbedaan b dan d:",
            leftHint = "d" to "Bulatannya ada di kiri",
            rightHint = "b" to "Bulatannya ada di kanan"
        ),
        HurufMiripQuestion(
            targetLetter = "p",
            questionText = "Mana huruf p?",
            choices = listOf("p", "q"),
            hintTitle = "Perbedaan p dan q:",
            leftHint = "p" to "Batang turun, bulatan di kanan",
            rightHint = "q" to "Batang turun, bulatan di kiri"
        ),
        HurufMiripQuestion(
            targetLetter = "q",
            questionText = "Mana huruf q?",
            choices = listOf("p", "q"),
            hintTitle = "Perbedaan p dan q:",
            leftHint = "q" to "Batang turun, bulatan di kiri",
            rightHint = "p" to "Batang turun, bulatan di kanan"
        )
    )

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedChoice by remember { mutableStateOf<String?>(null) }
    var showCompletionDialog by remember { mutableStateOf(false) }

    val currentQuestion = questions[currentQuestionIndex]
    val isAnswered = selectedChoice != null
    val isCorrect = selectedChoice == currentQuestion.targetLetter

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaBlue)
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
            // Top Bar (Kembali + Progress Pill)
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                // Progress Badge: Soal X dari 4
                Box(
                    modifier = Modifier
                        .shadow(4.dp, PillShape)
                        .clip(PillShape)
                        .background(AksaWhite)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Soal ${currentQuestionIndex + 1} dari ${questions.size}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // REVISION 13: VISUAL EXPLANATION / EDUCATIONAL HINT CARD
            val hintCardShape = RoundedCornerShape(24.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, hintCardShape, spotColor = Color(0x22441100))
                    .background(AksaWhite, hintCardShape)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AksaYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AksaIcons.AutoAwesome,
                                    contentDescription = null,
                                    tint = AksaDarkBrown,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Petunjuk Pintar",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AksaDarkBrown
                            )
                        }

                        Text(
                            text = "Aksa Edu",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = AksaOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Visual Side-by-Side Comparison Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VisualHintPill(
                            letter = currentQuestion.leftHint.first,
                            rule = currentQuestion.leftHint.second,
                            highlightColor = AksaOrange,
                            modifier = Modifier.weight(1f)
                        )
                        VisualHintPill(
                            letter = currentQuestion.rightHint.first,
                            rule = currentQuestion.rightHint.second,
                            highlightColor = AksaLimeDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // MAIN QUESTION CARD
            val cardShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, cardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, cardShape)
                    .padding(22.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Huruf Mirip",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaOrange
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentQuestion.questionText,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        textAlign = TextAlign.Center,
                        color = AksaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // TWO LARGE WHITE ROUNDED LETTER CHOICE CARDS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        for (choice in currentQuestion.choices) {
                            val isChosen = selectedChoice == choice
                            val isChoiceCorrect = choice == currentQuestion.targetLetter

                            LetterChoiceCard(
                                letter = choice,
                                isChosen = isChosen,
                                isAnswered = isAnswered,
                                isChoiceCorrect = isChoiceCorrect,
                                onClick = {
                                    if (!isAnswered) {
                                        selectedChoice = choice
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // FEEDBACK SECTION & MASCOT
                    AnimatedVisibility(
                        visible = isAnswered,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Feedback Banner
                            val feedbackBgColor = if (isCorrect) AksaLime.copy(alpha = 0.25f) else Color(0xFFFFEBEE)
                            val feedbackBorderColor = if (isCorrect) AksaLimeDark else Color(0xFFE53935)
                            val feedbackTextColor = if (isCorrect) AksaDarkBrown else Color(0xFFC62828)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(feedbackBgColor)
                                    .border(1.5.dp, feedbackBorderColor, RoundedCornerShape(18.dp))
                                    .padding(vertical = 12.dp, horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = if (isCorrect) AksaIcons.Check else AksaIcons.Info,
                                        contentDescription = null,
                                        tint = feedbackBorderColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = if (isCorrect) {
                                            "Benar! Ini huruf ${currentQuestion.targetLetter}."
                                        } else {
                                            "Belum tepat. Coba lihat arah bulatannya lagi."
                                        },
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = feedbackTextColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Mascot Feedback Illustration
                            if (isCorrect) {
                                MascotCheering(size = 96.dp)
                            } else {
                                MascotLearning(
                                    size = 96.dp,
                                    animated = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BUTTON: SELANJUTNYA
                    AksaButton(
                        text = if (currentQuestionIndex == questions.size - 1) "Selesai" else "Selanjutnya",
                        style = if (isAnswered) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                        enabled = isAnswered,
                        onClick = {
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedChoice = null
                            } else {
                                // Mark completed in prototype state
                                AksaUserState.hurufMiripCompleted = true
                                showCompletionDialog = true
                            }
                        }
                    )
                }
            }
        }

        // COMPLETION CELEBRATION MODAL
        if (showCompletionDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { /* Block clicks behind */ }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                val dialogShape = RoundedCornerShape(32.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, dialogShape)
                        .background(AksaWhite, dialogShape)
                        .padding(28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MascotCheering(size = 130.dp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Luar Biasa!",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = AksaDarkBrown
                        )

                        Text(
                            text = "Kamu sudah bisa membedakan huruf mirip b, d, p, dan q dengan tepat!",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            color = AksaDarkBrown.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                        )

                        AksaButton(
                            text = "Kembali ke Pulau Alfabet",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = onNavigateBack
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        AksaButton(
                            text = "Lihat Peta Petualangan",
                            style = AksaButtonStyle.SECONDARY_LIME,
                            onClick = onNavigateToLevelMap
                        )
                    }
                }
            }
        }
    }
}

/**
 * Visual Educational Hint Pill Card
 */
@Composable
private fun VisualHintPill(
    letter: String,
    rule: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(AksaPeachLight)
            .border(1.5.dp, highlightColor.copy(alpha = 0.4f), pillShape)
            .padding(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(AksaWhite)
                    .border(2.dp, highlightColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = highlightColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = rule,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                color = AksaDarkBrown
            )
        }
    }
}

/**
 * Large Interactive Letter Card
 */
@Composable
private fun LetterChoiceCard(
    letter: String,
    isChosen: Boolean,
    isAnswered: Boolean,
    isChoiceCorrect: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(26.dp)

    val scale by animateFloatAsState(
        targetValue = if (isChosen) 1.05f else 1f,
        label = "LetterCardScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isChosen && isChoiceCorrect -> AksaLime.copy(alpha = 0.2f)
            isChosen && !isChoiceCorrect -> Color(0xFFFFCDD2)
            else -> AksaPeachLight
        },
        label = "LetterCardBg"
    )

    val borderColor = when {
        isChosen && isChoiceCorrect -> AksaLimeDark
        isChosen && !isChoiceCorrect -> Color(0xFFE53935)
        isChosen -> AksaOrange
        else -> AksaGrayBorderColor
    }

    Box(
        modifier = modifier
            .height(130.dp)
            .scale(scale)
            .shadow(if (isChosen) 8.dp else 4.dp, cardShape, spotColor = Color(0x22441100))
            .clip(cardShape)
            .background(backgroundColor)
            .border(if (isChosen) 3.dp else 1.5.dp, borderColor, cardShape)
            .clickable(enabled = !isAnswered, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp,
            color = AksaDarkBrown
        )
    }
}

private val AksaGrayBorderColor = Color(0xFFE0E0E0)
