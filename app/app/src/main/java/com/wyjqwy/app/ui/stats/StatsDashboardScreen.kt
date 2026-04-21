package com.wyjqwy.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.category.categoryIconForName
import com.wyjqwy.app.ui.theme.BookColors
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.temporal.WeekFields
import kotlin.math.abs

private enum class BillType(val label: String, val value: Int) {
    EXPENSE("支出", 1),
    INCOME("收入", 2)
}

private enum class StatsPeriod(val label: String) {
    WEEK("周"),
    MONTH("月"),
    YEAR("年")
}

private data class TrendPoint(
    val axisLabel: String,
    val amount: Double,
    val bubbleLabel: String
)

private data class CategoryRankItem(
    val categoryId: Long,
    val categoryName: String,
    val totalAmount: Double,
    val count: Int,
    val seedTx: TransactionItem
)

@Composable
fun StatsDashboardScreen(
    vm: AppViewModel,
    rankListState: LazyListState,
    onOpenCategoryStats: (TransactionItem) -> Unit
) {
    val chart by vm.chartStats.collectAsState()
    val weekFields = WeekFields.ISO
    val period = StatsPeriod.entries[chart.periodOrdinal.coerceIn(0, 2)]
    val billType = if (chart.billType == 2) BillType.INCOME else BillType.EXPENSE

    var showYearPicker by remember { mutableStateOf(false) }
    var showYearMonthPicker by remember { mutableStateOf(false) }
    var showYearWeekPicker by remember { mutableStateOf(false) }

    val (from, to) = remember(period, chart.selectedYear, chart.selectedMonth, chart.selectedWeek) {
        when (period) {
            StatsPeriod.WEEK -> {
                val weekStart = LocalDate.of(chart.selectedYear, 1, 4)
                    .with(weekFields.weekOfWeekBasedYear(), chart.selectedWeek.toLong())
                    .with(weekFields.dayOfWeek(), 1)
                weekStart.atStartOfDay() to weekStart.plusWeeks(1).atStartOfDay()
            }
            StatsPeriod.MONTH -> {
                val ym = YearMonth.of(chart.selectedYear, chart.selectedMonth)
                ym.atDay(1).atStartOfDay() to ym.plusMonths(1).atDay(1).atStartOfDay()
            }
            StatsPeriod.YEAR -> {
                val start = LocalDate.of(chart.selectedYear - 4, 1, 1).atStartOfDay()
                start to LocalDate.of(chart.selectedYear + 1, 1, 1).atStartOfDay()
            }
        }
    }

    val yearsNeeded = remember(from, to) { yearsSpannedByRange(from, to) }
    LaunchedEffect(from, to) {
        vm.ensureChartYearsLoaded(yearsNeeded)
    }

    val mergedTx = remember(chart.yearTransactions, yearsNeeded) {
        yearsNeeded.flatMap { y -> chart.yearTransactions[y].orEmpty() }
    }
    val inWindow = remember(mergedTx, from, to) {
        mergedTx.filter { tx ->
            val dt = tx.parsedOccurredAt ?: return@filter false
            !dt.isBefore(from) && dt.isBefore(to)
        }
    }
    val filteredByType = remember(inWindow, chart.billType) {
        inWindow.filter { it.type == chart.billType }
    }

    val anyLoading = yearsNeeded.any { y -> chart.loadingYears.contains(y) }
    val showBlocking = yearsNeeded.isNotEmpty() && mergedTx.isEmpty() && anyLoading
    val ranked = remember(filteredByType) {
        filteredByType
            .groupBy { it.categoryId to it.categoryName }
            .mapNotNull { (key, items) ->
                val seed = items.maxByOrNull { abs(it.amount) } ?: return@mapNotNull null
                CategoryRankItem(
                    categoryId = key.first,
                    categoryName = key.second,
                    totalAmount = items.sumOf { abs(it.amount) },
                    count = items.size,
                    seedTx = seed
                )
            }
            .sortedByDescending { it.totalAmount }
            .take(10)
    }
    val totalAmount = ranked.sumOf { it.totalAmount }
    val trendData = remember(filteredByType, period, from) {
        buildTrendSeries(filteredByType, period, from)
    }
    val trendAverage = remember(trendData) {
        if (trendData.isEmpty()) 0.0 else trendData.map { it.amount }.average()
    }

    if (showYearPicker) {
        YearOnlyPickerSheet(
            initialYear = chart.selectedYear,
            onDismiss = { showYearPicker = false },
            onConfirm = { y ->
                vm.setChartYearOnly(y)
            }
        )
    }
    if (showYearMonthPicker) {
        YearMonthPickerSheet(
            selectedYear = chart.selectedYear,
            initialMonth = chart.selectedMonth,
            onDismiss = { showYearMonthPicker = false },
            onConfirm = { y, m ->
                vm.setChartYearMonth(y, m)
            }
        )
    }
    if (showYearWeekPicker) {
        YearWeekPickerSheet(
            selectedYear = chart.selectedYear,
            initialWeek = chart.selectedWeek,
            onDismiss = { showYearWeekPicker = false },
            onConfirm = { y, w ->
                vm.setChartYearWeek(y, w)
            }
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(BookColors.BrandTeal)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Spacer(Modifier.size(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TimeRangeChip(
                    text = when (period) {
                        StatsPeriod.WEEK -> "${chart.selectedYear}年 第${chart.selectedWeek}周"
                        StatsPeriod.MONTH -> "${chart.selectedYear}年 ${chart.selectedMonth}月"
                        StatsPeriod.YEAR -> "${chart.selectedYear}年"
                    },
                    onClick = {
                        when (period) {
                            StatsPeriod.WEEK -> showYearWeekPicker = true
                            StatsPeriod.MONTH -> showYearMonthPicker = true
                            StatsPeriod.YEAR -> showYearPicker = true
                        }
                    }
                )
                Spacer(Modifier.weight(1f))
                BillType.entries.forEach { t ->
                    AssistChip(
                        onClick = { vm.setChartBillType(if (t == BillType.INCOME) 2 else 1) },
                        label = { Text(t.label) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (billType == t) BookColors.TextBlack else BookColors.BrandTeal,
                            labelColor = if (billType == t) BookColors.White else BookColors.TextBlack
                        )
                    )
                }
            }
            Spacer(Modifier.size(8.dp))
            Row(Modifier.fillMaxWidth()) {
                StatsPeriod.entries.forEach { p ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(0.dp))
                            .clickable { vm.setChartPeriodOrdinal(p.ordinal) },
                        border = BorderStroke(1.dp, BookColors.TextBlack),
                        color = if (period == p) BookColors.TextBlack else BookColors.BrandTeal
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.label,
                                color = if (period == p) BookColors.White else BookColors.TextBlack,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        if (showBlocking) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BookColors.BrandTeal)
            }
            return@Column
        }
        if (mergedTx.isEmpty() && chart.lastError != null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(chart.lastError ?: "", color = BookColors.RedExpense)
            }
            return@Column
        }

        Column(Modifier.fillMaxSize()) {
            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = "总${billType.label}: ${String.format("%.2f", filteredByType.sumOf { abs(it.amount) })}",
                    color = BookColors.TextBlack,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.size(4.dp))
                Text(
                    text = "共 ${filteredByType.size} 笔，分类 ${ranked.size} 个（Top10）",
                    color = BookColors.TextGray,
                    fontSize = 13.sp
                )
                if (chart.lastError != null && mergedTx.isNotEmpty()) {
                    Spacer(Modifier.size(4.dp))
                    Text(
                        text = chart.lastError ?: "",
                        color = BookColors.TextGray,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.size(10.dp))
                TrendLineChart(
                    data = trendData,
                    average = trendAverage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                // 剔除了这里底部多余的平均线文字展示
            }
            HorizontalDivider(color = BookColors.Line)
            Text(
                text = "${billType.label}排行榜",
                color = BookColors.TextBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
            )
            if (ranked.isEmpty()) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("当前时间范围暂无数据", color = BookColors.TextGray)
                }
            } else {
                LazyColumn(
                    state = rankListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(ranked, key = { it.categoryId }) { item ->
                        val pct = if (totalAmount <= 0) 0f else (item.totalAmount / totalAmount).toFloat()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenCategoryStats(item.seedTx) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryDot(item.categoryName)
                            Spacer(Modifier.size(10.dp))
                            Column(Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(item.categoryName, color = BookColors.TextBlack, fontSize = 16.sp)
                                    Text(
                                        "${String.format("%.1f", pct * 100)}%",
                                        color = BookColors.TextGray,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        String.format("%.2f", item.totalAmount),
                                        color = BookColors.TextBlack,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(Modifier.size(6.dp))
                                LinearProgressIndicator(
                                    progress = { pct },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = BookColors.BrandTeal,
                                    trackColor = BookColors.Line
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeRangeChip(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(5.dp)).clickable(onClick = onClick),
        color = BookColors.BrandTeal
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = BookColors.TextBlack, fontSize = 16.sp)
            Icon(Icons.Outlined.KeyboardArrowDown, null, tint = BookColors.TextGray, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CategoryDot(categoryName: String) {
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

private data class TrendChartGeometry(
    val points: List<Offset>,
    val avgY: Float,
    val avgYRatio: Float
)

@Composable
private fun TrendLineChart(
    data: List<TrendPoint>,
    average: Double,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("暂无趋势数据", color = BookColors.TextGray, fontSize = 12.sp)
        }
        return
    }
    val maxValue = (data.maxOfOrNull { it.amount } ?: 0.0).coerceAtLeast(average).coerceAtLeast(1.0)
    var selectedIndex by remember(data) { mutableStateOf(-1) }
    val density = LocalDensity.current
    var chartWidthPx by remember { mutableIntStateOf(0) }
    var chartHeightPx by remember { mutableIntStateOf(0) }
    var bubbleWidthPx by remember { mutableIntStateOf(0) }
    var bubbleHeightPx by remember { mutableIntStateOf(0) }
    val geometry = remember(data, chartWidthPx, chartHeightPx, maxValue, average, density) {
        if (chartWidthPx <= 0 || chartHeightPx <= 0) return@remember null
        val leftPad = with(density) { 12.dp.toPx() }
        val rightPad = with(density) { 12.dp.toPx() }
        val topPad = with(density) { 8.dp.toPx() }
        val bottomPad = with(density) { 10.dp.toPx() }
        val width = chartWidthPx.toFloat() - leftPad - rightPad
        val height = chartHeightPx.toFloat() - topPad - bottomPad
        if (width <= 0f || height <= 0f) return@remember null
        val stepX = if (data.size <= 1) 0f else width / (data.size - 1)
        val points = data.mapIndexed { idx, p ->
            val x = leftPad + idx * stepX
            val y = topPad + height * (1f - (p.amount / maxValue).toFloat().coerceIn(0f, 1f))
            Offset(x, y)
        }
        val avgY = topPad + height * (1f - (average / maxValue).toFloat().coerceIn(0f, 1f))
        val avgYRatio = avgY / chartHeightPx.toFloat()
        TrendChartGeometry(points, avgY, avgYRatio)
    }
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onGloballyPositioned {
                    chartWidthPx = it.size.width
                    chartHeightPx = it.size.height
                }
                .pointerInput(data, chartWidthPx) {
                    detectTapGestures { tap ->
                        val leftPad = with(density) { 12.dp.toPx() }
                        val rightPad = with(density) { 12.dp.toPx() }
                        val width = chartWidthPx.toFloat() - leftPad - rightPad
                        if (data.size <= 1 || width <= 0f) return@detectTapGestures
                        val stepX = width / (data.size - 1)
                        val raw = ((tap.x - leftPad) / stepX).toInt()
                        selectedIndex = raw.coerceIn(0, data.lastIndex)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val g = geometry ?: return@Canvas
                val leftPad = 12.dp.toPx()
                val rightPad = 12.dp.toPx()
                val width = size.width - leftPad - rightPad
                drawLine(
                    color = BookColors.TextGray.copy(alpha = 0.55f),
                    start = Offset(leftPad, g.avgY),
                    end = Offset(leftPad + width, g.avgY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
                val path = Path().apply {
                    g.points.forEachIndexed { index, p ->
                        if (index == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                    }
                }
                drawPath(
                    path = path,
                    color = BookColors.BrandTeal,
                    style = Stroke(width = 2.dp.toPx())
                )
                val maxIdx = data.indices.maxByOrNull { data[it].amount } ?: 0
                val minIdx = data.indices.minByOrNull { data[it].amount } ?: 0
                g.points.forEachIndexed { index, p ->
                    val isSelected = index == selectedIndex
                    drawCircle(
                        color = when {
                            isSelected -> BookColors.TextBlack
                            index == maxIdx -> androidx.compose.ui.graphics.Color(0xFFD32F2F)
                            index == minIdx -> androidx.compose.ui.graphics.Color(0xFF1976D2)
                            else -> BookColors.BrandTeal
                        },
                        radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                        center = p
                    )
                }
            }
            val g = geometry
            if (chartWidthPx > 0 && chartHeightPx > 0 && g != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = (chartHeightPx * g.avgYRatio - 20.dp.toPx()).toInt().coerceAtLeast(0)
                            )
                        },
                    color = BookColors.White,
                    tonalElevation = 1.dp,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "平均 ${String.format("%.2f", average)}",
                        color = BookColors.TextGray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (selectedIndex in data.indices) {
                val label = "${data[selectedIndex].bubbleLabel}  ¥${String.format("%.2f", data[selectedIndex].amount)}"
                val point = g?.points?.getOrNull(selectedIndex)
                val spacingPx = with(density) { 8.dp.toPx() }
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .onSizeChanged {
                            bubbleWidthPx = it.width
                            bubbleHeightPx = it.height
                        }
                        .offset {
                            val px = point?.x ?: 0f
                            val py = point?.y ?: 0f
                            val chartW = chartWidthPx.toFloat()
                            val chartH = chartHeightPx.toFloat()
                            val bw = bubbleWidthPx.toFloat().takeIf { it > 0f } ?: with(density) { 140.dp.toPx() }
                            val bh = bubbleHeightPx.toFloat().takeIf { it > 0f } ?: with(density) { 36.dp.toPx() }
                            val preferRight = (px + bw + spacingPx) <= chartW
                            val preferUp = (py - bh - spacingPx) >= 0f
                            val rawX = if (preferRight) px + spacingPx else px - bw - spacingPx
                            val rawY = if (preferUp) py - bh - spacingPx else py + spacingPx
                            val offsetX = rawX.coerceIn(0f, (chartW - bw).coerceAtLeast(0f))
                            val offsetY = rawY.coerceIn(0f, (chartH - bh).coerceAtLeast(0f))
                            IntOffset(
                                x = offsetX.toInt(),
                                y = offsetY.toInt()
                            )
                        },
                    color = BookColors.White,
                    tonalElevation = 2.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = label,
                        color = BookColors.TextBlack,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
        val axisLabels = remember(data) { buildAxisLabels(data) }
        if (axisLabels.isNotEmpty()) {
            Spacer(Modifier.size(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                axisLabels.forEach { txt ->
                    Text(text = txt, color = BookColors.TextGray, fontSize = 10.sp)
                }
            }
        }
    }
}

private fun yearsSpannedByRange(from: LocalDateTime, to: LocalDateTime): Set<Int> {
    val start = from.toLocalDate()
    val endInclusive = to.toLocalDate().minusDays(1)
    if (endInclusive.isBefore(start)) return setOf(start.year)
    return (start.year..endInclusive.year).toSet()
}

private fun buildTrendSeries(
    list: List<TransactionItem>,
    period: StatsPeriod,
    from: LocalDateTime
): List<TrendPoint> {
    return when (period) {
        StatsPeriod.WEEK -> {
            val dayMap = (0..6).associateWith { 0.0 }.toMutableMap()
            list.forEach { tx ->
                val dt = tx.parsedOccurredAt ?: return@forEach
                val idx = dt.toLocalDate().toEpochDay() - from.toLocalDate().toEpochDay()
                if (idx in 0L..6L) dayMap[idx.toInt()] = dayMap.getValue(idx.toInt()) + abs(tx.amount)
            }
            (0..6).map { idx ->
                val d = from.toLocalDate().plusDays(idx.toLong())
                TrendPoint(
                    axisLabel = "周${idx + 1}",
                    amount = dayMap.getValue(idx),
                    bubbleLabel = "星期${weekDayCn(d.dayOfWeek.value)}"
                )
            }
        }
        StatsPeriod.MONTH -> {
            val ym = YearMonth.from(from)
            val count = ym.lengthOfMonth()
            val dayMap = (1..count).associateWith { 0.0 }.toMutableMap()
            list.forEach { tx ->
                val dt = tx.parsedOccurredAt ?: return@forEach
                if (YearMonth.from(dt) == ym) {
                    val day = dt.dayOfMonth
                    dayMap[day] = dayMap.getValue(day) + abs(tx.amount)
                }
            }
            (1..count).map { day ->
                TrendPoint(
                    axisLabel = day.toString(),
                    amount = dayMap.getValue(day),
                    bubbleLabel = "${ym.monthValue}-${day}"
                )
            }
        }
        StatsPeriod.YEAR -> {
            val endYear = from.year + 4
            val yearMap = (from.year..endYear).associateWith { 0.0 }.toMutableMap()
            list.forEach { tx ->
                val dt = tx.parsedOccurredAt ?: return@forEach
                if (dt.year in from.year..endYear) {
                    yearMap[dt.year] = yearMap.getValue(dt.year) + abs(tx.amount)
                }
            }
            (from.year..endYear).map { y ->
                TrendPoint(
                    axisLabel = "${y}年",
                    amount = yearMap.getValue(y),
                    bubbleLabel = "${y}年"
                )
            }
        }
    }
}

private fun weekDayCn(v: Int): String = when (v) {
    1 -> "一"
    2 -> "二"
    3 -> "三"
    4 -> "四"
    5 -> "五"
    6 -> "六"
    else -> "日"
}

private fun buildAxisLabels(data: List<TrendPoint>): List<String> {
    if (data.size <= 7) return data.map { it.axisLabel }
    if (data.size <= 12) {
        val mid = data.size / 2
        return listOf(data.first().axisLabel, data[mid].axisLabel, data.last().axisLabel)
    }
    val step = (data.size / 4).coerceAtLeast(1)
    val labels = mutableListOf<String>()
    var i = 0
    while (i < data.size) {
        labels += data[i].axisLabel
        i += step
    }
    if (labels.lastOrNull() != data.last().axisLabel) labels += data.last().axisLabel
    return labels.take(5)
}