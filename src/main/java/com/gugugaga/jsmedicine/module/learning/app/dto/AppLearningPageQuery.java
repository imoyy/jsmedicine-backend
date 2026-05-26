package com.gugugaga.jsmedicine.module.learning.app.dto;

public record AppLearningPageQuery(
        long page,
        long size,
        String sort,
        String keyword,
        Long categoryId
) {
}
