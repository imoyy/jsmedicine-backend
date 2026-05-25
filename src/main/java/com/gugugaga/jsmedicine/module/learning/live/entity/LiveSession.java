package com.gugugaga.jsmedicine.module.learning.live.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.LiveStatus;
import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("live_sessions")
@EqualsAndHashCode(callSuper = true)
public class LiveSession extends ManagedEntity {
    private String title;
    private String coverUrl;
    private String anchorName;
    private String liveUrl;
    private String playbackUrl;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private ReviewStatus reviewStatus;
    private LiveStatus liveStatus;
}

