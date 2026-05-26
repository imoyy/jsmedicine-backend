package com.gugugaga.jsmedicine.module.statistics.dto;

public record ContentInteractionStatisticsResponse(
        String resourceType,
        Long resourceId,
        Long browseCount,
        Long favoriteCount,
        Long shareCount,
        Long uniqueBrowseUsers,
        Long uniqueFavoriteUsers,
        Long uniqueShareUsers
) {
}
