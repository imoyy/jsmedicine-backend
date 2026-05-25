CREATE TABLE sys_admins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    username VARCHAR(64) NOT NULL COMMENT 'Login username',
    password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt password hash',
    real_name VARCHAR(64) NOT NULL COMMENT 'Administrator real name',
    mobile VARCHAR(32) NULL COMMENT 'Mobile number',
    email VARCHAR(128) NULL COMMENT 'Email address',
    avatar_url VARCHAR(512) NULL COMMENT 'Avatar URL',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    last_login_at DATETIME NULL COMMENT 'Last login time',
    last_login_ip VARCHAR(64) NULL COMMENT 'Last login IP',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_sys_admins_username (username),
    KEY idx_sys_admins_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System administrators';

CREATE TABLE sys_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    role_code VARCHAR(64) NOT NULL COMMENT 'Role code',
    role_name VARCHAR(64) NOT NULL COMMENT 'Role name',
    description VARCHAR(255) NULL COMMENT 'Role description',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_sys_roles_code (role_code),
    KEY idx_sys_roles_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System roles';

CREATE TABLE sys_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    parent_id BIGINT NULL COMMENT 'Parent permission ID',
    permission_code VARCHAR(128) NOT NULL COMMENT 'Permission code',
    permission_name VARCHAR(128) NOT NULL COMMENT 'Permission name',
    permission_type TINYINT NOT NULL COMMENT '1 menu, 2 button, 3 API',
    route_path VARCHAR(255) NULL COMMENT 'Frontend route path',
    api_method VARCHAR(16) NULL COMMENT 'HTTP method',
    api_path VARCHAR(255) NULL COMMENT 'API path',
    icon VARCHAR(64) NULL COMMENT 'Menu icon',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_sys_permissions_code (permission_code),
    KEY idx_sys_permissions_parent (parent_id, sort_order),
    KEY idx_sys_permissions_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System permissions';

CREATE TABLE sys_admin_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    admin_id BIGINT NOT NULL COMMENT 'Admin ID',
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_sys_admin_roles_admin_role (admin_id, role_id),
    KEY idx_sys_admin_roles_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Admin-role relations';

CREATE TABLE sys_role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    role_id BIGINT NOT NULL COMMENT 'Role ID',
    permission_id BIGINT NOT NULL COMMENT 'Permission ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_sys_role_permissions_role_permission (role_id, permission_id),
    KEY idx_sys_role_permissions_permission (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Role-permission relations';

CREATE TABLE audit_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    target_type VARCHAR(64) NOT NULL COMMENT 'Target business type',
    target_id BIGINT NOT NULL COMMENT 'Target business ID',
    before_status TINYINT NULL COMMENT 'Status before audit',
    after_status TINYINT NOT NULL COMMENT 'Status after audit',
    audit_comment VARCHAR(512) NULL COMMENT 'Audit comment',
    auditor_id BIGINT NOT NULL COMMENT 'Auditor admin ID',
    audited_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Audit time',
    KEY idx_audit_records_target (target_type, target_id),
    KEY idx_audit_records_auditor (auditor_id, audited_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Business audit records';

CREATE TABLE app_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    username VARCHAR(64) NULL COMMENT 'User username',
    mobile VARCHAR(32) NULL COMMENT 'Mobile number',
    email VARCHAR(128) NULL COMMENT 'Email address',
    nickname VARCHAR(64) NULL COMMENT 'Nickname',
    avatar_url VARCHAR(512) NULL COMMENT 'Avatar URL',
    gender TINYINT NOT NULL DEFAULT 0 COMMENT '0 unknown, 1 male, 2 female',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    registered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Register time',
    last_login_at DATETIME NULL COMMENT 'Last login time',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_app_users_username (username),
    UNIQUE KEY uk_app_users_mobile (mobile),
    KEY idx_app_users_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Frontend users';

CREATE TABLE students (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT NULL COMMENT 'Related frontend user ID',
    student_no VARCHAR(64) NULL COMMENT 'Student number',
    real_name VARCHAR(64) NOT NULL COMMENT 'Student real name',
    mobile VARCHAR(32) NULL COMMENT 'Mobile number',
    id_card_no VARCHAR(32) NULL COMMENT 'ID card number',
    province VARCHAR(64) NULL COMMENT 'Province',
    city VARCHAR(64) NULL COMMENT 'City',
    district VARCHAR(64) NULL COMMENT 'District',
    organization VARCHAR(128) NULL COMMENT 'Organization',
    position_title VARCHAR(128) NULL COMMENT 'Position title',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    enrolled_at DATETIME NULL COMMENT 'Enroll time',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_students_student_no (student_no),
    KEY idx_students_user (user_id),
    KEY idx_students_region (province, city, district),
    KEY idx_students_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Students';

CREATE TABLE file_assets (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    asset_type VARCHAR(32) NOT NULL COMMENT 'image, video, audio, document',
    storage_provider VARCHAR(32) NOT NULL DEFAULT 'minio' COMMENT 'Storage provider',
    bucket_name VARCHAR(128) NULL COMMENT 'Storage bucket',
    object_key VARCHAR(512) NOT NULL COMMENT 'Storage object key',
    original_name VARCHAR(255) NULL COMMENT 'Original file name',
    content_type VARCHAR(128) NULL COMMENT 'Content type',
    file_size BIGINT NULL COMMENT 'File size in bytes',
    url VARCHAR(1024) NULL COMMENT 'Public or signed URL',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_file_assets_type (asset_type, deleted),
    KEY idx_file_assets_object (bucket_name, object_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Uploaded file assets';

CREATE TABLE tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    tag_name VARCHAR(64) NOT NULL COMMENT 'Tag name',
    tag_type VARCHAR(32) NULL COMMENT 'Tag business type',
    color VARCHAR(32) NULL COMMENT 'Display color',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_tags_name_type (tag_name, tag_type),
    KEY idx_tags_type_status (tag_type, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Reusable resource tags';

CREATE TABLE resource_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    tag_id BIGINT NOT NULL COMMENT 'Tag ID',
    resource_type VARCHAR(32) NOT NULL COMMENT 'course, book, article, podcast, topic, expert, live',
    resource_id BIGINT NOT NULL COMMENT 'Resource ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_resource_tags_resource_tag (resource_type, resource_id, tag_id),
    KEY idx_resource_tags_tag (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Resource tag relations';

CREATE TABLE entity_extensions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    owner_type VARCHAR(64) NOT NULL COMMENT 'Owner business type',
    owner_id BIGINT NOT NULL COMMENT 'Owner business ID',
    field_key VARCHAR(128) NOT NULL COMMENT 'Extension field key',
    field_value TEXT NULL COMMENT 'Extension field value',
    value_type VARCHAR(32) NOT NULL DEFAULT 'string' COMMENT 'string, number, boolean, date, json',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    UNIQUE KEY uk_entity_extensions_owner_key (owner_type, owner_id, field_key),
    KEY idx_entity_extensions_key (owner_type, field_key, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Controlled extension fields for future requirements';

CREATE TABLE home_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    parent_id BIGINT NULL COMMENT 'Parent category ID',
    category_name VARCHAR(128) NOT NULL COMMENT 'Category name',
    category_code VARCHAR(64) NULL COMMENT 'Category code',
    icon_url VARCHAR(512) NULL COMMENT 'Icon URL',
    description VARCHAR(255) NULL COMMENT 'Category description',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_home_categories_parent (parent_id, sort_order),
    KEY idx_home_categories_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mini app home categories';

CREATE TABLE home_contents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    category_id BIGINT NULL COMMENT 'Home category ID',
    content_type VARCHAR(32) NOT NULL COMMENT 'banner, course, book, article, podcast, topic, live',
    target_id BIGINT NULL COMMENT 'Related business ID',
    title VARCHAR(255) NOT NULL COMMENT 'Display title',
    cover_url VARCHAR(512) NULL COMMENT 'Cover URL',
    link_url VARCHAR(512) NULL COMMENT 'External link URL',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    start_at DATETIME NULL COMMENT 'Display start time',
    end_at DATETIME NULL COMMENT 'Display end time',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_home_contents_category (category_id, sort_order),
    KEY idx_home_contents_type_target (content_type, target_id),
    KEY idx_home_contents_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mini app home content configuration';

CREATE TABLE question_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    parent_id BIGINT NULL COMMENT 'Parent category ID',
    category_name VARCHAR(128) NOT NULL COMMENT 'Category name',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_question_categories_parent (parent_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Question categories';

CREATE TABLE questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    category_id BIGINT NULL COMMENT 'Question category ID',
    question_type TINYINT NOT NULL COMMENT '1 single choice, 2 multiple choice, 3 true/false, 4 short answer',
    title TEXT NOT NULL COMMENT 'Question title',
    analysis TEXT NULL COMMENT 'Answer analysis',
    difficulty TINYINT NOT NULL DEFAULT 1 COMMENT '1 easy, 2 medium, 3 hard',
    score DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT 'Default score',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_questions_category (category_id, deleted),
    KEY idx_questions_type (question_type, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Question bank';

CREATE TABLE question_options (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    question_id BIGINT NOT NULL COMMENT 'Question ID',
    option_key VARCHAR(16) NOT NULL COMMENT 'Option key',
    option_content TEXT NOT NULL COMMENT 'Option content',
    correct TINYINT NOT NULL DEFAULT 0 COMMENT '1 correct, 0 incorrect',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_question_options_question_key (question_id, option_key),
    KEY idx_question_options_question (question_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Question options';

CREATE TABLE exam_papers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    paper_name VARCHAR(255) NOT NULL COMMENT 'Paper name',
    description VARCHAR(512) NULL COMMENT 'Paper description',
    total_score DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT 'Total score',
    pass_score DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT 'Pass score',
    duration_minutes INT NULL COMMENT 'Exam duration in minutes',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_exam_papers_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Exam papers';

CREATE TABLE exam_paper_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    paper_id BIGINT NOT NULL COMMENT 'Paper ID',
    question_id BIGINT NOT NULL COMMENT 'Question ID',
    score DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT 'Question score in paper',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_exam_paper_questions_paper_question (paper_id, question_id),
    KEY idx_exam_paper_questions_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Paper question relations';

CREATE TABLE courses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    course_name VARCHAR(255) NOT NULL COMMENT 'Course name',
    subtitle VARCHAR(255) NULL COMMENT 'Course subtitle',
    cover_url VARCHAR(512) NULL COMMENT 'Cover URL',
    lecturer_name VARCHAR(128) NULL COMMENT 'Lecturer name',
    introduction TEXT NULL COMMENT 'Course introduction',
    review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 pending, 2 approved, 3 rejected',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 unpublished, 1 published',
    paper_id BIGINT NULL COMMENT 'Related exam paper ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    published_at DATETIME NULL COMMENT 'Publish time',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_courses_review (review_status, deleted),
    KEY idx_courses_publish (publish_status, sort_order, deleted),
    KEY idx_courses_paper (paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Courses';

CREATE TABLE course_videos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    course_id BIGINT NOT NULL COMMENT 'Course ID',
    title VARCHAR(255) NOT NULL COMMENT 'Video title',
    video_url VARCHAR(1024) NOT NULL COMMENT 'Video URL',
    duration_seconds INT NULL COMMENT 'Video duration in seconds',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_course_videos_course (course_id, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Course videos';

CREATE TABLE book_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    parent_id BIGINT NULL COMMENT 'Parent category ID',
    category_name VARCHAR(128) NOT NULL COMMENT 'Category name',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_book_categories_parent (parent_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Book categories';

CREATE TABLE books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    category_id BIGINT NULL COMMENT 'Book category ID',
    book_name VARCHAR(255) NOT NULL COMMENT 'Book name',
    author VARCHAR(128) NULL COMMENT 'Author',
    publisher VARCHAR(128) NULL COMMENT 'Publisher',
    cover_url VARCHAR(512) NULL COMMENT 'Cover URL',
    introduction TEXT NULL COMMENT 'Book introduction',
    review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 pending, 2 approved, 3 rejected',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 unpublished, 1 published',
    paper_id BIGINT NULL COMMENT 'Related exam paper ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    published_at DATETIME NULL COMMENT 'Publish time',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_books_category (category_id, deleted),
    KEY idx_books_review (review_status, deleted),
    KEY idx_books_publish (publish_status, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Books';

CREATE TABLE book_chapters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    book_id BIGINT NOT NULL COMMENT 'Book ID',
    parent_id BIGINT NULL COMMENT 'Parent chapter ID',
    chapter_title VARCHAR(255) NOT NULL COMMENT 'Chapter title',
    content LONGTEXT NULL COMMENT 'Chapter content',
    paper_id BIGINT NULL COMMENT 'Related exam paper ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_book_chapters_book (book_id, parent_id, sort_order, deleted),
    KEY idx_book_chapters_paper (paper_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Book chapters';

CREATE TABLE articles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    title VARCHAR(255) NOT NULL COMMENT 'Article title',
    summary VARCHAR(512) NULL COMMENT 'Article summary',
    cover_url VARCHAR(512) NULL COMMENT 'Cover URL',
    content LONGTEXT NOT NULL COMMENT 'Rich text content',
    author_name VARCHAR(128) NULL COMMENT 'Author name',
    review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 pending, 2 approved, 3 rejected',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 unpublished, 1 published',
    published_at DATETIME NULL COMMENT 'Publish time',
    view_count BIGINT NOT NULL DEFAULT 0 COMMENT 'View count',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_articles_review (review_status, deleted),
    KEY idx_articles_publish (publish_status, published_at, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Information articles';

CREATE TABLE podcasts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    title VARCHAR(255) NOT NULL COMMENT 'Podcast title',
    summary VARCHAR(512) NULL COMMENT 'Podcast summary',
    cover_url VARCHAR(512) NULL COMMENT 'Cover URL',
    review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 pending, 2 approved, 3 rejected',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 unpublished, 1 published',
    published_at DATETIME NULL COMMENT 'Publish time',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_podcasts_review (review_status, deleted),
    KEY idx_podcasts_publish (publish_status, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Podcasts';

CREATE TABLE podcast_audios (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    podcast_id BIGINT NOT NULL COMMENT 'Podcast ID',
    title VARCHAR(255) NOT NULL COMMENT 'Audio title',
    audio_url VARCHAR(1024) NOT NULL COMMENT 'Audio URL',
    duration_seconds INT NULL COMMENT 'Audio duration in seconds',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_podcast_audios_podcast (podcast_id, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Podcast audio items';

CREATE TABLE topics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    title VARCHAR(255) NOT NULL COMMENT 'Topic title',
    summary VARCHAR(512) NULL COMMENT 'Topic summary',
    cover_url VARCHAR(512) NULL COMMENT 'Cover URL',
    review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 pending, 2 approved, 3 rejected',
    publish_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 unpublished, 1 published',
    published_at DATETIME NULL COMMENT 'Publish time',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_topics_review (review_status, deleted),
    KEY idx_topics_publish (publish_status, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Topics';

CREATE TABLE topic_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    topic_id BIGINT NOT NULL COMMENT 'Topic ID',
    item_type VARCHAR(32) NOT NULL COMMENT 'course, book, student, article, podcast, question, live',
    item_id BIGINT NOT NULL COMMENT 'Related business ID',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_topic_items_topic_type_item (topic_id, item_type, item_id),
    KEY idx_topic_items_item (item_type, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Topic content relations';

CREATE TABLE expert_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    parent_id BIGINT NULL COMMENT 'Parent category ID',
    category_name VARCHAR(128) NOT NULL COMMENT 'Category name',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_expert_categories_parent (parent_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Expert categories';

CREATE TABLE experts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    real_name VARCHAR(64) NOT NULL COMMENT 'Expert real name',
    avatar_url VARCHAR(512) NULL COMMENT 'Avatar URL',
    title VARCHAR(128) NULL COMMENT 'Professional title',
    organization VARCHAR(128) NULL COMMENT 'Organization',
    specialty VARCHAR(255) NULL COMMENT 'Specialty',
    introduction TEXT NULL COMMENT 'Expert introduction',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1 enabled, 0 disabled',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_experts_status (status, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Experts';

CREATE TABLE expert_category_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    expert_id BIGINT NOT NULL COMMENT 'Expert ID',
    category_id BIGINT NOT NULL COMMENT 'Expert category ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_expert_category_relations (expert_id, category_id),
    KEY idx_expert_category_relations_category (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Expert-category relations';

CREATE TABLE expert_experiences (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    expert_id BIGINT NOT NULL COMMENT 'Expert ID',
    experience_type VARCHAR(32) NOT NULL COMMENT 'education, work, achievement',
    title VARCHAR(255) NOT NULL COMMENT 'Experience title',
    description TEXT NULL COMMENT 'Experience description',
    start_date DATE NULL COMMENT 'Start date',
    end_date DATE NULL COMMENT 'End date',
    sort_order INT NOT NULL DEFAULT 0 COMMENT 'Display order',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_expert_experiences_expert (expert_id, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Expert experiences';

CREATE TABLE live_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    title VARCHAR(255) NOT NULL COMMENT 'Live title',
    cover_url VARCHAR(512) NULL COMMENT 'Cover URL',
    anchor_name VARCHAR(128) NULL COMMENT 'Anchor name',
    live_url VARCHAR(1024) NULL COMMENT 'Live stream URL',
    playback_url VARCHAR(1024) NULL COMMENT 'Playback URL',
    start_at DATETIME NOT NULL COMMENT 'Live start time',
    end_at DATETIME NULL COMMENT 'Live end time',
    review_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 draft, 1 pending, 2 approved, 3 rejected',
    live_status TINYINT NOT NULL DEFAULT 0 COMMENT '0 not started, 1 live, 2 ended, 3 canceled',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_live_sessions_time (start_at, end_at),
    KEY idx_live_sessions_review (review_status, deleted),
    KEY idx_live_sessions_status (live_status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Live sessions';

CREATE TABLE qa_questions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    student_id BIGINT NULL COMMENT 'Student ID',
    user_id BIGINT NULL COMMENT 'Frontend user ID',
    title VARCHAR(255) NOT NULL COMMENT 'Question title',
    content TEXT NOT NULL COMMENT 'Question content',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending, 1 answered, 2 closed',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_qa_questions_student (student_id, created_at),
    KEY idx_qa_questions_status (status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='QA questions';

CREATE TABLE qa_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    question_id BIGINT NOT NULL COMMENT 'QA question ID',
    admin_id BIGINT NULL COMMENT 'Answer admin ID',
    content TEXT NOT NULL COMMENT 'Answer content',
    answered_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Answer time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_qa_answers_question (question_id, answered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='QA answers';

CREATE TABLE feedbacks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    user_id BIGINT NULL COMMENT 'Frontend user ID',
    student_id BIGINT NULL COMMENT 'Student ID',
    feedback_type VARCHAR(32) NULL COMMENT 'Feedback type',
    content TEXT NOT NULL COMMENT 'Feedback content',
    contact VARCHAR(128) NULL COMMENT 'Contact information',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0 pending, 1 processed',
    processed_by BIGINT NULL COMMENT 'Processor admin ID',
    processed_at DATETIME NULL COMMENT 'Process time',
    process_note VARCHAR(512) NULL COMMENT 'Process note',
    created_by BIGINT NULL COMMENT 'Creator admin ID',
    updated_by BIGINT NULL COMMENT 'Updater admin ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag',
    KEY idx_feedbacks_status (status, deleted),
    KEY idx_feedbacks_user (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User feedbacks';

CREATE TABLE learning_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    student_id BIGINT NOT NULL COMMENT 'Student ID',
    resource_type VARCHAR(32) NOT NULL COMMENT 'course, course_video, book, book_chapter, podcast, live',
    resource_id BIGINT NOT NULL COMMENT 'Resource ID',
    study_seconds INT NOT NULL DEFAULT 0 COMMENT 'Study duration in seconds',
    progress_percent DECIMAL(5,2) NOT NULL DEFAULT 0 COMMENT 'Learning progress percent',
    completed TINYINT NOT NULL DEFAULT 0 COMMENT '1 completed, 0 incomplete',
    completed_at DATETIME NULL COMMENT 'Complete time',
    last_studied_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Last study time',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    UNIQUE KEY uk_learning_records_student_resource (student_id, resource_type, resource_id),
    KEY idx_learning_records_resource (resource_type, resource_id),
    KEY idx_learning_records_completed (completed, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student learning records';

CREATE TABLE exam_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    student_id BIGINT NOT NULL COMMENT 'Student ID',
    paper_id BIGINT NOT NULL COMMENT 'Paper ID',
    source_type VARCHAR(32) NULL COMMENT 'course, book, chapter, topic',
    source_id BIGINT NULL COMMENT 'Source business ID',
    score DECIMAL(8,2) NOT NULL DEFAULT 0 COMMENT 'Exam score',
    passed TINYINT NOT NULL DEFAULT 0 COMMENT '1 passed, 0 failed',
    started_at DATETIME NULL COMMENT 'Exam start time',
    submitted_at DATETIME NULL COMMENT 'Exam submit time',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    KEY idx_exam_records_student (student_id, submitted_at),
    KEY idx_exam_records_paper (paper_id),
    KEY idx_exam_records_source (source_type, source_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student exam records';

CREATE TABLE exam_record_answers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    exam_record_id BIGINT NOT NULL COMMENT 'Exam record ID',
    question_id BIGINT NOT NULL COMMENT 'Question ID',
    answer_content TEXT NULL COMMENT 'Student answer content',
    score DECIMAL(6,2) NOT NULL DEFAULT 0 COMMENT 'Answer score',
    correct TINYINT NOT NULL DEFAULT 0 COMMENT '1 correct, 0 incorrect',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    UNIQUE KEY uk_exam_record_answers_record_question (exam_record_id, question_id),
    KEY idx_exam_record_answers_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Student exam answer details';
