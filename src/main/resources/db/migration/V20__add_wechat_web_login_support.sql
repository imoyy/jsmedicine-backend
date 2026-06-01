ALTER TABLE app_users
    ADD COLUMN wechat_web_open_id VARCHAR(64) NULL COMMENT 'WeChat website open id' AFTER wechat_open_id,
    ADD UNIQUE KEY uk_app_users_wechat_web_open_id (wechat_web_open_id);
