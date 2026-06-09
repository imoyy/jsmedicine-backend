package com.gugugaga.jsmedicine.module.content.app.dto;

import java.util.List;

public record AppHomeSectionResponse(
        Long id,
        String categoryName,
        String categoryCode,
        String iconUrl,
        String description,
        Integer sortOrder,
        List<AppHomeItemResponse> items
) {
}
