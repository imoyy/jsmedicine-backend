package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;

public record CourseResponse(
        Long id,
        String courseName,
        String subtitle,
        String coverUrl,
        String lecturerName,
        String introduction,
        Long paperId,
        Integer sortOrder,
        ReviewStatus reviewStatus,
        PublishStatus publishStatus,
        LocalDateTime publishedAt
) {
}
