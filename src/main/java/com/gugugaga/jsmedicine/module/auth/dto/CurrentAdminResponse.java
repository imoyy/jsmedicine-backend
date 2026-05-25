package com.gugugaga.jsmedicine.module.auth.dto;

import java.util.List;

public record CurrentAdminResponse(
        Long id,
        String username,
        String realName,
        List<String> roles,
        List<String> permissions
) {
}
