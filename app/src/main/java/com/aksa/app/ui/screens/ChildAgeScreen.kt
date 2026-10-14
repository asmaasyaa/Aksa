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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.aksa.app.ui.components.AksaButton
import com.aksa.app.ui.components.AksaButtonStyle
import com.aksa.app.ui.components.MascotPeeking
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaGrayBorder
import com.aksa.app.ui.theme.AksaGrayText
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaOrangeDark
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.Poppins

@Composable
fun ChildAgeScreen(
    onNavigateToHome: () -> Unit
) {
    var selectedAge by remember { mutableIntStateOf(12) }
    val ages = listOf(10, 11, 12, 13, 14, 15)

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
                    text = "LANGKAH 2 DARI 2",
                    fontFamily = Poppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    color = AksaYellow
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // White Rounded Form Card
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
                        text = "Umur Anak",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = AksaDarkBrown
                    )

                    Text(
                        text = "Pilih usia anak untuk menyesuaikan materi belajar",
                        fontFamily = Poppins,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = AksaGrayText,
                        modifier = Modifier.padding(top = 6.dp, bottom = 24.dp)
                    )

                    // 2x3 Grid of selectable age cards (10, 11, 12, 13, 14, 15)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        for (rowIndex in 0..1) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                for (colIndex in 0..2) {
                                    val age = ages[rowIndex * 3 + colIndex]
                                    val isSelected = age == selectedAge

                                    AgeSelectionCard(
                                        age = age,
                                        isSelected = isSelected,
                                        onClick = { selectedAge = age },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Button: Lanjut
                    AksaButton(
                        text = "Lanjut",
                        style = AksaButtonStyle.PRIMARY_ORANGE,
                        onClick = onNavigateToHome
                    )
                }
            }

            Spacer(modifier = Modifier.height(140.dp)) // Space for peeking mascot
        }

        // Mascot peeking from bottom
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
private fun AgeSelectionCard(
    age: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(20.dp)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        label = "AgeCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) AksaYellow else AksaPeachLight,
        label = "AgeCardBg"
    )

    val borderColor = if (isSelected) AksaOrange else Color.Transparent
    val textColor = if (isSelected) AksaDarkBrown else AksaDarkBrown.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .height(84.dp)
            .scale(scale)
            .clip(cardShape)
            .background(bgColor)
            .border(if (isSelected) 2.5.dp else 1.dp, borderColor, cardShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = age.toString(),
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = textColor
            )
            Text(
                text = "Tahun",
                fontFamily = Poppins,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = textColor.copy(alpha = 0.75f)
            )
        }

        // Small cute star badge if selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(AksaOrange)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AksaIcons.Star,
                    contentDescription = null,
                    tint = AksaWhite,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
