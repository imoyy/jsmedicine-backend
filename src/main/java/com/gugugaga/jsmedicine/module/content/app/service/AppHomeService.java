package com.gugugaga.jsmedicine.module.content.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.entity.ReviewableEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.content.app.dto.AppHomeItemResponse;
import com.gugugaga.jsmedicine.module.content.app.dto.AppHomeResponse;
import com.gugugaga.jsmedicine.module.content.app.dto.AppHomeSectionResponse;
import com.gugugaga.jsmedicine.module.content.article.entity.Article;
import com.gugugaga.jsmedicine.module.content.article.mapper.ArticleMapper;
import com.gugugaga.jsmedicine.module.content.home.entity.HomeCategory;
import com.gugugaga.jsmedicine.module.content.home.entity.HomeContent;
import com.gugugaga.jsmedicine.module.content.home.mapper.HomeCategoryMapper;
import com.gugugaga.jsmedicine.module.content.home.mapper.HomeContentMapper;
import com.gugugaga.jsmedicine.module.content.topic.entity.Topic;
import com.gugugaga.jsmedicine.module.content.topic.mapper.TopicMapper;
import com.gugugaga.jsmedicine.module.learning.book.entity.Book;
import com.gugugaga.jsmedicine.module.learning.book.mapper.BookMapper;
import com.gugugaga.jsmedicine.module.learning.course.entity.Course;
import com.gugugaga.jsmedicine.module.learning.course.mapper.CourseMapper;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSession;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionMapper;
import com.gugugaga.jsmedicine.module.learning.podcast.entity.Podcast;
import com.gugugaga.jsmedicine.module.learning.podcast.mapper.PodcastMapper;
import com.gugugaga.jsmedicine.module.knowledge.entity.KnowledgeEntry;
import com.gugugaga.jsmedicine.module.knowledge.mapper.KnowledgeEntryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
public class AppHomeService {

    private static final String RESOURCE_TYPE_COURSE = "course";
    private static final String RESOURCE_TYPE_BOOK = "book";
    private static final String RESOURCE_TYPE_ARTICLE = "article";
    private static final String RESOURCE_TYPE_PODCAST = "podcast";
    private static final String RESOURCE_TYPE_TOPIC = "topic";
    private static final String RESOURCE_TYPE_KNOWLEDGE = "knowledge";
    private static final String RESOURCE_TYPE_LIVE = "live";
    private static final Map<String, String> HOME_CONTENT_TYPE_LABELS = Map.of(
            RESOURCE_TYPE_COURSE, "课程",
            RESOURCE_TYPE_BOOK, "图书",
            RESOURCE_TYPE_ARTICLE, "资讯",
            RESOURCE_TYPE_PODCAST, "播客",
            RESOURCE_TYPE_TOPIC, "专题",
            RESOURCE_TYPE_KNOWLEDGE, "知识库",
            RESOURCE_TYPE_LIVE, "直播"
    );

    private final HomeCategoryMapper homeCategoryMapper;
    private final HomeContentMapper homeContentMapper;
    private final ArticleMapper articleMapper;
    private final CourseMapper courseMapper;
    private final BookMapper bookMapper;
    private final PodcastMapper podcastMapper;
    private final TopicMapper topicMapper;
    private final KnowledgeEntryMapper knowledgeEntryMapper;
    private final LiveSessionMapper liveSessionMapper;

    public AppHomeService(
            HomeCategoryMapper homeCategoryMapper,
            HomeContentMapper homeContentMapper,
            ArticleMapper articleMapper,
            CourseMapper courseMapper,
            BookMapper bookMapper,
            PodcastMapper podcastMapper,
            TopicMapper topicMapper,
            KnowledgeEntryMapper knowledgeEntryMapper,
            LiveSessionMapper liveSessionMapper
    ) {
        this.homeCategoryMapper = homeCategoryMapper;
        this.homeContentMapper = homeContentMapper;
        this.articleMapper = articleMapper;
        this.courseMapper = courseMapper;
        this.bookMapper = bookMapper;
        this.podcastMapper = podcastMapper;
        this.topicMapper = topicMapper;
        this.knowledgeEntryMapper = knowledgeEntryMapper;
        this.liveSessionMapper = liveSessionMapper;
    }

    public AppHomeResponse home() {
        List<HomeCategory> categories = homeCategoryMapper.selectList(new LambdaQueryWrapper<HomeCategory>()
                .eq(HomeCategory::getDeleted, 0)
                .eq(HomeCategory::getStatus, EnabledStatus.ENABLED)
                .orderByAsc(HomeCategory::getSortOrder)
                .orderByDesc(HomeCategory::getCreatedAt));
        if (categories.isEmpty()) {
            return new AppHomeResponse(List.of());
        }

        List<Long> categoryIds = categories.stream().map(HomeCategory::getId).toList();
        List<HomeContent> contents = homeContentMapper.selectList(new LambdaQueryWrapper<HomeContent>()
                .eq(HomeContent::getDeleted, 0)
                .eq(HomeContent::getStatus, EnabledStatus.ENABLED)
                .in(HomeContent::getCategoryId, categoryIds)
                .orderByAsc(HomeContent::getSortOrder)
                .orderByDesc(HomeContent::getCreatedAt));
        Map<Long, List<AppHomeItemResponse>> itemsByCategory = groupVisibleItemsByCategory(contents, LocalDateTime.now());

        List<AppHomeSectionResponse> sections = categories.stream()
                .map(category -> toSectionResponse(category, itemsByCategory.getOrDefault(category.getId(), List.of())))
                .filter(section -> !section.items().isEmpty())
                .toList();
        return new AppHomeResponse(sections);
    }

    private Map<Long, List<AppHomeItemResponse>> groupVisibleItemsByCategory(List<HomeContent> contents, LocalDateTime now) {
        Map<Long, List<AppHomeItemResponse>> itemsByCategory = new LinkedHashMap<>();
        for (HomeContent content : contents) {
            if (!isWithinDisplayWindow(content, now)) {
                continue;
            }
            AppHomeItemResponse item = toHomeItemResponse(content);
            if (item == null) {
                continue;
            }
            itemsByCategory.computeIfAbsent(content.getCategoryId(), key -> new ArrayList<>()).add(item);
        }
        return itemsByCategory;
    }

    private AppHomeSectionResponse toSectionResponse(HomeCategory category, List<AppHomeItemResponse> items) {
        return new AppHomeSectionResponse(category.getId(), category.getCategoryName(), category.getCategoryCode(),
                category.getIconUrl(), category.getDescription(), category.getSortOrder(), items);
    }

    private AppHomeItemResponse toHomeItemResponse(HomeContent content) {
        if (content.getTargetId() == null) {
            return null;
        }
        String contentType = normalizeHomeContentType(content.getContentType());
        return switch (contentType) {
            case RESOURCE_TYPE_COURSE -> toCourseHomeItem(content);
            case RESOURCE_TYPE_BOOK -> toBookHomeItem(content);
            case RESOURCE_TYPE_ARTICLE -> toArticleHomeItem(content);
            case RESOURCE_TYPE_PODCAST -> toPodcastHomeItem(content);
            case RESOURCE_TYPE_TOPIC -> toTopicHomeItem(content);
            case RESOURCE_TYPE_KNOWLEDGE -> toKnowledgeHomeItem(content);
            case RESOURCE_TYPE_LIVE -> toLiveHomeItem(content);
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Unsupported home contentType: " + content.getContentType());
        };
    }

    private AppHomeItemResponse toCourseHomeItem(HomeContent content) {
        Course course = loadVisibleCourse(content.getTargetId());
        if (course == null) {
            return null;
        }
        return baseHomeItem(content, RESOURCE_TYPE_COURSE, course.getCourseName(), course.getLecturerName(),
                course.getSubtitle(), course.getCoverUrl());
    }

    private AppHomeItemResponse toBookHomeItem(HomeContent content) {
        Book book = loadVisibleBook(content.getTargetId());
        if (book == null) {
            return null;
        }
        return baseHomeItem(content, RESOURCE_TYPE_BOOK, book.getBookName(), book.getAuthor(),
                book.getIntroduction(), book.getCoverUrl());
    }

    private AppHomeItemResponse toArticleHomeItem(HomeContent content) {
        Article article = loadVisibleArticle(content.getTargetId());
        if (article == null) {
            return null;
        }
        return baseHomeItem(content, RESOURCE_TYPE_ARTICLE, article.getTitle(), article.getSource(),
                article.getSummary(), article.getCoverUrl());
    }

    private AppHomeItemResponse toPodcastHomeItem(HomeContent content) {
        Podcast podcast = loadVisiblePodcast(content.getTargetId());
        if (podcast == null) {
            return null;
        }
        return baseHomeItem(content, RESOURCE_TYPE_PODCAST, podcast.getTitle(), podcast.getSpeakerName(),
                podcast.getSummary(), podcast.getCoverUrl());
    }

    private AppHomeItemResponse toTopicHomeItem(HomeContent content) {
        Topic topic = loadVisibleTopic(content.getTargetId());
        if (topic == null) {
            return null;
        }
        return baseHomeItem(content, RESOURCE_TYPE_TOPIC, topic.getTitle(), null,
                topic.getSummary(), topic.getCoverUrl());
    }

    private AppHomeItemResponse toKnowledgeHomeItem(HomeContent content) {
        KnowledgeEntry entry = loadVisibleKnowledgeEntry(content.getTargetId());
        if (entry == null) {
            return null;
        }
        return baseHomeItem(content, RESOURCE_TYPE_KNOWLEDGE, entry.getTitle(), entry.getSource(),
                entry.getSummary(), entry.getCoverUrl());
    }

    private AppHomeItemResponse toLiveHomeItem(HomeContent content) {
        LiveSession liveSession = loadVisibleLiveSession(content.getTargetId());
        if (liveSession == null) {
            return null;
        }
        return baseHomeItem(content, RESOURCE_TYPE_LIVE, liveSession.getTitle(), resolvedLiveSpeakerName(liveSession),
                null, liveSession.getCoverUrl());
    }

    private AppHomeItemResponse baseHomeItem(
            HomeContent content,
            String contentType,
            String title,
            String subtitle,
            String summary,
            String coverUrl
    ) {
        return new AppHomeItemResponse(content.getId(), contentType,
                HOME_CONTENT_TYPE_LABELS.getOrDefault(contentType, contentType), content.getTargetId(),
                title, subtitle, summary, coverUrl, content.getLinkUrl(), content.getSortOrder());
    }

    private Article loadVisibleArticle(Long targetId) {
        Article article = articleMapper.selectById(targetId);
        return isVisibleReviewable(article) ? article : null;
    }

    private Course loadVisibleCourse(Long targetId) {
        Course course = courseMapper.selectById(targetId);
        return isVisibleReviewable(course) ? course : null;
    }

    private Book loadVisibleBook(Long targetId) {
        Book book = bookMapper.selectById(targetId);
        return isVisibleReviewable(book) ? book : null;
    }

    private Podcast loadVisiblePodcast(Long targetId) {
        Podcast podcast = podcastMapper.selectById(targetId);
        return isVisibleReviewable(podcast) ? podcast : null;
    }

    private Topic loadVisibleTopic(Long targetId) {
        Topic topic = topicMapper.selectById(targetId);
        return isVisibleReviewable(topic) ? topic : null;
    }

    private KnowledgeEntry loadVisibleKnowledgeEntry(Long targetId) {
        KnowledgeEntry entry = knowledgeEntryMapper.selectById(targetId);
        return isVisibleReviewable(entry) ? entry : null;
    }

    private LiveSession loadVisibleLiveSession(Long targetId) {
        LiveSession liveSession = liveSessionMapper.selectById(targetId);
        if (liveSession == null) {
            return null;
        }
        if (!Objects.equals(liveSession.getDeleted(), 0) || liveSession.getReviewStatus() != ReviewStatus.APPROVED) {
            return null;
        }
        return liveSession.getLiveStatus() == LiveStatus.CANCELED ? null : liveSession;
    }

    private boolean isVisibleReviewable(ReviewableEntity resource) {
        return resource != null
                && Objects.equals(resource.getDeleted(), 0)
                && resource.getReviewStatus() == ReviewStatus.APPROVED
                && resource.getPublishStatus() == PublishStatus.PUBLISHED;
    }

    private boolean isWithinDisplayWindow(HomeContent content, LocalDateTime now) {
        return (content.getStartAt() == null || !content.getStartAt().isAfter(now))
                && (content.getEndAt() == null || !content.getEndAt().isBefore(now));
    }

    private String normalizeHomeContentType(String contentType) {
        String normalizedType = contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
        if (!HOME_CONTENT_TYPE_LABELS.containsKey(normalizedType)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported home contentType: " + contentType);
        }
        return normalizedType;
    }

    private String resolvedLiveSpeakerName(LiveSession liveSession) {
        return hasText(liveSession.getSpeakerName()) ? liveSession.getSpeakerName() : liveSession.getAnchorName();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
