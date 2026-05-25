package com.gugugaga.jsmedicine.module.auth.dto;

import java.time.LocalDateTime;
import java.util.List;

public record LoginResponse(
        String tokenType,
        String accessToken,
        Long expiresIn,
        AdminProfile admin,
        List<String> permissions
) {

    public record AdminProfile(
            Long id,
            String username,
            String realName,
            LocalDateTime lastLoginAt
    ) {
    }
}
