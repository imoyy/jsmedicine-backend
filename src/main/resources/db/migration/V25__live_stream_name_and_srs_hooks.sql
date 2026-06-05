ALTER TABLE live_sessions
    ADD COLUMN stream_name VARCHAR(128) NULL COMMENT 'Live stream name' AFTER anchor_name,
    ADD UNIQUE KEY uk_live_sessions_stream_name (stream_name),
    ADD KEY idx_live_sessions_stream_name (stream_name, deleted);

UPDATE live_sessions
SET stream_name = CONCAT('live-', id)
WHERE stream_name IS NULL;
