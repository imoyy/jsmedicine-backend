package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record PodcastRequest(
        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @Size(max = 512, message = "summary length must be less than 512")
        String summary,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        @Schema(description = "封面稳定读取地址，只能填写管理端封面上传接口返回的 /api/v1/files/{id}/content", example = "/api/v1/files/103/content")
        String coverUrl,

        @Size(max = 128, message = "speakerName length must be less than 128")
        String speakerName,

        @Size(max = 20, message = "tags size must be less than or equal to 20")
        List<@NotBlank(message = "tag must not be blank") @Size(max = 32, message = "tag length must be less than 32") String> tags,

        Integer sortOrder,

        @NotNull(message = "reviewStatus must not be null")
        ReviewStatus reviewStatus,

        @NotNull(message = "publishStatus must not be null")
        PublishStatus publishStatus,

        LocalDateTime publishedAt
) {
}
