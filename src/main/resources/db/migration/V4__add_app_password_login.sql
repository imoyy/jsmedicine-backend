ALTER TABLE app_users
    ADD COLUMN password_hash VARCHAR(100) NULL COMMENT 'BCrypt password hash' AFTER username,
    ADD COLUMN password_updated_at DATETIME NULL COMMENT 'Password update time' AFTER profile_completed,
    ADD KEY idx_app_users_username_status (username, status, deleted);
