package com.gugugaga.jsmedicine.module.auth.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public record AppUserSession(
        Long userId,
        String username,
        String nickname,
        LocalDateTime loginAt,
        LocalDateTime expiresAt
) implements Serializable {
}
