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

        @Schema(description = "首页内容类型，仅支持 course/book/podcast/topic/live",
                allowableValues = {"course", "book", "podcast", "topic", "live"}, example = "course")
        @NotBlank(message = "contentType must not be blank")
        @Size(max = 32, message = "contentType length must be less than 32")
        String contentType,

        @Schema(description = "关联资源 ID，资源型首页内容必填", example = "1")
        @Positive(message = "targetId must be greater than 0")
        Long targetId,

        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        @Schema(description = "首页展示标题", example = "首页课程推荐")
        String title,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        @Schema(description = "首页展示封面稳定读取地址，只能填写管理端封面上传接口返回的 /api/v1/files/{id}/content；如需复用关联资源封面，也应先使用统一封面上传链路登记", example = "/api/v1/files/102/content")
        String coverUrl,

        @Size(max = 512, message = "linkUrl length must be less than 512")
        @Schema(description = "跳转链接，当前资源型快捷配置一般留空", example = "https://example.com/activity")
        String linkUrl,

        Integer sortOrder,
        LocalDateTime startAt,
        LocalDateTime endAt,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
