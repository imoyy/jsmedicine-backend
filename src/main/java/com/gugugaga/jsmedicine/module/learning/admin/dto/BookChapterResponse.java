package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record BookChapterResponse(
        Long id,
        Long bookId,
        Long parentId,
        String chapterTitle,
        String content,
        Long paperId,
        Integer sortOrder,
        EnabledStatus status
) {
}
