package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;

public record BookCategoryBookResponse(
        Long id,
        Long categoryId,
        String bookName,
        String author,
        String coverUrl,
        ReviewStatus reviewStatus,
        PublishStatus publishStatus,
        LocalDateTime updatedAt
) {
}
