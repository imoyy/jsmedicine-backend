package com.gugugaga.jsmedicine.module.interaction.app.dto;

import com.gugugaga.jsmedicine.common.enums.QaStatus;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaAnswerResponse;

import java.util.List;

public record AppQaQuestionResponse(
        Long id,
        Long expertCategoryId,
        Long expertId,
        String title,
        String content,
        QaStatus status,
        String statusCode,
        String statusLabel,
        List<QaAnswerResponse> answers
) {
}
