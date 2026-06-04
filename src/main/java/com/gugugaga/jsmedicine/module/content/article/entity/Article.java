package com.gugugaga.jsmedicine.module.content.article.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ReviewableEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("articles")
@EqualsAndHashCode(callSuper = true)
public class Article extends ReviewableEntity {
    private String title;
    private String summary;
    private String coverUrl;
    private Long coverFileAssetId;
    private String content;
    private String authorName;
    private String source;
    private Long viewCount;
}

