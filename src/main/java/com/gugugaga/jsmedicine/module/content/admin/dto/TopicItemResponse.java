package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;

public record TopicItemResponse(
        Long id,
        Long topicId,
        String itemType,
        String itemTypeLabel,
        Long itemId,
        Integer sortOrder,
        Boolean itemAvailable,
        String itemTitle,
        String itemSubtitle,
        String itemCoverUrl,
        ReviewStatus reviewStatus,
        PublishStatus publishStatus
) {
}
