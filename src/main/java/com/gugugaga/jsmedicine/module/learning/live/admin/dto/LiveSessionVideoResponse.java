package com.gugugaga.jsmedicine.module.learning.live.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record LiveSessionVideoResponse(
        Long id,
        Long liveSessionId,
        String title,
        String videoUrl,
        Integer durationSeconds,
        Integer sortOrder,
        EnabledStatus status
) {
}
