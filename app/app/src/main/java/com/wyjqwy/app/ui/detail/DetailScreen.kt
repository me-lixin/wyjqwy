package com.wyjqwy.app.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import com.commandiron.wheel_picker_compose.core.WheelTextPicker
import com.wyjqwy.app.R
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.category.categoryIconForName
import com.wyjqwy.app.ui.util.groupTransactionsByDay
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.channels.BufferOverflow

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    state: AppUiState,
    vm: AppViewModel,
    onOpenSearch: () -> Unit,
    onOpenEditTransaction: (TransactionItem) -> Unit,
    onOpenCategoryStats: (TransactionItem) -> Unit,
    listState: LazyListState
) {
    val density = LocalDensity.current
    val releaseThresholdPx = remember(density) { with(density) { 88.dp.toPx() } }
    val monthSwitchCooldownMs = 700L

    var topPullPx by remember { mutableFloatStateOf(0f) }
    var bottomPullPx by remember { mutableFloatStateOf(0f) }
    var topPullPeakPx by remember { mutableFloatStateOf(0f) }
    var bottomPullPeakPx by remember { mutableFloatStateOf(0f) }
    var lastMonthSwitchAt by remember { mutableLongStateOf(0L) }

    val loadingNow = rememberUpdatedState(state.loading)
    val selectedYmNow = rememberUpdatedState(state.selectedYearMonth)
    val fingerLift = remember {
        MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }

    LaunchedEffect(state.selectedYearMonth) {
        topPullPx = 0f
        bottomPullPx = 0f
        topPullPeakPx = 0f
        bottomPullPeakPx = 0f
    }

    LaunchedEffect(listState) {
        merge(
            snapshotFlow { listState.isScrollInProgress }
                .distinctUntilChanged()
                .filter { !it }
                .map { },
            fingerLift
        ).collect {
            delay(48)
            if (listState.isScrollInProgress) return@collect
            val fresh = vm.uiState.value
            if (fresh.loading) {
                topPullPx = 0f
                bottomPullPx = 0f
                topPullPeakPx = 0f
                bottomPullPeakPx = 0f
                return@collect
            }
            val now = System.currentTimeMillis()
            if (now - lastMonthSwitchAt < monthSwitchCooldownMs) {
                topPullPx = 0f
                bottomPullPx = 0f
                topPullPeakPx = 0f
                bottomPullPeakPx = 0f
                return@collect
            }
            val thisMonth = YearMonth.now()
            if (bottomPullPeakPx >= releaseThresholdPx) {
                vm.navigateMonthWindow(-1)
                lastMonthSwitchAt = System.currentTimeMillis()
            } else if (topPullPeakPx >= releaseThresholdPx && fresh.selectedYearMonth < thisMonth) {
                vm.navigateMonthWindow(1)
                lastMonthSwitchAt = System.currentTimeMillis()
            }
            topPullPx = 0f
            bottomPullPx = 0f
            topPullPeakPx = 0f
            bottomPullPeakPx = 0f
        }
    }

    val monthScrollConnection = remember(listState, releaseThresholdPx) {
        object : NestedScrollConnection {

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source != NestedScrollSource.Drag || loadingNow.value) return Offset.Zero

                if (bottomPullPx > 0f && available.y > 0f) {
                    val consumedY = available.y.coerceAtMost(bottomPullPx)
                    bottomPullPx -= consumedY
                    return Offset(0f, consumedY)
                }

                if (topPullPx > 0f && available.y < 0f) {
                    val consumedY = (-available.y).coerceAtMost(topPullPx)
                    topPullPx -= consumedY
                    return Offset(0f, -consumedY)
                }

                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source != NestedScrollSource.Drag || loadingNow.value) return Offset.Zero

                val atTop = !listState.canScrollBackward
                val atBottom = !listState.canScrollForward
                val canGoNextMonth = selectedYmNow.value < YearMonth.now()

                if (atBottom && available.y < 0f) {
                    bottomPullPx += (-available.y * 0.5f)
                    bottomPullPeakPx = max(bottomPullPeakPx, bottomPullPx)
                    return Offset(0f, available.y)
                } else if (atTop && available.y > 0f && canGoNextMonth) {
                    topPullPx += (available.y * 0.5f)
                    topPullPeakPx = max(topPullPeakPx, topPullPx)
                    return Offset(0f, available.y)
                }

                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return Velocity.Zero
            }
        }
    }

    val thisMonth = YearMonth.now()
    val bottomVisual = max(bottomPullPeakPx, bottomPullPx)
    val topVisual = max(topPullPeakPx, topPullPx)
    val bottomHintText = if (bottomVisual >= releaseThresholdPx) "松开查看上月数据" else null
    val topHintText = if (state.selectedYearMonth < thisMonth && topVisual >= releaseThresholdPx) "松开查看下月数据" else null

    var showYearMonthPicker by remember { mutableStateOf(false) }
    var pendingDeleteTx by remember { mutableStateOf<TransactionItem?>(null) }
    val appTitle = stringResource(R.string.app_display_name)

    if (showYearMonthPicker) {
        // 🌟 替换为内置的新滚轮组件
        DetailYearMonthPickerSheet(
            initial = state.selectedYearMonth,
            onDismiss = { showYearMonthPicker = false },
            onConfirm = { ym ->
                vm.setSelectedYearMonth(ym)
                showYearMonthPicker = false
            }
        )
    }
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

    Column(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .zIndex(1f)
                .background(BookColors.BrandTeal)
        ) {
            Column(Modifier.statusBarsPadding()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { vm.setAmountVisible(!state.amountVisible) }) {
                        Icon(
                            imageVector = if (state.amountVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = "显示或隐藏金额",
                            tint = BookColors.White
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BookColors.White.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = appTitle.take(1),
                                color = BookColors.White,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = appTitle,
                            color = BookColors.TextBlack,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = onOpenSearch) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = "搜索",
                            tint = BookColors.White
                        )
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .weight(1.4f)
                            .clickable { showYearMonthPicker = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "${state.selectedYearMonth.year}年",
                                color = BookColors.TextBlack,
                                fontSize = 13.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    String.format("%02d月", state.selectedYearMonth.monthValue),
                                    color = BookColors.TextBlack,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Icon(
                                    Icons.Outlined.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = BookColors.TextBlack,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    SummaryStat(
                        label = "收入",
                        value = state.summary?.totalIncome ?: 0.0,
                        visible = state.amountVisible,
                        valueColor = BookColors.TextBlack,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStat(
                        label = "支出",
                        value = state.summary?.totalExpense ?: 0.0,
                        visible = state.amountVisible,
                        valueColor = BookColors.TextBlack,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryStat(
                        label = "结余",
                        value = state.summary?.balance ?: 0.0,
                        visible = state.amountVisible,
                        valueColor = BookColors.TextBlack,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (state.loading) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BookColors.BrandTeal)
            }
        } else {
            val groups = remember(state.transactions) { groupTransactionsByDay(state.transactions) }
            AnimatedContent(
                targetState = state.selectedYearMonth,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 10 }) togetherWith
                            (fadeOut() + slideOutVertically { -it / 10 })
                },
                label = "month-switch"
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .nestedScroll(monthScrollConnection)
                        .pointerInput(fingerLift) {
                            awaitEachGesture {
                                var hadDown = false
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Final)
                                    val anyDown = event.changes.any { it.pressed }
                                    if (hadDown && !anyDown) {
                                        fingerLift.tryEmit(Unit)
                                        break
                                    }
                                    hadDown = anyDown
                                }
                            }
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        topHintText?.let { msg ->
                            Text(
                                text = msg,
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 24.dp),
                                color = BookColors.BrandTeal,
                                fontSize = 13.sp
                            )
                        }
                        bottomHintText?.let { msg ->
                            Text(
                                text = msg,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 34.dp),
                                color = BookColors.BrandTeal,
                                fontSize = 13.sp
                            )
                        }
                    }

                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationY = topPullPx - bottomPullPx
                            }
                            .background(MaterialTheme.colorScheme.surface),
                        state = listState
                    ) {
                        if (groups.isEmpty()) {
                            item {
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("暂无账单", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                                }
                            }
                        } else {
                            groups.forEach { (date, dayItems) ->
                                val dayExpense = dayItems.filter { it.type == 1 }.sumOf { it.amount }
                                item(key = "h_${date}") {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)
                                        Text(
                                            "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)} $weekday",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = if (state.amountVisible) "支出 ${String.format("%.2f", dayExpense)}" else "支出 ****",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                items(dayItems, key = { it.id }) { tx ->
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
                                        TransactionRow(
                                            tx = tx,
                                            amountVisible = state.amountVisible,
                                            onClick = { onOpenEditTransaction(tx) },
                                            onCategoryIconClick = { onOpenCategoryStats(tx) }
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
        }
    }
}

@Composable
private fun SummaryStat(
    label: String,
    value: Double,
    visible: Boolean,
    valueColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = BookColors.TextBlack, fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            text = if (visible) String.format("%.2f", value) else "****",
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun TransactionRow(
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
        CategoryIcon(tx.categoryName, onClick = onCategoryIconClick)
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
            text = if (amountVisible) "$prefix${String.format("%.2f", tx.amount)}" else "****",
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CategoryIcon(categoryName: String, onClick: () -> Unit) {
    val icon = categoryIconForName(categoryName)
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(BookColors.BrandTealIconBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = BookColors.BrandTeal, modifier = Modifier.size(20.dp))
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

// 🌟 为 DetailScreen 专属定制的滚轮时间选择器
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailYearMonthPickerSheet(
    initial: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    val currentYear = java.time.LocalDate.now().year
    val yearRange = remember(currentYear) { (currentYear - 4..currentYear).toList().reversed() }
    val yearsStr = remember(yearRange) { yearRange.map { "${it}年" } }
    val monthsStr = remember { (1..12).map { "${it}月" } }

    val initialYearIndex = remember(initial.year, yearRange) {
        yearRange.indexOf(initial.year).coerceAtLeast(0)
    }
    val initialMonthIndex = initial.monthValue - 1

    var finalYear by remember { mutableIntStateOf(if (initialYearIndex >= 0) yearRange[initialYearIndex] else currentYear) }
    var finalMonth by remember { mutableIntStateOf(initial.monthValue) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookColors.White,
        dragHandle = null
    ) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 20.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("取消", color = BookColors.BrandTeal) }
                Text("选择年月", color = BookColors.TextBlack, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { onConfirm(YearMonth.of(finalYear, finalMonth)) }) { Text("确定", color = BookColors.BrandTeal) }
            }
            HorizontalDivider(color = BookColors.Line)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().height(220.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    WheelTextPicker(
                        texts = yearsStr,
                        rowCount = 5,
                        startIndex = initialYearIndex,
                        onScrollFinished = { snappedIndex ->
                            finalYear = yearRange[snappedIndex]
                            return@WheelTextPicker null
                        }
                    )
                }
                Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                    WheelTextPicker(
                        texts = monthsStr,
                        rowCount = 5,
                        startIndex = initialMonthIndex,
                        onScrollFinished = { snappedIndex ->
                            finalMonth = snappedIndex + 1
                            return@WheelTextPicker null
                        }
                    )
                }
            }
        }
    }
}