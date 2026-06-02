package com.gugugaga.jsmedicine.module.learning.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AppTopicResponse(
        Long id,
        String title,
        String summary,
        String learningRequirements,
        String coverUrl,
        Long viewCount,
        LocalDateTime publishedAt,
        Long favoriteCount,
        Boolean favorited,
        List<AppTopicItemResponse> items
) {
}
