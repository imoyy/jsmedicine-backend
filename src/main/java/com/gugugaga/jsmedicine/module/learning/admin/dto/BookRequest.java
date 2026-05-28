package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record BookRequest(
        Long categoryId,

        @NotBlank(message = "bookName must not be blank")
        @Size(max = 128, message = "bookName length must be less than 128")
        String bookName,

        @Size(max = 64, message = "author length must be less than 64")
        String author,

        @Size(max = 64, message = "publisher length must be less than 64")
        String publisher,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        String coverUrl,

        String introduction,

        @Min(value = 0, message = "totalPages must be greater than or equal to 0")
        Integer totalPages,

        Long paperId,
        Integer sortOrder,

        @NotNull(message = "reviewStatus must not be null")
        ReviewStatus reviewStatus,

        @NotNull(message = "publishStatus must not be null")
        PublishStatus publishStatus,

        LocalDateTime publishedAt
) {
}
