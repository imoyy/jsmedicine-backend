package com.gugugaga.jsmedicine.module.expert.app.dto;

import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertExperienceResponse;

import java.time.LocalDate;
import java.util.List;

public record AppExpertResponse(
        Long id,
        String realName,
        Gender gender,
        LocalDate birthDate,
        String mobile,
        String avatarUrl,
        String coverUrl,
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
