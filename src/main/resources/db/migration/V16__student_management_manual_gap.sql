ALTER TABLE students
    ADD COLUMN gender TINYINT NOT NULL DEFAULT 0 COMMENT '0 unknown, 1 male, 2 female' AFTER real_name,
    ADD COLUMN age INT NULL COMMENT 'Student age' AFTER gender,
    ADD COLUMN education_level VARCHAR(64) NULL COMMENT 'Education level' AFTER age;

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'sys:student:create' AS permission_code, '新增学员' AS permission_name, 'POST' AS api_method, '/api/v1/admin/students' AS api_path, 43 AS sort_order
    UNION ALL SELECT 'sys:student:delete', '删除学员', 'DELETE', '/api/v1/admin/students/{id}', 44
    UNION ALL SELECT 'sys:student:batch-delete', '批量删除学员', 'POST', '/api/v1/admin/students/batch-delete', 45
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
    ON permission_table.permission_code IN ('sys:student:create', 'sys:student:delete', 'sys:student:batch-delete')
WHERE role_table.role_code = 'SUPER_ADMIN'
  AND role_table.deleted = 0
  AND permission_table.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_permissions relation_table
      WHERE relation_table.role_id = role_table.id
        AND relation_table.permission_id = permission_table.id
  );
