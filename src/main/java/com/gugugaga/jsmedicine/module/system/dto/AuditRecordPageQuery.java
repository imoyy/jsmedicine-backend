package com.gugugaga.jsmedicine.module.system.dto;

public record AuditRecordPageQuery(
        long page,
        long size,
        String sort,
        String targetType,
        Long targetId,
        Long auditorId
) {
}
