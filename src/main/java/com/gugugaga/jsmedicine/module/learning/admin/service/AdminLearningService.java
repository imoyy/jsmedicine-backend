package com.gugugaga.jsmedicine.module.learning.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.infrastructure.storage.service.StableCoverUrlService;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminLearningPageQuery;
import com.gugugaga.jsmedicine.module.learning.admin.dto.BookCategoryBookBindingRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.BookCategoryBookResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.BookCategoryRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.BookCategoryResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.BookChapterRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.BookChapterResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.BookRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.BookResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.CourseRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.CourseResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.CourseVideoRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.CourseVideoResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.ExamPaperQuestionRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.ExamPaperQuestionResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.ExamPaperRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.ExamPaperResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.LearningReviewRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.QuestionCategoryRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.QuestionCategoryResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.QuestionOptionRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.QuestionOptionResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.QuestionRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.QuestionResponse;
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
import com.gugugaga.jsmedicine.module.learning.question.entity.ExamPaper;
import com.gugugaga.jsmedicine.module.learning.question.entity.ExamPaperQuestion;
import com.gugugaga.jsmedicine.module.learning.question.entity.Question;
import com.gugugaga.jsmedicine.module.learning.question.entity.QuestionCategory;
import com.gugugaga.jsmedicine.module.learning.question.entity.QuestionOption;
import com.gugugaga.jsmedicine.module.learning.question.mapper.ExamPaperMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.ExamPaperQuestionMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.QuestionCategoryMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.QuestionMapper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.QuestionOptionMapper;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.service.AuditRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AdminLearningService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final CourseMapper courseMapper;
    private final CourseVideoMapper courseVideoMapper;
    private final BookCategoryMapper bookCategoryMapper;
    private final BookMapper bookMapper;
    private final BookChapterMapper bookChapterMapper;
    private final QuestionCategoryMapper questionCategoryMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final ExamPaperMapper examPaperMapper;
    private final ExamPaperQuestionMapper examPaperQuestionMapper;
    private final AuditRecordService auditRecordService;
    private final CurrentAdminAccessor currentAdminAccessor;
    private final StableCoverUrlService stableCoverUrlService;

    public AdminLearningService(
            CourseMapper courseMapper,
            CourseVideoMapper courseVideoMapper,
            BookCategoryMapper bookCategoryMapper,
            BookMapper bookMapper,
            BookChapterMapper bookChapterMapper,
            QuestionCategoryMapper questionCategoryMapper,
            QuestionMapper questionMapper,
            QuestionOptionMapper questionOptionMapper,
            ExamPaperMapper examPaperMapper,
            ExamPaperQuestionMapper examPaperQuestionMapper,
            AuditRecordService auditRecordService,
            CurrentAdminAccessor currentAdminAccessor,
            StableCoverUrlService stableCoverUrlService
    ) {
        this.courseMapper = courseMapper;
        this.courseVideoMapper = courseVideoMapper;
        this.bookCategoryMapper = bookCategoryMapper;
        this.bookMapper = bookMapper;
        this.bookChapterMapper = bookChapterMapper;
        this.questionCategoryMapper = questionCategoryMapper;
        this.questionMapper = questionMapper;
        this.questionOptionMapper = questionOptionMapper;
        this.examPaperMapper = examPaperMapper;
        this.examPaperQuestionMapper = examPaperQuestionMapper;
        this.auditRecordService = auditRecordService;
        this.currentAdminAccessor = currentAdminAccessor;
        this.stableCoverUrlService = stableCoverUrlService;
    }

    public PageResponse<CourseResponse> pageCourses(AdminLearningPageQuery query) {
        Page<Course> page = courseMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<Course>()
                        .eq(Course::getDeleted, 0)
                        .eq(query.reviewStatus() != null, Course::getReviewStatus, query.reviewStatus())
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(Course::getCourseName, query.keyword())
                                .or()
                                .like(Course::getLecturerName, query.keyword()))
                        .orderByAsc("sortOrderAsc".equals(query.sort()), Course::getSortOrder)
                        .orderByDesc(!"sortOrderAsc".equals(query.sort()), Course::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(this::toCourseResponse).toList());
    }

    public CourseResponse courseDetail(Long id) {
        return toCourseResponse(requireCourse(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseResponse createCourse(CourseRequest request) {
        Course course = new Course();
        fillCourse(course, request);
        course.setDeleted(0);
        courseMapper.insert(course);
        return toCourseResponse(course);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = requireCourse(id);
        fillCourse(course, request);
        courseMapper.updateById(course);
        return toCourseResponse(course);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseResponse reviewCourse(Long id, LearningReviewRequest request) {
        Course course = requireCourse(id);
        ReviewStatus before = course.getReviewStatus();
        applyReview(course, request.reviewStatus());
        courseMapper.updateById(course);
        saveAudit("course", id, before, request.reviewStatus(), request.comment());
        return toCourseResponse(course);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long id) {
        requireCourse(id);
        courseMapper.deleteById(id);
    }

    public PageResponse<CourseVideoResponse> pageCourseVideos(Long courseId, long page, long size) {
        Page<CourseVideo> videoPage = courseVideoMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<CourseVideo>()
                        .eq(CourseVideo::getDeleted, 0)
                        .eq(courseId != null, CourseVideo::getCourseId, courseId)
                        .orderByAsc(CourseVideo::getSortOrder)
                        .orderByDesc(CourseVideo::getCreatedAt));
        return pageResponse(videoPage, videoPage.getRecords().stream().map(this::toCourseVideoResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseVideoResponse createCourseVideo(CourseVideoRequest request) {
        requireCourse(request.courseId());
        CourseVideo video = new CourseVideo();
        fillCourseVideo(video, request);
        video.setDeleted(0);
        courseVideoMapper.insert(video);
        return toCourseVideoResponse(video);
    }

    @Transactional(rollbackFor = Exception.class)
    public CourseVideoResponse updateCourseVideo(Long id, CourseVideoRequest request) {
        requireCourseVideo(id);
        requireCourse(request.courseId());
        CourseVideo video = courseVideoMapper.selectById(id);
        fillCourseVideo(video, request);
        courseVideoMapper.updateById(video);
        return toCourseVideoResponse(video);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCourseVideo(Long id) {
        requireCourseVideo(id);
        courseVideoMapper.deleteById(id);
    }

    public PageResponse<BookCategoryResponse> pageBookCategories(AdminLearningPageQuery query) {
        Page<BookCategory> page = bookCategoryMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<BookCategory>()
                        .eq(BookCategory::getDeleted, 0)
                        .eq(query.categoryId() != null, BookCategory::getParentId, query.categoryId())
                        .eq(query.status() != null, BookCategory::getStatus, query.status())
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(BookCategory::getCategoryName, query.keyword()))
                        .orderByAsc(BookCategory::getSortOrder)
                        .orderByDesc(BookCategory::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(this::toBookCategoryResponse).toList());
    }

    public BookCategoryResponse bookCategoryDetail(Long id) {
        return toBookCategoryResponse(requireBookCategory(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public BookCategoryResponse createBookCategory(BookCategoryRequest request) {
        if (request.parentId() != null) {
            requireBookCategory(request.parentId());
        }
        BookCategory category = new BookCategory();
        fillBookCategory(category, request);
        category.setDeleted(0);
        bookCategoryMapper.insert(category);
        return toBookCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookCategoryResponse updateBookCategory(Long id, BookCategoryRequest request) {
        BookCategory category = requireBookCategory(id);
        if (request.parentId() != null) {
            requireBookCategory(request.parentId());
        }
        fillBookCategory(category, request);
        bookCategoryMapper.updateById(category);
        return toBookCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBookCategory(Long id) {
        requireBookCategory(id);
        bookCategoryMapper.deleteById(id);
    }

    public PageResponse<BookCategoryBookResponse> pageBooksByCategory(Long categoryId, long page, long size, String sort, String keyword) {
        requireBookCategory(categoryId);
        Page<Book> bookPage = bookMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<Book>()
                        .eq(Book::getDeleted, 0)
                        .eq(Book::getCategoryId, categoryId)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(Book::getBookName, keyword)
                                .or()
                                .like(Book::getAuthor, keyword))
                        .orderByAsc("sortOrderAsc".equals(sort), Book::getSortOrder)
                        .orderByDesc(!"sortOrderAsc".equals(sort), Book::getUpdatedAt));
        return pageResponse(bookPage, bookPage.getRecords().stream().map(this::toBookCategoryBookResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void addBooksToCategory(Long categoryId, BookCategoryBookBindingRequest request) {
        requireBookCategory(categoryId);
        for (Long bookId : normalizeBindingBookIds(request.bookIds())) {
            Book book = requireBook(bookId);
            book.setCategoryId(categoryId);
            bookMapper.updateById(book);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeBooksFromCategory(Long categoryId, BookCategoryBookBindingRequest request) {
        requireBookCategory(categoryId);
        for (Long bookId : normalizeBindingBookIds(request.bookIds())) {
            Book book = requireBook(bookId);
            if (!Objects.equals(book.getCategoryId(), categoryId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Book does not belong to current category");
            }
            book.setCategoryId(null);
            bookMapper.updateById(book);
        }
    }

    public PageResponse<BookResponse> pageBooks(AdminLearningPageQuery query) {
        Page<Book> page = bookMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<Book>()
                        .eq(Book::getDeleted, 0)
                        .eq(query.categoryId() != null, Book::getCategoryId, query.categoryId())
                        .eq(query.reviewStatus() != null, Book::getReviewStatus, query.reviewStatus())
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(Book::getBookName, query.keyword())
                                .or()
                                .like(Book::getAuthor, query.keyword()))
                        .orderByAsc("sortOrderAsc".equals(query.sort()), Book::getSortOrder)
                        .orderByDesc(!"sortOrderAsc".equals(query.sort()), Book::getCreatedAt));
        Map<Long, String> paperNameMap = loadExamPaperNameMap(page.getRecords().stream()
                .map(Book::getPaperId)
                .filter(Objects::nonNull)
                .toList());
        return pageResponse(page, page.getRecords().stream()
                .map(book -> toBookResponse(book, paperNameMap.get(book.getPaperId())))
                .toList());
    }

    public BookResponse bookDetail(Long id) {
        Book book = requireBook(id);
        return toBookResponse(book, loadExamPaperName(book.getPaperId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public BookResponse createBook(BookRequest request) {
        if (request.categoryId() != null) {
            requireBookCategory(request.categoryId());
        }
        if (request.paperId() != null) {
            requireExamPaper(request.paperId());
        }
        Book book = new Book();
        fillBook(book, request);
        book.setDeleted(0);
        bookMapper.insert(book);
        return toBookResponse(book, loadExamPaperName(book.getPaperId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = requireBook(id);
        if (request.categoryId() != null) {
            requireBookCategory(request.categoryId());
        }
        if (request.paperId() != null) {
            requireExamPaper(request.paperId());
        }
        fillBook(book, request);
        bookMapper.updateById(book);
        return toBookResponse(book, loadExamPaperName(book.getPaperId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public BookResponse reviewBook(Long id, LearningReviewRequest request) {
        Book book = requireBook(id);
        ReviewStatus before = book.getReviewStatus();
        applyReview(book, request.reviewStatus());
        bookMapper.updateById(book);
        saveAudit("book", id, before, request.reviewStatus(), request.comment());
        return toBookResponse(book, loadExamPaperName(book.getPaperId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long id) {
        requireBook(id);
        bookMapper.deleteById(id);
    }

    public PageResponse<BookChapterResponse> pageBookChapters(Long bookId, long page, long size) {
        Page<BookChapter> chapterPage = bookChapterMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<BookChapter>()
                        .eq(BookChapter::getDeleted, 0)
                        .eq(bookId != null, BookChapter::getBookId, bookId)
                        .orderByAsc(BookChapter::getSortOrder)
                        .orderByDesc(BookChapter::getCreatedAt));
        return pageResponse(chapterPage, chapterPage.getRecords().stream().map(this::toBookChapterResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public BookChapterResponse createBookChapter(BookChapterRequest request) {
        requireBook(request.bookId());
        BookChapter chapter = new BookChapter();
        fillBookChapter(chapter, request);
        chapter.setDeleted(0);
        bookChapterMapper.insert(chapter);
        return toBookChapterResponse(chapter);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookChapterResponse updateBookChapter(Long id, BookChapterRequest request) {
        requireBookChapter(id);
        requireBook(request.bookId());
        BookChapter chapter = bookChapterMapper.selectById(id);
        fillBookChapter(chapter, request);
        bookChapterMapper.updateById(chapter);
        return toBookChapterResponse(chapter);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteBookChapter(Long id) {
        requireBookChapter(id);
        bookChapterMapper.deleteById(id);
    }

    public PageResponse<QuestionCategoryResponse> pageQuestionCategories(AdminLearningPageQuery query) {
        Page<QuestionCategory> page = questionCategoryMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<QuestionCategory>()
                        .eq(QuestionCategory::getDeleted, 0)
                        .eq(query.categoryId() != null, QuestionCategory::getParentId, query.categoryId())
                        .eq(query.status() != null, QuestionCategory::getStatus, query.status())
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(QuestionCategory::getCategoryName, query.keyword()))
                        .orderByAsc(QuestionCategory::getSortOrder)
                        .orderByDesc(QuestionCategory::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(this::toQuestionCategoryResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public QuestionCategoryResponse createQuestionCategory(QuestionCategoryRequest request) {
        if (request.parentId() != null) {
            requireQuestionCategory(request.parentId());
        }
        QuestionCategory category = new QuestionCategory();
        fillQuestionCategory(category, request);
        category.setDeleted(0);
        questionCategoryMapper.insert(category);
        return toQuestionCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public QuestionCategoryResponse updateQuestionCategory(Long id, QuestionCategoryRequest request) {
        QuestionCategory category = requireQuestionCategory(id);
        if (request.parentId() != null) {
            requireQuestionCategory(request.parentId());
        }
        fillQuestionCategory(category, request);
        questionCategoryMapper.updateById(category);
        return toQuestionCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestionCategory(Long id) {
        requireQuestionCategory(id);
        questionCategoryMapper.deleteById(id);
    }

    public PageResponse<QuestionResponse> pageQuestions(AdminLearningPageQuery query) {
        Page<Question> page = questionMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<Question>()
                        .eq(Question::getDeleted, 0)
                        .eq(query.categoryId() != null, Question::getCategoryId, query.categoryId())
                        .eq(query.status() != null, Question::getStatus, query.status())
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(Question::getTitle, query.keyword()))
                        .orderByDesc(Question::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(question -> toQuestionResponse(question, false)).toList());
    }

    public QuestionResponse questionDetail(Long id) {
        return toQuestionResponse(requireQuestion(id), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse createQuestion(QuestionRequest request) {
        if (request.categoryId() != null) {
            requireQuestionCategory(request.categoryId());
        }
        Question question = new Question();
        fillQuestion(question, request);
        question.setDeleted(0);
        questionMapper.insert(question);
        replaceQuestionOptions(question.getId(), request.options());
        return toQuestionResponse(question, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        Question question = requireQuestion(id);
        if (request.categoryId() != null) {
            requireQuestionCategory(request.categoryId());
        }
        fillQuestion(question, request);
        questionMapper.updateById(question);
        replaceQuestionOptions(question.getId(), request.options());
        return toQuestionResponse(question, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<QuestionOptionResponse> replaceQuestionOptions(Long questionId, List<QuestionOptionRequest> requests) {
        requireQuestion(questionId);
        questionOptionMapper.delete(new LambdaQueryWrapper<QuestionOption>().eq(QuestionOption::getQuestionId, questionId));
        if (requests != null) {
            requests.forEach(request -> {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(questionId);
                fillQuestionOption(option, request);
                questionOptionMapper.insert(option);
            });
        }
        return loadQuestionOptions(questionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        requireQuestion(id);
        questionMapper.deleteById(id);
    }

    public PageResponse<ExamPaperResponse> pageExamPapers(AdminLearningPageQuery query) {
        Page<ExamPaper> page = examPaperMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<ExamPaper>()
                        .eq(ExamPaper::getDeleted, 0)
                        .eq(query.status() != null, ExamPaper::getStatus, query.status())
                        .and(hasText(query.keyword()), wrapper -> wrapper.like(ExamPaper::getPaperName, query.keyword()))
                        .orderByDesc(ExamPaper::getCreatedAt));
        return pageResponse(page, page.getRecords().stream().map(paper -> toExamPaperResponse(paper, false)).toList());
    }

    public ExamPaperResponse examPaperDetail(Long id) {
        return toExamPaperResponse(requireExamPaper(id), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ExamPaperResponse createExamPaper(ExamPaperRequest request) {
        ExamPaper paper = new ExamPaper();
        fillExamPaper(paper, request);
        paper.setDeleted(0);
        examPaperMapper.insert(paper);
        replaceExamPaperQuestions(paper.getId(), request.questions());
        return toExamPaperResponse(paper, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ExamPaperResponse updateExamPaper(Long id, ExamPaperRequest request) {
        ExamPaper paper = requireExamPaper(id);
        fillExamPaper(paper, request);
        examPaperMapper.updateById(paper);
        replaceExamPaperQuestions(paper.getId(), request.questions());
        return toExamPaperResponse(paper, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<ExamPaperQuestionResponse> replaceExamPaperQuestions(Long paperId, List<ExamPaperQuestionRequest> requests) {
        requireExamPaper(paperId);
        examPaperQuestionMapper.delete(new LambdaQueryWrapper<ExamPaperQuestion>().eq(ExamPaperQuestion::getPaperId, paperId));
        if (requests != null) {
            requests.forEach(request -> {
                requireQuestion(request.questionId());
                ExamPaperQuestion relation = new ExamPaperQuestion();
                relation.setPaperId(paperId);
                relation.setQuestionId(request.questionId());
                relation.setScore(request.score());
                relation.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
                examPaperQuestionMapper.insert(relation);
            });
        }
        return loadExamPaperQuestions(paperId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteExamPaper(Long id) {
        requireExamPaper(id);
        examPaperMapper.deleteById(id);
    }

    private void fillCourse(Course course, CourseRequest request) {
        StableCoverUrlService.CoverBinding coverBinding = stableCoverUrlService.resolveCoverBinding(
                request.coverUrl(),
                course.getCoverUrl(),
                course.getCoverFileAssetId()
        );
        StableCoverUrlService.CoverBinding lecturerAvatarBinding = stableCoverUrlService.resolvePublicImageBinding(
                request.lecturerAvatarUrl(),
                course.getLecturerAvatarUrl(),
                course.getLecturerAvatarFileAssetId(),
                "lecturerAvatarUrl"
        );
        course.setCourseName(request.courseName());
        course.setSubtitle(request.subtitle());
        course.setCoverUrl(coverBinding.coverUrl());
        course.setCoverFileAssetId(coverBinding.fileAssetId());
        course.setLecturerName(request.lecturerName());
        course.setLecturerAvatarUrl(lecturerAvatarBinding.coverUrl());
        course.setLecturerAvatarFileAssetId(lecturerAvatarBinding.fileAssetId());
        course.setIntroduction(request.introduction());
        course.setPaperId(request.paperId());
        course.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        course.setReviewStatus(request.reviewStatus());
        course.setPublishStatus(request.publishStatus());
        course.setPublishedAt(request.publishedAt());
    }

    private void fillCourseVideo(CourseVideo video, CourseVideoRequest request) {
        video.setCourseId(request.courseId());
        video.setTitle(request.title());
        video.setVideoUrl(request.videoUrl());
        video.setDurationSeconds(request.durationSeconds());
        video.setPaperId(request.paperId());
        video.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        video.setStatus(request.status());
    }

    private void fillBookCategory(BookCategory category, BookCategoryRequest request) {
        category.setParentId(request.parentId());
        category.setCategoryName(request.categoryName());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(request.status());
    }

    private void fillBook(Book book, BookRequest request) {
        StableCoverUrlService.CoverBinding coverBinding = stableCoverUrlService.resolveCoverBinding(
                request.coverUrl(),
                book.getCoverUrl(),
                book.getCoverFileAssetId()
        );
        book.setCategoryId(request.categoryId());
        book.setBookName(request.bookName());
        book.setAuthor(request.author());
        book.setPublisher(request.publisher());
        book.setCoverUrl(coverBinding.coverUrl());
        book.setCoverFileAssetId(coverBinding.fileAssetId());
        book.setIntroduction(request.introduction());
        book.setTotalPages(request.totalPages());
        book.setPaperId(request.paperId());
        book.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        book.setReviewStatus(request.reviewStatus());
        book.setPublishStatus(request.publishStatus());
        book.setPublishedAt(request.publishedAt());
    }

    private void fillBookChapter(BookChapter chapter, BookChapterRequest request) {
        chapter.setBookId(request.bookId());
        chapter.setParentId(request.parentId());
        chapter.setChapterTitle(request.chapterTitle());
        chapter.setContent(request.content());
        chapter.setStartPage(request.startPage());
        chapter.setPageCount(request.pageCount());
        chapter.setPaperId(request.paperId());
        chapter.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        chapter.setStatus(request.status());
    }

    private void fillQuestionCategory(QuestionCategory category, QuestionCategoryRequest request) {
        category.setParentId(request.parentId());
        category.setCategoryName(request.categoryName());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(request.status());
    }

    private void fillQuestion(Question question, QuestionRequest request) {
        question.setCategoryId(request.categoryId());
        question.setQuestionType(request.questionType());
        question.setTitle(request.title());
        question.setAnalysis(request.analysis());
        question.setDifficulty(request.difficulty());
        question.setScore(request.score());
        question.setStatus(request.status());
    }

    private void fillQuestionOption(QuestionOption option, QuestionOptionRequest request) {
        option.setOptionKey(request.optionKey());
        option.setOptionContent(request.optionContent());
        option.setCorrect(Boolean.TRUE.equals(request.correct()) ? 1 : 0);
        option.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private void fillExamPaper(ExamPaper paper, ExamPaperRequest request) {
        paper.setPaperName(request.paperName());
        paper.setDescription(request.description());
        paper.setTotalScore(request.totalScore());
        paper.setPassScore(request.passScore());
        paper.setDurationMinutes(request.durationMinutes());
        paper.setStatus(request.status());
    }

    private void applyReview(Course course, ReviewStatus reviewStatus) {
        course.setReviewStatus(reviewStatus);
        if (reviewStatus == ReviewStatus.APPROVED) {
            course.setPublishStatus(PublishStatus.PUBLISHED);
            course.setPublishedAt(LocalDateTime.now());
        }
        if (reviewStatus == ReviewStatus.REJECTED) {
            course.setPublishStatus(PublishStatus.UNPUBLISHED);
        }
    }

    private void applyReview(Book book, ReviewStatus reviewStatus) {
        book.setReviewStatus(reviewStatus);
        if (reviewStatus == ReviewStatus.APPROVED) {
            book.setPublishStatus(PublishStatus.PUBLISHED);
            book.setPublishedAt(LocalDateTime.now());
        }
        if (reviewStatus == ReviewStatus.REJECTED) {
            book.setPublishStatus(PublishStatus.UNPUBLISHED);
        }
    }

    private Course requireCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null || !Objects.equals(course.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course does not exist");
        }
        return course;
    }

    private CourseVideo requireCourseVideo(Long id) {
        CourseVideo video = courseVideoMapper.selectById(id);
        if (video == null || !Objects.equals(video.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Course video does not exist");
        }
        return video;
    }

    private BookCategory requireBookCategory(Long id) {
        BookCategory category = bookCategoryMapper.selectById(id);
        if (category == null || !Objects.equals(category.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Book category does not exist");
        }
        return category;
    }

    private Book requireBook(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null || !Objects.equals(book.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Book does not exist");
        }
        return book;
    }

    private BookChapter requireBookChapter(Long id) {
        BookChapter chapter = bookChapterMapper.selectById(id);
        if (chapter == null || !Objects.equals(chapter.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Book chapter does not exist");
        }
        return chapter;
    }

    private QuestionCategory requireQuestionCategory(Long id) {
        QuestionCategory category = questionCategoryMapper.selectById(id);
        if (category == null || !Objects.equals(category.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Question category does not exist");
        }
        return category;
    }

    private Question requireQuestion(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null || !Objects.equals(question.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Question does not exist");
        }
        return question;
    }

    private ExamPaper requireExamPaper(Long id) {
        ExamPaper paper = examPaperMapper.selectById(id);
        if (paper == null || !Objects.equals(paper.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Exam paper does not exist");
        }
        return paper;
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

    private CourseResponse toCourseResponse(Course course) {
        return new CourseResponse(course.getId(), course.getCourseName(), course.getSubtitle(), course.getCoverUrl(),
                course.getLecturerName(), course.getLecturerAvatarUrl(), course.getIntroduction(),
                course.getPaperId(), course.getSortOrder(),
                course.getReviewStatus(), course.getPublishStatus(), course.getPublishedAt());
    }

    private CourseVideoResponse toCourseVideoResponse(CourseVideo video) {
        return new CourseVideoResponse(video.getId(), video.getCourseId(), video.getTitle(), video.getVideoUrl(),
                video.getDurationSeconds(), video.getPaperId(), video.getSortOrder(), video.getStatus());
    }

    private BookCategoryResponse toBookCategoryResponse(BookCategory category) {
        return new BookCategoryResponse(category.getId(), category.getParentId(), category.getCategoryName(),
                category.getSortOrder(), category.getStatus(), category.getCreatedAt(), category.getUpdatedAt());
    }

    private BookCategoryBookResponse toBookCategoryBookResponse(Book book) {
        return new BookCategoryBookResponse(book.getId(), book.getCategoryId(), book.getBookName(), book.getAuthor(),
                book.getCoverUrl(), book.getReviewStatus(), book.getPublishStatus(), book.getUpdatedAt());
    }

    private BookResponse toBookResponse(Book book, String paperTitle) {
        return new BookResponse(book.getId(), book.getCategoryId(), book.getBookName(), book.getAuthor(),
                book.getPublisher(), book.getCoverUrl(), book.getIntroduction(), book.getTotalPages(),
                book.getPaperId(), paperTitle, book.getSortOrder(), book.getReviewStatus(), book.getPublishStatus(),
                book.getPublishedAt());
    }

    private List<Long> normalizeBindingBookIds(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "bookIds must not be empty");
        }
        List<Long> normalizedIds = new ArrayList<>();
        for (Long bookId : bookIds) {
            if (bookId == null || bookId <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "bookIds must contain positive values");
            }
            if (!normalizedIds.contains(bookId)) {
                normalizedIds.add(bookId);
            }
        }
        return normalizedIds;
    }

    private Map<Long, String> loadExamPaperNameMap(List<Long> paperIds) {
        if (paperIds == null || paperIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> result = new HashMap<>();
        examPaperMapper.selectList(new LambdaQueryWrapper<ExamPaper>()
                        .in(ExamPaper::getId, paperIds)
                        .eq(ExamPaper::getDeleted, 0))
                .forEach(paper -> result.put(paper.getId(), paper.getPaperName()));
        return result;
    }

    private String loadExamPaperName(Long paperId) {
        if (paperId == null) {
            return null;
        }
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null || !Objects.equals(paper.getDeleted(), 0)) {
            return null;
        }
        return paper.getPaperName();
    }

    private BookChapterResponse toBookChapterResponse(BookChapter chapter) {
        return new BookChapterResponse(chapter.getId(), chapter.getBookId(), chapter.getParentId(),
                chapter.getChapterTitle(), chapter.getContent(), chapter.getStartPage(), chapter.getPageCount(),
                chapter.getPaperId(), chapter.getSortOrder(), chapter.getStatus());
    }

    private QuestionCategoryResponse toQuestionCategoryResponse(QuestionCategory category) {
        return new QuestionCategoryResponse(category.getId(), category.getParentId(), category.getCategoryName(),
                category.getSortOrder(), category.getStatus());
    }

    private QuestionResponse toQuestionResponse(Question question, boolean includeOptions) {
        return new QuestionResponse(question.getId(), question.getCategoryId(), question.getQuestionType(),
                question.getTitle(), question.getAnalysis(), question.getDifficulty(), question.getScore(),
                question.getStatus(), includeOptions ? loadQuestionOptions(question.getId()) : List.of());
    }

    private List<QuestionOptionResponse> loadQuestionOptions(Long questionId) {
        return questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
                        .eq(QuestionOption::getQuestionId, questionId)
                        .orderByAsc(QuestionOption::getSortOrder)
                        .orderByAsc(QuestionOption::getOptionKey))
                .stream()
                .map(this::toQuestionOptionResponse)
                .toList();
    }

    private QuestionOptionResponse toQuestionOptionResponse(QuestionOption option) {
        return new QuestionOptionResponse(option.getId(), option.getQuestionId(), option.getOptionKey(),
                option.getOptionContent(), option.getCorrect(), option.getSortOrder());
    }

    private ExamPaperResponse toExamPaperResponse(ExamPaper paper, boolean includeQuestions) {
        return new ExamPaperResponse(paper.getId(), paper.getPaperName(), paper.getDescription(), paper.getTotalScore(),
                paper.getPassScore(), paper.getDurationMinutes(), paper.getStatus(),
                includeQuestions ? loadExamPaperQuestions(paper.getId()) : List.of());
    }

    private List<ExamPaperQuestionResponse> loadExamPaperQuestions(Long paperId) {
        return examPaperQuestionMapper.selectList(new LambdaQueryWrapper<ExamPaperQuestion>()
                        .eq(ExamPaperQuestion::getPaperId, paperId)
                        .orderByAsc(ExamPaperQuestion::getSortOrder)
                        .orderByAsc(ExamPaperQuestion::getId))
                .stream()
                .map(this::toExamPaperQuestionResponse)
                .toList();
    }

    private ExamPaperQuestionResponse toExamPaperQuestionResponse(ExamPaperQuestion relation) {
        Question question = questionMapper.selectById(relation.getQuestionId());
        return new ExamPaperQuestionResponse(relation.getId(), relation.getPaperId(), relation.getQuestionId(),
                relation.getScore(), relation.getSortOrder(), question == null ? null : toQuestionResponse(question, true));
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
