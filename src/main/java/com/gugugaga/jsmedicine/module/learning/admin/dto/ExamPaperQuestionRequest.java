package com.gugugaga.jsmedicine.module.learning.admin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ExamPaperQuestionRequest(
        @NotNull(message = "questionId must not be null")
        Long questionId,

        @NotNull(message = "score must not be null")
        @DecimalMin(value = "0.00", message = "score must be greater than or equal to 0")
        BigDecimal score,

        Integer sortOrder
) {
}
