package com.aksa.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.audio.AndroidTextToSpeechHelper
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.preview.DeveloperPreviewManager
import com.aksa.app.ui.preview.PreviewVisualState
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import com.aksa.app.ui.viewmodel.MiniGameStage
import com.aksa.app.ui.viewmodel.TangkapHurufViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.abs

/**
 * Aksa Mascot holding a woven wicker basket for catching letters.
 */
@Composable
fun MascotWithBasket(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    animated: Boolean = true
) {
    Box(
        modifier = modifier.size(width = size, height = size * 1.15f),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Mascot body at bottom
        MascotCheering(
            size = size,
            animated = animated
        )

        // Woven basket canvas positioned above paws
        Canvas(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.92f)
                .height(size * 0.45f)
        ) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f

            // Basket shadow
            drawOval(
                color = Color(0x20000000),
                topLeft = Offset(cx - w * 0.38f, h * 0.70f),
                size = Size(w * 0.76f, h * 0.22f)
            )

            // Basket geometry
            val rimTop = h * 0.05f
            val rimHeight = h * 0.24f
            val basketBodyTop = rimTop + rimHeight * 0.5f
            val basketBodyBottom = h * 0.88f
            val topWidth = w * 0.88f
            val bottomWidth = w * 0.62f

            // Basket body path (trapezoid with rounded base)
            val basketPath = Path().apply {
                moveTo(cx - topWidth / 2f, basketBodyTop)
                lineTo(cx + topWidth / 2f, basketBodyTop)
                quadraticTo(
                    cx + topWidth * 0.48f, basketBodyBottom,
                    cx + bottomWidth / 2f, basketBodyBottom
                )
                lineTo(cx - bottomWidth / 2f, basketBodyBottom)
                quadraticTo(
                    cx - topWidth * 0.48f, basketBodyBottom,
                    cx - topWidth / 2f, basketBodyTop
                )
                close()
            }

            // Fill basket body with warm wicker brown
            drawPath(path = basketPath, color = Color(0xFFD97706)) // Amber-600

            // Woven horizontal texture lines
            for (i in 1..3) {
                val lineY = basketBodyTop + (basketBodyBottom - basketBodyTop) * (i / 4f)
                val lineW = topWidth - (topWidth - bottomWidth) * (i / 4f)
                drawLine(
                    color = Color(0xFFB45309), // Amber-700
                    start = Offset(cx - lineW / 2f + 4f, lineY),
                    end = Offset(cx + lineW / 2f - 4f, lineY),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }

            // Outer basket rim ring
            drawOval(
                color = Color(0xFFF59E0B), // Amber-500
                topLeft = Offset(cx - topWidth / 2f, rimTop),
                size = Size(topWidth, rimHeight)
            )
            drawOval(
                color = Color(0xFF92400E), // Amber-800
                topLeft = Offset(cx - topWidth / 2f, rimTop),
                size = Size(topWidth, rimHeight),
                style = Stroke(width = 3.5f)
            )

            // Dark inner opening depth
            drawOval(
                color = Color(0xFF78350F), // Amber-900
                topLeft = Offset(cx - topWidth * 0.42f, rimTop + rimHeight * 0.18f),
                size = Size(topWidth * 0.84f, rimHeight * 0.62f)
            )
        }
    }
}

@Composable
fun TangkapHurufScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit,
    viewModel: TangkapHurufViewModel? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val tts = remember { AndroidTextToSpeechHelper(context) }

    val vm: TangkapHurufViewModel = viewModel ?: viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: java.lang.Class<T>): T {
                return TangkapHurufViewModel(audioSpeaker = tts) as T
            }
        }
    )

    val uiState by vm.uiState.collectAsState()

    // Developer Preview overrides
    val visualState by DeveloperPreviewManager.visualState.collectAsState()
    val isDevPreview by DeveloperPreviewManager.isPreviewActive.collectAsState()

    val effectiveStage = when {
        isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED -> MiniGameStage.RESULTS
        else -> uiState.stage
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
        }
    }

    // Exit back handling: abandon session if exiting while playing
    val handleExit = {
        if (uiState.stage == MiniGameStage.PLAYING) {
            vm.abandonSession()
        }
        onNavigateBack()
    }

    BackHandler(onBack = handleExit)

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
                            MiniGameStage.PLAYING -> "Ronde ${uiState.currentRound} / ${uiState.totalRounds}"
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
                        Spacer(modifier = Modifier.height(16.dp))

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
                                MascotWithBasket(size = 130.dp)

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Tangkap Huruf!",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = AksaDarkBrown
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Dengarkan suara huruf target, lalu geser keranjang Aksa untuk menangkap huruf yang tepat!",
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
                                                text = "Santai & Fun",
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
                                                text = "Level 1–3",
                                                fontFamily = Poppins,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = AksaDarkBrown
                                            )
                                            Text(
                                                text = "Otomatis Adaptif",
                                                fontFamily = Poppins,
                                                fontSize = 10.sp,
                                                color = AksaDarkBrown.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        AksaButton(
                            text = "Mulai Bermain!",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = {
                                vm.startGame()
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
                            // Target Banner with speaker icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    vm.playCurrentTargetAudio()
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
                                    text = uiState.targetLetter,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
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
                                    text = "Level ${uiState.difficultyLevel}",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = AksaDarkBrown
                                )
                            }

                            // Score Badge
                            Text(
                                text = "Skor: ${uiState.score}",
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
                            val containerWidthPx = constraints.maxWidth.toFloat()
                            val containerHeightPx = constraints.maxHeight.toFloat()

                            // Mascot Basket dimensions
                            val mascotWidthPx = with(density) { 130.dp.toPx() }
                            val mascotHeightPx = with(density) { 145.dp.toPx() }

                            val basketWidthPx = with(density) { 110.dp.toPx() }
                            val basketTopOffsetPx = with(density) { 5.dp.toPx() }
                            val basketBottomOffsetPx = with(density) { 45.dp.toPx() }
                            val letterSizePx = with(density) { 64.dp.toPx() }
                            val grassHeightPx = with(density) { 48.dp.toPx() }

                            // Horizontal basket position (normalized 0.12f .. 0.88f)
                            var basketNormX by remember { mutableFloatStateOf(0.50f) }
                            val currentBasketNormX by rememberUpdatedState(basketNormX)

                            // Reset mascot basket to center at start of each round
                            LaunchedEffect(uiState.currentRound) {
                                basketNormX = 0.50f
                            }

                            // Active letter progress map for this round: id -> normalized progress (0f..1f)
                            var letterProgressMap by remember(uiState.currentRound) { mutableStateOf<Map<String, Float>>(emptyMap()) }
                            var caughtLetterId by remember(uiState.currentRound) { mutableStateOf<String?>(null) }
                            val roundStartTimeMs = remember(uiState.currentRound) { System.currentTimeMillis() }

                            // Frame-synchronized gameplay loop for active round
                            LaunchedEffect(uiState.currentRound) {
                                letterProgressMap = emptyMap()
                                caughtLetterId = null
                                var roundHandled = false
                                val startFrameNanos = withFrameNanos { it }

                                while (isActive && !roundHandled && !uiState.roundResolved) {
                                    withFrameNanos { currentFrameNanos ->
                                        val elapsedMs = (currentFrameNanos - startFrameNanos) / 1_000_000L
                                        val basketCenterX = currentBasketNormX * containerWidthPx
                                        val basketTopY = containerHeightPx - mascotHeightPx + basketTopOffsetPx
                                        val basketBottomY = containerHeightPx - mascotHeightPx + basketBottomOffsetPx
                                        val fallDistancePx = containerHeightPx - grassHeightPx - letterSizePx / 2f

                                        val nextProgressMap = mutableMapOf<String, Float>()
                                        var allLettersCompleted = true

                                        for (item in uiState.fallingLetters) {
                                            if (elapsedMs < item.spawnDelayMs) {
                                                allLettersCompleted = false
                                                continue
                                            }

                                            val itemElapsedMs = elapsedMs - item.spawnDelayMs
                                            val progress = (itemElapsedMs.toFloat() / item.fallDurationMs).coerceAtLeast(0f)

                                            if (progress < 1.0f) {
                                                allLettersCompleted = false
                                                nextProgressMap[item.id] = progress

                                                // Collision check against basket rim opening
                                                if (!roundHandled && !uiState.roundResolved && progress > 0.05f) {
                                                    val itemCenterX = item.normX * containerWidthPx
                                                    val letterTopY = fallDistancePx * progress
                                                    val letterCenterY = letterTopY + letterSizePx / 2f

                                                    val horizontalDist = abs(itemCenterX - basketCenterX)
                                                    val isHorizontallyAligned = horizontalDist <= (basketWidthPx / 2f + with(density) { 18.dp.toPx() })
                                                    val isVerticallyInBasket = letterCenterY >= (basketTopY - with(density) { 18.dp.toPx() }) &&
                                                            letterCenterY <= (basketBottomY + with(density) { 14.dp.toPx() })

                                                    if (isHorizontallyAligned && isVerticallyInBasket) {
                                                        roundHandled = true
                                                        caughtLetterId = item.id
                                                        val responseTime = (elapsedMs - item.spawnDelayMs).coerceAtLeast(0L)
                                                        vm.onLetterCaught(item, responseTime)
                                                        return@withFrameNanos
                                                    }
                                                }
                                            } else {
                                                // Letter hit the bottom grass
                                                if (item.isTarget && !roundHandled && !uiState.roundResolved) {
                                                    roundHandled = true
                                                    val responseTime = (elapsedMs - item.spawnDelayMs).coerceAtLeast(0L)
                                                    vm.onLetterMissed(responseTime)
                                                    return@withFrameNanos
                                                }
                                            }
                                        }

                                        if (!roundHandled && allLettersCompleted && !uiState.roundResolved) {
                                            roundHandled = true
                                            val responseTime = (System.currentTimeMillis() - roundStartTimeMs).coerceAtLeast(0L)
                                            vm.onLetterMissed(responseTime)
                                            return@withFrameNanos
                                        }

                                        letterProgressMap = nextProgressMap
                                    }
                                }
                            }

                            // Handle transition to next round after feedback delay
                            LaunchedEffect(uiState.roundResolved) {
                                if (uiState.roundResolved) {
                                    delay(1400)
                                    vm.nextRoundOrFinish()
                                }
                            }

                            // Green grass at bottom
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(AksaLime)
                            )

                            // Render falling letters
                            val basketCenterX = currentBasketNormX * containerWidthPx
                            val basketTopY = containerHeightPx - mascotHeightPx + basketTopOffsetPx
                            val fallDistancePx = containerHeightPx - grassHeightPx - letterSizePx / 2f

                            for (item in uiState.fallingLetters) {
                                val progress = letterProgressMap[item.id]
                                val isThisCaught = (item.id == caughtLetterId)

                                if (progress != null || (isThisCaught && uiState.roundResolved)) {
                                    val itemCenterX = item.normX * containerWidthPx
                                    val letterXOffsetPx = if (isThisCaught) {
                                        (basketCenterX - letterSizePx / 2f).coerceIn(4f, containerWidthPx - letterSizePx - 4f)
                                    } else {
                                        (itemCenterX - letterSizePx / 2f).coerceIn(4f, containerWidthPx - letterSizePx - 4f)
                                    }

                                    val letterYPx = if (isThisCaught) {
                                        basketTopY + with(density) { 6.dp.toPx() }
                                    } else {
                                        fallDistancePx * (progress ?: 0f)
                                    }

                                    val letterXOffsetDp = with(density) { letterXOffsetPx.toDp() }
                                    val currentYDp = with(density) { letterYPx.toDp() }

                                    Box(
                                        modifier = Modifier
                                            .offset(
                                                x = letterXOffsetDp,
                                                y = currentYDp
                                            )
                                            .size(64.dp)
                                            .shadow(6.dp, CircleShape)
                                            .clip(CircleShape)
                                            .background(if (item.isTarget) AksaYellow else AksaWhite)
                                            .border(3.dp, if (item.isTarget) AksaOrange else Color(0xFFCBD5E1), CircleShape),
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
                            }

                            // DRAGGABLE MASCOT WITH BASKET
                            val mascotXOffsetPx = (basketCenterX - mascotWidthPx / 2f)
                                .coerceIn(0f, containerWidthPx - mascotWidthPx)
                            val mascotXOffsetDp = with(density) { mascotXOffsetPx.toDp() }

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(
                                        x = mascotXOffsetDp,
                                        y = (-10).dp
                                    )
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val newNormX = basketNormX + (dragAmount.x / containerWidthPx)
                                            basketNormX = newNormX.coerceIn(0.12f, 0.88f)
                                        }
                                    }
                            ) {
                                MascotWithBasket(size = 120.dp)
                            }

                            // Interactive Drag Hint overlay at start of round 1
                            val isEarlyRound1 = uiState.currentRound == 1 && !uiState.roundResolved &&
                                    (letterProgressMap.values.any { it < 0.35f } || letterProgressMap.isEmpty())
                            if (isEarlyRound1) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 135.dp)
                                        .clip(PillShape)
                                        .background(AksaDarkBrown.copy(alpha = 0.75f))
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "👈 Geser Aksa untuk menangkap 👉",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AksaWhite
                                    )
                                }
                            }

                            // Dynamic Round Feedback Banner
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AnimatedVisibility(
                                    visible = uiState.roundFeedbackMsg != null,
                                    enter = fadeIn() + scaleIn()
                                ) {
                                    uiState.roundFeedbackMsg?.let { msg ->
                                        val isCorrect = uiState.lastIsCorrect == true
                                        Box(
                                            modifier = Modifier
                                                .shadow(10.dp, PillShape)
                                                .clip(PillShape)
                                                .background(if (isCorrect) AksaLime else AksaOrange)
                                                .padding(horizontal = 22.dp, vertical = 12.dp)
                                        ) {
                                            Text(
                                                text = msg,
                                                fontFamily = Poppins,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                color = if (isCorrect) AksaDarkBrown else AksaWhite
                                            )
                                        }
                                    }
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
                        Spacer(modifier = Modifier.height(16.dp))

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
                                MascotCheering(size = 130.dp)

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

                                Spacer(modifier = Modifier.height(16.dp))

                                // Stars Rating
                                val starsCount = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) 3 else uiState.starRating
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(3) { index ->
                                        Text(
                                            text = if (index < starsCount) "⭐" else "☆",
                                            fontSize = 32.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                val finalScoreDisplay = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "14 / 15" else "${uiState.correctAnswersCount} / ${uiState.totalRounds}"
                                val finalAccuracyPct = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "93%" else "${(uiState.accuracy * 100).toInt()}%"
                                val finalScorePoints = if (isDevPreview && visualState == PreviewVisualState.SESSION_COMPLETED) "210 Point" else "${uiState.score} Point"

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                                fontSize = 20.sp,
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
                                                fontSize = 20.sp,
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

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(AksaOrange.copy(alpha = 0.2f))
                                            .padding(14.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = finalScorePoints,
                                                fontFamily = Poppins,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = AksaDarkBrown
                                            )
                                            Text(
                                                text = "Total Skor",
                                                fontFamily = Poppins,
                                                fontSize = 11.sp,
                                                color = AksaDarkBrown.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        AksaButton(
                            text = "Main Lagi",
                            style = AksaButtonStyle.SECONDARY_LIME,
                            onClick = {
                                vm.startGame()
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
