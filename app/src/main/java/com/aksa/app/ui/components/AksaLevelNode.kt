package com.aksa.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayDisabled
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaOrangeDark
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.Poppins

enum class LevelNodeStatus {
    ACTIVE,
    COMPLETED,
    LOCKED
}

@Composable
fun AksaLevelNode(
    levelNumber: Int,
    status: LevelNodeStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 76.dp,
    title: String? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "NodePulse")
    val pulseScale by if (status == LevelNodeStatus.ACTIVE) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "NodeScale"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val (bgColor, bottomBevel, borderColor, contentColor) = when (status) {
        LevelNodeStatus.ACTIVE -> QuadNodeColors(
            AksaYellow,
            AksaYellowDark,
            AksaOrange,
            AksaDarkBrown
        )
        LevelNodeStatus.COMPLETED -> QuadNodeColors(
            AksaLime,
            AksaLimeDark,
            AksaWhite,
            AksaDarkBrown
        )
        LevelNodeStatus.LOCKED -> QuadNodeColors(
            Color(0xFFE2E2E8),
            Color(0xFFCACACE),
            Color(0xFFD6D6DC),
            AksaGrayText
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size * pulseScale)
                .clickable(enabled = status != LevelNodeStatus.LOCKED, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // Bottom 3D bevel
            Box(
                modifier = Modifier
                    .size(size)
                    .offset(y = 5.dp)
                    .background(bottomBevel, CircleShape)
            )

            // Top surface
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(bgColor)
                    .border(3.5.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (status) {
                    LevelNodeStatus.ACTIVE -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = AksaIcons.Star,
                                contentDescription = null,
                                tint = AksaOrange,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = levelNumber.toString(),
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = contentColor
                            )
                        }
                    }
                    LevelNodeStatus.COMPLETED -> {
                        Icon(
                            imageVector = AksaIcons.Check,
                            contentDescription = "Completed",
                            tint = AksaWhite,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    LevelNodeStatus.LOCKED -> {
                        Icon(
                            imageVector = AksaIcons.Lock,
                            contentDescription = "Locked",
                            tint = AksaGrayText,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        if (!title.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (status == LevelNodeStatus.LOCKED) AksaGrayText else AksaDarkBrown
            )
        }
    }
}

private data class QuadNodeColors(
    val bg: Color,
    val bevel: Color,
    val border: Color,
    val content: Color
)
