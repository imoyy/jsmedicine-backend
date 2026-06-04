package com.gugugaga.jsmedicine.module.content.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.common.service.ResourceTagService;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import com.gugugaga.jsmedicine.module.content.app.dto.AppArticleResponse;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppContentService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final long ZERO_COUNT = 0L;
    private static final String RESOURCE_TYPE_ARTICLE = "article";
    private static final String SORT_PUBLISHED_AT_ASC = "publishedAtAsc";

    private final CurrentAppUserResolver currentAppUserResolver;
    private final ArticleMapper articleMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final ResourceTagService resourceTagService;

    public AppContentService(
            CurrentAppUserResolver currentAppUserResolver,
            ArticleMapper articleMapper,
            UserFavoriteMapper userFavoriteMapper,
            UserBrowseHistoryMapper userBrowseHistoryMapper,
            ResourceTagService resourceTagService
    ) {
        this.currentAppUserResolver = currentAppUserResolver;
        this.articleMapper = articleMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
        this.resourceTagService = resourceTagService;
    }

    public PageResponse<AppArticleResponse> pageArticles(long page, long size, String sort, String keyword) {
        Page<Article> articlePage = articleMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                visibleArticleWrapper()
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(Article::getTitle, keyword)
                                .or()
                                .like(Article::getSource, keyword)
                                .or()
                                .like(Article::getSummary, keyword))
                        .orderByAsc(SORT_PUBLISHED_AT_ASC.equals(sort), Article::getPublishedAt)
                        .orderByDesc(!SORT_PUBLISHED_AT_ASC.equals(sort), Article::getPublishedAt)
                        .orderByDesc(Article::getCreatedAt));
        Long userId = currentUserId().orElse(null);
        Map<Long, ResourceInteractionSnapshot> snapshots = loadInteractionSnapshots(userId,
                articlePage.getRecords().stream().map(Article::getId).toList());
        return pageResponse(articlePage, articlePage.getRecords().stream()
                .map(article -> toResponse(article, snapshots.get(article.getId())))
                .toList());
    }

    public AppArticleResponse articleDetail(Long id) {
        Article article = requireVisibleArticle(id);
        return toResponse(article, loadInteractionSnapshot(currentUserId().orElse(null), article.getId()));
    }

    private Article requireVisibleArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || !isVisible(article.getDeleted(), article.getReviewStatus(), article.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Article does not exist");
        }
        return article;
    }

    private AppArticleResponse toResponse(Article article, ResourceInteractionSnapshot snapshot) {
        ResourceInteractionSnapshot resolvedSnapshot = snapshot == null ? ResourceInteractionSnapshot.empty() : snapshot;
        long articleViewCount = article.getViewCount() == null ? 0L : article.getViewCount();
        long resolvedViewCount = Math.max(articleViewCount, resolvedSnapshot.browseCount());
        return new AppArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSummary(),
                article.getCoverUrl(),
                article.getContent(),
                article.getAuthorName(),
                article.getSource(),
                resourceTagService.loadTagNames(RESOURCE_TYPE_ARTICLE, article.getId()),
                resolvedViewCount,
                resolvedSnapshot.favoriteCount(),
                resolvedSnapshot.favorited(),
                article.getPublishedAt()
        );
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

    private LambdaQueryWrapper<Article> visibleArticleWrapper() {
        return new LambdaQueryWrapper<Article>()
                .eq(Article::getDeleted, 0)
                .eq(Article::getReviewStatus, ReviewStatus.APPROVED)
                .eq(Article::getPublishStatus, PublishStatus.PUBLISHED);
    }

    private Optional<Long> currentUserId() {
        return currentAppUserResolver.currentSession().map(AppUserSession::userId);
    }

    private boolean isVisible(Integer deleted, ReviewStatus reviewStatus, PublishStatus publishStatus) {
        return Objects.equals(deleted, 0)
                && reviewStatus == ReviewStatus.APPROVED
                && publishStatus == PublishStatus.PUBLISHED;
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
