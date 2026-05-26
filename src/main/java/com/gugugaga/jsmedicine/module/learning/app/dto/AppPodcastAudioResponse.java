package com.gugugaga.jsmedicine.module.learning.app.dto;

public record AppPodcastAudioResponse(
        Long id,
        Long podcastId,
        String title,
        String audioUrl,
        Integer durationSeconds,
        Integer sortOrder
) {
}
