package com.gugugaga.jsmedicine.module.auth.app.dto;

import java.time.LocalDateTime;

public record AppLoginResponse(
        String tokenType,
        String accessToken,
        Long expiresIn,
        UserProfile user
) {

    public record UserProfile(
            Long id,
            String username,
            String nickname,
            String avatarUrl,
            LocalDateTime lastLoginAt
    ) {
    }
}
