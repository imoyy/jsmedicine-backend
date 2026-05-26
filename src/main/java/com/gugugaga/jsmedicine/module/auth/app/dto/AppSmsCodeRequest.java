package com.gugugaga.jsmedicine.module.auth.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AppSmsCodeRequest(
        @NotBlank(message = "mobile must not be blank")
        @Pattern(regexp = "^1\\d{10}$", message = "mobile format is invalid")
        String mobile
) {
}
