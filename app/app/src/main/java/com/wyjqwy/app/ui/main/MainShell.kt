@file:OptIn(ExperimentalMaterial3Api::class)

package com.wyjqwy.app.ui.main

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.category.CategoryPickerScreen
import com.wyjqwy.app.ui.detail.DetailCalendarScreen
import com.wyjqwy.app.ui.detail.DetailScreen
import com.wyjqwy.app.ui.invest.AutoInvestNoteDetailScreen
import com.wyjqwy.app.ui.invest.AutoInvestScreen
import com.wyjqwy.app.ui.search.SearchScreen
import com.wyjqwy.app.ui.stats.CategoryStatsScreen
import com.wyjqwy.app.ui.stats.StatsDashboardScreen
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.DressUpScreen
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// 增强版辅助函数：严格限制4个字符宽度，超过则省略
@Composable
fun TruncatedText(
    text: String,
    color: Color = BookColors.TextBlack,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.widthIn(max = 65.dp) // 强制在大约4个汉字处截断
    )
}

@Composable
fun MainShell(state: AppUiState, vm: AppViewModel) {
    val context = LocalContext.current
    val primaryColor = rememberThemePrimaryColor()
    var tab by remember { mutableIntStateOf(0) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showCalendar by remember { mutableStateOf(false) }
    var showMineLogin by remember { mutableStateOf(false) }
    var showMineAccountSettings by remember { mutableStateOf(false) }
    var mineProfileRefreshTick by remember { mutableIntStateOf(0) }
    var mineFeaturePlaceholderTitle by remember { mutableStateOf<String?>(null) }
    var showDressUp by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<TransactionItem?>(null) }
    var statsTx by remember { mutableStateOf<TransactionItem?>(null) }
    var statsScopedTransactions by remember { mutableStateOf<List<TransactionItem>?>(null) }
    var statsLoading by remember { mutableStateOf(false) }
    var investNoteKey by remember { mutableStateOf<String?>(null) }
    var investNoteName by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    var voiceArmVisible by remember { mutableStateOf(false) }
    var voiceModeVisible by remember { mutableStateOf(false) }
    var voiceMicSelected by remember { mutableStateOf(false) }
    var centerAddWindowCenter by remember { mutableStateOf<Offset?>(null) }
    var voiceInputText by remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val voiceInputFocusRequester = remember { FocusRequester() }

    LaunchedEffect(voiceModeVisible) {
        if (voiceModeVisible) {
            voiceInputFocusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val density = LocalDensity.current
    val micSizeDp = 52.dp
    val micGapDp = 18.dp
    val micSizePx = with(density) { micSizeDp.toPx() }
    val micGapPx = with(density) { micGapDp.toPx() }
    val micCenter = centerAddWindowCenter?.let { Offset(it.x, it.y - micSizePx - micGapPx) }

    fun updateMicSelection(pointer: Offset?) {
        val currentCenter = centerAddWindowCenter?.let { Offset(it.x, it.y - micSizePx - micGapPx) }
        if (!voiceArmVisible || pointer == null || currentCenter == null) {
            voiceMicSelected = false
            return
        }
        val dx = pointer.x - currentCenter.x
        val dy = pointer.y - currentCenter.y
        val dist = sqrt(dx * dx + dy * dy)
        voiceMicSelected = dist <= micSizePx * 1.8f
    }

    val detailListState = rememberLazyListState()
    val chartListState = rememberLazyListState()
    val autoInvestListState = rememberLazyListState()
    val autoInvestDetailListState = rememberLazyListState()

    val hasSubPage = showSearch || showCalendar || showMineLogin ||
            mineFeaturePlaceholderTitle != null || showDressUp ||
            showCategoryPicker || statsTx != null || investNoteKey != null ||
            showMineAccountSettings
    var lastExitGestureAt by remember { mutableLongStateOf(0L) }

    BackHandler(enabled = hasSubPage) {
        when {
            showCategoryPicker -> { showCategoryPicker = false; editingTx = null }
            statsTx != null -> {
                statsTx = null
                statsScopedTransactions = null
                statsLoading = false
            }
            investNoteKey != null -> { investNoteKey = null; investNoteName = null }
            showSearch -> showSearch = false
            showCalendar -> showCalendar = false
            showMineLogin -> showMineLogin = false
            showMineAccountSettings -> showMineAccountSettings = false
            mineFeaturePlaceholderTitle != null -> mineFeaturePlaceholderTitle = null
            showDressUp -> showDressUp = false
        }
    }
    BackHandler(enabled = !hasSubPage) {
        val now = System.currentTimeMillis()
        if (now - lastExitGestureAt <= 1000L) {
            (context as? Activity)?.finish()
        } else {
            lastExitGestureAt = now
            Toast.makeText(context, "请在1秒内再滑一次退出应用", Toast.LENGTH_SHORT).show()
        }
    }

    // ... (中间页面跳转逻辑保持原样)
    if (showSearch) { SearchScreen(vm = vm, amountVisible = state.amountVisible, onBack = { showSearch = false }, onEditTransaction = { tx -> editingTx = tx; showSearch = false; showCategoryPicker = true }, onDeleteTransaction = { tx -> vm.deleteTransaction(tx.id) }); return }
    if (showCalendar) { DetailCalendarScreen(state = state, onLoadMonth = { ym -> if (ym != state.selectedYearMonth) vm.loadTransactionsForMonth(ym) }, onBack = { showCalendar = false }, onEditTransaction = { tx -> editingTx = tx; showCalendar = false; showCategoryPicker = true }, onOpenCategoryStats = { tx -> showCalendar = false; statsScopedTransactions = null; statsLoading = false; statsTx = tx }); return }
    if (showMineLogin) { LaunchedEffect(state.loggedIn) { if (state.loggedIn) showMineLogin = false }; BookkeepingLoginScreen(state = state, vm = vm, onBack = { showMineLogin = false }); return }
    if (showMineAccountSettings) {
        MineAccountSettingsScreen(
            loginPhone = state.loginPhone,
            onBack = {
                showMineAccountSettings = false
                mineProfileRefreshTick++
            }
        )
        return
    }
    mineFeaturePlaceholderTitle?.let { title -> MineFeaturePlaceholderScreen(title = title, onBack = { mineFeaturePlaceholderTitle = null }); return }
    if (showDressUp) { DressUpScreen(onBack = { showDressUp = false }); return }
    if (showCategoryPicker) { CategoryPickerScreen(state = state, vm = vm, initialTransaction = editingTx, onBack = { showCategoryPicker = false; editingTx = null }); return }
    statsTx?.let { tx ->
        CategoryStatsScreen(
            state = state,
            vm = vm,
            seedTx = tx,
            scopedTransactions = statsScopedTransactions,
            loading = statsLoading,
            onBack = {
                statsTx = null
                statsScopedTransactions = null
                statsLoading = false
            }
        ) { editTx -> editingTx = editTx; showCategoryPicker = true }
        return
    }
    investNoteKey?.let { key -> AutoInvestNoteDetailScreen(vm = vm, noteKey = key, noteDisplayName = investNoteName ?: "未备注", listState = autoInvestDetailListState, onBack = { investNoteKey = null; investNoteName = null }, onEditTransaction = { tx -> editingTx = tx; showCategoryPicker = true }); return }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = BookColors.Background,
            contentWindowInsets = WindowInsets.navigationBars,
            bottomBar = {
                SharkBottomBar(
                    selectedIndex = tab,
                    onSelect = { tab = it },
                    onCenterAdd = { editingTx = null; showCategoryPicker = true },
                    onCenterAddPositionChanged = { centerAddWindowCenter = it },
                    onVoiceGestureStart = { voiceArmVisible = true; voiceMicSelected = false },
                    onVoiceGestureMove = { pointer -> updateMicSelection(pointer) },
                    onVoiceGestureEnd = {
                        val shouldTrigger = voiceMicSelected
                        voiceArmVisible = false
                        voiceMicSelected = false
                        if (shouldTrigger) {
                            voiceInputText = ""
                            voiceModeVisible = true
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (tab) {
                    0 -> DetailScreen(state = state, vm = vm, onOpenSearch = { showSearch = true }, onOpenCalendar = { showCalendar = true }, onOpenEditTransaction = { tx -> editingTx = tx; showCategoryPicker = true }, onOpenCategoryStats = { tx -> statsScopedTransactions = null; statsLoading = false; statsTx = tx }, listState = detailListState)
                    1 -> StatsDashboardScreen(
                        vm = vm,
                        rankListState = chartListState,
                        onOpenCategoryStats = { tx, from, to ->
                            statsTx = tx
                            statsLoading = true
                            statsScopedTransactions = emptyList()
                            scope.launch {
                                runCatching { vm.fetchTransactionsForRange(from, to) }
                                    .onSuccess { list ->
                                        statsScopedTransactions = list
                                    }
                                    .onFailure {
                                        statsScopedTransactions = emptyList()
                                        Toast.makeText(context, "加载分类明细失败，请重试", Toast.LENGTH_SHORT).show()
                                    }
                                statsLoading = false
                            }
                        }
                    )
                    3 -> AutoInvestScreen(vm = vm, listState = autoInvestListState, onOpenNoteDetails = { key, name -> investNoteKey = key; investNoteName = name })
                    4 -> MineTabScreen(
                        state = state,
                        vm = vm,
                        onOpenLoginRegister = { showMineLogin = true },
                        onOpenImport = { mineFeaturePlaceholderTitle = "导入数据" },
                        onOpenExport = { mineFeaturePlaceholderTitle = "导出数据" },
                        onOpenDressUp = { showDressUp = true },
                        onOpenAccountSettings = { showMineAccountSettings = true },
                        profileRefreshTick = mineProfileRefreshTick
                    )
                    else -> DetailScreen(state = state, vm = vm, onOpenSearch = { showSearch = true }, onOpenCalendar = { showCalendar = true }, onOpenEditTransaction = { tx -> editingTx = tx; showCategoryPicker = true }, onOpenCategoryStats = { tx -> statsScopedTransactions = null; statsLoading = false; statsTx = tx }, listState = detailListState)
                }
            }
        }

        if (voiceArmVisible || voiceModeVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(enabled = voiceModeVisible) {
                        voiceModeVisible = false
                        keyboardController?.hide()
                    }
            )
        }

        if (voiceArmVisible && micCenter != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                Box(
                    modifier = Modifier
                        .offset { androidx.compose.ui.unit.IntOffset((micCenter.x - micSizePx / 2f).toInt(), (micCenter.y - micSizePx / 2f).toInt()) }
                        .size(micSizeDp)
                        .clip(CircleShape)
                        .background(if (voiceMicSelected) primaryColor else primaryColor.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
            }
        }

        // --- 对话记账卡片 ---
        if (voiceModeVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding() // 核心修复：随键盘自动上弹
                    .navigationBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-40).dp)
                        .padding(horizontal = 40.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.size(26.dp).background(primaryColor.copy(0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.ChatBubbleOutline, null, tint = primaryColor, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        // 标题：保留4字
                        TruncatedText("对话记账", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = voiceInputText,
                        onValueChange = { voiceInputText = it },
                        modifier = Modifier.fillMaxWidth().focusRequester(voiceInputFocusRequester),
                        placeholder = { Text("例:吃晚饭50元", color = Color.Gray, fontSize = 12.sp) },
                        maxLines = 2,
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8F8F8),
                            unfocusedContainerColor = Color(0xFFF8F8F8),
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = primaryColor
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { voiceModeVisible = false; keyboardController?.hide() },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2), contentColor = Color.Gray)
                        ) {
                            // 按钮：保留4字
                            TruncatedText("取消", fontSize = 13.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                val payload = voiceInputText.trim()
                                // 逻辑：长度必须大于2才发送
                                if (payload.length > 2) {
                                    Toast.makeText(context, "正在生成记账中...", Toast.LENGTH_SHORT).show()
                                    vm.submitVoiceAccounting(
                                        voiceText = payload,
                                        onFinished = { ok ->
                                            Toast.makeText(
                                                context,
                                                if (ok) "记账已完成" else "记账失败，请重试",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                    voiceModeVisible = false
                                    voiceInputText = ""
                                    keyboardController?.hide()
                                } else {
                                    Toast.makeText(context, "内容太短，请多说一点", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            // 按钮：保留4字
                            TruncatedText("发送", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}