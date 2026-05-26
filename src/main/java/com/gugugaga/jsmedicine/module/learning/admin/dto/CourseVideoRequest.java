package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CourseVideoRequest(
        @NotNull(message = "courseId must not be null")
        Long courseId,

        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @NotBlank(message = "videoUrl must not be blank")
        @Size(max = 512, message = "videoUrl length must be less than 512")
        String videoUrl,

        Integer durationSeconds,
        Integer sortOrder,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
