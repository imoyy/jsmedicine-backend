package com.gugugaga.jsmedicine.module.expert.admin.controller;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertCategoryRequest;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertCategoryResponse;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertExperienceRequest;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertExperienceResponse;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertRequest;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertResponse;
import com.gugugaga.jsmedicine.module.expert.admin.service.AdminExpertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理端专家管理")
@RestController
@RequestMapping("/api/v1/admin/experts")
public class AdminExpertController {

    private final AdminExpertService adminExpertService;

    public AdminExpertController(AdminExpertService adminExpertService) {
        this.adminExpertService = adminExpertService;
    }

    @Operation(summary = "分页查询专家分类")
    @PreAuthorize("hasAuthority('expert:category:view')")
    @GetMapping("/categories")
    public ApiResponse<PageResponse<ExpertCategoryResponse>> pageCategories(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminExpertService.pageCategories(page, size, keyword, parentId, status));
    }

    @Operation(summary = "新增专家分类")
    @PreAuthorize("hasAuthority('expert:category:edit')")
    @PostMapping("/categories")
    public ApiResponse<ExpertCategoryResponse> createCategory(@Valid @RequestBody ExpertCategoryRequest request) {
        return ApiResponse.ok(adminExpertService.createCategory(request));
    }

    @Operation(summary = "修改专家分类")
    @PreAuthorize("hasAuthority('expert:category:edit')")
    @PutMapping("/categories/{id}")
    public ApiResponse<ExpertCategoryResponse> updateCategory(@PathVariable Long id, @Valid @RequestBody ExpertCategoryRequest request) {
        return ApiResponse.ok(adminExpertService.updateCategory(id, request));
    }

    @Operation(summary = "删除专家分类")
    @PreAuthorize("hasAuthority('expert:category:edit')")
    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        adminExpertService.deleteCategory(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询专家")
    @PreAuthorize("hasAuthority('expert:view')")
    @GetMapping
    public ApiResponse<PageResponse<ExpertResponse>> pageExperts(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminExpertService.pageExperts(page, size, keyword, categoryId, status));
    }

    @Operation(summary = "专家详情")
    @PreAuthorize("hasAuthority('expert:view')")
    @GetMapping("/{id}")
    public ApiResponse<ExpertResponse> expertDetail(@PathVariable Long id) {
        return ApiResponse.ok(adminExpertService.expertDetail(id));
    }

    @Operation(summary = "新增专家")
    @PreAuthorize("hasAuthority('expert:edit')")
    @PostMapping
    public ApiResponse<ExpertResponse> createExpert(@Valid @RequestBody ExpertRequest request) {
        return ApiResponse.ok(adminExpertService.createExpert(request));
    }

    @Operation(summary = "修改专家")
    @PreAuthorize("hasAuthority('expert:edit')")
    @PutMapping("/{id}")
    public ApiResponse<ExpertResponse> updateExpert(@PathVariable Long id, @Valid @RequestBody ExpertRequest request) {
        return ApiResponse.ok(adminExpertService.updateExpert(id, request));
    }

    @Operation(summary = "删除专家")
    @PreAuthorize("hasAuthority('expert:edit')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteExpert(@PathVariable Long id) {
        adminExpertService.deleteExpert(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "替换专家分类")
    @PreAuthorize("hasAuthority('expert:edit')")
    @PutMapping("/{id}/categories")
    public ApiResponse<List<Long>> replaceExpertCategories(@PathVariable Long id, @RequestBody List<Long> categoryIds) {
        return ApiResponse.ok(adminExpertService.replaceExpertCategories(id, categoryIds));
    }

    @Operation(summary = "替换专家履历")
    @PreAuthorize("hasAuthority('expert:edit')")
    @PutMapping("/{id}/experiences")
    public ApiResponse<List<ExpertExperienceResponse>> replaceExperiences(
            @PathVariable Long id,
            @Valid @RequestBody List<ExpertExperienceRequest> requests
    ) {
        return ApiResponse.ok(adminExpertService.replaceExperiences(id, requests));
    }
}
