package com.gugugaga.jsmedicine.module.podcast.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("podcast_audios")
@EqualsAndHashCode(callSuper = true)
public class PodcastAudio extends ManagedEntity {
    private Long podcastId;
    private String title;
    private String audioUrl;
    private Integer durationSeconds;
    private Integer sortOrder;
    private EnabledStatus status;
}
