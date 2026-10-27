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
import com.aksa.app.ui.components.AksaSyllableCard
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.MascotLearning
import com.aksa.app.ui.components.SyllableCardColor
import com.aksa.app.ui.preview.DeveloperPreviewManager
import com.aksa.app.ui.preview.PreviewVisualState
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

data class SyllableQuizItem(
    val id: String,
    val targetWord: String,
    val part1: String,
    val part2: String,
    val choices: List<String>
)

@Composable
fun SyllableQuizScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tts = remember { AndroidTextToSpeechHelper(context) }

    val quizItems = remember {
        listOf(
            SyllableQuizItem("sq_1", "MATA", "MA", "TA", listOf("KU", "CA", "MA", "GA", "SO", "TA")),
            SyllableQuizItem("sq_2", "BUKU", "BU", "KU", listOf("BU", "SA", "PI", "KU", "DA", "LA")),
            SyllableQuizItem("sq_3", "BATU", "BA", "TU", listOf("BA", "TU", "MA", "RI", "KA", "LI")),
            SyllableQuizItem("sq_4", "BOLA", "BO", "LA", listOf("BO", "KU", "LA", "PA", "TA", "MI")),
            SyllableQuizItem("sq_5", "KAKI", "KA", "KI", listOf("KA", "MA", "KI", "DA", "LU", "SI"))
        )
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var slot1 by remember { mutableStateOf<String?>(null) }
    var slot2 by remember { mutableStateOf<String?>(null) }
    var correctCount by remember { mutableIntStateOf(0) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var sessionCommitted by remember { mutableStateOf(false) }

    val currentQ = quizItems[currentQuestionIndex]

    // Developer Preview overrides
    val visualState by DeveloperPreviewManager.visualState.collectAsState()
    val isDevPreview by DeveloperPreviewManager.isPreviewActive.collectAsState()

    val effectiveSlot1 = when {
        isDevPreview && visualState == PreviewVisualState.CORRECT_FEEDBACK -> currentQ.part1
        isDevPreview && visualState == PreviewVisualState.INCORRECT_FEEDBACK -> currentQ.choices.firstOrNull { it != currentQ.part1 } ?: "SA"
        else -> slot1
    }

    val effectiveSlot2 = when {
        isDevPreview && visualState == PreviewVisualState.CORRECT_FEEDBACK -> currentQ.part2
        isDevPreview && visualState == PreviewVisualState.INCORRECT_FEEDBACK -> currentQ.choices.lastOrNull { it != currentQ.part2 } ?: "LA"
        else -> slot2
    }

    val isFilled = effectiveSlot1 != null && effectiveSlot2 != null
    val isCorrect = effectiveSlot1 == currentQ.part1 && effectiveSlot2 == currentQ.part2
    val effectiveShowCompletion = (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) || showCompletionDialog

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
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

                // Question progress pill
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AksaWhite.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Soal ${currentQuestionIndex + 1} dari ${quizItems.size}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaDarkBrown
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large White Rounded Card
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
                            text = "Susun Suku Kata",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AksaOrange
                        )

                        // TTS Audio Button
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(AksaPeachLight)
                                .clickable {
                                    tts.speak("Susun kata: ${currentQ.targetWord}.")
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

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Susun kata '${currentQ.targetWord}':",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = AksaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // TARGET SLOTS ROW
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Slot 1
                        SyllableTargetSlot(
                            syllable = effectiveSlot1,
                            onClear = {
                                if (!isFilled) slot1 = null
                            }
                        )

                        Text(
                            text = "+",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = AksaDarkBrown
                        )

                        // Slot 2
                        SyllableTargetSlot(
                            syllable = effectiveSlot2,
                            onClear = {
                                if (!isFilled) slot2 = null
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // FEEDBACK BANNER & MASCOT
                    AnimatedVisibility(
                        visible = isFilled,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val feedbackBg = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            val feedbackBorder = if (isCorrect) AksaLimeDark else Color(0xFFE53935)
                            val feedbackMsg = if (isCorrect) {
                                "Luar biasa! ${currentQ.part1} + ${currentQ.part2} = ${currentQ.targetWord}."
                            } else {
                                "Belum tepat. Coba ketuk kotak untuk mengosongkan dan susun lagi ya!"
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
                                        imageVector = if (isCorrect) AksaIcons.Check else AksaIcons.Info,
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
                                        color = if (isCorrect) AksaDarkBrown else Color(0xFFC62828)
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

                    Spacer(modifier = Modifier.height(18.dp))

                    // INSTRUCTION LABEL
                    Text(
                        text = if (!isFilled) "Ketuk suku kata di bawah untuk mengisi:" else "Susunan selesai!",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AksaDarkBrown.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // CHOICE TILES GRID (2 rows of 3)
                    val row1 = currentQ.choices.take(3)
                    val row2 = currentQ.choices.drop(3)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (syl in row1) {
                            val isUsed = syl == effectiveSlot1 || syl == effectiveSlot2
                            Box(modifier = Modifier.weight(1f)) {
                                AksaSyllableCard(
                                    syllable = syl,
                                    colorTheme = if (isUsed) SyllableCardColor.ORANGE else SyllableCardColor.GREEN,
                                    onClick = {
                                        if (!isFilled) {
                                            if (slot1 == null) {
                                                slot1 = syl
                                                tts.speak(syl)
                                            } else if (slot2 == null && slot1 != syl) {
                                                slot2 = syl
                                                tts.speak(syl)
                                                if (slot1 == currentQ.part1 && syl == currentQ.part2) {
                                                    correctCount++
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (syl in row2) {
                            val isUsed = syl == effectiveSlot1 || syl == effectiveSlot2
                            Box(modifier = Modifier.weight(1f)) {
                                AksaSyllableCard(
                                    syllable = syl,
                                    colorTheme = if (isUsed) SyllableCardColor.ORANGE else SyllableCardColor.GREEN,
                                    onClick = {
                                        if (!isFilled) {
                                            if (slot1 == null) {
                                                slot1 = syl
                                                tts.speak(syl)
                                            } else if (slot2 == null && slot1 != syl) {
                                                slot2 = syl
                                                tts.speak(syl)
                                                if (slot1 == currentQ.part1 && syl == currentQ.part2) {
                                                    correctCount++
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // BUTTON: NEXT QUESTION / FINISH
                    AksaButton(
                        text = if (currentQuestionIndex < quizItems.size - 1) "Soal Berikutnya" else "Lihat Hasil Latihan",
                        style = if (isFilled) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                        enabled = isFilled,
                        onClick = {
                            if (currentQuestionIndex < quizItems.size - 1) {
                                currentQuestionIndex++
                                slot1 = null
                                slot2 = null
                                tts.speak("Susun kata: ${quizItems[currentQuestionIndex].targetWord}.")
                            } else {
                                // Finalize session
                                if (!sessionCommitted) {
                                    sessionCommitted = true
                                    coroutineScope.launch {
                                        val sessionResult = SessionResult(
                                            sessionId = "session_syllable_${UUID.randomUUID()}",
                                            childId = "child_marvin_demo",
                                            moduleId = "dunia_suku_kata",
                                            activityId = "syllable_quiz",
                                            startedAt = System.currentTimeMillis() - 90_000,
                                            completedAt = System.currentTimeMillis(),
                                            totalQuestions = quizItems.size,
                                            correctAnswers = correctCount,
                                            incorrectAnswers = quizItems.size - correctCount,
                                            accuracy = correctCount.toFloat() / quizItems.size.toFloat(),
                                            status = SessionStatus.COMPLETED
                                        )
                                        AksaRepositoryProvider.sessionRepository.saveSessionResult(sessionResult)
                                    }
                                }
                                showCompletionDialog = true
                            }
                        }
                    )
                }
            }
        }

        // COMPLETION CELEBRATION MODAL
        if (effectiveShowCompletion) {
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

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Latihan Selesai!",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = AksaDarkBrown
                        )

                        Text(
                            text = "Keren! Kamu berhasil menyelesaikan 5 soal penyusunan suku kata di Dunia Suku Kata.",
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            color = AksaDarkBrown.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        val scoreText = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "5 / 5" else "$correctCount / ${quizItems.size}"
                        val accuracyText = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "100%" else "${((correctCount.toFloat() / quizItems.size) * 100).toInt()}%"

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
                            text = "Ulangi Latihan",
                            style = AksaButtonStyle.SECONDARY_LIME,
                            onClick = {
                                showCompletionDialog = false
                                currentQuestionIndex = 0
                                slot1 = null
                                slot2 = null
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

@Composable
private fun SyllableTargetSlot(
    syllable: String?,
    onClear: () -> Unit
) {
    val isOccupied = syllable != null
    val bgColor = if (isOccupied) AksaLime.copy(alpha = 0.25f) else Color(0xFFF1F5F9)
    val borderColor = if (isOccupied) AksaLimeDark else Color(0xFFCBD5E1)

    Box(
        modifier = Modifier
            .size(80.dp)
            .shadow(if (isOccupied) 4.dp else 0.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(enabled = isOccupied, onClick = onClear),
        contentAlignment = Alignment.Center
    ) {
        if (isOccupied) {
            Text(
                text = syllable ?: "",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = AksaDarkBrown
            )
        } else {
            Text(
                text = "?",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
