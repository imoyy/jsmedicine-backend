INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'content:home:view' AS permission_code, '查看首页配置' AS permission_name, 'GET' AS api_method, '/api/v1/admin/content/home' AS api_path, 100 AS sort_order
    UNION ALL SELECT 'content:home:edit', '编辑首页配置', 'POST', '/api/v1/admin/content/home', 101
    UNION ALL SELECT 'content:article:view', '查看资讯', 'GET', '/api/v1/admin/content/articles', 110
    UNION ALL SELECT 'content:article:edit', '编辑资讯', 'POST', '/api/v1/admin/content/articles', 111
    UNION ALL SELECT 'content:article:review', '审核资讯', 'PATCH', '/api/v1/admin/content/articles/{id}/review', 112
    UNION ALL SELECT 'content:podcast:view', '查看播客', 'GET', '/api/v1/admin/content/podcasts', 120
    UNION ALL SELECT 'content:podcast:edit', '编辑播客', 'POST', '/api/v1/admin/content/podcasts', 121
    UNION ALL SELECT 'content:podcast:review', '审核播客', 'PATCH', '/api/v1/admin/content/podcasts/{id}/review', 122
    UNION ALL SELECT 'content:topic:view', '查看专题', 'GET', '/api/v1/admin/content/topics', 130
    UNION ALL SELECT 'content:topic:edit', '编辑专题', 'POST', '/api/v1/admin/content/topics', 131
    UNION ALL SELECT 'content:topic:review', '审核专题', 'PATCH', '/api/v1/admin/content/topics/{id}/review', 132
    UNION ALL SELECT 'content:file:view', '查看文件资源', 'GET', '/api/v1/admin/content/files', 140
    UNION ALL SELECT 'content:file:edit', '编辑文件资源', 'POST', '/api/v1/admin/content/files', 141
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
        'content:home:view', 'content:home:edit',
        'content:article:view', 'content:article:edit', 'content:article:review',
        'content:podcast:view', 'content:podcast:edit', 'content:podcast:review',
        'content:topic:view', 'content:topic:edit', 'content:topic:review',
        'content:file:view', 'content:file:edit'
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
