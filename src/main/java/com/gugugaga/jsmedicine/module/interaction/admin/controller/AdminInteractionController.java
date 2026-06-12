package com.gugugaga.jsmedicine.module.interaction.admin.controller;

import com.gugugaga.jsmedicine.common.enums.FeedbackStatus;
import com.gugugaga.jsmedicine.common.enums.QaStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertRequest;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertResponse;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.FeedbackProcessRequest;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.FeedbackResponse;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaAnswerRequest;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaQuestionResponse;
import com.gugugaga.jsmedicine.module.interaction.admin.service.AdminInteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端互动处理")
@RestController
@RequestMapping("/api/v1/admin/interaction")
public class AdminInteractionController {

    private final AdminInteractionService adminInteractionService;

    public AdminInteractionController(AdminInteractionService adminInteractionService) {
        this.adminInteractionService = adminInteractionService;
    }

    @Operation(summary = "分页查询答疑问题")
    @PreAuthorize("hasAuthority('interaction:qa:view')")
    @GetMapping("/qa/questions")
    public ApiResponse<PageResponse<QaQuestionResponse>> pageQaQuestions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) QaStatus status
    ) {
        return ApiResponse.ok(adminInteractionService.pageQaQuestions(page, size, keyword, status));
    }

    @Operation(summary = "新增可咨询专家",
            description = "供咨询管理页面直接新增专家资料并进入可咨询名单。该接口底层复用专家主数据模型，要求请求体中的 consultEnabled 必须为 ENABLED。")
    @PreAuthorize("hasAuthority('expert:edit')")
    @PostMapping("/qa/experts")
    public ApiResponse<ExpertResponse> createConsultExpert(@Valid @RequestBody ExpertRequest request) {
        return ApiResponse.ok(adminInteractionService.createConsultExpert(request));
    }

    @Operation(summary = "答疑问题详情")
    @PreAuthorize("hasAuthority('interaction:qa:view')")
    @GetMapping("/qa/questions/{id}")
    public ApiResponse<QaQuestionResponse> qaQuestionDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminInteractionService.qaQuestionDetail(id));
    }

    @Operation(summary = "回复答疑问题")
    @PreAuthorize("hasAuthority('interaction:qa:reply')")
    @PostMapping("/qa/questions/{id}/answers")
    public ApiResponse<QaQuestionResponse> answerQuestion(@PathVariable Long id, @Valid @RequestBody QaAnswerRequest request) {
        return ApiResponse.ok(adminInteractionService.answerQuestion(id, request));
    }

    @Operation(summary = "删除答疑问题")
    @PreAuthorize("hasAuthority('interaction:qa:edit')")
    @DeleteMapping("/qa/questions/{id}")
    public ApiResponse<Void> deleteQaQuestion(@PathVariable Long id) {
        adminInteractionService.deleteQaQuestion(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询反馈")
    @PreAuthorize("hasAuthority('interaction:feedback:view')")
    @GetMapping("/feedbacks")
    public ApiResponse<PageResponse<FeedbackResponse>> pageFeedbacks(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) FeedbackStatus status
    ) {
        return ApiResponse.ok(adminInteractionService.pageFeedbacks(page, size, keyword, status));
    }

    @Operation(summary = "反馈详情")
    @PreAuthorize("hasAuthority('interaction:feedback:view')")
    @GetMapping("/feedbacks/{id}")
    public ApiResponse<FeedbackResponse> feedbackDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminInteractionService.feedbackDetail(id));
    }

    @Operation(summary = "处理反馈")
    @PreAuthorize("hasAuthority('interaction:feedback:process')")
    @PatchMapping("/feedbacks/{id}/process")
    public ApiResponse<FeedbackResponse> processFeedback(@PathVariable Long id, @Valid @RequestBody FeedbackProcessRequest request) {
        return ApiResponse.ok(adminInteractionService.processFeedback(id, request));
    }

    @Operation(summary = "删除反馈")
    @PreAuthorize("hasAuthority('interaction:feedback:edit')")
    @DeleteMapping("/feedbacks/{id}")
    public ApiResponse<Void> deleteFeedback(@PathVariable Long id) {
        adminInteractionService.deleteFeedback(id);
        return ApiResponse.ok();
    }
}
