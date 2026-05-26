package com.gugugaga.jsmedicine.module.expert.app.dto;

import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertExperienceResponse;

import java.util.List;

public record AppExpertResponse(
        Long id,
        String realName,
        String avatarUrl,
        String title,
        String organization,
        String specialty,
        String introduction,
        String consultationNotice,
        Integer sortOrder,
        List<Long> categoryIds,
        List<ExpertExperienceResponse> experiences
) {
}
