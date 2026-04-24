package com.wyjqwy.app.ui.invest

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs

private data class InvestGroupItem(
    val key: String,
    val name: String,
    val totalAmount: Double,
    val count: Int,
    val latestDate: LocalDate?,
    val color: Color
)

private val investPalette = listOf(
    Color(0xFF4DB6AC),
    Color(0xFF81C784),
    Color(0xFFFFB74D),
    Color(0xFF64B5F6),
    Color(0xFFBA68C8),
    Color(0xFFE57373),
    Color(0xFFA1887F),
    Color(0xFF90A4AE)
)

@Composable
fun AutoInvestScreen(
    vm: AppViewModel,
    listState: LazyListState,
    onOpenNoteDetails: (noteKey: String, noteDisplayName: String) -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val state by vm.autoInvest.collectAsState()
    val currentYear = LocalDate.now().year
    val yearsNeeded = remember(currentYear) { (currentYear - 9..currentYear).toSet() }

    LaunchedEffect(yearsNeeded) {
        vm.ensureAutoInvestYearsLoaded(yearsNeeded)
    }

    val merged = remember(state.yearTransactions, yearsNeeded) {
        yearsNeeded.flatMap { state.yearTransactions[it].orEmpty() }
    }
    val investTx = remember(merged) {
        merged
            .filter { it.isInvestmentExpense() }
            .sortedByDescending { it.parsedOccurredAt ?: LocalDate.MIN.atStartOfDay() }
    }
    val anyLoading = yearsNeeded.any { it in state.loadingYears }
    val showBlocking = merged.isEmpty() && anyLoading

    val groups = remember(investTx) {
        investTx
            .groupBy { it.investNoteKey() }
            .entries
            .mapIndexed { index, entry ->
                val items = entry.value
                InvestGroupItem(
                    key = entry.key,
                    name = items.firstNotNullOfOrNull { it.note?.trim()?.takeIf(String::isNotBlank) } ?: "未备注",
                    totalAmount = items.sumOf { abs(it.amount) },
                    count = items.size,
                    latestDate = items.maxOfOrNull { it.parsedOccurredAt?.toLocalDate() ?: LocalDate.MIN },
                    color = investPalette[index % investPalette.size]
                )
            }
            .sortedByDescending { it.totalAmount }
    }

    val totalAmount = groups.sumOf { it.totalAmount }
    val firstDate = investTx.minOfOrNull { it.parsedOccurredAt?.toLocalDate() ?: LocalDate.MAX }
    val totalDays = remember(firstDate) {
        if (firstDate == null || firstDate == LocalDate.MAX) 0L
        else ChronoUnit.DAYS.between(firstDate, LocalDate.now()).coerceAtLeast(0)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "定投管理",
                color = BookColors.TextBlack,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (showBlocking) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
            return@Column
        }

        if (investTx.isEmpty() && state.lastError != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.lastError.orEmpty(), color = BookColors.RedExpense)
            }
            return@Column
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = BookColors.White,
                    shadowElevation = 6.dp
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // 替代原本的 3 个 InvestStatCell 调用
                            Column(Modifier.fillMaxWidth()) {
                                // 第一行：数值行
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("¥${formatAmount(totalAmount)}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${investTx.size}次", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${totalDays}天", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Spacer(Modifier.height(4.dp))
                                // 第二行：标签行
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("总资产", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = BookColors.TextGray, fontSize = 12.sp)
                                    Text("总次数", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = BookColors.TextGray, fontSize = 12.sp)
                                    Text("总时长", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = BookColors.TextGray, fontSize = 12.sp)
                                }
                            }                        }
                        Spacer(Modifier.height(18.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InvestDonutChart(
                                groups = groups,
                                totalAmount = totalAmount,
                                modifier = Modifier.size(168.dp)
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                groups.take(6).forEach { item ->
                                    InvestLegendRow(item = item, totalAmount = totalAmount)
                                }
                                if (groups.isEmpty()) {
                                    Text("暂无投资数据", color = BookColors.TextGray, fontSize = 13.sp)
                                }
                            }
                        }
                        if (state.lastError != null && merged.isNotEmpty()) {
                            Spacer(Modifier.height(10.dp))
                            Text(state.lastError.orEmpty(), color = BookColors.TextGray, fontSize = 11.sp)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "投资分类列表",
                    color = BookColors.TextBlack,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            if (groups.isEmpty()) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无投资明细", color = BookColors.TextGray)
                    }
                }
            } else {
                items(groups, key = { it.key }) { item ->
                    InvestGroupRow(
                        item = item,
                        totalAmount = totalAmount,
                        onClick = { onOpenNoteDetails(item.key, item.name) }
                    )
                    HorizontalDivider(color = BookColors.Line)
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun InvestStatCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            value,
            color = BookColors.TextBlack,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = BookColors.TextGray,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun InvestDonutChart(
    groups: List<InvestGroupItem>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = size.minDimension * 0.16f
            val diameter = size.minDimension - strokeWidth
            val topLeft = androidx.compose.ui.geometry.Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            if (groups.isEmpty() || totalAmount <= 0.0) {
                drawArc(
                    color = BookColors.Line,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(diameter, diameter),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                var startAngle = -90f
                groups.forEach { item ->
                    val sweep = (item.totalAmount / totalAmount * 360f).toFloat()
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(diameter, diameter),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    startAngle += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("总金额", color = BookColors.TextGray, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text("¥${formatAmount(totalAmount)}", color = BookColors.TextBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InvestLegendRow(item: InvestGroupItem, totalAmount: Double) {
    val percent = if (totalAmount <= 0.0) 0.0 else item.totalAmount / totalAmount * 100
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(item.color, CircleShape)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = item.name,
            color = BookColors.TextBlack,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
        Text("${String.format("%.1f", percent)}%", color = BookColors.TextGray, fontSize = 12.sp)
    }
}

@Composable
private fun InvestGroupRow(
    item: InvestGroupItem,
    totalAmount: Double,
    onClick: () -> Unit
) {
    val pct = if (totalAmount <= 0.0) 0f else (item.totalAmount / totalAmount).toFloat()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .background(item.color.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(14.dp)
                    .background(item.color, CircleShape)
            )
        }
        Spacer(Modifier.size(10.dp))
        Column(Modifier.weight(1f)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.name, color = BookColors.TextBlack, fontSize = 16.sp)
                Text("${String.format("%.1f", pct * 100)}%", color = BookColors.TextGray, fontSize = 13.sp)
                Text("¥${formatAmount(item.totalAmount)}", color = BookColors.TextBlack, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = buildString {
                    append("${item.count}次")
                    item.latestDate?.takeIf { it != LocalDate.MIN }?.let {
                        append(" · 最近 ${it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))}")
                    }
                },
                color = BookColors.TextGray,
                fontSize = 12.sp
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier.fillMaxWidth(),
                color = item.color,
                trackColor = BookColors.Line
            )
        }
    }
}

private fun TransactionItem.isInvestmentExpense(): Boolean {
    return type == 1 && categoryName.contains("投资", ignoreCase = true)
}

private fun TransactionItem.investNoteKey(): String {
    return note?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: "未备注"
}

private fun formatAmount(v: Double): String = String.format("%.2f", v)
