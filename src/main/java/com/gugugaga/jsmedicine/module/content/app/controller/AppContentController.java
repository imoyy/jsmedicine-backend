package com.gugugaga.jsmedicine.module.content.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.content.app.dto.AppArticleResponse;
import com.gugugaga.jsmedicine.module.content.app.service.AppContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端内容")
@RestController
@RequestMapping("/api/v1/app/content")
public class AppContentController {

    private final AppContentService appContentService;

    public AppContentController(AppContentService appContentService) {
        this.appContentService = appContentService;
    }

    @Operation(summary = "分页查询资讯")
    @GetMapping("/articles")
    public ApiResponse<PageResponse<AppArticleResponse>> pageArticles(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(appContentService.pageArticles(page, size, sort, keyword));
    }

    @Operation(summary = "资讯详情")
    @GetMapping("/articles/{id}")
    public ApiResponse<AppArticleResponse> articleDetail(@PathVariable Long id) {
        return ApiResponse.ok(appContentService.articleDetail(id));
    }
}
