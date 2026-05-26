package com.gugugaga.jsmedicine.module.statistics.dto;

import java.math.BigDecimal;

public record ExamPaperScoreResponse(
        Long paperId,
        String paperTitle,
        Long examCount,
        Long studentCount,
        Long passedCount,
        BigDecimal passRatePercent,
        BigDecimal averageScore
) {
}
