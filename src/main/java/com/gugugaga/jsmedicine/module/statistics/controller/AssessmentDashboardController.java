package com.gugugaga.jsmedicine.module.statistics.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentDashboardResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentParticipantResponse;
import com.gugugaga.jsmedicine.module.statistics.service.AssessmentDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "管理端统计管理")
@RestController
@RequestMapping("/api/v1/admin/statistics/exam-assessments")
public class AssessmentDashboardController {

    private static final DateTimeFormatter EXPORT_FILE_NAME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AssessmentDashboardService assessmentDashboardService;

    public AssessmentDashboardController(AssessmentDashboardService assessmentDashboardService) {
        this.assessmentDashboardService = assessmentDashboardService;
    }

    @Operation(summary = "查询考核大屏聚合数据")
    @PreAuthorize("hasAuthority('statistics:dashboard:view')")
    @GetMapping("/{id}/dashboard")
    public ApiResponse<AssessmentDashboardResponse> dashboard(
            @PathVariable Long id,
            @RequestParam(required = false) String dimension,
            @RequestParam(required = false) Integer bucketMinutes
    ) {
        return ApiResponse.ok(assessmentDashboardService.dashboard(id, dimension, bucketMinutes));
    }

    @Operation(summary = "分页查询考核参与明细")
    @PreAuthorize("hasAuthority('statistics:dashboard:view')")
    @GetMapping("/{id}/participants")
    public ApiResponse<PageResponse<AssessmentParticipantResponse>> pageParticipants(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dimensionCode,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.ok(assessmentDashboardService.pageParticipants(id, page, size, status, dimensionCode, keyword));
    }

    @Operation(summary = "导出考核参与明细")
    @PreAuthorize("hasAuthority('statistics:dashboard:view')")
    @GetMapping("/{id}/participants/export")
    public ResponseEntity<byte[]> exportParticipants(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String dimensionCode,
            @RequestParam(required = false) String keyword
    ) {
        byte[] content = assessmentDashboardService.exportParticipants(id, status, dimensionCode, keyword);
        String fileName = "assessment-participants-" + LocalDateTime.now().format(EXPORT_FILE_NAME_FORMATTER) + ".xlsx";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .body(content);
    }
}
