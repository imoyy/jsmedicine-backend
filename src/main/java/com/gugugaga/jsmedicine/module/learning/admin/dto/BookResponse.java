package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record BookResponse(
        Long id,
        Long categoryId,
        String bookName,
        String author,
        String publisher,
        String coverUrl,
        String introduction,
        Integer totalPages,
        @Schema(description = "图书绑定的考卷 ID。图书采用单考卷模型，null 表示未配置考卷", example = "12")
        Long paperId,
        @Schema(description = "图书绑定考卷名称；未配置或考卷已失效时返回 null", example = "中医基础理论阶段测验")
        String paperTitle,
        Integer sortOrder,
        ReviewStatus reviewStatus,
        PublishStatus publishStatus,
        LocalDateTime publishedAt
) {
}
