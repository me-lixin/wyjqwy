package com.wyjqwy.server.model.dto.stats;

import java.math.BigDecimal;

public record CategoryStatResponse(Long categoryId, String categoryName, BigDecimal amount) {
}
