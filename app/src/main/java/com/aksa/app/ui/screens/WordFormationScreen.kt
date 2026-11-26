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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class WordQuestion(
    val id: String,
    val word: String,
    val emoji: String,
    val clueText: String,
    val speechText: String,
    val letters: List<String>,
    val shuffled: List<String>
)

private fun shuffleNotEqual(letters: List<String>): List<String> {
    if (letters.distinct().size <= 1) return letters
    var candidate = letters.shuffled()
    var attempts = 0
    while (candidate == letters && attempts < 5) {
        candidate = letters.shuffled()
        attempts++
    }
    return candidate
}

private val wordQuestionPool = listOf(
    WordQuestion("wq_1", "BOLA", "⚽", "Benda bulat untuk bermain", "Susun kata: BOLA. Benda bulat untuk bermain.", listOf("B", "O", "L", "A"), listOf("L", "A", "B", "O")),
    WordQuestion("wq_2", "BUKU", "📘", "Tempat membaca cerita seru", "Susun kata: BUKU. Tempat membaca cerita seru.", listOf("B", "U", "K", "U"), listOf("U", "K", "B", "U")),
    WordQuestion("wq_3", "KUDA", "🐴", "Hewan yang berlari kencang", "Susun kata: KUDA. Hewan yang berlari kencang.", listOf("K", "U", "D", "A"), listOf("D", "A", "K", "U")),
    WordQuestion("wq_4", "MATA", "👀", "Anggota tubuh untuk melihat", "Susun kata: MATA. Anggota tubuh untuk melihat.", listOf("M", "A", "T", "A"), listOf("T", "A", "M", "A")),
    WordQuestion("wq_5", "SAPI", "🐄", "Hewan penghasil susu segar", "Susun kata: SAPI. Hewan penghasil susu segar.", listOf("S", "A", "P", "I"), listOf("P", "I", "S", "A")),
    WordQuestion("wq_6", "BAJU", "👕", "Pakaian yang kita kenakan", "Susun kata: BAJU. Pakaian yang kita kenakan.", listOf("B", "A", "J", "U"), listOf("J", "U", "B", "A")),
    WordQuestion("wq_7", "TOPI", "🧢", "Dipakai di kepala saat panas", "Susun kata: TOPI. Dipakai di kepala saat panas.", listOf("T", "O", "P", "I"), listOf("P", "I", "T", "O")),
    WordQuestion("wq_8", "IKAN", "🐟", "Hewan yang hidup di air", "Susun kata: IKAN. Hewan yang hidup di air.", listOf("I", "K", "A", "N"), listOf("K", "N", "I", "A")),
    WordQuestion("wq_9", "APEL", "🍎", "Buah manis berwarna merah", "Susun kata: APEL. Buah manis berwarna merah.", listOf("A", "P", "E", "L"), listOf("P", "L", "A", "E")),
    WordQuestion("wq_10", "DAUN", "🍃", "Bagian pohon berwarna hijau", "Susun kata: DAUN. Bagian pohon berwarna hijau.", listOf("D", "A", "U", "N"), listOf("U", "N", "D", "A")),
    WordQuestion("wq_11", "AWAN", "☁️", "Gumpalan putih di langit", "Susun kata: AWAN. Gumpalan putih di langit.", listOf("A", "W", "A", "N"), listOf("W", "N", "A", "A")),
    WordQuestion("wq_12", "RODA", "🛞", "Bagian bundar pada mobil", "Susun kata: RODA. Bagian bundar pada mobil.", listOf("R", "O", "D", "A"), listOf("D", "A", "R", "O")),
    WordQuestion("wq_13", "PITA", "🎀", "Hiasan rambut yang cantik", "Susun kata: PITA. Hiasan rambut yang cantik.", listOf("P", "I", "T", "A"), listOf("T", "A", "P", "I")),
    WordQuestion("wq_14", "KADO", "🎁", "Hadiah spesial saat ulang tahun", "Susun kata: KADO. Hadiah spesial saat ulang tahun.", listOf("K", "A", "D", "O"), listOf("D", "O", "K", "A")),
    WordQuestion("wq_15", "NASI", "🍚", "Makanan pokok setiap hari", "Susun kata: NASI. Makanan pokok setiap hari.", listOf("N", "A", "S", "I"), listOf("S", "I", "N", "A"))
)

private fun generateWordFormationSession(count: Int = 5): List<WordQuestion> {
    return wordQuestionPool
        .shuffled()
        .take(count)
        .map { q -> q.copy(shuffled = shuffleNotEqual(q.letters)) }
}

@Composable
fun WordFormationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tts = remember { AndroidTextToSpeechHelper(context) }
    val isTtsReady by tts.isReady.collectAsState()

    var questions by remember { mutableStateOf(generateWordFormationSession()) }

    var sessionId by remember { mutableStateOf("session_word_${UUID.randomUUID()}") }
    var sessionStartedAt by remember { mutableStateOf(System.currentTimeMillis()) }
    val recordedAnswers = remember { mutableStateListOf<AnswerLog>() }
    val activeChildResolver = remember { GetActiveChildIdUseCase() }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val currentQ = questions[currentQuestionIndex]
    val selectedIndices = remember { mutableStateListOf<Int>() }

    var correctCount by remember { mutableIntStateOf(0) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var sessionCommitted by remember { mutableStateOf(false) }
    var isSavingSession by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }

    val requestExit = {
        if (!showCompletionDialog) {
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

    val currentLetters = selectedIndices.map { currentQ.shuffled[it] }
    val isComplete = selectedIndices.size == currentQ.letters.size
    val isCorrect = isComplete && currentLetters == currentQ.letters

    LaunchedEffect(sessionId, currentQuestionIndex, isTtsReady) {
        if (isTtsReady && !showCompletionDialog) {
            tts.speak(questions[currentQuestionIndex].speechText)
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
                text = "Dengarkan suaranya dan susun huruf menjadi kata!",
                fontFamily = Poppins,
                fontSize = 13.sp,
                color = AksaDarkBrown.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(18.dp))

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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(AksaPeachLight)
                            .clickable {
                                tts.speak(currentQ.speechText)
                            },
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

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .clip(PillShape)
                                .background(AksaWhite)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = AksaIcons.volumeUp(),
                                    contentDescription = "Dengarkan",
                                    tint = AksaOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dengar",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = AksaDarkBrown
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = currentQ.clueText,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = AksaDarkBrown.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until currentQ.letters.size) {
                            val letterInSlot = if (i < selectedIndices.size) currentQ.shuffled[selectedIndices[i]] else null
                            WordTargetSlot(
                                letter = letterInSlot,
                                onClick = {
                                    if (i < selectedIndices.size && !isComplete) {
                                        selectedIndices.removeAt(i)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedVisibility(
                        visible = isComplete,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val feedbackBg = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            val feedbackBorder = if (isCorrect) AksaLimeDark else Color(0xFFE53935)
                            val feedbackMsg = if (isCorrect) {
                                "Hebat sekali! Kamu berhasil menyusun kata ${currentQ.word}!"
                            } else {
                                "Belum tepat. Kata yang benar adalah ${currentQ.word}."
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
                                        painter = if (isCorrect) AksaIcons.check() else AksaIcons.info(),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (!isComplete) "Pilih huruf secara berurutan:" else "Susunan selesai!",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AksaDarkBrown.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
                                        // Ucapkan huruf yang diketuk terlebih dahulu
                                        tts.speak(letter)
                                        if (selectedIndices.size == currentQ.letters.size) {
                                            val formed = selectedIndices.map { currentQ.shuffled[it] }
                                            if (formed == currentQ.letters) {
                                                correctCount++
                                                tts.speakQueued("Benar! ${currentQ.word}!")
                                            } else {
                                                tts.speakQueued("Belum tepat. Yang benar adalah ${currentQ.word}.")
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AksaButton(
                        text = if (currentQuestionIndex < questions.size - 1) "Soal Berikutnya" else "Lihat Hasil Latihan",
                        style = if (isComplete) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                        enabled = isComplete,
                        onClick = {
                            val childId = runCatching { activeChildResolver.getSync() }
                                .getOrDefault(AksaUserState.currentProfileId)
                            val log = AnswerLog(
                                answerId = "ans_${sessionId}_word_${currentQ.id}",
                                sessionId = sessionId,
                                childId = childId,
                                moduleId = "gunung_kata",
                                activityId = "word_formation",
                                questionId = currentQ.id,
                                targetChar = currentQ.word,
                                chosenChar = currentLetters.joinToString(""),
                                isCorrect = isCorrect,
                                responseTimeMs = 2000L,
                                timestamp = System.currentTimeMillis()
                            )
                            recordedAnswers.add(log)

                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedIndices.clear()
                            } else {
                                if (!sessionCommitted) {
                                    sessionCommitted = true
                                    isSavingSession = true
                                    coroutineScope.launch {
                                        try {
                                            withContext(NonCancellable) {
                                                runCatching {
                                                    val actualChildId = activeChildResolver()
                                                    for (ans in recordedAnswers) {
                                                        AksaRepositoryProvider.sessionRepository
                                                            .recordAnswer(ans.copy(childId = actualChildId))
                                                    }
                                                    val sessionResult = SessionResult(
                                                        sessionId = sessionId,
                                                        childId = actualChildId,
                                                        moduleId = "gunung_kata",
                                                        activityId = "word_formation",
                                                        startedAt = sessionStartedAt,
                                                        completedAt = System.currentTimeMillis(),
                                                        totalQuestions = questions.size,
                                                        correctAnswers = correctCount,
                                                        incorrectAnswers = questions.size - correctCount,
                                                        accuracy = correctCount.toFloat() / questions.size.toFloat(),
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
                                tts.speak("Hebat! Latihan selesai!")
                                showCompletionDialog = true
                            }
                        }
                    )
                }
            }
        }

        if (showCompletionDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .clickable { }
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
                        Spacer(modifier = Modifier.height(8.dp))
                        EarnedStarsRow(accuracy = correctCount.toFloat() / questions.size.toFloat())

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

                        val scoreText = "$correctCount / ${questions.size}"
                        val accuracyText = "${((correctCount.toFloat() / questions.size) * 100).toInt()}%"

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
                                questions = generateWordFormationSession()
                                currentQuestionIndex = 0
                                selectedIndices.clear()
                                correctCount = 0
                                sessionCommitted = false
                                sessionId = "session_word_${UUID.randomUUID()}"
                                sessionStartedAt = System.currentTimeMillis()
                                recordedAnswers.clear()
                            }
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