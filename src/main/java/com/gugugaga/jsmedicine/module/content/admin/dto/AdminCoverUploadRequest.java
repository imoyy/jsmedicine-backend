package com.gugugaga.jsmedicine.module.content.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdminCoverUploadRequest(
        @NotBlank(message = "usage must not be blank")
        @Size(max = 64, message = "usage length must be less than 64")
        String usage,

        @NotBlank(message = "originalName must not be blank")
        @Size(max = 255, message = "originalName length must be less than 255")
        String originalName,

        @NotBlank(message = "contentType must not be blank")
        @Size(max = 128, message = "contentType length must be less than 128")
        String contentType,

        @NotNull(message = "fileSize must not be null")
        @Positive(message = "fileSize must be greater than 0")
        Long fileSize
) {
}
