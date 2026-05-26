package com.gugugaga.jsmedicine.module.expert.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.expert.admin.dto.ExpertExperienceResponse;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertCategoryResponse;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertResponse;
import com.gugugaga.jsmedicine.module.expert.entity.Expert;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCategory;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertCategoryRelation;
import com.gugugaga.jsmedicine.module.expert.entity.ExpertExperience;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCategoryMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertCategoryRelationMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertExperienceMapper;
import com.gugugaga.jsmedicine.module.expert.mapper.ExpertMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AppExpertService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final ExpertCategoryMapper expertCategoryMapper;
    private final ExpertMapper expertMapper;
    private final ExpertCategoryRelationMapper expertCategoryRelationMapper;
    private final ExpertExperienceMapper expertExperienceMapper;

    public AppExpertService(
            ExpertCategoryMapper expertCategoryMapper,
            ExpertMapper expertMapper,
            ExpertCategoryRelationMapper expertCategoryRelationMapper,
            ExpertExperienceMapper expertExperienceMapper
    ) {
        this.expertCategoryMapper = expertCategoryMapper;
        this.expertMapper = expertMapper;
        this.expertCategoryRelationMapper = expertCategoryRelationMapper;
        this.expertExperienceMapper = expertExperienceMapper;
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
        return new AppExpertResponse(expert.getId(), expert.getRealName(), expert.getAvatarUrl(), expert.getTitle(),
                expert.getOrganization(), expert.getSpecialty(), expert.getIntroduction(), expert.getConsultationNotice(),
                expert.getSortOrder(), includeDetails ? loadCategoryIds(expert.getId()) : List.of(),
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
