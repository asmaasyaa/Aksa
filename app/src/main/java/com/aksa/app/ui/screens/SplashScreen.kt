package com.aksa.app.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.MascotPeeking
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.Poppins
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit
) {
    // Automatically navigate to Login after 2 seconds
    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateToLogin()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashAnimation")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaOrange)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onNavigateToLogin
            )
    ) {
        // Subtle floating alphabet pattern in background
        AlphabetBackgroundPattern()

        // Centered AKSA branding
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .scale(pulse),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Big playful badge
            Box(
                modifier = Modifier
                    .background(AksaWhite.copy(alpha = 0.16f), RoundedCornerShape(32.dp))
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "PETUALANGAN BELAJAR",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    color = AksaYellow
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Large centered text: AKSA
            Text(
                text = "AKSA",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 72.sp,
                letterSpacing = 4.sp,
                color = AksaWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Belajar Membaca Jadi Menyenangkan",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = AksaWhite.copy(alpha = 0.92f)
            )
        }

        // Mascot peeking from the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            MascotPeeking(
                width = 240.dp,
                height = 140.dp
            )
        }
    }
}

/**
 * Playful transparent background letters and decorative elements
 */
@Composable
private fun AlphabetBackgroundPattern() {
    val letters = listOf(
        AlphabetItem("A", 0.15f, 0.18f, 38.sp, -12f),
        AlphabetItem("K", 0.82f, 0.14f, 44.sp, 15f),
        AlphabetItem("S", 0.12f, 0.42f, 34.sp, 8f),
        AlphabetItem("A", 0.88f, 0.48f, 40.sp, -18f),
        AlphabetItem("B", 0.22f, 0.72f, 36.sp, 14f),
        AlphabetItem("C", 0.78f, 0.74f, 32.sp, -10f),
        AlphabetItem("D", 0.48f, 0.15f, 28.sp, 6f),
        AlphabetItem("M", 0.50f, 0.80f, 30.sp, -8f)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        letters.forEach { item ->
            Text(
                text = item.char,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = item.size,
                color = AksaWhite.copy(alpha = 0.12f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = (item.xPercent * 340).dp,
                        top = (item.yPercent * 700).dp
                    )
                    .rotate(item.rotation)
            )
        }

        // Faint decorative bubbles / stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = AksaWhite.copy(alpha = 0.08f),
                radius = 70f,
                center = Offset(size.width * 0.12f, size.height * 0.28f)
            )
            drawCircle(
                color = AksaWhite.copy(alpha = 0.06f),
                radius = 110f,
                center = Offset(size.width * 0.90f, size.height * 0.32f)
            )
            drawCircle(
                color = AksaWhite.copy(alpha = 0.08f),
                radius = 50f,
                center = Offset(size.width * 0.82f, size.height * 0.65f)
            )
        }
    }
}

private data class AlphabetItem(
    val char: String,
    val xPercent: Float,
    val yPercent: Float,
    val size: androidx.compose.ui.unit.TextUnit,
    val rotation: Float
)
