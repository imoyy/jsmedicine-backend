package com.gugugaga.jsmedicine.module.interaction.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.FeedbackResponse;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppBrowseHistoryRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppFeedbackRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppFavoriteRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppQaQuestionRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppQaQuestionResponse;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppResourceInteractionResponse;
import com.gugugaga.jsmedicine.module.interaction.app.service.AppInteractionService;
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

@Tag(name = "用户端互动")
@RestController
@RequestMapping("/api/v1/app/interaction")
public class AppInteractionController {

    private final AppInteractionService appInteractionService;

    public AppInteractionController(AppInteractionService appInteractionService) {
        this.appInteractionService = appInteractionService;
    }

    @Operation(summary = "发起咨询")
    @PostMapping("/qa/questions")
    public ApiResponse<AppQaQuestionResponse> createQuestion(@Valid @RequestBody AppQaQuestionRequest request) {
        return ApiResponse.ok(appInteractionService.createQuestion(request));
    }

    @Operation(summary = "我的咨询列表")
    @GetMapping("/qa/questions")
    public ApiResponse<PageResponse<AppQaQuestionResponse>> myQuestions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        return ApiResponse.ok(appInteractionService.myQuestions(page, size));
    }

    @Operation(summary = "我的咨询详情")
    @GetMapping("/qa/questions/{id}")
    public ApiResponse<AppQaQuestionResponse> questionDetail(@PathVariable Long id) {
        return ApiResponse.ok(appInteractionService.questionDetail(id));
    }

    @Operation(summary = "提交反馈")
    @PostMapping("/feedbacks")
    public ApiResponse<FeedbackResponse> submitFeedback(@Valid @RequestBody AppFeedbackRequest request) {
        return ApiResponse.ok(appInteractionService.submitFeedback(request));
    }

    @Operation(summary = "收藏或取消收藏资源")
    @PostMapping("/favorites")
    public ApiResponse<AppResourceInteractionResponse> toggleFavorite(@Valid @RequestBody AppFavoriteRequest request) {
        return ApiResponse.ok(appInteractionService.toggleFavorite(request));
    }

    @Operation(summary = "同步资源浏览记录")
    @PostMapping("/browse-histories")
    public ApiResponse<AppResourceInteractionResponse> syncBrowseHistory(@Valid @RequestBody AppBrowseHistoryRequest request) {
        return ApiResponse.ok(appInteractionService.syncBrowseHistory(request));
    }
}
