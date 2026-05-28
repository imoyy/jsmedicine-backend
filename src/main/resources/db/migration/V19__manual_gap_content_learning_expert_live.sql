ALTER TABLE course_videos
    ADD COLUMN paper_id BIGINT NULL COMMENT 'Related exam paper ID' AFTER duration_seconds,
    ADD KEY idx_course_videos_paper (paper_id);

ALTER TABLE books
    ADD COLUMN total_pages INT NULL COMMENT 'Total pages' AFTER introduction;

ALTER TABLE book_chapters
    ADD COLUMN start_page INT NULL COMMENT 'Chapter start page' AFTER content,
    ADD COLUMN page_count INT NULL COMMENT 'Chapter page count' AFTER start_page;

ALTER TABLE articles
    ADD COLUMN source VARCHAR(128) NULL COMMENT 'Article source' AFTER author_name;

ALTER TABLE podcasts
    ADD COLUMN speaker_name VARCHAR(128) NULL COMMENT 'Podcast speaker name' AFTER cover_url;

ALTER TABLE podcast_audios
    ADD COLUMN paper_id BIGINT NULL COMMENT 'Related exam paper ID' AFTER duration_seconds,
    ADD KEY idx_podcast_audios_paper (paper_id);

ALTER TABLE experts
    ADD COLUMN gender TINYINT NULL COMMENT '0 unknown, 1 male, 2 female' AFTER real_name,
    ADD COLUMN birth_date DATE NULL COMMENT 'Birth date' AFTER gender,
    ADD COLUMN mobile VARCHAR(32) NULL COMMENT 'Mobile phone' AFTER birth_date,
    ADD COLUMN cover_url VARCHAR(512) NULL COMMENT 'Cover URL' AFTER avatar_url;

ALTER TABLE live_sessions
    ADD COLUMN speaker_name VARCHAR(128) NULL COMMENT 'Speaker name' AFTER anchor_name;

CREATE TABLE live_session_videos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Primary key',
    live_session_id BIGINT NOT NULL COMMENT 'Live session ID',
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
    KEY idx_live_session_videos_live (live_session_id, sort_order, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Live session videos';
