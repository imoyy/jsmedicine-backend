package com.gugugaga.jsmedicine.module.knowledge.app.dto;

import java.time.LocalDateTime;

public record AppKnowledgeEntryResponse(
        Long id,
        Long categoryId,
        String title,
        String summary,
        String coverUrl,
        String content,
        String keywords,
        String source,
        Integer sortOrder,
        Long viewCount,
        LocalDateTime publishedAt
) {
}
