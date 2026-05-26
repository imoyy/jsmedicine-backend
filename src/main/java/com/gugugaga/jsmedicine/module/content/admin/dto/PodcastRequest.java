package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PodcastRequest(
        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @Size(max = 512, message = "summary length must be less than 512")
        String summary,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        String coverUrl,

        Integer sortOrder,

        @NotNull(message = "reviewStatus must not be null")
        ReviewStatus reviewStatus,

        @NotNull(message = "publishStatus must not be null")
        PublishStatus publishStatus,

        LocalDateTime publishedAt
) {
}
