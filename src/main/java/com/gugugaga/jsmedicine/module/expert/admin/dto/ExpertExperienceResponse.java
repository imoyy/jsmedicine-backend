package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.ExperienceType;

import java.time.LocalDate;

public record ExpertExperienceResponse(
        Long id,
        Long expertId,
        ExperienceType experienceType,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Integer sortOrder
) {
}
