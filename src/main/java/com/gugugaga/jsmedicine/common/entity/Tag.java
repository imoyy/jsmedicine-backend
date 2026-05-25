package com.gugugaga.jsmedicine.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("tags")
@EqualsAndHashCode(callSuper = true)
public class Tag extends ManagedEntity {
    private String tagName;
    private String tagType;
    private String color;
    private EnabledStatus status;
}
