-- 幂等测试数据脚本
-- 约定：
-- 1. 所有测试数据统一使用 td_、TD_ 或 [TD] 前缀，便于重复执行和清理。
-- 2. 登录管理员：
--    username = td_admin / password = Admin@123456
--    username = td_viewer / password = Admin@123456

SET NAMES utf8mb4;

DELETE FROM exam_record_answers
WHERE exam_record_id IN (
    SELECT id FROM exam_records WHERE source_type = 'td-seed'
);

DELETE FROM exam_records WHERE source_type = 'td-seed';
DELETE FROM learning_records WHERE resource_type IN ('td-course', 'td-book', 'td-podcast', 'td-live');
DELETE FROM qa_answers WHERE question_id IN (SELECT id FROM qa_questions WHERE title LIKE '[TD]%');
DELETE FROM qa_questions WHERE title LIKE '[TD]%';
DELETE FROM feedbacks WHERE feedback_type = 'td-seed';
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
DELETE FROM resource_tags WHERE resource_type LIKE 'td_%';
DELETE FROM tags WHERE tag_name LIKE '[TD]%';
DELETE FROM file_assets WHERE object_key LIKE 'td/%';
DELETE FROM audit_records WHERE target_type LIKE 'td_%';
DELETE FROM students WHERE student_no LIKE 'TD-%';
DELETE FROM app_users WHERE username LIKE 'td_%';
DELETE FROM sys_admin_roles WHERE admin_id IN (SELECT id FROM sys_admins WHERE username IN ('td_admin', 'td_viewer'));
DELETE FROM sys_admins WHERE username IN ('td_admin', 'td_viewer');
DELETE FROM sys_role_permissions WHERE role_id IN (SELECT id FROM sys_roles WHERE role_code IN ('TD_CONTENT_EDITOR', 'TD_READONLY'));
DELETE FROM sys_roles WHERE role_code IN ('TD_CONTENT_EDITOR', 'TD_READONLY');
DELETE FROM sys_permissions WHERE permission_code IN (
    'auth:me', 'auth:logout', 'auth:status',
    'sys:admin:view', 'sys:admin:create', 'sys:admin:update', 'sys:admin:disable', 'sys:admin:reset-password',
    'course:view', 'course:review', 'book:view', 'article:review', 'topic:view', 'expert:view'
);

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
SELECT NULL, 'sys:admin:view', '查看管理员', 3, NULL, 'GET', '/api/v1/system/admins', NULL, 10, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'sys:admin:view' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'sys:admin:create', '创建管理员', 3, NULL, 'POST', '/api/v1/system/admins', NULL, 11, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'sys:admin:create' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'sys:admin:update', '更新管理员', 3, NULL, 'PUT', '/api/v1/system/admins/{id}', NULL, 12, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'sys:admin:update' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'sys:admin:disable', '禁用管理员', 3, NULL, 'PATCH', '/api/v1/system/admins/{id}/status', NULL, 13, 1, 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM sys_permissions WHERE permission_code = 'sys:admin:disable' AND deleted = 0);

INSERT INTO sys_permissions (parent_id, permission_code, permission_name, permission_type, route_path, api_method, api_path, icon, sort_order, status, created_by, updated_by, deleted)
SELECT NULL, 'sys:admin:reset-password', '重置管理员密码', 3, NULL, 'PATCH', '/api/v1/system/admins/{id}/password/reset', NULL, 14, 1, 0, 0, 0
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
('[TD]名师', 'expert', '#D97706', 1, @admin_td_admin, @admin_td_admin, 0);

SET @tag_course = (SELECT id FROM tags WHERE tag_name = '[TD]针灸' AND deleted = 0 LIMIT 1);
SET @tag_book = (SELECT id FROM tags WHERE tag_name = '[TD]经典' AND deleted = 0 LIMIT 1);
SET @tag_expert = (SELECT id FROM tags WHERE tag_name = '[TD]名师' AND deleted = 0 LIMIT 1);

INSERT INTO app_users (username, mobile, email, nickname, avatar_url, gender, status, registered_at, last_login_at, created_by, updated_by, deleted)
VALUES
('td_user_01', '13900000001', 'td_user_01@example.com', '杏林新苗', 'https://example.com/assets/td/user-1.png', 1, 1, NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 1 DAY, @admin_td_admin, @admin_td_admin, 0),
('td_user_02', '13900000002', 'td_user_02@example.com', '岐黄同道', 'https://example.com/assets/td/user-2.png', 2, 1, NOW() - INTERVAL 28 DAY, NOW() - INTERVAL 2 DAY, @admin_td_admin, @admin_td_admin, 0),
('td_user_03', '13900000003', 'td_user_03@example.com', '本草随行', 'https://example.com/assets/td/user-3.png', 1, 1, NOW() - INTERVAL 21 DAY, NOW() - INTERVAL 3 DAY, @admin_td_admin, @admin_td_admin, 0);

SET @user_01 = (SELECT id FROM app_users WHERE username = 'td_user_01' AND deleted = 0 LIMIT 1);
SET @user_02 = (SELECT id FROM app_users WHERE username = 'td_user_02' AND deleted = 0 LIMIT 1);
SET @user_03 = (SELECT id FROM app_users WHERE username = 'td_user_03' AND deleted = 0 LIMIT 1);

INSERT INTO students (user_id, student_no, real_name, mobile, id_card_no, province, city, district, organization, position_title, status, enrolled_at, created_by, updated_by, deleted)
VALUES
(@user_01, 'TD-STU-001', '张青云', '13900000001', '110101199001010011', '浙江省', '杭州市', '西湖区', '国医馆一部', '住院医师', 1, NOW() - INTERVAL 20 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_02, 'TD-STU-002', '李若水', '13900000002', '110101199202020022', '江苏省', '苏州市', '姑苏区', '针灸研究所', '主治医师', 1, NOW() - INTERVAL 18 DAY, @admin_td_admin, @admin_td_admin, 0),
(@user_03, 'TD-STU-003', '王知秋', '13900000003', '110101199303030033', '四川省', '成都市', '高新区', '中医药大学', '讲师', 1, NOW() - INTERVAL 15 DAY, @admin_td_admin, @admin_td_admin, 0);

SET @student_01 = (SELECT id FROM students WHERE student_no = 'TD-STU-001' AND deleted = 0 LIMIT 1);
SET @student_02 = (SELECT id FROM students WHERE student_no = 'TD-STU-002' AND deleted = 0 LIMIT 1);
SET @student_03 = (SELECT id FROM students WHERE student_no = 'TD-STU-003' AND deleted = 0 LIMIT 1);

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

INSERT INTO courses (course_name, subtitle, cover_url, lecturer_name, introduction, review_status, publish_status, paper_id, sort_order, published_at, created_by, updated_by, deleted)
VALUES
('[TD]经络腧穴速学', '测试课程一', 'https://example.com/assets/td/course-cover-1.jpg', '陈思远', '测试课程简介，覆盖审核、发布和学习记录场景。', 2, 1, @paper_01, 1, NOW() - INTERVAL 10 DAY, @admin_td_admin, @admin_td_admin, 0),
('[TD]针灸临床入门', '测试课程二', 'https://example.com/assets/td/course-cover-2.jpg', '周岐黄', '测试课程简介二。', 1, 0, @paper_01, 2, NULL, @admin_td_admin, @admin_td_admin, 0);

SET @course_01 = (SELECT id FROM courses WHERE course_name = '[TD]经络腧穴速学' AND deleted = 0 LIMIT 1);
SET @course_02 = (SELECT id FROM courses WHERE course_name = '[TD]针灸临床入门' AND deleted = 0 LIMIT 1);

INSERT INTO course_videos (course_id, title, video_url, duration_seconds, sort_order, status, created_by, updated_by, deleted)
VALUES
(@course_01, '[TD]经络总论', 'https://example.com/assets/td/course-video-1.mp4', 900, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@course_01, '[TD]常用腧穴', 'https://example.com/assets/td/course-video-2.mp4', 1200, 2, 1, @admin_td_admin, @admin_td_admin, 0),
(@course_02, '[TD]临床案例导读', 'https://example.com/assets/td/course-video-3.mp4', 1500, 1, 1, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO book_categories (parent_id, category_name, sort_order, status, created_by, updated_by, deleted)
VALUES
(NULL, '[TD]针灸教材', 1, 1, @admin_td_admin, @admin_td_admin, 0),
(NULL, '[TD]经典方剂', 2, 1, @admin_td_admin, @admin_td_admin, 0);

SET @book_category_01 = (SELECT id FROM book_categories WHERE category_name = '[TD]针灸教材' AND deleted = 0 LIMIT 1);
SET @book_category_02 = (SELECT id FROM book_categories WHERE category_name = '[TD]经典方剂' AND deleted = 0 LIMIT 1);

INSERT INTO books (category_id, book_name, author, publisher, cover_url, introduction, review_status, publish_status, paper_id, sort_order, published_at, created_by, updated_by, deleted)
VALUES
(@book_category_01, '[TD]针灸学临证读本', '林问岐', '中医古籍出版社', 'https://example.com/assets/td/book-cover-1.jpg', '测试图书简介一。', 2, 1, @paper_01, 1, NOW() - INTERVAL 14 DAY, @admin_td_admin, @admin_td_admin, 0),
(@book_category_02, '[TD]方剂辨治精要', '许本草', '人民卫生出版社', 'https://example.com/assets/td/book-cover-2.jpg', '测试图书简介二。', 1, 0, @paper_01, 2, NULL, @admin_td_admin, @admin_td_admin, 0);

SET @book_01 = (SELECT id FROM books WHERE book_name = '[TD]针灸学临证读本' AND deleted = 0 LIMIT 1);
SET @book_02 = (SELECT id FROM books WHERE book_name = '[TD]方剂辨治精要' AND deleted = 0 LIMIT 1);

INSERT INTO book_chapters (book_id, parent_id, chapter_title, content, paper_id, sort_order, status, created_by, updated_by, deleted)
VALUES
(@book_01, NULL, '[TD]第一章 经络基础', '测试章节内容一。', @paper_01, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@book_01, NULL, '[TD]第二章 常用腧穴', '测试章节内容二。', NULL, 2, 1, @admin_td_admin, @admin_td_admin, 0),
(@book_02, NULL, '[TD]第一章 补益方总论', '测试章节内容三。', NULL, 1, 1, @admin_td_admin, @admin_td_admin, 0);

SET @book_chapter_01 = (SELECT id FROM book_chapters WHERE chapter_title = '[TD]第一章 经络基础' AND deleted = 0 LIMIT 1);

INSERT INTO articles (title, summary, cover_url, content, author_name, review_status, publish_status, published_at, view_count, created_by, updated_by, deleted)
VALUES
('[TD]春季养肝调气指南', '测试资讯摘要一', 'https://example.com/assets/td/article-cover-1.jpg', '<p>测试资讯正文一</p>', '编辑部', 2, 1, NOW() - INTERVAL 6 DAY, 120, @admin_td_admin, @admin_td_admin, 0),
('[TD]针灸门诊带教札记', '测试资讯摘要二', 'https://example.com/assets/td/article-cover-2.jpg', '<p>测试资讯正文二</p>', '陈思远', 1, 0, NULL, 38, @admin_td_admin, @admin_td_admin, 0);

SET @article_01 = (SELECT id FROM articles WHERE title = '[TD]春季养肝调气指南' AND deleted = 0 LIMIT 1);
SET @article_02 = (SELECT id FROM articles WHERE title = '[TD]针灸门诊带教札记' AND deleted = 0 LIMIT 1);

INSERT INTO podcasts (title, summary, cover_url, review_status, publish_status, published_at, sort_order, created_by, updated_by, deleted)
VALUES
('[TD]黄帝内经夜读', '测试播客摘要一', 'https://example.com/assets/td/podcast-cover-1.jpg', 2, 1, NOW() - INTERVAL 5 DAY, 1, @admin_td_admin, @admin_td_admin, 0),
('[TD]本草问答录', '测试播客摘要二', 'https://example.com/assets/td/podcast-cover-2.jpg', 1, 0, NULL, 2, @admin_td_admin, @admin_td_admin, 0);

SET @podcast_01 = (SELECT id FROM podcasts WHERE title = '[TD]黄帝内经夜读' AND deleted = 0 LIMIT 1);

INSERT INTO podcast_audios (podcast_id, title, audio_url, duration_seconds, sort_order, status, created_by, updated_by, deleted)
VALUES
(@podcast_01, '[TD]第一期 经络循行', 'https://example.com/assets/td/podcast-audio-1.mp3', 1800, 1, 1, @admin_td_admin, @admin_td_admin, 0),
(@podcast_01, '[TD]第二期 脏腑表里', 'https://example.com/assets/td/podcast-audio-2.mp3', 2100, 2, 1, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO topics (title, summary, cover_url, review_status, publish_status, published_at, sort_order, created_by, updated_by, deleted)
VALUES
('[TD]针灸临床专题', '测试专题摘要', 'https://example.com/assets/td/topic-cover-1.jpg', 2, 1, NOW() - INTERVAL 4 DAY, 1, @admin_td_admin, @admin_td_admin, 0);

SET @topic_01 = (SELECT id FROM topics WHERE title = '[TD]针灸临床专题' AND deleted = 0 LIMIT 1);

INSERT INTO topic_items (topic_id, item_type, item_id, sort_order)
VALUES
(@topic_01, 'course', @course_01, 1),
(@topic_01, 'book', @book_01, 2),
(@topic_01, 'article', @article_01, 3),
(@topic_01, 'podcast', @podcast_01, 4),
(@topic_01, 'question', @question_01, 5);

INSERT INTO expert_categories (parent_id, category_name, sort_order, status, created_by, updated_by, deleted)
VALUES
(NULL, '[TD]针灸专家', 1, 1, @admin_td_admin, @admin_td_admin, 0),
(NULL, '[TD]方剂专家', 2, 1, @admin_td_admin, @admin_td_admin, 0);

SET @expert_category_01 = (SELECT id FROM expert_categories WHERE category_name = '[TD]针灸专家' AND deleted = 0 LIMIT 1);
SET @expert_category_02 = (SELECT id FROM expert_categories WHERE category_name = '[TD]方剂专家' AND deleted = 0 LIMIT 1);

INSERT INTO experts (real_name, avatar_url, title, organization, specialty, introduction, status, sort_order, created_by, updated_by, deleted)
VALUES
('[TD]陈景岐', 'https://example.com/assets/td/expert-1.jpg', '主任医师', '省中医院', '针灸与经络', '测试专家简介一。', 1, 1, @admin_td_admin, @admin_td_admin, 0),
('[TD]宋本草', 'https://example.com/assets/td/expert-2.jpg', '教授', '中医药大学', '方剂配伍', '测试专家简介二。', 1, 2, @admin_td_admin, @admin_td_admin, 0);

SET @expert_01 = (SELECT id FROM experts WHERE real_name = '[TD]陈景岐' AND deleted = 0 LIMIT 1);
SET @expert_02 = (SELECT id FROM experts WHERE real_name = '[TD]宋本草' AND deleted = 0 LIMIT 1);

INSERT INTO expert_category_relations (expert_id, category_id)
VALUES
(@expert_01, @expert_category_01),
(@expert_02, @expert_category_02);

INSERT INTO expert_experiences (expert_id, experience_type, title, description, start_date, end_date, sort_order, created_by, updated_by, deleted)
VALUES
(@expert_01, 'work', '[TD]针灸门诊负责人', '测试工作履历。', '2015-01-01', NULL, 1, @admin_td_admin, @admin_td_admin, 0),
(@expert_01, 'achievement', '[TD]省级课题负责人', '测试成果履历。', '2022-01-01', NULL, 2, @admin_td_admin, @admin_td_admin, 0),
(@expert_02, 'education', '[TD]中医方剂学博士', '测试教育履历。', '2008-09-01', '2011-06-30', 1, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO live_sessions (title, cover_url, anchor_name, live_url, playback_url, start_at, end_at, review_status, live_status, created_by, updated_by, deleted)
VALUES
('[TD]针灸实操直播', 'https://example.com/assets/td/live-cover-1.jpg', '陈景岐', 'https://example.com/live/td-1', 'https://example.com/live/td-1/playback', NOW() + INTERVAL 2 DAY, NOW() + INTERVAL 2 DAY + INTERVAL 2 HOUR, 2, 0, @admin_td_admin, @admin_td_admin, 0),
('[TD]方剂答疑直播', 'https://example.com/assets/td/live-cover-2.jpg', '宋本草', 'https://example.com/live/td-2', NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 1 HOUR, 2, 2, @admin_td_admin, @admin_td_admin, 0);

SET @live_01 = (SELECT id FROM live_sessions WHERE title = '[TD]针灸实操直播' AND deleted = 0 LIMIT 1);

INSERT INTO home_contents (category_id, content_type, target_id, title, cover_url, link_url, sort_order, start_at, end_at, status, created_by, updated_by, deleted)
VALUES
(@home_category_01, 'course', @course_01, '[TD]首页课程推荐', 'https://example.com/assets/td/home-course.jpg', NULL, 1, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, @admin_td_admin, @admin_td_admin, 0),
(@home_category_01, 'book', @book_01, '[TD]首页图书推荐', 'https://example.com/assets/td/home-book.jpg', NULL, 2, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, @admin_td_admin, @admin_td_admin, 0),
(@home_category_02, 'topic', @topic_01, '[TD]首页专题推荐', 'https://example.com/assets/td/home-topic.jpg', NULL, 1, NOW() - INTERVAL 1 DAY, NOW() + INTERVAL 30 DAY, 1, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO resource_tags (tag_id, resource_type, resource_id)
VALUES
(@tag_course, 'td_course', @course_01),
(@tag_book, 'td_book', @book_01),
(@tag_expert, 'td_expert', @expert_01);

INSERT INTO entity_extensions (owner_type, owner_id, field_key, field_value, value_type, created_by, updated_by, deleted)
VALUES
('td_course', @course_01, 'difficulty_label', '初阶实训', 'string', @admin_td_admin, @admin_td_admin, 0),
('td_book', @book_01, 'source_edition', '2026测试版', 'string', @admin_td_admin, @admin_td_admin, 0),
('td_student', @student_01, 'training_focus', '针灸临床', 'string', @admin_td_admin, @admin_td_admin, 0);

INSERT INTO qa_questions (student_id, user_id, title, content, status, created_by, updated_by, deleted)
VALUES
(@student_01, @user_01, '[TD]艾灸后局部发红是否正常？', '测试答疑问题内容一。', 1, @admin_td_admin, @admin_td_admin, 0),
(@student_02, @user_02, '[TD]四物汤与八珍汤如何区分？', '测试答疑问题内容二。', 0, @admin_td_admin, @admin_td_admin, 0);

SET @qa_question_01 = (SELECT id FROM qa_questions WHERE title = '[TD]艾灸后局部发红是否正常？' AND deleted = 0 LIMIT 1);

INSERT INTO qa_answers (question_id, admin_id, content, answered_at, deleted)
VALUES
(@qa_question_01, @admin_td_admin, '局部轻微发红一般属于正常反应，需结合灸感与持续时间观察。', NOW() - INTERVAL 1 DAY, 0);

INSERT INTO feedbacks (user_id, student_id, feedback_type, content, contact, status, processed_by, processed_at, process_note, created_by, updated_by, deleted)
VALUES
(@user_01, @student_01, 'td-seed', '测试反馈：希望增加针灸案例演示。', 'td_user_01@example.com', 1, @admin_td_admin, NOW() - INTERVAL 2 DAY, '已记录到课程优化池。', @admin_td_admin, @admin_td_admin, 0),
(@user_03, @student_03, 'td-seed', '测试反馈：移动端播放有轻微卡顿。', 'td_user_03@example.com', 0, NULL, NULL, NULL, @admin_td_admin, @admin_td_admin, 0);

INSERT INTO learning_records (student_id, resource_type, resource_id, study_seconds, progress_percent, completed, completed_at, last_studied_at, updated_at)
VALUES
(@student_01, 'td-course', @course_01, 3600, 100.00, 1, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
(@student_02, 'td-course', @course_01, 2100, 62.50, 0, NULL, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
(@student_02, 'td-book', @book_01, 1800, 48.00, 0, NULL, NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
(@student_03, 'td-podcast', @podcast_01, 1500, 83.33, 0, NULL, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
(@student_01, 'td-live', @live_01, 4200, 100.00, 1, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY);

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

INSERT INTO audit_records (target_type, target_id, before_status, after_status, audit_comment, auditor_id, audited_at, created_at)
VALUES
('td_course_review', @course_02, 1, 2, '测试课程审核通过', @admin_td_admin, NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
('td_article_review', @article_02, 1, 2, '测试资讯审核通过', @admin_td_admin, NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
('td_feedback_process', 0, 0, 1, '测试反馈已处理', @admin_td_admin, NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY);
