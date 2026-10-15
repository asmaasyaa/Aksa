package com.aksa.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaGrayBackground
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

@Composable
fun AksaProfileCard(
    name: String = "MARVIN",
    age: String = "8 tahun",
    level: String = "LV 3 Master",
    streakCount: Int = 9,
    avatarId: String = "mascot_classic",
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(26.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = cardShape, spotColor = Color(0x2A6B240C))
            .background(AksaWhite, cardShape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circle with selected Aksa avatar
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(AksaPeachLight)
                    .border(2.5.dp, AksaSoftPeach, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AksaAvatar(
                    avatarId = avatarId,
                    size = 52.dp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Profile info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AksaDarkBrown
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = age,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AksaDarkBrown.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(AksaDarkBrown.copy(alpha = 0.3f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(AksaPeachLight, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = AksaIcons.Star,
                            contentDescription = null,
                            tint = AksaYellowDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = level,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = AksaDarkBrown
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Streak Pill Badge
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(AksaYellow)
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = AksaIcons.Flame,
                        contentDescription = "Streak",
                        tint = AksaOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$streakCount Streak",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AksaDarkBrown
                    )
                }
            }
        }
    }
}
