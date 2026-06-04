package com.gugugaga.jsmedicine.module.content.admin.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminContentPageQuery;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminCoverAssetResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminCoverConfirmRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminCoverUploadRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminCoverUploadResponse;
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
import com.gugugaga.jsmedicine.module.content.admin.service.AdminCoverUploadService;
import com.gugugaga.jsmedicine.module.content.admin.service.AdminContentService;
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

@Tag(name = "管理端内容配置")
@RestController
@RequestMapping("/api/v1/admin/content")
public class AdminContentController {

    private final AdminContentService adminContentService;
    private final AdminCoverUploadService adminCoverUploadService;

    public AdminContentController(
            AdminContentService adminContentService,
            AdminCoverUploadService adminCoverUploadService
    ) {
        this.adminContentService = adminContentService;
        this.adminCoverUploadService = adminCoverUploadService;
    }

    @Operation(summary = "分页查询首页分类")
    @PreAuthorize("hasAuthority('content:home:view')")
    @GetMapping("/home/categories")
    public ApiResponse<PageResponse<HomeCategoryResponse>> pageHomeCategories(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(adminContentService.pageHomeCategories(new AdminContentPageQuery(page, size, sort, keyword, null, null)));
    }

    @Operation(summary = "新增首页分类")
    @PreAuthorize("hasAuthority('content:home:edit')")
    @PostMapping("/home/categories")
    public ApiResponse<HomeCategoryResponse> createHomeCategory(@Valid @RequestBody HomeCategoryRequest request) {
        return ApiResponse.ok(adminContentService.createHomeCategory(request));
    }

    @Operation(summary = "修改首页分类")
    @PreAuthorize("hasAuthority('content:home:edit')")
    @PutMapping("/home/categories/{id}")
    public ApiResponse<HomeCategoryResponse> updateHomeCategory(@PathVariable Long id, @Valid @RequestBody HomeCategoryRequest request) {
        return ApiResponse.ok(adminContentService.updateHomeCategory(id, request));
    }

    @Operation(summary = "删除首页分类")
    @PreAuthorize("hasAuthority('content:home:edit')")
    @DeleteMapping("/home/categories/{id}")
    public ApiResponse<Void> deleteHomeCategory(@PathVariable Long id) {
        adminContentService.deleteHomeCategory(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询首页内容")
    @PreAuthorize("hasAuthority('content:home:view')")
    @GetMapping("/home/contents")
    public ApiResponse<PageResponse<HomeContentResponse>> pageHomeContents(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(adminContentService.pageHomeContents(new AdminContentPageQuery(page, size, sort, keyword, null, null)));
    }

    @Operation(summary = "新增首页内容",
            description = "首页快捷配置继续沿用统一 contentType + targetId 模型；当前仅支持 course、book、podcast、topic、live 五类资源，并在保存时校验目标资源存在。")
    @PreAuthorize("hasAuthority('content:home:edit')")
    @PostMapping("/home/contents")
    public ApiResponse<HomeContentResponse> createHomeContent(@Valid @RequestBody HomeContentRequest request) {
        return ApiResponse.ok(adminContentService.createHomeContent(request));
    }

    @Operation(summary = "修改首页内容",
            description = "修改时沿用新增接口相同规则：contentType 必须是受支持的资源类型，targetId 需指向真实资源，startAt 需早于 endAt。")
    @PreAuthorize("hasAuthority('content:home:edit')")
    @PutMapping("/home/contents/{id}")
    public ApiResponse<HomeContentResponse> updateHomeContent(@PathVariable Long id, @Valid @RequestBody HomeContentRequest request) {
        return ApiResponse.ok(adminContentService.updateHomeContent(id, request));
    }

    @Operation(summary = "删除首页内容")
    @PreAuthorize("hasAuthority('content:home:edit')")
    @DeleteMapping("/home/contents/{id}")
    public ApiResponse<Void> deleteHomeContent(@PathVariable Long id) {
        adminContentService.deleteHomeContent(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询资讯")
    @PreAuthorize("hasAuthority('content:article:view')")
    @GetMapping("/articles")
    public ApiResponse<PageResponse<ArticleResponse>> pageArticles(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(adminContentService.pageArticles(new AdminContentPageQuery(page, size, sort, keyword, null, null)));
    }

    @Operation(summary = "新增资讯")
    @PreAuthorize("hasAuthority('content:article:edit')")
    @PostMapping("/articles")
    public ApiResponse<ArticleResponse> createArticle(@Valid @RequestBody ArticleRequest request) {
        return ApiResponse.ok(adminContentService.createArticle(request));
    }

    @Operation(summary = "修改资讯")
    @PreAuthorize("hasAuthority('content:article:edit')")
    @PutMapping("/articles/{id}")
    public ApiResponse<ArticleResponse> updateArticle(@PathVariable Long id, @Valid @RequestBody ArticleRequest request) {
        return ApiResponse.ok(adminContentService.updateArticle(id, request));
    }

    @Operation(summary = "审核资讯")
    @PreAuthorize("hasAuthority('content:article:review')")
    @PatchMapping("/articles/{id}/review")
    public ApiResponse<ArticleResponse> reviewArticle(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok(adminContentService.reviewArticle(id, request));
    }

    @Operation(summary = "删除资讯")
    @PreAuthorize("hasAuthority('content:article:edit')")
    @DeleteMapping("/articles/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        adminContentService.deleteArticle(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询播客")
    @PreAuthorize("hasAuthority('content:podcast:view')")
    @GetMapping("/podcasts")
    public ApiResponse<PageResponse<PodcastResponse>> pagePodcasts(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(adminContentService.pagePodcasts(new AdminContentPageQuery(page, size, sort, keyword, null, null)));
    }

    @Operation(summary = "新增播客")
    @PreAuthorize("hasAuthority('content:podcast:edit')")
    @PostMapping("/podcasts")
    public ApiResponse<PodcastResponse> createPodcast(@Valid @RequestBody PodcastRequest request) {
        return ApiResponse.ok(adminContentService.createPodcast(request));
    }

    @Operation(summary = "修改播客")
    @PreAuthorize("hasAuthority('content:podcast:edit')")
    @PutMapping("/podcasts/{id}")
    public ApiResponse<PodcastResponse> updatePodcast(@PathVariable Long id, @Valid @RequestBody PodcastRequest request) {
        return ApiResponse.ok(adminContentService.updatePodcast(id, request));
    }

    @Operation(summary = "审核播客")
    @PreAuthorize("hasAuthority('content:podcast:review')")
    @PatchMapping("/podcasts/{id}/review")
    public ApiResponse<PodcastResponse> reviewPodcast(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok(adminContentService.reviewPodcast(id, request));
    }

    @Operation(summary = "删除播客")
    @PreAuthorize("hasAuthority('content:podcast:edit')")
    @DeleteMapping("/podcasts/{id}")
    public ApiResponse<Void> deletePodcast(@PathVariable Long id) {
        adminContentService.deletePodcast(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询播客音频")
    @PreAuthorize("hasAuthority('content:podcast:view')")
    @GetMapping("/podcasts/{podcastId}/audios")
    public ApiResponse<PageResponse<PodcastAudioResponse>> pagePodcastAudios(
            @PathVariable Long podcastId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size
    ) {
        return ApiResponse.ok(adminContentService.pagePodcastAudios(podcastId, page, size));
    }

    @Operation(summary = "新增播客音频")
    @PreAuthorize("hasAuthority('content:podcast:edit')")
    @PostMapping("/podcasts/audios")
    public ApiResponse<PodcastAudioResponse> createPodcastAudio(@Valid @RequestBody PodcastAudioRequest request) {
        return ApiResponse.ok(adminContentService.createPodcastAudio(request));
    }

    @Operation(summary = "修改播客音频")
    @PreAuthorize("hasAuthority('content:podcast:edit')")
    @PutMapping("/podcasts/audios/{id}")
    public ApiResponse<PodcastAudioResponse> updatePodcastAudio(@PathVariable Long id, @Valid @RequestBody PodcastAudioRequest request) {
        return ApiResponse.ok(adminContentService.updatePodcastAudio(id, request));
    }

    @Operation(summary = "删除播客音频")
    @PreAuthorize("hasAuthority('content:podcast:edit')")
    @DeleteMapping("/podcasts/audios/{id}")
    public ApiResponse<Void> deletePodcastAudio(@PathVariable Long id) {
        adminContentService.deletePodcastAudio(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询专题")
    @PreAuthorize("hasAuthority('content:topic:view')")
    @GetMapping("/topics")
    public ApiResponse<PageResponse<TopicResponse>> pageTopics(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(adminContentService.pageTopics(new AdminContentPageQuery(page, size, sort, keyword, null, null)));
    }

    @Operation(summary = "新增专题")
    @PreAuthorize("hasAuthority('content:topic:edit')")
    @PostMapping("/topics")
    public ApiResponse<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request) {
        return ApiResponse.ok(adminContentService.createTopic(request));
    }

    @Operation(summary = "修改专题")
    @PreAuthorize("hasAuthority('content:topic:edit')")
    @PutMapping("/topics/{id}")
    public ApiResponse<TopicResponse> updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest request) {
        return ApiResponse.ok(adminContentService.updateTopic(id, request));
    }

    @Operation(summary = "审核专题")
    @PreAuthorize("hasAuthority('content:topic:review')")
    @PatchMapping("/topics/{id}/review")
    public ApiResponse<TopicResponse> reviewTopic(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return ApiResponse.ok(adminContentService.reviewTopic(id, request));
    }

    @Operation(summary = "删除专题")
    @PreAuthorize("hasAuthority('content:topic:edit')")
    @DeleteMapping("/topics/{id}")
    public ApiResponse<Void> deleteTopic(@PathVariable Long id) {
        adminContentService.deleteTopic(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "替换专题关联项",
            description = "仅支持 course、book、podcast 三种分项类型；会按 sortOrder 和请求顺序统一归一化排序，且禁止同一专题内重复关联同类型同资源。")
    @PreAuthorize("hasAuthority('content:topic:edit')")
    @PutMapping("/topics/{id}/items")
    public ApiResponse<List<TopicItemResponse>> replaceTopicItems(
            @PathVariable Long id,
            @Valid @RequestBody List<TopicItemRequest> requests
    ) {
        return ApiResponse.ok(adminContentService.replaceTopicItems(id, requests));
    }

    @Operation(summary = "分页查询文件资源")
    @PreAuthorize("hasAuthority('content:file:view')")
    @GetMapping("/files")
    public ApiResponse<PageResponse<FileAssetResponse>> pageFileAssets(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(adminContentService.pageFileAssets(new AdminContentPageQuery(page, size, sort, keyword, null, null)));
    }

    @Operation(summary = "登记文件资源")
    @PreAuthorize("hasAuthority('content:file:edit')")
    @PostMapping("/files")
    public ApiResponse<FileAssetResponse> createFileAsset(@Valid @RequestBody FileAssetRequest request) {
        return ApiResponse.ok(adminContentService.createFileAsset(request));
    }

    @Operation(
            summary = "申请封面上传地址",
            description = "统一用于资讯、课程、图书、播客、专题、直播、专家、知识库、首页内容等管理端封面上传。前端先调用本接口获取预签名上传地址，上传完成后再调用确认接口换取稳定 coverUrl。"
    )
    @PreAuthorize("hasAuthority('content:file:edit')")
    @PostMapping("/files/covers/upload-url")
    public ApiResponse<AdminCoverUploadResponse> createCoverUploadUrl(
            @Valid @RequestBody AdminCoverUploadRequest request
    ) {
        return ApiResponse.ok(adminCoverUploadService.createUploadUrl(request));
    }

    @Operation(
            summary = "确认封面上传",
            description = "确认成功后会写入 file_assets，并返回稳定读取地址 /api/v1/files/{id}/content；业务表中的 coverUrl 应保存该稳定地址，而不是对象存储临时签名 URL。"
    )
    @PreAuthorize("hasAuthority('content:file:edit')")
    @PostMapping("/files/covers/confirm")
    public ApiResponse<AdminCoverAssetResponse> confirmCoverUpload(
            @Valid @RequestBody AdminCoverConfirmRequest request
    ) {
        return ApiResponse.ok(adminCoverUploadService.confirmUpload(request));
    }

    @Operation(summary = "删除文件资源")
    @PreAuthorize("hasAuthority('content:file:edit')")
    @DeleteMapping("/files/{id}")
    public ApiResponse<Void> deleteFileAsset(@PathVariable Long id) {
        adminContentService.deleteFileAsset(id);
        return ApiResponse.ok();
    }
}
