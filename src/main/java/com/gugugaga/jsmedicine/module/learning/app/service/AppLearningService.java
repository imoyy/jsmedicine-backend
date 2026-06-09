package com.gugugaga.jsmedicine.module.learning.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.ExamRecordStatus;
import com.gugugaga.jsmedicine.common.enums.ExamSubmitType;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.QuestionType;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.common.service.ResourceTagService;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import com.gugugaga.jsmedicine.module.content.topic.entity.Topic;
import com.gugugaga.jsmedicine.module.content.topic.entity.TopicItem;
import com.gugugaga.jsmedicine.module.content.topic.mapper.TopicItemMapper;
import com.gugugaga.jsmedicine.module.content.topic.mapper.TopicMapper;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppBookCategoryResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppBookChapterResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppBookResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppCourseResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppCourseVideoResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamAnswerResultResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamPaperResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamQuestionOptionResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamQuestionResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamRecordResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamSubmitRequest;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppLearningPageQuery;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppLearningRecordRequest;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppLearningRecordResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppPodcastAudioResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppPodcastResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppTopicCardResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppTopicDetailResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppTopicResourceCardResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppTopicSectionResponse;
import com.gugugaga.jsmedicine.module.learning.book.entity.Book;
import com.gugugaga.jsmedicine.module.learning.book.entity.BookCategory;
import com.gugugaga.jsmedicine.module.learning.book.entity.BookChapter;
import com.gugugaga.jsmedicine.module.learning.book.mapper.BookCategoryMapper;
import com.gugugaga.jsmedicine.module.learning.book.mapper.BookChapterMapper;
import com.gugugaga.jsmedicine.module.learning.book.mapper.BookMapper;
import com.gugugaga.jsmedicine.module.learning.course.entity.Course;
import com.gugugaga.jsmedicine.module.learning.course.entity.CourseVideo;
import com.gugugaga.jsmedicine.module.learning.course.mapper.CourseMapper;
import com.gugugaga.jsmedicine.module.learning.course.mapper.CourseVideoMapper;
import com.gugugaga.jsmedicine.module.learning.podcast.entity.Podcast;
import com.gugugaga.jsmedicine.module.learning.podcast.entity.PodcastAudio;
import com.gugugaga.jsmedicine.module.learning.podcast.mapper.PodcastAudioMapper;
import com.gugugaga.jsmedicine.module.learning.podcast.mapper.PodcastMapper;
import com.gugugaga.jsmedicine.module.learning.question.entity.ExamPaper;
import com.gugugaga.jsmedicine.module.learning.question.entity.ExamPaperQuestion;
import com.gugugaga.jsmedicine.module.learning.question.entity.Question;
import com.gugugaga.jsmedicine.module.learning.question.entity.QuestionOption;
import com.gugugaga.jsmedicine.module.learning.question.mapper.ExamPaperMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.ExamPaperQuestionMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.QuestionMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.QuestionOptionMapper;
import com.gugugaga.jsmedicine.module.learning.record.entity.ExamRecord;
import com.gugugaga.jsmedicine.module.learning.record.entity.ExamRecordAnswer;
import com.gugugaga.jsmedicine.module.learning.record.mapper.ExamRecordAnswerMapper;
import com.gugugaga.jsmedicine.module.learning.record.mapper.ExamRecordMapper;
import com.gugugaga.jsmedicine.module.learning.record.entity.LearningRecord;
import com.gugugaga.jsmedicine.module.learning.record.mapper.LearningRecordMapper;
import com.gugugaga.jsmedicine.module.interaction.favorite.entity.UserFavorite;
import com.gugugaga.jsmedicine.module.interaction.favorite.mapper.UserFavoriteMapper;
import com.gugugaga.jsmedicine.module.interaction.history.entity.UserBrowseHistory;
import com.gugugaga.jsmedicine.module.interaction.history.mapper.UserBrowseHistoryMapper;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AppLearningService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final BigDecimal ZERO_PROGRESS = BigDecimal.ZERO.setScale(2);
    private static final long ZERO_COUNT = 0L;
    private static final String RESOURCE_TYPE_COURSE = "course";
    private static final String RESOURCE_TYPE_BOOK = "book";
    private static final String RESOURCE_TYPE_PODCAST = "podcast";
    private static final String RESOURCE_TYPE_TOPIC = "topic";
    private static final String TOPIC_SECTION_LEARNING = "learning";
    private static final String TOPIC_SECTION_VIDEO = "video";
    private static final String TOPIC_SECTION_AUDIO = "audio";
    private static final int TOPIC_SECTION_PREVIEW_SIZE = 3;
    private static final Map<String, String> TOPIC_RESOURCE_TYPE_LABELS = Map.of(
            RESOURCE_TYPE_COURSE, "课程",
            RESOURCE_TYPE_BOOK, "图书",
            RESOURCE_TYPE_PODCAST, "播客"
    );

    private final CurrentAppUserResolver currentAppUserResolver;
    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;
    private final CourseVideoMapper courseVideoMapper;
    private final BookCategoryMapper bookCategoryMapper;
    private final BookMapper bookMapper;
    private final BookChapterMapper bookChapterMapper;
    private final PodcastMapper podcastMapper;
    private final PodcastAudioMapper podcastAudioMapper;
    private final TopicMapper topicMapper;
    private final TopicItemMapper topicItemMapper;
    private final LearningRecordMapper learningRecordMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final ExamRecordAnswerMapper examRecordAnswerMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;
    private final ResourceTagService resourceTagService;

    public AppLearningService(
            CurrentAppUserResolver currentAppUserResolver,
            StudentMapper studentMapper,
            CourseMapper courseMapper,
            CourseVideoMapper courseVideoMapper,
            BookCategoryMapper bookCategoryMapper,
            BookMapper bookMapper,
            BookChapterMapper bookChapterMapper,
            PodcastMapper podcastMapper,
            PodcastAudioMapper podcastAudioMapper,
            TopicMapper topicMapper,
            TopicItemMapper topicItemMapper,
            LearningRecordMapper learningRecordMapper,
            ExamPaperMapper examPaperMapper,
            ExamPaperQuestionMapper examPaperQuestionMapper,
            QuestionMapper questionMapper,
            QuestionOptionMapper questionOptionMapper,
            ExamRecordMapper examRecordMapper,
            ExamRecordAnswerMapper examRecordAnswerMapper,
            UserFavoriteMapper userFavoriteMapper,
            UserBrowseHistoryMapper userBrowseHistoryMapper,
            ResourceTagService resourceTagService
    ) {
        this.currentAppUserResolver = currentAppUserResolver;
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
        this.courseVideoMapper = courseVideoMapper;
        this.bookCategoryMapper = bookCategoryMapper;
        this.bookMapper = bookMapper;
        this.bookChapterMapper = bookChapterMapper;
        this.podcastMapper = podcastMapper;
        this.podcastAudioMapper = podcastAudioMapper;
        this.topicMapper = topicMapper;
        this.topicItemMapper = topicItemMapper;
        this.learningRecordMapper = learningRecordMapper;
        this.examPaperMapper = examPaperMapper;
        this.examPaperQuestionMapper = examPaperQuestionMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.examRecordMapper = examRecordMapper;
        this.examRecordAnswerMapper = examRecordAnswerMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
        this.resourceTagService = resourceTagService;
    }

    public PageResponse<AppCourseResponse> pageCourses(AppLearningPageQuery query) {
        Page<Course> page = courseMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                visibleCourseWrapper()
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(Course::getCourseName, query.keyword())
                                .or()
                                .like(Course::getLecturerName, query.keyword()))
                        .orderByAsc("sortOrderAsc".equals(query.sort()), Course::getSortOrder)
                        .orderByDesc(!"sortOrderAsc".equals(query.sort()), Course::getPublishedAt));
        Long studentId = currentStudentId().orElse(null);
        Long userId = currentUserId().orElse(null);
        Map<Long, ResourceInteractionSnapshot> snapshots = loadInteractionSnapshots(userId, RESOURCE_TYPE_COURSE,
                page.getRecords().stream().map(Course::getId).toList());
        return pageResponse(page, page.getRecords().stream()
                .map(course -> toCourseResponse(course, false, studentId, snapshots.get(course.getId())))
                .toList());
    }

    public AppCourseResponse courseDetail(Long id) {
        Course course = requireVisibleCourse(id);
        return toCourseResponse(course, true, currentStudentId().orElse(null),
                loadInteractionSnapshot(currentUserId().orElse(null), RESOURCE_TYPE_COURSE, course.getId()));
    }

    public AppCourseVideoResponse courseVideoDetail(Long courseId, Long videoId) {
        requireVisibleCourse(courseId);
        CourseVideo video = requireVisibleCourseVideo(videoId);
        if (!Objects.equals(video.getCourseId(), courseId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course video does not exist");
        }
        return toCourseVideoResponse(video);
    }

    public PageResponse<AppBookCategoryResponse> pageBookCategories(AppLearningPageQuery query) {
        Page<BookCategory> page = bookCategoryMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<BookCategory>()
                        .eq(BookCategory::getDeleted, 0)
                        .eq(BookCategory::getStatus, EnabledStatus.ENABLED)
                        .eq(query.categoryId() != null, BookCategory::getParentId, query.categoryId())
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(BookCategory::getCategoryName, query.keyword()))
                        .orderByAsc(BookCategory::getSortOrder)
                        .orderByDesc(BookCategory::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(this::toBookCategoryResponse).toList());
    }

    public PageResponse<AppBookResponse> pageBooks(AppLearningPageQuery query) {
        Page<Book> page = bookMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                visibleBookWrapper()
                        .eq(query.categoryId() != null, Book::getCategoryId, query.categoryId())
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(Book::getBookName, query.keyword())
                                .or()
                                .like(Book::getAuthor, query.keyword()))
                        .orderByAsc("sortOrderAsc".equals(query.sort()), Book::getSortOrder)
                        .orderByDesc(!"sortOrderAsc".equals(query.sort()), Book::getPublishedAt));
        Long studentId = currentStudentId().orElse(null);
        Long userId = currentUserId().orElse(null);
        Map<Long, ResourceInteractionSnapshot> snapshots = loadInteractionSnapshots(userId, RESOURCE_TYPE_BOOK,
                page.getRecords().stream().map(Book::getId).toList());
        return pageResponse(page, page.getRecords().stream()
                .map(book -> toBookResponse(book, false, studentId, snapshots.get(book.getId())))
                .toList());
    }

    public AppBookResponse bookDetail(Long id) {
        Book book = requireVisibleBook(id);
        return toBookResponse(book, true, currentStudentId().orElse(null),
                loadInteractionSnapshot(currentUserId().orElse(null), RESOURCE_TYPE_BOOK, book.getId()));
    }

    public AppBookChapterResponse bookChapterDetail(Long bookId, Long chapterId) {
        requireVisibleBook(bookId);
        BookChapter chapter = requireVisibleBookChapter(chapterId);
        if (!Objects.equals(chapter.getBookId(), bookId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Book chapter does not exist");
        }
        return toBookChapterResponse(chapter);
    }

    public PageResponse<AppPodcastResponse> pagePodcasts(AppLearningPageQuery query) {
        Page<Podcast> page = podcastMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                visiblePodcastWrapper()
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(Podcast::getTitle, query.keyword())
                                .or()
                                .like(Podcast::getSpeakerName, query.keyword()))
                        .orderByAsc("sortOrderAsc".equals(query.sort()), Podcast::getSortOrder)
                        .orderByDesc(!"sortOrderAsc".equals(query.sort()), Podcast::getPublishedAt));
        Long studentId = currentStudentId().orElse(null);
        Long userId = currentUserId().orElse(null);
        Map<Long, ResourceInteractionSnapshot> snapshots = loadInteractionSnapshots(userId, RESOURCE_TYPE_PODCAST,
                page.getRecords().stream().map(Podcast::getId).toList());
        return pageResponse(page, page.getRecords().stream()
                .map(podcast -> toPodcastResponse(podcast, false, studentId, snapshots.get(podcast.getId())))
                .toList());
    }

    public AppPodcastResponse podcastDetail(Long id) {
        Podcast podcast = requireVisiblePodcast(id);
        return toPodcastResponse(podcast, true, currentStudentId().orElse(null),
                loadInteractionSnapshot(currentUserId().orElse(null), RESOURCE_TYPE_PODCAST, podcast.getId()));
    }

    public PageResponse<AppExamPaperResponse> pageExamPapers(AppLearningPageQuery query) {
        Page<ExamPaper> page = examPaperMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<ExamPaper>()
                        .eq(ExamPaper::getDeleted, 0)
                        .eq(ExamPaper::getStatus, EnabledStatus.ENABLED)
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(ExamPaper::getPaperName, query.keyword()))
                        .orderByDesc(ExamPaper::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(paper -> toExamPaperResponse(paper, false)).toList());
    }

    public AppExamPaperResponse examPaperDetail(Long id) {
        return toExamPaperResponse(requireVisibleExamPaper(id), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppExamRecordResponse submitExam(Long paperId, AppExamSubmitRequest request) {
        Student student = requireCurrentStudent();
        ExamPaper paper = requireVisibleExamPaper(paperId);
        List<ExamPaperQuestion> paperQuestions = loadPaperQuestionRelations(paperId);
        if (paperQuestions.isEmpty()) {
            throw new BusinessException(ErrorCode.CONFLICT, "Exam paper has no questions");
        }
        Map<Long, String> submittedAnswers = request.answers().stream()
                .collect(Collectors.toMap(AppExamSubmitRequest.Answer::questionId,
                        answer -> normalizeAnswer(answer.answerContent()),
                        (left, right) -> right));
        LocalDateTime now = LocalDateTime.now();
        ExamRecord record = new ExamRecord();
        record.setStudentId(student.getId());
        record.setPaperId(paperId);
        record.setAssessmentId(null);
        record.setSourceType(request.sourceType());
        record.setSourceId(request.sourceId());
        record.setScore(BigDecimal.ZERO.setScale(2));
        record.setPassed(0);
        record.setStatus(ExamRecordStatus.SUBMITTED);
        record.setSubmitType(ExamSubmitType.NORMAL);
        record.setStartedAt(now);
        record.setSubmittedAt(now);
        record.setLastActiveAt(now);
        record.setLastSubmitRequestId(request.requestId());
        examRecordMapper.insert(record);

        BigDecimal totalScore = BigDecimal.ZERO.setScale(2);
        for (ExamPaperQuestion relation : paperQuestions) {
            Question question = requireVisibleQuestion(relation.getQuestionId());
            String answerContent = submittedAnswers.getOrDefault(question.getId(), "");
            BigDecimal answerScore = gradeAnswer(question, relation.getScore(), answerContent);
            totalScore = totalScore.add(answerScore);
            ExamRecordAnswer answer = new ExamRecordAnswer();
            answer.setExamRecordId(record.getId());
            answer.setQuestionId(question.getId());
            answer.setAnswerContent(answerContent);
            answer.setScore(answerScore);
            answer.setCorrect(answerScore.compareTo(BigDecimal.ZERO) > 0 && answerScore.compareTo(relation.getScore()) == 0 ? 1 : 0);
            examRecordAnswerMapper.insert(answer);
        }
        record.setScore(totalScore.setScale(2, RoundingMode.HALF_UP));
        record.setPassed(record.getScore().compareTo(paper.getPassScore()) >= 0 ? 1 : 0);
        examRecordMapper.updateById(record);
        return toExamRecordResponse(record, true);
    }

    public PageResponse<AppExamRecordResponse> pageExamRecords(long page, long size) {
        Student student = requireCurrentStudent();
        Page<ExamRecord> recordPage = examRecordMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getStudentId, student.getId())
                        .orderByDesc(ExamRecord::getSubmittedAt)
                        .orderByDesc(ExamRecord::getCreatedAt));
        return pageResponse(recordPage, recordPage.getRecords().stream().map(record -> toExamRecordResponse(record, false)).toList());
    }

    public AppExamRecordResponse examRecordDetail(Long recordId) {
        Student student = requireCurrentStudent();
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null || !Objects.equals(record.getStudentId(), student.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Exam record does not exist");
        }
        return toExamRecordResponse(record, true);
    }

    public PageResponse<AppTopicCardResponse> pageTopics(AppLearningPageQuery query) {
        Page<Topic> page = topicMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                visibleTopicWrapper()
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(Topic::getTitle, query.keyword()))
                        .orderByAsc("sortOrderAsc".equals(query.sort()), Topic::getSortOrder)
                        .orderByDesc(!"sortOrderAsc".equals(query.sort()), Topic::getPublishedAt));
        Long userId = currentUserId().orElse(null);
        Map<Long, ResourceInteractionSnapshot> snapshots = loadInteractionSnapshots(userId, RESOURCE_TYPE_TOPIC,
                page.getRecords().stream().map(Topic::getId).toList());
        return pageResponse(page, page.getRecords().stream()
                .map(topic -> toTopicCardResponse(topic, snapshots.get(topic.getId())))
                .toList());
    }

    public AppTopicDetailResponse topicDetail(Long id) {
        Topic topic = requireVisibleTopic(id);
        topic.setViewCount((topic.getViewCount() == null ? 0 : topic.getViewCount()) + 1);
        topicMapper.updateById(topic);
        Long userId = currentUserId().orElse(null);
        Long studentId = currentStudentId().orElse(null);
        return toTopicDetailResponse(topic,
                loadInteractionSnapshot(userId, RESOURCE_TYPE_TOPIC, topic.getId()),
                studentId,
                userId);
    }

    public PageResponse<AppTopicResourceCardResponse> pageTopicSection(Long topicId, String sectionType, long page, long size) {
        Topic topic = requireVisibleTopic(topicId);
        TopicSectionDefinition sectionDefinition = requireTopicSectionDefinition(sectionType);
        Long studentId = currentStudentId().orElse(null);
        Long userId = currentUserId().orElse(null);
        List<AppTopicResourceCardResponse> cards = loadTopicSectionCards(topic.getId(), sectionDefinition.itemType(), studentId, userId);
        long normalizedPage = normalizePage(page);
        long normalizedSize = normalizeSize(size);
        int fromIndex = (int) Math.min(cards.size(), Math.max(0, (normalizedPage - 1) * normalizedSize));
        int toIndex = (int) Math.min(cards.size(), fromIndex + normalizedSize);
        return new PageResponse<>(cards.subList(fromIndex, toIndex), cards.size(), normalizedPage, normalizedSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppLearningRecordResponse syncLearningRecord(AppLearningRecordRequest request) {
        Student student = requireCurrentStudent();
        validateVisibleResource(request.resourceType(), request.resourceId());
        LearningRecord record = learningRecordMapper.selectOne(new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getStudentId, student.getId())
                .eq(LearningRecord::getResourceType, request.resourceType())
                .eq(LearningRecord::getResourceId, request.resourceId())
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (record == null) {
            record = new LearningRecord();
            record.setStudentId(student.getId());
            record.setResourceType(request.resourceType());
            record.setResourceId(request.resourceId());
            record.setStudySeconds(0);
            record.setProgressPercent(ZERO_PROGRESS);
            record.setCompleted(0);
        }
        record.setStudySeconds(Math.max(0, request.studySeconds() == null ? record.getStudySeconds() : request.studySeconds()));
        record.setProgressPercent(normalizeProgress(request.progressPercent() == null ? record.getProgressPercent() : request.progressPercent()));
        boolean completed = Boolean.TRUE.equals(request.completed()) || record.getProgressPercent().compareTo(BigDecimal.valueOf(100)) >= 0;
        record.setCompleted(completed ? 1 : 0);
        record.setCompletedAt(completed && record.getCompletedAt() == null ? now : record.getCompletedAt());
        record.setLastStudiedAt(now);
        record.setUpdatedAt(now);
        if (record.getId() == null) {
            learningRecordMapper.insert(record);
        } else {
            learningRecordMapper.updateById(record);
        }
        return toLearningRecordResponse(record);
    }

    private void validateVisibleResource(String resourceType, Long resourceId) {
        switch (resourceType) {
            case "course" -> requireVisibleCourse(resourceId);
            case "course_video" -> requireVisibleCourseVideo(resourceId);
            case "book" -> requireVisibleBook(resourceId);
            case "book_chapter" -> requireVisibleBookChapter(resourceId);
            case "podcast" -> requireVisiblePodcast(resourceId);
            case "podcast_audio" -> requireVisiblePodcastAudio(resourceId);
            case "topic" -> requireVisibleTopic(resourceId);
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported learning resource type");
        }
    }

    private Optional<Long> currentStudentId() {
        return currentAppUserResolver.currentSession()
                .flatMap(session -> findStudent(session.userId()))
                .map(Student::getId);
    }

    private Student requireCurrentStudent() {
        AppUserSession session = currentAppUserResolver.requireCurrentUser();
        Student student = findStudent(session.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN, "Student certification is required"));
        if (student.getStatus() != EnabledStatus.ENABLED || student.getCertificationStatus() != StudentCertificationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Student certification is not approved");
        }
        return student;
    }

    private Optional<Student> findStudent(Long userId) {
        return Optional.ofNullable(studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
                .eq(Student::getDeleted, 0)
                .last("LIMIT 1")));
    }

    private Course requireVisibleCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null || !isVisible(course.getDeleted(), course.getReviewStatus(), course.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course does not exist");
        }
        return course;
    }

    private CourseVideo requireVisibleCourseVideo(Long id) {
        CourseVideo video = courseVideoMapper.selectById(id);
        if (video == null || !Objects.equals(video.getDeleted(), 0) || video.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course video does not exist");
        }
        requireVisibleCourse(video.getCourseId());
        return video;
    }

    private Book requireVisibleBook(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null || !isVisible(book.getDeleted(), book.getReviewStatus(), book.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Book does not exist");
        }
        return book;
    }

    private BookChapter requireVisibleBookChapter(Long id) {
        BookChapter chapter = bookChapterMapper.selectById(id);
        if (chapter == null || !Objects.equals(chapter.getDeleted(), 0) || chapter.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Book chapter does not exist");
        }
        requireVisibleBook(chapter.getBookId());
        return chapter;
    }

    private Podcast requireVisiblePodcast(Long id) {
        Podcast podcast = podcastMapper.selectById(id);
        if (podcast == null || !isVisible(podcast.getDeleted(), podcast.getReviewStatus(), podcast.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Podcast does not exist");
        }
        return podcast;
    }

    private PodcastAudio requireVisiblePodcastAudio(Long id) {
        PodcastAudio audio = podcastAudioMapper.selectById(id);
        if (audio == null || !Objects.equals(audio.getDeleted(), 0) || audio.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Podcast audio does not exist");
        }
        requireVisiblePodcast(audio.getPodcastId());
        return audio;
    }

    private Topic requireVisibleTopic(Long id) {
        Topic topic = topicMapper.selectById(id);
        if (topic == null || !isVisible(topic.getDeleted(), topic.getReviewStatus(), topic.getPublishStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Topic does not exist");
        }
        return topic;
    }

    private ExamPaper requireVisibleExamPaper(Long id) {
        ExamPaper paper = examPaperMapper.selectById(id);
        if (paper == null || !Objects.equals(paper.getDeleted(), 0) || paper.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Exam paper does not exist");
        }
        return paper;
    }

    private Question requireVisibleQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null || !Objects.equals(question.getDeleted(), 0) || question.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Question does not exist");
        }
        return question;
    }

    private AppCourseResponse toCourseResponse(Course course, boolean includeVideos, Long studentId,
                                               ResourceInteractionSnapshot snapshot) {
        LearningRecord record = findLearningRecord(studentId, RESOURCE_TYPE_COURSE, course.getId()).orElse(null);
        return new AppCourseResponse(course.getId(), course.getCourseName(), course.getSubtitle(), course.getCoverUrl(),
                course.getLecturerName(), course.getLecturerAvatarUrl(), course.getIntroduction(),
                course.getPaperId(), course.getPublishedAt(),
                snapshot.browseCount(), snapshot.favoriteCount(), snapshot.favorited(),
                progress(record), studySeconds(record), includeVideos ? loadCourseVideos(course.getId()) : List.of());
    }

    private List<AppCourseVideoResponse> loadCourseVideos(Long courseId) {
        return courseVideoMapper.selectList(new LambdaQueryWrapper<CourseVideo>()
                        .eq(CourseVideo::getDeleted, 0)
                        .eq(CourseVideo::getCourseId, courseId)
                        .eq(CourseVideo::getStatus, EnabledStatus.ENABLED)
                        .orderByAsc(CourseVideo::getSortOrder)
                        .orderByDesc(CourseVideo::getCreatedAt))
                .stream()
                .map(this::toCourseVideoResponse)
                .toList();
    }

    private AppCourseVideoResponse toCourseVideoResponse(CourseVideo video) {
        return new AppCourseVideoResponse(video.getId(), video.getCourseId(), video.getTitle(), video.getVideoUrl(),
                video.getDurationSeconds(), video.getPaperId(), video.getSortOrder());
    }

    private AppBookCategoryResponse toBookCategoryResponse(BookCategory category) {
        return new AppBookCategoryResponse(category.getId(), category.getParentId(), category.getCategoryName(),
                category.getSortOrder());
    }

    private AppBookResponse toBookResponse(Book book, boolean includeChapters, Long studentId,
                                           ResourceInteractionSnapshot snapshot) {
        LearningRecord record = findLearningRecord(studentId, RESOURCE_TYPE_BOOK, book.getId()).orElse(null);
        return new AppBookResponse(book.getId(), book.getCategoryId(), book.getBookName(), book.getAuthor(),
                book.getPublisher(), book.getCoverUrl(), book.getIntroduction(), book.getTotalPages(), book.getPaperId(),
                book.getPublishedAt(), snapshot.browseCount(), snapshot.favoriteCount(), snapshot.favorited(),
                progress(record), studySeconds(record),
                includeChapters ? loadBookChapters(book.getId()) : List.of());
    }

    private List<AppBookChapterResponse> loadBookChapters(Long bookId) {
        return bookChapterMapper.selectList(new LambdaQueryWrapper<BookChapter>()
                        .eq(BookChapter::getDeleted, 0)
                        .eq(BookChapter::getBookId, bookId)
                        .eq(BookChapter::getStatus, EnabledStatus.ENABLED)
                        .orderByAsc(BookChapter::getSortOrder)
                        .orderByDesc(BookChapter::getCreatedAt))
                .stream()
                .map(this::toBookChapterResponse)
                .toList();
    }

    private AppBookChapterResponse toBookChapterResponse(BookChapter chapter) {
        return new AppBookChapterResponse(chapter.getId(), chapter.getBookId(), chapter.getParentId(),
                chapter.getChapterTitle(), chapter.getContent(), chapter.getStartPage(), chapter.getPageCount(),
                chapter.getPaperId(), chapter.getSortOrder());
    }

    private AppPodcastResponse toPodcastResponse(Podcast podcast, boolean includeAudios, Long studentId,
                                                 ResourceInteractionSnapshot snapshot) {
        LearningRecord record = findLearningRecord(studentId, RESOURCE_TYPE_PODCAST, podcast.getId()).orElse(null);
        return new AppPodcastResponse(podcast.getId(), podcast.getTitle(), podcast.getSummary(), podcast.getCoverUrl(),
                podcast.getSpeakerName(), resourceTagService.loadTagNames(RESOURCE_TYPE_PODCAST, podcast.getId()),
                podcast.getPublishedAt(), snapshot.browseCount(), snapshot.favoriteCount(), snapshot.favorited(),
                progress(record), studySeconds(record),
                includeAudios ? loadPodcastAudios(podcast.getId()) : List.of());
    }

    private List<AppPodcastAudioResponse> loadPodcastAudios(Long podcastId) {
        return podcastAudioMapper.selectList(new LambdaQueryWrapper<PodcastAudio>()
                        .eq(PodcastAudio::getDeleted, 0)
                        .eq(PodcastAudio::getPodcastId, podcastId)
                        .eq(PodcastAudio::getStatus, EnabledStatus.ENABLED)
                        .orderByAsc(PodcastAudio::getSortOrder)
                        .orderByDesc(PodcastAudio::getCreatedAt))
                .stream()
                .map(audio -> new AppPodcastAudioResponse(audio.getId(), audio.getPodcastId(), audio.getTitle(),
                        audio.getAudioUrl(), audio.getDurationSeconds(), audio.getPaperId(), audio.getSortOrder()))
                .toList();
    }

    private AppExamPaperResponse toExamPaperResponse(ExamPaper paper, boolean includeQuestions) {
        return new AppExamPaperResponse(paper.getId(), paper.getPaperName(), paper.getDescription(), paper.getTotalScore(),
                paper.getPassScore(), paper.getDurationMinutes(), paper.getStatus(),
                includeQuestions ? loadExamQuestions(paper.getId()) : List.of());
    }

    private List<AppExamQuestionResponse> loadExamQuestions(Long paperId) {
        return loadPaperQuestionRelations(paperId).stream()
                .map(this::toExamQuestionResponse)
                .toList();
    }

    private List<ExamPaperQuestion> loadPaperQuestionRelations(Long paperId) {
        return examPaperQuestionMapper.selectList(new LambdaQueryWrapper<ExamPaperQuestion>()
                .eq(ExamPaperQuestion::getPaperId, paperId)
                .orderByAsc(ExamPaperQuestion::getSortOrder)
                .orderByAsc(ExamPaperQuestion::getId));
    }

    private AppExamQuestionResponse toExamQuestionResponse(ExamPaperQuestion relation) {
        Question question = requireVisibleQuestion(relation.getQuestionId());
        return new AppExamQuestionResponse(question.getId(), question.getQuestionType(), question.getTitle(),
                question.getDifficulty(), relation.getScore(), relation.getSortOrder(), loadExamQuestionOptions(question.getId()));
    }

    private List<AppExamQuestionOptionResponse> loadExamQuestionOptions(Long questionId) {
        return questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
                        .eq(QuestionOption::getQuestionId, questionId)
                        .orderByAsc(QuestionOption::getSortOrder)
                        .orderByAsc(QuestionOption::getOptionKey))
                .stream()
                .map(option -> new AppExamQuestionOptionResponse(option.getId(), option.getOptionKey(), option.getOptionContent(), option.getSortOrder()))
                .toList();
    }

    private AppExamRecordResponse toExamRecordResponse(ExamRecord record, boolean includeAnswers) {
        ExamPaper paper = examPaperMapper.selectById(record.getPaperId());
        return new AppExamRecordResponse(record.getId(), record.getStudentId(), record.getPaperId(), record.getAssessmentId(),
                paper == null ? null : paper.getPaperName(), record.getSourceType(), record.getSourceId(),
                record.getScore(), record.getPassed(), record.getStatus(), record.getSubmitType(),
                record.getStartedAt(), record.getSubmittedAt(), record.getLastActiveAt(),
                includeAnswers ? loadExamAnswerResults(record.getId()) : List.of());
    }

    private List<AppExamAnswerResultResponse> loadExamAnswerResults(Long recordId) {
        return examRecordAnswerMapper.selectList(new LambdaQueryWrapper<ExamRecordAnswer>()
                        .eq(ExamRecordAnswer::getExamRecordId, recordId)
                        .orderByAsc(ExamRecordAnswer::getId))
                .stream()
                .map(this::toExamAnswerResultResponse)
                .toList();
    }

    private AppExamAnswerResultResponse toExamAnswerResultResponse(ExamRecordAnswer answer) {
        Question question = questionMapper.selectById(answer.getQuestionId());
        if (question == null) {
            return new AppExamAnswerResultResponse(answer.getQuestionId(), null, null, answer.getAnswerContent(),
                    null, null, answer.getScore(), answer.getCorrect(), List.of());
        }
        return new AppExamAnswerResultResponse(question.getId(), question.getQuestionType(), question.getTitle(),
                answer.getAnswerContent(), correctAnswer(question.getId()), question.getAnalysis(), answer.getScore(),
                answer.getCorrect(), loadExamQuestionOptions(question.getId()));
    }

    private BigDecimal gradeAnswer(Question question, BigDecimal score, String answerContent) {
        if (question.getQuestionType() == QuestionType.SHORT_ANSWER) {
            return BigDecimal.ZERO.setScale(2);
        }
        Set<String> submitted = answerSet(answerContent);
        Set<String> correct = correctAnswerSet(question.getId());
        if (!correct.isEmpty() && submitted.equals(correct)) {
            return score.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2);
    }

    private String correctAnswer(Long questionId) {
        return correctAnswerSet(questionId).stream()
                .sorted()
                .collect(Collectors.joining(","));
    }

    private Set<String> correctAnswerSet(Long questionId) {
        return questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
                        .eq(QuestionOption::getQuestionId, questionId)
                        .eq(QuestionOption::getCorrect, 1))
                .stream()
                .map(QuestionOption::getOptionKey)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private Set<String> answerSet(String answerContent) {
        return Arrays.stream(normalizeAnswer(answerContent).split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    private String normalizeAnswer(String answerContent) {
        if (answerContent == null) {
            return "";
        }
        return Arrays.stream(answerContent.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining(","));
    }

    private AppTopicCardResponse toTopicCardResponse(Topic topic, ResourceInteractionSnapshot snapshot) {
        ResourceInteractionSnapshot resolvedSnapshot = snapshot == null ? ResourceInteractionSnapshot.empty() : snapshot;
        return new AppTopicCardResponse(topic.getId(), topic.getTitle(), topic.getSummary(), topic.getLearningRequirements(),
                topic.getCoverUrl(), resourceTagService.loadTagNames(RESOURCE_TYPE_TOPIC, topic.getId()),
                topic.getViewCount() == null ? 0L : topic.getViewCount(), topic.getPublishedAt(),
                resolvedSnapshot.favoriteCount(), resolvedSnapshot.favorited());
    }

    private AppTopicDetailResponse toTopicDetailResponse(Topic topic, ResourceInteractionSnapshot snapshot,
                                                         Long studentId, Long userId) {
        ResourceInteractionSnapshot resolvedSnapshot = snapshot == null ? ResourceInteractionSnapshot.empty() : snapshot;
        List<AppTopicSectionResponse> sections = List.of(
                buildTopicSectionResponse(topic.getId(), TOPIC_SECTION_LEARNING, studentId, userId),
                buildTopicSectionResponse(topic.getId(), TOPIC_SECTION_VIDEO, studentId, userId),
                buildTopicSectionResponse(topic.getId(), TOPIC_SECTION_AUDIO, studentId, userId)
        );
        return new AppTopicDetailResponse(topic.getId(), topic.getTitle(), topic.getSummary(),
                topic.getLearningRequirements(), topic.getCoverUrl(),
                resourceTagService.loadTagNames(RESOURCE_TYPE_TOPIC, topic.getId()),
                topic.getViewCount() == null ? 0L : topic.getViewCount(), topic.getPublishedAt(),
                resolvedSnapshot.favoriteCount(), resolvedSnapshot.favorited(), sections);
    }

    private AppTopicSectionResponse buildTopicSectionResponse(Long topicId, String sectionType, Long studentId, Long userId) {
        TopicSectionDefinition sectionDefinition = requireTopicSectionDefinition(sectionType);
        List<AppTopicResourceCardResponse> cards = loadTopicSectionCards(topicId, sectionDefinition.itemType(), studentId, userId);
        int previewSize = Math.min(cards.size(), TOPIC_SECTION_PREVIEW_SIZE);
        return new AppTopicSectionResponse(sectionDefinition.sectionType(), sectionDefinition.sectionLabel(),
                (long) cards.size(), cards.size() > TOPIC_SECTION_PREVIEW_SIZE, cards.subList(0, previewSize));
    }

    private TopicSectionDefinition requireTopicSectionDefinition(String sectionType) {
        String normalizedSectionType = sectionType == null ? "" : sectionType.trim().toLowerCase(Locale.ROOT);
        return switch (normalizedSectionType) {
            case TOPIC_SECTION_LEARNING -> new TopicSectionDefinition(TOPIC_SECTION_LEARNING, "学习", RESOURCE_TYPE_BOOK);
            case TOPIC_SECTION_VIDEO -> new TopicSectionDefinition(TOPIC_SECTION_VIDEO, "视频", RESOURCE_TYPE_COURSE);
            case TOPIC_SECTION_AUDIO -> new TopicSectionDefinition(TOPIC_SECTION_AUDIO, "音频", RESOURCE_TYPE_PODCAST);
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported topic sectionType: " + sectionType);
        };
    }

    private List<AppTopicResourceCardResponse> loadTopicSectionCards(Long topicId, String itemType, Long studentId, Long userId) {
        List<Long> itemIds = topicItemMapper.selectList(new LambdaQueryWrapper<TopicItem>()
                        .eq(TopicItem::getTopicId, topicId)
                        .eq(TopicItem::getItemType, itemType)
                        .orderByAsc(TopicItem::getSortOrder)
                        .orderByAsc(TopicItem::getId))
                .stream()
                .map(TopicItem::getItemId)
                .filter(Objects::nonNull)
                .toList();
        if (itemIds.isEmpty()) {
            return List.of();
        }
        Map<Long, ResourceInteractionSnapshot> snapshots = loadInteractionSnapshots(userId, itemType, itemIds);
        Map<Long, LearningRecord> learningRecords = loadLearningRecordMap(studentId, itemType, itemIds);
        return switch (itemType) {
            case RESOURCE_TYPE_COURSE -> buildCourseTopicCards(itemIds, snapshots, learningRecords);
            case RESOURCE_TYPE_BOOK -> buildBookTopicCards(itemIds, snapshots, learningRecords);
            case RESOURCE_TYPE_PODCAST -> buildPodcastTopicCards(itemIds, snapshots, learningRecords);
            default -> List.of();
        };
    }

    private List<AppTopicResourceCardResponse> buildCourseTopicCards(List<Long> itemIds,
                                                                     Map<Long, ResourceInteractionSnapshot> snapshots,
                                                                     Map<Long, LearningRecord> learningRecords) {
        Map<Long, Course> courses = courseMapper.selectList(visibleCourseWrapper().in(Course::getId, itemIds))
                .stream()
                .collect(Collectors.toMap(Course::getId, course -> course, (left, right) -> left));
        return itemIds.stream()
                .map(courses::get)
                .filter(Objects::nonNull)
                .map(course -> new AppTopicResourceCardResponse(
                        RESOURCE_TYPE_COURSE,
                        TOPIC_RESOURCE_TYPE_LABELS.get(RESOURCE_TYPE_COURSE),
                        course.getId(),
                        course.getCourseName(),
                        course.getSubtitle(),
                        course.getCoverUrl(),
                        List.of(),
                        snapshotOf(snapshots, course.getId()).browseCount(),
                        snapshotOf(snapshots, course.getId()).favoriteCount(),
                        snapshotOf(snapshots, course.getId()).favorited(),
                        progress(learningRecords.get(course.getId())),
                        studySeconds(learningRecords.get(course.getId()))
                ))
                .toList();
    }

    private List<AppTopicResourceCardResponse> buildBookTopicCards(List<Long> itemIds,
                                                                   Map<Long, ResourceInteractionSnapshot> snapshots,
                                                                   Map<Long, LearningRecord> learningRecords) {
        Map<Long, Book> books = bookMapper.selectList(visibleBookWrapper().in(Book::getId, itemIds))
                .stream()
                .collect(Collectors.toMap(Book::getId, book -> book, (left, right) -> left));
        return itemIds.stream()
                .map(books::get)
                .filter(Objects::nonNull)
                .map(book -> new AppTopicResourceCardResponse(
                        RESOURCE_TYPE_BOOK,
                        TOPIC_RESOURCE_TYPE_LABELS.get(RESOURCE_TYPE_BOOK),
                        book.getId(),
                        book.getBookName(),
                        hasText(book.getAuthor()) ? book.getAuthor() : book.getPublisher(),
                        book.getCoverUrl(),
                        List.of(),
                        snapshotOf(snapshots, book.getId()).browseCount(),
                        snapshotOf(snapshots, book.getId()).favoriteCount(),
                        snapshotOf(snapshots, book.getId()).favorited(),
                        progress(learningRecords.get(book.getId())),
                        studySeconds(learningRecords.get(book.getId()))
                ))
                .toList();
    }

    private List<AppTopicResourceCardResponse> buildPodcastTopicCards(List<Long> itemIds,
                                                                      Map<Long, ResourceInteractionSnapshot> snapshots,
                                                                      Map<Long, LearningRecord> learningRecords) {
        Map<Long, Podcast> podcasts = podcastMapper.selectList(visiblePodcastWrapper().in(Podcast::getId, itemIds))
                .stream()
                .collect(Collectors.toMap(Podcast::getId, podcast -> podcast, (left, right) -> left));
        return itemIds.stream()
                .map(podcasts::get)
                .filter(Objects::nonNull)
                .map(podcast -> new AppTopicResourceCardResponse(
                        RESOURCE_TYPE_PODCAST,
                        TOPIC_RESOURCE_TYPE_LABELS.get(RESOURCE_TYPE_PODCAST),
                        podcast.getId(),
                        podcast.getTitle(),
                        podcast.getSpeakerName(),
                        podcast.getCoverUrl(),
                        resourceTagService.loadTagNames(RESOURCE_TYPE_PODCAST, podcast.getId()),
                        snapshotOf(snapshots, podcast.getId()).browseCount(),
                        snapshotOf(snapshots, podcast.getId()).favoriteCount(),
                        snapshotOf(snapshots, podcast.getId()).favorited(),
                        progress(learningRecords.get(podcast.getId())),
                        studySeconds(learningRecords.get(podcast.getId()))
                ))
                .toList();
    }

    private Map<Long, ResourceInteractionSnapshot> loadInteractionSnapshots(Long userId, String resourceType, List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> browseCounts = loadBrowseCounts(resourceType, resourceIds);
        Map<Long, Long> favoriteCounts = loadFavoriteCounts(resourceType, resourceIds);
        Set<Long> favoritedIds = loadFavoritedIds(userId, resourceType, resourceIds);
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

    private ResourceInteractionSnapshot loadInteractionSnapshot(Long userId, String resourceType, Long resourceId) {
        return loadInteractionSnapshots(userId, resourceType, List.of(resourceId))
                .getOrDefault(resourceId, ResourceInteractionSnapshot.empty());
    }

    private Map<Long, Long> loadBrowseCounts(String resourceType, List<Long> resourceIds) {
        return userBrowseHistoryMapper.selectList(new LambdaQueryWrapper<UserBrowseHistory>()
                        .eq(UserBrowseHistory::getResourceType, resourceType)
                        .in(UserBrowseHistory::getResourceId, resourceIds))
                .stream()
                .collect(Collectors.groupingBy(UserBrowseHistory::getResourceId,
                        Collectors.summingLong(history -> history.getViewCount() == null ? 0 : history.getViewCount())));
    }

    private Map<Long, Long> loadFavoriteCounts(String resourceType, List<Long> resourceIds) {
        return userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getResourceType, resourceType)
                        .in(UserFavorite::getResourceId, resourceIds))
                .stream()
                .collect(Collectors.groupingBy(UserFavorite::getResourceId, Collectors.counting()));
    }

    private Set<Long> loadFavoritedIds(Long userId, String resourceType, List<Long> resourceIds) {
        if (userId == null) {
            return Set.of();
        }
        return userFavoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getResourceType, resourceType)
                        .in(UserFavorite::getResourceId, resourceIds))
                .stream()
                .map(UserFavorite::getResourceId)
                .collect(Collectors.toSet());
    }

    private Map<Long, LearningRecord> loadLearningRecordMap(Long studentId, String resourceType, List<Long> resourceIds) {
        if (studentId == null || resourceIds == null || resourceIds.isEmpty()) {
            return Map.of();
        }
        return learningRecordMapper.selectList(new LambdaQueryWrapper<LearningRecord>()
                        .eq(LearningRecord::getStudentId, studentId)
                        .eq(LearningRecord::getResourceType, resourceType)
                        .in(LearningRecord::getResourceId, resourceIds))
                .stream()
                .collect(Collectors.toMap(LearningRecord::getResourceId, record -> record, (left, right) -> left));
    }

    private ResourceInteractionSnapshot snapshotOf(Map<Long, ResourceInteractionSnapshot> snapshots, Long resourceId) {
        return snapshots.getOrDefault(resourceId, ResourceInteractionSnapshot.empty());
    }

    private Optional<LearningRecord> findLearningRecord(Long studentId, String resourceType, Long resourceId) {
        if (studentId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(learningRecordMapper.selectOne(new LambdaQueryWrapper<LearningRecord>()
                .eq(LearningRecord::getStudentId, studentId)
                .eq(LearningRecord::getResourceType, resourceType)
                .eq(LearningRecord::getResourceId, resourceId)
                .last("LIMIT 1")));
    }

    private AppLearningRecordResponse toLearningRecordResponse(LearningRecord record) {
        return new AppLearningRecordResponse(record.getId(), record.getStudentId(), record.getResourceType(),
                record.getResourceId(), record.getStudySeconds(), record.getProgressPercent(), record.getCompleted(),
                record.getCompletedAt(), record.getLastStudiedAt());
    }

    private BigDecimal progress(LearningRecord record) {
        return record == null || record.getProgressPercent() == null ? ZERO_PROGRESS : record.getProgressPercent();
    }

    private Integer studySeconds(LearningRecord record) {
        return record == null || record.getStudySeconds() == null ? 0 : record.getStudySeconds();
    }

    private Optional<Long> currentUserId() {
        return currentAppUserResolver.currentSession().map(AppUserSession::userId);
    }

    private BigDecimal normalizeProgress(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return ZERO_PROGRESS;
        }
        if (value.compareTo(BigDecimal.valueOf(100)) > 0) {
            return BigDecimal.valueOf(100).setScale(2);
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private boolean isVisible(Integer deleted, ReviewStatus reviewStatus, PublishStatus publishStatus) {
        return Objects.equals(deleted, 0)
                && reviewStatus == ReviewStatus.APPROVED
                && publishStatus == PublishStatus.PUBLISHED;
    }

    private LambdaQueryWrapper<Course> visibleCourseWrapper() {
        return new LambdaQueryWrapper<Course>()
                .eq(Course::getDeleted, 0)
                .eq(Course::getReviewStatus, ReviewStatus.APPROVED)
                .eq(Course::getPublishStatus, PublishStatus.PUBLISHED);
    }

    private LambdaQueryWrapper<Book> visibleBookWrapper() {
        return new LambdaQueryWrapper<Book>()
                .eq(Book::getDeleted, 0)
                .eq(Book::getReviewStatus, ReviewStatus.APPROVED)
                .eq(Book::getPublishStatus, PublishStatus.PUBLISHED);
    }

    private LambdaQueryWrapper<Podcast> visiblePodcastWrapper() {
        return new LambdaQueryWrapper<Podcast>()
                .eq(Podcast::getDeleted, 0)
                .eq(Podcast::getReviewStatus, ReviewStatus.APPROVED)
                .eq(Podcast::getPublishStatus, PublishStatus.PUBLISHED);
    }

    private LambdaQueryWrapper<Topic> visibleTopicWrapper() {
        return new LambdaQueryWrapper<Topic>()
                .eq(Topic::getDeleted, 0)
                .eq(Topic::getReviewStatus, ReviewStatus.APPROVED)
                .eq(Topic::getPublishStatus, PublishStatus.PUBLISHED);
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

    private record ResourceInteractionSnapshot(Long browseCount, Long favoriteCount, Boolean favorited) {

        private static ResourceInteractionSnapshot empty() {
            return new ResourceInteractionSnapshot(ZERO_COUNT, ZERO_COUNT, false);
        }
    }

    private record TopicSectionDefinition(String sectionType, String sectionLabel, String itemType) {
    }
}
