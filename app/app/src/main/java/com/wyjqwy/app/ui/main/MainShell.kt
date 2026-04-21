@file:OptIn(ExperimentalMaterial3Api::class)

package com.wyjqwy.app.ui.main

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.detail.DetailScreen
import com.wyjqwy.app.ui.search.SearchScreen
import com.wyjqwy.app.ui.category.CategoryPickerScreen
import com.wyjqwy.app.ui.invest.AutoInvestNoteDetailScreen
import com.wyjqwy.app.ui.invest.AutoInvestScreen
import com.wyjqwy.app.ui.stats.CategoryStatsScreen
import com.wyjqwy.app.ui.stats.StatsDashboardScreen
import java.time.LocalDate

@Composable
fun MainShell(state: AppUiState, vm: AppViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<TransactionItem?>(null) }
    var statsTx by remember { mutableStateOf<TransactionItem?>(null) }
    var investNoteKey by remember { mutableStateOf<String?>(null) }
    var investNoteName by remember { mutableStateOf<String?>(null) }
    val detailListState = rememberLazyListState()
    val chartListState = rememberLazyListState()
    val autoInvestListState = rememberLazyListState()
    val autoInvestDetailListState = rememberLazyListState()

    if (showSearch) {
        SearchScreen(
            transactions = state.transactions,
            amountVisible = state.amountVisible,
            onBack = { showSearch = false },
            onEditTransaction = { tx ->
                editingTx = tx
                showSearch = false
                showCategoryPicker = true
            },
            onDeleteTransaction = { tx ->
                vm.deleteTransaction(tx.id)
            }
        )
        return
    }

    if (showCategoryPicker) {
        CategoryPickerScreen(
            state = state,
            vm = vm,
            initialTransaction = editingTx,
            onBack = {
                showCategoryPicker = false
                editingTx = null
            }
        )
        return
    }

    statsTx?.let { tx ->
        CategoryStatsScreen(
            state = state,
            vm = vm,
            seedTx = tx,
            onBack = { statsTx = null }
        ) { editTx ->
            editingTx = editTx
            showCategoryPicker = true
        }
        return
    }
    investNoteKey?.let { key ->
        AutoInvestNoteDetailScreen(
            vm = vm,
            noteKey = key,
            noteDisplayName = investNoteName ?: "未备注",
            listState = autoInvestDetailListState,
            onBack = {
                investNoteKey = null
                investNoteName = null
            },
            onEditTransaction = { tx ->
                editingTx = tx
                showCategoryPicker = true
            }
        )
        return
    }

    Scaffold(
        containerColor = BookColors.Background,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            SharkBottomBar(
                selectedIndex = tab,
                onSelect = { tab = it },
                onCenterAdd = {
                    editingTx = null
                    showCategoryPicker = true
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (tab) {
                0 -> DetailScreen(
                    state = state,
                    vm = vm,
                    onOpenSearch = { showSearch = true },
                    onOpenEditTransaction = { tx ->
                        editingTx = tx
                        showCategoryPicker = true
                    },
                    onOpenCategoryStats = { tx -> statsTx = tx },
                    listState = detailListState
                )
                1 -> StatsDashboardScreen(
                    vm = vm,
                    rankListState = chartListState,
                    onOpenCategoryStats = { tx -> statsTx = tx }
                )
                3 -> AutoInvestScreen(
                    vm = vm,
                    listState = autoInvestListState,
                    onOpenNoteDetails = { key, name ->
                        investNoteKey = key
                        investNoteName = name
                    }
                )
                4 -> MineTabScreen(state, vm)
                else -> DetailScreen(
                    state = state,
                    vm = vm,
                    onOpenSearch = { showSearch = true },
                    onOpenEditTransaction = { tx ->
                        editingTx = tx
                        showCategoryPicker = true
                    },
                    onOpenCategoryStats = { tx -> statsTx = tx },
                    listState = detailListState
                )
            }
        }
    }
}

private data class BottomTab(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val isCenter: Boolean = false
)

@Composable
private fun SharkBottomBar(
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onCenterAdd: () -> Unit
) {
    val tabs = listOf(
        BottomTab(0, "明细", Icons.Outlined.Article),
        BottomTab(1, "图表", Icons.Outlined.BarChart),
        BottomTab(2, "记账", Icons.Outlined.Add, isCenter = true),
        BottomTab(3, "定投", Icons.Outlined.Explore),
        BottomTab(4, "我的", Icons.Outlined.Person)
    )
    Column(
        Modifier
            .fillMaxWidth()
            .background(BookColors.White)
    ) {
        HorizontalDivider(color = BookColors.TabBarTopLine, thickness = 1.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
        ) {
            tabs.forEach { tab ->
                if (tab.isCenter) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            Modifier
                                .offset(y = (-28).dp)
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(BookColors.BrandTeal)
                                .clickable { onCenterAdd() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = BookColors.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            color = BookColors.TextGray,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        )
                    }
                } else {
                    val selected = selectedIndex == tab.index
                    Column(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable { onSelect(tab.index) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) BookColors.BrandTeal else BookColors.TextGray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            color = if (selected) BookColors.BrandTeal else BookColors.TextGray,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartTabScreen(state: AppUiState, vm: AppViewModel) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(BookColors.Main)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                "图表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = BookColors.TextBlack
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "本月收支概览（后续可接趋势图、分类占比）",
                fontSize = 12.sp,
                color = BookColors.TextBlack.copy(alpha = 0.85f)
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BookColors.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("总收入", color = BookColors.TextGray)
                        Text(
                            String.format("%.2f", state.summary?.totalIncome ?: 0.0),
                            color = BookColors.TextBlack,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("总支出", color = BookColors.TextGray)
                        Text(
                            String.format("%.2f", state.summary?.totalExpense ?: 0.0),
                            color = BookColors.RedExpense,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = BookColors.Line)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("结余", color = BookColors.TextGray)
                        Text(
                            String.format("%.2f", state.summary?.balance ?: 0.0),
                            color = BookColors.TextBlack,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { vm.loadHomeData() }) { Text("刷新数据", color = BookColors.TextBlack) }
        }
    }
}

@Composable
private fun DiscoverTabScreen(state: AppUiState, vm: AppViewModel) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(BookColors.Main)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Text(
                "定投",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = BookColors.TextBlack
            )
            Spacer(Modifier.height(4.dp))
            Text("快捷模板", fontSize = 12.sp, color = BookColors.TextBlack.copy(alpha = 0.85f))
        }
        LazyColumn(
            Modifier
                .fillMaxSize()
                .background(BookColors.Background)
        ) {
            items(state.templates, key = { it.id }) { t ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = BookColors.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(t.categoryName, color = BookColors.TextBlack, fontSize = 15.sp)
                            Text(
                                t.note ?: "",
                                color = BookColors.TextGray,
                                fontSize = 12.sp
                            )
                        }
                        TextButton(onClick = { vm.applyTemplate(t.id) }) {
                            Text("记一笔", color = BookColors.TextBlack)
                        }
                    }
                }
            }
            item {
                if (state.templates.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("暂无模板", color = BookColors.TextGray)
                    }
                }
            }
        }
    }
}

@Composable
private fun MineTabScreen(state: AppUiState, vm: AppViewModel) {
    var showAccountSettings by remember { mutableStateOf(false) }
    val autoInvest by vm.autoInvest.collectAsState()
    val currentYear = LocalDate.now().year
    val yearsNeeded = remember(currentYear) { (currentYear - 9..currentYear).toSet() }
    LaunchedEffect(yearsNeeded) {
        vm.ensureAutoInvestYearsLoaded(yearsNeeded)
    }
    val investCount = remember(autoInvest.yearTransactions, yearsNeeded) {
        yearsNeeded
            .flatMap { autoInvest.yearTransactions[it].orEmpty() }
            .count { it.type == 1 && it.categoryName.contains("投资", ignoreCase = true) }
    }
    val totalDays = remember(state.transactions) {
        state.transactions.mapNotNull { it.parsedOccurredAt?.toLocalDate() }.distinct().size
    }
    val totalCount = state.transactions.size

    if (showAccountSettings) {
        MineAccountSettingsPlaceholder(onBack = { showAccountSettings = false })
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(BookColors.Main)
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(BookColors.White)
                            .clickable { showAccountSettings = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "账号设置",
                            tint = BookColors.TextGray,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = "李鑫啊",
                        color = BookColors.TextBlack,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BookColors.White)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccountBalanceWallet,
                            contentDescription = null,
                            tint = BookColors.TextBlack,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("打卡", color = BookColors.TextBlack, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MineStatCell(value = investCount.toString(), label = "定投次数", modifier = Modifier.weight(1f))
                MineStatCell(value = totalDays.toString(), label = "记账总天数", modifier = Modifier.weight(1f))
                MineStatCell(value = totalCount.toString(), label = "记账总笔数", modifier = Modifier.weight(1f))
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BookColors.Background)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                MineMenuCard {
                    MineMenuRow(icon = Icons.Outlined.Person, title = "登录 / 注册")
                }
            }
            item {
                MineMenuCard {
                    MineMenuRow(icon = Icons.Outlined.SwapHoriz, title = "导入数据")
                    HorizontalDivider(color = BookColors.Line)
                    MineMenuRow(icon = Icons.Outlined.WorkOutline, title = "导出数据")
                }
            }
            item {
                MineMenuCard {
                    MineMenuRow(icon = Icons.Outlined.Face, title = "个性装扮")
                }
            }
        }
    }
}

@Composable
private fun MineAccountSettingsPlaceholder(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookColors.BrandTeal)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                modifier = Modifier.clickable { onBack() }.padding(6.dp),
                color = BookColors.TextBlack,
                fontSize = 18.sp
            )
            Spacer(Modifier.width(8.dp))
            Text("账号设置", color = BookColors.TextBlack, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("账号设置页面开发中", color = BookColors.TextGray, fontSize = 15.sp)
        }
    }
}

@Composable
private fun MineStatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = BookColors.TextBlack, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(label, color = BookColors.TextBlack.copy(alpha = 0.75f), fontSize = 15.sp)
    }
}

@Composable
private fun MineMenuCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BookColors.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun MineMenuRow(
    icon: ImageVector,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BookColors.BrandTealIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = BookColors.BrandTeal, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(title, color = BookColors.TextBlack, fontSize = 22.sp, modifier = Modifier.weight(1f))
        Text(">", color = BookColors.TextGray, fontSize = 22.sp)
    }
}

