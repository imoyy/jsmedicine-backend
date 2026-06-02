package com.gugugaga.jsmedicine.module.learning.app.dto;

import java.math.BigDecimal;
import java.util.List;

public record AppTopicResourceCardResponse(
        String resourceType,
        String resourceTypeLabel,
        Long resourceId,
        String title,
        String subtitle,
        String coverUrl,
        List<String> tags,
        Long browseCount,
        Long favoriteCount,
        Boolean favorited,
        BigDecimal progressPercent,
        Integer studySeconds
) {
}
