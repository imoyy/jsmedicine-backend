package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TopicResponse(
        Long id,
        String title,
        String summary,
        String learningRequirements,
        String coverUrl,
        Integer sortOrder,
        Long viewCount,
        ReviewStatus reviewStatus,
        PublishStatus publishStatus,
        LocalDateTime publishedAt,
        List<TopicItemResponse> items
) {
}
