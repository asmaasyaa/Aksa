package com.aksa.app.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaProgressBar
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.ProgressBarColor
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayBorder
import com.aksa.app.ui.theme.AksaGrayDisabled
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

@Composable
fun ProgressAchievementsScreen(
    onNavigateBack: () -> Unit
) {
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

                Text(
                    text = "Progress Belajar",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AksaWhite
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Summary Streak Card
            val streakCardShape = RoundedCornerShape(26.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, streakCardShape, spotColor = Color(0x33441100))
                    .background(AksaYellow, streakCardShape)
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(AksaWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AksaIcons.Flame,
                            contentDescription = null,
                            tint = AksaOrange,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "9 Hari Berturut-turut!",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AksaDarkBrown
                        )
                        Text(
                            text = "Hebat! Marvin sangat rajin berlatih setiap hari.",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = AksaDarkBrown.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 1: LEARNING PROGRESS
            val progressCardShape = RoundedCornerShape(30.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, progressCardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, progressCardShape)
                    .padding(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Statistik Belajar",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AksaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Huruf (12 / 26)
                    ProgressMetricRow(
                        label = "Huruf",
                        completed = 12,
                        total = 26,
                        color = ProgressBarColor.ORANGE
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Suku Kata (3 / 12)
                    ProgressMetricRow(
                        label = "Suku Kata",
                        completed = 3,
                        total = 12,
                        color = ProgressBarColor.LIME
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Kata (2 / 10)
                    ProgressMetricRow(
                        label = "Kata",
                        completed = 2,
                        total = 10,
                        color = ProgressBarColor.YELLOW
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Cerita (1 / 5)
                    ProgressMetricRow(
                        label = "Cerita",
                        completed = 1,
                        total = 5,
                        color = ProgressBarColor.LIME
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: ACHIEVEMENTS (Pencapaian)
            val achievementCardShape = RoundedCornerShape(30.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, achievementCardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, achievementCardShape)
                    .padding(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Pencapaian (Badges)",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AksaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Badge 1: Huruf Pertamaku (Unlocked)
                    BadgeItemRow(
                        icon = AksaIcons.Star,
                        iconBg = AksaYellow,
                        title = "Huruf Pertamaku",
                        desc = "Menyelesaikan latihan huruf A",
                        isUnlocked = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge 2: Rajin Belajar (Unlocked)
                    BadgeItemRow(
                        icon = AksaIcons.Flame,
                        iconBg = AksaOrange,
                        title = "Rajin Belajar",
                        desc = "Mencapai 3 hari streak belajar",
                        isUnlocked = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge 3: Jago Suku Kata (Unlocked)
                    BadgeItemRow(
                        icon = AksaIcons.Check,
                        iconBg = AksaLime,
                        title = "Jago Suku Kata",
                        desc = "Menyelesaikan Modul 2 Suku Kata",
                        isUnlocked = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Badge 4: Pembaca Hebat (Locked)
                    BadgeItemRow(
                        icon = AksaIcons.Lock,
                        iconBg = Color(0xFFE4E4E8),
                        title = "Pembaca Hebat",
                        desc = "Selesaikan seluruh modul membaca",
                        isUnlocked = false
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Cheering Mascot at bottom of achievements
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        MascotCheering(size = 130.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressMetricRow(
    label: String,
    completed: Int,
    total: Int,
    color: ProgressBarColor
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AksaDarkBrown
            )
            Text(
                text = "$completed / $total",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AksaDarkBrown.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        AksaProgressBar(
            progress = completed.toFloat() / total.toFloat(),
            colorTheme = color,
            height = 14.dp
        )
    }
}

@Composable
private fun BadgeItemRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    desc: String,
    isUnlocked: Boolean
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isUnlocked) AksaPeachLight else Color(0xFFF7F7FA))
            .border(
                width = 1.5.dp,
                color = if (isUnlocked) AksaSoftPeach else Color(0xFFE8E8EE),
                shape = shape
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isUnlocked) AksaDarkBrown else AksaGrayText,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isUnlocked) AksaDarkBrown else AksaGrayText
            )
            Text(
                text = desc,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = if (isUnlocked) AksaDarkBrown.copy(alpha = 0.7f) else AksaGrayText
            )
        }

        if (isUnlocked) {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(AksaLime)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Dapat",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = AksaDarkBrown
                )
            }
        }
    }
}
