INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'sys:role:view' AS permission_code, '查看角色' AS permission_name, 'GET' AS api_method, '/api/v1/admin/system/roles' AS api_path, 20 AS sort_order
    UNION ALL SELECT 'sys:role:create', '创建角色', 'POST', '/api/v1/admin/system/roles', 21
    UNION ALL SELECT 'sys:role:update', '更新角色', 'PUT', '/api/v1/admin/system/roles/{id}', 22
    UNION ALL SELECT 'sys:role:disable', '禁用角色', 'PATCH', '/api/v1/admin/system/roles/{id}/status', 23
    UNION ALL SELECT 'sys:role:permission', '分配角色权限', 'PUT', '/api/v1/admin/system/roles/{id}/permissions', 24
    UNION ALL SELECT 'sys:permission:view', '查看权限', 'GET', '/api/v1/admin/system/permissions', 25
    UNION ALL SELECT 'sys:user:view', '查看用户', 'GET', '/api/v1/admin/users', 30
    UNION ALL SELECT 'sys:user:update', '更新用户状态', 'PATCH', '/api/v1/admin/users/{id}/status', 31
    UNION ALL SELECT 'sys:student:view', '查看学员', 'GET', '/api/v1/admin/students', 40
    UNION ALL SELECT 'sys:student:update', '维护学员信息', 'PUT', '/api/v1/admin/students/{id}', 41
    UNION ALL SELECT 'sys:student:review', '审核学员认证', 'PATCH', '/api/v1/admin/students/{id}/certification', 42
    UNION ALL SELECT 'sys:audit:view', '查看操作审计', 'GET', '/api/v1/admin/system/audit-records', 50
) permission_seed
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permissions
    WHERE sys_permissions.permission_code = permission_seed.permission_code
      AND sys_permissions.deleted = 0
);

INSERT INTO sys_role_permissions (role_id, permission_id)
SELECT role_table.id, permission_table.id
FROM sys_roles role_table
JOIN sys_permissions permission_table
    ON permission_table.permission_code IN (
        'sys:role:view', 'sys:role:create', 'sys:role:update', 'sys:role:disable', 'sys:role:permission',
        'sys:permission:view', 'sys:user:view', 'sys:user:update',
        'sys:student:view', 'sys:student:update', 'sys:student:review', 'sys:audit:view'
    )
WHERE role_table.role_code = 'SUPER_ADMIN'
  AND role_table.deleted = 0
  AND permission_table.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_permissions relation_table
      WHERE relation_table.role_id = role_table.id
        AND relation_table.permission_id = permission_table.id
  );
