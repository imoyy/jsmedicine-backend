package com.gugugaga.jsmedicine.module.user.controller;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.module.user.dto.OrganizationResponse;
import com.gugugaga.jsmedicine.module.user.dto.OrganizationUpsertRequest;
import com.gugugaga.jsmedicine.module.user.dto.PracticeTypeResponse;
import com.gugugaga.jsmedicine.module.user.dto.PracticeTypeUpsertRequest;
import com.gugugaga.jsmedicine.module.user.service.AdminReferenceService;
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

@Tag(name = "管理端基础数据")
@RestController
@RequestMapping("/api/v1/admin/references")
public class AdminReferenceController {

    private final AdminReferenceService adminReferenceService;

    public AdminReferenceController(AdminReferenceService adminReferenceService) {
        this.adminReferenceService = adminReferenceService;
    }

    @Operation(summary = "查询机构列表")
    @PreAuthorize("hasAuthority('sys:reference:view')")
    @GetMapping("/organizations")
    public ApiResponse<List<OrganizationResponse>> organizations(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String provinceCode,
            @RequestParam(required = false) String cityCode,
            @RequestParam(required = false) String districtCode,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminReferenceService.organizations(keyword, provinceCode, cityCode, districtCode, status));
    }

    @Operation(summary = "查询机构详情")
    @PreAuthorize("hasAuthority('sys:reference:view')")
    @GetMapping("/organizations/{id}")
    public ApiResponse<OrganizationResponse> getOrganization(@PathVariable Long id) {
        return ApiResponse.ok(adminReferenceService.getOrganization(id));
    }

    @Operation(summary = "新增机构")
    @PreAuthorize("hasAuthority('sys:reference:create')")
    @PostMapping("/organizations")
    public ApiResponse<OrganizationResponse> createOrganization(@Valid @RequestBody OrganizationUpsertRequest request) {
        return ApiResponse.ok(adminReferenceService.createOrganization(request));
    }

    @Operation(summary = "修改机构")
    @PreAuthorize("hasAuthority('sys:reference:update')")
    @PutMapping("/organizations/{id}")
    public ApiResponse<OrganizationResponse> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationUpsertRequest request
    ) {
        return ApiResponse.ok(adminReferenceService.updateOrganization(id, request));
    }

    @Operation(summary = "删除机构")
    @PreAuthorize("hasAuthority('sys:reference:delete')")
    @DeleteMapping("/organizations/{id}")
    public ApiResponse<Void> deleteOrganization(@PathVariable Long id) {
        adminReferenceService.deleteOrganization(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "查询执业类型列表")
    @PreAuthorize("hasAuthority('sys:reference:view')")
    @GetMapping("/practice-types")
    public ApiResponse<List<PracticeTypeResponse>> practiceTypes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminReferenceService.practiceTypes(keyword, parentId, status));
    }

    @Operation(summary = "查询执业类型详情")
    @PreAuthorize("hasAuthority('sys:reference:view')")
    @GetMapping("/practice-types/{id}")
    public ApiResponse<PracticeTypeResponse> getPracticeType(@PathVariable Long id) {
        return ApiResponse.ok(adminReferenceService.getPracticeType(id));
    }

    @Operation(summary = "新增执业类型")
    @PreAuthorize("hasAuthority('sys:reference:create')")
    @PostMapping("/practice-types")
    public ApiResponse<PracticeTypeResponse> createPracticeType(@Valid @RequestBody PracticeTypeUpsertRequest request) {
        return ApiResponse.ok(adminReferenceService.createPracticeType(request));
    }

    @Operation(summary = "修改执业类型")
    @PreAuthorize("hasAuthority('sys:reference:update')")
    @PutMapping("/practice-types/{id}")
    public ApiResponse<PracticeTypeResponse> updatePracticeType(
            @PathVariable Long id,
            @Valid @RequestBody PracticeTypeUpsertRequest request
    ) {
        return ApiResponse.ok(adminReferenceService.updatePracticeType(id, request));
    }

    @Operation(summary = "删除执业类型")
    @PreAuthorize("hasAuthority('sys:reference:delete')")
    @DeleteMapping("/practice-types/{id}")
    public ApiResponse<Void> deletePracticeType(@PathVariable Long id) {
        adminReferenceService.deletePracticeType(id);
        return ApiResponse.ok();
    }
}
