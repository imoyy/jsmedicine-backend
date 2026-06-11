package com.gugugaga.jsmedicine.module.interaction.app.controller;

import com.gugugaga.jsmedicine.common.enums.QaStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppExpertQaAnswerRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppExpertQaQuestionResponse;
import com.gugugaga.jsmedicine.module.interaction.app.service.AppExpertInteractionService;
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

@Tag(name = "用户端专家答疑")
@RestController
@RequestMapping("/api/v1/app/interaction/expert")
public class AppExpertInteractionController {

    private final AppExpertInteractionService appExpertInteractionService;

    public AppExpertInteractionController(AppExpertInteractionService appExpertInteractionService) {
        this.appExpertInteractionService = appExpertInteractionService;
    }

    @Operation(summary = "专家侧分页查询咨询问题")
    @GetMapping("/qa/questions")
    public ApiResponse<PageResponse<AppExpertQaQuestionResponse>> pageQuestions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) QaStatus status
    ) {
        return ApiResponse.ok(appExpertInteractionService.pageQuestions(page, size, keyword, status));
    }

    @Operation(summary = "专家侧咨询详情")
    @GetMapping("/qa/questions/{id}")
    public ApiResponse<AppExpertQaQuestionResponse> questionDetail(@PathVariable Long id) {
        return ApiResponse.ok(appExpertInteractionService.questionDetail(id));
    }

    @Operation(summary = "专家侧回复咨询")
    @PostMapping("/qa/questions/{id}/answers")
    public ApiResponse<AppExpertQaQuestionResponse> answerQuestion(
            @PathVariable Long id,
            @Valid @RequestBody AppExpertQaAnswerRequest request
    ) {
        return ApiResponse.ok(appExpertInteractionService.answerQuestion(id, request));
    }
}
