package com.wyjqwy.server.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 4, max = 50) String username,
        @NotBlank @Size(min = 6, max = 64) String password
) {
}
