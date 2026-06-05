package com.gugugaga.jsmedicine.module.learning.live.admin.dto;

import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

public record LiveSessionStreamResponse(
        Long id,
        String streamName,
        String publishUrl,
        String httpFlvUrl,
        String hlsUrl,
        String callbackUrl,
        String liveUrl,
        String playbackUrl,
        ReviewStatus reviewStatus,
        LiveStatus liveStatus
) {
}
