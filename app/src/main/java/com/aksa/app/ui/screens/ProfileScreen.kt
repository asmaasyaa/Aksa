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
import androidx.compose.material3.HorizontalDivider
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
import com.aksa.app.ui.components.AksaAvatar
import com.aksa.app.ui.components.MascotStanding
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayBorder
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToDeveloperPreview: () -> Unit = {},
    authViewModel: com.aksa.app.ui.viewmodel.AuthViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    val guardianEmail = authState.currentGuardian?.email ?: "demo.orangtua@aksa.id"
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
                    text = "Profil",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AksaWhite
                )

                Spacer(modifier = Modifier.width(48.dp)) // Visual balance
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large White Rounded Profile Card
            val cardShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, cardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, cardShape)
                    .padding(24.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar Circle with Selected Avatar
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(AksaPeachLight)
                            .border(3.5.dp, AksaSoftPeach, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        AksaAvatar(avatarId = AksaUserState.avatarId, size = 80.dp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = AksaUserState.username,
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = AksaDarkBrown
                    )

                    Text(
                        text = "${AksaUserState.age} tahun",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = AksaDarkBrown.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    // Guardian account & mode indicator
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(AksaPeachLight)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Wali: $guardianEmail (${if (authState.isDemoMode) "Demo" else "Firebase"})",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = AksaDarkBrown.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Badges Row: Level & Streak
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Level Badge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AksaPeachLight)
                                .padding(vertical = 12.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AksaIcons.Star,
                                    contentDescription = null,
                                    tint = AksaYellowDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AksaUserState.level,
                                    fontFamily = Poppins,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = AksaDarkBrown
                                )
                            }
                        }

                        // Streak Badge
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AksaYellow)
                                .padding(vertical = 12.dp, horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = AksaIcons.Flame,
                                    contentDescription = null,
                                    tint = AksaOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${AksaUserState.streakCount} Hari Streak",
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

            Spacer(modifier = Modifier.height(20.dp))

            // White Rounded Menu Rows Card
            val menuCardShape = RoundedCornerShape(26.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, menuCardShape, spotColor = Color(0x22441100))
                    .background(AksaWhite, menuCardShape)
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Row 1: Edit Profil
                    ProfileMenuRow(
                        icon = AksaIcons.Edit,
                        iconBg = AksaPeachLight,
                        title = "Edit Profil",
                        onClick = { /* UI Mockup action */ }
                    )

                    HorizontalDivider(color = AksaGrayBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

                    // Row 2: Progress Belajar
                    ProfileMenuRow(
                        icon = AksaIcons.EmojiEvents,
                        iconBg = AksaYellow.copy(alpha = 0.35f),
                        title = "Progress Belajar",
                        badge = "Lihat",
                        onClick = onNavigateToProgress
                    )

                    HorizontalDivider(color = AksaGrayBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

                    // Row 3: Pengaturan
                    ProfileMenuRow(
                        icon = AksaIcons.Settings,
                        iconBg = Color(0xFFE8F2FA),
                        title = "Pengaturan",
                        onClick = { /* UI Mockup action */ }
                    )

                    if (com.aksa.app.BuildConfig.DEBUG) {
                        HorizontalDivider(color = AksaGrayBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                        ProfileMenuRow(
                            icon = AksaIcons.Star,
                            iconBg = AksaYellow.copy(alpha = 0.3f),
                            title = "Developer Preview Mode",
                            badge = "Debug",
                            textColor = AksaDarkBrown,
                            onClick = onNavigateToDeveloperPreview
                        )
                    }

                    HorizontalDivider(color = AksaGrayBorder, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))

                    // Row 4: Keluar
                    ProfileMenuRow(
                        icon = AksaIcons.ExitToApp,
                        iconBg = Color(0xFFFFECE6),
                        title = "Keluar",
                        textColor = AksaOrange,
                        onClick = {
                            authViewModel.signOut(onNavigateToLogin)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
    textColor: Color = AksaDarkBrown,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (textColor == AksaOrange) AksaOrange else AksaDarkBrown,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontFamily = Poppins,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        if (!badge.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(AksaOrange.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AksaOrange
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = AksaIcons.ChevronRight,
            contentDescription = null,
            tint = AksaGrayText,
            modifier = Modifier.size(20.dp)
        )
    }
}
