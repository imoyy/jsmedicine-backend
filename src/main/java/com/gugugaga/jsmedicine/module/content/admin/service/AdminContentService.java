package com.gugugaga.jsmedicine.module.content.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.common.service.ResourceTagService;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.infrastructure.storage.service.StableCoverUrlService;
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
import com.gugugaga.jsmedicine.module.learning.book.entity.Book;
import com.gugugaga.jsmedicine.module.learning.book.mapper.BookMapper;
import com.gugugaga.jsmedicine.module.learning.course.entity.Course;
import com.gugugaga.jsmedicine.module.learning.course.mapper.CourseMapper;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSession;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionMapper;
import com.gugugaga.jsmedicine.module.learning.podcast.entity.Podcast;
import com.gugugaga.jsmedicine.module.learning.podcast.entity.PodcastAudio;
import com.gugugaga.jsmedicine.module.learning.podcast.mapper.PodcastAudioMapper;
import com.gugugaga.jsmedicine.module.learning.podcast.mapper.PodcastMapper;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.service.AuditRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AdminContentService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final String RESOURCE_TYPE_COURSE = "course";
    private static final String RESOURCE_TYPE_BOOK = "book";
    private static final String RESOURCE_TYPE_ARTICLE = "article";
    private static final String RESOURCE_TYPE_PODCAST = "podcast";
    private static final String RESOURCE_TYPE_TOPIC = "topic";
    private static final String RESOURCE_TYPE_LIVE = "live";
    private static final Map<String, String> TOPIC_ITEM_TYPE_LABELS = Map.of(
            RESOURCE_TYPE_COURSE, "课程",
            RESOURCE_TYPE_BOOK, "图书",
            RESOURCE_TYPE_PODCAST, "播客"
    );
    private static final Map<String, String> HOME_CONTENT_TYPE_LABELS = Map.of(
            RESOURCE_TYPE_COURSE, "课程",
            RESOURCE_TYPE_BOOK, "图书",
            RESOURCE_TYPE_PODCAST, "播客",
            RESOURCE_TYPE_TOPIC, "专题",
            RESOURCE_TYPE_LIVE, "直播"
    );

    private final HomeCategoryMapper homeCategoryMapper;
    private final HomeContentMapper homeContentMapper;
    private final ArticleMapper articleMapper;
    private final CourseMapper courseMapper;
    private final BookMapper bookMapper;
    private final PodcastMapper podcastMapper;
    private final LiveSessionMapper liveSessionMapper;
    private final PodcastAudioMapper podcastAudioMapper;
    private final TopicMapper topicMapper;
    private final TopicItemMapper topicItemMapper;
    private final FileAssetMapper fileAssetMapper;
    private final AuditRecordService auditRecordService;
    private final CurrentAdminAccessor currentAdminAccessor;
    private final ResourceTagService resourceTagService;
    private final StableCoverUrlService stableCoverUrlService;

    public AdminContentService(
            HomeCategoryMapper homeCategoryMapper,
            HomeContentMapper homeContentMapper,
            ArticleMapper articleMapper,
            CourseMapper courseMapper,
            BookMapper bookMapper,
            PodcastMapper podcastMapper,
            LiveSessionMapper liveSessionMapper,
            PodcastAudioMapper podcastAudioMapper,
            TopicMapper topicMapper,
            TopicItemMapper topicItemMapper,
            FileAssetMapper fileAssetMapper,
            AuditRecordService auditRecordService,
            CurrentAdminAccessor currentAdminAccessor,
            ResourceTagService resourceTagService,
            StableCoverUrlService stableCoverUrlService
    ) {
        this.homeCategoryMapper = homeCategoryMapper;
        this.homeContentMapper = homeContentMapper;
        this.articleMapper = articleMapper;
        this.courseMapper = courseMapper;
        this.bookMapper = bookMapper;
        this.podcastMapper = podcastMapper;
        this.liveSessionMapper = liveSessionMapper;
        this.podcastAudioMapper = podcastAudioMapper;
        this.topicMapper = topicMapper;
        this.topicItemMapper = topicItemMapper;
        this.fileAssetMapper = fileAssetMapper;
        this.auditRecordService = auditRecordService;
        this.currentAdminAccessor = currentAdminAccessor;
        this.resourceTagService = resourceTagService;
        this.stableCoverUrlService = stableCoverUrlService;
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
        NormalizedHomeContentRequest normalizedRequest = normalizeHomeContentRequest(request);
        HomeContent content = new HomeContent();
        fillHomeContent(content, normalizedRequest);
        content.setDeleted(0);
        homeContentMapper.insert(content);
        return toHomeContentResponse(content);
    }

    @Transactional(rollbackFor = Exception.class)
    public HomeContentResponse updateHomeContent(Long id, HomeContentRequest request) {
        requireHomeContent(id);
        requireHomeCategory(request.categoryId());
        NormalizedHomeContentRequest normalizedRequest = normalizeHomeContentRequest(request);
        HomeContent content = homeContentMapper.selectById(id);
        fillHomeContent(content, normalizedRequest);
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
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(Article::getTitle, query.keyword())
                                .or()
                                .like(Article::getSource, query.keyword()))
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
        resourceTagService.replaceTags(RESOURCE_TYPE_ARTICLE, article.getId(), request.tags());
        return toArticleResponse(article);
    }

    @Transactional(rollbackFor = Exception.class)
    public ArticleResponse updateArticle(Long id, ArticleRequest request) {
        Article article = requireArticle(id);
        fillArticle(article, request);
        articleMapper.updateById(article);
        resourceTagService.replaceTags(RESOURCE_TYPE_ARTICLE, article.getId(), request.tags());
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
        resourceTagService.replaceTags(RESOURCE_TYPE_ARTICLE, id, List.of());
    }

    public PageResponse<PodcastResponse> pagePodcasts(AdminContentPageQuery query) {
        Page<Podcast> page = podcastMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<Podcast>()
                        .eq(Podcast::getDeleted, 0)
                        .eq(query.reviewStatus() != null, Podcast::getReviewStatus, query.reviewStatus())
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(Podcast::getTitle, query.keyword())
                                .or()
                                .like(Podcast::getSpeakerName, query.keyword()))
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
        resourceTagService.replaceTags(RESOURCE_TYPE_PODCAST, podcast.getId(), request.tags());
        return toPodcastResponse(podcast);
    }

    @Transactional(rollbackFor = Exception.class)
    public PodcastResponse updatePodcast(Long id, PodcastRequest request) {
        Podcast podcast = requirePodcast(id);
        fillPodcast(podcast, request);
        podcastMapper.updateById(podcast);
        resourceTagService.replaceTags(RESOURCE_TYPE_PODCAST, podcast.getId(), request.tags());
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
        resourceTagService.replaceTags(RESOURCE_TYPE_PODCAST, id, List.of());
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
        List<NormalizedTopicItemRequest> normalizedRequests = normalizeTopicItemRequests(requests);
        topicItemMapper.delete(new LambdaQueryWrapper<TopicItem>().eq(TopicItem::getTopicId, topicId));
        if (!normalizedRequests.isEmpty()) {
            normalizedRequests.forEach(request -> {
                TopicItem item = new TopicItem();
                item.setTopicId(topicId);
                item.setItemType(request.itemType());
                item.setItemId(request.itemId());
                item.setSortOrder(request.sortOrder());
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

    private void fillHomeContent(HomeContent content, NormalizedHomeContentRequest request) {
        StableCoverUrlService.CoverBinding coverBinding = stableCoverUrlService.resolveCoverBinding(
                request.coverUrl(),
                content.getCoverUrl(),
                content.getCoverFileAssetId()
        );
        content.setCategoryId(request.categoryId());
        content.setContentType(request.contentType());
        content.setTargetId(request.targetId());
        content.setTitle(request.title());
        content.setCoverUrl(coverBinding.coverUrl());
        content.setCoverFileAssetId(coverBinding.fileAssetId());
        content.setLinkUrl(request.linkUrl());
        content.setSortOrder(request.sortOrder());
        content.setStartAt(request.startAt());
        content.setEndAt(request.endAt());
        content.setStatus(request.status());
    }

    private void fillArticle(Article article, ArticleRequest request) {
        StableCoverUrlService.CoverBinding coverBinding = stableCoverUrlService.resolveCoverBinding(
                request.coverUrl(),
                article.getCoverUrl(),
                article.getCoverFileAssetId()
        );
        article.setTitle(request.title());
        article.setSummary(request.summary());
        article.setCoverUrl(coverBinding.coverUrl());
        article.setCoverFileAssetId(coverBinding.fileAssetId());
        article.setContent(request.content());
        article.setAuthorName(request.authorName());
        article.setSource(request.source());
        article.setReviewStatus(request.reviewStatus());
        article.setPublishStatus(request.publishStatus());
        article.setPublishedAt(request.publishedAt());
    }

    private void fillPodcast(Podcast podcast, PodcastRequest request) {
        StableCoverUrlService.CoverBinding coverBinding = stableCoverUrlService.resolveCoverBinding(
                request.coverUrl(),
                podcast.getCoverUrl(),
                podcast.getCoverFileAssetId()
        );
        podcast.setTitle(request.title());
        podcast.setSummary(request.summary());
        podcast.setCoverUrl(coverBinding.coverUrl());
        podcast.setCoverFileAssetId(coverBinding.fileAssetId());
        podcast.setSpeakerName(request.speakerName());
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
        audio.setPaperId(request.paperId());
        audio.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        audio.setStatus(request.status());
    }

    private void fillTopic(Topic topic, TopicRequest request) {
        StableCoverUrlService.CoverBinding coverBinding = stableCoverUrlService.resolveCoverBinding(
                request.coverUrl(),
                topic.getCoverUrl(),
                topic.getCoverFileAssetId()
        );
        topic.setTitle(request.title());
        topic.setSummary(request.summary());
        topic.setLearningRequirements(request.learningRequirements());
        topic.setCoverUrl(coverBinding.coverUrl());
        topic.setCoverFileAssetId(coverBinding.fileAssetId());
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
        HomeContentTargetSummary summary = loadHomeContentTargetSummarySafely(content.getContentType(), content.getTargetId());
        return new HomeContentResponse(content.getId(), content.getCategoryId(), content.getContentType(),
                HOME_CONTENT_TYPE_LABELS.getOrDefault(content.getContentType(), content.getContentType()),
                content.getTargetId(), summary != null, summary == null ? null : summary.targetTitle(),
                content.getTitle(), content.getCoverUrl(), content.getLinkUrl(),
                content.getSortOrder(), content.getStartAt(), content.getEndAt(), content.getStatus());
    }

    private ArticleResponse toArticleResponse(Article article) {
        return new ArticleResponse(article.getId(), article.getTitle(), article.getSummary(), article.getCoverUrl(),
                article.getContent(), article.getAuthorName(), article.getSource(),
                resourceTagService.loadTagNames(RESOURCE_TYPE_ARTICLE, article.getId()), article.getViewCount(),
                article.getReviewStatus(), article.getPublishStatus(), article.getPublishedAt());
    }

    private PodcastResponse toPodcastResponse(Podcast podcast) {
        return new PodcastResponse(podcast.getId(), podcast.getTitle(), podcast.getSummary(), podcast.getCoverUrl(),
                podcast.getSpeakerName(), resourceTagService.loadTagNames(RESOURCE_TYPE_PODCAST, podcast.getId()),
                podcast.getSortOrder(), podcast.getReviewStatus(), podcast.getPublishStatus(), podcast.getPublishedAt());
    }

    private PodcastAudioResponse toPodcastAudioResponse(PodcastAudio audio) {
        return new PodcastAudioResponse(audio.getId(), audio.getPodcastId(), audio.getTitle(), audio.getAudioUrl(),
                audio.getDurationSeconds(), audio.getPaperId(), audio.getSortOrder(), audio.getStatus());
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
                .map(this::toTopicItemResponse)
                .toList();
    }

    private TopicItemResponse toTopicItemResponse(TopicItem item) {
        TopicItemResourceSummary summary = loadTopicItemResourceSummarySafely(item.getItemType(), item.getItemId());
        String itemTypeLabel = TOPIC_ITEM_TYPE_LABELS.getOrDefault(item.getItemType(), item.getItemType());
        return new TopicItemResponse(item.getId(), item.getTopicId(), item.getItemType(), itemTypeLabel,
                item.getItemId(), item.getSortOrder(), summary != null,
                summary == null ? null : summary.itemTitle(),
                summary == null ? null : summary.itemSubtitle(),
                summary == null ? null : summary.itemCoverUrl(),
                summary == null ? null : summary.reviewStatus(),
                summary == null ? null : summary.publishStatus());
    }

    private List<NormalizedTopicItemRequest> normalizeTopicItemRequests(List<TopicItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        List<NormalizedTopicItemRequest> normalizedRequests = new ArrayList<>();
        Set<String> duplicateKeys = new HashSet<>();
        for (int index = 0; index < requests.size(); index++) {
            TopicItemRequest request = requests.get(index);
            if (request == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Topic item request must not be null");
            }
            String itemType = normalizeTopicItemType(request.itemType());
            Long itemId = request.itemId();
            String duplicateKey = itemType + ":" + itemId;
            if (!duplicateKeys.add(duplicateKey)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                        "Duplicate topic item is not allowed: " + itemType + "#" + itemId);
            }
            if (request.sortOrder() != null && request.sortOrder() < 1) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "sortOrder must be greater than 0");
            }
            requireTopicItemResourceSummary(itemType, itemId);
            int requestedSortOrder = request.sortOrder() == null ? index + 1 : request.sortOrder();
            normalizedRequests.add(new NormalizedTopicItemRequest(itemType, itemId, requestedSortOrder, index));
        }
        normalizedRequests.sort(Comparator.comparingInt(NormalizedTopicItemRequest::sortOrder)
                .thenComparingInt(NormalizedTopicItemRequest::requestIndex));
        List<NormalizedTopicItemRequest> result = new ArrayList<>(normalizedRequests.size());
        for (int index = 0; index < normalizedRequests.size(); index++) {
            NormalizedTopicItemRequest request = normalizedRequests.get(index);
            result.add(new NormalizedTopicItemRequest(request.itemType(), request.itemId(), index + 1,
                    request.requestIndex()));
        }
        return result;
    }

    private NormalizedHomeContentRequest normalizeHomeContentRequest(HomeContentRequest request) {
        String normalizedContentType = normalizeHomeContentType(request.contentType());
        if (request.startAt() != null && request.endAt() != null && request.startAt().isAfter(request.endAt())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "startAt must be before endAt");
        }
        if (request.targetId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "targetId must not be null");
        }
        if (request.sortOrder() != null && request.sortOrder() < 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "sortOrder must not be less than 0");
        }
        requireHomeContentTargetSummary(normalizedContentType, request.targetId());
        return new NormalizedHomeContentRequest(
                request.categoryId(),
                normalizedContentType,
                request.targetId(),
                request.title(),
                request.coverUrl(),
                request.linkUrl(),
                request.sortOrder() == null ? 0 : request.sortOrder(),
                request.startAt(),
                request.endAt(),
                request.status()
        );
    }

    private String normalizeHomeContentType(String contentType) {
        String normalizedType = contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
        if (!HOME_CONTENT_TYPE_LABELS.containsKey(normalizedType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported home contentType: " + contentType);
        }
        return normalizedType;
    }

    private String normalizeTopicItemType(String itemType) {
        String normalizedType = itemType == null ? "" : itemType.trim().toLowerCase(Locale.ROOT);
        if (!TOPIC_ITEM_TYPE_LABELS.containsKey(normalizedType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported topic itemType: " + itemType);
        }
        return normalizedType;
    }

    private HomeContentTargetSummary loadHomeContentTargetSummarySafely(String contentType, Long targetId) {
        try {
            return requireHomeContentTargetSummary(contentType, targetId);
        } catch (BusinessException exception) {
            return null;
        }
    }

    private HomeContentTargetSummary requireHomeContentTargetSummary(String contentType, Long targetId) {
        if (targetId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "targetId must not be null");
        }
        return switch (contentType) {
            case RESOURCE_TYPE_COURSE -> new HomeContentTargetSummary(requireTopicCourse(targetId).getCourseName());
            case RESOURCE_TYPE_BOOK -> new HomeContentTargetSummary(requireTopicBook(targetId).getBookName());
            case RESOURCE_TYPE_PODCAST -> new HomeContentTargetSummary(requireTopicPodcast(targetId).getTitle());
            case RESOURCE_TYPE_TOPIC -> new HomeContentTargetSummary(requireTopic(targetId).getTitle());
            case RESOURCE_TYPE_LIVE -> new HomeContentTargetSummary(requireLiveSession(targetId).getTitle());
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported home contentType: " + contentType);
        };
    }

    private TopicItemResourceSummary loadTopicItemResourceSummarySafely(String itemType, Long itemId) {
        try {
            return requireTopicItemResourceSummary(itemType, itemId);
        } catch (BusinessException exception) {
            return null;
        }
    }

    private TopicItemResourceSummary requireTopicItemResourceSummary(String itemType, Long itemId) {
        return switch (itemType) {
            case RESOURCE_TYPE_COURSE -> {
                Course course = requireTopicCourse(itemId);
                yield new TopicItemResourceSummary(course.getCourseName(), course.getLecturerName(),
                        course.getCoverUrl(), course.getReviewStatus(),
                        course.getPublishStatus());
            }
            case RESOURCE_TYPE_BOOK -> {
                Book book = requireTopicBook(itemId);
                yield new TopicItemResourceSummary(book.getBookName(), book.getAuthor(), book.getCoverUrl(),
                        book.getReviewStatus(), book.getPublishStatus());
            }
            case RESOURCE_TYPE_PODCAST -> {
                Podcast podcast = requireTopicPodcast(itemId);
                yield new TopicItemResourceSummary(podcast.getTitle(), podcast.getSpeakerName(),
                        podcast.getCoverUrl(), podcast.getReviewStatus(),
                        podcast.getPublishStatus());
            }
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported topic itemType: " + itemType);
        };
    }

    private Course requireTopicCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null || !Objects.equals(course.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course does not exist");
        }
        return course;
    }

    private Book requireTopicBook(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null || !Objects.equals(book.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Book does not exist");
        }
        return book;
    }

    private Podcast requireTopicPodcast(Long id) {
        Podcast podcast = podcastMapper.selectById(id);
        if (podcast == null || !Objects.equals(podcast.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Podcast does not exist");
        }
        return podcast;
    }

    private LiveSession requireLiveSession(Long id) {
        LiveSession liveSession = liveSessionMapper.selectById(id);
        if (liveSession == null || !Objects.equals(liveSession.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Live session does not exist");
        }
        return liveSession;
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

    private record NormalizedTopicItemRequest(String itemType, Long itemId, int sortOrder, int requestIndex) {
    }

    private record NormalizedHomeContentRequest(
            Long categoryId,
            String contentType,
            Long targetId,
            String title,
            String coverUrl,
            String linkUrl,
            Integer sortOrder,
            LocalDateTime startAt,
            LocalDateTime endAt,
            com.gugugaga.jsmedicine.common.enums.EnabledStatus status
    ) {
    }

    private record HomeContentTargetSummary(String targetTitle) {
    }

    private record TopicItemResourceSummary(
            String itemTitle,
            String itemSubtitle,
            String itemCoverUrl,
            ReviewStatus reviewStatus,
            PublishStatus publishStatus
    ) {
    }
}
