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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.domain.model.MilestoneBadgeInfo
import com.aksa.app.ui.components.AksaProgressBar
import com.aksa.app.ui.components.MascotCheering
import com.aksa.app.ui.components.ProgressBarColor
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayBorder
import com.aksa.app.ui.theme.AksaGrayDisabled
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
import com.aksa.app.ui.viewmodel.ProgressAchievementsViewModel

@Composable
fun ProgressAchievementsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProgressAchievementsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDevPreview by com.aksa.app.ui.preview.DeveloperPreviewManager.isPreviewActive.collectAsState()
    val previewDays by com.aksa.app.ui.preview.DeveloperPreviewManager.previewWeeklyDays.collectAsState()

    val effectiveStreak = if (isDevPreview) previewDays else uiState.currentStreak
    val effectiveBadges = if (isDevPreview) {
        com.aksa.app.ui.preview.DeveloperPreviewFixtures.createMilestoneBadges(previewDays)
    } else {
        uiState.milestoneBadges
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

            // Real Streak Card
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
                        val streak = effectiveStreak
                        Text(
                            text = if (streak > 0) "$streak Hari Berturut-turut!" else "Mulai Streak Belajar!",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AksaDarkBrown
                        )
                        Text(
                            text = if (streak > 0) {
                                "Hebat! ${AksaUserState.username} rajin berlatih setiap hari."
                            } else {
                                "Selesaikan sesi belajar untuk meraih streak pertamamu!"
                            },
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = AksaDarkBrown.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 1: LEARNING STATS
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
                        text = "Statistik Belajar Nyata",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = AksaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sesi Belajar Selesai
                    StatRow(
                        label = "Sesi Selesai",
                        value = "${uiState.completedSessionsCount} sesi",
                        highlightColor = AksaOrange
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total Soal Dijawab
                    StatRow(
                        label = "Total Soal Dijawab",
                        value = "${uiState.totalQuestionsAnswered} soal",
                        highlightColor = AksaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rata-rata Akurasi
                    StatRow(
                        label = "Rata-rata Akurasi",
                        value = "${uiState.averageAccuracyPercent}%",
                        highlightColor = AksaLimeDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AksaProgressBar(
                        progress = (uiState.averageAccuracyPercent / 100f).coerceIn(0f, 1f),
                        colorTheme = ProgressBarColor.LIME,
                        height = 14.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 2: WEEKLY ACHIEVEMENTS (Badges)
            val achievementCardShape = RoundedCornerShape(30.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, achievementCardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, achievementCardShape)
                    .padding(22.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lencana Mingguan",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = AksaDarkBrown
                        )

                        Text(
                            text = "Dapat Berulang",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = AksaOrange
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display the 3 independent weekly milestone badges
                    for (badge in effectiveBadges) {
                        WeeklyBadgeItemRow(badge = badge)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
private fun StatRow(
    label: String,
    value: String,
    highlightColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = AksaDarkBrown
        )
        Text(
            text = value,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = highlightColor
        )
    }
}

@Composable
private fun WeeklyBadgeItemRow(badge: MilestoneBadgeInfo) {
    val isEarned = badge.cumulativeEarnedCount > 0 || badge.isEarnedThisWeek
    val shape = RoundedCornerShape(18.dp)

    val icon = when (badge.milestoneDays) {
        2 -> AksaIcons.Star
        3 -> AksaIcons.Flame
        7 -> AksaIcons.EmojiEvents
        else -> AksaIcons.Star
    }

    val iconBg = when {
        !isEarned -> Color(0xFFE4E4E8)
        badge.milestoneDays == 7 -> AksaYellow
        badge.milestoneDays == 3 -> AksaOrange
        else -> AksaLime
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isEarned) AksaPeachLight else Color(0xFFF7F7FA))
            .border(
                width = 1.5.dp,
                color = if (isEarned) AksaSoftPeach else Color(0xFFE8E8EE),
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
                imageVector = if (isEarned) icon else AksaIcons.Lock,
                contentDescription = null,
                tint = if (isEarned) AksaDarkBrown else AksaGrayText,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = badge.title,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isEarned) AksaDarkBrown else AksaGrayText
            )
            Text(
                text = badge.description,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                color = if (isEarned) AksaDarkBrown.copy(alpha = 0.7f) else AksaGrayText
            )
        }

        if (isEarned) {
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(if (badge.isEarnedThisWeek) AksaLime else AksaYellow)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (badge.isEarnedThisWeek) "Minggu Ini" else "Pernah Diraih",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = AksaDarkBrown
                    )
                }
                if (badge.cumulativeEarnedCount > 0) {
                    Text(
                        text = "${badge.cumulativeEarnedCount}x diraih",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = AksaOrange,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
