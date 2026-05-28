package com.gugugaga.jsmedicine.module.learning.app.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AppBookResponse(
        Long id,
        Long categoryId,
        String bookName,
        String author,
        String publisher,
        String coverUrl,
        String introduction,
        Integer totalPages,
        Long paperId,
        LocalDateTime publishedAt,
        BigDecimal progressPercent,
        Integer studySeconds,
        List<AppBookChapterResponse> chapters
) {
}
