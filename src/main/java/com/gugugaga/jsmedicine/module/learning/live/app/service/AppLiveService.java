package com.gugugaga.jsmedicine.module.learning.live.app.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionResponse;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSession;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class AppLiveService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final LiveSessionMapper liveSessionMapper;

    public AppLiveService(LiveSessionMapper liveSessionMapper) {
        this.liveSessionMapper = liveSessionMapper;
    }

    public PageResponse<LiveSessionResponse> pageLives(long page, long size, String keyword, LiveStatus liveStatus) {
        Page<LiveSession> livePage = liveSessionMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size)),
                visibleWrapper()
                        .eq(liveStatus != null, LiveSession::getLiveStatus, liveStatus)
                        .and(hasText(keyword), wrapper -> wrapper.like(LiveSession::getTitle, keyword).or().like(LiveSession::getAnchorName, keyword))
                        .orderByDesc(LiveSession::getStartAt));
        return pageResponse(livePage, livePage.getRecords().stream().map(this::toResponse).toList());
    }

    public LiveSessionResponse liveDetail(Long id) {
        LiveSession live = liveSessionMapper.selectById(id);
        if (live == null
                || !Objects.equals(live.getDeleted(), 0)
                || live.getReviewStatus() != ReviewStatus.APPROVED
                || live.getLiveStatus() == LiveStatus.CANCELED) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Live session does not exist");
        }
        return toResponse(live);
    }

    private LambdaQueryWrapper<LiveSession> visibleWrapper() {
        return new LambdaQueryWrapper<LiveSession>()
                .eq(LiveSession::getDeleted, 0)
                .eq(LiveSession::getReviewStatus, ReviewStatus.APPROVED)
                .ne(LiveSession::getLiveStatus, LiveStatus.CANCELED);
    }

    private LiveSessionResponse toResponse(LiveSession live) {
        return new LiveSessionResponse(live.getId(), live.getTitle(), live.getCoverUrl(), live.getAnchorName(),
                live.getLiveUrl(), live.getPlaybackUrl(), live.getStartAt(), live.getEndAt(),
                live.getReviewStatus(), live.getLiveStatus());
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
