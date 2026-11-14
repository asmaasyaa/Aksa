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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.domain.model.Profile
import com.aksa.app.ui.components.AksaAvatar
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotStanding
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.Poppins
import com.aksa.app.ui.viewmodel.ProfileManagementViewModel

@Composable
fun SelectProfileScreen(
    onProfileSelected: () -> Unit,
    onNavigateToAddProfile: () -> Unit,
    viewModel: ProfileManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Step/Category pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(AksaWhite.copy(alpha = 0.2f))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "PILIH PROFIL",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = AksaYellow
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Siapa yang Mau Belajar?",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = AksaWhite,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Pilih profil anak untuk melanjutkan petualangan membaca",
                fontFamily = Poppins,
                fontSize = 13.sp,
                color = AksaWhite.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AksaWhite)
                }
            } else if (uiState.profiles.isEmpty()) {
                val cardShape = RoundedCornerShape(28.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, cardShape)
                        .background(AksaWhite, cardShape)
                        .padding(28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MascotStanding(size = 110.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Belum Ada Profil",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AksaDarkBrown
                        )
                        Text(
                            text = "Buat profil anak pertama untuk mulai belajar bersama Aksa.",
                            fontFamily = Poppins,
                            fontSize = 13.sp,
                            color = AksaGrayText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                        )
                        AksaButton(
                            text = "Buat Profil Anak",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = onNavigateToAddProfile
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    uiState.profiles.forEach { profile ->
                        val isActive = profile.profileId == AksaUserState.currentProfileId

                        ChildProfileSelectionCard(
                            profile = profile,
                            isActive = isActive,
                            onClick = {
                                viewModel.selectProfile(profile, onProfileSelected)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Add Child Profile Button
                    AksaButton(
                        text = "+ Tambah Profil Anak",
                        style = AksaButtonStyle.WHITE_CARD,
                        onClick = onNavigateToAddProfile,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ChildProfileSelectionCard(
    profile: Profile,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(24.dp)
    val borderColor = if (isActive) AksaYellow else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, cardShape, spotColor = Color(0x33441100))
            .background(AksaWhite, cardShape)
            .border(if (isActive) 2.5.dp else 0.dp, borderColor, cardShape)
            .clip(cardShape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(AksaPeachLight)
                    .border(2.dp, AksaSoftPeach, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AksaAvatar(
                    avatarId = profile.avatarId,
                    size = 52.dp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = AksaDarkBrown
                )
                Text(
                    text = if (profile.age > 0) "${profile.age} tahun" else "Usia belajar",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = AksaGrayText
                )
            }

            if (isActive) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AksaYellow.copy(alpha = 0.25f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Aktif",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = AksaDarkBrown
                    )
                }
            } else {
                Icon(
                    imageVector = AksaIcons.ChevronRight,
                    contentDescription = "Pilih",
                    tint = AksaGrayText,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

