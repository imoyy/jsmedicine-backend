# 共享联调环境约定

## 1. 环境定位

`test` 环境用于多人共享联调，不用于正式生产。

## 2. 使用约束

- 前端、测试、产品统一使用同一个测试域名。
- 禁止将联调环境数据库当作个人临时实验环境。
- 禁止未经通知直接清库、改表或重置测试账号。
- 发布入口统一由后端负责人执行。

## 3. 建议流程

1. 本地开发完成。
2. 合并到共享联调分支。
3. 更新测试环境。
4. 通知前端和测试开始验证。

## 4. 短信配置

共享联调环境从 `.env.test` 读取阿里云短信配置。Docker Compose 会通过 `compose.test.yml` 注入应用容器；直接运行 Spring Boot 时，`application.yaml` 也会可选读取项目根目录下的 `.env.test`。

必填项：

- `ALIYUN_SMS_ACCESS_KEY_ID`
- `ALIYUN_SMS_ACCESS_KEY_SECRET`
- `ALIYUN_SMS_SIGN_NAME`
- `ALIYUN_SMS_TEMPLATE_CODE`

可选项：

- `ALIYUN_SMS_REGION_ID`，默认 `ap-southeast-1`
- `ALIYUN_SMS_ENDPOINT`，默认 `dypnsapi.aliyuncs.com`

当以上必填项配置完整时，用户端 `/api/v1/app/auth/sms-code` 会通过阿里云 Dypnsapi `SendSmsVerifyCode` 真实发送；配置不完整时才使用 mock。

如果 `.env.test` 中的中文签名在日志中出现 `éé...` 这类乱码，说明 UTF-8 被 properties 方式读取成 ISO-8859-1。后端会自动修正常见中文误读；也可以显式写 Unicode escape，例如 `速通互联验证码` 写作 `\\u901F\\u901A\\u4E92\\u8054\\u9A8C\\u8BC1\\u7801`。

## 5. 资源地址策略

- 当前共享联调环境里，用户头像已经稳定走 `/api/v1/files/{id}/content` 公开读取路径。
- 课程、图书、资讯、播客、专题、直播、专家、知识库、首页配置等业务字段里的 `coverUrl`，以及 `audioUrl`、`videoUrl`、`playbackUrl`、`linkUrl` 等字段，当前仍允许保存普通 URL 字符串。
- dev 验收种子中的 `https://example.com/assets/...`、`https://example.com/live/...` 只是占位值，不保证在共享联调环境真实可访问。
- 需要对前端、测试提供真实可访问资源时，优先使用真实公网地址，或将对应文件登记到 `file_assets` 并把业务字段回填为 `/api/v1/files/{id}/content`。
- 禁止把对象存储临时签名 URL 当作长期业务真相源；签名 URL 只用于短期上传或受控下载链路。
