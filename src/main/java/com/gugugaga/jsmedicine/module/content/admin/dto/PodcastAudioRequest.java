package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PodcastAudioRequest(
        @NotNull(message = "podcastId must not be null")
        Long podcastId,

        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @NotBlank(message = "audioUrl must not be blank")
        @Size(max = 512, message = "audioUrl length must be less than 512")
        String audioUrl,

        Integer durationSeconds,
        Integer sortOrder,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
