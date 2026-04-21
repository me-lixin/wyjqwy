package com.wyjqwy.server.model.dto.auth;

public record AuthTokenResponse(String accessToken, String refreshToken) {
}
