package com.gugugaga.jsmedicine.module.learning.app.dto;

import com.gugugaga.jsmedicine.common.enums.Difficulty;
import com.gugugaga.jsmedicine.common.enums.QuestionType;

import java.math.BigDecimal;
import java.util.List;

public record AppExamQuestionResponse(
        Long questionId,
        QuestionType questionType,
        String title,
        Difficulty difficulty,
        BigDecimal score,
        Integer sortOrder,
        List<AppExamQuestionOptionResponse> options
) {
}
