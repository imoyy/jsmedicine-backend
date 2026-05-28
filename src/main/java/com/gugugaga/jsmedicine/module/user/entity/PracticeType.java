package com.gugugaga.jsmedicine.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("practice_types")
@EqualsAndHashCode(callSuper = true)
public class PracticeType extends ManagedEntity {
    private Long parentId;
    private String typeCode;
    private String typeName;
    private EnabledStatus status;
    private Integer sortOrder;
}
