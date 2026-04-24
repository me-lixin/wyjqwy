package com.wyjqwy.server.model.dto.category;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoryUpsertRequest(
        @NotNull @Min(1) @Max(2) Integer type,
        @NotBlank @Size(max = 30) String name,
        @Size(max = 50) String icon,
        Integer sort
) {
}
