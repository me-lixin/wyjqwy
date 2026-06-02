package com.wyjqwy.app.ui.category

import com.wyjqwy.app.data.TransactionItem

private const val MAX_QUICK_NOTE_SUGGESTIONS = 8

/**
 * 仅基于用户历史习惯构建快捷备注：
 * - 按收支类型过滤（txType）
 * - 仅统计非空备注
 * - 按使用次数倒序，最多 [MAX_QUICK_NOTE_SUGGESTIONS] 条
 * [txType]：1 支出，2 收入。
 */
fun buildQuickNoteSuggestions(
    transactions: List<TransactionItem>,
    txType: Int
): List<String> {
    return transactions
        .asSequence()
        .filter { it.type == txType }
        .mapNotNull { it.note?.trim() }
        .filter { it.isNotBlank() }
        .groupingBy { it }
        .eachCount()
        .entries
        .sortedByDescending { it.value }
        .map { it.key }
        .take(MAX_QUICK_NOTE_SUGGESTIONS)
        .toList()
}
