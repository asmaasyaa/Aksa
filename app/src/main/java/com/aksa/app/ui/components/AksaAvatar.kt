package com.aksa.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aksa.app.ui.state.AksaAvatarItem
import com.aksa.app.ui.state.AksaUserState
import com.aksa.app.ui.theme.AksaBlue
import com.aksa.app.ui.theme.AksaBlueDark
import com.aksa.app.ui.theme.AksaDarkBrown
import com.aksa.app.ui.theme.AksaIcons
import com.aksa.app.ui.theme.AksaLime
import com.aksa.app.ui.theme.AksaLimeDark
import com.aksa.app.ui.theme.AksaOrange
import com.aksa.app.ui.theme.AksaOrangeLight
import com.aksa.app.ui.theme.AksaPeachDark
import com.aksa.app.ui.theme.AksaPeachLight
import com.aksa.app.ui.theme.AksaSoftPeach
import com.aksa.app.ui.theme.AksaWhite
import com.aksa.app.ui.theme.AksaYellow
import com.aksa.app.ui.theme.AksaYellowDark
import com.aksa.app.ui.theme.AksaYellowLight
import com.aksa.app.ui.theme.Poppins

@Composable
fun AksaAvatar(
    avatarId: String,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        when (avatarId) {
            "mascot_champion" -> drawAvatarChampion(w, h)
            "mascot_happy" -> drawAvatarHappy(w, h)
            "cat_orange" -> drawAvatarCat(w, h)
            "bunny_blue" -> drawAvatarBunny(w, h)
            "dino_lime" -> drawAvatarDino(w, h)
            "mascot_classic" -> drawAvatarClassic(w, h)
            else -> drawAvatarClassic(w, h)
        }
    }
}


@Composable
fun AksaAvatarSelector(
    selectedAvatarId: String,
    onAvatarSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = "Pilih Avatar"
) {
    val avatars = AksaUserState.availableAvatars

    Column(modifier = modifier.fillMaxWidth()) {
        if (!title.isNullOrBlank()) {
            Text(
                text = title,
                fontFamily = Poppins,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = AksaDarkBrown,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }

        // 3x2 Grid of Avatars
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (row in 0..1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (col in 0..2) {
                        val index = row * 3 + col
                        if (index < avatars.size) {
                            val item = avatars[index]
                            val isSelected = item.id == selectedAvatarId

                            AvatarOptionCard(
                                item = item,
                                isSelected = isSelected,
                                onClick = { onAvatarSelected(item.id) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarOptionCard(
    item: AksaAvatarItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        label = "AvatarCardScale"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) AksaYellowLight else AksaPeachLight,
        label = "AvatarCardBg"
    )

    val borderColor = if (isSelected) AksaOrange else Color.Transparent

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(bgColor)
            .border(if (isSelected) 2.5.dp else 1.dp, borderColor, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(AksaWhite),
                contentAlignment = Alignment.Center
            ) {
                AksaAvatar(
                    avatarId = item.id,
                    size = 46.dp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.name,
                fontFamily = Poppins,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                color = AksaDarkBrown
            )
        }

        // Selection Checkmark Badge
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(AksaOrange)
                    .align(Alignment.TopEnd)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AksaIcons.Check,
                    contentDescription = "Terpilih",
                    tint = AksaWhite,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}


private fun DrawScope.drawAvatarClassic(w: Float, h: Float) {
    val cx = w / 2f
    val cy = h * 0.54f
    val r = minOf(w, h) * 0.36f

    val earR = r * 0.35f
    val earY = cy - r * 0.82f
    val earSpacing = r * 0.72f

    drawCircle(AksaWhite, earR, Offset(cx - earSpacing, earY))
    drawCircle(AksaSoftPeach, earR * 0.55f, Offset(cx - earSpacing, earY))
    drawCircle(AksaWhite, earR, Offset(cx + earSpacing, earY))
    drawCircle(AksaSoftPeach, earR * 0.55f, Offset(cx + earSpacing, earY))

    val headPath = Path().apply {
        val rect = Rect(cx - r, cy - r, cx + r, cy + r * 1.05f)
        addRoundRect(RoundRect(rect, CornerRadius(r * 0.95f, r * 0.95f)))
    }
    drawPath(headPath, AksaWhite)

    drawAvatarFace(cx, cy, r)
}

private fun DrawScope.drawAvatarChampion(w: Float, h: Float) {
    val cx = w / 2f
    val cy = h * 0.54f
    val r = minOf(w, h) * 0.36f
    val earR = r * 0.35f
    val earY = cy - r * 0.82f
    val earSpacing = r * 0.72f

    drawCircle(AksaWhite, earR, Offset(cx - earSpacing, earY))
    drawCircle(AksaSoftPeach, earR * 0.55f, Offset(cx - earSpacing, earY))
    drawCircle(AksaWhite, earR, Offset(cx + earSpacing, earY))
    drawCircle(AksaSoftPeach, earR * 0.55f, Offset(cx + earSpacing, earY))

    val headPath = Path().apply {
        val rect = Rect(cx - r, cy - r, cx + r, cy + r * 1.05f)
        addRoundRect(RoundRect(rect, CornerRadius(r * 0.95f, r * 0.95f)))
    }
    drawPath(headPath, AksaWhite)

    val bandY = cy - r * 0.55f
    val bandH = r * 0.28f
    drawRoundRect(
        color = AksaYellow,
        topLeft = Offset(cx - r * 0.95f, bandY),
        size = Size(r * 1.9f, bandH),
        cornerRadius = CornerRadius(bandH * 0.3f, bandH * 0.3f)
    )
    drawCircle(AksaYellowDark, r * 0.16f, Offset(cx + r * 0.85f, bandY + bandH * 0.5f))
    drawCircle(AksaYellow, r * 0.12f, Offset(cx + r * 0.95f, bandY + bandH * 0.2f))

    drawAvatarFace(cx, cy + r * 0.08f, r)
}


private fun DrawScope.drawAvatarHappy(w: Float, h: Float) {
    val cx = w / 2f
    val cy = h * 0.54f
    val r = minOf(w, h) * 0.36f

    val earR = r * 0.35f
    val earY = cy - r * 0.82f
    val earSpacing = r * 0.72f

    drawCircle(AksaWhite, earR, Offset(cx - earSpacing, earY))
    drawCircle(AksaSoftPeach, earR * 0.55f, Offset(cx - earSpacing, earY))
    drawCircle(AksaWhite, earR, Offset(cx + earSpacing, earY))
    drawCircle(AksaSoftPeach, earR * 0.55f, Offset(cx + earSpacing, earY))

    val headPath = Path().apply {
        val rect = Rect(cx - r, cy - r, cx + r, cy + r * 1.05f)
        addRoundRect(RoundRect(rect, CornerRadius(r * 0.95f, r * 0.95f)))
    }
    drawPath(headPath, AksaWhite)

    val eyeSpacing = r * 0.36f
    val eyeY = cy - r * 0.1f
    val eyeW = r * 0.28f

    drawArc(
        color = AksaDarkBrown,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx - eyeSpacing - eyeW / 2f, eyeY - eyeW * 0.4f),
        size = Size(eyeW, eyeW * 0.8f),
        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
    )

    drawArc(
        color = AksaDarkBrown,
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx + eyeSpacing - eyeW / 2f, eyeY - eyeW * 0.4f),
        size = Size(eyeW, eyeW * 0.8f),
        style = Stroke(width = 3.5f, cap = StrokeCap.Round)
    )

    drawCircle(AksaSoftPeach, r * 0.22f, Offset(cx - r * 0.54f, cy + r * 0.14f))
    drawCircle(AksaSoftPeach, r * 0.22f, Offset(cx + r * 0.54f, cy + r * 0.14f))

    val mouthPath = Path().apply {
        moveTo(cx - r * 0.18f, cy + r * 0.12f)
        quadraticTo(cx, cy + r * 0.36f, cx + r * 0.18f, cy + r * 0.12f)
        close()
    }
    drawPath(mouthPath, AksaOrange)

    drawCircle(AksaYellow, r * 0.16f, Offset(cx + r * 0.72f, cy - r * 0.2f))
}


private fun DrawScope.drawAvatarCat(w: Float, h: Float) {
    val cx = w / 2f
    val cy = h * 0.54f
    val r = minOf(w, h) * 0.36f

    val leftEar = Path().apply {
        moveTo(cx - r * 0.85f, cy - r * 0.3f)
        lineTo(cx - r * 0.75f, cy - r * 1.15f)
        lineTo(cx - r * 0.2f, cy - r * 0.7f)
        close()
    }
    drawPath(leftEar, AksaOrangeLight)

    val leftInnerEar = Path().apply {
        moveTo(cx - r * 0.8f, cy - r * 0.4f)
        lineTo(cx - r * 0.73f, cy - r * 1.02f)
        lineTo(cx - r * 0.3f, cy - r * 0.68f)
        close()
    }
    drawPath(leftInnerEar, AksaSoftPeach)

    val rightEar = Path().apply {
        moveTo(cx + r * 0.85f, cy - r * 0.3f)
        lineTo(cx + r * 0.75f, cy - r * 1.15f)
        lineTo(cx + r * 0.2f, cy - r * 0.7f)
        close()
    }
    drawPath(rightEar, AksaOrangeLight)

    val rightInnerEar = Path().apply {
        moveTo(cx + r * 0.8f, cy - r * 0.4f)
        lineTo(cx + r * 0.73f, cy - r * 1.02f)
        lineTo(cx + r * 0.3f, cy - r * 0.68f)
        close()
    }
    drawPath(rightInnerEar, AksaSoftPeach)

    drawCircle(AksaOrangeLight, r, Offset(cx, cy))

    drawOval(
        color = AksaWhite,
        topLeft = Offset(cx - r * 0.45f, cy),
        size = Size(r * 0.9f, r * 0.65f)
    )

    val eyeW = r * 0.14f
    val eyeH = r * 0.24f
    val eyeSpacing = r * 0.36f
    val eyeY = cy - r * 0.12f

    drawOval(AksaDarkBrown, Offset(cx - eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH))
    drawCircle(AksaWhite, eyeW * 0.28f, Offset(cx - eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f))
    drawOval(AksaDarkBrown, Offset(cx + eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH))
    drawCircle(AksaWhite, eyeW * 0.28f, Offset(cx + eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f))

    drawCircle(AksaSoftPeach, r * 0.18f, Offset(cx - r * 0.58f, cy + r * 0.14f))
    drawCircle(AksaSoftPeach, r * 0.18f, Offset(cx + r * 0.58f, cy + r * 0.14f))

    val nosePath = Path().apply {
        moveTo(cx - r * 0.1f, cy + r * 0.12f)
        lineTo(cx + r * 0.1f, cy + r * 0.12f)
        lineTo(cx, cy + r * 0.22f)
        close()
    }
    drawPath(nosePath, AksaOrange)

    drawLine(AksaDarkBrown.copy(alpha = 0.5f), Offset(cx - r * 0.4f, cy + r * 0.2f), Offset(cx - r * 0.75f, cy + r * 0.16f), 2.5f, StrokeCap.Round)
    drawLine(AksaDarkBrown.copy(alpha = 0.5f), Offset(cx - r * 0.4f, cy + r * 0.28f), Offset(cx - r * 0.75f, cy + r * 0.32f), 2.5f, StrokeCap.Round)
    drawLine(AksaDarkBrown.copy(alpha = 0.5f), Offset(cx + r * 0.4f, cy + r * 0.2f), Offset(cx + r * 0.75f, cy + r * 0.16f), 2.5f, StrokeCap.Round)
    drawLine(AksaDarkBrown.copy(alpha = 0.5f), Offset(cx + r * 0.4f, cy + r * 0.28f), Offset(cx + r * 0.75f, cy + r * 0.32f), 2.5f, StrokeCap.Round)
}


private fun DrawScope.drawAvatarBunny(w: Float, h: Float) {
    val cx = w / 2f
    val cy = h * 0.58f
    val r = minOf(w, h) * 0.34f

    val earW = r * 0.34f
    val earH = r * 0.95f
    val earSpacing = r * 0.45f
    val earY = cy - r * 1.45f

    drawRoundRect(
        color = AksaBlueDark,
        topLeft = Offset(cx - earSpacing - earW / 2f, earY),
        size = Size(earW, earH),
        cornerRadius = CornerRadius(earW / 2f, earW / 2f)
    )
    drawRoundRect(
        color = AksaSoftPeach,
        topLeft = Offset(cx - earSpacing - earW * 0.3f, earY + earH * 0.15f),
        size = Size(earW * 0.6f, earH * 0.7f),
        cornerRadius = CornerRadius(earW * 0.3f, earW * 0.3f)
    )

    drawRoundRect(
        color = AksaBlueDark,
        topLeft = Offset(cx + earSpacing - earW / 2f, earY),
        size = Size(earW, earH),
        cornerRadius = CornerRadius(earW / 2f, earW / 2f)
    )
    drawRoundRect(
        color = AksaSoftPeach,
        topLeft = Offset(cx + earSpacing - earW * 0.3f, earY + earH * 0.15f),
        size = Size(earW * 0.6f, earH * 0.7f),
        cornerRadius = CornerRadius(earW * 0.3f, earW * 0.3f)
    )

    drawCircle(AksaBlueDark, r, Offset(cx, cy))

    drawCircle(AksaSoftPeach, r * 0.24f, Offset(cx - r * 0.54f, cy + r * 0.14f))
    drawCircle(AksaSoftPeach, r * 0.24f, Offset(cx + r * 0.54f, cy + r * 0.14f))

    val eyeW = r * 0.14f
    val eyeH = r * 0.24f
    val eyeSpacing = r * 0.34f
    val eyeY = cy - r * 0.10f

    drawOval(AksaDarkBrown, Offset(cx - eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH))
    drawCircle(AksaWhite, eyeW * 0.28f, Offset(cx - eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f))
    drawOval(AksaDarkBrown, Offset(cx + eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH))
    drawCircle(AksaWhite, eyeW * 0.28f, Offset(cx + eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f))

    drawCircle(AksaOrange, r * 0.08f, Offset(cx, cy + r * 0.14f))
    drawArc(
        color = AksaDarkBrown,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx - r * 0.12f, cy + r * 0.16f),
        size = Size(r * 0.24f, r * 0.16f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawAvatarDino(w: Float, h: Float) {
    val cx = w / 2f
    val cy = h * 0.54f
    val r = minOf(w, h) * 0.36f

    drawCircle(AksaYellow, r * 0.2f, Offset(cx - r * 0.45f, cy - r * 0.95f))
    drawCircle(AksaYellow, r * 0.22f, Offset(cx, cy - r * 1.05f))
    drawCircle(AksaYellow, r * 0.2f, Offset(cx + r * 0.45f, cy - r * 0.95f))
    drawCircle(AksaLime, r, Offset(cx, cy))

    drawCircle(AksaPeachDark, r * 0.2f, Offset(cx - r * 0.54f, cy + r * 0.16f))
    drawCircle(AksaPeachDark, r * 0.2f, Offset(cx + r * 0.54f, cy + r * 0.16f))

    val eyeW = r * 0.14f
    val eyeH = r * 0.24f
    val eyeSpacing = r * 0.36f
    val eyeY = cy - r * 0.08f

    drawOval(AksaDarkBrown, Offset(cx - eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH))
    drawCircle(AksaWhite, eyeW * 0.28f, Offset(cx - eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f))
    drawOval(AksaDarkBrown, Offset(cx + eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH))
    drawCircle(AksaWhite, eyeW * 0.28f, Offset(cx + eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f))

    val mouthPath = Path().apply {
        moveTo(cx - r * 0.18f, cy + r * 0.18f)
        quadraticTo(cx, cy + r * 0.38f, cx + r * 0.18f, cy + r * 0.18f)
    }
    drawPath(mouthPath, AksaDarkBrown, style = Stroke(width = 3.5f, cap = StrokeCap.Round))

    val toothPath = Path().apply {
        moveTo(cx - r * 0.08f, cy + r * 0.24f)
        lineTo(cx - r * 0.02f, cy + r * 0.32f)
        lineTo(cx + r * 0.04f, cy + r * 0.24f)
        close()
    }
    drawPath(toothPath, AksaWhite)
}

private fun DrawScope.drawAvatarFace(cx: Float, cy: Float, r: Float) {
    val eyeW = r * 0.14f
    val eyeH = r * 0.24f
    val eyeSpacing = r * 0.36f
    val eyeY = cy - r * 0.12f

    drawOval(AksaDarkBrown, Offset(cx - eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH))
    drawCircle(AksaWhite, eyeW * 0.28f, Offset(cx - eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f))
    drawOval(AksaDarkBrown, Offset(cx + eyeSpacing - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH))
    drawCircle(AksaWhite, eyeW * 0.28f, Offset(cx + eyeSpacing - eyeW * 0.1f, eyeY - eyeH * 0.22f))

    drawCircle(AksaSoftPeach, r * 0.22f, Offset(cx - r * 0.54f, cy + r * 0.14f))
    drawCircle(AksaSoftPeach, r * 0.22f, Offset(cx + r * 0.54f, cy + r * 0.14f))

    val mouthPath = Path().apply {
        moveTo(cx - r * 0.14f, cy + r * 0.16f)
        quadraticTo(cx, cy + r * 0.28f, cx + r * 0.14f, cy + r * 0.16f)
    }
    drawPath(mouthPath, AksaDarkBrown, style = Stroke(width = 3f, cap = StrokeCap.Round))
}
