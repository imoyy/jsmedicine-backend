package com.gugugaga.jsmedicine.module.statistics.dto;

import com.gugugaga.jsmedicine.common.enums.Gender;

public record TopicStudentStatisticsRecordResponse(
        Long studentId,
        String studentNo,
        String realName,
        Gender gender,
        String mobile,
        Integer age,
        String educationLevel,
        String organization,
        String practiceTypeName,
        Double studyHours,
        String topicLearningStatus,
        String topicLearningStatusLabel,
        Boolean isLearningCurrentTopic
) {
}
