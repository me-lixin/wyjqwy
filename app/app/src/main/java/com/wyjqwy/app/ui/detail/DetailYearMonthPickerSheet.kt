package com.wyjqwy.app.ui.detail

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
import com.commandiron.wheel_picker_compose.core.WheelTextPicker
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailYearMonthPickerSheet(
    initial: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val currentYear = LocalDate.now().year
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
                TextButton(onClick = onDismiss) { Text("取消", color = primaryColor) }
                Text("选择年月", color = BookColors.TextBlack, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = { onConfirm(YearMonth.of(finalYear, finalMonth)) }) { Text("确定", color = primaryColor) }
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
