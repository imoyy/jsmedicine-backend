package com.gugugaga.jsmedicine.module.knowledge.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record KnowledgeCategoryRequest(
        Long parentId,

        @NotBlank(message = "categoryName must not be blank")
        @Size(max = 64, message = "categoryName length must be less than 64")
        String categoryName,

        @Size(max = 64, message = "categoryCode length must be less than 64")
        String categoryCode,

        @Size(max = 255, message = "description length must be less than 255")
        String description,

        Integer sortOrder,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
