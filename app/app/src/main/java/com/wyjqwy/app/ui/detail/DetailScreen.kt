package com.wyjqwy.app.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.wyjqwy.app.R
import com.wyjqwy.app.data.TemplateItem
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.category.categoryIconForIconKey
import com.wyjqwy.app.ui.category.categoryIconForName
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import com.wyjqwy.app.ui.util.groupTransactionsByDay
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScreen(
    state: AppUiState,
    vm: AppViewModel,
    onOpenSearch: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenEditTransaction: (TransactionItem) -> Unit,
    onOpenCategoryStats: (TransactionItem) -> Unit,
    listState: LazyListState
) {
    val primaryColor = rememberThemePrimaryColor()
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
    val appLogo = stringResource(R.string.app_logo_glyph)

    if (showYearMonthPicker) {
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
        DetailHeaderSection(
            appTitle = appTitle,
            appLogo = appLogo,
            selectedYearMonth = state.selectedYearMonth,
            amountVisible = state.amountVisible,
            totalIncome = state.summary?.totalIncome ?: 0.0,
            totalExpense = state.summary?.totalExpense ?: 0.0,
            templates = state.templates,
            onToggleAmountVisible = { vm.setAmountVisible(!state.amountVisible) },
            onOpenCalendar = onOpenCalendar,
            onOpenSearch = onOpenSearch,
            onOpenYearMonthPicker = { showYearMonthPicker = true },
            onApplyTemplate = { vm.applyTemplate(it.id) },
            onDeleteTemplate = { vm.deleteTemplateSilently(it.id) },
            modifier = Modifier.zIndex(1f)
        )

        // 🌟 修复二：将 AnimatedContent 和 Loading 剥离，避免相互销毁
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            val groups = remember(state.transactions) { groupTransactionsByDay(state.transactions) }
            
            // 列表始终存在
            AnimatedContent(
                targetState = state.selectedYearMonth,
                modifier = Modifier.fillMaxSize(),
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
                                color = primaryColor,
                                fontSize = 13.sp
                            )
                        }
                        bottomHintText?.let { msg ->
                            Text(
                                text = msg,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 34.dp),
                                color = primaryColor,
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
                        if (groups.isEmpty() && !state.loading) {
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
                                val dayIncome = dayItems.filter { it.type == 2 }.sumOf { kotlin.math.abs(it.amount) }
                                val dayExpense = dayItems.filter { it.type == 1 }.sumOf { kotlin.math.abs(it.amount) }
                                item(key = "h_${date}") {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .background(BookColors.White)
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
                                            text = if (state.amountVisible) {
                                                "收入 ${String.format("%.2f", dayIncome)}  支出 ${String.format("%.2f", dayExpense)}"
                                            } else {
                                                "收入 **** 支出 ****"
                                            },
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                                
                                items(dayItems, key = { it.id }) { tx ->
                                    // 🌟 修复一：使用自定义的 SwipeActionMenu 替换原本无法定格的 SwipeToDismissBox
                                    SwipeActionMenu(
                                        menuWidth = 116.dp, // 36*2 + 12间距 + 16*2 padding ≈ 116
                                        backgroundContent = { closeMenu ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.surface) // 保持底部颜色自然
                                                    .padding(horizontal = 16.dp),
                                                contentAlignment = Alignment.CenterEnd
                                            ) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF2E7D32))
                                                            .clickable {
                                                                vm.createTemplateFromTransaction(tx)
                                                                closeMenu()
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Add,
                                                            contentDescription = "添加模板",
                                                            tint = BookColors.White
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(36.dp)
                                                            .clip(CircleShape)
                                                            .background(BookColors.RedExpense)
                                                            .clickable {
                                                                pendingDeleteTx = tx
                                                                closeMenu()
                                                            },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.DeleteOutline,
                                                            contentDescription = "删除明细",
                                                            tint = BookColors.White
                                                        )
                                                    }
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

            // 🌟 独立处理 Loading：只在没有数据且 loading 时才显示大菊花，不阻碍列表渲染
            if (state.loading && groups.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
        }
    }
}

