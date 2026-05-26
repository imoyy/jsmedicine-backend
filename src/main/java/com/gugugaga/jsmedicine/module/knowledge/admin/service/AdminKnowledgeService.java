package com.gugugaga.jsmedicine.module.knowledge.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.PublishStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeCategoryRequest;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeCategoryResponse;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeEntryRequest;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeEntryResponse;
import com.gugugaga.jsmedicine.module.knowledge.admin.dto.KnowledgeReviewRequest;
import com.gugugaga.jsmedicine.module.knowledge.entity.KnowledgeCategory;
import com.gugugaga.jsmedicine.module.knowledge.entity.KnowledgeEntry;
import com.gugugaga.jsmedicine.module.knowledge.mapper.KnowledgeCategoryMapper;
import com.gugugaga.jsmedicine.module.knowledge.mapper.KnowledgeEntryMapper;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.service.AuditRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AdminKnowledgeService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeEntryMapper entryMapper;
    private final AuditRecordService auditRecordService;
    private final CurrentAdminAccessor currentAdminAccessor;

    public AdminKnowledgeService(
            KnowledgeCategoryMapper categoryMapper,
            KnowledgeEntryMapper entryMapper,
            AuditRecordService auditRecordService,
            CurrentAdminAccessor currentAdminAccessor
    ) {
        this.categoryMapper = categoryMapper;
        this.entryMapper = entryMapper;
        this.auditRecordService = auditRecordService;
        this.currentAdminAccessor = currentAdminAccessor;
    }

    public PageResponse<KnowledgeCategoryResponse> pageCategories(long page, long size, String keyword, Long parentId, EnabledStatus status) {
        Page<KnowledgeCategory> categoryPage = categoryMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<KnowledgeCategory>()
                        .eq(KnowledgeCategory::getDeleted, 0)
                        .eq(parentId != null, KnowledgeCategory::getParentId, parentId)
                        .eq(status != null, KnowledgeCategory::getStatus, status)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(KnowledgeCategory::getCategoryName, keyword)
                                .or()
                                .like(KnowledgeCategory::getCategoryCode, keyword))
                        .orderByAsc(KnowledgeCategory::getSortOrder)
                        .orderByDesc(KnowledgeCategory::getCreatedAt));
        return pageResponse(categoryPage, categoryPage.getRecords().stream().map(this::toCategoryResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public KnowledgeCategoryResponse createCategory(KnowledgeCategoryRequest request) {
        if (request.parentId() != null) {
            requireCategory(request.parentId());
        }
        KnowledgeCategory category = new KnowledgeCategory();
        fillCategory(category, request);
        category.setDeleted(0);
        categoryMapper.insert(category);
        return toCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public KnowledgeCategoryResponse updateCategory(Long id, KnowledgeCategoryRequest request) {
        KnowledgeCategory category = requireCategory(id);
        if (request.parentId() != null) {
            requireCategory(request.parentId());
        }
        fillCategory(category, request);
        categoryMapper.updateById(category);
        return toCategoryResponse(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        requireCategory(id);
        categoryMapper.deleteById(id);
    }

    public PageResponse<KnowledgeEntryResponse> pageEntries(long page, long size, String keyword, Long categoryId, ReviewStatus reviewStatus) {
        Page<KnowledgeEntry> entryPage = entryMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<KnowledgeEntry>()
                        .eq(KnowledgeEntry::getDeleted, 0)
                        .eq(categoryId != null, KnowledgeEntry::getCategoryId, categoryId)
                        .eq(reviewStatus != null, KnowledgeEntry::getReviewStatus, reviewStatus)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(KnowledgeEntry::getTitle, keyword)
                                .or()
                                .like(KnowledgeEntry::getKeywords, keyword))
                        .orderByAsc(KnowledgeEntry::getSortOrder)
                        .orderByDesc(KnowledgeEntry::getCreatedAt));
        return pageResponse(entryPage, entryPage.getRecords().stream().map(this::toEntryResponse).toList());
    }

    public KnowledgeEntryResponse entryDetail(Long id) {
        return toEntryResponse(requireEntry(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public KnowledgeEntryResponse createEntry(KnowledgeEntryRequest request) {
        if (request.categoryId() != null) {
            requireCategory(request.categoryId());
        }
        KnowledgeEntry entry = new KnowledgeEntry();
        fillEntry(entry, request);
        entry.setViewCount(0L);
        entry.setDeleted(0);
        entryMapper.insert(entry);
        return toEntryResponse(entry);
    }

    @Transactional(rollbackFor = Exception.class)
    public KnowledgeEntryResponse updateEntry(Long id, KnowledgeEntryRequest request) {
        KnowledgeEntry entry = requireEntry(id);
        if (request.categoryId() != null) {
            requireCategory(request.categoryId());
        }
        fillEntry(entry, request);
        entryMapper.updateById(entry);
        return toEntryResponse(entry);
    }

    @Transactional(rollbackFor = Exception.class)
    public KnowledgeEntryResponse reviewEntry(Long id, KnowledgeReviewRequest request) {
        KnowledgeEntry entry = requireEntry(id);
        ReviewStatus before = entry.getReviewStatus();
        entry.setReviewStatus(request.reviewStatus());
        if (request.reviewStatus() == ReviewStatus.APPROVED) {
            entry.setPublishStatus(PublishStatus.PUBLISHED);
            entry.setPublishedAt(LocalDateTime.now());
        }
        if (request.reviewStatus() == ReviewStatus.REJECTED) {
            entry.setPublishStatus(PublishStatus.UNPUBLISHED);
        }
        entryMapper.updateById(entry);
        saveAudit("knowledge_entry", id, before, request.reviewStatus(), request.comment());
        return toEntryResponse(entry);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteEntry(Long id) {
        requireEntry(id);
        entryMapper.deleteById(id);
    }

    private void fillCategory(KnowledgeCategory category, KnowledgeCategoryRequest request) {
        category.setParentId(request.parentId());
        category.setCategoryName(request.categoryName());
        category.setCategoryCode(request.categoryCode());
        category.setDescription(request.description());
        category.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        category.setStatus(request.status());
    }

    private void fillEntry(KnowledgeEntry entry, KnowledgeEntryRequest request) {
        entry.setCategoryId(request.categoryId());
        entry.setTitle(request.title());
        entry.setSummary(request.summary());
        entry.setCoverUrl(request.coverUrl());
        entry.setContent(request.content());
        entry.setKeywords(request.keywords());
        entry.setSource(request.source());
        entry.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        entry.setReviewStatus(request.reviewStatus());
        entry.setPublishStatus(request.publishStatus());
        entry.setPublishedAt(request.publishedAt());
    }

    private KnowledgeCategory requireCategory(Long id) {
        KnowledgeCategory category = categoryMapper.selectById(id);
        if (category == null || !Objects.equals(category.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Knowledge category does not exist");
        }
        return category;
    }

    private KnowledgeEntry requireEntry(Long id) {
        KnowledgeEntry entry = entryMapper.selectById(id);
        if (entry == null || !Objects.equals(entry.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Knowledge entry does not exist");
        }
        return entry;
    }

    private KnowledgeCategoryResponse toCategoryResponse(KnowledgeCategory category) {
        return new KnowledgeCategoryResponse(category.getId(), category.getParentId(), category.getCategoryName(),
                category.getCategoryCode(), category.getDescription(), category.getSortOrder(), category.getStatus());
    }

    private KnowledgeEntryResponse toEntryResponse(KnowledgeEntry entry) {
        return new KnowledgeEntryResponse(entry.getId(), entry.getCategoryId(), entry.getTitle(), entry.getSummary(),
                entry.getCoverUrl(), entry.getContent(), entry.getKeywords(), entry.getSource(), entry.getSortOrder(),
                entry.getViewCount(), entry.getReviewStatus(), entry.getPublishStatus(), entry.getPublishedAt());
    }

    private void saveAudit(String targetType, Long targetId, ReviewStatus before, ReviewStatus after, String comment) {
        AuditRecord auditRecord = new AuditRecord();
        auditRecord.setTargetType(targetType);
        auditRecord.setTargetId(targetId);
        auditRecord.setBeforeStatus(before == null ? null : before.getValue());
        auditRecord.setAfterStatus(after.getValue());
        auditRecord.setAuditComment(comment);
        auditRecord.setAuditorId(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        auditRecord.setAuditedAt(LocalDateTime.now());
        auditRecordService.save(auditRecord);
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
