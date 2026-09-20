# 智能工单管理系统（AI-Powered Ticket Management System）

一个基于 **Spring Boot 3 + Spring AI + Vue 3** 的企业级工单管理系统。系统集成了 AI 能力，实现工单自动分类、RAG 知识库问答、SSE 流式对话等核心功能，帮助企业内部高效处理问题反馈。

## ✨ 核心功能

- **用户模块**：注册、登录、JWT 认证、权限控制
- **工单模块**：工单 CRUD、状态流转、权限隔离
- **AI 工单分析**：提交工单时，AI 自动提取摘要、分类、优先级
- **AI 智能对话**：支持流式输出（打字机效果）、多轮对话记忆
- **RAG 知识库**：上传企业文档，AI 基于文档内容精准回答
- **完整前端**：Vue 3 + Element Plus 实现的可视化界面
- **Docker 部署**：4 个容器一键编排启动

## 🛠 技术栈

### 后端
- **框架**：Spring Boot 3.5.0
- **AI 框架**：Spring AI 1.0.0
- **大模型**：通义千问 qwen-plus（对话）+ text-embedding-v4（向量化）
- **ORM**：MyBatis-Plus 3.5.9
- **认证**：JWT（jjwt 0.12.6）+ BCrypt 密码加密
- **数据库**：MySQL 8.0 + Redis Stack（向量存储）
- **JDK**：Java 25

### 前端
- **框架**：Vue 3 + Vite
- **UI**：Element Plus
- **HTTP**：Axios
- **路由**：Vue Router 4

### 部署
- **容器**：Docker + Docker Compose
- **Web 服务器**：Nginx（前端 + 反向代理）
- **环境**：Ubuntu 虚拟机 + VMware

## 🏗 系统架构
<img width="698" height="795" alt="image" src="https://github.com/user-attachments/assets/e1fbdd87-518a-48cf-8556-46844a24deb6" />

## 🎯 项目亮点

### 1. AI 与业务深度融合
工单提交时，后端自动调用 AI 分析内容，提取摘要、分类、优先级，实现"用户只管描述问题，AI 自动完成结构化"。**AI 分析失败时使用默认值兜底，保证工单能够正常创建**——这是工程思维。

### 2. RAG 知识库问答
基于 **Redis Stack 向量数据库** + 通义千问 Embedding 模型，实现企业文档的向量化存储与检索。用户提问时，系统先检索相关文档片段，再让 AI 基于这些片段回答，**做到不瞎编、可溯源**。

### 3. SSE 流式输出
使用 Spring AI 的 `stream()` API + WebFlux 的 `Flux<String>`，配合前端 `fetch` + `ReadableStream` 手动解析 SSE，实现打字机效果。

### 4. 多轮对话记忆
基于 Spring AI 的 `MessageWindowChatMemory` + `InMemoryChatMemoryRepository`，按 `sessionId` 隔离不同用户的对话历史，支持多轮上下文。

### 5. 手动配置 VectorStore Bean
由于 Spring AI 1.0.0 的 `spring-ai-starter-vector-store-redis` 自动配置在特定环境下不稳定，改用 `@Configuration` 手动配置 `RedisVectorStore` Bean，**提高可控性**。

### 6. Docker Compose 一键编排
4 个容器（Nginx、后端、MySQL、Redis）通过 `docker-compose.yml` 统一编排，支持一键启动、一键停止、一键回滚。

## 🚀 快速启动

### 前置条件
- Docker + Docker Compose
- 通义千问 API Key（[阿里云百炼](https://bailian.console.aliyun.com/) 注册获取）

### 启动步骤

**1. 克隆仓库**

```bash
git clone https://github.com/Bulut3900/ai-ticket-system.git
cd ai-ticket-system
2. 打包后端

bash
cd backend
mvn clean package -DskipTests
cp target/ai-service-0.0.1-SNAPSHOT.jar ../aiservice-deploy/backend/
cd ..
3. 打包前端

bash
cd frontend
npm install
npm run build
cp -r dist ../aiservice-deploy/frontend/
cd ..
4. 配置 API Key

编辑 aiservice-deploy/docker-compose.yml，把 SPRING_AI_OPENAI_API_KEY 改成你的真实 Key：

yaml
environment:
  SPRING_AI_OPENAI_API_KEY: sk-your-real-key-here
5. 启动所有容器

bash
cd aiservice-deploy
docker compose up -d
6. 初始化数据库

用任意 MySQL 客户端连接 localhost:3306（root/123456），执行建表 SQL（见 backend/src/main/resources/schema.sql）。

7. 访问系统

浏览器打开：http://localhost

📁 项目结构
ai-ticket-system/
├── backend/                              # 后端源码
│   ├── src/main/java/com/example/aiservice/
│   │   ├── common/                       # 公共类（Result、异常处理）
│   │   ├── config/                       # 配置类（WebConfig、VectorStoreConfig）
│   │   ├── controller/                   # 控制器
│   │   ├── dto/                          # 数据传输对象
│   │   ├── entity/                       # 实体类
│   │   ├── interceptor/                  # JWT 拦截器
│   │   ├── mapper/                       # MyBatis Mapper
│   │   ├── model/                        # AI 模型（TicketAnalysis）
│   │   ├── service/                      # 业务层
│   │   └── util/                         # 工具类（JwtUtil、UserContext）
│   ├── src/main/resources/
│   │   └── application.yml               # 配置文件
│   └── pom.xml
├── frontend/                             # 前端源码
│   ├── src/
│   │   ├── views/                        # 页面（Login、Register、Tickets、Chat、Knowledge）
│   │   ├── router/                       # 路由
│   │   ├── utils/                        # Axios 封装
│   │   ├── App.vue
│   │   └── main.js
│   ├── package.json
│   └── vite.config.js
├── aiservice-deploy/                     # 部署文件
│   ├── backend/
│   │   └── Dockerfile
│   ├── frontend/
│   │   ├── Dockerfile
│   │   └── nginx.conf
│   └── docker-compose.yml
├── .gitignore
└── README.md
📚 API 文档
用户模块
方法	路径	说明
POST	/api/user/register	用户注册
POST	/api/user/login	用户登录（返回 JWT）
工单模块
方法	路径	说明
POST	/api/ticket	创建工单（自动 AI 分析）
GET	/api/ticket	查询工单列表
GET	/api/ticket/{id}	查询工单详情
PUT	/api/ticket/{id}/status	更新工单状态（仅管理员）
DELETE	/api/ticket/{id}	删除工单
AI 模块
方法	路径	说明
GET	/ai/chat	AI 对话（普通）
GET	/ai/stream	AI 对话（SSE 流式）
GET	/ai/analyze	工单分析（结构化输出）
知识库模块
方法	路径	说明
POST	/api/kb/upload	上传文档到知识库
POST	/api/kb/ask	基于知识库提问
DELETE	/api/kb/clear	清空知识库
🔧 技术难点与解决方案
难点	解决方案
Spring AI 1.0.0 与 Spring Boot 4.x 不兼容	降级 Spring Boot 至 3.5.0
RedisVectorStore 自动配置不稳定	手动配置 Bean
SSE 中文乱码	配置 server.servlet.encoding.force=true
Docker 内 Redis 连接失败	通过环境变量 SPRING_DATA_REDIS_HOST=redis 注入
Nginx 启动时 backend DNS 解析失败	使用 resolver 127.0.0.11 + 变量延迟解析
前后端跨域	前端使用相对路径 + Nginx 反向代理
📝 后续计划
□ 微服务拆分（Spring Cloud Alibaba）：auth-service、ticket-service、ai-service、gateway
□ Nacos 服务注册与配置中心
□ Sentinel 限流熔断
□ RAG 支持 PDF、Word、Markdown 等多种格式
□ 管理员后台（用户管理、工单分配）
□ 单元测试与集成测试
👨💻 作者
GitHub: @Bulut3900

📄 License
MIT License

text

---

## 操作方式

1. 打开 `https://github.com/Bulut3900/ai-ticket-system`
2. 点击 **"Add a README"** 绿色按钮（如果已经创建过 README，点编辑铅笔图标）
3. **全选、删除**原有内容（如果有）
4. **粘贴**上面这一整段
5. 拉到页面底部，Commit message 填：`docs: 添加 README`
6. 点 **Commit changes**

---

## 粘贴后检查

刷新仓库首页，应该能看到 README 的**渲染效果**，包括：
- 标题
- 技术栈列表
- 架构图（代码块）
- 项目结构树
- API 表格
