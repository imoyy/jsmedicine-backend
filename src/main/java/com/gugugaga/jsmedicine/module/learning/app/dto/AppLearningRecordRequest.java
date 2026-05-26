package com.gugugaga.jsmedicine.module.learning.app.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AppLearningRecordRequest(
        @NotBlank(message = "resourceType must not be blank")
        @Size(max = 32, message = "resourceType length must be less than 32")
        String resourceType,

        @NotNull(message = "resourceId must not be null")
        Long resourceId,

        @Min(value = 0, message = "studySeconds must be greater than or equal to 0")
        Integer studySeconds,

        @DecimalMin(value = "0.00", message = "progressPercent must be greater than or equal to 0")
        @DecimalMax(value = "100.00", message = "progressPercent must be less than or equal to 100")
        BigDecimal progressPercent,

        Boolean completed
) {
}
