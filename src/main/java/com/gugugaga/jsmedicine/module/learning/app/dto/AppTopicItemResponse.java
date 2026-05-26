package com.gugugaga.jsmedicine.module.learning.app.dto;

public record AppTopicItemResponse(
        Long id,
        Long topicId,
        String itemType,
        Long itemId,
        Integer sortOrder,
        Object resource
) {
}
