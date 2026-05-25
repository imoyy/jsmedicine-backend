package com.gugugaga.jsmedicine.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.PermissionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_permissions")
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends ManagedEntity {
    private Long parentId;
    private String permissionCode;
    private String permissionName;
    private PermissionType permissionType;
    private String routePath;
    private String apiMethod;
    private String apiPath;
    private String icon;
    private Integer sortOrder;
    private EnabledStatus status;
}
