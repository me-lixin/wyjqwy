package com.wyjqwy.app.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.commandiron.wheel_picker_compose.core.WheelTextPicker
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import java.time.LocalDate
import java.time.temporal.WeekFields

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearOnlyPickerSheet(
    initialYear: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val currentYear = LocalDate.now().year
    // 生成近10年的列表，反转顺序让最近的年份在最上面
    val yearRange = remember(currentYear) { (currentYear - 4..currentYear).toList().reversed() }
    val yearsStr = remember(yearRange) { yearRange.map { "${it}年" } }

    // 记录最终选中的年份
    var finalYear by remember { mutableIntStateOf(initialYear) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    StatsPickerSheetFrame(
        title = "选择年份",
        sheetState = sheetState,
        onDismiss = onDismiss,
        onConfirm = {
            onConfirm(finalYear)
            onDismiss()
        }
    ) {
        Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
            WheelTextPicker(
                texts = yearsStr,
                rowCount = 5, // 👈 修复点：明确告诉组件显示 5 行
                startIndex = yearRange.indexOf(finalYear).coerceAtLeast(0),
                onScrollFinished = { snappedIndex ->
                    finalYear = yearRange[snappedIndex]
                    return@WheelTextPicker null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearMonthPickerSheet(
    selectedYear: Int,
    initialMonth: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val monthsStr = remember { (1..12).map { "${it}月" } }
    var finalMonth by remember { mutableIntStateOf(initialMonth) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    StatsPickerSheetFrame(
        title = "选择年月",
        sheetState = sheetState,
        onDismiss = onDismiss,
        onConfirm = {
            onConfirm(selectedYear, finalMonth)
            onDismiss()
        }
    ) {
        Row(Modifier.fillMaxWidth().height(220.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 左侧固定显示的年份
            Box(
                Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${selectedYear}年",
                    color = BookColors.TextBlack,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // 右侧滚动的月份
            Box(
                Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                WheelTextPicker(
                    texts = monthsStr,
                    rowCount = 5, // 👈 修复点：明确告诉组件显示 5 行
                    startIndex = (finalMonth - 1).coerceIn(0, 11),
                    onScrollFinished = { snappedIndex ->
                        finalMonth = snappedIndex + 1
                        return@WheelTextPicker null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearWeekPickerSheet(
    selectedYear: Int,
    initialWeek: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val weekField = WeekFields.ISO.weekOfWeekBasedYear()
    val maxWeek = remember(selectedYear) { LocalDate.of(selectedYear, 12, 28).get(weekField) }
    val weeksStr = remember(maxWeek) { (1..maxWeek).map { "第${it}周" } }

    var finalWeek by remember { mutableIntStateOf(initialWeek) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    StatsPickerSheetFrame(
        title = "选择年周",
        sheetState = sheetState,
        onDismiss = onDismiss,
        onConfirm = {
            onConfirm(selectedYear, finalWeek)
            onDismiss()
        }
    ) {
        Row(Modifier.fillMaxWidth().height(220.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // 左侧固定年份
            Box(
                Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${selectedYear}年",
                    color = BookColors.TextBlack,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            // 右侧滚轮选择周数
            Box(
                Modifier.weight(1f).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                WheelTextPicker(
                    texts = weeksStr,
                    rowCount = 5, // 👈 修复点：明确告诉组件显示 5 行
                    startIndex = (finalWeek - 1).coerceIn(0, maxWeek - 1),
                    onScrollFinished = { snappedIndex ->
                        finalWeek = snappedIndex + 1
                        return@WheelTextPicker null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsPickerSheetFrame(
    title: String,
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookColors.White,
        dragHandle = null
    ) {
        Column(Modifier.navigationBarsPadding().padding(bottom = 20.dp)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) { Text("取消", color = primaryColor) }
                Text(title, color = BookColors.TextBlack, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onConfirm) { Text("确定", color = primaryColor) }
            }
            HorizontalDivider(color = BookColors.Line)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}