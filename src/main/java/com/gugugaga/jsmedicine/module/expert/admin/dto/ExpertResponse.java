package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

import java.util.List;

public record ExpertResponse(
        Long id,
        Long userId,
        String realName,
        String avatarUrl,
        String title,
        String organization,
        Long organizationId,
        String specialty,
        Long practiceTypeId,
        String introduction,
        EnabledStatus status,
        EnabledStatus consultEnabled,
        String consultationNotice,
        Integer sortOrder,
        List<Long> categoryIds,
        List<ExpertExperienceResponse> experiences
) {
}
