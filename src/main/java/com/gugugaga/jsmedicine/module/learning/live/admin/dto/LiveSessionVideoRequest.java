package com.gugugaga.jsmedicine.module.learning.live.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LiveSessionVideoRequest(
        @NotNull(message = "liveSessionId must not be null")
        Long liveSessionId,

        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @NotBlank(message = "videoUrl must not be blank")
        @Size(max = 1024, message = "videoUrl length must be less than 1024")
        String videoUrl,

        Integer durationSeconds,
        Integer sortOrder,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
