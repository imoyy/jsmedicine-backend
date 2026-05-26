package com.gugugaga.jsmedicine.module.interaction.admin.dto;

import com.gugugaga.jsmedicine.common.enums.FeedbackStatus;

import java.time.LocalDateTime;

public record FeedbackResponse(
        Long id,
        Long userId,
        Long studentId,
        String feedbackType,
        String content,
        String contact,
        FeedbackStatus status,
        Long processedBy,
        LocalDateTime processedAt,
        String processNote
) {
}
