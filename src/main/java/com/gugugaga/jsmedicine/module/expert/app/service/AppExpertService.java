package com.gugugaga.jsmedicine.module.expert.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.ExpertCertificationStatus;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.storage.service.AppUserAvatarUrlResolver;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertExperienceResponse;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertCategoryResponse;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertCertificationFileRequest;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertCertificationFileResponse;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertCertificationRequest;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertCertificationResponse;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertResponse;
import com.gugugaga.jsmedicine.module.expert.entity.Expert;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCategory;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCategoryRelation;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCertification;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCertificationCategoryRelation;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCertificationFile;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertExperience;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCategoryMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCategoryRelationMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCertificationCategoryRelationMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCertificationFileMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCertificationMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertExperienceMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertMapper;
import com.gugugaga.jsmedicine.module.auth.app.entity.AppUserSession;
import com.gugugaga.jsmedicine.module.auth.app.service.CurrentAppUserResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AppExpertService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final ExpertCategoryMapper expertCategoryMapper;
    private final ExpertMapper expertMapper;
    private final ExpertCategoryRelationMapper expertCategoryRelationMapper;
    private final ExpertExperienceMapper expertExperienceMapper;
    private final AppUserAvatarUrlResolver appUserAvatarUrlResolver;
    private final CurrentAppUserResolver currentAppUserResolver;
    private final ExpertCertificationMapper expertCertificationMapper;
    private final ExpertCertificationFileMapper expertCertificationFileMapper;
    private final ExpertCertificationCategoryRelationMapper expertCertificationCategoryRelationMapper;

    public AppExpertService(
            ExpertCategoryMapper expertCategoryMapper,
            ExpertMapper expertMapper,
            ExpertCategoryRelationMapper expertCategoryRelationMapper,
            ExpertExperienceMapper expertExperienceMapper,
            AppUserAvatarUrlResolver appUserAvatarUrlResolver,
            CurrentAppUserResolver currentAppUserResolver,
            ExpertCertificationMapper expertCertificationMapper,
            ExpertCertificationFileMapper expertCertificationFileMapper,
            ExpertCertificationCategoryRelationMapper expertCertificationCategoryRelationMapper
    ) {
        this.expertCategoryMapper = expertCategoryMapper;
        this.expertMapper = expertMapper;
        this.expertCategoryRelationMapper = expertCategoryRelationMapper;
        this.expertExperienceMapper = expertExperienceMapper;
        this.appUserAvatarUrlResolver = appUserAvatarUrlResolver;
        this.currentAppUserResolver = currentAppUserResolver;
        this.expertCertificationMapper = expertCertificationMapper;
        this.expertCertificationFileMapper = expertCertificationFileMapper;
        this.expertCertificationCategoryRelationMapper = expertCertificationCategoryRelationMapper;
    }

    public PageResponse<AppExpertCategoryResponse> pageCategories(long page, long size, String keyword, Long parentId) {
        Page<ExpertCategory> categoryPage = expertCategoryMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<ExpertCategory>()
                        .eq(ExpertCategory::getDeleted, 0)
                        .eq(ExpertCategory::getStatus, EnabledStatus.ENABLED)
                        .eq(parentId != null, ExpertCategory::getParentId, parentId)
                        .and(hasText(keyword), wrapper -> wrapper.like(ExpertCategory::getCategoryName, keyword))
                        .orderByAsc(ExpertCategory::getSortOrder)
                        .orderByDesc(ExpertCategory::getCreatedAt));
        return pageResponse(categoryPage, categoryPage.getRecords().stream()
                .map(category -> new AppExpertCategoryResponse(category.getId(), category.getParentId(), category.getCategoryName(), category.getSortOrder()))
                .toList());
    }

    public PageResponse<AppExpertResponse> pageExperts(long page, long size, String keyword, Long categoryId) {
        List<Long> expertIds = categoryId == null ? null : expertCategoryRelationMapper.selectList(new LambdaQueryWrapper<ExpertCategoryRelation>()
                        .eq(ExpertCategoryRelation::getCategoryId, categoryId))
                .stream()
                .map(ExpertCategoryRelation::getExpertId)
                .toList();
        if (categoryId != null && expertIds.isEmpty()) {
            return new PageResponse<>(List.of(), 0, normalizePage(page), normalizeSize(size));
        }
        Page<Expert> expertPage = expertMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                visibleExpertWrapper()
                        .in(expertIds != null, Expert::getId, expertIds)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(Expert::getRealName, keyword)
                                .or()
                                .like(Expert::getSpecialty, keyword))
                        .orderByAsc(Expert::getSortOrder)
                        .orderByDesc(Expert::getCreatedAt));
        return pageResponse(expertPage, expertPage.getRecords().stream().map(expert -> toExpertResponse(expert, false)).toList());
    }

    public AppExpertResponse expertDetail(Long id) {
        return toExpertResponse(requireVisibleExpert(id), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public AppExpertCertificationResponse submitCertification(AppExpertCertificationRequest request) {
        Long userId = requireCurrentUserSession().userId();
        validateCategoryIds(request.categoryIds());
        ExpertCertification certification = findCurrentCertification(userId)
                .orElseGet(() -> createCertification(userId));
        if (certification.getCertificationStatus() == ExpertCertificationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.CONFLICT, "Expert certification is already approved");
        }
        certification.setRealName(request.realName());
        certification.setGender(request.gender() == null ? Gender.UNKNOWN : request.gender());
        certification.setBirthDate(request.birthDate());
        certification.setMobile(normalizeText(request.mobile()));
        certification.setTitle(normalizeText(request.title()));
        certification.setOrganization(normalizeText(request.organization()));
        certification.setOrganizationId(request.organizationId());
        certification.setPracticeTypeId(request.practiceTypeId());
        certification.setSpecialty(normalizeText(request.specialty()));
        certification.setIntroduction(request.introduction());
        certification.setConsultationNotice(normalizeText(request.consultationNotice()));
        certification.setCertificationStatus(ExpertCertificationStatus.PENDING);
        certification.setCertificationSubmittedAt(LocalDateTime.now());
        certification.setCertificationReviewedAt(null);
        certification.setCertificationReviewedBy(null);
        certification.setRejectReason(null);
        if (certification.getId() == null) {
            expertCertificationMapper.insert(certification);
        } else {
            expertCertificationMapper.updateById(certification);
        }
        replaceCertificationCategories(certification.getId(), request.categoryIds());
        replaceCertificationFiles(certification.getId(), request.certificationFiles());
        return certificationStatus();
    }

    public AppExpertCertificationResponse certificationStatus() {
        Long userId = requireCurrentUserSession().userId();
        return findCurrentCertification(userId)
                .map(this::toCertificationResponse)
                .orElse(null);
    }

    private Expert requireVisibleExpert(Long id) {
        Expert expert = expertMapper.selectById(id);
        if (expert == null
                || !Objects.equals(expert.getDeleted(), 0)
                || expert.getStatus() != EnabledStatus.ENABLED
                || expert.getConsultEnabled() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Expert does not exist");
        }
        return expert;
    }

    private LambdaQueryWrapper<Expert> visibleExpertWrapper() {
        return new LambdaQueryWrapper<Expert>()
                .eq(Expert::getDeleted, 0)
                .eq(Expert::getStatus, EnabledStatus.ENABLED)
                .eq(Expert::getConsultEnabled, EnabledStatus.ENABLED);
    }

    private AppExpertResponse toExpertResponse(Expert expert, boolean includeDetails) {
        return new AppExpertResponse(expert.getId(), expert.getRealName(), expert.getGender(), expert.getBirthDate(),
                expert.getMobile(), appUserAvatarUrlResolver.resolve(null, expert.getAvatarUrl()), expert.getCoverUrl(), expert.getTitle(),
                expert.getOrganization(), expert.getSpecialty(), expert.getIntroduction(),
                expert.getConsultationNotice(), expert.getSortOrder(), includeDetails ? loadCategoryIds(expert.getId()) : List.of(),
                includeDetails ? loadExperiences(expert.getId()) : List.of());
    }

    private AppUserSession requireCurrentUserSession() {
        return currentAppUserResolver.requireCurrentUser();
    }

    private Optional<ExpertCertification> findCurrentCertification(Long userId) {
        return Optional.ofNullable(expertCertificationMapper.selectOne(new LambdaQueryWrapper<ExpertCertification>()
                .eq(ExpertCertification::getUserId, userId)
                .eq(ExpertCertification::getDeleted, 0)
                .last("LIMIT 1")));
    }

    private ExpertCertification createCertification(Long userId) {
        ExpertCertification certification = new ExpertCertification();
        certification.setUserId(userId);
        certification.setGender(Gender.UNKNOWN);
        certification.setCertificationStatus(ExpertCertificationStatus.UNSUBMITTED);
        certification.setDeleted(0);
        return certification;
    }

    private void replaceCertificationCategories(Long certificationId, List<Long> categoryIds) {
        expertCertificationCategoryRelationMapper.delete(new LambdaQueryWrapper<ExpertCertificationCategoryRelation>()
                .eq(ExpertCertificationCategoryRelation::getCertificationId, certificationId));
        for (Long categoryId : categoryIds.stream().distinct().toList()) {
            ExpertCertificationCategoryRelation relation = new ExpertCertificationCategoryRelation();
            relation.setCertificationId(certificationId);
            relation.setCategoryId(categoryId);
            expertCertificationCategoryRelationMapper.insert(relation);
        }
    }

    private void replaceCertificationFiles(Long certificationId, List<AppExpertCertificationFileRequest> files) {
        expertCertificationFileMapper.delete(new LambdaQueryWrapper<ExpertCertificationFile>()
                .eq(ExpertCertificationFile::getCertificationId, certificationId));
        if (files == null || files.isEmpty()) {
            return;
        }
        for (int i = 0; i < files.size(); i++) {
            AppExpertCertificationFileRequest fileRequest = files.get(i);
            if (fileRequest.fileAssetId() == null && !hasText(fileRequest.sourceUrl())) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Certification file requires fileAssetId or sourceUrl");
            }
            ExpertCertificationFile file = new ExpertCertificationFile();
            file.setCertificationId(certificationId);
            file.setFileAssetId(fileRequest.fileAssetId());
            file.setSourceUrl(normalizeText(fileRequest.sourceUrl()));
            file.setMaterialType(normalizeText(fileRequest.materialType()));
            file.setSortOrder(fileRequest.sortOrder() == null ? i : fileRequest.sortOrder());
            file.setDeleted(0);
            expertCertificationFileMapper.insert(file);
        }
    }

    private AppExpertCertificationResponse toCertificationResponse(ExpertCertification certification) {
        return new AppExpertCertificationResponse(
                certification.getId(),
                certification.getUserId(),
                certification.getRealName(),
                certification.getGender(),
                certification.getBirthDate(),
                certification.getMobile(),
                certification.getTitle(),
                certification.getOrganization(),
                certification.getOrganizationId(),
                certification.getPracticeTypeId(),
                certification.getSpecialty(),
                certification.getIntroduction(),
                certification.getConsultationNotice(),
                loadCertificationCategoryIds(certification.getId()),
                certification.getCertificationStatus(),
                certification.getCertificationSubmittedAt(),
                certification.getCertificationReviewedAt(),
                certification.getCertificationReviewedBy(),
                certification.getRejectReason(),
                loadCertificationFiles(certification.getId())
        );
    }

    private List<Long> loadCertificationCategoryIds(Long certificationId) {
        return expertCertificationCategoryRelationMapper.selectList(new LambdaQueryWrapper<ExpertCertificationCategoryRelation>()
                        .eq(ExpertCertificationCategoryRelation::getCertificationId, certificationId))
                .stream()
                .map(ExpertCertificationCategoryRelation::getCategoryId)
                .toList();
    }

    private List<AppExpertCertificationFileResponse> loadCertificationFiles(Long certificationId) {
        return expertCertificationFileMapper.selectList(new LambdaQueryWrapper<ExpertCertificationFile>()
                        .eq(ExpertCertificationFile::getCertificationId, certificationId)
                        .eq(ExpertCertificationFile::getDeleted, 0))
                .stream()
                .sorted(Comparator.comparing(ExpertCertificationFile::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(ExpertCertificationFile::getId))
                .map(file -> new AppExpertCertificationFileResponse(
                        file.getId(),
                        file.getFileAssetId(),
                        file.getSourceUrl(),
                        file.getMaterialType(),
                        file.getSortOrder()
                ))
                .toList();
    }

    private void validateCategoryIds(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "categoryIds must not be empty");
        }
        categoryIds.stream().distinct().forEach(this::requireEnabledCategory);
    }

    private void requireEnabledCategory(Long categoryId) {
        ExpertCategory category = expertCategoryMapper.selectById(categoryId);
        if (category == null
                || !Objects.equals(category.getDeleted(), 0)
                || category.getStatus() != EnabledStatus.ENABLED) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Expert category does not exist");
        }
    }

    private List<Long> loadCategoryIds(Long expertId) {
        return expertCategoryRelationMapper.selectList(new LambdaQueryWrapper<ExpertCategoryRelation>()
                        .eq(ExpertCategoryRelation::getExpertId, expertId))
                .stream()
                .map(ExpertCategoryRelation::getCategoryId)
                .toList();
    }

    private List<ExpertExperienceResponse> loadExperiences(Long expertId) {
        return expertExperienceMapper.selectList(new LambdaQueryWrapper<ExpertExperience>()
                        .eq(ExpertExperience::getDeleted, 0)
                        .eq(ExpertExperience::getExpertId, expertId)
                        .orderByAsc(ExpertExperience::getSortOrder)
                        .orderByDesc(ExpertExperience::getCreatedAt))
                .stream()
                .map(experience -> new ExpertExperienceResponse(experience.getId(), experience.getExpertId(),
                        experience.getExperienceType(), experience.getTitle(), experience.getDescription(),
                        experience.getStartDate(), experience.getEndDate(), experience.getSortOrder()))
                .toList();
    }

    private <E, R> PageResponse<R> pageResponse(Page<E> page, List<R> records) {
        return new PageResponse<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private long normalizePage(long page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(long size) {
        return size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeText(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
