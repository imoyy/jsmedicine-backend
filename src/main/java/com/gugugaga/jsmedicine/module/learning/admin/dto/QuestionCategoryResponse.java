package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record QuestionCategoryResponse(
        Long id,
        Long parentId,
        String categoryName,
        Integer sortOrder,
        EnabledStatus status
) {
}
