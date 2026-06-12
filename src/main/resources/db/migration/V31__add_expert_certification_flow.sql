CREATE TABLE expert_certifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT NOT NULL COMMENT 'Related frontend user ID',
    real_name VARCHAR(64) NOT NULL COMMENT 'Expert applicant real name',
    gender TINYINT NOT NULL DEFAULT 0 COMMENT '0 unknown, 1 male, 2 female',
    birth_date DATE NULL COMMENT 'Birth date',
    mobile VARCHAR(32) NULL COMMENT 'Mobile number',
    title VARCHAR(128) NULL COMMENT 'Professional title',
    organization VARCHAR(128) NULL COMMENT 'Organization',
    organization_id BIGINT NULL COMMENT 'Organization ID',
    practice_type_id BIGINT NULL COMMENT 'Practice type ID',
    specialty VARCHAR(255) NULL COMMENT 'Specialty',
    introduction TEXT NULL COMMENT 'Introduction',
    consultation_notice VARCHAR(512) NULL COMMENT 'Consultation notice',
    certification_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 unsubmitted, 1 pending, 2 approved, 3 rejected',
    certification_submitted_at DATETIME NULL COMMENT 'Submission time',
    certification_reviewed_at DATETIME NULL COMMENT 'Review time',
    certification_reviewed_by BIGINT NULL COMMENT 'Reviewer admin ID',
    reject_reason VARCHAR(512) NULL COMMENT 'Reject reason',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_expert_certifications_user (user_id),
    KEY idx_expert_certifications_status (certification_status, deleted),
    KEY idx_expert_certifications_review (certification_reviewed_by, certification_reviewed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Expert certification applications';

CREATE TABLE expert_certification_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    certification_id BIGINT NOT NULL COMMENT 'Expert certification ID',
    file_asset_id BIGINT NULL COMMENT 'Uploaded file asset ID',
    source_url VARCHAR(1024) NULL COMMENT 'External source URL',
    material_type VARCHAR(64) NULL COMMENT 'Material type',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_expert_certification_files_certification (certification_id, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Expert certification files';

CREATE TABLE expert_certification_category_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    certification_id BIGINT NOT NULL COMMENT 'Expert certification ID',
    category_id BIGINT NOT NULL COMMENT 'Expert category ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_expert_certification_category (certification_id, category_id),
    KEY idx_expert_certification_category_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Expert certification category relations';

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'expert:certification:view' AS permission_code, '查看专家认证' AS permission_name, 'GET' AS api_method, '/api/v1/admin/experts/certifications' AS api_path, 312 AS sort_order
    UNION ALL
    SELECT 'expert:certification:review', '审核专家认证', 'PATCH', '/api/v1/admin/experts/certifications/{id}/review', 313
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
    ON permission_table.permission_code IN ('expert:certification:view', 'expert:certification:review')
WHERE role_table.role_code = 'SUPER_ADMIN'
  AND role_table.deleted = 0
  AND permission_table.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM sys_role_permissions relation_table
      WHERE relation_table.role_id = role_table.id
        AND relation_table.permission_id = permission_table.id
  );
