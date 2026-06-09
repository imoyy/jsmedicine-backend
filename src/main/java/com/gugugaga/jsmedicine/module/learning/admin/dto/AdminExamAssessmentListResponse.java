package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.enums.AssessmentType;

import java.time.LocalDateTime;

public record AdminExamAssessmentListResponse(
        Long id,
        String assessmentName,
        Long paperId,
        String paperName,
        AssessmentType assessmentType,
        AssessmentStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        long expectedStudentCount,
        AdminExamAssessmentScopeSummaryResponse scopeSummary
) {
}
