package com.gugugaga.jsmedicine.module.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
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
import com.gugugaga.jsmedicine.module.user.dto.StudentCertificationReviewRequest;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class AdminUserService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final AppUserMapper appUserMapper;
    private final StudentMapper studentMapper;
    private final CurrentAdminAccessor currentAdminAccessor;

    public AdminUserService(
            AppUserMapper appUserMapper,
            StudentMapper studentMapper,
            CurrentAdminAccessor currentAdminAccessor
    ) {
        this.appUserMapper = appUserMapper;
        this.studentMapper = studentMapper;
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
        student.setCity(request.city());
        student.setDistrict(request.district());
        student.setOrganization(request.organization());
        student.setPositionTitle(request.positionTitle());
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

    private AdminUserResponse toUserResponse(AppUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getMobile(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getAuthProvider(),
                user.getWechatOpenId(),
                user.getWechatUnionId(),
                user.getGender(),
                user.getStatus(),
                user.getRegisteredAt(),
                user.getLastLoginAt(),
                user.getLastLoginIp(),
                user.getProfileCompleted()
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
                student.getCity(),
                student.getDistrict(),
                student.getOrganization(),
                student.getPositionTitle(),
                student.getStatus(),
                student.getCertificationStatus(),
                student.getCertificationSubmittedAt(),
                student.getCertificationReviewedAt(),
                student.getCertificationReviewedBy(),
                student.getRejectReason(),
                student.getCertificationMaterials(),
                student.getEnrolledAt()
        );
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
