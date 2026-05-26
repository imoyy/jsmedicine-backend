package com.gugugaga.jsmedicine.module.statistics.dto;

import java.math.BigDecimal;

public record ExamScoreSummaryResponse(
        Long examCount,
        Long studentCount,
        Long passedCount,
        BigDecimal passRatePercent,
        BigDecimal averageScore,
        BigDecimal maxScore,
        BigDecimal minScore
) {
}
