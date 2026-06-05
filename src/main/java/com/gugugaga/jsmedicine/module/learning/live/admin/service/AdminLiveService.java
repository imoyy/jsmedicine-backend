package com.gugugaga.jsmedicine.module.learning.live.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.common.service.ResourceTagService;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.infrastructure.storage.service.StableCoverUrlService;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionRequest;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionStreamResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionVideoRequest;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionVideoResponse;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSession;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSessionVideo;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionMapper;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionVideoMapper;
import com.gugugaga.jsmedicine.module.learning.live.service.LiveStreamService;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.service.AuditRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AdminLiveService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;
    private static final String RESOURCE_TYPE_LIVE = "live";

    private final LiveSessionMapper liveSessionMapper;
    private final LiveSessionVideoMapper liveSessionVideoMapper;
    private final CurrentAdminAccessor currentAdminAccessor;
    private final AuditRecordService auditRecordService;
    private final ResourceTagService resourceTagService;
    private final StableCoverUrlService stableCoverUrlService;
    private final LiveStreamService liveStreamService;

    public AdminLiveService(
            LiveSessionMapper liveSessionMapper,
            LiveSessionVideoMapper liveSessionVideoMapper,
            CurrentAdminAccessor currentAdminAccessor,
            AuditRecordService auditRecordService,
            ResourceTagService resourceTagService,
            StableCoverUrlService stableCoverUrlService,
            LiveStreamService liveStreamService
    ) {
        this.liveSessionMapper = liveSessionMapper;
        this.liveSessionVideoMapper = liveSessionVideoMapper;
        this.currentAdminAccessor = currentAdminAccessor;
        this.auditRecordService = auditRecordService;
        this.resourceTagService = resourceTagService;
        this.stableCoverUrlService = stableCoverUrlService;
        this.liveStreamService = liveStreamService;
    }

    public PageResponse<LiveSessionResponse> pageLives(long page, long size, String keyword, ReviewStatus reviewStatus, LiveStatus liveStatus) {
        Page<LiveSession> livePage = liveSessionMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<LiveSession>()
                        .eq(LiveSession::getDeleted, 0)
                        .eq(reviewStatus != null, LiveSession::getReviewStatus, reviewStatus)
                        .eq(liveStatus != null, LiveSession::getLiveStatus, liveStatus)
                        .and(hasText(keyword), wrapper -> wrapper
                                .like(LiveSession::getTitle, keyword)
                                .or()
                                .like(LiveSession::getAnchorName, keyword)
                                .or()
                                .like(LiveSession::getSpeakerName, keyword))
                        .orderByDesc(LiveSession::getStartAt));
        return pageResponse(livePage, livePage.getRecords().stream().map(live -> toResponse(live, false)).toList());
    }

    public LiveSessionResponse liveDetail(Long id) {
        return toResponse(requireLive(id), true);
    }

    public LiveSessionStreamResponse liveStreamDetail(Long id) {
        return liveStreamService.buildStreamResponse(requireLive(id));
    }

    public PageResponse<LiveSessionVideoResponse> pageLiveVideos(Long liveSessionId, long page, long size) {
        Page<LiveSessionVideo> videoPage = liveSessionVideoMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                new LambdaQueryWrapper<LiveSessionVideo>()
                        .eq(LiveSessionVideo::getDeleted, 0)
                        .eq(liveSessionId != null, LiveSessionVideo::getLiveSessionId, liveSessionId)
                        .orderByAsc(LiveSessionVideo::getSortOrder)
                        .orderByDesc(LiveSessionVideo::getCreatedAt));
        return pageResponse(videoPage, videoPage.getRecords().stream().map(this::toVideoResponse).toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public LiveSessionResponse createLive(LiveSessionRequest request) {
        validateTime(request.startAt(), request.endAt());
        LiveSession live = new LiveSession();
        fillLive(live, request);
        live.setDeleted(0);
        if (StringUtils.hasText(live.getStreamName())) {
            liveStreamService.ensureUniqueStreamName(live.getStreamName(), null);
        }
        liveSessionMapper.insert(live);
        if (!StringUtils.hasText(live.getStreamName())) {
            live.setStreamName(liveStreamService.buildDefaultStreamName(live.getId()));
            liveStreamService.ensureUniqueStreamName(live.getStreamName(), live.getId());
            liveSessionMapper.updateById(live);
        }
        resourceTagService.replaceTags(RESOURCE_TYPE_LIVE, live.getId(), request.tags());
        return toResponse(requireLive(live.getId()), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public LiveSessionResponse updateLive(Long id, LiveSessionRequest request) {
        validateTime(request.startAt(), request.endAt());
        LiveSession live = requireLive(id);
        fillLive(live, request);
        if (StringUtils.hasText(live.getStreamName())) {
            liveStreamService.ensureUniqueStreamName(live.getStreamName(), live.getId());
        }
        liveSessionMapper.updateById(live);
        resourceTagService.replaceTags(RESOURCE_TYPE_LIVE, live.getId(), request.tags());
        return toResponse(requireLive(id), true);
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
        saveAudit(id, before.getValue(), reviewStatus.getValue(), comment);
        return toResponse(requireLive(id), true);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLive(Long id) {
        requireLive(id);
        liveSessionVideoMapper.delete(new LambdaQueryWrapper<LiveSessionVideo>()
                .eq(LiveSessionVideo::getLiveSessionId, id));
        liveSessionMapper.deleteById(id);
        resourceTagService.replaceTags(RESOURCE_TYPE_LIVE, id, List.of());
    }

    @Transactional(rollbackFor = Exception.class)
    public LiveSessionVideoResponse createLiveVideo(LiveSessionVideoRequest request) {
        requireLive(request.liveSessionId());
        LiveSessionVideo video = new LiveSessionVideo();
        fillLiveVideo(video, request);
        video.setDeleted(0);
        liveSessionVideoMapper.insert(video);
        return toVideoResponse(video);
    }

    @Transactional(rollbackFor = Exception.class)
    public LiveSessionVideoResponse updateLiveVideo(Long id, LiveSessionVideoRequest request) {
        requireLive(request.liveSessionId());
        LiveSessionVideo video = requireLiveVideo(id);
        fillLiveVideo(video, request);
        liveSessionVideoMapper.updateById(video);
        return toVideoResponse(video);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLiveVideo(Long id) {
        requireLiveVideo(id);
        liveSessionVideoMapper.deleteById(id);
    }

    private void fillLive(LiveSession live, LiveSessionRequest request) {
        StableCoverUrlService.CoverBinding coverBinding = stableCoverUrlService.resolveCoverBinding(
                request.coverUrl(),
                live.getCoverUrl(),
                live.getCoverFileAssetId()
        );
        live.setTitle(request.title());
        live.setCoverUrl(coverBinding.coverUrl());
        live.setCoverFileAssetId(coverBinding.fileAssetId());
        String anchorName = hasText(request.anchorName()) ? request.anchorName() : request.speakerName();
        String speakerName = hasText(request.speakerName()) ? request.speakerName() : request.anchorName();
        live.setAnchorName(anchorName);
        live.setStreamName(hasText(request.streamName()) ? request.streamName() : live.getStreamName());
        live.setSpeakerName(speakerName);
        live.setLiveUrl(request.liveUrl());
        live.setPlaybackUrl(request.playbackUrl());
        live.setStartAt(request.startAt());
        live.setEndAt(request.endAt());
        live.setReviewStatus(request.reviewStatus());
        live.setLiveStatus(request.liveStatus());
    }

    private void fillLiveVideo(LiveSessionVideo video, LiveSessionVideoRequest request) {
        video.setLiveSessionId(request.liveSessionId());
        video.setTitle(request.title());
        video.setVideoUrl(request.videoUrl());
        video.setDurationSeconds(request.durationSeconds());
        video.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
        video.setStatus(request.status());
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

    private LiveSessionVideo requireLiveVideo(Long id) {
        LiveSessionVideo video = liveSessionVideoMapper.selectById(id);
        if (video == null || !Objects.equals(video.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Live session video does not exist");
        }
        return video;
    }

    private LiveSessionResponse toResponse(LiveSession live, boolean includeVideos) {
        return new LiveSessionResponse(live.getId(), live.getTitle(), live.getCoverUrl(), live.getAnchorName(),
                live.getStreamName(), resolvedSpeakerName(live), resourceTagService.loadTagNames(RESOURCE_TYPE_LIVE, live.getId()),
                live.getLiveUrl(), live.getPlaybackUrl(), live.getStartAt(), live.getEndAt(),
                live.getReviewStatus(), live.getLiveStatus(), includeVideos ? loadLiveVideos(live.getId()) : List.of());
    }

    private List<LiveSessionVideoResponse> loadLiveVideos(Long liveSessionId) {
        return liveSessionVideoMapper.selectList(new LambdaQueryWrapper<LiveSessionVideo>()
                        .eq(LiveSessionVideo::getDeleted, 0)
                        .eq(LiveSessionVideo::getLiveSessionId, liveSessionId)
                        .orderByAsc(LiveSessionVideo::getSortOrder)
                        .orderByDesc(LiveSessionVideo::getCreatedAt))
                .stream()
                .map(this::toVideoResponse)
                .toList();
    }

    private LiveSessionVideoResponse toVideoResponse(LiveSessionVideo video) {
        return new LiveSessionVideoResponse(video.getId(), video.getLiveSessionId(), video.getTitle(), video.getVideoUrl(),
                video.getDurationSeconds(), video.getSortOrder(), video.getStatus());
    }

    private String resolvedSpeakerName(LiveSession live) {
        return hasText(live.getSpeakerName()) ? live.getSpeakerName() : live.getAnchorName();
    }

    private void saveAudit(Long targetId, Integer before, Integer after, String comment) {
        AuditRecord record = new AuditRecord();
        record.setTargetType("live_session");
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
