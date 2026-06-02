package com.gugugaga.jsmedicine.module.learning.app.dto;

import java.util.List;

public record AppTopicSectionResponse(
        String sectionType,
        String sectionLabel,
        Long total,
        Boolean hasMore,
        List<AppTopicResourceCardResponse> items
) {
}
