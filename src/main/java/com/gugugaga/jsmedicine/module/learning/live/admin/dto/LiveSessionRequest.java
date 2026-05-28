package com.gugugaga.jsmedicine.module.learning.live.admin.dto;

import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record LiveSessionRequest(
        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        String coverUrl,

        @Size(max = 128, message = "anchorName length must be less than 128")
        String anchorName,

        @Size(max = 128, message = "speakerName length must be less than 128")
        String speakerName,

        @Size(max = 20, message = "tags size must be less than or equal to 20")
        List<@NotBlank(message = "tag must not be blank") @Size(max = 32, message = "tag length must be less than 32") String> tags,

        @Size(max = 1024, message = "liveUrl length must be less than 1024")
        String liveUrl,

        @Size(max = 1024, message = "playbackUrl length must be less than 1024")
        String playbackUrl,

        @NotNull(message = "startAt must not be null")
        LocalDateTime startAt,

        LocalDateTime endAt,

        @NotNull(message = "reviewStatus must not be null")
        ReviewStatus reviewStatus,

        @NotNull(message = "liveStatus must not be null")
        LiveStatus liveStatus
) {
}
