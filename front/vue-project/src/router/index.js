import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/components/MainLayout.vue'
import Home from '@/views/Home.vue'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import Profile from '@/views/Profile.vue'
import ProductList from '@/views/ProductList.vue'
import ProductDetail from '@/views/ProductDetail.vue'
import ProductForm from '@/views/ProductForm.vue'
import MyProducts from '@/views/MyProducts.vue'
import Messages from '@/views/Messages.vue'
import Cart from '@/views/Cart.vue'
import OrderList from '@/views/OrderList.vue'
import OrderDetail from '@/views/OrderDetail.vue'
import Payment from '@/views/Payment.vue'
import PaymentHistory from '@/views/PaymentHistory.vue'
import PaymentDemo from '@/views/PaymentDemo.vue'
import CreditInfo from '@/views/Credit/CreditInfo.vue'
import ArbitrationApply from '@/views/Arbitration/ArbitrationApply.vue'
import ArbitrationList from '@/views/Arbitration/ArbitrationList.vue'
import ArbitrationDetail from '@/views/Arbitration/ArbitrationDetail.vue'
import ArbitrationAdmin from '@/views/Arbitration/ArbitrationAdmin.vue'
import SimpleTest from '@/views/SimpleTest.vue'
import UserProfile from '@/views/UserProfile.vue'

// 管理系统相关导入
import AdminLayout from '@/layouts/AdminLayout.vue'
import AdminLogin from '@/views/Admin/Login.vue'
import AdminDashboard from '@/views/Admin/Dashboard.vue'

import DebugPage from '@/views/DebugPage.vue'
import RouteTest from '@/views/RouteTest.vue'

import LoginDebug from '@/views/LoginDebug.vue'

// 路由配置
const routes = [
  // 登录调试路由
  {
    path: '/login-debug',
    name: 'LoginDebug',
    component: LoginDebug,
    meta: {
      title: '登录调试'
    }
  },
  // 路由测试
  {
    path: '/route-test',
    name: 'RouteTest',
    component: RouteTest,
    meta: {
      title: '路由测试'
    }
  },
  // 调试路由
  {
    path: '/debug',
    name: 'DebugPage',
    component: DebugPage,
    meta: {
      title: 'Vue应用调试'
    }
  },
  // 测试路由
  {
    path: '/test',
    name: 'SimpleTest',
    component: SimpleTest,
    meta: {
      title: '测试页面'
    }
  },
  // 用户端独立页面 - 不需要导航栏的页面
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '用户登录',
      requiresGuest: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: '用户注册',
      requiresGuest: true
    }
  },
  // 用户端主布局路由 - 包含导航栏的页面
  {
    path: '/',
    component: MainLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: Home,
        meta: {
          title: '首页'
        }
      },
      // 商品相关路由
      {
        path: 'products',
        name: 'ProductList',
        component: ProductList,
        meta: {
          title: '商品列表'
        }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: ProductDetail,
        meta: {
          title: '商品详情'
        }
      },
      {
        path: 'product/publish',
        name: 'ProductPublish',
        component: ProductForm,
        meta: {
          title: '发布商品',
          requiresAuth: true
        }
      },
      {
        path: 'product/edit/:id',
        name: 'ProductEdit',
        component: ProductForm,
        meta: {
          title: '编辑商品',
          requiresAuth: true
        }
      },
      {
        path: 'my-products',
        name: 'MyProducts',
        component: MyProducts,
        meta: {
          title: '我的商品',
          requiresAuth: true
        }
      },
      // 用户相关路由
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: {
          title: '个人中心',
          requiresAuth: true
        }
      },
      {
        path: 'profile/:id',
        name: 'UserProfile',
        component: UserProfile,
        meta: {
          title: '用户资料',
          requiresAuth: true
        }
      },
      // 消息相关路由
      {
        path: 'messages',
        name: 'Messages',
        component: Messages,
        meta: {
          title: '消息',
          requiresAuth: true
        }
      },
      // 交易相关路由
      {
        path: 'cart',
        name: 'Cart',
        component: Cart,
        meta: {
          title: '购物车',
          requiresAuth: true
        }
      },
      {
        path: 'orders',
        name: 'OrderList',
        component: OrderList,
        meta: {
          title: '我的订单',
          requiresAuth: true
        }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: OrderDetail,
        meta: {
          title: '订单详情',
          requiresAuth: true
        }
      },
      // 支付相关路由
      {
        path: 'payment/:id',
        name: 'Payment',
        component: Payment,
        meta: {
          title: '订单支付',
          requiresAuth: true
        }
      },
      {
        path: 'payment-history',
        name: 'PaymentHistory',
        component: PaymentHistory,
        meta: {
          title: '支付记录',
          requiresAuth: true
        }
      },
      {
        path: 'payment-demo',
        name: 'PaymentDemo',
        component: PaymentDemo,
        meta: {
          title: '支付系统演示'
        }
      },
      // 信用评价相关路由
      {
        path: 'credit',
        name: 'CreditInfo',
        component: CreditInfo,
        meta: {
          title: '我的信用',
          requiresAuth: true
        }
      },
      // 仲裁相关路由
      {
        path: 'arbitration/apply',
        name: 'ArbitrationApply',
        component: ArbitrationApply,
        meta: {
          title: '申请仲裁',
          requiresAuth: true
        }
      },
      {
        path: 'arbitration/list',
        name: 'ArbitrationList',
        component: ArbitrationList,
        meta: {
          title: '我的仲裁',
          requiresAuth: true
        }
      },
      {
        path: 'arbitration/detail/:id',
        name: 'ArbitrationDetail',
        component: ArbitrationDetail,
        meta: {
          title: '仲裁详情',
          requiresAuth: true
        }
      }
    ]
  },
  // 用户端独立页面 - 不需要导航栏的页面
  {
    path: '/user/login',
    name: 'LoginOld',
    redirect: '/login'
  },
  {
    path: '/user/register',
    name: 'RegisterOld',
    redirect: '/register'
  },
  // 管理端登录
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: AdminLogin,
    meta: {
      title: '管理员登录'
    }
  },
  // 管理端布局路由
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      requiresAuth: true,
      requiresAdmin: true
    },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      // 仪表盘
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: AdminDashboard,
        meta: {
          title: '工作台'
        }
      },
      // 仲裁管理路由
      {
        path: 'arbitration/pending',
        name: 'ArbitrationPending',
        component: () => import('@/views/Admin/Arbitration/PendingCases.vue'),
        meta: {
          title: '待处理仲裁'
        }
      },
      {
        path: 'arbitration/processing',
        name: 'ArbitrationProcessing',
        component: () => import('@/views/Admin/Arbitration/ProcessingCases.vue'),
        meta: {
          title: '处理中仲裁'
        }
      },
      {
        path: 'arbitration/detail/:id',
        name: 'AdminArbitrationDetail',
        component: () => import('@/views/Admin/Arbitration/CaseDetail.vue'),
        meta: {
          title: '仲裁详情'
        }
      },
      {
        path: 'arbitration/completed',
        name: 'ArbitrationCompleted',
        component: () => import('@/views/Admin/Arbitration/CompletedCases.vue'),
        meta: {
          title: '已完成仲裁'
        }
      },
      // 数据统计路由
      {
        path: 'statistics/overview',
        name: 'StatisticsOverview',
        component: () => import('@/views/Admin/Statistics/Overview.vue'),
        meta: {
          title: '数据概览'
        }
      },
      {
        path: 'statistics/arbitration',
        name: 'StatisticsArbitration',
        component: () => import('@/views/Admin/Statistics/ArbitrationStats.vue'),
        meta: {
          title: '仲裁统计'
        }
      },
      {
        path: 'statistics/trend',
        name: 'StatisticsTrend',
        component: () => import('@/views/Admin/Statistics/TrendAnalysis.vue'),
        meta: {
          title: '趋势分析'
        }
      },
      {
        path: 'statistics/reports',
        name: 'StatisticsReports',
        component: () => import('@/views/Admin/Statistics/Reports.vue'),
        meta: {
          title: '报表导出'
        }
      }
    ]
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

  // 调试路由，直接通过
  if (to.path === '/debug') {
    next()
    return
  }

  // 管理端路由处理
  if (to.path.startsWith('/admin')) {
    // 管理员登录页面特殊处理
    if (to.path === '/admin/login') {
      const adminToken = localStorage.getItem('adminToken')
      if (adminToken) {
        next('/admin/dashboard')
        return
      }
      next()
      return
    }

    // 检查管理员登录状态
    const adminToken = localStorage.getItem('adminToken')
    if (!adminToken) {
      console.log('未检测到管理员token，跳转到登录页')
      next('/admin/login')
      return
    }

    // 检查角色权限
    if (to.meta.requiresRole) {
      const userRole = localStorage.getItem('userRole')
      if (userRole !== to.meta.requiresRole) {
        alert('权限不足')
        next('/admin/dashboard')
        return
      }
    }

    next()
    return
  }

  // 普通用户路由处理
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
