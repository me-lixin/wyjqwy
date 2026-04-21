package com.wyjqwy.server.controller;

import com.wyjqwy.server.common.ApiResponse;
import com.wyjqwy.server.model.dto.auth.AuthTokenResponse;
import com.wyjqwy.server.model.dto.auth.LoginRequest;
import com.wyjqwy.server.model.dto.auth.RegisterRequest;
import com.wyjqwy.server.model.dto.auth.TokenRefreshRequest;
import com.wyjqwy.server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthTokenResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }
}
