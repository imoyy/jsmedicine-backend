package com.gugugaga.jsmedicine.module.knowledge.app.dto;

public record AppKnowledgeSearchResult(
        Long id,
        String title,
        String summary,
        Long categoryId,
        String categoryName,
        String categoryCode
) {
}
