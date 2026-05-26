package com.gugugaga.jsmedicine.module.knowledge.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record KnowledgeCategoryResponse(
        Long id,
        Long parentId,
        String categoryName,
        String categoryCode,
        String description,
        Integer sortOrder,
        EnabledStatus status
) {
}
