package com.aksa.app.ui.preview

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
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

@Composable
fun DeveloperPreviewScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToLevelMap: () -> Unit,
    onNavigateToAlphabetSelection: () -> Unit,
    onNavigateToLetterLearning: () -> Unit,
    onNavigateToHurufMirip: () -> Unit,
    onNavigateToSyllableIntro: () -> Unit,
    onNavigateToSyllableExample: () -> Unit,
    onNavigateToSyllableQuiz: () -> Unit,
    onNavigateToWordFormation: () -> Unit,
    onNavigateToReadingPractice: () -> Unit,
    onNavigateToTangkapHuruf: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToProgressAchievements: () -> Unit
) {
    val isPreviewActive by DeveloperPreviewManager.isPreviewActive.collectAsState()
    val visualState by DeveloperPreviewManager.visualState.collectAsState()
    val levelMapState by DeveloperPreviewManager.levelMapState.collectAsState()
    val previewDays by DeveloperPreviewManager.previewWeeklyDays.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 40.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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

                // Debug Banner Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AksaYellow)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "DEV PREVIEW (DEBUG)",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = AksaDarkBrown
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AKSA Developer Control Center",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AksaWhite
            )

            Text(
                text = "Mode pratinjau deterministik untuk tangkapan layar Figma dan evaluasi UX. Tidak mempengaruhi data progres nyata anak.",
                fontFamily = Poppins,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SECTION 1: QUICK SCREEN NAVIGATION
            PreviewSectionCard(title = "1. Navigasi Cepat Seluruh Layar") {
                PreviewNavButton(label = "Home Screen", subtitle = "Kalender mingguan & modul aktif", onClick = onNavigateToHome)
                PreviewNavButton(label = "Level Map (Peta Petualangan)", subtitle = "4 Dunia belajar & rute eksplorasi", onClick = onNavigateToLevelMap)
                PreviewNavButton(label = "Level 1: Pulau Alfabet (Daftar Huruf)", subtitle = "Grid huruf A–N & banner Huruf Mirip", onClick = onNavigateToAlphabetSelection)
                PreviewNavButton(label = "Level 1: Belajar & Kuis 3 Huruf", subtitle = "Intro huruf + 3 soal kuis audio", onClick = onNavigateToLetterLearning)
                PreviewNavButton(label = "Level 1: Huruf Mirip (10 Soal)", subtitle = "Latihan spesifik b/d dan p/q dengan TTS", onClick = onNavigateToHurufMirip)
                PreviewNavButton(label = "Level 2: Dunia Suku Kata (Intro)", subtitle = "Pengantar bunyi dan gabungan konsonan vokal", onClick = onNavigateToSyllableIntro)
                PreviewNavButton(label = "Level 2: Dunia Suku Kata (Contoh)", subtitle = "Kartu interaktif MA, TA, BU, KU", onClick = onNavigateToSyllableExample)
                PreviewNavButton(label = "Level 2: Dunia Suku Kata (Kuis 5 Soal)", subtitle = "Penyusunan suku kata interaktif", onClick = onNavigateToSyllableQuiz)
                PreviewNavButton(label = "Level 3: Gunung Kata (5 Kata Bergambar)", subtitle = "Penyusunan kata & tebak gambar (BOLA, BUKU, dll)", onClick = onNavigateToWordFormation)
                PreviewNavButton(label = "Level 4: Sungai Cerita (Cerita & Kuis)", subtitle = "Panel cerita interaktif + kuis pemahaman", onClick = onNavigateToReadingPractice)
                PreviewNavButton(label = "Mini-Game: Tangkap Huruf", subtitle = "Game huruf jatuh 15 ronde adaptif", onClick = onNavigateToTangkapHuruf)
                PreviewNavButton(label = "Profil Pengguna", subtitle = "Info anak, wali, dan pengaturan akun", onClick = onNavigateToProfile)
                PreviewNavButton(label = "Progress & Pencapaian", subtitle = "Statistik akurasi & lencana mingguan", onClick = onNavigateToProgressAchievements)
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 2: LEVEL MAP SIMULATION
            PreviewSectionCard(title = "2. Simulasi State Level Map") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StateFilterChip(
                        label = "Normal",
                        selected = levelMapState == LevelMapPreviewState.DEFAULT_PROGRESSION,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setLevelMapState(LevelMapPreviewState.DEFAULT_PROGRESSION)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StateFilterChip(
                        label = "Semua Terbuka",
                        selected = levelMapState == LevelMapPreviewState.ALL_UNLOCKED,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setLevelMapState(LevelMapPreviewState.ALL_UNLOCKED)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StateFilterChip(
                        label = "Semua Selesai",
                        selected = levelMapState == LevelMapPreviewState.ALL_COMPLETED,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setLevelMapState(LevelMapPreviewState.ALL_COMPLETED)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StateFilterChip(
                        label = "Level 1 Saja",
                        selected = levelMapState == LevelMapPreviewState.ALL_LOCKED_EXCEPT_LEVEL_1,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setLevelMapState(LevelMapPreviewState.ALL_LOCKED_EXCEPT_LEVEL_1)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 3: VISUAL STATE OVERRIDES
            PreviewSectionCard(title = "3. State Visual Jawaban & Hasil") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StateFilterChip(
                        label = "Default/Idle",
                        selected = visualState == PreviewVisualState.DEFAULT || visualState == PreviewVisualState.IDLE,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setVisualState(PreviewVisualState.IDLE)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StateFilterChip(
                        label = "Jawaban Benar",
                        selected = visualState == PreviewVisualState.CORRECT_FEEDBACK,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setVisualState(PreviewVisualState.CORRECT_FEEDBACK)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StateFilterChip(
                        label = "Jawaban Salah",
                        selected = visualState == PreviewVisualState.INCORRECT_FEEDBACK,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setVisualState(PreviewVisualState.INCORRECT_FEEDBACK)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StateFilterChip(
                        label = "Sesi Selesai (Modal)",
                        selected = visualState == PreviewVisualState.SESSION_COMPLETED,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setVisualState(PreviewVisualState.SESSION_COMPLETED)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 4: WEEKLY ACTIVITY & BADGE SIMULATION
            PreviewSectionCard(title = "4. Simulasi Kalender Mingguan (Home & Progress)") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StateFilterChip(
                        label = "0 Hari",
                        selected = previewDays == 0,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setPreviewWeeklyDays(0)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StateFilterChip(
                        label = "2 Hari",
                        selected = previewDays == 2,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setPreviewWeeklyDays(2)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StateFilterChip(
                        label = "3 Hari",
                        selected = previewDays == 3,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setPreviewWeeklyDays(3)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    StateFilterChip(
                        label = "7 Hari",
                        selected = previewDays == 7,
                        onClick = {
                            DeveloperPreviewManager.enablePreview(true)
                            DeveloperPreviewManager.setPreviewWeeklyDays(7)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // RESET BUTTON
            AksaButton(
                text = "Reset Mode Pratinjau ke Normal",
                style = AksaButtonStyle.SECONDARY_LIME,
                onClick = {
                    DeveloperPreviewManager.reset()
                    DeveloperPreviewManager.enablePreview(false)
                }
            )
        }
    }
}

@Composable
private fun PreviewSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AksaLime
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun PreviewNavButton(
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E293B))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = AksaWhite
                )
                Text(
                    text = subtitle,
                    fontFamily = Poppins,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            Icon(
                imageVector = AksaIcons.ArrowForward,
                contentDescription = null,
                tint = AksaLime,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun StateFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) AksaOrange else Color(0xFF1E293B)
    val textCol = if (selected) AksaWhite else Color(0xFFCBD5E1)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = Poppins,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = textCol
        )
    }
}
