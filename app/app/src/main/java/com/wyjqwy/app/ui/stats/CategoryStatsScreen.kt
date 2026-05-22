package com.wyjqwy.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.category.categoryIconForIconKey
import com.wyjqwy.app.ui.category.categoryIconForName
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import com.wyjqwy.app.ui.theme.themeColors
import com.wyjqwy.app.ui.util.toAmountText
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class CategorySortMode(val label: String) {
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
    sortMode: CategorySortMode = CategorySortMode.TIME_DESC,
    onSortModeChange: (CategorySortMode) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    scopedTransactions: List<TransactionItem>? = null,
    loading: Boolean = false,
    onBack: () -> Unit,
    onEditTransaction: (TransactionItem) -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val tc = themeColors()
    var pendingDeleteTx by remember { mutableStateOf<TransactionItem?>(null) }
    
    // 🌟 核心：引入一个滚动触发计数器
    var scrollTrigger by remember { mutableIntStateOf(0) }

    // 🌟 只有当 scrollTrigger 增加时（即手动点击了排序），才执行回顶
    LaunchedEffect(scrollTrigger) {
        if (scrollTrigger > 0) {
            // scrollToItem(0) 是瞬间回顶，animateScrollToItem(0) 是平滑滚动
            listState.scrollToItem(0)
        }
    }

    val source = scopedTransactions ?: state.transactions
    val txList = remember(source, seedTx, sortMode) {
        source
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
                }) { Text("删除", color = tc.expense) }
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
        // 顶部工具栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回", tint = tc.textPrimary)
            }
            Text(
                text = "分类汇总",
                color = tc.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // 统计卡片
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tc.surface)
                .padding(horizontal = 16.dp, vertical = 18.dp)
        ) {
            Column {
                Text(
                    text = seedTx.categoryName,
                    color = tc.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
                Spacer(Modifier.size(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    StatCell(value = "${txList.size}笔", label = "总笔数", modifier = Modifier.weight(1f))
                    StatCell(value = "¥${totalAmount.toAmountText()}", label = "总金额", modifier = Modifier.weight(1f))
                    StatCell(value = "¥${avgAmount.toAmountText()}", label = "平均金额", modifier = Modifier.weight(1.2f))
                }
            }
        }

        // 排序筛选栏
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Sort, null, tint = tc.textSecondary, modifier = Modifier.size(16.dp))
                Text("排序", color = tc.textSecondary, fontSize = 12.sp)
            }
            SortChipWrap(
                sortMode = sortMode,
                onSelect = { newMode ->
                    // 🌟 只有模式真的改变时才处理
                    if (sortMode != newMode) {
                        onSortModeChange(newMode)
                        // 🌟 关键：手动操作时增加计数器，触发顶部的 LaunchedEffect
                        scrollTrigger++ 
                    }
                },
                primaryColor = primaryColor
            )
        }

        if (loading) {
            Box(Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        } else {
            LazyColumn(
                state = listState, // 🌟 必须绑定外部传入的 listState
                modifier = Modifier.fillMaxSize()
            ) {
                // 🌟 使用 key = { it.id } 对保持位置非常重要
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
                                    modifier = Modifier.fillMaxSize().background(tc.expense.copy(alpha = 0.12f)).padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Outlined.DeleteOutline, "删除", tint = tc.expense)
                                }
                            }
                        }
                    ) {
                        Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(formatDateWithWeekday(tx.occurredAt), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                                Text(
                                    text = if (tx.type == 1) "支出 ${abs(tx.amount).toAmountText()}" else "收入 ${abs(tx.amount).toAmountText()}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                            Row(
                                Modifier.fillMaxWidth().clickable { onEditTransaction(tx) }.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryIcon(tx.categoryName, tx.categoryIcon)
                                Spacer(Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(if (!tx.note.isNullOrBlank()) tx.note else tx.categoryName, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                                    Spacer(Modifier.size(2.dp))
                                    Text(formatTime(tx.occurredAt), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                }
                                Text(
                                    text = if (state.amountVisible) {
                                        val prefix = if (tx.type == 2) "+" else "-"
                                        "$prefix${abs(tx.amount).toAmountText()}"
                                    } else "****",
                                    color = if (tx.type == 2) tc.income else tc.expense,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 0.5.dp, modifier = Modifier.padding(start = 60.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SortChipWrap(
    sortMode: CategorySortMode,
    onSelect: (CategorySortMode) -> Unit,
    primaryColor: androidx.compose.ui.graphics.Color
) {
    val tc = themeColors()
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CategorySortMode.entries.forEach { mode ->
            AssistChip(
                onClick = { onSelect(mode) },
                label = { Box(Modifier.fillMaxWidth(), Alignment.Center) { Text(mode.label, fontSize = 11.sp, maxLines = 1) } },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (sortMode == mode) primaryColor else tc.surface,
                    labelColor = if (sortMode == mode) Color.White else tc.textPrimary
                ),
                border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = if (sortMode == mode) primaryColor else tc.textSecondary.copy(alpha = 0.2f), borderWidth = 1.dp),
                modifier = Modifier.weight(1f).height(34.dp)
            )
        }
    }
}

// 其余辅助函数 (StatCell, CategoryIcon, formatDateWithWeekday, formatTime, parseTimeOrMin) 保持不变
@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    val primaryColor = rememberThemePrimaryColor()
    val tc = themeColors()
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(text = value, color = primaryColor, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 20.sp)
        Spacer(Modifier.size(4.dp))
        Text(text = label, color = tc.textSecondary, fontSize = 12.sp, lineHeight = 14.sp)
    }
}

@Composable
private fun CategoryIcon(categoryName: String, iconKey: String?) {
    val primaryColor = rememberThemePrimaryColor()
    val icon = categoryIconForIconKey(iconKey).takeIf { !iconKey.isNullOrBlank() } ?: categoryIconForName(categoryName)
    Box(Modifier.size(36.dp).clip(CircleShape).background(primaryColor.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = primaryColor, modifier = Modifier.size(20.dp))
    }
}

private fun formatDateWithWeekday(raw: String): String {
    return try {
        val dt = LocalDateTime.parse(raw)
        val date = dt.toLocalDate()
        val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)
        "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)} $weekday"
    } catch (_: Exception) { raw }
}

private fun formatTime(raw: String): String {
    return try {
        val dt = LocalDateTime.parse(raw)
        dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) { raw }
}

private fun parseTimeOrMin(raw: String): LocalDateTime {
    return try { LocalDateTime.parse(raw) } catch (_: Exception) { LocalDateTime.MIN }
}