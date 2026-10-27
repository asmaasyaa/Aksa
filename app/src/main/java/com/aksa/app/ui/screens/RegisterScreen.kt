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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.ui.components.AksaAvatarSelector
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaTextField
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import com.aksa.app.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigateToParentWelcome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var guardianEmail by remember { mutableStateOf("") }
    var guardianPassword by remember { mutableStateOf("") }
    var childName by remember { mutableStateOf("") }
    var selectedAvatarId by remember { mutableStateOf("mascot_classic") }
    var selectedAge by remember { mutableIntStateOf(8) }
    var localError by remember { mutableStateOf<String?>(null) }

    val errorMessage = localError ?: uiState.errorMessage

    val isFormValid = guardianEmail.isNotBlank() &&
            guardianPassword.isNotBlank() &&
            childName.isNotBlank() &&
            selectedAvatarId.isNotBlank() &&
            selectedAge in 6..12

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaOrange)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Title Branding
            Text(
                text = "AKSA",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                letterSpacing = 2.sp,
                color = AksaWhite
            )

            // Environment Mode Pill
            Box(
                modifier = Modifier
                    .padding(top = 4.dp, bottom = 14.dp)
                    .clip(PillShape)
                    .background(if (uiState.isDemoMode) AksaYellow else AksaLime)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (uiState.isDemoMode) "Mode Demo (Lokal)" else "Mode Firebase (Tersinkron)",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AksaDarkBrown
                )
            }

            // Large White Rounded Card
            val cardShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = cardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, cardShape)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daftar Akun Belajar",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = AksaDarkBrown
                    )

                    Text(
                        text = "Akun orang tua untuk mendampingi profil belajar anak",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = AksaGrayText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    // 1. Email Orang Tua
                    AksaTextField(
                        value = guardianEmail,
                        onValueChange = {
                            guardianEmail = it
                            localError = null
                        },
                        label = "Email Orang Tua / Wali",
                        placeholder = "Masukkan email orang tua",
                        leadingIcon = AksaIcons.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Password Orang Tua
                    AksaTextField(
                        value = guardianPassword,
                        onValueChange = {
                            guardianPassword = it
                            localError = null
                        },
                        label = "Password Akun",
                        placeholder = "Minimal 6 karakter",
                        leadingIcon = AksaIcons.Lock,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AksaPeachLight)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Profil Pembelajar Cilik",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AksaDarkBrown
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Nama Panggilan Anak
                    AksaTextField(
                        value = childName,
                        onValueChange = {
                            childName = it
                            localError = null
                        },
                        label = "Nama Panggilan Anak",
                        placeholder = "Masukkan nama anak",
                        leadingIcon = AksaIcons.Face,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. Avatar Selection directly in Register
                    AksaAvatarSelector(
                        selectedAvatarId = selectedAvatarId,
                        onAvatarSelected = {
                            selectedAvatarId = it
                            localError = null
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 5. Age Selection (6-12)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Usia Anak (6–12 Tahun)",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AksaDarkBrown,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        // 2 rows of age buttons: 6, 7, 8, 9 and 10, 11, 12
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (age in listOf(6, 7, 8, 9)) {
                                    RegisterAgeChip(
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
                                    RegisterAgeChip(
                                        age = age,
                                        isSelected = age == selectedAge,
                                        onClick = { selectedAge = age },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target Age Information Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(AksaSoftPeach)
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = AksaIcons.Info,
                                contentDescription = null,
                                tint = AksaDarkBrown,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Aplikasi ini diperuntukkan untuk anak usia 6–12 tahun dengan pendekatan ramah & terstruktur.",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                color = AksaDarkBrown
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = AksaOrange,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Button: Daftar
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = AksaOrange,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        AksaButton(
                            text = "Daftar",
                            style = if (isFormValid) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                            onClick = {
                                if (!isFormValid) {
                                    localError = "Lengkapi email, password, nama anak, avatar, dan usia"
                                } else {
                                    viewModel.register(
                                        email = guardianEmail,
                                        pass = guardianPassword,
                                        childName = childName,
                                        childAge = selectedAge,
                                        childAvatarId = selectedAvatarId,
                                        onSuccess = onNavigateToParentWelcome
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Bottom Link: Sudah punya akun? Masuk
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Sudah punya akun? ",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = AksaDarkBrown
                        )
                        Text(
                            text = "Masuk",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AksaOrange,
                            modifier = Modifier.clickable(onClick = onNavigateToLogin)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RegisterAgeChip(
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
            .border(if (isSelected) 2.5.dp else 1.dp, borderColor, chipShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$age",
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = AksaDarkBrown
            )
            Text(
                text = "Tahun",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                color = AksaDarkBrown.copy(alpha = 0.75f)
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(AksaOrange)
                    .align(Alignment.TopEnd)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AksaIcons.Check,
                    contentDescription = null,
                    tint = AksaWhite,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
