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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaLevelNode
import com.aksa.app.ui.components.LevelNodeStatus
import com.aksa.app.ui.components.MascotPeeking
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aksa.app.ui.preview.DeveloperPreviewManager
import com.aksa.app.ui.preview.LevelMapPreviewState

private data class MapStatuses(
    val s1: LevelNodeStatus,
    val s2: LevelNodeStatus,
    val s3: LevelNodeStatus,
    val s4: LevelNodeStatus
)

@Composable
fun LevelMapScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLevel1: () -> Unit,
    onNavigateToLevel2: () -> Unit,
    onNavigateToLevel3: () -> Unit,
    onNavigateToLevel4: () -> Unit,
    onNavigateToTangkapHuruf: () -> Unit = {},
    onNavigateToDeveloperPreview: () -> Unit = {}
) {
    val levelMapState by DeveloperPreviewManager.levelMapState.collectAsState()
    val isPreviewActive by DeveloperPreviewManager.isPreviewActive.collectAsState()

    val (status1, status2, status3, status4) = when (levelMapState) {
        LevelMapPreviewState.ALL_UNLOCKED ->
            MapStatuses(LevelNodeStatus.ACTIVE, LevelNodeStatus.ACTIVE, LevelNodeStatus.ACTIVE, LevelNodeStatus.ACTIVE)
        LevelMapPreviewState.ALL_COMPLETED ->
            MapStatuses(LevelNodeStatus.COMPLETED, LevelNodeStatus.COMPLETED, LevelNodeStatus.COMPLETED, LevelNodeStatus.COMPLETED)
        LevelMapPreviewState.ALL_LOCKED_EXCEPT_LEVEL_1 ->
            MapStatuses(LevelNodeStatus.ACTIVE, LevelNodeStatus.LOCKED, LevelNodeStatus.LOCKED, LevelNodeStatus.LOCKED)
        LevelMapPreviewState.DEFAULT_PROGRESSION ->
            MapStatuses(LevelNodeStatus.ACTIVE, LevelNodeStatus.ACTIVE, LevelNodeStatus.LOCKED, LevelNodeStatus.LOCKED)
    }

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
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(AksaYellow)
                        .clickable(onClick = onNavigateToTangkapHuruf)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🎮 Tangkap Huruf",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AksaDarkBrown
                        )
                    }
                }

                if (com.aksa.app.BuildConfig.DEBUG) {
                    Box(
                        modifier = Modifier
                            .shadow(4.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                            .background(AksaWhite)
                            .clickable(onClick = onNavigateToDeveloperPreview)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "🛠️ Dev Preview",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AksaDarkBrown
                        )
                    }
                }
            }

            // Map Area containing winding path, nodes, and decorations
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(820.dp)
            ) {
                // Background Vector Illustrations (hills, lake, path, trees, houses, mountains)
                AdventureMapCanvas()

                // LEVEL 4 (Top: Sungai Cerita)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 60.dp, y = 40.dp)
                ) {
                    AksaLevelNode(
                        levelNumber = 4,
                        title = "Sungai Cerita",
                        status = status4,
                        onClick = onNavigateToLevel4
                    )
                }

                // LEVEL 3 (Upper-Middle: Gunung Kata)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-80).dp, y = 230.dp)
                ) {
                    AksaLevelNode(
                        levelNumber = 3,
                        title = "Gunung Kata",
                        status = status3,
                        onClick = onNavigateToLevel3
                    )
                }

                // LEVEL 2 (Lower-Middle: Dunia Suku Kata)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = 80.dp, y = 430.dp)
                ) {
                    AksaLevelNode(
                        levelNumber = 2,
                        title = "Dunia Suku Kata",
                        status = status2,
                        onClick = onNavigateToLevel2
                    )
                }

                // LEVEL 1 (Bottom: Pulau Alfabet)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(x = (-40).dp, y = 630.dp)
                ) {
                    AksaLevelNode(
                        levelNumber = 1,
                        title = "Pulau Alfabet",
                        status = status1,
                        onClick = onNavigateToLevel1
                    )
                }
            }
        }

        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // White rounded "Kembali" pill button
            Box(
                modifier = Modifier
                    .shadow(4.dp, PillShape)
                    .clip(PillShape)
                    .background(AksaWhite)
                    .clickable(onClick = onNavigateBack)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
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

            // Title Pill
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(AksaWhite.copy(alpha = 0.9f))
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Peta Petualangan",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AksaDarkBrown
                )
            }
        }

        // Decorative Mascot positioned safely at lower-right corner (never overlaps title, back button, or level nodes)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 5.dp)
        ) {
            MascotPeeking(
                width = 130.dp,
                height = 85.dp
            )
        }
    }
}

/**
 * Adventure Map Canvas drawing:
 * - Large winding white path connecting all 4 levels
 * - Playful mountains, lake, trees, small houses, bushes, clouds
 */
@Composable
private fun AdventureMapCanvas() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Mountains in background (top)
        drawMountain(Offset(w * 0.15f, h * 0.10f), 80f, Color(0xFF8BBF29))
        drawMountain(Offset(w * 0.32f, h * 0.08f), 110f, Color(0xFF75A61E))
        drawMountain(Offset(w * 0.82f, h * 0.12f), 90f, Color(0xFF75A61E))

        // 2. Playful Blue Lake (Middle-Left)
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
        // Path starts at Level 1 (bottom left), winds to Level 2 (middle right),
        // then Level 3 (middle left), and finishes at Level 4 (top right).
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
        // Trees
        drawMapTree(Offset(w * 0.78f, h * 0.72f), 22f)
        drawMapTree(Offset(w * 0.86f, h * 0.76f), 18f)
        drawMapTree(Offset(w * 0.18f, h * 0.78f), 24f)
        drawMapTree(Offset(w * 0.72f, h * 0.48f), 20f)
        drawMapTree(Offset(w * 0.35f, h * 0.26f), 22f)
        drawMapTree(Offset(w * 0.88f, h * 0.24f), 26f)

        // Small Red/Orange Houses
        drawSmallHouse(Offset(w * 0.82f, h * 0.58f), 28f)
        drawSmallHouse(Offset(w * 0.22f, h * 0.20f), 24f)

        // Small Bushes
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
    // Trunk
    drawLine(
        color = AksaDarkBrown,
        start = base,
        end = Offset(base.x, base.y + size * 0.9f),
        strokeWidth = size * 0.22f
    )
    // Foliage
    drawCircle(color = AksaLimeDark, radius = size * 0.7f, center = base)
    drawCircle(color = Color(0xFF6DA118), radius = size * 0.45f, center = Offset(base.x - size * 0.2f, base.y - size * 0.2f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmallHouse(pos: Offset, size: Float) {
    // Body (White card)
    drawRoundRect(
        color = AksaWhite,
        topLeft = pos,
        size = Size(size, size * 0.85f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Roof (Orange)
    val roof = Path().apply {
        moveTo(pos.x - size * 0.2f, pos.y)
        lineTo(pos.x + size * 0.5f, pos.y - size * 0.65f)
        lineTo(pos.x + size * 1.2f, pos.y)
        close()
    }
    drawPath(roof, color = AksaOrange)
    // Door
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
