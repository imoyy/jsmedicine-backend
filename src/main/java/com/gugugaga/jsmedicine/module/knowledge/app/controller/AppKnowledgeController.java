package com.gugugaga.jsmedicine.module.knowledge.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.knowledge.app.dto.AppKnowledgeCategoryResponse;
import com.gugugaga.jsmedicine.module.knowledge.app.dto.AppKnowledgeEntryResponse;
import com.gugugaga.jsmedicine.module.knowledge.app.dto.AppKnowledgeSearchResult;
import com.gugugaga.jsmedicine.module.knowledge.app.service.AppKnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户端知识库")
@RestController
@RequestMapping("/api/v1/app/knowledge")
public class AppKnowledgeController {

    private final AppKnowledgeService appKnowledgeService;

    public AppKnowledgeController(AppKnowledgeService appKnowledgeService) {
        this.appKnowledgeService = appKnowledgeService;
    }

    @Operation(summary = "知识库分类树")
    @GetMapping("/categories/tree")
    public ApiResponse<List<AppKnowledgeCategoryResponse>> categoryTree() {
        return ApiResponse.ok(appKnowledgeService.categoryTree());
    }

    @Operation(summary = "搜索知识库条目")
    @GetMapping("/entries")
    public ApiResponse<PageResponse<AppKnowledgeSearchResult>> search(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        return ApiResponse.ok(appKnowledgeService.search(page, size, keyword, categoryId));
    }

    @Operation(summary = "知识库条目详情")
    @GetMapping("/entries/{id}")
    public ApiResponse<AppKnowledgeEntryResponse> entryDetail(@PathVariable Long id) {
        return ApiResponse.ok(appKnowledgeService.entryDetail(id));
    }
}
