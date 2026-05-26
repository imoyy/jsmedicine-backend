package com.gugugaga.jsmedicine.module.system.dto;

import java.time.LocalDateTime;

public record AuditRecordResponse(
        Long id,
        String targetType,
        Long targetId,
        Integer beforeStatus,
        Integer afterStatus,
        String auditComment,
        Long auditorId,
        LocalDateTime auditedAt,
        LocalDateTime createdAt
) {
}
