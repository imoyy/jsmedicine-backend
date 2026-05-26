INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'statistics:view' AS permission_code, '查看统计数据' AS permission_name, 'GET' AS api_method, '/api/v1/admin/statistics/**' AS api_path, 500 AS sort_order
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
    ON permission_table.permission_code IN ('statistics:view')
WHERE role_table.role_code = 'SUPER_ADMIN'
  AND role_table.deleted = 0
  AND permission_table.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_permissions relation_table
      WHERE relation_table.role_id = role_table.id
        AND relation_table.permission_id = permission_table.id
  );

ALTER TABLE learning_records
    ADD KEY idx_learning_records_stats_time (last_studied_at, resource_type, resource_id, student_id);

ALTER TABLE exam_records
    ADD KEY idx_exam_records_stats_time (submitted_at, source_type, source_id, student_id);

ALTER TABLE students
    ADD KEY idx_students_stats_created_region (created_at, province, city, certification_status, status, deleted);

ALTER TABLE user_browse_histories
    ADD KEY idx_user_browse_histories_stats (viewed_at, resource_type, resource_id, user_id);

ALTER TABLE user_favorites
    ADD KEY idx_user_favorites_stats (created_at, resource_type, resource_id, user_id);

ALTER TABLE user_share_records
    ADD KEY idx_user_share_records_stats (created_at, resource_type, resource_id, user_id);
