package com.aksa.app.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

enum class MiniGameStage {
    INSTRUCTIONS,
    PLAYING,
    RESULTS
}

data class FallingLetter(
    val letter: String,
    val isTarget: Boolean,
    val laneIndex: Int // 0, 1, 2
)

@Composable
fun TangkapHurufScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tts = remember { AndroidTextToSpeechHelper(context) }

    val alphabetPool = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U")

    var stage by remember { mutableStateOf(MiniGameStage.INSTRUCTIONS) }
    var currentRound by remember { mutableIntStateOf(1) }
    val totalRounds = 15

    var score by remember { mutableIntStateOf(0) }
    var difficultyLevel by remember { mutableIntStateOf(1) } // 1, 2, 3
    var consecutiveCorrect by remember { mutableIntStateOf(0) }
    var consecutiveIncorrect by remember { mutableIntStateOf(0) }

    var targetLetter by remember { mutableStateOf("B") }
    var fallingItems by remember { mutableStateOf<List<FallingLetter>>(emptyList()) }
    var roundResolved by remember { mutableStateOf(false) }
    var roundFeedbackMsg by remember { mutableStateOf<String?>(null) }
    var sessionCommitted by remember { mutableStateOf(false) }

    // Fall animation progress (0f at top to 1f at bottom)
    val fallProgress = remember { Animatable(0f) }

    // Developer Preview overrides
    val visualState by DeveloperPreviewManager.visualState.collectAsState()
    val isDevPreview by DeveloperPreviewManager.isPreviewActive.collectAsState()

    val effectiveStage = when {
        isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED -> MiniGameStage.RESULTS
        else -> stage
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
        }
    }

    // Function to generate and start a new round
    fun startRound(roundNum: Int) {
        roundResolved = false
        roundFeedbackMsg = null

        val target = alphabetPool.random()
        targetLetter = target

        val distractors = alphabetPool.filter { it != target }.shuffled().take(2)
        val lanes = listOf(0, 1, 2).shuffled()

        val items = listOf(
            FallingLetter(target, isTarget = true, laneIndex = lanes[0]),
            FallingLetter(distractors[0], isTarget = false, laneIndex = lanes[1]),
            FallingLetter(distractors[1], isTarget = false, laneIndex = lanes[2])
        )
        fallingItems = items

        coroutineScope.launch {
            tts.speak("Tangkap huruf $target!")
            fallProgress.snapTo(0f)

            // Duration inversely related to difficulty
            val fallDuration = when (difficultyLevel) {
                1 -> 5500
                2 -> 4200
                else -> 3200
            }

            fallProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = fallDuration, easing = LinearEasing)
            )

            // Reached grass without catch
            if (!roundResolved) {
                roundResolved = true
                consecutiveCorrect = 0
                consecutiveIncorrect++
                if (consecutiveIncorrect >= 2 && difficultyLevel > 1) {
                    difficultyLevel--
                    consecutiveIncorrect = 0
                }
                delay(700)
                if (currentRound < totalRounds) {
                    currentRound++
                    startRound(currentRound)
                } else {
                    stage = MiniGameStage.RESULTS
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2FE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BAR
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

                // Stage or Round Pill
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AksaWhite)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = when (effectiveStage) {
                            MiniGameStage.INSTRUCTIONS -> "Tantangan Mini-Game"
                            MiniGameStage.PLAYING -> "Ronde $currentRound / $totalRounds"
                            MiniGameStage.RESULTS -> "Hasil Permainan"
                        },
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaDarkBrown
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (effectiveStage) {
                // ==================== STAGE 1: INSTRUCTIONS ====================
                MiniGameStage.INSTRUCTIONS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

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
                                MascotCheering(size = 120.dp)

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Tangkap Huruf!",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = AksaDarkBrown
                                )

                                Text(
                                    text = "Dengarkan suara huruf target, lalu ketuk huruf yang jatuh sebelum menyentuh rumput!",
                                    fontFamily = Poppins,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = AksaDarkBrown.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Feature Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(AksaYellow.copy(alpha = 0.3f))
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "15 Ronde",
                                                fontFamily = Poppins,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = AksaDarkBrown
                                            )
                                            Text(
                                                text = "Santai & Menyenangkan",
                                                fontFamily = Poppins,
                                                fontSize = 10.sp,
                                                color = AksaDarkBrown.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(AksaLime.copy(alpha = 0.35f))
                                            .padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "Adaptif 1–3",
                                                fontFamily = Poppins,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = AksaDarkBrown
                                            )
                                            Text(
                                                text = "Tingkat Kesulitan",
                                                fontFamily = Poppins,
                                                fontSize = 10.sp,
                                                color = AksaDarkBrown.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        AksaButton(
                            text = "Mulai Bermain!",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = {
                                stage = MiniGameStage.PLAYING
                                currentRound = 1
                                score = 0
                                difficultyLevel = 1
                                startRound(1)
                            }
                        )
                    }
                }

                // ==================== STAGE 2: PLAYING ====================
                MiniGameStage.PLAYING -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Game Status Bar: Target + Difficulty + Score
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(6.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(AksaWhite)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Target Banner with speaker
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    tts.speak("Tangkap huruf $targetLetter!")
                                }
                            ) {
                                Icon(
                                    imageVector = AksaIcons.VolumeUp,
                                    contentDescription = "Dengarkan",
                                    tint = AksaOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Target: ",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = AksaDarkBrown
                                )
                                Text(
                                    text = targetLetter,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color = AksaOrange
                                )
                            }

                            // Difficulty Level Chip
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(AksaLime.copy(alpha = 0.4f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Level $difficultyLevel",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = AksaDarkBrown
                                )
                            }

                            // Score Badge
                            Text(
                                text = "Skor: $score",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = AksaDarkBrown
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // FALLING SKY GAMEPLAY CANVAS AREA
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .shadow(8.dp, RoundedCornerShape(28.dp))
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFBAE6FD), Color(0xFFE0F2FE), Color(0xFFF0FDF4))
                                    )
                                )
                        ) {
                            val areaWidth = maxWidth
                            val areaHeight = maxHeight

                            // Green grass at bottom
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(AksaLime)
                            )

                            // Render falling letters
                            val lanePositions = listOf(0.18f, 0.50f, 0.82f)
                            val currentY = (areaHeight.value - 90f) * fallProgress.value

                            for (item in fallingItems) {
                                val laneX = lanePositions[item.laneIndex.coerceIn(0, 2)]
                                val letterXOffset = (areaWidth.value * laneX - 32f).dp

                                Box(
                                    modifier = Modifier
                                        .offset(x = letterXOffset, y = currentY.dp)
                                        .size(64.dp)
                                        .shadow(6.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(if (item.isTarget) AksaYellow else AksaWhite)
                                        .border(3.dp, if (item.isTarget) AksaOrange else Color(0xFFCBD5E1), CircleShape)
                                        .clickable(enabled = !roundResolved) {
                                            roundResolved = true
                                            if (item.isTarget) {
                                                score++
                                                consecutiveCorrect++
                                                consecutiveIncorrect = 0
                                                tts.speak("Bagus! Huruf $targetLetter tertangkap!")
                                                roundFeedbackMsg = "Bagus! +1 Skor"

                                                if (consecutiveCorrect >= 3 && difficultyLevel < 3) {
                                                    difficultyLevel++
                                                    consecutiveCorrect = 0
                                                }
                                            } else {
                                                consecutiveCorrect = 0
                                                consecutiveIncorrect++
                                                tts.speak("Itu huruf ${item.letter}. Cari huruf $targetLetter ya!")
                                                roundFeedbackMsg = "Itu huruf ${item.letter}!"

                                                if (consecutiveIncorrect >= 2 && difficultyLevel > 1) {
                                                    difficultyLevel--
                                                    consecutiveIncorrect = 0
                                                }
                                            }

                                            coroutineScope.launch {
                                                delay(900)
                                                if (currentRound < totalRounds) {
                                                    currentRound++
                                                    startRound(currentRound)
                                                } else {
                                                    stage = MiniGameStage.RESULTS
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.letter,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 28.sp,
                                        color = AksaDarkBrown
                                    )
                                }
                            }

                            // Dynamic Round Feedback Text
                            roundFeedbackMsg?.let { msg ->
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .clip(PillShape)
                                        .background(AksaDarkBrown.copy(alpha = 0.85f))
                                        .padding(horizontal = 20.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = msg,
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = AksaWhite
                                    )
                                }
                            }
                        }
                    }
                }

                // ==================== STAGE 3: RESULTS ====================
                MiniGameStage.RESULTS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(20.dp))

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
                                MascotCheering(size = 120.dp)

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Permainan Selesai!",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = AksaDarkBrown
                                )

                                Text(
                                    text = "Hebat! Kamu telah menyelesaikan 15 ronde Tangkap Huruf dengan penuh semangat.",
                                    fontFamily = Poppins,
                                    fontSize = 13.sp,
                                    color = AksaDarkBrown.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                val finalScoreDisplay = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "14 / 15" else "$score / $totalRounds"
                                val finalAccuracyPct = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "93%" else "${((score.toFloat() / totalRounds) * 100).toInt()}%"

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
                                                text = finalScoreDisplay,
                                                fontFamily = Poppins,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp,
                                                color = AksaDarkBrown
                                            )
                                            Text(
                                                text = "Huruf Tertangkap",
                                                fontFamily = Poppins,
                                                fontSize = 11.sp,
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
                                                text = finalAccuracyPct,
                                                fontFamily = Poppins,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp,
                                                color = AksaDarkBrown
                                            )
                                            Text(
                                                text = "Akurasi Tangkapan",
                                                fontFamily = Poppins,
                                                fontSize = 11.sp,
                                                color = AksaDarkBrown.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }

                                // Commit session to repository
                                LaunchedEffect(Unit) {
                                    if (!sessionCommitted && !isDevPreview) {
                                        sessionCommitted = true
                                        val sessionResult = SessionResult(
                                            sessionId = "session_minigame_${UUID.randomUUID()}",
                                            childId = "child_marvin_demo",
                                            moduleId = "pulau_alfabet",
                                            activityId = "tangkap_huruf",
                                            startedAt = System.currentTimeMillis() - 100_000,
                                            completedAt = System.currentTimeMillis(),
                                            totalQuestions = totalRounds,
                                            correctAnswers = score,
                                            incorrectAnswers = totalRounds - score,
                                            accuracy = score.toFloat() / totalRounds.toFloat(),
                                            status = SessionStatus.COMPLETED
                                        )
                                        AksaRepositoryProvider.sessionRepository.saveSessionResult(sessionResult)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        AksaButton(
                            text = "Main Lagi",
                            style = AksaButtonStyle.SECONDARY_LIME,
                            onClick = {
                                stage = MiniGameStage.PLAYING
                                currentRound = 1
                                score = 0
                                difficultyLevel = 1
                                sessionCommitted = false
                                startRound(1)
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
