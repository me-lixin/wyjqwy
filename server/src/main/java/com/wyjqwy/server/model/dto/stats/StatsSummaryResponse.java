package com.wyjqwy.server.model.dto.stats;

import java.math.BigDecimal;

public record StatsSummaryResponse(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal balance) {
}
