package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpertCategoryRequest(
        @Schema(description = "父分类 ID；为空表示一级科室，非空表示二级科室，且父分类必须是一级科室", example = "1")
        Long parentId,

        @NotBlank(message = "categoryName must not be blank")
        @Size(max = 64, message = "categoryName length must be less than 64")
        @Schema(description = "分类名称", example = "针灸科")
        String categoryName,

        @Schema(description = "排序值", example = "1")
        Integer sortOrder,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
