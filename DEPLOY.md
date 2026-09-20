# 部署文档

本文档描述如何将智能工单管理系统从零部署到一台服务器上。

## 部署架构

项目使用 **Docker Compose** 编排 4 个容器：
<img width="720" height="682" alt="image" src="https://github.com/user-attachments/assets/80db65f6-4f57-4ec0-b39d-ff47cf037205" />

## 环境要求

### 服务器

- **操作系统**：Ubuntu 20.04 或以上（本文以 Ubuntu 26 为例）
- **内存**：建议 4GB 以上（4 个容器加起来约 2.5GB）
- **磁盘**：建议 20GB 以上
- **网络**：能访问外网（拉取 Docker 镜像、调用通义千问 API）

### 软件

| 软件 | 版本要求 | 说明 |
|------|---------|------|
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | v2+ | 容器编排（注意：是 `docker compose` 而不是 `docker-compose`） |

### 外部服务

- **通义千问 API Key**：在[阿里云百炼](https://bailian.console.aliyun.com/)注册并生成

## 部署步骤

### 1. 安装 Docker

bash
# 更新软件源
sudo apt update

# 安装 Docker（使用官方脚本）
curl -fsSL https://get.docker.com | sudo sh

# 如果国内网络慢，使用阿里云镜像：
# curl -fsSL https://get.docker.com | sudo sh -s docker --mirror Aliyun

# 将当前用户加入 docker 组（免 sudo）
sudo usermod -aG docker $USER

# 重新登录以使用户组生效
exit
# 重新 SSH 登录后

验证：

bash
docker --version
docker compose version
docker run hello-world
2. 克隆项目
bash
git clone https://github.com/Bulut3900/ai-ticket-system.git
cd ai-ticket-system
3. 打包后端
在开发机（Windows/Mac）上执行，或将源码复制到服务器打包：

bash
cd backend
mvn clean package -DskipTests
cp target/ai-service-0.0.1-SNAPSHOT.jar ../aiservice-deploy/backend/
cd ..
4. 打包前端
bash
cd frontend
npm install
npm run build
cp -r dist ../aiservice-deploy/frontend/
cd ..
5. 配置 API Key
编辑 aiservice-deploy/docker-compose.yml：

yaml
services:
  backend:
    environment:
      SPRING_AI_OPENAI_API_KEY: sk-your-real-key-here   # ← 修改这里
注意：不要把真实 API Key 提交到 Git。生产环境推荐通过环境变量或密钥管理服务注入。

6. 启动所有容器
bash
cd aiservice-deploy
docker compose up -d
首次启动会：

拉取镜像（MySQL、Redis Stack、JDK 25、Nginx）

构建后端、前端镜像

启动 4 个容器

首次启动时间约 5-10 分钟（取决于网络）。

7. 初始化数据库
首次部署时，MySQL 是空的，需要建表。

方式 A：用任意 MySQL 客户端（Navicat、DBeaver）连接：

主机：服务器 IP

端口：3306

用户名：root

密码：123456

数据库：ticket_db

执行 backend/src/main/resources/schema.sql 里的建表语句。

方式 B：直接在服务器上执行：

bash
docker exec -i mysql-ticket mysql -uroot -p123456 < backend/src/main/resources/schema.sql
8. 验证部署
bash
# 查看所有容器状态
docker compose ps

# 查看后端日志
docker compose logs --tail=100 backend

# 查看前端日志
docker compose logs --tail=100 frontend
预期：4 个容器都显示 Up，后端日志出现 Started AiServiceApplication in x.xxx seconds。

浏览器访问 http://服务器 IP，应该能看到登录页。

运维操作
停止所有容器
bash
docker compose down
重启所有容器
bash
docker compose restart
重启单个服务
bash
docker compose restart backend
查看实时日志
bash
docker compose logs -f backend
重新构建并启动（代码更新后）
bash
# 后端更新
cd aiservice-deploy/backend
# 更新 JAR
docker compose build --no-cache backend
docker compose up -d backend

# 前端更新
cd aiservice-deploy/frontend
# 更新 dist
docker compose build --no-cache frontend
docker compose up -d frontend
数据持久化
MySQL 数据存储在 Docker Volume mysql-data 中，即使容器被删除，数据也不会丢失。

备份数据：

bash
docker exec mysql-ticket mysqldump -uroot -p123456 ticket_db > backup_$(date +%Y%m%d).sql
恢复数据：

bash
docker exec -i mysql-ticket mysql -uroot -p123456 ticket_db < backup_20260920.sql
版本回滚
假设你部署了新版本，发现问题需要回滚：

方式 1：用 Git 回滚

bash
cd ~/ai-ticket-system
git log --oneline                    # 找到上一个版本的 commit ID
git checkout <commit-id>
cd aiservice-deploy
docker compose down
docker compose build --no-cache
docker compose up -d
方式 2：保留 JAR 备份

每次部署前，备份旧 JAR：

bash
cp aiservice-deploy/backend/ai-service-0.0.1-SNAPSHOT.jar \
   aiservice-deploy/backend/ai-service-0.0.1-SNAPSHOT.jar.bak
回滚时反向覆盖：

bash
cp aiservice-deploy/backend/ai-service-0.0.1-SNAPSHOT.jar.bak \
   aiservice-deploy/backend/ai-service-0.0.1-SNAPSHOT.jar
docker compose build --no-cache backend
docker compose up -d backend
常见问题排查
问题 1：MySQL 连接失败
现象：后端日志显示 Communications link failure

排查：

bash
# 检查 MySQL 容器是否运行
docker compose ps mysql-ticket

# 检查 MySQL 是否健康
docker exec mysql-ticket mysql -uroot -p123456 -e "show databases;"
原因：

MySQL 容器挂掉 → docker compose restart mysql-ticket

端口被占用 → sudo netstat -tlnp | grep 3306 查看占用进程

问题 2：Redis 连接失败
现象：后端日志显示 Failed to connect to 127.0.0.1:6379

排查：

bash
# 检查 Redis 容器是否运行
docker compose ps redis-stack

# 检查 Redis 是否健康
docker exec redis-stack redis-cli ping   # 应返回 PONG
原因：

Redis 容器挂掉 → docker compose restart redis-stack

环境变量未生效 → 确认 docker-compose.yml 里 SPRING_DATA_REDIS_HOST=redis

问题 3：Nginx 启动报 host not found in upstream "backend"
现象：前端容器反复重启，日志显示 host not found in upstream "backend"

原因：Nginx 启动时，backend 容器还没启动，DNS 解析失败。

解决：修改 frontend/nginx.conf，使用 resolver + 变量延迟解析：

nginx
resolver 127.0.0.11 valid=10s;

location /api/ {
    set $backend_host "backend:8081";
    proxy_pass http://$backend_host;
    ...
}
问题 4：前端能打开，但登录提示"网络异常"
原因：前端 baseURL 配置成了 http://localhost:8081，部署后应该用相对路径。

解决：修改 frontend/src/utils/request.js：

javascript
const request = axios.create({
    baseURL: '',   // 空字符串，使用相对路径
    timeout: 30000
})
问题 5：AI 接口返回 500 错误
排查：

bash
docker compose logs --tail=50 backend | grep -i "ai"
常见原因：

API Key 无效或余额不足

网络无法访问 dashscope.aliyuncs.com

模型名错误（应使用 qwen-plus）

问题 6：RAG 知识库上传失败
排查：

bash
# 检查 Redis Stack 是否加载了 RediSearch 模块
docker exec redis-stack redis-cli MODULE LIST | grep search
预期：输出 search 模块信息。

如果没有，说明 Redis 镜像不对，应该用 redis/redis-stack:latest。

生产环境建议
本文档描述的部署方式适用于测试和演示。生产环境需要考虑：

API Key 安全：不要写在 docker-compose.yml，用 Docker Secrets 或环境变量文件

HTTPS：用 Nginx 配置 SSL 证书（Let's Encrypt）

数据库密码：修改默认密码，使用强密码

日志收集：使用 ELK 或 Loki 收集容器日志

监控告警：使用 Prometheus + Grafana 监控容器状态

数据备份：定期备份 MySQL 和 Redis 数据

CI/CD：使用 GitHub Actions 或 Jenkins 自动构建部署

限流熔断：引入 Sentinel 保护后端接口

附：目录结构
text
aiservice-deploy/
├── backend/
│   ├── Dockerfile                        # 后端镜像构建脚本
│   └── ai-service-0.0.1-SNAPSHOT.jar     # 后端可执行 JAR
├── frontend/
│   ├── Dockerfile                        # 前端镜像构建脚本
│   ├── nginx.conf                        # Nginx 配置
│   └── dist/                             # 前端构建产物
└── docker-compose.yml                    # 容器编排配置


---

## 操作步骤

1. 打开 GitHub 仓库：https://github.com/Bulut3900/ai-ticket-system
2. 点击 **"Add file"** → **"Create new file"**
3. 文件名输入：`DEPLOY.md`
4. 把上面 markdown 代码块里**所有内容**粘贴进去
5. Commit message 填：`docs: 添加部署文档`
6. 点击 **Commit new file**

---

## 顺便完善 README

打开 `README.md`，在 `## 📚 API 文档` 那一节**下面**，加上：

```markdown
## 🚀 部署文档

详细的部署步骤和常见问题排查请查看 [DEPLOY.md](./DEPLOY.md)。

完成后你的仓库会是这样的
text
ai-ticket-system/
├── README.md         ← 项目总览 + 架构 + 快速启动
├── API.md            ← 接口文档
├── DEPLOY.md         ← 部署文档
├── backend/
├── frontend/
└── aiservice-deploy/
