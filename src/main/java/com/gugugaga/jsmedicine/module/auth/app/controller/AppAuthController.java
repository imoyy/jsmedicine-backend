package com.gugugaga.jsmedicine.module.auth.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.CurrentAppUserResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppLoginResponse;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppSmsCodeRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppSmsLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.dto.AppWechatLoginRequest;
import com.gugugaga.jsmedicine.module.auth.app.service.AppAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @Operation(summary = "发送用户端手机号验证码")
    @PostMapping("/sms-code")
    public ApiResponse<Void> sendSmsCode(@Valid @RequestBody AppSmsCodeRequest request) {
        appAuthService.sendSmsCode(request.mobile());
        return ApiResponse.ok();
    }

    @Operation(summary = "用户端手机号验证码登录")
    @PostMapping("/sms-login")
    public ApiResponse<AppLoginResponse> smsLogin(
            @Valid @RequestBody AppSmsLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.ok(appAuthService.loginBySms(request, httpServletRequest));
    }

    @Operation(summary = "用户端微信授权登录")
    @PostMapping("/wechat-login")
    public ApiResponse<AppLoginResponse> wechatLogin(
            @Valid @RequestBody AppWechatLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ApiResponse.ok(appAuthService.loginByWechat(request, httpServletRequest));
    }

    @Operation(summary = "用户端退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        appAuthService.logout(authorizationHeader);
        return ApiResponse.ok();
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public ApiResponse<CurrentAppUserResponse> currentUser() {
        return ApiResponse.ok(appAuthService.currentUser());
    }

    @Operation(summary = "校验用户端登录状态")
    @GetMapping("/status")
    public ApiResponse<Boolean> validateStatus() {
        return ApiResponse.ok(appAuthService.validateCurrentToken());
    }
}
