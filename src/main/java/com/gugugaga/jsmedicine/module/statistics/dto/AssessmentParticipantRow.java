package com.gugugaga.jsmedicine.module.statistics.dto;

import com.gugugaga.jsmedicine.common.enums.ExamRecordStatus;
import com.gugugaga.jsmedicine.common.enums.ExamSubmitType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AssessmentParticipantRow(
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
        Long recordId,
        ExamRecordStatus recordStatus,
        ExamSubmitType submitType,
        BigDecimal score,
        Integer passed,
        LocalDateTime startedAt,
        LocalDateTime submittedAt
) {
}
