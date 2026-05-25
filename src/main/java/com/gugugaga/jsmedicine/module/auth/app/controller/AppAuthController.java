package com.gugugaga.jsmedicine.module.auth.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginResponse;
import com.gugugaga.jsmedicine.module.auth.app.service.AppAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端认证")
@RestController
@RequestMapping("/api/v1/app/auth")
public class AppAuthController {

    private final AppAuthService appAuthService;

    public AppAuthController(AppAuthService appAuthService) {
        this.appAuthService = appAuthService;
    }

    @Operation(summary = "用户端账号密码登录")
    @PostMapping("/login")
    public ApiResponse<AppLoginResponse> login(
            @Valid @RequestBody AppLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.ok(appAuthService.login(request, httpServletRequest));
    }
}
