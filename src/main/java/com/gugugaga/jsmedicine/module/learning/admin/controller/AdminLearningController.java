package com.gugugaga.jsmedicine.module.learning.admin.controller;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
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
import com.gugugaga.jsmedicine.module.learning.admin.service.AdminLearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理端学习资源")
@RestController
@RequestMapping("/api/v1/admin/learning")
public class AdminLearningController {

    private final AdminLearningService adminLearningService;

    public AdminLearningController(AdminLearningService adminLearningService) {
        this.adminLearningService = adminLearningService;
    }

    @Operation(summary = "分页查询课程")
    @PreAuthorize("hasAuthority('learning:course:view')")
    @GetMapping("/courses")
    public ApiResponse<PageResponse<CourseResponse>> pageCourses(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) ReviewStatus reviewStatus
    ) {
        return ApiResponse.ok(adminLearningService.pageCourses(new AdminLearningPageQuery(page, size, sort, keyword, null, null, reviewStatus)));
    }

    @Operation(summary = "查询课程详情")
    @PreAuthorize("hasAuthority('learning:course:view')")
    @GetMapping("/courses/{id}")
    public ApiResponse<CourseResponse> courseDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminLearningService.courseDetail(id));
    }

    @Operation(summary = "新增课程")
    @PreAuthorize("hasAuthority('learning:course:edit')")
    @PostMapping("/courses")
    public ApiResponse<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ApiResponse.ok(adminLearningService.createCourse(request));
    }

    @Operation(summary = "修改课程")
    @PreAuthorize("hasAuthority('learning:course:edit')")
    @PutMapping("/courses/{id}")
    public ApiResponse<CourseResponse> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ApiResponse.ok(adminLearningService.updateCourse(id, request));
    }

    @Operation(summary = "审核课程")
    @PreAuthorize("hasAuthority('learning:course:review')")
    @PatchMapping("/courses/{id}/review")
    public ApiResponse<CourseResponse> reviewCourse(@PathVariable Long id, @Valid @RequestBody LearningReviewRequest request) {
        return ApiResponse.ok(adminLearningService.reviewCourse(id, request));
    }

    @Operation(summary = "删除课程")
    @PreAuthorize("hasAuthority('learning:course:edit')")
    @DeleteMapping("/courses/{id}")
    public ApiResponse<Void> deleteCourse(@PathVariable Long id) {
        adminLearningService.deleteCourse(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询课程视频")
    @PreAuthorize("hasAuthority('learning:course:view')")
    @GetMapping("/courses/{courseId}/videos")
    public ApiResponse<PageResponse<CourseVideoResponse>> pageCourseVideos(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        return ApiResponse.ok(adminLearningService.pageCourseVideos(courseId, page, size));
    }

    @Operation(summary = "新增课程视频")
    @PreAuthorize("hasAuthority('learning:course:edit')")
    @PostMapping("/courses/videos")
    public ApiResponse<CourseVideoResponse> createCourseVideo(@Valid @RequestBody CourseVideoRequest request) {
        return ApiResponse.ok(adminLearningService.createCourseVideo(request));
    }

    @Operation(summary = "修改课程视频")
    @PreAuthorize("hasAuthority('learning:course:edit')")
    @PutMapping("/courses/videos/{id}")
    public ApiResponse<CourseVideoResponse> updateCourseVideo(@PathVariable Long id, @Valid @RequestBody CourseVideoRequest request) {
        return ApiResponse.ok(adminLearningService.updateCourseVideo(id, request));
    }

    @Operation(summary = "删除课程视频")
    @PreAuthorize("hasAuthority('learning:course:edit')")
    @DeleteMapping("/courses/videos/{id}")
    public ApiResponse<Void> deleteCourseVideo(@PathVariable Long id) {
        adminLearningService.deleteCourseVideo(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询图书分类")
    @PreAuthorize("hasAuthority('learning:book:view')")
    @GetMapping("/book-categories")
    public ApiResponse<PageResponse<BookCategoryResponse>> pageBookCategories(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminLearningService.pageBookCategories(new AdminLearningPageQuery(page, size, sort, keyword, parentId, status, null)));
    }

    @Operation(summary = "新增图书分类")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @PostMapping("/book-categories")
    public ApiResponse<BookCategoryResponse> createBookCategory(@Valid @RequestBody BookCategoryRequest request) {
        return ApiResponse.ok(adminLearningService.createBookCategory(request));
    }

    @Operation(summary = "查询图书分类详情")
    @PreAuthorize("hasAuthority('learning:book:view')")
    @GetMapping("/book-categories/{id}")
    public ApiResponse<BookCategoryResponse> bookCategoryDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminLearningService.bookCategoryDetail(id));
    }

    @Operation(summary = "修改图书分类")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @PutMapping("/book-categories/{id}")
    public ApiResponse<BookCategoryResponse> updateBookCategory(@PathVariable Long id, @Valid @RequestBody BookCategoryRequest request) {
        return ApiResponse.ok(adminLearningService.updateBookCategory(id, request));
    }

    @Operation(summary = "删除图书分类")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @DeleteMapping("/book-categories/{id}")
    public ApiResponse<Void> deleteBookCategory(@PathVariable Long id) {
        adminLearningService.deleteBookCategory(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询分类下图书")
    @PreAuthorize("hasAuthority('learning:book:view')")
    @GetMapping("/book-categories/{id}/books")
    public ApiResponse<PageResponse<BookCategoryBookResponse>> pageBooksByCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(adminLearningService.pageBooksByCategory(id, page, size, sort, keyword));
    }

    @Operation(summary = "分类批量加入图书")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @PostMapping("/book-categories/{id}/books")
    public ApiResponse<Void> addBooksToCategory(
            @PathVariable Long id,
            @Valid @RequestBody BookCategoryBookBindingRequest request
    ) {
        adminLearningService.addBooksToCategory(id, request);
        return ApiResponse.ok();
    }

    @Operation(summary = "分类批量移除图书")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @DeleteMapping("/book-categories/{id}/books")
    public ApiResponse<Void> removeBooksFromCategory(
            @PathVariable Long id,
            @Valid @RequestBody BookCategoryBookBindingRequest request
    ) {
        adminLearningService.removeBooksFromCategory(id, request);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询图书")
    @PreAuthorize("hasAuthority('learning:book:view')")
    @GetMapping("/books")
    public ApiResponse<PageResponse<BookResponse>> pageBooks(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ReviewStatus reviewStatus
    ) {
        return ApiResponse.ok(adminLearningService.pageBooks(new AdminLearningPageQuery(page, size, sort, keyword, categoryId, null, reviewStatus)));
    }

    @Operation(summary = "查询图书详情", description = "图书当前采用单考卷配置模型，返回的 `paperId` / `paperTitle` 即该图书关联考卷；如需选择考卷，可复用考卷列表与详情接口。")
    @PreAuthorize("hasAuthority('learning:book:view')")
    @GetMapping("/books/{id}")
    public ApiResponse<BookResponse> bookDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminLearningService.bookDetail(id));
    }

    @Operation(summary = "新增图书", description = "通过请求体中的 `paperId` 维护图书级单考卷配置；传 null 表示不绑定考卷，不提供独立图书考卷配置接口。")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @PostMapping("/books")
    public ApiResponse<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        return ApiResponse.ok(adminLearningService.createBook(request));
    }

    @Operation(summary = "修改图书", description = "通过请求体中的 `paperId` 维护图书级单考卷配置；如需更换考卷，直接调用本接口更新 `paperId`。")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @PutMapping("/books/{id}")
    public ApiResponse<BookResponse> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return ApiResponse.ok(adminLearningService.updateBook(id, request));
    }

    @Operation(summary = "审核图书")
    @PreAuthorize("hasAuthority('learning:book:review')")
    @PatchMapping("/books/{id}/review")
    public ApiResponse<BookResponse> reviewBook(@PathVariable Long id, @Valid @RequestBody LearningReviewRequest request) {
        return ApiResponse.ok(adminLearningService.reviewBook(id, request));
    }

    @Operation(summary = "删除图书")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @DeleteMapping("/books/{id}")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        adminLearningService.deleteBook(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询图书章节")
    @PreAuthorize("hasAuthority('learning:book:view')")
    @GetMapping("/books/{bookId}/chapters")
    public ApiResponse<PageResponse<BookChapterResponse>> pageBookChapters(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        return ApiResponse.ok(adminLearningService.pageBookChapters(bookId, page, size));
    }

    @Operation(summary = "新增图书章节")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @PostMapping("/books/chapters")
    public ApiResponse<BookChapterResponse> createBookChapter(@Valid @RequestBody BookChapterRequest request) {
        return ApiResponse.ok(adminLearningService.createBookChapter(request));
    }

    @Operation(summary = "修改图书章节")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @PutMapping("/books/chapters/{id}")
    public ApiResponse<BookChapterResponse> updateBookChapter(@PathVariable Long id, @Valid @RequestBody BookChapterRequest request) {
        return ApiResponse.ok(adminLearningService.updateBookChapter(id, request));
    }

    @Operation(summary = "删除图书章节")
    @PreAuthorize("hasAuthority('learning:book:edit')")
    @DeleteMapping("/books/chapters/{id}")
    public ApiResponse<Void> deleteBookChapter(@PathVariable Long id) {
        adminLearningService.deleteBookChapter(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询题库分类")
    @PreAuthorize("hasAuthority('learning:question:view')")
    @GetMapping("/question-categories")
    public ApiResponse<PageResponse<QuestionCategoryResponse>> pageQuestionCategories(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminLearningService.pageQuestionCategories(new AdminLearningPageQuery(page, size, sort, keyword, parentId, status, null)));
    }

    @Operation(summary = "新增题库分类")
    @PreAuthorize("hasAuthority('learning:question:edit')")
    @PostMapping("/question-categories")
    public ApiResponse<QuestionCategoryResponse> createQuestionCategory(@Valid @RequestBody QuestionCategoryRequest request) {
        return ApiResponse.ok(adminLearningService.createQuestionCategory(request));
    }

    @Operation(summary = "修改题库分类")
    @PreAuthorize("hasAuthority('learning:question:edit')")
    @PutMapping("/question-categories/{id}")
    public ApiResponse<QuestionCategoryResponse> updateQuestionCategory(@PathVariable Long id, @Valid @RequestBody QuestionCategoryRequest request) {
        return ApiResponse.ok(adminLearningService.updateQuestionCategory(id, request));
    }

    @Operation(summary = "删除题库分类")
    @PreAuthorize("hasAuthority('learning:question:edit')")
    @DeleteMapping("/question-categories/{id}")
    public ApiResponse<Void> deleteQuestionCategory(@PathVariable Long id) {
        adminLearningService.deleteQuestionCategory(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询题目")
    @PreAuthorize("hasAuthority('learning:question:view')")
    @GetMapping("/questions")
    public ApiResponse<PageResponse<QuestionResponse>> pageQuestions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminLearningService.pageQuestions(new AdminLearningPageQuery(page, size, sort, keyword, categoryId, status, null)));
    }

    @Operation(summary = "题目详情")
    @PreAuthorize("hasAuthority('learning:question:view')")
    @GetMapping("/questions/{id}")
    public ApiResponse<QuestionResponse> questionDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminLearningService.questionDetail(id));
    }

    @Operation(summary = "新增题目")
    @PreAuthorize("hasAuthority('learning:question:edit')")
    @PostMapping("/questions")
    public ApiResponse<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        return ApiResponse.ok(adminLearningService.createQuestion(request));
    }

    @Operation(summary = "修改题目")
    @PreAuthorize("hasAuthority('learning:question:edit')")
    @PutMapping("/questions/{id}")
    public ApiResponse<QuestionResponse> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return ApiResponse.ok(adminLearningService.updateQuestion(id, request));
    }

    @Operation(summary = "替换题目选项")
    @PreAuthorize("hasAuthority('learning:question:edit')")
    @PutMapping("/questions/{id}/options")
    public ApiResponse<List<QuestionOptionResponse>> replaceQuestionOptions(
            @PathVariable Long id,
            @Valid @RequestBody List<QuestionOptionRequest> requests
    ) {
        return ApiResponse.ok(adminLearningService.replaceQuestionOptions(id, requests));
    }

    @Operation(summary = "删除题目")
    @PreAuthorize("hasAuthority('learning:question:edit')")
    @DeleteMapping("/questions/{id}")
    public ApiResponse<Void> deleteQuestion(@PathVariable Long id) {
        adminLearningService.deleteQuestion(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询考卷")
    @PreAuthorize("hasAuthority('learning:exam:view')")
    @GetMapping("/exam-papers")
    public ApiResponse<PageResponse<ExamPaperResponse>> pageExamPapers(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminLearningService.pageExamPapers(new AdminLearningPageQuery(page, size, sort, keyword, null, status, null)));
    }

    @Operation(summary = "考卷详情")
    @PreAuthorize("hasAuthority('learning:exam:view')")
    @GetMapping("/exam-papers/{id}")
    public ApiResponse<ExamPaperResponse> examPaperDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminLearningService.examPaperDetail(id));
    }

    @Operation(summary = "新增考卷")
    @PreAuthorize("hasAuthority('learning:exam:edit')")
    @PostMapping("/exam-papers")
    public ApiResponse<ExamPaperResponse> createExamPaper(@Valid @RequestBody ExamPaperRequest request) {
        return ApiResponse.ok(adminLearningService.createExamPaper(request));
    }

    @Operation(summary = "修改考卷")
    @PreAuthorize("hasAuthority('learning:exam:edit')")
    @PutMapping("/exam-papers/{id}")
    public ApiResponse<ExamPaperResponse> updateExamPaper(@PathVariable Long id, @Valid @RequestBody ExamPaperRequest request) {
        return ApiResponse.ok(adminLearningService.updateExamPaper(id, request));
    }

    @Operation(summary = "替换考卷题目")
    @PreAuthorize("hasAuthority('learning:exam:edit')")
    @PutMapping("/exam-papers/{id}/questions")
    public ApiResponse<List<ExamPaperQuestionResponse>> replaceExamPaperQuestions(
            @PathVariable Long id,
            @Valid @RequestBody List<ExamPaperQuestionRequest> requests
    ) {
        return ApiResponse.ok(adminLearningService.replaceExamPaperQuestions(id, requests));
    }

    @Operation(summary = "删除考卷")
    @PreAuthorize("hasAuthority('learning:exam:edit')")
    @DeleteMapping("/exam-papers/{id}")
    public ApiResponse<Void> deleteExamPaper(@PathVariable Long id) {
        adminLearningService.deleteExamPaper(id);
        return ApiResponse.ok();
    }
}
