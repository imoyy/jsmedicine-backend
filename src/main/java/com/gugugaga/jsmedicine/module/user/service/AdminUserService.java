package com.gugugaga.jsmedicine.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityStatus;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityType;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentPageQuery;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentResponse;
import com.gugugaga.jsmedicine.module.user.dto.AdminStudentUpdateRequest;
import com.gugugaga.jsmedicine.module.user.dto.AdminUserPageQuery;
import com.gugugaga.jsmedicine.module.user.dto.AdminUserResponse;
import com.gugugaga.jsmedicine.module.user.dto.AdminUserUpdateRequest;
import com.gugugaga.jsmedicine.module.user.dto.AppUserManagementRole;
import com.gugugaga.jsmedicine.module.user.dto.StudentCertificationReviewRequest;
import com.gugugaga.jsmedicine.module.user.dto.StudentCertificationFileResponse;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.entity.AppUserIdentity;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.entity.StudentCertificationFile;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserIdentityMapper;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentCertificationFileMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import com.gugugaga.jsmedicine.module.expert.entity.Expert;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class AdminUserService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final AppUserMapper appUserMapper;
    private final AppUserIdentityMapper appUserIdentityMapper;
    private final StudentMapper studentMapper;
    private final StudentCertificationFileMapper studentCertificationFileMapper;
    private final ExpertMapper expertMapper;
    private final CurrentAdminAccessor currentAdminAccessor;

    public AdminUserService(
            AppUserMapper appUserMapper,
            AppUserIdentityMapper appUserIdentityMapper,
            StudentMapper studentMapper,
            StudentCertificationFileMapper studentCertificationFileMapper,
            ExpertMapper expertMapper,
            CurrentAdminAccessor currentAdminAccessor
    ) {
        this.appUserMapper = appUserMapper;
        this.appUserIdentityMapper = appUserIdentityMapper;
        this.studentMapper = studentMapper;
        this.studentCertificationFileMapper = studentCertificationFileMapper;
        this.expertMapper = expertMapper;
        this.currentAdminAccessor = currentAdminAccessor;
    }

    public PageResponse<AdminUserResponse> pageUsers(AdminUserPageQuery query) {
        Page<AppUser> page = appUserMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<AppUser>()
                        .eq(AppUser::getDeleted, 0)
                        .eq(query.status() != null, AppUser::getStatus, query.status())
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(AppUser::getUsername, query.keyword())
                                .or()
                                .like(AppUser::getNickname, query.keyword())
                                .or()
                                .like(AppUser::getMobile, query.keyword()))
                        .orderByAsc("registeredAtAsc".equals(query.sort()), AppUser::getRegisteredAt)
                        .orderByDesc(!"registeredAtAsc".equals(query.sort()), AppUser::getRegisteredAt));
        return new PageResponse<>(
                page.getRecords().stream().map(this::toUserResponse).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    public AdminUserResponse getUser(Long id) {
        return toUserResponse(requireUser(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long id, EnabledStatus status) {
        requireUser(id);
        appUserMapper.update(null, new LambdaUpdateWrapper<AppUser>()
                .eq(AppUser::getId, id)
                .set(AppUser::getStatus, status));
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminUserResponse updateUser(Long id, AdminUserUpdateRequest request) {
        AppUser user = requireUser(id);
        user.setNickname(request.nickname());
        user.setProfileSignature(request.profileSignature());
        user.setStatus(request.status());
        appUserMapper.updateById(user);
        applyUserRole(user.getId(), request);
        return getUser(id);
    }

    public PageResponse<AdminStudentResponse> pageStudents(AdminStudentPageQuery query) {
        Page<Student> page = studentMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getDeleted, 0)
                        .eq(query.status() != null, Student::getStatus, query.status())
                        .eq(query.certificationStatus() != null, Student::getCertificationStatus, query.certificationStatus())
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(Student::getStudentNo, query.keyword())
                                .or()
                                .like(Student::getRealName, query.keyword())
                                .or()
                                .like(Student::getMobile, query.keyword())
                                .or()
                                .like(Student::getOrganization, query.keyword()))
                        .orderByAsc("submittedAtAsc".equals(query.sort()), Student::getCertificationSubmittedAt)
                        .orderByDesc(!"submittedAtAsc".equals(query.sort()), Student::getCertificationSubmittedAt));
        return new PageResponse<>(
                page.getRecords().stream().map(this::toStudentResponse).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    public AdminStudentResponse getStudent(Long id) {
        return toStudentResponse(requireStudent(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminStudentResponse updateStudent(Long id, AdminStudentUpdateRequest request) {
        Student student = requireStudent(id);
        if (hasText(request.studentNo())) {
            ensureStudentNoAvailable(request.studentNo(), id);
        }
        student.setStudentNo(request.studentNo());
        student.setRealName(request.realName());
        student.setMobile(request.mobile());
        student.setIdCardNo(request.idCardNo());
        student.setProvince(request.province());
        student.setProvinceCode(request.provinceCode());
        student.setCity(request.city());
        student.setCityCode(request.cityCode());
        student.setDistrict(request.district());
        student.setDistrictCode(request.districtCode());
        student.setOrganization(request.organization());
        student.setOrganizationId(request.organizationId());
        student.setPositionTitle(request.positionTitle());
        student.setPracticeTypeId(request.practiceTypeId());
        student.setStatus(request.status());
        studentMapper.updateById(student);
        return getStudent(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminStudentResponse reviewCertification(Long id, StudentCertificationReviewRequest request) {
        Student student = requireStudent(id);
        if (request.certificationStatus() != StudentCertificationStatus.APPROVED
                && request.certificationStatus() != StudentCertificationStatus.REJECTED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Certification review status must be approved or rejected");
        }
        student.setCertificationStatus(request.certificationStatus());
        student.setCertificationReviewedAt(LocalDateTime.now());
        student.setCertificationReviewedBy(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        student.setRejectReason(request.certificationStatus() == StudentCertificationStatus.REJECTED ? request.rejectReason() : null);
        if (request.certificationStatus() == StudentCertificationStatus.APPROVED && student.getEnrolledAt() == null) {
            student.setEnrolledAt(LocalDateTime.now());
        }
        studentMapper.updateById(student);
        return getStudent(id);
    }

    private AppUser requireUser(Long id) {
        AppUser user = appUserMapper.selectById(id);
        if (user == null || !Objects.equals(user.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "App user does not exist");
        }
        return user;
    }

    private Student requireStudent(Long id) {
        Student student = studentMapper.selectById(id);
        if (student == null || !Objects.equals(student.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Student does not exist");
        }
        return student;
    }

    private void ensureStudentNoAvailable(String studentNo, Long ignoredId) {
        Student existing = studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getStudentNo, studentNo)
                .eq(Student::getDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), ignoredId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Student number already exists");
        }
    }

    private void applyUserRole(Long userId, AdminUserUpdateRequest request) {
        if (request.role() == AppUserManagementRole.NORMAL) {
            deactivateIdentity(userId, AppUserIdentityType.STUDENT);
            deactivateIdentity(userId, AppUserIdentityType.EXPERT);
            unbindCurrentStudent(userId);
            unbindCurrentExpert(userId);
            return;
        }
        if (request.role() == AppUserManagementRole.STUDENT) {
            bindStudent(userId, request);
            ensureIdentity(userId, AppUserIdentityType.STUDENT, true);
            deactivateIdentity(userId, AppUserIdentityType.EXPERT);
            unbindCurrentExpert(userId);
            return;
        }
        ensureIdentity(userId, AppUserIdentityType.EXPERT, true);
        deactivateIdentity(userId, AppUserIdentityType.STUDENT);
        unbindCurrentStudent(userId);
    }

    private void bindStudent(Long userId, AdminUserUpdateRequest request) {
        if (request.studentId() == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "studentId is required when role is STUDENT");
        }
        Student student = requireStudent(request.studentId());
        if (student.getUserId() != null && !Objects.equals(student.getUserId(), userId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Student is already bound to another user");
        }
        Student currentStudent = findStudentByUserId(userId);
        if (currentStudent != null && !Objects.equals(currentStudent.getId(), student.getId())) {
            currentStudent.setUserId(null);
            studentMapper.updateById(currentStudent);
        }
        student.setUserId(userId);
        student.setProvince(request.province());
        student.setProvinceCode(request.provinceCode());
        student.setCity(request.city());
        student.setCityCode(request.cityCode());
        student.setDistrict(request.district());
        student.setDistrictCode(request.districtCode());
        student.setOrganization(request.organization());
        student.setOrganizationId(request.organizationId());
        student.setPracticeTypeId(request.practiceTypeId());
        studentMapper.updateById(student);
    }

    private void unbindCurrentStudent(Long userId) {
        Student student = findStudentByUserId(userId);
        if (student == null) {
            return;
        }
        student.setUserId(null);
        studentMapper.updateById(student);
    }

    private Student findStudentByUserId(Long userId) {
        return studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
                .eq(Student::getDeleted, 0)
                .last("LIMIT 1"));
    }

    private void unbindCurrentExpert(Long userId) {
        Expert expert = expertMapper.selectOne(new LambdaQueryWrapper<Expert>()
                .eq(Expert::getUserId, userId)
                .eq(Expert::getDeleted, 0)
                .last("LIMIT 1"));
        if (expert == null) {
            return;
        }
        expert.setUserId(null);
        expertMapper.updateById(expert);
    }

    private void ensureIdentity(Long userId, AppUserIdentityType identityType, boolean primary) {
        AppUserIdentity identity = findIdentity(userId, identityType);
        if (identity == null) {
            identity = new AppUserIdentity();
            identity.setUserId(userId);
            identity.setIdentityType(identityType);
            identity.setDeleted(0);
        }
        identity.setIdentityStatus(AppUserIdentityStatus.ACTIVE);
        identity.setIsPrimary(primary);
        if (identity.getActivatedAt() == null) {
            identity.setActivatedAt(LocalDateTime.now());
        }
        identity.setDeactivatedAt(null);
        if (identity.getId() == null) {
            appUserIdentityMapper.insert(identity);
        } else {
            appUserIdentityMapper.updateById(identity);
        }
    }

    private void deactivateIdentity(Long userId, AppUserIdentityType identityType) {
        AppUserIdentity identity = findIdentity(userId, identityType);
        if (identity == null || identity.getIdentityStatus() == AppUserIdentityStatus.INACTIVE) {
            return;
        }
        identity.setIdentityStatus(AppUserIdentityStatus.INACTIVE);
        identity.setIsPrimary(false);
        identity.setDeactivatedAt(LocalDateTime.now());
        appUserIdentityMapper.updateById(identity);
    }

    private AppUserIdentity findIdentity(Long userId, AppUserIdentityType identityType) {
        return appUserIdentityMapper.selectOne(new LambdaQueryWrapper<AppUserIdentity>()
                .eq(AppUserIdentity::getUserId, userId)
                .eq(AppUserIdentity::getIdentityType, identityType)
                .eq(AppUserIdentity::getDeleted, 0)
                .last("LIMIT 1"));
    }

    private AppUserManagementRole resolveRole(Long userId) {
        AppUserIdentity expertIdentity = findIdentity(userId, AppUserIdentityType.EXPERT);
        if (expertIdentity != null && expertIdentity.getIdentityStatus() == AppUserIdentityStatus.ACTIVE) {
            return AppUserManagementRole.EXPERT;
        }
        AppUserIdentity studentIdentity = findIdentity(userId, AppUserIdentityType.STUDENT);
        if (studentIdentity != null && studentIdentity.getIdentityStatus() == AppUserIdentityStatus.ACTIVE) {
            return AppUserManagementRole.STUDENT;
        }
        return AppUserManagementRole.NORMAL;
    }

    private AdminUserResponse toUserResponse(AppUser user) {
        Student student = findStudentByUserId(user.getId());
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getMobile(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileSignature(),
                user.getAvatarUrl(),
                user.getAuthProvider(),
                user.getWechatOpenId(),
                user.getWechatUnionId(),
                user.getGender(),
                user.getStatus(),
                user.getRegisteredAt(),
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                user.getProfileCompleted(),
                resolveRole(user.getId()),
                student == null ? null : student.getId(),
                student == null ? null : student.getRealName(),
                student == null ? null : student.getProvince(),
                student == null ? null : student.getProvinceCode(),
                student == null ? null : student.getCity(),
                student == null ? null : student.getCityCode(),
                student == null ? null : student.getDistrict(),
                student == null ? null : student.getDistrictCode(),
                student == null ? null : student.getOrganization(),
                student == null ? null : student.getOrganizationId(),
                student == null ? null : student.getPracticeTypeId()
        );
    }

    private AdminStudentResponse toStudentResponse(Student student) {
        return new AdminStudentResponse(
                student.getId(),
                student.getUserId(),
                student.getStudentNo(),
                student.getRealName(),
                student.getMobile(),
                student.getIdCardNo(),
                student.getProvince(),
                student.getProvinceCode(),
                student.getCity(),
                student.getCityCode(),
                student.getDistrict(),
                student.getDistrictCode(),
                student.getOrganization(),
                student.getOrganizationId(),
                student.getPositionTitle(),
                student.getPracticeTypeId(),
                student.getStatus(),
                student.getCertificationStatus(),
                student.getCertificationSubmittedAt(),
                student.getCertificationReviewedAt(),
                student.getCertificationReviewedBy(),
                student.getRejectReason(),
                student.getCertificationMaterials(),
                loadCertificationFiles(student.getId()),
                student.getEnrolledAt()
        );
    }

    private List<StudentCertificationFileResponse> loadCertificationFiles(Long studentId) {
        return studentCertificationFileMapper.selectList(new LambdaQueryWrapper<StudentCertificationFile>()
                        .eq(StudentCertificationFile::getStudentId, studentId))
                .stream()
                .sorted(Comparator.comparing(StudentCertificationFile::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(StudentCertificationFile::getId))
                .map(file -> new StudentCertificationFileResponse(
                        file.getId(),
                        file.getFileAssetId(),
                        file.getSourceUrl(),
                        file.getMaterialType(),
                        file.getSortOrder()
                ))
                .toList();
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
