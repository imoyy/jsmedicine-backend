package com.gugugaga.jsmedicine.module.content.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.infrastructure.storage.entity.FileAsset;
import com.gugugaga.jsmedicine.infrastructure.storage.mapper.FileAssetMapper;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminContentPageQuery;
import com.gugugaga.jsmedicine.module.content.admin.dto.ArticleRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.ArticleResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.FileAssetRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.FileAssetResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.HomeCategoryRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.HomeCategoryResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.HomeContentRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.HomeContentResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.PodcastAudioRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.PodcastAudioResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.PodcastRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.PodcastResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.ReviewRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.TopicItemRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.TopicItemResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.TopicRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.TopicResponse;
import com.gugugaga.jsmedicine.module.content.article.entity.Article;
import com.gugugaga.jsmedicine.module.content.article.mapper.ArticleMapper;
import com.gugugaga.jsmedicine.module.content.home.entity.HomeCategory;
import com.gugugaga.jsmedicine.module.content.home.entity.HomeContent;
import com.gugugaga.jsmedicine.module.content.home.mapper.HomeCategoryMapper;
import com.gugugaga.jsmedicine.module.content.home.mapper.HomeContentMapper;
import com.gugugaga.jsmedicine.module.content.topic.entity.Topic;
import com.gugugaga.jsmedicine.module.content.topic.entity.TopicItem;
import com.gugugaga.jsmedicine.module.content.topic.mapper.TopicItemMapper;
import com.gugugaga.jsmedicine.module.content.topic.mapper.TopicMapper;
import com.gugugaga.jsmedicine.module.learning.podcast.entity.Podcast;
import com.gugugaga.jsmedicine.module.learning.podcast.entity.PodcastAudio;
import com.gugugaga.jsmedicine.module.learning.podcast.mapper.PodcastAudioMapper;
import com.gugugaga.jsmedicine.module.learning.podcast.mapper.PodcastMapper;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.service.AuditRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AdminContentService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final HomeCategoryMapper homeCategoryMapper;
    private final HomeContentMapper homeContentMapper;
    private final ArticleMapper articleMapper;
    private final PodcastMapper podcastMapper;
    private final PodcastAudioMapper podcastAudioMapper;
    private final TopicMapper topicMapper;
    private final TopicItemMapper topicItemMapper;
    private final FileAssetMapper fileAssetMapper;
    private final AuditRecordService auditRecordService;
    private final CurrentAdminAccessor currentAdminAccessor;

    public AdminContentService(
            HomeCategoryMapper homeCategoryMapper,
            HomeContentMapper homeContentMapper,
            ArticleMapper articleMapper,
            PodcastMapper podcastMapper,
            PodcastAudioMapper podcastAudioMapper,
            TopicMapper topicMapper,
            TopicItemMapper topicItemMapper,
            FileAssetMapper fileAssetMapper,
            AuditRecordService auditRecordService,
            CurrentAdminAccessor currentAdminAccessor
    ) {
        this.homeCategoryMapper = homeCategoryMapper;
        this.homeContentMapper = homeContentMapper;
        this.articleMapper = articleMapper;
        this.podcastMapper = podcastMapper;
        this.podcastAudioMapper = podcastAudioMapper;
        this.topicMapper = topicMapper;
        this.topicItemMapper = topicItemMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.auditRecordService = auditRecordService;
        this.currentAdminAccessor = currentAdminAccessor;
    }

    public PageResponse<HomeCategoryResponse> pageHomeCategories(AdminContentPageQuery query) {
        Page<HomeCategory> page = homeCategoryMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<HomeCategory>()
                        .eq(HomeCategory::getDeleted, 0)
                        .eq(query.status() != null, HomeCategory::getStatus, query.status())
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(HomeCategory::getCategoryName, query.keyword())
                                .or()
                                .like(HomeCategory::getCategoryCode, query.keyword()))
                        .orderByAsc(HomeCategory::getSortOrder)
                        .orderByDesc(HomeCategory::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(this::toHomeCategoryResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public HomeCategoryResponse createHomeCategory(HomeCategoryRequest request) {
        HomeCategory category = new HomeCategory();
        fillHomeCategory(category, request);
        category.setDeleted(0);
        homeCategoryMapper.insert(category);
        return toHomeCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public HomeCategoryResponse updateHomeCategory(Long id, HomeCategoryRequest request) {
        HomeCategory category = requireHomeCategory(id);
        fillHomeCategory(category, request);
        homeCategoryMapper.updateById(category);
        return toHomeCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteHomeCategory(Long id) {
        requireHomeCategory(id);
        homeCategoryMapper.deleteById(id);
    }

    public PageResponse<HomeContentResponse> pageHomeContents(AdminContentPageQuery query) {
        Page<HomeContent> page = homeContentMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<HomeContent>()
                        .eq(HomeContent::getDeleted, 0)
                        .eq(query.status() != null, HomeContent::getStatus, query.status())
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(HomeContent::getTitle, query.keyword()))
                        .orderByAsc(HomeContent::getSortOrder)
                        .orderByDesc(HomeContent::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(this::toHomeContentResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public HomeContentResponse createHomeContent(HomeContentRequest request) {
        requireHomeCategory(request.categoryId());
        HomeContent content = new HomeContent();
        fillHomeContent(content, request);
        content.setDeleted(0);
        homeContentMapper.insert(content);
        return toHomeContentResponse(content);
    }

    @Transactional(rollbackFor = Exception.class)
    public HomeContentResponse updateHomeContent(Long id, HomeContentRequest request) {
        requireHomeContent(id);
        requireHomeCategory(request.categoryId());
        HomeContent content = homeContentMapper.selectById(id);
        fillHomeContent(content, request);
        homeContentMapper.updateById(content);
        return toHomeContentResponse(content);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteHomeContent(Long id) {
        requireHomeContent(id);
        homeContentMapper.deleteById(id);
    }

    public PageResponse<ArticleResponse> pageArticles(AdminContentPageQuery query) {
        Page<Article> page = articleMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getDeleted, 0)
                        .eq(query.reviewStatus() != null, Article::getReviewStatus, query.reviewStatus())
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(Article::getTitle, query.keyword()))
                        .orderByDesc(Article::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(this::toArticleResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse createArticle(ArticleRequest request) {
        Article article = new Article();
        fillArticle(article, request);
        article.setViewCount(0L);
        article.setDeleted(0);
        articleMapper.insert(article);
        return toArticleResponse(article);
    }

    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse updateArticle(Long id, ArticleRequest request) {
        Article article = requireArticle(id);
        fillArticle(article, request);
        articleMapper.updateById(article);
        return toArticleResponse(article);
    }

    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse reviewArticle(Long id, ReviewRequest request) {
        Article article = requireArticle(id);
        ReviewStatus before = article.getReviewStatus();
        article.setReviewStatus(request.reviewStatus());
        if (request.reviewStatus() == ReviewStatus.APPROVED) {
            article.setPublishStatus(PublishStatus.PUBLISHED);
            article.setPublishedAt(LocalDateTime.now());
        }
        if (request.reviewStatus() == ReviewStatus.REJECTED) {
            article.setPublishStatus(PublishStatus.UNPUBLISHED);
        }
        articleMapper.updateById(article);
        saveAudit("article", id, before, request.reviewStatus(), request.comment());
        return toArticleResponse(article);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        requireArticle(id);
        articleMapper.deleteById(id);
    }

    public PageResponse<PodcastResponse> pagePodcasts(AdminContentPageQuery query) {
        Page<Podcast> page = podcastMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<Podcast>()
                        .eq(Podcast::getDeleted, 0)
                        .eq(query.reviewStatus() != null, Podcast::getReviewStatus, query.reviewStatus())
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(Podcast::getTitle, query.keyword()))
                        .orderByAsc(Podcast::getSortOrder)
                        .orderByDesc(Podcast::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(this::toPodcastResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public PodcastResponse createPodcast(PodcastRequest request) {
        Podcast podcast = new Podcast();
        fillPodcast(podcast, request);
        podcast.setDeleted(0);
        podcastMapper.insert(podcast);
        return toPodcastResponse(podcast);
    }

    @Transactional(rollbackFor = Exception.class)
    public PodcastResponse updatePodcast(Long id, PodcastRequest request) {
        Podcast podcast = requirePodcast(id);
        fillPodcast(podcast, request);
        podcastMapper.updateById(podcast);
        return toPodcastResponse(podcast);
    }

    @Transactional(rollbackFor = Exception.class)
    public PodcastResponse reviewPodcast(Long id, ReviewRequest request) {
        Podcast podcast = requirePodcast(id);
        ReviewStatus before = podcast.getReviewStatus();
        podcast.setReviewStatus(request.reviewStatus());
        if (request.reviewStatus() == ReviewStatus.APPROVED) {
            podcast.setPublishStatus(PublishStatus.PUBLISHED);
            podcast.setPublishedAt(LocalDateTime.now());
        }
        if (request.reviewStatus() == ReviewStatus.REJECTED) {
            podcast.setPublishStatus(PublishStatus.UNPUBLISHED);
        }
        podcastMapper.updateById(podcast);
        saveAudit("podcast", id, before, request.reviewStatus(), request.comment());
        return toPodcastResponse(podcast);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePodcast(Long id) {
        requirePodcast(id);
        podcastMapper.deleteById(id);
    }

    public PageResponse<PodcastAudioResponse> pagePodcastAudios(Long podcastId, long page, long size) {
        Page<PodcastAudio> audioPage = podcastAudioMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<PodcastAudio>()
                        .eq(PodcastAudio::getDeleted, 0)
                        .eq(podcastId != null, PodcastAudio::getPodcastId, podcastId)
                        .orderByAsc(PodcastAudio::getSortOrder)
                        .orderByDesc(PodcastAudio::getCreatedAt));
        return pageResponse(audioPage, audioPage.getRecords().stream().map(this::toPodcastAudioResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public PodcastAudioResponse createPodcastAudio(PodcastAudioRequest request) {
        requirePodcast(request.podcastId());
        PodcastAudio audio = new PodcastAudio();
        fillPodcastAudio(audio, request);
        audio.setDeleted(0);
        podcastAudioMapper.insert(audio);
        return toPodcastAudioResponse(audio);
    }

    @Transactional(rollbackFor = Exception.class)
    public PodcastAudioResponse updatePodcastAudio(Long id, PodcastAudioRequest request) {
        requirePodcastAudio(id);
        requirePodcast(request.podcastId());
        PodcastAudio audio = podcastAudioMapper.selectById(id);
        fillPodcastAudio(audio, request);
        podcastAudioMapper.updateById(audio);
        return toPodcastAudioResponse(audio);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePodcastAudio(Long id) {
        requirePodcastAudio(id);
        podcastAudioMapper.deleteById(id);
    }

    public PageResponse<TopicResponse> pageTopics(AdminContentPageQuery query) {
        Page<Topic> page = topicMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<Topic>()
                        .eq(Topic::getDeleted, 0)
                        .eq(query.reviewStatus() != null, Topic::getReviewStatus, query.reviewStatus())
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(Topic::getTitle, query.keyword()))
                        .orderByAsc(Topic::getSortOrder)
                        .orderByDesc(Topic::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(topic -> toTopicResponse(topic, false)).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public TopicResponse createTopic(TopicRequest request) {
        Topic topic = new Topic();
        fillTopic(topic, request);
        topic.setViewCount(0L);
        topic.setDeleted(0);
        topicMapper.insert(topic);
        return toTopicResponse(topic, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public TopicResponse updateTopic(Long id, TopicRequest request) {
        Topic topic = requireTopic(id);
        fillTopic(topic, request);
        topicMapper.updateById(topic);
        return toTopicResponse(topic, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public TopicResponse reviewTopic(Long id, ReviewRequest request) {
        Topic topic = requireTopic(id);
        ReviewStatus before = topic.getReviewStatus();
        topic.setReviewStatus(request.reviewStatus());
        if (request.reviewStatus() == ReviewStatus.APPROVED) {
            topic.setPublishStatus(PublishStatus.PUBLISHED);
            topic.setPublishedAt(LocalDateTime.now());
        }
        if (request.reviewStatus() == ReviewStatus.REJECTED) {
            topic.setPublishStatus(PublishStatus.UNPUBLISHED);
        }
        topicMapper.updateById(topic);
        saveAudit("topic", id, before, request.reviewStatus(), request.comment());
        return toTopicResponse(topic, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTopic(Long id) {
        requireTopic(id);
        topicMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TopicItemResponse> replaceTopicItems(Long topicId, List<TopicItemRequest> requests) {
        requireTopic(topicId);
        topicItemMapper.delete(new LambdaQueryWrapper<TopicItem>().eq(TopicItem::getTopicId, topicId));
        if (requests != null) {
            requests.forEach(request -> {
                TopicItem item = new TopicItem();
                item.setTopicId(topicId);
                item.setItemType(request.itemType());
                item.setItemId(request.itemId());
                item.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
                topicItemMapper.insert(item);
            });
        }
        return loadTopicItems(topicId);
    }

    public PageResponse<FileAssetResponse> pageFileAssets(AdminContentPageQuery query) {
        Page<FileAsset> page = fileAssetMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<FileAsset>()
                        .eq(FileAsset::getDeleted, 0)
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(FileAsset::getOriginalName, query.keyword())
                                .or()
                                .like(FileAsset::getObjectKey, query.keyword()))
                        .orderByDesc(FileAsset::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(this::toFileAssetResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public FileAssetResponse createFileAsset(FileAssetRequest request) {
        FileAsset fileAsset = new FileAsset();
        fillFileAsset(fileAsset, request);
        fileAsset.setStorageProvider(hasText(request.storageProvider()) ? request.storageProvider() : "minio");
        fileAsset.setCreatedBy(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        fileAsset.setDeleted(0);
        fileAssetMapper.insert(fileAsset);
        return toFileAssetResponse(fileAsset);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteFileAsset(Long id) {
        requireFileAsset(id);
        fileAssetMapper.deleteById(id);
    }

    private void fillHomeCategory(HomeCategory category, HomeCategoryRequest request) {
        category.setParentId(request.parentId());
        category.setCategoryName(request.categoryName());
        category.setCategoryCode(request.categoryCode());
        category.setIconUrl(request.iconUrl());
        category.setDescription(request.description());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(request.status());
    }

    private void fillHomeContent(HomeContent content, HomeContentRequest request) {
        content.setCategoryId(request.categoryId());
        content.setContentType(request.contentType());
        content.setTargetId(request.targetId());
        content.setTitle(request.title());
        content.setCoverUrl(request.coverUrl());
        content.setLinkUrl(request.linkUrl());
        content.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        content.setStartAt(request.startAt());
        content.setEndAt(request.endAt());
        content.setStatus(request.status());
    }

    private void fillArticle(Article article, ArticleRequest request) {
        article.setTitle(request.title());
        article.setSummary(request.summary());
        article.setCoverUrl(request.coverUrl());
        article.setContent(request.content());
        article.setAuthorName(request.authorName());
        article.setReviewStatus(request.reviewStatus());
        article.setPublishStatus(request.publishStatus());
        article.setPublishedAt(request.publishedAt());
    }

    private void fillPodcast(Podcast podcast, PodcastRequest request) {
        podcast.setTitle(request.title());
        podcast.setSummary(request.summary());
        podcast.setCoverUrl(request.coverUrl());
        podcast.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        podcast.setReviewStatus(request.reviewStatus());
        podcast.setPublishStatus(request.publishStatus());
        podcast.setPublishedAt(request.publishedAt());
    }

    private void fillPodcastAudio(PodcastAudio audio, PodcastAudioRequest request) {
        audio.setPodcastId(request.podcastId());
        audio.setTitle(request.title());
        audio.setAudioUrl(request.audioUrl());
        audio.setDurationSeconds(request.durationSeconds());
        audio.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        audio.setStatus(request.status());
    }

    private void fillTopic(Topic topic, TopicRequest request) {
        topic.setTitle(request.title());
        topic.setSummary(request.summary());
        topic.setLearningRequirements(request.learningRequirements());
        topic.setCoverUrl(request.coverUrl());
        topic.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        topic.setReviewStatus(request.reviewStatus());
        topic.setPublishStatus(request.publishStatus());
        topic.setPublishedAt(request.publishedAt());
    }

    private void fillFileAsset(FileAsset fileAsset, FileAssetRequest request) {
        fileAsset.setAssetType(request.assetType());
        fileAsset.setStorageProvider(request.storageProvider());
        fileAsset.setBucketName(request.bucketName());
        fileAsset.setObjectKey(request.objectKey());
        fileAsset.setOriginalName(request.originalName());
        fileAsset.setContentType(request.contentType());
        fileAsset.setFileSize(request.fileSize());
        fileAsset.setUrl(request.url());
    }

    private HomeCategory requireHomeCategory(Long id) {
        HomeCategory category = homeCategoryMapper.selectById(id);
        if (category == null || !Objects.equals(category.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Home category does not exist");
        }
        return category;
    }

    private HomeContent requireHomeContent(Long id) {
        HomeContent content = homeContentMapper.selectById(id);
        if (content == null || !Objects.equals(content.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Home content does not exist");
        }
        return content;
    }

    private Article requireArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null || !Objects.equals(article.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Article does not exist");
        }
        return article;
    }

    private Podcast requirePodcast(Long id) {
        Podcast podcast = podcastMapper.selectById(id);
        if (podcast == null || !Objects.equals(podcast.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Podcast does not exist");
        }
        return podcast;
    }

    private PodcastAudio requirePodcastAudio(Long id) {
        PodcastAudio audio = podcastAudioMapper.selectById(id);
        if (audio == null || !Objects.equals(audio.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Podcast audio does not exist");
        }
        return audio;
    }

    private Topic requireTopic(Long id) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null || !Objects.equals(topic.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Topic does not exist");
        }
        return topic;
    }

    private FileAsset requireFileAsset(Long id) {
        FileAsset fileAsset = fileAssetMapper.selectById(id);
        if (fileAsset == null || !Objects.equals(fileAsset.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "File asset does not exist");
        }
        return fileAsset;
    }

    private void saveAudit(String targetType, Long targetId, ReviewStatus before, ReviewStatus after, String comment) {
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setTargetType(targetType);
        auditRecord.setTargetId(targetId);
        auditRecord.setBeforeStatus(before == null ? null : before.getValue());
        auditRecord.setAfterStatus(after.getValue());
        auditRecord.setAuditComment(comment);
        auditRecord.setAuditorId(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        auditRecord.setAuditedAt(LocalDateTime.now());
        auditRecordService.save(auditRecord);
    }

    private HomeCategoryResponse toHomeCategoryResponse(HomeCategory category) {
        return new HomeCategoryResponse(category.getId(), category.getParentId(), category.getCategoryName(),
                category.getCategoryCode(), category.getIconUrl(), category.getDescription(), category.getSortOrder(),
                category.getStatus());
    }

    private HomeContentResponse toHomeContentResponse(HomeContent content) {
        return new HomeContentResponse(content.getId(), content.getCategoryId(), content.getContentType(),
                content.getTargetId(), content.getTitle(), content.getCoverUrl(), content.getLinkUrl(),
                content.getSortOrder(), content.getStartAt(), content.getEndAt(), content.getStatus());
    }

    private ArticleResponse toArticleResponse(Article article) {
        return new ArticleResponse(article.getId(), article.getTitle(), article.getSummary(), article.getCoverUrl(),
                article.getContent(), article.getAuthorName(), article.getViewCount(), article.getReviewStatus(),
                article.getPublishStatus(), article.getPublishedAt());
    }

    private PodcastResponse toPodcastResponse(Podcast podcast) {
        return new PodcastResponse(podcast.getId(), podcast.getTitle(), podcast.getSummary(), podcast.getCoverUrl(),
                podcast.getSortOrder(), podcast.getReviewStatus(), podcast.getPublishStatus(), podcast.getPublishedAt());
    }

    private PodcastAudioResponse toPodcastAudioResponse(PodcastAudio audio) {
        return new PodcastAudioResponse(audio.getId(), audio.getPodcastId(), audio.getTitle(), audio.getAudioUrl(),
                audio.getDurationSeconds(), audio.getSortOrder(), audio.getStatus());
    }

    private TopicResponse toTopicResponse(Topic topic, boolean includeItems) {
        return new TopicResponse(topic.getId(), topic.getTitle(), topic.getSummary(), topic.getLearningRequirements(),
                topic.getCoverUrl(), topic.getSortOrder(), topic.getViewCount(), topic.getReviewStatus(),
                topic.getPublishStatus(), topic.getPublishedAt(), includeItems ? loadTopicItems(topic.getId()) : List.of());
    }

    private List<TopicItemResponse> loadTopicItems(Long topicId) {
        return topicItemMapper.selectList(new LambdaQueryWrapper<TopicItem>()
                        .eq(TopicItem::getTopicId, topicId)
                        .orderByAsc(TopicItem::getSortOrder))
                .stream()
                .map(item -> new TopicItemResponse(item.getId(), item.getTopicId(), item.getItemType(), item.getItemId(), item.getSortOrder()))
                .toList();
    }

    private FileAssetResponse toFileAssetResponse(FileAsset fileAsset) {
        return new FileAssetResponse(fileAsset.getId(), fileAsset.getAssetType(), fileAsset.getStorageProvider(),
                fileAsset.getBucketName(), fileAsset.getObjectKey(), fileAsset.getOriginalName(),
                fileAsset.getContentType(), fileAsset.getFileSize(), fileAsset.getUrl(), fileAsset.getCreatedBy());
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
}
