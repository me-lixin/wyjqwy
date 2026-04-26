package com.wyjqwy.app.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object PrintTokens {
    val cardRadius: Dp = 12.dp
    val cardShadow: Dp = 6.dp
    val cardShadowSoft: Dp = 3.dp
    val iconDecorSize: Dp = 40.dp
}

fun Modifier.printCardShadow(
    radius: Dp = PrintTokens.cardRadius,
    elevation: Dp = PrintTokens.cardShadow
): Modifier {
    return this
        .shadow(elevation = elevation, shape = RoundedCornerShape(radius), clip = false)
        .clip(RoundedCornerShape(radius))
}

@Composable
fun DecoratedThemeIcon(
    icon: ImageVector,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = PrintTokens.iconDecorSize
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(primaryColor.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = size.toPx() / 4f
            var start = -size.toPx()
            while (start < size.toPx() * 2f) {
                drawLine(
                    color = primaryColor.copy(alpha = 0.10f),
                    start = Offset(start, 0f),
                    end = Offset(start + size.toPx(), size.toPx()),
                    strokeWidth = 1.dp.toPx()
                )
                start += step
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primaryColor
        )
    }
}
