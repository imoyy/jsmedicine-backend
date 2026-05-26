package com.gugugaga.jsmedicine.module.learning.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

public record AdminLearningPageQuery(
        long page,
        long size,
        String sort,
        String keyword,
        Long categoryId,
        EnabledStatus status,
        ReviewStatus reviewStatus
) {
}
