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
import com.aksa.app.domain.model.WeeklyProgress
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayBorder
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import java.time.LocalDate

/**
 * Compact weekly learning activity card for HomeScreen.
 * Displays Monday through Sunday, active learning days, current day, and milestone progress.
 */
@Composable
fun WeeklyActivityCard(
    weeklyProgress: WeeklyProgress?,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(26.dp)
    val today = LocalDate.now()
    val monday = weeklyProgress?.weekStartDate ?: today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))

    val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val totalActiveDays = weeklyProgress?.totalActiveDays ?: 0

    val encouragingText = when {
        totalActiveDays >= 7 -> "Sempurna! Kamu belajar setiap hari minggu ini!"
        totalActiveDays >= 3 -> "Hebat! Target 3 hari tercapai, teruskan!"
        totalActiveDays >= 2 -> "Keren! Lencana 2 hari sudah kamu raih!"
        totalActiveDays == 1 -> "Awal yang baik! Ayo berlatih 1 hari lagi untuk lencana pertama!"
        else -> "Yuk mulai latihan hari ini untuk membuka lencana mingguan!"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 6.dp, shape = cardShape, spotColor = Color(0x2A6B240C))
            .background(AksaWhite, cardShape)
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row: Title & Active Day Count Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AksaPeachLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AksaIcons.Calendar,
                            contentDescription = "Kalender Belajar",
                            tint = AksaOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Aktivitas Minggu Ini",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = AksaDarkBrown
                    )
                }

                // Active Days Pill: X / 7 Hari
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(if (totalActiveDays >= 2) AksaLime.copy(alpha = 0.25f) else AksaPeachLight)
                        .border(
                            1.dp,
                            if (totalActiveDays >= 2) AksaLimeDark else AksaSoftPeach,
                            PillShape
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$totalActiveDays / 7 Hari",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (totalActiveDays >= 2) AksaDarkBrown else AksaOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7 Days of the Week Row (Monday to Sunday)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0 until 7) {
                    val date = monday.plusDays(i.toLong())
                    val isActive = weeklyProgress?.isActiveDay(date) == true
                    val isToday = date == today

                    DayColumn(
                        dayName = dayNames[i],
                        dayNumber = date.dayOfMonth,
                        isActive = isActive,
                        isToday = isToday
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Milestone Badges Mini Status Row (2 Hari, 3 Hari, 7 Hari)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniMilestoneChip(
                    label = "2 Hari",
                    isEarned = totalActiveDays >= 2,
                    modifier = Modifier.weight(1f)
                )
                MiniMilestoneChip(
                    label = "3 Hari",
                    isEarned = totalActiveDays >= 3,
                    modifier = Modifier.weight(1f)
                )
                MiniMilestoneChip(
                    label = "7 Hari",
                    isEarned = totalActiveDays >= 7,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Encouraging Friendly Text
            Text(
                text = encouragingText,
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = AksaDarkBrown.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DayColumn(
    dayName: String,
    dayNumber: Int,
    isActive: Boolean,
    isToday: Boolean
) {
    val circleShape = CircleShape
    val circleBg = when {
        isActive -> AksaLime
        isToday -> AksaYellow.copy(alpha = 0.35f)
        else -> AksaPeachLight
    }

    val circleBorder = when {
        isToday -> AksaOrange
        isActive -> AksaLimeDark
        else -> Color.Transparent
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayName,
            fontFamily = Poppins,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = if (isToday) AksaOrange else AksaDarkBrown.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(circleShape)
                .background(circleBg)
                .border(if (isToday || isActive) 1.5.dp else 0.dp, circleBorder, circleShape),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Icon(
                    imageVector = AksaIcons.Check,
                    contentDescription = "Selesai",
                    tint = AksaDarkBrown,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = dayNumber.toString(),
                    fontFamily = Poppins,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp,
                    color = if (isToday) AksaOrange else AksaDarkBrown.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun MiniMilestoneChip(
    label: String,
    isEarned: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val bg = if (isEarned) AksaYellow.copy(alpha = 0.25f) else Color(0xFFF5F5F7)
    val border = if (isEarned) AksaYellowDark else AksaGrayBorder
    val textColor = if (isEarned) AksaDarkBrown else AksaGrayText

    Row(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .border(1.dp, border, shape)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isEarned) AksaIcons.Star else AksaIcons.Lock,
            contentDescription = null,
            tint = if (isEarned) AksaYellowDark else AksaGrayText,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = textColor
        )
    }
}
