package com.gugugaga.jsmedicine.module.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.common.response.PageResponse;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.module.system.dto.AuditRecordPageQuery;
import com.gugugaga.jsmedicine.module.system.dto.AuditRecordResponse;
import com.gugugaga.jsmedicine.module.system.dto.SysAdminPageQuery;
import com.gugugaga.jsmedicine.module.system.dto.SysAdminResponse;
import com.gugugaga.jsmedicine.module.system.dto.SysAdminUpsertRequest;
import com.gugugaga.jsmedicine.module.system.dto.SysPermissionResponse;
import com.gugugaga.jsmedicine.module.system.dto.SysRolePageQuery;
import com.gugugaga.jsmedicine.module.system.dto.SysRoleResponse;
import com.gugugaga.jsmedicine.module.system.dto.SysRoleUpsertRequest;
import com.gugugaga.jsmedicine.module.system.entity.AuditRecord;
import com.gugugaga.jsmedicine.module.system.entity.SysAdmin;
import com.gugugaga.jsmedicine.module.system.entity.SysAdminRole;
import com.gugugaga.jsmedicine.module.system.entity.SysPermission;
import com.gugugaga.jsmedicine.module.system.entity.SysRole;
import com.gugugaga.jsmedicine.module.system.entity.SysRolePermission;
import com.gugugaga.jsmedicine.module.system.mapper.AuditRecordMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysAdminMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysAdminRoleMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysPermissionMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysRoleMapper;
import com.gugugaga.jsmedicine.module.system.mapper.SysRolePermissionMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class SystemAdminService {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_SIZE = 20L;
    private static final long MAX_SIZE = 100L;

    private final SysAdminMapper sysAdminMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysAdminRoleMapper sysAdminRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final PasswordEncoder passwordEncoder;
    private final CurrentAdminAccessor currentAdminAccessor;

    public SystemAdminService(
            SysAdminMapper sysAdminMapper,
            SysRoleMapper sysRoleMapper,
            SysPermissionMapper sysPermissionMapper,
            SysAdminRoleMapper sysAdminRoleMapper,
            SysRolePermissionMapper sysRolePermissionMapper,
            AuditRecordMapper auditRecordMapper,
            PasswordEncoder passwordEncoder,
            CurrentAdminAccessor currentAdminAccessor
    ) {
        this.sysAdminMapper = sysAdminMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysPermissionMapper = sysPermissionMapper;
        this.sysAdminRoleMapper = sysAdminRoleMapper;
        this.sysRolePermissionMapper = sysRolePermissionMapper;
        this.auditRecordMapper = auditRecordMapper;
        this.passwordEncoder = passwordEncoder;
        this.currentAdminAccessor = currentAdminAccessor;
    }

    public PageResponse<SysAdminResponse> pageAdmins(SysAdminPageQuery query) {
        Page<SysAdmin> page = sysAdminMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<SysAdmin>()
                        .eq(SysAdmin::getDeleted, 0)
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(SysAdmin::getUsername, query.keyword())
                                .or()
                                .like(SysAdmin::getRealName, query.keyword())
                                .or()
                                .like(SysAdmin::getMobile, query.keyword()))
                        .orderByDesc(!"createdAtAsc".equals(query.sort()), SysAdmin::getCreatedAt)
                        .orderByAsc("createdAtAsc".equals(query.sort()), SysAdmin::getCreatedAt));
        Map<Long, List<String>> roleMap = loadAdminRoleNames(page.getRecords().stream().map(SysAdmin::getId).toList());
        return new PageResponse<>(
                page.getRecords().stream().map(admin -> toAdminResponse(admin, roleMap.getOrDefault(admin.getId(), List.of()))).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    public SysAdminResponse getAdmin(Long id) {
        SysAdmin admin = requireAdmin(id);
        return toAdminResponse(admin, loadAdminRoleNames(List.of(id)).getOrDefault(id, List.of()));
    }

    @Transactional(rollbackFor = Exception.class)
    public SysAdminResponse createAdmin(SysAdminUpsertRequest request) {
        if (!hasText(request.password())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "password must not be blank");
        }
        ensureUsernameAvailable(request.username(), null);
        SysAdmin admin = new SysAdmin();
        fillAdmin(admin, request);
        admin.setPasswordHash(passwordEncoder.encode(request.password()));
        admin.setDeleted(0);
        sysAdminMapper.insert(admin);
        return getAdmin(admin.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public SysAdminResponse updateAdmin(Long id, SysAdminUpsertRequest request) {
        SysAdmin admin = requireAdmin(id);
        ensureUsernameAvailable(request.username(), id);
        fillAdmin(admin, request);
        if (hasText(request.password())) {
            admin.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        sysAdminMapper.updateById(admin);
        return getAdmin(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAdminStatus(Long id, EnabledStatus status) {
        requireAdmin(id);
        sysAdminMapper.update(null, new LambdaUpdateWrapper<SysAdmin>()
                .eq(SysAdmin::getId, id)
                .set(SysAdmin::getStatus, status));
    }

    @Transactional(rollbackFor = Exception.class)
    public void resetAdminPassword(Long id, String password) {
        requireAdmin(id);
        sysAdminMapper.update(null, new LambdaUpdateWrapper<SysAdmin>()
                .eq(SysAdmin::getId, id)
                .set(SysAdmin::getPasswordHash, passwordEncoder.encode(password)));
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindAdminRoles(Long adminId, List<Long> roleIds) {
        requireAdmin(adminId);
        validateRoles(roleIds);
        sysAdminRoleMapper.delete(new LambdaQueryWrapper<SysAdminRole>().eq(SysAdminRole::getAdminId, adminId));
        distinctIds(roleIds).forEach(roleId -> {
            SysAdminRole relation = new SysAdminRole();
            relation.setAdminId(adminId);
            relation.setRoleId(roleId);
            sysAdminRoleMapper.insert(relation);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAdmin(Long id) {
        requireAdmin(id);
        Long currentAdminId = currentAdminAccessor.getCurrentAdminId().orElse(null);
        if (Objects.equals(currentAdminId, id)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Current admin cannot delete itself");
        }
        sysAdminRoleMapper.delete(new LambdaQueryWrapper<SysAdminRole>().eq(SysAdminRole::getAdminId, id));
        sysAdminMapper.deleteById(id);
    }

    public PageResponse<SysRoleResponse> pageRoles(SysRolePageQuery query) {
        Page<SysRole> page = sysRoleMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getDeleted, 0)
                        .and(hasText(query.keyword()), wrapper -> wrapper
                                .like(SysRole::getRoleCode, query.keyword())
                                .or()
                                .like(SysRole::getRoleName, query.keyword()))
                        .orderByAsc(SysRole::getSortOrder)
                        .orderByDesc(SysRole::getCreatedAt));
        Map<Long, Long> permissionCounts = loadRolePermissionCounts(page.getRecords().stream().map(SysRole::getId).toList());
        return new PageResponse<>(
                page.getRecords().stream().map(role -> toRoleResponse(role, permissionCounts.getOrDefault(role.getId(), 0L))).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    public SysRoleResponse getRole(Long id) {
        SysRole role = requireRole(id);
        long count = sysRolePermissionMapper.selectCount(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, id));
        return toRoleResponse(role, count);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysRoleResponse createRole(SysRoleUpsertRequest request) {
        ensureRoleCodeAvailable(request.roleCode(), null);
        SysRole role = new SysRole();
        fillRole(role, request);
        role.setDeleted(0);
        sysRoleMapper.insert(role);
        return getRole(role.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public SysRoleResponse updateRole(Long id, SysRoleUpsertRequest request) {
        SysRole role = requireRole(id);
        ensureRoleCodeAvailable(request.roleCode(), id);
        fillRole(role, request);
        sysRoleMapper.updateById(role);
        return getRole(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRoleStatus(Long id, EnabledStatus status) {
        requireRole(id);
        sysRoleMapper.update(null, new LambdaUpdateWrapper<SysRole>()
                .eq(SysRole::getId, id)
                .set(SysRole::getStatus, status));
    }

    @Transactional(rollbackFor = Exception.class)
    public void bindRolePermissions(Long roleId, List<Long> permissionIds) {
        requireRole(roleId);
        validatePermissions(permissionIds);
        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        distinctIds(permissionIds).forEach(permissionId -> {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            sysRolePermissionMapper.insert(relation);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = requireRole(id);
        if ("SUPER_ADMIN".equals(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.CONFLICT, "SUPER_ADMIN role cannot be deleted");
        }
        long relationCount = sysAdminRoleMapper.selectCount(new LambdaQueryWrapper<SysAdminRole>()
                .eq(SysAdminRole::getRoleId, id));
        if (relationCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "Role is still assigned to admins");
        }
        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        sysRoleMapper.deleteById(id);
    }

    public List<SysPermissionResponse> listPermissions() {
        return sysPermissionMapper.selectList(new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getDeleted, 0)
                        .orderByAsc(SysPermission::getSortOrder)
                        .orderByAsc(SysPermission::getId))
                .stream()
                .map(this::toPermissionResponse)
                .toList();
    }

    public PageResponse<AuditRecordResponse> pageAuditRecords(AuditRecordPageQuery query) {
        Page<AuditRecord> page = auditRecordMapper.selectPage(new Page<>(normalizePage(query.page()), normalizeSize(query.size())),
                new LambdaQueryWrapper<AuditRecord>()
                        .eq(hasText(query.targetType()), AuditRecord::getTargetType, query.targetType())
                        .eq(query.targetId() != null, AuditRecord::getTargetId, query.targetId())
                        .eq(query.auditorId() != null, AuditRecord::getAuditorId, query.auditorId())
                        .orderByAsc("auditedAtAsc".equals(query.sort()), AuditRecord::getAuditedAt)
                        .orderByDesc(!"auditedAtAsc".equals(query.sort()), AuditRecord::getAuditedAt));
        return new PageResponse<>(
                page.getRecords().stream().map(this::toAuditRecordResponse).toList(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
        );
    }

    private SysAdmin requireAdmin(Long id) {
        SysAdmin admin = sysAdminMapper.selectById(id);
        if (admin == null || !Objects.equals(admin.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Admin does not exist");
        }
        return admin;
    }

    private SysRole requireRole(Long id) {
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null || !Objects.equals(role.getDeleted(), 0)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Role does not exist");
        }
        return role;
    }

    private void ensureUsernameAvailable(String username, Long ignoredId) {
        SysAdmin existing = sysAdminMapper.selectOne(new LambdaQueryWrapper<SysAdmin>()
                .eq(SysAdmin::getUsername, username)
                .eq(SysAdmin::getDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), ignoredId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Admin username already exists");
        }
    }

    private void ensureRoleCodeAvailable(String roleCode, Long ignoredId) {
        SysRole existing = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getDeleted, 0)
                .last("LIMIT 1"));
        if (existing != null && !Objects.equals(existing.getId(), ignoredId)) {
            throw new BusinessException(ErrorCode.CONFLICT, "Role code already exists");
        }
    }

    private void validateRoles(List<Long> roleIds) {
        Set<Long> ids = distinctIds(roleIds);
        if (ids.isEmpty()) {
            return;
        }
        long count = sysRoleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, ids)
                .eq(SysRole::getDeleted, 0));
        if (count != ids.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Role ids contain invalid item");
        }
    }

    private void validatePermissions(List<Long> permissionIds) {
        Set<Long> ids = distinctIds(permissionIds);
        if (ids.isEmpty()) {
            return;
        }
        long count = sysPermissionMapper.selectCount(new LambdaQueryWrapper<SysPermission>()
                .in(SysPermission::getId, ids)
                .eq(SysPermission::getDeleted, 0));
        if (count != ids.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Permission ids contain invalid item");
        }
    }

    private void fillAdmin(SysAdmin admin, SysAdminUpsertRequest request) {
        admin.setUsername(request.username());
        admin.setRealName(request.realName());
        admin.setMobile(request.mobile());
        admin.setEmail(request.email());
        admin.setAvatarUrl(request.avatarUrl());
        admin.setStatus(request.status());
    }

    private void fillRole(SysRole role, SysRoleUpsertRequest request) {
        role.setRoleCode(request.roleCode());
        role.setRoleName(request.roleName());
        role.setDescription(request.description());
        role.setStatus(request.status());
        role.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }

    private Map<Long, List<String>> loadAdminRoleNames(List<Long> adminIds) {
        if (adminIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<SysAdminRole> relations = sysAdminRoleMapper.selectList(new LambdaQueryWrapper<SysAdminRole>()
                .in(SysAdminRole::getAdminId, adminIds));
        List<Long> roleIds = relations.stream().map(SysAdminRole::getRoleId).distinct().toList();
        if (roleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> roleNames = new HashMap<>();
        sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                        .eq(SysRole::getDeleted, 0))
                .forEach(role -> roleNames.put(role.getId(), role.getRoleName()));
        Map<Long, List<String>> result = new HashMap<>();
        adminIds.forEach(adminId -> {
            List<String> names = relations.stream()
                    .filter(relation -> Objects.equals(relation.getAdminId(), adminId))
                    .map(relation -> roleNames.get(relation.getRoleId()))
                    .filter(Objects::nonNull)
                    .toList();
            result.put(adminId, names);
        });
        return result;
    }

    private Map<Long, Long> loadRolePermissionCounts(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, Long> result = new HashMap<>();
        roleIds.forEach(roleId -> result.put(roleId, 0L));
        sysRolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                        .in(SysRolePermission::getRoleId, roleIds))
                .forEach(relation -> result.computeIfPresent(relation.getRoleId(), (id, count) -> count + 1));
        return result;
    }

    private SysAdminResponse toAdminResponse(SysAdmin admin, List<String> roles) {
        return new SysAdminResponse(
                admin.getId(),
                admin.getUsername(),
                admin.getRealName(),
                admin.getMobile(),
                admin.getEmail(),
                admin.getAvatarUrl(),
                admin.getStatus(),
                admin.getLastLoginAt(),
                admin.getLastLoginIp(),
                roles
        );
    }

    private SysRoleResponse toRoleResponse(SysRole role, long permissionCount) {
        return new SysRoleResponse(
                role.getId(),
                role.getRoleCode(),
                role.getRoleName(),
                role.getDescription(),
                role.getStatus(),
                role.getSortOrder(),
                permissionCount
        );
    }

    private SysPermissionResponse toPermissionResponse(SysPermission permission) {
        return new SysPermissionResponse(
                permission.getId(),
                permission.getParentId(),
                permission.getPermissionCode(),
                permission.getPermissionName(),
                permission.getPermissionType(),
                permission.getRoutePath(),
                permission.getApiMethod(),
                permission.getApiPath(),
                permission.getIcon(),
                permission.getSortOrder(),
                permission.getStatus()
        );
    }

    private AuditRecordResponse toAuditRecordResponse(AuditRecord auditRecord) {
        return new AuditRecordResponse(
                auditRecord.getId(),
                auditRecord.getTargetType(),
                auditRecord.getTargetId(),
                auditRecord.getBeforeStatus(),
                auditRecord.getAfterStatus(),
                auditRecord.getAuditComment(),
                auditRecord.getAuditorId(),
                auditRecord.getAuditedAt(),
                auditRecord.getCreatedAt()
        );
    }

    private Set<Long> distinctIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        Set<Long> result = new HashSet<>();
        ids.stream().filter(Objects::nonNull).forEach(result::add);
        return result;
    }

    private long normalizePage(long page) {
        return page < 1 ? DEFAULT_PAGE : page;
    }

    private long normalizeSize(long size) {
        if (size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
