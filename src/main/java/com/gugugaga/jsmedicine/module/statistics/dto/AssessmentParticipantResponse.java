package com.gugugaga.jsmedicine.module.statistics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssessmentParticipantResponse(
        Long studentId,
        String studentName,
        String mobile,
        String maskedIdCardNo,
        String provinceCode,
        String provinceName,
        String cityCode,
        String cityName,
        String districtCode,
        String districtName,
        Long organizationId,
        String organizationName,
        LocalDateTime enteredAt,
        LocalDateTime submittedAt,
        String status,
        BigDecimal score,
        Boolean passed
) {
}
