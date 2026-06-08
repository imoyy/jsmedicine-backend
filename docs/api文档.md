# API 文档

本文档基于当前仓库中的 `api/api.json`、Controller 实现和现有测试数据整理，供前端联调、测试联调和接口排查使用。

## 基础说明

- 项目：`中医在线` 后端
- 文档更新时间：`2026-06-08`
- 认证方式：`Authorization: Bearer <token>`
- 统一返回：`ApiResponse<T>`
- 分页参数：`page`、`size`、`sort`

## 资源地址策略

当前仓库对“公开可读资源地址”的约定如下：

- 用户头像是当前唯一已经完成稳定读取链路的业务资源。`app_users.avatarUrl` 应返回 `/api/v1/files/{id}/content`，来源于 `file_assets.url` 的稳定公开路径，而不是对象存储临时签名 URL。
- `GET /api/v1/files/{id}/content` 当前可用于读取公开 `file_assets` 资源。只要业务表中的 URL 字段显式保存了这个路径，前端就可以把它当成稳定真相源使用。
- 课程、图书、资讯、播客、专题、直播、专家、知识库、首页分类、首页内容等模块里的 `coverUrl`，以及 `audioUrl`、`videoUrl`、`linkUrl`、`playbackUrl` 这类字段，当前仍是普通字符串 URL 字段；后端尚未统一强制它们都走 `file_assets` 稳定读取地址。
- 学员认证材料 `student_certification_files.sourceUrl` 当前继续允许保存历史外链或外部地址，用于兼容存量数据和未迁移资源。
- dev 种子里的 `https://example.com/assets/...`、`https://example.com/live/...` 仅用于页面占位和字段联调，不代表共享联调环境或后续环境中这些地址真实可访问。
- 新接入真实资源时，如果已经有公开 `file_assets` 记录，优先把业务字段写成 `/api/v1/files/{id}/content`；不要把对象存储临时签名 URL 当作长期真相源。

## 近期契约变更

- 管理端学员新增 `POST /api/v1/admin/students/import` 和 `GET /api/v1/admin/students/export`，用于 Excel 导入导出。
- `GET /api/v1/admin/system/audit-records` 补充 `targetTypeLabel`、`statusType`、`beforeStatusLabel`、`afterStatusLabel`、`auditorName`、`auditorUsername`，前端无需再自行硬编码审核资源类型和状态语义。
- 图书继续沿用“图书级单考卷”模型，图书请求体使用 `paperId` 维护绑定关系，图书响应新增 `paperTitle`。
- 管理端专题分项 `PUT /api/v1/admin/content/topics/{id}/items` 只允许 `course`、`book`、`podcast` 三类资源，后端统一做资源存在性、去重和排序归一化校验。
- 管理端专题分项响应新增 `itemTypeLabel`、`itemAvailable`、`itemTitle`、`itemSubtitle`、`itemCoverUrl`、`reviewStatus`、`publishStatus`。
- 用户端专题页契约已收口：专题列表改为显式卡片 DTO，专题详情按 `学习 / 视频 / 音频` 分区返回，并新增专题分区分页接口，不再返回 `items[].resource` 裸 `Object`。
- 首页内容契约已收口为“首页分类 + 业务资源引用配置”模型：前端以 `categoryId + targetId` 为主，`contentType` 改为兼容字段，由首页分类 `categoryCode` 自动推导；当前支持 `course`、`book`、`article`、`podcast`、`topic`、`knowledge`、`live` 七类资源。
- 管理端新增 `GET /api/v1/admin/content/home/candidates`，用于按首页分类分页拉取候选课程、图书、资讯、播客、专题、知识库或直播资源。
- 首页内容响应新增 `categoryName`、`contentTypeLabel`、`targetAvailable`、`targetTitle`、`targetCoverUrl`、`createdAt`、`updatedAt`，便于管理端直接渲染分类、资源封面和时间信息。
- 专家分类继续复用同一套接口，按 `parentId` 表达两级结构，响应新增 `parentCategoryName`、`level`。
- 管理端和用户端问答响应在保留原 `status` 的同时新增 `statusCode`、`statusLabel`。
- 用户反馈继续保留 `feedbackType` 自由文本模型，`contact` 明确表示主联系方式字段。
- 用户资料新增 `profileSignature`，用于用户端个人签名展示和管理端用户详情回显。
- 学员认证新增结构化材料字段 `certificationFiles`，旧字段 `certificationMaterials` 暂时保留为兼容字段。
- 学员和专家新增基础数据关联字段：`organizationId`、`practiceTypeId`；学员额外新增 `provinceCode`、`cityCode`、`districtCode`。
- 管理端专家新增可选 `userId`，用于把专家主数据绑定到前台登录用户；绑定后后端会同步维护用户 `EXPERT` 身份。
- 管理端新增 `PUT /api/v1/admin/users/{id}`，用于支撑“修改用户信息”弹窗一次性保存昵称、口号、状态、角色、学员绑定、地区和医院。
- 管理端新增基础数据接口 `/api/v1/admin/references/organizations`、`/api/v1/admin/references/practice-types`，用于机构和执业类型下拉。
- 学员管理新增 `POST /api/v1/admin/students`、`DELETE /api/v1/admin/students/{id}`、`POST /api/v1/admin/students/batch-delete`，并补齐 `gender`、`age`、`educationLevel` 字段。
- 系统管理新增管理员删除和角色删除接口，删除时分别带有“不可自删”和“不可删除 SUPER_ADMIN / 已绑定角色”的约束。
- 内容配置新增资讯 `source`、`tags`，播客 `speakerName`、`tags`，播客音频新增 `paperId`。
- 学习资源新增课程视频 `paperId`、图书 `totalPages`、图书章节 `startPage` / `pageCount`。
- 专家新增 `gender`、`birthDate`、`mobile`、`coverUrl` 展示字段。
- 直播新增 `speakerName`、`tags` 和直播视频子资源接口，用于支撑管理端直播配置弹窗和用户端回放列表展示。

## 端侧划分

- 管理员端：`/api/v1/auth/**`、`/api/v1/admin/**`
- 用户端：`/api/v1/app/**`
- 用户端网页端与小程序共用：除微信授权外的大部分用户端接口
- 用户端小程序独有：微信授权登录与微信授权后绑定手机号接口
- 用户端网页端独有：`GET /api/v1/app/auth/wechat-web/qr-config`、`POST /api/v1/app/auth/wechat-web/login`
- 端侧登录态边界：`/api/v1/app/**` 只接受用户端登录态；管理端 token 命中用户端受保护接口时应返回 `403 FORBIDDEN`，不再按“用户未登录”兼容处理。

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
| DELETE | `/api/v1/admin/system/admins/{id}` | 删除管理员 |
| PATCH | `/api/v1/admin/system/admins/{id}/password/reset` | 重置管理员密码 |
| PUT | `/api/v1/admin/system/admins/{id}/roles` | 绑定管理员角色 |
| PATCH | `/api/v1/admin/system/admins/{id}/status` | 修改管理员状态 |
| GET | `/api/v1/admin/system/audit-records` | 分页查询操作审计记录 |
| GET | `/api/v1/admin/system/permissions` | 查询权限列表 |
| GET | `/api/v1/admin/system/roles` | 分页查询角色 |
| POST | `/api/v1/admin/system/roles` | 新增角色 |
| GET | `/api/v1/admin/system/roles/{id}` | 查询角色详情 |
| PUT | `/api/v1/admin/system/roles/{id}` | 修改角色 |
| DELETE | `/api/v1/admin/system/roles/{id}` | 删除角色 |
| PUT | `/api/v1/admin/system/roles/{id}/permissions` | 绑定角色权限 |
| PATCH | `/api/v1/admin/system/roles/{id}/status` | 修改角色状态 |

#### 2.1 审核日志响应补充

`GET /api/v1/admin/system/audit-records` 当前除原有字段外，还会补充：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `targetTypeLabel` | `string` | 审核目标类型中文名，例如 `资讯`、`专题`、`图书` |
| `statusType` | `string` | 状态语义类型，当前可能为 `review_status`、`qa_status`、`feedback_status`、`login_result` |
| `beforeStatusLabel` | `string` | 变更前状态中文说明 |
| `afterStatusLabel` | `string` | 变更后状态中文说明 |
| `auditorName` | `string` | 审核人展示名，优先真实姓名，缺失时回退管理员账号 |
| `auditorUsername` | `string` | 审核人管理员账号 |

`targetType` 当前可见编码包括：`article`、`podcast`、`topic`、`course`、`book`、`knowledge_entry`、`live_session`、`qa_question`、`feedback`、`sys_admin_login`。

### 3. 用户与学员

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/users` | 分页查询用户 |
| GET | `/api/v1/admin/users/{id}` | 查询用户详情 |
| PUT | `/api/v1/admin/users/{id}` | 修改用户信息 |
| PATCH | `/api/v1/admin/users/{id}/status` | 修改用户状态 |
| GET | `/api/v1/admin/students` | 分页查询学员 |
| POST | `/api/v1/admin/students` | 新增学员 |
| POST | `/api/v1/admin/students/import` | 导入学员，`multipart/form-data`，文件字段名为 `file` |
| GET | `/api/v1/admin/students/export` | 导出学员，按列表筛选条件生成 Excel |
| GET | `/api/v1/admin/students/{id}` | 查询学员详情 |
| PUT | `/api/v1/admin/students/{id}` | 维护学员信息 |
| DELETE | `/api/v1/admin/students/{id}` | 删除学员 |
| POST | `/api/v1/admin/students/batch-delete` | 批量删除学员 |
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
| `gender` | `integer` | 性别枚举，详见 `Gender` |
| `age` | `integer` | 年龄，非负整数 |
| `educationLevel` | `string` | 文化程度/学历，最长 64 字符 |
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

`POST /api/v1/admin/students` 与 `PUT /api/v1/admin/students/{id}` 使用相同的 `AdminStudentUpsertRequest` 结构；`POST /api/v1/admin/students/batch-delete` 请求体为学员 ID 数组封装对象。

#### 3.3 学员导入与导出

`POST /api/v1/admin/students/import` 通过 Excel 批量新增学员，当前要求：

- 请求类型为 `multipart/form-data`，文件字段名固定为 `file`
- 文件扩展名仅支持 `.xls`、`.xlsx`
- 表头必须至少包含：`学号`、`姓名`、`性别`、`年龄`、`学历`、`手机号`、`身份证号`、`省份`、`省份编码`、`城市`、`城市编码`、`区县`、`区县编码`、`单位`、`机构ID`、`职称`、`执业类型ID`、`状态`

导入响应字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalRows` | `integer` | 实际处理的数据行数，不含空行和表头 |
| `successCount` | `integer` | 导入成功行数 |
| `failureCount` | `integer` | 导入失败行数 |
| `failures` | `array` | 失败明细 |
| `failures[].rowNumber` | `integer` | Excel 实际行号，从 1 开始计数 |
| `failures[].studentNo` | `string` | 失败行学号 |
| `failures[].realName` | `string` | 失败行姓名 |
| `failures[].errorMessage` | `string` | 失败原因 |

`GET /api/v1/admin/students/export` 复用学员列表筛选参数：`sort`、`keyword`、`status`、`certificationStatus`。响应为 `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` 文件下载，不再包在 `ApiResponse<T>` 中。

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
| GET | `/api/v1/admin/content/home/candidates` | 按首页分类分页查询候选资源 |
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

#### 4.1 资讯与播客字段补充

`POST /api/v1/admin/content/articles`、`PUT /api/v1/admin/content/articles/{id}` 额外支持：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `source` | `string` | 资讯来源，最长 128 字符 |
| `tags` | `array<string>` | 标签列表，最多 20 个，每个标签最长 32 字符 |

`GET /api/v1/admin/content/articles`、`GET /api/v1/admin/content/articles/{id}` 响应同步返回 `source`、`tags`。

`POST /api/v1/admin/content/podcasts`、`PUT /api/v1/admin/content/podcasts/{id}` 额外支持：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `speakerName` | `string` | 主讲人/主播名，最长 128 字符 |
| `tags` | `array<string>` | 标签列表，最多 20 个，每个标签最长 32 字符 |

`GET /api/v1/admin/content/podcasts`、`GET /api/v1/admin/content/podcasts/{id}` 响应同步返回 `speakerName`、`tags`。

`POST /api/v1/admin/content/podcasts/audios`、`PUT /api/v1/admin/content/podcasts/audios/{id}` 额外支持：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `paperId` | `integer` | 关联考卷 ID，可为空 |

#### 4.2 首页内容快捷配置规则

`POST /api/v1/admin/content/home/contents`、`PUT /api/v1/admin/content/home/contents/{id}` 当前按“首页分类 + 业务资源引用配置”模型工作，规则如下：

- `categoryId` 必填，且必须指向启用中的首页分类
- 首页分类 `categoryCode` 是资源类型真相源，当前支持 `course`、`book`、`article`、`podcast`、`topic`、`knowledge`、`live`
- `contentType` 改为兼容字段；通常不需要前端传值，若传值则必须与 `categoryCode` 推导出的资源类型一致
- `targetId` 必填，且必须指向首页分类绑定模块下真实存在的资源
- 同一首页分类下不允许重复绑定同一个 `targetId`
- `title`、`coverUrl` 为兼容字段，保存时以后端按目标资源自动派生的标题和封面为准
- `startAt` 和 `endAt` 可为空；同时传值时必须满足 `startAt < endAt`

`GET /api/v1/admin/content/home/contents` 及详情型响应当前包含：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `categoryName` | `string` | 首页分类名称 |
| `contentTypeLabel` | `string` | 首页内容类型中文名 |
| `targetAvailable` | `boolean` | 目标资源当前是否可用 |
| `targetTitle` | `string` | 目标资源标题 |
| `targetCoverUrl` | `string` | 目标资源封面地址 |
| `createdAt` | `string(date-time)` | 创建时间 |
| `updatedAt` | `string(date-time)` | 更新时间 |

`GET /api/v1/admin/content/home/candidates` 用于支撑首页内容配置弹窗中的资源下拉与搜索，请求参数如下：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `categoryId` | `integer` | 是 | 首页分类 ID，后端会按该分类的 `categoryCode` 自动路由候选资源类型 |
| `page` | `integer` | 否 | 页码，默认 `1` |
| `size` | `integer` | 否 | 每页条数，默认 `20` |
| `keyword` | `string` | 否 | 标题/作者/讲师/关键词等关键字搜索 |

候选资源响应记录当前统一包含：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `integer` | 候选资源 ID |
| `title` | `string` | 候选资源标题 |
| `coverUrl` | `string` | 候选资源封面 |
| `subtitle` | `string` | 候选资源副标题，如作者、讲师、来源、摘要 |
| `resourceStatus` | `string` | 候选资源当前状态，例如 `draft`、`pending`、`approved`、`published`、`live` |
| `available` | `boolean` | 当前是否可用于首页绑定 |

#### 4.3 专题分项配置规则

`PUT /api/v1/admin/content/topics/{id}/items` 当前是专题分项唯一正式入口，规则如下：

- `itemType` 仅支持 `course`、`book`、`podcast`
- `itemId` 必须指向真实存在的对应资源
- 同一专题内禁止重复绑定“同类型 + 同资源”组合
- `sortOrder` 可为空；为空时后端按请求顺序自动归一化排序

接口响应新增字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `itemTypeLabel` | `string` | 分项类型中文名 |
| `itemAvailable` | `boolean` | 分项资源当前是否可用 |
| `itemTitle` | `string` | 分项主标题 |
| `itemSubtitle` | `string` | 分项副标题 |
| `itemCoverUrl` | `string` | 分项封面 |
| `reviewStatus` | `string` | 分项审核状态 |
| `publishStatus` | `string` | 分项发布状态 |

### 5. 学习资源

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/admin/learning/book-categories` | 分页查询图书分类 |
| POST | `/api/v1/admin/learning/book-categories` | 新增图书分类 |
| GET | `/api/v1/admin/learning/book-categories/{id}` | 查询图书分类详情 |
| PUT | `/api/v1/admin/learning/book-categories/{id}` | 修改图书分类 |
| DELETE | `/api/v1/admin/learning/book-categories/{id}` | 删除图书分类 |
| GET | `/api/v1/admin/learning/book-categories/{id}/books` | 分页查询分类下图书 |
| POST | `/api/v1/admin/learning/book-categories/{id}/books` | 分类批量加入图书 |
| DELETE | `/api/v1/admin/learning/book-categories/{id}/books` | 分类批量移除图书 |
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

#### 5.1 学习资源字段补充

图书分类管理当前补充了分类详情、分类下图书查询和分类维度批量绑定能力。图书继续采用单分类模型：一本图书只有一个 `categoryId`，不是多分类关系表。

`GET /api/v1/admin/learning/book-categories` 与 `GET /api/v1/admin/learning/book-categories/{id}` 当前统一返回：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `integer` | 图书分类 ID |
| `parentId` | `integer` | 父级分类 ID，可为空 |
| `categoryName` | `string` | 分类名称 |
| `sortOrder` | `integer` | 排序值 |
| `status` | `string` | 分类状态 |
| `createdAt` | `string(date-time)` | 创建时间 |
| `updatedAt` | `string(date-time)` | 更新时间 |

`GET /api/v1/admin/learning/book-categories/{id}/books` 用于分类编辑弹窗内查看当前分类已绑定图书，请求参数如下：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | `integer` | 否 | 页码，默认 `1` |
| `size` | `integer` | 否 | 每页条数，默认 `20` |
| `sort` | `string` | 否 | 当前支持 `sortOrderAsc`，其余值按更新时间倒序 |
| `keyword` | `string` | 否 | 图书名称或作者关键字 |

分类下图书响应记录当前统一包含：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | `integer` | 图书 ID |
| `categoryId` | `integer` | 当前分类 ID |
| `bookName` | `string` | 图书名称 |
| `author` | `string` | 作者 |
| `coverUrl` | `string` | 封面地址 |
| `reviewStatus` | `string` | 审核状态 |
| `publishStatus` | `string` | 发布状态 |
| `updatedAt` | `string(date-time)` | 更新时间 |

`POST /api/v1/admin/learning/book-categories/{id}/books` 与 `DELETE /api/v1/admin/learning/book-categories/{id}/books` 请求体统一为：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `bookIds` | `array<integer>` | 是 | 需要加入或移除的图书 ID 列表 |

处理语义：

- 批量加入：把 `bookIds` 对应图书的 `categoryId` 统一改为路径中的分类 ID。
- 批量移除：仅允许移除当前 `categoryId = {id}` 的图书；移除后直接把 `books.categoryId` 置空。

`POST /api/v1/admin/learning/courses/videos`、`PUT /api/v1/admin/learning/courses/videos/{id}` 额外支持：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `paperId` | `integer` | 关联考卷 ID，可为空 |

`POST /api/v1/admin/learning/books`、`PUT /api/v1/admin/learning/books/{id}` 额外支持：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `totalPages` | `integer` | 图书总页数，非负整数 |
| `paperId` | `integer` | 图书绑定的考卷 ID；当前图书采用单考卷模型，留空表示未配置 |

图书响应 `BookResponse` 额外返回：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `paperTitle` | `string` | 当前绑定考卷名称；未配置或考卷已失效时返回 `null` |

`POST /api/v1/admin/learning/books/chapters`、`PUT /api/v1/admin/learning/books/chapters/{id}` 额外支持：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `startPage` | `integer` | 章节起始页码，最小为 1 |
| `pageCount` | `integer` | 章节页数，非负整数 |

相关响应 `CourseVideoResponse`、`BookResponse`、`BookChapterResponse` 会同步返回这些字段。图书保存时如果传入 `paperId`，后端会校验该考卷必须真实存在。

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
| `gender` | `integer` | 性别枚举，详见 `Gender` |
| `birthDate` | `string(date)` | 出生日期 |
| `mobile` | `string` | 手机号，最长 32 字符 |
| `organizationId` | `integer` | 机构 ID，对应 `organizations.id` |
| `practiceTypeId` | `integer` | 执业类型 ID，对应 `practice_types.id` |
| `coverUrl` | `string` | 专家封面图，最长 512 字符 |

专家响应同步返回 `userId`、`gender`、`birthDate`、`mobile`、`organizationId`、`practiceTypeId`、`coverUrl`。当 `userId` 非空时，后端会确保该前台用户存在激活的 `EXPERT` 身份记录。

#### 6.2 专家分类层级规则

专家分类继续复用 `GET/POST/PUT/DELETE /api/v1/admin/experts/categories` 同一套接口，不新增“二级科室专用接口”。当前规则：

- `parentId = null` 表示一级分类
- `parentId = 一级分类 ID` 表示二级分类
- 不允许创建三级分类
- 删除父分类前，必须先清空其子分类和专家绑定

分类响应补充字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `parentCategoryName` | `string` | 父级分类名称；一级分类时为空 |
| `level` | `integer` | 当前层级，一级为 `1`，二级为 `2` |

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

#### 7.1 反馈与答疑字段语义

反馈字段语义：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `feedbackType` | `string` | 自由文本分类，当前可由前端按页面自行约定，如 `功能建议`、`内容纠错` |
| `contact` | `string` | 主联系方式字段，可填写手机号、微信号、邮箱等任一种便于联系的信息 |

问答状态字段语义：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `status` | `string / number` | 兼容旧版状态字段，继续保留 |
| `statusCode` | `string` | 稳定状态编码，如 `PENDING`、`ANSWERED`、`CLOSED` |
| `statusLabel` | `string` | 稳定状态中文名，如 `待回复`、`已回复`、`已关闭` |

上述 `statusCode`、`statusLabel` 同时适用于管理端问答接口和用户端“我的咨询”接口。

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
| GET | `/api/v1/admin/live-sessions/{liveSessionId}/videos` | 分页查询直播视频 |
| PUT | `/api/v1/admin/live-sessions/{id}` | 修改直播 |
| DELETE | `/api/v1/admin/live-sessions/{id}` | 删除直播 |
| PATCH | `/api/v1/admin/live-sessions/{id}/review` | 审核直播 |
| POST | `/api/v1/admin/live-sessions/videos` | 新增直播视频 |
| PUT | `/api/v1/admin/live-sessions/videos/{id}` | 修改直播视频 |
| DELETE | `/api/v1/admin/live-sessions/videos/{id}` | 删除直播视频 |

#### 9.1 直播字段补充

`POST /api/v1/admin/live-sessions`、`PUT /api/v1/admin/live-sessions/{id}` 额外支持：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `speakerName` | `string` | 主讲人姓名，最长 128 字符；未传时回退为 `anchorName` |
| `tags` | `array<string>` | 标签列表，最多 20 个，每个标签最长 32 字符 |

直播响应同步返回：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `speakerName` | `string` | 主讲人姓名 |
| `tags` | `array<string>` | 标签列表 |
| `videos` | `array` | 直播视频列表；详情接口默认返回，分页接口不返回 |

直播视频子资源 `POST /api/v1/admin/live-sessions/videos`、`PUT /api/v1/admin/live-sessions/videos/{id}` 请求字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `liveSessionId` | `integer` | 直播 ID |
| `title` | `string` | 视频标题 |
| `videoUrl` | `string` | 视频地址 |
| `durationSeconds` | `integer` | 视频时长，单位秒 |
| `sortOrder` | `integer` | 排序值 |
| `status` | `integer` | 状态：`1` 启用，`0` 禁用 |

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
| POST | `/api/v1/admin/references/organizations` | 新增机构 |
| GET | `/api/v1/admin/references/organizations/{id}` | 查询机构详情 |
| PUT | `/api/v1/admin/references/organizations/{id}` | 修改机构 |
| DELETE | `/api/v1/admin/references/organizations/{id}` | 删除机构 |
| GET | `/api/v1/admin/references/practice-types` | 查询执业类型列表，支持按 `keyword`、`parentId`、`status` 过滤 |
| POST | `/api/v1/admin/references/practice-types` | 新增执业类型 |
| GET | `/api/v1/admin/references/practice-types/{id}` | 查询执业类型详情 |
| PUT | `/api/v1/admin/references/practice-types/{id}` | 修改执业类型 |
| DELETE | `/api/v1/admin/references/practice-types/{id}` | 删除执业类型 |

## 用户端 API

### 1. 网页端与小程序共用 API

#### 1.1 认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/app/auth/login` | 用户端账号密码登录 |
| POST | `/api/v1/app/auth/sms-code` | 发送用户端手机号验证码 |
| POST | `/api/v1/app/auth/sms-login` | 用户端手机号验证码登录 |
| POST | `/api/v1/app/auth/wechat-bind-mobile` | 用户端微信授权后绑定手机号并完成登录 |
| POST | `/api/v1/app/auth/logout` | 用户端退出登录 |
| GET | `/api/v1/app/auth/me` | 获取当前登录用户 |
| GET | `/api/v1/app/auth/status` | 校验用户端登录状态 |

#### 1.2 个人中心

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/app/profile` | 获取用户端个人资料 |
| PUT | `/api/v1/app/profile` | 修改用户端个人资料 |
| POST | `/api/v1/app/profile/avatar/upload-url` | 申请头像上传地址 |
| POST | `/api/v1/app/profile/avatar/confirm` | 确认头像上传并更新头像 |
| GET | `/api/v1/app/profile/browse-histories` | 查询浏览记录 |
| GET | `/api/v1/app/profile/certification` | 查询学员认证结果 |
| POST | `/api/v1/app/profile/certification` | 提交学员认证申请 |
| GET | `/api/v1/app/profile/favorites` | 查询我的收藏 |
| GET | `/api/v1/app/profile/summary` | 查询个人中心聚合信息 |

##### 1.2.1 头像上传链路

用户端自定义头像不再通过 `PUT /api/v1/app/profile` 直接写 `avatarUrl`，而是走对象存储签名上传链路：

1. 调用 `POST /api/v1/app/profile/avatar/upload-url` 申请上传地址。
2. 前端使用响应中的 `method` 和 `uploadUrl` 直接上传头像文件到对象存储。
3. 上传成功后调用 `POST /api/v1/app/profile/avatar/confirm` 确认上传，后端会校验对象存在、大小和 `contentType`，写入 `file_assets`，再把用户头像更新为稳定读取地址。

`POST /api/v1/app/profile/avatar/upload-url` 请求体：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `originalName` | `string` | 原始文件名，最长 255 字符 |
| `contentType` | `string` | 文件 MIME 类型，仅支持 `image/jpeg`、`image/png`、`image/webp` |
| `fileSize` | `integer` | 文件字节数，必须大于 0，当前上限 5 MB |

响应体字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `method` | `string` | 当前固定为 `PUT` |
| `uploadUrl` | `string` | 对象存储签名上传地址 |
| `bucketName` | `string` | 对象存储桶名 |
| `objectKey` | `string` | 后端为当前用户生成的对象 key |
| `contentType` | `string` | 归一化后的 MIME 类型 |
| `fileSize` | `integer` | 本次申请的文件字节数 |
| `expiresAt` | `string(date-time)` | 签名地址过期时间 |

`POST /api/v1/app/profile/avatar/confirm` 请求体：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `objectKey` | `string` | 上传完成后的对象 key，必须与当前用户申请的头像前缀一致 |
| `originalName` | `string` | 原始文件名，可为空；为空时后端使用对象名回填 |

确认成功后返回最新 `AppProfileResponse`，其中 `avatarUrl` 为后端稳定读取地址，如 `/api/v1/files/123/content`。

##### 1.2.2 个人资料字段补充

`PUT /api/v1/app/profile` 请求支持以下补充字段，`GET /api/v1/app/profile` 响应同步返回：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `profileSignature` | `string` | 用户个人签名，最长 255 字符 |

说明：

- `avatarUrl` 仍会在个人资料响应中返回，但该字段现在由头像确认接口驱动更新。
- 如果继续在 `PUT /api/v1/app/profile` 中直接传入与当前值不同的 `avatarUrl`，后端会返回业务错误。

##### 1.2.3 学员认证字段补充

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
| GET | `/api/v1/app/learning/topics/{id}/sections/{sectionType}` | 分页查询专题分区内容 |

##### 1.3.1 用户端学习资源返回字段补充

用户端学习资源相关响应同步补齐以下字段：

| 资源 | 字段 | 说明 |
| --- | --- | --- |
| 课程视频 | `paperId` | 视频关联考卷 ID |
| 图书 | `totalPages` | 图书总页数 |
| 图书章节 | `startPage` / `pageCount` | 章节起始页和页数 |
| 播客 | `speakerName` / `tags` | 主讲人和标签列表 |
| 播客音频 | `paperId` | 音频关联考卷 ID |

##### 1.3.2 专题页页面化契约

当前用户端专题接口已按官网页面结构收口：

- `GET /api/v1/app/learning/topics` 返回 `AppTopicCardResponse`，用于专题列表卡片。除原有标题、摘要、封面、发布时间外，响应新增 `tags`、`favoriteCount`、`favorited`，前端可直接渲染专题主标签与收藏态。
- `GET /api/v1/app/learning/topics/{id}` 返回 `AppTopicDetailResponse`，使用 `sections` 数组表达页面分区，而不是继续返回平铺 `items`。
- 每个分区对象包含 `sectionType`、`sectionLabel`、`total`、`hasMore`、`items`。当前固定约定如下：
  - `learning`：映射专题内 `book` 资源，分区标题为 `学习`
  - `video`：映射专题内 `course` 资源，分区标题为 `视频`
  - `audio`：映射专题内 `podcast` 资源，分区标题为 `音频`
- 分区首屏 `items` 最多返回 3 条预览数据；完整列表通过 `GET /api/v1/app/learning/topics/{id}/sections/{sectionType}` 分页获取。
- 分区分页和首屏预览统一返回 `AppTopicResourceCardResponse`，字段固定为 `resourceType`、`resourceTypeLabel`、`resourceId`、`title`、`subtitle`、`coverUrl`、`tags`、`browseCount`、`favoriteCount`、`favorited`、`progressPercent`、`studySeconds`。
- 后端查询时会过滤无法展示的专题关联项，联调时不需要再兼容 `resource = null` 或未知结构的裸对象分支。

#### 1.4 专家

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/app/experts` | 分页查询可咨询专家 |
| GET | `/api/v1/app/experts/categories` | 分页查询专家分类 |
| GET | `/api/v1/app/experts/{id}` | 专家详情 |

用户端专家响应同步返回 `gender`、`birthDate`、`mobile`、`coverUrl`，便于详情页展示专家基础画像。

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

#### 1.7 公共文件读取

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/files/{id}/content` | 读取公开图片资源内容 |

说明：

- 该接口当前用于读取用户端已确认上传的公开头像图片。
- `avatarUrl` 等对外图片字段应优先返回这个稳定路径，而不是对象存储临时签名 URL。

#### 1.8 直播

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/app/live-sessions` | 分页查询直播 |
| GET | `/api/v1/app/live-sessions/{id}` | 直播详情 |

用户端直播响应同步返回 `speakerName`、`tags`，详情接口还会返回已启用的 `videos` 列表，便于直播结束后展示回放。

### 2. 小程序独有 API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/v1/app/auth/wechat-login` | 用户端微信授权登录 |
| POST | `/api/v1/app/auth/wechat-bind-mobile` | 用户端微信授权后绑定手机号 |

说明：

- `POST /api/v1/app/auth/wechat-login` 用于小程序 `wx.login()` 后的微信授权登录。
- 老用户：后端通过 `openid` 命中已存在的 `app_users.wechat_open_id`，直接返回登录态。
- 新用户：后端不立即写入 `app_users`，而是返回临时绑定态，前端跳转手机号绑定页。
- `POST /api/v1/app/auth/wechat-bind-mobile` 复用现有短信验证码能力；前端先调用 `/api/v1/app/auth/sms-code`，再提交 `bindToken + mobile + code` 完成入库或补绑。

`POST /api/v1/app/auth/wechat-login` 响应要点：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `registered` | `boolean` | 是否已注册并直接登录成功 |
| `needBindMobile` | `boolean` | 是否需要先绑定手机号 |
| `bindToken` | `string` | 新用户绑定手机号时使用的临时令牌，老用户为空 |
| `tokenType` | `string` | 老用户直接登录时返回，固定为 `Bearer` |
| `accessToken` | `string` | 老用户直接登录时返回 |
| `expiresIn` | `integer` | 老用户直接登录时为 token 过期秒数；新用户场景下为 `bindToken` 过期秒数 |
| `user` | `object` | 老用户直接登录时返回的用户概要，新用户场景为空 |

`POST /api/v1/app/auth/wechat-bind-mobile` 请求字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `bindToken` | `string` | `wechat-login` 返回的临时绑定令牌 |
| `mobile` | `string` | 11 位中国大陆手机号 |
| `code` | `string` | 通过 `/api/v1/app/auth/sms-code` 获取的短信验证码 |

### 3. 网页端独有 API

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/v1/app/auth/wechat-web/qr-config` | 获取官网微信扫码登录所需的 `appId`、`redirectUri`、`scope`、`state` |
| POST | `/api/v1/app/auth/wechat-web/login` | 官网微信扫码回调后，使用 `code + state` 完成登录或进入绑定手机号链路 |

说明：

- `wechat-web/login` 返回结构与小程序 `wechat-login` 一致，仍使用 `AppWechatLoginResponse`
- 老用户优先按 `wechat_union_id` 恢复，其次按 `wechat_web_open_id` 恢复
- 未注册用户仍返回 `needBindMobile=true`，后续继续调用 `POST /api/v1/app/auth/wechat-bind-mobile`

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
| 直播视频 | `[TD]针灸手法演示回放` |
| 直播视频 | `[TD]课后答疑回放` |
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

老用户响应示例：

```json
{
  "success": true,
  "code": "OK",
  "message": "Success",
  "data": {
    "registered": true,
    "needBindMobile": false,
    "bindToken": null,
    "tokenType": "Bearer",
    "accessToken": "app-user-token",
    "expiresIn": 7200,
    "user": {
      "id": 1001,
      "username": "u13900000001",
      "nickname": "杏林新苗",
      "avatarUrl": "https://example.com/avatar.png",
      "lastLoginAt": "2026-05-28T18:30:00"
    }
  }
}
```

新用户响应示例：

```json
{
  "success": true,
  "code": "OK",
  "message": "Success",
  "data": {
    "registered": false,
    "needBindMobile": true,
    "bindToken": "wechat-bind-token",
    "tokenType": null,
    "accessToken": null,
    "expiresIn": 600,
    "user": null
  }
}
```

### 5. 用户端小程序微信绑定手机号

```http
POST /api/v1/app/auth/wechat-bind-mobile
Content-Type: application/json

{
  "bindToken": "wechat-bind-token",
  "mobile": "13900000003",
  "code": "123456"
}
```

## 使用建议

- 联调时优先搜索 `[TD]` 前缀资源，不要依赖固定数据库 ID。
- 用户端如果要做网页端和小程序端适配，可以直接按“共用 API + 小程序独有微信登录 / 绑定手机号 API”拆分。
- 真正的字段结构、请求体和响应体，以 `api/api.json` 和 Swagger UI 为准。
