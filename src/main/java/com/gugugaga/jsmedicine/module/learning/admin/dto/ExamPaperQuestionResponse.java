package com.gugugaga.jsmedicine.module.learning.admin.dto;

import java.math.BigDecimal;

public record ExamPaperQuestionResponse(
        Long id,
        Long paperId,
        Long questionId,
        BigDecimal score,
        Integer sortOrder,
        QuestionResponse question
) {
}
