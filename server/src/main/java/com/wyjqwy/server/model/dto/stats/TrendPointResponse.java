package com.wyjqwy.server.model.dto.stats;

import java.math.BigDecimal;

public record TrendPointResponse(String period, BigDecimal amount) {
}
