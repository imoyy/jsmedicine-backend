package com.gugugaga.jsmedicine.module.book.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("book_categories")
@EqualsAndHashCode(callSuper = true)
public class BookCategory extends ManagedEntity {
    private Long parentId;
    private String categoryName;
    private Integer sortOrder;
    private EnabledStatus status;
}
