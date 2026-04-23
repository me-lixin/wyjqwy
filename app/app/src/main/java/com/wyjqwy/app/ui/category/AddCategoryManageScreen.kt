package com.wyjqwy.app.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.wyjqwy.app.ui.AppUiState
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.SubPageTopBar
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor

sealed class CategoryManageSheetRequest {
    data class Add(val isExpense: Boolean) : CategoryManageSheetRequest()
    data class Edit(
        val categoryId: Long,
        val type: Int,
        val oldName: String,
        val initialName: String,
        val initialIconKey: String
    ) : CategoryManageSheetRequest()
}

/**
 * 全屏弹层：添加 / 编辑支出、收入类别（图标分组 + 名称输入）。
 */
@Composable
fun AddCategoryManageDialog(
    request: CategoryManageSheetRequest,
    state: AppUiState,
    vm: AppViewModel,
    prefsStore: com.wyjqwy.app.data.PreferencesStore,
    onDismiss: () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val isEdit = request is CategoryManageSheetRequest.Edit
    val initialExpense = when (request) {
        is CategoryManageSheetRequest.Add -> request.isExpense
        is CategoryManageSheetRequest.Edit -> request.type == 1
    }
    var isExpense by remember { mutableStateOf(initialExpense) }
    var name by remember(request) {
        mutableStateOf(
            when (request) {
                is CategoryManageSheetRequest.Add -> ""
                is CategoryManageSheetRequest.Edit -> request.initialName
            }
        )
    }
    var selectedSlot by remember(request) {
        mutableStateOf(
            when (request) {
                is CategoryManageSheetRequest.Add -> firstSlotForExpenseIncome(initialExpense)
                is CategoryManageSheetRequest.Edit -> manageSlotForIconKey(
                    request.type == 1,
                    request.initialIconKey
                )
            }
        )
    }
    var localError by remember(request) { mutableStateOf("") }

    LaunchedEffect(isExpense) {
        if (!isEdit) {
            selectedSlot = firstSlotForExpenseIncome(isExpense)
        }
    }

    LaunchedEffect(state.message, state.loading, name, request) {
        if (!state.loading && (state.message == "分类已新增" || state.message == "分类已更新")) {
            val editReq = request as? CategoryManageSheetRequest.Edit
            if (state.message == "分类已更新" && editReq != null) {
                val newName = name.trim()
                if (newName.isNotEmpty() && newName != editReq.oldName) {
                    prefsStore.renameInCategoryOrderNow(editReq.type, editReq.oldName, newName)
                }
            }
            vm.clearUiMessage()
            onDismiss()
        }
    }

    val sections = if (isExpense) expenseManageIconSections else incomeManageIconSections
    val systemNames = remember(isExpense) {
        (if (isExpense) expenseCategoryPresets else incomeCategoryPresets).map { it.name }.toSet()
    }
    val canSubmit = name.isNotBlank() && name.length <= 4 && !state.loading

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = BookColors.White
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                AddCategoryTopBar(
                    isExpense = isExpense,
                    typeSwitchEnabled = !isEdit,
                    onExpenseClick = { if (!isEdit) isExpense = true },
                    onIncomeClick = { if (!isEdit) isExpense = false },
                    onCancel = onDismiss,
                    onDone = {
                        val n = name.trim()
                        if (n.isNotEmpty() && n.length <= 4) {
                            if (!isEdit && n in systemNames) {
                                localError = "系统默认分类不可自定义，请换个名称"
                                return@AddCategoryTopBar
                            }
                            localError = ""
                            val type = if (isExpense) 1 else 2
                            when (request) {
                                is CategoryManageSheetRequest.Add ->
                                    vm.addCategory(type = type, name = n, iconKey = selectedSlot.iconKey)
                                is CategoryManageSheetRequest.Edit ->
                                    vm.updateCategory(
                                        categoryId = request.categoryId,
                                        type = request.type,
                                        name = n,
                                        iconKey = selectedSlot.iconKey
                                    )
                            }
                        }
                    },
                    canSubmit = canSubmit
                )

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .background(primaryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = selectedSlot.icon,
                        contentDescription = null,
                        tint = BookColors.TextBlack,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                TextField(
                    value = name,
                    onValueChange = { if (it.length <= 4) name = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    placeholder = {
                        Text(
                            "输入类别名称（不超过6个汉字）",
                            color = BookColors.TextGray,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF0F0F0),
                        disabledContainerColor = Color(0xFFF0F0F0),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = BookColors.TextBlack,
                        unfocusedTextColor = BookColors.TextBlack,
                        cursorColor = BookColors.TextBlack,
                        focusedPlaceholderColor = BookColors.TextGray,
                        unfocusedPlaceholderColor = BookColors.TextGray
                    )
                )

                Spacer(Modifier.height(8.dp))

                if (!state.loading && state.message.startsWith("请求失败")) {
                    Text(
                        text = state.message,
                        color = BookColors.RedExpense,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (localError.isNotBlank()) {
                    Text(
                        text = localError,
                        color = BookColors.RedExpense,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    sections.forEach { section ->
                        item(key = "h_${section.title}") {
                            Text(
                                text = section.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                color = BookColors.TextGray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        val rows = section.slots.chunked(5)
                        items(rows.size, key = { "${section.title}_$it" }) { rowIndex ->
                            val row = rows[rowIndex]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                row.forEach { slot ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                if (slot.iconKey == selectedSlot.iconKey) {
                                                    primaryColor
                                                } else {
                                                    BookColors.CategoryGridCircle
                                                }
                                            )
                                            .clickable {
                                                selectedSlot = slot
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = slot.icon,
                                            contentDescription = slot.iconKey,
                                            tint = if (slot.iconKey == selectedSlot.iconKey) BookColors.White else BookColors.CategoryGridIcon,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }
                                repeat(5 - row.size) {
                                    Spacer(
                                        Modifier
                                            .weight(1f)
                                            .padding(4.dp)
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
private fun AddCategoryTopBar(
    isExpense: Boolean,
    typeSwitchEnabled: Boolean,
    onExpenseClick: () -> Unit,
    onIncomeClick: () -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    canSubmit: Boolean
) {
    val primaryColor = rememberThemePrimaryColor()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SubPageTopBar(
            title = "添加分类",
            onBack = onCancel,
            trailingContent = {
                TextButton(
                    onClick = { if (canSubmit) onDone() },
                    enabled = canSubmit
                ) {
                    Text(
                        text = "完成",
                        color = if (canSubmit) primaryColor else BookColors.TextGray,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "支出",
            fontSize = 17.sp,
            fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Normal,
            color = when {
                !typeSwitchEnabled && isExpense -> BookColors.TextBlack
                !typeSwitchEnabled -> BookColors.TextGray.copy(alpha = 0.45f)
                isExpense -> BookColors.TextBlack
                else -> BookColors.TextGray
            },
            modifier = Modifier
                .clickable(enabled = typeSwitchEnabled, onClick = onExpenseClick)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
        Text(
            text = "收入",
            fontSize = 17.sp,
            fontWeight = if (!isExpense) FontWeight.Bold else FontWeight.Normal,
            color = when {
                !typeSwitchEnabled && !isExpense -> BookColors.TextBlack
                !typeSwitchEnabled -> BookColors.TextGray.copy(alpha = 0.45f)
                !isExpense -> BookColors.TextBlack
                else -> BookColors.TextGray
            },
            modifier = Modifier
                .clickable(enabled = typeSwitchEnabled, onClick = onIncomeClick)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
