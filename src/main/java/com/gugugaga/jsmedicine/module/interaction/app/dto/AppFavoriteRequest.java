package com.gugugaga.jsmedicine.module.interaction.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AppFavoriteRequest(
        @NotBlank(message = "resourceType must not be blank")
        @Size(max = 32, message = "resourceType length must be less than 32")
        String resourceType,
        @NotNull(message = "resourceId must not be null")
        Long resourceId,
        @NotNull(message = "favorited must not be null")
        Boolean favorited
) {
}
