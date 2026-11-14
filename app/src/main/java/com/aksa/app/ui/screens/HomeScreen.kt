package com.aksa.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaProgressBar
import com.aksa.app.ui.components.AksaProfileCard
import com.aksa.app.ui.components.MascotHeadband
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaBlueDark
import com.aksa.app.ui.theme.AksaBlueLight
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaOrangeDark
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToLevelMap: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: com.aksa.app.ui.viewmodel.HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    androidx.compose.runtime.LaunchedEffect(AksaUserState.currentProfileId) {
        viewModel.loadData()
    }

    val profile = uiState.childProfile
    val displayName = profile?.displayName?.takeIf { it.isNotBlank() }
        ?: AksaUserState.username.takeIf { it.isNotBlank() }
        ?: "Pengguna"
    val displayAge = "${profile?.age ?: AksaUserState.age} tahun"
    val avatarId = profile?.avatarId ?: AksaUserState.avatarId
    val isDevPreview by com.aksa.app.ui.preview.DeveloperPreviewManager.isPreviewActive.collectAsState()
    val previewDays by com.aksa.app.ui.preview.DeveloperPreviewManager.previewWeeklyDays.collectAsState()

    val effectiveWeeklyProgress = if (isDevPreview) {
        com.aksa.app.ui.preview.DeveloperPreviewFixtures.createWeeklyProgress(previewDays)
    } else {
        uiState.weeklyProgress
    }
    val currentStreak = effectiveWeeklyProgress?.currentStreak ?: AksaUserState.streakCount

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaOrange)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp, bottom = 100.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AksaProfileCard(
                name = displayName.uppercase(),
                age = displayAge,
                level = AksaUserState.level,
                streakCount = currentStreak,
                avatarId = avatarId
            )

            Spacer(modifier = Modifier.height(16.dp))

            com.aksa.app.ui.components.WeeklyActivityCard(
                weeklyProgress = effectiveWeeklyProgress
            )

            Spacer(modifier = Modifier.height(16.dp))

            val learningCardShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 10.dp, shape = learningCardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, learningCardShape)
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val environmentShape = RoundedCornerShape(24.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(environmentShape)
                            .background(AksaBlue)
                    ) {
                        LandscapeIllustration()

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 6.dp)
                        ) {
                            MascotHeadband(size = 150.dp)
                        }

                        Box(
                            modifier = Modifier
                                .padding(12.dp)
                                .clip(PillShape)
                                .background(AksaWhite.copy(alpha = 0.92f))
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AksaIcons.Star,
                                    contentDescription = null,
                                    tint = AksaYellowDark,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "MODUL 1",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = AksaDarkBrown
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Petualangan Membaca",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = AksaDarkBrown
                    )

                    Text(
                        text = "Latihan pengenalan huruf dan bunyi suara kata",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = AksaGrayText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progres Modul",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = AksaDarkBrown
                        )
                        Text(
                            text = "3 / 8 Selesai",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AksaOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    AksaProgressBar(
                        progress = 3f / 8f,
                        height = 14.dp
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    AksaButton(
                        text = "Mulai Latihan",
                        style = AksaButtonStyle.SECONDARY_YELLOW,
                        trailingIcon = AksaIcons.ArrowForward,
                        height = 60.dp,
                        fontSize = 18.sp,
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Memulai modul latihan petualangan!")
                            }
                            onNavigateToLevelMap()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DailyChallengeCard(streakCount = currentStreak)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 36.dp)
        )
    }
}

@Composable
private fun LandscapeIllustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "CloudDrift")
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CloudAnim"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = AksaYellow,
            radius = 32f,
            center = Offset(w * 0.85f, h * 0.28f)
        )
        drawCircle(
            color = AksaYellow.copy(alpha = 0.35f),
            radius = 42f,
            center = Offset(w * 0.85f, h * 0.28f)
        )

        drawCloud(Offset(w * 0.20f + cloudOffset, h * 0.22f), 28f)
        drawCloud(Offset(w * 0.65f - cloudOffset, h * 0.16f), 22f)

        val hillPath1 = Path().apply {
            moveTo(0f, h * 0.70f)
            quadraticTo(w * 0.35f, h * 0.52f, w * 0.75f, h * 0.72f)
            quadraticTo(w * 0.90f, h * 0.78f, w, h * 0.70f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(hillPath1, color = AksaLimeDark)

        val hillPath2 = Path().apply {
            moveTo(0f, h * 0.80f)
            quadraticTo(w * 0.45f, h * 0.64f, w, h * 0.78f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }   
        drawPath(hillPath2, color = AksaLime)

        drawMiniTree(Offset(w * 0.12f, h * 0.68f), 18f)
        drawMiniTree(Offset(w * 0.88f, h * 0.66f), 22f)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloud(center: Offset, r: Float) {
    drawCircle(AksaWhite.copy(alpha = 0.85f), r, center)
    drawCircle(AksaWhite.copy(alpha = 0.85f), r * 0.75f, center.copy(x = center.x - r * 0.8f, y = center.y + r * 0.2f))
    drawCircle(AksaWhite.copy(alpha = 0.85f), r * 0.75f, center.copy(x = center.x + r * 0.8f, y = center.y + r * 0.2f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniTree(base: Offset, size: Float) {
    drawLine(
        color = AksaDarkBrown,
        start = base,
        end = Offset(base.x, base.y + size * 0.8f),
        strokeWidth = size * 0.25f
    )
    drawCircle(
        color = Color(0xFF6FA800),
        radius = size * 0.6f,
        center = base
    )
}

@Composable
private fun DailyChallengeCard(streakCount: Int = 0) {
    val shape = RoundedCornerShape(24.dp)
    val streakText = if (streakCount > 0) {
        "Selesaikan latihan hari ini untuk menjaga $streakCount hari streak!"
    } else {
        "Selesaikan latihan pertama hari ini untuk memulai streak!"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AksaPeachLight)
            .border(1.5.dp, AksaSoftPeach, shape)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(AksaYellow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AksaIcons.EmojiEvents,
                    contentDescription = null,
                    tint = AksaDarkBrown,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Target Belajar Hari Ini",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AksaDarkBrown
                )
                Text(
                    text = streakText,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = AksaDarkBrown.copy(alpha = 0.7f)
                )
            }
        }
    }
}
