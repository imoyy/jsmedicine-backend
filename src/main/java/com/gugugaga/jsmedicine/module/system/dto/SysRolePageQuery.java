package com.gugugaga.jsmedicine.module.system.dto;

public record SysRolePageQuery(
        long page,
        long size,
        String sort,
        String keyword
) {
}
