package com.gugugaga.jsmedicine.module.auth.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.module.system.entity.SysAdmin;
import com.gugugaga.jsmedicine.module.system.entity.SysAdminRole;
import com.gugugaga.jsmedicine.module.system.entity.SysRole;
import com.gugugaga.jsmedicine.module.system.mapper.SysAdminMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysAdminRoleMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysRoleMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthBootstrapService {

    private static final Logger log = LoggerFactory.getLogger(AuthBootstrapService.class);
    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    private final AuthProperties authProperties;
    private final SysAdminMapper sysAdminMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthBootstrapService(
            AuthProperties authProperties,
            SysAdminMapper sysAdminMapper,
            SysRoleMapper sysRoleMapper,
            SysAdminRoleMapper sysAdminRoleMapper,
            PasswordEncoder passwordEncoder
    ) {
        this.authProperties = authProperties;
        this.sysAdminMapper = sysAdminMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void bootstrapSuperAdmin() {
        SysAdmin existingAdmin = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, authProperties.getBootstrapUsername())
                .eq(SysAdmin::getDeleted, 0)
                .last("LIMIT 1"));
        if (existingAdmin != null) {
            bindSuperAdminRoleIfNecessary(existingAdmin.getId());
            return;
        }
        if (authProperties.getBootstrapPassword() == null || authProperties.getBootstrapPassword().isBlank()) {
            log.warn("Bootstrap super admin skipped because app.auth.bootstrap-password is not configured");
            return;
        }
        SysAdmin admin = new SysAdmin();
        admin.setUsername(authProperties.getBootstrapUsername());
        admin.setPasswordHash(passwordEncoder.encode(authProperties.getBootstrapPassword()));
        admin.setRealName(authProperties.getBootstrapRealName());
        admin.setStatus(EnabledStatus.ENABLED);
        sysAdminMapper.insert(admin);
        bindSuperAdminRoleIfNecessary(admin.getId());
        log.info("Bootstrap super admin initialized username={}", authProperties.getBootstrapUsername());
    }

    private void bindSuperAdminRoleIfNecessary(Long adminId) {
        SysRole superAdminRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, SUPER_ADMIN_ROLE_CODE)
                .eq(SysRole::getDeleted, 0)
                .last("LIMIT 1"));
        if (superAdminRole == null) {
            log.warn("Bootstrap super admin role binding skipped because role {} is missing", SUPER_ADMIN_ROLE_CODE);
            return;
        }
        SysAdminRole existingRelation = sysAdminRoleMapper.selectOne(new LambdaQueryWrapper<SysAdminRole>()
                .eq(SysAdminRole::getAdminId, adminId)
                .eq(SysAdminRole::getRoleId, superAdminRole.getId())
                .last("LIMIT 1"));
        if (existingRelation != null) {
            return;
        }
        SysAdminRole relation = new SysAdminRole();
        relation.setAdminId(adminId);
        relation.setRoleId(superAdminRole.getId());
        sysAdminRoleMapper.insert(relation);
    }
}

