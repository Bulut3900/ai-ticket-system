# 部署文档

本文档描述如何将智能工单管理系统从零部署到一台服务器上。

## 部署架构

项目使用 Docker Compose 编排 8 个容器，构成完整的微服务集群：

┌─────────────────────────────────────────────┐
│              宿主机（Ubuntu）                │
│                                              │
│  ┌──────────┐                                │
│  │ frontend │  ← Nginx（80）                 │
│  └────┬─────┘                                │
│       ↓ /api/**  /ai/**                      │
│  ┌──────────┐                                │
│  │ gateway  │  ← Spring Cloud Gateway（9000）│
│  └────┬─────┘                                │
│       ↓ Nacos 服务发现                        │
│  ┌────┴──────┬──────────┬─────────┐         │
│  ↓           ↓          ↓         ↓         │
│ auth-service ticket-service ai-service      │
│  (8082)      (8083)        (8081)           │
│                  ↓ Feign                    │
│                  └─────→ ai-service         │
│                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  MySQL   │  │  Redis   │  │  Nacos   │   │
│  │  (3306)  │  │  (6379)  │  │  (8848)  │   │
│  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────┘

### 8 个容器一览

| 容器 | 镜像 | 端口 | 职责 |
|------|------|------|------|
| frontend | nginx:alpine | 80 | 前端静态文件 + 反向代理 |
| gateway | eclipse-temurin:25-jre | 9000 | API 网关、路由、跨域 |
| auth-service | eclipse-temurin:25-jre | 8082 | 用户注册、登录、JWT |
| ticket-service | eclipse-temurin:25-jre | 8083 | 工单 CRUD、Feign 调 AI |
| ai-service | eclipse-temurin:25-jre | 8081 | AI 对话、RAG、工单分析 |
| nacos | nacos/nacos-server:v2.4.3 | 8848, 9848 | 服务注册与配置中心 |
| mysql | mysql:8.0 | 3306 | 业务数据库 |
| redis | redis/redis-stack:latest | 6379 | 向量存储 |

## 环境要求

### 服务器

- 操作系统：Ubuntu 20.04 或以上（本文以 Ubuntu 26 为例）
- 内存：建议 8GB 以上（8 个容器加起来约 5-6GB）
- 磁盘：建议 30GB 以上
- 网络：能访问外网（拉取 Docker 镜像、调用通义千问 API）

### 软件

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | v2+ | 容器编排（注意是 docker compose 而不是 docker-compose） |
| JDK | 17+ | 本地打包 JAR 用 |
| Maven | 3.8+ | 本地打包 JAR 用 |
| Node.js | 18+ | 本地打包前端用 |

### 外部服务

- 通义千问 API Key：在阿里云百炼（https://bailian.console.aliyun.com/）注册并生成

## 部署步骤

### 1. 安装 Docker

sudo apt update
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
exit
# 重新登录后
docker --version
docker compose version
docker run hello-world

### 2. 克隆项目

git clone https://github.com/Bulut3900/ai-ticket-system.git
cd ai-ticket-system/backend/ai-service

### 3. 打包后端（4 个微服务）

在开发机上执行：

cd backend/ai-service
mvn clean install -DskipTests

打包后会生成 4 个 JAR：

- gateway/target/gateway-1.0.0-SNAPSHOT.jar
- auth-service/target/auth-service-1.0.0-SNAPSHOT.jar
- ticket-service/target/ticket-service-1.0.0-SNAPSHOT.jar
- ai-service/target/ai-service-1.0.0-SNAPSHOT.jar

### 4. 打包前端

cd frontend/ticket-frontend
npm install
npm run build
cp -r dist ../../backend/ai-service/frontend/

打包后会生成：

- backend/ai-service/frontend/dist/（前端产物）

### 5. 配置 API Key

编辑 backend/ai-service/docker-compose.yml：

services:
  ai-service:
    environment:
      SPRING_AI_OPENAI_API_KEY: sk-your-real-key-here   # ← 修改这里

注意：不要把真实 API Key 提交到 Git。

### 6. 启动所有容器

cd backend/ai-service
docker compose up -d --build

首次启动会：

- 拉取镜像（Nacos、MySQL、Redis Stack、JDK 25、Nginx），约 3-5GB
- 构建 5 个自定义镜像（gateway、auth、ticket、ai、frontend）
- 启动 8 个容器

首次启动时间约 10-20 分钟（取决于网络）。

### 7. 初始化数据库

首次部署时，MySQL 会自动执行 mysql-init/init.sql（通过 volume 挂载），自动建表 + 初始化 admin 用户。

如果没有自动执行，手动执行：

docker exec -i mysql mysql -uroot -p123456 < backend/ai-service/mysql-init/init.sql

### 8. 验证部署

# 查看所有容器状态
docker compose ps

# 预期 8 个容器都是 Up

# 查看各服务日志
docker compose logs --tail=100 gateway
docker compose logs --tail=100 auth-service
docker compose logs --tail=100 ticket-service
docker compose logs --tail=100 ai-service

预期：各服务日志出现 Started XxxApplication in x.xxx seconds 和 nacos registry ... register finished。

访问 Nacos 控制台：http://服务器IP:8848/nacos

应该看到 4 个服务：gateway、auth-service、ticket-service、ai-service。

浏览器访问：http://服务器IP

应该能看到登录页。

## 运维操作

### 停止所有容器

docker compose down

### 重启所有容器

docker compose restart

### 重启单个服务

docker compose restart gateway
docker compose restart ai-service

### 查看实时日志

docker compose logs -f gateway
docker compose logs -f ai-service

### 重新构建并启动（代码更新后）

# 后端某服务更新
cd backend/ai-service
mvn clean install -DskipTests
docker compose build --no-cache gateway
docker compose up -d gateway

# 前端更新
cd frontend/ticket-frontend
npm run build
cp -r dist ../../backend/ai-service/frontend/
cd ../../backend/ai-service
docker compose build --no-cache frontend
docker compose up -d frontend

### 数据持久化

MySQL 数据存储在 Docker Volume mysql-data 中，即使容器被删除，数据也不会丢失。

备份数据：

docker exec mysql mysqldump -uroot -p123456 ticket_db > backup_$(date +%Y%m%d).sql

恢复数据：

docker exec -i mysql mysql -uroot -p123456 ticket_db < backup_20260921.sql

## 版本回滚

方式 1：用 Git 回滚

cd ~/ai-ticket-system
git log --oneline                    # 找到上一个版本的 commit ID
git checkout <commit-id>
cd backend/ai-service
docker compose down
docker compose build --no-cache
docker compose up -d

方式 2：保留 JAR 备份

每次部署前，备份旧 JAR：

cp gateway/target/gateway-1.0.0-SNAPSHOT.jar gateway/target/gateway-1.0.0-SNAPSHOT.jar.bak

回滚时反向覆盖，重新构建：

cp gateway/target/gateway-1.0.0-SNAPSHOT.jar.bak gateway/target/gateway-1.0.0-SNAPSHOT.jar
docker compose build --no-cache gateway
docker compose up -d gateway

## 常见问题排查

### 问题 1：服务注册失败 Client not connected, current status:STARTING

现象：Gateway 或某个服务反复重启，日志显示 Client not connected, current status:STARTING。

原因：Nacos 2.4.3 启动需要 20-30 秒，服务启动时 Nacos 还没就绪。

解决：

1. 给所有 Java 服务加 restart: always（在 docker-compose.yml 里）
2. 在 application.yml 里加 spring.cloud.nacos.discovery.fail-fast: false

### 问题 2：Bean 冲突 ConflictingBeanDefinitionException

现象：auth-service 启动失败，日志显示 ConflictingBeanDefinitionException: bean name 'webConfig' ... conflicts。

原因：common 模块和 auth-service 模块都有 WebConfig，Bean 名冲突。

解决：删除 auth-service 模块下的 WebConfig.java 和 JwtInterceptor.java（它们已经移到 common 模块）。

### 问题 3：服务注册冲突（服务名重复）

现象：ticket-service 在 Nacos 里看不到，但容器显示 Up。

原因：ticket-service/application.yml 里 spring.application.name 被误写成 auth-service。

解决：改成 ticket-service。

### 问题 4：MySQL 连接失败

现象：服务日志显示 Communications link failure 或 Field 'creator_id' doesn't have a default value。

排查：

docker compose ps mysql
docker exec mysql mysql -uroot -p123456 -e "use ticket_db; show tables;"

原因：

- MySQL 容器挂了 → docker compose restart mysql
- 表不存在 → 手动执行 mysql-init/init.sql

### 问题 5：Nginx 启动报 host not found in upstream "gateway"

现象：前端容器反复重启，日志显示 host not found in upstream "gateway"。

原因：Nginx 启动时，gateway 容器还没启动，DNS 解析失败。

解决：在 frontend/nginx.conf 里用 resolver 127.0.0.11 + 变量延迟解析：

resolver 127.0.0.11 valid=10s;

location /api/ {
    set $gateway_host "gateway:9000";
    proxy_pass http://$gateway_host;
    ...
}

### 问题 6：前端能打开，但接口报"网络异常"

原因：前端 baseURL 配置成了 http://localhost:8081，部署后应该用相对路径。

解决：修改 frontend/ticket-frontend/src/utils/request.js：

const request = axios.create({
    baseURL: '',
    timeout: 30000
})

### 问题 7：AI 接口返回 500

排查：

docker compose logs --tail=50 ai-service | grep -i "ai"

常见原因：

- API Key 无效或余额不足
- 网络无法访问 dashscope.aliyuncs.com
- 模型名错误（应使用 qwen-plus）

## 生产环境建议

本文档描述的部署方式适用于测试和演示。生产环境需要考虑：

1. API Key 安全：使用 Docker Secrets 或环境变量文件
2. HTTPS：用 Nginx 配置 SSL 证书（Let's Encrypt）
3. 数据库密码：修改默认密码，使用强密码
4. 日志收集：使用 ELK 或 Loki 收集容器日志
5. 监控告警：使用 Prometheus + Grafana 监控容器状态
6. 数据备份：定期备份 MySQL 和 Redis 数据
7. CI/CD：使用 GitHub Actions 或 Jenkins 自动构建部署
8. 限流熔断：引入 Sentinel 保护后端接口

## 附：目录结构

backend/ai-service/
├── common/                          # 公共模块
├── gateway/                         # 网关服务
│   ├── Dockerfile
│   └── target/gateway-1.0.0-SNAPSHOT.jar
├── auth-service/                    # 认证服务
│   ├── Dockerfile
│   └── target/auth-service-1.0.0-SNAPSHOT.jar
├── ticket-service/                  # 工单服务
│   ├── Dockerfile
│   └── target/ticket-service-1.0.0-SNAPSHOT.jar
├── ai-service/                      # AI 服务
│   ├── Dockerfile
│   └── target/ai-service-1.0.0-SNAPSHOT.jar
├── frontend/                        # 前端镜像
│   ├── Dockerfile
│   ├── nginx.conf
│   └── dist/
├── mysql-init/                      # MySQL 初始化脚本
│   └── init.sql
├── pom.xml                          # 父 POM
└── docker-compose.yml               # 8 容器编排