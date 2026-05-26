package com.gugugaga.jsmedicine.module.interaction.admin.dto;

import com.gugugaga.jsmedicine.common.enums.QaStatus;

import java.util.List;

public record QaQuestionResponse(
        Long id,
        Long studentId,
        Long userId,
        Long expertCategoryId,
        Long expertId,
        String title,
        String content,
        QaStatus status,
        List<QaAnswerResponse> answers
) {
}
