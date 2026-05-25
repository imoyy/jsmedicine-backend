package com.gugugaga.jsmedicine.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("resource_tags")
@EqualsAndHashCode(callSuper = true)
public class ResourceTag extends BaseEntity {
    private Long tagId;
    private String resourceType;
    private Long resourceId;
}
