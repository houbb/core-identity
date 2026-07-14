package com.coreplatform.identity.controller;

import com.coreplatform.identity.dto.LoginRequest;
import com.coreplatform.identity.dto.RefreshTokenRequest;
import com.coreplatform.identity.dto.RegisterRequest;
import com.coreplatform.identity.entity.ApiResponse;
import com.coreplatform.identity.security.JwtAuthenticationToken;
import com.coreplatform.identity.service.AuthService;
import com.coreplatform.identity.vo.LoginResponse;
import com.coreplatform.identity.vo.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getDisplayName());
        return ApiResponse.success(response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getUsername(), request.getPassword());
        return ApiResponse.success(response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal JwtAuthenticationToken auth) {
        authService.logout(auth.getUserId());
        return ApiResponse.success();
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ApiResponse.success(response);
    }
}