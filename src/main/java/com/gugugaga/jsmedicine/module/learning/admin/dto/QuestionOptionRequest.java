package com.gugugaga.jsmedicine.module.learning.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionOptionRequest(
        @NotBlank(message = "optionKey must not be blank")
        @Size(max = 16, message = "optionKey length must be less than 16")
        String optionKey,

        @NotBlank(message = "optionContent must not be blank")
        String optionContent,

        @NotNull(message = "correct must not be null")
        Boolean correct,

        Integer sortOrder
) {
}
