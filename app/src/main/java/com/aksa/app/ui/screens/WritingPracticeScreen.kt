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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayBorder
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

@Composable
fun WritingPracticeScreen(
    letter: String = "A",
    onNavigateBack: () -> Unit,
    onNavigateToLevelMap: () -> Unit
) {
    // List of stroke paths (each stroke is a list of Offset points)
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var isSubmitted by remember { mutableStateOf(false) }

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
                .padding(bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar (Kembali + Progress)
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

                // Progress Badge
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

            // Instruction Text
            Text(
                text = "Gambar Huruf Di Kotak",
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

            // Large White Rounded Drawing Canvas
            val canvasShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .shadow(10.dp, canvasShape, spotColor = Color(0x33003366))
                    .clip(canvasShape)
                    .background(AksaWhite)
                    .border(2.dp, AksaSoftPeach, canvasShape)
            ) {
                // Interactive Drawing Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(isSubmitted) {
                            if (!isSubmitted) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentStroke = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentStroke = currentStroke + change.position
                                    },
                                    onDragEnd = {
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

                    // 1. Faint Dashed Letter 'A' outline in background
                    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                    val letterPath = Path().apply {
                        val top = Offset(w * 0.5f, h * 0.20f)
                        val bLeft = Offset(w * 0.24f, h * 0.82f)
                        val bRight = Offset(w * 0.76f, h * 0.82f)
                        val crossLeft = Offset(w * 0.35f, h * 0.58f)
                        val crossRight = Offset(w * 0.65f, h * 0.58f)

                        moveTo(bLeft.x, bLeft.y)
                        lineTo(top.x, top.y)
                        lineTo(bRight.x, bRight.y)

                        moveTo(crossLeft.x, crossLeft.y)
                        lineTo(crossRight.x, crossRight.y)
                    }

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

                // Helper touch hint if no drawing yet
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

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons: Ulangi & Submit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // "Ulangi" (Reset Canvas) Button
                Box(modifier = Modifier.weight(1f)) {
                    AksaButton(
                        text = "Ulangi",
                        style = AksaButtonStyle.WHITE_CARD,
                        icon = AksaIcons.Refresh,
                        onClick = {
                            strokes.clear()
                            currentStroke = emptyList()
                        }
                    )
                }

                // "Submit" Button
                Box(modifier = Modifier.weight(1f)) {
                    AksaButton(
                        text = "Submit",
                        style = AksaButtonStyle.PRIMARY_ORANGE,
                        icon = AksaIcons.Check,
                        onClick = {
                            isSubmitted = true
                        }
                    )
                }
            }
        }

        // Success Dialog Overlay after Submit
        AnimatedVisibility(
            visible = isSubmitted,
            enter = fadeIn() + scaleIn(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x77000000))
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
                        .padding(28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Cheering Mascot
                        MascotCheering(size = 140.dp)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Bagus sekali!",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = AksaDarkBrown
                        )

                        Text(
                            text = "Kamu berhasil menulis huruf $letter.",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = AksaDarkBrown.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                        )

                        // Primary Button: Selesai
                        AksaButton(
                            text = "Selesai",
                            style = AksaButtonStyle.SECONDARY_LIME,
                            onClick = onNavigateToLevelMap
                        )
                    }
                }
            }
        }
    }
}
