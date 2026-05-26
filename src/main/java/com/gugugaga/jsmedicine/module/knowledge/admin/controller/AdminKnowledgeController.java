package com.gugugaga.jsmedicine.module.knowledge.admin.controller;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeCategoryRequest;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeCategoryResponse;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeEntryRequest;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeEntryResponse;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeReviewRequest;
import com.gugugaga.jsmedicine.module.knowledge.admin.service.AdminKnowledgeService;
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

@Tag(name = "管理端知识库")
@RestController
@RequestMapping("/api/v1/admin/knowledge")
public class AdminKnowledgeController {

    private final AdminKnowledgeService adminKnowledgeService;

    public AdminKnowledgeController(AdminKnowledgeService adminKnowledgeService) {
        this.adminKnowledgeService = adminKnowledgeService;
    }

    @Operation(summary = "分页查询知识库分类")
    @PreAuthorize("hasAuthority('knowledge:category:view')")
    @GetMapping("/categories")
    public ApiResponse<PageResponse<KnowledgeCategoryResponse>> pageCategories(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminKnowledgeService.pageCategories(page, size, keyword, parentId, status));
    }

    @Operation(summary = "新增知识库分类")
    @PreAuthorize("hasAuthority('knowledge:category:edit')")
    @PostMapping("/categories")
    public ApiResponse<KnowledgeCategoryResponse> createCategory(@Valid @RequestBody KnowledgeCategoryRequest request) {
        return ApiResponse.ok(adminKnowledgeService.createCategory(request));
    }

    @Operation(summary = "修改知识库分类")
    @PreAuthorize("hasAuthority('knowledge:category:edit')")
    @PutMapping("/categories/{id}")
    public ApiResponse<KnowledgeCategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody KnowledgeCategoryRequest request) {
        return ApiResponse.ok(adminKnowledgeService.updateCategory(id, request));
    }

    @Operation(summary = "删除知识库分类")
    @PreAuthorize("hasAuthority('knowledge:category:edit')")
    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        adminKnowledgeService.deleteCategory(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询知识库条目")
    @PreAuthorize("hasAuthority('knowledge:entry:view')")
    @GetMapping("/entries")
    public ApiResponse<PageResponse<KnowledgeEntryResponse>> pageEntries(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) ReviewStatus reviewStatus
    ) {
        return ApiResponse.ok(adminKnowledgeService.pageEntries(page, size, keyword, categoryId, reviewStatus));
    }

    @Operation(summary = "知识库条目详情")
    @PreAuthorize("hasAuthority('knowledge:entry:view')")
    @GetMapping("/entries/{id}")
    public ApiResponse<KnowledgeEntryResponse> entryDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminKnowledgeService.entryDetail(id));
    }

    @Operation(summary = "新增知识库条目")
    @PreAuthorize("hasAuthority('knowledge:entry:edit')")
    @PostMapping("/entries")
    public ApiResponse<KnowledgeEntryResponse> createEntry(@Valid @RequestBody KnowledgeEntryRequest request) {
        return ApiResponse.ok(adminKnowledgeService.createEntry(request));
    }

    @Operation(summary = "修改知识库条目")
    @PreAuthorize("hasAuthority('knowledge:entry:edit')")
    @PutMapping("/entries/{id}")
    public ApiResponse<KnowledgeEntryResponse> updateEntry(@PathVariable Long id, @Valid @RequestBody KnowledgeEntryRequest request) {
        return ApiResponse.ok(adminKnowledgeService.updateEntry(id, request));
    }

    @Operation(summary = "审核知识库条目")
    @PreAuthorize("hasAuthority('knowledge:entry:review')")
    @PatchMapping("/entries/{id}/review")
    public ApiResponse<KnowledgeEntryResponse> reviewEntry(@PathVariable Long id, @Valid @RequestBody KnowledgeReviewRequest request) {
        return ApiResponse.ok(adminKnowledgeService.reviewEntry(id, request));
    }

    @Operation(summary = "删除知识库条目")
    @PreAuthorize("hasAuthority('knowledge:entry:edit')")
    @DeleteMapping("/entries/{id}")
    public ApiResponse<Void> deleteEntry(@PathVariable Long id) {
        adminKnowledgeService.deleteEntry(id);
        return ApiResponse.ok();
    }
}
