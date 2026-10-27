package com.aksa.app.ui.screens

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.audio.AndroidTextToSpeechHelper
import com.aksa.app.data.repository.AksaRepositoryProvider
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaProgressBar
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.MascotLearning
import com.aksa.app.ui.components.ProgressBarColor
import com.aksa.app.ui.preview.DeveloperPreviewManager
import com.aksa.app.ui.preview.PreviewVisualState
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import kotlinx.coroutines.launch
import java.util.UUID

data class LetterIntroData(
    val letter: String,
    val word: String,
    val description: String
)

data class LetterQuizQuestion(
    val targetLetter: String,
    val prompt: String,
    val choices: List<String>
)

enum class LetterLearningStep {
    INTRODUCTION,
    QUIZ,
    COMPLETION
}

@Composable
fun LetterLearningScreen(
    startLetter: String = "A",
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit,
    onNavigateToWriting: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tts = remember { AndroidTextToSpeechHelper(context) }

    val lettersToLearn = remember(startLetter) {
        when (startLetter.uppercase()) {
            "D", "E", "F" -> listOf(
                LetterIntroData("D", "Daun", "D untuk Daun yang hijau"),
                LetterIntroData("E", "Ekor", "E untuk Ekor kelinci"),
                LetterIntroData("F", "Foto", "F untuk Foto pemandangan")
            )
            else -> listOf(
                LetterIntroData("A", "Apel", "A untuk Apel yang manis"),
                LetterIntroData("B", "Bola", "B untuk Bola yang bulat"),
                LetterIntroData("C", "Cacing", "C untuk Cacing di tanah")
            )
        }
    }

    val quizQuestions = remember(lettersToLearn) {
        listOf(
            LetterQuizQuestion(lettersToLearn[0].letter, "Mana huruf ${lettersToLearn[0].letter}?", listOf(lettersToLearn[0].letter, lettersToLearn[1].letter, lettersToLearn[2].letter).shuffled()),
            LetterQuizQuestion(lettersToLearn[1].letter, "Mana huruf ${lettersToLearn[1].letter}?", listOf(lettersToLearn[1].letter, lettersToLearn[0].letter, lettersToLearn[2].letter).shuffled()),
            LetterQuizQuestion(lettersToLearn[2].letter, "Mana huruf ${lettersToLearn[2].letter}?", listOf(lettersToLearn[2].letter, lettersToLearn[1].letter, lettersToLearn[0].letter).shuffled())
        )
    }

    var step by remember { mutableStateOf(LetterLearningStep.INTRODUCTION) }
    var currentIntroIndex by remember { mutableIntStateOf(0) }
    var currentQuizIndex by remember { mutableIntStateOf(0) }
    var selectedChoice by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var sessionCommitted by remember { mutableStateOf(false) }

    // Developer Preview overrides
    val visualState by DeveloperPreviewManager.visualState.collectAsState()
    val isDevPreview by DeveloperPreviewManager.isPreviewActive.collectAsState()

    val effectiveStep = when {
        isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED -> LetterLearningStep.COMPLETION
        isDevPreview && (visualState == PreviewVisualState.CORRECT_FEEDBACK || visualState == PreviewVisualState.INCORRECT_FEEDBACK) -> LetterLearningStep.QUIZ
        else -> step
    }

    val effectiveIsAnswered = if (isDevPreview && (visualState == PreviewVisualState.CORRECT_FEEDBACK || visualState == PreviewVisualState.INCORRECT_FEEDBACK)) true else isAnswered
    val effectiveIsCorrect = if (isDevPreview && visualState == PreviewVisualState.CORRECT_FEEDBACK) true else if (isDevPreview && visualState == PreviewVisualState.INCORRECT_FEEDBACK) false else isCorrect
    val effectiveSelectedChoice = if (isDevPreview && visualState == PreviewVisualState.CORRECT_FEEDBACK) quizQuestions[currentQuizIndex].targetLetter else if (isDevPreview && visualState == PreviewVisualState.INCORRECT_FEEDBACK) quizQuestions[currentQuizIndex].choices.firstOrNull { it != quizQuestions[currentQuizIndex].targetLetter } ?: "X" else selectedChoice

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
        }
    }

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
            // TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                // Progress Indicator Pill
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AksaWhite.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when (effectiveStep) {
                            LetterLearningStep.INTRODUCTION -> "Pengenalan ${currentIntroIndex + 1}/3"
                            LetterLearningStep.QUIZ -> "Soal ${currentQuizIndex + 1}/3"
                            LetterLearningStep.COMPLETION -> "Selesai"
                        },
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaDarkBrown
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            when (effectiveStep) {
                // ==================== STEP 1: INTRODUCTION ====================
                LetterLearningStep.INTRODUCTION -> {
                    val currentLetterData = lettersToLearn[currentIntroIndex]

                    val introCardShape = RoundedCornerShape(32.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, introCardShape, spotColor = Color(0x33441100))
                            .background(AksaWhite, introCardShape)
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kenali Huruf",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = AksaOrange
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Large Letter Display
                            Box(
                                modifier = Modifier
                                    .size(140.dp)
                                    .clip(CircleShape)
                                    .background(AksaYellow.copy(alpha = 0.25f))
                                    .border(4.dp, AksaYellow, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentLetterData.letter,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 72.sp,
                                    color = AksaDarkBrown
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Word association
                            Text(
                                text = currentLetterData.word,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = AksaDarkBrown
                            )

                            Text(
                                text = currentLetterData.description,
                                fontFamily = Poppins,
                                fontSize = 14.sp,
                                color = AksaDarkBrown.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Audio TTS Button
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(AksaLime)
                                    .clickable {
                                        tts.speak("Huruf ${currentLetterData.letter}. Seperti pada kata ${currentLetterData.word}.")
                                    }
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = AksaIcons.VolumeUp,
                                        contentDescription = "Dengarkan",
                                        tint = AksaDarkBrown,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Dengarkan Suara",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AksaDarkBrown
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (currentIntroIndex > 0) {
                            AksaButton(
                                text = "Sebelumnya",
                                style = AksaButtonStyle.SECONDARY_LIME,
                                onClick = { currentIntroIndex-- },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        AksaButton(
                            text = if (currentIntroIndex < lettersToLearn.size - 1) "Huruf Selanjutnya" else "Mulai Latihan Kuis",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = {
                                if (currentIntroIndex < lettersToLearn.size - 1) {
                                    currentIntroIndex++
                                } else {
                                    step = LetterLearningStep.QUIZ
                                    currentQuizIndex = 0
                                    tts.speak("Dengarkan suara: ${quizQuestions[0].prompt}")
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ==================== STEP 2: QUIZ ====================
                LetterLearningStep.QUIZ -> {
                    val currentQ = quizQuestions[currentQuizIndex]

                    val quizCardShape = RoundedCornerShape(32.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, quizCardShape, spotColor = Color(0x33441100))
                            .background(AksaWhite, quizCardShape)
                            .padding(22.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Kuis Dengar & Pilih",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AksaOrange
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(AksaPeachLight)
                                        .clickable {
                                            tts.speak("Dengarkan suara: ${currentQ.prompt}")
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = AksaIcons.VolumeUp,
                                            contentDescription = "Dengarkan",
                                            tint = AksaDarkBrown,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Putar",
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = AksaDarkBrown
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = currentQ.prompt,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                                color = AksaDarkBrown
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Choice Cards Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                for (choice in currentQ.choices) {
                                    val isSelected = effectiveSelectedChoice == choice
                                    val isChoiceTarget = choice == currentQ.targetLetter

                                    val (bgColor, borderColor) = when {
                                        !effectiveIsAnswered -> Pair(AksaWhite, Color(0xFFE2E8F0))
                                        isSelected && effectiveIsCorrect -> Pair(Color(0xFFE8F5E9), AksaLimeDark)
                                        isSelected && !effectiveIsCorrect -> Pair(Color(0xFFFFEBEE), Color(0xFFE53935))
                                        effectiveIsAnswered && isChoiceTarget -> Pair(Color(0xFFE8F5E9), AksaLimeDark)
                                        else -> Pair(AksaWhite, Color(0xFFE2E8F0))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(110.dp)
                                            .shadow(4.dp, RoundedCornerShape(22.dp))
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(bgColor)
                                            .border(2.5.dp, borderColor, RoundedCornerShape(22.dp))
                                            .clickable(enabled = !effectiveIsAnswered) {
                                                selectedChoice = choice
                                                isAnswered = true
                                                val correct = choice == currentQ.targetLetter
                                                isCorrect = correct
                                                if (correct) {
                                                    correctCount++
                                                    tts.speak("Benar! Ini huruf $choice.")
                                                } else {
                                                    tts.speak("Belum tepat. Coba perhatikan lagi ya.")
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = choice,
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 44.sp,
                                            color = AksaDarkBrown
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Feedback Banner
                            AnimatedVisibility(
                                visible = effectiveIsAnswered,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val bannerBg = if (effectiveIsCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    val bannerBorder = if (effectiveIsCorrect) AksaLimeDark else Color(0xFFE53935)
                                    val bannerText = if (effectiveIsCorrect) "Benar! Pintar sekali!" else "Belum tepat. Jawaban benar adalah huruf ${currentQ.targetLetter}."

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(bannerBg)
                                            .border(1.5.dp, bannerBorder, RoundedCornerShape(16.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (effectiveIsCorrect) AksaIcons.Check else AksaIcons.Info,
                                                contentDescription = null,
                                                tint = bannerBorder,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = bannerText,
                                                fontFamily = Poppins,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (effectiveIsCorrect) AksaDarkBrown else Color(0xFFC62828)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    if (effectiveIsCorrect) {
                                        MascotCheering(size = 80.dp)
                                    } else {
                                        MascotLearning(size = 80.dp, animated = false)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            AksaButton(
                                text = if (currentQuizIndex < quizQuestions.size - 1) "Soal Berikutnya" else "Lihat Hasil Latihan",
                                style = if (effectiveIsAnswered) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                                enabled = effectiveIsAnswered,
                                onClick = {
                                    if (currentQuizIndex < quizQuestions.size - 1) {
                                        currentQuizIndex++
                                        selectedChoice = null
                                        isAnswered = false
                                        isCorrect = false
                                        tts.speak("Dengarkan suara: ${quizQuestions[currentQuizIndex].prompt}")
                                    } else {
                                        // Commit session
                                        if (!sessionCommitted) {
                                            sessionCommitted = true
                                            coroutineScope.launch {
                                                val sessionResult = SessionResult(
                                                    sessionId = "session_letter_${UUID.randomUUID()}",
                                                    childId = "child_marvin_demo",
                                                    moduleId = "pulau_alfabet",
                                                    activityId = "letter_learning",
                                                    startedAt = System.currentTimeMillis() - 60_000,
                                                    completedAt = System.currentTimeMillis(),
                                                    totalQuestions = quizQuestions.size,
                                                    correctAnswers = correctCount,
                                                    incorrectAnswers = quizQuestions.size - correctCount,
                                                    accuracy = correctCount.toFloat() / quizQuestions.size.toFloat(),
                                                    status = SessionStatus.COMPLETED
                                                )
                                                AksaRepositoryProvider.sessionRepository.saveSessionResult(sessionResult)
                                            }
                                        }
                                        step = LetterLearningStep.COMPLETION
                                    }
                                }
                            )
                        }
                    }
                }

                // ==================== STEP 3: COMPLETION ====================
                LetterLearningStep.COMPLETION -> {
                    val compCardShape = RoundedCornerShape(32.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, compCardShape, spotColor = Color(0x33441100))
                            .background(AksaWhite, compCardShape)
                            .padding(26.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            MascotCheering(size = 120.dp)

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Latihan Selesai!",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = AksaDarkBrown
                            )

                            Text(
                                text = "Hebat! Kamu telah mengenal 3 huruf dan menyelesaikan kuis suara.",
                                fontFamily = Poppins,
                                fontSize = 13.sp,
                                color = AksaDarkBrown.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            // Score Badge
                            val scoreDisplay = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "3 / 3" else "$correctCount / ${quizQuestions.size}"
                            val accuracyDisplay = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "100%" else "${((correctCount.toFloat() / quizQuestions.size) * 100).toInt()}%"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(AksaYellow.copy(alpha = 0.3f))
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = scoreDisplay,
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = AksaDarkBrown
                                        )
                                        Text(
                                            text = "Benar",
                                            fontFamily = Poppins,
                                            fontSize = 12.sp,
                                            color = AksaDarkBrown.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(AksaLime.copy(alpha = 0.35f))
                                        .padding(14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = accuracyDisplay,
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp,
                                            color = AksaDarkBrown
                                        )
                                        Text(
                                            text = "Akurasi",
                                            fontFamily = Poppins,
                                            fontSize = 12.sp,
                                            color = AksaDarkBrown.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            AksaButton(
                                text = "Latihan Menulis (Opsional)",
                                style = AksaButtonStyle.SECONDARY_LIME,
                                onClick = onNavigateToWriting
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            AksaButton(
                                text = "Lihat Peta Petualangan",
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
