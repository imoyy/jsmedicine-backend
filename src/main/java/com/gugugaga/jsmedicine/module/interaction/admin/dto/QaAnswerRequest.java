package com.gugugaga.jsmedicine.module.interaction.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QaAnswerRequest(
        Long expertId,

        @NotBlank(message = "content must not be blank")
        String content,

        @Size(max = 512, message = "auditComment length must be less than 512")
        String auditComment
) {
}
