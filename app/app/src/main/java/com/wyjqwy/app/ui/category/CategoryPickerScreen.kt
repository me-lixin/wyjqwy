package com.wyjqwy.app.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import com.wyjqwy.app.data.Category
import com.wyjqwy.app.data.PreferencesStore
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.SubPageTopBar
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyGridState
import org.burnoutcrew.reorderable.reorderable
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

private data class CategoryDeleteMigrateSession(
    val deletingCategoryId: Long,
    val deletingCategoryName: String,
    val categoryType: Int,
    val pendingTransactionCount: Long
)

/**
 * 全屏：支出/收入大类切换 + 四列分类宫格；选分类后进入金额备注。
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryPickerScreen(
    state: AppUiState,
    vm: AppViewModel,
    initialTransaction: TransactionItem? = null,
    onBack: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    var tab by remember(initialTransaction?.id) {
        mutableIntStateOf(if (initialTransaction?.type == 2) 1 else 0)
    } // 0 支出 1 收入
    var selectedEntry by remember { mutableStateOf<CategoryPickerEntry?>(null) }
    var showNumberPad by remember(initialTransaction?.id) { mutableStateOf(initialTransaction != null) }
    var amountText by remember(initialTransaction?.id) {
        mutableStateOf(
            if (initialTransaction != null) formatAmountInput(initialTransaction.amount) else ""
        )
    }
    var note by remember(initialTransaction?.id) { mutableStateOf(initialTransaction?.note.orEmpty()) }
    var selectedDate by remember(initialTransaction?.id) {
        mutableStateOf(initialTransaction?.occurredAt?.toLocalDateOrNull() ?: LocalDate.now())
    }
    var selectedTime by remember(initialTransaction?.id) {
        mutableStateOf(initialTransaction?.occurredAt?.toLocalTimeOrNull() ?: LocalTime.now())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var categorySheet by remember { mutableStateOf<CategoryManageSheetRequest?>(null) }
    var editDeleteMode by remember { mutableStateOf(false) }
    var deleteCheckLoading by remember { mutableStateOf(false) }
    var migrateSession by remember { mutableStateOf<CategoryDeleteMigrateSession?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current.applicationContext
    val prefsStore = remember(context) { PreferencesStore(context) }

    LaunchedEffect(state.message, state.loading) {
        if (!state.loading && state.message.isNotEmpty() && state.message.startsWith("请求失败")) {
            snackbarHostState.showSnackbar(state.message, duration = SnackbarDuration.Long)
            vm.clearUiMessage()
        }
    }

    val txType = if (tab == 0) 1 else 2
    val presets = if (tab == 0) expenseCategoryPresets else incomeCategoryPresets
    val apiList = if (txType == 1) state.categoriesExpense else state.categoriesIncome
    val apiSignature = remember(txType, apiList) {
        apiList.joinToString("|") { "${it.id}_${it.name}_${it.icon}_${it.userId}" }
    }
    val orderedEntries = remember(txType) { mutableStateListOf<CategoryPickerEntry>() }

    LaunchedEffect(txType, apiSignature, presets) {
        val saved = prefsStore.getCategoryOrderNow(txType)
        val merged = mergedPickerEntries(txType, presets, apiList, saved)
        orderedEntries.clear()
        orderedEntries.addAll(merged)
        prefsStore.setCategoryOrderNow(txType, merged.map { it.name })
        launch { prefsStore.setCategoryOrder(txType, merged.map { it.name }) }
    }
    val hotNoteSuggestions = remember(state.transactions) {
        state.transactions
            .mapNotNull { it.note?.trim() }
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key.length })
            .map { it.key }
            .take(12)
    }
    val migrateTargetCategories = remember(
        migrateSession?.deletingCategoryId,
        migrateSession?.categoryType,
        state.categoriesExpense,
        state.categoriesIncome
    ) {
        val s = migrateSession ?: return@remember emptyList<Category>()
        val list = if (s.categoryType == 1) state.categoriesExpense else state.categoriesIncome
        list.filter { it.id != s.deletingCategoryId }
    }
    val currentYear = LocalDate.now().year
    val editingBottomInset = if (showNumberPad) 400.dp else 0.dp
    var initialApplied by remember(initialTransaction?.id) { mutableStateOf(false) }
    LaunchedEffect(initialTransaction?.id, txType, orderedEntries.size) {
        val tx = initialTransaction ?: return@LaunchedEffect
        if (initialApplied) return@LaunchedEffect
        if (tx.type != txType) return@LaunchedEffect
        val target = orderedEntries.firstOrNull {
            (it.serverId > 0L && it.serverId == tx.categoryId) || it.name == tx.categoryName
        } ?: return@LaunchedEffect
        selectedEntry = target
        showNumberPad = true
        initialApplied = true
    }
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            yearRange = (currentYear - 3)..currentYear
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        selectedDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(
                state = pickerState,
                showModeToggle = false,
                title = null,
                headline = null
                )
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(BookColors.White)
    ) {
    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .statusBarsPadding()
        ) {
            SubPageTopBar(
                title = if (initialTransaction == null) "添加记账" else "编辑记账",
                onBack = {
                    if (selectedEntry != null) {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        amountText = ""
                        note = ""
                        selectedEntry = null
                        showNumberPad = false
                    } else {
                        editDeleteMode = false
                        onBack()
                    }
                },
                modifier = Modifier.padding(horizontal = 4.dp),
                trailingContent = {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!editDeleteMode) {
                            TextButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    showNumberPad = false
                                    editDeleteMode = true
                                }
                            ) {
                                Text(
                                    "编辑",
                                    color = BookColors.TextBlack,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                if (editDeleteMode) {
                                    editDeleteMode = false
                                } else {
                                    categorySheet = CategoryManageSheetRequest.Add(tab == 0)
                                }
                            }
                        ) {
                            Text(
                                text = if (editDeleteMode) "完成" else "新增",
                                color = BookColors.TextBlack,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                CategoryTab(
                    label = "支出分类",
                    selected = tab == 0,
                    onClick = {
                        tab = 0
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        amountText = ""
                        note = ""
                        selectedEntry = null
                        showNumberPad = false
                        editDeleteMode = false
                    }
                )
                Spacer(Modifier.size(48.dp))
                CategoryTab(
                    label = "收入分类",
                    selected = tab == 1,
                    onClick = {
                        tab = 1
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        amountText = ""
                        note = ""
                        selectedEntry = null
                        showNumberPad = false
                        editDeleteMode = false
                    }
                )
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth()) {
            // 支出/收入必须各自一套 LazyGrid + Reorderable 状态，否则切换 tab 后库内拖拽状态与列表错位，出现碰撞断触
            key(txType) {
                val reorderState = rememberReorderableLazyGridState(
                    onMove = { from, to ->
                        if (from.index in orderedEntries.indices && to.index in orderedEntries.indices) {
                            orderedEntries.swap(from.index, to.index)
                        }
                    },
                    maxScrollPerFrame = 56.dp,
                    onDragEnd = { _, _ ->
                        prefsStore.setCategoryOrderNow(txType, orderedEntries.map { it.name })
                        scope.launch { prefsStore.setCategoryOrder(txType, orderedEntries.map { it.name }) }
                    }
                )

                LaunchedEffect(showNumberPad, selectedEntry?.name, orderedEntries.size) {
                    if (!showNumberPad) return@LaunchedEffect
                    val name = selectedEntry?.name ?: return@LaunchedEffect
                    val index = orderedEntries.indexOfFirst { it.name == name }
                    if (index >= 0) {
                        delay(48)
                        reorderState.gridState.scrollToItem(index)
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        state = reorderState.gridState,
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxSize()
                            .reorderable(reorderState)
                            .detectReorderAfterLongPress(reorderState)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 22.dp,
                            top = 16.dp,
                            bottom = 16.dp + editingBottomInset
                        )
                    ) {
                        items(orderedEntries, key = { "${txType}_${it.name}" }) { entry ->
                            ReorderableItem(
                                reorderState,
                                key = "${txType}_${entry.name}"
                            ) { isDragging ->
                                val showSideActions =
                                    editDeleteMode && entry.isUserCustom && entry.serverId > 0L
                                CategoryGridItem(
                                    modifier = dragVisualModifier(isDragging).fillMaxWidth(),
                                    label = entry.name,
                                    icon = entry.icon,
                                    selected = selectedEntry?.name == entry.name,
                                    showSideActions = showSideActions,
                                    bookkeepingEnabled = !editDeleteMode,
                                    onCategoryClick = {
                                        if (isDragging) return@CategoryGridItem
                                        val isSameCategory = selectedEntry?.name == entry.name
                                        selectedEntry = entry
                                        if (!isSameCategory) {
                                            amountText = ""
                                            note = ""
                                        }
                                        showNumberPad = true
                                        focusManager.clearFocus()
                                        keyboardController?.hide()
                                        val index = orderedEntries.indexOfFirst { it.name == entry.name }
                                        if (index >= 0) {
                                            scope.launch { reorderState.gridState.scrollToItem(index) }
                                        }
                                    },
                                    onEditClick = {
                                        categorySheet = CategoryManageSheetRequest.Edit(
                                            categoryId = entry.serverId,
                                            type = txType,
                                            oldName = entry.name,
                                            initialName = entry.name,
                                            initialIconKey = entry.iconKey
                                        )
                                    },
                                    onDeleteClick = {
                                        scope.launch {
                                            deleteCheckLoading = true
                                            var toastMsg: String? = null
                                            var toastLong = false
                                            try {
                                                val result = vm.tryDeleteCategory(entry.serverId)
                                                if (result.deleted) {
                                                    orderedEntries.removeAll { it.serverId == entry.serverId }
                                                    vm.loadHomeDataQuietly()
                                                    toastMsg = "分类已删除"
                                                } else {
                                                    migrateSession = CategoryDeleteMigrateSession(
                                                        deletingCategoryId = entry.serverId,
                                                        deletingCategoryName = entry.name,
                                                        categoryType = txType,
                                                        pendingTransactionCount = result.pendingTransactionCount
                                                    )
                                                }
                                            } catch (e: Exception) {
                                                toastMsg = "删除失败：${e.message ?: "未知错误"}"
                                                toastLong = true
                                            } finally {
                                                deleteCheckLoading = false
                                            }
                                            toastMsg?.let { msg ->
                                                snackbarHostState.showSnackbar(
                                                    msg,
                                                    duration = if (toastLong) SnackbarDuration.Long else SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    LazyGridScrollThumb(
                        state = reorderState.gridState,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(end = 6.dp, top = 8.dp, bottom = 8.dp + editingBottomInset)
                    )
                }
            }

            if (showNumberPad) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.22f))
                        .clickable {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                            showNumberPad = false
                        }
                )
            }

            if (showNumberPad) {
                selectedEntry?.let { entry ->
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(BookColors.White)
                        .navigationBarsPadding()
                ) {
                    Column {
                        val amountDisplay = formatAmountDisplay(amountText)
                        Text(
                            text = amountDisplay,
                            fontSize = 42.sp,
                            color = BookColors.TextBlack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            textAlign = TextAlign.End
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("备注：点击填写备注") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor
                            )
                        )
                        if (hotNoteSuggestions.isNotEmpty()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                hotNoteSuggestions.forEach { suggestion ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(BookColors.Background)
                                            .clickable {
                                                note = suggestion
                                                focusManager.clearFocus()
                                                keyboardController?.hide()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = suggestion,
                                            fontSize = 13.sp,
                                            color = BookColors.TextBlack
                                        )
                                    }
                                }
                            }
                        }
                        NumberPad(
                            loading = state.loading,
                            onInput = { key ->
                                amountText = appendAmountDigitOrDot(amountText, key)
                            },
                            onDelete = {
                                amountText = if (amountText.isNotEmpty()) amountText.dropLast(1) else ""
                            },
                            onPickDate = {
                                focusManager.clearFocus()
                                keyboardController?.hide()
                                showDatePicker = true
                            },
                            onOperator = { op ->
                                amountText = appendAmountOperator(amountText, op)
                            },
                            onDone = {
                                val amount = parseAmountForSubmit(amountText) ?: return@NumberPad
                                val occurredAt = LocalDateTime.of(selectedDate, selectedTime)
                                val editingTx = initialTransaction
                                if (editingTx != null) {
                                    vm.updateTransactionAfterCategory(
                                        transactionId = editingTx.id,
                                        type = txType,
                                        categoryName = entry.name,
                                        iconKey = entry.iconKey,
                                        amount = amount,
                                        note = note,
                                        occurredAt = occurredAt
                                    ) {
                                        onBack()
                                    }
                                } else {
                                    vm.addTransactionAfterCategory(
                                        type = txType,
                                        categoryName = entry.name,
                                        iconKey = entry.iconKey,
                                        amount = amount,
                                        note = note,
                                        occurredAt = occurredAt
                                    ) {
                                        onBack()
                                    }
                                }
                            }
                        )
                    }
                }
            }
            }
        }
    }
    categorySheet?.let { req ->
        AddCategoryManageDialog(
            request = req,
            state = state,
            vm = vm,
            prefsStore = prefsStore,
            onDismiss = { categorySheet = null }
        )
    }
    migrateSession?.let { session ->
        CategoryMigrateTargetDialog(
            session = session,
            targetCategories = migrateTargetCategories,
            migrating = state.loading,
            onDismiss = { if (!state.loading) migrateSession = null },
            onMigrate = { targetId ->
                vm.migrateAndDeleteCategory(
                    sourceCategoryId = session.deletingCategoryId,
                    targetCategoryId = targetId
                ) {
                    migrateSession = null
                    scope.launch {
                        snackbarHostState.showSnackbar("账单已迁移并删除分类")
                    }
                    vm.clearUiMessage()
                }
            }
        )
    }
    if (deleteCheckLoading) {
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(8f)
                .background(Color.Black.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryColor)
        }
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 72.dp)
    )
    }
}

/** 拆出「最后一个运算数」之前的部分与当前正在输入的数（含前导负号） */
private fun splitLastAmountSegment(expr: String): Pair<String, String> {
    if (expr.isEmpty()) return "" to ""
    var lastOp = -1
    for (i in expr.indices.reversed()) {
        val c = expr[i]
        if (c == '+' || c == '-') {
            if (c == '-' && i == 0) break
            lastOp = i
            break
        }
    }
    if (lastOp < 0) return "" to expr
    return expr.substring(0, lastOp + 1) to expr.substring(lastOp + 1)
}

private fun appendAmountDigitOrDot(current: String, key: String): String {
    val (prefix, segment) = splitLastAmountSegment(current)
    val newSegment = appendNumberInSegment(segment, key)
    return prefix + newSegment
}

private fun appendNumberInSegment(segment: String, key: String): String {
    if (key == "." && segment.contains(".")) return segment
    if (key == "." && segment.isEmpty()) return "0."
    if (segment == "0" && key != ".") return key
    if (segment == "-0" && key != ".") return "-$key"
    val candidate = segment + key
    val dotIndex = candidate.indexOf('.')
    if (dotIndex >= 0 && candidate.length - dotIndex - 1 > 2) return segment
    return candidate
}

private fun appendAmountOperator(current: String, op: String): String {
    val ch = op.first()
    if (current.isEmpty()) {
        return if (ch == '-') "-" else ""
    }
    val last = current.last()
    if (last == '+' || last == '-') {
        return current.dropLast(1) + ch
    }
    if (last.isDigit() || last == '.') {
        return current + ch
    }
    return current
}

/**
 * 仅支持 +、-，从左到右计算（如 66+4=70，10-3+2=9）。
 * 未完成表达式（末尾为运算符或孤立的点）返回 null。
 */
private fun evaluateAmountExpression(expr: String): Double? {
    val t = expr.trim()
    if (t.isEmpty()) return null
    if (t.endsWith("+") || t.endsWith("-")) return null
    if (t.endsWith(".")) return null
    var pos = 0
    fun parseDouble(): Double? {
        val start = pos
        if (pos < t.length && t[pos] == '-') pos++
        if (pos >= t.length) return null
        if (t[pos] == '.') return null
        while (pos < t.length && t[pos].isDigit()) pos++
        if (pos < t.length && t[pos] == '.') {
            pos++
            while (pos < t.length && t[pos].isDigit()) pos++
        }
        val sub = t.substring(start, pos)
        if (sub == "-" || sub == "-." || sub.endsWith(".")) return null
        return sub.toDoubleOrNull()
    }
    var acc = parseDouble() ?: return null
    while (pos < t.length) {
        val op = t[pos]
        if (op != '+' && op != '-') return null
        pos++
        val next = parseDouble() ?: return null
        acc = if (op == '+') acc + next else acc - next
    }
    return acc
}

private fun formatAmountDisplay(expr: String): String {
    if (expr.isBlank()) return "0"
    val v = evaluateAmountExpression(expr)
    return if (v != null) formatAmountInput(v) else expr
}

private fun parseAmountForSubmit(raw: String): Double? {
    if (raw.isBlank()) return null
    evaluateAmountExpression(raw)?.let { return it }
    val normalized = if (raw.endsWith(".")) raw.dropLast(1) else raw
    if (normalized.isBlank()) return null
    return normalized.toDoubleOrNull()
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return try {
        LocalDateTime.parse(this).toLocalDate()
    } catch (_: Exception) {
        null
    }
}

private fun String.toLocalTimeOrNull(): LocalTime? {
    return try {
        LocalDateTime.parse(this).toLocalTime()
    } catch (_: Exception) {
        null
    }
}

private fun formatAmountInput(value: Double): String {
    return BigDecimal.valueOf(value)
        .setScale(2, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}

private fun <T> MutableList<T>.swap(i: Int, j: Int) {
    if (i == j) return
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

@Composable
private fun dragVisualModifier(isDragging: Boolean): Modifier {
    val baseScale by animateFloatAsState(
        targetValue = if (isDragging) 1.08f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "dragScale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (isDragging) 0.84f else 1f,
        animationSpec = tween(durationMillis = 120),
        label = "dragAlpha"
    )
    val breath = if (isDragging) {
        val transition = rememberInfiniteTransition(label = "dragBreath")
        transition.animateFloat(
            initialValue = 0.985f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 520),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dragBreathScale"
        ).value
    } else 1f
    return Modifier
        .zIndex(if (isDragging) 1f else 0f)
        .graphicsLayer {
            scaleX = baseScale * breath
            scaleY = baseScale * breath
            this.alpha = alpha
        }
}

@Composable
private fun NumberPad(
    loading: Boolean,
    onInput: (String) -> Unit,
    onDelete: () -> Unit,
    onPickDate: () -> Unit,
    onOperator: (String) -> Unit,
    onDone: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val rows = listOf(
        listOf("7", "8", "9", "DEL"),
        listOf("4", "5", "6", "+"),
        listOf("1", "2", "3", "-"),
        listOf(".", "0", "DATE", "完成")
    )
    Column(Modifier.fillMaxWidth()) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth()) {
                row.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .background(
                                when (key) {
                                    "完成" -> primaryColor
                                    else -> Color(0xFFF7F7F7)
                                }
                            )
                            .clickable(enabled = !loading) {
                                when (key) {
                                    "DEL" -> onDelete()
                                    "+" -> onOperator("+")
                                    "-" -> onOperator("-")
                                    "完成" -> onDone()
                                    "DATE" -> onPickDate()
                                    else -> onInput(key)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (key == "DEL") {
                            Icon(
                                imageVector = Icons.Outlined.Backspace,
                                contentDescription = "删除",
                                tint = BookColors.TextBlack
                            )
                        } else if (key == "DATE") {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = "选择日期",
                                tint = BookColors.TextBlack
                            )
                        } else {
                            Text(
                                text = key,
                                color = if (key == "完成") BookColors.White else BookColors.TextBlack,
                                fontWeight = if (key == "完成") FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 24.sp
                            )
                        }
                    }
                }
            }
        }
        if (loading) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = primaryColor,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
private fun LazyGridScrollThumb(
    state: LazyGridState,
    modifier: Modifier = Modifier
) {
    val layoutInfo = state.layoutInfo
    val total = layoutInfo.totalItemsCount
    if (total <= 0) return
    val visible = layoutInfo.visibleItemsInfo
    if (visible.isEmpty()) return
    val visibleCount = visible.size
    if (visibleCount >= total) return
    val density = LocalDensity.current
    val viewportH = layoutInfo.viewportSize.height.toFloat()
    if (viewportH <= 0f) return
    val thumbHeightPx = viewportH * (visibleCount.toFloat() / total.toFloat())
    val minPx = with(density) { 28.dp.toPx() }
    val h = thumbHeightPx.coerceAtLeast(minPx).coerceAtMost(viewportH)
    val maxTravel = (viewportH - h).coerceAtLeast(0f)
    val denom = (total - visibleCount).coerceAtLeast(1)
    val offsetPx = (state.firstVisibleItemIndex.toFloat() / denom) * maxTravel
    Box(
        modifier = modifier.width(5.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(with(density) { h.toDp() })
                .offset(y = with(density) { offsetPx.toDp() })
                .clip(RoundedCornerShape(2.5.dp))
                .background(BookColors.TextGray.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun CategoryMigrateTargetDialog(
    session: CategoryDeleteMigrateSession,
    targetCategories: List<Category>,
    migrating: Boolean,
    onDismiss: () -> Unit,
    onMigrate: (Long) -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    var selectedId by remember(session.deletingCategoryId) { mutableStateOf<Long?>(null) }
    val typeLabel = if (session.categoryType == 1) "支出" else "收入"

    Dialog(onDismissRequest = { if (!migrating) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BookColors.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
                Text(
                    text = "无法直接删除",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BookColors.TextBlack
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "「${session.deletingCategoryName}」下仍有 ${session.pendingTransactionCount} 笔账单，请选择迁入的${typeLabel}分类后迁移并删除。",
                    fontSize = 14.sp,
                    color = BookColors.TextBlack,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(12.dp))
                if (migrating) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = primaryColor
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Text(
                    text = "可选${typeLabel}分类",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BookColors.TextBlack
                )
                Spacer(Modifier.height(6.dp))
                if (targetCategories.isEmpty()) {
                    Text(
                        text = "暂无其他同类型分类，请先新增一个分类后再删除本分类。",
                        fontSize = 14.sp,
                        color = BookColors.TextGray
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 360.dp)
                    ) {
                        items(targetCategories, key = { it.id }) { cat ->
                            CategoryMigrateSelectableRow(
                                name = cat.name,
                                selected = selectedId == cat.id,
                                onClick = { selectedId = cat.id }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !migrating
                    ) {
                        Text("取消", color = BookColors.TextBlack)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val id = selectedId ?: return@Button
                            onMigrate(id)
                        },
                        enabled = selectedId != null && !migrating && targetCategories.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = BookColors.White,
                            disabledContainerColor = BookColors.TextGray.copy(alpha = 0.35f),
                            disabledContentColor = BookColors.White.copy(alpha = 0.8f)
                        )
                    ) {
                        Text("迁移")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryMigrateSelectableRow(
    name: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = name,
            fontSize = 15.sp,
            color = BookColors.TextBlack
        )
    }
}

@Composable
private fun CategoryTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = BookColors.TextBlack.copy(alpha = if (selected) 1f else 0.65f)
        )
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .width(56.dp)
                .height(3.dp)
                .background(if (selected) Color(0xFF1A1A1A) else Color.Transparent)
        )
    }
}

@Composable
private fun CategoryGridItem(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    selected: Boolean,
    showSideActions: Boolean,
    bookkeepingEnabled: Boolean,
    onCategoryClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val iconSize = 52.dp
    val sideBtnSize = 28.dp
    val sideGap = (-8).dp
    val sideOffset = iconSize / 2 + sideGap + sideBtnSize / 2

    Box(modifier = modifier.padding(vertical = 4.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(iconSize),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(iconSize)
                        .clip(CircleShape)
                        .background(
                            if (selected) primaryColor
                            else BookColors.CategoryGridCircle
                        )
                        .then(
                            if (bookkeepingEnabled) {
                                Modifier.clickable(onClick = onCategoryClick)
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) BookColors.White else BookColors.CategoryGridIcon,
                        modifier = Modifier.size(26.dp)
                    )
                }
                if (showSideActions) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = -sideOffset,y = (-20).dp)
                            .zIndex(1f)
                            .size(sideBtnSize)
                            .clip(CircleShape)
                            .background(BookColors.RedExpense.copy(alpha = 0.4f))
                            .clickable(onClick = onDeleteClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "删除",
                            tint = BookColors.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = sideOffset,y = (-20).dp)
                            .zIndex(1f)
                            .size(sideBtnSize)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.4f))
                            .clickable(onClick = onEditClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "编辑",
                            tint = BookColors.TextBlack,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = if (selected) primaryColor else BookColors.TextBlack,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = if (bookkeepingEnabled) {
                    Modifier.clickable(onClick = onCategoryClick)
                } else {
                    Modifier
                }
            )
        }
    }
}
