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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.Poppins

@Composable
fun RegisterScreen(
    onNavigateToParentWelcome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedAvatarId by remember { mutableStateOf("mascot_classic") }
    var selectedAge by remember { mutableIntStateOf(8) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val ageOptions = listOf(6, 7, 8, 9, 10, 11, 12)

    val isFormValid = username.isNotBlank() &&
            password.isNotBlank() &&
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
                color = AksaWhite,
                modifier = Modifier.padding(bottom = 16.dp)
            )

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
                        text = "Buat Profil AKSA",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = AksaDarkBrown
                    )

                    Text(
                        text = "Yuk buat profil untuk memulai petualangan belajar!",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = AksaGrayText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // 1. Nama Pengguna
                    AksaTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = "Nama Pengguna",
                        placeholder = "Masukkan nama kamu",
                        leadingIcon = AksaIcons.Face,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Password with show/hide icon
                    AksaTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = "Password",
                        placeholder = "Masukkan password",
                        leadingIcon = AksaIcons.Lock,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 3. Avatar Selection directly in Register
                    AksaAvatarSelector(
                        selectedAvatarId = selectedAvatarId,
                        onAvatarSelected = {
                            selectedAvatarId = it
                            errorMessage = null
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 4. Age Selection (6-12)
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Usia",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = AksaDarkBrown,
                            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                        )

                        // 2 rows of age buttons: 6, 7, 8, 9 and 10, 11, 12
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Row 1: 6, 7, 8, 9
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

                            // Row 2: 10, 11, 12
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

                    Spacer(modifier = Modifier.height(18.dp))

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
                                text = "Aplikasi ini diperuntukkan untuk anak usia 6–12 tahun.",
                                fontFamily = Poppins,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = AksaDarkBrown
                            )
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = AksaOrange,
                            modifier = Modifier.padding(top = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Button: Daftar
                    AksaButton(
                        text = "Daftar",
                        style = if (isFormValid) AksaButtonStyle.PRIMARY_ORANGE else AksaButtonStyle.SECONDARY_LIME,
                        onClick = {
                            if (!isFormValid) {
                                errorMessage = "Lengkapi nama pengguna, password, avatar, dan usia"
                            } else {
                                AksaUserState.updateProfile(
                                    newUsername = username.trim(),
                                    newPassword = password,
                                    newAvatarId = selectedAvatarId,
                                    newAge = selectedAge
                                )
                                onNavigateToParentWelcome()
                            }
                        }
                    )

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
