package com.gugugaga.jsmedicine.module.user.controller;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.system.dto.IdListRequest;
import com.gugugaga.jsmedicine.module.system.dto.StatusUpdateRequest;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentImportResponse;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentPageQuery;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentResponse;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentUpsertRequest;
import com.gugugaga.jsmedicine.module.user.dto.AdminUserPageQuery;
import com.gugugaga.jsmedicine.module.user.dto.AdminUserResponse;
import com.gugugaga.jsmedicine.module.user.dto.AdminUserUpdateRequest;
import com.gugugaga.jsmedicine.module.user.dto.StudentCertificationReviewRequest;
import com.gugugaga.jsmedicine.module.user.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "管理端用户与学员")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private static final DateTimeFormatter EXPORT_FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(summary = "分页查询用户")
    @PreAuthorize("hasAuthority('sys:user:view')")
    @GetMapping("/users")
    public ApiResponse<PageResponse<AdminUserResponse>> pageUsers(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EnabledStatus status
    ) {
        return ApiResponse.ok(adminUserService.pageUsers(new AdminUserPageQuery(page, size, sort, keyword, status)));
    }

    @Operation(summary = "查询用户详情")
    @PreAuthorize("hasAuthority('sys:user:view')")
    @GetMapping("/users/{id}")
    public ApiResponse<AdminUserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.ok(adminUserService.getUser(id));
    }

    @Operation(summary = "修改用户信息")
    @PreAuthorize("hasAuthority('sys:user:update')")
    @PutMapping("/users/{id}")
    public ApiResponse<AdminUserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequest request
    ) {
        return ApiResponse.ok(adminUserService.updateUser(id, request));
    }

    @Operation(summary = "修改用户状态")
    @PreAuthorize("hasAuthority('sys:user:update')")
    @PatchMapping("/users/{id}/status")
    public ApiResponse<Void> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request
    ) {
        adminUserService.updateUserStatus(id, request.status());
        return ApiResponse.ok();
    }

    @Operation(summary = "分页查询学员")
    @PreAuthorize("hasAuthority('sys:student:view')")
    @GetMapping("/students")
    public ApiResponse<PageResponse<AdminStudentResponse>> pageStudents(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EnabledStatus status,
            @RequestParam(required = false) StudentCertificationStatus certificationStatus
    ) {
        return ApiResponse.ok(adminUserService.pageStudents(
                new AdminStudentPageQuery(page, size, sort, keyword, status, certificationStatus)
        ));
    }

    @Operation(summary = "查询学员详情")
    @PreAuthorize("hasAuthority('sys:student:view')")
    @GetMapping("/students/{id}")
    public ApiResponse<AdminStudentResponse> getStudent(@PathVariable Long id) {
        return ApiResponse.ok(adminUserService.getStudent(id));
    }

    @Operation(summary = "新增学员")
    @PreAuthorize("hasAuthority('sys:student:create')")
    @PostMapping("/students")
    public ApiResponse<AdminStudentResponse> createStudent(@Valid @RequestBody AdminStudentUpsertRequest request) {
        return ApiResponse.ok(adminUserService.createStudent(request));
    }

    @Operation(summary = "导入学员")
    @PreAuthorize("hasAuthority('sys:student:import')")
    @PostMapping(value = "/students/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AdminStudentImportResponse> importStudents(@RequestPart("file") MultipartFile file) {
        return ApiResponse.ok(adminUserService.importStudents(file));
    }

    @Operation(summary = "导出学员")
    @PreAuthorize("hasAuthority('sys:student:export')")
    @GetMapping("/students/export")
    public ResponseEntity<byte[]> exportStudents(
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EnabledStatus status,
            @RequestParam(required = false) StudentCertificationStatus certificationStatus
    ) {
        byte[] content = adminUserService.exportStudents(new AdminStudentPageQuery(1, 20, sort, keyword, status, certificationStatus));
        String fileName = "students-" + LocalDateTime.now().format(EXPORT_FILE_NAME_FORMATTER) + ".xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .body(content);
    }

    @Operation(summary = "维护学员信息")
    @PreAuthorize("hasAuthority('sys:student:update')")
    @PutMapping("/students/{id}")
    public ApiResponse<AdminStudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody AdminStudentUpsertRequest request
    ) {
        return ApiResponse.ok(adminUserService.updateStudent(id, request));
    }

    @Operation(summary = "删除学员")
    @PreAuthorize("hasAuthority('sys:student:delete')")
    @DeleteMapping("/students/{id}")
    public ApiResponse<Void> deleteStudent(@PathVariable Long id) {
        adminUserService.deleteStudent(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "批量删除学员")
    @PreAuthorize("hasAuthority('sys:student:batch-delete')")
    @PostMapping("/students/batch-delete")
    public ApiResponse<Void> batchDeleteStudents(@Valid @RequestBody IdListRequest request) {
        adminUserService.batchDeleteStudents(request.ids());
        return ApiResponse.ok();
    }

    @Operation(summary = "审核学员认证")
    @PreAuthorize("hasAuthority('sys:student:review')")
    @PatchMapping("/students/{id}/certification")
    public ApiResponse<AdminStudentResponse> reviewCertification(
            @PathVariable Long id,
            @Valid @RequestBody StudentCertificationReviewRequest request
    ) {
        return ApiResponse.ok(adminUserService.reviewCertification(id, request));
    }
}
