package com.aksa.app.ui.screens

import android.content.Context
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import kotlinx.coroutines.launch
import java.util.UUID

data class WordQuestion(
    val id: String,
    val word: String,
    val emoji: String,
    val hint: String,
    val letters: List<String>,
    val shuffled: List<String>
)

@Composable
fun WordFormationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tts = remember { AndroidTextToSpeechHelper(context) }

    val questions = remember {
        listOf(
            WordQuestion("wq_1", "BOLA", "⚽", "B-O-L-A -> Bola untuk bermain", listOf("B", "O", "L", "A"), listOf("L", "A", "B", "O")),
            WordQuestion("wq_2", "BUKU", "📖", "B-U-K-U -> Buku cerita seru", listOf("B", "U", "K", "U"), listOf("U", "K", "B", "U")),
            WordQuestion("wq_3", "KUDA", "🐴", "K-U-D-A -> Kuda berlari kencang", listOf("K", "U", "D", "A"), listOf("D", "A", "K", "U")),
            WordQuestion("wq_4", "MATA", "👀", "M-A-T-A -> Mata untuk melihat", listOf("M", "A", "T", "A"), listOf("T", "A", "M", "A")),
            WordQuestion("wq_5", "SAPI", "🐮", "S-A-P-I -> Sapi menghasilkan susu", listOf("S", "A", "P", "I"), listOf("P", "I", "S", "A"))
        )
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val currentQ = questions[currentQuestionIndex]
    val selectedIndices = remember { mutableStateListOf<Int>() }

    var correctCount by remember { mutableIntStateOf(0) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var sessionCommitted by remember { mutableStateOf(false) }

    // Developer Preview overrides
    val visualState by DeveloperPreviewManager.visualState.collectAsState()
    val isDevPreview by DeveloperPreviewManager.isPreviewActive.collectAsState()

    val currentLetters = selectedIndices.map { currentQ.shuffled[it] }
    val isComplete = selectedIndices.size == currentQ.letters.size
    val isCorrect = isComplete && currentLetters == currentQ.letters

    val effectiveIsComplete = if (isDevPreview && (visualState == PreviewVisualState.CORRECT_FEEDBACK || visualState == PreviewVisualState.INCORRECT_FEEDBACK)) true else isComplete
    val effectiveIsCorrect = if (isDevPreview && visualState == PreviewVisualState.CORRECT_FEEDBACK) true else if (isDevPreview && visualState == PreviewVisualState.INCORRECT_FEEDBACK) false else isCorrect
    val effectiveShowCompletion = (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) || showCompletionDialog

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
        }
    }

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

                // Question Counter Pill
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AksaWhite)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Soal ${currentQuestionIndex + 1} dari ${questions.size}",
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
                fontSize = 26.sp,
                color = AksaDarkBrown
            )

            Text(
                text = "Susun huruf-huruf menjadi kata yang tepat!",
                fontFamily = Poppins,
                fontSize = 13.sp,
                color = AksaDarkBrown.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Main White Rounded Puzzle Card
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
                    // Illustration Header Box with Audio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(AksaPeachLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (currentQ.word == "BOLA") {
                                BallIllustration()
                            } else {
                                Text(
                                    text = currentQ.emoji,
                                    fontSize = 72.sp
                                )
                            }
                        }

                        // Audio button in top-right of illustration box
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AksaWhite)
                                .clickable {
                                    tts.speak("Susun kata: ${currentQ.word}. ${currentQ.hint}.")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AksaIcons.VolumeUp,
                                contentDescription = "Dengarkan",
                                tint = AksaOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Petunjuk: ${currentQ.hint}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AksaDarkBrown.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // TARGET WORD SLOTS
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until currentQ.letters.size) {
                            val letterInSlot = when {
                                isDevPreview && visualState == PreviewVisualState.CORRECT_FEEDBACK -> currentQ.letters[i]
                                isDevPreview && visualState == PreviewVisualState.INCORRECT_FEEDBACK -> currentQ.shuffled[i]
                                i < selectedIndices.size -> currentQ.shuffled[selectedIndices[i]]
                                else -> null
                            }
                            WordTargetSlot(
                                letter = letterInSlot,
                                onClick = {
                                    if (i < selectedIndices.size && !isDevPreview) {
                                        selectedIndices.removeAt(i)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // FEEDBACK BANNER
                    AnimatedVisibility(
                        visible = effectiveIsComplete,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val feedbackBg = if (effectiveIsCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            val feedbackBorder = if (effectiveIsCorrect) AksaLimeDark else Color(0xFFE53935)
                            val feedbackMsg = if (effectiveIsCorrect) {
                                "Hebat sekali! Kamu berhasil menyusun kata ${currentQ.word}!"
                            } else {
                                "Belum tepat. Ketuk kotak huruf untuk menghapus dan coba lagi ya!"
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (!effectiveIsComplete) "Pilih huruf secara berurutan:" else "Susunan selesai!",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AksaDarkBrown.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // SHUFFLED LETTER CHOICES
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        currentQ.shuffled.forEachIndexed { index, letter ->
                            val isSelected = selectedIndices.contains(index)
                            LetterChoiceTile(
                                letter = letter,
                                isSelected = isSelected,
                                onClick = {
                                    if (!isComplete && !isSelected) {
                                        selectedIndices.add(index)
                                        tts.speak(letter)
                                        if (selectedIndices.size == currentQ.letters.size) {
                                            val formed = selectedIndices.map { currentQ.shuffled[it] }
                                            if (formed == currentQ.letters) {
                                                correctCount++
                                                tts.speak("Benar! ${currentQ.word}!")
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // NEXT BUTTON
                    AksaButton(
                        text = if (currentQuestionIndex < questions.size - 1) "Soal Berikutnya" else "Lihat Hasil Latihan",
                        style = if (effectiveIsComplete) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                        enabled = effectiveIsComplete,
                        onClick = {
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedIndices.clear()
                                tts.speak("Susun kata: ${questions[currentQuestionIndex].word}.")
                            } else {
                                if (!sessionCommitted) {
                                    sessionCommitted = true
                                    coroutineScope.launch {
                                        val sessionResult = SessionResult(
                                            sessionId = "session_word_${UUID.randomUUID()}",
                                            childId = "child_marvin_demo",
                                            moduleId = "gunung_kata",
                                            activityId = "word_formation",
                                            startedAt = System.currentTimeMillis() - 120_000,
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
                            text = "Luar biasa! Kamu berhasil menaklukkan Gunung Kata dengan menyusun 5 kata bahasa Indonesia.",
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            color = AksaDarkBrown.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        val scoreText = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "5 / 5" else "$correctCount / ${questions.size}"
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
                            text = "Ulangi Latihan",
                            style = AksaButtonStyle.SECONDARY_LIME,
                            onClick = {
                                showCompletionDialog = false
                                currentQuestionIndex = 0
                                selectedIndices.clear()
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
private fun WordTargetSlot(
    letter: String?,
    onClick: () -> Unit
) {
    val isFilled = letter != null
    val shape = RoundedCornerShape(16.dp)

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

@Composable
private fun BallIllustration() {
    Canvas(modifier = Modifier.size(90.dp)) {
        val r = size.minDimension / 2f
        val c = Offset(size.width / 2f, size.height / 2f)

        // Base circle
        drawCircle(color = AksaWhite, radius = r, center = c)

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

        drawCircle(color = AksaDarkBrown, radius = r, center = c, style = Stroke(width = 3.5f))
    }
}
