package com.gugugaga.jsmedicine.module.interaction.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppFeedbackRequest(
        @Size(max = 32, message = "feedbackType length must be less than 32")
        String feedbackType,

        @NotBlank(message = "content must not be blank")
        String content,

        @Size(max = 128, message = "contact length must be less than 128")
        String contact
) {
}
