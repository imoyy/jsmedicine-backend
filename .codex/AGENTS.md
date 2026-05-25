# AGENTS.md

## 项目概览

`中医在线` 管理端后端项目，覆盖登录、账户管理、系统管理、首页管理、课程管理、图书管理、资讯管理、播客管理、专题管理、专家管理、题库管理、统计管理、直播管理、答疑管理、反馈管理等完整后台能力。

- 面向管理端提供 RESTful API。
- 开发过程覆盖 MVP、联调、验收、上线前质量保障，不只停留在最小可用版本。
- 优先复用成熟方案，避免重复造轮子。
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
| MySQL | 生产数据库 |
| Redis | 缓存、会话  |
| Lombok | 简化样板代码 |
| MapStruct | DTO / Entity 映射 |
| springdoc-openapi | Swagger UI 接口文档 |
| Flyway | 数据库迁移（优先） |

## 项目结构

```text
.
├── src/main/java/com/gugugaga/jsmedicine/
│   ├── JsmedicineApplication.java
│   ├── common/
│   │   ├── config/
│   │   ├── exception/
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
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
├── src/test/java/
└── api/
```

## 架构原则

- Controller 只负责参数接收、校验、响应返回。
- Service 负责业务编排、事务控制、权限判断、状态流转。
- Repository / Mapper 只负责数据访问。
- DTO 和 Entity 分离，不直接暴露数据库模型。
- 模块按业务域拆分，模块间通过 Service 协作，避免循环依赖。
- 不要增加无必要的中间层和抽象层。

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

- 必须接入 `springdoc-openapi` 并提供 Swagger UI。
- 每完成一个后端接口任务，必须同步导出最新的全量 Swagger JSON。
- 导出的文件保存到项目主目录下的 `api/` 文件夹。
- 保留历史文件，不覆盖删除。
- 最新接口文件名必须固定为 `api.json`。
- 如果需要保留阶段版本，可额外生成带时间戳或版本号的文件，但 `api.json` 始终代表最新版本。

## 数据层

- 优先使用 MySQL。
- 数据表结构通过迁移脚本管理，优先使用 Flyway。
- 表结构和字段命名保持清晰、可读、可维护。
- 涉及状态流转的字段必须用枚举或明确常量定义。
- 不在 Entity 中写业务逻辑。

## 安全

- 使用 Spring Security 实现认证与授权。
- 密码必须加密存储，优先使用 BCrypt。
- 权限控制按角色和资源粒度设计，管理员与普通用户职责分离。
- 敏感配置通过环境变量或外部配置注入，不硬编码。

## 构建命令

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw clean package -DskipTests
./mvnw test
```

## 测试与质量

- 新增业务必须补核心测试，优先覆盖 Service 和关键 Controller。
- 关键流程至少包含：登录、权限、审核、配置、统计、导出相关测试。
- 生产前必须检查：接口契约、异常处理、日志、权限、数据一致性、回滚策略。
- 发现设计风险时，先评估再实现，不要直接做缩水版。

## 日志与配置

- 日志使用 SLF4J + Logback。
- 日志消息使用英文，代码注释使用英文。
- 开发环境优先 DEBUG，生产环境 INFO。
- 配置分环境管理，避免将密钥、口令、地址写死在代码里。

## 重要事项

- 优先使用成熟依赖和官方推荐方案。
- 先定义接口契约，再实现服务与数据层。
- 不要为了短期交付牺牲可维护性。
- 面向上线质量开发：可观测性、异常处理、权限、回滚、兼容性都要考虑。
