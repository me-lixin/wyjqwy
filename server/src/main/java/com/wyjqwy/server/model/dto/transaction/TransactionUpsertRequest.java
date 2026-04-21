package com.wyjqwy.server.model.dto.transaction;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionUpsertRequest(
        @NotNull @Min(1) @Max(2) Integer type,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull Long categoryId,
        @Size(max = 200) String note,
        @NotNull LocalDateTime occurredAt
) {
}
