CREATE TABLE student_score_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    student_id BIGINT NOT NULL COMMENT 'Student ID',
    theory_training_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT 'Theory training status: pass, fail, none',
    clinical_practice_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT 'Clinical practice status: pass, fail, none',
    practical_assessment_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT 'Practical assessment status: pass, fail, none',
    theory_assessment_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT 'Theory assessment status: pass, fail, none',
    online_training_status VARCHAR(16) NOT NULL DEFAULT 'none' COMMENT 'Online training status: pass, fail, none',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_student_score_records_student (student_id),
    KEY idx_student_score_records_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student score records';

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'statistics:score:edit' AS permission_code, '更新学员成绩状态' AS permission_name, 'PATCH' AS api_method,
           '/api/v1/admin/statistics/student-scores/*' AS api_path, 501 AS sort_order
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
    ON permission_table.permission_code IN ('statistics:score:edit')
WHERE role_table.role_code = 'SUPER_ADMIN'
  AND role_table.deleted = 0
  AND permission_table.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_permissions relation_table
      WHERE relation_table.role_id = role_table.id
        AND relation_table.permission_id = permission_table.id
  );

ALTER TABLE students
    ADD KEY idx_students_stats_region_district (province, city, district, deleted);
