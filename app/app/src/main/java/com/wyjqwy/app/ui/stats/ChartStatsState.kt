package com.wyjqwy.app.ui.stats

import com.wyjqwy.app.data.TransactionItem
import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * 图表页 UI 与按年缓存（切换周/月/年只做本地过滤，避免重复请求与白屏）。
 */
data class ChartStatsState(
    /** 1=支出 2=收入 */
    val billType: Int = 1,
    /** 0=周 1=月 2=年 */
    val periodOrdinal: Int = 1,
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedWeek: Int = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear()),
    /** 按自然年缓存的账单（Jan1 00:00 ~ 次年 Jan1 00:00） */
    val yearTransactions: Map<Int, List<TransactionItem>> = emptyMap(),
    val loadingYears: Set<Int> = emptySet(),
    val lastError: String? = null
)
