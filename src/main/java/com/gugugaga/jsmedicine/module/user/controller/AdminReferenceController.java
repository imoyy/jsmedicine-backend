package com.gugugaga.jsmedicine.module.user.controller;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.module.user.dto.OrganizationResponse;
import com.gugugaga.jsmedicine.module.user.dto.PracticeTypeResponse;
import com.gugugaga.jsmedicine.module.user.service.AdminReferenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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

    @Operation(summary = "查询执业类型列表")
    @PreAuthorize("hasAuthority('sys:reference:view')")
    @GetMapping("/practice-types")
    public ApiResponse<List<PracticeTypeResponse>> practiceTypes(
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminReferenceService.practiceTypes(parentId, status));
    }
}
