package com.gugugaga.jsmedicine.module.content.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TopicItemRequest(
        @Schema(description = "专题分项类型，仅支持 course/book/podcast", allowableValues = {"course", "book", "podcast"},
                example = "course")
        @NotBlank(message = "itemType must not be blank")
        @Size(max = 32, message = "itemType length must be less than 32")
        String itemType,

        @Schema(description = "关联资源 ID", example = "1")
        @NotNull(message = "itemId must not be null")
        @Positive(message = "itemId must be greater than 0")
        Long itemId,

        @Schema(description = "展示排序，允许为空；为空时按请求顺序自动归一化", example = "1")
        Integer sortOrder
) {
}
