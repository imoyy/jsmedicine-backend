package com.gugugaga.jsmedicine.module.learning.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamAssessmentEnterRequest;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamAssessmentResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamRecordResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamSubmitRequest;
import com.gugugaga.jsmedicine.module.learning.assessment.service.AppExamAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端学习资源")
@RestController
@RequestMapping("/api/v1/app/learning/exam-assessments")
public class AppExamAssessmentController {

    private final AppExamAssessmentService appExamAssessmentService;

    public AppExamAssessmentController(AppExamAssessmentService appExamAssessmentService) {
        this.appExamAssessmentService = appExamAssessmentService;
    }

    @Operation(summary = "分页查询考核场次")
    @GetMapping
    public ApiResponse<PageResponse<AppExamAssessmentResponse>> pageAssessments(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        return ApiResponse.ok(appExamAssessmentService.pageAssessments(page, size));
    }

    @Operation(summary = "查询考核场次详情")
    @GetMapping("/{id}")
    public ApiResponse<AppExamAssessmentResponse> getAssessment(@PathVariable Long id) {
        return ApiResponse.ok(appExamAssessmentService.getAssessment(id));
    }

    @Operation(summary = "进入考核场次")
    @PostMapping("/{id}/enter")
    public ApiResponse<AppExamRecordResponse> enterAssessment(
            @PathVariable Long id,
            @Valid @RequestBody AppExamAssessmentEnterRequest request
    ) {
        return ApiResponse.ok(appExamAssessmentService.enterAssessment(id, request));
    }

    @Operation(summary = "提交考核场次答案")
    @PostMapping("/{id}/submit")
    public ApiResponse<AppExamRecordResponse> submitAssessment(
            @PathVariable Long id,
            @Valid @RequestBody AppExamSubmitRequest request
    ) {
        return ApiResponse.ok(appExamAssessmentService.submitAssessment(id, request));
    }
}
