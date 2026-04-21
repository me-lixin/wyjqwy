package com.wyjqwy.app.ui.invest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wyjqwy.app.data.TransactionItem
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.BookColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun AutoInvestNoteDetailScreen(
    vm: AppViewModel,
    noteKey: String,
    noteDisplayName: String,
    listState: LazyListState,
    onBack: () -> Unit,
    onEditTransaction: (TransactionItem) -> Unit
) {
    val state by vm.autoInvest.collectAsState()
    val all = remember(state.yearTransactions) {
        state.yearTransactions.values.flatten()
    }
    val txList = remember(all, noteKey) {
        all.asSequence()
            .filter { it.type == 1 && it.categoryName.contains("投资", ignoreCase = true) }
            .filter { normalizedInvestNote(it.note) == noteKey }
            .sortedByDescending { it.parsedOccurredAt ?: LocalDate.MIN.atStartOfDay() }
            .toList()
    }
    val total = txList.sumOf { abs(it.amount) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BookColors.BrandTeal)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回", tint = BookColors.TextBlack)
            }
            Text(
                text = noteDisplayName,
                color = BookColors.TextBlack,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text("共 ${txList.size} 笔", color = BookColors.TextGray, fontSize = 13.sp)
            Spacer(Modifier.size(4.dp))
            Text("总金额 ¥${String.format("%.2f", total)}", color = BookColors.TextBlack, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = BookColors.Line)

        if (txList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("该备注下暂无明细", color = BookColors.TextGray)
            }
            return@Column
        }

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(txList, key = { it.id }) { tx ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEditTransaction(tx) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = tx.parsedOccurredAt?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) ?: tx.occurredAt,
                            color = BookColors.TextBlack,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.size(2.dp))
                        Text(
                            text = tx.categoryName,
                            color = BookColors.TextGray,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = "-${String.format("%.2f", abs(tx.amount))}",
                        color = BookColors.RedExpense,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                HorizontalDivider(color = BookColors.Line)
            }
        }
    }
}

private fun normalizedInvestNote(raw: String?): String {
    return raw?.trim()?.lowercase()?.takeIf { it.isNotBlank() } ?: "未备注"
}
