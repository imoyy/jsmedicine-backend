package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.Difficulty;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record QuestionRequest(
        Long categoryId,

        @NotNull(message = "questionType must not be null")
        QuestionType questionType,

        @NotBlank(message = "title must not be blank")
        String title,

        String analysis,

        @NotNull(message = "difficulty must not be null")
        Difficulty difficulty,

        @NotNull(message = "score must not be null")
        @DecimalMin(value = "0.00", message = "score must be greater than or equal to 0")
        BigDecimal score,

        @NotNull(message = "status must not be null")
        EnabledStatus status,

        @Valid
        List<QuestionOptionRequest> options
) {
}
