package com.wyjqwy.server.service;

import com.wyjqwy.server.common.BizException;
import com.wyjqwy.server.mapper.UserMapper;
import com.wyjqwy.server.model.dto.auth.AuthTokenResponse;
import com.wyjqwy.server.model.dto.auth.LoginRequest;
import com.wyjqwy.server.model.dto.auth.RegisterRequest;
import com.wyjqwy.server.model.entity.UserEntity;
import com.wyjqwy.server.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private Claims claims;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerThrowsWhenUsernameExists() {
        when(userMapper.selectOne(any())).thenReturn(new UserEntity());
        assertThrows(BizException.class, () -> authService.register(new RegisterRequest("alice", "123456")));
    }

    @Test
    void loginReturnsTokenPair() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("alice");
        user.setPasswordHash("hash");
        when(userMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("123456", "hash")).thenReturn(true);
        when(jwtTokenService.generateAccessToken(1L, "alice")).thenReturn("a-token");
        when(jwtTokenService.generateRefreshToken(1L, "alice")).thenReturn("r-token");

        AuthTokenResponse response = authService.login(new LoginRequest("alice", "123456"));

        assertEquals("a-token", response.accessToken());
        assertEquals("r-token", response.refreshToken());
    }

    @Test
    void refreshRejectsInvalidTokenType() {
        when(jwtTokenService.parse("r")).thenReturn(claims);
        when(claims.get("tokenType", String.class)).thenReturn("access");

        assertThrows(BizException.class, () -> authService.refresh("r"));
    }
}
