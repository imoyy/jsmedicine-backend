package com.gugugaga.jsmedicine.common.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("entity_extensions")
@EqualsAndHashCode(callSuper = true)
public class EntityExtension extends ManagedEntity {
    private String ownerType;
    private Long ownerId;
    private String fieldKey;
    private String fieldValue;
    private String valueType;
}
