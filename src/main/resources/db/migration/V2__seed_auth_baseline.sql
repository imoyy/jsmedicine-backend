INSERT INTO sys_roles (
    role_code, role_name, description, status, sort_order, created_by, updated_by, deleted
)
SELECT 'SUPER_ADMIN', '超级管理员', '系统超级管理员基础角色', 1, 0, 0, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_roles WHERE role_code = 'SUPER_ADMIN' AND deleted = 0
);

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, 'auth:login', '管理员登录', 3, NULL, 'POST', '/api/v1/auth/login', NULL, 0, 1, 0, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permissions WHERE permission_code = 'auth:login' AND deleted = 0
);

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, 'auth:logout', '退出登录', 3, NULL, 'POST', '/api/v1/auth/logout', NULL, 1, 1, 0, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permissions WHERE permission_code = 'auth:logout' AND deleted = 0
);

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, 'auth:me', '获取当前管理员信息', 3, NULL, 'GET', '/api/v1/auth/me', NULL, 2, 1, 0, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permissions WHERE permission_code = 'auth:me' AND deleted = 0
);

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, 'auth:status', '校验登录状态', 3, NULL, 'GET', '/api/v1/auth/status', NULL, 3, 1, 0, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM sys_permissions WHERE permission_code = 'auth:status' AND deleted = 0
);

INSERT INTO sys_role_permissions (role_id, permission_id)
SELECT role_table.id, permission_table.id
FROM sys_roles role_table
JOIN sys_permissions permission_table
    ON permission_table.permission_code IN ('auth:login', 'auth:logout', 'auth:me', 'auth:status')
WHERE role_table.role_code = 'SUPER_ADMIN'
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_permissions relation_table
      WHERE relation_table.role_id = role_table.id
        AND relation_table.permission_id = permission_table.id
  );
