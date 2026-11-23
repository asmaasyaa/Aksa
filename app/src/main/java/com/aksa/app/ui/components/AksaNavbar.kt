package com.aksa.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import kotlin.math.roundToInt

@Composable
fun AksaNavbar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val barShape = RoundedCornerShape(32.dp)

    var rowWidthPx by remember { mutableStateOf(0f) }
    var dragOffset by remember { mutableStateOf(0f) }

    val indicatorOffsetPx by animateFloatAsState(
        targetValue = if (selectedTab == 0) 0f else rowWidthPx / 2f,
        animationSpec = spring(
            dampingRatio = 0.5f, 
            stiffness = Spring.StiffnessLow 
        ),
        label = "indicatorOffset"
    )

    Box(
        modifier = modifier
            .shadow(elevation = 14.dp, shape = barShape, spotColor = Color(0x44441100))
            .background(AksaWhite, barShape)
            .border(2.dp, AksaSoftPeach, barShape)
            .padding(8.dp)
            .height(40.dp)
            .width(160.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        dragOffset = 0f
                    },
                    onDragEnd = {
                        if (dragOffset > 40 && selectedTab == 0) {
                            onTabSelected(1)
                        } else if (dragOffset < -40 && selectedTab == 1) {
                            onTabSelected(0)
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(indicatorOffsetPx.roundToInt(), 0) }
                .fillMaxHeight()
                .width(with(LocalDensity.current) { (rowWidthPx / 2f).toDp() })
                .background(AksaOrange, RoundedCornerShape(24.dp))
        )

        Row(
            modifier = Modifier
                .matchParentSize()
                .onSizeChanged { rowWidthPx = it.width.toFloat() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TabIcon(
                icon = AksaIcons.map(),
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )

            TabIcon(
                icon = AksaIcons.person(),
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabIcon(
    icon: Painter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) AksaWhite else AksaDarkBrown.copy(alpha = 0.5f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "iconColor"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Deprecated("Use AksaNavbar instead", ReplaceWith("AksaNavbar(selectedTab, onTabSelected, modifier)"))
@Composable
fun MinimalAksaBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) = AksaNavbar(selectedTab, onTabSelected, modifier)