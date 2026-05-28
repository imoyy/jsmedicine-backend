package com.gugugaga.jsmedicine.module.user.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityStatus;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityType;
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
import com.gugugaga.jsmedicine.module.user.app.dto.AppStudentCertificationFileRequest;
import com.gugugaga.jsmedicine.module.user.app.dto.AppStudentCertificationFileResponse;
import com.gugugaga.jsmedicine.module.user.app.dto.AppStudentCertificationResponse;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.entity.AppUserIdentity;
import com.gugugaga.jsmedicine.module.user.entity.Student;
import com.gugugaga.jsmedicine.module.user.entity.StudentCertificationFile;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserIdentityMapper;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentCertificationFileMapper;
import com.gugugaga.jsmedicine.module.user.mapper.StudentMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AppProfileService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final CurrentAppUserResolver currentAppUserResolver;
    private final AppUserMapper appUserMapper;
    private final AppUserIdentityMapper appUserIdentityMapper;
    private final StudentMapper studentMapper;
    private final StudentCertificationFileMapper studentCertificationFileMapper;
    private final UserFavoriteMapper userFavoriteMapper;
    private final UserBrowseHistoryMapper userBrowseHistoryMapper;

    public AppProfileService(
            CurrentAppUserResolver currentAppUserResolver,
            AppUserMapper appUserMapper,
            AppUserIdentityMapper appUserIdentityMapper,
            StudentMapper studentMapper,
            StudentCertificationFileMapper studentCertificationFileMapper,
            UserFavoriteMapper userFavoriteMapper,
            UserBrowseHistoryMapper userBrowseHistoryMapper
    ) {
        this.currentAppUserResolver = currentAppUserResolver;
        this.appUserMapper = appUserMapper;
        this.appUserIdentityMapper = appUserIdentityMapper;
        this.studentMapper = studentMapper;
        this.studentCertificationFileMapper = studentCertificationFileMapper;
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
        user.setProfileSignature(request.profileSignature());
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
        student.setProvinceCode(request.provinceCode());
        student.setCity(request.city());
        student.setCityCode(request.cityCode());
        student.setDistrict(request.district());
        student.setDistrictCode(request.districtCode());
        student.setOrganization(request.organization());
        student.setOrganizationId(request.organizationId());
        student.setPositionTitle(request.positionTitle());
        student.setPracticeTypeId(request.practiceTypeId());
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
        replaceCertificationFiles(student.getId(), request.certificationFiles());
        ensureIdentity(user.getId(), AppUserIdentityType.STUDENT, true);
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

    private void ensureIdentity(Long userId, AppUserIdentityType identityType, boolean primary) {
        AppUserIdentity identity = appUserIdentityMapper.selectOne(new LambdaQueryWrapper<AppUserIdentity>()
                .eq(AppUserIdentity::getUserId, userId)
                .eq(AppUserIdentity::getIdentityType, identityType)
                .eq(AppUserIdentity::getDeleted, 0)
                .last("LIMIT 1"));
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

    private void replaceCertificationFiles(Long studentId, List<AppStudentCertificationFileRequest> files) {
        studentCertificationFileMapper.delete(new LambdaQueryWrapper<StudentCertificationFile>()
                .eq(StudentCertificationFile::getStudentId, studentId));
        if (files == null || files.isEmpty()) {
            return;
        }
        for (int i = 0; i < files.size(); i++) {
            AppStudentCertificationFileRequest fileRequest = files.get(i);
            if (fileRequest.fileAssetId() == null && !hasText(fileRequest.sourceUrl())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Certification file requires fileAssetId or sourceUrl");
            }
            StudentCertificationFile file = new StudentCertificationFile();
            file.setStudentId(studentId);
            file.setFileAssetId(fileRequest.fileAssetId());
            file.setSourceUrl(fileRequest.sourceUrl());
            file.setMaterialType(fileRequest.materialType());
            file.setSortOrder(fileRequest.sortOrder() == null ? i : fileRequest.sortOrder());
            studentCertificationFileMapper.insert(file);
        }
    }

    private AppProfileResponse toProfileResponse(AppUser user, Student student) {
        return new AppProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getMobile(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileSignature(),
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
                student.getRejectReason(),
                student.getCertificationMaterials(),
                loadCertificationFiles(student.getId()),
                student.getEnrolledAt()
        );
    }

    private List<AppStudentCertificationFileResponse> loadCertificationFiles(Long studentId) {
        return studentCertificationFileMapper.selectList(new LambdaQueryWrapper<StudentCertificationFile>()
                        .eq(StudentCertificationFile::getStudentId, studentId))
                .stream()
                .sorted(Comparator.comparing(StudentCertificationFile::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(StudentCertificationFile::getId))
                .map(file -> new AppStudentCertificationFileResponse(
                        file.getId(),
                        file.getFileAssetId(),
                        file.getSourceUrl(),
                        file.getMaterialType(),
                        file.getSortOrder()
                ))
                .toList();
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
