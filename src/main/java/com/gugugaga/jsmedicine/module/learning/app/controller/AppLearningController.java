package com.gugugaga.jsmedicine.module.learning.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppBookCategoryResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppBookChapterResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppBookResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppCourseResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppCourseVideoResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamPaperResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamRecordResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppExamSubmitRequest;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppLearningPageQuery;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppLearningRecordRequest;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppLearningRecordResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppPodcastResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppTopicCardResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppTopicDetailResponse;
import com.gugugaga.jsmedicine.module.learning.app.dto.AppTopicResourceCardResponse;
import com.gugugaga.jsmedicine.module.learning.app.service.AppLearningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端学习资源")
@RestController
@RequestMapping("/api/v1/app/learning")
public class AppLearningController {

    private final AppLearningService appLearningService;

    public AppLearningController(AppLearningService appLearningService) {
        this.appLearningService = appLearningService;
    }

    @Operation(summary = "分页查询课程")
    @GetMapping("/courses")
    public ApiResponse<PageResponse<AppCourseResponse>> pageCourses(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(appLearningService.pageCourses(new AppLearningPageQuery(page, size, sort, keyword, null)));
    }

    @Operation(summary = "课程详情")
    @GetMapping("/courses/{id}")
    public ApiResponse<AppCourseResponse> courseDetail(@PathVariable Long id) {
        return ApiResponse.ok(appLearningService.courseDetail(id));
    }

    @Operation(summary = "课程视频详情")
    @GetMapping("/courses/{courseId}/videos/{videoId}")
    public ApiResponse<AppCourseVideoResponse> courseVideoDetail(@PathVariable Long courseId, @PathVariable Long videoId) {
        return ApiResponse.ok(appLearningService.courseVideoDetail(courseId, videoId));
    }

    @Operation(summary = "分页查询图书分类")
    @GetMapping("/book-categories")
    public ApiResponse<PageResponse<AppBookCategoryResponse>> pageBookCategories(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long parentId
    ) {
        return ApiResponse.ok(appLearningService.pageBookCategories(new AppLearningPageQuery(page, size, sort, keyword, parentId)));
    }

    @Operation(summary = "分页查询图书")
    @GetMapping("/books")
    public ApiResponse<PageResponse<AppBookResponse>> pageBooks(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        return ApiResponse.ok(appLearningService.pageBooks(new AppLearningPageQuery(page, size, sort, keyword, categoryId)));
    }

    @Operation(summary = "图书详情")
    @GetMapping("/books/{id}")
    public ApiResponse<AppBookResponse> bookDetail(@PathVariable Long id) {
        return ApiResponse.ok(appLearningService.bookDetail(id));
    }

    @Operation(summary = "图书章节详情")
    @GetMapping("/books/{bookId}/chapters/{chapterId}")
    public ApiResponse<AppBookChapterResponse> bookChapterDetail(@PathVariable Long bookId, @PathVariable Long chapterId) {
        return ApiResponse.ok(appLearningService.bookChapterDetail(bookId, chapterId));
    }

    @Operation(summary = "分页查询播客")
    @GetMapping("/podcasts")
    public ApiResponse<PageResponse<AppPodcastResponse>> pagePodcasts(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(appLearningService.pagePodcasts(new AppLearningPageQuery(page, size, sort, keyword, null)));
    }

    @Operation(summary = "播客详情")
    @GetMapping("/podcasts/{id}")
    public ApiResponse<AppPodcastResponse> podcastDetail(@PathVariable Long id) {
        return ApiResponse.ok(appLearningService.podcastDetail(id));
    }

    @Operation(summary = "分页查询考卷")
    @GetMapping("/exam-papers")
    public ApiResponse<PageResponse<AppExamPaperResponse>> pageExamPapers(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(appLearningService.pageExamPapers(new AppLearningPageQuery(page, size, sort, keyword, null)));
    }

    @Operation(summary = "考卷详情")
    @GetMapping("/exam-papers/{id}")
    public ApiResponse<AppExamPaperResponse> examPaperDetail(@PathVariable Long id) {
        return ApiResponse.ok(appLearningService.examPaperDetail(id));
    }

    @Operation(summary = "提交考卷答案")
    @PostMapping("/exam-papers/{id}/submit")
    public ApiResponse<AppExamRecordResponse> submitExam(@PathVariable Long id, @Valid @RequestBody AppExamSubmitRequest request) {
        return ApiResponse.ok(appLearningService.submitExam(id, request));
    }

    @Operation(summary = "分页查询考试记录")
    @GetMapping("/exam-records")
    public ApiResponse<PageResponse<AppExamRecordResponse>> pageExamRecords(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        return ApiResponse.ok(appLearningService.pageExamRecords(page, size));
    }

    @Operation(summary = "考试结果与解析")
    @GetMapping("/exam-records/{id}")
    public ApiResponse<AppExamRecordResponse> examRecordDetail(@PathVariable Long id) {
        return ApiResponse.ok(appLearningService.examRecordDetail(id));
    }

    @Operation(summary = "分页查询专题")
    @GetMapping("/topics")
    public ApiResponse<PageResponse<AppTopicCardResponse>> pageTopics(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(appLearningService.pageTopics(new AppLearningPageQuery(page, size, sort, keyword, null)));
    }

    @Operation(summary = "专题详情")
    @GetMapping("/topics/{id}")
    public ApiResponse<AppTopicDetailResponse> topicDetail(@PathVariable Long id) {
        return ApiResponse.ok(appLearningService.topicDetail(id));
    }

    @Operation(summary = "分页查询专题分区内容")
    @GetMapping("/topics/{id}/sections/{sectionType}")
    public ApiResponse<PageResponse<AppTopicResourceCardResponse>> pageTopicSection(
            @PathVariable Long id,
            @PathVariable String sectionType,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        return ApiResponse.ok(appLearningService.pageTopicSection(id, sectionType, page, size));
    }

    @Operation(summary = "同步学习记录")
    @PostMapping("/records")
    public ApiResponse<AppLearningRecordResponse> syncLearningRecord(@Valid @RequestBody AppLearningRecordRequest request) {
        return ApiResponse.ok(appLearningService.syncLearningRecord(request));
    }
}
