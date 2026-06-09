CREATE TABLE exam_assessments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    assessment_name VARCHAR(255) NOT NULL COMMENT 'Assessment name',
    paper_id BIGINT NOT NULL COMMENT 'Exam paper ID',
    assessment_type VARCHAR(16) NOT NULL COMMENT 'Assessment type: formal, makeup, mock',
    status VARCHAR(16) NOT NULL DEFAULT 'not_started' COMMENT 'Manual assessment status: not_started, cancelled, archived',
    start_at DATETIME NOT NULL COMMENT 'Assessment start time',
    end_at DATETIME NOT NULL COMMENT 'Assessment end time',
    province_code VARCHAR(32) NULL COMMENT 'Province code',
    city_code VARCHAR(32) NULL COMMENT 'City code',
    district_code VARCHAR(32) NULL COMMENT 'District code',
    expected_student_count BIGINT NOT NULL DEFAULT 0 COMMENT 'Expected participant count',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_exam_assessments_paper (paper_id, deleted),
    KEY idx_exam_assessments_status_time (status, start_at, end_at, deleted),
    KEY idx_exam_assessments_region (province_code, city_code, district_code, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Exam assessments';

CREATE TABLE exam_assessment_organizations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    assessment_id BIGINT NOT NULL COMMENT 'Assessment ID',
    organization_id BIGINT NOT NULL COMMENT 'Organization ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_exam_assessment_org (assessment_id, organization_id),
    KEY idx_exam_assessment_org_assessment (assessment_id),
    KEY idx_exam_assessment_org_organization (organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Exam assessment organizations';

CREATE TABLE exam_assessment_students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    assessment_id BIGINT NOT NULL COMMENT 'Assessment ID',
    student_id BIGINT NOT NULL COMMENT 'Student ID',
    assign_source VARCHAR(16) NOT NULL COMMENT 'Assignment source: filter, explicit',
    student_name_snapshot VARCHAR(128) NULL COMMENT 'Student name snapshot',
    mobile_snapshot VARCHAR(32) NULL COMMENT 'Mobile snapshot',
    masked_id_card_no_snapshot VARCHAR(32) NULL COMMENT 'Masked id card snapshot',
    province_code_snapshot VARCHAR(32) NULL COMMENT 'Province code snapshot',
    province_name_snapshot VARCHAR(64) NULL COMMENT 'Province name snapshot',
    city_code_snapshot VARCHAR(32) NULL COMMENT 'City code snapshot',
    city_name_snapshot VARCHAR(64) NULL COMMENT 'City name snapshot',
    district_code_snapshot VARCHAR(32) NULL COMMENT 'District code snapshot',
    district_name_snapshot VARCHAR(64) NULL COMMENT 'District name snapshot',
    organization_id_snapshot BIGINT NULL COMMENT 'Organization ID snapshot',
    organization_name_snapshot VARCHAR(128) NULL COMMENT 'Organization name snapshot',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_exam_assessment_student (assessment_id, student_id),
    KEY idx_exam_assessment_students_assessment (assessment_id),
    KEY idx_exam_assessment_students_region (province_code_snapshot, city_code_snapshot, district_code_snapshot),
    KEY idx_exam_assessment_students_organization (organization_id_snapshot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Exam assessment student snapshots';

CREATE TABLE exam_assessment_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    assessment_id BIGINT NOT NULL COMMENT 'Assessment ID',
    student_id BIGINT NOT NULL COMMENT 'Student ID',
    event_type VARCHAR(32) NOT NULL COMMENT 'Assessment event type',
    request_id VARCHAR(64) NULL COMMENT 'Idempotent request ID',
    event_time DATETIME NOT NULL COMMENT 'Event time',
    description VARCHAR(255) NULL COMMENT 'Event description',
    province_code_snapshot VARCHAR(32) NULL COMMENT 'Province code snapshot',
    province_name_snapshot VARCHAR(64) NULL COMMENT 'Province name snapshot',
    city_code_snapshot VARCHAR(32) NULL COMMENT 'City code snapshot',
    city_name_snapshot VARCHAR(64) NULL COMMENT 'City name snapshot',
    district_code_snapshot VARCHAR(32) NULL COMMENT 'District code snapshot',
    district_name_snapshot VARCHAR(64) NULL COMMENT 'District name snapshot',
    organization_id_snapshot BIGINT NULL COMMENT 'Organization ID snapshot',
    organization_name_snapshot VARCHAR(128) NULL COMMENT 'Organization name snapshot',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    KEY idx_exam_assessment_events_time (assessment_id, event_time),
    KEY idx_exam_assessment_events_student (assessment_id, student_id, event_type),
    KEY idx_exam_assessment_events_request (assessment_id, student_id, request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Exam assessment events';

ALTER TABLE exam_records
    ADD COLUMN assessment_id BIGINT NULL COMMENT 'Assessment ID' AFTER paper_id,
    ADD COLUMN status VARCHAR(24) NULL COMMENT 'Exam record status: in_progress, submitted, forced_submitted, timed_out' AFTER passed,
    ADD COLUMN submit_type VARCHAR(24) NULL COMMENT 'Submit type: normal, forced, timeout' AFTER status,
    ADD COLUMN last_active_at DATETIME NULL COMMENT 'Last active time' AFTER submitted_at,
    ADD COLUMN last_enter_request_id VARCHAR(64) NULL COMMENT 'Last enter request ID' AFTER last_active_at,
    ADD COLUMN last_submit_request_id VARCHAR(64) NULL COMMENT 'Last submit request ID' AFTER last_enter_request_id,
    ADD UNIQUE KEY uk_exam_records_assessment_student (assessment_id, student_id),
    ADD KEY idx_exam_records_assessment (assessment_id, status, submitted_at);

INSERT INTO sys_permissions (
    parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path,
    icon, sort_order, status, created_by, updated_by, deleted
)
SELECT NULL, permission_code, permission_name, 3, NULL, api_method, api_path, NULL, sort_order, 1, 0, 0, 0
FROM (
    SELECT 'learning:assessment:view' AS permission_code, '查看考核场次' AS permission_name, 'GET' AS api_method,
           '/api/v1/admin/learning/exam-assessments' AS api_path, 232 AS sort_order
    UNION ALL
    SELECT 'learning:assessment:edit', '编辑考核场次', 'POST', '/api/v1/admin/learning/exam-assessments', 233
    UNION ALL
    SELECT 'statistics:dashboard:view', '查看考核大屏统计', 'GET', '/api/v1/admin/statistics/exam-assessments/*/dashboard', 502
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
    ON permission_table.permission_code IN (
        'learning:assessment:view',
        'learning:assessment:edit',
        'statistics:dashboard:view'
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
