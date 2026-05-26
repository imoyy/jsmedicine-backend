package com.gugugaga.jsmedicine.module.statistics.dto;

import java.math.BigDecimal;

public record StudyHoursResourceResponse(
        String resourceType,
        Long recordCount,
        Long studentCount,
        Long completedCount,
        Long totalStudySeconds,
        BigDecimal totalStudyHours,
        BigDecimal averageProgressPercent
) {
}
