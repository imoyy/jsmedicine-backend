package com.gugugaga.jsmedicine.module.auth.admin.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record AdminSession(
        Long adminId,
        String username,
        String realName,
        List<String> roleCodes,
        List<String> permissionCodes,
        LocalDateTime loginAt,
        LocalDateTime expiresAt
) implements Serializable {
}

