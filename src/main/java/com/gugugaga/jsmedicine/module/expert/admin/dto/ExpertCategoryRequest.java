package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpertCategoryRequest(
        Long parentId,

        @NotBlank(message = "categoryName must not be blank")
        @Size(max = 64, message = "categoryName length must be less than 64")
        String categoryName,

        Integer sortOrder,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
