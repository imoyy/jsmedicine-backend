package com.gugugaga.jsmedicine.module.learning.app.dto;

import jakarta.validation.constraints.Size;

public record AppExamAssessmentEnterRequest(
        @Size(max = 64, message = "requestId length must be less than 64")
        String requestId
) {
}
