package com.gugugaga.jsmedicine.module.learning.admin.controller;

import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.enums.AssessmentType;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminExamAssessmentDetailResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminExamAssessmentListResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminExamAssessmentStatusUpdateRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminExamAssessmentUpsertRequest;
import com.gugugaga.jsmedicine.module.learning.assessment.service.AdminExamAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "管理端学习资源")
@RestController
@RequestMapping("/api/v1/admin/learning/exam-assessments")
public class AdminExamAssessmentController {

    private final AdminExamAssessmentService adminExamAssessmentService;

    public AdminExamAssessmentController(AdminExamAssessmentService adminExamAssessmentService) {
        this.adminExamAssessmentService = adminExamAssessmentService;
    }

    @Operation(summary = "分页查询考核场次")
    @PreAuthorize("hasAuthority('learning:assessment:view')")
    @GetMapping
    public ApiResponse<PageResponse<AdminExamAssessmentListResponse>> pageAssessments(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AssessmentType assessmentType,
            @RequestParam(required = false) AssessmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt
    ) {
        return ApiResponse.ok(adminExamAssessmentService.pageAssessments(
                page, size, sort, keyword, assessmentType, status, startAt, endAt));
    }

    @Operation(summary = "查询考核场次详情")
    @PreAuthorize("hasAuthority('learning:assessment:view')")
    @GetMapping("/{id}")
    public ApiResponse<AdminExamAssessmentDetailResponse> getAssessment(@PathVariable Long id) {
        return ApiResponse.ok(adminExamAssessmentService.getAssessment(id));
    }

    @Operation(summary = "新增考核场次")
    @PreAuthorize("hasAuthority('learning:assessment:edit')")
    @PostMapping
    public ApiResponse<AdminExamAssessmentDetailResponse> createAssessment(
            @Valid @RequestBody AdminExamAssessmentUpsertRequest request
    ) {
        return ApiResponse.ok(adminExamAssessmentService.createAssessment(request));
    }

    @Operation(summary = "修改考核场次")
    @PreAuthorize("hasAuthority('learning:assessment:edit')")
    @PutMapping("/{id}")
    public ApiResponse<AdminExamAssessmentDetailResponse> updateAssessment(
            @PathVariable Long id,
            @Valid @RequestBody AdminExamAssessmentUpsertRequest request
    ) {
        return ApiResponse.ok(adminExamAssessmentService.updateAssessment(id, request));
    }

    @Operation(summary = "更新考核场次状态")
    @PreAuthorize("hasAuthority('learning:assessment:edit')")
    @PatchMapping("/{id}/status")
    public ApiResponse<AdminExamAssessmentDetailResponse> updateAssessmentStatus(
            @PathVariable Long id,
            @Valid @RequestBody AdminExamAssessmentStatusUpdateRequest request
    ) {
        return ApiResponse.ok(adminExamAssessmentService.updateAssessmentStatus(id, request));
    }
}
