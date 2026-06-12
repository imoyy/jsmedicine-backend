# 质量保障与功能完善度提升计划

## 状态说明

- `[x]` 已完成：已实现并通过必要验证。
- `[~]` 进行中：当前阶段正在推进。
- `[ ]` 待开始：尚未进入实现。
- `[!]` 阻塞：存在依赖、需求或环境问题，需要先处理。

## 当前阶段定位

阶段 0 至阶段 12 的核心功能首轮实现已完成。接下来进入质量保障与功能完善度提升阶段。

本轮计划目标：

- 稳定管理端与用户端核心流程。
- 修复联调发现的功能缺口与契约不一致。
- 强化权限、异常、事务、审计、日志和数据一致性。
- 不新增测试文件，通过接口联调、脚本化请求、现有测试、编译打包和 Swagger 契约验证降低回归风险。
- 保持 `api/api.json` 与真实接口一致。
- 准备共享联调与上线前检查清单。

## 执行规则

- 每次开始任务前先读本文件，优先处理当前 `[~]` 阶段未完成任务。
- 发现任务拆分不合理或顺序需要调整时，直接更新本文件并说明原因。
- 每完成一个任务，更新对应复选框。
- 每完成一个阶段，必须满足该阶段验收标准后再改为 `[x]`。
- 修改接口后必须导出最新 `api/api.json`。
- 涉及数据库变更时，从 `V13__...sql` 起新增迁移，不修改 `V1` 到 `V12`。
- 下个质量保障阶段不新增测试文件，不修改既有测试文件；可运行现有测试命令作为健康检查。

## 阶段 Q0：基线审计与问题台账

状态：`[x]` 已完成

目标：建立质量提升阶段的事实基线，先明确当前系统真实状态和优先级。

- `[x]` 梳理当前所有管理端接口，按模块标记：已联调、待联调、需补强、疑似废弃。
- `[x]` 梳理当前所有用户端接口，按登录、个人中心、学习、考试、咨询、知识库、直播、反馈分类。
- `[x]` 对照《管理端使用手册》梳理管理端页面字段、业务入口与当前数据库/实体落地差异。
- `[x]` 对比 `api/api.json` 与 Controller，确认接口契约完整且无明显遗漏。
- `[x]` 梳理 Flyway 当前版本、表结构、关键索引和已知迁移风险。
- `[x]` 整理现有测试运行状态、接口联调缺口和脚本化验收缺口。
- `[x]` 建立缺陷与完善项清单，按 P0 / P1 / P2 标优先级。

验收标准：

- `[x]` 形成可执行的问题清单。
- `[x]` P0 问题有明确修复顺序。
- `[x]` 当前接口契约可被前端和联调人员使用。

当前已识别数据库问题：

- `[x]` 识别账户域核心建模缺口：`app_users` 缺少用户公共资料补充位，且未显式建模前台用户业务身份。
- `[x]` 识别前台用户、学员、专家三层关系不清：`students.user_id` 缺唯一约束，`experts` 缺 `user_id` 绑定，无法稳定承接手册中的“普通/学员/专家”分类。
- `[x]` 识别地区与机构建模不足：`students` 仅存省市区名称与 `organization` 文本，难以支撑手册中的地区筛选、医院选择和地区统计。
- `[x]` 识别基础字典缺口：手册中的“执业类型”缺少独立字典表和业务关联字段。
- `[x]` 识别认证材料建模不规范：`students.certification_materials` 以 JSON 文本保存，不利于文件管理、审计和扩展。
- `[x]` 形成账户域数据库整改方案清单，明确 `V13` 及后续迁移拆分顺序、回填策略和接口影响范围。

当前已确认契约与迁移基线：

- `[x]` `api/api.json` 与当前 17 个 Controller 的 182 个 HTTP 操作静态比对一致，未发现 Controller 有而 Swagger JSON 缺失的接口，也未发现 Swagger 残留旧路径。
- `[x]` Flyway 当前最新版本为 `V12__stage11_statistics_permissions_and_indexes.sql`，后续数据库调整必须从 `V13__...sql` 起新增迁移。
- `[x]` 当前数据库迁移主线为：`V1` 建基表，`V3`/`V4` 补账户域与互动域字段，`V6`~`V11` 补模块权限种子，`V12` 补统计索引。
- `[x]` 当前已识别迁移风险：账户域主数据边界不清、互动域局部字段约束偏弱、Windows 下 Maven/脚本命令写法需要显式适配 PowerShell。

当前测试与脚本化验收基线：

- `[x]` 现有测试文件仅 2 个测试类：`JsmedicineApplicationTests`、`Stage2InfrastructureIntegrationTest`。
- `[x]` 现有测试覆盖范围仅包括应用启动、未认证响应、参数校验统一响应、操作审计切面，不覆盖真实业务闭环。
- `[x]` 使用 Windows 仓库包装器命令 `.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" test` 已验证通过，结果为 `Tests run: 4, Failures: 0, Errors: 0, Skipped: 0`。
- `[x]` 当前仓库现成脚本化资产仅发现 `export-api.ps1`，缺少认证、权限、核心业务流程的接口验收脚本。
- `[x]` 当前脚本化验收缺口明确存在，后续需以 PowerShell 或 HTTP 请求脚本补齐认证、用户、学员、内容、学习、互动、统计等高风险路径。

当前问题优先级清单：

- `[x]` `P0` 账户域建模缺口：前台账号、用户业务身份、学员档案、专家档案边界不清，已直接影响管理端手册中的用户分类、专家绑定和医院/地区选择落地。
- `[x]` `P0` 专家与前台用户缺少稳定绑定：`experts` 无 `user_id`，现有模型无法表达“一个登录用户是专家”。
- `[x]` `P1` 学员与用户关系约束不足：`students.user_id` 缺唯一约束，存在一个用户多学员档案的脏数据风险。
- `[x]` `P1` 地区、医院/机构、执业类型仍以自由文本或缺表方式存在，不利于筛选、统计、导入和联调一致性。
- `[x]` `P1` 学员认证材料以 JSON 文本存储，后续审计、文件替换、结构化查询和数据迁移成本高。
- `[x]` `P1` 自动化验收覆盖不足：当前无核心业务接口脚本，真实联调风险主要依赖人工发现。
- `[x]` `P2` 反馈类型、答疑回答主体、编译参数警告等问题存在规范性和可维护性风险，但不阻塞当前账户域整改。

当前 P0 修复顺序：

- `[x]` 第 1 优先级：形成账户域数据库整改方案清单，明确 `app_users`、`students`、`experts`、身份关系表、机构和地区字典的目标模型。
- `[x]` 第 2 优先级：输出 `V13` 起的迁移拆分与回填顺序，避免一次性大迁移扩大风险。
- `[x]` 第 3 优先级：在数据库模型收敛后，再补脚本化接口验收，验证账户域与联动接口契约。

## 阶段 Q1：认证、权限与会话稳定性

状态：`[~]` 进行中

目标：保证管理端和用户端身份边界稳定，避免越权、误鉴权和会话恢复失败。

- `[x]` 验证管理端登录、退出、`/api/v1/auth/me`、`/api/v1/auth/status`。
- `[ ]` 验证用户端账号密码登录、短信登录、微信登录、退出、`/api/v1/app/auth/me`、`/api/v1/app/auth/status`。
- `[x]` 调整用户端微信登录首登链路：新用户先绑定手机号再入库，老用户按 `wechat_open_id` 直接恢复登录态。
- `[x]` 新增官网微信扫码登录链路：提供二维码配置与 `state` 校验，扫码后按 `wechat_union_id` / `wechat_web_open_id` 恢复登录态，未注册用户继续走手机号绑定。
- `[x]` 开发阶段暂切换为后端 mock 的官网扫码登录链路：`WECHAT_WEB_MOCK_ENABLED=true` 时允许 `qr-config` 返回占位配置并直接用 mock `code -> openid/unionid` 联调；产品官网上线并申请通过网站应用后，需切回真实微信扫码配置并完成联调验收。
- `[ ]` 验证 Redis 中管理端与用户端 token key 前缀隔离。
- `[ ]` 验证 token 过期、无效 token、缺失 token 的统一错误响应。
- `[x]` 检查所有管理端敏感接口是否具备 `@PreAuthorize`。
- `[x]` 检查新增权限迁移是否完整绑定 `SUPER_ADMIN`。
- `[ ]` 使用脚本化请求覆盖认证与权限核心场景，不新增测试文件。

当前认证边界加固记录：

- `2026-06-02`：已在 `SecurityConfig` 增加 `/api/v1/app/** -> ROLE_APP_USER` 路径级约束，避免管理端 token 继续误打用户端接口并得到 `200/401` 混杂结果；后续仍需通过实际请求补齐 admin/app 跨端 token 验证验收。
- `2026-06-02`：已按 `/api/v1/admin/**` controller 方法做静态扫描，当前管理端敏感接口均已显式标注 `@PreAuthorize`；未发现新的漏鉴权入口。
- `2026-06-02`：静态对账 `@PreAuthorize` 权限码与 Flyway 权限种子后，补充 `V23__seed_system_admin_permissions.sql`，正式纳入 `sys:admin:view/create/update/disable/reset-password` 并绑定 `SUPER_ADMIN`；同时修正 dev 验收种子里这组权限的旧后台路径。
- `2026-06-09`：根据联调复核修复管理端专家列表异常映射。共享联调环境中 `superadmin` 登录后缺少 `expert:view`，访问 `GET /api/v1/admin/experts` 时方法级鉴权异常被 `GlobalExceptionHandler` 兜底吞成 `500`；已补安全异常到 `403/401` 的显式映射，并新增 `V26__backfill_expert_view_permission.sql` 幂等回填 `expert:view -> SUPER_ADMIN` 绑定。
- `2026-06-09`：本地 `dev` 环境已通过实际请求完成管理端账号密码登录、退出、`/api/v1/auth/me`、`/api/v1/auth/status` 以及 admin/app 双向跨端 token 验证；其中本地 `app token -> /api/v1/auth/me` 已稳定返回 `403 FORBIDDEN`，未再出现 `500 INTERNAL_ERROR`。
- `2026-06-09`：共享联调环境按同组认证请求复核时，`app token -> /api/v1/auth/me` 仍返回 `500 INTERNAL_ERROR`，与本地最新代码结果不一致；当前判断为测试环境尚未部署最新安全异常映射修复，而不是仓库当前代码仍存在回归。

验收标准：

- `[ ]` 管理端与用户端 token 不能互相冒用。
- `[ ]` 权限不足返回 `FORBIDDEN`，未认证返回 `UNAUTHORIZED`。
- `[ ]` 登录态恢复稳定，重启应用后 Redis 现有 session 可按预期处理。
- `[ ]` 现有 `./mvnw -Dmaven.repo.local=.m2/repository test` 通过。

## 阶段 Q2：接口契约、响应结构与错误码统一

状态：`[~]` 进行中

目标：减少前端联调不确定性，保证接口输入输出稳定。

- `[ ]` 检查所有 Controller 是否返回 `ApiResponse<T>`。
- `[ ]` 检查分页接口是否统一使用 `page`、`size`、`sort` 和 `PageResponse<T>`。
- `[ ]` 检查请求 DTO 的校验注解和错误提示。
- `[ ]` 检查业务异常是否使用 `BusinessException` 与 `ErrorCode`。
- `[ ]` 检查接口路径是否符合 `/api/v1/admin/...` 和 `/api/v1/app/...` 端侧边界。
- `[ ]` 检查 Swagger schema 是否存在裸 Entity 泄漏。
- `[ ]` 修复命名不清晰、响应字段不稳定或前后端难以理解的接口。
- `[x]` 收敛官网专题页页面化契约：消除 `AppTopicItemResponse.resource` 裸 `Object` 返回，明确专题标签、详情分区、卡片 DTO 与“更多”分页接口 schema。
- `[x]` 导出最新 `api/api.json`。

验收标准：

- `[x]` `api/api.json` 可被前端直接作为最新契约使用。
- `[ ]` 常见错误场景响应结构一致。
- `[ ]` 不存在管理端与用户端混用路径或混用鉴权语义。

当前契约收敛重点：

- `[x]` 官网专题页页面化契约已完成：用户端专题列表改为显式卡片 DTO，专题详情按 `learning/book`、`video/course`、`audio/podcast` 固定映射输出分区结构，并补齐专题分区分页接口与稳定 OpenAPI schema。
- `[x]` 管理端联调第二批反馈第一轮收口已完成：学员导入/导出、审核日志、图书考卷配置、专题分项规则、专家分类层级、首页内容 `contentType/targetId` 规则、反馈字段语义、答疑状态输出形式、测试资源地址策略均已完成当前阶段契约收口并同步到 Swagger/联调文档。
- `[x]` 用户端首页已接通管理端首页配置联动：新增 `GET /api/v1/app/home`，直接复用 `home_categories + home_contents` 返回首页分区与卡片列表，只输出启用中的分类、启用且在投放时间窗内的首页内容，并按用户端现有可见性规则过滤未审核/未发布/已取消资源。

## 阶段 Q3：核心业务流程联调补强

状态：`[~]` 进行中

目标：逐条走通核心业务闭环，修复功能完整度缺口。

- `[ ]` 管理端系统管理：管理员、角色、权限、审计查询。
- `[ ]` 管理端账号管理：用户、学员、认证审核、学习信息维护。
- `[ ]` 管理端内容管理：首页、资讯、播客、专题、文件资源。
- `[ ]` 管理端学习资源：课程、视频、图书、章节、题库、考卷。
- `[ ]` 管理端专家、咨询、直播、反馈、知识库。
- `[ ]` 用户端个人中心：资料、认证、收藏、浏览记录、我的页面聚合。
- `[~]` 用户端学习闭环：课程、图书、播客、专题、学习记录。
- `[ ]` 用户端考试闭环：考卷列表、详情、提交、判分、结果、错题解析。
- `[ ]` 用户端互动闭环：专家咨询、答疑回复、直播观看、反馈提交、知识库检索。
- `2026-06-12`：收口管理端咨询列表“新增可咨询专家”联调缺口。新增 `POST /api/v1/admin/interaction/qa/experts`，供咨询管理页面直接创建进入可咨询名单的专家；底层继续复用 `experts` 主数据与既有 `expert:edit` 权限，不新增平行表或平行专家模型，并显式要求请求体 `consultEnabled=ENABLED`，避免接口语义与实际数据状态不一致。
- `2026-06-11`：已补用户端专家模式第一轮后端接口。新增 `/api/v1/app/interaction/expert/qa/questions`、`/api/v1/app/interaction/expert/qa/questions/{id}`、`/api/v1/app/interaction/expert/qa/questions/{id}/answers`，同一 app 登录用户在具备激活中的专家身份且专家档案启用可接诊时，可直接查看分配给自己的咨询，或按专家分类接收待回复咨询并首条回复时自动认领；同时 `/api/v1/app/auth/me` 补充 `identities` 与 `expertMode` 字段，便于前端按登录态切换专家工作台入口。用户端发起咨询现同步校验 `expertCategoryId/expertId` 路由目标，避免再写入无法被专家侧消费的悬空问题。

当前根据《管理端使用手册》新增的差距清单：

- `[x]` 账户管理补齐：学员管理补新增、删除、批量删除、导入、导出接口，并补齐性别、年龄/出生日期、文化程度等页面字段。
  2026-05-28：已完成第一批闭环，新增学员新增、删除、批量删除接口，补齐 `gender`、`age`、`educationLevel` 字段，并完成 `V16` 权限/表结构迁移。
  2026-06-02：已补管理端学员导入/导出接口，新增 `POST /api/v1/admin/students/import` 和 `GET /api/v1/admin/students/export`，复用现有学员查询条件导出 Excel，导入返回成功数、失败数与失败行明细，并通过 `V22__student_import_export_permissions.sql` 补齐 `sys:student:import`、`sys:student:export` 权限。
- `[x]` 系统管理补齐：明确管理员、角色的“删除”语义；若保留物理/逻辑删除能力，则补管理端删除接口和约束校验。
  2026-05-28：已补管理员、角色删除接口，删除管理员时禁止删除当前登录管理员并清理角色绑定；删除角色时禁止删除 `SUPER_ADMIN` 和仍被管理员绑定的角色，并清理角色权限绑定。
- `[x]` 基础数据补齐：执业类型、机构从“仅查询”补到“可管理”，支撑手册中的独立管理页面。
  2026-05-28：已补机构、执业类型的详情、新增、修改、删除接口，并增加删除前引用校验与 `sys:reference:create/update/delete` 权限种子。
- `[x]` 首页与内容补齐：资讯补 `source`、`tags`；首页内容配置补齐与页面下拉、资源关联一致的字段和查询能力。
  2026-06-08：已完成管理端首页内容第二轮契约收口，新增 `GET /api/v1/admin/content/home/candidates` 候选资源接口，支持按 `categoryId + contentType` 分页查询课程/图书/资讯/播客/专题/知识库/直播候选资源；首页分类继续保留 `TD_HOME_REC` 这类展示位业务编码，不再承担资源类型限定职责；首页内容保存改为按 `categoryId + contentType + targetId` 校验与去重，并继续由后端自动派生 `title/coverUrl`，响应补齐 `categoryName`、`targetCoverUrl`、`createdAt`、`updatedAt` 等页面直出字段。
- `[x]` 学习资源补齐：课程视频、播客音频补 `paperId`；图书补 `totalPages`；章节补 `startPage`、`pageCount`。
  2026-06-08：已补管理端图书分类管理第二轮契约，新增 `GET /api/v1/admin/learning/book-categories/{id}`、`GET /api/v1/admin/learning/book-categories/{id}/books`、`POST /api/v1/admin/learning/book-categories/{id}/books`、`DELETE /api/v1/admin/learning/book-categories/{id}/books`；图书分类列表/详情补 `createdAt`、`updatedAt`，并明确图书继续保持单分类模型，分类移除时直接把 `books.categoryId` 置空。
  2026-06-09：已补课程讲师头像字段。`courses` 新增 `lecturer_avatar_url`、`lecturer_avatar_file_asset_id`，管理端课程新增/修改/列表/详情及用户端课程列表/详情现统一返回 `lecturerAvatarUrl`；讲师头像与课程封面一样只接受管理端上传接口返回的稳定文件地址 `/api/v1/files/{id}/content`，不接受外链或临时签名 URL。
- `[x]` 专家与直播补齐：专家补 `gender`、`birthDate`、`mobile` 等展示字段；直播拆出视频子资源，支撑直播配置弹窗。
- `2026-06-11`：已修复管理端专家分类时间字段联调缺口。`expert_categories` 表中的 `created_at`、`updated_at` 实际已有值，问题在于 `ExpertCategoryResponse` 未返回这两个字段；现已补齐管理端专家分类列表/详情响应时间字段，并需同步更新 `api/api.json`。
- `[~]` 图像上传存储治理：补齐对象存储签名上传、头像/管理端封面确认入库和稳定读取地址，逐步替换用户端直接写 `avatarUrl` 与管理端手填 `coverUrl` / 先外传再回填 URL 的模式。
- `2026-06-05`：已补头像读取兜底收口。`AppUserAvatarUrlResolver` 现在只会在头像 `file_assets` 元数据和对象存储对象都真实存在时返回 `/api/v1/files/{id}/content`；若头像对象缺失，则统一回退 `/images/default-avatar.svg`，同时在 `SecurityConfig` 放开 `/images/**`，避免管理端用户列表继续出现头像破图。
- `[x]` 学习页契约补齐：补充课程、图书、播客、专题接口的浏览/收藏统计与当前用户收藏态，并新增用户端收藏切换、浏览记录上报接口，支撑小程序学习页联调。
  2026-06-02：已在 `AppLearningController` 返回 DTO 中补 `browseCount/favoriteCount/favorited` 等字段，在 `AppInteractionController` 新增 `/favorites`、`/browse-histories` 写接口；评论数与学习首页聚合流仍未建模，后续按页面真实契约继续收敛。
- `[x]` 用户端资讯联调修复：补回用户端资讯列表/详情源码实现，并让资讯支持收藏与浏览记录联动。
  2026-06-04：已新增 `GET /api/v1/app/content/articles`、`GET /api/v1/app/content/articles/{id}` 的本地源码实现，统一按 `deleted=0 + review=APPROVED + publish=PUBLISHED` 暴露资讯；同时在 `AppInteractionService` 补齐 `article` 资源类型校验与浏览量同步，修复资讯列表/详情 `500` 以及资讯收藏/浏览上报 `400 Unsupported interaction resource type`。
- `[x]` 用户端直播收藏联调修复：让互动域正式支持 `live` 资源收藏，并给直播列表/详情回传收藏统计与当前用户收藏态。
  2026-06-05：已在 `AppInteractionService` 补齐 `live` 资源类型可见性校验，修复 `POST /api/v1/app/interaction/favorites` 对直播返回 `400 Unsupported interaction resource type`；同时新增用户端专用直播响应 DTO，`GET /api/v1/app/live-sessions` 与 `GET /api/v1/app/live-sessions/{id}` 现统一返回 `browseCount/favoriteCount/favorited`，并已重新导出 `api/api.json`。
  2026-06-07：已补用户端直播播放契约收口，`GET /api/v1/app/live-sessions` 与 `GET /api/v1/app/live-sessions/{id}` 现额外返回 `streamName`、`httpFlvUrl`、`hlsUrl`；当管理端未手填 `liveUrl` / `playbackUrl` 时，后端会基于 `streamName + app.live` 自动回退到 SRS 播放地址，避免前端拿不到可播流地址。
- `[x]` 管理端直播 SRS 接入：补齐直播流信息查询与 SRS 回调，支持按 `streamName` 维护直播状态。
  2026-06-05：已新增管理端直播流信息接口 `GET /api/v1/admin/live-sessions/{id}/streaming` 与公开回调 `POST /api/v1/integrations/srs/live-hooks`，通过 `streamName` 反查直播并在 `on_publish` / `on_unpublish` 时同步更新 `liveStatus`；同时补充 `app.live` 配置、`streamName` 字段、数据库迁移 `V25__live_stream_name_and_srs_hooks.sql`，并重新导出 `api/api.json`。
  2026-06-05：补齐管理端直播批量删除接口 `POST /api/v1/admin/live-sessions/batch-delete`，与学员管理保持一致的 `IdListRequest` 语义。
  2026-06-07：已补直播地址配置收口。`app.live` 现支持分别配置 `publish-host`、`playback-host`、`playback-scheme`，避免继续用单一 `mediaHost` 同时承担 OBS 推流地址和前端播放地址，支持公网推流与 HTTPS 页面播放分离部署。
  2026-06-07：已新增脚本化直播冒烟验收资产 `scripts/live-smoke.sh`，覆盖管理端登录、创建直播、审核、查询流配置，以及用户端登录、查询直播详情，便于后续用 dev 验收账号快速校验 OBS 推流前置配置是否齐全。
  2026-06-07：已增强 `scripts/live-smoke.sh`，新增对 `POST /api/v1/integrations/srs/live-hooks` 的 `on_publish` / `on_unpublish` 模拟回调校验，确保直播状态可按 `NOT_STARTED -> LIVE -> ENDED` 正常流转。
  2026-06-07：已完成一次最小媒体层实跑验证。使用独立 `ossrs/srs:5` 容器和本机 `ffmpeg` 成功推送测试流到 `rtmp://127.0.0.1:1935/live/smoke-media-test`，确认 `GET /api/v1/versions` 正常、`GET /live/smoke-media-test.m3u8` 返回有效 HLS 清单，且 SRS 日志可见 HTTP-FLV consumer 创建记录；相关实操步骤已补入 `docs/直播功能接入说明.md`。
  2026-06-07：已新增媒体层自动化验收脚本 `scripts/live-media-smoke.sh`，可自动拉起独立 SRS 容器、用 `ffmpeg` 推送测试流，并校验 RTMP -> HTTP-FLV/HLS 最小链路是否可用。
  2026-06-07：已新增本地直播调试页 `src/main/resources/static/debug/live.html`，支持直接输入 `httpFlvUrl` / `hlsUrl` 在浏览器里验证播放，帮助把媒体链路问题与业务前端播放器问题分开排查。
- `[x]` dev 验收数据补强：补齐直播与互动联调所需的更真实种子数据，避免继续依赖过度占位的演示文案和空资源。
  2026-06-05：已更新 `seed_test_data.sql`，补充直播标签、直播子视频、首页直播推荐、直播收藏/浏览/分享记录，并把课程、图书、资讯、播客、知识库、专家简介等关键展示文案改为更贴近真实联调场景的数据。
- `[x]` 管理端统一封面上传接口补齐：复用现有 MinIO 预签名上传与 `file_assets` 入库链路，新增 `POST /api/v1/admin/content/files/covers/upload-url`、`POST /api/v1/admin/content/files/covers/confirm`，统一承接资讯、课程、图书、播客、专题、直播、专家、知识库、首页内容等封面上传；确认后返回稳定读取地址 `/api/v1/files/{id}/content`，不再要求前端手填 `coverUrl` 或自行维护外部 URL。
  2026-06-04：已完成第一轮落地，新增管理端封面 `usage` 约束、对象 key 规则、二次 `statObject` 校验和 `file_assets` 持久化；同时扩展公开文件读取策略，允许管理端封面通过稳定地址公开读取。
  2026-06-04：已完成第二轮收口，管理端内容、学习、直播、专家、知识库等所有写入 `coverUrl` 的保存入口现已统一校验，只接受管理端封面上传接口返回的稳定文件地址；手填外链和对象存储临时 URL 会直接返回业务错误。
- `[x]` 官网专题页补齐：专题列表补展示标签；专题详情按页面结构补“学习 / 视频 / 音频”分区、每区首屏条数和“更多”跳转所需分页接口；专题卡片统一返回页面可直接渲染的标题、副标题、封面、标签和浏览/收藏统计。
  2026-06-02：根据官网专题页效果图完成第一轮缺口审计，确认当前后端已有专题列表/详情与专题关联资源能力，但仍缺少专题主标签、详情分区结构、显式卡片 DTO 和“更多”分页接口，暂不建议继续沿用平铺 `items + Object resource` 作为最终契约。
  2026-06-02：已先完成专题关联资源第一轮稳定性收口，`AppLearningService` 不再向用户端返回 `resource = null` 的静默分项；但专题详情仍未拆出页面分区和显式卡片 DTO，后续继续在该任务下推进。
  2026-06-02：已完成页面化契约落地。`GET /api/v1/app/learning/topics` 改为专题卡片 DTO，`GET /api/v1/app/learning/topics/{id}` 改为分区详情 DTO，并新增 `GET /api/v1/app/learning/topics/{id}/sections/{sectionType}` 分页接口；固定映射为 `learning=book`、`video=course`、`audio=podcast`。
- `2026-06-09`：补齐管理端专题详情回显接口 `GET /api/v1/admin/content/topics/{id}`，用于专题配置弹窗稳定回显当前已绑定 `items`。现有 `PUT /api/v1/admin/content/topics/{id}/items` 代码侧已支持 `course/book/podcast/student/article/question/examPaper` 七种 `itemType`，本次不重复扩容保存链路，只补详情读取契约并同步更新 Swagger。
- `[x]` 管理端联调第二批差距补齐：
  2026-06-02：已完成代码侧第一轮核对，当前研判如下：
  1. 学员导入接口：已完成，新增正式业务入口，限定 Excel 文件上传，返回成功数、失败数与失败行明细。
  2. 学员导出接口：已完成，新增与列表页同查询条件的 Excel 导出能力，并通过下载响应返回文件。
  3. 审核日志：已完成第一轮契约补强，统一接口 `GET /api/v1/admin/system/audit-records` 已补 `targetTypeLabel`、`statusType`、`beforeStatusLabel`、`afterStatusLabel`、`auditorName`、`auditorUsername` 等字段，前端可直接渲染资源类型、审核人和状态语义。
  4. 图书考卷配置：已完成契约收口，明确采用“图书级单考卷”模型，复用图书新增/修改接口的 `paperId` 维护考卷绑定；图书详情/列表响应补 `paperTitle`，并在保存时校验 `paperId` 必须指向真实考卷。
  5. 专题分项配置：已完成第二轮规则收口，`PUT /api/v1/admin/content/topics/{id}/items` 现支持 `course/book/podcast/student/article/question/examPaper` 七类资源，服务层补齐资源存在性校验、同专题内去重、按 `sortOrder` 与请求顺序统一归一化排序；读取响应补 `itemTypeLabel`、`itemAvailable`、标题/副标题/封面与审核/发布状态字段，便于前端直接渲染和识别遗留失效分项。
  6. 专家分类二级科室：已完成第一轮层级契约收口，继续复用同一套分类接口；Swagger 已补“一级科室 / 二级科室 / `parentId` 分组”语义，响应新增 `parentCategoryName`、`level` 字段，并在服务层限制父分类必须是一级科室、禁止形成三级分类，同时拦截“删除仍有子分类/专家绑定的分类”脏数据场景。
  7. 首页内容快捷配置：已明确继续沿用统一 `contentType + targetId` 模型，不新增平行快捷接口；当前服务层已把 `contentType` 收口为 `course/book/podcast/topic/live`，补齐目标资源存在性、`startAt/endAt` 时间范围和 `targetId` 必填校验，响应新增类型中文说明与目标资源可用性/标题字段。
  8. 用户反馈字段语义：已完成第一轮语义说明收口，当前继续保留 `feedbackType` 自由文本模型，不强行收成枚举；Swagger 已明确 `feedbackType` 为前端约定/用户自填分类，`contact` 为主联系方式字段，可填写手机号、微信号、邮箱等一种便于回访的信息。
  9. 答疑状态：已完成第一轮语义化输出收口，在保留现有 `status=0/1/2` 数值兼容的前提下，管理端和用户端问答响应已补 `statusCode`、`statusLabel`，前端无需再自行硬编码状态映射。
  10. 公开资源图片地址：已完成当前阶段契约收口。现阶段仅用户头像稳定走 `/api/v1/files/{id}/content`；其余课程/图书/播客/专题/直播/专家/知识库/首页等资源地址仍是普通 URL 字段。2026-06-08 已把 dev 种子中的主要媒体样例地址直接替换为已验证可访问的 samplefile.com 公网样例资源，便于本地前端联调；共享联调与前端文档已明确真实资源应优先使用可访问公网地址或 `/api/v1/files/{id}/content`，且不得把对象存储临时签名 URL 当长期真相源。
  11. 学员导出运行时 500：已完成修复。2026-06-04 复核前端联调反馈后，本地复现 `GET /api/v1/admin/students/export` 返回 `Internal server error`，根因是 Hutool ExcelWriter 运行时缺少 Apache POI，抛出 `ClassNotFoundException: org.apache.poi.ss.usermodel.Sheet`；已在 `pom.xml` 补充 `org.apache.poi:poi-ooxml:5.4.1`，并用打包产物实际验证导出接口可返回 `200` 和有效 `.xlsx` 文件。
- `[~]` 统计管理补齐：已补学时按地区聚合、专题学员统计明细、地区学时字段和学员成绩状态管理；剩余“卡片/图表/导出”页面级验收待继续收口。
- `[ ]` 工作台与附加内容补齐：确认首页工作台、附加内容管理是否进入当前范围；若保留，补对应接口和契约。

验收标准：

- `[ ]` 每条核心流程至少用 dev 验收账号实际调用一次。
- `[ ]` 发现的功能缺口已进入问题清单或已修复。
- `[ ]` P0 / P1 流程缺陷清零。

## 阶段 Q4：数据一致性、事务与状态流转

状态：`[ ]` 待开始

目标：减少脏数据、半提交和状态错乱风险。

- `[ ]` 检查所有多表写操作是否使用 `@Transactional(rollbackFor = Exception.class)`。
- `[ ]` 检查审核、发布、删除、处理、回复、提交考试等状态流转是否受控。
- `[ ]` 检查逻辑删除是否统一使用 `deleted` 和 MyBatis-Plus `@TableLogic`。
- `[ ]` 规范账户域主数据模型：拆分前台账号、业务身份、学员档案、专家档案的职责边界，避免继续以页面语义反向污染表结构。
- `[ ]` 新增账户域迁移方案并验证数据可回填：至少覆盖用户身份关系、专家绑定用户、学员用户唯一约束。
- `[ ]` 规范地区、医院/机构、执业类型等基础数据建模，避免继续依赖自由文本字段支撑筛选和统计。
- `[ ]` 规范学员认证材料存储模型，评估从 JSON 文本迁移到独立关系表的影响和回填路径。
- `[ ]` 检查考试提交的成绩、通过状态、答案明细是否一致。
- `[ ]` 检查学习记录进度、完成状态和资源详情展示是否一致。
- `[ ]` 检查专题资源关系、首页资源关系、标签关系是否避免孤儿记录。
- `[ ]` 收敛专题关联资源可见性：专题详情与分区分页不应继续返回 `resource = null` 的静默关联项；失效、下线或不可见资源需在查询侧过滤，必要时补清理脚本或管理端校验。
  2026-06-02：已完成第一步，用户端专题详情读取时会过滤解析失败或不可见的关联项，避免继续把 `resource = null` 暴露给前端。
  2026-06-02：已完成第二步，专题详情与分区分页已切到显式卡片和分区结构，不再对前端暴露 `Object resource`；后续仍需补历史脏数据清理与更严格的管理端可见性校验。
- `[~]` 收敛专题/首页内容/资源图片配置真相源：若封面、音频、视频等资源后续统一走 `file_assets` 稳定读取地址，则需补迁移与回填策略；若部分资源继续使用外链，则需明确哪些字段允许外链、哪些只是 dev 占位数据。
  2026-06-04：已先完成管理端封面上传统一入口，新增稳定封面上传/确认接口并允许封面类 `file_assets` 通过 `/api/v1/files/{id}/content` 公开读取；同时已封死管理端手填 `coverUrl` 的保存路径，后续新增/修改封面必须走统一上传接口。历史 `cover_url` 数据尚未回填，音频、视频与非封面图片仍未统一切换到稳定文件地址。
  2026-06-04：已完成第二轮数据真相源收口，课程、图书、资讯、播客、专题、首页内容、直播、专家、知识库等管理端封面写入链路现会同步持久化 `cover_file_asset_id`；`V24__normalize_cover_file_asset_references.sql` 用于给历史 `cover_url` 回填关联，避免后续只保存字符串 URL 而丢失 `file_assets` 真相源。
  2026-06-04：已在本地 dev 库实际执行 `V24` 并完成回填核验，`flyway_schema_history` 已到 `v24`；当前 dev 种子里的历史封面仍是 `https://example.com/assets/...` 占位外链，且 `file_assets` 中暂无 `admin/covers/` 对象，因此各业务表历史 `cover_url -> cover_file_asset_id` 回填结果为 `0`，符合当前占位数据现状，不是迁移失败。
- `[ ]` 检查知识库分类删除时是否处理子分类和条目约束。
- `[ ]` 使用接口调用和数据库明细核对验证关键 Service 一致性，不新增测试文件。

验收标准：

- `[ ]` 核心多表流程异常时可回滚。
- `[ ]` 状态流转非法输入会返回稳定业务错误。
- `[ ]` 不产生明显孤儿关系或不可追溯数据。

## 阶段 Q5：统计、查询性能与索引优化

状态：`[ ]` 待开始

目标：确保管理端统计和高频查询在数据量增长后仍可维护。

- `[ ]` 验证学时统计与 `learning_records` 明细一致。
- `[ ]` 验证学员统计、地区统计与 `students` 明细一致。
- `[ ]` 验证成绩统计与 `exam_records`、`exam_record_answers` 明细一致。
- `[ ]` 验证浏览、收藏、分享统计与 interaction 明细一致。
- `[ ]` 检查统计 SQL 是否全部在 Mapper 注解或 XML 中。
- `[ ]` 对学习记录、考试记录、互动记录、知识库搜索、高频分页接口执行慢查询评估。
- `[ ]` 如需新增索引，使用 `V13__...sql` 或后续迁移补充。
- `[ ]` 使用接口调用和 SQL 明细核对验证统计结果，不新增测试文件。

验收标准：

- `[ ]` 统计结果可解释、可追溯。
- `[ ]` 高频查询有合理索引支撑。
- `[ ]` 没有 Service 层拼接复杂 SQL。

## 阶段 Q6：日志、审计与敏感信息治理

状态：`[ ]` 待开始

目标：提升可观测性，同时避免敏感信息泄漏。

- `[ ]` 检查请求日志是否包含 requestId、路径、状态码、耗时和必要上下文。
- `[ ]` 检查日志中是否输出密码、token、验证码、身份证号、手机号完整值等敏感信息。
- `[ ]` 检查关键管理端操作是否写入审计记录。
- `[ ]` 检查登录成功、审核、处理反馈、重要状态变更的审计记录是否可追溯。
- `[ ]` 统一异常日志等级，避免业务异常刷 ERROR。
- `[ ]` 检查生产配置日志级别和 Actuator 暴露范围。

验收标准：

- `[ ]` 关键操作可追溯。
- `[ ]` 日志不泄漏敏感数据。
- `[ ]` 生产环境不会暴露调试级信息。

## 阶段 Q7：配置、部署与共享联调环境

状态：`[ ]` 待开始

目标：保证本地、dev、test、prod 配置边界清晰，可重复部署。

- `[ ]` 检查 `application.yaml`、`application-dev.yaml`、`application-test.yaml`、`application-prod.yaml`。
- `[ ]` 检查 `.env.example` 与 `.env.test` 所需变量是否完整。
- `[ ]` 检查 `compose.test.yml` 与 Dockerfile 是否和当前应用端口、健康检查、环境变量一致。
- `[ ]` 检查 Nginx 模板和共享联调域名配置。
- `[ ]` 确认共享联调环境禁止 dev profile。
- `[ ]` 编写或更新共享联调部署步骤。
- `[ ]` 明确共享联调资源地址策略：`example.com/assets/...` 这类 dev 占位资源是否需要在 test 环境替换为真实可访问地址，或统一走后端稳定文件读取路径。
  2026-06-08：已先完成本地 dev 验收数据的最小治理。`seed_test_data.sql` 中课程/图书/资讯/播客/专题/直播/专家/知识库等主要媒体样例地址已直接替换为已验证可访问的 samplefile.com 公网样例资源；当前只解决本地联调可访问性，不改变 test 环境资源策略，后续仍需单独确认共享联调环境是保留公网样例地址还是统一切到 `file_assets` 稳定读取路径。
- `[ ]` 在共享联调环境导入或刷新验收数据前形成影响说明。

验收标准：

- `[ ]` 新环境可以按文档启动。
- `[ ]` test profile 不依赖 dev 自动导入逻辑。
- `[ ]` 配置中无硬编码密钥、密码或环境专属私有值。

## 阶段 Q8：无新增测试文件的回归验证

状态：`[ ]` 待开始

目标：不新增测试文件，通过现有测试、脚本化接口调用、数据库明细核对和打包验证覆盖高风险回归点。

- `[ ]` 认证与权限验收：登录、退出、token 恢复、无权限、跨端 token。
- `[ ]` 管理端审核验收：课程、图书、资讯、播客、专题、直播、知识库。
- `[ ]` 用户端数据隔离验收：个人资料、收藏、浏览、学习记录、咨询、反馈。
- `[ ]` 考试判分验收：单选、多选、判断、得分、通过状态、答案记录。
- `[ ]` 统计验收：学时、成绩、地区、互动数据聚合。
  2026-06-09：已补 `GET /api/v1/admin/statistics/study-hours/regions`、`GET /api/v1/admin/statistics/topics/{topicId}/students`、`GET/PATCH /api/v1/admin/statistics/student-scores`，并为 `GET /api/v1/admin/statistics/regions` 补学时字段；本地已通过 `compile`、`test`、`clean package -DskipTests` 和 Swagger 导出验证，后续仍需用 dev 验收账号补人工接口验收记录。
  2026-06-09：继续修正学员存量统计口径。`GET /api/v1/admin/statistics/students/summary` 与 `GET /api/v1/admin/statistics/regions` 现已改为默认按全量学员统计，仅当前端显式传 `startAt/endAt` 时才按学员创建时间过滤；本地最新实例实测默认返回已恢复为全量地区分布，不再只剩最近 30 天新建学员。
  2026-06-09：已新增考核场次与大屏统计后端基础能力，落地 `exam_assessments`、`exam_assessment_organizations`、`exam_assessment_students`、`exam_assessment_events` 和 `exam_records` 场次字段/唯一约束/幂等字段；新增 `/api/v1/admin/learning/exam-assessments`、`/api/v1/app/learning/exam-assessments`、`/api/v1/admin/statistics/exam-assessments/{id}/dashboard|participants|participants/export`，并完成 `api/api.json` 导出验证。
  2026-06-11：已修复考核场次列表容错问题。`GET /api/v1/admin/learning/exam-assessments` 过去会在当前页混入“关联试卷已失效”的历史场次时整页抛 `Exam paper does not exist`；现已改为列表仅加载现存试卷名称，单条异常数据不再导致整页 404，本地已通过 `compile` 验证。
- `[ ]` 异常响应验收：参数校验、资源不存在、非法状态流转。
- `[ ]` 复用 dev 验收数据和现有测试命令，不创建 `src/test` 新文件。

验收标准：

- `[ ]` 现有 `./mvnw -Dmaven.repo.local=.m2/repository test` 稳定通过。
- `[ ]` P0 / P1 高风险流程已有脚本化请求或明确人工验收记录。
- `[ ]` 不新增测试文件，不依赖共享联调数据库或真实短信/微信外部服务。

## 阶段 Q9：文档、契约归档与发布前检查

状态：`[ ]` 待开始

目标：在进入发布或大规模联调前完成交付物收口。

- `[ ]` 更新 `AGENTS.md` 中架构约束和开发禁区。
- `[ ]` 更新 `Todos.md` 阶段状态和剩余问题。
- `[ ]` 导出最终 `api/api.json`。
- `[ ]` 归档关键接口说明、验收账号、联调流程。
- `[ ]` 生成发布前检查清单。
- `[ ]` 运行 `./mvnw -Dmaven.repo.local=.m2/repository test`。
- `[ ]` 运行 `./mvnw -Dmaven.repo.local=.m2/repository clean package -DskipTests`。
- `[ ]` 确认临时服务端口已关闭。

验收标准：

- `[ ]` 文档与代码当前状态一致。
- `[ ]` Swagger JSON 是最新接口契约。
- `[x]` 现有测试命令和打包均通过。
- `[ ]` 发布风险和遗留事项已明确记录。

## 当前优先级

1. 并行推进 Q1 和 Q2，稳定认证权限与接口契约，避免新增接口带来边界回归。
2. 按 Q4 到 Q6 修复数据一致性、统计能力、性能和日志审计问题。
3. 最后完成 Q7 到 Q9，进入共享联调与发布准备。

## 变更记录

- 2026-06-12：修复管理端用户切回学员时的身份恢复异常。`PUT /api/v1/admin/users/{id}` 在 `role=STUDENT` 场景下，现会优先复用包含逻辑删除在内的历史身份记录，避免 `app_user_identities` 因旧 `STUDENT` 记录被软删除后再次插入时撞 `(user_id, identity_type)` 唯一键，并被统一映射成 `500 INTERNAL_ERROR`。
- 2026-06-11：修复“用户转专家”后无法打开专家信息的联调问题。此前管理端用户切换为 `EXPERT` 只会激活 `app_user_identities`，不会自动创建或绑定 `experts` 档案，导致前端只能看到专家按钮却查不到档案；现已在用户角色切换为 `EXPERT` 时自动确保存在最小专家档案，并在 `AdminUserResponse` 中补充 `expertId`、`expertName`、`expertProfileBound` 供前端稳定跳转，同时新增 `V30__backfill_missing_expert_profiles.sql` 回填历史“有专家身份但无专家档案”的存量数据。
- 2026-06-11：修复管理端专家分类列表联调异常。根因是 `GET /api/v1/admin/experts/categories` 在组装 `parentCategoryName` 时对历史孤儿二级分类继续调用 `requireCategory(parentId)`，只要分页结果里存在父分类已被删除或缺失的数据就会整页返回 `NOT_FOUND`；现已改为列表回显时对缺失父分类做容错返回并记录告警日志，避免单条脏数据阻断整个分类管理页。
- 2026-06-04：修复管理端学员导出运行时 500。根因是 `AdminUserService.exportStudents` 调用 Hutool ExcelWriter 时运行时缺少 Apache POI，导致 `GET /api/v1/admin/students/export` 抛出 `ClassNotFoundException: org.apache.poi.ss.usermodel.Sheet`；已在 `pom.xml` 补充 `poi-ooxml` 依赖，并用打包产物实测导出接口返回 `200` 和有效 `.xlsx` 文件。
- 2026-06-04：启动管理端统一封面上传治理，复用现有对象存储预签名上传与 `file_assets` 入库链路，新增 `POST /api/v1/admin/content/files/covers/upload-url`、`POST /api/v1/admin/content/files/covers/confirm`；封面确认成功后直接返回稳定读取地址 `/api/v1/files/{id}/content`，并扩展公开文件读取白名单以支持管理端封面对象前缀。
- 2026-06-04：继续收口管理端封面契约，新增 `StableCoverUrlService`，统一校验内容、学习、直播、专家、知识库等管理端保存接口中的 `coverUrl`；现仅接受管理端封面上传接口返回的稳定文件地址，拒绝手填外链和对象存储临时 URL，并同步更新 Swagger 字段说明。
- 2026-06-04：继续推进封面文件真相源治理，补齐内容、学习、直播、专家、知识库等管理端保存链路对 `cover_file_asset_id` 的同步持久化，并新增 `V24__normalize_cover_file_asset_references.sql` 为历史封面 URL 回填 `file_assets` 关联；通过现有测试和打包验证，未涉及接口结构变更。
- 2026-06-04：在本地 `dev` 库通过 `spring-boot:run` 实际执行 `V24` 并核验回填结果，Flyway 已从 `v23` 升到 `v24`；由于当前 dev 验收数据里的课程、图书、资讯、专题、专家等历史封面仍是 `example.com/assets/...` 占位 URL，且 `file_assets` 中暂无 `admin/covers/` 记录，历史数据回填数为 `0`，后续需用真实管理端封面上传链路新增或编辑资源，才能看到 `cover_file_asset_id` 实际落库。
- 2026-06-05：补齐默认头像接口层兜底缺口。`SystemAdminService`、管理端专家返回和用户端专家返回现统一复用头像解析器：当 `avatarUrl` 为空时返回 `/images/default-avatar.svg`，避免管理员/专家资料继续向前端透传空头像；本次未做数据库字段批量回填，`sys_admins`、`experts` 等历史空值仍保持原样。已通过 `.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" test` 与 `.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" clean package -DskipTests`。
- 2026-06-05：为测试环境补齐直播基础设施部署配置。`compose.test.yml` 新增 `srs` service，按 SRS 官方 getting-started 直播方案暴露 `1935/1985/8080` 端口，并新增 `docker/srs/srs.template.conf`、`docker/srs/start-srs.sh` 承接 `on_publish` / `on_unpublish` 到 `/api/v1/integrations/srs/live-hooks`；同时补充 `.env.test.example` 和《直播功能接入说明》中的 SRS 部署变量说明，便于测试环境直接拉起 RTMP 推流、HTTP-FLV/HLS 播放和直播状态回调。
- 2026-06-09：修复管理端专家列表联调异常映射与权限回填。共享联调环境中 `superadmin` 登录返回权限集缺少 `expert:view`，访问 `GET /api/v1/admin/experts` 时 `@PreAuthorize` 安全异常被全局 `Exception` 处理器误映射成 `500`；已在 `GlobalExceptionHandler` 显式处理 Spring Security 鉴权/鉴权不足异常，保证缺权限稳定返回 `403/401`，并新增 `V26__backfill_expert_view_permission.sql` 幂等回填 `expert:view` 权限和 `SUPER_ADMIN` 绑定。
- 2026-06-09：补齐用户端首页聚合接口 `GET /api/v1/app/home`，与管理端首页内容管理直接联动，返回首页分类分组下的课程/图书/资讯/播客/专题/知识库/直播卡片摘要；接口复用现有资源发布可见性规则，不额外新增平行首页配置表或用户端专用配置模型。
- 2026-06-09：补齐统计管理手册缺口，新增学时按地区聚合、专题学员统计明细、学员成绩状态查询与更新接口，并为地区统计补充学时字段；新增 `V27__complete_statistics_management_gap.sql` 创建 `student_score_records` 表和 `statistics:score:edit` 权限，已重新导出 `api/api.json`。
- 2026-06-02：收口公开资源地址策略：明确当前仅用户头像稳定走 `/api/v1/files/{id}/content`，课程/图书/播客/专题/直播/专家/知识库/首页等封面与音视频地址仍是普通 URL 字段；dev 种子中的 `example.com/assets/...`、`example.com/live/...` 统一作为占位数据处理，并同步更新 API 文档、前端测试数据文档和共享联调环境约定。
- 2026-06-02：完成官网专题页页面化契约收口：用户端专题列表改为显式卡片 DTO，专题详情改为 `学习 / 视频 / 音频` 分区结构，并新增专题分区分页接口；固定映射 `learning=book`、`video=course`、`audio=podcast`，同步更新文档与 OpenAPI 契约。
- 2026-06-02：收口反馈字段语义与答疑状态输出契约：反馈继续保留 `feedbackType` 自由文本模型，并在 Swagger 明确 `contact` 为主联系方式字段；问答响应在兼容原 `status=0/1/2` 的前提下新增 `statusCode`、`statusLabel` 语义字段，同时更新 Swagger 契约。
- 2026-06-02：收口专家分类二级层级与首页内容快捷配置契约：专家分类继续复用 `parentId` 两级模型，补层级语义字段和父子/删除约束；首页内容继续复用统一 `contentType + targetId` 模型，收口为 `course/book/podcast/topic/live` 五类资源并补资源存在性与时间范围校验，同时更新 Swagger 契约。
- 2026-06-02：收口图书考卷配置契约，明确图书沿用 `paperId` 作为单考卷绑定字段，复用图书新增/修改接口维护；图书响应补 `paperTitle`，并在写入时校验考卷存在，同时更新 Swagger 文档说明。
- 2026-06-02：补强管理端审核日志契约，`GET /api/v1/admin/system/audit-records` 新增资源类型中文说明、状态语义类型、前后状态中文说明和审核人展示字段，并同步更新 Swagger 契约，前端无需再自行硬编码 `targetType` 与状态值映射。
- 2026-06-02：完成学员导入/导出收尾，新增 `POST /api/v1/admin/students/import`、`GET /api/v1/admin/students/export`，补充 `V22__student_import_export_permissions.sql` 权限种子，复用现有测试完成健康检查，并重新导出 `api/api.json`。
- 2026-06-02：录入前端第二批管理端联调反馈并完成代码侧初判：学员导入/导出接口当前确实缺失；统一审核日志接口已存在但字段和资源类型说明不足；图书 `paperId` 已支持单考卷模型但缺少“考卷配置”页面级说明；专家分类已支持 `parentId` 层级；首页内容仍是通用 `contentType + targetId` 模型；反馈 `feedbackType` 仍为自由文本、`contact` 为主联系方式字段；答疑状态内部为 `PENDING/ANSWERED/CLOSED` 但对外仍输出数值；大量 `example.com/assets/...` 仍是 dev 种子占位资源。
- 2026-06-02：根据官网专题页效果图启动专题页面契约收口，已将缺口拆入 `Q2/Q3/Q4`：当前专题列表与详情接口可用，但详情仍是平铺 `items + Object resource`，缺少专题主标签、详情分区、“更多”分页接口与失效资源过滤策略，后续按页面化契约改造推进。
- 2026-06-02：修复管理端联调 500，纠正成绩统计按试卷查询 `GROUP BY` 仍引用旧列 `ep.title` 的问题，并新增 `V21__fix_qa_answers_created_at.sql` 为 `qa_answers` 补 `created_at` 列，消除答疑回复插入时与 `QaAnswer` 实体不一致导致的 500；同时兼容旧数据里答疑状态为空时按 `PENDING` 处理。
- 2026-06-01：修复管理端联调首批问题，反馈列表补齐 `nickname`、`avatarUrl`、`mobile`、`createdAt`，管理端用户头像优先解析为稳定文件地址 `/api/v1/files/{id}/content`，修正成绩统计按试卷查询 SQL 字段名，并补充 dev 验收种子中的图书页数、资讯来源/标签、播客主讲人/标签、专家画像和用户稳定头像数据。
- 2026-06-01：根据当前仍处于开发阶段、尚无正式官网地址的实际情况，将官网微信扫码登录开发态切换为“纯后端 mock 链路可跑”，允许 `WECHAT_WEB_MOCK_ENABLED=true` 时跳过真实网站应用配置校验；同时在任务清单中明确记录，产品官网上线后需关闭 mock、补齐真实网站应用配置并切回微信官方扫码登录。
- 2026-06-01：开始推进 Q1 用户端认证收口，新增官网微信扫码登录支持，增加 `wechat_web_open_id` 字段、网站扫码 `state`/绑定 token Redis 能力、`/api/v1/app/auth/wechat-web/*` 接口和配置项；用户端微信登录匹配逻辑统一为优先 `wechat_union_id` 再按端侧 `openid` 恢复登录态。
- 2026-05-29：启动图像上传存储治理，接入 MinIO 签名上传基础设施，新增用户端头像上传申请、上传确认和稳定读取地址，收口用户端直接提交 `avatarUrl` 的更新方式；管理端内容图片和认证材料复用待后续接通。
- 2026-05-26：核心功能首轮实现完成，阶段 0 至阶段 12 收口。
- 2026-05-26：进入质量保障与功能完善度提升阶段，重写任务计划，后续以 Q0 至 Q9 推进。
- 2026-05-26：修复用户端短信验证码联调问题；阿里云短信配置完整时强制真实发送，配置不完整时才使用 mock，并补充脱敏诊断日志。
- 2026-05-26：补充 `.env.example`、`.env.test.example`、`compose.test.yml` 和共享联调文档中的阿里云短信配置示例。
- 2026-05-26：补充 Spring Boot 可选导入项目根目录 `.env` / `.env.test`，避免本地直接启动时环境变量未加载导致短信继续 mock。
- 2026-05-26：短信发送从 Dysmsapi `SendSms` 切换到 Dypnsapi `SendSmsVerifyCode`，匹配当前验证码模板来源。
- 2026-05-26：补强短信发送失败日志的签名、模板、区域、端点上下文，并修正 Controller 异常请求日志状态码误记为 200 的问题。
- 2026-05-26：修正 `.env` 中文短信签名经 Spring properties 导入时可能被 ISO-8859-1 误读的问题，并补充 Unicode escape 配置说明。
- 2026-05-28：完成 `api/api.json` 与 Controller 的静态比对，确认 182 个 HTTP 操作全部一致。
- 2026-05-28：完成 Q0 测试基线审计，确认现有测试仅覆盖启动、统一异常响应与审计切面；使用 `.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" test` 验证现有 4 个测试全部通过。
- 2026-05-28：新增账户域数据库整改方案文档，明确 `app_users`、`students`、`experts`、身份关系表、机构与执业类型字典、认证材料关系表的目标模型及 `V13`~`V16` 迁移顺序。
- 2026-05-28：开始落地 `V13` 最小实施面，新增 `app_user_identities` 表、`app_users.profile_signature`、`experts.user_id` 和账户域枚举/实体/Mapper 骨架。
- 2026-05-28：继续落地账户域规范化，新增 `V14` 基础数据迁移，补充 `organizations`、`practice_types`、`student_certification_files` 以及 `students`、`experts` 关联字段，并接通 `profile_signature` 到用户端资料接口与管理端返回 DTO。
- 2026-05-28：接通学员认证材料结构化存储，用户端提交认证时写入 `student_certification_files`，用户端与管理端学员详情返回 `certificationFiles`；同步更新 dev 验收数据的身份、机构、执业类型和认证材料种子。
- 2026-05-28：根据管理端“修改用户信息”弹窗补齐管理端用户更新接口，支持昵称、口号、启用状态、普通/学员/专家身份切换、学员绑定、地区与机构字段，并新增机构和执业类型基础数据查询接口。
- 2026-05-28：根据《管理端使用手册》补充数据库问题台账，确认账户域存在用户身份、专家绑定、机构/地区、执业类型和认证材料建模缺口，后续以 `V13` 起新增迁移整改。
- 2026-05-28：继续对照《管理端使用手册》逐页核对管理端效果图，确认当前后端无法完整覆盖学员新增/删除/导入导出、执业类型管理、课程视频/播客音频考卷配置、图书页数字段、专家展示字段、直播视频子资源、统计明细/图表/导出等页面级能力，并将“手册差距补齐”提升为 Q3 当前进行中主线。
- 2026-05-28：完成学员管理第一批闭环，新增管理端学员新增、删除、批量删除接口，补齐学员性别、年龄、文化程度字段，新增 `V16__student_management_manual_gap.sql` 权限和表结构迁移；使用 `.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" test`、`.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" clean package -DskipTests` 验证通过，并通过临时服务导出最新 `api/api.json`。
- 2026-05-28：完成基础数据管理闭环，新增机构与执业类型的详情、新增、修改、删除接口，执业类型列表补充 `keyword` 查询，删除前增加学员/专家引用和子节点校验；新增 `V17__reference_management_permissions.sql`，并使用 `.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" compile -DskipTests` 完成无测试编译校验。
- 2026-05-28：完成系统管理删除语义补齐，新增管理员、角色删除接口和 `V18__system_delete_permissions.sql` 权限迁移；管理员删除增加“不可自删”约束，角色删除增加“不可删除 SUPER_ADMIN / 不可删除仍被绑定角色”约束，并使用 `.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" compile -DskipTests` 完成无测试编译校验。
- 2026-05-28：完成内容、学习资源、专家和直播第二批手册差距补齐，新增 `V19__manual_gap_content_learning_expert_live.sql`，补齐资讯 `source/tags`、播客 `speakerName/tags`、播客音频与课程视频 `paperId`、图书 `totalPages`、章节 `startPage/pageCount`、专家 `gender/birthDate/mobile/coverUrl`，并新增直播视频子资源接口与标签支持；使用 `.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" compile -DskipTests` 完成无测试编译校验，并通过临时服务导出最新 `api/api.json`。
