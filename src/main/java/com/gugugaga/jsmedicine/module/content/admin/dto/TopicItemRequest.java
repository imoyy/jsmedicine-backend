package com.gugugaga.jsmedicine.module.content.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TopicItemRequest(
        @NotBlank(message = "itemType must not be blank")
        @Size(max = 32, message = "itemType length must be less than 32")
        String itemType,

        @NotNull(message = "itemId must not be null")
        Long itemId,

        Integer sortOrder
) {
}
