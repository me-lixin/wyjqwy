package com.wyjqwy.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.category.categoryIconForIconKey
import com.wyjqwy.app.ui.category.categoryIconForName
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import com.wyjqwy.app.ui.util.toAmountText
import java.time.format.DateTimeFormatter

@Composable
internal fun TransactionRow(
    tx: TransactionItem,
    amountVisible: Boolean,
    onClick: () -> Unit,
    onCategoryIconClick: () -> Unit
) {
    val title = if (!tx.note.isNullOrBlank()) tx.note else tx.categoryName
    val timeText = formatOccurredAt(tx.occurredAt)
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(tx.categoryName, tx.categoryIcon, onClick = onCategoryIconClick)
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = timeText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        val income = tx.type == 2
        val prefix = if (income) "+" else "-"
        val color = if (income) androidx.compose.ui.graphics.Color(0xFF2E7D32) else BookColors.RedExpense
        Text(
            text = if (amountVisible) "$prefix${tx.amount.toAmountText()}" else "****",
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CategoryIcon(categoryName: String, iconKey: String?, onClick: () -> Unit) {
    val primaryColor = rememberThemePrimaryColor()
    val icon = categoryIconForIconKey(iconKey).takeIf { iconKey?.isNotBlank() == true } ?: categoryIconForName(categoryName)
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(primaryColor.copy(alpha = 0.18f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = primaryColor, modifier = Modifier.size(20.dp))
    }
}

private fun formatOccurredAt(raw: String): String {
    return try {
        val dt = java.time.LocalDateTime.parse(raw)
        dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        raw
    }
}

