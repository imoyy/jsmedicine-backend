# AGENTS.md

## 项目概览

`中医在线` 管理端后端项目，覆盖登录、账户管理、系统管理、首页管理、课程管理、图书管理、资讯管理、播客管理、专题管理、专家管理、题库管理、统计管理、直播管理、答疑管理、反馈管理等完整后台能力。

- 面向管理端提供 RESTful API。
- 开发过程覆盖 MVP、联调、验收、上线前质量保障，不只停留在最小可用版本。
- 优先复用成熟方案和现成依赖，必要时允许在 `pom.xml` 中新增成熟库，避免重复造轮子。
- 不要过早引入微服务、DDD、CQRS 等重型架构，保持单体、分层清晰、按功能演进。

## 运行环境要求

### 硬件

- 服务器 CPU 4 核及以上。
- 内存 8G 及以上。

### 软件

| 类型 | 名称 | 版本 | 用途 |
|---|---|---:|---|
| 服务器操作系统 | Linux / CentOS | 7.3+ | 运行应用与数据库 |
| 数据库 | MySQL | 5.0+ | 数据存储 |
| 应用发布 | Nginx | 1.14.0 | 反向代理与发布 |
| 浏览器 | Chrome | 72.0.3626+ | 管理端访问与联调 |

## 技术栈

| 组件 | 说明 |
|---|---|
| Java 17+ | 语言 |
| Spring Boot 3.x 最新稳定版 | 主框架 |
| Maven | 依赖管理与构建 |
| Spring Web | REST API |
| Spring Validation | 参数校验 |
| Spring Security | 认证与授权 |
| Spring AOP | 审计、日志、通用切面能力 |
| Spring Data Redis | Redis 访问与缓存支持 |
| MySQL | 生产数据库 |
| Redis | 缓存、会话 |
| MyBatis | 核心 ORM / SQL 映射，优先用于可控查询开发 |
| MyBatis-Plus | 提升 CRUD、分页、条件构造、代码开发效率 |
| MyBatis-Plus Pagination | 分页查询能力，优先复用成熟分页方案 |
| dynamic-datasource-spring-boot-starter | 多数据源场景下优先复用成熟方案 |
| Lombok | 简化样板代码 |
| MapStruct | DTO / Entity 映射 |
| Hutool | 常见工具能力补充，避免重复封装零散工具类 |
| Jackson | JSON 序列化 / 反序列化 |
| Apache Commons Lang / Collections | 通用字符串、集合、对象工具 |
| commons-codec | 编码、摘要、签名等基础能力 |
| springdoc-openapi | Swagger UI 接口文档 |
| Flyway | 数据库迁移（优先） |
| spring-boot-starter-test | 测试基础设施 |
| MockMvc / Spring Security Test | 接口层与权限测试 |

## 项目结构

```text
.
├── src/main/java/com/gugugaga/jsmedicine/
│   ├── JsmedicineApplication.java
│   ├── common/
│   │   ├── config/
│   │   ├── entity/
│   │   ├── enums/
│   │   ├── exception/
│   │   ├── mapper/
│   │   ├── response/
│   │   └── util/
│   ├── module/
│   │   ├── auth/
│   │   ├── account/
│   │   ├── system/
│   │   ├── home/
│   │   ├── course/
│   │   ├── book/
│   │   ├── content/
│   │   ├── podcast/
│   │   ├── topic/
│   │   ├── expert/
│   │   ├── question/
│   │   ├── statistics/
│   │   ├── live/
│   │   ├── qa/
│   │   └── feedback/
│   └── infrastructure/
│       ├── security/
│       ├── storage/
│       └── integration/
├── src/main/resources/
│   ├── application.yaml
│   ├── application-dev.yaml
│   ├── application-prod.yaml
│   └── db/migration/
├── docs/
├── src/test/java/
└── api/
```

## 开发计划与进度追踪

- 项目根目录的 `Todos.md` 是后续开发阶段、任务优先级和验收状态的计划来源。
- 每次开始新功能或新阶段前，必须先阅读 `Todos.md`，确认当前应推进的阶段和任务，不要跳过前置依赖。
- 如果用户没有指定具体任务，应优先选择 `Todos.md` 中当前 `进行中` 阶段的未完成任务；当前阶段完成后再进入下一阶段。
- 开发过程中如果发现任务拆分不合理、存在阻塞或需要调整顺序，必须同步更新 `Todos.md`，并在回复中说明调整原因。
- 每完成一个任务，必须更新 `Todos.md` 中对应复选框、阶段状态和必要的验收记录。
- 每完成一个阶段，必须确认该阶段验收标准全部满足；未满足时保留阶段为 `进行中` 或标记阻塞，不要提前标记完成。
- `Todos.md` 只记录项目推进计划和验收状态，不替代接口文档、数据库设计文档或代码注释。

## 架构原则

- Controller 只负责参数接收、校验、响应返回。
- Service 负责业务编排、事务控制、权限判断、状态流转。
- Repository / Mapper 只负责数据访问，优先采用 MyBatis / MyBatis-Plus 落地。
- DTO 和 Entity 分离，不直接暴露数据库模型。
- Entity 是当前数据模型的代码源头，数据库迁移脚本是运行时落地产物；新增字段、表、状态时必须先更新实体和枚举，再同步 Flyway。
- 模块按业务域拆分，模块间通过 Service 协作，避免循环依赖。
- 不要增加无必要的中间层和抽象层。
- 能直接复用现成依赖解决的问题，不手写低价值基础设施代码。
- 简单 CRUD、分页、批量更新、条件查询优先使用 MyBatis-Plus 等成熟能力，不重复封装轮子。
- 当前应用入口已通过 `@MapperScan("com.gugugaga.jsmedicine.**.mapper")` 扫描 Mapper；新增模块应按 `module/<domain>/{entity,mapper}` 结构扩展，不要把 Mapper 分散到非约定目录。

## 功能范围

后端实现需覆盖管理端使用手册中的核心业务能力：

- 用户登录、记住账号密码、权限识别。
- 账户管理：用户管理、学员管理。
- 系统管理：管理员管理、角色管理、权限分配。
- 首页管理：分类管理、首页内容配置。
- 课程管理：课程列表、审核、考卷、视频配置。
- 图书管理：图书列表、分类、审核、章节、考卷配置。
- 资讯管理：帖子新增、富文本编辑、删除、审核。
- 播客管理：音频管理、配置、审核。
- 专题管理：课程、书本、学员、资讯、博客、题库等关联配置。
- 专家管理：专家信息、履历、分类管理。
- 题库管理：题目新增、修改、删除。
- 统计管理：学时、学员、地区、成绩统计。
- 直播管理：直播新增、修改、删除、审核。
- 答疑管理：问题查看、回复查看、删除。
- 反馈管理：反馈查看、删除。

## API 规范

- RESTful 风格，资源名使用复数。
- 统一使用版本前缀，如 `/api/v1/...`。
- 分页参数统一为 `page`、`size`、`sort`。
- 认证采用 `Authorization: Bearer <token>`。
- 返回统一响应结构，错误码、提示信息、数据结构必须稳定。
- 所有接口必须写清楚请求参数、响应体、错误场景。

## Swagger 与接口归档

- 接入 `springdoc-openapi` 并提供 Swagger UI。
- 每完成一个后端接口任务，必须同步导出最新的全量 Swagger JSON。
- 导出的文件保存到项目主目录下的 `api/` 文件夹。
- 保留历史文件，不覆盖删除。
- 最新接口文件名必须固定为 `api.json`。
- 如果需要保留阶段版本，可额外生成带时间戳或版本号的文件，但 `api.json` 始终代表最新版本。

## 数据层

- 优先使用 MySQL。
- 数据表结构通过迁移脚本管理，优先使用 Flyway。
- 当前首版迁移脚本为 `src/main/resources/db/migration/V1__init_schema.sql`；后续变更必须新增 `V2__...sql`、`V3__...sql` 等增量迁移，不要直接改动已发布迁移。
- 数据访问优先使用 MyBatis / MyBatis-Plus，提高 CRUD、分页、条件查询开发效率。
- 简单单表 CRUD、分页、批量操作优先复用 MyBatis-Plus；复杂 SQL、统计查询、联表查询优先使用 MyBatis 明确编写。
- 禁止把业务数据结构只写在 SQL 中而不创建对应 Entity；每张业务表必须有清晰的 Entity，常规表必须有对应 Mapper。
- 优先复用成熟插件与能力，例如分页、乐观锁、自动填充、逻辑删除、数据权限，不手写重复功能。
- 表结构和字段命名保持清晰、可读、可维护。
- 涉及状态流转的字段必须用枚举或明确常量定义。
- 不在 Entity 中写业务逻辑。
- 逻辑删除字段统一使用 `deleted`，并通过 MyBatis-Plus `@TableLogic` 管理。
- 通用审计字段优先继承公共基类，不要在业务实体中重复定义同名字段。
- 跨资源配置已采用 `(resource_type, resource_id)` 或 `(item_type, item_id)` 形态支持首页、专题、标签等场景；后续扩展资源关系时优先沿用该模式，避免为每类资源无限增加相似中间表。
- 扩展性字段优先评估 `tags`、`resource_tags`、`entity_extensions` 是否满足需求；只有当字段进入核心查询、约束或强业务流程时，才新增正式列。
- 文件与媒体元数据集中在 `file_assets`，业务表可保留 URL / key 等读路径字段；不要在各业务模块重复设计完整文件表。
- 统计类复杂查询可以使用 MyBatis XML 或注解 SQL，但输入输出仍应通过明确 DTO / VO 承载，不要直接向 Controller 暴露数据库行结构。

## 安全

- 使用 Spring Security 实现认证与授权。
- 密码必须加密存储，优先使用 BCrypt。
- 权限控制按角色和资源粒度设计，管理员与普通用户职责分离。
- 敏感配置通过环境变量或外部配置注入，不硬编码。
- 当前没有内置默认管理员账号和硬编码密码；初始化管理员、角色、权限时应通过迁移脚本或受控初始化流程写入 BCrypt 哈希。
- 认证采用 `Authorization: Bearer <token>`，后续 JWT / Token 过滤器应接入现有 `SecurityFilterChain`。

## 构建命令

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw clean package -DskipTests
./mvnw test
```

在受限环境或本地不希望写入用户 Maven 缓存时，优先使用项目内 Maven 仓库：

```bash
./mvnw -Dmaven.repo.local=.m2/repository test
./mvnw -Dmaven.repo.local=.m2/repository clean package -DskipTests
```

## 测试与质量

- 新增业务必须补核心测试，优先覆盖 Service 和关键 Controller。
- 关键流程至少包含：登录、权限、审核、配置、统计、导出相关测试。
- 测试实现优先复用 `spring-boot-starter-test`、MockMvc、Spring Security Test、Testcontainers 等成熟测试依赖。
- 生产前必须检查：接口契约、异常处理、日志、权限、数据一致性、回滚策略。
- 发现设计风险时，先评估再实现，不要直接做缩水版。

## 日志与配置

- 日志使用 SLF4J + Logback。
- 日志消息使用英文，代码注释使用英文。
- 开发环境优先 DEBUG，生产环境 INFO。
- 配置分环境管理，避免将密钥、口令、地址写死在代码里。

## 重要事项

- 积极使用 idea mcp 工具辅助开发
- 后续开发必须按 `Todos.md` 的阶段安排推进，并在任务完成、阶段调整或发现阻塞时同步更新该文件。
- 优先使用成熟依赖、官方推荐方案和现成 starter，必要时直接在 `pom.xml` 中补充，不要手动造轮子。
- 优先采用 MyBatis、MyBatis-Plus 及其成熟生态提升开发效率，避免为通用 CRUD、分页、条件构造重复造基础设施。
- 先定义接口契约，再实现服务与数据层。
- 后续新增功能必须优先复用现有实体基类、枚举、Mapper、统一响应、异常处理和安全配置；不要重复创建平行基础设施。
- 每次新增接口后必须同步导出最新 Swagger JSON 到 `api/api.json`。
- 不要为了短期交付牺牲可维护性。
- 面向上线质量开发：可观测性、异常处理、权限、回滚、兼容性都要考虑。
