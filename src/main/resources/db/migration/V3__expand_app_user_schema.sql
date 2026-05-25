ALTER TABLE app_users
    ADD COLUMN auth_provider VARCHAR(32) NOT NULL DEFAULT 'wechat_miniapp' COMMENT 'Auth provider' AFTER avatar_url,
    ADD COLUMN wechat_open_id VARCHAR(64) NULL COMMENT 'WeChat mini app open id' AFTER auth_provider,
    ADD COLUMN wechat_union_id VARCHAR(64) NULL COMMENT 'WeChat union id' AFTER wechat_open_id,
    ADD COLUMN last_login_ip VARCHAR(64) NULL COMMENT 'Last login IP' AFTER last_login_at,
    ADD COLUMN profile_completed TINYINT NOT NULL DEFAULT 0 COMMENT '1 completed, 0 incomplete' AFTER last_login_ip,
    ADD UNIQUE KEY uk_app_users_wechat_open_id (wechat_open_id),
    ADD KEY idx_app_users_wechat_union_id (wechat_union_id),
    ADD KEY idx_app_users_provider_status (auth_provider, status, deleted);

ALTER TABLE students
    ADD COLUMN certification_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 unsubmitted, 1 pending, 2 approved, 3 rejected' AFTER status,
    ADD COLUMN certification_submitted_at DATETIME NULL COMMENT 'Certification submit time' AFTER certification_status,
    ADD COLUMN certification_reviewed_at DATETIME NULL COMMENT 'Certification review time' AFTER certification_submitted_at,
    ADD COLUMN certification_reviewed_by BIGINT NULL COMMENT 'Certification reviewer admin ID' AFTER certification_reviewed_at,
    ADD COLUMN reject_reason VARCHAR(512) NULL COMMENT 'Certification reject reason' AFTER certification_reviewed_by,
    ADD COLUMN certification_materials TEXT NULL COMMENT 'Certification material URLs JSON' AFTER reject_reason,
    ADD KEY idx_students_certification (certification_status, deleted);

ALTER TABLE topics
    ADD COLUMN learning_requirements TEXT NULL COMMENT 'Topic learning requirements' AFTER summary,
    ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0 COMMENT 'View count' AFTER sort_order;

ALTER TABLE experts
    ADD COLUMN consult_enabled TINYINT NOT NULL DEFAULT 1 COMMENT '1 consult enabled, 0 disabled' AFTER status,
    ADD COLUMN consultation_notice VARCHAR(512) NULL COMMENT 'Consultation notice' AFTER consult_enabled,
    ADD KEY idx_experts_consult_enabled (consult_enabled, deleted);

ALTER TABLE qa_questions
    ADD COLUMN expert_category_id BIGINT NULL COMMENT 'Expert category ID' AFTER user_id,
    ADD COLUMN expert_id BIGINT NULL COMMENT 'Expert ID' AFTER expert_category_id,
    ADD KEY idx_qa_questions_expert (expert_id, created_at),
    ADD KEY idx_qa_questions_expert_category (expert_category_id, created_at);

ALTER TABLE qa_answers
    ADD COLUMN expert_id BIGINT NULL COMMENT 'Answer expert ID' AFTER admin_id,
    ADD KEY idx_qa_answers_expert (expert_id, answered_at);

CREATE TABLE user_favorites (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT NOT NULL COMMENT 'Frontend user ID',
    resource_type VARCHAR(32) NOT NULL COMMENT 'article, topic, course, book, podcast, live, knowledge',
    resource_id BIGINT NOT NULL COMMENT 'Resource ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_user_favorites_user_resource (user_id, resource_type, resource_id),
    KEY idx_user_favorites_resource (resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User favorites';

CREATE TABLE user_browse_histories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT NOT NULL COMMENT 'Frontend user ID',
    resource_type VARCHAR(32) NOT NULL COMMENT 'article, topic, course, book, podcast, live, knowledge',
    resource_id BIGINT NOT NULL COMMENT 'Resource ID',
    source VARCHAR(32) NULL COMMENT 'home, search, detail, topic',
    view_count INT NOT NULL DEFAULT 1 COMMENT 'View count',
    viewed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last viewed time',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    UNIQUE KEY uk_user_browse_histories_user_resource (user_id, resource_type, resource_id),
    KEY idx_user_browse_histories_viewed_at (user_id, viewed_at),
    KEY idx_user_browse_histories_resource (resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User browse histories';

CREATE TABLE user_share_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT NOT NULL COMMENT 'Frontend user ID',
    resource_type VARCHAR(32) NOT NULL COMMENT 'article, topic, course, book, podcast, live, knowledge',
    resource_id BIGINT NOT NULL COMMENT 'Resource ID',
    share_channel VARCHAR(32) NOT NULL COMMENT 'wechat_session, wechat_timeline, link',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    KEY idx_user_share_records_user (user_id, created_at),
    KEY idx_user_share_records_resource (resource_type, resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User share records';

CREATE TABLE knowledge_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    parent_id BIGINT NULL COMMENT 'Parent category ID',
    category_name VARCHAR(128) NOT NULL COMMENT 'Category name',
    category_code VARCHAR(64) NULL COMMENT 'Category code',
    description VARCHAR(255) NULL COMMENT 'Category description',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_knowledge_categories_code (category_code),
    KEY idx_knowledge_categories_parent (parent_id, sort_order),
    KEY idx_knowledge_categories_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Knowledge base categories';

CREATE TABLE knowledge_entries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    category_id BIGINT NULL COMMENT 'Knowledge category ID',
    title VARCHAR(255) NOT NULL COMMENT 'Knowledge title',
    summary VARCHAR(512) NULL COMMENT 'Knowledge summary',
    cover_url VARCHAR(512) NULL COMMENT 'Cover URL',
    content LONGTEXT NOT NULL COMMENT 'Knowledge content',
    keywords VARCHAR(255) NULL COMMENT 'Search keywords',
    source VARCHAR(255) NULL COMMENT 'Content source',
    review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 pending, 2 approved, 3 rejected',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 unpublished, 1 published',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    published_at DATETIME NULL COMMENT 'Publish time',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT 'View count',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_knowledge_entries_category (category_id, deleted),
    KEY idx_knowledge_entries_review (review_status, deleted),
    KEY idx_knowledge_entries_publish (publish_status, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Knowledge base entries';
