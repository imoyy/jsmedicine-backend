package com.gugugaga.jsmedicine.module.user.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import com.gugugaga.jsmedicine.module.interaction.favorite.entity.UserFavorite;
import com.gugugaga.jsmedicine.module.interaction.favorite.mapper.UserFavoriteMapper;
import com.gugugaga.jsmedicine.module.interaction.history.entity.UserBrowseHistory;
import com.gugugaga.jsmedicine.module.interaction.history.mapper.UserBrowseHistoryMapper;
import com.gugugaga.jsmedicine.module.user.app.dto.AppProfileResponse;
import com.gugugaga.jsmedicine.module.user.app.dto.AppProfileSummaryResponse;
import com.gugugaga.jsmedicine.module.user.app.dto.AppProfileUpdateRequest;
import com.gugugaga.jsmedicine.module.user.app.dto.AppResourceRecordResponse;
import com.gugugaga.jsmedicine.module.user.app.dto.AppStudentCertificationRequest;
import com.gugugaga.jsmedicine.module.user.app.dto.AppStudentCertificationResponse;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
public class AppProfileService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final CurrentAppUserResolver currentAppUserResolver;
    private final AppUserMapper appUserMapper;
    private final StudentMapper studentMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;

    public AppProfileService(
            CurrentAppUserResolver currentAppUserResolver,
            AppUserMapper appUserMapper,
            StudentMapper studentMapper,
            UserFavoriteMapper userFavoriteMapper,
            UserBrowseHistoryMapper userBrowseHistoryMapper
    ) {
        this.currentAppUserResolver = currentAppUserResolver;
        this.appUserMapper = appUserMapper;
        this.studentMapper = studentMapper;
        this.userFavoriteMapper = userFavoriteMapper;
        this.userBrowseHistoryMapper = userBrowseHistoryMapper;
    }

    public AppProfileResponse currentProfile() {
        AppUser user = requireCurrentUser();
        return toProfileResponse(user, findCurrentStudent(user.getId()).orElse(null));
    }

    @Transactional(rollbackFor = Exception.class)
    public AppProfileResponse updateProfile(AppProfileUpdateRequest request) {
        AppUser user = requireCurrentUser();
        user.setNickname(request.nickname());
        user.setAvatarUrl(request.avatarUrl());
        user.setEmail(request.email());
        user.setGender(request.gender() == null ? Gender.UNKNOWN : request.gender());
        user.setProfileCompleted(hasText(request.nickname()) && hasText(request.avatarUrl()));
        appUserMapper.updateById(user);
        return currentProfile();
    }

    @Transactional(rollbackFor = Exception.class)
    public AppStudentCertificationResponse submitCertification(AppStudentCertificationRequest request) {
        AppUser user = requireCurrentUser();
        Student student = findCurrentStudent(user.getId()).orElseGet(() -> createStudent(user.getId()));
        if (student.getCertificationStatus() == StudentCertificationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.CONFLICT, "Student certification is already approved");
        }
        student.setRealName(request.realName());
        student.setMobile(hasText(request.mobile()) ? request.mobile() : user.getMobile());
        student.setIdCardNo(request.idCardNo());
        student.setProvince(request.province());
        student.setCity(request.city());
        student.setDistrict(request.district());
        student.setOrganization(request.organization());
        student.setPositionTitle(request.positionTitle());
        student.setStatus(EnabledStatus.ENABLED);
        student.setCertificationStatus(StudentCertificationStatus.PENDING);
        student.setCertificationSubmittedAt(LocalDateTime.now());
        student.setCertificationReviewedAt(null);
        student.setCertificationReviewedBy(null);
        student.setRejectReason(null);
        student.setCertificationMaterials(request.certificationMaterials());
        if (student.getId() == null) {
            studentMapper.insert(student);
        } else {
            studentMapper.updateById(student);
        }
        return certificationStatus();
    }

    public AppStudentCertificationResponse certificationStatus() {
        AppUser user = requireCurrentUser();
        return findCurrentStudent(user.getId())
                .map(this::toCertificationResponse)
                .orElse(null);
    }

    public PageResponse<AppResourceRecordResponse> favoritePage(long page, long size, String sort) {
        Long userId = requireCurrentUserSession().userId();
        Page<UserFavorite> favoritePage = userFavoriteMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .orderByAsc("createdAtAsc".equals(sort), UserFavorite::getCreatedAt)
                        .orderByDesc(!"createdAtAsc".equals(sort), UserFavorite::getCreatedAt));
        return new PageResponse<>(
                favoritePage.getRecords().stream().map(this::toFavoriteResponse).toList(),
                favoritePage.getTotal(),
                favoritePage.getCurrent(),
                favoritePage.getSize()
        );
    }

    public PageResponse<AppResourceRecordResponse> browseHistoryPage(long page, long size, String sort) {
        Long userId = requireCurrentUserSession().userId();
        Page<UserBrowseHistory> historyPage = userBrowseHistoryMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<UserBrowseHistory>()
                        .eq(UserBrowseHistory::getUserId, userId)
                        .orderByAsc("viewedAtAsc".equals(sort), UserBrowseHistory::getViewedAt)
                        .orderByDesc(!"viewedAtAsc".equals(sort), UserBrowseHistory::getViewedAt));
        return new PageResponse<>(
                historyPage.getRecords().stream().map(this::toBrowseHistoryResponse).toList(),
                historyPage.getTotal(),
                historyPage.getCurrent(),
                historyPage.getSize()
        );
    }

    public AppProfileSummaryResponse summary() {
        AppUser user = requireCurrentUser();
        Student student = findCurrentStudent(user.getId()).orElse(null);
        long favoriteCount = userFavoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, user.getId()));
        long browseHistoryCount = userBrowseHistoryMapper.selectCount(new LambdaQueryWrapper<UserBrowseHistory>()
                .eq(UserBrowseHistory::getUserId, user.getId()));
        return new AppProfileSummaryResponse(
                toProfileResponse(user, student),
                student == null ? StudentCertificationStatus.UNSUBMITTED : student.getCertificationStatus(),
                favoriteCount,
                browseHistoryCount
        );
    }

    private AppUserSession requireCurrentUserSession() {
        return currentAppUserResolver.requireCurrentUser();
    }

    private AppUser requireCurrentUser() {
        AppUserSession session = requireCurrentUserSession();
        AppUser user = appUserMapper.selectById(session.userId());
        if (user == null || !Objects.equals(user.getDeleted(), 0) || user.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "App user account does not exist");
        }
        return user;
    }

    private Optional<Student> findCurrentStudent(Long userId) {
        return Optional.ofNullable(studentMapper.selectOne(new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, userId)
                .eq(Student::getDeleted, 0)
                .last("LIMIT 1")));
    }

    private Student createStudent(Long userId) {
        Student student = new Student();
        student.setUserId(userId);
        student.setStatus(EnabledStatus.ENABLED);
        student.setCertificationStatus(StudentCertificationStatus.UNSUBMITTED);
        student.setDeleted(0);
        return student;
    }

    private AppProfileResponse toProfileResponse(AppUser user, Student student) {
        return new AppProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getMobile(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatarUrl(),
                user.getAuthProvider(),
                user.getGender(),
                user.getStatus(),
                user.getProfileCompleted(),
                student == null ? null : student.getId(),
                student == null ? StudentCertificationStatus.UNSUBMITTED : student.getCertificationStatus()
        );
    }

    private AppStudentCertificationResponse toCertificationResponse(Student student) {
        return new AppStudentCertificationResponse(
                student.getId(),
                student.getStudentNo(),
                student.getRealName(),
                student.getMobile(),
                student.getProvince(),
                student.getCity(),
                student.getDistrict(),
                student.getOrganization(),
                student.getPositionTitle(),
                student.getStatus(),
                student.getCertificationStatus(),
                student.getCertificationSubmittedAt(),
                student.getCertificationReviewedAt(),
                student.getRejectReason(),
                student.getCertificationMaterials(),
                student.getEnrolledAt()
        );
    }

    private AppResourceRecordResponse toFavoriteResponse(UserFavorite favorite) {
        return new AppResourceRecordResponse(
                favorite.getId(),
                favorite.getResourceType(),
                favorite.getResourceId(),
                null,
                null,
                favorite.getCreatedAt()
        );
    }

    private AppResourceRecordResponse toBrowseHistoryResponse(UserBrowseHistory history) {
        return new AppResourceRecordResponse(
                history.getId(),
                history.getResourceType(),
                history.getResourceId(),
                history.getSource(),
                history.getViewCount(),
                history.getViewedAt()
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
