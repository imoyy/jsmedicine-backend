package com.gugugaga.jsmedicine.module.learning.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminLearningPageQuery;
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
import com.gugugaga.jsmedicine.module.learning.admin.dto.LearningReviewRequest;
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
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.service.AuditRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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
    private final AuditRecordService auditRecordService;
    private final CurrentAdminAccessor currentAdminAccessor;

    public AdminLearningService(
            CourseMapper courseMapper,
            CourseVideoMapper courseVideoMapper,
            BookCategoryMapper bookCategoryMapper,
            BookMapper bookMapper,
            BookChapterMapper bookChapterMapper,
            AuditRecordService auditRecordService,
            CurrentAdminAccessor currentAdminAccessor
    ) {
        this.courseMapper = courseMapper;
        this.courseVideoMapper = courseVideoMapper;
        this.bookCategoryMapper = bookCategoryMapper;
        this.bookMapper = bookMapper;
        this.bookChapterMapper = bookChapterMapper;
        this.auditRecordService = auditRecordService;
        this.currentAdminAccessor = currentAdminAccessor;
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
        return pageResponse(page, page.getRecords().stream().map(this::toBookResponse).toList());
    }

    public BookResponse bookDetail(Long id) {
        return toBookResponse(requireBook(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public BookResponse createBook(BookRequest request) {
        if (request.categoryId() != null) {
            requireBookCategory(request.categoryId());
        }
        Book book = new Book();
        fillBook(book, request);
        book.setDeleted(0);
        bookMapper.insert(book);
        return toBookResponse(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = requireBook(id);
        if (request.categoryId() != null) {
            requireBookCategory(request.categoryId());
        }
        fillBook(book, request);
        bookMapper.updateById(book);
        return toBookResponse(book);
    }

    @Transactional(rollbackFor = Exception.class)
    public BookResponse reviewBook(Long id, LearningReviewRequest request) {
        Book book = requireBook(id);
        ReviewStatus before = book.getReviewStatus();
        applyReview(book, request.reviewStatus());
        bookMapper.updateById(book);
        saveAudit("book", id, before, request.reviewStatus(), request.comment());
        return toBookResponse(book);
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

    private void fillCourse(Course course, CourseRequest request) {
        course.setCourseName(request.courseName());
        course.setSubtitle(request.subtitle());
        course.setCoverUrl(request.coverUrl());
        course.setLecturerName(request.lecturerName());
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
        book.setCategoryId(request.categoryId());
        book.setBookName(request.bookName());
        book.setAuthor(request.author());
        book.setPublisher(request.publisher());
        book.setCoverUrl(request.coverUrl());
        book.setIntroduction(request.introduction());
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
        chapter.setPaperId(request.paperId());
        chapter.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        chapter.setStatus(request.status());
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
                course.getLecturerName(), course.getIntroduction(), course.getPaperId(), course.getSortOrder(),
                course.getReviewStatus(), course.getPublishStatus(), course.getPublishedAt());
    }

    private CourseVideoResponse toCourseVideoResponse(CourseVideo video) {
        return new CourseVideoResponse(video.getId(), video.getCourseId(), video.getTitle(), video.getVideoUrl(),
                video.getDurationSeconds(), video.getSortOrder(), video.getStatus());
    }

    private BookCategoryResponse toBookCategoryResponse(BookCategory category) {
        return new BookCategoryResponse(category.getId(), category.getParentId(), category.getCategoryName(),
                category.getSortOrder(), category.getStatus());
    }

    private BookResponse toBookResponse(Book book) {
        return new BookResponse(book.getId(), book.getCategoryId(), book.getBookName(), book.getAuthor(),
                book.getPublisher(), book.getCoverUrl(), book.getIntroduction(), book.getPaperId(), book.getSortOrder(),
                book.getReviewStatus(), book.getPublishStatus(), book.getPublishedAt());
    }

    private BookChapterResponse toBookChapterResponse(BookChapter chapter) {
        return new BookChapterResponse(chapter.getId(), chapter.getBookId(), chapter.getParentId(),
                chapter.getChapterTitle(), chapter.getContent(), chapter.getPaperId(), chapter.getSortOrder(),
                chapter.getStatus());
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
