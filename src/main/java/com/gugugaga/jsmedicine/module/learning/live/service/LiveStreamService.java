package com.gugugaga.jsmedicine.module.learning.live.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.module.learning.live.LiveStreamingProperties;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.LiveSessionStreamResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.SrsHookResponse;
import com.gugugaga.jsmedicine.module.learning.live.admin.dto.SrsLiveHookRequest;
import com.gugugaga.jsmedicine.module.learning.live.entity.LiveSession;
import com.gugugaga.jsmedicine.module.learning.live.mapper.LiveSessionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
public class LiveStreamService {

    private static final String DEFAULT_STREAM_PREFIX = "live";
    private static final String ACTION_ON_PUBLISH = "on_publish";
    private static final String ACTION_ON_UNPUBLISH = "on_unpublish";

    private final LiveSessionMapper liveSessionMapper;
    private final LiveStreamingProperties liveStreamingProperties;

    public LiveStreamService(LiveSessionMapper liveSessionMapper, LiveStreamingProperties liveStreamingProperties) {
        this.liveSessionMapper = liveSessionMapper;
        this.liveStreamingProperties = liveStreamingProperties;
    }

    public LiveSessionStreamResponse buildStreamResponse(LiveSession liveSession) {
        String streamName = resolveStreamName(liveSession);
        return new LiveSessionStreamResponse(
                liveSession.getId(),
                streamName,
                liveStreamingProperties.buildPublishUrl(streamName),
                liveStreamingProperties.buildHttpFlvUrl(streamName),
                liveStreamingProperties.buildHlsUrl(streamName),
                liveStreamingProperties.buildCallbackUrl(),
                liveSession.getLiveUrl(),
                liveSession.getPlaybackUrl(),
                liveSession.getReviewStatus(),
                liveSession.getLiveStatus()
        );
    }

    public String resolveStreamName(LiveSession liveSession) {
        if (liveSession == null) {
            return null;
        }
        if (StringUtils.hasText(liveSession.getStreamName())) {
            return liveSession.getStreamName().trim();
        }
        return buildDefaultStreamName(liveSession.getId());
    }

    public String buildDefaultStreamName(Long liveSessionId) {
        return DEFAULT_STREAM_PREFIX + "-" + liveSessionId;
    }

    @Transactional(rollbackFor = Exception.class)
    public SrsHookResponse handleHook(SrsLiveHookRequest request, String token) {
        if (!liveStreamingProperties.isEnabled()) {
            return new SrsHookResponse(1, "Live streaming is disabled");
        }
        String expectedToken = liveStreamingProperties.getCallbackToken();
        if (StringUtils.hasText(expectedToken) && !Objects.equals(expectedToken, token)) {
            return new SrsHookResponse(1, "Invalid callback token");
        }
        String action = request == null ? null : request.action();
        if (!StringUtils.hasText(action)) {
            return new SrsHookResponse(0, "Ignored");
        }
        String streamName = request.stream();
        if (!StringUtils.hasText(streamName)) {
            return new SrsHookResponse(1, "Missing stream name");
        }
        LiveSession liveSession = liveSessionMapper.selectOne(new LambdaQueryWrapper<LiveSession>()
                .eq(LiveSession::getDeleted, 0)
                .eq(LiveSession::getStreamName, streamName.trim())
                .last("limit 1"));
        if (liveSession == null) {
            return new SrsHookResponse(1, "Live session not found");
        }
        if (ACTION_ON_PUBLISH.equals(action)) {
            if (liveSession.getReviewStatus() != ReviewStatus.APPROVED || liveSession.getLiveStatus() == LiveStatus.CANCELED) {
                return new SrsHookResponse(1, "Live session is not ready");
            }
            liveSession.setLiveStatus(LiveStatus.LIVE);
            liveSessionMapper.updateById(liveSession);
            return new SrsHookResponse(0, "Published");
        }
        if (ACTION_ON_UNPUBLISH.equals(action)) {
            if (liveSession.getLiveStatus() != LiveStatus.CANCELED) {
                liveSession.setLiveStatus(LiveStatus.ENDED);
                liveSessionMapper.updateById(liveSession);
            }
            return new SrsHookResponse(0, "Unpublished");
        }
        return new SrsHookResponse(0, "Ignored");
    }

    public void ensureUniqueStreamName(String streamName, Long currentLiveSessionId) {
        if (!StringUtils.hasText(streamName)) {
            return;
        }
        LiveSession duplicate = liveSessionMapper.selectOne(new LambdaQueryWrapper<LiveSession>()
                .eq(LiveSession::getDeleted, 0)
                .eq(LiveSession::getStreamName, streamName.trim())
                .ne(currentLiveSessionId != null, LiveSession::getId, currentLiveSessionId)
                .last("limit 1"));
        if (duplicate != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Stream name already exists");
        }
    }
}
