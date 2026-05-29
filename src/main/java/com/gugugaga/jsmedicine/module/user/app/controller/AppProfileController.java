package com.gugugaga.jsmedicine.module.user.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.user.app.dto.AppAvatarConfirmRequest;
import com.gugugaga.jsmedicine.module.user.app.dto.AppAvatarUploadRequest;
import com.gugugaga.jsmedicine.module.user.app.dto.AppAvatarUploadResponse;
import com.gugugaga.jsmedicine.module.user.app.dto.AppProfileResponse;
import com.gugugaga.jsmedicine.module.user.app.dto.AppProfileSummaryResponse;
import com.gugugaga.jsmedicine.module.user.app.dto.AppProfileUpdateRequest;
import com.gugugaga.jsmedicine.module.user.app.dto.AppResourceRecordResponse;
import com.gugugaga.jsmedicine.module.user.app.dto.AppStudentCertificationRequest;
import com.gugugaga.jsmedicine.module.user.app.dto.AppStudentCertificationResponse;
import com.gugugaga.jsmedicine.module.user.app.service.AppProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端个人中心")
@RestController
@RequestMapping("/api/v1/app/profile")
public class AppProfileController {

    private final AppProfileService appProfileService;

    public AppProfileController(AppProfileService appProfileService) {
        this.appProfileService = appProfileService;
    }

    @Operation(summary = "获取用户端个人资料")
    @GetMapping
    public ApiResponse<AppProfileResponse> currentProfile() {
        return ApiResponse.ok(appProfileService.currentProfile());
    }

    @Operation(summary = "修改用户端个人资料")
    @PutMapping
    public ApiResponse<AppProfileResponse> updateProfile(@Valid @RequestBody AppProfileUpdateRequest request) {
        return ApiResponse.ok(appProfileService.updateProfile(request));
    }

    @Operation(summary = "申请头像上传地址")
    @PostMapping("/avatar/upload-url")
    public ApiResponse<AppAvatarUploadResponse> createAvatarUploadUrl(
            @Valid @RequestBody AppAvatarUploadRequest request
    ) {
        return ApiResponse.ok(appProfileService.createAvatarUploadUrl(request));
    }

    @Operation(summary = "确认头像上传")
    @PostMapping("/avatar/confirm")
    public ApiResponse<AppProfileResponse> confirmAvatarUpload(
            @Valid @RequestBody AppAvatarConfirmRequest request
    ) {
        return ApiResponse.ok(appProfileService.confirmAvatarUpload(request));
    }

    @Operation(summary = "提交学员认证申请")
    @PostMapping("/certification")
    public ApiResponse<AppStudentCertificationResponse> submitCertification(
            @Valid @RequestBody AppStudentCertificationRequest request
    ) {
        return ApiResponse.ok(appProfileService.submitCertification(request));
    }

    @Operation(summary = "查询学员认证结果")
    @GetMapping("/certification")
    public ApiResponse<AppStudentCertificationResponse> certificationStatus() {
        return ApiResponse.ok(appProfileService.certificationStatus());
    }

    @Operation(summary = "查询我的收藏")
    @GetMapping("/favorites")
    public ApiResponse<PageResponse<AppResourceRecordResponse>> favoritePage(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(appProfileService.favoritePage(page, size, sort));
    }

    @Operation(summary = "查询浏览记录")
    @GetMapping("/browse-histories")
    public ApiResponse<PageResponse<AppResourceRecordResponse>> browseHistoryPage(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort
    ) {
        return ApiResponse.ok(appProfileService.browseHistoryPage(page, size, sort));
    }

    @Operation(summary = "查询个人中心聚合信息")
    @GetMapping("/summary")
    public ApiResponse<AppProfileSummaryResponse> summary() {
        return ApiResponse.ok(appProfileService.summary());
    }
}
