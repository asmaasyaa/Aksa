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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaTextField
import com.aksa.app.ui.components.MascotStanding
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.PillShape
import com.aksa.app.ui.theme.Poppins
import com.aksa.app.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf(if (uiState.isDemoMode) "demo.orangtua@aksa.id" else "") }
    var password by remember { mutableStateOf(if (uiState.isDemoMode) "123456" else "") }
    var localError by remember { mutableStateOf<String?>(null) }

    val errorMessage = localError ?: uiState.errorMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AksaOrange)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Mascot Branding Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                MascotStanding(size = 90.dp, animated = false)
                Spacer(modifier = Modifier.height(8.dp))
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
                        .padding(top = 6.dp)
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
            }

            // White Rounded Login Card
            val cardShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = cardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, cardShape)
                    .padding(26.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Selamat Datang",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = AksaDarkBrown
                    )

                    Text(
                        text = "Masuk akun orang tua untuk mendampingi belajar anak",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = AksaGrayText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 22.dp)
                    )

                    // Input 1: Email Orang Tua / Wali
                    AksaTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localError = null
                        },
                        label = "Email Orang Tua / Wali",
                        placeholder = "Masukkan email akun orang tua",
                        leadingIcon = AksaIcons.Email,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Input 2: Password with show/hide icon
                    AksaTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        label = "Password",
                        placeholder = "Masukkan password",
                        leadingIcon = AksaIcons.Lock,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = AksaOrange,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Primary Button: Masuk
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = AksaOrange,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        AksaButton(
                            text = "Masuk",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = {
                                if (email.isBlank() || password.isBlank()) {
                                    localError = "Silakan masukkan email dan password"
                                } else {
                                    viewModel.login(
                                        email = email,
                                        pass = password,
                                        onSuccess = onLoginSuccess
                                    )
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Bottom Link: Belum punya akun? Daftar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Belum punya akun? ",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = AksaDarkBrown
                        )
                        Text(
                            text = "Daftar",
                            fontFamily = Poppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = AksaOrange,
                            modifier = Modifier.clickable(onClick = onNavigateToRegister)
                        )
                    }
                }
            }
        }
    }
}
