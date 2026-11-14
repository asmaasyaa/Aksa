package com.aksa.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.ui.components.AksaAvatarSelector
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaTextField
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.Poppins
import com.aksa.app.ui.viewmodel.ProfileManagementViewModel

@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileManagementViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeProfile = uiState.activeProfile

    var name by remember { mutableStateOf(activeProfile?.name ?: AksaUserState.username) }
    var selectedAge by remember { mutableIntStateOf(if ((activeProfile?.age ?: 0) > 0) activeProfile!!.age else AksaUserState.age) }
    var selectedAvatarId by remember { mutableStateOf(activeProfile?.avatarId ?: AksaUserState.avatarId) }

    // Sync initial state when active profile loads
    LaunchedEffect(activeProfile) {
        if (activeProfile != null) {
            name = activeProfile.name
            if (activeProfile.age > 0) {
                selectedAge = activeProfile.age
            }
            selectedAvatarId = activeProfile.avatarId
        }
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(AksaWhite.copy(alpha = 0.25f))
                ) {
                    Icon(
                        imageVector = AksaIcons.ArrowBack,
                        contentDescription = "Kembali",
                        tint = AksaWhite
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Edit Profil Anak",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AksaWhite
                )

                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.size(42.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            val cardShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, cardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, cardShape)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Data Profil",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = AksaDarkBrown
                    )

                    Text(
                        text = "Perbarui nama, usia, dan avatar teman belajar",
                        fontFamily = Poppins,
                        fontSize = 13.sp,
                        color = AksaGrayText,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    AksaTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.clearError()
                        },
                        label = "Nama Anak",
                        placeholder = "Masukkan nama anak",
                        leadingIcon = AksaIcons.Face,
                        height = 58.dp,
                        cornerRadius = 20.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Usia Anak: $selectedAge Tahun",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AksaDarkBrown,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (age in listOf(6, 7, 8, 9)) {
                                AgeChip(
                                    age = age,
                                    isSelected = age == selectedAge,
                                    onClick = { selectedAge = age },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (age in listOf(10, 11, 12)) {
                                AgeChip(
                                    age = age,
                                    isSelected = age == selectedAge,
                                    onClick = { selectedAge = age },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    AksaAvatarSelector(
                        selectedAvatarId = selectedAvatarId,
                        onAvatarSelected = { selectedAvatarId = it },
                        title = "Pilih Avatar"
                    )

                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = uiState.errorMessage!!,
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = Color(0xFFCC3300),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(26.dp))

                    if (uiState.isUpdating) {
                        CircularProgressIndicator(
                            color = AksaOrange,
                            modifier = Modifier.size(44.dp)
                        )
                    } else {
                        AksaButton(
                            text = "Simpan Perubahan",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = {
                                viewModel.updateProfile(
                                    name = name,
                                    age = selectedAge,
                                    avatarId = selectedAvatarId,
                                    onSuccess = onNavigateBack
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgeChip(
    age: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipShape = RoundedCornerShape(16.dp)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        label = "AgeChipScale"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) AksaYellow else AksaPeachLight,
        label = "AgeChipBg"
    )
    val borderColor = if (isSelected) AksaOrange else Color.Transparent

    Box(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clip(chipShape)
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, chipShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$age thn",
            fontFamily = Poppins,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 13.sp,
            color = AksaDarkBrown
        )
    }
}

