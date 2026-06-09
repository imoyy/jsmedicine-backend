package com.gugugaga.jsmedicine.module.statistics.dto;

import com.gugugaga.jsmedicine.common.enums.QuestionType;

import java.math.BigDecimal;

public record AssessmentQuestionStructureRow(
        QuestionType questionType,
        long questionCount,
        BigDecimal scorePerQuestion,
        BigDecimal totalScore
) {
}
