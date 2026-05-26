package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

public record AdminContentPageQuery(
        long page,
        long size,
        String sort,
        String keyword,
        EnabledStatus status,
        ReviewStatus reviewStatus
) {
}
