package com.gugugaga.jsmedicine.module.system.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

import java.time.LocalDateTime;
import java.util.List;

public record SysAdminResponse(
        Long id,
        String username,
        String realName,
        String mobile,
        String email,
        String avatarUrl,
        EnabledStatus status,
        LocalDateTime lastLoginAt,
        String lastLoginIp,
        List<String> roles
) {
}
