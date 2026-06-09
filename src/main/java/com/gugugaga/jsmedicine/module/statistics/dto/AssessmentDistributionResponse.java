package com.gugugaga.jsmedicine.module.statistics.dto;

import java.math.BigDecimal;

public record AssessmentDistributionResponse(
        String dimensionCode,
        String dimensionName,
        long expectedCount,
        long actualAttendCount,
        long absentCount,
        long inProgressCount,
        long completedCount,
        long passCount,
        long failCount,
        BigDecimal passRate,
        BigDecimal averageScore
) {
}
