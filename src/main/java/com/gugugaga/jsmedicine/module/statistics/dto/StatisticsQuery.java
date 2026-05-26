package com.gugugaga.jsmedicine.module.statistics.dto;

import java.time.LocalDateTime;

public record StatisticsQuery(
        LocalDateTime startAt,
        LocalDateTime endAt,
        String resourceType,
        Long resourceId,
        Long studentId,
        String province,
        String city
) {
}
