package com.gugugaga.jsmedicine.module.learning.book.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ReviewableEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("books")
@EqualsAndHashCode(callSuper = true)
public class Book extends ReviewableEntity {
    private Long categoryId;
    private String bookName;
    private String author;
    private String publisher;
    private String coverUrl;
    private Long coverFileAssetId;
    private String introduction;
    private Integer totalPages;
    private Long paperId;
    private Integer sortOrder;
}

