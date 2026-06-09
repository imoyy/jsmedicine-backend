package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import jakarta.validation.constraints.NotNull;

public record AdminExamAssessmentStatusUpdateRequest(
        @NotNull(message = "status must not be null")
        AssessmentStatus status
) {
}
