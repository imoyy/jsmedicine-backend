# 账户域数据库整改方案

本文档用于收敛管理端手册、当前代码实现和 Q0 审计结果之间的差异，作为后续 `V13__...sql` 起数据库迁移与接口调整的实施依据。

## 1. 现状判断

当前账户域模型由三部分组成：

- `app_users`：前台登录账号与基础资料
- `students`：学员扩展档案
- `experts`：专家主数据

现有问题不在于“少几个字段”，而在于三层模型职责边界不清：

1. `app_users` 没有显式表达前台用户业务身份，无法稳定承接管理端“普通 / 学员 / 专家”分类。
2. `students.user_id` 没有唯一约束，存在一名用户关联多条学员档案的风险。
3. `experts` 没有 `user_id`，专家主数据与前台登录账号完全脱节。
4. 学员地区、医院/机构、执业类型仍以自由文本或缺少字典表的方式存在，不利于筛选、统计、导入和联调一致性。
5. `students.certification_materials` 用 JSON 字符串承载认证材料，后续难以审计、替换和结构化查询。

## 2. 整改目标

整改目标不是重做账户体系，而是在保持当前模块边界的前提下，把“账号、身份、档案、基础字典”拆清楚：

- `app_users` 继续负责登录账号和公共资料
- 学员、专家仍保留在各自业务域
- 用户业务身份独立建模，避免把页面语义硬塞进单表
- 地区、机构、执业类型进入可维护的基础数据模型
- 认证材料从字符串迁移到结构化关系

## 3. 目标模型

### 3.1 账号层：`app_users`

保留现有账号职责，并补充少量公共资料字段：

- 保留：`username`、`password_hash`、`mobile`、`email`、`nickname`、`avatar_url`
- 保留：`auth_provider`、`wechat_open_id`、`wechat_union_id`
- 保留：`gender`、`status`、`registered_at`、`last_login_at`、`last_login_ip`
- 保留：`profile_completed`、`password_updated_at`
- 新增建议：`profile_signature`

约束说明：

- `app_users` 不承担“学员/专家”身份判定职责
- `profile_completed` 只作为展示缓存态，不作为唯一业务判断依据

### 3.2 身份层：`app_user_identities`

新增表：`app_user_identities`

建议字段：

- `id`
- `user_id`
- `identity_type`
- `identity_status`
- `is_primary`
- `activated_at`
- `deactivated_at`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`
- `deleted`

建议约束：

- `uk_app_user_identities_user_type (user_id, identity_type)`
- 索引：`idx_app_user_identities_user_primary (user_id, is_primary, deleted)`
- `identity_type` 仅存 `STUDENT`、`EXPERT`

约定：

- “普通用户”不单独落库，定义为“当前没有激活中的业务身份”
- 如果一个用户同时具备学员和专家资格，`is_primary` 只用于前台主展示身份，不替代业务档案

### 3.3 学员档案层：`students`

保留 `students` 作为学员业务档案，并做以下规范化：

- 保留：`user_id`、`student_no`、`real_name`、`mobile`、`id_card_no`
- 保留：认证状态与审核字段
- 保留：`enrolled_at`
- 补充唯一约束：`uk_students_user_id (user_id)`
- 新增建议：
  - `province_code`
  - `city_code`
  - `district_code`
  - `organization_id`
  - `practice_type_id`

保留现有文本字段用于过渡和冗余展示：

- `province`
- `city`
- `district`
- `organization`
- `position_title`

### 3.4 专家档案层：`experts`

保留 `experts` 作为专家主数据，并补充账号绑定能力：

- 新增：`user_id`
- 新增建议：`organization_id`
- 新增建议：`practice_type_id`

约束建议：

- `uk_experts_user_id (user_id)`，允许空值，但非空时唯一

说明：

- 历史专家不必强制全部绑定用户
- 后续“用户成为专家”场景通过 `experts.user_id` 建立一对一绑定

### 3.5 基础字典：机构与执业类型

新增表：`organizations`

建议字段：

- `id`
- `org_code`
- `org_name`
- `org_type`
- `province_code`
- `city_code`
- `district_code`
- `address`
- `status`
- `sort_order`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`
- `deleted`

新增表：`practice_types`

建议字段：

- `id`
- `parent_id`
- `type_code`
- `type_name`
- `status`
- `sort_order`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`
- `deleted`

说明：

- `students.practice_type_id`、`experts.practice_type_id` 均引用该表
- 行政区暂不单独建库表，先以编码字段落入业务表；如果后续联调需要完整区域字典，再单独扩展

### 3.6 认证材料：`student_certification_files`

新增表：`student_certification_files`

建议字段：

- `id`
- `student_id`
- `file_asset_id`
- `source_url`
- `material_type`
- `sort_order`
- `created_by`
- `updated_by`
- `created_at`
- `updated_at`
- `deleted`

说明：

- 现有 `students.certification_materials` 迁移完成前保留
- 新流程优先引用 `file_assets.id`，也允许暂存 `source_url` 承接历史 URL 和外部资源

## 4. 迁移拆分顺序

### V13：账户身份主链收口

目标：先把账号、身份、学员、专家之间的主链关系补齐。

建议内容：

- 新增 `app_user_identities`
- `experts` 增加 `user_id`
- `students.user_id` 增加唯一约束
- `app_users` 增加 `profile_signature`

### V14：地区、机构、执业类型和认证材料规范化

目标：把筛选、统计和认证审核依赖的自由文本收敛为可维护结构。

建议内容：

- 新增 `organizations`
- 新增 `practice_types`
- `students` 增加地区编码、`organization_id`、`practice_type_id`
- `experts` 增加 `organization_id`、`practice_type_id`
- 新增 `student_certification_files`
- 业务代码改为读新表

### V15：账户域接口聚合补强

目标：在账户域主链稳定后，补充管理端需要的身份聚合和基础字典读取能力。

建议内容：

- 用户详情聚合返回激活身份、学员摘要和专家摘要
- 管理端按机构、执业类型、地区编码筛选学员和专家
- 根据前端联调需要补充基础字典查询接口

### V16：互动域约束补强

目标：处理账户域整改之后可顺手修复的规范性问题。

建议内容：

- 收敛 `feedback_type` 为枚举或字典
- 为答疑回复主体补业务约束
- 补充必要索引和数据清理脚本

## 5. 回填策略

### 5.1 用户身份回填

`app_user_identities` 回填规则：

- 对每个存在 `students.user_id` 的用户插入 `STUDENT`
- 对每个存在 `experts.user_id` 的用户插入 `EXPERT`
- 如果一个用户只有一种身份，则设为 `is_primary = 1`
- 如果一个用户同时具备两种身份，先统一以 `STUDENT` 设主身份，后续由业务确认是否需要调整

### 5.2 专家用户绑定回填

历史专家数据当前没有稳定的账号关联依据。

处理原则：

- 不做猜测性回填
- `experts.user_id` 初始允许为空
- 仅对有明确业务依据的数据做手工绑定

### 5.3 学员唯一约束前的数据清洗

在增加 `students.user_id` 唯一约束前，先执行排查：

- 查询 `user_id` 重复的学员记录
- 保留认证状态更完整、更新时间更新的一条
- 其余记录人工确认后再归档或逻辑删除

### 5.4 认证材料回填

从 `students.certification_materials` 读取历史 JSON URL 列表：

- 逐条尝试匹配 `file_assets.url`
- 匹配成功则回填 `file_asset_id`
- 匹配失败则保留原 URL 作为迁移备注，避免静默丢失

## 6. 接口影响边界

### 管理端

受影响最大的是账户管理：

- `/api/v1/admin/users`
- `/api/v1/admin/users/{id}`
- `/api/v1/admin/students`
- `/api/v1/admin/students/{id}`
- `/api/v1/admin/experts`

调整方向：

- 用户列表和详情改为聚合返回账号、主身份、学员摘要、专家摘要
- 学员、专家的专业资料编辑仍留在各自模块，不合并为一个混合接口

### 用户端

首批只做兼容性调整，不重设计接口：

- `/api/v1/app/profile`
- `/api/v1/app/profile/certification`

调整原则：

- 当前 `AppProfileResponse` 可继续返回 `studentId` 和认证状态
- 后续如需展示主身份，再新增字段，不直接破坏现有前端契约

## 7. 非目标

本轮整改明确不做以下事情：

- 不把前台用户身份并入后台 `sys_roles`
- 不重做管理端 RBAC
- 不强制所有专家都变成登录用户
- 不一次性改写所有列表和统计接口
- 不修改 `V1` 到 `V12` 历史迁移

## 8. 实施建议

推荐实施顺序：

1. 先落 `V13` 设计和实体调整草案
2. 先做不破坏现有接口的字段与关系补充
3. 完成数据清洗和回填验证后，再收紧唯一约束和新接口语义
4. 等账户域主链稳定后，再补脚本化接口验收

当前项目下一步应优先推进：

- 编写 `V13__normalize_account_domain_core.sql`
- 新增 `AppUserIdentityType`、`AppUserIdentityStatus` 等枚举与实体
- 补充账户域聚合查询 DTO 设计
