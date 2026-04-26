package com.wyjqwy.app.ui.util

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.toAmountText(maxScale: Int = 2): String {
    if (!this.isFinite()) return "0"
    return BigDecimal.valueOf(this).toAmountText(maxScale)
}

fun BigDecimal.toAmountText(maxScale: Int = 2): String {
    val normalized = setScale(maxScale, RoundingMode.HALF_UP)
    // 超过 5 位数统一使用 w（万）单位展示
    if (normalized.abs().compareTo(BigDecimal("99999")) > 0) {
        return normalized
            // w 单位展示使用截断，避免 9999999 被显示成 1000w（1000万）
            .divide(BigDecimal("10000"), maxScale, RoundingMode.DOWN)
            .stripTrailingZeros()
            .toPlainString() + "w"
    }
    return normalized.stripTrailingZeros().toPlainString()
}
