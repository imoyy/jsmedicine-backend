package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "封面稳定读取地址，只能填写管理端封面上传接口返回的 /api/v1/files/{id}/content", example = "/api/v1/files/105/content")
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
