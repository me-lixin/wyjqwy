package com.wyjqwy.app.data.bill

import com.wyjqwy.app.data.TransactionItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

/**
 * 与 App 导出格式一致的 CSV：UTF-8（带 BOM）、首行为表头。
 * 列：类型,金额,分类,备注,发生时间
 */
object BillCsv {

    const val HEADER = "类型,金额,分类,备注,发生时间"

    data class BillImportRow(
        /** 文件中的物理行号（含表头为第 1 行） */
        val lineNumber: Int,
        val type: Int,
        val amount: Double,
        val categoryName: String,
        val note: String?,
        val occurredAt: LocalDateTime
    )

    sealed class ParseResult {
        data class Ok(val rows: List<BillImportRow>) : ParseResult()
        data class Error(val lineNumber: Int, val userMessage: String) : ParseResult()
    }

    /** 将解析错误格式化为用户可见文案（含「第 n 行数据…」） */
    fun formatImportError(e: ParseResult.Error): String = when {
        e.lineNumber == 0 -> e.userMessage
        e.userMessage == "文件为空" -> "未读取到有效内容（文件为空）"
        e.lineNumber == 1 && e.userMessage.contains("表头") -> e.userMessage
        else -> "第 ${e.lineNumber} 行数据${e.userMessage}"
    }

    fun exportToCsvBytes(transactions: List<TransactionItem>): ByteArray {
        val sorted = transactions.sortedWith(
            compareByDescending<TransactionItem> { it.parsedOccurredAt ?: LocalDateTime.MIN }
        )
        val lines = mutableListOf<String>()
        lines.add(HEADER)
        for (tx in sorted) {
            val typeLabel = if (tx.type == 1) "支出" else "收入"
            val amountStr = String.format(Locale.US, "%.2f", tx.amount)
            val note = tx.note.orEmpty()
            val row = listOf(
                typeLabel,
                amountStr,
                quoteIfNeeded(tx.categoryName),
                quoteIfNeeded(note),
                quoteIfNeeded(tx.occurredAt)
            ).joinToString(",")
            lines.add(row)
        }
        val body = lines.joinToString("\r\n")
        val bom = "\uFEFF"
        return (bom + body).toByteArray(Charsets.UTF_8)
    }

    private fun quoteIfNeeded(s: String): String {
        if (s.contains(',') || s.contains('"') || s.contains('\n') || s.contains('\r')) {
            return "\"" + s.replace("\"", "\"\"") + "\""
        }
        return s
    }

    fun parseImport(text: String): ParseResult {
        val raw = stripBom(text.trim())
        if (raw.isEmpty()) {
            return ParseResult.Error(1, "文件为空")
        }
        val physicalLines = splitCsvPhysicalLines(raw)
        if (physicalLines.isEmpty()) {
            return ParseResult.Error(1, "文件为空")
        }
        val headerCells = parseCsvLine(physicalLines.first())
        val col = resolveColumns(headerCells)
            ?: return ParseResult.Error(1, "表头无法识别，需包含「类型」「金额」等列（或使用本应用导出的 CSV）")

        val out = mutableListOf<BillImportRow>()
        for (i in 1 until physicalLines.size) {
            val lineNum = i + 1
            val line = physicalLines[i].trim()
            if (line.isEmpty()) continue
            val cells = parseCsvLine(line)
            when (val row = parseDataRow(lineNum, cells, col)) {
                is RowParse.Ok -> out.add(row.value)
                is RowParse.Err -> return ParseResult.Error(row.lineNumber, row.message)
            }
        }
        if (out.isEmpty()) {
            return ParseResult.Error(0, "除表头外没有有效的数据行")
        }
        return ParseResult.Ok(out)
    }

    private sealed class RowParse {
        data class Ok(val value: BillImportRow) : RowParse()
        data class Err(val lineNumber: Int, val message: String) : RowParse()
    }

    private data class ColumnMap(
        val typeIdx: Int,
        val amountIdx: Int,
        val categoryIdx: Int,
        val noteIdx: Int,
        val timeIdx: Int
    )

    private fun resolveColumns(headerCells: List<String>): ColumnMap? {
        fun findIdx(vararg keys: String): Int {
            val lowered = keys.map { it.lowercase(Locale.ROOT) }
            return headerCells.indexOfFirst { cell ->
                val n = normalizeHeaderToken(cell)
                lowered.any { it == n }
            }
        }
        val t = findIdx("类型", "type", "收支", "收支类型")
        val a = findIdx("金额", "amount", "money")
        if (t < 0 || a < 0) return null
        var c = findIdx("分类", "category", "类别")
        if (c < 0 && headerCells.size >= 3) c = 2
        var n = findIdx("备注", "note", "说明")
        if (n < 0 && headerCells.size >= 4) n = 3
        var tm = findIdx("发生时间", "时间", "time", "date", "日期", "occurred")
        if (tm < 0 && headerCells.size >= 5) tm = 4
        if (c < 0 || n < 0 || tm < 0) return null
        return ColumnMap(t, a, c, n, tm)
    }

    private fun normalizeHeaderToken(s: String): String {
        return stripBom(s).trim().lowercase(Locale.ROOT)
    }

    private fun parseDataRow(lineNumber: Int, cells: List<String>, col: ColumnMap): RowParse {
        fun cell(i: Int): String = cells.getOrNull(i)?.trim().orEmpty()
        val typeRaw = cell(col.typeIdx)
        val type = parseType(typeRaw)
            ?: return RowParse.Err(lineNumber, "「类型」无效，请填写「支出」或「收入」（或 1 / 2）")
        val amountStr = cell(col.amountIdx)
        val amount = parseAmount(amountStr)
            ?: return RowParse.Err(lineNumber, "金额格式不正确")
        if (amount < 0) {
            return RowParse.Err(lineNumber, "金额不能为负数")
        }
        val category = cell(col.categoryIdx)
        val note = cell(col.noteIdx).ifBlank { null }
        val timeRaw = cell(col.timeIdx)
        val at = parseDateTime(timeRaw)
            ?: return RowParse.Err(lineNumber, "时间格式无法识别，请使用如 2026-04-29T12:30:00 或 2026-04-29")
        return RowParse.Ok(
            BillImportRow(
                lineNumber = lineNumber,
                type = type,
                amount = amount,
                categoryName = category,
                note = note,
                occurredAt = at
            )
        )
    }

    private fun parseType(s: String): Int? {
        val t = s.trim()
        if (t == "1" || t.equals("支出", ignoreCase = true) || t.equals("expense", ignoreCase = true)) return 1
        if (t == "2" || t.equals("收入", ignoreCase = true) || t.equals("income", ignoreCase = true)) return 2
        return null
    }

    private fun parseAmount(s: String): Double? {
        val t = s.trim()
            .removePrefix("￥")
            .removePrefix("¥")
            .replace(",", "")
            .trim()
        if (t.isEmpty()) return null
        return t.toDoubleOrNull()
    }

    private val fmtIso = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val fmtDate = DateTimeFormatter.ISO_LOCAL_DATE
    private val fmtSpace = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private fun parseDateTime(s: String): LocalDateTime? {
        val t = s.trim()
        if (t.isEmpty()) return null
        try {
            return LocalDateTime.parse(t, fmtIso)
        } catch (_: DateTimeParseException) {
        }
        try {
            return LocalDateTime.parse(t, fmtSpace)
        } catch (_: DateTimeParseException) {
        }
        try {
            val d = LocalDate.parse(t, fmtDate)
            return d.atStartOfDay()
        } catch (_: DateTimeParseException) {
        }
        return null
    }

    private fun stripBom(s: String): String =
        if (s.startsWith("\uFEFF")) s.substring(1) else s

    /**
     * 按未在引号内的换行拆成「逻辑行」。
     */
    private fun splitCsvPhysicalLines(text: String): List<String> {
        val lines = mutableListOf<String>()
        val cur = StringBuilder()
        var i = 0
        var inq = false
        while (i < text.length) {
            val c = text[i]
            when {
                c == '"' -> {
                    if (inq && i + 1 < text.length && text[i + 1] == '"') {
                        cur.append('"')
                        i += 2
                        continue
                    }
                    inq = !inq
                    cur.append(c)
                }
                (c == '\n' || c == '\r') && !inq -> {
                    lines.add(cur.toString())
                    cur.clear()
                    if (c == '\r' && i + 1 < text.length && text[i + 1] == '\n') i++
                }
                else -> cur.append(c)
            }
            i++
        }
        lines.add(cur.toString())
        return lines.map { it.trimEnd('\r', '\n') }.filter { it.isNotEmpty() }
    }

    fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val cur = StringBuilder()
        var i = 0
        var inq = false
        while (i < line.length) {
            val c = line[i]
            when {
                inq -> when {
                    c == '"' && i + 1 < line.length && line[i + 1] == '"' -> {
                        cur.append('"')
                        i++
                    }
                    c == '"' -> inq = false
                    else -> cur.append(c)
                }
                c == '"' -> inq = true
                c == ',' -> {
                    fields.add(cur.toString())
                    cur.clear()
                }
                else -> cur.append(c)
            }
            i++
        }
        fields.add(cur.toString())
        return fields
    }
}
