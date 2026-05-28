ALTER TABLE app_users
    ADD COLUMN profile_signature VARCHAR(255) NULL COMMENT 'User profile signature' AFTER nickname;

ALTER TABLE experts
    ADD COLUMN user_id BIGINT NULL COMMENT 'Bound app user ID' AFTER id,
    ADD UNIQUE KEY uk_experts_user_id (user_id);

CREATE TABLE app_user_identities (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT NOT NULL COMMENT 'App user ID',
    identity_type VARCHAR(32) NOT NULL COMMENT 'STUDENT, EXPERT',
    identity_status TINYINT NOT NULL DEFAULT 1 COMMENT '1 active, 0 inactive',
    is_primary TINYINT NOT NULL DEFAULT 0 COMMENT '1 primary identity, 0 secondary identity',
    activated_at DATETIME NULL COMMENT 'Identity activation time',
    deactivated_at DATETIME NULL COMMENT 'Identity deactivation time',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_app_user_identities_user_type (user_id, identity_type),
    KEY idx_app_user_identities_user_primary (user_id, is_primary, deleted),
    KEY idx_app_user_identities_type_status (identity_type, identity_status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='App user business identities';

ALTER TABLE students
    ADD UNIQUE KEY uk_students_user_id (user_id);

INSERT INTO app_user_identities (
    user_id, identity_type, identity_status, is_primary, activated_at, created_by, updated_by, deleted
)
SELECT s.user_id, 'STUDENT', 1, 1, COALESCE(s.enrolled_at, s.certification_submitted_at, s.created_at), 0, 0, 0
FROM students s
WHERE s.user_id IS NOT NULL
  AND s.deleted = 0
  AND NOT EXISTS (
      SELECT 1
      FROM app_user_identities identities
      WHERE identities.user_id = s.user_id
        AND identities.identity_type = 'STUDENT'
        AND identities.deleted = 0
  );
