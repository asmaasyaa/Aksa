package com.aksa.app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.audio.AndroidTextToSpeechHelper
import com.aksa.app.domain.model.HurufMiripQuestion
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.MascotLearning
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
import com.aksa.app.ui.viewmodel.HurufMiripViewModel

@Composable
fun HurufMiripScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit,
    viewModel: HurufMiripViewModel? = null
) {
    val context = LocalContext.current
    val audioSpeaker = remember { AndroidTextToSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioSpeaker.release()
        }
    }

    val vm: HurufMiripViewModel = viewModel ?: viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return HurufMiripViewModel(audioSpeaker = audioSpeaker) as T
            }
        }
    )

    val uiState by vm.uiState.collectAsState()
    val currentQuestion = uiState.currentQuestion

    // Handle back button safely: abandon session if exiting early
    val handleExit = {
        if (!uiState.showCompletionDialog && uiState.currentQuestionIndex < uiState.totalQuestions) {
            vm.abandonSession()
        }
        onNavigateBack()
    }

    BackHandler(onBack = handleExit)

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
                        .clickable(onClick = handleExit)
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

                // Progress Badge: Soal X dari 10
                Box(
                    modifier = Modifier
                        .shadow(4.dp, PillShape)
                        .clip(PillShape)
                        .background(AksaWhite)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Soal ${uiState.currentQuestionIndex + 1} dari ${uiState.totalQuestions}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // VISUAL EXPLANATION / EDUCATIONAL HINT CARD
            if (currentQuestion != null) {
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
            }

            Spacer(modifier = Modifier.height(18.dp))

            // MAIN QUESTION CARD
            if (currentQuestion != null) {
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Huruf Mirip",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AksaOrange
                            )

                            // Replayable Audio Speaker Button
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(AksaYellow.copy(alpha = 0.25f))
                                    .clickable { vm.playCurrentQuestionAudio() }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = AksaIcons.VolumeUp,
                                        contentDescription = "Dengarkan Suara",
                                        tint = AksaDarkBrown,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Dengarkan",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AksaDarkBrown
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = currentQuestion.promptText,
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
                                val isChosen = uiState.selectedChoice == choice
                                val isChoiceCorrect = choice == currentQuestion.targetLetter

                                LetterChoiceCard(
                                    letter = choice,
                                    isChosen = isChosen,
                                    isAnswered = uiState.isAnswered,
                                    isChoiceCorrect = isChoiceCorrect,
                                    onClick = { vm.selectChoice(choice) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // FEEDBACK SECTION & MASCOT
                        AnimatedVisibility(
                            visible = uiState.isAnswered,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val isCorrect = uiState.isCorrect
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
                                                "Belum tepat. Huruf ini adalah '${uiState.selectedChoice}'. Coba perhatikan arah lengkungannya ya!"
                                            },
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = feedbackTextColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (isCorrect) {
                                    MascotCheering(size = 96.dp)
                                } else {
                                    MascotLearning(size = 96.dp, animated = false)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // BUTTON: SELANJUTNYA / SELESAI
                        AksaButton(
                            text = if (uiState.currentQuestionIndex == uiState.totalQuestions - 1) "Selesai" else "Selanjutnya",
                            style = if (uiState.isAnswered) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                            enabled = uiState.isAnswered,
                            onClick = { vm.nextQuestion() }
                        )
                    }
                }
            }
        }

        // COMPLETION CELEBRATION MODAL
        if (uiState.showCompletionDialog) {
            val result = uiState.sessionResult
            val correct = result?.correctAnswers ?: uiState.correctAnswersCount
            val total = result?.totalQuestions ?: uiState.totalQuestions
            val accuracyPct = ((correct.toFloat() / total.toFloat()) * 100).toInt()

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
                        .padding(26.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MascotCheering(size = 120.dp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Latihan Selesai!",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = AksaDarkBrown
                        )

                        Text(
                            text = "Hebat! Kamu telah menyelesaikan 10 soal latihan huruf mirip b, d, p, dan q.",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = AksaDarkBrown.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        // Score & Accuracy Box
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(AksaPeachLight)
                                .border(1.dp, AksaSoftPeach, RoundedCornerShape(18.dp))
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$correct / $total",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = AksaOrange
                                )
                                Text(
                                    text = "Benar",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = AksaDarkBrown.copy(alpha = 0.7f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(AksaSoftPeach)
                            )

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$accuracyPct%",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = AksaLimeDark
                                )
                                Text(
                                    text = "Akurasi",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = AksaDarkBrown.copy(alpha = 0.7f)
                                )
                            }
                        }

                        // Milestone notice if awarded
                        if (uiState.newlyAwardedMilestones.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(AksaYellow.copy(alpha = 0.25f))
                                    .border(1.dp, AksaYellowDark, RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = AksaIcons.Star,
                                        contentDescription = null,
                                        tint = AksaYellowDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Selamat! Kamu membuka lencana mingguan baru!",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = AksaDarkBrown
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

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
        else -> Color(0xFFE0E0E0)
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
