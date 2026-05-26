package com.gugugaga.jsmedicine.module.knowledge.app.dto;

import java.util.List;

public record AppKnowledgeCategoryResponse(
        Long id,
        Long parentId,
        String categoryName,
        String categoryCode,
        String description,
        Integer sortOrder,
        List<AppKnowledgeCategoryResponse> children
) {
}
