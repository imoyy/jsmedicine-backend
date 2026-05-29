package com.gugugaga.jsmedicine.module.user.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppAvatarConfirmRequest(
        @NotBlank(message = "objectKey must not be blank")
        @Size(max = 512, message = "objectKey length must be less than 512")
        String objectKey,

        @Size(max = 255, message = "originalName length must be less than 255")
        String originalName
) {
}
