package com.gugugaga.jsmedicine.module.system.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.system.dto.AuditRecordPageQuery;
import com.gugugaga.jsmedicine.module.system.dto.AuditRecordResponse;
import com.gugugaga.jsmedicine.module.system.dto.IdListRequest;
import com.gugugaga.jsmedicine.module.system.dto.PasswordResetRequest;
import com.gugugaga.jsmedicine.module.system.dto.StatusUpdateRequest;
import com.gugugaga.jsmedicine.module.system.dto.SysAdminPageQuery;
import com.gugugaga.jsmedicine.module.system.dto.SysAdminResponse;
import com.gugugaga.jsmedicine.module.system.dto.SysAdminUpsertRequest;
import com.gugugaga.jsmedicine.module.system.dto.SysPermissionResponse;
import com.gugugaga.jsmedicine.module.system.dto.SysRolePageQuery;
import com.gugugaga.jsmedicine.module.system.dto.SysRoleResponse;
import com.gugugaga.jsmedicine.module.system.dto.SysRoleUpsertRequest;
import com.gugugaga.jsmedicine.module.system.service.SystemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
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

@Tag(name = "管理端系统管理")
@RestController
@RequestMapping("/api/v1/admin/system")
public class AdminSystemController {

    private final SystemAdminService systemAdminService;

    public AdminSystemController(SystemAdminService systemAdminService) {
        this.systemAdminService = systemAdminService;
    }

    @Operation(summary = "分页查询管理员")
    @PreAuthorize("hasAuthority('sys:admin:view')")
    @GetMapping("/admins")
    public ApiResponse<PageResponse<SysAdminResponse>> pageAdmins(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(systemAdminService.pageAdmins(new SysAdminPageQuery(page, size, sort, keyword)));
    }

    @Operation(summary = "查询管理员详情")
    @PreAuthorize("hasAuthority('sys:admin:view')")
    @GetMapping("/admins/{id}")
    public ApiResponse<SysAdminResponse> getAdmin(@PathVariable Long id) {
        return ApiResponse.ok(systemAdminService.getAdmin(id));
    }

    @Operation(summary = "新增管理员")
    @PreAuthorize("hasAuthority('sys:admin:create')")
    @PostMapping("/admins")
    public ApiResponse<SysAdminResponse> createAdmin(@Valid @RequestBody SysAdminUpsertRequest request) {
        return ApiResponse.ok(systemAdminService.createAdmin(request));
    }

    @Operation(summary = "修改管理员")
    @PreAuthorize("hasAuthority('sys:admin:update')")
    @PutMapping("/admins/{id}")
    public ApiResponse<SysAdminResponse> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody SysAdminUpsertRequest request
    ) {
        return ApiResponse.ok(systemAdminService.updateAdmin(id, request));
    }

    @Operation(summary = "修改管理员状态")
    @PreAuthorize("hasAuthority('sys:admin:disable')")
    @PatchMapping("/admins/{id}/status")
    public ApiResponse<Void> updateAdminStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        systemAdminService.updateAdminStatus(id, request.status());
        return ApiResponse.ok();
    }

    @Operation(summary = "重置管理员密码")
    @PreAuthorize("hasAuthority('sys:admin:reset-password')")
    @PatchMapping("/admins/{id}/password/reset")
    public ApiResponse<Void> resetAdminPassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordResetRequest request
    ) {
        systemAdminService.resetAdminPassword(id, request.password());
        return ApiResponse.ok();
    }

    @Operation(summary = "绑定管理员角色")
    @PreAuthorize("hasAuthority('sys:admin:update')")
    @PutMapping("/admins/{id}/roles")
    public ApiResponse<Void> bindAdminRoles(
            @PathVariable Long id,
            @Valid @RequestBody IdListRequest request
    ) {
        systemAdminService.bindAdminRoles(id, request.ids());
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询角色")
    @PreAuthorize("hasAuthority('sys:role:view')")
    @GetMapping("/roles")
    public ApiResponse<PageResponse<SysRoleResponse>> pageRoles(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(systemAdminService.pageRoles(new SysRolePageQuery(page, size, sort, keyword)));
    }

    @Operation(summary = "查询角色详情")
    @PreAuthorize("hasAuthority('sys:role:view')")
    @GetMapping("/roles/{id}")
    public ApiResponse<SysRoleResponse> getRole(@PathVariable Long id) {
        return ApiResponse.ok(systemAdminService.getRole(id));
    }

    @Operation(summary = "新增角色")
    @PreAuthorize("hasAuthority('sys:role:create')")
    @PostMapping("/roles")
    public ApiResponse<SysRoleResponse> createRole(@Valid @RequestBody SysRoleUpsertRequest request) {
        return ApiResponse.ok(systemAdminService.createRole(request));
    }

    @Operation(summary = "修改角色")
    @PreAuthorize("hasAuthority('sys:role:update')")
    @PutMapping("/roles/{id}")
    public ApiResponse<SysRoleResponse> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody SysRoleUpsertRequest request
    ) {
        return ApiResponse.ok(systemAdminService.updateRole(id, request));
    }

    @Operation(summary = "修改角色状态")
    @PreAuthorize("hasAuthority('sys:role:disable')")
    @PatchMapping("/roles/{id}/status")
    public ApiResponse<Void> updateRoleStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        systemAdminService.updateRoleStatus(id, request.status());
        return ApiResponse.ok();
    }

    @Operation(summary = "绑定角色权限")
    @PreAuthorize("hasAuthority('sys:role:permission')")
    @PutMapping("/roles/{id}/permissions")
    public ApiResponse<Void> bindRolePermissions(
            @PathVariable Long id,
            @Valid @RequestBody IdListRequest request
    ) {
        systemAdminService.bindRolePermissions(id, request.ids());
        return ApiResponse.ok();
    }

    @Operation(summary = "查询权限列表")
    @PreAuthorize("hasAuthority('sys:permission:view')")
    @GetMapping("/permissions")
    public ApiResponse<List<SysPermissionResponse>> listPermissions() {
        return ApiResponse.ok(systemAdminService.listPermissions());
    }

    @Operation(summary = "分页查询操作审计记录")
    @PreAuthorize("hasAuthority('sys:audit:view')")
    @GetMapping("/audit-records")
    public ApiResponse<PageResponse<AuditRecordResponse>> pageAuditRecords(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) Long auditorId
    ) {
        return ApiResponse.ok(systemAdminService.pageAuditRecords(
                new AuditRecordPageQuery(page, size, sort, targetType, targetId, auditorId)
        ));
    }
}
