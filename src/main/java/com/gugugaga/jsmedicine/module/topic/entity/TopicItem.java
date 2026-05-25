package com.gugugaga.jsmedicine.module.topic.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("topic_items")
@EqualsAndHashCode(callSuper = true)
public class TopicItem extends BaseEntity {
    private Long topicId;
    private String itemType;
    private Long itemId;
    private Integer sortOrder;
}
