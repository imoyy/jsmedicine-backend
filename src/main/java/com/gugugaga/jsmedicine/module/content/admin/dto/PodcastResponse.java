package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PodcastResponse(
        Long id,
        String title,
        String summary,
        String coverUrl,
        String speakerName,
        List<String> tags,
        Integer sortOrder,
        ReviewStatus reviewStatus,
        PublishStatus publishStatus,
        LocalDateTime publishedAt
) {
}
