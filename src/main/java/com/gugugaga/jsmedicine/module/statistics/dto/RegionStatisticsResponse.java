package com.gugugaga.jsmedicine.module.statistics.dto;

public record RegionStatisticsResponse(
        String province,
        String city,
        Long studentCount,
        Long approvedStudentCount,
        Long enabledStudentCount
) {
}
