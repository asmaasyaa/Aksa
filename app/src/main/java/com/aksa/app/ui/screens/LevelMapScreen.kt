package com.aksa.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaLevelNode
import com.aksa.app.ui.components.LevelNodeStatus
import com.aksa.app.ui.components.MascotLearning
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
import com.aksa.app.ui.viewmodel.LevelMapViewModel
import com.aksa.app.ui.viewmodel.LockedLevelDetails
import kotlinx.coroutines.launch

@Composable
fun LevelMapScreen(
    viewModel: LevelMapViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLevel1: () -> Unit,
    onNavigateToLevel2: () -> Unit,
    onNavigateToLevel3: () -> Unit,
    onNavigateToLevel4: () -> Unit,
    onNavigateToTangkapHuruf: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !uiState.isLoading) {
                coroutineScope.launch {
                    viewModel.refreshProgression()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val status1 = uiState.effectiveStatus1
    val status2 = uiState.effectiveStatus2
    val status3 = uiState.effectiveStatus3
    val status4 = uiState.effectiveStatus4

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaLime)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Top App Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(4.dp, PillShape)
                        .clip(PillShape)
                        .background(AksaWhite)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = AksaIcons.arrowBack(),
                        contentDescription = "Kembali",
                        tint = AksaDarkBrown,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(AksaWhite)
                        .padding(horizontal = 20.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "Peta Petualangan",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = AksaDarkBrown
                    )
                }

                Spacer(modifier = Modifier.size(46.dp))
            }

            uiState.errorMessage?.let { errorMsg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AksaWhite.copy(alpha = 0.95f))
                        .clickable { viewModel.onProfileChanged() }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "$errorMsg (Ketuk untuk muat ulang)",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = AksaOrange,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Area Peta Proporsional mengikuti sisa tinggi layar (tanpa perlu scroll)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val mapH = maxHeight
                val y4 = mapH * 0.03f
                val y3 = mapH * 0.26f
                val y2 = mapH * 0.50f
                val y1 = mapH * 0.74f

                AdventureMapCanvas(modifier = Modifier.fillMaxSize())

                // Maskot Full Body di pojok kiri bawah
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 18.dp, bottom = 10.dp)
                ) {
                    MascotLearning(size = 76.dp, animated = true)
                }

                // LEVEL 4 — Sungai Cerita (diberi latar lembut saat terkunci agar tulisan tidak nabrak warna gunung)
                // LEVEL 4 — Sungai Cerita
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 60.dp, y = y4)
                ) {
                    AksaLevelNode(
                        levelNumber = 4,
                        title = "Sungai Cerita",
                        status = status4,
                        onClick = {
                            viewModel.onLevelClick(4, onNavigateToLevel4)
                        }
                    )
                }

                // LEVEL 3 — Gunung Kata
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-80).dp, y = y3)
                ) {
                    AksaLevelNode(
                        levelNumber = 3,
                        title = "Gunung Kata",
                        status = status3,
                        onClick = {
                            viewModel.onLevelClick(3, onNavigateToLevel3)
                        }
                    )
                }

                // LEVEL 2 — Dunia Suku Kata
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 80.dp, y = y2)
                ) {
                    AksaLevelNode(
                        levelNumber = 2,
                        title = "Dunia Suku Kata",
                        status = status2,
                        onClick = {
                            viewModel.onLevelClick(2, onNavigateToLevel2)
                        }
                    )
                }

                // LEVEL 1 — Pulau Alfabet
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-36).dp, y = y1)
                ) {
                    AksaLevelNode(
                        levelNumber = 1,
                        title = "Pulau Alfabet",
                        status = status1,
                        onClick = {
                            viewModel.onLevelClick(1, onNavigateToLevel1)
                        }
                    )
                }
            }

            // Kartu Tangkap Huruf tepat di bagian bawah layar (langsung terlihat penuh)
            TangkapHurufActionCard(
                onClick = onNavigateToTangkapHuruf,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }

        // Friendly Explanation Modal for Locked Levels
        uiState.selectedLockedLevel?.let { locked ->
            LockedLevelExplanationDialog(
                locked = locked,
                onPracticePrerequisite = {
                    val prereqLvl = locked.prerequisiteNumber
                    viewModel.dismissLockedDialog()
                    when (prereqLvl) {
                        1 -> viewModel.onLevelClick(1, onNavigateToLevel1)
                        2 -> viewModel.onLevelClick(2, onNavigateToLevel2)
                        3 -> viewModel.onLevelClick(3, onNavigateToLevel3)
                        else -> viewModel.onLevelClick(1, onNavigateToLevel1)
                    }
                },
                onDismiss = {
                    viewModel.dismissLockedDialog()
                }
            )
        }
    }
}

@Composable
private fun TangkapHurufActionCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "TangkapHurufCardScale"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "TangkapHurufWiggle")

    val cardShakeRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2200
                0f at 0 using FastOutSlowInEasing
                -1.4f at 90 using FastOutSlowInEasing
                1.4f at 180 using FastOutSlowInEasing
                -1.0f at 270 using FastOutSlowInEasing
                1.0f at 360 using FastOutSlowInEasing
                0f at 450 using FastOutSlowInEasing
                0f at 2200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "CardShakeRotation"
    )

    val iconBounceY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconBounceY"
    )

    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = if (isPressed) 0f else cardShakeRotation
            }
            .shadow(8.dp, cardShape, spotColor = Color(0x33441100))
            .clip(cardShape)
            .background(AksaWhite)
            .border(2.5.dp, AksaYellow, cardShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AksaPeachLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = AksaIcons.star(),
                    contentDescription = "Ikon Tangkap Huruf",
                    tint = AksaOrange,
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer { translationY = iconBounceY }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Tangkap Huruf",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = AksaDarkBrown
                )
                Text(
                    text = "Main sambil belajar huruf",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = AksaDarkBrown.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(AksaOrange)
                    .padding(horizontal = 15.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = AksaIcons.arrowForward(),
                        contentDescription = "Main",
                        tint = AksaWhite,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Main",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun LockedLevelExplanationDialog(
    locked: LockedLevelDetails,
    onPracticePrerequisite: () -> Unit,
    onDismiss: () -> Unit
) {
    val isSessionsMet = locked.currentSessions >= locked.requiredSessions
    val isAccuracyMet = isSessionsMet && locked.currentAccuracyPercent >= locked.requiredAccuracyPercent

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(AksaWhite)
                .clickable(enabled = false) {}
                .padding(22.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                MascotLearning(size = 84.dp, animated = false)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (locked.isReadError) "Gagal Memuat Progres" else "${locked.title} Terkunci 🔒",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = AksaDarkBrown,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = locked.explanation,
                    fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = AksaDarkBrown.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                if (!locked.isReadError) {
                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(AksaPeachLight.copy(alpha = 0.55f))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Progres & Syarat Membuka Level:",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AksaDarkBrown
                            )

                            RequirementStatusRow(
                                isMet = isSessionsMet,
                                title = "Minimal ${locked.requiredSessions} sesi selesai di ${locked.prerequisiteTitle}",
                                currentStatus = "Saat ini: ${locked.currentSessions} / ${locked.requiredSessions} sesi"
                            )

                            RequirementStatusRow(
                                isMet = isAccuracyMet,
                                title = locked.targetAccuracies,
                                currentStatus = if (locked.currentSessions > 0) {
                                    "Saat ini: ${locked.currentAccuracyPercent}% (target ${locked.requiredAccuracyPercent}%)"
                                } else {
                                    "Saat ini: 0% (belum ada sesi)"
                                }
                            )

                            if (locked.hasLetterErrorRateCheck) {
                                RequirementStatusRow(
                                    isMet = locked.isErrorRateMet,
                                    title = locked.targetErrorLimit,
                                    currentStatus = locked.errorRateSummary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    if (locked.isPrerequisiteUnlocked) {
                        AksaButton(
                            text = "Ayo Latihan di ${locked.prerequisiteTitle}",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = onPracticePrerequisite
                        )
                    } else {
                        // Tombol dibuat abu-abu + petunjuk jika level prasyaratnya juga masih terkunci
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .clip(PillShape)
                                .background(Color(0xFFE0E0E0))
                                .border(1.5.dp, Color(0xFFBDBDBD), PillShape)
                                .clickable(onClick = onPracticePrerequisite)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ayo Latihan di ${locked.prerequisiteTitle} 🔒",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF757575),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "💡 ${locked.prerequisiteLockedHint}",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = Color(0xFF616161),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                AksaButton(
                    text = "Tutup",
                    style = AksaButtonStyle.SECONDARY_LIME,
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun RequirementStatusRow(
    isMet: Boolean,
    title: String,
    currentStatus: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isMet) AksaLimeDark else AksaOrange.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (isMet) {
                Icon(
                    painter = AksaIcons.check(),
                    contentDescription = "Tercapai",
                    tint = AksaWhite,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AksaOrange)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Poppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = AksaDarkBrown
            )
            Text(
                text = currentStatus,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isMet) AksaLimeDark else AksaOrange
            )
        }
    }
}

@Composable
private fun AdventureMapCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawMountain(base = Offset(w * 0.2f, h * 0.21f), size = 95.dp.toPx(), color = Color(0xFF81B828))
        drawMountain(base = Offset(w * 0.45f, h * 0.18f), size = 120.dp.toPx(), color = Color(0xFF6E9F20))
        drawMountain(base = Offset(w * 0.8f, h * 0.22f), size = 105.dp.toPx(), color = Color(0xFF81B828))

        val riverPath = Path().apply {
            moveTo(w * 0.45f, 0f)
            cubicTo(w * 0.65f, h * 0.07f, w * 0.35f, h * 0.14f, w * 0.55f, h * 0.22f)
            cubicTo(w * 0.75f, h * 0.30f, w * 0.95f, h * 0.29f, w, h * 0.33f)
            lineTo(w, h * 0.28f)
            cubicTo(w * 0.92f, h * 0.26f, w * 0.72f, h * 0.26f, w * 0.55f, h * 0.19f)
            cubicTo(w * 0.38f, h * 0.12f, w * 0.62f, h * 0.06f, w * 0.45f, 0f)
            close()
        }
        drawPath(riverPath, color = AksaBlue)

        val lakePath = Path().apply {
            moveTo(w * 0.05f, h * 0.38f)
            cubicTo(w * 0.25f, h * 0.34f, w * 0.35f, h * 0.44f, w * 0.20f, h * 0.52f)
            cubicTo(w * 0.10f, h * 0.56f, w * 0.02f, h * 0.48f, w * 0.05f, h * 0.38f)
            close()
        }
        drawPath(lakePath, color = AksaBlue)
        drawLine(
            color = AksaWhite.copy(alpha = 0.7f),
            start = Offset(w * 0.12f, h * 0.42f),
            end = Offset(w * 0.18f, h * 0.42f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        val path = Path().apply {
            val nodeCenterOffset = 34.dp.toPx()
            val p1 = Offset(w * 0.5f - 36.dp.toPx(), h * 0.74f + nodeCenterOffset)
            val p2 = Offset(w * 0.5f + 80.dp.toPx(), h * 0.50f + nodeCenterOffset)
            val p3 = Offset(w * 0.5f - 80.dp.toPx(), h * 0.26f + nodeCenterOffset)
            val p4 = Offset(w * 0.5f + 60.dp.toPx(), h * 0.03f + nodeCenterOffset)

            moveTo(p1.x, p1.y)
            cubicTo(w * 0.2f, (p1.y + p2.y) / 2f, w * 0.85f, (p1.y + p2.y) / 2f, p2.x, p2.y)
            cubicTo(w * 0.9f, (p2.y + p3.y) / 2f, w * 0.15f, (p2.y + p3.y) / 2f, p3.x, p3.y)
            cubicTo(w * 0.1f, (p3.y + p4.y) / 2f, w * 0.75f, (p3.y + p4.y) / 2f, p4.x, p4.y)
        }

        drawPath(
            path = path,
            color = Color(0x33441100),
            style = Stroke(width = 44f, cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            color = AksaWhite,
            style = Stroke(width = 36f, cap = StrokeCap.Round)
        )
        drawPath(
            path = path,
            color = AksaYellow.copy(alpha = 0.8f),
            style = Stroke(
                width = 8f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )
        )

        drawMapTree(Offset(w * 0.78f, h * 0.72f), 20f)
        drawMapTree(Offset(w * 0.86f, h * 0.76f), 16f)
        drawMapTree(Offset(w * 0.26f, h * 0.74f), 20f)
        drawMapTree(Offset(w * 0.72f, h * 0.48f), 18f)
        drawMapTree(Offset(w * 0.35f, h * 0.26f), 20f)
        drawMapTree(Offset(w * 0.88f, h * 0.24f), 22f)

        drawSmallHouse(Offset(w * 0.82f, h * 0.58f), 26f)
        drawSmallHouse(Offset(w * 0.22f, h * 0.20f), 22f)

        drawBush(Offset(w * 0.65f, h * 0.80f), 13f)
        drawBush(Offset(w * 0.30f, h * 0.62f), 14f)
        drawBush(Offset(w * 0.80f, h * 0.36f), 14f)
    }
}

private fun DrawScope.drawMountain(base: Offset, size: Float, color: Color) {
    val mPath = Path().apply {
        moveTo(base.x - size, base.y + size * 0.8f)
        lineTo(base.x, base.y - size * 0.8f)
        lineTo(base.x + size, base.y + size * 0.8f)
        close()
    }
    drawPath(mPath, color = color)
}

private fun DrawScope.drawMapTree(base: Offset, size: Float) {
    drawLine(
        color = AksaDarkBrown,
        start = base,
        end = Offset(base.x, base.y + size * 0.9f),
        strokeWidth = size * 0.22f
    )
    drawCircle(color = AksaLimeDark, radius = size * 0.7f, center = base)
    drawCircle(color = Color(0xFF6DA118), radius = size * 0.45f, center = Offset(base.x - size * 0.2f, base.y - size * 0.2f))
}

private fun DrawScope.drawSmallHouse(pos: Offset, size: Float) {
    drawRoundRect(
        color = AksaWhite,
        topLeft = pos,
        size = Size(size, size * 0.85f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    val roof = Path().apply {
        moveTo(pos.x - size * 0.2f, pos.y)
        lineTo(pos.x + size * 0.5f, pos.y - size * 0.65f)
        lineTo(pos.x + size * 1.2f, pos.y)
        close()
    }
    drawPath(roof, color = AksaOrange)
    drawRoundRect(
        color = AksaDarkBrown,
        topLeft = Offset(pos.x + size * 0.35f, pos.y + size * 0.35f),
        size = Size(size * 0.3f, size * 0.5f),
        cornerRadius = CornerRadius(2f, 2f)
    )
}

private fun DrawScope.drawBush(pos: Offset, r: Float) {
    drawCircle(color = Color(0xFF75A61E), radius = r, center = pos)
    drawCircle(color = Color(0xFF8BBF29), radius = r * 0.75f, center = Offset(pos.x + r * 0.6f, pos.y))
}