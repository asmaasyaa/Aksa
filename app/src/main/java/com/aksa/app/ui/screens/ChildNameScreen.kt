package com.aksa.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.AksaTextField
import com.aksa.app.ui.components.MascotPeeking
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.Poppins

@Composable
fun ChildNameScreen(
    onNavigateToChildAge: () -> Unit
) {
    var childName by remember { mutableStateOf("Marvin") }

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
                    text = "LANGKAH 1 DARI 2",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = AksaYellow
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // White Rounded Input Card
            val cardShape = RoundedCornerShape(32.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 12.dp, shape = cardShape, spotColor = Color(0x33441100))
                    .background(AksaWhite, cardShape)
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nama Anak",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = AksaDarkBrown
                    )

                    Text(
                        text = "Siapa nama pahlawan cilik yang akan belajar?",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = AksaGrayText,
                        modifier = Modifier.padding(top = 6.dp, bottom = 26.dp)
                    )

                    // Large rounded white input
                    AksaTextField(
                        value = childName,
                        onValueChange = { childName = it },
                        label = "Nama Lengkap / Panggilan",
                        placeholder = "Masukkan Nama Di Sini",
                        leadingIcon = AksaIcons.Face,
                        height = 60.dp,
                        cornerRadius = 22.dp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Button: Lanjut
                    AksaButton(
                        text = "Lanjut",
                        style = AksaButtonStyle.PRIMARY_ORANGE,
                        onClick = onNavigateToChildAge
                    )
                }
            }

            Spacer(modifier = Modifier.height(140.dp)) // Space for mascot
        }

        // Mascot peeking from bottom-right as specified
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp)
        ) {
            MascotPeeking(
                width = 200.dp,
                height = 130.dp
            )
        }
    }
}
