package com.gugugaga.jsmedicine.module.interaction.app.dto;

import com.gugugaga.jsmedicine.common.enums.QaStatus;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaAnswerResponse;

import java.time.LocalDateTime;
import java.util.List;

public record AppExpertQaQuestionResponse(
        Long id,
        Long userId,
        Long studentId,
        Long expertCategoryId,
        Long expertId,
        String title,
        String content,
        QaStatus status,
        String statusCode,
        String statusLabel,
        LocalDateTime createdAt,
        List<QaAnswerResponse> answers
) {
}
