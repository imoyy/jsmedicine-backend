package com.gugugaga.jsmedicine.module.auth.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppWechatWebLoginRequest(
        @NotBlank(message = "code must not be blank")
        @Size(max = 128, message = "code length must be less than 128")
        String code,

        @NotBlank(message = "state must not be blank")
        @Size(max = 64, message = "state length must be less than 64")
        String state
) {
}
