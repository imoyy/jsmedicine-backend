package com.gugugaga.jsmedicine.module.statistics.dto;

import java.time.LocalDateTime;

public record AssessmentTrendPointResponse(
        LocalDateTime bucketTime,
        long actualAttendCount,
        long inProgressCount,
        long completedCount,
        long passCount,
        long failCount
) {
}
