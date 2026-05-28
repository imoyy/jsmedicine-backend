package com.gugugaga.jsmedicine.module.learning.app.dto;

public record AppBookChapterResponse(
        Long id,
        Long bookId,
        Long parentId,
        String chapterTitle,
        String content,
        Integer startPage,
        Integer pageCount,
        Long paperId,
        Integer sortOrder
) {
}
