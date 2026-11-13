package com.aksa.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aksa.app.R
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaTextField
import com.aksa.app.ui.components.MascotStanding
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.Poppins
import com.aksa.app.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

@Composable
fun LoginScreen(
    onLoginSuccess: (hasProfile: Boolean) -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val errorMessage = localError ?: uiState.errorMessage

    // Clear error when user edits credentials
    LaunchedEffect(email, password) {
        if (uiState.errorMessage != null) {
            viewModel.clearError()
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (!idToken.isNullOrBlank()) {
                    viewModel.signInWithGoogle(idToken) { hasProfile ->
                        onLoginSuccess(hasProfile)
                    }
                } else {
                    viewModel.setError("Gagal mendapatkan token autentikasi Google.")
                }
            } catch (e: ApiException) {
                if (e.statusCode == CommonStatusCodes.CANCELED || e.statusCode == 12501) {
                    // User cancelled account selection
                    viewModel.clearLoading()
                } else if (e.statusCode == CommonStatusCodes.NETWORK_ERROR || e.statusCode == 7) {
                    viewModel.setError("Koneksi internet bermasalah. Periksa jaringan Anda.")
                } else {
                    viewModel.setError("Google Sign-In gagal: ${e.localizedMessage ?: "Kode ${e.statusCode}"}")
                }
            }
        } else {
            // Dismissed without sign in
            viewModel.clearLoading()
        }
    }

    fun handleGoogleSignIn() {
        if (uiState.isLoading) return
        localError = null

        val webClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            ""
        }

        if (webClientId.isBlank() || webClientId == "PLACEHOLDER_WEB_CLIENT_ID") {
            viewModel.setError(
                "Google Sign-In belum dikonfigurasi. Web Client ID belum disetel di Firebase Console."
            )
            return
        }

        viewModel.setLoading(true)
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            val client = GoogleSignIn.getClient(context, gso)
            client.signOut().addOnCompleteListener {
                googleSignInLauncher.launch(client.signInIntent)
            }
        } catch (e: Exception) {
            viewModel.setError("Gagal memulai Google Sign-In: ${e.localizedMessage}")
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
            }

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
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 22.dp)
                    )

                    // Google Sign-In button
                    AksaButton(
                        text = "Masuk dengan Google",
                        style = AksaButtonStyle.SOCIAL,
                        icon = AksaIcons.Google,
                        enabled = !uiState.isLoading,
                        onClick = { handleGoogleSignIn() }
                    )

                    // Divider "atau"
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x22000000))
                        Text(
                            text = "  atau  ",
                            fontFamily = Poppins,
                            fontSize = 12.sp,
                            color = AksaGrayText
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0x22000000))
                    }

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

                    AksaTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localError = null
                        },
                        label = "Kata Sandi",
                        placeholder = "Masukkan kata sandi",
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
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
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
