package com.gugugaga.jsmedicine.module.learning.live.app.dto;

import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionVideoResponse;

import java.time.LocalDateTime;
import java.util.List;

public record AppLiveSessionResponse(
        Long id,
        String title,
        String coverUrl,
        String anchorName,
        String speakerName,
        List<String> tags,
        String liveUrl,
        String playbackUrl,
        String streamName,
        String httpFlvUrl,
        String hlsUrl,
        LocalDateTime startAt,
        LocalDateTime endAt,
        ReviewStatus reviewStatus,
        LiveStatus liveStatus,
        Long browseCount,
        Long favoriteCount,
        Boolean favorited,
        List<LiveSessionVideoResponse> videos
) {
}
