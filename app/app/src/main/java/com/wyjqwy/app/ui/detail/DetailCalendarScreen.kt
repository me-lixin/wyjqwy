package com.wyjqwy.app.ui.detail

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
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.category.categoryIconForIconKey
import com.wyjqwy.app.ui.category.categoryIconForName
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.SubPageTopBar
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

private data class CalendarCell(
    val date: LocalDate,
    val inCurrentMonth: Boolean,
    val incomeAmount: Double,
    val expenseAmount: Double
)

@Composable
fun DetailCalendarScreen(
    state: AppUiState,
    onLoadMonth: (YearMonth) -> Unit,
    onBack: () -> Unit,
    onEditTransaction: (TransactionItem) -> Unit,
    onOpenCategoryStats: (TransactionItem) -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    var showingMonth by remember { mutableStateOf(state.selectedYearMonth) }
    val today = remember { LocalDate.now() }

    val txByDate = remember(state.transactions) {
        state.transactions.groupBy { it.parsedOccurredAt?.toLocalDate() ?: LocalDate.now() }
    }
    val daySummaryByDate = remember(txByDate) {
        txByDate.mapValues { (_, list) ->
            val income = list.filter { it.type == 2 }.sumOf { abs(it.amount) }
            val expense = list.filter { it.type == 1 }.sumOf { abs(it.amount) }
            income to expense
        }
    }
    val cells = remember(showingMonth, daySummaryByDate) { buildCalendarCells(showingMonth, daySummaryByDate) }
    var selectedDate by remember(showingMonth, txByDate) {
        mutableStateOf(
            txByDate.keys
                .filter {
                    YearMonth.from(it) == showingMonth && (
                        (daySummaryByDate[it]?.first ?: 0.0) > 0.0 ||
                            (daySummaryByDate[it]?.second ?: 0.0) > 0.0
                        )
                }
                .maxOrNull()
        )
    }
    val selectedList = remember(selectedDate, txByDate) {
        txByDate[selectedDate].orEmpty().sortedByDescending { it.parsedOccurredAt ?: LocalDate.MIN.atStartOfDay() }
    }
    androidx.compose.runtime.LaunchedEffect(showingMonth) {
        onLoadMonth(showingMonth)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            SubPageTopBar(
                title = "日历",
                onBack = onBack,
                trailingContent = {
                    Text(
                        text = "今天",
                        color = BookColors.TextBlack,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable {
                            showingMonth = YearMonth.now()
                            selectedDate = LocalDate.now()
                        }
                    )
                }
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BookColors.White)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { showingMonth = showingMonth.minusMonths(1) }) {
                                Icon(Icons.Outlined.KeyboardArrowLeft, contentDescription = "上个月", tint = primaryColor)
                            }
                            Text(
                                text = "${showingMonth.year}年${showingMonth.monthValue}月",
                                style = MaterialTheme.typography.titleLarge,
                                color = BookColors.TextBlack
                            )
                            IconButton(onClick = { showingMonth = showingMonth.plusMonths(1) }) {
                                Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = "下个月", tint = primaryColor)
                            }
                        }
                        Spacer(Modifier.size(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日").forEach { w ->
                                Text(text = w, color = BookColors.TextGray, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.size(6.dp))
                        cells.chunked(7).forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                week.forEach { cell ->
                                    val hasAmount = cell.incomeAmount > 0.0 || cell.expenseAmount > 0.0
                                    val selected = selectedDate == cell.date
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 6.dp)
                                            .clickable(enabled = hasAmount) { selectedDate = cell.date },
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .background(
                                                    when {
                                                        selected -> primaryColor
                                                        cell.date == today -> primaryColor.copy(alpha = 0.18f)
                                                        else -> BookColors.White
                                                    },
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = cell.date.dayOfMonth.toString(),
                                                color = when {
                                                    selected -> BookColors.White
                                                    cell.inCurrentMonth -> BookColors.TextBlack
                                                    else -> BookColors.TextGray.copy(alpha = 0.4f)
                                                },
                                                fontSize = 16.sp
                                            )
                                        }
                                        Spacer(Modifier.size(2.dp))
                                        Text(
                                            text = if (cell.incomeAmount > 0.0) "收 ${formatCompactAmount(cell.incomeAmount)}" else "",
                                            color = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = if (cell.expenseAmount > 0.0) "支 ${formatCompactAmount(cell.expenseAmount)}" else "",
                                            color = BookColors.RedExpense,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (selectedList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("该日期暂无明细", color = BookColors.TextGray)
                    }
                }
            } else {
                items(selectedList, key = { it.id }) { tx ->
                    CalendarTransactionRow(
                        tx = tx,
                        amountVisible = state.amountVisible,
                        onEdit = { onEditTransaction(tx) },
                        onCategoryClick = { onOpenCategoryStats(tx) }
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(start = 60.dp)
                    )
                }
            }
            item { Spacer(Modifier.size(12.dp)) }
        }
    }
}

@Composable
private fun CalendarTransactionRow(
    tx: TransactionItem,
    amountVisible: Boolean,
    onEdit: () -> Unit,
    onCategoryClick: () -> Unit
) {
    val title = if (!tx.note.isNullOrBlank()) tx.note else tx.categoryName
    val timeText = tx.parsedOccurredAt?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: tx.occurredAt
    val primaryColor = rememberThemePrimaryColor()
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onEdit)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .background(primaryColor.copy(alpha = 0.18f), CircleShape)
                .clickable(onClick = onCategoryClick),
            contentAlignment = Alignment.Center
        ) {
            val icon = categoryIconForIconKey(tx.categoryIcon).takeIf { !tx.categoryIcon.isNullOrBlank() }
                ?: categoryIconForName(tx.categoryName)
            Icon(icon, null, tint = primaryColor, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
            Spacer(Modifier.size(2.dp))
            Text(timeText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
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

private fun buildCalendarCells(
    showingMonth: YearMonth,
    daySummaryByDate: Map<LocalDate, Pair<Double, Double>>
): List<CalendarCell> {
    val first = showingMonth.atDay(1)
    val startShift = (first.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val startDate = first.minusDays(startShift.toLong())
    return (0 until 42).map { idx ->
        val d = startDate.plusDays(idx.toLong())
        val summary = daySummaryByDate[d] ?: (0.0 to 0.0)
        CalendarCell(
            date = d,
            inCurrentMonth = YearMonth.from(d) == showingMonth,
            incomeAmount = summary.first,
            expenseAmount = summary.second
        )
    }
}

private fun formatCompactAmount(v: Double): String {
    val abs = abs(v)
    return if (abs >= 10000) {
        "${String.format("%.1f", abs / 10000)}w"
    } else {
        String.format("%.0f", abs)
    }
}
