package com.gugugaga.jsmedicine.module.interaction.admin.dto;

import com.gugugaga.jsmedicine.common.enums.FeedbackStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        Long userId,
        Long studentId,
        String nickname,
        String avatarUrl,
        String mobile,
        @Schema(description = "反馈分类，当前为用户自填或前端约定值，后端暂不做固定枚举校验", example = "功能建议")
        String feedbackType,
        String content,
        @Schema(description = "主联系方式字段，可填写手机号、微信号、邮箱等任一便于回访的联系方式", example = "13800000000")
        String contact,
        FeedbackStatus status,
        Long processedBy,
        LocalDateTime processedAt,
        String processNote,
        LocalDateTime createdAt
) {
}
