package com.aksa.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotPeeking
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.Poppins

@Composable
fun ParentWelcomeScreen(
    onNavigateToHome: () -> Unit = {},
    onNavigateToChildName: () -> Unit = onNavigateToHome
) {
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
            // Large AKSA Branding Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(AksaWhite.copy(alpha = 0.2f))
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "AKSA UNTUK ORANG TUA",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = AksaYellow
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Large White Rounded Card
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
                        text = "Halo, Ayah dan Bunda!",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                        color = AksaDarkBrown
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "“Aplikasi ini dirancang untuk membantu anak berlatih membaca dengan cara yang lebih menyenangkan.”",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        textAlign = TextAlign.Center,
                        color = AksaDarkBrown.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3 Reassuring Feature Highlights
                    ParentFeatureHighlight(
                        icon = AksaIcons.AutoAwesome,
                        iconBg = AksaYellow,
                        title = "Pendekatan Tanpa Tekanan",
                        description = "Belajar perlahan dengan ritme ramah anak"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ParentFeatureHighlight(
                        icon = AksaIcons.MenuBook,
                        iconBg = AksaBlue,
                        title = "Materi Terstruktur",
                        description = "Mulai dari fonetik, huruf, hingga suku kata"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ParentFeatureHighlight(
                        icon = AksaIcons.Favorite,
                        iconBg = AksaLime,
                        title = "Dukungan Penuh",
                        description = "Pantau dan rayakan setiap pencapaian kecilnya"
                    )

                    Spacer(modifier = Modifier.height(26.dp))

                    // Primary Button: Mulai Petualangan
                    AksaButton(
                        text = "Mulai Petualangan",
                        style = AksaButtonStyle.PRIMARY_ORANGE,
                        onClick = onNavigateToHome
                    )
                }
            }

            Spacer(modifier = Modifier.height(130.dp)) // Space for peeking mascot
        }

        // Large mascot peeking from bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            MascotPeeking(
                width = 240.dp,
                height = 140.dp
            )
        }
    }
}

@Composable
private fun ParentFeatureHighlight(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AksaPeachLight)
            .padding(12.dp),
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
                tint = AksaDarkBrown,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = AksaDarkBrown
            )
            Text(
                text = description,
                fontFamily = Poppins,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = AksaDarkBrown.copy(alpha = 0.65f)
            )
        }
    }
}
