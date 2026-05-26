package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record AdminUserPageQuery(
        long page,
        long size,
        String sort,
        String keyword,
        EnabledStatus status
) {
}
