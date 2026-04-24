@file:OptIn(ExperimentalMaterial3Api::class)

package com.wyjqwy.app.ui.main

import android.graphics.BitmapFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.detail.DetailCalendarScreen
import com.wyjqwy.app.ui.detail.DetailScreen
import com.wyjqwy.app.ui.search.SearchScreen
import com.wyjqwy.app.ui.category.CategoryPickerScreen
import com.wyjqwy.app.ui.invest.AutoInvestNoteDetailScreen
import com.wyjqwy.app.ui.invest.AutoInvestScreen
import com.wyjqwy.app.ui.stats.CategoryStatsScreen
import com.wyjqwy.app.ui.stats.StatsDashboardScreen
import com.wyjqwy.app.ui.theme.DressUpScreen
import com.wyjqwy.app.ui.theme.DecoratedThemeIcon
import com.wyjqwy.app.ui.theme.ThemeBackgroundManager
import com.wyjqwy.app.ui.theme.ThemeUtils
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import java.time.LocalDate
import android.widget.FrameLayout
import androidx.compose.ui.platform.LocalContext

@Composable
fun MainShell(state: AppUiState, vm: AppViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showMineLogin by remember { mutableStateOf(false) }
    var mineFeaturePlaceholderTitle by remember { mutableStateOf<String?>(null) }
    var showDressUp by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<TransactionItem?>(null) }
    var statsTx by remember { mutableStateOf<TransactionItem?>(null) }
    var investNoteKey by remember { mutableStateOf<String?>(null) }
    var investNoteName by remember { mutableStateOf<String?>(null) }
    val detailListState = rememberLazyListState()
    val chartListState = rememberLazyListState()
    val autoInvestListState = rememberLazyListState()
    val autoInvestDetailListState = rememberLazyListState()
    val hasSubPage = showSearch ||
        showCalendar ||
        showMineLogin ||
        mineFeaturePlaceholderTitle != null ||
        showDressUp ||
        showCategoryPicker ||
        statsTx != null ||
        investNoteKey != null
    BackHandler(enabled = hasSubPage) {
        when {
            showCategoryPicker -> {
                showCategoryPicker = false
                editingTx = null
            }
            statsTx != null -> statsTx = null
            investNoteKey != null -> {
                investNoteKey = null
                investNoteName = null
            }
            showSearch -> showSearch = false
            showCalendar -> showCalendar = false
            showMineLogin -> showMineLogin = false
            mineFeaturePlaceholderTitle != null -> mineFeaturePlaceholderTitle = null
            showDressUp -> showDressUp = false
        }
    }

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
    if (showCalendar) {
        DetailCalendarScreen(
            state = state,
            onLoadMonth = { ym ->
                if (ym != state.selectedYearMonth) vm.loadTransactionsForMonth(ym)
            },
            onBack = { showCalendar = false },
            onEditTransaction = { tx ->
                editingTx = tx
                showCalendar = false
                showCategoryPicker = true
            },
            onOpenCategoryStats = { tx ->
                showCalendar = false
                statsTx = tx
            }
        )
        return
    }
    if (showMineLogin) {
        LaunchedEffect(state.loggedIn) {
            if (state.loggedIn) showMineLogin = false
        }
        BookkeepingLoginScreen(
            state = state,
            vm = vm,
            onBack = { showMineLogin = false }
        )
        return
    }
    mineFeaturePlaceholderTitle?.let { title ->
        MineFeaturePlaceholderScreen(
            title = title,
            onBack = { mineFeaturePlaceholderTitle = null }
        )
        return
    }
    if (showDressUp) {
        DressUpScreen(onBack = { showDressUp = false })
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
                    onOpenCalendar = { showCalendar = true },
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
                4 -> MineTabScreen(
                    state = state,
                    vm = vm,
                    onOpenLoginRegister = { showMineLogin = true },
                    onOpenImport = { mineFeaturePlaceholderTitle = "导入数据" },
                    onOpenExport = { mineFeaturePlaceholderTitle = "导出数据" },
                    onOpenDressUp = { showDressUp = true }
                )
                else -> DetailScreen(
                    state = state,
                    vm = vm,
                    onOpenSearch = { showSearch = true },
                    onOpenCalendar = { showCalendar = true },
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
    val primaryColor = rememberThemePrimaryColor()
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
                                .offset(y = (-20).dp)
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
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
                            tint = if (selected) primaryColor else BookColors.TextGray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            color = if (selected) primaryColor else BookColors.TextGray,
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MineTabScreen(
    state: AppUiState,
    vm: AppViewModel,
    onOpenLoginRegister: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenDressUp: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val mineTextScale = rememberMineTextScale()
    val context = LocalContext.current
    val selectedTexture = remember { ThemeUtils.getTextureType(context) }
    var showAccountSettings by remember { mutableStateOf(false) }
    val loginPhone = state.loginPhone
    var localProfile by remember(loginPhone) { mutableStateOf(loadProfileByPhone(context, loginPhone)) }
    val headerAvatarBitmap = remember(localProfile.avatarPath) {
        if (localProfile.avatarPath.isBlank()) null else BitmapFactory.decodeFile(localProfile.avatarPath)
    }
    LaunchedEffect(showAccountSettings, loginPhone) {
        if (!showAccountSettings) {
            localProfile = loadProfileByPhone(context, loginPhone)
        }
    }
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
    LaunchedEffect(state.loggedIn) {
        if (state.loggedIn) vm.ensureOverviewStatsLoaded()
    }
    val overviewDaysText = when {
        state.overviewTotalDays != null -> state.overviewTotalDays.toString()
        state.overviewLoading -> "..."
        else -> totalDays.toString()
    }
    val overviewCountText = when {
        state.overviewTotalCount != null -> state.overviewTotalCount.toString()
        state.overviewLoading -> "..."
        else -> totalCount.toString()
    }

    if (showAccountSettings) {
        MineAccountSettingsScreen(
            loginPhone = loginPhone,
            onBack = { showAccountSettings = false }
        )
        return
    }

    Column(Modifier.fillMaxSize()) {
        TextureBackgroundContainer(
            modifier = Modifier.fillMaxWidth(),
            type = selectedTexture,
            primaryColor = primaryColor
        ) {
            Column(
                Modifier
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
                                .clickable {
                                    if (state.loggedIn) showAccountSettings = true else onOpenLoginRegister()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (headerAvatarBitmap != null) {
                                Image(
                                    bitmap = headerAvatarBitmap.asImageBitmap(),
                                    contentDescription = "账号设置",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Person,
                                    contentDescription = "账号设置",
                                    tint = BookColors.TextGray,
                                    modifier = Modifier.size(42.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = if (state.loggedIn) {
                                localProfile.nickname.ifBlank { "未设置昵称" }
                            } else {
                                "点击登录"
                            },
                            color = BookColors.TextBlack,
                            style = MaterialTheme.typography.displaySmall.scaled(mineTextScale),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    MineStatCell(value = investCount.toString(), label = "定投次数", textScale = mineTextScale, modifier = Modifier.weight(1f))
                    MineStatCell(value = overviewDaysText, label = "记账总天数", textScale = mineTextScale, modifier = Modifier.weight(1f))
                    MineStatCell(value = overviewCountText, label = "记账总笔数", textScale = mineTextScale, modifier = Modifier.weight(1f))
                }
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
                    MineMenuRow(icon = Icons.Outlined.SwapHoriz, title = "导入数据", onClick = onOpenImport)
                    HorizontalDivider(color = BookColors.Line)
                    MineMenuRow(icon = Icons.Outlined.WorkOutline, title = "导出数据", onClick = onOpenExport)
                    HorizontalDivider(color = BookColors.Line)
                    MineMenuRow(icon = Icons.Outlined.Face, title = "个性装扮", onClick = onOpenDressUp)
                    HorizontalDivider(color = BookColors.Line)
                    MineMenuRow(
                        icon = Icons.Outlined.Person,
                        title = if (state.loggedIn) "退出登录" else "登录 / 注册",
                        onClick = {
                            if (state.loggedIn) vm.logout() else onOpenLoginRegister()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MineFeaturePlaceholderScreen(
    title: String,
    onBack: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val mineTextScale = rememberMineTextScale()
    val context = LocalContext.current
    val selectedTexture = remember { ThemeUtils.getTextureType(context) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TextureBackgroundContainer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            type = selectedTexture,
            primaryColor = primaryColor
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "返回",
                    tint = BookColors.TextBlack,
                    modifier = Modifier.size(24.dp).clickable { onBack() }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    color = BookColors.TextBlack,
                    style = MaterialTheme.typography.titleLarge.scaled(mineTextScale),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("${title}功能开发中", color = BookColors.TextGray, fontSize = 15.sp)
        }
    }
}

@Composable
private fun MineAccountSettingsPlaceholder(onBack: () -> Unit) {
    val primaryColor = rememberThemePrimaryColor()
    val mineTextScale = rememberMineTextScale()
    val context = LocalContext.current
    val selectedTexture = remember { ThemeUtils.getTextureType(context) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TextureBackgroundContainer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            type = selectedTexture,
            primaryColor = primaryColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "返回",
                    tint = BookColors.TextBlack,
                    modifier = Modifier.size(24.dp).clickable { onBack() }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "账号设置",
                    color = BookColors.TextBlack,
                    style = MaterialTheme.typography.titleLarge.scaled(mineTextScale),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("账号设置页面开发中", color = BookColors.TextGray, fontSize = 15.sp)
        }
    }
}

@Composable
private fun MineStatCell(value: String, label: String, textScale: Float, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            color = BookColors.TextBlack,
            style = MaterialTheme.typography.displaySmall.scaled(textScale),
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            color = BookColors.TextBlack.copy(alpha = 0.75f),
            style = MaterialTheme.typography.bodyLarge.scaled(textScale)
        )
    }
}

@Composable
private fun MineMenuCard(content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BookColors.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        content()
    }
}

@Composable
private fun TextureBackgroundContainer(
    modifier: Modifier = Modifier,
    type: ThemeBackgroundManager.TextureType,
    primaryColor: Color,
    content: @Composable () -> Unit
) {
    val manager = remember { ThemeBackgroundManager() }
    var measuredHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val measuredHeightDp = with(density) { measuredHeightPx.toDp() }
    Box(
        modifier = modifier.onSizeChanged {
            measuredHeightPx = it.height
        }
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(measuredHeightDp),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    manager.applyTextureToView(this, type, primaryColor.toArgb())
                }
            },
            update = { view ->
                manager.applyTextureToView(view, type, primaryColor.toArgb())
            }
        )
        content()
    }
}

@Composable
private fun MineMenuRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val mineTextScale = rememberMineTextScale()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            DecoratedThemeIcon(
                icon = icon,
                primaryColor = primaryColor,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            color = BookColors.TextBlack,
            style = MaterialTheme.typography.titleLarge.scaled(mineTextScale),
            modifier = Modifier.weight(1f)
        )
        Text(
            ">",
            color = BookColors.TextGray,
            style = MaterialTheme.typography.titleLarge.scaled(mineTextScale)
        )
    }
}

@Composable
private fun rememberMineTextScale(): Float {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp <= 360 -> 0.92f
        widthDp >= 420 -> 1.08f
        else -> 1f
    }
}

private fun TextStyle.scaled(scale: Float): TextStyle {
    return copy(fontSize = fontSize.scaled(scale), lineHeight = lineHeight.scaled(scale))
}

private fun TextUnit.scaled(scale: Float): TextUnit {
    return if (value.isNaN()) this else (value * scale).sp
}

