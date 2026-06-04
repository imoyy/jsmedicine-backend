ALTER TABLE home_contents
    ADD COLUMN cover_file_asset_id BIGINT NULL COMMENT 'Cover file asset ID' AFTER cover_url,
    ADD KEY idx_home_contents_cover_file_asset (cover_file_asset_id);

ALTER TABLE courses
    ADD COLUMN cover_file_asset_id BIGINT NULL COMMENT 'Cover file asset ID' AFTER cover_url,
    ADD KEY idx_courses_cover_file_asset (cover_file_asset_id);

ALTER TABLE books
    ADD COLUMN cover_file_asset_id BIGINT NULL COMMENT 'Cover file asset ID' AFTER cover_url,
    ADD KEY idx_books_cover_file_asset (cover_file_asset_id);

ALTER TABLE articles
    ADD COLUMN cover_file_asset_id BIGINT NULL COMMENT 'Cover file asset ID' AFTER cover_url,
    ADD KEY idx_articles_cover_file_asset (cover_file_asset_id);

ALTER TABLE podcasts
    ADD COLUMN cover_file_asset_id BIGINT NULL COMMENT 'Cover file asset ID' AFTER cover_url,
    ADD KEY idx_podcasts_cover_file_asset (cover_file_asset_id);

ALTER TABLE topics
    ADD COLUMN cover_file_asset_id BIGINT NULL COMMENT 'Cover file asset ID' AFTER cover_url,
    ADD KEY idx_topics_cover_file_asset (cover_file_asset_id);

ALTER TABLE experts
    ADD COLUMN cover_file_asset_id BIGINT NULL COMMENT 'Cover file asset ID' AFTER cover_url,
    ADD KEY idx_experts_cover_file_asset (cover_file_asset_id);

ALTER TABLE live_sessions
    ADD COLUMN cover_file_asset_id BIGINT NULL COMMENT 'Cover file asset ID' AFTER cover_url,
    ADD KEY idx_live_sessions_cover_file_asset (cover_file_asset_id);

ALTER TABLE knowledge_entries
    ADD COLUMN cover_file_asset_id BIGINT NULL COMMENT 'Cover file asset ID' AFTER cover_url,
    ADD KEY idx_knowledge_entries_cover_file_asset (cover_file_asset_id);

UPDATE home_contents content_table
JOIN file_assets asset_table
    ON asset_table.url = content_table.cover_url
   AND asset_table.deleted = 0
SET content_table.cover_file_asset_id = asset_table.id
WHERE content_table.cover_url IS NOT NULL
  AND content_table.cover_file_asset_id IS NULL;

UPDATE courses course_table
JOIN file_assets asset_table
    ON asset_table.url = course_table.cover_url
   AND asset_table.deleted = 0
SET course_table.cover_file_asset_id = asset_table.id
WHERE course_table.cover_url IS NOT NULL
  AND course_table.cover_file_asset_id IS NULL;

UPDATE books book_table
JOIN file_assets asset_table
    ON asset_table.url = book_table.cover_url
   AND asset_table.deleted = 0
SET book_table.cover_file_asset_id = asset_table.id
WHERE book_table.cover_url IS NOT NULL
  AND book_table.cover_file_asset_id IS NULL;

UPDATE articles article_table
JOIN file_assets asset_table
    ON asset_table.url = article_table.cover_url
   AND asset_table.deleted = 0
SET article_table.cover_file_asset_id = asset_table.id
WHERE article_table.cover_url IS NOT NULL
  AND article_table.cover_file_asset_id IS NULL;

UPDATE podcasts podcast_table
JOIN file_assets asset_table
    ON asset_table.url = podcast_table.cover_url
   AND asset_table.deleted = 0
SET podcast_table.cover_file_asset_id = asset_table.id
WHERE podcast_table.cover_url IS NOT NULL
  AND podcast_table.cover_file_asset_id IS NULL;

UPDATE topics topic_table
JOIN file_assets asset_table
    ON asset_table.url = topic_table.cover_url
   AND asset_table.deleted = 0
SET topic_table.cover_file_asset_id = asset_table.id
WHERE topic_table.cover_url IS NOT NULL
  AND topic_table.cover_file_asset_id IS NULL;

UPDATE experts expert_table
JOIN file_assets asset_table
    ON asset_table.url = expert_table.cover_url
   AND asset_table.deleted = 0
SET expert_table.cover_file_asset_id = asset_table.id
WHERE expert_table.cover_url IS NOT NULL
  AND expert_table.cover_file_asset_id IS NULL;

UPDATE live_sessions live_table
JOIN file_assets asset_table
    ON asset_table.url = live_table.cover_url
   AND asset_table.deleted = 0
SET live_table.cover_file_asset_id = asset_table.id
WHERE live_table.cover_url IS NOT NULL
  AND live_table.cover_file_asset_id IS NULL;

UPDATE knowledge_entries entry_table
JOIN file_assets asset_table
    ON asset_table.url = entry_table.cover_url
   AND asset_table.deleted = 0
SET entry_table.cover_file_asset_id = asset_table.id
WHERE entry_table.cover_url IS NOT NULL
  AND entry_table.cover_file_asset_id IS NULL;
