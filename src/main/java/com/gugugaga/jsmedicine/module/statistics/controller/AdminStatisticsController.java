package com.gugugaga.jsmedicine.module.statistics.controller;

import com.gugugaga.jsmedicine.common.response.ApiResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ContentInteractionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamPaperScoreResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamScoreSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.RegionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StatisticsQuery;
import com.gugugaga.jsmedicine.module.statistics.dto.StudentSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursResourceResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.service.AdminStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "管理端统计管理")
@RestController
@RequestMapping("/api/v1/admin/statistics")
public class AdminStatisticsController {

    private final AdminStatisticsService statisticsService;

    public AdminStatisticsController(AdminStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "查询学时统计汇总")
    @PreAuthorize("hasAuthority('statistics:view')")
    @GetMapping("/study-hours/summary")
    public ApiResponse<StudyHoursSummaryResponse> studyHoursSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city
    ) {
        return ApiResponse.ok(statisticsService.studyHoursSummary(
                query(startAt, endAt, resourceType, resourceId, studentId, province, city)));
    }

    @Operation(summary = "按资源类型查询学时统计")
    @PreAuthorize("hasAuthority('statistics:view')")
    @GetMapping("/study-hours/resources")
    public ApiResponse<List<StudyHoursResourceResponse>> studyHoursByResource(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city
    ) {
        return ApiResponse.ok(statisticsService.studyHoursByResource(
                query(startAt, endAt, resourceType, null, studentId, province, city)));
    }

    @Operation(summary = "查询学员统计汇总")
    @PreAuthorize("hasAuthority('statistics:view')")
    @GetMapping("/students/summary")
    public ApiResponse<StudentSummaryResponse> studentSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city
    ) {
        return ApiResponse.ok(statisticsService.studentSummary(
                query(startAt, endAt, null, null, null, province, city)));
    }

    @Operation(summary = "查询地区学员统计")
    @PreAuthorize("hasAuthority('statistics:view')")
    @GetMapping("/regions")
    public ApiResponse<List<RegionStatisticsResponse>> regionStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city
    ) {
        return ApiResponse.ok(statisticsService.regionStatistics(
                query(startAt, endAt, null, null, null, province, city)));
    }

    @Operation(summary = "查询成绩统计汇总")
    @PreAuthorize("hasAuthority('statistics:view')")
    @GetMapping("/exam-scores/summary")
    public ApiResponse<ExamScoreSummaryResponse> examScoreSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city
    ) {
        return ApiResponse.ok(statisticsService.examScoreSummary(
                query(startAt, endAt, resourceType, resourceId, studentId, province, city)));
    }

    @Operation(summary = "按试卷查询成绩统计")
    @PreAuthorize("hasAuthority('statistics:view')")
    @GetMapping("/exam-scores/papers")
    public ApiResponse<List<ExamPaperScoreResponse>> examScoresByPaper(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long resourceId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city
    ) {
        return ApiResponse.ok(statisticsService.examScoresByPaper(
                query(startAt, endAt, resourceType, resourceId, studentId, province, city)));
    }

    @Operation(summary = "查询内容浏览收藏分享统计")
    @PreAuthorize("hasAuthority('statistics:view')")
    @GetMapping("/content-interactions")
    public ApiResponse<List<ContentInteractionStatisticsResponse>> contentInteractions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long resourceId
    ) {
        return ApiResponse.ok(statisticsService.contentInteractions(
                query(startAt, endAt, resourceType, resourceId, null, null, null)));
    }

    private StatisticsQuery query(
            LocalDateTime startAt,
            LocalDateTime endAt,
            String resourceType,
            Long resourceId,
            Long studentId,
            String province,
            String city
    ) {
        return new StatisticsQuery(startAt, endAt, resourceType, resourceId, studentId, province, city);
    }
}
