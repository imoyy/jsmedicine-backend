package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.Difficulty;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.QuestionType;

import java.math.BigDecimal;
import java.util.List;

public record QuestionResponse(
        Long id,
        Long categoryId,
        QuestionType questionType,
        String title,
        String analysis,
        Difficulty difficulty,
        BigDecimal score,
        EnabledStatus status,
        List<QuestionOptionResponse> options
) {
}
