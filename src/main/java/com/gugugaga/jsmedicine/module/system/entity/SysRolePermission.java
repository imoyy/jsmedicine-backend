package com.gugugaga.jsmedicine.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_role_permissions")
@EqualsAndHashCode(callSuper = true)
public class SysRolePermission extends BaseEntity {
    private Long roleId;
    private Long permissionId;
}
