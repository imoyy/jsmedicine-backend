CREATE TABLE organizations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    org_code VARCHAR(64) NULL COMMENT 'Organization code',
    org_name VARCHAR(128) NOT NULL COMMENT 'Organization name',
    org_type VARCHAR(32) NULL COMMENT 'hospital, clinic, school, enterprise',
    province_code VARCHAR(32) NULL COMMENT 'Province code',
    city_code VARCHAR(32) NULL COMMENT 'City code',
    district_code VARCHAR(32) NULL COMMENT 'District code',
    address VARCHAR(255) NULL COMMENT 'Organization address',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_organizations_code (org_code),
    KEY idx_organizations_name (org_name, deleted),
    KEY idx_organizations_region (province_code, city_code, district_code, deleted),
    KEY idx_organizations_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Organizations';

CREATE TABLE practice_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    parent_id BIGINT NULL COMMENT 'Parent practice type ID',
    type_code VARCHAR(64) NOT NULL COMMENT 'Practice type code',
    type_name VARCHAR(128) NOT NULL COMMENT 'Practice type name',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_practice_types_code (type_code),
    KEY idx_practice_types_parent (parent_id, sort_order),
    KEY idx_practice_types_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Practice types';

CREATE TABLE student_certification_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    student_id BIGINT NOT NULL COMMENT 'Student ID',
    file_asset_id BIGINT NULL COMMENT 'File asset ID',
    source_url VARCHAR(1024) NULL COMMENT 'Original source URL',
    material_type VARCHAR(32) NULL COMMENT 'id_card, qualification, other',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_student_certification_files_student (student_id, deleted, sort_order),
    KEY idx_student_certification_files_asset (file_asset_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student certification files';

ALTER TABLE students
    ADD COLUMN province_code VARCHAR(32) NULL COMMENT 'Province code' AFTER province,
    ADD COLUMN city_code VARCHAR(32) NULL COMMENT 'City code' AFTER city,
    ADD COLUMN district_code VARCHAR(32) NULL COMMENT 'District code' AFTER district,
    ADD COLUMN organization_id BIGINT NULL COMMENT 'Organization ID' AFTER organization,
    ADD COLUMN practice_type_id BIGINT NULL COMMENT 'Practice type ID' AFTER position_title,
    ADD KEY idx_students_region_code (province_code, city_code, district_code),
    ADD KEY idx_students_organization_id (organization_id),
    ADD KEY idx_students_practice_type_id (practice_type_id);

ALTER TABLE experts
    ADD COLUMN organization_id BIGINT NULL COMMENT 'Organization ID' AFTER organization,
    ADD COLUMN practice_type_id BIGINT NULL COMMENT 'Practice type ID' AFTER specialty,
    ADD KEY idx_experts_organization_id (organization_id),
    ADD KEY idx_experts_practice_type_id (practice_type_id);
