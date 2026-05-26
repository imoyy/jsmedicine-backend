package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CourseRequest(
        @NotBlank(message = "courseName must not be blank")
        @Size(max = 128, message = "courseName length must be less than 128")
        String courseName,

        @Size(max = 255, message = "subtitle length must be less than 255")
        String subtitle,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        String coverUrl,

        @Size(max = 64, message = "lecturerName length must be less than 64")
        String lecturerName,

        String introduction,
        Long paperId,
        Integer sortOrder,

        @NotNull(message = "reviewStatus must not be null")
        ReviewStatus reviewStatus,

        @NotNull(message = "publishStatus must not be null")
        PublishStatus publishStatus,

        LocalDateTime publishedAt
) {
}
