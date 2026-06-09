package com.gugugaga.jsmedicine.module.learning.app.dto;

import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.enums.AssessmentType;
import com.gugugaga.jsmedicine.common.enums.ExamRecordStatus;

import java.time.LocalDateTime;

public record AppExamAssessmentResponse(
        Long id,
        String assessmentName,
        Long paperId,
        String paperName,
        AssessmentType assessmentType,
        AssessmentStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime serverTime,
        long countdownSeconds,
        Integer durationMinutes,
        Long participantRecordId,
        ExamRecordStatus participantStatus
) {
}
