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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.MascotLearning
import com.aksa.app.ui.preview.DeveloperPreviewManager
import com.aksa.app.ui.preview.PreviewVisualState
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
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import kotlinx.coroutines.launch
import java.util.UUID

data class StoryPanel(
    val title: String,
    val text: String,
    val emoji: String
)

data class ComprehensionQuestion(
    val id: String,
    val questionText: String,
    val targetAnswer: String,
    val choices: List<String>
)

enum class ReadingStage {
    READING_STORY,
    COMPREHENSION_QUIZ,
    COMPLETION
}

@Composable
fun ReadingPracticeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tts = remember { AndroidTextToSpeechHelper(context) }

    val storyPanels = remember {
        listOf(
            StoryPanel("Kancil dan Kebun Buah", "Kancil berjalan riang ke kebun buah di dekat sungai yang jernih.", "🦌🌳"),
            StoryPanel("Apel Merah yang Manis", "Di kebun, Kancil memetik buah apel merah yang matang dan manis rasanya.", "🍎🧺"),
            StoryPanel("Berbagi Bersama Sahabat", "Kancil membagikan buah apel kepada burung pipit dan kelinci sahabatnya.", "🐰🐦")
        )
    }

    val questions = remember {
        listOf(
            ComprehensionQuestion("cq_1", "Siapa yang pergi ke kebun buah di dekat sungai?", "Kancil", listOf("Kancil", "Gajah", "Kucing")),
            ComprehensionQuestion("cq_2", "Buah apa yang dipetik oleh Kancil di kebun?", "Apel merah", listOf("Apel merah", "Jeruk", "Pisang")),
            ComprehensionQuestion("cq_3", "Kepada siapa Kancil membagikan buah apel?", "Burung dan kelinci", listOf("Burung dan kelinci", "Harimau", "Buaya"))
        )
    }

    var stage by remember { mutableStateOf(ReadingStage.READING_STORY) }
    var currentPanelIndex by remember { mutableIntStateOf(0) }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }

    var selectedChoice by remember { mutableStateOf<String?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var sessionCommitted by remember { mutableStateOf(false) }

    val currentQ = questions[currentQuestionIndex]

    // Developer Preview overrides
    val visualState by DeveloperPreviewManager.visualState.collectAsState()
    val isDevPreview by DeveloperPreviewManager.isPreviewActive.collectAsState()

    val effectiveStage = when {
        isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED -> ReadingStage.COMPLETION
        isDevPreview && (visualState == PreviewVisualState.CORRECT_FEEDBACK || visualState == PreviewVisualState.INCORRECT_FEEDBACK) -> ReadingStage.COMPREHENSION_QUIZ
        else -> stage
    }

    val effectiveIsAnswered = if (isDevPreview && (visualState == PreviewVisualState.CORRECT_FEEDBACK || visualState == PreviewVisualState.INCORRECT_FEEDBACK)) true else isAnswered
    val effectiveIsCorrect = if (isDevPreview && visualState == PreviewVisualState.CORRECT_FEEDBACK) true else if (isDevPreview && visualState == PreviewVisualState.INCORRECT_FEEDBACK) false else isCorrect
    val effectiveSelectedChoice = if (isDevPreview && visualState == PreviewVisualState.CORRECT_FEEDBACK) currentQ.targetAnswer else if (isDevPreview && visualState == PreviewVisualState.INCORRECT_FEEDBACK) currentQ.choices.firstOrNull { it != currentQ.targetAnswer } ?: "Lain" else selectedChoice

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

                // Stage Pill
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AksaWhite.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when (effectiveStage) {
                            ReadingStage.READING_STORY -> "Bagian ${currentPanelIndex + 1}/${storyPanels.size}"
                            ReadingStage.COMPREHENSION_QUIZ -> "Kuis ${currentQuestionIndex + 1}/${questions.size}"
                            ReadingStage.COMPLETION -> "Selesai"
                        },
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaDarkBrown
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (effectiveStage) {
                // ==================== STAGE 1: STORY READING ====================
                ReadingStage.READING_STORY -> {
                    val currentPanel = storyPanels[currentPanelIndex]

                    val cardShape = RoundedCornerShape(32.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, cardShape, spotColor = Color(0x33441100))
                            .background(AksaWhite, cardShape)
                            .padding(24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Story Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sungai Cerita",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = AksaOrange
                                )

                                // Audio narration button
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(AksaPeachLight)
                                        .clickable {
                                            tts.speak("${currentPanel.title}. ${currentPanel.text}")
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
                                            text = "Bacakan",
                                            fontFamily = Poppins,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = AksaDarkBrown
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Illustration Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentPanel.emoji,
                                    fontSize = 64.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            Text(
                                text = currentPanel.title,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                color = AksaDarkBrown
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = currentPanel.text,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                textAlign = TextAlign.Center,
                                color = AksaDarkBrown.copy(alpha = 0.85f),
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Story progress dots
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (i in 0 until storyPanels.size) {
                                    val isCurrent = i == currentPanelIndex
                                    Box(
                                        modifier = Modifier
                                            .size(if (isCurrent) 24.dp else 10.dp, 10.dp)
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(if (isCurrent) AksaOrange else Color(0xFFCBD5E1))
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
                        if (currentPanelIndex > 0) {
                            AksaButton(
                                text = "Sebelumnya",
                                style = AksaButtonStyle.SECONDARY_LIME,
                                onClick = { currentPanelIndex-- },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        AksaButton(
                            text = if (currentPanelIndex < storyPanels.size - 1) "Lanjut Cerita" else "Jawab Soal Cerita",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = {
                                if (currentPanelIndex < storyPanels.size - 1) {
                                    currentPanelIndex++
                                    tts.speak(storyPanels[currentPanelIndex].text)
                                } else {
                                    stage = ReadingStage.COMPREHENSION_QUIZ
                                    currentQuestionIndex = 0
                                    tts.speak(questions[0].questionText)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // ==================== STAGE 2: COMPREHENSION QUIZ ====================
                ReadingStage.COMPREHENSION_QUIZ -> {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Pertanyaan Pemahaman",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AksaOrange
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(AksaPeachLight)
                                        .clickable {
                                            tts.speak(currentQ.questionText)
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
                                            text = "Dengarkan",
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
                                text = currentQ.questionText,
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center,
                                color = AksaDarkBrown
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Choice Cards
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (choice in currentQ.choices) {
                                    val isSelected = effectiveSelectedChoice == choice
                                    val isChoiceCorrect = choice == currentQ.targetAnswer

                                    val (choiceBg, choiceBorder) = when {
                                        !effectiveIsAnswered -> Pair(AksaWhite, Color(0xFFE2E8F0))
                                        isSelected && effectiveIsCorrect -> Pair(Color(0xFFE8F5E9), AksaLimeDark)
                                        isSelected && !effectiveIsCorrect -> Pair(Color(0xFFFFEBEE), Color(0xFFE53935))
                                        effectiveIsAnswered && isChoiceCorrect -> Pair(Color(0xFFE8F5E9), AksaLimeDark)
                                        else -> Pair(AksaWhite, Color(0xFFE2E8F0))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .shadow(2.dp, RoundedCornerShape(18.dp))
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(choiceBg)
                                            .border(2.dp, choiceBorder, RoundedCornerShape(18.dp))
                                            .clickable(enabled = !effectiveIsAnswered) {
                                                selectedChoice = choice
                                                isAnswered = true
                                                val correct = choice == currentQ.targetAnswer
                                                isCorrect = correct
                                                if (correct) {
                                                    correctCount++
                                                    tts.speak("Benar! Pintar sekali!")
                                                } else {
                                                    tts.speak("Belum tepat. Jawabannya adalah ${currentQ.targetAnswer}.")
                                                }
                                            }
                                            .padding(horizontal = 18.dp, vertical = 14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = choice,
                                                fontFamily = Poppins,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = AksaDarkBrown
                                            )
                                            if (effectiveIsAnswered && isChoiceCorrect) {
                                                Icon(
                                                    imageVector = AksaIcons.Check,
                                                    contentDescription = null,
                                                    tint = AksaLimeDark,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Feedback Banner
                            AnimatedVisibility(
                                visible = effectiveIsAnswered,
                                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val feedbackBg = if (effectiveIsCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    val feedbackBorder = if (effectiveIsCorrect) AksaLimeDark else Color(0xFFE53935)
                                    val feedbackMsg = if (effectiveIsCorrect) {
                                        "Luar biasa! Pemahamanmu sangat tepat."
                                    } else {
                                        "Belum tepat. Ingat kembali cerita: Kancil membagikan apel kepada ${currentQ.targetAnswer}."
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(feedbackBg)
                                            .border(1.5.dp, feedbackBorder, RoundedCornerShape(16.dp))
                                            .padding(14.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (effectiveIsCorrect) AksaIcons.Check else AksaIcons.Info,
                                                contentDescription = null,
                                                tint = feedbackBorder,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = feedbackMsg,
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
                                text = if (currentQuestionIndex < questions.size - 1) "Soal Berikutnya" else "Lihat Hasil Pemahaman",
                                style = if (effectiveIsAnswered) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                                enabled = effectiveIsAnswered,
                                onClick = {
                                    if (currentQuestionIndex < questions.size - 1) {
                                        currentQuestionIndex++
                                        selectedChoice = null
                                        isAnswered = false
                                        isCorrect = false
                                        tts.speak(questions[currentQuestionIndex].questionText)
                                    } else {
                                        if (!sessionCommitted) {
                                            sessionCommitted = true
                                            coroutineScope.launch {
                                                val sessionResult = SessionResult(
                                                    sessionId = "session_reading_${UUID.randomUUID()}",
                                                    childId = "child_marvin_demo",
                                                    moduleId = "sungai_cerita",
                                                    activityId = "reading_comprehension",
                                                    startedAt = System.currentTimeMillis() - 150_000,
                                                    completedAt = System.currentTimeMillis(),
                                                    totalQuestions = questions.size,
                                                    correctAnswers = correctCount,
                                                    incorrectAnswers = questions.size - correctCount,
                                                    accuracy = correctCount.toFloat() / questions.size.toFloat(),
                                                    status = SessionStatus.COMPLETED
                                                )
                                                AksaRepositoryProvider.sessionRepository.saveSessionResult(sessionResult)
                                            }
                                        }
                                        stage = ReadingStage.COMPLETION
                                    }
                                }
                            )
                        }
                    }
                }

                // ==================== STAGE 3: COMPLETION ====================
                ReadingStage.COMPLETION -> {
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
                                text = "Petualangan Selesai!",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = AksaDarkBrown
                            )

                            Text(
                                text = "Selamat! Kamu telah menyelesaikan membaca cerita dan menjawab soal pemahaman di Sungai Cerita.",
                                fontFamily = Poppins,
                                fontSize = 13.sp,
                                color = AksaDarkBrown.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            val scoreText = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "3 / 3" else "$correctCount / ${questions.size}"
                            val accuracyText = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "100%" else "${((correctCount.toFloat() / questions.size) * 100).toInt()}%"

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
                                            text = scoreText,
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
                                            text = accuracyText,
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
                                text = "Baca Cerita Lagi",
                                style = AksaButtonStyle.SECONDARY_LIME,
                                onClick = {
                                    stage = ReadingStage.READING_STORY
                                    currentPanelIndex = 0
                                    currentQuestionIndex = 0
                                    selectedChoice = null
                                    correctCount = 0
                                    sessionCommitted = false
                                }
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
