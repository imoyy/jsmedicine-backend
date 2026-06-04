package com.gugugaga.jsmedicine.module.knowledge.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record KnowledgeEntryRequest(
        Long categoryId,

        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @Size(max = 512, message = "summary length must be less than 512")
        String summary,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        @Schema(description = "封面稳定读取地址，只能填写管理端封面上传接口返回的 /api/v1/files/{id}/content", example = "/api/v1/files/108/content")
        String coverUrl,

        @NotBlank(message = "content must not be blank")
        String content,

        @Size(max = 255, message = "keywords length must be less than 255")
        String keywords,

        @Size(max = 255, message = "source length must be less than 255")
        String source,

        Integer sortOrder,

        @NotNull(message = "reviewStatus must not be null")
        ReviewStatus reviewStatus,

        @NotNull(message = "publishStatus must not be null")
        PublishStatus publishStatus,

        LocalDateTime publishedAt
) {
}
