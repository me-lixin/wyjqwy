package com.wyjqwy.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.category.categoryIconForName
import com.wyjqwy.app.ui.theme.BookColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

private enum class CategorySortMode(val label: String) {
    TIME_DESC("时间↓"),
    TIME_ASC("时间↑"),
    AMOUNT_DESC("金额↓"),
    AMOUNT_ASC("金额↑")
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CategoryStatsScreen(
    state: AppUiState,
    vm: AppViewModel,
    seedTx: TransactionItem,
    onBack: () -> Unit,
    onEditTransaction: (TransactionItem) -> Unit
) {
    var sortMode by remember { mutableStateOf(CategorySortMode.TIME_DESC) }
    var pendingDeleteTx by remember { mutableStateOf<TransactionItem?>(null) }

    val txList = remember(state.transactions, seedTx, sortMode) {
        state.transactions
            .filter { it.categoryId == seedTx.categoryId || it.categoryName == seedTx.categoryName }
            .sortedWith(
                when (sortMode) {
                    CategorySortMode.TIME_DESC -> compareByDescending<TransactionItem> { it.parsedOccurredAt ?: parseTimeOrMin(it.occurredAt) }
                        .thenByDescending { it.id }
                    CategorySortMode.TIME_ASC -> compareBy<TransactionItem> { it.parsedOccurredAt ?: parseTimeOrMin(it.occurredAt) }
                        .thenBy { it.id }
                    CategorySortMode.AMOUNT_DESC -> compareByDescending<TransactionItem> { abs(it.amount) }
                        .thenByDescending { it.parsedOccurredAt ?: parseTimeOrMin(it.occurredAt) }
                    CategorySortMode.AMOUNT_ASC -> compareBy<TransactionItem> { abs(it.amount) }
                        .thenByDescending { it.parsedOccurredAt ?: parseTimeOrMin(it.occurredAt) }
                }
            )
    }
    val totalAmount = txList.sumOf { abs(it.amount) }
    val avgAmount = if (txList.isEmpty()) 0.0 else totalAmount / txList.size

    pendingDeleteTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { pendingDeleteTx = null },
            title = { Text("确认删除明细") },
            text = { Text("删除后不可恢复，确定删除这条明细吗？") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteTransaction(tx.id)
                    pendingDeleteTx = null
                }) { Text("删除", color = BookColors.RedExpense) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTx = null }) { Text("取消") }
            }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 顶部工具栏：返回箭头旁改为“账单明细”
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookColors.BrandTeal)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回", tint = BookColors.White)
            }
        }

        // 统计卡片
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(BookColors.White)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column {
                // 卡片标题改为当前分类名称
                Text(
                    text = seedTx.categoryName,
                    color = BookColors.TextBlack,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
                Spacer(Modifier.size(16.dp))
                // 优化 Y 轴对齐：使用 Alignment.Bottom 确保数字基线对齐
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    StatCell(value = "${txList.size}笔", label = "总笔数", modifier = Modifier.weight(1f))
                    StatCell(value = compactAmount(totalAmount), label = "总金额", modifier = Modifier.weight(1f))
                    StatCell(value = "¥${String.format("%.2f", avgAmount)}", label = "平均金额", modifier = Modifier.weight(1.2f))
                }
            }
        }

        // 排序筛选栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Outlined.Sort, contentDescription = null, tint = BookColors.TextGray, modifier = Modifier.size(16.dp))
            Text("排序", color = BookColors.TextGray, fontSize = 14.sp)
            CategorySortMode.entries.forEach { mode ->
                AssistChip(
                    onClick = { sortMode = mode },
                    label = { Text(mode.label) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (sortMode == mode) BookColors.BrandTeal else BookColors.White,
                        labelColor = if (sortMode == mode) BookColors.White else BookColors.TextBlack
                    )
                )
            }
        }

        // 明细列表
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(txList, key = { it.id }) { tx ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value != SwipeToDismissBoxValue.Settled) pendingDeleteTx = tx
                        false
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        if (dismissState.progress > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(BookColors.RedExpense.copy(alpha = 0.12f))
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "删除明细",
                                    tint = BookColors.RedExpense
                                )
                            }
                        }
                    }
                ) {
                    Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatDateWithWeekday(tx.occurredAt),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (tx.type == 1) "支出 ${abs(tx.amount).trimZeros()}" else "收入 ${abs(tx.amount).trimZeros()}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onEditTransaction(tx) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIcon(tx.categoryName)
                            Spacer(Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (!tx.note.isNullOrBlank()) tx.note else tx.categoryName,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                )
                                Spacer(Modifier.size(2.dp))
                                Text(
                                    text = formatTime(tx.occurredAt),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = if (state.amountVisible) {
                                    val prefix = if (tx.type == 2) "+" else "-"
                                    "$prefix${abs(tx.amount).trimZeros()}"
                                } else "****",
                                color = if (tx.type == 2) androidx.compose.ui.graphics.Color(0xFF2E7D32) else BookColors.RedExpense,
                                fontSize = 12.sp, // 稍微缩小了列表金额字号，避免压迫感
                                fontWeight = FontWeight.Medium
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 60.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = value,
            color = BookColors.BrandTeal,
            fontSize = 22.sp, // 统一下调至 22sp，更符合阅读习惯
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = label,
            color = BookColors.TextGray,
            fontSize = 12.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun CategoryIcon(categoryName: String) {
    val icon = categoryIconForName(categoryName)
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(BookColors.BrandTealIconBg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = BookColors.BrandTeal, modifier = Modifier.size(20.dp))
    }
}

private fun formatDateWithWeekday(raw: String): String {
    return try {
        val dt = LocalDateTime.parse(raw)
        val date = dt.toLocalDate()
        val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)
        "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)} $weekday"
    } catch (_: Exception) {
        raw
    }
}

private fun formatTime(raw: String): String {
    return try {
        val dt = LocalDateTime.parse(raw)
        dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        raw
    }
}

private fun parseTimeOrMin(raw: String): LocalDateTime {
    return try {
        LocalDateTime.parse(raw)
    } catch (_: Exception) {
        LocalDateTime.MIN
    }
}

private fun compactAmount(v: Double): String {
    return if (v >= 1000) {
        val k = v / 1000.0
        "¥${String.format("%.1f", k)}k" // 优化了“千”的显示，用 k 更简洁
    } else {
        "¥${String.format("%.2f", v)}"
    }
}

private fun Double.trimZeros(): String {
    val s = String.format("%.2f", this)
    return s.trimEnd('0').trimEnd('.')
}