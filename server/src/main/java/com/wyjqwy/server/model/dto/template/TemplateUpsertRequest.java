package com.wyjqwy.server.model.dto.template;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record TemplateUpsertRequest(
        @NotNull @Min(1) @Max(2) Integer type,
        @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull Long categoryId,
        @Size(max = 200) String note,
        Integer sort
) {
}
