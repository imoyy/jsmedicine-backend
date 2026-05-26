INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'expert:category:view' AS permission_code, '查看专家分类' AS permission_name, 'GET' AS api_method, '/api/v1/admin/experts/categories' AS api_path, 300 AS sort_order
    UNION ALL SELECT 'expert:category:edit', '编辑专家分类', 'POST', '/api/v1/admin/experts/categories', 301
    UNION ALL SELECT 'expert:view', '查看专家', 'GET', '/api/v1/admin/experts', 310
    UNION ALL SELECT 'expert:edit', '编辑专家', 'POST', '/api/v1/admin/experts', 311
    UNION ALL SELECT 'interaction:qa:view', '查看答疑', 'GET', '/api/v1/admin/interaction/qa/questions', 320
    UNION ALL SELECT 'interaction:qa:reply', '回复答疑', 'POST', '/api/v1/admin/interaction/qa/questions/{id}/answers', 321
    UNION ALL SELECT 'interaction:qa:edit', '编辑答疑', 'DELETE', '/api/v1/admin/interaction/qa/questions/{id}', 322
    UNION ALL SELECT 'interaction:feedback:view', '查看反馈', 'GET', '/api/v1/admin/interaction/feedbacks', 330
    UNION ALL SELECT 'interaction:feedback:process', '处理反馈', 'PATCH', '/api/v1/admin/interaction/feedbacks/{id}/process', 331
    UNION ALL SELECT 'interaction:feedback:edit', '编辑反馈', 'DELETE', '/api/v1/admin/interaction/feedbacks/{id}', 332
    UNION ALL SELECT 'live:view', '查看直播', 'GET', '/api/v1/admin/live-sessions', 340
    UNION ALL SELECT 'live:edit', '编辑直播', 'POST', '/api/v1/admin/live-sessions', 341
    UNION ALL SELECT 'live:review', '审核直播', 'PATCH', '/api/v1/admin/live-sessions/{id}/review', 342
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
        'expert:category:view', 'expert:category:edit', 'expert:view', 'expert:edit',
        'interaction:qa:view', 'interaction:qa:reply', 'interaction:qa:edit',
        'interaction:feedback:view', 'interaction:feedback:process', 'interaction:feedback:edit',
        'live:view', 'live:edit', 'live:review'
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
