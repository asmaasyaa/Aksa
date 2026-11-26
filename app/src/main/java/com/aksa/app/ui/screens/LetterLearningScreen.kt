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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.aksa.app.domain.model.AnswerLog
import com.aksa.app.domain.model.SessionResult
import com.aksa.app.domain.model.SessionStatus
import com.aksa.app.domain.usecase.GetActiveChildIdUseCase
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.EarnedStarsRow
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.MascotLearning
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaError
import com.aksa.app.ui.theme.AksaErrorLight
import com.aksa.app.ui.theme.AksaErrorText
import com.aksa.app.ui.theme.AksaGrayBorder
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaSuccessLight
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class LetterIntroData(
    val letter: String,
    val word: String,
    val description: String
)

data class LetterQuizQuestion(
    val targetLetter: String,
    val audioInstruction: String,
    val choices: List<String>
)

enum class LetterLearningStep {
    INTRODUCTION,
    QUIZ,
    COMPLETION
}

private val ALL_ALPHABET_DATA = listOf(
    LetterIntroData("A", "Apel", "A untuk Apel yang manis"),
    LetterIntroData("B", "Bola", "B untuk Bola yang bulat"),
    LetterIntroData("C", "Cacing", "C untuk Cacing di tanah"),
    LetterIntroData("D", "Daun", "D untuk Daun yang hijau"),
    LetterIntroData("E", "Ekor", "E untuk Ekor kelinci"),
    LetterIntroData("F", "Foto", "F untuk Foto pemandangan"),
    LetterIntroData("G", "Gajah", "G untuk Gajah yang besar"),
    LetterIntroData("H", "Hujan", "H untuk Hujan yang sejuk"),
    LetterIntroData("I", "Ikan", "I untuk Ikan di kolam"),
    LetterIntroData("J", "Jeruk", "J untuk Jeruk yang segar"),
    LetterIntroData("K", "Kuda", "K untuk Kuda yang berlari"),
    LetterIntroData("L", "Lilin", "L untuk Lilin yang terang"),
    LetterIntroData("M", "Mata", "M untuk Mata yang melihat"),
    LetterIntroData("N", "Nasi", "N untuk Nasi yang hangat"),
    LetterIntroData("O", "Obat", "O untuk Obat penyembuh"),
    LetterIntroData("P", "Paku", "P untuk Paku di dinding"),
    LetterIntroData("Q", "Quran", "Q untuk Quran kitab suci"),
    LetterIntroData("R", "Roda", "R untuk Roda berputar"),
    LetterIntroData("S", "Sapi", "S untuk Sapi di padang rumput"),
    LetterIntroData("T", "Topi", "T untuk Topi pelindung kepala"),
    LetterIntroData("U", "Ular", "U untuk Ular melata"),
    LetterIntroData("V", "Vas", "V untuk Vas bunga cantik"),
    LetterIntroData("W", "Wortel", "W untuk Wortel kesukaan kelinci"),
    LetterIntroData("X", "Xilofon", "X untuk Xilofon alat musik"),
    LetterIntroData("Y", "Yoyo", "Y untuk Yoyo mainan seru"),
    LetterIntroData("Z", "Zebra", "Z untuk Zebra belang hitam putih")
)

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
    val isTtsReady by tts.isReady.collectAsState()

    val lettersToLearn = remember(startLetter) {
        val normalized = startLetter.uppercase()
        val startIndex = ALL_ALPHABET_DATA.indexOfFirst { it.letter == normalized }.coerceAtLeast(0)
        val groupStart = (startIndex / 3) * 3
        val slice = ALL_ALPHABET_DATA.drop(groupStart).take(3)
        val fullSlice = if (slice.size == 3) {
            slice
        } else {
            (slice + ALL_ALPHABET_DATA.take(3 - slice.size))
        }
        val clickedItem = fullSlice.find { it.letter == normalized }
        if (clickedItem != null) {
            listOf(clickedItem) + fullSlice.filterNot { it.letter == normalized }
        } else {
            fullSlice
        }
    }

    val quizQuestions = remember(lettersToLearn) {
        listOf(
            LetterQuizQuestion(
                targetLetter = lettersToLearn[0].letter,
                audioInstruction = "Mana huruf ${lettersToLearn[0].letter}?",
                choices = listOf(lettersToLearn[0].letter, lettersToLearn[1].letter, lettersToLearn[2].letter).shuffled()
            ),
            LetterQuizQuestion(
                targetLetter = lettersToLearn[1].letter,
                audioInstruction = "Mana huruf ${lettersToLearn[1].letter}?",
                choices = listOf(lettersToLearn[1].letter, lettersToLearn[0].letter, lettersToLearn[2].letter).shuffled()
            ),
            LetterQuizQuestion(
                targetLetter = lettersToLearn[2].letter,
                audioInstruction = "Mana huruf ${lettersToLearn[2].letter}?",
                choices = listOf(lettersToLearn[2].letter, lettersToLearn[1].letter, lettersToLearn[0].letter).shuffled()
            )
        )
    }

    val sessionId = remember { "session_letter_${UUID.randomUUID()}" }
    val sessionStartedAt = remember { System.currentTimeMillis() }
    val recordedAnswers = remember { mutableStateListOf<AnswerLog>() }
    val activeChildResolver = remember { GetActiveChildIdUseCase() }

    var step by remember { mutableStateOf(LetterLearningStep.INTRODUCTION) }
    var currentIntroIndex by remember { mutableIntStateOf(0) }
    var currentQuizIndex by remember { mutableIntStateOf(0) }
    var selectedChoice by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var sessionCommitted by remember { mutableStateOf(false) }
    var isSavingSession by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    val requestExit = {
        if (step != LetterLearningStep.COMPLETION) {
            showExitConfirmation = true
        } else {
            onNavigateBack()
        }
    }

    androidx.activity.compose.BackHandler {
        if (showExitConfirmation) {
            showExitConfirmation = false
        } else {
            requestExit()
        }
    }

    LaunchedEffect(step, currentIntroIndex, currentQuizIndex, isTtsReady) {
        if (!isTtsReady) return@LaunchedEffect
        when (step) {
            LetterLearningStep.INTRODUCTION -> {
                val data = lettersToLearn[currentIntroIndex]
                tts.speak("Huruf ${data.letter}. Seperti pada kata ${data.word}.")
            }
            LetterLearningStep.QUIZ -> {
                val q = quizQuestions[currentQuizIndex]
                tts.speak("Dengarkan baik-baik. ${q.audioInstruction}")
            }
            LetterLearningStep.COMPLETION -> {
                tts.speak("Hebat! Latihan selesai!")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.release()
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
                        .clickable(onClick = requestExit)
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

                // Badge kanan atas: klik "Selesai" langsung kembali ke Peta Petualangan
                Box(
                    modifier = Modifier
                        .shadow(if (step == LetterLearningStep.COMPLETION) 4.dp else 0.dp, PillShape)
                        .clip(PillShape)
                        .background(if (step == LetterLearningStep.COMPLETION) AksaLime else AksaWhite.copy(alpha = 0.9f))
                        .clickable(enabled = step == LetterLearningStep.COMPLETION) {
                            onNavigateToLevelMap()
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = when (step) {
                                LetterLearningStep.INTRODUCTION -> "Pengenalan ${currentIntroIndex + 1}/3"
                                LetterLearningStep.QUIZ -> "Soal ${currentQuizIndex + 1}/3"
                                LetterLearningStep.COMPLETION -> "Selesai"
                            },
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AksaDarkBrown
                        )
                        if (step == LetterLearningStep.COMPLETION) {
                            Icon(
                                painter = AksaIcons.check(),
                                contentDescription = null,
                                tint = AksaDarkBrown,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            when (step) {
                // ==================== STEP 1: INTRODUCTION ====================
                LetterLearningStep.INTRODUCTION -> {
                    val currentLetterData = lettersToLearn[currentIntroIndex]
                    val letterPairDisplay = "${currentLetterData.letter}${currentLetterData.letter.lowercase()}"

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

                            Box(
                                modifier = Modifier
                                    .size(144.dp)
                                    .clip(CircleShape)
                                    .background(AksaYellow.copy(alpha = 0.25f))
                                    .border(4.dp, AksaYellow, CircleShape)
                                    .clickable {
                                        tts.speak("Huruf ${currentLetterData.letter}. Seperti pada kata ${currentLetterData.word}.")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = letterPairDisplay,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 54.sp,
                                    color = AksaDarkBrown
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

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
                                        painter = AksaIcons.volumeUp(),
                                        contentDescription = "Dengarkan",
                                        tint = AksaDarkBrown,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Ulangi Suara",
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
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ==================== STEP 2: QUIZ (AUDIO-FIRST) ====================
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
                            Text(
                                text = "Kuis Dengar & Pilih",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = AksaOrange
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(AksaYellow)
                                    .clickable {
                                        tts.speak(currentQ.audioInstruction)
                                    }
                                    .padding(horizontal = 24.dp, vertical = 14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = AksaIcons.volumeUp(),
                                        contentDescription = "Dengarkan Soal",
                                        tint = AksaDarkBrown,
                                        modifier = Modifier.size(26.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Dengarkan Hurufnya",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = AksaDarkBrown
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Ketuk huruf yang sesuai dengan suara!",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = AksaDarkBrown.copy(alpha = 0.75f)
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                for (choice in currentQ.choices) {
                                    val isSelected = selectedChoice == choice
                                    val isChoiceTarget = choice == currentQ.targetLetter

                                    val (bgColor, borderColor) = when {
                                        !isAnswered -> Pair(AksaWhite, AksaGrayBorder)
                                        isSelected && isCorrect -> Pair(AksaSuccessLight, AksaLimeDark)
                                        isSelected && !isCorrect -> Pair(AksaErrorLight, AksaError)
                                        isAnswered && isChoiceTarget -> Pair(AksaSuccessLight, AksaLimeDark)
                                        else -> Pair(AksaWhite, AksaGrayBorder)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(110.dp)
                                            .shadow(4.dp, RoundedCornerShape(22.dp))
                                            .clip(RoundedCornerShape(22.dp))
                                            .background(bgColor)
                                            .border(2.5.dp, borderColor, RoundedCornerShape(22.dp))
                                            .clickable(enabled = !isAnswered) {
                                                selectedChoice = choice
                                                isAnswered = true
                                                val correct = choice == currentQ.targetLetter
                                                isCorrect = correct
                                                val childId = runCatching { activeChildResolver.getSync() }
                                                    .getOrDefault(AksaUserState.currentProfileId)
                                                val log = AnswerLog(
                                                    answerId = "ans_${sessionId}_letter_${currentQuizIndex}",
                                                    sessionId = sessionId,
                                                    childId = childId,
                                                    moduleId = "pulau_alfabet",
                                                    activityId = "letter_learning",
                                                    questionId = "letter_q_${currentQ.targetLetter}_$currentQuizIndex",
                                                    targetChar = currentQ.targetLetter,
                                                    chosenChar = choice,
                                                    isCorrect = correct,
                                                    responseTimeMs = 1000L,
                                                    timestamp = System.currentTimeMillis()
                                                )
                                                recordedAnswers.add(log)
                                                if (correct) {
                                                    correctCount++
                                                    tts.speak("Benar! Ini huruf $choice.")
                                                } else {
                                                    tts.speak("Belum tepat. Yang benar adalah huruf ${currentQ.targetLetter}.")
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

                            AnimatedVisibility(
                                visible = isAnswered,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val bannerBg = if (isCorrect) AksaSuccessLight else AksaErrorLight
                                    val bannerBorder = if (isCorrect) AksaLimeDark else AksaError
                                    val bannerText = if (isCorrect) {
                                        "Benar! Ini adalah huruf ${currentQ.targetLetter}."
                                    } else {
                                        "Belum tepat. Jawaban benar adalah huruf ${currentQ.targetLetter}."
                                    }

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
                                                painter = if (isCorrect) AksaIcons.check() else AksaIcons.info(),
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
                                                color = if (isCorrect) AksaDarkBrown else AksaErrorText
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    if (isCorrect) {
                                        MascotCheering(size = 80.dp)
                                    } else {
                                        MascotLearning(size = 80.dp, animated = false)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            AksaButton(
                                text = if (currentQuizIndex < quizQuestions.size - 1) "Soal Berikutnya" else "Lihat Hasil Latihan",
                                style = if (isAnswered) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                                enabled = isAnswered,
                                onClick = {
                                    if (currentQuizIndex < quizQuestions.size - 1) {
                                        currentQuizIndex++
                                        selectedChoice = null
                                        isAnswered = false
                                        isCorrect = false
                                    } else {
                                        if (!sessionCommitted) {
                                            sessionCommitted = true
                                            isSavingSession = true
                                            coroutineScope.launch {
                                                try {
                                                    withContext(NonCancellable) {
                                                        runCatching {
                                                            val childId = activeChildResolver()
                                                            for (ans in recordedAnswers) {
                                                                AksaRepositoryProvider.sessionRepository
                                                                    .recordAnswer(ans.copy(childId = childId))
                                                            }
                                                            val sessionResult = SessionResult(
                                                                sessionId = sessionId,
                                                                childId = childId,
                                                                moduleId = "pulau_alfabet",
                                                                activityId = "letter_learning",
                                                                startedAt = sessionStartedAt,
                                                                completedAt = System.currentTimeMillis(),
                                                                totalQuestions = quizQuestions.size,
                                                                correctAnswers = correctCount,
                                                                incorrectAnswers = quizQuestions.size - correctCount,
                                                                accuracy = correctCount.toFloat() / quizQuestions.size.toFloat(),
                                                                status = SessionStatus.COMPLETED
                                                            )
                                                            AksaRepositoryProvider.sessionRepository
                                                                .saveSessionResult(sessionResult)
                                                        }
                                                    }
                                                } finally {
                                                    isSavingSession = false
                                                }
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
                            Spacer(modifier = Modifier.height(8.dp))
                            EarnedStarsRow(accuracy = correctCount.toFloat() / quizQuestions.size.toFloat())

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

                            val scoreDisplay = "$correctCount / ${quizQuestions.size}"
                            val accuracyDisplay = "${((correctCount.toFloat() / quizQuestions.size) * 100).toInt()}%"

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
                                text = if (isSavingSession) "Menyimpan progres..." else "Lihat Peta Petualangan",
                                style = AksaButtonStyle.PRIMARY_ORANGE,
                                enabled = !isSavingSession,
                                onClick = onNavigateToLevelMap
                            )
                        }
                    }
                }
            }
        }

        if (showExitConfirmation) {
            com.aksa.app.ui.components.AksaExitConfirmationDialog(
                isMiniGame = false,
                onContinue = { showExitConfirmation = false },
                onExit = {
                    showExitConfirmation = false
                    onNavigateBack()
                }
            )
        }
    }
}