package com.wyjqwy.app.ui.util

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.toAmountText(maxScale: Int = 2): String {
    if (!this.isFinite()) return "0"
    return BigDecimal.valueOf(this).toAmountText(maxScale)
}

fun BigDecimal.toAmountText(maxScale: Int = 2): String {
    return setScale(maxScale, RoundingMode.HALF_UP)
        .stripTrailingZeros()
        .toPlainString()
}
