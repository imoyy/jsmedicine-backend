package com.gugugaga.jsmedicine.module.content.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.common.service.ResourceTagService;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import com.gugugaga.jsmedicine.module.content.app.dto.AppArticleCardResponse;
import com.gugugaga.jsmedicine.module.content.app.dto.AppArticleDetailResponse;
import com.gugugaga.jsmedicine.module.content.article.entity.Article;
import com.gugugaga.jsmedicine.module.content.article.mapper.ArticleMapper;
import com.gugugaga.jsmedicine.module.interaction.favorite.entity.UserFavorite;
import com.gugugaga.jsmedicine.module.interaction.favorite.mapper.UserFavoriteMapper;
import com.gugugaga.jsmedicine.module.interaction.history.entity.UserBrowseHistory;
import com.gugugaga.jsmedicine.module.interaction.history.mapper.UserBrowseHistoryMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppContentService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final long ZERO_COUNT = 0L;
    private static final String RESOURCE_TYPE_ARTICLE = "article";

    private final ArticleMapper articleMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final CurrentAppUserResolver currentAppUserResolver;
    private final ResourceTagService resourceTagService;

    public AppContentService(
            ArticleMapper articleMapper,
            UserFavoriteMapper userFavoriteMapper,
            UserBrowseHistoryMapper userBrowseHistoryMapper,
            CurrentAppUserResolver currentAppUserResolver,
            ResourceTagService resourceTagService
    ) {
        this.articleMapper = articleMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
        this.currentAppUserResolver = currentAppUserResolver;
        this.resourceTagService = resourceTagService;
    }

    public PageResponse<AppArticleCardResponse> pageArticles(long page, long size, String keyword) {
        Long userId = currentAppUserResolver.requireCurrentUser().userId();
        Page<Article> articlePage = articleMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                visibleArticleWrapper()
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(Article::getTitle, keyword)
                                .or()
                                .like(Article::getSource, keyword)
                                .or()
                                .like(Article::getSummary, keyword))
                        .orderByDesc(Article::getPublishedAt)
                        .orderByDesc(Article::getCreatedAt));
        List<Long> articleIds = articlePage.getRecords().stream().map(Article::getId).toList();
        Map<Long, ResourceInteractionSnapshot> snapshots = loadInteractionSnapshots(userId, articleIds);
        return pageResponse(articlePage, articlePage.getRecords().stream()
                .map(article -> toArticleCardResponse(article, snapshotOf(snapshots, article.getId())))
                .toList());
    }

    public AppArticleDetailResponse articleDetail(Long id) {
        Long userId = currentAppUserResolver.requireCurrentUser().userId();
        Article article = requireVisibleArticle(id);
        return toArticleDetailResponse(article, loadInteractionSnapshot(userId, id));
    }

    private Article requireVisibleArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null
                || !Objects.equals(article.getDeleted(), 0)
                || article.getReviewStatus() != ReviewStatus.APPROVED
                || article.getPublishStatus() != PublishStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Article does not exist");
        }
        return article;
    }

    private LambdaQueryWrapper<Article> visibleArticleWrapper() {
        return new LambdaQueryWrapper<Article>()
                .eq(Article::getDeleted, 0)
                .eq(Article::getReviewStatus, ReviewStatus.APPROVED)
                .eq(Article::getPublishStatus, PublishStatus.PUBLISHED);
    }

    private AppArticleCardResponse toArticleCardResponse(Article article, ResourceInteractionSnapshot snapshot) {
        ResourceInteractionSnapshot resolvedSnapshot = snapshot == null ? ResourceInteractionSnapshot.empty() : snapshot;
        return new AppArticleCardResponse(article.getId(), article.getTitle(), article.getSummary(), article.getCoverUrl(),
                article.getAuthorName(), article.getSource(),
                resourceTagService.loadTagNames(RESOURCE_TYPE_ARTICLE, article.getId()),
                resolvedSnapshot.viewCount(), resolvedSnapshot.favoriteCount(), resolvedSnapshot.favorited(),
                article.getPublishedAt());
    }

    private AppArticleDetailResponse toArticleDetailResponse(Article article, ResourceInteractionSnapshot snapshot) {
        ResourceInteractionSnapshot resolvedSnapshot = snapshot == null ? ResourceInteractionSnapshot.empty() : snapshot;
        return new AppArticleDetailResponse(article.getId(), article.getTitle(), article.getSummary(), article.getCoverUrl(),
                article.getContent(), article.getAuthorName(), article.getSource(),
                resourceTagService.loadTagNames(RESOURCE_TYPE_ARTICLE, article.getId()),
                resolvedSnapshot.viewCount(), resolvedSnapshot.favoriteCount(), resolvedSnapshot.favorited(),
                article.getPublishedAt());
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
        return loadInteractionSnapshots(userId, List.of(resourceId)).getOrDefault(resourceId, ResourceInteractionSnapshot.empty());
    }

    private Map<Long, Long> loadBrowseCounts(List<Long> resourceIds) {
        return userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                        .eq(UserBrowseHistory::getResourceType, RESOURCE_TYPE_ARTICLE)
                        .in(UserBrowseHistory::getResourceId, resourceIds))
                .stream()
                .collect(Collectors.groupingBy(UserBrowseHistory::getResourceId,
                        Collectors.summingLong(history -> history.getViewCount() == null ? 0 : history.getViewCount())));
    }

    private Map<Long, Long> loadFavoriteCounts(List<Long> resourceIds) {
        return userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getResourceType, RESOURCE_TYPE_ARTICLE)
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
                        .eq(UserFavorite::getResourceType, RESOURCE_TYPE_ARTICLE)
                        .in(UserFavorite::getResourceId, resourceIds))
                .stream()
                .map(UserFavorite::getResourceId)
                .collect(Collectors.toSet());
    }

    private ResourceInteractionSnapshot snapshotOf(Map<Long, ResourceInteractionSnapshot> snapshots, Long resourceId) {
        return snapshots.getOrDefault(resourceId, ResourceInteractionSnapshot.empty());
    }

    private <E, R> PageResponse<R> pageResponse(Page<E> page, List<R> records) {
        return new PageResponse<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private long normalizePage(long page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(long size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ResourceInteractionSnapshot(Long viewCount, Long favoriteCount, Boolean favorited) {

        private static ResourceInteractionSnapshot empty() {
            return new ResourceInteractionSnapshot(ZERO_COUNT, ZERO_COUNT, false);
        }
    }
}
