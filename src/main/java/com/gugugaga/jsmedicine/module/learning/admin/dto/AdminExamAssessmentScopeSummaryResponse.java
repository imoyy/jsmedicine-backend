package com.gugugaga.jsmedicine.module.learning.admin.dto;

public record AdminExamAssessmentScopeSummaryResponse(
        String provinceCode,
        String cityCode,
        String districtCode,
        long organizationCount,
        long explicitStudentCount,
        long expectedStudentCount
) {
}
