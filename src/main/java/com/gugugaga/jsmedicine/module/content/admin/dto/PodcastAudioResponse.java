package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record PodcastAudioResponse(
        Long id,
        Long podcastId,
        String title,
        String audioUrl,
        Integer durationSeconds,
        Integer sortOrder,
        EnabledStatus status
) {
}
