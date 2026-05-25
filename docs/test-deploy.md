# 测试环境部署说明

## 1. 目标

本说明用于部署多人共享联调环境，供前端、测试和产品统一访问。

推荐访问链路：

```text
https://${TEST_DOMAIN} -> Nginx -> 127.0.0.1:18080 -> Spring Boot
```

## 2. 文件说明

- `src/main/resources/application-test.yaml`：测试环境 Spring Boot 配置。
- `Dockerfile`：应用镜像构建文件。
- `compose.test.yml`：测试环境 `app + mysql + redis` 编排文件。
- `.env.test.example`：测试环境变量模板。
- `docker/nginx/jsmedicine-test.conf.template`：宿主机 Nginx 站点配置模板。

## 3. 服务器准备

要求：

- 已安装 Docker Engine 和 Docker Compose Plugin。
- 宿主机已安装 Nginx。
- 域名已解析到服务器公网 IP。

推荐目录：

```bash
/root/jsmedicine-test
```

## 4. 部署步骤

1. 上传代码到服务器，或在服务器拉取仓库。
2. 复制环境变量模板：

```bash
cp .env.test.example .env.test
```

3. 修改 `.env.test` 中的 `TEST_DOMAIN`、密码、域名白名单和端口绑定。
4. 启动服务：

```bash
docker compose --env-file .env.test -f compose.test.yml up -d --build
```

5. 检查容器状态：

```bash
docker compose --env-file .env.test -f compose.test.yml ps
```

6. 检查应用健康状态：

```bash
curl http://127.0.0.1:18080/actuator/health
```

## 5. Nginx 配置

1. 将 `docker/nginx/jsmedicine-test.conf` 复制到宿主机，例如：

```bash
export $(grep -v '^#' .env.test | xargs)
envsubst '${TEST_DOMAIN}' < docker/nginx/jsmedicine-test.conf.template > /etc/nginx/conf.d/jsmedicine-test.conf
```

2. 确认生成后的 `server_name` 已替换为真实联调域名 `${TEST_DOMAIN}`。
3. 测试并重载 Nginx：

```bash
nginx -t
systemctl reload nginx
```

## 6. 交付给联调人员的信息

- 接口根地址：`https://${TEST_DOMAIN}`
- Swagger UI：`https://${TEST_DOMAIN}/swagger-ui.html`
- OpenAPI JSON：`https://${TEST_DOMAIN}/api/docs`
- 管理端测试账号：`td_admin / Admin@123456`
- 用户端测试账号：`td_user_01 / User@123456`

## 7. 发布与回滚

重新发布：

```bash
git pull
docker compose --env-file .env.test -f compose.test.yml up -d --build
```

查看日志：

```bash
docker compose --env-file .env.test -f compose.test.yml logs -f app
```

停止环境：

```bash
docker compose --env-file .env.test -f compose.test.yml down
```

回滚建议：

- 回滚到上一个 Git 提交，再执行 `docker compose ... up -d --build`。
- 不要在未确认备份可恢复前删除 MySQL 数据卷。

## 8. 注意事项

- 共享联调环境不要使用 `dev` profile。
- 测试环境数据库和正式数据库必须完全隔离。
- `app` 对宿主机仅绑定 `127.0.0.1:18080`，不要直接暴露公网端口。
- `mysql` 和 `redis` 不要映射公网端口。
- 共享联调环境由固定负责人发布，避免多人直接改环境。
- 如果联调域名变更，只需修改 `.env.test` 中的 `TEST_DOMAIN`，然后重新生成 Nginx 配置并重载。
