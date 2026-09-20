import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: () => import('../views/Login.vue') },
    { path: '/register', component: () => import('../views/Register.vue') },
    { path: '/tickets', component: () => import('../views/Tickets.vue') },
    { path: '/chat', component: () => import('../views/Chat.vue') },
    { path: '/kb', component: () => import('../views/Knowledge.vue') }   // ← 新增
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫：未登录的用户只能访问登录/注册页
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path === '/login' || to.path === '/register') {
        next()
    } else if (!token) {
        next('/login')
    } else {
        next()
    }
})

export default router