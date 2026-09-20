<template>
  <div class="chat-page">
    <!-- 顶部 -->
    <div class="header">
      <el-button type="primary" link @click="goBack">← 返回工单</el-button>
      <div class="title">AI 智能助手</div>
      <el-button type="danger" link @click="clearChat">清空对话</el-button>
    </div>

    <!-- 消息列表 -->
    <div class="messages" ref="messagesRef">
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role === 'user' ? 'user-msg' : 'ai-msg']"
      >
        <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
        <div class="bubble">{{ msg.content }}</div>
      </div>

      <div v-if="loading" class="message ai-msg">
        <div class="avatar">AI</div>
        <div class="bubble typing">思考中...</div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="input-area">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="输入消息，按 Enter 发送（Shift + Enter 换行）"
        @keydown.enter.exact.prevent="sendMessage"
        resize="none"
      />
      <el-button
        type="primary"
        @click="sendMessage"
        :loading="loading"
        :disabled="!inputText.trim()"
      >
        发送
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()

const inputText = ref('')
const messages = ref([])
const loading = ref(false)
const messagesRef = ref(null)

// 每个用户一个独立的 sessionId（用来区分对话记忆）
const sessionId = ref('')

onMounted(() => {
  // 用用户 ID 作为 sessionId，保证同一个用户刷新页面后还能继续之前的对话
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  sessionId.value = 'user_' + (userInfo.userId || 'anonymous')

  // 欢迎语
  messages.value.push({
    role: 'ai',
    content: '你好，我是 AI 智能助手，有什么可以帮你的吗？'
  })
})

// ============== 发送消息 ==============
const sendMessage = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 1. 把用户消息加入列表
  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  loading.value = true

  // 2. 滚动到底部
  await scrollToBottom()

  // 3. 准备接收 AI 回复
  const aiMessage = { role: 'ai', content: '' }
  messages.value.push(aiMessage)

  try {
    // 4. 用 fetch 发起 SSE 请求
    const url = `/ai/stream?message=${encodeURIComponent(text)}&sessionId=${sessionId.value}`

    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Accept': 'text/event-stream'
      }
    })

    if (!response.ok) {
      throw new Error('请求失败: ' + response.status)
    }

    // 5. 读取流
    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

        while (true) {
      const { done, value } = await reader.read()
      if (done) break

      // 解码字节流
      buffer += decoder.decode(value, { stream: true })

      // 按行分割（SSE 每行是一条消息）
      const lines = buffer.split('\n')
      buffer = lines.pop() // 最后一段可能不完整，留到下一次处理

      for (const line of lines) {
        // 跳过空行
        if (!line.trim()) continue
        // 只处理 data: 开头的行
        if (line.startsWith('data:')) {
          const content = line.substring(5) // 去掉 "data:"
          // 如果内容前后有空行，去掉
          aiMessage.content += content
        }
      }
      
      // 实时更新界面
      messages.value = [...messages.value]
      await scrollToBottom()
    }
  } catch (e) {
    ElMessage.error('AI 响应失败：' + e.message)
    // 如果 AI 消息为空，就删掉这一条
    if (!aiMessage.content) {
      messages.value.pop()
    }
  } finally {
    loading.value = false
    await scrollToBottom()
  }
}

// ============== 滚动到底部 ==============
const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// ============== 清空对话 ==============
const clearChat = () => {
  messages.value = [
    { role: 'ai', content: '你好，我是 AI 智能助手，有什么可以帮你的吗？' }
  ]
}

// ============== 返回工单页 ==============
const goBack = () => {
  router.push('/tickets')
}
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.message {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.user-msg {
  flex-direction: row-reverse;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: #fff;
  flex-shrink: 0;
}

.ai-msg .avatar {
  background: #667eea;
  margin-right: 12px;
}

.user-msg .avatar {
  background: #52c41a;
  margin-left: 12px;
}

.bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 8px;
  line-height: 1.6;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.ai-msg .bubble {
  background: #fff;
  color: #333;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.user-msg .bubble {
  background: #667eea;
  color: #fff;
}

.typing {
  color: #999;
  font-style: italic;
}

.input-area {
  display: flex;
  gap: 12px;
  padding: 16px 24px;
  background: #fff;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);
  align-items: flex-end;
}

.input-area .el-input {
  flex: 1;
}

.input-area .el-button {
  height: 54px;
  width: 100px;
}
</style>