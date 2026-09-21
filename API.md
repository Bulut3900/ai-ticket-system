# API 文档

本文档描述智能工单管理系统的所有 HTTP 接口。

## 基础信息

- **Base URL**（本地开发）：`http://localhost:8081`
- **Base URL**（Docker 部署）：`http://localhost`（通过 Nginx 反向代理）
- **认证方式**：JWT（除注册、登录外，所有接口都需要在请求头携带 Token）
- **请求格式**：JSON（除文件上传外）
- **响应格式**：统一 JSON

## 统一返回格式

所有接口返回统一结构：

json
{
  "code": 200,
  "message": "success",
  "data": { }
}
字段	类型	说明
code	Integer	状态码，200 表示成功，其他表示失败
message	String	提示信息
data	Any	返回数据，可能为 null
状态码约定
code	含义
200	成功
400	参数校验失败
401	未登录或 Token 过期
403	无权限访问
500	业务异常或系统异常
认证方式
除 /api/user/register 和 /api/user/login 外，所有接口都需要在请求头中携带 JWT Token：

text
Authorization: Bearer <your-token>
Token 通过登录接口获取，有效期 7 天。

一、用户模块
1.1 用户注册
URL：POST /api/user/register

是否需要认证：否

请求体：

参数	类型	必填	说明
username	String	是	用户名，3-20 位
password	String	是	密码，6-20 位
nickname	String	否	昵称，不填默认等于用户名
请求示例：

json
{
  "username": "alice",
  "password": "123456",
  "nickname": "爱丽丝"
}
成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": null
}
失败响应（用户名已存在）：

json
{
  "code": 500,
  "message": "用户名已存在",
  "data": null
}
1.2 用户登录
URL：POST /api/user/login

是否需要认证：否

请求体：

参数	类型	必填	说明
username	String	是	用户名
password	String	是	密码
请求示例：

json
{
  "username": "alice",
  "password": "123456"
}
成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 2,
    "username": "alice",
    "nickname": "爱丽丝",
    "role": "USER"
  }
}
失败响应（用户名或密码错误）：

json
{
  "code": 500,
  "message": "用户名或密码错误",
  "data": null
}
二、工单模块
2.1 创建工单
URL：POST /api/ticket

是否需要认证：是

说明：创建时会自动调用 AI 分析工单内容，提取摘要、分类、优先级。若 AI 分析失败，会使用默认值兜底，不影响工单创建。

请求体：

参数	类型	必填	说明
title	String	是	工单标题
content	String	是	工单内容
请求示例：

json
{
  "title": "公司系统登录失败",
  "content": "我今天早上登录公司系统，一直提示密码错误，试了五次都不行，急死了，下午还要开会"
}
成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "公司系统登录失败",
    "content": "我今天早上登录公司系统...",
    "summary": "用户今早登录公司系统时提示密码错误",
    "category": "技术故障",
    "priority": "高",
    "status": "PENDING",
    "creatorId": 2,
    "handlerId": null,
    "createTime": "2026-09-20T11:00:00",
    "updateTime": "2026-09-20T11:00:00"
  }
}
AI 分析字段说明：

字段	可能的值
category	技术故障、业务咨询、投诉建议、其他
priority	高、中、低
status	PENDING、PROCESSING、RESOLVED、CLOSED
2.2 查询工单列表
URL：GET /api/ticket

是否需要认证：是

说明：普通用户只能查到自己的工单，管理员（ADMIN）能查到所有工单。

成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "title": "公司系统登录失败",
      "summary": "用户今早登录公司系统时提示密码错误",
      "category": "技术故障",
      "priority": "高",
      "status": "PENDING",
      "createTime": "2026-09-20T11:00:00"
    }
  ]
}
2.3 查询工单详情
URL：GET /api/ticket/{id}

是否需要认证：是

路径参数：

参数	类型	说明
id	Long	工单 ID
成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "公司系统登录失败",
    "content": "...",
    "summary": "...",
    "category": "技术故障",
    "priority": "高",
    "status": "PENDING",
    "creatorId": 2,
    "createTime": "2026-09-20T11:00:00"
  }
}
失败响应（无权访问）：

json
{
  "code": 403,
  "message": "无权访问该工单",
  "data": null
}
2.4 更新工单状态
URL：PUT /api/ticket/{id}/status

是否需要认证：是

权限：仅管理员（ADMIN）

路径参数：

参数	类型	说明
id	Long	工单 ID
请求体：

参数	类型	必填	说明
status	String	是	新状态：PENDING / PROCESSING / RESOLVED / CLOSED
请求示例：

json
{
  "status": "RESOLVED"
}
成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": null
}
失败响应（无权限）：

json
{
  "code": 403,
  "message": "无权修改工单状态",
  "data": null
}
2.5 删除工单
URL：DELETE /api/ticket/{id}

是否需要认证：是

说明：普通用户只能删自己的工单，管理员能删任意工单。

路径参数：

参数	类型	说明
id	Long	工单 ID
成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": null
}
三、AI 模块
3.1 AI 对话（普通）
URL：GET /ai/chat

是否需要认证：否

查询参数：

参数	类型	必填	说明
message	String	是	用户消息
sessionId	String	否	会话 ID，默认 "default"，用于区分不同用户的对话记忆
成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": "你好！有什么可以帮你的吗？"
}
3.2 AI 对话（SSE 流式）
URL：GET /ai/stream

是否需要认证：否

响应类型：text/event-stream（SSE）

查询参数：同 /ai/chat

响应示例（SSE 格式）：

text
data:你

data:好

data:！
前端接收方式：使用 fetch + ReadableStream 手动解析 SSE。

3.3 工单智能分析
URL：GET /ai/analyze

是否需要认证：否

查询参数：

参数	类型	必填	说明
message	String	是	待分析的问题描述
成功响应：

json
{
  "summary": "系统登录失败",
  "category": "技术故障",
  "priority": "高"
}
四、知识库模块
4.1 上传文档
URL：POST /api/kb/upload

是否需要认证：是

Content-Type：multipart/form-data

请求参数：

参数	类型	必填	说明
file	File	是	待上传的文本文件（.txt）
成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": null
}
说明：上传后，后端会将文档切片、向量化，存入 Redis Stack 向量数据库。

4.2 知识库问答
URL：POST /api/kb/ask

是否需要认证：是

请求体：

参数	类型	必填	说明
question	String	是	用户问题
请求示例：

json
{
  "question": "年假有多少天？"
}
成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": "根据文档片段2，入职满 1 年有 5 天年假，满 3 年有 10 天年假，满 5 年有 15 天年假。"
}
说明：AI 会先检索知识库中最相关的文档片段，然后基于片段生成回答，不会编造知识库之外的内容。

4.3 清空知识库
URL：DELETE /api/kb/clear

是否需要认证：是

成功响应：

json
{
  "code": 200,
  "message": "success",
  "data": null
}
五、错误码总览
code	message 示例	触发场景
200	success	请求成功
400	用户名不能为空	参数校验失败
400	用户名长度必须在3-20位之间	参数校验失败
401	未登录，请先登录	未携带 Token
401	登录已过期，请重新登录	Token 无效或过期
403	无权访问该工单	越权访问
403	无权修改工单状态	普通用户尝试修改状态
500	用户名已存在	业务异常
500	用户名或密码错误	业务异常
500	工单不存在	业务异常
500	系统异常，请稍后重试	未捕获的系统异常
六、前端调用示例（Axios）
项目已经封装了 Axios 请求工具 src/utils/request.js，自动处理：

请求时自动添加 Authorization 请求头

响应时统一处理错误码

401 自动跳转登录页

调用示例：

javascript
import request from '@/utils/request'

// 登录
const res = await request.post('/api/user/login', {
  username: 'alice',
  password: '123456'
})
console.log(res.data.token)

// 创建工单
const ticket = await request.post('/api/ticket', {
  title: '打印机坏了',
  content: '办公室的打印机一直报错...'
})

// 上传知识库文档
const formData = new FormData()
formData.append('file', file)
await request.post('/api/kb/upload', formData, {
  headers: { 'Content-Type': 'multipart/form-data' }
})
七、SSE 流式接口的前端接收方式
因为 Axios 不支持流式接收，/ai/stream 需要用原生 fetch + ReadableStream：

javascript
const url = `/ai/stream?message=${encodeURIComponent(text)}&sessionId=${sessionId}`
const response = await fetch(url)
const reader = response.body.getReader()
const decoder = new TextDecoder('utf-8')
let buffer = ''

while (true) {
  const { done, value } = await reader.read()
  if (done) break

  buffer += decoder.decode(value, { stream: true })
  const lines = buffer.split('\n')
  buffer = lines.pop()

  for (const line of lines) {
    if (line.startsWith('data:')) {
      const content = line.substring(5)
      // 追加到消息里，实时更新界面
    }
  }
}
text

---

## 操作步骤

1. 打开 GitHub 仓库：https://github.com/Bulut3900/ai-ticket-system
2. 点击 **"Add file"** → **"Create new file"**
3. 文件名输入：`API.md`
4. 把上面 markdown 代码块里**所有内容**粘贴进去（**不包括最外层反引号**）
5. 页面底部 Commit message 填：`docs: 添加 API 文档`
6. 点击 **Commit new file**

---

## 补充：README 里加个链接指向 API.md

打开 README.md，找到 `## 📚 API 文档` 这一节，把它**改成**：

```markdown
## 📚 API 文档

详细的接口文档请查看 [API.md](./API.md)。
