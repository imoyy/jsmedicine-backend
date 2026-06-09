package com.gugugaga.jsmedicine.module.statistics.dto;

import com.gugugaga.jsmedicine.common.enums.AssessmentEventType;

import java.time.LocalDateTime;

public record AssessmentLatestEventResponse(
        Long eventId,
        AssessmentEventType eventType,
        Long studentId,
        String studentName,
        String organizationName,
        String provinceCode,
        String cityCode,
        String districtCode,
        LocalDateTime eventTime,
        String description
) {
}
