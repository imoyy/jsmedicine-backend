package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.AssessmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record AdminExamAssessmentUpsertRequest(
        @NotBlank(message = "assessmentName must not be blank")
        @Size(max = 255, message = "assessmentName length must be less than 255")
        String assessmentName,

        @NotNull(message = "paperId must not be null")
        Long paperId,

        @NotNull(message = "assessmentType must not be null")
        AssessmentType assessmentType,

        @NotNull(message = "startAt must not be null")
        LocalDateTime startAt,

        @NotNull(message = "endAt must not be null")
        LocalDateTime endAt,

        @Size(max = 32, message = "provinceCode length must be less than 32")
        String provinceCode,

        @Size(max = 32, message = "cityCode length must be less than 32")
        String cityCode,

        @Size(max = 32, message = "districtCode length must be less than 32")
        String districtCode,

        List<Long> organizationIds,

        List<Long> studentIds
) {
}
