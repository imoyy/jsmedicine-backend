package com.gugugaga.jsmedicine.module.content.admin.dto;

public record TopicItemResponse(
        Long id,
        Long topicId,
        String itemType,
        Long itemId,
        Integer sortOrder
) {
}
