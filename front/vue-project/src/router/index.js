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
import DisputeApply from '@/views/Dispute/DisputeApply.vue'
import DisputeDetail from '@/views/Dispute/DisputeDetail.vue'
import SellerDisputeDetail from '@/views/Dispute/SellerDisputeDetail.vue'
import DisputeCenter from '@/views/Dispute/DisputeCenter.vue'
import SimpleTest from '@/views/SimpleTest.vue'
import UserProfile from '@/views/UserProfile.vue'
import AddressManage from '@/views/AddressManage.vue'

// 绠＄悊绯荤粺鐩稿叧瀵煎叆
import AdminLayout from '@/layouts/AdminLayout.vue'
import AdminLogin from '@/views/Admin/Login.vue'

import DebugPage from '@/views/DebugPage.vue'
import RouteTest from '@/views/RouteTest.vue'

import LoginDebug from '@/views/LoginDebug.vue'

// 璺敱閰嶇疆
const routes = [
  // 鐧诲綍璋冭瘯璺敱
  {
    path: '/login-debug',
    name: 'LoginDebug',
    component: LoginDebug,
    meta: {
      title: '登录调试'
    }
  },
  // 璺敱娴嬭瘯
  {
    path: '/route-test',
    name: 'RouteTest',
    component: RouteTest,
    meta: {
      title: '路由测试'
    }
  },
  // 璋冭瘯璺敱
  {
    path: '/debug',
    name: 'DebugPage',
    component: DebugPage,
    meta: {
      title: 'Vue应用调试'
    }
  },
  // 娴嬭瘯璺敱
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
  // 鐢ㄦ埛绔富甯冨眬璺敱 - 鍖呭惈瀵艰埅鏍忕殑椤甸潰
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
      // 鍟嗗搧鐩稿叧璺敱
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
      // 鐢ㄦ埛鐩稿叧璺敱
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
      {
        path: 'addresses',
        name: 'AddressManage',
        component: AddressManage,
        meta: {
          title: '收货地址',
          requiresAuth: true
        }
      },
      // 娑堟伅鐩稿叧璺敱
      {
        path: 'messages',
        name: 'Messages',
        component: Messages,
        meta: {
          title: '消息',
          requiresAuth: true
        }
      },
      // 浜ゆ槗鐩稿叧璺敱
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
      // 鏀粯鐩稿叧璺敱
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
      // 淇＄敤璇勪环鐩稿叧璺敱
      {
        path: 'credit',
        name: 'CreditInfo',
        component: CreditInfo,
        meta: {
          title: '我的信用',
          requiresAuth: true
        }
      },
      // 浠茶鐩稿叧璺敱
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
      },
      {
        path: 'dispute/center',
        name: 'DisputeCenter',
        component: DisputeCenter,
        meta: {
          title: '争议中心',
          requiresAuth: true
        }
      },
      {
        path: 'dispute/apply',
        name: 'DisputeApply',
        component: DisputeApply,
        meta: {
          title: '发起争议',
          requiresAuth: true
        }
      },
      {
        path: 'dispute/my',
        name: 'DisputeMyList',
        redirect: (to) => ({
          path: '/dispute/center',
          query: {
            ...to.query,
            tab: 'buyer'
          }
        })
      },
      {
        path: 'dispute/detail/:id',
        name: 'DisputeDetail',
        component: DisputeDetail,
        meta: {
          title: '争议详情',
          requiresAuth: true
        }
      },
      {
        path: 'dispute/seller/pending',
        name: 'SellerDisputePending',
        redirect: (to) => ({
          path: '/dispute/center',
          query: {
            ...to.query,
            tab: 'seller'
          }
        })
      },
      {
        path: 'dispute/seller/detail/:id',
        name: 'SellerDisputeDetail',
        component: SellerDisputeDetail,
        meta: {
          title: '卖家响应争议',
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
  // 绠＄悊绔竷灞€璺敱
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
        redirect: '/admin/arbitration/pending'
      },
      // 浠茶绠＄悊璺敱
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
        component: () => import('@/views/Admin/Arbitration/CaseWorkbench.vue'),
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
      }
    ]
  }
]

// 鍒涘缓璺敱瀹炰緥
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 璺敱瀹堝崼
router.beforeEach((to, from, next) => {
  // 璁剧疆椤甸潰鏍囬
  document.title = to.meta.title ? `${to.meta.title} - 校园跳蚤市场` : '校园跳蚤市场'

  // 璋冭瘯璺敱锛岀洿鎺ラ€氳繃
  if (to.path === '/debug') {
    next()
    return
  }

  // 管理端路由处理
  if (to.path.startsWith('/admin')) {
    // 管理员登录页特殊处理
    if (to.path === '/admin/login') {
      const adminToken = localStorage.getItem('adminToken')
      if (adminToken) {
        next('/admin/arbitration/pending')
        return
      }
      next()
      return
    }

    // 检查管理员登录状态
    const adminToken = localStorage.getItem('adminToken')
    if (!adminToken) {
      console.log('未检测到管理员 token，跳转到登录页')
      next('/admin/login')
      return
    }

    // 检查角色权限
    if (to.meta.requiresRole) {
      const userRole = localStorage.getItem('userRole')
      if (userRole !== to.meta.requiresRole) {
        alert('权限不足')
        next('/admin/arbitration/pending')
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

  // 如果页面要求未登录状态（如登录页、注册页）
  if (to.meta.requiresGuest && isLoggedIn) {
    next('/')
    return
  }

  next()
})

export default router

