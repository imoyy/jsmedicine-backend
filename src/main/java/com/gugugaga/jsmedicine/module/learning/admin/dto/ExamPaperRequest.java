package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ExamPaperRequest(
        @NotBlank(message = "paperName must not be blank")
        @Size(max = 128, message = "paperName length must be less than 128")
        String paperName,

        @Size(max = 512, message = "description length must be less than 512")
        String description,

        @NotNull(message = "totalScore must not be null")
        @DecimalMin(value = "0.00", message = "totalScore must be greater than or equal to 0")
        BigDecimal totalScore,

        @NotNull(message = "passScore must not be null")
        @DecimalMin(value = "0.00", message = "passScore must be greater than or equal to 0")
        BigDecimal passScore,

        Integer durationMinutes,

        @NotNull(message = "status must not be null")
        EnabledStatus status,

        @Valid
        List<ExamPaperQuestionRequest> questions
) {
}
