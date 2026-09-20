<template>
  <div class="page">
    <!-- 顶部导航栏 -->
    <div class="header">
      <div class="title">智能工单管理系统</div>
      <div class="actions">
        <span class="welcome">欢迎，{{ userInfo.nickname || userInfo.username }}</span>
        <el-button type="primary" link @click="goChat">AI 助手</el-button>
        <el-button type="primary" link @click="goKb">知识库</el-button>
        <el-button type="danger" link @click="logout">退出登录</el-button>
      </div>
    </div>

    <!-- 操作区 -->
    <div class="toolbar">
      <el-button type="primary" @click="openCreateDialog">+ 新建工单</el-button>
      <el-button @click="loadTickets">刷新</el-button>
    </div>

    <!-- 工单列表 -->
    <el-table :data="tickets" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="summary" label="AI 摘要" min-width="200" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="120">
        <template #default="{ row }">
          <el-tag>{{ row.category || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="{ row }">
          <el-tag :type="priorityType(row.priority)">{{ row.priority || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
          <el-button type="danger" link @click="deleteTicket(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新建工单对话框 -->
    <el-dialog v-model="createDialogVisible" title="新建工单" width="600px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" placeholder="简要描述问题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="createForm.content"
            type="textarea"
            :rows="6"
            placeholder="详细描述问题，AI 会自动分析并归类"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">提交</el-button>
      </template>
    </el-dialog>

    <!-- 工单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="工单详情" width="600px">
      <el-descriptions :column="1" border v-if="currentTicket">
        <el-descriptions-item label="ID">{{ currentTicket.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ currentTicket.title }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ currentTicket.content }}</el-descriptions-item>
        <el-descriptions-item label="AI 摘要">{{ currentTicket.summary || '-' }}</el-descriptions-item>
        <el-descriptions-item label="AI 分类">{{ currentTicket.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="AI 优先级">{{ currentTicket.priority || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(currentTicket.status) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTicket.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()

const goKb = () => {
  router.push('/kb')
}
// 用户信息
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

// 工单列表
const tickets = ref([])
const loading = ref(false)

// 新建工单
const createDialogVisible = ref(false)
const createFormRef = ref(null)
const submitting = ref(false)
const createForm = reactive({
  title: '',
  content: ''
})
const createRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

// 详情
const detailDialogVisible = ref(false)
const currentTicket = ref(null)

// =============== 页面加载 ===============
onMounted(() => {
  loadTickets()
})

// =============== 加载工单列表 ===============
const loadTickets = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/ticket')
    tickets.value = res.data
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    loading.value = false
  }
}

// =============== 打开新建对话框 ===============
const openCreateDialog = () => {
  createForm.title = ''
  createForm.content = ''
  createDialogVisible.value = true
}

// =============== 提交新建工单 ===============
const submitCreate = async () => {
  await createFormRef.value.validate()

  submitting.value = true
  try {
    await request.post('/api/ticket', createForm)
    ElMessage.success('工单创建成功，AI 已自动分析')
    createDialogVisible.value = false
    loadTickets()
  } catch (e) {
    // 错误已由拦截器处理
  } finally {
    submitting.value = false
  }
}

// =============== 查看详情 ===============
const viewDetail = (row) => {
  currentTicket.value = row
  detailDialogVisible.value = true
}

// =============== 删除工单 ===============
const deleteTicket = (row) => {
  ElMessageBox.confirm('确定删除该工单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await request.delete(`/api/ticket/${row.id}`)
    ElMessage.success('删除成功')
    loadTickets()
  }).catch(() => {})
}

// =============== 跳转 AI 对话 ===============
const goChat = () => {
  router.push('/chat')
}

// =============== 退出登录 ===============
const logout = () => {
  ElMessageBox.confirm('确定退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    router.push('/login')
  }).catch(() => {})
}

// =============== 辅助方法 ===============
const statusText = (status) => {
  const map = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已解决',
    CLOSED: '已关闭'
  }
  return map[status] || status
}

const statusType = (status) => {
  const map = {
    PENDING: 'warning',
    PROCESSING: 'primary',
    RESOLVED: 'success',
    CLOSED: 'info'
  }
  return map[status] || 'info'
}

const priorityType = (priority) => {
  const map = {
    高: 'danger',
    中: 'warning',
    低: 'success'
  }
  return map[priority] || 'info'
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
.title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
}
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.welcome {
  color: #666;
  font-size: 14px;
} 
.toolbar {
  background: #fff;
  padding: 16px 24px;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}
</style>