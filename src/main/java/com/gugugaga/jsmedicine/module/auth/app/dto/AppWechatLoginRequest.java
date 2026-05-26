package com.gugugaga.jsmedicine.module.auth.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppWechatLoginRequest(
        @NotBlank(message = "code must not be blank")
        @Size(max = 128, message = "code length must be less than 128")
        String code,

        @Size(max = 64, message = "nickname length must be less than 64")
        String nickname,

        @Size(max = 512, message = "avatarUrl length must be less than 512")
        String avatarUrl
) {
}
