package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

import java.time.LocalDateTime;

public record BookCategoryResponse(
        Long id,
        Long parentId,
        String categoryName,
        Integer sortOrder,
        EnabledStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
