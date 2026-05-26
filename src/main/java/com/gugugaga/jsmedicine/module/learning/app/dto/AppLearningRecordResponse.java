package com.gugugaga.jsmedicine.module.learning.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AppLearningRecordResponse(
        Long id,
        Long studentId,
        String resourceType,
        Long resourceId,
        Integer studySeconds,
        BigDecimal progressPercent,
        Integer completed,
        LocalDateTime completedAt,
        LocalDateTime lastStudiedAt
) {
}
