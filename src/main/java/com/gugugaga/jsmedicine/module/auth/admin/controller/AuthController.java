package com.gugugaga.jsmedicine.module.auth.admin.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.module.auth.admin.dto.CurrentAdminResponse;
import com.gugugaga.jsmedicine.module.auth.admin.dto.LoginRequest;
import com.gugugaga.jsmedicine.module.auth.admin.dto.LoginResponse;
import com.gugugaga.jsmedicine.module.auth.admin.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证与权限")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ApiResponse.ok(authService.login(request, httpServletRequest));
    }

    @Operation(summary = "退出登录")
    @PreAuthorize("hasAuthority('auth:logout')")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        authService.logout(authorizationHeader);
        return ApiResponse.ok();
    }

    @Operation(summary = "获取当前管理员信息")
    @PreAuthorize("hasAuthority('auth:me')")
    @GetMapping("/me")
    public ApiResponse<CurrentAdminResponse> currentAdmin() {
        return ApiResponse.ok(authService.currentAdmin());
    }

    @Operation(summary = "校验登录状态")
    @PreAuthorize("hasAuthority('auth:status')")
    @GetMapping("/status")
    public ApiResponse<Boolean> validateStatus() {
        return ApiResponse.ok(authService.validateCurrentToken());
    }
}

