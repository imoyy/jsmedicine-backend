# 接口模块清单

> 基于后端 Controller 实现整理的完整接口列表，按业务域和端侧划分。

---

## 接口前缀约定

| 端侧 | 前缀 | 认证方式 |
|------|------|----------|
| 管理端认证 | `/api/v1/auth/**` | 部分公开，部分 Bearer Token |
| 管理端业务 | `/api/v1/admin/**` | Bearer Token + `@PreAuthorize` |
| 用户端 | `/api/v1/app/**` | Bearer Token (ROLE_APP_USER) |
| 公共资源 | `/api/v1/files/**` | 公开 |
| 集成回调 | `/api/v1/integrations/**` | 公开 |

---

## 一、管理端 API（Admin）

### 1.1 认证模块（auth）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/v1/auth/login` | 管理员登录 | 公开 |
| POST | `/api/v1/auth/logout` | 退出登录 | auth:logout |
| GET | `/api/v1/auth/me` | 获取当前管理员信息 | auth:me |
| GET | `/api/v1/auth/status` | 校验登录状态 | auth:status |

### 1.2 系统管理（system）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/system/admins` | 分页查询管理员 | sys:admin:view |
| POST | `/api/v1/admin/system/admins` | 新增管理员 | sys:admin:create |
| GET | `/api/v1/admin/system/admins/{id}` | 管理员详情 | sys:admin:view |
| PUT | `/api/v1/admin/system/admins/{id}` | 修改管理员 | sys:admin:update |
| DELETE | `/api/v1/admin/system/admins/{id}` | 删除管理员 | sys:admin:delete |
| PATCH | `/api/v1/admin/system/admins/{id}/password/reset` | 重置管理员密码 | sys:admin:reset-password |
| PATCH | `/api/v1/admin/system/admins/{id}/status` | 修改管理员状态 | sys:admin:disable |
| PUT | `/api/v1/admin/system/admins/{id}/roles` | 绑定管理员角色 | sys:admin:update |
| GET | `/api/v1/admin/system/roles` | 分页查询角色 | sys:role:view |
| POST | `/api/v1/admin/system/roles` | 新增角色 | sys:role:create |
| GET | `/api/v1/admin/system/roles/{id}` | 角色详情 | sys:role:view |
| PUT | `/api/v1/admin/system/roles/{id}` | 修改角色 | sys:role:update |
| DELETE | `/api/v1/admin/system/roles/{id}` | 删除角色 | sys:role:delete |
| PUT | `/api/v1/admin/system/roles/{id}/permissions` | 绑定角色权限 | sys:role:permission |
| PATCH | `/api/v1/admin/system/roles/{id}/status` | 修改角色状态 | sys:role:disable |
| GET | `/api/v1/admin/system/permissions` | 查询权限列表 | sys:permission:view |
| GET | `/api/v1/admin/system/audit-records` | 分页查询审核日志 | sys:audit:view |

### 1.3 用户与学员管理（user）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/users` | 分页查询用户 | sys:user:view |
| GET | `/api/v1/admin/users/{id}` | 用户详情 | sys:user:view |
| PUT | `/api/v1/admin/users/{id}` | 修改用户信息 | sys:user:update |
| PATCH | `/api/v1/admin/users/{id}/status` | 修改用户状态 | sys:user:update |
| GET | `/api/v1/admin/students` | 分页查询学员 | sys:student:view |
| POST | `/api/v1/admin/students` | 新增学员 | sys:student:create |
| GET | `/api/v1/admin/students/{id}` | 学员详情 | sys:student:view |
| PUT | `/api/v1/admin/students/{id}` | 修改学员信息 | sys:student:update |
| DELETE | `/api/v1/admin/students/{id}` | 删除学员 | sys:student:delete |
| POST | `/api/v1/admin/students/batch-delete` | 批量删除学员 | sys:student:batch-delete |
| POST | `/api/v1/admin/students/import` | 导入学员（Excel） | sys:student:import |
| GET | `/api/v1/admin/students/export` | 导出学员（Excel） | sys:student:export |
| PATCH | `/api/v1/admin/students/{id}/certification` | 审核学员认证 | sys:student:review |

### 1.4 基础数据（reference）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/references/organizations` | 查询机构列表 | sys:reference:view |
| POST | `/api/v1/admin/references/organizations` | 新增机构 | sys:reference:create |
| GET | `/api/v1/admin/references/organizations/{id}` | 机构详情 | sys:reference:view |
| PUT | `/api/v1/admin/references/organizations/{id}` | 修改机构 | sys:reference:update |
| DELETE | `/api/v1/admin/references/organizations/{id}` | 删除机构 | sys:reference:delete |
| GET | `/api/v1/admin/references/practice-types` | 查询执业类型列表 | sys:reference:view |
| POST | `/api/v1/admin/references/practice-types` | 新增执业类型 | sys:reference:create |
| GET | `/api/v1/admin/references/practice-types/{id}` | 执业类型详情 | sys:reference:view |
| PUT | `/api/v1/admin/references/practice-types/{id}` | 修改执业类型 | sys:reference:update |
| DELETE | `/api/v1/admin/references/practice-types/{id}` | 删除执业类型 | sys:reference:delete |

### 1.5 内容管理（content）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/content/home/categories` | 分页查询首页分类 | content:home:view |
| POST | `/api/v1/admin/content/home/categories` | 新增首页分类 | content:home:edit |
| PUT | `/api/v1/admin/content/home/categories/{id}` | 修改首页分类 | content:home:edit |
| DELETE | `/api/v1/admin/content/home/categories/{id}` | 删除首页分类 | content:home:edit |
| GET | `/api/v1/admin/content/home/contents` | 分页查询首页内容 | content:home:view |
| POST | `/api/v1/admin/content/home/contents` | 新增首页内容 | content:home:edit |
| PUT | `/api/v1/admin/content/home/contents/{id}` | 修改首页内容 | content:home:edit |
| DELETE | `/api/v1/admin/content/home/contents/{id}` | 删除首页内容 | content:home:edit |
| GET | `/api/v1/admin/content/home/candidates` | 查询首页候选资源 | content:home:view |
| GET | `/api/v1/admin/content/articles` | 分页查询资讯 | content:article:view |
| POST | `/api/v1/admin/content/articles` | 新增资讯 | content:article:edit |
| PUT | `/api/v1/admin/content/articles/{id}` | 修改资讯 | content:article:edit |
| DELETE | `/api/v1/admin/content/articles/{id}` | 删除资讯 | content:article:edit |
| PATCH | `/api/v1/admin/content/articles/{id}/review` | 审核资讯 | content:article:review |
| GET | `/api/v1/admin/content/podcasts` | 分页查询播客 | content:podcast:view |
| POST | `/api/v1/admin/content/podcasts` | 新增播客 | content:podcast:edit |
| PUT | `/api/v1/admin/content/podcasts/{id}` | 修改播客 | content:podcast:edit |
| DELETE | `/api/v1/admin/content/podcasts/{id}` | 删除播客 | content:podcast:edit |
| PATCH | `/api/v1/admin/content/podcasts/{id}/review` | 审核播客 | content:podcast:review |
| GET | `/api/v1/admin/content/podcasts/{podcastId}/audios` | 查询播客音频列表 | content:podcast:view |
| POST | `/api/v1/admin/content/podcasts/audios` | 新增播客音频 | content:podcast:edit |
| PUT | `/api/v1/admin/content/podcasts/audios/{id}` | 修改播客音频 | content:podcast:edit |
| DELETE | `/api/v1/admin/content/podcasts/audios/{id}` | 删除播客音频 | content:podcast:edit |
| GET | `/api/v1/admin/content/topics` | 分页查询专题 | content:topic:view |
| POST | `/api/v1/admin/content/topics` | 新增专题 | content:topic:edit |
| PUT | `/api/v1/admin/content/topics/{id}` | 修改专题 | content:topic:edit |
| DELETE | `/api/v1/admin/content/topics/{id}` | 删除专题 | content:topic:edit |
| PUT | `/api/v1/admin/content/topics/{id}/items` | 替换专题关联项 | content:topic:edit |
| PATCH | `/api/v1/admin/content/topics/{id}/review` | 审核专题 | content:topic:review |
| GET | `/api/v1/admin/content/files` | 分页查询文件资源 | content:file:view |
| POST | `/api/v1/admin/content/files` | 登记文件资源 | content:file:edit |
| DELETE | `/api/v1/admin/content/files/{id}` | 删除文件资源 | content:file:edit |

> 头像封面签名上传与确认接口：`POST /api/v1/admin/content/files/covers/upload-url`、`POST /api/v1/admin/content/files/covers/confirm`

### 1.6 学习资源（learning）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/learning/courses` | 分页查询课程 | learning:course:view |
| POST | `/api/v1/admin/learning/courses` | 新增课程 | learning:course:edit |
| GET | `/api/v1/admin/learning/courses/{id}` | 课程详情 | learning:course:view |
| PUT | `/api/v1/admin/learning/courses/{id}` | 修改课程 | learning:course:edit |
| DELETE | `/api/v1/admin/learning/courses/{id}` | 删除课程 | learning:course:edit |
| PATCH | `/api/v1/admin/learning/courses/{id}/review` | 审核课程 | learning:course:review |
| GET | `/api/v1/admin/learning/courses/{courseId}/videos` | 查询课程视频列表 | learning:course:view |
| POST | `/api/v1/admin/learning/courses/videos` | 新增课程视频 | learning:course:edit |
| PUT | `/api/v1/admin/learning/courses/videos/{id}` | 修改课程视频 | learning:course:edit |
| DELETE | `/api/v1/admin/learning/courses/videos/{id}` | 删除课程视频 | learning:course:edit |
| GET | `/api/v1/admin/learning/book-categories` | 分页查询图书分类 | learning:book:view |
| POST | `/api/v1/admin/learning/book-categories` | 新增图书分类 | learning:book:edit |
| GET | `/api/v1/admin/learning/book-categories/{id}` | 图书分类详情 | learning:book:view |
| PUT | `/api/v1/admin/learning/book-categories/{id}` | 修改图书分类 | learning:book:edit |
| DELETE | `/api/v1/admin/learning/book-categories/{id}` | 删除图书分类 | learning:book:edit |
| GET | `/api/v1/admin/learning/book-categories/{id}/books` | 查询分类下图书 | learning:book:view |
| POST | `/api/v1/admin/learning/book-categories/{id}/books` | 批量加入分类图书 | learning:book:edit |
| DELETE | `/api/v1/admin/learning/book-categories/{id}/books` | 批量移除分类图书 | learning:book:edit |
| GET | `/api/v1/admin/learning/books` | 分页查询图书 | learning:book:view |
| POST | `/api/v1/admin/learning/books` | 新增图书 | learning:book:edit |
| GET | `/api/v1/admin/learning/books/{id}` | 图书详情 | learning:book:view |
| PUT | `/api/v1/admin/learning/books/{id}` | 修改图书 | learning:book:edit |
| DELETE | `/api/v1/admin/learning/books/{id}` | 删除图书 | learning:book:edit |
| PATCH | `/api/v1/admin/learning/books/{id}/review` | 审核图书 | learning:book:review |
| GET | `/api/v1/admin/learning/books/{bookId}/chapters` | 查询图书章节列表 | learning:book:view |
| POST | `/api/v1/admin/learning/books/chapters` | 新增图书章节 | learning:book:edit |
| PUT | `/api/v1/admin/learning/books/chapters/{id}` | 修改图书章节 | learning:book:edit |
| DELETE | `/api/v1/admin/learning/books/chapters/{id}` | 删除图书章节 | learning:book:edit |
| GET | `/api/v1/admin/learning/question-categories` | 分页查询题库分类 | learning:question:view |
| POST | `/api/v1/admin/learning/question-categories` | 新增题库分类 | learning:question:edit |
| PUT | `/api/v1/admin/learning/question-categories/{id}` | 修改题库分类 | learning:question:edit |
| DELETE | `/api/v1/admin/learning/question-categories/{id}` | 删除题库分类 | learning:question:edit |
| GET | `/api/v1/admin/learning/questions` | 分页查询题目 | learning:question:view |
| POST | `/api/v1/admin/learning/questions` | 新增题目 | learning:question:edit |
| GET | `/api/v1/admin/learning/questions/{id}` | 题目详情 | learning:question:view |
| PUT | `/api/v1/admin/learning/questions/{id}` | 修改题目 | learning:question:edit |
| DELETE | `/api/v1/admin/learning/questions/{id}` | 删除题目 | learning:question:edit |
| PUT | `/api/v1/admin/learning/questions/{id}/options` | 替换题目选项 | learning:question:edit |
| GET | `/api/v1/admin/learning/exam-papers` | 分页查询考卷 | learning:exam:view |
| POST | `/api/v1/admin/learning/exam-papers` | 新增考卷 | learning:exam:edit |
| GET | `/api/v1/admin/learning/exam-papers/{id}` | 考卷详情 | learning:exam:view |
| PUT | `/api/v1/admin/learning/exam-papers/{id}` | 修改考卷 | learning:exam:edit |
| DELETE | `/api/v1/admin/learning/exam-papers/{id}` | 删除考卷 | learning:exam:edit |
| PUT | `/api/v1/admin/learning/exam-papers/{id}/questions` | 替换考卷题目 | learning:exam:edit |

### 1.7 直播管理（live）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/live-sessions` | 分页查询直播 | live:view |
| POST | `/api/v1/admin/live-sessions` | 新增直播 | live:edit |
| GET | `/api/v1/admin/live-sessions/{id}` | 直播详情 | live:view |
| PUT | `/api/v1/admin/live-sessions/{id}` | 修改直播 | live:edit |
| DELETE | `/api/v1/admin/live-sessions/{id}` | 删除直播 | live:edit |
| PATCH | `/api/v1/admin/live-sessions/{id}/review` | 审核直播 | live:review |
| GET | `/api/v1/admin/live-sessions/{liveSessionId}/videos` | 查询直播视频列表 | live:view |
| POST | `/api/v1/admin/live-sessions/videos` | 新增直播视频 | live:edit |
| PUT | `/api/v1/admin/live-sessions/videos/{id}` | 修改直播视频 | live:edit |
| DELETE | `/api/v1/admin/live-sessions/videos/{id}` | 删除直播视频 | live:edit |
| POST | `/api/v1/admin/live-sessions/batch-delete` | 批量删除直播 | live:edit |

### 1.8 专家管理（expert）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/experts/categories` | 分页查询专家分类 | expert:category:view |
| POST | `/api/v1/admin/experts/categories` | 新增专家分类 | expert:category:edit |
| PUT | `/api/v1/admin/experts/categories/{id}` | 修改专家分类 | expert:category:edit |
| DELETE | `/api/v1/admin/experts/categories/{id}` | 删除专家分类 | expert:category:edit |
| GET | `/api/v1/admin/experts` | 分页查询专家 | expert:view |
| POST | `/api/v1/admin/experts` | 新增专家 | expert:edit |
| GET | `/api/v1/admin/experts/{id}` | 专家详情 | expert:view |
| PUT | `/api/v1/admin/experts/{id}` | 修改专家 | expert:edit |
| DELETE | `/api/v1/admin/experts/{id}` | 删除专家 | expert:edit |
| PUT | `/api/v1/admin/experts/{id}/categories` | 替换专家分类 | expert:edit |
| PUT | `/api/v1/admin/experts/{id}/experiences` | 替换专家履历 | expert:edit |

### 1.9 互动管理（interaction）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/interaction/qa/questions` | 分页查询答疑问题 | interaction:qa:view |
| GET | `/api/v1/admin/interaction/qa/questions/{id}` | 答疑问题详情 | interaction:qa:view |
| DELETE | `/api/v1/admin/interaction/qa/questions/{id}` | 删除答疑问题 | interaction:qa:edit |
| POST | `/api/v1/admin/interaction/qa/questions/{id}/answers` | 回复答疑问题 | interaction:qa:reply |
| GET | `/api/v1/admin/interaction/feedbacks` | 分页查询反馈 | interaction:feedback:view |
| GET | `/api/v1/admin/interaction/feedbacks/{id}` | 反馈详情 | interaction:feedback:view |
| DELETE | `/api/v1/admin/interaction/feedbacks/{id}` | 删除反馈 | interaction:feedback:edit |
| PATCH | `/api/v1/admin/interaction/feedbacks/{id}/process` | 处理反馈 | interaction:feedback:process |

### 1.10 知识库管理（knowledge）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/knowledge/categories` | 分页查询知识库分类 | knowledge:category:view |
| POST | `/api/v1/admin/knowledge/categories` | 新增知识库分类 | knowledge:category:edit |
| PUT | `/api/v1/admin/knowledge/categories/{id}` | 修改知识库分类 | knowledge:category:edit |
| DELETE | `/api/v1/admin/knowledge/categories/{id}` | 删除知识库分类 | knowledge:category:edit |
| GET | `/api/v1/admin/knowledge/entries` | 分页查询知识库条目 | knowledge:entry:view |
| POST | `/api/v1/admin/knowledge/entries` | 新增知识库条目 | knowledge:entry:edit |
| GET | `/api/v1/admin/knowledge/entries/{id}` | 知识库条目详情 | knowledge:entry:view |
| PUT | `/api/v1/admin/knowledge/entries/{id}` | 修改知识库条目 | knowledge:entry:edit |
| DELETE | `/api/v1/admin/knowledge/entries/{id}` | 删除知识库条目 | knowledge:entry:edit |
| PATCH | `/api/v1/admin/knowledge/entries/{id}/review` | 审核知识库条目 | knowledge:entry:review |

### 1.11 统计管理（statistics）

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/admin/statistics/study-hours/summary` | 学时统计汇总 | statistics:view |
| GET | `/api/v1/admin/statistics/study-hours/resources` | 按资源类型学时统计 | statistics:view |
| GET | `/api/v1/admin/statistics/students/summary` | 学员统计汇总 | statistics:view |
| GET | `/api/v1/admin/statistics/regions` | 地区学员统计 | statistics:view |
| GET | `/api/v1/admin/statistics/exam-scores/summary` | 成绩统计汇总 | statistics:view |
| GET | `/api/v1/admin/statistics/exam-scores/papers` | 按试卷成绩统计 | statistics:view |
| GET | `/api/v1/admin/statistics/content-interactions` | 内容互动统计 | statistics:view |

---

## 二、用户端 API（App）

### 2.1 认证（app auth）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/app/auth/login` | 账号密码登录 |
| POST | `/api/v1/app/auth/sms-code` | 发送短信验证码 |
| POST | `/api/v1/app/auth/sms-login` | 短信验证码登录 |
| POST | `/api/v1/app/auth/wechat-login` | 微信小程序授权登录 |
| POST | `/api/v1/app/auth/wechat-web/qr-config` | 获取微信扫码登录配置（网页端） |
| POST | `/api/v1/app/auth/wechat-web/login` | 微信扫码回调登录（网页端） |
| POST | `/api/v1/app/auth/wechat-bind-mobile` | 微信绑定手机号 |
| POST | `/api/v1/app/auth/logout` | 退出登录 |
| GET | `/api/v1/app/auth/me` | 获取当前用户信息 |
| GET | `/api/v1/app/auth/status` | 校验登录状态 |

### 2.2 个人中心（profile）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/app/profile` | 获取个人资料 |
| PUT | `/api/v1/app/profile` | 修改个人资料 |
| POST | `/api/v1/app/profile/avatar/upload-url` | 申请头像上传地址 |
| POST | `/api/v1/app/profile/avatar/confirm` | 确认头像上传 |
| GET | `/api/v1/app/profile/certification` | 查询学员认证结果 |
| POST | `/api/v1/app/profile/certification` | 提交学员认证申请 |
| GET | `/api/v1/app/profile/favorites` | 查询我的收藏 |
| GET | `/api/v1/app/profile/browse-histories` | 查询浏览记录 |
| GET | `/api/v1/app/profile/summary` | 查询个人中心聚合信息 |

### 2.3 学习资源（learning）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/app/learning/courses` | 分页查询课程 |
| GET | `/api/v1/app/learning/courses/{id}` | 课程详情 |
| GET | `/api/v1/app/learning/courses/{courseId}/videos/{videoId}` | 课程视频详情 |
| GET | `/api/v1/app/learning/book-categories` | 查询图书分类 |
| GET | `/api/v1/app/learning/books` | 分页查询图书 |
| GET | `/api/v1/app/learning/books/{id}` | 图书详情 |
| GET | `/api/v1/app/learning/books/{bookId}/chapters/{chapterId}` | 图书章节详情 |
| GET | `/api/v1/app/learning/podcasts` | 分页查询播客 |
| GET | `/api/v1/app/learning/podcasts/{id}` | 播客详情 |
| GET | `/api/v1/app/learning/exam-papers` | 分页查询考卷 |
| GET | `/api/v1/app/learning/exam-papers/{id}` | 考卷详情 |
| POST | `/api/v1/app/learning/exam-papers/{id}/submit` | 提交考卷答案 |
| GET | `/api/v1/app/learning/exam-records` | 分页查询考试记录 |
| GET | `/api/v1/app/learning/exam-records/{id}` | 考试结果与解析 |
| POST | `/api/v1/app/learning/records` | 同步学习记录 |
| GET | `/api/v1/app/learning/topics` | 分页查询专题 |
| GET | `/api/v1/app/learning/topics/{id}` | 专题详情 |
| GET | `/api/v1/app/learning/topics/{id}/sections/{sectionType}` | 分页查询专题分区内容 |

### 2.4 直播（live）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/app/live-sessions` | 分页查询直播 |
| GET | `/api/v1/app/live-sessions/{id}` | 直播详情 |

### 2.5 内容（content）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/app/content/articles` | 分页查询资讯 |
| GET | `/api/v1/app/content/articles/{id}` | 资讯详情 |

### 2.6 专家（expert）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/app/experts/categories` | 查询专家分类 |
| GET | `/api/v1/app/experts` | 分页查询可咨询专家 |
| GET | `/api/v1/app/experts/{id}` | 专家详情 |

### 2.7 互动（interaction）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/app/interaction/qa/questions` | 发起咨询 |
| GET | `/api/v1/app/interaction/qa/questions` | 我的咨询列表 |
| GET | `/api/v1/app/interaction/qa/questions/{id}` | 咨询详情 |
| POST | `/api/v1/app/interaction/feedbacks` | 提交反馈 |
| POST | `/api/v1/app/interaction/favorites` | 切换收藏 |
| POST | `/api/v1/app/interaction/browse-histories` | 上报浏览记录 |

### 2.8 知识库（knowledge）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/app/knowledge/categories/tree` | 知识库分类树 |
| GET | `/api/v1/app/knowledge/entries` | 搜索知识库条目 |
| GET | `/api/v1/app/knowledge/entries/{id}` | 知识库条目详情 |

---

## 三、公共与集成接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/files/{id}/content` | 读取公开文件（头像等） |
| POST | `/api/v1/integrations/srs/live-hooks` | SRS 直播状态回调 |
