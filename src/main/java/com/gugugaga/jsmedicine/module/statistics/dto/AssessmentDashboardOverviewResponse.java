package com.gugugaga.jsmedicine.module.statistics.dto;

import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.enums.AssessmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssessmentDashboardOverviewResponse(
        Long assessmentId,
        String assessmentName,
        Long paperId,
        String paperName,
        AssessmentType assessmentType,
        AssessmentStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime serverTime,
        long countdownSeconds,
        long expectedCount,
        long actualAttendCount,
        long notStartedCount,
        long absentCount,
        long inProgressCount,
        long completedCount,
        long passCount,
        long failCount,
        BigDecimal passRate,
        BigDecimal averageScore,
        BigDecimal highestScore,
        BigDecimal lowestScore
) {
}
