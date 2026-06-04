package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "封面稳定读取地址，只能填写管理端封面上传接口返回的 /api/v1/files/{id}/content", example = "/api/v1/files/106/content")
        String coverUrl,

        String introduction,

        @Min(value = 0, message = "totalPages must be greater than or equal to 0")
        Integer totalPages,

        @Schema(description = "图书级单考卷配置字段。传考卷 ID 表示绑定一张考卷，传 null 表示不配置考卷；复用图书新增/修改接口维护，不单独提供图书考卷配置接口",
                example = "12")
        Long paperId,
        Integer sortOrder,

        @NotNull(message = "reviewStatus must not be null")
        ReviewStatus reviewStatus,

        @NotNull(message = "publishStatus must not be null")
        PublishStatus publishStatus,

        LocalDateTime publishedAt
) {
}
