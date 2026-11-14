package com.aksa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import com.aksa.app.ui.components.AksaAvatarSelector
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotPeeking
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.Poppins
import com.aksa.app.ui.viewmodel.CreateProfileViewModel

@Composable
fun ChildAvatarScreen(
    viewModel: CreateProfileViewModel,
    onNavigateToHome: () -> Unit
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step pill indicator
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(AksaWhite.copy(alpha = 0.2f))
                    .padding(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "LANGKAH 3 DARI 3",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = AksaYellow
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                        text = "Pilih Avatar",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = AksaDarkBrown
                    )

                    Text(
                        text = "Pilih teman belajar untuk ${uiState.name.ifBlank { "si kecil" }}",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = AksaGrayText,
                        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                    )

                    // Real drawn Aksa Avatars using AksaAvatarSelector
                    AksaAvatarSelector(
                        selectedAvatarId = uiState.avatarId,
                        onAvatarSelected = { viewModel.setAvatarId(it) },
                        title = null
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

                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = AksaOrange,
                            modifier = Modifier.size(44.dp)
                        )
                    } else {
                        AksaButton(
                            text = "Mulai Belajar!",
                            style = AksaButtonStyle.PRIMARY_ORANGE,
                            onClick = {
                                viewModel.createProfile(onSuccess = onNavigateToHome)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }

        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            MascotPeeking(
                width = 240.dp,
                height = 140.dp
            )
        }
    }
}
