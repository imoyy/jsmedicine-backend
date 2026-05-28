package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PracticeTypeUpsertRequest(
        Long parentId,

        @NotBlank(message = "typeCode must not be blank")
        @Size(max = 64, message = "typeCode length must be less than 64")
        String typeCode,

        @NotBlank(message = "typeName must not be blank")
        @Size(max = 128, message = "typeName length must be less than 128")
        String typeName,

        @NotNull(message = "status must not be null")
        EnabledStatus status,

        @Min(value = 0, message = "sortOrder must be greater than or equal to 0")
        Integer sortOrder
) {
}
