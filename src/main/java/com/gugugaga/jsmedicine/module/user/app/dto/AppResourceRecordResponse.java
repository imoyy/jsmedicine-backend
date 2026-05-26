package com.gugugaga.jsmedicine.module.user.app.dto;

import java.time.LocalDateTime;

public record AppResourceRecordResponse(
        Long id,
        String resourceType,
        Long resourceId,
        String source,
        Integer viewCount,
        LocalDateTime occurredAt
) {
}
