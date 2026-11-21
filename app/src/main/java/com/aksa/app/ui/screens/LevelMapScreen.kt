package com.aksa.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaLevelNode
import com.aksa.app.ui.components.LevelNodeStatus
import com.aksa.app.ui.components.MascotLearning
import com.aksa.app.ui.components.MascotPeeking
import com.aksa.app.ui.state.AksaUserState
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

@Composable
fun LevelMapScreen(
    viewModel: LevelMapViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToLevel1: () -> Unit,
    onNavigateToLevel2: () -> Unit,
    onNavigateToLevel3: () -> Unit,
    onNavigateToLevel4: () -> Unit,
    onNavigateToTangkapHuruf: () -> Unit = {},
    onNavigateToDeveloperPreview: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    // Sync LevelMap observation whenever the active child profile changes
    LaunchedEffect(AksaUserState.currentProfileId) {
        viewModel.onProfileChanged()
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
        // Scrollable Adventure Map Landscape
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp)
        ) {
            // Top Bar Area
            Spacer(modifier = Modifier.height(110.dp))

            // Mini-Game & Dev Quick Access Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Mini-Game Tangkap Huruf Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AksaWhite)
                        .clickable { onNavigateToTangkapHuruf() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Tangkap Huruf",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AksaDarkBrown
                        )
                    }
                }

                // Dev Preview Quick Access Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(AksaOrange.copy(alpha = 0.15f))
                        .clickable { onNavigateToDeveloperPreview() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Dev Preview",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaOrange
                    )
                }
            }

            if (uiState.isPreviewActive) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AksaYellow.copy(alpha = 0.25f))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mode Pratinjau Pengembang Aktif: Status level diatur secara manual untuk pengujian UI.",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = AksaDarkBrown,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // The Map Canvas containing the path and nodes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(860.dp)
            ) {
                // Background Vector Illustration (Winding Road, Mountains, Trees, River)
                AdventureMapCanvas(modifier = Modifier.fillMaxSize())

                // Mascot Peeking near Level 1
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = 24.dp, y = (-70).dp)
                ) {
                    MascotPeeking(width = 90.dp, height = 60.dp)
                }

                // LEVEL 4 — Sungai Cerita (Top Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 60.dp, y = 50.dp)
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

                // LEVEL 3 — Gunung Kata (Middle Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-80).dp, y = 240.dp)
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

                // LEVEL 2 — Dunia Suku Kata (Middle Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 80.dp, y = 440.dp)
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

                // LEVEL 1 — Pulau Alfabet (Bottom Center-Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-40).dp, y = 640.dp)
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
        }

        // Fixed Top Floating App Bar with Back and Title
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, PillShape)
                        .clip(PillShape)
                        .background(AksaWhite)
                        .clickable { onNavigateBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = AksaIcons.ArrowBack,
                        contentDescription = "Kembali",
                        tint = AksaDarkBrown,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Header Badge Pill
                Box(
                    modifier = Modifier
                        .shadow(4.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(AksaWhite)
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Peta Petualangan",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AksaDarkBrown
                    )
                }

                // Balance spacer for centered pill
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        // Friendly Explanation Modal for Locked Levels
        uiState.selectedLockedLevel?.let { locked ->
            LockedLevelExplanationDialog(
                locked = locked,
                onPracticePrerequisite = {
                    viewModel.dismissLockedDialog()
                    when (locked.recommendedLevelNumber) {
                        1 -> onNavigateToLevel1()
                        2 -> onNavigateToLevel2()
                        3 -> onNavigateToLevel3()
                        else -> onNavigateToLevel1()
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
private fun LockedLevelExplanationDialog(
    locked: LockedLevelDetails,
    onPracticePrerequisite: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(AksaWhite)
                .clickable(enabled = false) {}
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                MascotLearning(size = 90.dp, animated = false)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${locked.title} Terkunci \uD83D\uDD12",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AksaDarkBrown,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = locked.explanation,
                    fontFamily = Poppins,
                    fontSize = 13.sp,
                    color = AksaDarkBrown.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Requirements checklist card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(AksaPeachLight.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Syarat Membuka Level:",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AksaDarkBrown
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AksaIcons.Check,
                                contentDescription = null,
                                tint = AksaLimeDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Minimal 3 sesi selesai di ${locked.prerequisiteTitle}",
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = AksaDarkBrown
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AksaIcons.Check,
                                contentDescription = null,
                                tint = AksaLimeDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = locked.targetAccuracies,
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = AksaDarkBrown
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AksaIcons.Check,
                                contentDescription = null,
                                tint = AksaLimeDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = locked.targetErrorLimit,
                                fontFamily = Poppins,
                                fontSize = 11.sp,
                                color = AksaDarkBrown
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AksaButton(
                    text = "Ayo Latihan di ${locked.prerequisiteTitle}",
                    style = AksaButtonStyle.PRIMARY_ORANGE,
                    onClick = onPracticePrerequisite
                )

                Spacer(modifier = Modifier.height(10.dp))

                AksaButton(
                    text = "Tutup",
                    style = AksaButtonStyle.SECONDARY_LIME,
                    onClick = onDismiss
                )
            }
        }
    }
}

/**
 * Draws the picturesque, storybook-style 2D vector environment.
 */
@Composable
private fun AdventureMapCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Distant Mountain Ranges (Top / Level 3 & 4 backdrop)
        drawMountain(base = Offset(w * 0.2f, 180.dp.toPx()), size = 110.dp.toPx(), color = Color(0xFF81B828))
        drawMountain(base = Offset(w * 0.45f, 150.dp.toPx()), size = 140.dp.toPx(), color = Color(0xFF6E9F20))
        drawMountain(base = Offset(w * 0.8f, 190.dp.toPx()), size = 120.dp.toPx(), color = Color(0xFF81B828))

        // 2. Winding River (Sungai Cerita) near the top (Level 4 - Blue Path)
        val riverPath = Path().apply {
            moveTo(w * 0.45f, 0f)
            cubicTo(w * 0.65f, 60.dp.toPx(), w * 0.35f, 120.dp.toPx(), w * 0.55f, 190.dp.toPx())
            cubicTo(w * 0.75f, 260.dp.toPx(), w * 0.95f, 250.dp.toPx(), w, 280.dp.toPx())
            lineTo(w, 240.dp.toPx())
            cubicTo(w * 0.92f, 220.dp.toPx(), w * 0.72f, 220.dp.toPx(), w * 0.55f, 160.dp.toPx())
            cubicTo(w * 0.38f, 100.dp.toPx(), w * 0.62f, 50.dp.toPx(), w * 0.45f, 0f)
            close()
        }
        drawPath(riverPath, color = AksaBlue)

        // Small Lake / Pond near Level 3 (Middle Left)
        val lakePath = Path().apply {
            moveTo(w * 0.05f, h * 0.38f)
            cubicTo(w * 0.25f, h * 0.34f, w * 0.35f, h * 0.44f, w * 0.20f, h * 0.52f)
            cubicTo(w * 0.10f, h * 0.56f, w * 0.02f, h * 0.48f, w * 0.05f, h * 0.38f)
            close()
        }
        drawPath(lakePath, color = AksaBlue)
        // Little water reflection ripples
        drawLine(
            color = AksaWhite.copy(alpha = 0.7f),
            start = Offset(w * 0.12f, h * 0.42f),
            end = Offset(w * 0.18f, h * 0.42f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        // 3. Winding WHITE Adventure Path
        val path = Path().apply {
            val p1 = Offset(w * 0.5f - 40.dp.toPx(), 670.dp.toPx())
            val p2 = Offset(w * 0.5f + 80.dp.toPx(), 470.dp.toPx())
            val p3 = Offset(w * 0.5f - 80.dp.toPx(), 270.dp.toPx())
            val p4 = Offset(w * 0.5f + 60.dp.toPx(), 80.dp.toPx())

            moveTo(p1.x, p1.y)
            cubicTo(w * 0.2f, 570.dp.toPx(), w * 0.85f, 570.dp.toPx(), p2.x, p2.y)
            cubicTo(w * 0.9f, 370.dp.toPx(), w * 0.15f, 370.dp.toPx(), p3.x, p3.y)
            cubicTo(w * 0.1f, 170.dp.toPx(), w * 0.75f, 170.dp.toPx(), p4.x, p4.y)
        }

        // Path border / tactile shadow
        drawPath(
            path = path,
            color = Color(0x33441100),
            style = Stroke(width = 46f, cap = StrokeCap.Round)
        )
        // Path base (bright pure white)
        drawPath(
            path = path,
            color = AksaWhite,
            style = Stroke(width = 38f, cap = StrokeCap.Round)
        )
        // Dotted center line (soft peach / orange dashes)
        drawPath(
            path = path,
            color = AksaYellow.copy(alpha = 0.8f),
            style = Stroke(
                width = 8f,
                cap = StrokeCap.Round,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
            )
        )

        // 4. Flat Vector Decorations along the path
        drawMapTree(Offset(w * 0.78f, h * 0.72f), 22f)
        drawMapTree(Offset(w * 0.86f, h * 0.76f), 18f)
        drawMapTree(Offset(w * 0.18f, h * 0.78f), 24f)
        drawMapTree(Offset(w * 0.72f, h * 0.48f), 20f)
        drawMapTree(Offset(w * 0.35f, h * 0.26f), 22f)
        drawMapTree(Offset(w * 0.88f, h * 0.24f), 26f)

        drawSmallHouse(Offset(w * 0.82f, h * 0.58f), 28f)
        drawSmallHouse(Offset(w * 0.22f, h * 0.20f), 24f)

        drawBush(Offset(w * 0.65f, h * 0.80f), 14f)
        drawBush(Offset(w * 0.30f, h * 0.62f), 16f)
        drawBush(Offset(w * 0.80f, h * 0.36f), 15f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMountain(base: Offset, size: Float, color: Color) {
    val mPath = Path().apply {
        moveTo(base.x - size, base.y + size * 0.8f)
        lineTo(base.x, base.y - size * 0.8f)
        lineTo(base.x + size, base.y + size * 0.8f)
        close()
    }
    drawPath(mPath, color = color)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMapTree(base: Offset, size: Float) {
    drawLine(
        color = AksaDarkBrown,
        start = base,
        end = Offset(base.x, base.y + size * 0.9f),
        strokeWidth = size * 0.22f
    )
    drawCircle(color = AksaLimeDark, radius = size * 0.7f, center = base)
    drawCircle(color = Color(0xFF6DA118), radius = size * 0.45f, center = Offset(base.x - size * 0.2f, base.y - size * 0.2f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmallHouse(pos: Offset, size: Float) {
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBush(pos: Offset, r: Float) {
    drawCircle(color = Color(0xFF75A61E), radius = r, center = pos)
    drawCircle(color = Color(0xFF8BBF29), radius = r * 0.75f, center = Offset(pos.x + r * 0.6f, pos.y))
}
