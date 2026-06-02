package com.gugugaga.jsmedicine.module.learning.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AppCourseResponse(
        Long id,
        String courseName,
        String subtitle,
        String coverUrl,
        String lecturerName,
        String introduction,
        Long paperId,
        LocalDateTime publishedAt,
        Long browseCount,
        Long favoriteCount,
        Boolean favorited,
        BigDecimal progressPercent,
        Integer studySeconds,
        List<AppCourseVideoResponse> videos
) {
}
