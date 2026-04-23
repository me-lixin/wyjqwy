package com.wyjqwy.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.ui.theme.BookColors
import com.wyjqwy.app.ui.theme.rememberThemePrimaryColor
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearMonthPickerSheet(
    initial: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    val primaryColor = rememberThemePrimaryColor()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var year by remember { mutableIntStateOf(initial.year) }
    var month by remember { mutableIntStateOf(initial.monthValue) }
    val years = remember { (2000..2100).toList() }
    val months = remember { (1..12).toList() }
    val yearList = rememberLazyListState()
    val monthList = rememberLazyListState()

    LaunchedEffect(Unit) {
        yearList.scrollToItem(years.indexOf(year).coerceAtLeast(0))
        monthList.scrollToItem(month - 1)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BookColors.White,
        dragHandle = null
    ) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("取消", color = primaryColor, fontSize = 15.sp)
                }
                Text("选择日期", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = BookColors.TextBlack)
                TextButton(onClick = { onConfirm(YearMonth.of(year, month)); onDismiss() }) {
                    Text("确定", color = primaryColor, fontSize = 15.sp)
                }
            }
            HorizontalDivider(color = BookColors.Line)
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    LazyColumn(
                        state = yearList,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(years) { y ->
                            val selected = y == year
                            TextButton(onClick = { year = y }) {
                                Text(
                                    "$y 年",
                                    color = if (selected) BookColors.TextBlack else BookColors.TextGray,
                                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                    fontSize = if (selected) 18.sp else 15.sp
                                )
                            }
                        }
                    }
                }
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    LazyColumn(
                        state = monthList,
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(months) { m ->
                            val selected = m == month
                            TextButton(onClick = { month = m }) {
                                Text(
                                    String.format("%02d 月", m),
                                    color = if (selected) BookColors.TextBlack else BookColors.TextGray,
                                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                                    fontSize = if (selected) 18.sp else 15.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
