package com.gugugaga.jsmedicine.module.learning.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AppExamRecordResponse(
        Long id,
        Long studentId,
        Long paperId,
        String paperName,
        String sourceType,
        Long sourceId,
        BigDecimal score,
        Integer passed,
        LocalDateTime startedAt,
        LocalDateTime submittedAt,
        List<AppExamAnswerResultResponse> answers
) {
}
