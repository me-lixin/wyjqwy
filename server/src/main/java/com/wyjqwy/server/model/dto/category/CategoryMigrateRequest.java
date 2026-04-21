package com.wyjqwy.server.model.dto.category;

import jakarta.validation.constraints.NotNull;

public record CategoryMigrateRequest(@NotNull Long targetCategoryId) {}
