package com.gugugaga.jsmedicine.module.system.dto;

public record SysAdminPageQuery(
        long page,
        long size,
        String sort,
        String keyword
) {
}
