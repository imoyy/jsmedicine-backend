package com.gugugaga.jsmedicine.module.learning.app.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

import java.math.BigDecimal;
import java.util.List;

public record AppExamPaperResponse(
        Long id,
        String paperName,
        String description,
        BigDecimal totalScore,
        BigDecimal passScore,
        Integer durationMinutes,
        EnabledStatus status,
        List<AppExamQuestionResponse> questions
) {
}
