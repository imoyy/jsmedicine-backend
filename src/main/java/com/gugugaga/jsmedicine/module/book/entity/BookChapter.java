package com.gugugaga.jsmedicine.module.book.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("book_chapters")
@EqualsAndHashCode(callSuper = true)
public class BookChapter extends ManagedEntity {
    private Long bookId;
    private Long parentId;
    private String chapterTitle;
    private String content;
    private Long paperId;
    private Integer sortOrder;
    private EnabledStatus status;
}
