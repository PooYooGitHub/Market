import { createRouter, createWebHistory } from 'vue-router'
import Welcome from '@/views/Welcome.vue'
import SimpleTest from '@/views/SimpleTest.vue'
import SimpleHome from '@/views/SimpleHome.vue'

// 测试单个组件
const routes = [
  // 测试路由
  {
    path: '/test',
    name: 'SimpleTest',
    component: SimpleTest,
    meta: {
      title: '测试页面'
    }
  },
  // 根路径 - 显示欢迎选择页面
  {
    path: '/',
    name: 'Welcome',
    component: Welcome,
    meta: {
      title: '欢迎访问校园跳蚤市场'
    }
  },
  // 测试Home组件
  {
    path: '/home',
    name: 'SimpleHome',
    component: SimpleHome,
    meta: {
      title: '首页测试'
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 简化的路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园跳蚤市场` : '校园跳蚤市场'
  next()
})

export default router