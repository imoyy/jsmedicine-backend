package com.gugugaga.jsmedicine.module.content.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AppArticleCardResponse(
        Long id,
        String title,
        String summary,
        String coverUrl,
        String authorName,
        String source,
        List<String> tags,
        Long viewCount,
        Long favoriteCount,
        Boolean favorited,
        LocalDateTime publishedAt
) {
}
