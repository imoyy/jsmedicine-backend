# API 文档

本文档基于当前仓库中的 `api/api.json`、Controller 实现和现有测试数据整理，供前端联调、测试联调和接口排查使用。

## 基础说明

- 项目：`中医在线` 后端
- 文档更新时间：`2026-05-28`
- 认证方式：`Authorization: Bearer <token>`
- 统一返回：`ApiResponse<T>`
- 分页参数：`page`、`size`、`sort`

## 近期契约变更

- 用户资料新增 `profileSignature`，用于用户端个人签名展示和管理端用户详情回显。
- 学员认证新增结构化材料字段 `certificationFiles`，旧字段 `certificationMaterials` 暂时保留为兼容字段。
- 学员和专家新增基础数据关联字段：`organizationId`、`practiceTypeId`；学员额外新增 `provinceCode`、`cityCode`、`districtCode`。
- 管理端专家新增可选 `userId`，用于把专家主数据绑定到前台登录用户；绑定后后端会同步维护用户 `EXPERT` 身份。
- 管理端新增 `PUT /api/v1/admin/users/{id}`，用于支撑“修改用户信息”弹窗一次性保存昵称、口号、状态、角色、学员绑定、地区和医院。
- 管理端新增基础数据接口 `/api/v1/admin/references/organizations`、`/api/v1/admin/references/practice-types`，用于机构和执业类型下拉。

## 端侧划分

- 管理员端：`/api/v1/auth/**`、`/api/v1/admin/**`
- 用户端：`/api/v1/app/**`
- 用户端网页端与小程序共用：除微信授权外的大部分用户端接口
- 用户端小程序独有：当前只有微信授权登录接口
- 用户端网页端独有：当前未发现单独面向网页端的专属接口

## 管理员端 API

### 1. 认证与权限

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/auth/login` | 管理员登录 |
| POST | `/api/v1/auth/logout` | 退出登录 |
| GET | `/api/v1/auth/me` | 获取当前管理员信息 |
| GET | `/api/v1/auth/status` | 校验登录状态 |

### 2. 系统管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/system/admins` | 分页查询管理员 |
| POST | `/api/v1/admin/system/admins` | 新增管理员 |
| GET | `/api/v1/admin/system/admins/{id}` | 查询管理员详情 |
| PUT | `/api/v1/admin/system/admins/{id}` | 修改管理员 |
| PATCH | `/api/v1/admin/system/admins/{id}/password/reset` | 重置管理员密码 |
| PUT | `/api/v1/admin/system/admins/{id}/roles` | 绑定管理员角色 |
| PATCH | `/api/v1/admin/system/admins/{id}/status` | 修改管理员状态 |
| GET | `/api/v1/admin/system/audit-records` | 分页查询操作审计记录 |
| GET | `/api/v1/admin/system/permissions` | 查询权限列表 |
| GET | `/api/v1/admin/system/roles` | 分页查询角色 |
| POST | `/api/v1/admin/system/roles` | 新增角色 |
| GET | `/api/v1/admin/system/roles/{id}` | 查询角色详情 |
| PUT | `/api/v1/admin/system/roles/{id}` | 修改角色 |
| PUT | `/api/v1/admin/system/roles/{id}/permissions` | 绑定角色权限 |
| PATCH | `/api/v1/admin/system/roles/{id}/status` | 修改角色状态 |

### 3. 用户与学员

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/users` | 分页查询用户 |
| GET | `/api/v1/admin/users/{id}` | 查询用户详情 |
| PUT | `/api/v1/admin/users/{id}` | 修改用户信息 |
| PATCH | `/api/v1/admin/users/{id}/status` | 修改用户状态 |
| GET | `/api/v1/admin/students` | 分页查询学员 |
| GET | `/api/v1/admin/students/{id}` | 查询学员详情 |
| PUT | `/api/v1/admin/students/{id}` | 维护学员信息 |
| PATCH | `/api/v1/admin/students/{id}/certification` | 审核学员认证 |

#### 3.1 用户字段补充

`GET /api/v1/admin/users`、`GET /api/v1/admin/users/{id}` 的用户响应包含以下账户域补充字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `profileSignature` | `string` | 用户个人签名 |
| `role` | `string` | 管理端用户业务身份：`NORMAL`、`STUDENT`、`EXPERT` |
| `studentId` | `integer` | 当前绑定学员 ID，仅学员身份通常有值 |
| `studentName` | `string` | 当前绑定学员姓名 |
| `province` / `provinceCode` | `string` | 当前绑定学员的省名称和编码 |
| `city` / `cityCode` | `string` | 当前绑定学员的市名称和编码 |
| `district` / `districtCode` | `string` | 当前绑定学员的区县名称和编码 |
| `organization` / `organizationId` | `string` / `integer` | 当前绑定学员的机构名称和机构 ID |
| `practiceTypeId` | `integer` | 当前绑定学员的执业类型 ID |

`PUT /api/v1/admin/users/{id}` 请求字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `nickname` | `string` | 昵称，必填，最长 64 字符 |
| `profileSignature` | `string` | 口号/个人签名，必填，最长 255 字符 |
| `status` | `integer` | 用户状态：`1` 启用，`0` 禁用 |
| `role` | `string` | 业务身份：`NORMAL`、`STUDENT`、`EXPERT` |
| `studentId` | `integer` | 当 `role=STUDENT` 时必填，表示绑定的学员 |
| `province` / `provinceCode` | `string` | 当绑定学员时同步维护学员省信息 |
| `city` / `cityCode` | `string` | 当绑定学员时同步维护学员市信息 |
| `district` / `districtCode` | `string` | 当绑定学员时同步维护学员区县信息 |
| `organization` / `organizationId` | `string` / `integer` | 当绑定学员时同步维护学员机构 |
| `practiceTypeId` | `integer` | 当绑定学员时同步维护学员执业类型 |

角色处理规则：

- `role=NORMAL`：停用该用户的 `STUDENT`、`EXPERT` 身份，并解除当前学员/专家绑定。
- `role=STUDENT`：必须传 `studentId`；后端会把该学员绑定到当前用户，激活 `STUDENT` 身份，并解除该用户已有专家绑定。
- `role=EXPERT`：后端会激活 `EXPERT` 身份，并解除该用户已有学员绑定；专家主数据仍建议在专家管理模块维护。

#### 3.2 学员请求与响应字段补充

`PUT /api/v1/admin/students/{id}` 支持维护以下规范化字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `provinceCode` | `string` | 省级行政区编码 |
| `cityCode` | `string` | 市级行政区编码 |
| `districtCode` | `string` | 区县行政区编码 |
| `organizationId` | `integer` | 机构 ID，对应 `organizations.id` |
| `practiceTypeId` | `integer` | 执业类型 ID，对应 `practice_types.id` |

`GET /api/v1/admin/students`、`GET /api/v1/admin/students/{id}` 响应同步返回上述字段，并额外返回结构化认证材料：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `certificationFiles` | `array` | 学员认证材料文件列表 |
| `certificationFiles[].id` | `integer` | 认证材料记录 ID |
| `certificationFiles[].fileAssetId` | `integer` | 文件资产 ID，可为空 |
| `certificationFiles[].sourceUrl` | `string` | 外部或历史材料 URL，可为空 |
| `certificationFiles[].materialType` | `string` | 材料类型，如 `id_card`、`qualification`、`other` |
| `certificationFiles[].sortOrder` | `integer` | 展示顺序 |

### 4. 内容配置

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/content/articles` | 分页查询资讯 |
| POST | `/api/v1/admin/content/articles` | 新增资讯 |
| PUT | `/api/v1/admin/content/articles/{id}` | 修改资讯 |
| DELETE | `/api/v1/admin/content/articles/{id}` | 删除资讯 |
| PATCH | `/api/v1/admin/content/articles/{id}/review` | 审核资讯 |
| GET | `/api/v1/admin/content/files` | 分页查询文件资源 |
| POST | `/api/v1/admin/content/files` | 登记文件资源 |
| DELETE | `/api/v1/admin/content/files/{id}` | 删除文件资源 |
| GET | `/api/v1/admin/content/home/categories` | 分页查询首页分类 |
| POST | `/api/v1/admin/content/home/categories` | 新增首页分类 |
| PUT | `/api/v1/admin/content/home/categories/{id}` | 修改首页分类 |
| DELETE | `/api/v1/admin/content/home/categories/{id}` | 删除首页分类 |
| GET | `/api/v1/admin/content/home/contents` | 分页查询首页内容 |
| POST | `/api/v1/admin/content/home/contents` | 新增首页内容 |
| PUT | `/api/v1/admin/content/home/contents/{id}` | 修改首页内容 |
| DELETE | `/api/v1/admin/content/home/contents/{id}` | 删除首页内容 |
| GET | `/api/v1/admin/content/podcasts` | 分页查询播客 |
| POST | `/api/v1/admin/content/podcasts` | 新增播客 |
| PUT | `/api/v1/admin/content/podcasts/{id}` | 修改播客 |
| DELETE | `/api/v1/admin/content/podcasts/{id}` | 删除播客 |
| PATCH | `/api/v1/admin/content/podcasts/{id}/review` | 审核播客 |
| GET | `/api/v1/admin/content/podcasts/{podcastId}/audios` | 分页查询播客音频 |
| POST | `/api/v1/admin/content/podcasts/audios` | 新增播客音频 |
| PUT | `/api/v1/admin/content/podcasts/audios/{id}` | 修改播客音频 |
| DELETE | `/api/v1/admin/content/podcasts/audios/{id}` | 删除播客音频 |
| GET | `/api/v1/admin/content/topics` | 分页查询专题 |
| POST | `/api/v1/admin/content/topics` | 新增专题 |
| PUT | `/api/v1/admin/content/topics/{id}` | 修改专题 |
| DELETE | `/api/v1/admin/content/topics/{id}` | 删除专题 |
| PUT | `/api/v1/admin/content/topics/{id}/items` | 替换专题关联项 |
| PATCH | `/api/v1/admin/content/topics/{id}/review` | 审核专题 |

### 5. 学习资源

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/learning/book-categories` | 分页查询图书分类 |
| POST | `/api/v1/admin/learning/book-categories` | 新增图书分类 |
| PUT | `/api/v1/admin/learning/book-categories/{id}` | 修改图书分类 |
| DELETE | `/api/v1/admin/learning/book-categories/{id}` | 删除图书分类 |
| GET | `/api/v1/admin/learning/books` | 分页查询图书 |
| POST | `/api/v1/admin/learning/books` | 新增图书 |
| GET | `/api/v1/admin/learning/books/{id}` | 查询图书详情 |
| PUT | `/api/v1/admin/learning/books/{id}` | 修改图书 |
| DELETE | `/api/v1/admin/learning/books/{id}` | 删除图书 |
| PATCH | `/api/v1/admin/learning/books/{id}/review` | 审核图书 |
| GET | `/api/v1/admin/learning/books/{bookId}/chapters` | 分页查询图书章节 |
| POST | `/api/v1/admin/learning/books/chapters` | 新增图书章节 |
| PUT | `/api/v1/admin/learning/books/chapters/{id}` | 修改图书章节 |
| DELETE | `/api/v1/admin/learning/books/chapters/{id}` | 删除图书章节 |
| GET | `/api/v1/admin/learning/courses` | 分页查询课程 |
| POST | `/api/v1/admin/learning/courses` | 新增课程 |
| GET | `/api/v1/admin/learning/courses/{id}` | 查询课程详情 |
| PUT | `/api/v1/admin/learning/courses/{id}` | 修改课程 |
| DELETE | `/api/v1/admin/learning/courses/{id}` | 删除课程 |
| PATCH | `/api/v1/admin/learning/courses/{id}/review` | 审核课程 |
| GET | `/api/v1/admin/learning/courses/{courseId}/videos` | 分页查询课程视频 |
| POST | `/api/v1/admin/learning/courses/videos` | 新增课程视频 |
| PUT | `/api/v1/admin/learning/courses/videos/{id}` | 修改课程视频 |
| DELETE | `/api/v1/admin/learning/courses/videos/{id}` | 删除课程视频 |
| GET | `/api/v1/admin/learning/exam-papers` | 分页查询考卷 |
| POST | `/api/v1/admin/learning/exam-papers` | 新增考卷 |
| GET | `/api/v1/admin/learning/exam-papers/{id}` | 考卷详情 |
| PUT | `/api/v1/admin/learning/exam-papers/{id}` | 修改考卷 |
| DELETE | `/api/v1/admin/learning/exam-papers/{id}` | 删除考卷 |
| PUT | `/api/v1/admin/learning/exam-papers/{id}/questions` | 替换考卷题目 |
| GET | `/api/v1/admin/learning/question-categories` | 分页查询题库分类 |
| POST | `/api/v1/admin/learning/question-categories` | 新增题库分类 |
| PUT | `/api/v1/admin/learning/question-categories/{id}` | 修改题库分类 |
| DELETE | `/api/v1/admin/learning/question-categories/{id}` | 删除题库分类 |
| GET | `/api/v1/admin/learning/questions` | 分页查询题目 |
| POST | `/api/v1/admin/learning/questions` | 新增题目 |
| GET | `/api/v1/admin/learning/questions/{id}` | 题目详情 |
| PUT | `/api/v1/admin/learning/questions/{id}` | 修改题目 |
| DELETE | `/api/v1/admin/learning/questions/{id}` | 删除题目 |
| PUT | `/api/v1/admin/learning/questions/{id}/options` | 替换题目选项 |

### 6. 专家管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/experts` | 分页查询专家 |
| POST | `/api/v1/admin/experts` | 新增专家 |
| GET | `/api/v1/admin/experts/{id}` | 专家详情 |
| PUT | `/api/v1/admin/experts/{id}` | 修改专家 |
| DELETE | `/api/v1/admin/experts/{id}` | 删除专家 |
| GET | `/api/v1/admin/experts/categories` | 分页查询专家分类 |
| POST | `/api/v1/admin/experts/categories` | 新增专家分类 |
| PUT | `/api/v1/admin/experts/categories/{id}` | 修改专家分类 |
| DELETE | `/api/v1/admin/experts/categories/{id}` | 删除专家分类 |
| PUT | `/api/v1/admin/experts/{id}/categories` | 替换专家分类 |
| PUT | `/api/v1/admin/experts/{id}/experiences` | 替换专家履历 |

#### 6.1 专家请求与响应字段补充

`POST /api/v1/admin/experts`、`PUT /api/v1/admin/experts/{id}` 支持以下账户域补充字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userId` | `integer` | 绑定的前台用户 ID，可为空；非空时同一用户只能绑定一个专家 |
| `organizationId` | `integer` | 机构 ID，对应 `organizations.id` |
| `practiceTypeId` | `integer` | 执业类型 ID，对应 `practice_types.id` |

专家响应同步返回 `userId`、`organizationId`、`practiceTypeId`。当 `userId` 非空时，后端会确保该前台用户存在激活的 `EXPERT` 身份记录。

### 7. 互动处理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/interaction/feedbacks` | 分页查询反馈 |
| GET | `/api/v1/admin/interaction/feedbacks/{id}` | 反馈详情 |
| DELETE | `/api/v1/admin/interaction/feedbacks/{id}` | 删除反馈 |
| PATCH | `/api/v1/admin/interaction/feedbacks/{id}/process` | 处理反馈 |
| GET | `/api/v1/admin/interaction/qa/questions` | 分页查询答疑问题 |
| GET | `/api/v1/admin/interaction/qa/questions/{id}` | 答疑问题详情 |
| DELETE | `/api/v1/admin/interaction/qa/questions/{id}` | 删除答疑问题 |
| POST | `/api/v1/admin/interaction/qa/questions/{id}/answers` | 回复答疑问题 |

### 8. 知识库

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/knowledge/categories` | 分页查询知识库分类 |
| POST | `/api/v1/admin/knowledge/categories` | 新增知识库分类 |
| PUT | `/api/v1/admin/knowledge/categories/{id}` | 修改知识库分类 |
| DELETE | `/api/v1/admin/knowledge/categories/{id}` | 删除知识库分类 |
| GET | `/api/v1/admin/knowledge/entries` | 分页查询知识库条目 |
| POST | `/api/v1/admin/knowledge/entries` | 新增知识库条目 |
| GET | `/api/v1/admin/knowledge/entries/{id}` | 知识库条目详情 |
| PUT | `/api/v1/admin/knowledge/entries/{id}` | 修改知识库条目 |
| DELETE | `/api/v1/admin/knowledge/entries/{id}` | 删除知识库条目 |
| PATCH | `/api/v1/admin/knowledge/entries/{id}/review` | 审核知识库条目 |

### 9. 直播

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/live-sessions` | 分页查询直播 |
| POST | `/api/v1/admin/live-sessions` | 新增直播 |
| GET | `/api/v1/admin/live-sessions/{id}` | 直播详情 |
| PUT | `/api/v1/admin/live-sessions/{id}` | 修改直播 |
| DELETE | `/api/v1/admin/live-sessions/{id}` | 删除直播 |
| PATCH | `/api/v1/admin/live-sessions/{id}/review` | 审核直播 |

### 10. 统计管理

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/statistics/study-hours/summary` | 查询学时统计汇总 |
| GET | `/api/v1/admin/statistics/study-hours/resources` | 按资源类型查询学时统计 |
| GET | `/api/v1/admin/statistics/students/summary` | 查询学员统计汇总 |
| GET | `/api/v1/admin/statistics/regions` | 查询地区学员统计 |
| GET | `/api/v1/admin/statistics/exam-scores/summary` | 查询成绩统计汇总 |
| GET | `/api/v1/admin/statistics/exam-scores/papers` | 按试卷查询成绩统计 |
| GET | `/api/v1/admin/statistics/content-interactions` | 查询内容浏览收藏分享统计 |

### 11. 基础数据

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/references/organizations` | 查询机构列表，支持按 `keyword`、`provinceCode`、`cityCode`、`districtCode`、`status` 过滤 |
| GET | `/api/v1/admin/references/practice-types` | 查询执业类型列表，支持按 `parentId`、`status` 过滤 |

## 用户端 API

### 1. 网页端与小程序共用 API

#### 1.1 认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/app/auth/login` | 用户端账号密码登录 |
| POST | `/api/v1/app/auth/sms-code` | 发送用户端手机号验证码 |
| POST | `/api/v1/app/auth/sms-login` | 用户端手机号验证码登录 |
| POST | `/api/v1/app/auth/logout` | 用户端退出登录 |
| GET | `/api/v1/app/auth/me` | 获取当前登录用户 |
| GET | `/api/v1/app/auth/status` | 校验用户端登录状态 |

#### 1.2 个人中心

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/app/profile` | 获取用户端个人资料 |
| PUT | `/api/v1/app/profile` | 修改用户端个人资料 |
| GET | `/api/v1/app/profile/browse-histories` | 查询浏览记录 |
| GET | `/api/v1/app/profile/certification` | 查询学员认证结果 |
| POST | `/api/v1/app/profile/certification` | 提交学员认证申请 |
| GET | `/api/v1/app/profile/favorites` | 查询我的收藏 |
| GET | `/api/v1/app/profile/summary` | 查询个人中心聚合信息 |

##### 1.2.1 个人资料字段补充

`PUT /api/v1/app/profile` 请求支持以下补充字段，`GET /api/v1/app/profile` 响应同步返回：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `profileSignature` | `string` | 用户个人签名，最长 255 字符 |

##### 1.2.2 学员认证字段补充

`POST /api/v1/app/profile/certification` 请求支持地区编码、基础数据关联和结构化认证材料：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `provinceCode` | `string` | 省级行政区编码 |
| `cityCode` | `string` | 市级行政区编码 |
| `districtCode` | `string` | 区县行政区编码 |
| `organizationId` | `integer` | 机构 ID，对应 `organizations.id` |
| `practiceTypeId` | `integer` | 执业类型 ID，对应 `practice_types.id` |
| `certificationFiles` | `array` | 本次提交的认证材料列表；提交后会替换该学员旧材料 |
| `certificationFiles[].fileAssetId` | `integer` | 文件资产 ID，可为空 |
| `certificationFiles[].sourceUrl` | `string` | 外部或历史材料 URL；当 `fileAssetId` 为空时必填 |
| `certificationFiles[].materialType` | `string` | 材料类型，如 `id_card`、`qualification`、`other` |
| `certificationFiles[].sortOrder` | `integer` | 展示顺序，未传时按数组顺序生成 |

`GET /api/v1/app/profile/certification` 响应会返回 `certificationFiles`，字段结构与管理端学员响应一致。`certificationMaterials` 仍保留，但只作为旧版兼容字段。

#### 1.3 学习资源

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/app/learning/book-categories` | 分页查询图书分类 |
| GET | `/api/v1/app/learning/books` | 分页查询图书 |
| GET | `/api/v1/app/learning/books/{id}` | 图书详情 |
| GET | `/api/v1/app/learning/books/{bookId}/chapters/{chapterId}` | 图书章节详情 |
| GET | `/api/v1/app/learning/courses` | 分页查询课程 |
| GET | `/api/v1/app/learning/courses/{id}` | 课程详情 |
| GET | `/api/v1/app/learning/courses/{courseId}/videos/{videoId}` | 课程视频详情 |
| GET | `/api/v1/app/learning/exam-papers` | 分页查询考卷 |
| GET | `/api/v1/app/learning/exam-papers/{id}` | 考卷详情 |
| POST | `/api/v1/app/learning/exam-papers/{id}/submit` | 提交考卷答案 |
| GET | `/api/v1/app/learning/exam-records` | 分页查询考试记录 |
| GET | `/api/v1/app/learning/exam-records/{id}` | 考试结果与解析 |
| GET | `/api/v1/app/learning/podcasts` | 分页查询播客 |
| GET | `/api/v1/app/learning/podcasts/{id}` | 播客详情 |
| POST | `/api/v1/app/learning/records` | 同步学习记录 |
| GET | `/api/v1/app/learning/topics` | 分页查询专题 |
| GET | `/api/v1/app/learning/topics/{id}` | 专题详情 |

#### 1.4 专家

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/app/experts` | 分页查询可咨询专家 |
| GET | `/api/v1/app/experts/categories` | 分页查询专家分类 |
| GET | `/api/v1/app/experts/{id}` | 专家详情 |

#### 1.5 互动

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/app/interaction/feedbacks` | 提交反馈 |
| GET | `/api/v1/app/interaction/qa/questions` | 我的咨询列表 |
| POST | `/api/v1/app/interaction/qa/questions` | 发起咨询 |
| GET | `/api/v1/app/interaction/qa/questions/{id}` | 我的咨询详情 |

#### 1.6 知识库

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/app/knowledge/categories/tree` | 知识库分类树 |
| GET | `/api/v1/app/knowledge/entries` | 搜索知识库条目 |
| GET | `/api/v1/app/knowledge/entries/{id}` | 知识库条目详情 |

#### 1.7 直播

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/app/live-sessions` | 分页查询直播 |
| GET | `/api/v1/app/live-sessions/{id}` | 直播详情 |

### 2. 小程序独有 API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/app/auth/wechat-login` | 用户端微信授权登录 |

说明：

- 该接口用于小程序 `wx.login()` 后的微信授权登录。
- 当前仓库中未发现网页端专属登录接口。
- 当前仓库中未发现“微信手机号换取”独立接口。

## 测试账号

### 1. 管理员端

| 账号 | 密码 | 用途 |
| --- | --- | --- |
| `td_admin` | `Admin@123456` | 管理端全量联调账号 |
| `td_viewer` | `Admin@123456` | 管理端只读联调账号 |

### 2. 用户端

| 账号 | 密码 | 手机号 | 昵称 | 说明 |
| --- | --- | --- | --- | --- |
| `td_user_01` | `User@123456` | `13900000001` | 杏林新苗 | 已认证学员，适合完整学习链路 |
| `td_user_02` | `User@123456` | `13900000002` | 岐黄同道 | 认证中学员，适合审核中状态联调 |
| `td_user_03` | 无密码登录 | `13900000003` | 本草随行 | 微信身份样例账号，同时绑定一个专家身份 |

## 典型测试数据

### 1. 学员

| 学员编号 | 用户账号 | 姓名 | 地区 | 认证状态 |
| --- | --- | --- | --- | --- |
| `TD-STU-001` | `td_user_01` | 张青云 | 浙江省 杭州市 西湖区 | 已认证 |
| `TD-STU-002` | `td_user_02` | 李若水 | 江苏省 苏州市 姑苏区 | 待审核 |
| `TD-STU-003` | `td_user_03` | 王知秋 | 四川省 成都市 高新区 | 已驳回 |

#### 1.1 基础数据与认证材料

| 类型 | 样例 |
| --- | --- |
| 机构 | `省中医院`、`针灸研究所`、`中医药大学` |
| 执业类型 | `临床执业`、`教学科研`、`针灸推拿` |
| 认证材料 | `TD-STU-001` 有 2 条结构化认证材料 |
| 认证材料 | `TD-STU-002`、`TD-STU-003` 各有 1 条结构化认证材料 |

### 2. 首页与内容

| 类型 | 名称 |
| --- | --- |
| 首页分类 | `[TD]首页推荐` |
| 首页分类 | `[TD]热门专题` |
| 首页内容 | `[TD]首页课程推荐` |
| 首页内容 | `[TD]首页图书推荐` |
| 首页内容 | `[TD]首页专题推荐` |
| 资讯 | `[TD]春季养肝调气指南` |
| 资讯 | `[TD]针灸门诊带教札记` |
| 播客 | `[TD]黄帝内经夜读` |
| 播客 | `[TD]本草问答录` |
| 播客音频 | `[TD]第一期 经络循行` |
| 播客音频 | `[TD]第二期 脏腑表里` |
| 专题 | `[TD]针灸临床专题` |

### 3. 学习资源

| 类型 | 名称 |
| --- | --- |
| 课程 | `[TD]经络腧穴速学` |
| 课程 | `[TD]针灸临床入门` |
| 课程视频 | `[TD]经络总论` |
| 课程视频 | `[TD]常用腧穴` |
| 课程视频 | `[TD]临床案例导读` |
| 图书分类 | `[TD]针灸教材` |
| 图书分类 | `[TD]经典方剂` |
| 图书 | `[TD]针灸学临证读本` |
| 图书 | `[TD]方剂辨治精要` |
| 图书章节 | `[TD]第一章 经络基础` |
| 图书章节 | `[TD]第二章 常用腧穴` |
| 图书章节 | `[TD]第一章 补益方总论` |

### 4. 题库与考试

| 类型 | 名称 |
| --- | --- |
| 题目分类 | `[TD]针灸基础` |
| 题目分类 | `[TD]方剂学` |
| 单选题 | `[TD]针灸最常用的毫针规格是？` |
| 多选题 | `[TD]下列哪些属于针刺禁忌？` |
| 判断题 | `[TD]四物汤属于补血剂。` |
| 试卷 | `[TD]针灸基础试卷` |

### 5. 专家、互动与直播

| 类型 | 名称 |
| --- | --- |
| 专家分类 | `[TD]针灸专家` |
| 专家分类 | `[TD]方剂专家` |
| 专家 | `[TD]陈景岐` |
| 专家 | `[TD]宋本草` |
| 答疑问题 | `[TD]艾灸后局部发红是否正常？` |
| 答疑问题 | `[TD]四物汤与八珍汤如何区分？` |
| 直播 | `[TD]针灸实操直播` |
| 直播 | `[TD]方剂答疑直播` |
| 反馈 | `测试反馈：希望增加针灸案例演示。` |
| 反馈 | `测试反馈：移动端播放有轻微卡顿。` |

### 6. 知识库

| 类型 | 名称 |
| --- | --- |
| 知识库分类 | `[TD]中医基础理论` |
| 知识库分类 | `[TD]方剂学` |
| 知识条目 | `[TD]阴阳学说概览` |
| 知识条目 | `[TD]四物汤配伍要点` |

### 7. 统计相关样例

| 场景 | 样例 |
| --- | --- |
| 学习时长 | `TD-STU-001` 有课程和直播完成记录 |
| 学习进度 | `TD-STU-002` 有课程和图书未完成记录 |
| 考试结果 | `TD-STU-001` 通过 `[TD]针灸基础试卷` |
| 考试结果 | `TD-STU-002` 未通过 `[TD]针灸基础试卷` |

## 登录示例

### 1. 管理员登录

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "td_admin",
  "password": "Admin@123456"
}
```

### 2. 用户端账号密码登录

```http
POST /api/v1/app/auth/login
Content-Type: application/json

{
  "username": "td_user_01",
  "password": "User@123456"
}
```

### 3. 用户端短信登录

```http
POST /api/v1/app/auth/sms-code
Content-Type: application/json

{
  "mobile": "13900000001"
}
```

```http
POST /api/v1/app/auth/sms-login
Content-Type: application/json

{
  "mobile": "13900000001",
  "code": "123456"
}
```

### 4. 用户端小程序微信登录

```http
POST /api/v1/app/auth/wechat-login
Content-Type: application/json

{
  "code": "wx-login-code",
  "nickname": "杏林新苗",
  "avatarUrl": "https://example.com/avatar.png"
}
```

## 使用建议

- 联调时优先搜索 `[TD]` 前缀资源，不要依赖固定数据库 ID。
- 用户端如果要做网页端和小程序端适配，可以直接按“共用 API + 小程序独有微信登录 API”拆分。
- 真正的字段结构、请求体和响应体，以 `api/api.json` 和 Swagger UI 为准。
