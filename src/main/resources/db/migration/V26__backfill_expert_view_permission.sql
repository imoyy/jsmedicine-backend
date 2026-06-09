UPDATE sys_permissions
SET permission_name = '查看专家',
    api_method = 'GET',
    api_path = '/api/v1/admin/experts',
    sort_order = 310,
    status = 1,
    deleted = 0,
    updated_by = 0
WHERE permission_code = 'expert:view';

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, 'expert:view', '查看专家', 3, NULL, 'GET', '/api/v1/admin/experts', NULL, 310, 1, 0, 0, 0
WHERE NOT EXISTS (
    SELECT 1
    FROM sys_permissions
    WHERE permission_code = 'expert:view'
      AND deleted = 0
);

INSERT INTO sys_role_permissions (role_id, permission_id)
SELECT role_table.id, permission_table.id
FROM sys_roles role_table
JOIN sys_permissions permission_table
    ON permission_table.permission_code = 'expert:view'
WHERE role_table.role_code = 'SUPER_ADMIN'
  AND role_table.deleted = 0
  AND permission_table.deleted = 0
  AND permission_table.status = 1
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_permissions relation_table
      WHERE relation_table.role_id = role_table.id
        AND relation_table.permission_id = permission_table.id
  );
