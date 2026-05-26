package com.gugugaga.jsmedicine.module.expert.app.dto;

public record AppExpertCategoryResponse(
        Long id,
        Long parentId,
        String categoryName,
        Integer sortOrder
) {
}
