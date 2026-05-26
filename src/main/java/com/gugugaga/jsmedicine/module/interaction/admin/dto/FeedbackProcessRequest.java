package com.gugugaga.jsmedicine.module.interaction.admin.dto;

import jakarta.validation.constraints.Size;

public record FeedbackProcessRequest(
        @Size(max = 512, message = "processNote length must be less than 512")
        String processNote
) {
}
