ALTER TABLE courses
    ADD COLUMN lecturer_avatar_url VARCHAR(512) NULL COMMENT 'Lecturer avatar URL' AFTER lecturer_name,
    ADD COLUMN lecturer_avatar_file_asset_id BIGINT NULL COMMENT 'Lecturer avatar file asset ID' AFTER lecturer_avatar_url,
    ADD KEY idx_courses_lecturer_avatar_file_asset (lecturer_avatar_file_asset_id);
