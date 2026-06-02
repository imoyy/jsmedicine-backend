package com.gugugaga.jsmedicine.module.learning.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AppTopicDetailResponse(
        Long id,
        String title,
        String summary,
        String learningRequirements,
        String coverUrl,
        List<String> tags,
        Long viewCount,
        LocalDateTime publishedAt,
        Long favoriteCount,
        Boolean favorited,
        List<AppTopicSectionResponse> sections
) {
}
