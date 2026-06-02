package com.gugugaga.jsmedicine.module.interaction.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppBrowseHistoryRequest(
        @NotBlank(message = "resourceType must not be blank")
        @Size(max = 32, message = "resourceType length must be less than 32")
        String resourceType,
        @NotNull(message = "resourceId must not be null")
        Long resourceId,
        @Size(max = 64, message = "source length must be less than 64")
        String source,
        @Max(value = 1000, message = "viewCount must be less than or equal to 1000")
        Integer viewCount
) {
}
