package com.wyjqwy.server.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wyjqwy.server.common.BizException;
import com.wyjqwy.server.mapper.UserMapper;
import com.wyjqwy.server.model.dto.auth.AuthTokenResponse;
import com.wyjqwy.server.model.dto.auth.LoginRequest;
import com.wyjqwy.server.model.dto.auth.RegisterRequest;
import com.wyjqwy.server.model.entity.UserEntity;
import com.wyjqwy.server.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public void register(RegisterRequest request) {
        UserEntity existing = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, request.username()));
        if (existing != null) {
            throw new BizException("username already exists");
        }
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        userMapper.insert(user);
    }

    public AuthTokenResponse login(LoginRequest request) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BizException("username or password error");
        }
        return new AuthTokenResponse(
                jwtTokenService.generateAccessToken(user.getId(), user.getUsername()),
                jwtTokenService.generateRefreshToken(user.getId(), user.getUsername()));
    }

    public AuthTokenResponse refresh(String refreshToken) {
        Claims claims = jwtTokenService.parse(refreshToken);
        if (!"refresh".equals(claims.get("tokenType", String.class))) {
            throw new BizException("invalid refresh token");
        }
        Long userId = Long.valueOf(claims.getSubject());
        String username = claims.get("username", String.class);
        return new AuthTokenResponse(
                jwtTokenService.generateAccessToken(userId, username),
                jwtTokenService.generateRefreshToken(userId, username));
    }
}
