package com.gugugaga.jsmedicine.module.statistics.dto;

public record StudyHoursRegionResponse(
        String province,
        String city,
        String district,
        Long studentCount,
        Long completedCount,
        Long totalStudySeconds,
        Double totalStudyHours,
        Double averageStudyHours,
        Double averageProgressPercent
) {
}
