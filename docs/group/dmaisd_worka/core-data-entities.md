# 核心数据实体清单

> 基于 `jsmedicine` 后端 Spring Boot 项目实体类整理，涵盖所有数据库表映射的 Java Entity。

---

## 1. 系统与权限（System / RBAC）

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `SysAdmin` | `sys_admins` | 后台管理员账号 | ManagedEntity |
| `SysRole` | `sys_roles` | 后台角色 | ManagedEntity |
| `SysPermission` | `sys_permissions` | 后台权限点（菜单/按钮/API） | ManagedEntity |
| `SysAdminRole` | `sys_admin_roles` | 管理员-角色关联表 | BaseEntity |
| `SysRolePermission` | `sys_role_permissions` | 角色-权限关联表 | BaseEntity |
| `AuditRecord` | `audit_records` | 操作审计/审核记录 | BaseEntity |

## 2. 用户与学员（User）

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `AppUser` | `app_users` | 用户端账号（含微信/手机绑定） | ManagedEntity |
| `AppUserIdentity` | `app_user_identities` | 用户业务身份（学员/专家） | ManagedEntity |
| `Student` | `students` | 学员档案（含认证信息） | ManagedEntity |
| `StudentCertificationFile` | `student_certification_files` | 学员认证材料文件 | ManagedEntity |
| `Organization` | `organizations` | 机构/单位 | ManagedEntity |
| `PracticeType` | `practice_types` | 执业类型 | ManagedEntity |

## 3. 内容管理（Content）

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `Article` | `articles` | 资讯文章 | ReviewableEntity |
| `HomeCategory` | `home_categories` | 首页分类 | ManagedEntity |
| `HomeContent` | `home_contents` | 首页内容配置 | ManagedEntity |
| `Topic` | `topics` | 专题 | ReviewableEntity |
| `TopicItem` | `topic_items` | 专题关联项（课程/图书/播客） | BaseEntity |

## 4. 学习资源（Learning）

### 4.1 课程

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `Course` | `courses` | 课程 | ReviewableEntity |
| `CourseVideo` | `course_videos` | 课程视频 | ManagedEntity |

### 4.2 图书

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `Book` | `books` | 图书 | ReviewableEntity |
| `BookCategory` | `book_categories` | 图书分类 | ManagedEntity |
| `BookChapter` | `book_chapters` | 图书章节 | ManagedEntity |

### 4.3 播客

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `Podcast` | `podcasts` | 播客 | ReviewableEntity |
| `PodcastAudio` | `podcast_audios` | 播客音频 | ManagedEntity |

### 4.4 直播

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `LiveSession` | `live_sessions` | 直播场次 | ManagedEntity |
| `LiveSessionVideo` | `live_session_videos` | 直播回放视频 | ManagedEntity |

### 4.5 题库与考试

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `QuestionCategory` | `question_categories` | 题目分类 | ManagedEntity |
| `Question` | `questions` | 题目 | ManagedEntity |
| `QuestionOption` | `question_options` | 题目选项 | BaseEntity |
| `ExamPaper` | `exam_papers` | 考卷 | ManagedEntity |
| `ExamPaperQuestion` | `exam_paper_questions` | 考卷-题目关联 | BaseEntity |

### 4.6 学习记录

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `LearningRecord` | `learning_records` | 学习进度记录 | BaseEntity |
| `ExamRecord` | `exam_records` | 考试记录 | BaseEntity |
| `ExamRecordAnswer` | `exam_record_answers` | 考试答题记录 | BaseEntity |

## 5. 专家（Expert）

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `Expert` | `experts` | 专家资料 | ManagedEntity |
| `ExpertCategory` | `expert_categories` | 专家分类（两级） | ManagedEntity |
| `ExpertCategoryRelation` | `expert_category_relations` | 专家-分类关联 | BaseEntity |
| `ExpertExperience` | `expert_experiences` | 专家履历 | ManagedEntity |

## 6. 互动（Interaction）

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `QaQuestion` | `qa_questions` | 答疑/咨询问题 | ManagedEntity |
| `QaAnswer` | `qa_answers` | 答疑回复 | BaseEntity |
| `Feedback` | `feedbacks` | 用户反馈 | ManagedEntity |
| `UserFavorite` | `user_favorites` | 用户收藏 | BaseEntity |
| `UserBrowseHistory` | `user_browse_histories` | 用户浏览记录 | BaseEntity |
| `UserShareRecord` | `user_share_records` | 用户分享记录 | BaseEntity |

## 7. 知识库（Knowledge）

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `KnowledgeCategory` | `knowledge_categories` | 知识库分类 | ManagedEntity |
| `KnowledgeEntry` | `knowledge_entries` | 知识库条目 | ReviewableEntity |

## 8. 通用扩展（Common）

| 实体 | 表名 | 说明 | 继承 |
|------|------|------|------|
| `FileAsset` | `file_assets` | 文件资产元数据 | ManagedEntity |
| `Tag` | `tags` | 标签 | ManagedEntity |
| `ResourceTag` | `resource_tags` | 资源-标签关联 | BaseEntity |
| `EntityExtension` | `entity_extensions` | 实体扩展字段 | BaseEntity |

---

## 基类继承结构

```
BaseEntity
├── id (Long, auto-increment)
├── createdAt (LocalDateTime)
│
├── ManagedEntity (extends BaseEntity)
│   ├── createdBy (Long)
│   ├── updatedBy (Long)
│   ├── updatedAt (LocalDateTime)
│   ├── deleted (Boolean, 逻辑删除)
│   │
│   └── ReviewableEntity (extends ManagedEntity)
│       ├── reviewStatus (ReviewStatus)
│       ├── publishStatus (PublishStatus)
│       └── publishedAt (LocalDateTime)
```

## 模块与表映射

| 模块 | 表数量 | 核心表 |
|------|--------|--------|
| system | 6 | sys_admins, sys_roles, sys_permissions, audit_records |
| user | 6 | app_users, students, organizations, practice_types |
| content | 5 | articles, home_categories, home_contents, topics, topic_items |
| learning | 16 | courses, books, book_chapters, podcasts, questions, exam_papers, learning_records, live_sessions |
| expert | 4 | experts, expert_categories, expert_experiences |
| interaction | 6 | qa_questions, qa_answers, feedbacks, user_favorites |
| knowledge | 2 | knowledge_categories, knowledge_entries |
| common | 4 | file_assets, tags, entity_extensions |
