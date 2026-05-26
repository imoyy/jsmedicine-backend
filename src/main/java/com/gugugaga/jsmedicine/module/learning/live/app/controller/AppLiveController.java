package com.gugugaga.jsmedicine.module.learning.live.app.controller;

import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionResponse;
import com.gugugaga.jsmedicine.module.learning.live.app.service.AppLiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端直播")
@RestController
@RequestMapping("/api/v1/app/live-sessions")
public class AppLiveController {

    private final AppLiveService appLiveService;

    public AppLiveController(AppLiveService appLiveService) {
        this.appLiveService = appLiveService;
    }

    @Operation(summary = "分页查询直播")
    @GetMapping
    public ApiResponse<PageResponse<LiveSessionResponse>> pageLives(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LiveStatus liveStatus
    ) {
        return ApiResponse.ok(appLiveService.pageLives(page, size, keyword, liveStatus));
    }

    @Operation(summary = "直播详情")
    @GetMapping("/{id}")
    public ApiResponse<LiveSessionResponse> liveDetail(@PathVariable Long id) {
        return ApiResponse.ok(appLiveService.liveDetail(id));
    }
}
