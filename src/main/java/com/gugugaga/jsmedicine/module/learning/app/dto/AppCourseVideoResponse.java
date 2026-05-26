package com.gugugaga.jsmedicine.module.learning.app.dto;

public record AppCourseVideoResponse(
        Long id,
        Long courseId,
        String title,
        String videoUrl,
        Integer durationSeconds,
        Integer sortOrder
) {
}
