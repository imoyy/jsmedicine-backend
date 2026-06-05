package com.gugugaga.jsmedicine.module.interaction.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.FeedbackStatus;
import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.QaStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.FeedbackResponse;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppBrowseHistoryRequest;
import com.gugugaga.jsmedicine.module.interaction.admin.dto.QaAnswerResponse;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppFeedbackRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppFavoriteRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppQaQuestionRequest;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppQaQuestionResponse;
import com.gugugaga.jsmedicine.module.interaction.app.dto.AppResourceInteractionResponse;
import com.gugugaga.jsmedicine.module.interaction.feedback.entity.Feedback;
import com.gugugaga.jsmedicine.module.interaction.feedback.mapper.FeedbackMapper;
import com.gugugaga.jsmedicine.module.interaction.favorite.entity.UserFavorite;
import com.gugugaga.jsmedicine.module.interaction.favorite.mapper.UserFavoriteMapper;
import com.gugugaga.jsmedicine.module.interaction.history.entity.UserBrowseHistory;
import com.gugugaga.jsmedicine.module.interaction.history.mapper.UserBrowseHistoryMapper;
import com.gugugaga.jsmedicine.module.interaction.qa.entity.QaAnswer;
import com.gugugaga.jsmedicine.module.interaction.qa.entity.QaQuestion;
import com.gugugaga.jsmedicine.module.interaction.qa.mapper.QaAnswerMapper;
import com.gugugaga.jsmedicine.module.interaction.qa.mapper.QaQuestionMapper;
import com.gugugaga.jsmedicine.module.content.article.entity.Article;
import com.gugugaga.jsmedicine.module.content.article.mapper.ArticleMapper;
import com.gugugaga.jsmedicine.module.learning.book.entity.Book;
import com.gugugaga.jsmedicine.module.learning.book.mapper.BookMapper;
import com.gugugaga.jsmedicine.module.learning.course.entity.Course;
import com.gugugaga.jsmedicine.module.learning.course.mapper.CourseMapper;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSession;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionMapper;
import com.gugugaga.jsmedicine.module.learning.podcast.entity.Podcast;
import com.gugugaga.jsmedicine.module.learning.podcast.mapper.PodcastMapper;
import com.gugugaga.jsmedicine.module.content.topic.entity.Topic;
import com.gugugaga.jsmedicine.module.content.topic.mapper.TopicMapper;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AppInteractionService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final String RESOURCE_TYPE_ARTICLE = "article";
    private static final String RESOURCE_TYPE_COURSE = "course";
    private static final String RESOURCE_TYPE_BOOK = "book";
    private static final String RESOURCE_TYPE_PODCAST = "podcast";
    private static final String RESOURCE_TYPE_TOPIC = "topic";
    private static final String RESOURCE_TYPE_LIVE = "live";

    private final CurrentAppUserResolver currentAppUserResolver;
    private final StudentMapper studentMapper;
    private final QaQuestionMapper qaQuestionMapper;
    private final QaAnswerMapper qaAnswerMapper;
    private final FeedbackMapper feedbackMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final ArticleMapper articleMapper;
    private final CourseMapper courseMapper;
    private final BookMapper bookMapper;
    private final PodcastMapper podcastMapper;
    private final TopicMapper topicMapper;
    private final LiveSessionMapper liveSessionMapper;

    public AppInteractionService(
            CurrentAppUserResolver currentAppUserResolver,
            StudentMapper studentMapper,
            QaQuestionMapper qaQuestionMapper,
            QaAnswerMapper qaAnswerMapper,
            FeedbackMapper feedbackMapper,
            UserFavoriteMapper userFavoriteMapper,
            UserBrowseHistoryMapper userBrowseHistoryMapper,
            ArticleMapper articleMapper,
            CourseMapper courseMapper,
            BookMapper bookMapper,
            PodcastMapper podcastMapper,
            TopicMapper topicMapper,
            LiveSessionMapper liveSessionMapper
    ) {
        this.currentAppUserResolver = currentAppUserResolver;
        this.studentMapper = studentMapper;
        this.qaQuestionMapper = qaQuestionMapper;
        this.qaAnswerMapper = qaAnswerMapper;
        this.feedbackMapper = feedbackMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
        this.articleMapper = articleMapper;
        this.courseMapper = courseMapper;
        this.bookMapper = bookMapper;
        this.podcastMapper = podcastMapper;
        this.topicMapper = topicMapper;
        this.liveSessionMapper = liveSessionMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public AppQaQuestionResponse createQuestion(AppQaQuestionRequest request) {
        AppUserSession session = currentAppUserResolver.requireCurrentUser();
        QaQuestion question = new QaQuestion();
        question.setUserId(session.userId());
        question.setStudentId(findStudent(session.userId()).map(Student::getId).orElse(null));
        question.setExpertCategoryId(request.expertCategoryId());
        question.setExpertId(request.expertId());
        question.setTitle(request.title());
        question.setContent(request.content());
        question.setStatus(QaStatus.PENDING);
        question.setDeleted(0);
        qaQuestionMapper.insert(question);
        return toQaQuestionResponse(question, true);
    }

    public PageResponse<AppQaQuestionResponse> myQuestions(long page, long size) {
        Long userId = currentAppUserResolver.requireCurrentUser().userId();
        Page<QaQuestion> questionPage = qaQuestionMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<QaQuestion>()
                        .eq(QaQuestion::getDeleted, 0)
                        .eq(QaQuestion::getUserId, userId)
                        .orderByDesc(QaQuestion::getCreatedAt));
        return pageResponse(questionPage, questionPage.getRecords().stream().map(question -> toQaQuestionResponse(question, false)).toList());
    }

    public AppQaQuestionResponse questionDetail(Long id) {
        Long userId = currentAppUserResolver.requireCurrentUser().userId();
        QaQuestion question = qaQuestionMapper.selectOne(new LambdaQueryWrapper<QaQuestion>()
                .eq(QaQuestion::getDeleted, 0)
                .eq(QaQuestion::getId, id)
                .eq(QaQuestion::getUserId, userId)
                .last("LIMIT 1"));
        if (question == null) {
            throw new com.gugugaga.jsmedicine.common.exception.BusinessException(
                    com.gugugaga.jsmedicine.common.exception.ErrorCode.NOT_FOUND, "QA question does not exist");
        }
        return toQaQuestionResponse(question, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public FeedbackResponse submitFeedback(AppFeedbackRequest request) {
        AppUserSession session = currentAppUserResolver.requireCurrentUser();
        Feedback feedback = new Feedback();
        feedback.setUserId(session.userId());
        feedback.setStudentId(findStudent(session.userId()).map(Student::getId).orElse(null));
        feedback.setFeedbackType(request.feedbackType());
        feedback.setContent(request.content());
        feedback.setContact(request.contact());
        feedback.setStatus(FeedbackStatus.PENDING);
        feedback.setDeleted(0);
        feedbackMapper.insert(feedback);
        return toFeedbackResponse(feedback);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppResourceInteractionResponse toggleFavorite(AppFavoriteRequest request) {
        AppUserSession session = currentAppUserResolver.requireCurrentUser();
        validateVisibleResource(request.resourceType(), request.resourceId());
        UserFavorite favorite = userFavoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, session.userId())
                .eq(UserFavorite::getResourceType, request.resourceType())
                .eq(UserFavorite::getResourceId, request.resourceId())
                .last("LIMIT 1"));
        if (Boolean.TRUE.equals(request.favorited())) {
            if (favorite == null) {
                favorite = new UserFavorite();
                favorite.setUserId(session.userId());
                favorite.setResourceType(request.resourceType());
                favorite.setResourceId(request.resourceId());
                userFavoriteMapper.insert(favorite);
            }
        } else if (favorite != null) {
            userFavoriteMapper.deleteById(favorite.getId());
        }
        return buildInteractionResponse(session.userId(), request.resourceType(), request.resourceId());
    }

    @Transactional(rollbackFor = Exception.class)
    public AppResourceInteractionResponse syncBrowseHistory(AppBrowseHistoryRequest request) {
        AppUserSession session = currentAppUserResolver.requireCurrentUser();
        validateVisibleResource(request.resourceType(), request.resourceId());
        UserBrowseHistory history = userBrowseHistoryMapper.selectOne(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, session.userId())
                .eq(UserBrowseHistory::getResourceType, request.resourceType())
                .eq(UserBrowseHistory::getResourceId, request.resourceId())
                .last("LIMIT 1"));
        int increment = request.viewCount() == null || request.viewCount() < 1 ? 1 : request.viewCount();
        LocalDateTime now = LocalDateTime.now();
        if (history == null) {
            history = new UserBrowseHistory();
            history.setUserId(session.userId());
            history.setResourceType(request.resourceType());
            history.setResourceId(request.resourceId());
            history.setViewCount(increment);
        } else {
            history.setViewCount((history.getViewCount() == null ? 0 : history.getViewCount()) + increment);
        }
        history.setSource(request.source());
        history.setViewedAt(now);
        history.setUpdatedAt(now);
        if (history.getId() == null) {
            userBrowseHistoryMapper.insert(history);
        } else {
            userBrowseHistoryMapper.updateById(history);
        }
        if (RESOURCE_TYPE_ARTICLE.equals(request.resourceType())) {
            syncArticleViewCount(request.resourceId());
        }
        if (RESOURCE_TYPE_TOPIC.equals(request.resourceType())) {
            syncTopicViewCount(request.resourceId());
        }
        return buildInteractionResponse(session.userId(), request.resourceType(), request.resourceId());
    }

    private Optional<Student> findStudent(Long userId) {
        return Optional.ofNullable(studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
                .eq(Student::getDeleted, 0)
                .eq(Student::getStatus, EnabledStatus.ENABLED)
                .last("LIMIT 1")));
    }

    private void validateVisibleResource(String resourceType, Long resourceId) {
        switch (resourceType) {
            case RESOURCE_TYPE_ARTICLE -> requireVisibleArticle(resourceId);
            case RESOURCE_TYPE_COURSE -> requireVisibleCourse(resourceId);
            case RESOURCE_TYPE_BOOK -> requireVisibleBook(resourceId);
            case RESOURCE_TYPE_PODCAST -> requireVisiblePodcast(resourceId);
            case RESOURCE_TYPE_TOPIC -> requireVisibleTopic(resourceId);
            case RESOURCE_TYPE_LIVE -> requireVisibleLive(resourceId);
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported interaction resource type");
        }
    }

    private void requireVisibleCourse(Long resourceId) {
        Course course = courseMapper.selectById(resourceId);
        if (course == null || !isVisible(course.getDeleted(), course.getReviewStatus(), course.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course does not exist");
        }
    }

    private void requireVisibleArticle(Long resourceId) {
        Article article = articleMapper.selectById(resourceId);
        if (article == null || !isVisible(article.getDeleted(), article.getReviewStatus(), article.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Article does not exist");
        }
    }

    private void requireVisibleBook(Long resourceId) {
        Book book = bookMapper.selectById(resourceId);
        if (book == null || !isVisible(book.getDeleted(), book.getReviewStatus(), book.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Book does not exist");
        }
    }

    private void requireVisiblePodcast(Long resourceId) {
        Podcast podcast = podcastMapper.selectById(resourceId);
        if (podcast == null || !isVisible(podcast.getDeleted(), podcast.getReviewStatus(), podcast.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Podcast does not exist");
        }
    }

    private void requireVisibleTopic(Long resourceId) {
        Topic topic = topicMapper.selectById(resourceId);
        if (topic == null || !isVisible(topic.getDeleted(), topic.getReviewStatus(), topic.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Topic does not exist");
        }
    }

    private void requireVisibleLive(Long resourceId) {
        LiveSession liveSession = liveSessionMapper.selectById(resourceId);
        if (liveSession == null
                || !Objects.equals(liveSession.getDeleted(), 0)
                || liveSession.getReviewStatus() != ReviewStatus.APPROVED
                || liveSession.getLiveStatus() == null
                || liveSession.getLiveStatus() == LiveStatus.CANCELED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Live session does not exist");
        }
    }

    private AppResourceInteractionResponse buildInteractionResponse(Long userId, String resourceType, Long resourceId) {
        long browseCount = userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                        .eq(UserBrowseHistory::getResourceType, resourceType)
                        .eq(UserBrowseHistory::getResourceId, resourceId))
                .stream()
                .map(UserBrowseHistory::getViewCount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        long favoriteCount = userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getResourceType, resourceType)
                .eq(UserFavorite::getResourceId, resourceId));
        boolean favorited = userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getResourceType, resourceType)
                .eq(UserFavorite::getResourceId, resourceId)) > 0;
        return new AppResourceInteractionResponse(resourceType, resourceId, browseCount, favoriteCount, favorited);
    }

    private void syncArticleViewCount(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            return;
        }
        long browseCount = userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                        .eq(UserBrowseHistory::getResourceType, RESOURCE_TYPE_ARTICLE)
                        .eq(UserBrowseHistory::getResourceId, articleId))
                .stream()
                .map(UserBrowseHistory::getViewCount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        long currentViewCount = article.getViewCount() == null ? 0L : article.getViewCount();
        article.setViewCount(Math.max(currentViewCount, browseCount));
        articleMapper.updateById(article);
    }

    private void syncTopicViewCount(Long topicId) {
        Topic topic = topicMapper.selectById(topicId);
        if (topic == null) {
            return;
        }
        long browseCount = userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                        .eq(UserBrowseHistory::getResourceType, RESOURCE_TYPE_TOPIC)
                        .eq(UserBrowseHistory::getResourceId, topicId))
                .stream()
                .map(UserBrowseHistory::getViewCount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        topic.setViewCount(browseCount);
        topicMapper.updateById(topic);
    }

    private boolean isVisible(Integer deleted, ReviewStatus reviewStatus, PublishStatus publishStatus) {
        return Objects.equals(deleted, 0)
                && reviewStatus == ReviewStatus.APPROVED
                && publishStatus == PublishStatus.PUBLISHED;
    }

    private AppQaQuestionResponse toQaQuestionResponse(QaQuestion question, boolean includeAnswers) {
        return new AppQaQuestionResponse(question.getId(), question.getExpertCategoryId(), question.getExpertId(),
                question.getTitle(), question.getContent(), question.getStatus(),
                qaStatusCode(question.getStatus()), qaStatusLabel(question.getStatus()),
                includeAnswers ? loadAnswers(question.getId()) : List.of());
    }

    private List<QaAnswerResponse> loadAnswers(Long questionId) {
        return qaAnswerMapper.selectList(new LambdaQueryWrapper<QaAnswer>()
                        .eq(QaAnswer::getDeleted, 0)
                        .eq(QaAnswer::getQuestionId, questionId)
                        .orderByAsc(QaAnswer::getAnsweredAt))
                .stream()
                .map(answer -> new QaAnswerResponse(answer.getId(), answer.getQuestionId(), answer.getAdminId(),
                        answer.getExpertId(), answer.getContent(), answer.getAnsweredAt()))
                .toList();
    }

    private FeedbackResponse toFeedbackResponse(Feedback feedback) {
        return new FeedbackResponse(feedback.getId(), feedback.getUserId(), feedback.getStudentId(),
                null, null, null, feedback.getFeedbackType(), feedback.getContent(), feedback.getContact(),
                feedback.getStatus(), feedback.getProcessedBy(), feedback.getProcessedAt(),
                feedback.getProcessNote(), feedback.getCreatedAt());
    }

    private String qaStatusCode(QaStatus status) {
        QaStatus safeStatus = status == null ? QaStatus.PENDING : status;
        return switch (safeStatus) {
            case PENDING -> "pending";
            case ANSWERED -> "answered";
            case CLOSED -> "closed";
        };
    }

    private String qaStatusLabel(QaStatus status) {
        QaStatus safeStatus = status == null ? QaStatus.PENDING : status;
        return switch (safeStatus) {
            case PENDING -> "待回复";
            case ANSWERED -> "已回复";
            case CLOSED -> "已关闭";
        };
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
}
