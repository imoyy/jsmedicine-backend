package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

import java.time.LocalDateTime;

public record HomeContentResponse(
        Long id,
        Long categoryId,
        String contentType,
        Long targetId,
        String title,
        String coverUrl,
        String linkUrl,
        Integer sortOrder,
        LocalDateTime startAt,
        LocalDateTime endAt,
        EnabledStatus status
) {
}
