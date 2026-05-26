INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'learning:course:view' AS permission_code, '查看课程' AS permission_name, 'GET' AS api_method, '/api/v1/admin/learning/courses' AS api_path, 200 AS sort_order
    UNION ALL SELECT 'learning:course:edit', '编辑课程', 'POST', '/api/v1/admin/learning/courses', 201
    UNION ALL SELECT 'learning:course:review', '审核课程', 'PATCH', '/api/v1/admin/learning/courses/{id}/review', 202
    UNION ALL SELECT 'learning:book:view', '查看图书', 'GET', '/api/v1/admin/learning/books', 210
    UNION ALL SELECT 'learning:book:edit', '编辑图书', 'POST', '/api/v1/admin/learning/books', 211
    UNION ALL SELECT 'learning:book:review', '审核图书', 'PATCH', '/api/v1/admin/learning/books/{id}/review', 212
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
        'learning:course:view', 'learning:course:edit', 'learning:course:review',
        'learning:book:view', 'learning:book:edit', 'learning:book:review'
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
