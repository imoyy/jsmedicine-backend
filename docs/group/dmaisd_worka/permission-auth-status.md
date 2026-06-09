# 权限鉴权与状态字段说明

---

## 一、认证体系

### 1.1 双端隔离认证

项目采用管理端与用户端完全隔离的 Bearer Token 会话方案，不使用 JWT。

| 维度 | 管理端（Admin） | 用户端（App） |
|------|----------------|---------------|
| Token 前缀 | `admin:token:` | `app:user:token:` |
| Token 生成 | UUID（无横线） | UUID（无横线） |
| 存储介质 | Redis（JSON 序列化） | Redis（JSON 序列化） |
| Session 类型 | `AdminSession` | `AppUserSession` |
| 认证过滤器 | `BearerTokenAuthenticationFilter` | 同一过滤器，先尝试 admin 再尝试 app |
| 权限模型 | RBAC（角色+权限码） | 单一 `ROLE_APP_USER` |
| 接口前缀 | `/api/v1/auth/` `/api/v1/admin/` | `/api/v1/app/` |

### 1.2 认证流程

```
请求 → BearerTokenAuthenticationFilter
  ├─ 提取 Authorization: Bearer <token>
  ├─ 尝试 AdminTokenService.getSession(token)
  │   ├─ 成功 → 设置 Authentication（含 roleCodes + permissionCodes）
  │   └─ 失败 → 继续
  ├─ 尝试 AppUserTokenService.getSession(token)
  │   ├─ 成功 → 设置 Authentication（ROLE_APP_USER）
  │   └─ 失败 → 返回 401
  └─ 请求继续到对应的 Security 配置
```

### 1.3 公开端点（无需认证）

- `POST /api/v1/auth/login`
- `POST /api/v1/app/auth/login`
- `POST /api/v1/app/auth/sms-code`
- `POST /api/v1/app/auth/sms-login`
- `POST /api/v1/app/auth/wechat-login`
- `GET /api/v1/app/auth/wechat-web/qr-config`
- `POST /api/v1/app/auth/wechat-web/login`
- `POST /api/v1/app/auth/wechat-bind-mobile`
- `GET /api/v1/files/{id}/content`
- `POST /api/v1/integrations/srs/live-hooks`
- Swagger UI `/swagger-ui/**`, `/api/docs`

---

## 二、管理端 RBAC 权限模型

### 2.1 模型结构

```
SysAdmin ──── N:N ──── SysRole ──── N:N ──── SysPermission
    │                    │
  sys_admin_roles    sys_role_permissions
```

### 2.2 权限编码清单

#### 系统管理（system）

| 权限编码 | 说明 |
|----------|------|
| `sys:admin:view` | 查看管理员 |
| `sys:admin:create` | 新增管理员 |
| `sys:admin:update` | 修改管理员 |
| `sys:admin:delete` | 删除管理员 |
| `sys:admin:disable` | 禁用/启用管理员 |
| `sys:admin:reset-password` | 重置管理员密码 |
| `sys:role:view` | 查看角色 |
| `sys:role:create` | 新增角色 |
| `sys:role:update` | 修改角色 |
| `sys:role:delete` | 删除角色 |
| `sys:role:disable` | 禁用/启用角色 |
| `sys:role:permission` | 绑定角色权限 |
| `sys:permission:view` | 查看权限列表 |
| `sys:audit:view` | 查看审核日志 |

#### 用户管理（user）

| 权限编码 | 说明 |
|----------|------|
| `sys:user:view` | 查看用户 |
| `sys:user:update` | 修改用户 |
| `sys:student:view` | 查看学员 |
| `sys:student:create` | 新增学员 |
| `sys:student:update` | 修改学员 |
| `sys:student:delete` | 删除学员 |
| `sys:student:import` | 导入学员 |
| `sys:student:export` | 导出学员 |
| `sys:student:batch-delete` | 批量删除学员 |
| `sys:student:review` | 审核学员认证 |
| `sys:reference:view` | 查看基础数据 |
| `sys:reference:create` | 新增基础数据 |
| `sys:reference:update` | 修改基础数据 |
| `sys:reference:delete` | 删除基础数据 |

#### 学习资源（learning）

| 权限编码 | 说明 |
|----------|------|
| `learning:course:view` | 查看课程 |
| `learning:course:edit` | 管理课程 |
| `learning:course:review` | 审核课程 |
| `learning:book:view` | 查看图书 |
| `learning:book:edit` | 管理图书 |
| `learning:book:review` | 审核图书 |
| `learning:question:view` | 查看题目 |
| `learning:question:edit` | 管理题目 |
| `learning:exam:view` | 查看考卷 |
| `learning:exam:edit` | 管理考卷 |

#### 直播（live）

| 权限编码 | 说明 |
|----------|------|
| `live:view` | 查看直播 |
| `live:edit` | 管理直播 |
| `live:review` | 审核直播 |

#### 内容管理（content）

| 权限编码 | 说明 |
|----------|------|
| `content:home:view` | 查看首页配置 |
| `content:home:edit` | 管理首页配置 |
| `content:article:view` | 查看资讯 |
| `content:article:edit` | 管理资讯 |
| `content:article:review` | 审核资讯 |
| `content:podcast:view` | 查看播客 |
| `content:podcast:edit` | 管理播客 |
| `content:podcast:review` | 审核播客 |
| `content:topic:view` | 查看专题 |
| `content:topic:edit` | 管理专题 |
| `content:topic:review` | 审核专题 |
| `content:file:view` | 查看文件 |
| `content:file:edit` | 管理文件 |

#### 专家（expert）

| 权限编码 | 说明 |
|----------|------|
| `expert:view` | 查看专家 |
| `expert:edit` | 管理专家 |
| `expert:category:view` | 查看专家分类 |
| `expert:category:edit` | 管理专家分类 |

#### 互动（interaction）

| 权限编码 | 说明 |
|----------|------|
| `interaction:qa:view` | 查看答疑 |
| `interaction:qa:reply` | 回复答疑 |
| `interaction:qa:edit` | 管理答疑 |
| `interaction:feedback:view` | 查看反馈 |
| `interaction:feedback:process` | 处理反馈 |
| `interaction:feedback:edit` | 管理反馈 |

#### 知识库（knowledge）

| 权限编码 | 说明 |
|----------|------|
| `knowledge:category:view` | 查看知识库分类 |
| `knowledge:category:edit` | 管理知识库分类 |
| `knowledge:entry:view` | 查看知识库条目 |
| `knowledge:entry:edit` | 管理知识库条目 |
| `knowledge:entry:review` | 审核知识库条目 |

#### 统计（statistics）

| 权限编码 | 说明 |
|----------|------|
| `statistics:view` | 查看统计数据 |

#### 认证（auth）

| 权限编码 | 说明 |
|----------|------|
| `auth:logout` | 退出登录 |
| `auth:me` | 获取当前信息 |
| `auth:status` | 校验登录状态 |

---

## 三、状态字段枚举说明

### 3.1 通用状态枚举

#### `EnabledStatus` — 启用/禁用状态

| 值 | 编码 | 说明 |
|----|------|------|
| `DISABLED` | `0` | 禁用 |
| `ENABLED` | `1` | 启用 |

**适用表**: `sys_admins.status`, `sys_roles.status`, `sys_permissions.status`, `app_users.status`, `students.status`, `tags.status`, `course_videos.status`, `book_chapters.status`, `podcast_audios.status`, `live_session_videos.status`, `home_categories.status`, `home_contents.status`, `organizations.status`, `practice_types.status`, `question_categories.status`, `questions.status`, `exam_papers.status`, `book_categories.status`, `expert_categories.status`, `experts.status`, `knowledge_categories.status`, `knowledge_entries.status`

#### `ReviewStatus` — 审核状态

| 值 | 编码 | 说明 |
|----|------|------|
| `DRAFT` | `0` | 草稿 |
| `PENDING` | `1` | 待审核 |
| `APPROVED` | `2` | 已通过 |
| `REJECTED` | `3` | 已驳回 |

**适用表**: `articles`, `courses`, `books`, `topics`, `podcasts`, `knowledge_entries`, `live_sessions`

#### `PublishStatus` — 发布状态

| 值 | 编码 | 说明 |
|----|------|------|
| `UNPUBLISHED` | `0` | 未发布 |
| `PUBLISHED` | `1` | 已发布 |

**适用表**: `articles`, `courses`, `books`, `topics`, `podcasts`, `knowledge_entries`（通过 `ReviewableEntity` 继承）

### 3.2 用户相关枚举

#### `Gender` — 性别

| 值 | 编码 | 说明 |
|----|------|------|
| `UNKNOWN` | `0` | 未知 |
| `MALE` | `1` | 男 |
| `FEMALE` | `2` | 女 |

#### `UserAuthProvider` — 用户认证提供商

| 值 | 编码 | 说明 |
|----|------|------|
| `WECHAT_MINIAPP` | `wechat_miniapp` | 微信小程序 |
| `WECHAT_WEB` | `wechat_web` | 微信网页 |
| `MOBILE_SMS` | `mobile_sms` | 手机短信 |

#### `AppUserIdentityType` — 用户身份类型

| 值 | 编码 | 说明 |
|----|------|------|
| `STUDENT` | `STUDENT` | 学员 |
| `EXPERT` | `EXPERT` | 专家 |

**适用表**: `app_user_identities.identity_type`

#### `AppUserIdentityStatus` — 用户身份状态

| 值 | 编码 | 说明 |
|----|------|------|
| `INACTIVE` | `0` | 未激活 |
| `ACTIVE` | `1` | 已激活 |

#### `StudentCertificationStatus` — 学员认证状态

| 值 | 编码 | 说明 |
|----|------|------|
| `UNSUBMITTED` | `0` | 未提交 |
| `PENDING` | `1` | 待审核 |
| `APPROVED` | `2` | 已认证 |
| `REJECTED` | `3` | 已驳回 |

**适用表**: `students.certification_status`

#### `AppUserManagementRole` — 管理端用户业务身份（DTO 枚举）

| 值 | 说明 |
|----|------|
| `NORMAL` | 普通用户 |
| `STUDENT` | 学员 |
| `EXPERT` | 专家 |

### 3.3 内容与学习枚举

#### `Difficulty` — 题目难度

| 值 | 编码 | 说明 |
|----|------|------|
| `EASY` | `1` | 简单 |
| `MEDIUM` | `2` | 中等 |
| `HARD` | `3` | 困难 |

#### `QuestionType` — 题目类型

| 值 | 编码 | 说明 |
|----|------|------|
| `SINGLE_CHOICE` | `1` | 单选题 |
| `MULTIPLE_CHOICE` | `2` | 多选题 |
| `TRUE_FALSE` | `3` | 判断题 |
| `SHORT_ANSWER` | `4` | 简答题 |

#### `LiveStatus` — 直播状态

| 值 | 编码 | 说明 |
|----|------|------|
| `NOT_STARTED` | `0` | 未开始 |
| `LIVE` | `1` | 直播中 |
| `ENDED` | `2` | 已结束 |
| `CANCELED` | `3` | 已取消 |

**适用表**: `live_sessions.live_status`

### 3.4 互动枚举

#### `QaStatus` — 问答状态

| 值 | 编码 | 说明 |
|----|------|------|
| `PENDING` | `0` | 待回复 |
| `ANSWERED` | `1` | 已回复 |
| `CLOSED` | `2` | 已关闭 |

**适用表**: `qa_questions.status`

#### `FeedbackStatus` — 反馈状态

| 值 | 编码 | 说明 |
|----|------|------|
| `PENDING` | `0` | 待处理 |
| `PROCESSED` | `1` | 已处理 |

**适用表**: `feedbacks.status`

### 3.5 扩展枚举

#### `PermissionType` — 权限类型

| 值 | 编码 | 说明 |
|----|------|------|
| `MENU` | `1` | 菜单 |
| `BUTTON` | `2` | 按钮 |
| `API` | `3` | 接口 |

**适用表**: `sys_permissions.permission_type`

#### `ValueType` — 扩展字段值类型

| 值 | 编码 | 说明 |
|----|------|------|
| `STRING` | `string` | 字符串 |
| `NUMBER` | `number` | 数字 |
| `BOOLEAN` | `boolean` | 布尔 |
| `DATE` | `date` | 日期 |
| `JSON` | `json` | JSON |

**适用表**: `entity_extensions.value_type`

#### `ExperienceType` — 专家履历类型

| 值 | 编码 | 说明 |
|----|------|------|
| `EDUCATION` | `education` | 教育经历 |
| `WORK` | `work` | 工作经历 |
| `ACHIEVEMENT` | `achievement` | 成就荣誉 |

**适用表**: `expert_experiences.experience_type`

---

## 四、通用基类字段

### BaseEntity（所有实体继承）

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `Long` | 自增主键 |
| `created_at` | `datetime` | 创建时间 |

### ManagedEntity（主要业务实体继承）

| 字段 | 类型 | 说明 |
|------|------|------|
| 继承 BaseEntity 字段 | | |
| `created_by` | `Long` | 创建人 ID |
| `updated_by` | `Long` | 最后修改人 ID |
| `updated_at` | `datetime` | 最后修改时间 |
| `deleted` | `tinyint(1)` | 逻辑删除标记（0-正常，1-删除） |

### ReviewableEntity（需审核内容实体继承）

| 字段 | 类型 | 说明 |
|------|------|------|
| 继承 ManagedEntity 字段 | | |
| `review_status` | `tinyint` | 审核状态，见 `ReviewStatus` |
| `publish_status` | `tinyint` | 发布状态，见 `PublishStatus` |
| `published_at` | `datetime` | 发布时间 |
