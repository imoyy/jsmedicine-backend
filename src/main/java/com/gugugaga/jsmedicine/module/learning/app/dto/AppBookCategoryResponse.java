package com.gugugaga.jsmedicine.module.learning.app.dto;

public record AppBookCategoryResponse(
        Long id,
        Long parentId,
        String categoryName,
        Integer sortOrder
) {
}
