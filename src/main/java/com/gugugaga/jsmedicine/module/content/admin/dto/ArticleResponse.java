package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;

public record ArticleResponse(
        Long id,
        String title,
        String summary,
        String coverUrl,
        String content,
        String authorName,
        Long viewCount,
        ReviewStatus reviewStatus,
        PublishStatus publishStatus,
        LocalDateTime publishedAt
) {
}
