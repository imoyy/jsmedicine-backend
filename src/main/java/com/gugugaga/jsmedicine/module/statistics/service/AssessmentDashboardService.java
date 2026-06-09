package com.gugugaga.jsmedicine.module.statistics.service;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.enums.ExamRecordStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.learning.assessment.entity.ExamAssessment;
import com.gugugaga.jsmedicine.module.learning.assessment.service.ExamAssessmentSupportService;
import com.gugugaga.jsmedicine.module.learning.question.entity.ExamPaper;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentDashboardOverviewResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentDashboardResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentDistributionResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentLatestEventResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentPaperStructureResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentParticipantResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentParticipantRow;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentQuestionStructureRow;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentQuestionTypeBreakdownResponse;
import com.gugugaga.jsmedicine.module.statistics.dto.AssessmentTrendPointResponse;
import com.gugugaga.jsmedicine.module.statistics.mapper.AssessmentDashboardMapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AssessmentDashboardService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final List<Integer> SUPPORTED_BUCKET_MINUTES = List.of(5, 10, 30);

    private final AssessmentDashboardMapper assessmentDashboardMapper;
    private final ExamAssessmentSupportService assessmentSupportService;

    public AssessmentDashboardService(
            AssessmentDashboardMapper assessmentDashboardMapper,
            ExamAssessmentSupportService assessmentSupportService
    ) {
        this.assessmentDashboardMapper = assessmentDashboardMapper;
        this.assessmentSupportService = assessmentSupportService;
    }

    public AssessmentDashboardResponse dashboard(Long assessmentId, String dimension, Integer bucketMinutes) {
        ExamAssessment assessment = assessmentSupportService.requireAssessment(assessmentId);
        ExamPaper paper = assessmentSupportService.requireExamPaper(assessment.getPaperId());
        LocalDateTime now = LocalDateTime.now();
        AssessmentStatus status = assessmentSupportService.resolveDisplayStatus(assessment, now);
        List<AssessmentParticipantRow> rows = assessmentDashboardMapper.selectParticipantRows(assessmentId);
        AssessmentDashboardOverviewResponse overview = buildOverview(assessment, paper, status, now, rows);
        AssessmentPaperStructureResponse paperStructure = buildPaperStructure(paper, assessmentId);
        List<AssessmentDistributionResponse> distribution = buildDistribution(rows, status, requireDimension(dimension));
        List<AssessmentTrendPointResponse> trend = buildTrend(rows, assessment, now, normalizeBucketMinutes(bucketMinutes));
        List<AssessmentLatestEventResponse> latestEvents = assessmentDashboardMapper.selectLatestEvents(assessmentId, 20L);
        return new AssessmentDashboardResponse(overview, paperStructure, distribution, trend, latestEvents);
    }

    public PageResponse<AssessmentParticipantResponse> pageParticipants(
            Long assessmentId,
            long page,
            long size,
            String status,
            String dimensionCode,
            String keyword
    ) {
        List<AssessmentParticipantResponse> all = listParticipants(assessmentId, status, dimensionCode, keyword);
        long normalizedPage = normalizePage(page);
        long normalizedSize = normalizeSize(size);
        int fromIndex = (int) Math.min(all.size(), Math.max(0, (normalizedPage - 1) * normalizedSize));
        int toIndex = (int) Math.min(all.size(), fromIndex + normalizedSize);
        return new PageResponse<>(all.subList(fromIndex, toIndex), all.size(), normalizedPage, normalizedSize);
    }

    public byte[] exportParticipants(Long assessmentId, String status, String dimensionCode, String keyword) {
        List<AssessmentParticipantResponse> rows = listParticipants(assessmentId, status, dimensionCode, keyword);
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.writeHeadRow(List.of(
                "学员ID", "姓名", "手机号", "脱敏证件号", "省编码", "省份", "市编码", "城市",
                "区编码", "区县", "机构ID", "机构名称", "开始答题时间", "交卷时间", "当前状态", "得分", "是否合格"
        ));
        rows.forEach(row -> writer.writeRow(List.of(
                row.studentId(),
                nullToEmpty(row.studentName()),
                nullToEmpty(row.mobile()),
                nullToEmpty(row.maskedIdCardNo()),
                nullToEmpty(row.provinceCode()),
                nullToEmpty(row.provinceName()),
                nullToEmpty(row.cityCode()),
                nullToEmpty(row.cityName()),
                nullToEmpty(row.districtCode()),
                nullToEmpty(row.districtName()),
                row.organizationId() == null ? "" : row.organizationId(),
                nullToEmpty(row.organizationName()),
                row.enteredAt() == null ? "" : row.enteredAt(),
                row.submittedAt() == null ? "" : row.submittedAt(),
                row.status(),
                row.score() == null ? "" : row.score(),
                row.passed() == null ? "" : row.passed()
        )));
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writer.flush(outputStream, true);
            return outputStream.toByteArray();
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Failed to export assessment participants");
        } finally {
            writer.close();
        }
    }

    private List<AssessmentParticipantResponse> listParticipants(
            Long assessmentId,
            String status,
            String dimensionCode,
            String keyword
    ) {
        ExamAssessment assessment = assessmentSupportService.requireAssessment(assessmentId);
        AssessmentStatus displayStatus = assessmentSupportService.resolveDisplayStatus(assessment, LocalDateTime.now());
        return assessmentDashboardMapper.selectParticipantRows(assessmentId).stream()
                .map(row -> toParticipantResponse(row, displayStatus))
                .filter(row -> matchesStatus(row, status))
                .filter(row -> matchesDimension(row, dimensionCode))
                .filter(row -> matchesKeyword(row, keyword))
                .sorted(Comparator.comparing(AssessmentParticipantResponse::studentId))
                .toList();
    }

    private AssessmentDashboardOverviewResponse buildOverview(
            ExamAssessment assessment,
            ExamPaper paper,
            AssessmentStatus status,
            LocalDateTime now,
            List<AssessmentParticipantRow> rows
    ) {
        long expectedCount = rows.size();
        long actualAttendCount = rows.stream().filter(row -> row.recordId() != null).count();
        long notStartedCount = status == AssessmentStatus.ENDED ? 0L : rows.stream().filter(row -> row.recordId() == null).count();
        long absentCount = status == AssessmentStatus.ENDED ? rows.stream().filter(row -> row.recordId() == null).count() : 0L;
        long inProgressCount = rows.stream().filter(row -> row.recordStatus() == ExamRecordStatus.IN_PROGRESS).count();
        List<AssessmentParticipantRow> completedRows = rows.stream()
                .filter(row -> row.recordStatus() != null && row.recordStatus() != ExamRecordStatus.IN_PROGRESS)
                .toList();
        long completedCount = completedRows.size();
        long passCount = completedRows.stream().filter(row -> Objects.equals(row.passed(), 1)).count();
        long failCount = completedRows.stream().filter(row -> row.passed() != null && row.passed() == 0).count();
        BigDecimal averageScore = completedRows.isEmpty()
                ? BigDecimal.ZERO.setScale(2)
                : completedRows.stream().map(AssessmentParticipantRow::score)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(completedRows.size()), 2, RoundingMode.HALF_UP);
        BigDecimal highestScore = completedRows.stream().map(AssessmentParticipantRow::score).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO.setScale(2));
        BigDecimal lowestScore = completedRows.stream().map(AssessmentParticipantRow::score).filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO.setScale(2));
        BigDecimal passRate = completedCount == 0
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(passCount * 100D / completedCount).setScale(2, RoundingMode.HALF_UP);
        return new AssessmentDashboardOverviewResponse(
                assessment.getId(),
                assessment.getAssessmentName(),
                assessment.getPaperId(),
                paper.getPaperName(),
                assessment.getAssessmentType(),
                status,
                assessment.getStartAt(),
                assessment.getEndAt(),
                now,
                assessmentSupportService.countdownSeconds(status, assessment, now),
                expectedCount,
                actualAttendCount,
                notStartedCount,
                absentCount,
                inProgressCount,
                completedCount,
                passCount,
                failCount,
                passRate,
                averageScore,
                highestScore,
                lowestScore
        );
    }

    private AssessmentPaperStructureResponse buildPaperStructure(ExamPaper paper, Long assessmentId) {
        List<AssessmentQuestionTypeBreakdownResponse> breakdown = assessmentDashboardMapper.selectQuestionStructureRows(assessmentId).stream()
                .map(this::toQuestionTypeBreakdown)
                .toList();
        long questionCount = breakdown.stream().mapToLong(AssessmentQuestionTypeBreakdownResponse::questionCount).sum();
        return new AssessmentPaperStructureResponse(
                paper.getId(),
                paper.getPaperName(),
                paper.getTotalScore(),
                paper.getPassScore(),
                paper.getDurationMinutes(),
                questionCount,
                breakdown
        );
    }

    private List<AssessmentDistributionResponse> buildDistribution(
            List<AssessmentParticipantRow> rows,
            AssessmentStatus assessmentStatus,
            String dimension
    ) {
        Map<String, List<AssessmentParticipantRow>> grouped = rows.stream()
                .collect(Collectors.groupingBy(row -> dimensionCode(row, dimension), LinkedHashMap::new, Collectors.toList()));
        return grouped.entrySet().stream()
                .map(entry -> toDistribution(entry.getKey(), dimensionName(entry.getValue().get(0), dimension), entry.getValue(), assessmentStatus))
                .sorted(Comparator.comparing(AssessmentDistributionResponse::expectedCount).reversed()
                        .thenComparing(AssessmentDistributionResponse::dimensionName, Comparator.nullsLast(String::compareTo)))
                .toList();
    }

    private List<AssessmentTrendPointResponse> buildTrend(
            List<AssessmentParticipantRow> rows,
            ExamAssessment assessment,
            LocalDateTime now,
            int bucketMinutes
    ) {
        LocalDateTime trendEnd = now.isBefore(assessment.getEndAt()) ? now : assessment.getEndAt();
        if (trendEnd.isBefore(assessment.getStartAt())) {
            return List.of();
        }
        LocalDateTime bucketTime = assessment.getStartAt();
        List<AssessmentTrendPointResponse> result = new ArrayList<>();
        while (!bucketTime.isAfter(trendEnd)) {
            LocalDateTime currentBucket = bucketTime;
            long actualAttendCount = rows.stream()
                    .filter(row -> row.startedAt() != null && !row.startedAt().isAfter(currentBucket))
                    .count();
            long inProgressCount = rows.stream()
                    .filter(row -> row.startedAt() != null && !row.startedAt().isAfter(currentBucket))
                    .filter(row -> row.submittedAt() == null || row.submittedAt().isAfter(currentBucket))
                    .count();
            List<AssessmentParticipantRow> completedRows = rows.stream()
                    .filter(row -> row.submittedAt() != null && !row.submittedAt().isAfter(currentBucket))
                    .toList();
            long completedCount = completedRows.size();
            long passCount = completedRows.stream().filter(row -> Objects.equals(row.passed(), 1)).count();
            long failCount = completedRows.stream().filter(row -> row.passed() != null && row.passed() == 0).count();
            result.add(new AssessmentTrendPointResponse(
                    currentBucket,
                    actualAttendCount,
                    inProgressCount,
                    completedCount,
                    passCount,
                    failCount
            ));
            bucketTime = bucketTime.plusMinutes(bucketMinutes);
        }
        return result;
    }

    private AssessmentDistributionResponse toDistribution(
            String dimensionCode,
            String dimensionName,
            List<AssessmentParticipantRow> rows,
            AssessmentStatus assessmentStatus
    ) {
        long expectedCount = rows.size();
        long actualAttendCount = rows.stream().filter(row -> row.recordId() != null).count();
        long absentCount = assessmentStatus == AssessmentStatus.ENDED ? rows.stream().filter(row -> row.recordId() == null).count() : 0L;
        long inProgressCount = rows.stream().filter(row -> row.recordStatus() == ExamRecordStatus.IN_PROGRESS).count();
        List<AssessmentParticipantRow> completedRows = rows.stream()
                .filter(row -> row.recordStatus() != null && row.recordStatus() != ExamRecordStatus.IN_PROGRESS)
                .toList();
        long completedCount = completedRows.size();
        long passCount = completedRows.stream().filter(row -> Objects.equals(row.passed(), 1)).count();
        long failCount = completedRows.stream().filter(row -> row.passed() != null && row.passed() == 0).count();
        BigDecimal passRate = completedCount == 0
                ? BigDecimal.ZERO.setScale(2)
                : BigDecimal.valueOf(passCount * 100D / completedCount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal averageScore = completedRows.isEmpty()
                ? BigDecimal.ZERO.setScale(2)
                : completedRows.stream().map(AssessmentParticipantRow::score).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(completedRows.size()), 2, RoundingMode.HALF_UP);
        return new AssessmentDistributionResponse(
                dimensionCode,
                dimensionName,
                expectedCount,
                actualAttendCount,
                absentCount,
                inProgressCount,
                completedCount,
                passCount,
                failCount,
                passRate,
                averageScore
        );
    }

    private AssessmentParticipantResponse toParticipantResponse(AssessmentParticipantRow row, AssessmentStatus assessmentStatus) {
        return new AssessmentParticipantResponse(
                row.studentId(),
                row.studentName(),
                row.mobile(),
                row.maskedIdCardNo(),
                row.provinceCode(),
                row.provinceName(),
                row.cityCode(),
                row.cityName(),
                row.districtCode(),
                row.districtName(),
                row.organizationId(),
                row.organizationName(),
                row.startedAt(),
                row.submittedAt(),
                participantStatus(row, assessmentStatus),
                row.score(),
                row.passed() == null ? null : row.passed() == 1
        );
    }

    private String participantStatus(AssessmentParticipantRow row, AssessmentStatus assessmentStatus) {
        if (row.recordStatus() == null) {
            return assessmentStatus == AssessmentStatus.ENDED ? "absent" : "not_started";
        }
        return row.recordStatus().getValue();
    }

    private String requireDimension(String dimension) {
        String normalized = dimension == null || dimension.isBlank() ? "province" : dimension.trim();
        if (!List.of("province", "city", "district", "organization").contains(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "dimension must be one of province, city, district or organization");
        }
        return normalized;
    }

    private int normalizeBucketMinutes(Integer bucketMinutes) {
        int normalized = bucketMinutes == null ? 5 : bucketMinutes;
        if (!SUPPORTED_BUCKET_MINUTES.contains(normalized)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "bucketMinutes must be one of 5, 10 or 30");
        }
        return normalized;
    }

    private long normalizePage(long page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(long size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private AssessmentQuestionTypeBreakdownResponse toQuestionTypeBreakdown(AssessmentQuestionStructureRow row) {
        return new AssessmentQuestionTypeBreakdownResponse(
                row.questionType(),
                row.questionCount(),
                row.scorePerQuestion(),
                row.totalScore()
        );
    }

    private boolean matchesStatus(AssessmentParticipantResponse row, String status) {
        return status == null || status.isBlank() || Objects.equals(row.status(), status.trim());
    }

    private boolean matchesDimension(AssessmentParticipantResponse row, String dimensionCode) {
        if (dimensionCode == null || dimensionCode.isBlank()) {
            return true;
        }
        String normalized = dimensionCode.trim();
        return normalized.equals(row.provinceCode())
                || normalized.equals(row.cityCode())
                || normalized.equals(row.districtCode())
                || normalized.equals(String.valueOf(row.organizationId()));
    }

    private boolean matchesKeyword(AssessmentParticipantResponse row, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.trim();
        return contains(row.studentName(), normalized)
                || contains(row.mobile(), normalized)
                || contains(row.organizationName(), normalized);
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.contains(keyword);
    }

    private String dimensionCode(AssessmentParticipantRow row, String dimension) {
        return switch (dimension) {
            case "city" -> nullToEmpty(row.cityCode());
            case "district" -> nullToEmpty(row.districtCode());
            case "organization" -> row.organizationId() == null ? "" : String.valueOf(row.organizationId());
            default -> nullToEmpty(row.provinceCode());
        };
    }

    private String dimensionName(AssessmentParticipantRow row, String dimension) {
        return switch (dimension) {
            case "city" -> nullToEmpty(row.cityName());
            case "district" -> nullToEmpty(row.districtName());
            case "organization" -> nullToEmpty(row.organizationName());
            default -> nullToEmpty(row.provinceName());
        };
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
