package com.gugugaga.jsmedicine.module.content.app.dto;

public record AppHomeItemResponse(
        Long id,
        String contentType,
        String contentTypeLabel,
        Long targetId,
        String title,
        String subtitle,
        String summary,
        String coverUrl,
        String linkUrl,
        Integer sortOrder
) {
}
