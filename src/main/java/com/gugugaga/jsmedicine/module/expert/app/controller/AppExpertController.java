package com.gugugaga.jsmedicine.module.expert.app.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertCategoryResponse;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertResponse;
import com.gugugaga.jsmedicine.module.expert.app.service.AppExpertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户端专家")
@RestController
@RequestMapping("/api/v1/app/experts")
public class AppExpertController {

    private final AppExpertService appExpertService;

    public AppExpertController(AppExpertService appExpertService) {
        this.appExpertService = appExpertService;
    }

    @Operation(summary = "分页查询专家分类")
    @GetMapping("/categories")
    public ApiResponse<PageResponse<AppExpertCategoryResponse>> pageCategories(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long parentId
    ) {
        return ApiResponse.ok(appExpertService.pageCategories(page, size, keyword, parentId));
    }

    @Operation(summary = "分页查询可咨询专家")
    @GetMapping
    public ApiResponse<PageResponse<AppExpertResponse>> pageExperts(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId
    ) {
        return ApiResponse.ok(appExpertService.pageExperts(page, size, keyword, categoryId));
    }

    @Operation(summary = "专家详情")
    @GetMapping("/{id}")
    public ApiResponse<AppExpertResponse> expertDetail(@PathVariable Long id) {
        return ApiResponse.ok(appExpertService.expertDetail(id));
    }
}
