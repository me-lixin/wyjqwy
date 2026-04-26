package com.wyjqwy.server.model.dto.template;

import java.math.BigDecimal;

public record TemplateResponse(
        Long id,
        Integer type,
        BigDecimal amount,
        Long categoryId,
        String categoryName,
        String categoryIcon,
        String note,
        Integer sort
) {
}
