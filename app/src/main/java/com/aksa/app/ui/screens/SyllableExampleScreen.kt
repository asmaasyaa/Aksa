package com.aksa.app.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaSyllableCard
import com.aksa.app.ui.components.SyllableCardColor
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.aksa.app.audio.AndroidTextToSpeechHelper

@Composable
fun SyllableExampleScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuiz: () -> Unit
) {
    val context = LocalContext.current
    val tts = remember { AndroidTextToSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaOrange)
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

                // Badge: Contoh
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(AksaWhite.copy(alpha = 0.2f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CONTOH",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = AksaYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Large White Rounded Illustration Card
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
                    // Illustration of Eyes (Mata)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(AksaPeachLight),
                        contentAlignment = Alignment.Center
                    ) {
                        EyesIllustration()
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Susun suku kata berikut:",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = AksaDarkBrown.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Green Syllable Tiles: MA and TA
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AksaSyllableCard(
                            syllable = "MA",
                            colorTheme = SyllableCardColor.GREEN,
                            onClick = {}
                        )

                        Text(
                            text = "+",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = AksaDarkBrown
                        )

                        AksaSyllableCard(
                            syllable = "TA",
                            colorTheme = SyllableCardColor.GREEN,
                            onClick = {}
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Combined Word Result: MATA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(AksaSoftPeach)
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "MATA",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp,
                                    letterSpacing = 3.sp,
                                    color = AksaOrange
                                )
                                Text(
                                    text = "MA + TA = MATA",
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = AksaDarkBrown.copy(alpha = 0.8f)
                                )
                            }

                            // Speaker button
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(AksaWhite)
                                    .clickable {
                                        tts.speak("Suku kata M A, ma. Suku kata T A, ta. MA ditambah TA menjadi MATA.")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AksaIcons.VolumeUp,
                                    contentDescription = "Bunyikan",
                                    tint = AksaOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Button: Coba Sekarang
                    AksaButton(
                        text = "Coba Sekarang",
                        style = AksaButtonStyle.PRIMARY_ORANGE,
                        onClick = onNavigateToQuiz
                    )
                }
            }
        }
    }
}

/**
 * Clean flat vector illustration of cute expressive eyes
 */
@Composable
fun EyesIllustration() {
    Canvas(modifier = Modifier.size(160.dp, 100.dp)) {
        val w = size.width
        val h = size.height

        val eyeRadius = 32f
        val leftEyeCenter = Offset(w * 0.32f, h * 0.5f)
        val rightEyeCenter = Offset(w * 0.68f, h * 0.5f)

        // Sclera (White base)
        drawCircle(color = AksaWhite, radius = eyeRadius, center = leftEyeCenter)
        drawCircle(color = AksaWhite, radius = eyeRadius, center = rightEyeCenter)

        // Eyeball Border
        drawCircle(color = AksaDarkBrown, radius = eyeRadius, center = leftEyeCenter, style = Stroke(width = 4f))
        drawCircle(color = AksaDarkBrown, radius = eyeRadius, center = rightEyeCenter, style = Stroke(width = 4f))

        // Pupils (Dark Brown)
        val pupilRadius = 18f
        val pupilOffset = 4f
        drawCircle(color = AksaDarkBrown, radius = pupilRadius, center = leftEyeCenter.copy(y = leftEyeCenter.y + pupilOffset))
        drawCircle(color = AksaDarkBrown, radius = pupilRadius, center = rightEyeCenter.copy(y = rightEyeCenter.y + pupilOffset))

        // Sparkle reflection highlights
        drawCircle(color = AksaWhite, radius = 6f, center = Offset(leftEyeCenter.x - 5f, leftEyeCenter.y - 2f))
        drawCircle(color = AksaWhite, radius = 3.5f, center = Offset(leftEyeCenter.x + 6f, leftEyeCenter.y + 6f))

        drawCircle(color = AksaWhite, radius = 6f, center = Offset(rightEyeCenter.x - 5f, rightEyeCenter.y - 2f))
        drawCircle(color = AksaWhite, radius = 3.5f, center = Offset(rightEyeCenter.x + 6f, rightEyeCenter.y + 6f))

        // Eyebrows
        val browPathLeft = Path().apply {
            moveTo(leftEyeCenter.x - 24f, leftEyeCenter.y - 38f)
            quadraticTo(leftEyeCenter.x, leftEyeCenter.y - 48f, leftEyeCenter.x + 22f, leftEyeCenter.y - 40f)
        }
        drawPath(browPathLeft, color = AksaDarkBrown, style = Stroke(width = 5f, cap = StrokeCap.Round))

        val browPathRight = Path().apply {
            moveTo(rightEyeCenter.x - 22f, rightEyeCenter.y - 40f)
            quadraticTo(rightEyeCenter.x, rightEyeCenter.y - 48f, rightEyeCenter.x + 24f, rightEyeCenter.y - 38f)
        }
        drawPath(browPathRight, color = AksaDarkBrown, style = Stroke(width = 5f, cap = StrokeCap.Round))
    }
}
