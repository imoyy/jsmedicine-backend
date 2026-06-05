-- 幂等 dev 验收数据脚本
-- 目标：
-- 1. 提供管理端/用户端联调所需的完整展示数据。
-- 2. 避免继续混用旧口径测试数据与新契约字段。
-- 3. 用户头像统一回填为稳定文件地址 /api/v1/files/{id}/content。

SET NAMES utf8mb4;

-- 清理依赖数据
DELETE FROM exam_record_answers
WHERE exam_record_id IN (
    SELECT id FROM exam_records WHERE source_type = 'td-seed'
);

DELETE FROM exam_records WHERE source_type = 'td-seed';
DELETE FROM learning_records WHERE resource_type IN ('course', 'book', 'podcast', 'live');
DELETE FROM qa_answers WHERE question_id IN (SELECT id FROM qa_questions WHERE title LIKE '[TD]%');
DELETE FROM qa_questions WHERE title LIKE '[TD]%';
DELETE FROM feedbacks WHERE feedback_type = 'td-seed';
DELETE FROM user_share_records WHERE user_id IN (SELECT id FROM app_users WHERE username LIKE 'td_%');
DELETE FROM user_browse_histories WHERE user_id IN (SELECT id FROM app_users WHERE username LIKE 'td_%');
DELETE FROM user_favorites WHERE user_id IN (SELECT id FROM app_users WHERE username LIKE 'td_%');
DELETE FROM knowledge_entries WHERE title LIKE '[TD]%';
DELETE FROM knowledge_categories WHERE category_name LIKE '[TD]%';
DELETE FROM live_session_videos WHERE live_session_id IN (SELECT id FROM live_sessions WHERE title LIKE '[TD]%');
DELETE FROM expert_experiences WHERE expert_id IN (SELECT id FROM experts WHERE real_name LIKE '[TD]%');
DELETE FROM expert_category_relations WHERE expert_id IN (SELECT id FROM experts WHERE real_name LIKE '[TD]%');
DELETE FROM experts WHERE real_name LIKE '[TD]%';
DELETE FROM expert_categories WHERE category_name LIKE '[TD]%';
DELETE FROM topic_items WHERE topic_id IN (SELECT id FROM topics WHERE title LIKE '[TD]%');
DELETE FROM topics WHERE title LIKE '[TD]%';
DELETE FROM podcast_audios WHERE podcast_id IN (SELECT id FROM podcasts WHERE title LIKE '[TD]%');
DELETE FROM podcasts WHERE title LIKE '[TD]%';
DELETE FROM articles WHERE title LIKE '[TD]%';
DELETE FROM book_chapters WHERE book_id IN (SELECT id FROM books WHERE book_name LIKE '[TD]%');
DELETE FROM books WHERE book_name LIKE '[TD]%';
DELETE FROM book_categories WHERE category_name LIKE '[TD]%';
DELETE FROM course_videos WHERE course_id IN (SELECT id FROM courses WHERE course_name LIKE '[TD]%');
DELETE FROM courses WHERE course_name LIKE '[TD]%';
DELETE FROM live_sessions WHERE title LIKE '[TD]%';
DELETE FROM exam_paper_questions WHERE paper_id IN (SELECT id FROM exam_papers WHERE paper_name LIKE '[TD]%');
DELETE FROM exam_papers WHERE paper_name LIKE '[TD]%';
DELETE FROM question_options WHERE question_id IN (SELECT id FROM questions WHERE title LIKE '[TD]%');
DELETE FROM questions WHERE title LIKE '[TD]%';
DELETE FROM question_categories WHERE category_name LIKE '[TD]%';
DELETE FROM home_contents WHERE title LIKE '[TD]%';
DELETE FROM home_categories WHERE category_name LIKE '[TD]%';
DELETE FROM entity_extensions WHERE owner_type LIKE 'td_%';
DELETE FROM resource_tags WHERE tag_id IN (SELECT id FROM tags WHERE tag_name LIKE '[TD]%' AND deleted = 0);
DELETE FROM tags WHERE tag_name LIKE '[TD]%';
DELETE FROM file_assets WHERE object_key LIKE 'td/%' OR object_key LIKE 'app-users/%/avatars/%';
DELETE FROM audit_records WHERE target_type LIKE 'td_%';
DELETE FROM student_certification_files WHERE student_id IN (SELECT id FROM students WHERE student_no LIKE 'TD-%');
DELETE FROM students WHERE student_no LIKE 'TD-%';
DELETE FROM app_user_identities WHERE user_id IN (SELECT id FROM app_users WHERE username LIKE 'td_%');
DELETE FROM app_users WHERE username LIKE 'td_%';
DELETE FROM organizations WHERE org_code LIKE 'TD_%';
DELETE FROM practice_types WHERE type_code LIKE 'TD_%';
DELETE FROM sys_admin_roles WHERE admin_id IN (SELECT id FROM sys_admins WHERE username IN ('td_admin', 'td_viewer'));
DELETE FROM sys_admins WHERE username IN ('td_admin', 'td_viewer');
DELETE FROM sys_role_permissions WHERE role_id IN (SELECT id FROM sys_roles WHERE role_code IN ('TD_CONTENT_EDITOR', 'TD_READONLY'));
DELETE FROM sys_roles WHERE role_code IN ('TD_CONTENT_EDITOR', 'TD_READONLY');
DELETE FROM sys_permissions WHERE permission_code IN (
    'auth:me', 'auth:logout', 'auth:status',
    'sys:admin:view', 'sys:admin:create', 'sys:admin:update', 'sys:admin:disable', 'sys:admin:reset-password',
    'course:view', 'course:review', 'book:view', 'article:review', 'topic:view', 'expert:view'
);

-- 管理端账号与权限
INSERT INTO sys_roles (role_code, role_name, description, status, sort_order, created_by, updated_by, deleted)
SELECT 'TD_CONTENT_EDITOR', '测试内容运营', '测试数据内容运营角色', 1, 10, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_roles WHERE role_code = 'TD_CONTENT_EDITOR' AND deleted = 0);

INSERT INTO sys_roles (role_code, role_name, description, status, sort_order, created_by, updated_by, deleted)
SELECT 'TD_READONLY', '测试只读账号', '测试数据只读角色', 1, 11, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_roles WHERE role_code = 'TD_READONLY' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'auth:me', '获取当前管理员信息', 3, NULL, 'GET', '/api/v1/auth/me', NULL, 1, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'auth:me' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'auth:logout', '退出登录', 3, NULL, 'POST', '/api/v1/auth/logout', NULL, 2, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'auth:logout' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'auth:status', '校验登录状态', 3, NULL, 'GET', '/api/v1/auth/status', NULL, 3, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'auth:status' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'sys:admin:view', '查看管理员', 3, NULL, 'GET', '/api/v1/admin/system/admins', NULL, 10, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'sys:admin:view' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'sys:admin:create', '创建管理员', 3, NULL, 'POST', '/api/v1/admin/system/admins', NULL, 11, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'sys:admin:create' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'sys:admin:update', '更新管理员', 3, NULL, 'PUT', '/api/v1/admin/system/admins/{id}', NULL, 12, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'sys:admin:update' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'sys:admin:disable', '禁用管理员', 3, NULL, 'PATCH', '/api/v1/admin/system/admins/{id}/status', NULL, 13, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'sys:admin:disable' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'sys:admin:reset-password', '重置管理员密码', 3, NULL, 'PATCH', '/api/v1/admin/system/admins/{id}/password/reset', NULL, 14, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'sys:admin:reset-password' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'course:view', '查看课程', 3, NULL, 'GET', '/api/v1/courses', NULL, 20, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'course:view' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'course:review', '审核课程', 3, NULL, 'PATCH', '/api/v1/courses/{id}/review', NULL, 21, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'course:review' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'book:view', '查看图书', 3, NULL, 'GET', '/api/v1/books', NULL, 22, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'book:view' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'article:review', '审核资讯', 3, NULL, 'PATCH', '/api/v1/articles/{id}/review', NULL, 23, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'article:review' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'topic:view', '查看专题', 3, NULL, 'GET', '/api/v1/topics', NULL, 24, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'topic:view' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'expert:view', '查看专家', 3, NULL, 'GET', '/api/v1/experts', NULL, 25, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'expert:view' AND deleted = 0);

SET @role_super_admin = (SELECT id FROM sys_roles WHERE role_code = 'SUPER_ADMIN' AND deleted = 0 LIMIT 1);
SET @role_editor = (SELECT id FROM sys_roles WHERE role_code = 'TD_CONTENT_EDITOR' AND deleted = 0 LIMIT 1);
SET @role_viewer = (SELECT id FROM sys_roles WHERE role_code = 'TD_READONLY' AND deleted = 0 LIMIT 1);

INSERT INTO sys_role_permissions (role_id, permission_id)
SELECT @role_editor, permission_table.id
FROM sys_permissions permission_table
WHERE permission_table.permission_code IN (
    'auth:me', 'auth:logout', 'auth:status',
    'sys:admin:view', 'sys:admin:create', 'sys:admin:update', 'sys:admin:disable', 'sys:admin:reset-password',
    'course:view', 'course:review', 'book:view', 'article:review', 'topic:view', 'expert:view'
)
AND NOT EXISTS (
    SELECT 1 FROM sys_role_permissions relation_table
    WHERE relation_table.role_id = @role_editor
      AND relation_table.permission_id = permission_table.id
);

INSERT INTO sys_role_permissions (role_id, permission_id)
SELECT @role_viewer, permission_table.id
FROM sys_permissions permission_table
WHERE permission_table.permission_code IN ('auth:status', 'auth:logout')
AND NOT EXISTS (
    SELECT 1 FROM sys_role_permissions relation_table
    WHERE relation_table.role_id = @role_viewer
      AND relation_table.permission_id = permission_table.id
);

INSERT INTO sys_admins (username, password_hash, real_name, mobile, email, avatar_url, status, created_by, updated_by, deleted)
VALUES
('td_admin', '$2a$10$er8DjyviKgNF3YaYFkHwrudM2F8DWE2mmB3SmlrU0eEyt2NglFEwG', '测试管理员', '13800000001', 'td_admin@example.com', 'https://example.com/assets/admin-1.png', 1, 0, 0, 0),
('td_viewer', '$2a$10$er8DjyviKgNF3YaYFkHwrudM2F8DWE2mmB3SmlrU0eEyt2NglFEwG', '测试只读员', '13800000002', 'td_viewer@example.com', 'https://example.com/assets/admin-2.png', 1, 0, 0, 0);

SET @admin_td_admin = (SELECT id FROM sys_admins WHERE username = 'td_admin' AND deleted = 0 LIMIT 1);
SET @admin_td_viewer = (SELECT id FROM sys_admins WHERE username = 'td_viewer' AND deleted = 0 LIMIT 1);

INSERT INTO sys_admin_roles (admin_id, role_id)
SELECT @admin_td_admin, role_id_source.role_id
FROM (
    SELECT @role_super_admin AS role_id
    UNION ALL
    SELECT @role_editor AS role_id
) role_id_source
WHERE role_id_source.role_id IS NOT NULL;

INSERT INTO sys_admin_roles (admin_id, role_id)
SELECT @admin_td_viewer, @role_viewer
WHERE @role_viewer IS NOT NULL;

-- 文件、标签、基础数据
INSERT INTO file_assets (asset_type, storage_provider, bucket_name, object_key, original_name, content_type, file_size, url, created_by, deleted)
VALUES
('image', 'minio', 'public', 'td/course-cover-1.jpg', 'course-cover-1.jpg', 'image/jpeg', 286720, 'https://example.com/assets/td/course-cover-1.jpg', @admin_td_admin, 0),
('image', 'minio', 'public', 'td/book-cover-1.jpg', 'book-cover-1.jpg', 'image/jpeg', 245760, 'https://example.com/assets/td/book-cover-1.jpg', @admin_td_admin, 0),
('audio', 'minio', 'public', 'td/podcast-audio-1.mp3', 'podcast-audio-1.mp3', 'audio/mpeg', 5242880, 'https://example.com/assets/td/podcast-audio-1.mp3', @admin_td_admin, 0),
('video', 'minio', 'public', 'td/course-video-1.mp4', 'course-video-1.mp4', 'video/mp4', 125829120, 'https://example.com/assets/td/course-video-1.mp4', @admin_td_admin, 0);

INSERT INTO tags (tag_name, tag_type, color, status, created_by, updated_by, deleted)
VALUES
('[TD]针灸', 'course', '#1D4ED8', 1, @admin_td_admin, @admin_td_admin, 0),
('[TD]经典', 'book', '#059669', 1, @admin_td_admin, @admin_td_admin, 0),
('[TD]名师', 'expert', '#D97706', 1, @admin_td_admin, @admin_td_admin, 0),
('[TD]资讯来源', 'article', '#2563EB', 1, @admin_td_admin, @admin_td_admin, 0),
('[TD]夜读', 'podcast', '#7C3AED', 1, @admin_td_admin, @admin_td_admin, 0),
('[TD]实操', 'live', '#DC2626', 1, @admin_td_admin, @admin_td_admin, 0);

SET @tag_course = (SELECT id FROM tags WHERE tag_name = '[TD]针灸' AND deleted = 0 LIMIT 1);
SET @tag_book = (SELECT id FROM tags WHERE tag_name = '[TD]经典' AND deleted = 0 LIMIT 1);
SET @tag_expert = (SELECT id FROM tags WHERE tag_name = '[TD]名师' AND deleted = 0 LIMIT 1);
SET @tag_article = (SELECT id FROM tags WHERE tag_name = '[TD]资讯来源' AND deleted = 0 LIMIT 1);
SET @tag_podcast = (SELECT id FROM tags WHERE tag_name = '[TD]夜读' AND deleted = 0 LIMIT 1);
SET @tag_live = (SELECT id FROM tags WHERE tag_name = '[TD]实操' AND deleted = 0 LIMIT 1);

INSERT INTO organizations (org_code, org_name, org_type, province_code, city_code, district_code, address, status, sort_order, created_by, updated_by, deleted)
VALUES
('TD_ORG_ZJ_HOSPITAL', '省中医院', 'hospital', '330000', '330100', '330106', '杭州市西湖区测试路 1 号', 1, 1, @admin_td_admin, @admin_td_admin, 0),
('TD_ORG_JS_INSTITUTE', '针灸研究所', 'school', '320000', '320500', '320508', '苏州市姑苏区测试路 2 号', 1, 2, @admin_td_admin, @admin_td_admin, 0),
('TD_ORG_SC_UNIVERSITY', '中医药大学', 'school', '510000', '510100', '510190', '成都市高新区测试路 3 号', 1, 3, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO practice_types (parent_id, type_code, type_name, status, sort_order, created_by, updated_by, deleted)
VALUES
(NULL, 'TD_PRACTICE_CLINICAL', '临床执业', 1, 1, @admin_td_admin, @admin_td_admin, 0),
(NULL, 'TD_PRACTICE_TEACHING', '教学科研', 1, 2, @admin_td_admin, @admin_td_admin, 0),
(NULL, 'TD_PRACTICE_ACUPUNCTURE', '针灸推拿', 1, 3, @admin_td_admin, @admin_td_admin, 0);

SET @org_zj_hospital = (SELECT id FROM organizations WHERE org_code = 'TD_ORG_ZJ_HOSPITAL' AND deleted = 0 LIMIT 1);
SET @org_js_institute = (SELECT id FROM organizations WHERE org_code = 'TD_ORG_JS_INSTITUTE' AND deleted = 0 LIMIT 1);
SET @org_sc_university = (SELECT id FROM organizations WHERE org_code = 'TD_ORG_SC_UNIVERSITY' AND deleted = 0 LIMIT 1);
SET @practice_clinical = (SELECT id FROM practice_types WHERE type_code = 'TD_PRACTICE_CLINICAL' AND deleted = 0 LIMIT 1);
SET @practice_teaching = (SELECT id FROM practice_types WHERE type_code = 'TD_PRACTICE_TEACHING' AND deleted = 0 LIMIT 1);
SET @practice_acupuncture = (SELECT id FROM practice_types WHERE type_code = 'TD_PRACTICE_ACUPUNCTURE' AND deleted = 0 LIMIT 1);

-- 用户、头像、学员
INSERT INTO app_users (
    username, password_hash, mobile, email, nickname, profile_signature, avatar_url,
    auth_provider, wechat_open_id, wechat_web_open_id, wechat_union_id, gender, status,
    registered_at, last_login_at, last_login_ip, profile_completed, password_updated_at,
    created_by, updated_by, deleted
)
VALUES
('td_user_01', '$2a$10$3oFVwaq7Yq5ATpz69DNiE.XKiawLOLEN7KifhpMohOizAEmEIyFe.', '13900000001', 'td_user_01@example.com', '杏林新苗', '愿以寸心守岐黄', NULL, 'wechat_miniapp', 'td-openid-01', NULL, 'td-unionid-01', 1, 1, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 1 DAY, '127.0.0.1', 1, NOW() - INTERVAL 30 DAY, @admin_td_admin, @admin_td_admin, 0),
('td_user_02', '$2a$10$3oFVwaq7Yq5ATpz69DNiE.XKiawLOLEN7KifhpMohOizAEmEIyFe.', '13900000002', 'td_user_02@example.com', '岐黄同道', '临证与学习并进', NULL, 'mobile_sms', NULL, NULL, NULL, 2, 1, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 2 DAY, '127.0.0.1', 1, NOW() - INTERVAL 28 DAY, @admin_td_admin, @admin_td_admin, 0),
('td_user_03', NULL, '13900000003', 'td_user_03@example.com', '本草随行', '以教促学，以学促研', NULL, 'wechat_web', 'td-openid-03', 'td-web-openid-03', 'td-unionid-03', 1, 1, NOW() - INTERVAL 21 DAY, NOW() - INTERVAL 3 DAY, '127.0.0.1', 1, NULL, @admin_td_admin, @admin_td_admin, 0),
('td_user_04', '$2a$10$3oFVwaq7Yq5ATpz69DNiE.XKiawLOLEN7KifhpMohOizAEmEIyFe.', '13900000004', 'td_user_04@example.com', '问素有方', '重视病机推演与复盘', NULL, 'mobile_sms', NULL, NULL, NULL, 2, 1, NOW() - INTERVAL 19 DAY, NOW() - INTERVAL 6 HOUR, '127.0.0.1', 1, NOW() - INTERVAL 19 DAY, @admin_td_admin, @admin_td_admin, 0),
('td_user_05', '$2a$10$3oFVwaq7Yq5ATpz69DNiE.XKiawLOLEN7KifhpMohOizAEmEIyFe.', '13900000005', 'td_user_05@example.com', '灸法小记', '偏爱实操直播与病例拆解', NULL, 'wechat_miniapp', 'td-openid-05', NULL, 'td-unionid-05', 1, 1, NOW() - INTERVAL 17 DAY, NOW() - INTERVAL 10 HOUR, '127.0.0.1', 1, NOW() - INTERVAL 17 DAY, @admin_td_admin, @admin_td_admin, 0),
('td_user_06', '$2a$10$3oFVwaq7Yq5ATpz69DNiE.XKiawLOLEN7KifhpMohOizAEmEIyFe.', '13900000006', 'td_user_06@example.com', '经方研习生', '专注方剂辨证与随访记录', NULL, 'mobile_sms', NULL, NULL, NULL, 1, 1, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 1 DAY, '127.0.0.1', 0, NOW() - INTERVAL 14 DAY, @admin_td_admin, @admin_td_admin, 0),
('td_user_07', '$2a$10$3oFVwaq7Yq5ATpz69DNiE.XKiawLOLEN7KifhpMohOizAEmEIyFe.', '13900000007', 'td_user_07@example.com', '杏林带教', '关注门诊带教与规范操作', NULL, 'wechat_web', 'td-openid-07', 'td-web-openid-07', 'td-unionid-07', 2, 1, NOW() - INTERVAL 12 DAY, NOW() - INTERVAL 4 HOUR, '127.0.0.1', 1, NOW() - INTERVAL 12 DAY, @admin_td_admin, @admin_td_admin, 0);

SET @user_01 = (SELECT id FROM app_users WHERE username = 'td_user_01' AND deleted = 0 LIMIT 1);
SET @user_02 = (SELECT id FROM app_users WHERE username = 'td_user_02' AND deleted = 0 LIMIT 1);
SET @user_03 = (SELECT id FROM app_users WHERE username = 'td_user_03' AND deleted = 0 LIMIT 1);
SET @user_04 = (SELECT id FROM app_users WHERE username = 'td_user_04' AND deleted = 0 LIMIT 1);
SET @user_05 = (SELECT id FROM app_users WHERE username = 'td_user_05' AND deleted = 0 LIMIT 1);
SET @user_06 = (SELECT id FROM app_users WHERE username = 'td_user_06' AND deleted = 0 LIMIT 1);
SET @user_07 = (SELECT id FROM app_users WHERE username = 'td_user_07' AND deleted = 0 LIMIT 1);

INSERT INTO file_assets (asset_type, storage_provider, bucket_name, object_key, original_name, content_type, file_size, url, created_by, deleted)
VALUES
('image', 'minio', 'public', CONCAT('app-users/', @user_01, '/avatars/2026/06/', UUID(), '.jpg'), 'td-user-01-avatar.jpg', 'image/jpeg', 102400, NULL, @admin_td_admin, 0),
('image', 'minio', 'public', CONCAT('app-users/', @user_02, '/avatars/2026/06/', UUID(), '.jpg'), 'td-user-02-avatar.jpg', 'image/jpeg', 104448, NULL, @admin_td_admin, 0),
('image', 'minio', 'public', CONCAT('app-users/', @user_03, '/avatars/2026/06/', UUID(), '.jpg'), 'td-user-03-avatar.jpg', 'image/jpeg', 106496, NULL, @admin_td_admin, 0),
('image', 'minio', 'public', CONCAT('app-users/', @user_04, '/avatars/2026/06/', UUID(), '.jpg'), 'td-user-04-avatar.jpg', 'image/jpeg', 101376, NULL, @admin_td_admin, 0),
('image', 'minio', 'public', CONCAT('app-users/', @user_05, '/avatars/2026/06/', UUID(), '.jpg'), 'td-user-05-avatar.jpg', 'image/jpeg', 108544, NULL, @admin_td_admin, 0),
('image', 'minio', 'public', CONCAT('app-users/', @user_06, '/avatars/2026/06/', UUID(), '.jpg'), 'td-user-06-avatar.jpg', 'image/jpeg', 103424, NULL, @admin_td_admin, 0),
('image', 'minio', 'public', CONCAT('app-users/', @user_07, '/avatars/2026/06/', UUID(), '.jpg'), 'td-user-07-avatar.jpg', 'image/jpeg', 107520, NULL, @admin_td_admin, 0);

SET @user_01_avatar_asset = (SELECT id FROM file_assets WHERE original_name = 'td-user-01-avatar.jpg' ORDER BY id DESC LIMIT 1);
SET @user_02_avatar_asset = (SELECT id FROM file_assets WHERE original_name = 'td-user-02-avatar.jpg' ORDER BY id DESC LIMIT 1);
SET @user_03_avatar_asset = (SELECT id FROM file_assets WHERE original_name = 'td-user-03-avatar.jpg' ORDER BY id DESC LIMIT 1);
SET @user_04_avatar_asset = (SELECT id FROM file_assets WHERE original_name = 'td-user-04-avatar.jpg' ORDER BY id DESC LIMIT 1);
SET @user_05_avatar_asset = (SELECT id FROM file_assets WHERE original_name = 'td-user-05-avatar.jpg' ORDER BY id DESC LIMIT 1);
SET @user_06_avatar_asset = (SELECT id FROM file_assets WHERE original_name = 'td-user-06-avatar.jpg' ORDER BY id DESC LIMIT 1);
SET @user_07_avatar_asset = (SELECT id FROM file_assets WHERE original_name = 'td-user-07-avatar.jpg' ORDER BY id DESC LIMIT 1);

UPDATE file_assets
SET url = CONCAT('/api/v1/files/', id, '/content')
WHERE id IN (@user_01_avatar_asset, @user_02_avatar_asset, @user_03_avatar_asset, @user_04_avatar_asset, @user_05_avatar_asset, @user_06_avatar_asset, @user_07_avatar_asset);

UPDATE app_users SET avatar_url = CONCAT('/api/v1/files/', @user_01_avatar_asset, '/content') WHERE id = @user_01;
UPDATE app_users SET avatar_url = CONCAT('/api/v1/files/', @user_02_avatar_asset, '/content') WHERE id = @user_02;
UPDATE app_users SET avatar_url = CONCAT('/api/v1/files/', @user_03_avatar_asset, '/content') WHERE id = @user_03;
UPDATE app_users SET avatar_url = CONCAT('/api/v1/files/', @user_04_avatar_asset, '/content') WHERE id = @user_04;
UPDATE app_users SET avatar_url = CONCAT('/api/v1/files/', @user_05_avatar_asset, '/content') WHERE id = @user_05;
UPDATE app_users SET avatar_url = CONCAT('/api/v1/files/', @user_06_avatar_asset, '/content') WHERE id = @user_06;
UPDATE app_users SET avatar_url = CONCAT('/api/v1/files/', @user_07_avatar_asset, '/content') WHERE id = @user_07;

INSERT INTO students (
    user_id, student_no, real_name, gender, age, education_level, mobile, id_card_no,
    province, province_code, city, city_code, district, district_code,
    organization, organization_id, position_title, practice_type_id, status,
    certification_status, certification_submitted_at, certification_reviewed_at,
    certification_reviewed_by, reject_reason, certification_materials, enrolled_at,
    created_by, updated_by, deleted
)
VALUES
(@user_01, 'TD-STU-001', '张青云', 1, 35, '本科', '13900000001', '110101199001010011', '浙江省', '330000', '杭州市', '330100', '西湖区', '330106', '省中医院', @org_zj_hospital, '住院医师', @practice_clinical, 1, 2, NOW() - INTERVAL 21 DAY, NOW() - INTERVAL 20 DAY, @admin_td_admin, NULL, '["https://example.com/assets/td/cert-1-a.jpg","https://example.com/assets/td/cert-1-b.jpg"]', NOW() - INTERVAL 20 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_02, 'TD-STU-002', '李若水', 2, 33, '硕士研究生', '13900000002', '110101199202020022', '江苏省', '320000', '苏州市', '320500', '姑苏区', '320508', '针灸研究所', @org_js_institute, '主治医师', @practice_acupuncture, 1, 1, NOW() - INTERVAL 19 DAY, NULL, NULL, NULL, '["https://example.com/assets/td/cert-2-a.jpg"]', NOW() - INTERVAL 18 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_03, 'TD-STU-003', '王知秋', 1, 32, '博士研究生', '13900000003', '110101199303030033', '四川省', '510000', '成都市', '510100', '高新区', '510190', '中医药大学', @org_sc_university, '讲师', @practice_teaching, 1, 3, NOW() - INTERVAL 16 DAY, NOW() - INTERVAL 15 DAY, @admin_td_admin, '身份证照片不清晰', '["https://example.com/assets/td/cert-3-a.jpg"]', NOW() - INTERVAL 15 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_04, 'TD-STU-004', '赵明岚', 2, 29, '本科', '13900000004', '110101199404040044', '浙江省', '330000', '杭州市', '330100', '滨江区', '330108', '省中医院', @org_zj_hospital, '康复治疗师', @practice_clinical, 1, 2, NOW() - INTERVAL 14 DAY, NOW() - INTERVAL 13 DAY, @admin_td_admin, NULL, '["https://example.com/assets/td/cert-4-a.jpg"]', NOW() - INTERVAL 13 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_05, 'TD-STU-005', '孙问素', 1, 31, '硕士研究生', '13900000005', '110101199505050055', '江苏省', '320000', '苏州市', '320500', '工业园区', '320571', '针灸研究所', @org_js_institute, '住院总医师', @practice_acupuncture, 1, 1, NOW() - INTERVAL 11 DAY, NULL, NULL, NULL, '["https://example.com/assets/td/cert-5-a.jpg","https://example.com/assets/td/cert-5-b.jpg"]', NOW() - INTERVAL 11 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_06, 'TD-STU-006', '何循经', 1, 38, '大专', '13900000006', '110101198806060066', '四川省', '510000', '成都市', '510100', '武侯区', '510107', '中医药大学', @org_sc_university, '门诊带教老师', @practice_teaching, 1, 2, NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 8 DAY, @admin_td_admin, NULL, '["https://example.com/assets/td/cert-6-a.jpg"]', NOW() - INTERVAL 8 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_07, 'TD-STU-007', '周砚秋', 2, 27, '本科', '13900000007', '110101199707070077', '广东省', '440000', '广州市', '440100', '天河区', '440106', '省中医院远程门诊协作点', NULL, '实习医师', @practice_clinical, 1, 3, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 6 DAY, @admin_td_admin, '执业证明材料页码缺失', '["https://example.com/assets/td/cert-7-a.jpg"]', NOW() - INTERVAL 7 DAY, @admin_td_admin, @admin_td_admin, 0);

SET @student_01 = (SELECT id FROM students WHERE student_no = 'TD-STU-001' AND deleted = 0 LIMIT 1);
SET @student_02 = (SELECT id FROM students WHERE student_no = 'TD-STU-002' AND deleted = 0 LIMIT 1);
SET @student_03 = (SELECT id FROM students WHERE student_no = 'TD-STU-003' AND deleted = 0 LIMIT 1);
SET @student_04 = (SELECT id FROM students WHERE student_no = 'TD-STU-004' AND deleted = 0 LIMIT 1);
SET @student_05 = (SELECT id FROM students WHERE student_no = 'TD-STU-005' AND deleted = 0 LIMIT 1);
SET @student_06 = (SELECT id FROM students WHERE student_no = 'TD-STU-006' AND deleted = 0 LIMIT 1);
SET @student_07 = (SELECT id FROM students WHERE student_no = 'TD-STU-007' AND deleted = 0 LIMIT 1);

INSERT INTO app_user_identities (user_id, identity_type, identity_status, is_primary, activated_at, created_by, updated_by, deleted)
VALUES
(@user_01, 'STUDENT', 1, 1, NOW() - INTERVAL 20 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_02, 'STUDENT', 1, 1, NOW() - INTERVAL 18 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_03, 'STUDENT', 1, 1, NOW() - INTERVAL 15 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_04, 'STUDENT', 1, 1, NOW() - INTERVAL 13 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_05, 'STUDENT', 1, 1, NOW() - INTERVAL 11 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_06, 'STUDENT', 1, 1, NOW() - INTERVAL 8 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_07, 'STUDENT', 1, 1, NOW() - INTERVAL 7 DAY, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO student_certification_files (student_id, file_asset_id, source_url, material_type, sort_order, created_by, updated_by, deleted)
VALUES
(@student_01, NULL, 'https://example.com/assets/td/cert-1-a.jpg', 'id_card', 1, @admin_td_admin, @admin_td_admin, 0),
(@student_01, NULL, 'https://example.com/assets/td/cert-1-b.jpg', 'qualification', 2, @admin_td_admin, @admin_td_admin, 0),
(@student_02, NULL, 'https://example.com/assets/td/cert-2-a.jpg', 'qualification', 1, @admin_td_admin, @admin_td_admin, 0),
(@student_03, NULL, 'https://example.com/assets/td/cert-3-a.jpg', 'id_card', 1, @admin_td_admin, @admin_td_admin, 0),
(@student_04, NULL, 'https://example.com/assets/td/cert-4-a.jpg', 'qualification', 1, @admin_td_admin, @admin_td_admin, 0),
(@student_05, NULL, 'https://example.com/assets/td/cert-5-a.jpg', 'id_card', 1, @admin_td_admin, @admin_td_admin, 0),
(@student_05, NULL, 'https://example.com/assets/td/cert-5-b.jpg', 'qualification', 2, @admin_td_admin, @admin_td_admin, 0),
(@student_06, NULL, 'https://example.com/assets/td/cert-6-a.jpg', 'qualification', 1, @admin_td_admin, @admin_td_admin, 0),
(@student_07, NULL, 'https://example.com/assets/td/cert-7-a.jpg', 'qualification', 1, @admin_td_admin, @admin_td_admin, 0);

-- 首页、题库、考卷
INSERT INTO home_categories (parent_id, category_name, category_code, icon_url, description, sort_order, status, created_by, updated_by, deleted)
VALUES
(NULL, '[TD]首页推荐', 'TD_HOME_REC', 'https://example.com/assets/td/home-rec.png', '测试首页推荐分类', 1, 1, @admin_td_admin, @admin_td_admin, 0),
(NULL, '[TD]热门专题', 'TD_HOME_TOPIC', 'https://example.com/assets/td/home-topic.png', '测试首页专题分类', 2, 1, @admin_td_admin, @admin_td_admin, 0);

SET @home_category_01 = (SELECT id FROM home_categories WHERE category_code = 'TD_HOME_REC' AND deleted = 0 LIMIT 1);
SET @home_category_02 = (SELECT id FROM home_categories WHERE category_code = 'TD_HOME_TOPIC' AND deleted = 0 LIMIT 1);

INSERT INTO question_categories (parent_id, category_name, sort_order, status, created_by, updated_by, deleted)
VALUES
(NULL, '[TD]针灸基础', 1, 1, @admin_td_admin, @admin_td_admin, 0),
(NULL, '[TD]方剂学', 2, 1, @admin_td_admin, @admin_td_admin, 0);

SET @question_category_01 = (SELECT id FROM question_categories WHERE category_name = '[TD]针灸基础' AND deleted = 0 LIMIT 1);
SET @question_category_02 = (SELECT id FROM question_categories WHERE category_name = '[TD]方剂学' AND deleted = 0 LIMIT 1);

INSERT INTO questions (category_id, question_type, title, analysis, difficulty, score, status, created_by, updated_by, deleted)
VALUES
(@question_category_01, 1, '[TD]针灸最常用的毫针规格是？', '测试单选题解析', 1, 5, 1, @admin_td_admin, @admin_td_admin, 0),
(@question_category_01, 2, '[TD]下列哪些属于针刺禁忌？', '测试多选题解析', 2, 10, 1, @admin_td_admin, @admin_td_admin, 0),
(@question_category_02, 3, '[TD]四物汤属于补血剂。', '测试判断题解析', 1, 5, 1, @admin_td_admin, @admin_td_admin, 0);

SET @question_01 = (SELECT id FROM questions WHERE title = '[TD]针灸最常用的毫针规格是？' AND deleted = 0 LIMIT 1);
SET @question_02 = (SELECT id FROM questions WHERE title = '[TD]下列哪些属于针刺禁忌？' AND deleted = 0 LIMIT 1);
SET @question_03 = (SELECT id FROM questions WHERE title = '[TD]四物汤属于补血剂。' AND deleted = 0 LIMIT 1);

INSERT INTO question_options (question_id, option_key, option_content, correct, sort_order)
VALUES
(@question_01, 'A', '0.25×40mm', 1, 1),
(@question_01, 'B', '0.50×75mm', 0, 2),
(@question_01, 'C', '1.00×100mm', 0, 3),
(@question_02, 'A', '饥饿过度时立即针刺', 1, 1),
(@question_02, 'B', '高热惊厥时配合急救', 0, 2),
(@question_02, 'C', '大汗大渴、体质极虚时强刺激', 1, 3),
(@question_02, 'D', '局部皮肤破损感染时针刺', 1, 4),
(@question_03, 'A', '正确', 1, 1),
(@question_03, 'B', '错误', 0, 2);

INSERT INTO exam_papers (paper_name, description, total_score, pass_score, duration_minutes, status, created_by, updated_by, deleted)
VALUES
('[TD]针灸基础试卷', '测试课程与图书共用试卷', 20, 12, 30, 1, @admin_td_admin, @admin_td_admin, 0);

SET @paper_01 = (SELECT id FROM exam_papers WHERE paper_name = '[TD]针灸基础试卷' AND deleted = 0 LIMIT 1);

INSERT INTO exam_paper_questions (paper_id, question_id, score, sort_order)
VALUES
(@paper_01, @question_01, 5, 1),
(@paper_01, @question_02, 10, 2),
(@paper_01, @question_03, 5, 3);

-- 学习资源
INSERT INTO courses (course_name, subtitle, cover_url, lecturer_name, introduction, review_status, publish_status, paper_id, sort_order, published_at, created_by, updated_by, deleted)
VALUES
('[TD]经络腧穴速学', '90 分钟掌握经络走向与常用取穴', 'https://example.com/assets/td/course-cover-1.jpg', '陈思远', '围绕十二经脉循行、常用穴定位和门诊高频配穴做系统讲解，适合执业医师与规培学员用于复训和带教前准备。', 2, 1, @paper_01, 1, NOW() - INTERVAL 10 DAY, @admin_td_admin, @admin_td_admin, 0),
('[TD]针灸临床入门', '门诊常见疼痛证型的辨证取穴思路', 'https://example.com/assets/td/course-cover-2.jpg', '周岐黄', '从颈肩腰腿痛、失眠与脾胃虚弱三个常见场景切入，整理临床首诊问诊要点与安全操作边界，保留未发布态用于审核联调。', 1, 0, @paper_01, 2, NULL, @admin_td_admin, @admin_td_admin, 0),
('[TD]经方辨证针灸应用', '从方证对应切入针药并用思路', 'https://example.com/assets/td/course-cover-3.jpg', '宋本草', '结合桂枝汤、四逆散与补中益气汤常见适应证，演示针药并用的病机分析路径和随访记录结构。', 2, 1, NULL, 3, NOW() - INTERVAL 9 DAY, @admin_td_admin, @admin_td_admin, 0),
('[TD]门诊肩颈腰腿痛针法', '聚焦疼痛科门诊高频病种', 'https://example.com/assets/td/course-cover-4.jpg', '陈景岐', '覆盖落枕、肩周炎、腰椎间盘突出恢复期和膝痹四类场景，适合联调课程列表筛选与学习进度展示。', 2, 1, @paper_01, 4, NOW() - INTERVAL 8 DAY, @admin_td_admin, @admin_td_admin, 0),
('[TD]督脉灸法基础', '灸法适应证、禁忌与操作流程', 'https://example.com/assets/td/course-cover-5.jpg', '赵明岚', '围绕督脉灸基础操作、患者沟通与不良反应观察做梳理，作为已发布但未绑定试卷的课程样本。', 2, 1, NULL, 5, NOW() - INTERVAL 6 DAY, @admin_td_admin, @admin_td_admin, 0),
('[TD]耳穴压豆实训', '门诊可复制的轻干预项目', 'https://example.com/assets/td/course-cover-6.jpg', '何循经', '聚焦睡眠管理、情志调适与门诊复诊随访，保留待审核待发布状态，用于后台筛选联调。', 1, 0, NULL, 6, NULL, @admin_td_admin, @admin_td_admin, 0);

SET @course_01 = (SELECT id FROM courses WHERE course_name = '[TD]经络腧穴速学' AND deleted = 0 LIMIT 1);
SET @course_02 = (SELECT id FROM courses WHERE course_name = '[TD]针灸临床入门' AND deleted = 0 LIMIT 1);
SET @course_03 = (SELECT id FROM courses WHERE course_name = '[TD]经方辨证针灸应用' AND deleted = 0 LIMIT 1);
SET @course_04 = (SELECT id FROM courses WHERE course_name = '[TD]门诊肩颈腰腿痛针法' AND deleted = 0 LIMIT 1);
SET @course_05 = (SELECT id FROM courses WHERE course_name = '[TD]督脉灸法基础' AND deleted = 0 LIMIT 1);

INSERT INTO course_videos (course_id, title, video_url, duration_seconds, paper_id, sort_order, status, created_by, updated_by, deleted)
VALUES
(@course_01, '[TD]经络总论', 'https://example.com/assets/td/course-video-1.mp4', 900, @paper_01, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@course_01, '[TD]常用腧穴', 'https://example.com/assets/td/course-video-2.mp4', 1200, @paper_01, 2, 1, @admin_td_admin, @admin_td_admin, 0),
(@course_02, '[TD]临床案例导读', 'https://example.com/assets/td/course-video-3.mp4', 1500, @paper_01, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@course_03, '[TD]桂枝汤证与经络辨治', 'https://example.com/assets/td/course-video-4.mp4', 1320, NULL, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@course_04, '[TD]肩井到合谷的配穴路径', 'https://example.com/assets/td/course-video-5.mp4', 1180, @paper_01, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@course_05, '[TD]督脉灸操作准备', 'https://example.com/assets/td/course-video-6.mp4', 980, NULL, 1, 1, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO book_categories (parent_id, category_name, sort_order, status, created_by, updated_by, deleted)
VALUES
(NULL, '[TD]针灸教材', 1, 1, @admin_td_admin, @admin_td_admin, 0),
(NULL, '[TD]经典方剂', 2, 1, @admin_td_admin, @admin_td_admin, 0);

SET @book_category_01 = (SELECT id FROM book_categories WHERE category_name = '[TD]针灸教材' AND deleted = 0 LIMIT 1);
SET @book_category_02 = (SELECT id FROM book_categories WHERE category_name = '[TD]经典方剂' AND deleted = 0 LIMIT 1);

INSERT INTO books (category_id, book_name, author, publisher, cover_url, introduction, total_pages, review_status, publish_status, paper_id, sort_order, published_at, created_by, updated_by, deleted)
VALUES
(@book_category_01, '[TD]针灸学临证读本', '林问岐', '中医古籍出版社', 'https://example.com/assets/td/book-cover-1.jpg', '以门诊真实病种串联经络、腧穴和操作手法，适合配合课程章节与考试试卷做整套学习闭环验收。', 286, 2, 1, @paper_01, 1, NOW() - INTERVAL 14 DAY, @admin_td_admin, @admin_td_admin, 0),
(@book_category_02, '[TD]方剂辨治精要', '许本草', '人民卫生出版社', 'https://example.com/assets/td/book-cover-2.jpg', '聚焦补益剂、和解剂和祛湿剂三类高频处方，保留待发布状态用于后台审核、预览和下架前联调。', 198, 1, 0, @paper_01, 2, NULL, @admin_td_admin, @admin_td_admin, 0),
(@book_category_01, '[TD]针灸门诊病案手册', '陈思远', '中国中医药出版社', 'https://example.com/assets/td/book-cover-3.jpg', '按初诊、复诊、疗效评估三段整理门诊病案，方便联调图书列表、详情和阅读记录。', 232, 2, 1, NULL, 3, NOW() - INTERVAL 11 DAY, @admin_td_admin, @admin_td_admin, 0),
(@book_category_02, '[TD]脾胃方剂速查', '宋本草', '上海科学技术出版社', 'https://example.com/assets/td/book-cover-4.jpg', '收录脾胃常见证型的基础处方、加减思路与服药提醒，作为已发布知识型图书样本。', 176, 2, 1, NULL, 4, NOW() - INTERVAL 9 DAY, @admin_td_admin, @admin_td_admin, 0);

SET @book_01 = (SELECT id FROM books WHERE book_name = '[TD]针灸学临证读本' AND deleted = 0 LIMIT 1);
SET @book_02 = (SELECT id FROM books WHERE book_name = '[TD]方剂辨治精要' AND deleted = 0 LIMIT 1);
SET @book_03 = (SELECT id FROM books WHERE book_name = '[TD]针灸门诊病案手册' AND deleted = 0 LIMIT 1);

INSERT INTO book_chapters (book_id, parent_id, chapter_title, content, start_page, page_count, paper_id, sort_order, status, created_by, updated_by, deleted)
VALUES
(@book_01, NULL, '[TD]第一章 经络基础', '测试章节内容一。', 1, 36, @paper_01, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@book_01, NULL, '[TD]第二章 常用腧穴', '测试章节内容二。', 37, 52, NULL, 2, 1, @admin_td_admin, @admin_td_admin, 0),
(@book_02, NULL, '[TD]第一章 补益方总论', '测试章节内容三。', 1, 28, NULL, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@book_03, NULL, '[TD]第一章 初诊问诊框架', '门诊病案采集、主诉整理与体征记录模板。', 1, 22, NULL, 1, 1, @admin_td_admin, @admin_td_admin, 0);

SET @book_chapter_01 = (SELECT id FROM book_chapters WHERE chapter_title = '[TD]第一章 经络基础' AND deleted = 0 LIMIT 1);

INSERT INTO articles (title, summary, cover_url, content, author_name, source, review_status, publish_status, published_at, view_count, created_by, updated_by, deleted)
VALUES
('[TD]春季养肝调气指南', '结合门诊常见失眠、胸闷与纳差表现，梳理春季疏肝理气的居家调养建议。', 'https://example.com/assets/td/article-cover-1.jpg', '<p>春季肝气升发，临床调护应兼顾作息、饮食与情志管理。</p><p>文章从起居、茶饮与穴位按揉三个方面给出可执行建议，方便前端联调富文本展示与详情页收藏浏览统计。</p>', '编辑部', '中医在线编辑部', 2, 1, NOW() - INTERVAL 6 DAY, 120, @admin_td_admin, @admin_td_admin, 0),
('[TD]针灸门诊带教札记', '记录门诊带教中关于取穴顺序、体位保护和知情沟通的高频提醒。', 'https://example.com/assets/td/article-cover-2.jpg', '<p>门诊教学最容易忽视的是体位摆放、针前沟通和留针观察。</p><p>本文用于演示待审核资讯在后台的查看与审核流程。</p>', '陈思远', '省中医院门诊部', 1, 0, NULL, 38, @admin_td_admin, @admin_td_admin, 0),
('[TD]三伏贴门诊安排说明', '用于公告门诊开诊时间、预约方式和贴敷注意事项。', 'https://example.com/assets/td/article-cover-3.jpg', '<p>三伏贴预约需提前登记基础病史，并避开皮肤破损区域。</p><p>本条资讯可用于联调后台列表排序与发布时间筛选。</p>', '运营中心', '治未病中心', 2, 1, NOW() - INTERVAL 3 DAY, 86, @admin_td_admin, @admin_td_admin, 0),
('[TD]实习医师针刺规范提醒', '汇总实习医师在门诊实操中的常见失误与防范要点。', 'https://example.com/assets/td/article-cover-4.jpg', '<p>包含进针前核对、留针观察、起针后宣教三部分。</p>', '何循经', '教学办公室', 2, 1, NOW() - INTERVAL 2 DAY, 54, @admin_td_admin, @admin_td_admin, 0),
('[TD]方剂直播预习资料', '用于直播前预习基础方证和加减规则。', 'https://example.com/assets/td/article-cover-5.jpg', '<p>配合直播答疑使用，条目保留待审核状态。</p>', '宋本草', '课程教研组', 1, 0, NULL, 12, @admin_td_admin, @admin_td_admin, 0);

SET @article_01 = (SELECT id FROM articles WHERE title = '[TD]春季养肝调气指南' AND deleted = 0 LIMIT 1);
SET @article_02 = (SELECT id FROM articles WHERE title = '[TD]针灸门诊带教札记' AND deleted = 0 LIMIT 1);

INSERT INTO podcasts (title, summary, cover_url, speaker_name, review_status, publish_status, published_at, sort_order, created_by, updated_by, deleted)
VALUES
('[TD]黄帝内经夜读', '每期 30 分钟，用通俗语言拆解经典条文与临床对应关系。', 'https://example.com/assets/td/podcast-cover-1.jpg', '陈景岐', 2, 1, NOW() - INTERVAL 5 DAY, 1, @admin_td_admin, @admin_td_admin, 0),
('[TD]本草问答录', '围绕药对配伍、煎服要点与常见误区做短音频答疑。', 'https://example.com/assets/td/podcast-cover-2.jpg', '宋本草', 1, 0, NULL, 2, @admin_td_admin, @admin_td_admin, 0),
('[TD]门诊复盘晨听', '10 分钟复盘前一日门诊病例与随访重点。', 'https://example.com/assets/td/podcast-cover-3.jpg', '赵明岚', 2, 1, NOW() - INTERVAL 4 DAY, 3, @admin_td_admin, @admin_td_admin, 0),
('[TD]经络答疑速记', '面向年轻医生的碎片化经络问答。', 'https://example.com/assets/td/podcast-cover-4.jpg', '王知秋', 2, 1, NOW() - INTERVAL 1 DAY, 4, @admin_td_admin, @admin_td_admin, 0);

SET @podcast_01 = (SELECT id FROM podcasts WHERE title = '[TD]黄帝内经夜读' AND deleted = 0 LIMIT 1);
SET @podcast_02 = (SELECT id FROM podcasts WHERE title = '[TD]门诊复盘晨听' AND deleted = 0 LIMIT 1);

INSERT INTO podcast_audios (podcast_id, title, audio_url, duration_seconds, paper_id, sort_order, status, created_by, updated_by, deleted)
VALUES
(@podcast_01, '[TD]第一期 经络循行', 'https://example.com/assets/td/podcast-audio-1.mp3', 1800, @paper_01, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@podcast_01, '[TD]第二期 脏腑表里', 'https://example.com/assets/td/podcast-audio-2.mp3', 2100, @paper_01, 2, 1, @admin_td_admin, @admin_td_admin, 0),
(@podcast_02, '[TD]第一期 门诊随访模板', 'https://example.com/assets/td/podcast-audio-3.mp3', 900, NULL, 1, 1, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO topics (title, summary, learning_requirements, cover_url, review_status, publish_status, published_at, sort_order, view_count, created_by, updated_by, deleted)
VALUES
('[TD]针灸临床专题', '聚合课程、图书与播客，覆盖经络基础到临床配穴的完整入门路径。', '完成专题下 2 个视频和 1 本图书学习后可参加考核。', 'https://example.com/assets/td/topic-cover-1.jpg', 2, 1, NOW() - INTERVAL 4 DAY, 1, 268, @admin_td_admin, @admin_td_admin, 0),
('[TD]门诊带教专题', '围绕门诊带教、病例复盘和知情沟通的专题聚合。', '建议先完成课程学习，再阅读病案手册与晨听播客。', 'https://example.com/assets/td/topic-cover-2.jpg', 2, 1, NOW() - INTERVAL 2 DAY, 2, 143, @admin_td_admin, @admin_td_admin, 0);

SET @topic_01 = (SELECT id FROM topics WHERE title = '[TD]针灸临床专题' AND deleted = 0 LIMIT 1);
SET @topic_02 = (SELECT id FROM topics WHERE title = '[TD]门诊带教专题' AND deleted = 0 LIMIT 1);

INSERT INTO topic_items (topic_id, item_type, item_id, sort_order)
VALUES
(@topic_01, 'course', @course_01, 1),
(@topic_01, 'course', @course_02, 2),
(@topic_01, 'book', @book_01, 3),
(@topic_01, 'podcast', @podcast_01, 4),
(@topic_02, 'course', @course_04, 1),
(@topic_02, 'book', @book_03, 2),
(@topic_02, 'podcast', @podcast_02, 3);

-- 专家与直播
INSERT INTO expert_categories (parent_id, category_name, sort_order, status, created_by, updated_by, deleted)
VALUES
(NULL, '[TD]针灸专家', 1, 1, @admin_td_admin, @admin_td_admin, 0),
(NULL, '[TD]方剂专家', 2, 1, @admin_td_admin, @admin_td_admin, 0);

SET @expert_category_01 = (SELECT id FROM expert_categories WHERE category_name = '[TD]针灸专家' AND deleted = 0 LIMIT 1);
SET @expert_category_02 = (SELECT id FROM expert_categories WHERE category_name = '[TD]方剂专家' AND deleted = 0 LIMIT 1);

INSERT INTO experts (
    user_id, real_name, gender, birth_date, mobile, avatar_url, cover_url, title,
    organization, organization_id, specialty, practice_type_id, introduction, status,
    consult_enabled, consultation_notice, sort_order, created_by, updated_by, deleted
)
VALUES
(@user_03, '[TD]陈景岐', 1, '1982-03-15', '13900000013', 'https://example.com/assets/td/expert-1.jpg', 'https://example.com/assets/td/expert-cover-1.jpg', '主任医师', '省中医院', @org_zj_hospital, '针灸与经络', @practice_acupuncture, '长期从事经络辨证、肩颈腰腿痛与亚健康调理的临床与教学工作。', 1, 1, '每周二、周四开放图文咨询。', 1, @admin_td_admin, @admin_td_admin, 0),
(NULL, '[TD]宋本草', 2, '1979-08-22', '13900000023', 'https://example.com/assets/td/expert-2.jpg', 'https://example.com/assets/td/expert-cover-2.jpg', '教授', '中医药大学', @org_sc_university, '方剂配伍', @practice_teaching, '聚焦经典方剂教学与研究，适合验证专家详情、履历与咨询说明展示。', 1, 1, '方剂配伍咨询需先完成基础问卷。', 2, @admin_td_admin, @admin_td_admin, 0);

SET @expert_01 = (SELECT id FROM experts WHERE real_name = '[TD]陈景岐' AND deleted = 0 LIMIT 1);
SET @expert_02 = (SELECT id FROM experts WHERE real_name = '[TD]宋本草' AND deleted = 0 LIMIT 1);

INSERT INTO app_user_identities (user_id, identity_type, identity_status, is_primary, activated_at, created_by, updated_by, deleted)
VALUES
(@user_03, 'EXPERT', 1, 0, NOW() - INTERVAL 10 DAY, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO expert_category_relations (expert_id, category_id)
VALUES
(@expert_01, @expert_category_01),
(@expert_02, @expert_category_02);

INSERT INTO expert_experiences (expert_id, experience_type, title, description, start_date, end_date, sort_order, created_by, updated_by, deleted)
VALUES
(@expert_01, 'work', '[TD]针灸门诊负责人', '负责针灸门诊带教、病例复盘和青年医师规范化培训。', '2015-01-01', NULL, 1, @admin_td_admin, @admin_td_admin, 0),
(@expert_01, 'achievement', '[TD]省级课题负责人', '主持省级针灸临床路径优化课题，形成院内带教标准化流程。', '2022-01-01', NULL, 2, @admin_td_admin, @admin_td_admin, 0),
(@expert_02, 'education', '[TD]中医方剂学博士', '系统研究补益剂与和解剂配伍规律，用于课程和直播答疑内容设计。', '2008-09-01', '2011-06-30', 1, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO live_sessions (title, cover_url, anchor_name, speaker_name, live_url, playback_url, start_at, end_at, review_status, live_status, created_by, updated_by, deleted)
VALUES
('[TD]针灸实操直播', 'https://example.com/assets/td/live-cover-1.jpg', '陈景岐', '陈景岐', 'https://example.com/live/td-1', 'https://example.com/live/td-1/playback', NOW() + INTERVAL 2 DAY, NOW() + INTERVAL 2 DAY + INTERVAL 2 HOUR, 2, 0, @admin_td_admin, @admin_td_admin, 0),
('[TD]方剂答疑直播', 'https://example.com/assets/td/live-cover-2.jpg', '宋本草', '宋本草', 'https://example.com/live/td-2', NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 1 HOUR, 2, 2, @admin_td_admin, @admin_td_admin, 0),
('[TD]门诊病例晨会', 'https://example.com/assets/td/live-cover-3.jpg', '赵明岚', '赵明岚', 'https://example.com/live/td-3', 'https://example.com/live/td-3/playback', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 45 MINUTE, 2, 1, @admin_td_admin, @admin_td_admin, 0),
('[TD]耳穴压豆答疑场', 'https://example.com/assets/td/live-cover-4.jpg', '何循经', '何循经', 'https://example.com/live/td-4', NULL, NOW() + INTERVAL 5 DAY, NOW() + INTERVAL 5 DAY + INTERVAL 90 MINUTE, 2, 0, @admin_td_admin, @admin_td_admin, 0),
('[TD]暑期带教预备会', 'https://example.com/assets/td/live-cover-5.jpg', '陈思远', '陈思远', 'https://example.com/live/td-5', NULL, NOW() + INTERVAL 7 DAY, NOW() + INTERVAL 7 DAY + INTERVAL 60 MINUTE, 1, 0, @admin_td_admin, @admin_td_admin, 0);

SET @live_01 = (SELECT id FROM live_sessions WHERE title = '[TD]针灸实操直播' AND deleted = 0 LIMIT 1);
SET @live_02 = (SELECT id FROM live_sessions WHERE title = '[TD]方剂答疑直播' AND deleted = 0 LIMIT 1);

INSERT INTO live_session_videos (live_session_id, title, video_url, duration_seconds, sort_order, status, created_by, updated_by, deleted)
VALUES
(@live_01, '[TD]肩颈常用取穴演示', 'https://example.com/live/td-1/clip-1.mp4', 1260, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@live_01, '[TD]进针角度与补泻手法', 'https://example.com/live/td-1/clip-2.mp4', 1540, 2, 1, @admin_td_admin, @admin_td_admin, 0),
(@live_02, '[TD]四物汤加减思路复盘', 'https://example.com/live/td-2/replay-1.mp4', 1980, 1, 1, @admin_td_admin, @admin_td_admin, 0),
((SELECT id FROM live_sessions WHERE title = '[TD]门诊病例晨会' AND deleted = 0 LIMIT 1), '[TD]腰痛病例复盘', 'https://example.com/live/td-3/replay-1.mp4', 1680, 1, 1, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO home_contents (category_id, content_type, target_id, title, cover_url, link_url, sort_order, start_at, end_at, status, created_by, updated_by, deleted)
VALUES
(@home_category_01, 'course', @course_01, '[TD]首页课程推荐', 'https://example.com/assets/td/home-course.jpg', NULL, 1, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, @admin_td_admin, @admin_td_admin, 0),
(@home_category_01, 'book', @book_01, '[TD]首页图书推荐', 'https://example.com/assets/td/home-book.jpg', NULL, 2, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, @admin_td_admin, @admin_td_admin, 0),
(@home_category_01, 'live', @live_01, '[TD]首页直播预告', 'https://example.com/assets/td/home-live.jpg', NULL, 3, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 7 DAY, 1, @admin_td_admin, @admin_td_admin, 0),
(@home_category_02, 'topic', @topic_01, '[TD]首页专题推荐', 'https://example.com/assets/td/home-topic.jpg', NULL, 1, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, @admin_td_admin, @admin_td_admin, 0),
(@home_category_02, 'topic', @topic_02, '[TD]首页带教专题', 'https://example.com/assets/td/home-topic-2.jpg', NULL, 2, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 20 DAY, 1, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO resource_tags (tag_id, resource_type, resource_id)
VALUES
(@tag_course, 'course', @course_01),
(@tag_book, 'book', @book_01),
(@tag_expert, 'expert', @expert_01),
(@tag_article, 'article', @article_01),
(@tag_podcast, 'podcast', @podcast_01),
(@tag_live, 'live', @live_01),
(@tag_live, 'live', @live_02);

INSERT INTO entity_extensions (owner_type, owner_id, field_key, field_value, value_type, created_by, updated_by, deleted)
VALUES
('td_course', @course_01, 'difficulty_label', '初阶实训', 'string', @admin_td_admin, @admin_td_admin, 0),
('td_book', @book_01, 'source_edition', '2026测试版', 'string', @admin_td_admin, @admin_td_admin, 0),
('td_student', @student_01, 'training_focus', '针灸临床', 'string', @admin_td_admin, @admin_td_admin, 0);

-- 互动与统计联调数据
INSERT INTO qa_questions (student_id, user_id, expert_category_id, expert_id, title, content, status, created_by, updated_by, deleted)
VALUES
(@student_01, @user_01, @expert_category_01, @expert_01, '[TD]艾灸后局部发红是否正常？', '测试答疑问题内容一。', 1, @admin_td_admin, @admin_td_admin, 0),
(@student_02, @user_02, @expert_category_02, @expert_02, '[TD]四物汤与八珍汤如何区分？', '测试答疑问题内容二。', 0, @admin_td_admin, @admin_td_admin, 0),
(@student_04, @user_04, @expert_category_01, @expert_01, '[TD]肩井穴进针角度如何把握？', '患者体型偏瘦，肩井穴进针时担心局部不适，想确认安全角度和针深。', 0, @admin_td_admin, @admin_td_admin, 0),
(@student_05, @user_05, @expert_category_01, @expert_01, '[TD]督脉灸后出现轻度疲乏怎么办？', '完成督脉灸后出现短时疲乏和轻微口渴，是否需要调整灸量。', 1, @admin_td_admin, @admin_td_admin, 0),
(@student_06, @user_06, @expert_category_02, @expert_02, '[TD]脾虚湿盛证如何区分基础方？', '门诊常见纳差乏力夹湿困表现，想确认香砂六君子汤与参苓白术散的选用边界。', 2, @admin_td_admin, @admin_td_admin, 0);

SET @qa_question_01 = (SELECT id FROM qa_questions WHERE title = '[TD]艾灸后局部发红是否正常？' AND deleted = 0 LIMIT 1);

INSERT INTO qa_answers (question_id, admin_id, expert_id, content, answered_at, deleted)
VALUES
(@qa_question_01, @admin_td_admin, @expert_01, '局部轻微发红一般属于正常反应，需结合灸感与持续时间观察。', NOW() - INTERVAL 1 DAY, 0);

INSERT INTO feedbacks (user_id, student_id, feedback_type, content, contact, status, processed_by, processed_at, process_note, created_by, updated_by, deleted)
VALUES
(@user_01, @student_01, 'td-seed', '测试反馈：希望增加针灸案例演示。', 'td_user_01@example.com', 1, @admin_td_admin, NOW() - INTERVAL 2 DAY, '已记录到课程优化池。', @admin_td_admin, @admin_td_admin, 0),
(@user_03, @student_03, 'td-seed', '测试反馈：移动端播放有轻微卡顿。', 'td_user_03@example.com', 0, NULL, NULL, NULL, @admin_td_admin, @admin_td_admin, 0),
(@user_04, @student_04, 'td-seed', '课程列表希望增加“最近学习”排序，方便快速回到上次进度。', 'td_user_04@example.com', 0, NULL, NULL, NULL, @admin_td_admin, @admin_td_admin, 0),
(@user_05, @student_05, 'td-seed', '直播回放建议补章节切点，方便复习督脉灸准备流程。', 'td_user_05@example.com', 1, @admin_td_admin, NOW() - INTERVAL 1 DAY, '已转交直播运营补充回放目录。', @admin_td_admin, @admin_td_admin, 0),
(@user_06, @student_06, 'td-seed', '知识库检索命中后希望高亮关键词。', 'td_user_06@example.com', 1, @admin_td_admin, NOW() - INTERVAL 12 HOUR, '已纳入知识库检索优化需求。', @admin_td_admin, @admin_td_admin, 0),
(@user_07, @student_07, 'td-seed', '学员认证驳回后建议在个人中心展示更明确的补交指引。', 'td_user_07@example.com', 0, NULL, NULL, NULL, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO learning_records (student_id, resource_type, resource_id, study_seconds, progress_percent, completed, completed_at, last_studied_at, updated_at)
VALUES
(@student_01, 'course', @course_01, 3600, 100.00, 1, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(@student_02, 'course', @course_01, 2100, 62.50, 0, NULL, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(@student_02, 'book', @book_01, 1800, 48.00, 0, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(@student_03, 'podcast', @podcast_01, 1500, 83.33, 0, NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(@student_01, 'live', @live_01, 4200, 100.00, 1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(@student_04, 'course', @course_04, 1260, 41.00, 0, NULL, NOW() - INTERVAL 9 HOUR, NOW() - INTERVAL 9 HOUR),
(@student_05, 'live', @live_02, 1980, 100.00, 1, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(@student_06, 'book', @book_03, 960, 36.00, 0, NULL, NOW() - INTERVAL 7 HOUR, NOW() - INTERVAL 7 HOUR),
(@student_07, 'podcast', @podcast_02, 540, 60.00, 0, NULL, NOW() - INTERVAL 5 HOUR, NOW() - INTERVAL 5 HOUR);

INSERT INTO exam_records (student_id, paper_id, source_type, source_id, score, passed, started_at, submitted_at, created_at)
VALUES
(@student_01, @paper_01, 'td-seed', @course_01, 18.00, 1, NOW() - INTERVAL 5 DAY - INTERVAL 30 MINUTE, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(@student_02, @paper_01, 'td-seed', @book_chapter_01, 11.00, 0, NOW() - INTERVAL 2 DAY - INTERVAL 40 MINUTE, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY);

SET @exam_record_01 = (SELECT id FROM exam_records WHERE student_id = @student_01 AND source_type = 'td-seed' ORDER BY id DESC LIMIT 1);
SET @exam_record_02 = (SELECT id FROM exam_records WHERE student_id = @student_02 AND source_type = 'td-seed' ORDER BY id DESC LIMIT 1);

INSERT INTO exam_record_answers (exam_record_id, question_id, answer_content, score, correct, created_at)
VALUES
(@exam_record_01, @question_01, 'A', 5.00, 1, NOW() - INTERVAL 5 DAY),
(@exam_record_01, @question_02, 'A,C,D', 8.00, 0, NOW() - INTERVAL 5 DAY),
(@exam_record_01, @question_03, 'A', 5.00, 1, NOW() - INTERVAL 5 DAY),
(@exam_record_02, @question_01, 'B', 0.00, 0, NOW() - INTERVAL 2 DAY),
(@exam_record_02, @question_03, 'A', 5.00, 1, NOW() - INTERVAL 2 DAY);

INSERT INTO user_favorites (user_id, resource_type, resource_id)
VALUES
(@user_01, 'article', @article_01),
(@user_01, 'topic', @topic_01),
(@user_01, 'live', @live_01),
(@user_02, 'course', @course_01),
(@user_02, 'live', @live_02),
(@user_04, 'course', @course_04),
(@user_05, 'book', @book_03),
(@user_06, 'topic', @topic_02);

INSERT INTO user_browse_histories (user_id, resource_type, resource_id, source, view_count, viewed_at)
VALUES
(@user_01, 'article', @article_01, 'home', 3, NOW() - INTERVAL 1 DAY),
(@user_01, 'topic', @topic_01, 'search', 2, NOW() - INTERVAL 12 HOUR),
(@user_01, 'live', @live_01, 'home', 1, NOW() - INTERVAL 3 HOUR),
(@user_02, 'course', @course_01, 'topic', 5, NOW() - INTERVAL 6 HOUR),
(@user_02, 'live', @live_02, 'detail', 2, NOW() - INTERVAL 5 HOUR),
(@user_03, 'book', @book_01, 'detail', 1, NOW() - INTERVAL 2 HOUR),
(@user_04, 'course', @course_04, 'home', 4, NOW() - INTERVAL 90 MINUTE),
(@user_05, 'topic', @topic_02, 'home', 3, NOW() - INTERVAL 50 MINUTE),
(@user_06, 'live', @live_01, 'detail', 2, NOW() - INTERVAL 40 MINUTE),
(@user_07, 'article', (SELECT id FROM articles WHERE title = '[TD]三伏贴门诊安排说明' AND deleted = 0 LIMIT 1), 'banner', 1, NOW() - INTERVAL 30 MINUTE);

INSERT INTO user_share_records (user_id, resource_type, resource_id, share_channel)
VALUES
(@user_01, 'article', @article_01, 'wechat_session'),
(@user_02, 'topic', @topic_01, 'wechat_timeline'),
(@user_02, 'live', @live_01, 'wechat_session'),
(@user_03, 'course', @course_01, 'link'),
(@user_05, 'topic', @topic_02, 'wechat_session'),
(@user_06, 'book', @book_03, 'link');

INSERT INTO knowledge_categories (parent_id, category_name, category_code, description, sort_order, status, created_by, updated_by, deleted)
VALUES
(NULL, '[TD]中医基础理论', 'TD_KNOW_BASE', '测试知识库一级分类', 1, 1, @admin_td_admin, @admin_td_admin, 0),
(NULL, '[TD]方剂学', 'TD_KNOW_FORMULA', '测试知识库一级分类', 2, 1, @admin_td_admin, @admin_td_admin, 0);

SET @knowledge_category_01 = (SELECT id FROM knowledge_categories WHERE category_code = 'TD_KNOW_BASE' AND deleted = 0 LIMIT 1);
SET @knowledge_category_02 = (SELECT id FROM knowledge_categories WHERE category_code = 'TD_KNOW_FORMULA' AND deleted = 0 LIMIT 1);

INSERT INTO knowledge_entries (category_id, title, summary, cover_url, content, keywords, source, review_status, publish_status, sort_order, published_at, view_count, created_by, updated_by, deleted)
VALUES
(@knowledge_category_01, '[TD]阴阳学说概览', '从临床常见寒热、虚实表现切入，快速回顾阴阳消长与转化。', 'https://example.com/assets/td/knowledge-cover-1.jpg', '<p>阴阳学说是理解脏腑、经络和病机变化的基础。</p><p>本条目用于联调知识库分类、详情与搜索高亮展示。</p>', '阴阳,基础理论', '测试资料库', 2, 1, 1, NOW() - INTERVAL 8 DAY, 156, @admin_td_admin, @admin_td_admin, 0),
(@knowledge_category_02, '[TD]四物汤配伍要点', '梳理四物汤在补血、调经与临床加减中的核心使用场景。', 'https://example.com/assets/td/knowledge-cover-2.jpg', '<p>四物汤由熟地黄、当归、白芍、川芎组成，是补血调血常用基础方。</p><p>条目内容可用于联调知识库详情页、搜索页和收藏统计场景。</p>', '四物汤,方剂学', '测试资料库', 2, 1, 2, NOW() - INTERVAL 7 DAY, 132, @admin_td_admin, @admin_td_admin, 0),
(@knowledge_category_01, '[TD]经络辨证速查表', '汇总头面、肩背、腰腿常见症状的经络归属与取穴思路。', 'https://example.com/assets/td/knowledge-cover-3.jpg', '<p>适合作为课程与直播后的速查资料，也可用于搜索联调。</p>', '经络,辨证', '门诊资料库', 2, 1, 3, NOW() - INTERVAL 5 DAY, 97, @admin_td_admin, @admin_td_admin, 0),
(@knowledge_category_02, '[TD]参苓白术散应用场景', '整理脾虚夹湿、久泻纳差与体倦乏力的辨证要点。', 'https://example.com/assets/td/knowledge-cover-4.jpg', '<p>条目用于补充知识库列表、详情和排序数据量。</p>', '参苓白术散,脾虚湿盛', '门诊资料库', 2, 1, 4, NOW() - INTERVAL 4 DAY, 88, @admin_td_admin, @admin_td_admin, 0);

SET @knowledge_entry_01 = (SELECT id FROM knowledge_entries WHERE title = '[TD]阴阳学说概览' AND deleted = 0 LIMIT 1);
SET @knowledge_entry_02 = (SELECT id FROM knowledge_entries WHERE title = '[TD]四物汤配伍要点' AND deleted = 0 LIMIT 1);
SET @knowledge_entry_03 = (SELECT id FROM knowledge_entries WHERE title = '[TD]经络辨证速查表' AND deleted = 0 LIMIT 1);

INSERT INTO user_favorites (user_id, resource_type, resource_id)
VALUES
(@user_03, 'knowledge', @knowledge_entry_01),
(@user_05, 'knowledge', @knowledge_entry_03);

INSERT INTO user_browse_histories (user_id, resource_type, resource_id, source, view_count, viewed_at)
VALUES
(@user_03, 'knowledge', @knowledge_entry_01, 'search', 2, NOW() - INTERVAL 90 MINUTE),
(@user_02, 'knowledge', @knowledge_entry_02, 'detail', 1, NOW() - INTERVAL 70 MINUTE),
(@user_04, 'knowledge', @knowledge_entry_03, 'search', 3, NOW() - INTERVAL 20 MINUTE);

INSERT INTO audit_records (target_type, target_id, before_status, after_status, audit_comment, auditor_id, audited_at, created_at)
VALUES
('td_course_review', @course_02, 1, 2, '测试课程审核通过', @admin_td_admin, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
('td_article_review', @article_02, 1, 2, '测试资讯审核通过', @admin_td_admin, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
('td_feedback_process', 0, 0, 1, '测试反馈已处理', @admin_td_admin, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY);
