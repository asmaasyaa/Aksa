package com.aksa.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aksa.app.ui.theme.AksaIcons

@Composable
fun EarnedStarsRow(
    accuracy: Float,
    modifier: Modifier = Modifier
) {
    val earnedStars = when {
        accuracy >= 0.8f -> 3
        accuracy >= 0.5f -> 2
        accuracy > 0f -> 1
        else -> 0
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..3) {
            val isEarned = i <= earnedStars
            Icon(
                painter = AksaIcons.star(),
                contentDescription = "Bintang $i",
                tint = if (isEarned) Color(0xFFFFB300) else Color(0xFFD6DCE2),
                modifier = Modifier.size(if (i == 2) 44.dp else 36.dp)
            )
        }
    }
}