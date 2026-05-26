package com.gugugaga.jsmedicine.module.interaction.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppQaQuestionRequest(
        Long expertCategoryId,
        Long expertId,

        @NotBlank(message = "title must not be blank")
        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @NotBlank(message = "content must not be blank")
        String content
) {
}
