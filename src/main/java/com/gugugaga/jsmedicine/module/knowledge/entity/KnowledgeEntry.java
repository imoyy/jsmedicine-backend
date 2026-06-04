package com.gugugaga.jsmedicine.module.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ReviewableEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("knowledge_entries")
@EqualsAndHashCode(callSuper = true)
public class KnowledgeEntry extends ReviewableEntity {
    private Long categoryId;
    private String title;
    private String summary;
    private String coverUrl;
    private Long coverFileAssetId;
    private String content;
    private String keywords;
    private String source;
    private Integer sortOrder;
    private Long viewCount;
}
