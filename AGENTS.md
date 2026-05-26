# AGENTS.md

## 项目当前阶段

`中医在线` 后端已经完成管理端与用户端核心功能首轮实现，当前进入“质量保障与功能完善度提升”阶段。

后续开发目标不再是快速补齐空白模块，而是围绕已实现能力做联调修复、契约稳定、权限校验、异常处理、数据一致性、性能索引、接口验收和上线准备。

重要原则：

- 不要重新设计一套平行架构。
- 不要把已拆分的管理端与用户端能力重新混回同一控制器、同一权限模型或同一 token 语义。
- 不要直接修改已发布 Flyway 迁移，后续数据库变更从 `V13__...sql` 开始新增。
- 新增或修改接口后必须同步更新 `api/api.json`。
- 下个质量保障阶段不新增测试文件，不修改既有测试文件；只运行现有测试作为健康检查，功能质量主要通过接口联调、脚本化请求、编译打包和 Swagger 契约验证保证。

## 当前架构事实

项目是 Spring Boot 3.x 单体分层后端，按业务域拆模块，并在模块内按管理端与用户端入口拆分。

```text
src/main/java/com/gugugaga/jsmedicine/
├── common/                 # 通用实体、枚举、异常、响应、配置、Mapper
├── infrastructure/
│   ├── security/           # Spring Security、Bearer Token 过滤器、RedisTemplate
│   ├── storage/            # 文件资产能力
│   └── tooling/            # dev 验收数据导入
└── module/
    ├── auth/               # admin/app 双端认证与会话
    ├── system/             # 后台 RBAC、管理员、角色、权限、审计
    ├── user/               # 用户、学员、用户端个人中心
    ├── content/            # 首页、资讯、专题、播客管理
    ├── learning/           # 课程、图书、题库、考试、学习记录、直播
    ├── expert/             # 专家分类、专家资料、履历
    ├── interaction/        # 收藏、浏览、分享、答疑、反馈
    ├── knowledge/          # 知识库分类、条目、检索
    └── statistics/         # 管理端统计查询
```

### 端侧边界

- 管理端接口前缀：`/api/v1/admin/...`
- 用户端接口前缀：`/api/v1/app/...`
- 管理端认证接口目前为：`/api/v1/auth/login`、`/api/v1/auth/logout`、`/api/v1/auth/me`、`/api/v1/auth/status`
- 用户端认证接口为：`/api/v1/app/auth/...`

禁止事项：

- 禁止让用户端复用后台 RBAC 权限模型。
- 禁止让后台管理员和用户端用户共用 `UserDetails`、Redis token 前缀、Session 载荷或当前用户解析器。
- 禁止为了复用少量代码，把 admin/app Controller 合并成一个混合入口。

## 认证与安全实现细节

认证采用等价 Bearer Token 会话方案，不使用 JWT。

核心类：

- `BearerTokenAuthenticationFilter`
- `AuthTokenService`
- `AppUserTokenService`
- `CurrentAdminResolver`
- `CurrentAppUserResolver`
- `AdminSession`
- `AppUserSession`

实现约定：

- 管理端 Redis key 前缀为 `admin:token:`。
- 用户端 Redis key 前缀为 `app:user:token:`。
- token value 使用项目 `ObjectMapper` 显式序列化为 JSON 字符串，再由 token service 显式反序列化。
- 不要直接依赖 Redis serializer 反序列化成 `AdminSession` / `AppUserSession` 实例；这在不同 ObjectMapper / Redis serializer 场景下容易出现类型恢复失败。
- `BearerTokenAuthenticationFilter` 会先尝试管理端 session，再尝试用户端 session。
- 认证成功后，`Authentication.credentials` 保存原始 token，`details` 保存对应 session。
- `AuthBootstrapService` 必须在 `app.auth.bootstrap-password` 未配置时直接跳过，不要先查 `sys_admins`。轻量验证环境可能没有完整业务 schema。

权限约定：

- 管理端接口使用 `@PreAuthorize("hasAuthority('xxx')")`。
- 新增管理端接口必须同步新增 `sys_permissions` 种子迁移，并绑定 `SUPER_ADMIN`。
- 权限编码使用业务域前缀，例如 `learning:course:view`、`knowledge:entry:review`、`statistics:view`。
- 用户端登录态只代表当前 app 用户，不承载后台权限码。

## 数据层与迁移

Entity 是代码侧数据模型来源，Flyway 是数据库落地来源。新增表、字段、索引、状态流转时，先更新 Entity / Enum，再新增迁移脚本。

当前已存在迁移到 `V12__stage11_statistics_permissions_and_indexes.sql`。后续迁移必须从 `V13__...sql` 开始。

禁止事项：

- 禁止修改已发布迁移 `V1` 到 `V12`。
- 禁止只在 SQL 中新增业务结构而不创建对应 Entity。
- 禁止 Controller 直接返回 Entity。
- 禁止在 Service 中拼接复杂统计 SQL。

推荐做法：

- 简单单表 CRUD、分页、条件查询使用 MyBatis-Plus。
- 复杂联表、统计、聚合查询放在 Mapper 注解 SQL 或 XML 中。
- 统计模块当前使用 `AdminStatisticsMapper` 注解 SQL，Service 只负责参数归一化和业务边界。
- 逻辑删除字段统一为 `deleted`，常规管理类实体继承 `ManagedEntity`。
- 通用基础字段优先复用 `BaseEntity` / `ManagedEntity`。
- 状态字段优先使用已有枚举，例如 `EnabledStatus`、`ReviewStatus`、`PublishStatus`、`StudentCertificationStatus`、`QaStatus`、`FeedbackStatus`、`LiveStatus`。

扩展性约定：

- 跨资源关系优先沿用 `(resource_type, resource_id)` 或 `(item_type, item_id)` 形态。
- 标签优先复用 `tags`、`resource_tags`。
- 弱扩展字段优先评估 `entity_extensions`。
- 文件与媒体元数据集中在 `file_assets`，业务表只保存必要读路径字段。

## 模块实现约定

### system

承载后台管理员、角色、权限、审计记录。不要把用户端个人中心逻辑放进 `system`。

### user

承载 `app_users`、`students`、用户端个人资料、学员认证、用户端“我的页面”聚合。后台用户/学员管理接口也在该域内，但接口路径仍使用 `/api/v1/admin/...`。

### content

承载首页分类、首页内容、资讯、专题、播客管理等内容分发能力。用户端消费学习资源时，如属于学习闭环，应优先在 `learning/app` 聚合。

### learning

承载课程、图书、播客播放、题库、考试、学习记录、直播等学习闭环。题库和考试已经实现后台配置与用户端提交判分，不要新增平行考试模块。

### expert

承载专家分类、专家资料、专家履历。咨询/答疑流程可引用专家，但专家主数据不下沉到 interaction。

### interaction

承载收藏、浏览、分享、答疑、反馈。答疑问题和回复、反馈处理都应使用逻辑删除与稳定状态流转。

### knowledge

承载知识库分类、条目、审核、发布和用户端搜索。知识库不是图书表的别名，不要把知识库检索硬塞进 book。

### statistics

承载管理端统计接口，当前包括：

- `/api/v1/admin/statistics/study-hours/summary`
- `/api/v1/admin/statistics/study-hours/resources`
- `/api/v1/admin/statistics/students/summary`
- `/api/v1/admin/statistics/regions`
- `/api/v1/admin/statistics/exam-scores/summary`
- `/api/v1/admin/statistics/exam-scores/papers`
- `/api/v1/admin/statistics/content-interactions`

统计 SQL 必须落在 Mapper 注解或 XML。Service 不做手写 SQL 拼接。

## API 与响应规范

- RESTful 风格，资源名使用复数。
- 分页参数统一为 `page`、`size`、`sort`。
- 时间范围参数优先使用 `startAt`、`endAt`，格式为 ISO date-time。
- 认证使用 `Authorization: Bearer <token>`。
- 返回统一使用 `ApiResponse<T>`。
- 分页返回统一使用 `PageResponse<T>`。
- 参数校验使用 Jakarta Validation。
- 业务错误抛 `BusinessException`，错误码使用 `ErrorCode`。
- 不要返回裸字符串、裸 Map 或数据库行结构，除非确实是内部临时工具接口且已说明。

## Swagger 与接口归档

OpenAPI 文档：

- JSON：`/api/docs`
- Swagger UI：`/swagger-ui.html`
- 最新归档文件：`api/api.json`

每次新增、删除、修改接口后必须导出最新 Swagger JSON：

```bash
./mvnw -Dmaven.repo.local=.m2/repository spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments=--server.port=18081
curl -fsS http://127.0.0.1:18081/api/docs -o api/api.json
python -m json.tool api/api.json >/dev/null
```

导出后关闭临时服务：

```bash
lsof -ti tcp:18081 | xargs -r kill
```

## 验收数据与联调环境

dev 环境启动时默认导入验收数据：

- 初始化类：`DevTestDataInitializer`
- SQL：`src/main/resources/scripts/sql/seed_test_data.sql`
- 可关闭：`APP_DEV_TEST_DATA_ENABLED=false`

验收账号：

- 管理端：`td_admin / Admin@123456`
- 管理端只读：`td_viewer / Admin@123456`
- 用户端：`td_user_01 / User@123456`
- 用户端：`td_user_02 / User@123456`
- `td_user_03` 仅保留微信身份样例，不提供密码登录。

共享联调环境：

- profile：`test`
- 地址：`https://api-test.arez.cc.cd`
- Swagger UI：`https://api-test.arez.cc.cd/swagger-ui.html`
- OpenAPI JSON：`https://api-test.arez.cc.cd/api/docs`
- 部署目录：`/root/jsmedicine-test`
- 部署文件：`compose.test.yml`、`.env.test`、`docker/nginx/jsmedicine-test.conf.template`

禁止在共享联调环境随意清库、改表或重置种子数据。需要刷新数据时，先说明影响范围，再做受控导入。

## 质量保障阶段工作方式

每次开始任务前：

1. 阅读 `Todos.md`，选择当前 `[~]` 阶段的最高优先级任务。
2. 先确认现有代码路径和既有模式。
3. 明确接口契约、权限、数据模型和迁移影响。
4. 小步实现，小步验证。
5. 更新 `Todos.md` 的任务状态和必要验收记录。

完成后至少执行：

```bash
./mvnw -Dmaven.repo.local=.m2/repository test
./mvnw -Dmaven.repo.local=.m2/repository clean package -DskipTests
```

说明：`test` 只运行仓库现有测试，后续阶段不新增测试文件。

如果改动接口，还必须导出 `api/api.json`。

质量检查重点：

- 登录、退出、token 恢复、权限不足、token 过期。
- 审核、发布、删除、状态流转。
- 当前用户只能访问自己的用户端数据。
- 管理端权限必须覆盖敏感操作。
- Service 方法涉及多表写入时必须有事务。
- 统计结果必须能追溯到基础明细数据。
- 异常响应必须稳定，不能把堆栈、SQL、密钥或敏感配置暴露给前端。
- 日志中不得输出密码、token、验证码、完整身份证号等敏感信息。

## 构建命令

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw -Dmaven.repo.local=.m2/repository test
./mvnw -Dmaven.repo.local=.m2/repository clean package -DskipTests
```

## 代码风格

- Java 注释和日志使用英文。
- 文档和任务记录可以使用中文。
- 不添加 `@author`、`@since`、创建时间等作者信息标签。
- 只在复杂逻辑、特殊行为、重要权衡、外部参考链接处添加注释。
- 优先简单、明确、低复杂度实现。
- 不引入微服务、DDD、CQRS 等重型架构。
- 不为少量重复创建过早抽象。
- 新依赖必须是成熟、必要、能明显降低维护成本的依赖。

## 当前开发禁区

- 不新增平行认证体系。
- 不新增平行统一响应结构。
- 不新增平行异常处理器。
- 不新增平行分页对象。
- 不绕过 `BearerTokenAuthenticationFilter` 自行解析 token。
- 不在 Controller 中写业务编排或数据访问。
- 不在 Service 中拼复杂统计 SQL。
- 不直接修改 `V1` 到 `V12` 迁移。
- 不把用户端能力塞回后台 RBAC。
- 不把知识库等同于图书、资讯或专题。
- 不用硬编码密码、密钥、域名替代配置。
