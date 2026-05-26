package com.gugugaga.jsmedicine.module.knowledge.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;

public record KnowledgeEntryResponse(
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
        ReviewStatus reviewStatus,
        PublishStatus publishStatus,
        LocalDateTime publishedAt
) {
}
