package com.gugugaga.jsmedicine.module.content.admin.dto;

public record HomeContentCandidateResponse(
        Long id,
        String title,
        String coverUrl,
        String subtitle,
        String resourceStatus,
        Boolean available
) {
}
