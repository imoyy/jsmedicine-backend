package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record CourseVideoResponse(
        Long id,
        Long courseId,
        String title,
        String videoUrl,
        Integer durationSeconds,
        Integer sortOrder,
        EnabledStatus status
) {
}
