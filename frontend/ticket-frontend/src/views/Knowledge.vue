<template>
  <div class="kb-page">
    <!-- 顶部 -->
    <div class="header">
      <el-button type="primary" link @click="goBack">← 返回工单</el-button>
      <div class="title">知识库</div>
      <div style="width: 80px"></div>
    </div>

    <div class="content">
      <!-- 上传区 -->
      <el-card class="upload-card">
        <template #header>
          <span>上传文档</span>
        </template>
        <el-upload
          :show-file-list="false"
          :before-upload="handleUpload"
          accept=".txt"
          drag
        >
          <div class="upload-area">
            <div class="upload-icon">📄</div>
            <div>点击或拖拽 .txt 文件到此处上传</div>
          </div>
        </el-upload>
        <div v-if="uploading" class="uploading">
          <el-icon class="is-loading"><Loading /></el-icon>
          正在上传和向量化，请稍候...
        </div>
      </el-card>

      <!-- 提问区 -->
      <el-card class="ask-card">
        <template #header>
          <span>知识库问答</span>
        </template>
        <div class="ask-input">
          <el-input
            v-model="question"
            placeholder="基于知识库提问，比如：年假有多少天？"
            @keydown.enter.exact.prevent="askQuestion"
          />
          <el-button
            type="primary"
            @click="askQuestion"
            :loading="asking"
            :disabled="!question.trim()"
          >
            提问
          </el-button>
        </div>

        <!-- 对话历史 -->
        <div class="history" v-if="history.length > 0">
          <div v-for="(item, index) in history" :key="index" class="qa-item">
            <div class="question-row">
              <span class="label">问：</span>
              <span>{{ item.question }}</span>
            </div>
            <div class="answer-row">
              <span class="label">答：</span>
              <span class="answer">{{ item.answer }}</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import request from '../utils/request'

const router = useRouter()
const uploading = ref(false)
const asking = ref(false)
const question = ref('')
const history = ref([])

// ================= 上传文档 =================
const handleUpload = async (file) => {
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)

    await request.post('/api/kb/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success('文档上传成功，AI 已完成向量化')
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    uploading.value = false
  }
  return false   // 阻止 el-upload 的默认行为
}

// ================= 提问 =================
const askQuestion = async () => {
  const q = question.value.trim()
  if (!q) return

  asking.value = true
  try {
    const res = await request.post('/api/kb/ask', { question: q })
    history.value.unshift({
      question: q,
      answer: res.data
    })
    question.value = ''
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    asking.value = false
  }
}

// ================= 返回 =================
const goBack = () => {
  router.push('/tickets')
}
</script>

<style scoped>
.kb-page {
  min-height: 100vh;
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

.content {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.upload-area {
  padding: 30px;
  text-align: center;
  color: #666;
}

.upload-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.uploading {
  margin-top: 12px;
  color: #667eea;
  font-size: 14px;
  text-align: center;
}

.ask-input {
  display: flex;
  gap: 12px;
}

.history {
  margin-top: 20px;
  border-top: 1px solid #eee;
  padding-top: 16px;
}

.qa-item {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 8px;
}

.question-row {
  color: #333;
  margin-bottom: 10px;
  font-weight: 500;
}

.answer-row {
  color: #555;
  line-height: 1.7;
}

.label {
  color: #667eea;
  font-weight: bold;
  margin-right: 6px;
}

.answer {
  white-space: pre-wrap;
}
</style>