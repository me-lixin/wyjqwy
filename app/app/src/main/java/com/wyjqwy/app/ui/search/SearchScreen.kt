package com.wyjqwy.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
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
import com.wyjqwy.app.ui.category.categoryIconForName
import com.wyjqwy.app.ui.theme.BookColors
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    transactions: List<TransactionItem>,
    amountVisible: Boolean,
    onBack: () -> Unit,
    onEditTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (TransactionItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var amountRangeFilter by remember { mutableStateOf(false) }
    var minAmountText by remember { mutableStateOf("") }
    var maxAmountText by remember { mutableStateOf("") }
    var pendingDeleteTx by remember { mutableStateOf<TransactionItem?>(null) }

    val results = remember(query, amountRangeFilter, minAmountText, maxAmountText, transactions) {
        filterTransactions(
            list = transactions,
            query = query,
            amountRangeFilter = amountRangeFilter,
            minAmountText = minAmountText,
            maxAmountText = maxAmountText
        )
    }

    pendingDeleteTx?.let { tx ->
        AlertDialog(
            onDismissRequest = { pendingDeleteTx = null },
            title = { Text("确认删除明细") },
            text = { Text("删除后不可恢复，确定删除这条明细吗？") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTransaction(tx)
                    pendingDeleteTx = null
                }) { Text("删除", color = BookColors.RedExpense) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTx = null }) { Text("取消") }
            }
        )
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().background(BookColors.BrandTeal)) {
            Row(
                Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "返回",
                        tint = BookColors.TextBlack
                    )
                }
                Text(
                    "搜索",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = BookColors.TextBlack,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索备注、分类或金额...", color = BookColors.TextGray, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = BookColors.TextGray)
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BookColors.BrandTeal,
                    unfocusedBorderColor = BookColors.Line,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("金额范围筛选", color = BookColors.TextBlack, fontSize = 14.sp)
                Switch(
                    checked = amountRangeFilter,
                    onCheckedChange = { amountRangeFilter = it }
                )
            }
            if (amountRangeFilter) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = minAmountText,
                        onValueChange = { minAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("最小金额") }
                    )
                    Text("至", color = BookColors.TextBlack, fontSize = 16.sp)
                    OutlinedTextField(
                        value = maxAmountText,
                        onValueChange = { maxAmountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("最大金额") }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = BookColors.Line)
            if (query.isBlank() && (!amountRangeFilter || (minAmountText.isBlank() && maxAmountText.isBlank()))) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                            tint = BookColors.TextGray.copy(alpha = 0.45f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("输入关键词开始搜索", color = BookColors.TextGray, fontSize = 14.sp)
                    }
                }
            } else if (results.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("无匹配结果", color = BookColors.TextGray)
                }
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(results, key = { it.id }) { tx ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                                    pendingDeleteTx = tx
                                }
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
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { onEditTransaction(tx) }
                                    .padding(horizontal = 4.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SearchCategoryIcon(tx.categoryName)
                                Spacer(Modifier.size(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = if (!tx.note.isNullOrBlank()) tx.note else tx.categoryName,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = formatDateTime(tx.occurredAt),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                }
                                val income = tx.type == 2
                                val prefix = if (income) "+" else "-"
                                val color = if (income) androidx.compose.ui.graphics.Color(0xFF2E7D32) else BookColors.RedExpense
                                Text(
                                    text = if (amountVisible) "$prefix${String.format("%.2f", tx.amount)}" else "****",
                                    color = color,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        HorizontalDivider(color = BookColors.Line)
                    }
                }
            }
        }
    }
}

private fun filterTransactions(
    list: List<TransactionItem>,
    query: String,
    amountRangeFilter: Boolean,
    minAmountText: String,
    maxAmountText: String
): List<TransactionItem> {
    val q = query.trim()
    val minAmount = minAmountText.toDoubleOrNull()
    val maxAmount = maxAmountText.toDoubleOrNull()
    if (q.isEmpty() && (!amountRangeFilter || (minAmount == null && maxAmount == null))) return emptyList()
    return list.filter { tx ->
        val note = tx.note.orEmpty()
        val cat = tx.categoryName
        val keywordOk = if (q.isBlank()) true else {
            note.contains(q, ignoreCase = true) || cat.contains(q, ignoreCase = true)
        }
        if (!keywordOk) return@filter false
        if (amountRangeFilter) {
            val absAmount = abs(tx.amount)
            if (minAmount != null && absAmount < minAmount) return@filter false
            if (maxAmount != null && absAmount > maxAmount) return@filter false
        }
        true
    }
}

@Composable
private fun SearchCategoryIcon(categoryName: String) {
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

private fun formatDateTime(raw: String): String {
    return try {
        val dt = LocalDateTime.parse(raw)
        dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (_: Exception) {
        raw
    }
}
