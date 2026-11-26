package com.aksa.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.EarnedStarsRow
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import kotlin.math.hypot

@Composable
fun WritingPracticeScreen(
    letter: String = "A",
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit,
    onNavigateToHurufMirip: () -> Unit = {}
) {
    val targetChar = letter.trim().uppercase().firstOrNull() ?: 'A'
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var isDrawing by remember { mutableStateOf(false) }
    var isSubmitted by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState(), enabled = !isDrawing)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 30.dp),
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
                        .clickable(onClick = onNavigateBack)
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
                        text = "2 dari 2",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Gambar Huruf $targetChar",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = AksaDarkBrown
            )

            Text(
                text = "Ikuti garis putus-putus menggunakan jarimu",
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = AksaDarkBrown.copy(alpha = 0.75f),
                modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
            )

            val canvasShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .shadow(10.dp, canvasShape, spotColor = Color(0x33003366))
                    .clip(canvasShape)
                    .background(AksaWhite)
                    .border(
                        width = 2.dp,
                        color = if (validationMessage != null) Color(0xFFE53935) else AksaSoftPeach,
                        shape = canvasShape
                    )
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(isSubmitted) {
                            if (!isSubmitted) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        isDrawing = true
                                        validationMessage = null
                                        currentStroke = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentStroke = currentStroke + change.position
                                    },
                                    onDragEnd = {
                                        isDrawing = false
                                        if (currentStroke.isNotEmpty()) {
                                            strokes.add(currentStroke)
                                            currentStroke = emptyList()
                                        }
                                    },
                                    onDragCancel = {
                                        isDrawing = false
                                        if (currentStroke.isNotEmpty()) {
                                            strokes.add(currentStroke)
                                            currentStroke = emptyList()
                                        }
                                    }
                                )
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Dynamic Dashed Letter Guide
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                    val letterPath = buildLetterGuidePath(targetChar, w, h)

                    drawPath(
                        path = letterPath,
                        color = Color(0xFFD6E3EA),
                        style = Stroke(
                            width = 24f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round,
                            pathEffect = dashedEffect
                        )
                    )

                    // 2. User Drawn Completed Strokes
                    strokes.forEach { strokePoints ->
                        if (strokePoints.size > 1) {
                            val strokePath = Path().apply {
                                moveTo(strokePoints.first().x, strokePoints.first().y)
                                for (i in 1 until strokePoints.size) {
                                    lineTo(strokePoints[i].x, strokePoints[i].y)
                                }
                            }
                            drawPath(
                                path = strokePath,
                                color = AksaOrange,
                                style = Stroke(
                                    width = 20f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        } else if (strokePoints.size == 1) {
                            drawCircle(
                                color = AksaOrange,
                                radius = 10f,
                                center = strokePoints.first()
                            )
                        }
                    }

                    // 3. Current active stroke
                    if (currentStroke.size > 1) {
                        val activePath = Path().apply {
                            moveTo(currentStroke.first().x, currentStroke.first().y)
                            for (i in 1 until currentStroke.size) {
                                lineTo(currentStroke[i].x, currentStroke[i].y)
                            }
                        }
                        drawPath(
                            path = activePath,
                            color = AksaOrange,
                            style = Stroke(
                                width = 20f,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                if (strokes.isEmpty() && currentStroke.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                            .clip(PillShape)
                            .background(AksaPeachLight)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "✍️ Sentuh & usap untuk menulis",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = AksaDarkBrown.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (validationMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFEBEE))
                        .border(1.dp, Color(0xFFE53935), RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = validationMessage!!,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color(0xFFC62828)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AksaButton(
                        text = "Ulangi",
                        style = AksaButtonStyle.WHITE_CARD,
                        iconPainter = AksaIcons.refresh(),
                        onClick = {
                            strokes.clear()
                            currentStroke = emptyList()
                            isDrawing = false
                            validationMessage = null
                        }
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    AksaButton(
                        text = "Submit",
                        style = AksaButtonStyle.PRIMARY_ORANGE,
                        iconPainter = AksaIcons.check(),
                        onClick = {
                            val allStrokes = if (currentStroke.isNotEmpty()) {
                                strokes.toList() + listOf(currentStroke)
                            } else {
                                strokes.toList()
                            }
                            val result = validateLetterStrokes(
                                targetLetter = targetChar,
                                strokes = allStrokes,
                                canvasWidth = canvasSize.width.toFloat(),
                                canvasHeight = canvasSize.height.toFloat()
                            )
                            if (result.isValid) {
                                validationMessage = null
                                isSubmitted = true
                            } else {
                                validationMessage = result.feedback
                            }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isSubmitted,
            enter = fadeIn() + scaleIn(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x77000000))
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
                        .padding(28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MascotCheering(size = 130.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        EarnedStarsRow(accuracy = 1.0f)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Bagus sekali!",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = AksaDarkBrown
                        )

                        Text(
                            text = "Kamu berhasil menulis huruf $targetChar.",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = AksaDarkBrown.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                        )

                        AksaButton(
                            text = "Lanjut ke Huruf Mirip",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = onNavigateToHurufMirip
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        AksaButton(
                            text = "Selesai ke Peta",
                            style = AksaButtonStyle.SECONDARY_LIME,
                            onClick = onNavigateToLevelMap
                        )
                    }
                }
            }
        }
    }
}

private data class StrokeValidationResult(
    val isValid: Boolean,
    val feedback: String
)

private fun getNormalizedLetterSegments(letter: Char): List<Pair<Offset, Offset>> {
    val left = 0.28f
    val right = 0.72f
    val midX = 0.50f
    val top = 0.20f
    val midY = 0.51f
    val bottom = 0.82f

    return when (letter.uppercaseChar()) {
        'A' -> listOf(
            Offset(0.24f, bottom) to Offset(midX, top),
            Offset(midX, top) to Offset(0.76f, bottom),
            Offset(0.35f, 0.58f) to Offset(0.65f, 0.58f)
        )
        'B' -> listOf(
            Offset(left, top) to Offset(left, bottom),
            Offset(left, top) to Offset(0.66f, top),
            Offset(0.66f, top) to Offset(0.66f, midY),
            Offset(0.66f, midY) to Offset(left, midY),
            Offset(left, midY) to Offset(right, midY),
            Offset(right, midY) to Offset(right, bottom),
            Offset(right, bottom) to Offset(left, bottom)
        )
        'C' -> listOf(
            Offset(right, top) to Offset(left, top),
            Offset(left, top) to Offset(left, bottom),
            Offset(left, bottom) to Offset(right, bottom)
        )
        'D' -> listOf(
            Offset(left, top) to Offset(left, bottom),
            Offset(left, top) to Offset(0.62f, top),
            Offset(0.62f, top) to Offset(right, 0.34f),
            Offset(right, 0.34f) to Offset(right, 0.68f),
            Offset(right, 0.68f) to Offset(0.62f, bottom),
            Offset(0.62f, bottom) to Offset(left, bottom)
        )
        'E' -> listOf(
            Offset(left, top) to Offset(left, bottom),
            Offset(left, top) to Offset(right, top),
            Offset(left, midY) to Offset(0.66f, midY),
            Offset(left, bottom) to Offset(right, bottom)
        )
        'F' -> listOf(
            Offset(left, top) to Offset(left, bottom),
            Offset(left, top) to Offset(right, top),
            Offset(left, midY) to Offset(0.66f, midY)
        )
        'G' -> listOf(
            Offset(right, top) to Offset(left, top),
            Offset(left, top) to Offset(left, bottom),
            Offset(left, bottom) to Offset(right, bottom),
            Offset(right, bottom) to Offset(right, midY),
            Offset(right, midY) to Offset(midX, midY)
        )
        'H' -> listOf(
            Offset(left, top) to Offset(left, bottom),
            Offset(right, top) to Offset(right, bottom),
            Offset(left, midY) to Offset(right, midY)
        )
        'I' -> listOf(
            Offset(0.36f, top) to Offset(0.64f, top),
            Offset(midX, top) to Offset(midX, bottom),
            Offset(0.36f, bottom) to Offset(0.64f, bottom)
        )
        'J' -> listOf(
            Offset(0.34f, top) to Offset(right, top),
            Offset(0.62f, top) to Offset(0.62f, bottom),
            Offset(0.62f, bottom) to Offset(0.32f, bottom),
            Offset(0.32f, bottom) to Offset(0.32f, 0.66f)
        )
        'K' -> listOf(
            Offset(left, top) to Offset(left, bottom),
            Offset(right, top) to Offset(left, midY),
            Offset(left, midY) to Offset(right, bottom)
        )
        'L' -> listOf(
            Offset(left, top) to Offset(left, bottom),
            Offset(left, bottom) to Offset(right, bottom)
        )
        'M' -> listOf(
            Offset(0.22f, bottom) to Offset(0.22f, top),
            Offset(0.22f, top) to Offset(midX, 0.58f),
            Offset(midX, 0.58f) to Offset(0.78f, top),
            Offset(0.78f, top) to Offset(0.78f, bottom)
        )
        'N' -> listOf(
            Offset(left, bottom) to Offset(left, top),
            Offset(left, top) to Offset(right, bottom),
            Offset(right, bottom) to Offset(right, top)
        )
        'O' -> listOf(
            Offset(left, top) to Offset(right, top),
            Offset(right, top) to Offset(right, bottom),
            Offset(right, bottom) to Offset(left, bottom),
            Offset(left, bottom) to Offset(left, top)
        )
        'P' -> listOf(
            Offset(left, bottom) to Offset(left, top),
            Offset(left, top) to Offset(right, top),
            Offset(right, top) to Offset(right, midY),
            Offset(right, midY) to Offset(left, midY)
        )
        'Q' -> listOf(
            Offset(left, top) to Offset(right, top),
            Offset(right, top) to Offset(right, bottom),
            Offset(right, bottom) to Offset(left, bottom),
            Offset(left, bottom) to Offset(left, top),
            Offset(0.56f, 0.66f) to Offset(0.78f, 0.86f)
        )
        'R' -> listOf(
            Offset(left, bottom) to Offset(left, top),
            Offset(left, top) to Offset(right, top),
            Offset(right, top) to Offset(right, midY),
            Offset(right, midY) to Offset(left, midY),
            Offset(0.48f, midY) to Offset(right, bottom)
        )
        'S' -> listOf(
            Offset(right, top) to Offset(left, top),
            Offset(left, top) to Offset(left, midY),
            Offset(left, midY) to Offset(right, midY),
            Offset(right, midY) to Offset(right, bottom),
            Offset(right, bottom) to Offset(left, bottom)
        )
        'T' -> listOf(
            Offset(0.24f, top) to Offset(0.76f, top),
            Offset(midX, top) to Offset(midX, bottom)
        )
        'U' -> listOf(
            Offset(left, top) to Offset(left, bottom),
            Offset(left, bottom) to Offset(right, bottom),
            Offset(right, bottom) to Offset(right, top)
        )
        'V' -> listOf(
            Offset(0.24f, top) to Offset(midX, bottom),
            Offset(midX, bottom) to Offset(0.76f, top)
        )
        'W' -> listOf(
            Offset(0.20f, top) to Offset(0.35f, bottom),
            Offset(0.35f, bottom) to Offset(midX, 0.48f),
            Offset(midX, 0.48f) to Offset(0.65f, bottom),
            Offset(0.65f, bottom) to Offset(0.80f, top)
        )
        'X' -> listOf(
            Offset(left, top) to Offset(right, bottom),
            Offset(right, top) to Offset(left, bottom)
        )
        'Y' -> listOf(
            Offset(0.24f, top) to Offset(midX, midY),
            Offset(0.76f, top) to Offset(midX, midY),
            Offset(midX, midY) to Offset(midX, bottom)
        )
        'Z' -> listOf(
            Offset(left, top) to Offset(right, top),
            Offset(right, top) to Offset(left, bottom),
            Offset(left, bottom) to Offset(right, bottom)
        )
        else -> listOf(
            Offset(0.24f, bottom) to Offset(midX, top),
            Offset(midX, top) to Offset(0.76f, bottom),
            Offset(0.35f, 0.58f) to Offset(0.65f, 0.58f)
        )
    }
}

private fun buildLetterGuidePath(letter: Char, w: Float, h: Float): Path {
    val segments = getNormalizedLetterSegments(letter)
    return Path().apply {
        segments.forEach { (start, end) ->
            moveTo(start.x * w, start.y * h)
            lineTo(end.x * w, end.y * h)
        }
    }
}

private fun validateLetterStrokes(
    targetLetter: Char,
    strokes: List<List<Offset>>,
    canvasWidth: Float,
    canvasHeight: Float
): StrokeValidationResult {
    val userPoints = strokes.flatten()
    if (userPoints.size < 6) {
        return StrokeValidationResult(
            isValid = false,
            feedback = "Yuk tulis huruf $targetLetter mengikuti garis putus-putus dulu!"
        )
    }

    if (canvasWidth <= 0f || canvasHeight <= 0f) {
        return StrokeValidationResult(isValid = true, feedback = "")
    }

    val segments = getNormalizedLetterSegments(targetLetter)
    val guideSamples = mutableListOf<Offset>()
    segments.forEach { (normStart, normEnd) ->
        val p1 = Offset(normStart.x * canvasWidth, normStart.y * canvasHeight)
        val p2 = Offset(normEnd.x * canvasWidth, normEnd.y * canvasHeight)
        val steps = 12
        for (i in 0..steps) {
            val t = i.toFloat() / steps
            guideSamples.add(
                Offset(
                    x = p1.x + (p2.x - p1.x) * t,
                    y = p1.y + (p2.y - p1.y) * t
                )
            )
        }
    }

    val tolerancePx = minOf(canvasWidth, canvasHeight) * 0.22f

    val coveredGuideCount = guideSamples.count { guidePt ->
        userPoints.any { userPt -> hypot(userPt.x - guidePt.x, userPt.y - guidePt.y) <= tolerancePx }
    }
    val coverageRatio = coveredGuideCount.toFloat() / guideSamples.size

    val onTrackUserCount = userPoints.count { userPt ->
        guideSamples.any { guidePt -> hypot(userPt.x - guidePt.x, userPt.y - guidePt.y) <= tolerancePx * 1.35f }
    }
    val precisionRatio = onTrackUserCount.toFloat() / userPoints.size

    return when {
        coverageRatio < 0.45f -> StrokeValidationResult(
            isValid = false,
            feedback = "Huruf $targetLetter belum lengkap. Ikuti semua garis putus-putusnya ya!"
        )
        precisionRatio < 0.45f -> StrokeValidationResult(
            isValid = false,
            feedback = "Goresan masih keluar jalur. Coba tekan Ulangi dan ikuti garis putus-putus."
        )
        else -> StrokeValidationResult(isValid = true, feedback = "")
    }
}