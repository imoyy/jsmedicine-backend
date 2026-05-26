package com.gugugaga.jsmedicine.module.learning.app.dto;

import com.gugugaga.jsmedicine.common.enums.QuestionType;

import java.math.BigDecimal;
import java.util.List;

public record AppExamAnswerResultResponse(
        Long questionId,
        QuestionType questionType,
        String title,
        String answerContent,
        String correctAnswer,
        String analysis,
        BigDecimal score,
        Integer correct,
        List<AppExamQuestionOptionResponse> options
) {
}
