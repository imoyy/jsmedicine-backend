package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.ExperienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ExpertExperienceRequest(
        @NotNull(message = "experienceType must not be null")
        ExperienceType experienceType,

        @NotBlank(message = "title must not be blank")
        @Size(max = 255, message = "title length must be less than 255")
        String title,

        String description,
        LocalDate startDate,
        LocalDate endDate,
        Integer sortOrder
) {
}
