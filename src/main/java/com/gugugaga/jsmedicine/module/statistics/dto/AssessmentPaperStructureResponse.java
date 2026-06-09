package com.gugugaga.jsmedicine.module.statistics.dto;

import java.math.BigDecimal;
import java.util.List;

public record AssessmentPaperStructureResponse(
        Long paperId,
        String paperName,
        BigDecimal totalScore,
        BigDecimal passScore,
        Integer durationMinutes,
        long questionCount,
        List<AssessmentQuestionTypeBreakdownResponse> questionTypeBreakdown
) {
}
