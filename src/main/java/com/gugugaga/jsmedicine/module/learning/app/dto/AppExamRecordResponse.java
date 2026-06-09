package com.gugugaga.jsmedicine.module.learning.app.dto;

import com.gugugaga.jsmedicine.common.enums.ExamRecordStatus;
import com.gugugaga.jsmedicine.common.enums.ExamSubmitType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AppExamRecordResponse(
        Long id,
        Long studentId,
        Long paperId,
        Long assessmentId,
        String paperName,
        String sourceType,
        Long sourceId,
        BigDecimal score,
        Integer passed,
        ExamRecordStatus status,
        ExamSubmitType submitType,
        LocalDateTime startedAt,
        LocalDateTime submittedAt,
        LocalDateTime lastActiveAt,
        List<AppExamAnswerResultResponse> answers
) {
}
