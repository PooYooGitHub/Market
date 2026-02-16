import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Profile from '@/views/Profile.vue'
import ProductList from '@/views/ProductList.vue'
import ProductDetail from '@/views/ProductDetail.vue'
import ProductForm from '@/views/ProductForm.vue'
import MyProducts from '@/views/MyProducts.vue'
import Messages from '@/views/Messages.vue'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: {
      title: '首页'
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '登录',
      requiresGuest: true // 已登录用户访问会跳转到首页
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: '注册',
      requiresGuest: true // 已登录用户访问会跳转到首页
    }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: {
      title: '个人中心',
      requiresAuth: true // 需要登录才能访问
    }
  },
  // 商品相关路由
  {
    path: '/products',
    name: 'ProductList',
    component: ProductList,
    meta: {
      title: '商品列表'
    }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: ProductDetail,
    meta: {
      title: '商品详情'
    }
  },
  {
    path: '/product/publish',
    name: 'ProductPublish',
    component: ProductForm,
    meta: {
      title: '发布商品',
      requiresAuth: true
    }
  },
  {
    path: '/product/edit/:id',
    name: 'ProductEdit',
    component: ProductForm,
    meta: {
      title: '编辑商品',
      requiresAuth: true
    }
  },
  {
    path: '/my-products',
    name: 'MyProducts',
    component: MyProducts,
    meta: {
      title: '我的商品',
      requiresAuth: true
    }
  },
  // 消息相关路由
  {
    path: '/messages',
    name: 'Messages',
    component: Messages,
    meta: {
      title: '消息',
      requiresAuth: true
    }
  }
]

// 创建路由实例
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 校园跳蚤市场` : '校园跳蚤市场'
  
  // 获取 token
  const token = localStorage.getItem('token')
  const isLoggedIn = !!token
  
  // 如果页面需要登录
  if (to.meta.requiresAuth && !isLoggedIn) {
    alert('请先登录')
    next('/login')
    return
  }
  
  // 如果页面需要未登录状态（如登录页、注册页）
  if (to.meta.requiresGuest && isLoggedIn) {
    next('/')
    return
  }
  
  next()
})

export default router
