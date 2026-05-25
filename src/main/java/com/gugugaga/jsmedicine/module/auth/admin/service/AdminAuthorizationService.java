package com.gugugaga.jsmedicine.module.auth.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.module.system.entity.SysAdmin;
import com.gugugaga.jsmedicine.module.system.entity.SysAdminRole;
import com.gugugaga.jsmedicine.module.system.entity.SysPermission;
import com.gugugaga.jsmedicine.module.system.entity.SysRole;
import com.gugugaga.jsmedicine.module.system.entity.SysRolePermission;
import com.gugugaga.jsmedicine.module.system.mapper.SysAdminMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysAdminRoleMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysPermissionMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysRoleMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysRolePermissionMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class AdminAuthorizationService {

    private final SysAdminMapper sysAdminMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    public AdminAuthorizationService(
            SysAdminMapper sysAdminMapper,
            SysAdminRoleMapper sysAdminRoleMapper,
            SysRoleMapper sysRoleMapper,
            SysRolePermissionMapper sysRolePermissionMapper,
            SysPermissionMapper sysPermissionMapper
    ) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.sysPermissionMapper = sysPermissionMapper;
    }

    public AdminAuthorizationInfo loadByUsername(String username) {
        SysAdmin admin = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, username)
                .eq(SysAdmin::getDeleted, 0)
                .last("LIMIT 1"));
        if (admin == null) {
            return null;
        }
        return new AdminAuthorizationInfo(admin, loadRoleCodes(admin.getId()), loadPermissionCodes(admin.getId()));
    }

    public AdminAuthorizationInfo loadByAdminId(Long adminId) {
        SysAdmin admin = sysAdminMapper.selectById(adminId);
        if (admin == null || !Objects.equals(admin.getDeleted(), 0)) {
            return null;
        }
        return new AdminAuthorizationInfo(admin, loadRoleCodes(adminId), loadPermissionCodes(adminId));
    }

    private List<String> loadRoleCodes(Long adminId) {
        List<Long> roleIds = sysAdminRoleMapper.selectList(new LambdaQueryWrapper<SysAdminRole>()
                        .eq(SysAdminRole::getAdminId, adminId))
                .stream()
                .map(SysAdminRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getDeleted, 0)
                        .eq(SysRole::getStatus, EnabledStatus.ENABLED))
                .stream()
                .map(SysRole::getRoleCode)
                .distinct()
                .toList();
    }

    private List<String> loadPermissionCodes(Long adminId) {
        List<Long> roleIds = sysAdminRoleMapper.selectList(new LambdaQueryWrapper<SysAdminRole>()
                        .eq(SysAdminRole::getAdminId, adminId))
                .stream()
                .map(SysAdminRole::getRoleId)
                .toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> permissionIds = sysRolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, roleIds))
                .stream()
                .map(SysRolePermission::getPermissionId)
                .toList();
        if (permissionIds.isEmpty()) {
            return Collections.emptyList();
        }
        return sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                        .in(SysPermission::getId, permissionIds)
                        .eq(SysPermission::getDeleted, 0)
                        .eq(SysPermission::getStatus, EnabledStatus.ENABLED))
                .stream()
                .map(SysPermission::getPermissionCode)
                .distinct()
                .toList();
    }
}

