package com.gugugaga.jsmedicine.module.learning.live.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.common.service.ResourceTagService;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import com.gugugaga.jsmedicine.module.interaction.favorite.entity.UserFavorite;
import com.gugugaga.jsmedicine.module.interaction.favorite.mapper.UserFavoriteMapper;
import com.gugugaga.jsmedicine.module.interaction.history.entity.UserBrowseHistory;
import com.gugugaga.jsmedicine.module.interaction.history.mapper.UserBrowseHistoryMapper;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionVideoResponse;
import com.gugugaga.jsmedicine.module.learning.live.app.dto.AppLiveSessionResponse;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSession;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSessionVideo;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionMapper;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionVideoMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppLiveService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final long ZERO_COUNT = 0L;
    private static final String RESOURCE_TYPE_LIVE = "live";

    private final CurrentAppUserResolver currentAppUserResolver;
    private final LiveSessionMapper liveSessionMapper;
    private final LiveSessionVideoMapper liveSessionVideoMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final ResourceTagService resourceTagService;

    public AppLiveService(
            CurrentAppUserResolver currentAppUserResolver,
            LiveSessionMapper liveSessionMapper,
            LiveSessionVideoMapper liveSessionVideoMapper,
            UserFavoriteMapper userFavoriteMapper,
            UserBrowseHistoryMapper userBrowseHistoryMapper,
            ResourceTagService resourceTagService
    ) {
        this.currentAppUserResolver = currentAppUserResolver;
        this.liveSessionMapper = liveSessionMapper;
        this.liveSessionVideoMapper = liveSessionVideoMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
        this.resourceTagService = resourceTagService;
    }

    public PageResponse<AppLiveSessionResponse> pageLives(long page, long size, String keyword, LiveStatus liveStatus) {
        Page<LiveSession> livePage = liveSessionMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                visibleWrapper()
                        .eq(liveStatus != null, LiveSession::getLiveStatus, liveStatus)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(LiveSession::getTitle, keyword)
                                .or()
                                .like(LiveSession::getAnchorName, keyword)
                                .or()
                                .like(LiveSession::getSpeakerName, keyword))
                        .orderByDesc(LiveSession::getStartAt));
        Long userId = currentUserId().orElse(null);
        Map<Long, ResourceInteractionSnapshot> snapshots = loadInteractionSnapshots(userId,
                livePage.getRecords().stream().map(LiveSession::getId).toList());
        return pageResponse(livePage, livePage.getRecords().stream()
                .map(live -> toResponse(live, false, snapshots.get(live.getId())))
                .toList());
    }

    public AppLiveSessionResponse liveDetail(Long id) {
        LiveSession live = liveSessionMapper.selectById(id);
        if (live == null
                || !Objects.equals(live.getDeleted(), 0)
                || live.getReviewStatus() != ReviewStatus.APPROVED
                || live.getLiveStatus() == LiveStatus.CANCELED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Live session does not exist");
        }
        return toResponse(live, true, loadInteractionSnapshot(currentUserId().orElse(null), id));
    }

    private LambdaQueryWrapper<LiveSession> visibleWrapper() {
        return new LambdaQueryWrapper<LiveSession>()
                .eq(LiveSession::getDeleted, 0)
                .eq(LiveSession::getReviewStatus, ReviewStatus.APPROVED)
                .ne(LiveSession::getLiveStatus, LiveStatus.CANCELED);
    }

    private AppLiveSessionResponse toResponse(
            LiveSession live,
            boolean includeVideos,
            ResourceInteractionSnapshot snapshot
    ) {
        ResourceInteractionSnapshot resolvedSnapshot = snapshot == null ? ResourceInteractionSnapshot.empty() : snapshot;
        return new AppLiveSessionResponse(live.getId(), live.getTitle(), live.getCoverUrl(), live.getAnchorName(),
                resolvedSpeakerName(live), resourceTagService.loadTagNames(RESOURCE_TYPE_LIVE, live.getId()),
                live.getLiveUrl(), live.getPlaybackUrl(), live.getStartAt(), live.getEndAt(),
                live.getReviewStatus(), live.getLiveStatus(), resolvedSnapshot.browseCount(),
                resolvedSnapshot.favoriteCount(), resolvedSnapshot.favorited(),
                includeVideos ? loadLiveVideos(live.getId()) : List.of());
    }

    private List<LiveSessionVideoResponse> loadLiveVideos(Long liveSessionId) {
        return liveSessionVideoMapper.selectList(new LambdaQueryWrapper<LiveSessionVideo>()
                        .eq(LiveSessionVideo::getDeleted, 0)
                        .eq(LiveSessionVideo::getLiveSessionId, liveSessionId)
                        .eq(LiveSessionVideo::getStatus, EnabledStatus.ENABLED)
                        .orderByAsc(LiveSessionVideo::getSortOrder)
                        .orderByDesc(LiveSessionVideo::getCreatedAt))
                .stream()
                .map(video -> new LiveSessionVideoResponse(video.getId(), video.getLiveSessionId(), video.getTitle(),
                        video.getVideoUrl(), video.getDurationSeconds(), video.getSortOrder(), video.getStatus()))
                .toList();
    }

    private String resolvedSpeakerName(LiveSession live) {
        return hasText(live.getSpeakerName()) ? live.getSpeakerName() : live.getAnchorName();
    }

    private Map<Long, ResourceInteractionSnapshot> loadInteractionSnapshots(Long userId, List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> browseCounts = loadBrowseCounts(resourceIds);
        Map<Long, Long> favoriteCounts = loadFavoriteCounts(resourceIds);
        Set<Long> favoritedIds = loadFavoritedIds(userId, resourceIds);
        Map<Long, ResourceInteractionSnapshot> snapshots = new HashMap<>();
        for (Long resourceId : resourceIds) {
            snapshots.put(resourceId, new ResourceInteractionSnapshot(
                    browseCounts.getOrDefault(resourceId, ZERO_COUNT),
                    favoriteCounts.getOrDefault(resourceId, ZERO_COUNT),
                    favoritedIds.contains(resourceId)
            ));
        }
        return snapshots;
    }

    private ResourceInteractionSnapshot loadInteractionSnapshot(Long userId, Long resourceId) {
        return loadInteractionSnapshots(userId, List.of(resourceId))
                .getOrDefault(resourceId, ResourceInteractionSnapshot.empty());
    }

    private Map<Long, Long> loadBrowseCounts(List<Long> resourceIds) {
        return userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                        .eq(UserBrowseHistory::getResourceType, RESOURCE_TYPE_LIVE)
                        .in(UserBrowseHistory::getResourceId, resourceIds))
                .stream()
                .collect(Collectors.groupingBy(UserBrowseHistory::getResourceId,
                        Collectors.summingLong(history -> history.getViewCount() == null ? 0 : history.getViewCount())));
    }

    private Map<Long, Long> loadFavoriteCounts(List<Long> resourceIds) {
        return userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getResourceType, RESOURCE_TYPE_LIVE)
                        .in(UserFavorite::getResourceId, resourceIds))
                .stream()
                .collect(Collectors.groupingBy(UserFavorite::getResourceId, Collectors.counting()));
    }

    private Set<Long> loadFavoritedIds(Long userId, List<Long> resourceIds) {
        if (userId == null) {
            return Set.of();
        }
        return userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getResourceType, RESOURCE_TYPE_LIVE)
                        .in(UserFavorite::getResourceId, resourceIds))
                .stream()
                .map(UserFavorite::getResourceId)
                .collect(Collectors.toSet());
    }

    private Optional<Long> currentUserId() {
        return currentAppUserResolver.currentSession().map(AppUserSession::userId);
    }

    private <E, R> PageResponse<R> pageResponse(Page<E> page, List<R> records) {
        return new PageResponse<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private long normalizePage(long page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(long size) {
        return size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ResourceInteractionSnapshot(Long browseCount, Long favoriteCount, Boolean favorited) {

        private static ResourceInteractionSnapshot empty() {
            return new ResourceInteractionSnapshot(ZERO_COUNT, ZERO_COUNT, false);
        }
    }
}
