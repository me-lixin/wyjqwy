package com.wyjqwy.server.model.dto.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Integer type,
        BigDecimal amount,
        Long categoryId,
        String categoryName,
        String categoryIcon,
        String note,
        LocalDateTime occurredAt
) {
}
