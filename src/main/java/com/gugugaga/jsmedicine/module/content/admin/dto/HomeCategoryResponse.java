package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record HomeCategoryResponse(
        Long id,
        Long parentId,
        String categoryName,
        String categoryCode,
        String iconUrl,
        String description,
        Integer sortOrder,
        EnabledStatus status
) {
}
