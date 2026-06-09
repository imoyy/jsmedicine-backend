package com.gugugaga.jsmedicine.module.statistics.dto;

import com.gugugaga.jsmedicine.common.enums.Gender;

public record StudentScoreResponse(
        Long studentId,
        String studentNo,
        String realName,
        Gender gender,
        String mobile,
        Integer age,
        String educationLevel,
        String organization,
        String practiceTypeName,
        String theoryTrainingStatus,
        String clinicalPracticeStatus,
        String practicalAssessmentStatus,
        String theoryAssessmentStatus,
        String onlineTrainingStatus
) {
}
