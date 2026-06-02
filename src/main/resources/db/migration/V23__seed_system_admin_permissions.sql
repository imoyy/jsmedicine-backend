UPDATE sys_permissions
SET permission_name = '查看管理员',
    api_method = 'GET',
    api_path = '/api/v1/admin/system/admins',
    sort_order = 10,
    updated_by = 0
WHERE permission_code = 'sys:admin:view'
  AND deleted = 0;

UPDATE sys_permissions
SET permission_name = '创建管理员',
    api_method = 'POST',
    api_path = '/api/v1/admin/system/admins',
    sort_order = 11,
    updated_by = 0
WHERE permission_code = 'sys:admin:create'
  AND deleted = 0;

UPDATE sys_permissions
SET permission_name = '更新管理员',
    api_method = 'PUT',
    api_path = '/api/v1/admin/system/admins/{id}',
    sort_order = 12,
    updated_by = 0
WHERE permission_code = 'sys:admin:update'
  AND deleted = 0;

UPDATE sys_permissions
SET permission_name = '禁用管理员',
    api_method = 'PATCH',
    api_path = '/api/v1/admin/system/admins/{id}/status',
    sort_order = 13,
    updated_by = 0
WHERE permission_code = 'sys:admin:disable'
  AND deleted = 0;

UPDATE sys_permissions
SET permission_name = '重置管理员密码',
    api_method = 'PATCH',
    api_path = '/api/v1/admin/system/admins/{id}/password/reset',
    sort_order = 14,
    updated_by = 0
WHERE permission_code = 'sys:admin:reset-password'
  AND deleted = 0;

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'sys:admin:view' AS permission_code, '查看管理员' AS permission_name, 'GET' AS api_method, '/api/v1/admin/system/admins' AS api_path, 10 AS sort_order
    UNION ALL
    SELECT 'sys:admin:create', '创建管理员', 'POST', '/api/v1/admin/system/admins', 11
    UNION ALL
    SELECT 'sys:admin:update', '更新管理员', 'PUT', '/api/v1/admin/system/admins/{id}', 12
    UNION ALL
    SELECT 'sys:admin:disable', '禁用管理员', 'PATCH', '/api/v1/admin/system/admins/{id}/status', 13
    UNION ALL
    SELECT 'sys:admin:reset-password', '重置管理员密码', 'PATCH', '/api/v1/admin/system/admins/{id}/password/reset', 14
) permission_seed
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_permissions
    WHERE sys_permissions.permission_code = permission_seed.permission_code
      AND sys_permissions.deleted = 0
);

INSERT INTO sys_role_permissions (role_id, permission_id)
SELECT role_table.id, permission_table.id
FROM sys_roles role_table
JOIN sys_permissions permission_table
    ON permission_table.permission_code IN (
        'sys:admin:view',
        'sys:admin:create',
        'sys:admin:update',
        'sys:admin:disable',
        'sys:admin:reset-password'
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
