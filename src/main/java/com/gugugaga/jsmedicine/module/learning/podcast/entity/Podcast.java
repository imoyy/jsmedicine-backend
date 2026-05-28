package com.gugugaga.jsmedicine.module.learning.podcast.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ReviewableEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("podcasts")
@EqualsAndHashCode(callSuper = true)
public class Podcast extends ReviewableEntity {
    private String title;
    private String summary;
    private String coverUrl;
    private String speakerName;
    private Integer sortOrder;
}

