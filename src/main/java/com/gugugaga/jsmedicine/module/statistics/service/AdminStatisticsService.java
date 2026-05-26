package com.gugugaga.jsmedicine.module.statistics.service;

import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.statistics.dto.ContentInteractionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamPaperScoreResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.ExamScoreSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.RegionStatisticsResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StatisticsQuery;
import com.gugugaga.jsmedicine.module.statistics.dto.StudentSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursResourceResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.StudyHoursSummaryResponse;
import com.gugugaga.jsmedicine.module.statistics.mapper.AdminStatisticsMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminStatisticsService {

    private final AdminStatisticsMapper statisticsMapper;

    public AdminStatisticsService(AdminStatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    public StudyHoursSummaryResponse studyHoursSummary(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectStudyHoursSummary(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.resourceId(),
                normalized.studentId(), normalized.province(), normalized.city());
    }

    public List<StudyHoursResourceResponse> studyHoursByResource(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectStudyHoursByResource(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.studentId(),
                normalized.province(), normalized.city());
    }

    public StudentSummaryResponse studentSummary(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectStudentSummary(
                normalized.startAt(), normalized.endAt(), normalized.province(), normalized.city());
    }

    public List<RegionStatisticsResponse> regionStatistics(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectRegionStatistics(
                normalized.startAt(), normalized.endAt(), normalized.province(), normalized.city());
    }

    public ExamScoreSummaryResponse examScoreSummary(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectExamScoreSummary(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.resourceId(),
                normalized.studentId(), normalized.province(), normalized.city());
    }

    public List<ExamPaperScoreResponse> examScoresByPaper(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectExamScoresByPaper(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.resourceId(),
                normalized.studentId(), normalized.province(), normalized.city());
    }

    public List<ContentInteractionStatisticsResponse> contentInteractions(StatisticsQuery query) {
        StatisticsQuery normalized = normalize(query);
        return statisticsMapper.selectContentInteractionStatistics(
                normalized.startAt(), normalized.endAt(), normalized.resourceType(), normalized.resourceId());
    }

    private StatisticsQuery normalize(StatisticsQuery query) {
        LocalDateTime endAt = query.endAt() == null ? LocalDateTime.now() : query.endAt();
        LocalDateTime startAt = query.startAt() == null ? endAt.minusDays(30) : query.startAt();
        if (!startAt.isBefore(endAt)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "startAt must be before endAt");
        }
        return new StatisticsQuery(
                startAt,
                endAt,
                blankToNull(query.resourceType()),
                query.resourceId(),
                query.studentId(),
                blankToNull(query.province()),
                blankToNull(query.city())
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
