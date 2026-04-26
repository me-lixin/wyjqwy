package com.wyjqwy.app.ui.util

import com.wyjqwy.app.data.TransactionItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun parseOccurredAtToLocalDateTime(raw: String): LocalDateTime? {
    return try {
        LocalDateTime.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (_: Exception) {
        try {
            LocalDate.parse(raw.take(10)).atStartOfDay()
        } catch (_: Exception) {
            null
        }
    }
}

fun parseOccurredDate(occurredAt: String): LocalDate {
    return parseOccurredAtToLocalDateTime(occurredAt)?.toLocalDate() ?: LocalDate.now()
}

fun groupTransactionsByDay(transactions: List<TransactionItem>): List<Pair<LocalDate, List<TransactionItem>>> {
    return transactions
        .groupBy { tx -> tx.parsedOccurredAt?.toLocalDate() ?: parseOccurredDate(tx.occurredAt) }
        .entries
        .sortedByDescending { it.key }
        .map { it.key to it.value.sortedByDescending { tx -> tx.occurredAt } }
}
