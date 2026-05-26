package com.gugugaga.jsmedicine.module.learning.live.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionRequest;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionResponse;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSession;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionMapper;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.service.AuditRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AdminLiveService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final LiveSessionMapper liveSessionMapper;
    private final CurrentAdminAccessor currentAdminAccessor;
    private final AuditRecordService auditRecordService;

    public AdminLiveService(
            LiveSessionMapper liveSessionMapper,
            CurrentAdminAccessor currentAdminAccessor,
            AuditRecordService auditRecordService
    ) {
        this.liveSessionMapper = liveSessionMapper;
        this.currentAdminAccessor = currentAdminAccessor;
        this.auditRecordService = auditRecordService;
    }

    public PageResponse<LiveSessionResponse> pageLives(long page, long size, String keyword, ReviewStatus reviewStatus, LiveStatus liveStatus) {
        Page<LiveSession> livePage = liveSessionMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<LiveSession>()
                        .eq(LiveSession::getDeleted, 0)
                        .eq(reviewStatus != null, LiveSession::getReviewStatus, reviewStatus)
                        .eq(liveStatus != null, LiveSession::getLiveStatus, liveStatus)
                        .and(hasText(keyword), wrapper -> wrapper.like(LiveSession::getTitle, keyword).or().like(LiveSession::getAnchorName, keyword))
                        .orderByDesc(LiveSession::getStartAt));
        return pageResponse(livePage, livePage.getRecords().stream().map(this::toResponse).toList());
    }

    public LiveSessionResponse liveDetail(Long id) {
        return toResponse(requireLive(id));
    }

    @Transactional(rollbackFor = Exception.class)
    public LiveSessionResponse createLive(LiveSessionRequest request) {
        validateTime(request.startAt(), request.endAt());
        LiveSession live = new LiveSession();
        fillLive(live, request);
        live.setDeleted(0);
        liveSessionMapper.insert(live);
        return toResponse(live);
    }

    @Transactional(rollbackFor = Exception.class)
    public LiveSessionResponse updateLive(Long id, LiveSessionRequest request) {
        validateTime(request.startAt(), request.endAt());
        LiveSession live = requireLive(id);
        fillLive(live, request);
        liveSessionMapper.updateById(live);
        return toResponse(live);
    }

    @Transactional(rollbackFor = Exception.class)
    public LiveSessionResponse reviewLive(Long id, ReviewStatus reviewStatus, String comment) {
        LiveSession live = requireLive(id);
        ReviewStatus before = live.getReviewStatus();
        live.setReviewStatus(reviewStatus);
        if (reviewStatus == ReviewStatus.REJECTED) {
            live.setLiveStatus(LiveStatus.CANCELED);
        }
        liveSessionMapper.updateById(live);
        saveAudit("live_session", id, before.getValue(), reviewStatus.getValue(), comment);
        return toResponse(live);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLive(Long id) {
        requireLive(id);
        liveSessionMapper.deleteById(id);
    }

    private void fillLive(LiveSession live, LiveSessionRequest request) {
        live.setTitle(request.title());
        live.setCoverUrl(request.coverUrl());
        live.setAnchorName(request.anchorName());
        live.setLiveUrl(request.liveUrl());
        live.setPlaybackUrl(request.playbackUrl());
        live.setStartAt(request.startAt());
        live.setEndAt(request.endAt());
        live.setReviewStatus(request.reviewStatus());
        live.setLiveStatus(request.liveStatus());
    }

    private void validateTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (endAt != null && !endAt.isAfter(startAt)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Live end time must be after start time");
        }
    }

    private LiveSession requireLive(Long id) {
        LiveSession live = liveSessionMapper.selectById(id);
        if (live == null || !Objects.equals(live.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Live session does not exist");
        }
        return live;
    }

    private LiveSessionResponse toResponse(LiveSession live) {
        return new LiveSessionResponse(live.getId(), live.getTitle(), live.getCoverUrl(), live.getAnchorName(),
                live.getLiveUrl(), live.getPlaybackUrl(), live.getStartAt(), live.getEndAt(),
                live.getReviewStatus(), live.getLiveStatus());
    }

    private void saveAudit(String targetType, Long targetId, Integer before, Integer after, String comment) {
        AuditRecord record = new AuditRecord();
        record.setTargetType(targetType);
        record.setTargetId(targetId);
        record.setBeforeStatus(before);
        record.setAfterStatus(after);
        record.setAuditComment(comment);
        record.setAuditorId(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        record.setAuditedAt(LocalDateTime.now());
        auditRecordService.save(record);
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
