package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.enums.AssessmentType;

import java.time.LocalDateTime;
import java.util.List;

public record AdminExamAssessmentDetailResponse(
        Long id,
        String assessmentName,
        Long paperId,
        String paperName,
        AssessmentType assessmentType,
        AssessmentStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String provinceCode,
        String cityCode,
        String districtCode,
        List<Long> organizationIds,
        List<Long> explicitStudentIds,
        AdminExamAssessmentScopeSummaryResponse scopeSummary
) {
}
