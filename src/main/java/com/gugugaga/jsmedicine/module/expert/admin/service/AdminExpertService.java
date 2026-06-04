package com.gugugaga.jsmedicine.module.expert.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityStatus;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityType;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.storage.service.StableCoverUrlService;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertCategoryRequest;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertCategoryResponse;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertExperienceRequest;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertExperienceResponse;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertRequest;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertResponse;
import com.gugugaga.jsmedicine.module.expert.entity.Expert;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCategory;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCategoryRelation;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertExperience;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCategoryMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCategoryRelationMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertExperienceMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertMapper;
import com.gugugaga.jsmedicine.module.user.entity.AppUser;
import com.gugugaga.jsmedicine.module.user.entity.AppUserIdentity;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserIdentityMapper;
import com.gugugaga.jsmedicine.module.user.mapper.AppUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AdminExpertService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final ExpertCategoryMapper expertCategoryMapper;
    private final ExpertMapper expertMapper;
    private final ExpertCategoryRelationMapper expertCategoryRelationMapper;
    private final ExpertExperienceMapper expertExperienceMapper;
    private final AppUserMapper appUserMapper;
    private final AppUserIdentityMapper appUserIdentityMapper;
    private final StableCoverUrlService stableCoverUrlService;

    public AdminExpertService(
            ExpertCategoryMapper expertCategoryMapper,
            ExpertMapper expertMapper,
            ExpertCategoryRelationMapper expertCategoryRelationMapper,
            ExpertExperienceMapper expertExperienceMapper,
            AppUserMapper appUserMapper,
            AppUserIdentityMapper appUserIdentityMapper,
            StableCoverUrlService stableCoverUrlService
    ) {
        this.expertCategoryMapper = expertCategoryMapper;
        this.expertMapper = expertMapper;
        this.expertCategoryRelationMapper = expertCategoryRelationMapper;
        this.expertExperienceMapper = expertExperienceMapper;
        this.appUserMapper = appUserMapper;
        this.appUserIdentityMapper = appUserIdentityMapper;
        this.stableCoverUrlService = stableCoverUrlService;
    }

    public PageResponse<ExpertCategoryResponse> pageCategories(long page, long size, String keyword, Long parentId, EnabledStatus status) {
        Page<ExpertCategory> categoryPage = expertCategoryMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<ExpertCategory>()
                        .eq(ExpertCategory::getDeleted, 0)
                        .eq(parentId != null, ExpertCategory::getParentId, parentId)
                        .eq(status != null, ExpertCategory::getStatus, status)
                        .and(hasText(keyword), wrapper -> wrapper.like(ExpertCategory::getCategoryName, keyword))
                        .orderByAsc(ExpertCategory::getSortOrder)
                        .orderByDesc(ExpertCategory::getCreatedAt));
        return pageResponse(categoryPage, categoryPage.getRecords().stream().map(this::toCategoryResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ExpertCategoryResponse createCategory(ExpertCategoryRequest request) {
        validateCategoryParent(request.parentId(), null);
        ExpertCategory category = new ExpertCategory();
        fillCategory(category, request);
        category.setDeleted(0);
        expertCategoryMapper.insert(category);
        return toCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public ExpertCategoryResponse updateCategory(Long id, ExpertCategoryRequest request) {
        ExpertCategory category = requireCategory(id);
        validateCategoryParent(request.parentId(), id);
        if (request.parentId() != null && hasChildCategories(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Category with child categories must remain a top-level category");
        }
        fillCategory(category, request);
        expertCategoryMapper.updateById(category);
        return toCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        requireCategory(id);
        if (hasChildCategories(id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Expert category has child categories");
        }
        if (hasExpertBindings(id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Expert category is bound to experts");
        }
        expertCategoryMapper.deleteById(id);
    }

    public PageResponse<ExpertResponse> pageExperts(long page, long size, String keyword, Long categoryId, EnabledStatus status) {
        List<Long> expertIds = categoryId == null ? null : expertCategoryRelationMapper.selectList(new LambdaQueryWrapper<ExpertCategoryRelation>()
                        .eq(ExpertCategoryRelation::getCategoryId, categoryId))
                .stream()
                .map(ExpertCategoryRelation::getExpertId)
                .toList();
        if (categoryId != null && expertIds.isEmpty()) {
            return new PageResponse<>(List.of(), 0, normalizePage(page), normalizeSize(size));
        }
        Page<Expert> expertPage = expertMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<Expert>()
                        .eq(Expert::getDeleted, 0)
                        .in(expertIds != null, Expert::getId, expertIds)
                        .eq(status != null, Expert::getStatus, status)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(Expert::getRealName, keyword)
                                .or()
                                .like(Expert::getSpecialty, keyword))
                        .orderByAsc(Expert::getSortOrder)
                        .orderByDesc(Expert::getCreatedAt));
        return pageResponse(expertPage, expertPage.getRecords().stream().map(expert -> toExpertResponse(expert, false)).toList());
    }

    public ExpertResponse expertDetail(Long id) {
        return toExpertResponse(requireExpert(id), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ExpertResponse createExpert(ExpertRequest request) {
        Expert expert = new Expert();
        fillExpert(expert, request);
        expert.setDeleted(0);
        expertMapper.insert(expert);
        if (expert.getUserId() != null) {
            ensureExpertIdentity(expert.getUserId());
        }
        return toExpertResponse(expert, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public ExpertResponse updateExpert(Long id, ExpertRequest request) {
        Expert expert = requireExpert(id);
        fillExpert(expert, request);
        expertMapper.updateById(expert);
        if (expert.getUserId() != null) {
            ensureExpertIdentity(expert.getUserId());
        }
        return toExpertResponse(expert, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteExpert(Long id) {
        requireExpert(id);
        expertMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> replaceExpertCategories(Long expertId, List<Long> categoryIds) {
        requireExpert(expertId);
        expertCategoryRelationMapper.delete(new LambdaQueryWrapper<ExpertCategoryRelation>()
                .eq(ExpertCategoryRelation::getExpertId, expertId));
        if (categoryIds != null) {
            categoryIds.stream().distinct().forEach(categoryId -> {
                requireCategory(categoryId);
                ExpertCategoryRelation relation = new ExpertCategoryRelation();
                relation.setExpertId(expertId);
                relation.setCategoryId(categoryId);
                expertCategoryRelationMapper.insert(relation);
            });
        }
        return loadCategoryIds(expertId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<ExpertExperienceResponse> replaceExperiences(Long expertId, List<ExpertExperienceRequest> requests) {
        requireExpert(expertId);
        expertExperienceMapper.delete(new LambdaQueryWrapper<ExpertExperience>().eq(ExpertExperience::getExpertId, expertId));
        if (requests != null) {
            requests.forEach(request -> {
                ExpertExperience experience = new ExpertExperience();
                experience.setExpertId(expertId);
                fillExperience(experience, request);
                experience.setDeleted(0);
                expertExperienceMapper.insert(experience);
            });
        }
        return loadExperiences(expertId);
    }

    private void fillCategory(ExpertCategory category, ExpertCategoryRequest request) {
        category.setParentId(request.parentId());
        category.setCategoryName(request.categoryName());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(request.status());
    }

    private void fillExpert(Expert expert, ExpertRequest request) {
        StableCoverUrlService.CoverBinding coverBinding = stableCoverUrlService.resolveCoverBinding(
                request.coverUrl(),
                expert.getCoverUrl(),
                expert.getCoverFileAssetId()
        );
        expert.setUserId(validateAndNormalizeUserId(request.userId(), expert.getId()));
        expert.setRealName(request.realName());
        expert.setGender(request.gender());
        expert.setBirthDate(request.birthDate());
        expert.setMobile(request.mobile());
        expert.setAvatarUrl(request.avatarUrl());
        expert.setCoverUrl(coverBinding.coverUrl());
        expert.setCoverFileAssetId(coverBinding.fileAssetId());
        expert.setTitle(request.title());
        expert.setOrganization(request.organization());
        expert.setOrganizationId(request.organizationId());
        expert.setSpecialty(request.specialty());
        expert.setPracticeTypeId(request.practiceTypeId());
        expert.setIntroduction(request.introduction());
        expert.setStatus(request.status());
        expert.setConsultEnabled(request.consultEnabled());
        expert.setConsultationNotice(request.consultationNotice());
        expert.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private void fillExperience(ExpertExperience experience, ExpertExperienceRequest request) {
        experience.setExperienceType(request.experienceType());
        experience.setTitle(request.title());
        experience.setDescription(request.description());
        experience.setStartDate(request.startDate());
        experience.setEndDate(request.endDate());
        experience.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private ExpertCategory requireCategory(Long id) {
        ExpertCategory category = expertCategoryMapper.selectById(id);
        if (category == null || !Objects.equals(category.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Expert category does not exist");
        }
        return category;
    }

    private void validateCategoryParent(Long parentId, Long currentId) {
        if (parentId == null) {
            return;
        }
        if (Objects.equals(parentId, currentId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Expert category parentId must not equal current id");
        }
        ExpertCategory parentCategory = requireCategory(parentId);
        if (parentCategory.getParentId() != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Expert category supports only two levels");
        }
    }

    private boolean hasChildCategories(Long categoryId) {
        return expertCategoryMapper.selectCount(new LambdaQueryWrapper<ExpertCategory>()
                .eq(ExpertCategory::getDeleted, 0)
                .eq(ExpertCategory::getParentId, categoryId)) > 0;
    }

    private boolean hasExpertBindings(Long categoryId) {
        return expertCategoryRelationMapper.selectCount(new LambdaQueryWrapper<ExpertCategoryRelation>()
                .eq(ExpertCategoryRelation::getCategoryId, categoryId)) > 0;
    }

    private Expert requireExpert(Long id) {
        Expert expert = expertMapper.selectById(id);
        if (expert == null || !Objects.equals(expert.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Expert does not exist");
        }
        return expert;
    }

    private Long validateAndNormalizeUserId(Long userId, Long currentExpertId) {
        if (userId == null) {
            return null;
        }
        AppUser user = appUserMapper.selectById(userId);
        if (user == null || !Objects.equals(user.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "App user does not exist");
        }
        Expert existing = expertMapper.selectOne(new LambdaQueryWrapper<Expert>()
                .eq(Expert::getUserId, userId)
                .eq(Expert::getDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), currentExpertId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "App user is already bound to another expert");
        }
        return userId;
    }

    private void ensureExpertIdentity(Long userId) {
        AppUserIdentity identity = appUserIdentityMapper.selectOne(new LambdaQueryWrapper<AppUserIdentity>()
                .eq(AppUserIdentity::getUserId, userId)
                .eq(AppUserIdentity::getIdentityType, AppUserIdentityType.EXPERT)
                .eq(AppUserIdentity::getDeleted, 0)
                .last("LIMIT 1"));
        if (identity == null) {
            identity = new AppUserIdentity();
            identity.setUserId(userId);
            identity.setIdentityType(AppUserIdentityType.EXPERT);
            identity.setDeleted(0);
        }
        identity.setIdentityStatus(AppUserIdentityStatus.ACTIVE);
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

    private ExpertCategoryResponse toCategoryResponse(ExpertCategory category) {
        ExpertCategory parentCategory = category.getParentId() == null ? null : requireCategory(category.getParentId());
        return new ExpertCategoryResponse(category.getId(), category.getParentId(),
                parentCategory == null ? null : parentCategory.getCategoryName(),
                category.getParentId() == null ? 1 : 2,
                category.getCategoryName(), category.getSortOrder(), category.getStatus());
    }

    private ExpertResponse toExpertResponse(Expert expert, boolean includeDetails) {
        return new ExpertResponse(expert.getId(), expert.getUserId(), expert.getRealName(), expert.getGender(),
                expert.getBirthDate(), expert.getMobile(), expert.getAvatarUrl(), expert.getCoverUrl(), expert.getTitle(),
                expert.getOrganization(), expert.getOrganizationId(), expert.getSpecialty(), expert.getPracticeTypeId(),
                expert.getIntroduction(), expert.getStatus(), expert.getConsultEnabled(), expert.getConsultationNotice(), expert.getSortOrder(),
                includeDetails ? loadCategoryIds(expert.getId()) : List.of(),
                includeDetails ? loadExperiences(expert.getId()) : List.of());
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
}
