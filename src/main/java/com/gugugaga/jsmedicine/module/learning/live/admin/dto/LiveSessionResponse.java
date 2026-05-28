package com.gugugaga.jsmedicine.module.learning.live.admin.dto;

import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;
import java.util.List;

public record LiveSessionResponse(
        Long id,
        String title,
        String coverUrl,
        String anchorName,
        String speakerName,
        List<String> tags,
        String liveUrl,
        String playbackUrl,
        LocalDateTime startAt,
        LocalDateTime endAt,
        ReviewStatus reviewStatus,
        LiveStatus liveStatus,
        List<LiveSessionVideoResponse> videos
) {
}
