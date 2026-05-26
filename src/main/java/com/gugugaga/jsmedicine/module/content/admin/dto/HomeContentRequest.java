package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record HomeContentRequest(
        @NotNull(message = "categoryId must not be null")
        Long categoryId,

        @NotBlank(message = "contentType must not be blank")
        @Size(max = 32, message = "contentType length must be less than 32")
        String contentType,

        Long targetId,

        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        String coverUrl,

        @Size(max = 512, message = "linkUrl length must be less than 512")
        String linkUrl,

        Integer sortOrder,
        LocalDateTime startAt,
        LocalDateTime endAt,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
