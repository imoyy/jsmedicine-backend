package com.gugugaga.jsmedicine.module.content.topic.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ReviewableEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("topics")
@EqualsAndHashCode(callSuper = true)
public class Topic extends ReviewableEntity {
    private String title;
    private String summary;
    private String learningRequirements;
    private String coverUrl;
    private Long coverFileAssetId;
    private Integer sortOrder;
    private Long viewCount;
}

