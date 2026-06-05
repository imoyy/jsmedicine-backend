package com.gugugaga.jsmedicine.module.learning.live.admin.controller;

import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.system.dto.IdListRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.ReviewRequest;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionRequest;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionStreamResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionVideoRequest;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionVideoResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.service.AdminLiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端直播")
@RestController
@RequestMapping("/api/v1/admin/live-sessions")
public class AdminLiveController {

    private final AdminLiveService adminLiveService;

    public AdminLiveController(AdminLiveService adminLiveService) {
        this.adminLiveService = adminLiveService;
    }

    @Operation(summary = "分页查询直播")
    @PreAuthorize("hasAuthority('live:view')")
    @GetMapping
    public ApiResponse<PageResponse<LiveSessionResponse>> pageLives(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ReviewStatus reviewStatus,
            @RequestParam(required = false) LiveStatus liveStatus
    ) {
        return ApiResponse.ok(adminLiveService.pageLives(page, size, keyword, reviewStatus, liveStatus));
    }

    @Operation(summary = "直播详情")
    @PreAuthorize("hasAuthority('live:view')")
    @GetMapping("/{id}")
    public ApiResponse<LiveSessionResponse> liveDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminLiveService.liveDetail(id));
    }

    @Operation(summary = "直播流配置")
    @PreAuthorize("hasAuthority('live:view')")
    @GetMapping("/{id}/streaming")
    public ApiResponse<LiveSessionStreamResponse> liveStreamDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminLiveService.liveStreamDetail(id));
    }

    @Operation(summary = "分页查询直播视频")
    @PreAuthorize("hasAuthority('live:view')")
    @GetMapping("/{liveSessionId}/videos")
    public ApiResponse<PageResponse<LiveSessionVideoResponse>> pageLiveVideos(
            @PathVariable Long liveSessionId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        return ApiResponse.ok(adminLiveService.pageLiveVideos(liveSessionId, page, size));
    }

    @Operation(summary = "新增直播")
    @PreAuthorize("hasAuthority('live:edit')")
    @PostMapping
    public ApiResponse<LiveSessionResponse> createLive(@Valid @RequestBody LiveSessionRequest request) {
        return ApiResponse.ok(adminLiveService.createLive(request));
    }

    @Operation(summary = "新增直播视频")
    @PreAuthorize("hasAuthority('live:edit')")
    @PostMapping("/videos")
    public ApiResponse<LiveSessionVideoResponse> createLiveVideo(@Valid @RequestBody LiveSessionVideoRequest request) {
        return ApiResponse.ok(adminLiveService.createLiveVideo(request));
    }

    @Operation(summary = "修改直播")
    @PreAuthorize("hasAuthority('live:edit')")
    @PutMapping("/{id}")
    public ApiResponse<LiveSessionResponse> updateLive(@PathVariable Long id, @Valid @RequestBody LiveSessionRequest request) {
        return ApiResponse.ok(adminLiveService.updateLive(id, request));
    }

    @Operation(summary = "修改直播视频")
    @PreAuthorize("hasAuthority('live:edit')")
    @PutMapping("/videos/{id}")
    public ApiResponse<LiveSessionVideoResponse> updateLiveVideo(@PathVariable Long id, @Valid @RequestBody LiveSessionVideoRequest request) {
        return ApiResponse.ok(adminLiveService.updateLiveVideo(id, request));
    }

    @Operation(summary = "审核直播")
    @PreAuthorize("hasAuthority('live:review')")
    @PatchMapping("/{id}/review")
    public ApiResponse<LiveSessionResponse> reviewLive(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok(adminLiveService.reviewLive(id, request.reviewStatus(), request.comment()));
    }

    @Operation(summary = "删除直播")
    @PreAuthorize("hasAuthority('live:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteLive(@PathVariable Long id) {
        adminLiveService.deleteLive(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "批量删除直播")
    @PreAuthorize("hasAuthority('live:edit')")
    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDeleteLives(@Valid @RequestBody IdListRequest request) {
        adminLiveService.batchDeleteLives(request.ids());
        return ApiResponse.ok();
    }

    @Operation(summary = "删除直播视频")
    @PreAuthorize("hasAuthority('live:edit')")
    @DeleteMapping("/videos/{id}")
    public ApiResponse<Void> deleteLiveVideo(@PathVariable Long id) {
        adminLiveService.deleteLiveVideo(id);
        return ApiResponse.ok();
    }
}
