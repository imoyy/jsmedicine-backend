package com.gugugaga.jsmedicine.module.learning.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AppPodcastResponse(
        Long id,
        String title,
        String summary,
        String coverUrl,
        String speakerName,
        List<String> tags,
        LocalDateTime publishedAt,
        BigDecimal progressPercent,
        Integer studySeconds,
        List<AppPodcastAudioResponse> audios
) {
}
