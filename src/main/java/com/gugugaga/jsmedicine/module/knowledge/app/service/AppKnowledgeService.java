package com.gugugaga.jsmedicine.module.knowledge.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.knowledge.app.dto.AppKnowledgeCategoryResponse;
import com.gugugaga.jsmedicine.module.knowledge.app.dto.AppKnowledgeEntryResponse;
import com.gugugaga.jsmedicine.module.knowledge.app.dto.AppKnowledgeSearchResult;
import com.gugugaga.jsmedicine.module.knowledge.entity.KnowledgeCategory;
import com.gugugaga.jsmedicine.module.knowledge.entity.KnowledgeEntry;
import com.gugugaga.jsmedicine.module.knowledge.mapper.KnowledgeCategoryMapper;
import com.gugugaga.jsmedicine.module.knowledge.mapper.KnowledgeEntryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AppKnowledgeService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeEntryMapper entryMapper;

    public AppKnowledgeService(KnowledgeCategoryMapper categoryMapper, KnowledgeEntryMapper entryMapper) {
        this.categoryMapper = categoryMapper;
        this.entryMapper = entryMapper;
    }

    public List<AppKnowledgeCategoryResponse> categoryTree() {
        List<KnowledgeCategory> categories = categoryMapper.selectList(new LambdaQueryWrapper<KnowledgeCategory>()
                .eq(KnowledgeCategory::getDeleted, 0)
                .eq(KnowledgeCategory::getStatus, EnabledStatus.ENABLED)
                .orderByAsc(KnowledgeCategory::getSortOrder)
                .orderByDesc(KnowledgeCategory::getCreatedAt));
        Map<Long, List<KnowledgeCategory>> byParent = categories.stream()
                .collect(Collectors.groupingBy(category -> category.getParentId() == null ? 0L : category.getParentId()));
        return buildCategoryTree(0L, byParent);
    }

    public PageResponse<AppKnowledgeSearchResult> search(long page, long size, String keyword, Long categoryId) {
        Page<KnowledgeEntry> entryPage = entryMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                visibleEntryWrapper()
                        .eq(categoryId != null, KnowledgeEntry::getCategoryId, categoryId)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(KnowledgeEntry::getTitle, keyword)
                                .or()
                                .like(KnowledgeEntry::getKeywords, keyword)
                                .or()
                                .like(KnowledgeEntry::getSummary, keyword))
                        .orderByAsc(KnowledgeEntry::getSortOrder)
                        .orderByDesc(KnowledgeEntry::getPublishedAt));
        return pageResponse(entryPage, entryPage.getRecords().stream().map(this::toSearchResult).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public AppKnowledgeEntryResponse entryDetail(Long id) {
        KnowledgeEntry entry = requireVisibleEntry(id);
        entry.setViewCount((entry.getViewCount() == null ? 0 : entry.getViewCount()) + 1);
        entryMapper.updateById(entry);
        return toEntryResponse(entry);
    }

    private List<AppKnowledgeCategoryResponse> buildCategoryTree(Long parentId, Map<Long, List<KnowledgeCategory>> byParent) {
        return byParent.getOrDefault(parentId, List.of())
                .stream()
                .sorted(Comparator.comparing(category -> category.getSortOrder() == null ? 0 : category.getSortOrder()))
                .map(category -> new AppKnowledgeCategoryResponse(category.getId(), category.getParentId(), category.getCategoryName(),
                        category.getCategoryCode(), category.getDescription(), category.getSortOrder(), buildCategoryTree(category.getId(), byParent)))
                .toList();
    }

    private KnowledgeEntry requireVisibleEntry(Long id) {
        KnowledgeEntry entry = entryMapper.selectById(id);
        if (entry == null
                || !Objects.equals(entry.getDeleted(), 0)
                || entry.getReviewStatus() != ReviewStatus.APPROVED
                || entry.getPublishStatus() != PublishStatus.PUBLISHED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Knowledge entry does not exist");
        }
        return entry;
    }

    private LambdaQueryWrapper<KnowledgeEntry> visibleEntryWrapper() {
        return new LambdaQueryWrapper<KnowledgeEntry>()
                .eq(KnowledgeEntry::getDeleted, 0)
                .eq(KnowledgeEntry::getReviewStatus, ReviewStatus.APPROVED)
                .eq(KnowledgeEntry::getPublishStatus, PublishStatus.PUBLISHED);
    }

    private AppKnowledgeSearchResult toSearchResult(KnowledgeEntry entry) {
        KnowledgeCategory category = entry.getCategoryId() == null ? null : categoryMapper.selectById(entry.getCategoryId());
        return new AppKnowledgeSearchResult(entry.getId(), entry.getTitle(), entry.getSummary(), entry.getCategoryId(),
                category == null ? null : category.getCategoryName(), category == null ? null : category.getCategoryCode());
    }

    private AppKnowledgeEntryResponse toEntryResponse(KnowledgeEntry entry) {
        return new AppKnowledgeEntryResponse(entry.getId(), entry.getCategoryId(), entry.getTitle(), entry.getSummary(),
                entry.getCoverUrl(), entry.getContent(), entry.getKeywords(), entry.getSource(), entry.getSortOrder(),
                entry.getViewCount(), entry.getPublishedAt());
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
