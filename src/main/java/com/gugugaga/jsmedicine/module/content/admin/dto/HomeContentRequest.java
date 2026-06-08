package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record HomeContentRequest(
        @NotNull(message = "categoryId must not be null")
        @Schema(description = "首页分类 ID", example = "1")
        Long categoryId,

        @NotBlank(message = "contentType must not be blank")
        @Schema(
                description = "首页内容来源模块。首页分类只承担展示位语义，资源类型以 contentType 为准。支持 course/book/article/podcast/topic/knowledge/live",
                allowableValues = {"course", "book", "article", "podcast", "topic", "knowledge", "live"},
                example = "course"
        )
        @Size(max = 32, message = "contentType length must be less than 32")
        String contentType,

        @Schema(description = "关联资源 ID，资源型首页内容必填", example = "1001")
        @Positive(message = "targetId must be greater than 0")
        Long targetId,

        @Size(max = 128, message = "title length must be less than 128")
        @Schema(description = "兼容字段。首页展示标题由关联资源自动派生，前端无需再维护。", example = "首页课程推荐")
        String title,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        @Schema(description = "兼容字段。首页展示封面由关联资源自动派生，前端无需再维护。", example = "/api/v1/files/102/content")
        String coverUrl,

        @Size(max = 512, message = "linkUrl length must be less than 512")
        @Schema(description = "兼容字段。资源型首页配置当前通常留空，前端无需再维护。", example = "https://example.com/activity")
        String linkUrl,

        Integer sortOrder,
        LocalDateTime startAt,
        LocalDateTime endAt,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
