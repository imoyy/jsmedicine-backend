package com.gugugaga.jsmedicine.module.learning.assessment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.enums.AssessmentEventType;
import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.ExamRecordStatus;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.learning.assessment.entity.ExamAssessment;
import com.gugugaga.jsmedicine.module.learning.assessment.entity.ExamAssessmentEvent;
import com.gugugaga.jsmedicine.module.learning.assessment.entity.ExamAssessmentStudent;
import com.gugugaga.jsmedicine.module.learning.assessment.mapper.ExamAssessmentEventMapper;
import com.gugugaga.jsmedicine.module.learning.assessment.mapper.ExamAssessmentMapper;
import com.gugugaga.jsmedicine.module.learning.question.entity.ExamPaper;
import com.gugugaga.jsmedicine.module.learning.question.mapper.ExamPaperMapper;
import com.gugugaga.jsmedicine.module.learning.record.entity.ExamRecord;
import com.gugugaga.jsmedicine.module.user.entity.Organization;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.OrganizationMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExamAssessmentSupportService {

    private final ExamAssessmentMapper examAssessmentMapper;
    private final ExamPaperMapper examPaperMapper;
    private final StudentMapper studentMapper;
    private final OrganizationMapper organizationMapper;
    private final ExamAssessmentEventMapper examAssessmentEventMapper;

    public ExamAssessmentSupportService(
            ExamAssessmentMapper examAssessmentMapper,
            ExamPaperMapper examPaperMapper,
            StudentMapper studentMapper,
            OrganizationMapper organizationMapper,
            ExamAssessmentEventMapper examAssessmentEventMapper
    ) {
        this.examAssessmentMapper = examAssessmentMapper;
        this.examPaperMapper = examPaperMapper;
        this.studentMapper = studentMapper;
        this.organizationMapper = organizationMapper;
        this.examAssessmentEventMapper = examAssessmentEventMapper;
    }

    public ExamAssessment requireAssessment(Long id) {
        ExamAssessment assessment = examAssessmentMapper.selectById(id);
        if (assessment == null || !Objects.equals(assessment.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Exam assessment does not exist");
        }
        return assessment;
    }

    public ExamPaper requireExamPaper(Long paperId) {
        ExamPaper paper = examPaperMapper.selectById(paperId);
        if (paper == null || !Objects.equals(paper.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Exam paper does not exist");
        }
        return paper;
    }

    public Map<Long, ExamPaper> loadExistingExamPapers(Collection<Long> paperIds) {
        if (paperIds == null || paperIds.isEmpty()) {
            return Map.of();
        }
        List<Long> distinctIds = paperIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        return examPaperMapper.selectList(new LambdaQueryWrapper<ExamPaper>()
                        .in(ExamPaper::getId, distinctIds)
                        .eq(ExamPaper::getDeleted, 0))
                .stream()
                .collect(Collectors.toMap(ExamPaper::getId, paper -> paper));
    }

    public AssessmentStatus resolveDisplayStatus(ExamAssessment assessment, LocalDateTime now) {
        if (assessment.getStatus() == AssessmentStatus.CANCELLED || assessment.getStatus() == AssessmentStatus.ARCHIVED) {
            return assessment.getStatus();
        }
        if (now.isBefore(assessment.getStartAt())) {
            return AssessmentStatus.NOT_STARTED;
        }
        if (now.isBefore(assessment.getEndAt())) {
            return AssessmentStatus.IN_PROGRESS;
        }
        return AssessmentStatus.ENDED;
    }

    public void validateAssessmentWindow(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null || !startAt.isBefore(endAt)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "startAt must be before endAt");
        }
    }

    public void validateRegionScope(String provinceCode, String cityCode, String districtCode) {
        if (isBlank(provinceCode) && !isBlank(cityCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "cityCode requires provinceCode");
        }
        if (isBlank(cityCode) && !isBlank(districtCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "districtCode requires cityCode");
        }
    }

    public List<Long> normalizeIds(List<Long> ids, String fieldName) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        Set<Long> normalized = new LinkedHashSet<>();
        for (Long id : ids) {
            if (id == null || id <= 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " must contain positive values");
            }
            normalized.add(id);
        }
        return new ArrayList<>(normalized);
    }

    public Map<Long, Organization> loadOrganizations(List<Long> organizationIds) {
        if (organizationIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Organization> result = organizationMapper.selectList(new LambdaQueryWrapper<Organization>()
                        .in(Organization::getId, organizationIds)
                        .eq(Organization::getDeleted, 0))
                .stream()
                .collect(Collectors.toMap(Organization::getId, organization -> organization));
        if (result.size() != organizationIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Organization does not exist");
        }
        return result;
    }

    public Map<Long, Student> loadEligibleStudents(Collection<Long> studentIds) {
        if (studentIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Student> result = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                        .in(Student::getId, studentIds)
                        .eq(Student::getDeleted, 0)
                        .eq(Student::getStatus, EnabledStatus.ENABLED)
                        .eq(Student::getCertificationStatus, StudentCertificationStatus.APPROVED))
                .stream()
                .collect(Collectors.toMap(Student::getId, student -> student));
        if (result.size() != studentIds.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "studentIds contains unavailable students");
        }
        return result;
    }

    public LinkedHashMap<Long, Student> loadAssignedStudents(
            String provinceCode,
            String cityCode,
            String districtCode,
            List<Long> organizationIds,
            List<Long> explicitStudentIds
    ) {
        LinkedHashMap<Long, Student> assigned = new LinkedHashMap<>();
        boolean hasRegion = !isBlank(provinceCode) || !isBlank(cityCode) || !isBlank(districtCode);
        boolean hasOrganizations = !organizationIds.isEmpty();
        if (hasRegion || hasOrganizations) {
            List<Student> filteredStudents = studentMapper.selectList(new LambdaQueryWrapper<Student>()
                    .eq(Student::getDeleted, 0)
                    .eq(Student::getStatus, EnabledStatus.ENABLED)
                    .eq(Student::getCertificationStatus, StudentCertificationStatus.APPROVED)
                    .eq(!isBlank(provinceCode), Student::getProvinceCode, provinceCode)
                    .eq(!isBlank(cityCode), Student::getCityCode, cityCode)
                    .eq(!isBlank(districtCode), Student::getDistrictCode, districtCode)
                    .in(hasOrganizations, Student::getOrganizationId, organizationIds)
                    .orderByAsc(Student::getId));
            filteredStudents.forEach(student -> assigned.put(student.getId(), student));
        }
        if (!explicitStudentIds.isEmpty()) {
            Map<Long, Student> explicitStudents = loadEligibleStudents(explicitStudentIds);
            explicitStudentIds.forEach(studentId -> assigned.put(studentId, explicitStudents.get(studentId)));
        }
        if (assigned.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Assessment scope resolved no eligible students");
        }
        return assigned;
    }

    public List<ExamAssessmentStudent> buildStudentSnapshots(
            Long assessmentId,
            String provinceCode,
            String cityCode,
            String districtCode,
            List<Long> organizationIds,
            List<Long> explicitStudentIds
    ) {
        LinkedHashMap<Long, Student> assignedStudents = loadAssignedStudents(
                provinceCode, cityCode, districtCode, organizationIds, explicitStudentIds);
        Set<Long> explicitStudentSet = new LinkedHashSet<>(explicitStudentIds);
        return assignedStudents.values().stream()
                .map(student -> toSnapshot(assessmentId, student, explicitStudentSet.contains(student.getId())))
                .toList();
    }

    public boolean hasEventRequestId(Long assessmentId, Long studentId, String requestId) {
        if (isBlank(requestId)) {
            return false;
        }
        return examAssessmentEventMapper.selectCount(new LambdaQueryWrapper<ExamAssessmentEvent>()
                .eq(ExamAssessmentEvent::getAssessmentId, assessmentId)
                .eq(ExamAssessmentEvent::getStudentId, studentId)
                .eq(ExamAssessmentEvent::getRequestId, requestId)) > 0;
    }

    public void saveEventIfNecessary(
            ExamAssessmentStudent snapshot,
            AssessmentEventType eventType,
            String requestId,
            String description
    ) {
        if (!isBlank(requestId) && hasEventRequestId(snapshot.getAssessmentId(), snapshot.getStudentId(), requestId)) {
            return;
        }
        ExamAssessmentEvent event = new ExamAssessmentEvent();
        event.setAssessmentId(snapshot.getAssessmentId());
        event.setStudentId(snapshot.getStudentId());
        event.setEventType(eventType);
        event.setRequestId(normalizeText(requestId));
        event.setEventTime(LocalDateTime.now());
        event.setDescription(normalizeText(description));
        event.setProvinceCodeSnapshot(snapshot.getProvinceCodeSnapshot());
        event.setProvinceNameSnapshot(snapshot.getProvinceNameSnapshot());
        event.setCityCodeSnapshot(snapshot.getCityCodeSnapshot());
        event.setCityNameSnapshot(snapshot.getCityNameSnapshot());
        event.setDistrictCodeSnapshot(snapshot.getDistrictCodeSnapshot());
        event.setDistrictNameSnapshot(snapshot.getDistrictNameSnapshot());
        event.setOrganizationIdSnapshot(snapshot.getOrganizationIdSnapshot());
        event.setOrganizationNameSnapshot(snapshot.getOrganizationNameSnapshot());
        examAssessmentEventMapper.insert(event);
    }

    public boolean isTerminalRecord(ExamRecord record) {
        return record != null && record.getStatus() != null && record.getStatus() != ExamRecordStatus.IN_PROGRESS;
    }

    public long countdownSeconds(AssessmentStatus status, ExamAssessment assessment, LocalDateTime now) {
        if (status == AssessmentStatus.NOT_STARTED) {
            return Math.max(0, java.time.Duration.between(now, assessment.getStartAt()).getSeconds());
        }
        if (status == AssessmentStatus.IN_PROGRESS) {
            return Math.max(0, java.time.Duration.between(now, assessment.getEndAt()).getSeconds());
        }
        return 0L;
    }

    private ExamAssessmentStudent toSnapshot(Long assessmentId, Student student, boolean explicit) {
        ExamAssessmentStudent snapshot = new ExamAssessmentStudent();
        snapshot.setAssessmentId(assessmentId);
        snapshot.setStudentId(student.getId());
        snapshot.setAssignSource(explicit ? "explicit" : "filter");
        snapshot.setStudentNameSnapshot(normalizeText(student.getRealName()));
        snapshot.setMobileSnapshot(normalizeText(student.getMobile()));
        snapshot.setMaskedIdCardNoSnapshot(maskIdCardNo(student.getIdCardNo()));
        snapshot.setProvinceCodeSnapshot(normalizeText(student.getProvinceCode()));
        snapshot.setProvinceNameSnapshot(normalizeText(student.getProvince()));
        snapshot.setCityCodeSnapshot(normalizeText(student.getCityCode()));
        snapshot.setCityNameSnapshot(normalizeText(student.getCity()));
        snapshot.setDistrictCodeSnapshot(normalizeText(student.getDistrictCode()));
        snapshot.setDistrictNameSnapshot(normalizeText(student.getDistrict()));
        snapshot.setOrganizationIdSnapshot(student.getOrganizationId());
        snapshot.setOrganizationNameSnapshot(normalizeText(student.getOrganization()));
        return snapshot;
    }

    public String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public String maskIdCardNo(String idCardNo) {
        String normalized = normalizeText(idCardNo);
        if (normalized == null) {
            return null;
        }
        if (normalized.length() <= 4) {
            return "*".repeat(normalized.length());
        }
        if (normalized.length() <= 8) {
            return normalized.substring(0, 2) + "*".repeat(normalized.length() - 4) + normalized.substring(normalized.length() - 2);
        }
        return normalized.substring(0, 3) + "*".repeat(normalized.length() - 7) + normalized.substring(normalized.length() - 4);
    }
}
