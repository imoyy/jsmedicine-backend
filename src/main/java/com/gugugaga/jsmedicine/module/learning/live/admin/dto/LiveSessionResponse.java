package com.gugugaga.jsmedicine.module.learning.live.admin.dto;

import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;

public record LiveSessionResponse(
        Long id,
        String title,
        String coverUrl,
        String anchorName,
        String liveUrl,
        String playbackUrl,
        LocalDateTime startAt,
        LocalDateTime endAt,
        ReviewStatus reviewStatus,
        LiveStatus liveStatus
) {
}
