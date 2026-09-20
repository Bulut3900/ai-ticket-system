import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 axios 实例
const request = axios.create({
    baseURL: '',   // 空字符串，用相对路径  // 后端地址
    timeout: 30000                     // 30 秒超时（AI 调用可能需要时间）
})

// 请求拦截器：自动带上 Token
request.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器：统一处理返回结果
request.interceptors.response.use(
    response => {
        const res = response.data
        // 后端返回 code=200 表示成功
        if (res.code === 200) {
            return res
        }
        // 401 未登录 → 跳转到登录页
        if (res.code === 401) {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            router.push('/login')
            return Promise.reject(new Error(res.message))
        }
        // 其他错误，弹出提示
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
    },
    error => {
        ElMessage.error('网络异常，请稍后重试')
        return Promise.reject(error)
    }
)

export default request