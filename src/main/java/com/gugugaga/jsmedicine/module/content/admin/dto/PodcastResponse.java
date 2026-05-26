package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;

public record PodcastResponse(
        Long id,
        String title,
        String summary,
        String coverUrl,
        Integer sortOrder,
        ReviewStatus reviewStatus,
        PublishStatus publishStatus,
        LocalDateTime publishedAt
) {
}
