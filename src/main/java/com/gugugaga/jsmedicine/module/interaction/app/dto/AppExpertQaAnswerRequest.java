package com.gugugaga.jsmedicine.module.interaction.app.dto;

import jakarta.validation.constraints.NotBlank;

public record AppExpertQaAnswerRequest(
        @NotBlank(message = "content must not be blank")
        String content
) {
}
