package com.gugugaga.jsmedicine.module.statistics.dto;

public record TopicStudentStatisticsSummaryResponse(
        Long totalStudents,
        Long topicStudents,
        Long learningStudents,
        Long completedStudents,
        Long notStartedStudents
) {
}
