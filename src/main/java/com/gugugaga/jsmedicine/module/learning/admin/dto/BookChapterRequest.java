package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookChapterRequest(
        @NotNull(message = "bookId must not be null")
        Long bookId,

        Long parentId,

        @NotBlank(message = "chapterTitle must not be blank")
        @Size(max = 128, message = "chapterTitle length must be less than 128")
        String chapterTitle,

        String content,

        @Min(value = 1, message = "startPage must be greater than or equal to 1")
        Integer startPage,

        @Min(value = 0, message = "pageCount must be greater than or equal to 0")
        Integer pageCount,

        Long paperId,
        Integer sortOrder,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
