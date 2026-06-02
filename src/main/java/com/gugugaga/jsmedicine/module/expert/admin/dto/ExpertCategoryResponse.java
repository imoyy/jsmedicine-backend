package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record ExpertCategoryResponse(
        Long id,
        Long parentId,
        String parentCategoryName,
        Integer level,
        String categoryName,
        Integer sortOrder,
        EnabledStatus status
) {
}
