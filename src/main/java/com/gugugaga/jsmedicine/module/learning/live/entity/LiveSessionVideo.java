package com.gugugaga.jsmedicine.module.learning.live.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("live_session_videos")
@EqualsAndHashCode(callSuper = true)
public class LiveSessionVideo extends ManagedEntity {
    private Long liveSessionId;
    private String title;
    private String videoUrl;
    private Integer durationSeconds;
    private Integer sortOrder;
    private EnabledStatus status;
}
