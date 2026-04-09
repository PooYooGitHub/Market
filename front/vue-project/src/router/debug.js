import { createRouter, createWebHistory } from 'vue-router'
import SimpleTest from '@/views/SimpleTest.vue'

// 临时简化的路由配置用于调试
const routes = [
  {
    path: '/',
    name: 'SimpleTest',
    component: SimpleTest,
    meta: {
      title: '测试页面'
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router