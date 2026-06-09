package com.gugugaga.jsmedicine.module.content.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.module.content.app.dto.AppHomeResponse;
import com.gugugaga.jsmedicine.module.content.app.service.AppHomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端首页")
@RestController
@RequestMapping("/api/v1/app/home")
public class AppHomeController {

    private final AppHomeService appHomeService;

    public AppHomeController(AppHomeService appHomeService) {
        this.appHomeService = appHomeService;
    }

    @Operation(summary = "用户端首页聚合",
            description = "直接复用管理端首页分类和首页内容配置，返回启用中的首页分区及其可见资源卡片。")
    @GetMapping
    public ApiResponse<AppHomeResponse> home() {
        return ApiResponse.ok(appHomeService.home());
    }
}
