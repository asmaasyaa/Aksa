package com.aksa.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.MascotPeeking
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaBlueDark
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaOrangeDark
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import kotlinx.coroutines.delay

enum class MicState {
    IDLE,
    LISTENING,
    SUCCESS
}

@Composable
fun PronunciationScreen(
    letter: String = "A",
    onNavigateBack: () -> Unit,
    onNavigateToWriting: () -> Unit
) {
    var micState by remember { mutableStateOf(MicState.IDLE) }
    var isPlayingSpeaker by remember { mutableStateOf(false) }

    // Handle simulated voice listening
    LaunchedEffect(micState) {
        if (micState == MicState.LISTENING) {
            delay(1600)
            micState = MicState.SUCCESS
        }
    }

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar (Kembali + Progress Pill)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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
                        text = "1 dari 2",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Instruction Text
            Text(
                text = "Bunyikan Huruf Ini!",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = AksaDarkBrown
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Large Centered White Rounded Learning Card
            val cardShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, cardShape, spotColor = Color(0x33003366))
                    .background(AksaWhite, cardShape)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Speaker Button (to hear sound)
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isPlayingSpeaker) AksaYellow else AksaBlue.copy(alpha = 0.5f))
                            .clickable {
                                isPlayingSpeaker = !isPlayingSpeaker
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AksaIcons.VolumeUp,
                            contentDescription = "Dengarkan Suara",
                            tint = AksaDarkBrown,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Huge Orange Letter
                    Text(
                        text = letter,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 110.sp,
                        color = AksaOrange
                    )

                    Text(
                        text = "Bunyi: \"/a/\" seperti pada Apel",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AksaDarkBrown.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Large Circular Microphone Button
                    MicrophoneButton(
                        state = micState,
                        onClick = {
                            if (micState != MicState.LISTENING) {
                                micState = MicState.LISTENING
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dynamic State Prompt
                    val promptText = when (micState) {
                        MicState.IDLE -> "Tekan mikrofon untuk mulai"
                        MicState.LISTENING -> "Sedang mendengarkan..."
                        MicState.SUCCESS -> "Pengucapan terdeteksi!"
                    }

                    Text(
                        text = promptText,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (micState == MicState.LISTENING) AksaOrange else AksaDarkBrown.copy(alpha = 0.75f)
                    )

                    // Green Success Feedback Banner
                    AnimatedVisibility(
                        visible = micState == MicState.SUCCESS,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(AksaLime)
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = AksaIcons.Check,
                                        contentDescription = null,
                                        tint = AksaDarkBrown,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bagus! Pengucapanmu benar!",
                                        fontFamily = Poppins,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AksaDarkBrown
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Revealed "Lanjut" Button
                            AksaButton(
                                text = "Lanjut",
                                style = AksaButtonStyle.PRIMARY_ORANGE,
                                onClick = onNavigateToWriting
                            )
                        }
                    }
                }
            }
        }

        // Mascot at bottom (Cheering when successful, Peeking otherwise)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            if (micState == MicState.SUCCESS) {
                MascotCheering(size = 135.dp)
            } else {
                MascotPeeking(width = 220.dp, height = 125.dp)
            }
        }
    }
}

@Composable
private fun MicrophoneButton(
    state: MicState,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MicPulse")
    val pulseScale by if (state == MicState.LISTENING) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.14f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "MicScale"
        )
    } else {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val (bg, bevel, iconColor) = when (state) {
        MicState.IDLE -> Triple(AksaOrange, AksaOrangeDark, AksaWhite)
        MicState.LISTENING -> Triple(AksaYellow, Color(0xFFD69A00), AksaDarkBrown)
        MicState.SUCCESS -> Triple(AksaLime, Color(0xFF75A61E), AksaDarkBrown)
    }

    Box(
        modifier = Modifier
            .size(88.dp)
            .scale(pulseScale)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // 3D bottom bevel
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(bevel, CircleShape)
                .padding(top = 4.dp)
        )

        // Top button circle
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(bg)
                .border(3.dp, AksaWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (state == MicState.SUCCESS) AksaIcons.Check else AksaIcons.Mic,
                contentDescription = "Mikrofon",
                tint = iconColor,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}
