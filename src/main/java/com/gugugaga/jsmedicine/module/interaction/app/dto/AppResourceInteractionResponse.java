package com.gugugaga.jsmedicine.module.interaction.app.dto;

public record AppResourceInteractionResponse(
        String resourceType,
        Long resourceId,
        Long browseCount,
        Long favoriteCount,
        Boolean favorited
) {
}
