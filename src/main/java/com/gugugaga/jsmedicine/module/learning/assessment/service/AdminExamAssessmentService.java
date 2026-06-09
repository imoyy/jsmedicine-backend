package com.gugugaga.jsmedicine.module.learning.assessment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.AssessmentType;
import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminExamAssessmentDetailResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminExamAssessmentListResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminExamAssessmentScopeSummaryResponse;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminExamAssessmentStatusUpdateRequest;
import com.gugugaga.jsmedicine.module.learning.admin.dto.AdminExamAssessmentUpsertRequest;
import com.gugugaga.jsmedicine.module.learning.assessment.entity.ExamAssessment;
import com.gugugaga.jsmedicine.module.learning.assessment.entity.ExamAssessmentOrganization;
import com.gugugaga.jsmedicine.module.learning.assessment.entity.ExamAssessmentStudent;
import com.gugugaga.jsmedicine.module.learning.assessment.mapper.ExamAssessmentMapper;
import com.gugugaga.jsmedicine.module.learning.assessment.mapper.ExamAssessmentOrganizationMapper;
import com.gugugaga.jsmedicine.module.learning.assessment.mapper.ExamAssessmentStudentMapper;
import com.gugugaga.jsmedicine.module.learning.question.entity.ExamPaper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AdminExamAssessmentService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final ExamAssessmentMapper examAssessmentMapper;
    private final ExamAssessmentOrganizationMapper examAssessmentOrganizationMapper;
    private final ExamAssessmentStudentMapper examAssessmentStudentMapper;
    private final ExamAssessmentSupportService assessmentSupportService;

    public AdminExamAssessmentService(
            ExamAssessmentMapper examAssessmentMapper,
            ExamAssessmentOrganizationMapper examAssessmentOrganizationMapper,
            ExamAssessmentStudentMapper examAssessmentStudentMapper,
            ExamAssessmentSupportService assessmentSupportService
    ) {
        this.examAssessmentMapper = examAssessmentMapper;
        this.examAssessmentOrganizationMapper = examAssessmentOrganizationMapper;
        this.examAssessmentStudentMapper = examAssessmentStudentMapper;
        this.assessmentSupportService = assessmentSupportService;
    }

    public PageResponse<AdminExamAssessmentListResponse> pageAssessments(
            long page,
            long size,
            String sort,
            String keyword,
            AssessmentType assessmentType,
            AssessmentStatus status,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        LocalDateTime now = LocalDateTime.now();
        Page<ExamAssessment> resultPage = examAssessmentMapper.selectPage(
                new Page<>(normalizePage(page), normalizeSize(size)),
                buildAssessmentQuery(sort, keyword, assessmentType, status, startAt, endAt, now));
        Map<Long, String> paperNames = loadPaperNames(resultPage.getRecords().stream()
                .map(ExamAssessment::getPaperId)
                .toList());
        Map<Long, Long> organizationCounts = loadOrganizationCounts(resultPage.getRecords().stream().map(ExamAssessment::getId).toList());
        Map<Long, Long> explicitCounts = loadExplicitCounts(resultPage.getRecords().stream().map(ExamAssessment::getId).toList());
        List<AdminExamAssessmentListResponse> records = resultPage.getRecords().stream()
                .map(assessment -> toListResponse(
                        assessment,
                        paperNames.get(assessment.getPaperId()),
                        organizationCounts.getOrDefault(assessment.getId(), 0L),
                        explicitCounts.getOrDefault(assessment.getId(), 0L),
                        now))
                .toList();
        return new PageResponse<>(records, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    public AdminExamAssessmentDetailResponse getAssessment(Long id) {
        ExamAssessment assessment = assessmentSupportService.requireAssessment(id);
        List<Long> organizationIds = examAssessmentOrganizationMapper.selectList(new LambdaQueryWrapper<ExamAssessmentOrganization>()
                        .eq(ExamAssessmentOrganization::getAssessmentId, id))
                .stream()
                .map(ExamAssessmentOrganization::getOrganizationId)
                .toList();
        List<ExamAssessmentStudent> snapshots = examAssessmentStudentMapper.selectList(new LambdaQueryWrapper<ExamAssessmentStudent>()
                .eq(ExamAssessmentStudent::getAssessmentId, id));
        List<Long> explicitStudentIds = snapshots.stream()
                .filter(snapshot -> "explicit".equals(snapshot.getAssignSource()))
                .map(ExamAssessmentStudent::getStudentId)
                .toList();
        return new AdminExamAssessmentDetailResponse(
                assessment.getId(),
                assessment.getAssessmentName(),
                assessment.getPaperId(),
                assessmentSupportService.requireExamPaper(assessment.getPaperId()).getPaperName(),
                assessment.getAssessmentType(),
                assessmentSupportService.resolveDisplayStatus(assessment, LocalDateTime.now()),
                assessment.getStartAt(),
                assessment.getEndAt(),
                assessment.getProvinceCode(),
                assessment.getCityCode(),
                assessment.getDistrictCode(),
                organizationIds,
                explicitStudentIds,
                new AdminExamAssessmentScopeSummaryResponse(
                        assessment.getProvinceCode(),
                        assessment.getCityCode(),
                        assessment.getDistrictCode(),
                        organizationIds.size(),
                        explicitStudentIds.size(),
                        snapshots.size()
                )
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminExamAssessmentDetailResponse createAssessment(AdminExamAssessmentUpsertRequest request) {
        List<Long> organizationIds = assessmentSupportService.normalizeIds(request.organizationIds(), "organizationIds");
        List<Long> explicitStudentIds = assessmentSupportService.normalizeIds(request.studentIds(), "studentIds");
        validateScope(request.provinceCode(), request.cityCode(), request.districtCode(), organizationIds, explicitStudentIds);
        assessmentSupportService.requireExamPaper(request.paperId());
        assessmentSupportService.loadOrganizations(organizationIds);
        assessmentSupportService.validateAssessmentWindow(request.startAt(), request.endAt());
        assessmentSupportService.validateRegionScope(request.provinceCode(), request.cityCode(), request.districtCode());

        ExamAssessment assessment = new ExamAssessment();
        fillAssessment(assessment, request);
        assessment.setStatus(AssessmentStatus.NOT_STARTED);
        assessment.setDeleted(0);
        examAssessmentMapper.insert(assessment);
        rebuildScope(assessment.getId(), request.provinceCode(), request.cityCode(), request.districtCode(), organizationIds, explicitStudentIds);
        return getAssessment(assessment.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminExamAssessmentDetailResponse updateAssessment(Long id, AdminExamAssessmentUpsertRequest request) {
        ExamAssessment assessment = assessmentSupportService.requireAssessment(id);
        if (assessmentSupportService.resolveDisplayStatus(assessment, LocalDateTime.now()) != AssessmentStatus.NOT_STARTED
                || assessment.getStatus() == AssessmentStatus.CANCELLED
                || assessment.getStatus() == AssessmentStatus.ARCHIVED) {
            throw new BusinessException(ErrorCode.CONFLICT, "Only not started assessments can be updated");
        }
        List<Long> organizationIds = assessmentSupportService.normalizeIds(request.organizationIds(), "organizationIds");
        List<Long> explicitStudentIds = assessmentSupportService.normalizeIds(request.studentIds(), "studentIds");
        validateScope(request.provinceCode(), request.cityCode(), request.districtCode(), organizationIds, explicitStudentIds);
        assessmentSupportService.requireExamPaper(request.paperId());
        assessmentSupportService.loadOrganizations(organizationIds);
        assessmentSupportService.validateAssessmentWindow(request.startAt(), request.endAt());
        assessmentSupportService.validateRegionScope(request.provinceCode(), request.cityCode(), request.districtCode());

        fillAssessment(assessment, request);
        examAssessmentMapper.updateById(assessment);
        rebuildScope(id, request.provinceCode(), request.cityCode(), request.districtCode(), organizationIds, explicitStudentIds);
        return getAssessment(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminExamAssessmentDetailResponse updateAssessmentStatus(Long id, AdminExamAssessmentStatusUpdateRequest request) {
        ExamAssessment assessment = assessmentSupportService.requireAssessment(id);
        if (request.status() != AssessmentStatus.CANCELLED && request.status() != AssessmentStatus.ARCHIVED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "status must be cancelled or archived");
        }
        if (assessment.getStatus() == AssessmentStatus.ARCHIVED) {
            throw new BusinessException(ErrorCode.CONFLICT, "Archived assessment cannot be changed");
        }
        assessment.setStatus(request.status());
        examAssessmentMapper.updateById(assessment);
        return getAssessment(id);
    }

    private LambdaQueryWrapper<ExamAssessment> buildAssessmentQuery(
            String sort,
            String keyword,
            AssessmentType assessmentType,
            AssessmentStatus status,
            LocalDateTime startAt,
            LocalDateTime endAt,
            LocalDateTime now
    ) {
        LambdaQueryWrapper<ExamAssessment> wrapper = new LambdaQueryWrapper<ExamAssessment>()
                .eq(ExamAssessment::getDeleted, 0)
                .eq(assessmentType != null, ExamAssessment::getAssessmentType, assessmentType)
                .and(hasText(keyword), query -> query.like(ExamAssessment::getAssessmentName, keyword.trim()))
                .ge(startAt != null, ExamAssessment::getStartAt, startAt)
                .lt(endAt != null, ExamAssessment::getEndAt, endAt);
        if (status == AssessmentStatus.CANCELLED || status == AssessmentStatus.ARCHIVED) {
            wrapper.eq(ExamAssessment::getStatus, status);
        } else if (status == AssessmentStatus.NOT_STARTED) {
            wrapper.eq(ExamAssessment::getStatus, AssessmentStatus.NOT_STARTED)
                    .gt(ExamAssessment::getStartAt, now);
        } else if (status == AssessmentStatus.IN_PROGRESS) {
            wrapper.eq(ExamAssessment::getStatus, AssessmentStatus.NOT_STARTED)
                    .le(ExamAssessment::getStartAt, now)
                    .gt(ExamAssessment::getEndAt, now);
        } else if (status == AssessmentStatus.ENDED) {
            wrapper.eq(ExamAssessment::getStatus, AssessmentStatus.NOT_STARTED)
                    .le(ExamAssessment::getEndAt, now);
        }
        wrapper.orderByAsc("startAtAsc".equals(sort), ExamAssessment::getStartAt)
                .orderByDesc(!"startAtAsc".equals(sort), ExamAssessment::getStartAt);
        return wrapper;
    }

    private Map<Long, String> loadPaperNames(List<Long> paperIds) {
        if (paperIds.isEmpty()) {
            return Map.of();
        }
        return paperIds.stream()
                .distinct()
                .map(assessmentSupportService::requireExamPaper)
                .collect(Collectors.toMap(ExamPaper::getId, ExamPaper::getPaperName));
    }

    private Map<Long, Long> loadOrganizationCounts(List<Long> assessmentIds) {
        if (assessmentIds.isEmpty()) {
            return Map.of();
        }
        return examAssessmentOrganizationMapper.selectList(new LambdaQueryWrapper<ExamAssessmentOrganization>()
                        .in(ExamAssessmentOrganization::getAssessmentId, assessmentIds))
                .stream()
                .collect(Collectors.groupingBy(ExamAssessmentOrganization::getAssessmentId, Collectors.counting()));
    }

    private Map<Long, Long> loadExplicitCounts(List<Long> assessmentIds) {
        if (assessmentIds.isEmpty()) {
            return Map.of();
        }
        return examAssessmentStudentMapper.selectList(new LambdaQueryWrapper<ExamAssessmentStudent>()
                        .in(ExamAssessmentStudent::getAssessmentId, assessmentIds)
                        .eq(ExamAssessmentStudent::getAssignSource, "explicit"))
                .stream()
                .collect(Collectors.groupingBy(ExamAssessmentStudent::getAssessmentId, Collectors.counting()));
    }

    private AdminExamAssessmentListResponse toListResponse(
            ExamAssessment assessment,
            String paperName,
            long organizationCount,
            long explicitCount,
            LocalDateTime now
    ) {
        return new AdminExamAssessmentListResponse(
                assessment.getId(),
                assessment.getAssessmentName(),
                assessment.getPaperId(),
                paperName,
                assessment.getAssessmentType(),
                assessmentSupportService.resolveDisplayStatus(assessment, now),
                assessment.getStartAt(),
                assessment.getEndAt(),
                assessment.getExpectedStudentCount() == null ? 0L : assessment.getExpectedStudentCount(),
                new AdminExamAssessmentScopeSummaryResponse(
                        assessment.getProvinceCode(),
                        assessment.getCityCode(),
                        assessment.getDistrictCode(),
                        organizationCount,
                        explicitCount,
                        assessment.getExpectedStudentCount() == null ? 0L : assessment.getExpectedStudentCount()
                )
        );
    }

    private void fillAssessment(ExamAssessment assessment, AdminExamAssessmentUpsertRequest request) {
        assessment.setAssessmentName(request.assessmentName().trim());
        assessment.setPaperId(request.paperId());
        assessment.setAssessmentType(request.assessmentType());
        assessment.setStartAt(request.startAt());
        assessment.setEndAt(request.endAt());
        assessment.setProvinceCode(assessmentSupportService.normalizeText(request.provinceCode()));
        assessment.setCityCode(assessmentSupportService.normalizeText(request.cityCode()));
        assessment.setDistrictCode(assessmentSupportService.normalizeText(request.districtCode()));
    }

    private void rebuildScope(
            Long assessmentId,
            String provinceCode,
            String cityCode,
            String districtCode,
            List<Long> organizationIds,
            List<Long> explicitStudentIds
    ) {
        examAssessmentOrganizationMapper.delete(new LambdaQueryWrapper<ExamAssessmentOrganization>()
                .eq(ExamAssessmentOrganization::getAssessmentId, assessmentId));
        if (!organizationIds.isEmpty()) {
            organizationIds.forEach(organizationId -> {
                ExamAssessmentOrganization relation = new ExamAssessmentOrganization();
                relation.setAssessmentId(assessmentId);
                relation.setOrganizationId(organizationId);
                examAssessmentOrganizationMapper.insert(relation);
            });
        }
        examAssessmentStudentMapper.delete(new LambdaQueryWrapper<ExamAssessmentStudent>()
                .eq(ExamAssessmentStudent::getAssessmentId, assessmentId));
        List<ExamAssessmentStudent> snapshots = assessmentSupportService.buildStudentSnapshots(
                assessmentId, provinceCode, cityCode, districtCode, organizationIds, explicitStudentIds);
        snapshots.forEach(examAssessmentStudentMapper::insert);
        ExamAssessment assessment = assessmentSupportService.requireAssessment(assessmentId);
        assessment.setExpectedStudentCount((long) snapshots.size());
        examAssessmentMapper.updateById(assessment);
    }

    private void validateScope(
            String provinceCode,
            String cityCode,
            String districtCode,
            List<Long> organizationIds,
            List<Long> explicitStudentIds
    ) {
        if (assessmentSupportService.isBlank(provinceCode)
                && assessmentSupportService.isBlank(cityCode)
                && assessmentSupportService.isBlank(districtCode)
                && organizationIds.isEmpty()
                && explicitStudentIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Assessment scope must not be empty");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
}
