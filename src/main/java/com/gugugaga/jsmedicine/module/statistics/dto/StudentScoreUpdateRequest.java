package com.gugugaga.jsmedicine.module.statistics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StudentScoreUpdateRequest(
        @NotBlank(message = "theoryTrainingStatus must not be blank")
        @Pattern(regexp = "pass|fail|none", message = "theoryTrainingStatus must be one of pass, fail or none")
        String theoryTrainingStatus,

        @NotBlank(message = "clinicalPracticeStatus must not be blank")
        @Pattern(regexp = "pass|fail|none", message = "clinicalPracticeStatus must be one of pass, fail or none")
        String clinicalPracticeStatus,

        @NotBlank(message = "practicalAssessmentStatus must not be blank")
        @Pattern(regexp = "pass|fail|none", message = "practicalAssessmentStatus must be one of pass, fail or none")
        String practicalAssessmentStatus,

        @NotBlank(message = "theoryAssessmentStatus must not be blank")
        @Pattern(regexp = "pass|fail|none", message = "theoryAssessmentStatus must be one of pass, fail or none")
        String theoryAssessmentStatus,

        @NotBlank(message = "onlineTrainingStatus must not be blank")
        @Pattern(regexp = "pass|fail|none", message = "onlineTrainingStatus must be one of pass, fail or none")
        String onlineTrainingStatus
) {
}
