package com.wyjqwy.app.ui.invest

import com.wyjqwy.app.data.TransactionItem

data class AutoInvestState(
    val yearTransactions: Map<Int, List<TransactionItem>> = emptyMap(),
    val loadingYears: Set<Int> = emptySet(),
    val lastError: String? = null
)
