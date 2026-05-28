package com.gugugaga.jsmedicine.module.user.app.dto;

import com.gugugaga.jsmedicine.common.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AppProfileUpdateRequest(
        @Size(max = 64, message = "nickname length must be less than 64")
        String nickname,

        @Size(max = 255, message = "profileSignature length must be less than 255")
        String profileSignature,

        @Size(max = 512, message = "avatarUrl length must be less than 512")
        String avatarUrl,

        @Email(message = "email format is invalid")
        @Size(max = 128, message = "email length must be less than 128")
        String email,

        Gender gender
) {
}
