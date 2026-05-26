package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

import java.time.LocalDateTime;

public record BookResponse(
        Long id,
        Long categoryId,
        String bookName,
        String author,
        String publisher,
        String coverUrl,
        String introduction,
        Long paperId,
        Integer sortOrder,
        ReviewStatus reviewStatus,
        PublishStatus publishStatus,
        LocalDateTime publishedAt
) {
}
