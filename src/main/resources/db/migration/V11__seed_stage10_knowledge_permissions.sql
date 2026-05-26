INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'knowledge:category:view' AS permission_code, '查看知识库分类' AS permission_name, 'GET' AS api_method, '/api/v1/admin/knowledge/categories' AS api_path, 400 AS sort_order
    UNION ALL SELECT 'knowledge:category:edit', '编辑知识库分类', 'POST', '/api/v1/admin/knowledge/categories', 401
    UNION ALL SELECT 'knowledge:entry:view', '查看知识库条目', 'GET', '/api/v1/admin/knowledge/entries', 410
    UNION ALL SELECT 'knowledge:entry:edit', '编辑知识库条目', 'POST', '/api/v1/admin/knowledge/entries', 411
    UNION ALL SELECT 'knowledge:entry:review', '审核知识库条目', 'PATCH', '/api/v1/admin/knowledge/entries/{id}/review', 412
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
        'knowledge:category:view', 'knowledge:category:edit',
        'knowledge:entry:view', 'knowledge:entry:edit', 'knowledge:entry:review'
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
