# 争议协商流程完整改动代码（2026-04-14）

## 文件清单
- front/vue-project/src/api/arbitration.js
- front/vue-project/src/components/MainLayout.vue
- front/vue-project/src/router/index.js
- front/vue-project/src/views/Admin/Arbitration/CaseWorkbench.vue
- front/vue-project/src/views/Admin/Arbitration/components/CaseSummaryCard.vue
- front/vue-project/src/views/OrderDetail.vue
- front/vue-project/src/views/Dispute/DisputeApply.vue
- front/vue-project/src/views/Dispute/DisputeMyList.vue
- front/vue-project/src/views/Dispute/DisputeDetail.vue
- front/vue-project/src/views/Dispute/SellerDisputePending.vue
- front/vue-project/src/views/Dispute/SellerDisputeDetail.vue
- market-gateway/src/main/resources/bootstrap.yml
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/entity/ArbitrationEntity.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/service/impl/ArbitrationServiceImpl.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/AdminArbitrationDetailVO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/controller/DisputeController.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/dto/BuyerConfirmProposalDTO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/dto/DisputeCreateDTO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/dto/DisputeEscalateDTO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/dto/SellerDisputeResponseDTO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/entity/DisputeEvidenceEntity.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/entity/DisputeNegotiationLogEntity.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/entity/DisputeRequestEntity.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/enums/DisputeActorRoleEnum.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/enums/DisputeNegotiationActionEnum.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/enums/DisputeStatusEnum.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/enums/SellerResponseTypeEnum.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/mapper/DisputeEvidenceMapper.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/mapper/DisputeNegotiationLogMapper.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/mapper/DisputeRequestMapper.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/service/IDisputeService.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/service/impl/DisputeServiceImpl.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeChatSummaryVO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeDetailVO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeEvidenceVO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeListItemVO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeNegotiationLogVO.java
- market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/SellerProposalVO.java
- market-service/market-service-arbitration/src/main/resources/mapper/DisputeRequestMapper.xml
- market-service/market-service-arbitration/src/main/resources/sql/market_dispute.sql

## front/vue-project/src/api/arbitration.js
```js
import request from '@/utils/request'

export const arbitrationApi = {
  createArbitration(data) {
    return request({
      url: '/api/arbitration/submit',
      method: 'post',
      data
    })
  },

  updateArbitration(id, data) {
    return request({
      url: `/api/arbitration/update/${id}`,
      method: 'put',
      data
    })
  },

  getUserArbitrationList(params) {
    return request({
      url: '/api/arbitration/my',
      method: 'get',
      params
    })
  },

  getArbitrationDetail(id) {
    return request({
      url: `/api/arbitration/detail/${id}`,
      method: 'get'
    })
  },

  getMyArbitrationByOrderId(orderId) {
    return request({
      url: `/api/arbitration/my/order/${orderId}`,
      method: 'get'
    })
  },

  cancelArbitration(id) {
    return request({
      url: `/api/arbitration/cancel/${id}`,
      method: 'post'
    })
  },

  getUserArbitrationStats() {
    return request({
      url: '/api/arbitration/stats/user',
      method: 'get'
    })
  },

  getArbitrationLogs(arbitrationId) {
    return request({
      url: `/api/arbitration/logs/${arbitrationId}`,
      method: 'get'
    })
  },

  getAdminArbitrationList(params) {
    return request({
      url: '/api/arbitration/admin/list',
      method: 'get',
      params
    })
  },

  getAdminArbitrationStats() {
    return request({
      url: '/api/arbitration/admin/stats',
      method: 'get'
    })
  },

  getAdminArbitrationDetail(id) {
    return request({
      url: `/api/arbitration/admin/detail/${id}`,
      method: 'get'
    })
  },

  acceptAdminArbitration(id) {
    return request({
      url: `/api/arbitration/admin/accept/${id}`,
      method: 'post'
    })
  },

  completeAdminArbitration(data) {
    return request({
      url: '/api/arbitration/admin/complete',
      method: 'post',
      data
    })
  },

  rejectAdminArbitration(data) {
    return request({
      url: '/api/arbitration/admin/reject',
      method: 'post',
      data
    })
  },

  requestSupplement(data) {
    return request({
      url: '/api/arbitration/admin/supplement/request',
      method: 'post',
      data
    })
  },

  expireSupplement(requestId) {
    return request({
      url: `/api/arbitration/admin/supplement/expire/${requestId}`,
      method: 'post'
    })
  },

  submitSupplement(data) {
    return request({
      url: '/api/arbitration/supplement/submit',
      method: 'post',
      data
    })
  },

  // 鍏煎鏃ц皟鐢?
  acceptArbitration(id) {
    return this.acceptAdminArbitration(id)
  },

  handleArbitration(data) {
    return this.completeAdminArbitration({
      arbitrationId: data?.arbitrationId ?? data?.id,
      decisionRemark: data?.decisionRemark ?? data?.result
    })
  },

  rejectArbitration(id, reason) {
    return this.rejectAdminArbitration({
      arbitrationId: id,
      rejectReason: reason
    })
  },

  getArbitrationStats() {
    return this.getAdminArbitrationStats()
  },

  createDispute(data) {
    return request({
      url: '/api/dispute/create',
      method: 'post',
      data
    })
  },

  getMyDisputeList(params) {
    return request({
      url: '/api/dispute/my',
      method: 'get',
      params
    })
  },

  getDisputeDetail(id) {
    return request({
      url: `/api/dispute/detail/${id}`,
      method: 'get'
    })
  },

  escalateDispute(data) {
    return request({
      url: '/api/dispute/escalate',
      method: 'post',
      data
    })
  },

  getSellerPendingDisputes(params) {
    return request({
      url: '/api/dispute/seller/pending',
      method: 'get',
      params
    })
  },

  sellerRespondDispute(data) {
    return request({
      url: '/api/dispute/seller/respond',
      method: 'post',
      data
    })
  },

  buyerConfirmProposal(data) {
    return request({
      url: '/api/dispute/buyer/confirm-proposal',
      method: 'post',
      data
    })
  },

  checkDisputeTimeout() {
    return request({
      url: '/api/dispute/system/check-timeout',
      method: 'post'
    })
  }
}

```

## front/vue-project/src/components/MainLayout.vue
```vue
<template>
  <div class="main-layout">
    <!-- 椤堕儴瀵艰埅鏍?-->
    <el-header class="header">
      <div class="header-content">
        <div class="logo-section" @click="$router.push('/')">
          <div class="logo-container">
            <el-icon size="20" color="#667eea">
              <ShoppingBag />
            </el-icon>
            <div class="logo-text">
              璺宠殼<br/>甯傚満
            </div>
          </div>
        </div>

        <el-menu
          mode="horizontal"
          :ellipsis="false"
          class="nav-menu"
          :default-active="$route.path"
          router
        >
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            棣栭〉
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><Goods /></el-icon>
            鍟嗗搧鍒楄〃
          </el-menu-item>

          <template v-if="userStore.isLoggedIn">
            <el-menu-item index="/product/publish">
              <el-icon><Plus /></el-icon>
              鍙戝竷鍟嗗搧
            </el-menu-item>
            <el-menu-item index="/my-products">
              <el-icon><Box /></el-icon>
              鎴戠殑鍟嗗搧
            </el-menu-item>
            <el-menu-item index="/cart">
              <el-icon><ShoppingCart /></el-icon>
              璐墿杞?            </el-menu-item>
            <el-menu-item index="/orders">
              <el-icon><Tickets /></el-icon>
              鎴戠殑璁㈠崟
            </el-menu-item>
            <el-menu-item index="/messages">
              <el-icon><ChatDotRound /></el-icon>
              娑堟伅
            </el-menu-item>
            <el-menu-item index="/credit">
              <el-icon><Medal /></el-icon>
              鎴戠殑淇＄敤
            </el-menu-item>
            <el-menu-item index="/dispute/my">
              <el-icon><DocumentChecked /></el-icon>
              争议协商
            </el-menu-item>
            <el-menu-item index="/dispute/seller/pending">
              <el-icon><DocumentChecked /></el-icon>
              卖家协商
            </el-menu-item>
          </template>
        </el-menu>

        <div class="user-section">
          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click" @command="handleCommand">
              <span class="el-dropdown-link">
                <el-avatar :size="32" :src="userStore.avatar">
                  <el-icon v-if="!userStore.avatar"><User /></el-icon>
                </el-avatar>
                <span class="username">{{ userStore.nickname || userStore.username }}</span>
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    涓汉涓績
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    閫€鍑虹櫥褰?                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button-group>
              <el-button @click="$router.push('/login')">鐧诲綍</el-button>
              <el-button type="primary" @click="$router.push('/register')">娉ㄥ唽</el-button>
            </el-button-group>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 涓诲唴瀹瑰尯 -->
    <el-main class="main-content">
      <router-view />
    </el-main>

    <!-- 椤佃剼 -->
    <el-footer class="footer">
      <div class="footer-content">
        <div class="footer-info">
          <p>&copy; 2026 鏍″洯璺宠殼甯傚満</p>
          <p>鍩轰簬 SpringCloud 寰湇鍔℃灦鏋?| Vue 3 + Element Plus</p>
        </div>
        <div class="footer-links">
          <el-link href="#" underline="never">鍏充簬鎴戜滑</el-link>
          <el-divider direction="vertical" />
          <el-link href="#" underline="never">甯姪涓績</el-link>
          <el-divider direction="vertical" />
          <el-link href="#" underline="never">鑱旂郴鎴戜滑</el-link>
        </div>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useArbitrationStore } from '@/stores/arbitration'
import { MessageBox } from '@/utils/message'
import {
  HomeFilled,
  Goods,
  Plus,
  Box,
  ShoppingCart,
  Tickets,
  ChatDotRound,
  Medal,
  User,
  ArrowDown,
  SwitchButton,
  DocumentChecked,
  ShoppingBag
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const arbitrationStore = useArbitrationStore()

// 处理下拉菜单命令
const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      try {
        await MessageBox.confirm('确定要退出登录吗？', '提示')
        userStore.logout()
        arbitrationStore.reset()
      } catch (error) {
        // 用户取消
      }
      break
  }
}

// 启动用户仲裁状态轮询
onMounted(() => {
  // 延迟启动轮询，确保 token 已经加载完成
  setTimeout(() => {
    if (userStore.isLoggedIn) {
      console.log('启动用户仲裁轮询，token:', userStore.token ? '存在' : '不存在')
      arbitrationStore.startUserPolling({ immediate: true })
    } else {
      console.log('用户未登录，跳过仲裁轮询启动')
    }
  }, 500) // 延迟 500ms 启动
})

onBeforeUnmount(() => {
  arbitrationStore.stopUserPolling()
})
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f5f7ff 0%, #e8f4fd 100%);
}

.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 20px rgba(102, 126, 234, 0.15);
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(102, 126, 234, 0.1);
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.logo-section {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 80px;
}

.logo-section:hover {
  transform: scale(1.02);
}

.logo-container {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.logo-container:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.logo-text {
  font-size: 11px;
  font-weight: 600;
  color: white;
  line-height: 1.1;
  text-align: center;
}

.nav-menu {
  flex: 1;
  margin: 0 20px;
  border-bottom: none;
  max-width: none;
}

/* 鑿滃崟椤规牱寮忎紭鍖?*/
.nav-menu .el-menu-item {
  color: #4a5568;
  font-weight: 500;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 0 4px;
}

.nav-menu .el-menu-item:hover {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}

.nav-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: 600;
}

.nav-menu .el-menu-item .el-icon {
  margin-right: 6px;
  font-size: 16px;
}

.user-section .el-dropdown-link {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #606266;
}

.username {
  font-size: 14px;
  font-weight: 500;
}

.main-content {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 0 20px;
  flex: 1;
}

.footer {
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-top: 1px solid rgba(102, 126, 234, 0.1);
  margin-top: auto;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
}

.footer-info p {
  margin: 0;
  color: #718096;
  font-size: 14px;
  line-height: 1.6;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 鍝嶅簲寮忚璁?*/
@media (max-width: 1024px) {
  .nav-menu {
    margin: 0 15px;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    padding: 12px 20px;
    gap: 12px;
  }

  .nav-menu {
    margin: 0;
    width: 100%;
  }

  .logo-container {
    padding: 4px 8px;
  }

  .logo-text {
    font-size: 10px;
  }

  .footer-content {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .footer-links {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>

```

## front/vue-project/src/router/index.js
```js
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
import DisputeMyList from '@/views/Dispute/DisputeMyList.vue'
import DisputeDetail from '@/views/Dispute/DisputeDetail.vue'
import SellerDisputePending from '@/views/Dispute/SellerDisputePending.vue'
import SellerDisputeDetail from '@/views/Dispute/SellerDisputeDetail.vue'
import SimpleTest from '@/views/SimpleTest.vue'
import UserProfile from '@/views/UserProfile.vue'

// 绠＄悊绯荤粺鐩稿叧瀵煎叆
import AdminLayout from '@/layouts/AdminLayout.vue'
import AdminLogin from '@/views/Admin/Login.vue'
import AdminDashboard from '@/views/Admin/Dashboard.vue'

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
      title: '鐧诲綍璋冭瘯'
    }
  },
  // 璺敱娴嬭瘯
  {
    path: '/route-test',
    name: 'RouteTest',
    component: RouteTest,
    meta: {
      title: '璺敱娴嬭瘯'
    }
  },
  // 璋冭瘯璺敱
  {
    path: '/debug',
    name: 'DebugPage',
    component: DebugPage,
    meta: {
      title: 'Vue搴旂敤璋冭瘯'
    }
  },
  // 娴嬭瘯璺敱
  {
    path: '/test',
    name: 'SimpleTest',
    component: SimpleTest,
    meta: {
      title: '娴嬭瘯椤甸潰'
    }
  },
  // 用户端独立页面 - 不需要导航栏的页面
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: {
      title: '鐢ㄦ埛鐧诲綍',
      requiresGuest: true
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: {
      title: '鐢ㄦ埛娉ㄥ唽',
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
          title: '棣栭〉'
        }
      },
      // 鍟嗗搧鐩稿叧璺敱
      {
        path: 'products',
        name: 'ProductList',
        component: ProductList,
        meta: {
          title: '鍟嗗搧鍒楄〃'
        }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: ProductDetail,
        meta: {
          title: '鍟嗗搧璇︽儏'
        }
      },
      {
        path: 'product/publish',
        name: 'ProductPublish',
        component: ProductForm,
        meta: {
          title: '鍙戝竷鍟嗗搧',
          requiresAuth: true
        }
      },
      {
        path: 'product/edit/:id',
        name: 'ProductEdit',
        component: ProductForm,
        meta: {
          title: '缂栬緫鍟嗗搧',
          requiresAuth: true
        }
      },
      {
        path: 'my-products',
        name: 'MyProducts',
        component: MyProducts,
        meta: {
          title: '鎴戠殑鍟嗗搧',
          requiresAuth: true
        }
      },
      // 鐢ㄦ埛鐩稿叧璺敱
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: {
          title: '涓汉涓績',
          requiresAuth: true
        }
      },
      {
        path: 'profile/:id',
        name: 'UserProfile',
        component: UserProfile,
        meta: {
          title: '鐢ㄦ埛璧勬枡',
          requiresAuth: true
        }
      },
      // 娑堟伅鐩稿叧璺敱
      {
        path: 'messages',
        name: 'Messages',
        component: Messages,
        meta: {
          title: '娑堟伅',
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
          title: '鎴戠殑璁㈠崟',
          requiresAuth: true
        }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: OrderDetail,
        meta: {
          title: '璁㈠崟璇︽儏',
          requiresAuth: true
        }
      },
      // 鏀粯鐩稿叧璺敱
      {
        path: 'payment/:id',
        name: 'Payment',
        component: Payment,
        meta: {
          title: '璁㈠崟鏀粯',
          requiresAuth: true
        }
      },
      {
        path: 'payment-history',
        name: 'PaymentHistory',
        component: PaymentHistory,
        meta: {
          title: '鏀粯璁板綍',
          requiresAuth: true
        }
      },
      {
        path: 'payment-demo',
        name: 'PaymentDemo',
        component: PaymentDemo,
        meta: {
          title: '鏀粯绯荤粺婕旂ず'
        }
      },
      // 淇＄敤璇勪环鐩稿叧璺敱
      {
        path: 'credit',
        name: 'CreditInfo',
        component: CreditInfo,
        meta: {
          title: '鎴戠殑淇＄敤',
          requiresAuth: true
        }
      },
      // 浠茶鐩稿叧璺敱
      {
        path: 'arbitration/apply',
        name: 'ArbitrationApply',
        component: ArbitrationApply,
        meta: {
          title: '鐢宠浠茶',
          requiresAuth: true
        }
      },
      {
        path: 'arbitration/list',
        name: 'ArbitrationList',
        component: ArbitrationList,
        meta: {
          title: '鎴戠殑浠茶',
          requiresAuth: true
        }
      },
      {
        path: 'arbitration/detail/:id',
        name: 'ArbitrationDetail',
        component: ArbitrationDetail,
        meta: {
          title: '浠茶璇︽儏',
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
        component: DisputeMyList,
        meta: {
          title: '我的争议',
          requiresAuth: true
        }
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
        component: SellerDisputePending,
        meta: {
          title: '卖家待响应争议',
          requiresAuth: true
        }
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
      },
      // 鏁版嵁缁熻璺敱
      {
        path: 'statistics/overview',
        name: 'StatisticsOverview',
        component: () => import('@/views/Admin/Statistics/Overview.vue'),
        meta: {
          title: '鏁版嵁姒傝'
        }
      },
      {
        path: 'statistics/arbitration',
        name: 'StatisticsArbitration',
        component: () => import('@/views/Admin/Statistics/ArbitrationStats.vue'),
        meta: {
          title: '浠茶缁熻'
        }
      },
      {
        path: 'statistics/trend',
        name: 'StatisticsTrend',
        component: () => import('@/views/Admin/Statistics/TrendAnalysis.vue'),
        meta: {
          title: '瓒嬪娍鍒嗘瀽'
        }
      },
      {
        path: 'statistics/reports',
        name: 'StatisticsReports',
        component: () => import('@/views/Admin/Statistics/Reports.vue'),
        meta: {
          title: '鎶ヨ〃瀵煎嚭'
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
  document.title = to.meta.title ? `${to.meta.title} - 鏍″洯璺宠殼甯傚満` : '鏍″洯璺宠殼甯傚満'

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
        next('/admin/dashboard')
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
        alert('鏉冮檺涓嶈冻')
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
    alert('璇峰厛鐧诲綍')
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


```

## front/vue-project/src/views/Admin/Arbitration/CaseWorkbench.vue
```vue
<template>
  <div class="case-workbench" v-loading="loading">
    <div class="workbench-topbar">
      <div>
        <h2 class="page-title">管理员仲裁详情工作台</h2>
        <p class="page-subtitle">聚焦争议核心、证据对照与处理动作，快速完成判案决策</p>
      </div>
      <div class="toolbar-actions">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <el-button :loading="loading" @click="loadCaseDetail">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-alert
      v-if="loadError"
      type="error"
      :closable="false"
      class="module-gap"
      :title="loadError"
    />

    <el-empty
      v-if="!loading && !caseDetail && !loadError"
      description="未找到案件数据"
      class="module-gap"
    />

    <CaseSummaryCard v-if="caseDetail" :case-data="caseDetail">
      <template #actions>
        <el-button
          v-for="action in quickActions"
          :key="action.key"
          :type="action.type"
          :plain="action.plain"
          @click="handleQuickAction(action.key)"
        >
          {{ action.label }}
        </el-button>
      </template>
    </CaseSummaryCard>

    <el-row v-if="caseDetail" :gutter="16" class="main-layout">
      <el-col :xs="24" :lg="17" class="left-col">
        <el-card class="module-card dispute-focus-card" shadow="never">
          <template #header>
            <div class="module-header">
              <span class="module-title">争议焦点</span>
              <span class="module-desc">办案摘要</span>
            </div>
          </template>

          <div class="focus-grid">
            <div class="focus-item buyer">
              <div class="focus-label">买家主张</div>
              <div class="focus-content">{{ caseDetail.buyerClaim }}</div>
            </div>
            <div class="focus-item seller">
              <div class="focus-label">卖家主张</div>
              <div class="focus-content">{{ caseDetail.sellerClaim }}</div>
            </div>
            <div class="focus-item platform">
              <div class="focus-label">平台关注点</div>
              <div class="focus-content">{{ caseDetail.platformFocus }}</div>
            </div>
            <div class="focus-item request">
              <div class="focus-label">仲裁请求</div>
              <div class="focus-content">{{ caseDetail.arbitrationRequest }}</div>
            </div>
          </div>
        </el-card>

        <el-card class="module-card module-gap" shadow="never">
          <template #header>
            <div class="module-header">
              <span class="module-title">协商记录摘要</span>
              <span class="module-desc">来源争议ID：{{ caseDetail.sourceDisputeId || '-' }}</span>
            </div>
          </template>
          <div class="negotiation-summary">
            {{ caseDetail.negotiationSummary || '-' }}
          </div>
        </el-card>

        <EvidenceComparePanel
          class="module-gap"
          :applicant-evidence="caseDetail.applicantEvidence"
          :respondent-evidence="caseDetail.respondentEvidence"
        />

        <el-card class="module-card module-gap" shadow="never">
          <template #header>
            <div class="module-header">
              <span class="module-title">平台辅助信息</span>
              <span class="module-desc">辅助判断，不抢主视觉</span>
            </div>
          </template>

          <el-tabs v-model="activeAssistTab" class="assist-tabs">
            <el-tab-pane label="聊天摘要" name="chat">
              <div class="chat-summary-list">
                <div
                  v-for="item in caseDetail.chatSummary"
                  :key="item.id"
                  class="chat-item"
                >
                  <div class="chat-meta">
                    <span class="speaker">{{ item.speaker }}</span>
                    <span class="time">{{ item.time }}</span>
                  </div>
                  <div class="chat-content">{{ item.content }}</div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="商品快照" name="product">
              <div class="snapshot-product">
                <el-image
                  :src="caseDetail.productImage"
                  :preview-src-list="[caseDetail.productImage]"
                  fit="cover"
                  class="product-image"
                  preview-teleported
                />
                <div class="snapshot-info-grid">
                  <div class="snapshot-item">
                    <span class="k">商品名称</span>
                    <span class="v">{{ caseDetail.productName }}</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="k">商品标价</span>
                    <span class="v">¥{{ formatAmount(caseDetail.productPrice) }}</span>
                  </div>
                  <div class="snapshot-item">
                    <span class="k">当前订单金额</span>
                    <span class="v">¥{{ formatAmount(caseDetail.orderAmount) }}</span>
                  </div>
                  <div class="snapshot-item full">
                    <span class="k">商品描述</span>
                    <span class="v">{{ caseDetail.productSnapshot.description }}</span>
                  </div>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="订单快照" name="order">
              <div class="snapshot-info-grid order-grid">
                <div class="snapshot-item">
                  <span class="k">订单号</span>
                  <span class="v">{{ caseDetail.orderSnapshot.orderNo }}</span>
                </div>
                <div class="snapshot-item">
                  <span class="k">订单状态</span>
                  <span class="v">{{ caseDetail.orderSnapshot.status }}</span>
                </div>
                <div class="snapshot-item">
                  <span class="k">下单时间</span>
                  <span class="v">{{ caseDetail.orderSnapshot.createTime }}</span>
                </div>
                <div class="snapshot-item">
                  <span class="k">成交金额</span>
                  <span class="v">¥{{ formatAmount(caseDetail.orderSnapshot.amount) }}</span>
                </div>
                <div class="snapshot-item full">
                  <span class="k">订单备注</span>
                  <span class="v">{{ caseDetail.orderSnapshot.note || '-' }}</span>
                </div>
              </div>
            </el-tab-pane>

            <el-tab-pane label="系统证据" name="system">
              <div class="system-evidence-list">
                <div
                  v-for="item in caseDetail.systemEvidence"
                  :key="item.id"
                  class="system-evidence-item"
                >
                  <div class="system-main">
                    <div class="system-top">
                      <span class="system-title">{{ item.title }}</span>
                      <el-tag size="small" :type="getEvidenceTagType(item.type)">
                        {{ getEvidenceTypeLabel(item.type) }}
                      </el-tag>
                    </div>
                    <div class="system-desc">{{ item.description }}</div>
                    <div class="system-time">{{ item.createTime }}</div>
                  </div>
                  <el-image
                    v-if="item.type === 'image'"
                    :src="item.thumbnail || item.url"
                    :preview-src-list="[item.url || item.thumbnail]"
                    fit="cover"
                    class="system-thumb"
                    preview-teleported
                  />
                  <div v-else class="system-icon-box">
                    <el-icon><DataAnalysis /></el-icon>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>

        <el-card class="module-card module-gap" shadow="never">
          <template #header>
            <div class="module-header">
              <span class="module-title">处理记录时间线</span>
              <span class="module-desc">案件流转过程</span>
            </div>
          </template>

          <el-timeline>
            <el-timeline-item
              v-for="item in caseDetail.timeline"
              :key="item.id"
              :timestamp="item.time"
              :color="item.color"
            >
              <div class="timeline-title">{{ item.title }}</div>
              <div class="timeline-desc">{{ item.description }}</div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="7" class="right-col">
        <div class="sticky-wrap">
          <el-card class="module-card decision-panel" shadow="never">
            <template #header>
              <div class="module-header">
                <span class="module-title">处理面板</span>
                <el-tag :type="statusInfo.type">{{ statusInfo.label }}</el-tag>
              </div>
            </template>

            <div class="panel-section">
              <div class="panel-label">风险提示</div>
              <div class="risk-list">
                <div v-for="(tip, idx) in riskTips" :key="idx" class="risk-item">
                  {{ tip }}
                </div>
              </div>
            </div>

            <div class="panel-section recommend-section">
              <div class="panel-label">推荐动作</div>
              <div class="recommend-text">{{ recommendedAction }}</div>
            </div>

            <div class="panel-section">
              <div class="panel-label">处理备注</div>
              <el-input
                v-model="decisionRemark"
                type="textarea"
                :rows="5"
                resize="none"
                :disabled="isReadonly"
                placeholder="请输入处理意见、证据判断理由"
              />
            </div>

            <div v-if="isReadonly" class="result-panel">
              <div class="result-title">处理结果</div>
              <div class="result-status">{{ caseDetail.status === 'completed' ? '已完结' : '已驳回' }}</div>
              <div class="result-remark">{{ caseDetail.decisionRemark || caseDetail.rejectReason || '-' }}</div>
            </div>

            <div v-else class="action-buttons">
              <el-button
                v-if="canAccept"
                type="primary"
                class="full-btn"
                @click="handleAccept"
              >
                受理
              </el-button>
              <el-button
                v-if="canComplete"
                type="success"
                class="full-btn"
                @click="handleComplete"
              >
                完结
              </el-button>
              <el-button
                v-if="canReject"
                type="danger"
                plain
                class="full-btn"
                @click="handleReject"
              >
                驳回
              </el-button>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, DataAnalysis, Refresh } from '@element-plus/icons-vue'
import { arbitrationApi } from '@/api/arbitration'
import CaseSummaryCard from './components/CaseSummaryCard.vue'
import EvidenceComparePanel from './components/EvidenceComparePanel.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const loadError = ref('')
const caseDetail = ref(null)
const activeAssistTab = ref('chat')
const decisionRemark = ref('')

const statusMap = {
  pending: { label: '待处理', type: 'warning' },
  processing: { label: '处理中', type: 'primary' },
  completed: { label: '已完结', type: 'success' },
  rejected: { label: '已驳回', type: 'danger' }
}

const statusInfo = computed(() => {
  const status = caseDetail.value?.status || 'pending'
  return statusMap[status] || statusMap.pending
})

const canAccept = computed(() => Boolean(caseDetail.value?.canAccept))
const canComplete = computed(() => Boolean(caseDetail.value?.canComplete))
const canReject = computed(() => Boolean(caseDetail.value?.canReject))
const isReadonly = computed(() => Boolean(caseDetail.value?.readonly))

const quickActions = computed(() => {
  if (!caseDetail.value) return []
  if (canAccept.value) {
    return [
      { key: 'accept', label: '立即受理', type: 'primary', plain: false },
      { key: 'reject', label: '直接驳回', type: 'danger', plain: true }
    ]
  }
  if (canComplete.value) {
    return [
      { key: 'complete', label: '完结案件', type: 'success', plain: false },
      { key: 'reject', label: '驳回申请', type: 'danger', plain: true }
    ]
  }
  return [{ key: 'readonly', label: '查看处理结果', type: 'info', plain: true }]
})

const riskTips = computed(() => {
  if (!caseDetail.value) return []
  const tips = []
  const diffCount = Math.abs(
    (caseDetail.value.applicantEvidence || []).length - (caseDetail.value.respondentEvidence || []).length
  )

  if (canAccept.value) tips.push('案件尚未受理，存在处理时效风险')
  if (canComplete.value) tips.push('处理中案件，建议在本轮给出明确结论')
  if (diffCount >= 2) tips.push('双方证据量差异较大，建议重点核验系统记录')
  if (Number(caseDetail.value.orderAmount || 0) >= 500) tips.push('争议金额较高，处理备注建议保留完整裁定依据')
  if (!tips.length) tips.push('当前案件风险可控，按流程归档即可')
  return tips.slice(0, 3)
})

const recommendedAction = computed(() => {
  if (!caseDetail.value) return '-'
  if (canAccept.value) return '建议先受理，再核对聊天摘要与系统证据是否一致。'
  if (canComplete.value) return '建议结合双方证据对照和平台关注点，输出可追溯的处理结论。'
  return '案件已结束，可复核处理结果并保留备注作为后续申诉依据。'
})

const loadCaseDetail = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const id = route.params.id
    if (!id) {
      caseDetail.value = null
      return
    }

    const response = await arbitrationApi.getAdminArbitrationDetail(id)
    const detail = response?.data
    if (!detail) {
      caseDetail.value = null
      return
    }

    caseDetail.value = normalizeCaseDetail(detail)
    decisionRemark.value = caseDetail.value.decisionRemark || caseDetail.value.rejectReason || ''
    activeAssistTab.value = 'chat'
  } catch (error) {
    caseDetail.value = null
    loadError.value = error?.message || '案件数据加载失败'
    ElMessage.error(loadError.value)
  } finally {
    loading.value = false
  }
}

const normalizeCaseDetail = (detail) => {
  const statusCode = Number(detail.status ?? 0)
  const status = statusCodeToKey(statusCode)
  const orderSnapshot = {
    ...(detail.orderSnapshot || {}),
    createTime: formatDateTime(detail.orderSnapshot?.createTime),
    updateTime: formatDateTime(detail.orderSnapshot?.updateTime)
  }

  return {
    ...detail,
    id: detail.id,
    status,
    caseNumber: detail.caseNumber || `ARB${String(detail.id || '').padStart(6, '0')}`,
    applicant: detail.applicantName || (detail.applicantId ? `用户${detail.applicantId}` : '-'),
    respondent: detail.respondentName || (detail.respondentId ? `用户${detail.respondentId}` : '-'),
    handler: detail.handlerName || '待分配',
    createTime: formatDateTime(detail.createTime),
    buyerClaim: detail.buyerClaim || '-',
    sellerClaim: detail.sellerClaim || '-',
    platformFocus: detail.platformFocus || '-',
    arbitrationRequest: detail.arbitrationRequest || '-',
    sourceDisputeId: detail.sourceDisputeId || null,
    negotiationSummary: detail.negotiationSummary || '-',
    applicantEvidence: normalizeEvidence(detail.applicantEvidence),
    respondentEvidence: normalizeEvidence(detail.respondentEvidence),
    systemEvidence: normalizeEvidence(detail.systemEvidence),
    chatSummary: normalizeChatSummary(detail.chatSummary),
    orderSnapshot,
    productSnapshot: {
      ...(detail.productSnapshot || {}),
      description: detail.productSnapshot?.description || '-'
    },
    timeline: normalizeTimeline(detail.timeline),
    canAccept: Boolean(detail.canAccept),
    canComplete: Boolean(detail.canComplete),
    canReject: Boolean(detail.canReject),
    readonly: Boolean(detail.readonly),
    decisionRemark: detail.decisionRemark || '',
    rejectReason: detail.rejectReason || ''
  }
}

const normalizeEvidence = (list) => {
  if (!Array.isArray(list)) return []
  return list.map((item, index) => ({
    id: item.id || `${index + 1}`,
    type: item.type || 'text',
    title: item.title || '证据材料',
    description: item.description || '',
    url: item.url || '',
    thumbnail: item.thumbnail || item.url || '',
    createTime: formatDateTime(item.createTime)
  }))
}

const normalizeChatSummary = (list) => {
  if (!Array.isArray(list)) return []
  return list.map((item, index) => ({
    id: item.id || `${index + 1}`,
    speaker: item.speaker || '平台',
    time: formatDateTime(item.time),
    content: item.content || ''
  }))
}

const normalizeTimeline = (list) => {
  if (!Array.isArray(list)) return []
  return list.map((item, index) => ({
    id: item.id || `${index + 1}`,
    title: item.title || '处理记录',
    description: item.description || '',
    time: formatDateTime(item.time),
    color: item.color || '#909399'
  }))
}

const statusCodeToKey = (status) => {
  if (status === 1) return 'processing'
  if (status === 2) return 'completed'
  if (status === 3) return 'rejected'
  return 'pending'
}

const handleQuickAction = (actionKey) => {
  if (actionKey === 'accept') return handleAccept()
  if (actionKey === 'complete') return handleComplete()
  if (actionKey === 'reject') return handleReject()
  ElMessage.info('案件已结束，请在右侧查看处理结论')
}

const handleAccept = async () => {
  if (!caseDetail.value || !canAccept.value || submitting.value) return
  try {
    await ElMessageBox.confirm('确认受理该案件？', '受理确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    submitting.value = true
    await arbitrationApi.acceptAdminArbitration(caseDetail.value.id)
    ElMessage.success('案件已受理')
    await loadCaseDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '受理失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleReject = async () => {
  if (!caseDetail.value || !canReject.value || submitting.value) return
  const remark = String(decisionRemark.value || '').trim()
  if (!remark) {
    ElMessage.warning('请先填写驳回原因')
    return
  }
  try {
    await ElMessageBox.confirm('确认驳回该案件？', '驳回确认', {
      confirmButtonText: '确认驳回',
      cancelButtonText: '取消',
      type: 'warning'
    })
    submitting.value = true
    await arbitrationApi.rejectAdminArbitration({
      arbitrationId: caseDetail.value.id,
      rejectReason: remark
    })
    ElMessage.success('案件已驳回')
    await loadCaseDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '驳回失败')
    }
  } finally {
    submitting.value = false
  }
}

const handleComplete = async () => {
  if (!caseDetail.value || !canComplete.value || submitting.value) return
  const remark = String(decisionRemark.value || '').trim()
  if (!remark) {
    ElMessage.warning('请先填写处理备注')
    return
  }
  try {
    await ElMessageBox.confirm('确认完结该案件？', '完结确认', {
      confirmButtonText: '确认完结',
      cancelButtonText: '取消',
      type: 'warning'
    })
    submitting.value = true
    await arbitrationApi.completeAdminArbitration({
      arbitrationId: caseDetail.value.id,
      decisionRemark: remark
    })
    ElMessage.success('案件已完结')
    await loadCaseDetail()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '完结失败')
    }
  } finally {
    submitting.value = false
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  if (Number.isNaN(date.getTime())) return String(dateTime)
  return date.toLocaleString('zh-CN')
}

const formatAmount = (amount) => {
  const value = Number(amount || 0)
  return Number.isNaN(value) ? '0.00' : value.toFixed(2)
}

const evidenceTypeLabelMap = {
  image: '图片',
  chat: '聊天截图',
  text: '文字说明',
  system: '系统记录'
}

const evidenceTypeTagMap = {
  image: 'success',
  chat: 'info',
  text: 'warning',
  system: 'primary'
}

const getEvidenceTypeLabel = (type) => evidenceTypeLabelMap[type] || '其他'
const getEvidenceTagType = (type) => evidenceTypeTagMap[type] || 'info'

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadCaseDetail()
})

watch(
  () => route.params.id,
  () => {
    loadCaseDetail()
  }
)
</script>

<style scoped>
.case-workbench {
  --line-color: #e5eaf3;
  --text-main: #111827;
  --text-sub: #6b7280;
  --brand: #1d4ed8;
  --danger: #dc2626;
  --success: #059669;

  padding: 18px;
  background: #f3f6fb;
  min-height: calc(100vh - 56px);
}

.workbench-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 14px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  color: var(--text-main);
}

.page-subtitle {
  margin: 6px 0 0;
  color: var(--text-sub);
  font-size: 13px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.main-layout {
  align-items: flex-start;
}

.module-card {
  border-radius: 12px;
  border: 1px solid #dce4f2;
}

.module-gap {
  margin-top: 16px;
}

.module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.module-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
}

.module-desc {
  font-size: 12px;
  color: #8a94a8;
}

.focus-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.focus-item {
  border-radius: 10px;
  border: 1px solid var(--line-color);
  padding: 12px;
  background: #fff;
}

.focus-item.buyer {
  border-color: #dce8ff;
  background: #f8fbff;
}

.focus-item.seller {
  border-color: #ffe1e1;
  background: #fff9f9;
}

.focus-item.platform {
  border-color: #dcefe6;
  background: #f8fdf9;
}

.focus-item.request {
  border-color: #fff0c7;
  background: #fffdf7;
}

.focus-label {
  font-size: 12px;
  color: #6b7280;
}

.focus-content {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.65;
  color: #1f2937;
}

.negotiation-summary {
  white-space: pre-wrap;
  line-height: 1.7;
  color: #1f2937;
  background: #f9fbff;
  border: 1px solid #e4ebf8;
  border-radius: 10px;
  padding: 12px;
}

.assist-tabs :deep(.el-tabs__header) {
  margin-bottom: 10px;
}

.chat-summary-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.chat-item {
  border: 1px solid var(--line-color);
  border-radius: 8px;
  padding: 10px;
  background: #fbfcff;
}

.chat-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.chat-meta .speaker {
  font-size: 13px;
  font-weight: 600;
  color: #1f2937;
}

.chat-meta .time {
  font-size: 12px;
  color: #8a94a8;
}

.chat-content {
  font-size: 13px;
  line-height: 1.6;
  color: #374151;
}

.snapshot-product {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.product-image {
  width: 190px;
  height: 132px;
  border-radius: 8px;
  border: 1px solid #dce4f2;
  flex-shrink: 0;
}

.snapshot-info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  width: 100%;
}

.order-grid {
  margin-top: 4px;
}

.snapshot-item {
  border: 1px solid var(--line-color);
  border-radius: 8px;
  padding: 10px;
  background: #fff;
}

.snapshot-item.full {
  grid-column: 1 / -1;
}

.snapshot-item .k {
  display: block;
  font-size: 12px;
  color: #6b7280;
}

.snapshot-item .v {
  display: block;
  margin-top: 5px;
  font-size: 13px;
  color: #1f2937;
  line-height: 1.55;
}

.system-evidence-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.system-evidence-item {
  border: 1px solid var(--line-color);
  border-radius: 8px;
  padding: 10px;
  display: flex;
  gap: 10px;
  background: #fff;
}

.system-main {
  flex: 1;
  min-width: 0;
}

.system-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.system-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.system-desc {
  margin-top: 6px;
  font-size: 13px;
  line-height: 1.5;
  color: #374151;
}

.system-time {
  margin-top: 6px;
  font-size: 12px;
  color: #8a94a8;
}

.system-thumb,
.system-icon-box {
  width: 70px;
  height: 70px;
  border-radius: 6px;
  border: 1px solid #dbe4f3;
}

.system-icon-box {
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f4f7fd;
  color: #627491;
  font-size: 22px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
}

.timeline-desc {
  margin-top: 4px;
  font-size: 13px;
  line-height: 1.5;
  color: #4b5563;
}

.sticky-wrap {
  position: sticky;
  top: 16px;
}

.decision-panel {
  background: #fbfdff;
}

.panel-section {
  margin-bottom: 14px;
}

.panel-label {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 8px;
}

.risk-list {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.risk-item {
  font-size: 12px;
  line-height: 1.5;
  color: #7c2d12;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 8px;
  padding: 7px 9px;
}

.recommend-section {
  padding: 10px;
  border: 1px solid #dce6f8;
  border-radius: 8px;
  background: #f7fbff;
}

.recommend-text {
  font-size: 13px;
  line-height: 1.6;
  color: #1f2937;
}

.result-panel {
  margin-top: 12px;
  padding: 10px;
  border: 1px solid #d5e8dc;
  background: #f8fdf9;
  border-radius: 8px;
}

.result-title {
  font-size: 12px;
  color: #6b7280;
}

.result-status {
  margin-top: 4px;
  font-size: 14px;
  font-weight: 700;
  color: #166534;
}

.result-remark {
  margin-top: 6px;
  font-size: 13px;
  color: #1f2937;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 14px;
}

.full-btn {
  margin-left: 0 !important;
  width: 100%;
}

@media (max-width: 1200px) {
  .focus-grid {
    grid-template-columns: 1fr;
  }

  .snapshot-product {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: 180px;
  }
}

@media (max-width: 992px) {
  .sticky-wrap {
    position: static;
    margin-top: 16px;
  }
}

@media (max-width: 768px) {
  .case-workbench {
    padding: 12px;
  }

  .workbench-topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .snapshot-info-grid {
    grid-template-columns: 1fr;
  }
}
</style>

```

## front/vue-project/src/views/Admin/Arbitration/components/CaseSummaryCard.vue
```vue
<template>
  <el-card class="case-summary-card" shadow="never">
    <div class="summary-head">
      <div class="head-main">
        <div class="case-no">{{ caseData.caseNumber }}</div>
        <div class="head-tags">
          <el-tag :type="statusInfo.type" effect="dark">{{ statusInfo.label }}</el-tag>
          <el-tag type="warning" effect="plain">{{ caseData.reasonLabel }}</el-tag>
          <el-tag type="success" effect="plain">¥{{ formatAmount(caseData.orderAmount) }}</el-tag>
        </div>
      </div>
      <div class="head-amount">
        <div class="amount-label">订单金额</div>
        <div class="amount-value">¥{{ formatAmount(caseData.orderAmount) }}</div>
      </div>
    </div>

    <div class="summary-grid">
      <div class="summary-item">
        <span class="label">商品名称</span>
        <span class="value strong">{{ caseData.productName }}</span>
      </div>
      <div class="summary-item">
        <span class="label">订单号</span>
        <span class="value">{{ caseData.orderNo }}</span>
      </div>
      <div class="summary-item">
        <span class="label">申请人</span>
        <span class="value">{{ caseData.applicant }}</span>
      </div>
      <div class="summary-item">
        <span class="label">被申请人</span>
        <span class="value">{{ caseData.respondent }}</span>
      </div>
      <div class="summary-item">
        <span class="label">发起时间</span>
        <span class="value">{{ caseData.createTime }}</span>
      </div>
      <div class="summary-item">
        <span class="label">当前处理人</span>
        <span class="value">{{ caseData.handler || '-' }}</span>
      </div>
      <div class="summary-item">
        <span class="label">来源争议ID</span>
        <span class="value">{{ caseData.sourceDisputeId || '-' }}</span>
      </div>
    </div>

    <div class="summary-actions">
      <slot name="actions" />
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  caseData: {
    type: Object,
    required: true
  }
})

const statusMap = {
  pending: { label: '待处理', type: 'warning' },
  processing: { label: '处理中', type: 'primary' },
  completed: { label: '已完结', type: 'success' },
  rejected: { label: '已驳回', type: 'danger' }
}

const statusInfo = computed(() => statusMap[props.caseData.status] || statusMap.pending)

const formatAmount = (amount) => {
  const value = Number(amount || 0)
  return value.toFixed(2)
}
</script>

<style scoped>
.case-summary-card {
  border: 1px solid #dbe6f5;
  border-radius: 14px;
  background: linear-gradient(135deg, #f8fbff 0%, #eef5ff 55%, #f6f9ff 100%);
  margin-bottom: 16px;
}

.summary-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 16px;
}

.head-main {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.case-no {
  font-size: 20px;
  font-weight: 700;
  color: #1f2a37;
  letter-spacing: 0.3px;
}

.head-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.head-amount {
  min-width: 164px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #dbe7ff;
}

.amount-label {
  font-size: 12px;
  color: #6b7280;
}

.amount-value {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
  color: #0f766e;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #ffffff;
  border: 1px solid #e5ebf6;
}

.summary-item .label {
  font-size: 12px;
  color: #6b7280;
}

.summary-item .value {
  font-size: 14px;
  color: #111827;
  font-weight: 500;
}

.summary-item .value.strong {
  font-size: 15px;
  font-weight: 600;
}

.summary-actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

@media (max-width: 1200px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .summary-head {
    flex-direction: column;
  }

  .head-amount {
    width: 100%;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>

```

## front/vue-project/src/views/OrderDetail.vue
```vue
<template>
  <div class="page-container">
    <div class="order-detail-container">
      <button class="back-btn" @click="router.back()">← 返回</button>

      <div v-if="loading" class="loading">加载中...</div>

      <div v-else-if="!order" class="empty">
        <p>订单不存在或无权限访问</p>
        <button class="btn btn-primary" @click="router.push('/orders')">返回订单列表</button>
      </div>

      <div v-else class="order-detail">
        <section class="status-section">
          <div :class="['status-icon', `status-${order.status}`]">{{ getStatusIcon(order.status) }}</div>
          <div class="status-info">
            <div :class="['status-text', `status-${order.status}`]">{{ order.statusDesc }}</div>
            <div class="status-tip">{{ getStatusTip(order.status) }}</div>
          </div>
        </section>

        <section class="card-section">
          <h3 class="section-title">订单信息</h3>
          <div class="info-grid">
            <div class="info-item"><span class="k">订单号</span><span class="v">{{ order.orderNo }}</span></div>
            <div class="info-item"><span class="k">下单时间</span><span class="v">{{ formatTime(order.createTime) }}</span></div>
            <div class="info-item" v-if="order.payTime"><span class="k">支付时间</span><span class="v">{{ formatTime(order.payTime) }}</span></div>
            <div class="info-item"><span class="k">订单状态</span><span class="v">{{ order.statusDesc }}</span></div>
          </div>
        </section>

        <section class="card-section">
          <h3 class="section-title">商品信息</h3>
          <div class="product-card" @click="router.push(`/product/${order.productId}`)">
            <img
              :src="order.productImage || defaultImage"
              :alt="order.productTitle"
              class="product-img"
              @error="e => (e.target.src = defaultImage)"
            />
            <div class="product-info">
              <div class="product-title">{{ order.productTitle }}</div>
              <div class="product-price">¥{{ Number(order.productPrice || 0).toFixed(2) }}</div>
            </div>
          </div>
        </section>

        <section class="card-section">
          <h3 class="section-title">交易双方</h3>
          <div class="parties-grid">
            <div class="party-card" @click="viewUserProfile(order.buyerId)">
              <div class="party-label">买家</div>
              <div class="party-main">
                <img :src="order.buyerAvatar || defaultAvatar" class="party-avatar" @error="e => (e.target.src = defaultAvatar)" />
                <span>{{ order.buyerNickname || `用户${order.buyerId}` }}</span>
              </div>
            </div>
            <div class="party-card" @click="viewUserProfile(order.sellerId)">
              <div class="party-label">卖家</div>
              <div class="party-main">
                <img :src="order.sellerAvatar || defaultAvatar" class="party-avatar" @error="e => (e.target.src = defaultAvatar)" />
                <span>{{ order.sellerNickname || `用户${order.sellerId}` }}</span>
              </div>
            </div>
          </div>
        </section>

        <section class="card-section">
          <h3 class="section-title">金额信息</h3>
          <div class="amount-list">
            <div class="amount-item"><span>商品价格</span><strong>¥{{ Number(order.productPrice || 0).toFixed(2) }}</strong></div>
            <div class="amount-item total"><span>订单总额</span><strong>¥{{ Number(order.totalAmount || 0).toFixed(2) }}</strong></div>
          </div>
        </section>

        <section class="actions-section">
          <button v-if="order.status === 0 && isBuyer" class="btn btn-primary" @click="handlePay">去支付</button>
          <button v-if="order.status === 0" class="btn btn-default" @click="handleCancel">取消订单</button>
          <button v-if="order.status === 1 && isSeller" class="btn btn-primary" @click="handleShip">确认发货</button>
          <button v-if="order.status === 2 && isBuyer" class="btn btn-primary" @click="handleReceive">确认收货</button>

          <button v-if="canShowDisputeAction" class="btn btn-warning" @click="goDisputeFlow">
            ⚖️ {{ disputeActionText }}
          </button>

          <button v-if="order.status >= 1 && order.status <= 2" class="btn btn-info" @click="contactOther">
            联系{{ isBuyer ? '卖家' : '买家' }}
          </button>

          <button v-if="order.status === 3 && !hasEvaluated" class="btn btn-success" @click="openEvaluationDialog">
            评价{{ isBuyer ? '卖家' : '买家' }}
          </button>
          <div v-if="order.status === 3 && hasEvaluated" class="evaluation-status">已评价</div>
        </section>
      </div>
    </div>

    <EvaluationForm
      v-model:visible="showEvaluationDialog"
      :order-info="order"
      :target-user="targetUser"
      :evaluator-id="userStore.userInfo?.id"
      @success="handleEvaluationSuccess"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, cancelOrder, payOrder, shipOrder, receiveOrder } from '@/api/trade'
import { checkEvaluationStatus } from '@/api/credit'
import { arbitrationApi } from '@/api/arbitration'
import { useUserStore } from '@/stores/user'
import EvaluationForm from '@/components/EvaluationForm.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const order = ref(null)
const loading = ref(false)
const showEvaluationDialog = ref(false)
const hasEvaluated = ref(false)
const targetUser = ref({})
const currentDispute = ref(null)

const defaultImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="100"%3E%3Crect fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999"%3E暂无图片%3C/text%3E%3C/svg%3E'
const defaultAvatar = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="40" height="40"%3E%3Ccircle cx="20" cy="20" r="20" fill="%23ddd"/%3E%3Ctext x="50%25" y="50%25" dominant-baseline="middle" text-anchor="middle" fill="%23999" font-size="12"%3E头像%3C/text%3E%3C/svg%3E'

const isBuyer = computed(() => order.value && userStore.userInfo && order.value.buyerId === userStore.userInfo.id)
const isSeller = computed(() => order.value && userStore.userInfo && order.value.sellerId === userStore.userInfo.id)

const canShowDisputeAction = computed(() => {
  if (!order.value) return false
  if (!isBuyer.value) return false
  return order.value.status === 1 || order.value.status === 2 || order.value.status === 5
})

const disputeActionText = computed(() => {
  if (!currentDispute.value) return '发起争议协商'
  if (currentDispute.value.status === 'ESCALATED_TO_ARBITRATION') return '查看争议与仲裁'
  return '查看争议协商'
})

const getStatusIcon = (status) => {
  const icons = { 0: '⏳', 1: '📦', 2: '🚚', 3: '✅', 4: '❌', 5: '🔧' }
  return icons[status] || '📄'
}

const getStatusTip = (status) => {
  const tips = {
    0: '请尽快完成支付',
    1: '等待卖家发货',
    2: '商品运输中，请注意查收',
    3: '交易已完成',
    4: '订单已取消',
    5: '售后处理中'
  }
  return tips[status] || ''
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  if (Number.isNaN(date.getTime())) return String(timeStr)
  return date.toLocaleString('zh-CN')
}

const loadOrderDispute = async (orderId) => {
  currentDispute.value = null
  try {
    const listRes = await arbitrationApi.getMyDisputeList({ current: 1, size: 100 })
    const records = listRes?.data?.records || []
    currentDispute.value = records.find(item => Number(item.orderId) === Number(orderId)) || null
  } catch (error) {
    console.warn('加载订单争议状态失败:', error)
  }
}

const loadOrderDetail = async () => {
  loading.value = true
  try {
    const orderId = Number(route.params.id)
    const res = await getOrderDetail(orderId)
    order.value = res.data
    await loadOrderDispute(orderId)

    if (order.value?.status === 3) {
      await checkOrderEvaluationStatus()
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    alert(error.message || '加载订单详情失败')
    order.value = null
  } finally {
    loading.value = false
  }
}

const checkOrderEvaluationStatus = async () => {
  try {
    const res = await checkEvaluationStatus(order.value.id)
    hasEvaluated.value = Boolean(res?.data?.hasEvaluated)
  } catch (error) {
    console.error('检查评价状态失败:', error)
    hasEvaluated.value = false
  }
}

const handlePay = async () => {
  if (!confirm('确认支付该订单？')) return
  try {
    await payOrder(order.value.id)
    alert('支付成功')
    await loadOrderDetail()
  } catch (error) {
    alert(error.message || '支付失败')
  }
}

const handleCancel = async () => {
  if (!confirm('确认取消该订单？')) return
  try {
    await cancelOrder(order.value.id)
    alert('订单已取消')
    await loadOrderDetail()
  } catch (error) {
    alert(error.message || '取消订单失败')
  }
}

const handleShip = async () => {
  if (!confirm('确认已发货？')) return
  try {
    await shipOrder(order.value.id)
    alert('发货成功')
    await loadOrderDetail()
  } catch (error) {
    alert(error.message || '发货失败')
  }
}

const handleReceive = async () => {
  if (!confirm('确认收货？')) return
  try {
    await receiveOrder(order.value.id)
    alert('确认收货成功')
    await loadOrderDetail()
  } catch (error) {
    alert(error.message || '确认收货失败')
  }
}

const goDisputeFlow = async () => {
  if (!order.value) return

  const actionText = disputeActionText.value || '发起争议协商'
  if (!confirm(`确认要${actionText}吗？\n\n请先准备好事实说明与诉求说明。`)) return

  try {
    const orderId = Number(order.value.id)
    const orderNo = order.value.orderNo || `ORDER${String(orderId).padStart(8, '0')}`
    await loadOrderDispute(orderId)

    if (currentDispute.value?.id) {
      router.push(`/dispute/detail/${currentDispute.value.id}`)
      return
    }

    router.push({
      path: '/dispute/apply',
      query: { orderNo }
    })
  } catch (error) {
    console.error('检查争议状态失败:', error)
    alert(error?.message || '检查争议状态失败，请稍后重试')
  }
}

const contactOther = () => {
  if (!order.value) return

  let targetUserId
  let targetNickname
  let targetAvatar

  if (isBuyer.value) {
    targetUserId = order.value.sellerId || order.value.seller?.id
    targetNickname = order.value.sellerNickname || order.value.seller?.nickname || order.value.seller?.username
    targetAvatar = order.value.sellerAvatar || order.value.seller?.avatar
  } else if (isSeller.value) {
    targetUserId = order.value.buyerId || order.value.buyer?.id
    targetNickname = order.value.buyerNickname || order.value.buyer?.nickname || order.value.buyer?.username
    targetAvatar = order.value.buyerAvatar || order.value.buyer?.avatar
  }

  if (!targetUserId) {
    alert('无法获取联系对象')
    return
  }

  router.push({
    path: '/messages',
    query: {
      userId: targetUserId,
      username: targetNickname || `用户${targetUserId}`,
      nickname: targetNickname || `用户${targetUserId}`,
      avatar: targetAvatar || '',
      productId: order.value.productId,
      orderNo: order.value.orderNo
    }
  })
}

const viewUserProfile = (userId) => {
  if (!userId) return
  router.push(`/profile/${userId}`)
}

const openEvaluationDialog = () => {
  if (!order.value) return

  if (isBuyer.value) {
    targetUser.value = {
      id: order.value.sellerId,
      nickname: order.value.sellerNickname,
      username: order.value.sellerNickname,
      avatar: order.value.sellerAvatar
    }
  } else if (isSeller.value) {
    targetUser.value = {
      id: order.value.buyerId,
      nickname: order.value.buyerNickname,
      username: order.value.buyerNickname,
      avatar: order.value.buyerAvatar
    }
  } else {
    alert('无法确定评价对象')
    return
  }

  if (!targetUser.value.id) {
    alert('无法获取评价对象信息')
    return
  }

  showEvaluationDialog.value = true
}

const handleEvaluationSuccess = () => {
  hasEvaluated.value = true
  alert('评价提交成功')
}

onMounted(() => {
  loadOrderDetail()
})
</script>

<style scoped>
.page-container {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px;
}

.order-detail-container {
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5ebf4;
  padding: 20px;
}

.back-btn {
  border: 1px solid #ced8ea;
  background: #f8fbff;
  border-radius: 8px;
  padding: 6px 12px;
  cursor: pointer;
}

.loading,
.empty {
  padding: 48px 0;
  text-align: center;
  color: #6b7280;
}

.order-detail {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.status-section {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8fbff;
  border: 1px solid #dce5f5;
  border-radius: 10px;
  padding: 12px;
}

.status-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.status-text {
  font-size: 16px;
  font-weight: 600;
}

.status-tip {
  margin-top: 2px;
  font-size: 12px;
  color: #6b7280;
}

.card-section {
  border: 1px solid #e5ebf4;
  border-radius: 10px;
  padding: 14px;
}

.section-title {
  margin: 0 0 10px;
  font-size: 15px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fbff;
}

.info-item .k {
  color: #6b7280;
}

.product-card {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.product-img {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  object-fit: cover;
}

.product-title {
  font-weight: 600;
}

.product-price {
  margin-top: 4px;
  color: #dc2626;
}

.parties-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.party-card {
  border: 1px solid #e5ebf4;
  border-radius: 8px;
  padding: 10px;
  cursor: pointer;
}

.party-label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 8px;
}

.party-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.party-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
}

.amount-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 10px;
  border-radius: 8px;
  background: #f8fbff;
}

.amount-item.total {
  background: #eef6ff;
}

.actions-section {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.btn {
  border: 1px solid #cfd9e9;
  background: #fff;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
}

.btn-primary {
  background: #2f7dff;
  color: #fff;
  border-color: #2f7dff;
}

.btn-warning {
  background: #fff5e8;
  border-color: #ffddb0;
  color: #b45309;
}

.btn-info {
  background: #ecfeff;
  border-color: #a5f3fc;
  color: #0f766e;
}

.btn-success {
  background: #ecfdf3;
  border-color: #bbf7d0;
  color: #15803d;
}

.evaluation-status {
  color: #15803d;
  font-weight: 600;
}

@media (max-width: 768px) {
  .info-grid,
  .parties-grid {
    grid-template-columns: 1fr;
  }
}
</style>

```

## front/vue-project/src/views/Dispute/DisputeApply.vue
```vue
<template>
  <div class="dispute-apply-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <h3>鍙戣捣浜夎鍗忓晢</h3>
          <el-button @click="$router.go(-1)" plain>杩斿洖</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="130px">
        <el-form-item label="璁㈠崟鍙? prop="orderNo">
          <el-input v-model="form.orderNo" placeholder="璇疯緭鍏ヨ鍗曞彿骞舵牎楠?>
            <template #append>
              <el-button :loading="orderLoading" @click="loadOrderInfo">鏍￠獙</el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-alert
          v-if="orderInfo"
          type="success"
          :closable="false"
          class="order-alert"
          :title="`璁㈠崟宸插尮閰嶏細${orderInfo.orderNo}锛屽崠瀹禝D锛?{orderInfo.sellerId}`"
        />

        <el-form-item label="浜夎鍘熷洜" prop="reason">
          <el-select v-model="form.reason" placeholder="璇烽€夋嫨浜夎鍘熷洜" style="width: 100%">
            <el-option label="鍟嗗搧璐ㄩ噺闂" value="QUALITY_ISSUE" />
            <el-option label="鍟嗗搧鎻忚堪涓嶇" value="DESCRIPTION_MISMATCH" />
            <el-option label="鐗╂祦鎴栧彂璐ч棶棰? value="SHIPPING_DELAY" />
            <el-option label="鍞悗鏈嶅姟闂" value="SERVICE_ISSUE" />
            <el-option label="鍏朵粬" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="浜嬪疄璇存槑" prop="factDescription">
          <el-input
            v-model="form.factDescription"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="鎻忚堪鍙戠敓浜嗕粈涔堥棶棰橈紙浜嬪疄锛?
          />
        </el-form-item>

        <el-form-item label="璇夋眰绫诲瀷" prop="requestType">
          <el-select v-model="form.requestType" placeholder="璇烽€夋嫨璇夋眰绫诲瀷" style="width: 100%">
            <el-option label="鍏ㄩ閫€娆? value="FULL_REFUND" />
            <el-option label="閮ㄥ垎閫€娆? value="PARTIAL_REFUND" />
            <el-option label="閫€璐ч€€娆? value="RETURN_AND_REFUND" />
            <el-option label="琛ュ彂/鎹㈣揣" value="REPLACE" />
          </el-select>
        </el-form-item>

        <el-form-item label="璇夋眰璇存槑" prop="requestDescription">
          <el-input
            v-model="form.requestDescription"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            placeholder="鎻忚堪浣犲笇鏈涘钩鍙板浣曞鐞嗭紙璇夋眰锛?
          />
        </el-form-item>

        <el-form-item label="鏈熸湜閲戦" prop="expectedAmount">
          <el-input-number v-model="form.expectedAmount" :precision="2" :min="0" :max="999999" />
        </el-form-item>

        <el-form-item label="璇佹嵁涓婁紶">
          <el-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :http-request="uploadEvidence"
            :on-preview="previewFile"
            :on-remove="removeEvidence"
            :limit="8"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submit">鎻愪氦浜夎鐢宠</el-button>
          <el-button @click="$router.push('/dispute/my')">鎴戠殑浜夎</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog v-model="previewVisible" title="璇佹嵁棰勮" width="520px">
      <img :src="previewUrl" alt="preview" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as tradeApi from '@/api/trade'
import { arbitrationApi } from '@/api/arbitration'
import { uploadArbitrationEvidence } from '@/api/file'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const orderLoading = ref(false)
const submitting = ref(false)
const orderInfo = ref(null)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const form = reactive({
  orderNo: '',
  orderId: null,
  reason: '',
  factDescription: '',
  requestType: '',
  requestDescription: '',
  expectedAmount: 0,
  evidenceList: []
})

const rules = {
  orderNo: [{ required: true, message: '璇疯緭鍏ヨ鍗曞彿', trigger: 'blur' }],
  reason: [{ required: true, message: '璇烽€夋嫨浜夎鍘熷洜', trigger: 'change' }],
  factDescription: [{ required: true, message: '璇疯緭鍏ヤ簨瀹炶鏄?, trigger: 'blur' }],
  requestType: [{ required: true, message: '璇烽€夋嫨璇夋眰绫诲瀷', trigger: 'change' }],
  requestDescription: [{ required: true, message: '璇疯緭鍏ヨ瘔姹傝鏄?, trigger: 'blur' }]
}

const loadOrderInfo = async () => {
  if (!form.orderNo) {
    ElMessage.warning('璇峰厛杈撳叆璁㈠崟鍙?)
    return
  }
  orderLoading.value = true
  try {
    const res = await tradeApi.getOrderByOrderNo(form.orderNo.trim())
    if (!res?.data?.id) {
      throw new Error(res?.message || '璁㈠崟涓嶅瓨鍦?)
    }
    orderInfo.value = res.data
    form.orderId = res.data.id
    ElMessage.success('璁㈠崟鏍￠獙鎴愬姛')
  } catch (error) {
    orderInfo.value = null
    form.orderId = null
    ElMessage.error(error?.message || '璁㈠崟鏍￠獙澶辫触')
  } finally {
    orderLoading.value = false
  }
}

const uploadEvidence = async (options) => {
  try {
    const res = await uploadArbitrationEvidence(options.file)
    if (res?.code === 200 && res?.data) {
      const url = String(res.data)
      const item = {
        evidenceType: 'IMAGE',
        title: options.file?.name || '璇佹嵁鍥剧墖',
        description: '',
        fileUrl: url,
        thumbnailUrl: url
      }
      form.evidenceList.push(item)
      options.onSuccess?.({ code: 200, data: { url } })
      ElMessage.success('璇佹嵁涓婁紶鎴愬姛')
      return
    }
    throw new Error(res?.message || '涓婁紶澶辫触')
  } catch (error) {
    options.onError?.(error)
    ElMessage.error(error?.message || '涓婁紶澶辫触')
  }
}

const removeEvidence = (file) => {
  const url = file.url || file.response?.data?.url
  form.evidenceList = form.evidenceList.filter(item => item.fileUrl !== url)
}

const previewFile = (file) => {
  previewUrl.value = file.url || file.response?.data?.url || ''
  previewVisible.value = true
}

const submit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (!form.orderId) {
      ElMessage.warning('璇峰厛鏍￠獙璁㈠崟鍙?)
      return
    }
    submitting.value = true
    const payload = {
      orderId: form.orderId,
      reason: form.reason,
      factDescription: form.factDescription,
      requestType: form.requestType,
      requestDescription: form.requestDescription,
      expectedAmount: form.expectedAmount,
      evidenceList: form.evidenceList
    }
    await arbitrationApi.createDispute(payload)
    ElMessage.success('浜夎鐢宠宸叉彁浜わ紝绛夊緟鍗栧鍗忓晢鍝嶅簲')
    router.push('/dispute/my')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '鎻愪氦澶辫触')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (route.query.orderNo) {
    form.orderNo = String(route.query.orderNo)
    loadOrderInfo()
  }
})
</script>

<style scoped>
.dispute-apply-page {
  max-width: 920px;
  margin: 0 auto;
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-alert {
  margin-bottom: 16px;
}
</style>


```

## front/vue-project/src/views/Dispute/DisputeMyList.vue
```vue
<template>
  <div class="dispute-list-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <h3>鎴戠殑浜夎鍗忓晢</h3>
          <el-button type="primary" @click="$router.push('/dispute/apply')">鍙戣捣浜夎</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="浜夎ID" width="100" />
        <el-table-column prop="orderId" label="璁㈠崟ID" width="120" />
        <el-table-column prop="reason" label="鍘熷洜" width="160" />
        <el-table-column prop="requestType" label="璇夋眰绫诲瀷" width="140" />
        <el-table-column prop="expectedAmount" label="鏈熸湜閲戦" width="120" />
        <el-table-column label="鐘舵€? width="170">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ row.statusLabel || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="鍊掕鏃? width="170">
          <template #default="{ row }">
            {{ formatCountdown(row.expireTime) }}
          </template>
        </el-table-column>
        <el-table-column label="鎿嶄綔" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="goDetail(row.id)">璇︽儏</el-button>
            <el-button v-if="row.canEscalate" size="small" type="danger" @click="escalate(row)">鍗囩骇浠茶</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10 })

const statusType = (status) => {
  if (status === 'NEGOTIATION_SUCCESS') return 'success'
  if (status === 'NEGOTIATION_FAILED' || status === 'SELLER_TIMEOUT') return 'warning'
  if (status === 'ESCALATED_TO_ARBITRATION') return 'danger'
  return 'info'
}

const formatCountdown = (expireTime) => {
  if (!expireTime) return '-'
  const diff = new Date(expireTime).getTime() - Date.now()
  if (Number.isNaN(diff) || diff <= 0) return '宸茶秴鏃?
  const h = Math.floor(diff / (1000 * 60 * 60))
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${h}灏忔椂${m}鍒哷
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await arbitrationApi.getMyDisputeList(query)
    list.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (error) {
    ElMessage.error(error?.message || '鍔犺浇澶辫触')
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => {
  router.push(`/dispute/detail/${id}`)
}

const escalate = async (row) => {
  try {
    await ElMessageBox.confirm(`纭灏嗕簤璁?#${row.id} 鍗囩骇鍒扮鐞嗗憳浠茶鍚楋紵`, '鍗囩骇浠茶', {
      confirmButtonText: '纭鍗囩骇',
      cancelButtonText: '鍙栨秷',
      type: 'warning'
    })
    const res = await arbitrationApi.escalateDispute({
      disputeId: row.id,
      escalateReason: '涔板涓诲姩鍗囩骇浠茶'
    })
    ElMessage.success(`鍗囩骇鎴愬姛锛屼徊瑁両D锛?{res?.data || '-'}`)
    loadList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '鍗囩骇澶辫触')
    }
  }
}

onMounted(loadList)
</script>

<style scoped>
.dispute-list-page {
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>


```

## front/vue-project/src/views/Dispute/DisputeDetail.vue
```vue
<template>
  <div class="dispute-detail-page" v-loading="loading">
    <el-card shadow="hover" v-if="detail">
      <template #header>
        <div class="header-row">
          <h3>浜夎璇︽儏 #{{ detail.id }}</h3>
          <div>
            <el-tag :type="statusType(detail.status)">{{ detail.statusLabel || detail.status }}</el-tag>
            <el-button style="margin-left: 8px" @click="$router.go(-1)">杩斿洖</el-button>
          </div>
        </div>
      </template>

      <el-descriptions border :column="2">
        <el-descriptions-item label="璁㈠崟ID">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="鍟嗗搧ID">{{ detail.productId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="浜夎鍘熷洜">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="璇夋眰绫诲瀷">{{ detail.requestType }}</el-descriptions-item>
        <el-descriptions-item label="鏈熸湜閲戦">{{ detail.expectedAmount }}</el-descriptions-item>
        <el-descriptions-item label="鍊掕鏃?>{{ countdownText }}</el-descriptions-item>
        <el-descriptions-item label="浜嬪疄璇存槑" :span="2">{{ detail.factDescription }}</el-descriptions-item>
        <el-descriptions-item label="璇夋眰璇存槑" :span="2">{{ detail.requestDescription }}</el-descriptions-item>
      </el-descriptions>

      <el-card class="sub-card" shadow="never" v-if="detail.sellerProposal">
        <template #header><span>鍗栧鍗忓晢鏂规</span></template>
        <el-descriptions border :column="2">
          <el-descriptions-item label="鏂规绫诲瀷">{{ detail.sellerProposal.proposalType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="鏂规閲戦">{{ detail.sellerProposal.proposalAmount || '-' }}</el-descriptions-item>
          <el-descriptions-item label="杩愯垂鎵挎媴">{{ detail.sellerProposal.freightBearer || '-' }}</el-descriptions-item>
          <el-descriptions-item label="鏂规璇存槑" :span="2">{{ detail.sellerProposal.proposalDescription || '-' }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card class="sub-card" shadow="never">
        <template #header><span>鍗忓晢璁板綍鎽樿</span></template>
        <div class="summary">{{ detail.negotiationSummary || '-' }}</div>
      </el-card>

      <el-card class="sub-card" shadow="never">
        <template #header><span>鍗忓晢鏃堕棿绾?/span></template>
        <el-timeline>
          <el-timeline-item
            v-for="log in detail.negotiationLogs"
            :key="log.id"
            :timestamp="formatTime(log.createTime)"
          >
            <div><strong>{{ log.actionLabel || log.actionType }}</strong></div>
            <div>{{ log.content }}</div>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <div class="action-row">
        <el-button v-if="detail.canBuyerConfirm" type="success" @click="confirmProposal(true)">鎺ュ彈鏂规</el-button>
        <el-button v-if="detail.canBuyerConfirm" type="danger" @click="confirmProposal(false)">鎷掔粷骞跺崌绾т徊瑁?/el-button>
        <el-button v-if="detail.canEscalate" type="danger" plain @click="escalate">鍗囩骇浠茶</el-button>
        <el-tag v-if="detail.escalatedArbitrationId" type="danger">宸插崌绾т徊瑁侊細{{ detail.escalatedArbitrationId }}</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'

const route = useRoute()
const loading = ref(false)
const detail = ref(null)

const statusType = (status) => {
  if (status === 'NEGOTIATION_SUCCESS') return 'success'
  if (status === 'NEGOTIATION_FAILED' || status === 'SELLER_TIMEOUT') return 'warning'
  if (status === 'ESCALATED_TO_ARBITRATION') return 'danger'
  return 'info'
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return String(time)
  return date.toLocaleString('zh-CN')
}

const countdownText = computed(() => {
  const seconds = Number(detail.value?.countdownSeconds || 0)
  if (seconds <= 0) return '-'
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  return `${h}灏忔椂${m}鍒哷
})

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await arbitrationApi.getDisputeDetail(route.params.id)
    detail.value = res?.data || null
  } catch (error) {
    ElMessage.error(error?.message || '鍔犺浇澶辫触')
  } finally {
    loading.value = false
  }
}

const confirmProposal = async (accept) => {
  try {
    const tip = accept ? '纭鎺ュ彈鍗栧鏂规锛? : '纭鎷掔粷鏂规骞跺崌绾т徊瑁侊紵'
    await ElMessageBox.confirm(tip, '涔板纭', {
      confirmButtonText: '纭',
      cancelButtonText: '鍙栨秷',
      type: 'warning'
    })
    await arbitrationApi.buyerConfirmProposal({
      disputeId: detail.value.id,
      acceptProposal: accept,
      remark: accept ? '涔板鎺ュ彈鏂规' : '涔板鎷掔粷鏂规骞跺崌绾т徊瑁?
    })
    ElMessage.success('鎿嶄綔鎴愬姛')
    loadDetail()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.message || '鎿嶄綔澶辫触')
  }
}

const escalate = async () => {
  try {
    await ElMessageBox.confirm('纭鍗囩骇浠茶鍚楋紵', '鍗囩骇浠茶', {
      confirmButtonText: '纭',
      cancelButtonText: '鍙栨秷',
      type: 'warning'
    })
    const res = await arbitrationApi.escalateDispute({
      disputeId: detail.value.id,
      escalateReason: '涔板涓诲姩鍗囩骇浠茶'
    })
    ElMessage.success(`鍗囩骇鎴愬姛锛屼徊瑁両D锛?{res?.data || '-'}`)
    loadDetail()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.message || '鍗囩骇澶辫触')
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.dispute-detail-page {
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sub-card {
  margin-top: 16px;
}

.summary {
  line-height: 1.8;
  color: #303133;
}

.action-row {
  margin-top: 18px;
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
</style>


```

## front/vue-project/src/views/Dispute/SellerDisputePending.vue
```vue
<template>
  <div class="seller-dispute-page">
    <el-card shadow="hover">
      <template #header>
        <div class="header-row">
          <h3>鍗栧寰呭搷搴斾簤璁?/h3>
          <el-button @click="loadList">鍒锋柊</el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="浜夎ID" width="100" />
        <el-table-column prop="orderId" label="璁㈠崟ID" width="120" />
        <el-table-column prop="reason" label="浜夎鍘熷洜" width="160" />
        <el-table-column prop="statusLabel" label="鐘舵€? width="160" />
        <el-table-column label="鍓╀綑鏃堕棿" width="160">
          <template #default="{ row }">{{ formatCountdown(row.expireTime) }}</template>
        </el-table-column>
        <el-table-column label="鎿嶄綔" width="160" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goDetail(row.id)">鍝嶅簲鍗忓晢</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'

const router = useRouter()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10 })

const formatCountdown = (expireTime) => {
  if (!expireTime) return '-'
  const diff = new Date(expireTime).getTime() - Date.now()
  if (diff <= 0) return '宸茶秴鏃?
  const h = Math.floor(diff / (1000 * 60 * 60))
  const m = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  return `${h}灏忔椂${m}鍒哷
}

const loadList = async () => {
  loading.value = true
  try {
    const res = await arbitrationApi.getSellerPendingDisputes(query)
    list.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (error) {
    ElMessage.error(error?.message || '鍔犺浇澶辫触')
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => {
  router.push(`/dispute/seller/detail/${id}`)
}

onMounted(loadList)
</script>

<style scoped>
.seller-dispute-page {
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>


```

## front/vue-project/src/views/Dispute/SellerDisputeDetail.vue
```vue
<template>
  <div class="seller-dispute-detail-page" v-loading="loading">
    <el-card shadow="hover" v-if="detail">
      <template #header>
        <div class="header-row">
          <h3>鍗栧鍗忓晢鍝嶅簲 #{{ detail.id }}</h3>
          <el-button @click="$router.go(-1)">杩斿洖</el-button>
        </div>
      </template>

      <el-descriptions border :column="2">
        <el-descriptions-item label="璁㈠崟ID">{{ detail.orderId }}</el-descriptions-item>
        <el-descriptions-item label="鐘舵€?>{{ detail.statusLabel }}</el-descriptions-item>
        <el-descriptions-item label="浜夎鍘熷洜">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="璇夋眰绫诲瀷">{{ detail.requestType }}</el-descriptions-item>
        <el-descriptions-item label="鏈熸湜閲戦">{{ detail.expectedAmount }}</el-descriptions-item>
        <el-descriptions-item label="鎴鏃堕棿">{{ formatTime(detail.expireTime) }}</el-descriptions-item>
        <el-descriptions-item label="浜嬪疄璇存槑" :span="2">{{ detail.factDescription }}</el-descriptions-item>
        <el-descriptions-item label="璇夋眰璇存槑" :span="2">{{ detail.requestDescription }}</el-descriptions-item>
      </el-descriptions>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" class="response-form">
        <el-form-item label="鍝嶅簲绫诲瀷" prop="responseType">
          <el-radio-group v-model="form.responseType">
            <el-radio value="AGREE">鍚屾剰涔板璇夋眰</el-radio>
            <el-radio value="REJECT">鎷掔粷涔板璇夋眰</el-radio>
            <el-radio value="PROPOSE">鎻愬嚭鏇夸唬鏂规</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="鍝嶅簲璇存槑" prop="responseDescription" v-if="form.responseType !== 'PROPOSE'">
          <el-input v-model="form.responseDescription" type="textarea" :rows="3" />
        </el-form-item>

        <template v-if="form.responseType === 'PROPOSE'">
          <el-form-item label="鏂规绫诲瀷" prop="proposalType">
            <el-select v-model="form.proposalType" style="width: 100%">
              <el-option label="閮ㄥ垎閫€娆? value="PARTIAL_REFUND" />
              <el-option label="鍏ㄩ閫€娆? value="FULL_REFUND" />
              <el-option label="閫€璐ч€€娆? value="RETURN_AND_REFUND" />
              <el-option label="琛ュ彂/鎹㈣揣" value="REPLACE" />
            </el-select>
          </el-form-item>
          <el-form-item label="鏂规閲戦" prop="proposalAmount">
            <el-input-number v-model="form.proposalAmount" :precision="2" :min="0" :max="999999" />
          </el-form-item>
          <el-form-item label="杩愯垂鎵挎媴" prop="freightBearer">
            <el-select v-model="form.freightBearer" style="width: 100%">
              <el-option label="涔板鎵挎媴" value="BUYER" />
              <el-option label="鍗栧鎵挎媴" value="SELLER" />
              <el-option label="骞冲彴鎵挎媴" value="PLATFORM" />
            </el-select>
          </el-form-item>
          <el-form-item label="鏂规璇存槑" prop="proposalDescription">
            <el-input v-model="form.proposalDescription" type="textarea" :rows="3" />
          </el-form-item>
        </template>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="submitResponse">鎻愪氦鍝嶅簲</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { arbitrationApi } from '@/api/arbitration'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const detail = ref(null)
const formRef = ref()

const form = reactive({
  responseType: 'AGREE',
  responseDescription: '',
  proposalType: '',
  proposalAmount: 0,
  proposalDescription: '',
  freightBearer: ''
})

const rules = {
  responseType: [{ required: true, message: '璇烽€夋嫨鍝嶅簲绫诲瀷', trigger: 'change' }],
  proposalType: [{
    validator: (_, value, callback) => {
      if (form.responseType === 'PROPOSE' && !value) callback(new Error('璇烽€夋嫨鏂规绫诲瀷'))
      else callback()
    }, trigger: 'change'
  }],
  proposalDescription: [{
    validator: (_, value, callback) => {
      if (form.responseType === 'PROPOSE' && !value) callback(new Error('璇峰～鍐欐柟妗堣鏄?))
      else callback()
    }, trigger: 'blur'
  }]
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return String(time)
  return date.toLocaleString('zh-CN')
}

const loadDetail = async () => {
  loading.value = true
  try {
    const res = await arbitrationApi.getDisputeDetail(route.params.id)
    detail.value = res?.data || null
  } catch (error) {
    ElMessage.error(error?.message || '鍔犺浇澶辫触')
  } finally {
    loading.value = false
  }
}

const submitResponse = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    const payload = {
      disputeId: Number(route.params.id),
      responseType: form.responseType,
      responseDescription: form.responseDescription,
      proposalType: form.proposalType,
      proposalAmount: form.proposalAmount,
      proposalDescription: form.proposalDescription,
      freightBearer: form.freightBearer
    }
    await arbitrationApi.sellerRespondDispute(payload)
    ElMessage.success('鍝嶅簲鎻愪氦鎴愬姛')
    router.push('/dispute/seller/pending')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error(error?.message || '鎻愪氦澶辫触')
  } finally {
    submitting.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.seller-dispute-detail-page {
  padding: 20px 0;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.response-form {
  margin-top: 20px;
}
</style>


```

## market-gateway/src/main/resources/bootstrap.yml
```yml
spring:
  application:
    name: market-gateway
  cloud:
    nacos:
      server-addr: 127.0.0.1:8848
      username: nacos
      password: nacos
      discovery:
        server-addr: 127.0.0.1:8848
        username: nacos
        password: nacos
        ip: 127.0.0.1
        # 閰嶇疆 Nacos 杩炴帴瓒呮椂鍜岄噸璇?
        naming-load-cache-at-start: false  # 涓嶅姞杞芥湰鍦扮紦瀛橈紝閬垮厤浣跨敤杩囨湡IP
        naming-polling-thread-count: 1  # 杞绾跨▼鏁?
        # 寮哄埗浣跨敤 HTTP 鑰屼笉鏄?gRPC
        use-only-site-interfaces: false
        register-enabled: true  # 閲嶆柊鍚敤鏈嶅姟娉ㄥ唽
        heartbeat-interval: 5000  # 蹇冭烦闂撮殧5绉?
        timeout: 10000  # 澧炲姞瓒呮椂鏃堕棿鍒?0绉?
        # 绂佺敤gRPC锛屼娇鐢℉TTP
        grpc:
          enable: false
      config:
        server-addr: 127.0.0.1:8848
        file-extension: yml
        username: nacos
        password: nacos
        timeout: 10000  # 澧炲姞瓒呮椂鏃堕棿
        # 绂佺敤gRPC锛屼娇鐢℉TTP
        grpc:
          enable: false
    loadbalancer:
      cache:
        enabled: true
        ttl: 5s  # 鏈嶅姟瀹炰緥缂撳瓨5绉掞紝閬垮厤闀挎椂闂寸紦瀛樿繃鏈烮P
        capacity: 256
    gateway:
      discovery:
        locator:
          enabled: true  # 鍚敤鏈嶅姟鍙戠幇璺敱
          lower-case-service-id: true
      routes:
        # 鐢ㄦ埛鏈嶅姟璺敱
        - id: market-service-user
          uri: lb://market-service-user
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1

        # 鍒嗙被鏈嶅姟璺敱锛堢嫭绔嬮厤缃紝閬垮厤涓庡晢鍝佽矾鐢卞啿绐侊級
        - id: market-service-category
          uri: lb://market-service-product
          predicates:
            - Path=/api/product/category/**
          filters:
            - StripPrefix=2  # 鍘绘帀 /api/product锛屼繚鐣?/category/**

        # 鍟嗗搧鏈嶅姟璺敱
        - id: market-service-product
          uri: lb://market-service-product
          predicates:
            - Path=/api/product/**
          filters:
            - StripPrefix=1

        # 浜ゆ槗鏈嶅姟璺敱
        - id: market-service-trade
          uri: lb://market-service-trade
          predicates:
            - Path=/api/trade/**
          filters:
            - StripPrefix=1

        # 鏀粯鏈嶅姟璺敱锛堟寚鍚戜氦鏄撴湇鍔★級
        - id: market-service-payment
          uri: lb://market-service-trade
          predicates:
            - Path=/api/payment/**
          filters:
            - StripPrefix=1

        # 鏂囦欢鏈嶅姟璺敱
        - id: market-service-file
          uri: lb://market-service-file
          predicates:
            - Path=/api/file/**
          filters:
            - StripPrefix=1

        # 娑堟伅鏈嶅姟璺敱
        - id: market-service-message
          uri: lb://market-service-message
          predicates:
            - Path=/api/message/**
          filters:
            - StripPrefix=1

        # 淇＄敤鏈嶅姟璺敱 - credit
        - id: market-service-credit-api
          uri: lb://market-service-credit
          predicates:
            - Path=/api/credit/**
          filters:
            - StripPrefix=1

        # 淇＄敤鏈嶅姟璺敱 - evaluation
        - id: market-service-evaluation-api
          uri: lb://market-service-credit
          predicates:
            - Path=/api/evaluation/**
          filters:
            - StripPrefix=1

        # 浠茶鏈嶅姟璺敱
        - id: market-service-arbitration
          uri: lb://market-service-arbitration
          predicates:
            - Path=/api/arbitration/**
          filters:
            - StripPrefix=1

        # 浜夎鍗忓晢鏈嶅姟璺敱
        - id: market-service-dispute
          uri: lb://market-service-arbitration
          predicates:
            - Path=/api/dispute/**
          filters:
            - StripPrefix=1

        # Feign鍐呴儴鎺ュ彛璺敱 - 浜ゆ槗鏈嶅姟锛堜笉鍘诲墠缂€锛岀洿鎺ヨ浆鍙戯級
        - id: feign-trade
          uri: lb://market-service-trade
          predicates:
            - Path=/feign/trade/**

        # Feign鍐呴儴鎺ュ彛璺敱 - 鍟嗗搧鏈嶅姟锛堜笉鍘诲墠缂€锛岀洿鎺ヨ浆鍙戯級
        - id: feign-product
          uri: lb://market-service-product
          predicates:
            - Path=/feign/product/**

        # Feign鍐呴儴鎺ュ彛璺敱 - 鐢ㄦ埛鏈嶅姟锛堜笉鍘诲墠缂€锛岀洿鎺ヨ浆鍙戯級
        - id: feign-user
          uri: lb://market-service-user
          predicates:
            - Path=/feign/user/**

  # Redis 閰嶇疆锛堢敤浜庨獙璇?Token锛?
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0
    password:
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms

server:
  port: 8100  # Gateway浣跨敤8100绔彛

# Gateway 璁よ瘉鐧藉悕鍗曢厤缃?
gateway:
  auth:
    white-list:
      # 鐢ㄦ埛鐩稿叧鍏紑鎺ュ彛
      - /api/user/auth/login
      - /api/user/auth/register
      - /api/user/login
      - /api/user/register
      # 鍟嗗搧鐩稿叧鍏紑鎺ュ彛
      - /api/product/list
      - /api/product/detail/**
      - /api/product/category/**
      - /api/product/statistics
      # Feign鍐呴儴璋冪敤
      - /feign/**

# 鏃ュ織閰嶇疆
logging:
  level:
    org.shyu.marketgateway: DEBUG
    org.springframework.cloud.gateway: INFO


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/entity/ArbitrationEntity.java
```java
package org.shyu.marketservicearbitration.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 浠茶鐢宠瀹炰綋绫? * @author shyu
 * @since 2026-04-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_arbitration")
public class ArbitrationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 涓婚敭ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 鍏宠仈璁㈠崟ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 鐢宠浜篒D
     */
    @TableField("applicant_id")
    private Long applicantId;

    /**
     * 琚敵璇変汉ID
     */
    @TableField("respondent_id")
    private Long respondentId;

    /**
     * 浠茶鍘熷洜
     */
    @TableField("reason")
    private String reason;

    /**
     * 璇︾粏鎻忚堪
     */
    @TableField("description")
    private String description;

    /**
     * 璇佹嵁鏉愭枡JSON
     */
    @TableField("evidence")
    private String evidence;

    @TableField("source_dispute_id")
    private Long sourceDisputeId;

    @TableField("request_type")
    private String requestType;

    @TableField("request_description")
    private String requestDescription;

    @TableField("expected_amount")
    private BigDecimal expectedAmount;

    @TableField("buyer_claim")
    private String buyerClaim;

    /**
     * 鐘舵€?0:寰呭鐞?1:澶勭悊涓?2:宸插畬缁?3:宸查┏鍥?     */
    @TableField("status")
    private Integer status;

    /**
     * 瑁佸喅缁撴灉
     */
    @TableField("result")
    private String result;

    @TableField("decision_remark")
    private String decisionRemark;

    @TableField("reject_reason")
    private String rejectReason;

    /**
     * 澶勭悊绠＄悊鍛業D
     */
    @TableField("handler_id")
    private Long handlerId;

    /**
     * 鍒涘缓鏃堕棿
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 鏇存柊鏃堕棿
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}

```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/service/impl/ArbitrationServiceImpl.java
```java
package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapiarbitration.enums.ArbitrationReason;
import org.shyu.marketapiarbitration.enums.ArbitrationStatus;
import org.shyu.marketapiproduct.dto.ProductDTO;
import org.shyu.marketapiproduct.feign.ProductFeignClient;
import org.shyu.marketapitrade.dto.OrderDTO;
import org.shyu.marketapitrade.feign.TradeFeignClient;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicearbitration.dto.ArbitrationCompleteDTO;
import org.shyu.marketservicearbitration.dto.ArbitrationRejectDTO;
import org.shyu.marketservicearbitration.dto.SupplementRequestDTO;
import org.shyu.marketservicearbitration.dto.SupplementSubmitDTO;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationEvidenceSubmissionEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationSupplementRequestEntity;
import org.shyu.marketservicearbitration.mapper.ArbitrationMapper;
import org.shyu.marketservicearbitration.service.IArbitrationEvidenceSubmissionService;
import org.shyu.marketservicearbitration.service.IArbitrationLogService;
import org.shyu.marketservicearbitration.service.IArbitrationService;
import org.shyu.marketservicearbitration.service.IArbitrationSupplementRequestService;
import org.shyu.marketservicearbitration.vo.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArbitrationServiceImpl extends ServiceImpl<ArbitrationMapper, ArbitrationEntity> implements IArbitrationService {

    private static final int PENDING = 0;
    private static final int PROCESSING = 1;
    private static final int COMPLETED = 2;
    private static final int REJECTED = 3;
    private static final int LEGACY_WAIT_SUPPLEMENT = 4;

    private static final int SR_PENDING = 0;
    private static final int SR_SUBMITTED = 1;
    private static final int SR_EXPIRED = 2;
    private static final int SR_CANCELED = 3;

    private static final String BUYER = "BUYER";
    private static final String SELLER = "SELLER";
    private static final String SYSTEM = "SYSTEM";

    @Autowired private IArbitrationLogService arbitrationLogService;
    @Autowired private IArbitrationSupplementRequestService supplementRequestService;
    @Autowired private IArbitrationEvidenceSubmissionService evidenceSubmissionService;
    @Autowired private TradeFeignClient tradeFeignClient;
    @Autowired private ProductFeignClient productFeignClient;
    @Autowired private UserFeignClient userFeignClient;

    private final ObjectMapper om = new ObjectMapper();

    @Override
    @Transactional
    public ArbitrationEntity submitArbitration(ArbitrationVO vo) {
        if (checkArbitrationExists(vo.getOrderId(), vo.getApplicantId())) throw new BusinessException("璇ヨ鍗曞凡瀛樺湪浠茶鐢宠锛岃鍕块噸澶嶆彁浜?);
        ArbitrationEntity e = new ArbitrationEntity();
        BeanUtils.copyProperties(vo, e);
        e.setStatus(PENDING);
        e.setCreateTime(LocalDateTime.now());
        if (!save(e)) throw new BusinessException("浠茶鐢宠鎻愪氦澶辫触");
        log(e.getId(), vo.getApplicantId(), "SUBMIT", "鐢ㄦ埛鎻愪氦浠茶鐢宠");
        try {
            ArbitrationEvidenceSubmissionEntity s = new ArbitrationEvidenceSubmissionEntity();
            s.setArbitrationId(e.getId());
            s.setSubmitterId(e.getApplicantId());
            s.setSubmitterRole(resolveRole(e, e.getApplicantId()));
            s.setClaimText(vo.getReason());
            s.setFactText(vo.getDescription());
            s.setEvidenceUrls(normalizeEvidenceJson(vo.getEvidence()));
            s.setNote("鍒濆浠茶鐢宠鏉愭枡");
            s.setIsLate(0);
            evidenceSubmissionService.save(s);
        } catch (Exception ex) {
            log.warn("save initial evidence failed", ex);
        }
        return e;
    }

    @Override
    @Transactional
    public ArbitrationEntity updateArbitration(Long id, Long applicantId, ArbitrationVO vo) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("浠茶璁板綍涓嶅瓨鍦?);
        if (!Objects.equals(applicantId, e.getApplicantId())) throw new BusinessException("鏃犳潈闄愪慨鏀硅浠茶鐢宠");
        if (Arrays.asList(COMPLETED, REJECTED).contains(e.getStatus())) throw new BusinessException("褰撳墠浠茶鐘舵€佷笉鍙慨鏀?);
        e.setReason(vo.getReason());
        e.setDescription(vo.getDescription());
        e.setEvidence(vo.getEvidence());
        if (vo.getRespondentId() != null) e.setRespondentId(vo.getRespondentId());
        e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("淇敼浠茶鐢宠澶辫触");
        log(id, applicantId, "UPDATE", "鐢ㄦ埛淇敼浠茶鐢宠");
        return e;
    }

    @Override
    public IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size, Integer status, Long applicantId, Long respondentId) {
        return getArbitrationPage(current, size, status, applicantId, respondentId, null, null);
    }

    @Override
    public IPage<ArbitrationEntity> getArbitrationPage(Integer current, Integer size, Integer status, Long applicantId, Long respondentId, String keyword, String priority) {
        Page<ArbitrationEntity> p = new Page<>(current, size);
        QueryWrapper<ArbitrationEntity> q = new QueryWrapper<>();
        if (status != null) {
            if (Objects.equals(status, PROCESSING)) q.in("status", Arrays.asList(PROCESSING, LEGACY_WAIT_SUPPLEMENT));
            else q.eq("status", status);
        }
        if (applicantId != null) q.eq("applicant_id", applicantId);
        if (respondentId != null) q.eq("respondent_id", respondentId);
        if (StringUtils.hasText(keyword)) {
            String k = keyword.trim();
            q.and(w -> w.like("reason", k).or().like("description", k).or().like("result", k)
                    .or().like("order_id", k).or().like("applicant_id", k).or().like("respondent_id", k));
        }
        if (StringUtils.hasText(priority)) {
            String pty = priority.trim().toLowerCase(Locale.ROOT);
            LocalDateTime now = LocalDateTime.now();
            if ("high".equals(pty)) q.le("create_time", now.minusDays(3));
            else if ("normal".equals(pty)) q.between("create_time", now.minusDays(3), now.minusDays(1));
            else if ("low".equals(pty)) q.ge("create_time", now.minusDays(1));
        }
        q.orderByDesc("create_time");
        return page(p, q);
    }

    @Override
    public ArbitrationEntity getArbitrationDetail(Long id) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("浠茶璁板綍涓嶅瓨鍦?);
        return e;
    }

    @Override
    public IPage<AdminArbitrationListItemVO> getAdminArbitrationList(Integer current, Integer size,
                                                                      Integer status, String keyword, String priority) {
        IPage<ArbitrationEntity> page = getArbitrationPage(current, size, status, null, null, keyword, priority);
        Page<AdminArbitrationListItemVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<AdminArbitrationListItemVO> records = page.getRecords().stream()
                .map(this::toAdminListItem)
                .collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    @Transactional
    public Boolean acceptAdminArbitration(Long id, Long handlerId) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("浠茶璁板綍涓嶅瓨鍦?);
        if (!Objects.equals(normalizeStatus(e.getStatus()), PENDING)) {
            throw new BusinessException("褰撳墠鐘舵€佷笉鍏佽鍙楃悊");
        }
        e.setStatus(PROCESSING);
        e.setHandlerId(handlerId);
        e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("鍙楃悊浠茶鐢宠澶辫触");
        log(id, handlerId, "ACCEPT", "骞冲彴鍙楃悊妗堜欢");
        log(id, handlerId, "PROCESSING", "骞冲彴澶勭悊涓?);
        return true;
    }

    @Override
    @Transactional
    public Boolean completeAdminArbitration(ArbitrationCompleteDTO dto, Long handlerId) {
        ArbitrationEntity e = getById(dto.getArbitrationId());
        if (e == null) throw new BusinessException("浠茶璁板綍涓嶅瓨鍦?);
        if (!Objects.equals(normalizeStatus(e.getStatus()), PROCESSING)) {
            throw new BusinessException("褰撳墠鐘舵€佷笉鍏佽瀹岀粨");
        }

        String decisionRemark = StringUtils.hasText(dto.getDecisionRemark()) ? dto.getDecisionRemark().trim() : "";
        if (!StringUtils.hasText(decisionRemark)) throw new BusinessException("decisionRemark涓嶈兘涓虹┖");

        List<ArbitrationSupplementRequestEntity> pending = supplementRequestService.listPendingByArbitrationId(e.getId());
        if (!pending.isEmpty()) {
            expireAllPending(e.getId(), handlerId, "妗堜欢瀹岀粨鍓嶈嚜鍔ㄧ粨杞緟琛ヨ瘉璇锋眰");
        }

        e.setStatus(COMPLETED);
        e.setResult(decisionRemark);
        e.setDecisionRemark(decisionRemark);
        e.setRejectReason(null);
        e.setHandlerId(handlerId);
        e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("瀹岀粨浠茶鐢宠澶辫触");
        log(e.getId(), handlerId, "COMPLETE", "妗堜欢瀹岀粨锛? + decisionRemark);
        return true;
    }

    @Override
    @Transactional
    public Boolean rejectAdminArbitration(ArbitrationRejectDTO dto, Long handlerId) {
        ArbitrationEntity e = getById(dto.getArbitrationId());
        if (e == null) throw new BusinessException("浠茶璁板綍涓嶅瓨鍦?);
        Integer status = normalizeStatus(e.getStatus());
        if (!Arrays.asList(PENDING, PROCESSING).contains(status)) {
            throw new BusinessException("褰撳墠鐘舵€佷笉鍏佽椹冲洖");
        }
        String rejectReason = StringUtils.hasText(dto.getRejectReason()) ? dto.getRejectReason().trim() : "";
        if (!StringUtils.hasText(rejectReason)) throw new BusinessException("rejectReason涓嶈兘涓虹┖");

        List<ArbitrationSupplementRequestEntity> pending = supplementRequestService.listPendingByArbitrationId(e.getId());
        if (!pending.isEmpty()) {
            expireAllPending(e.getId(), handlerId, "妗堜欢椹冲洖鍓嶈嚜鍔ㄧ粨杞緟琛ヨ瘉璇锋眰");
        }

        e.setStatus(REJECTED);
        e.setResult("椹冲洖鍘熷洜锛? + rejectReason);
        e.setDecisionRemark(null);
        e.setRejectReason(rejectReason);
        e.setHandlerId(handlerId);
        e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("椹冲洖浠茶鐢宠澶辫触");
        log(e.getId(), handlerId, "REJECT", "妗堜欢椹冲洖锛? + rejectReason);
        return true;
    }

    @Override
    @Transactional
    public Boolean acceptArbitration(Long id, Long handlerId) {
        return acceptAdminArbitration(id, handlerId);
    }

    @Override
    @Transactional
    public Boolean handleArbitration(Long id, String result, Long handlerId) {
        return handleArbitration(id, result, handlerId, false);
    }

    @Override
    @Transactional
    public Boolean handleArbitration(Long id, String result, Long handlerId, Boolean force) {
        ArbitrationCompleteDTO dto = new ArbitrationCompleteDTO();
        dto.setArbitrationId(id);
        dto.setDecisionRemark(result);
        return completeAdminArbitration(dto, handlerId);
    }

    @Override
    @Transactional
    public Boolean rejectArbitration(Long id, String reason, Long handlerId) {
        ArbitrationRejectDTO dto = new ArbitrationRejectDTO();
        dto.setArbitrationId(id);
        dto.setRejectReason(reason);
        return rejectAdminArbitration(dto, handlerId);
    }

    @Override
    @Transactional
    public Boolean cancelArbitration(Long id, Long applicantId) {
        ArbitrationEntity e = getById(id);
        if (e == null) throw new BusinessException("浠茶璁板綍涓嶅瓨鍦?);
        if (!Objects.equals(e.getApplicantId(), applicantId)) throw new BusinessException("鏃犳潈闄愬彇娑堣浠茶鐢宠");
        if (!Arrays.asList(PENDING, LEGACY_WAIT_SUPPLEMENT).contains(e.getStatus())) throw new BusinessException("褰撳墠鐘舵€佷笉鍏佽鍙栨秷");
        if (Objects.equals(e.getStatus(), LEGACY_WAIT_SUPPLEMENT)) {
            for (ArbitrationSupplementRequestEntity r : supplementRequestService.listPendingByArbitrationId(id)) {
                r.setStatus(SR_CANCELED); r.setUpdateTime(LocalDateTime.now()); supplementRequestService.updateById(r);
            }
        }
        e.setStatus(REJECTED); e.setResult("鐢宠浜哄凡鍙栨秷浠茶鐢宠"); e.setDecisionRemark(null); e.setRejectReason("鐢宠浜轰富鍔ㄥ彇娑?); e.setUpdateTime(LocalDateTime.now());
        if (!updateById(e)) throw new BusinessException("鍙栨秷浠茶鐢宠澶辫触");
        log(id, applicantId, "CANCEL", "鐢ㄦ埛鍙栨秷浠茶鐢宠");
        return true;
    }

    @Override
    public ArbitrationStatsVO getArbitrationStats() {
        ArbitrationStatsVO s = new ArbitrationStatsVO();
        s.setPendingCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", PENDING))));
        s.setProcessingCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", PROCESSING))));
        s.setCompletedCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", COMPLETED))));
        s.setRejectedCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", REJECTED))));
        int supplement = Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().eq("status", LEGACY_WAIT_SUPPLEMENT)));
        s.setProcessingCount(s.getProcessingCount() + supplement);
        int total = Math.toIntExact(count(new QueryWrapper<>()));
        s.setTotalCount(total); s.setTotalCases(total);
        s.setTodayNewCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().ge("create_time", LocalDate.now()))));
        s.setTodayCount(s.getTodayNewCount());
        s.setUrgentCount(Math.toIntExact(count(new QueryWrapper<ArbitrationEntity>().in("status", Arrays.asList(PENDING, PROCESSING, LEGACY_WAIT_SUPPLEMENT)).le("create_time", LocalDateTime.now().minusDays(3)))));
        double avg = avgHandleDays();
        s.setAvgHandleDays(avg); s.setAvgProcessDays(avg);
        s.setResolvedCount(s.getCompletedCount() + s.getRejectedCount());
        return s;
    }

    @Override
    public ArbitrationStatsVO getUserArbitrationStats(Long userId) {
        ArbitrationStatsVO s = new ArbitrationStatsVO();
        List<ArbitrationEntity> list = this.list(new QueryWrapper<ArbitrationEntity>().eq("applicant_id", userId));
        int pending=0, processing=0, completed=0, rejected=0;
        for (ArbitrationEntity e : list) {
            if (Objects.equals(e.getStatus(), PENDING)) pending++;
            else if (Arrays.asList(PROCESSING, LEGACY_WAIT_SUPPLEMENT).contains(e.getStatus())) processing++;
            else if (Objects.equals(e.getStatus(), COMPLETED)) completed++;
            else if (Objects.equals(e.getStatus(), REJECTED)) rejected++;
        }
        s.setTotalCount(list.size()); s.setPendingCount(pending); s.setProcessingCount(processing); s.setCompletedCount(completed); s.setRejectedCount(rejected); s.setSuccessfulCount(completed);
        int resolved = completed + rejected; s.setSuccessRate(resolved == 0 ? 0.0 : completed * 100.0 / resolved);
        return s;
    }

    @Override
    public IPage<ArbitrationEntity> getUserArbitrationList(Long userId, Integer current, Integer size) {
        QueryWrapper<ArbitrationEntity> q = new QueryWrapper<>();
        q.eq("applicant_id", userId).orderByDesc("create_time");
        return page(new Page<>(current, size), q);
    }

    @Override
    public ArbitrationEntity getUserArbitrationByOrderId(Long userId, Long orderId) {
        QueryWrapper<ArbitrationEntity> q = new QueryWrapper<>();
        q.eq("applicant_id", userId).eq("order_id", orderId).orderByDesc("create_time").last("limit 1");
        return getOne(q, false);
    }

    @Override
    public Boolean checkArbitrationExists(Long orderId, Long applicantId) {
        QueryWrapper<ArbitrationEntity> q = new QueryWrapper<>();
        q.eq("order_id", orderId).eq("applicant_id", applicantId).ne("status", REJECTED);
        return count(q) > 0;
    }

    @Override
    public AdminArbitrationDetailVO getAdminArbitrationDetail(Long arbitrationId) {
        ArbitrationEntity e = getById(arbitrationId);
        if (e == null) throw new BusinessException("浠茶璁板綍涓嶅瓨鍦?);

        Integer status = normalizeStatus(e.getStatus());
        OrderDTO orderDTO = fetchOrder(e.getOrderId());
        ProductDTO productDTO = orderDTO == null ? null : fetchProduct(orderDTO.getProductId());
        List<ArbitrationEvidenceSubmissionEntity> submissions = evidenceSubmissionService.listByArbitrationId(arbitrationId);
        List<ArbitrationLogEntity> logs = arbitrationLogService.getLogsByArbitrationId(arbitrationId);
        List<ArbitrationSupplementRequestEntity> requests = supplementRequestService.listByArbitrationId(arbitrationId);

        AdminArbitrationDetailVO vo = new AdminArbitrationDetailVO();
        vo.setId(e.getId());
        vo.setCaseNumber(buildCaseNumber(e.getId()));
        vo.setStatus(status);
        vo.setStatusLabel(statusLabel(status));
        vo.setReason(e.getReason());
        vo.setReasonLabel(reasonLabel(e.getReason()));
        vo.setCreateTime(e.getCreateTime());
        vo.setUpdateTime(e.getUpdateTime());

        vo.setOrderId(e.getOrderId());
        vo.setOrderNo(orderDTO == null ? null : orderDTO.getOrderNo());
        vo.setOrderAmount(orderDTO == null ? null : orderDTO.getAmount());
        vo.setProductId(orderDTO == null ? null : orderDTO.getProductId());
        vo.setProductName(productDTO == null ? null : productDTO.getTitle());
        vo.setProductPrice(productDTO == null ? null : productDTO.getPrice());
        vo.setProductImage(resolveProductImage(productDTO));
        vo.setSourceDisputeId(e.getSourceDisputeId());

        vo.setApplicantId(e.getApplicantId());
        vo.setApplicantName(resolveUserName(e.getApplicantId()));
        vo.setRespondentId(e.getRespondentId());
        vo.setRespondentName(resolveUserName(e.getRespondentId()));
        vo.setHandlerId(e.getHandlerId());
        vo.setHandlerName(resolveHandlerName(e.getHandlerId()));

        vo.setBuyerClaim(firstText(e.getBuyerClaim(), resolveBuyerClaim(e, submissions)));
        vo.setSellerClaim(resolveSellerClaim(submissions));
        vo.setPlatformFocus(resolvePlatformFocus(e, orderDTO, requests));
        vo.setArbitrationRequest(firstText(e.getRequestDescription(), e.getDescription(), reasonLabel(e.getReason())));
        vo.setNegotiationSummary(buildNegotiationSummary(submissions));

        vo.setApplicantEvidence(buildEvidenceByRole(e, submissions, BUYER));
        vo.setRespondentEvidence(buildEvidenceByRole(e, submissions, SELLER));
        vo.setSystemEvidence(buildSystemEvidence(e, orderDTO, productDTO, requests));
        vo.setChatSummary(buildChatSummary(submissions));
        vo.setOrderSnapshot(buildOrderSnapshot(orderDTO));
        vo.setProductSnapshot(buildProductSnapshot(productDTO));
        vo.setTimeline(buildTimeline(logs));

        String result = StringUtils.hasText(e.getResult()) ? e.getResult().trim() : "";
        vo.setDecisionRemark(status == COMPLETED ? firstText(e.getDecisionRemark(), result) : "");
        vo.setRejectReason(status == REJECTED ? firstText(e.getRejectReason(), stripRejectPrefix(result)) : "");

        vo.setCanAccept(status == PENDING);
        vo.setCanComplete(status == PROCESSING);
        vo.setCanReject(status == PENDING || status == PROCESSING);
        vo.setReadonly(status == COMPLETED || status == REJECTED);
        return vo;
    }

    @Override
    @Transactional
    public Boolean requestSupplement(SupplementRequestDTO dto, Long handlerId) {
        ArbitrationEntity e = getById(dto.getArbitrationId());
        if (e == null) throw new BusinessException("浠茶璁板綍涓嶅瓨鍦?);
        if (!Arrays.asList(PENDING, PROCESSING, LEGACY_WAIT_SUPPLEMENT).contains(e.getStatus())) throw new BusinessException("褰撳墠浠茶鐘舵€佷笉鍏佽鍙戣捣琛ヨ瘉");
        String target = normalizeTarget(dto.getTargetParty());
        int hours = dto.getDueHours() == null || dto.getDueHours() <= 0 ? 48 : dto.getDueHours();
        ArbitrationSupplementRequestEntity r = new ArbitrationSupplementRequestEntity();
        r.setArbitrationId(e.getId()); r.setRequestedBy(handlerId); r.setTargetParty(target); r.setRequiredItems(dto.getRequiredItems()); r.setRemark(dto.getRemark()); r.setDueTime(LocalDateTime.now().plusHours(hours)); r.setStatus(SR_PENDING);
        if (!supplementRequestService.save(r)) throw new BusinessException("鍙戣捣琛ヨ瘉澶辫触");
        if (Objects.equals(e.getStatus(), PENDING) || Objects.equals(e.getStatus(), LEGACY_WAIT_SUPPLEMENT)) {
            e.setStatus(PROCESSING);
        }
        e.setHandlerId(handlerId); e.setUpdateTime(LocalDateTime.now()); updateById(e);
        log(e.getId(), handlerId, "SUPPLEMENT_REQUEST", "鍙戣捣琛ヨ瘉锛岀洰鏍?" + target + "锛岃姹?" + dto.getRequiredItems());
        return true;
    }

    @Override
    @Transactional
    public Boolean submitSupplement(SupplementSubmitDTO dto, Long submitterId) {
        ArbitrationEntity e = getById(dto.getArbitrationId());
        if (e == null) throw new BusinessException("浠茶璁板綍涓嶅瓨鍦?);
        if (!Objects.equals(submitterId, e.getApplicantId()) && !Objects.equals(submitterId, e.getRespondentId())) throw new BusinessException("鏃犳潈闄愭彁浜よ浠茶琛ヨ瘉");
        if (!hasContent(dto)) throw new BusinessException("璇疯嚦灏戞彁浜や竴椤硅ˉ璇佸唴瀹?);
        String role = resolveRole(e, submitterId);
        ArbitrationSupplementRequestEntity req = resolveReq(e.getId(), dto.getSupplementRequestId(), role);
        boolean late = req.getDueTime() != null && LocalDateTime.now().isAfter(req.getDueTime());
        ArbitrationEvidenceSubmissionEntity sub = new ArbitrationEvidenceSubmissionEntity();
        sub.setArbitrationId(e.getId()); sub.setSupplementRequestId(req.getId()); sub.setSubmitterId(submitterId); sub.setSubmitterRole(role);
        sub.setClaimText(dto.getClaim()); sub.setFactText(dto.getFacts()); sub.setEvidenceUrls(toJson(dto.getEvidenceUrls())); sub.setNote(dto.getNote()); sub.setIsLate(late ? 1 : 0);
        if (!evidenceSubmissionService.save(sub)) throw new BusinessException("琛ヨ瘉鎻愪氦澶辫触");
        log(e.getId(), submitterId, "SUPPLEMENT_SUBMIT", late ? "琛ヨ瘉宸叉彁浜わ紙閫炬湡锛? : "琛ヨ瘉宸叉彁浜?);
        if (reqSatisfied(req)) {
            req.setStatus(SR_SUBMITTED); req.setUpdateTime(LocalDateTime.now()); supplementRequestService.updateById(req);
            log(e.getId(), submitterId, "SUPPLEMENT_COMPLETE", "琛ヨ瘉璇锋眰宸叉弧瓒?);
        }
        if (supplementRequestService.listPendingByArbitrationId(e.getId()).isEmpty() && Objects.equals(e.getStatus(), LEGACY_WAIT_SUPPLEMENT)) {
            e.setStatus(PROCESSING); e.setUpdateTime(LocalDateTime.now()); updateById(e);
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean expireSupplementRequest(Long requestId, Long operatorId) {
        ArbitrationSupplementRequestEntity r = supplementRequestService.getById(requestId);
        if (r == null) throw new BusinessException("琛ヨ瘉璇锋眰涓嶅瓨鍦?);
        if (!Objects.equals(r.getStatus(), SR_PENDING)) throw new BusinessException("琛ヨ瘉璇锋眰宸插鐞嗭紝鏃犻渶缁撹浆");
        r.setStatus(SR_EXPIRED); r.setUpdateTime(LocalDateTime.now()); supplementRequestService.updateById(r);
        ArbitrationEntity e = getById(r.getArbitrationId());
        if (e != null && supplementRequestService.listPendingByArbitrationId(e.getId()).isEmpty() && Objects.equals(e.getStatus(), LEGACY_WAIT_SUPPLEMENT)) {
            e.setStatus(PROCESSING); e.setUpdateTime(LocalDateTime.now()); updateById(e);
        }
        log(r.getArbitrationId(), operatorId, "SUPPLEMENT_EXPIRED", "琛ヨ瘉璇锋眰瓒呮椂锛屾寜涓嶅埄浜嬪疄缁х画瑁佸喅");
        return true;
    }

    private void expireAllPending(Long arbitrationId, Long operatorId, String prefix) {
        for (ArbitrationSupplementRequestEntity r : supplementRequestService.listPendingByArbitrationId(arbitrationId)) {
            r.setStatus(SR_EXPIRED); r.setUpdateTime(LocalDateTime.now()); supplementRequestService.updateById(r);
            log(arbitrationId, operatorId, "SUPPLEMENT_EXPIRED", prefix + "锛宺equestId=" + r.getId());
        }
    }

    private AdminArbitrationListItemVO toAdminListItem(ArbitrationEntity e) {
        AdminArbitrationListItemVO vo = new AdminArbitrationListItemVO();
        OrderDTO orderDTO = fetchOrder(e.getOrderId());
        Integer status = normalizeStatus(e.getStatus());
        vo.setId(e.getId());
        vo.setCaseNumber(buildCaseNumber(e.getId()));
        vo.setStatus(status);
        vo.setStatusLabel(statusLabel(status));
        vo.setReason(e.getReason());
        vo.setReasonLabel(reasonLabel(e.getReason()));
        vo.setTitle(firstText(e.getDescription(), vo.getReasonLabel()));
        vo.setDescription(e.getDescription());
        vo.setResult(e.getResult());
        vo.setOrderId(e.getOrderId());
        vo.setOrderNo(orderDTO == null ? null : orderDTO.getOrderNo());
        vo.setOrderAmount(orderDTO == null ? null : orderDTO.getAmount());
        vo.setApplicantId(e.getApplicantId());
        vo.setApplicantName(resolveUserName(e.getApplicantId()));
        vo.setRespondentId(e.getRespondentId());
        vo.setRespondentName(resolveUserName(e.getRespondentId()));
        vo.setHandlerId(e.getHandlerId());
        vo.setHandlerName(resolveHandlerName(e.getHandlerId()));
        vo.setCreateTime(e.getCreateTime());
        vo.setUpdateTime(e.getUpdateTime());
        return vo;
    }

    private List<ArbitrationEvidenceVO> buildEvidenceByRole(ArbitrationEntity arbitration,
                                                             List<ArbitrationEvidenceSubmissionEntity> submissions,
                                                             String role) {
        List<ArbitrationEvidenceVO> evidenceList = new ArrayList<>();
        List<ArbitrationEvidenceSubmissionEntity> roleSubs = submissions.stream()
                .filter(x -> role.equalsIgnoreCase(String.valueOf(x.getSubmitterRole())))
                .sorted(Comparator.comparing(ArbitrationEvidenceSubmissionEntity::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        for (ArbitrationEvidenceSubmissionEntity sub : roleSubs) {
            if (StringUtils.hasText(sub.getClaimText())) {
                ArbitrationEvidenceVO claim = new ArbitrationEvidenceVO();
                claim.setId(sub.getId());
                claim.setType("text");
                claim.setTitle("涓诲紶璇存槑");
                claim.setDescription(sub.getClaimText());
                claim.setCreateTime(sub.getCreateTime());
                evidenceList.add(claim);
            }
            if (StringUtils.hasText(sub.getFactText())) {
                ArbitrationEvidenceVO fact = new ArbitrationEvidenceVO();
                fact.setId(sub.getId());
                fact.setType("text");
                fact.setTitle("浜嬪疄琛ュ厖");
                fact.setDescription(sub.getFactText());
                fact.setCreateTime(sub.getCreateTime());
                evidenceList.add(fact);
            }
            List<String> urls = parseJson(sub.getEvidenceUrls());
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                long baseId = sub.getId() == null ? 0L : sub.getId();
                ArbitrationEvidenceVO image = new ArbitrationEvidenceVO();
                image.setId(baseId * 100 + i + 1);
                image.setType("image");
                image.setTitle("璇佹嵁鍥剧墖");
                image.setDescription(StringUtils.hasText(sub.getNote()) ? sub.getNote() : "鎻愪氦鍥剧墖璇佹嵁");
                image.setUrl(url);
                image.setThumbnail(url);
                image.setCreateTime(sub.getCreateTime());
                evidenceList.add(image);
            }
        }

        if (evidenceList.isEmpty() && BUYER.equals(role) && StringUtils.hasText(arbitration.getEvidence())) {
            List<String> urls = parseJson(arbitration.getEvidence());
            for (int i = 0; i < urls.size(); i++) {
                long arbitrationId = arbitration.getId() == null ? 0L : arbitration.getId();
                ArbitrationEvidenceVO image = new ArbitrationEvidenceVO();
                image.setId(arbitrationId * 1000 + i + 1);
                image.setType("image");
                image.setTitle("鍒濆璇佹嵁");
                image.setDescription("鐢宠浠茶鏃朵笂浼?);
                image.setUrl(urls.get(i));
                image.setThumbnail(urls.get(i));
                image.setCreateTime(arbitration.getCreateTime());
                evidenceList.add(image);
            }
        }
        return evidenceList;
    }

    private List<ArbitrationEvidenceVO> buildSystemEvidence(ArbitrationEntity arbitration,
                                                            OrderDTO orderDTO,
                                                            ProductDTO productDTO,
                                                            List<ArbitrationSupplementRequestEntity> requests) {
        List<ArbitrationEvidenceVO> list = new ArrayList<>();

        ArbitrationEvidenceVO statusEvidence = new ArbitrationEvidenceVO();
        statusEvidence.setId(arbitration.getId() * 10 + 1);
        statusEvidence.setType("system");
        statusEvidence.setTitle("骞冲彴鐘舵€佽褰?);
        statusEvidence.setDescription("妗堜欢褰撳墠鐘舵€侊細" + statusLabel(normalizeStatus(arbitration.getStatus())));
        statusEvidence.setCreateTime(arbitration.getUpdateTime() == null ? arbitration.getCreateTime() : arbitration.getUpdateTime());
        list.add(statusEvidence);

        if (orderDTO != null) {
            ArbitrationEvidenceVO orderEvidence = new ArbitrationEvidenceVO();
            orderEvidence.setId(arbitration.getId() * 10 + 2);
            orderEvidence.setType("system");
            orderEvidence.setTitle("璁㈠崟蹇収");
            orderEvidence.setDescription("璁㈠崟鍙凤細" + firstText(orderDTO.getOrderNo(), "-")
                    + "锛岄噾棰濓細" + (orderDTO.getAmount() == null ? "-" : orderDTO.getAmount().toPlainString()));
            orderEvidence.setCreateTime(orderDTO.getUpdateTime() == null ? orderDTO.getCreateTime() : orderDTO.getUpdateTime());
            list.add(orderEvidence);
        }

        if (productDTO != null) {
            ArbitrationEvidenceVO productEvidence = new ArbitrationEvidenceVO();
            productEvidence.setId(arbitration.getId() * 10 + 3);
            productEvidence.setType("system");
            productEvidence.setTitle("鍟嗗搧蹇収");
            productEvidence.setDescription(firstText(productDTO.getTitle(), "鍟嗗搧淇℃伅") + "锛屾爣浠凤細"
                    + (productDTO.getPrice() == null ? "-" : productDTO.getPrice().toPlainString()));
            productEvidence.setUrl(resolveProductImage(productDTO));
            productEvidence.setThumbnail(resolveProductImage(productDTO));
            productEvidence.setCreateTime(productDTO.getUpdateTime() == null ? productDTO.getCreateTime() : productDTO.getUpdateTime());
            list.add(productEvidence);
        }

        for (ArbitrationSupplementRequestEntity request : requests) {
            ArbitrationEvidenceVO requestEvidence = new ArbitrationEvidenceVO();
            requestEvidence.setId(request.getId());
            requestEvidence.setType("system");
            requestEvidence.setTitle("琛ヨ瘉璇锋眰");
            requestEvidence.setDescription("瀵硅薄锛? + request.getTargetParty() + "锛岃姹傦細" + request.getRequiredItems());
            requestEvidence.setCreateTime(request.getCreateTime());
            list.add(requestEvidence);
        }
        return list;
    }

    private List<ArbitrationChatSummaryVO> buildChatSummary(List<ArbitrationEvidenceSubmissionEntity> submissions) {
        List<ArbitrationChatSummaryVO> list = new ArrayList<>();
        long seed = 1L;
        for (ArbitrationEvidenceSubmissionEntity sub : submissions) {
            String speaker = BUYER.equalsIgnoreCase(sub.getSubmitterRole()) ? "涔板" :
                    (SELLER.equalsIgnoreCase(sub.getSubmitterRole()) ? "鍗栧" : "骞冲彴");
            if (StringUtils.hasText(sub.getClaimText())) {
                ArbitrationChatSummaryVO item = new ArbitrationChatSummaryVO();
                item.setId(seed++);
                item.setSpeaker(speaker);
                item.setTime(sub.getCreateTime());
                item.setContent(sub.getClaimText());
                list.add(item);
            }
            if (StringUtils.hasText(sub.getFactText())) {
                ArbitrationChatSummaryVO item = new ArbitrationChatSummaryVO();
                item.setId(seed++);
                item.setSpeaker(speaker);
                item.setTime(sub.getCreateTime());
                item.setContent(sub.getFactText());
                list.add(item);
            }
        }
        if (list.isEmpty()) {
            ArbitrationChatSummaryVO item = new ArbitrationChatSummaryVO();
            item.setId(1L);
            item.setSpeaker("骞冲彴");
            item.setTime(LocalDateTime.now());
            item.setContent("鏆傛棤鍙彁鍙栫殑鑱婂ぉ鎽樿锛屽缓璁粨鍚堣鍗曚笌璇佹嵁鏉愭枡鍒ゅ畾銆?);
            list.add(item);
        }
        return list;
    }

    private String buildNegotiationSummary(List<ArbitrationEvidenceSubmissionEntity> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return "鏆傛棤鍗忓晢璁板綍鎽樿";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringBuilder builder = new StringBuilder("鍗忓晢鎽樿锛?);
        int count = 0;
        for (ArbitrationEvidenceSubmissionEntity sub : submissions) {
            if (count >= 8) {
                builder.append("...锛堝叾浣欑渷鐣ワ級");
                break;
            }
            String role = BUYER.equalsIgnoreCase(sub.getSubmitterRole()) ? "涔板"
                    : (SELLER.equalsIgnoreCase(sub.getSubmitterRole()) ? "鍗栧" : "绯荤粺");
            String content = firstText(sub.getClaimText(), sub.getFactText(), sub.getNote(), "-");
            builder.append("[").append(sub.getCreateTime() == null ? "-" : formatter.format(sub.getCreateTime()))
                    .append("] ").append(role).append("锛?).append(content).append("锛?);
            count++;
        }
        return builder.toString();
    }

    private List<ArbitrationTimelineVO> buildTimeline(List<ArbitrationLogEntity> logs) {
        List<ArbitrationTimelineVO> timeline = new ArrayList<>();
        for (ArbitrationLogEntity logEntity : logs) {
            ArbitrationTimelineVO item = new ArbitrationTimelineVO();
            item.setId(logEntity.getId());
            item.setTime(logEntity.getCreateTime());
            item.setTitle(timelineTitle(logEntity.getAction()));
            item.setDescription(firstText(logEntity.getRemark(), "鏃犲娉?));
            item.setColor(timelineColor(logEntity.getAction()));
            timeline.add(item);
        }
        timeline.sort(Comparator.comparing(ArbitrationTimelineVO::getTime, Comparator.nullsLast(Comparator.naturalOrder())));
        return timeline;
    }

    private Map<String, Object> buildOrderSnapshot(OrderDTO orderDTO) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (orderDTO == null) {
            map.put("note", "鏆傛棤璁㈠崟蹇収");
            return map;
        }
        map.put("orderId", orderDTO.getId());
        map.put("orderNo", orderDTO.getOrderNo());
        map.put("status", orderDTO.getStatus());
        map.put("amount", orderDTO.getAmount());
        map.put("buyerId", orderDTO.getBuyerId());
        map.put("sellerId", orderDTO.getSellerId());
        map.put("createTime", orderDTO.getCreateTime());
        map.put("updateTime", orderDTO.getUpdateTime());
        return map;
    }

    private Map<String, Object> buildProductSnapshot(ProductDTO productDTO) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (productDTO == null) {
            map.put("description", "鏆傛棤鍟嗗搧蹇収");
            return map;
        }
        map.put("productId", productDTO.getId());
        map.put("title", productDTO.getTitle());
        map.put("description", productDTO.getDescription());
        map.put("price", productDTO.getPrice());
        map.put("status", productDTO.getStatus());
        map.put("imageUrls", productDTO.getImageUrls());
        return map;
    }

    private Integer normalizeStatus(Integer status) {
        if (Objects.equals(status, LEGACY_WAIT_SUPPLEMENT)) {
            return PROCESSING;
        }
        return status == null ? PENDING : status;
    }

    private String statusLabel(Integer status) {
        return ArbitrationStatus.getByCode(normalizeStatus(status)).getName();
    }

    private String reasonLabel(String reason) {
        return ArbitrationReason.getByCode(reason).getDescription();
    }

    private String buildCaseNumber(Long id) {
        return "ARB" + String.format("%06d", id == null ? 0L : id);
    }

    private String resolveUserName(Long userId) {
        if (userId == null) return "鏈煡鐢ㄦ埛";
        try {
            Result<UserDTO> result = userFeignClient.getUserById(userId);
            if (result != null && result.isSuccess() && result.getData() != null) {
                UserDTO user = result.getData();
                return firstText(user.getNickname(), user.getUsername(), "鐢ㄦ埛" + userId);
            }
        } catch (Exception e) {
            log.warn("fetch user failed, userId={}", userId, e);
        }
        return "鐢ㄦ埛" + userId;
    }

    private String resolveHandlerName(Long handlerId) {
        if (handlerId == null) return "寰呭垎閰?;
        return resolveUserName(handlerId);
    }

    private String resolveBuyerClaim(ArbitrationEntity arbitration, List<ArbitrationEvidenceSubmissionEntity> submissions) {
        Optional<ArbitrationEvidenceSubmissionEntity> latestBuyer = submissions.stream()
                .filter(x -> BUYER.equalsIgnoreCase(x.getSubmitterRole()))
                .max(Comparator.comparing(ArbitrationEvidenceSubmissionEntity::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        if (latestBuyer.isPresent()) {
            return firstText(latestBuyer.get().getClaimText(), latestBuyer.get().getFactText(),
                    arbitration.getDescription(), "鏆傛棤涔板涓诲紶");
        }
        return firstText(arbitration.getDescription(), "鏆傛棤涔板涓诲紶");
    }

    private String resolveSellerClaim(List<ArbitrationEvidenceSubmissionEntity> submissions) {
        Optional<ArbitrationEvidenceSubmissionEntity> latestSeller = submissions.stream()
                .filter(x -> SELLER.equalsIgnoreCase(x.getSubmitterRole()))
                .max(Comparator.comparing(ArbitrationEvidenceSubmissionEntity::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        if (!latestSeller.isPresent()) return "鏆傛棤鍗栧涓诲紶";
        return firstText(latestSeller.get().getClaimText(), latestSeller.get().getFactText(), "鏆傛棤鍗栧涓诲紶");
    }

    private String resolvePlatformFocus(ArbitrationEntity arbitration, OrderDTO orderDTO,
                                        List<ArbitrationSupplementRequestEntity> requests) {
        StringBuilder builder = new StringBuilder();
        builder.append("浜夎绫诲瀷锛?).append(reasonLabel(arbitration.getReason()));
        if (orderDTO != null && orderDTO.getAmount() != null) {
            builder.append("锛涜鍗曢噾棰濓細").append(orderDTO.getAmount().toPlainString());
        }
        if (!requests.isEmpty()) {
            builder.append("锛涘綋鍓嶅瓨鍦ㄨˉ璇佽姹?").append(requests.size()).append(" 鏉?);
        }
        return builder.toString();
    }

    private String resolveProductImage(ProductDTO productDTO) {
        if (productDTO == null) return "";
        if (StringUtils.hasText(productDTO.getCoverImage())) return productDTO.getCoverImage();
        if (productDTO.getImageUrls() != null && !productDTO.getImageUrls().isEmpty()) {
            return productDTO.getImageUrls().get(0);
        }
        return "";
    }

    private String stripRejectPrefix(String result) {
        if (!StringUtils.hasText(result)) return "";
        String text = result.trim();
        String prefix = "椹冲洖鍘熷洜锛?;
        return text.startsWith(prefix) ? text.substring(prefix.length()) : text;
    }

    private String timelineTitle(String action) {
        if (!StringUtils.hasText(action)) return "澶勭悊璁板綍";
        String a = action.trim().toUpperCase(Locale.ROOT);
        if ("SUBMIT".equals(a)) return "鍙戣捣浠茶";
        if ("ACCEPT".equals(a)) return "骞冲彴鍙楃悊";
        if ("SUPPLEMENT_SUBMIT".equals(a) || "SUPPLEMENT_COMPLETE".equals(a) || "UPDATE".equals(a)) return "琛ュ厖璇佹嵁";
        if ("PROCESSING".equals(a) || "SUPPLEMENT_REQUEST".equals(a) || "SUPPLEMENT_EXPIRED".equals(a)) return "骞冲彴澶勭悊涓?;
        if ("COMPLETE".equals(a) || "RESOLVE".equals(a)) return "妗堜欢瀹岀粨";
        if ("REJECT".equals(a) || "CANCEL".equals(a)) return "妗堜欢椹冲洖";
        return "澶勭悊璁板綍";
    }

    private String timelineColor(String action) {
        if (!StringUtils.hasText(action)) return "#909399";
        String a = action.trim().toUpperCase(Locale.ROOT);
        if ("SUBMIT".equals(a)) return "#E6A23C";
        if ("ACCEPT".equals(a) || "PROCESSING".equals(a) || "SUPPLEMENT_REQUEST".equals(a)
                || "SUPPLEMENT_SUBMIT".equals(a) || "SUPPLEMENT_COMPLETE".equals(a) || "SUPPLEMENT_EXPIRED".equals(a)) {
            return "#409EFF";
        }
        if ("COMPLETE".equals(a) || "RESOLVE".equals(a)) return "#67C23A";
        if ("REJECT".equals(a) || "CANCEL".equals(a)) return "#F56C6C";
        return "#909399";
    }

    private String firstText(String... values) {
        if (values == null) return "";
        for (String value : values) {
            if (StringUtils.hasText(value)) return value.trim();
        }
        return "";
    }

    private ArbitrationSupplementRequestEntity resolveReq(Long arbitrationId, Long requestId, String role) {
        if (requestId != null) {
            ArbitrationSupplementRequestEntity r = supplementRequestService.getById(requestId);
            if (r == null || !Objects.equals(r.getArbitrationId(), arbitrationId)) throw new BusinessException("琛ヨ瘉璇锋眰涓嶅瓨鍦?);
            if (!Objects.equals(r.getStatus(), SR_PENDING)) throw new BusinessException("璇ヨˉ璇佽姹傚凡缁撴潫");
            if (!targetMatch(r.getTargetParty(), role)) throw new BusinessException("褰撳墠鐢ㄦ埛涓嶅湪璇ヨˉ璇佽姹傚璞′腑");
            return r;
        }
        return supplementRequestService.listPendingByArbitrationId(arbitrationId).stream().filter(x -> targetMatch(x.getTargetParty(), role)).findFirst().orElseThrow(() -> new BusinessException("褰撳墠娌℃湁寰呮彁浜ょ殑琛ヨ瘉璇锋眰"));
    }

    private boolean reqSatisfied(ArbitrationSupplementRequestEntity req) {
        List<ArbitrationEvidenceSubmissionEntity> list = evidenceSubmissionService.listByRequestId(req.getId());
        boolean b = list.stream().anyMatch(x -> BUYER.equalsIgnoreCase(x.getSubmitterRole()));
        boolean s = list.stream().anyMatch(x -> SELLER.equalsIgnoreCase(x.getSubmitterRole()));
        String t = normalizeTarget(req.getTargetParty());
        if ("BOTH".equals(t)) return b && s;
        if ("BUYER".equals(t)) return b;
        return s;
    }

    private boolean targetMatch(String target, String role) { String t = normalizeTarget(target); return "BOTH".equals(t) ? (BUYER.equals(role) || SELLER.equals(role)) : t.equals(role); }

    private String normalizeTarget(String target) {
        String t = StringUtils.hasText(target) ? target.trim().toUpperCase(Locale.ROOT) : "BOTH";
        if (!Arrays.asList("BUYER", "SELLER", "BOTH").contains(t)) throw new BusinessException("琛ヨ瘉瀵硅薄鍙敮鎸?BUYER / SELLER / BOTH");
        return t;
    }

    private boolean hasContent(SupplementSubmitDTO dto) {
        return StringUtils.hasText(dto.getClaim()) || StringUtils.hasText(dto.getFacts()) || (dto.getEvidenceUrls() != null && !dto.getEvidenceUrls().isEmpty()) || StringUtils.hasText(dto.getNote());
    }

    private String resolveRole(ArbitrationEntity e, Long userId) {
        OrderDTO o = fetchOrder(e.getOrderId());
        if (o != null) {
            if (Objects.equals(o.getBuyerId(), userId)) return BUYER;
            if (Objects.equals(o.getSellerId(), userId)) return SELLER;
        }
        return Objects.equals(userId, e.getApplicantId()) ? BUYER : SELLER;
    }

    private ArbitrationSupplementRequestVO toRequestVO(ArbitrationSupplementRequestEntity e) {
        ArbitrationSupplementRequestVO v = new ArbitrationSupplementRequestVO();
        v.setId(e.getId()); v.setArbitrationId(e.getArbitrationId()); v.setRequestedBy(e.getRequestedBy()); v.setTargetParty(e.getTargetParty());
        v.setRequiredItems(e.getRequiredItems()); v.setRemark(e.getRemark()); v.setDueTime(e.getDueTime()); v.setStatus(e.getStatus()); v.setStatusDesc(reqStatusDesc(e.getStatus())); v.setCreateTime(e.getCreateTime());
        return v;
    }

    private String reqStatusDesc(Integer s) { if (Objects.equals(s, SR_PENDING)) return "寰呰ˉ璇?; if (Objects.equals(s, SR_SUBMITTED)) return "宸叉弧瓒?; if (Objects.equals(s, SR_EXPIRED)) return "宸茶秴鏃?; if (Objects.equals(s, SR_CANCELED)) return "宸插彇娑?; return "鏈煡"; }

    private List<ArbitrationEvidenceBundleVO> buildBundles(ArbitrationEntity a, List<ArbitrationEvidenceSubmissionEntity> subs) {
        List<ArbitrationEvidenceSubmissionEntity> list = new ArrayList<>(subs == null ? Collections.emptyList() : subs);
        if (list.isEmpty() && StringUtils.hasText(a.getEvidence())) {
            ArbitrationEvidenceSubmissionEntity legacy = new ArbitrationEvidenceSubmissionEntity();
            legacy.setId(0L); legacy.setArbitrationId(a.getId()); legacy.setSubmitterId(a.getApplicantId()); legacy.setSubmitterRole(BUYER);
            legacy.setClaimText(a.getReason()); legacy.setFactText(a.getDescription()); legacy.setEvidenceUrls(normalizeEvidenceJson(a.getEvidence())); legacy.setIsLate(0); legacy.setCreateTime(a.getCreateTime());
            list.add(legacy);
        }
        Map<String, List<ArbitrationEvidenceSubmissionEntity>> g = list.stream().collect(Collectors.groupingBy(x -> {
            String r = StringUtils.hasText(x.getSubmitterRole()) ? x.getSubmitterRole().toUpperCase(Locale.ROOT) : SYSTEM;
            return (BUYER.equals(r) || SELLER.equals(r) || SYSTEM.equals(r)) ? r : SYSTEM;
        }, LinkedHashMap::new, Collectors.toList()));
        List<ArbitrationEvidenceBundleVO> bundles = new ArrayList<>();
        for (String r : Arrays.asList(BUYER, SELLER, SYSTEM)) {
            ArbitrationEvidenceBundleVO b = new ArbitrationEvidenceBundleVO(); b.setRole(r);
            b.setSubmissions(g.getOrDefault(r, Collections.emptyList()).stream().map(this::toSubVO).collect(Collectors.toList()));
            bundles.add(b);
        }
        return bundles;
    }

    private ArbitrationEvidenceSubmissionVO toSubVO(ArbitrationEvidenceSubmissionEntity e) {
        ArbitrationEvidenceSubmissionVO v = new ArbitrationEvidenceSubmissionVO();
        v.setId(e.getId()); v.setSupplementRequestId(e.getSupplementRequestId()); v.setSubmitterId(e.getSubmitterId()); v.setSubmitterRole(e.getSubmitterRole());
        v.setClaim(e.getClaimText()); v.setFacts(e.getFactText()); v.setEvidenceUrls(parseJson(e.getEvidenceUrls())); v.setNote(e.getNote()); v.setLate(Objects.equals(e.getIsLate(), 1)); v.setCreateTime(e.getCreateTime());
        return v;
    }

    private ArbitrationSystemContextVO buildSystem(ArbitrationEntity a) {
        ArbitrationSystemContextVO v = new ArbitrationSystemContextVO();
        Map<String, Object> order = new LinkedHashMap<>();
        Map<String, Object> product = new LinkedHashMap<>();
        Map<String, Object> chat = new LinkedHashMap<>();
        OrderDTO o = fetchOrder(a.getOrderId());
        if (o != null) {
            order.put("orderId", o.getId()); order.put("orderNo", o.getOrderNo()); order.put("status", o.getStatus()); order.put("buyerId", o.getBuyerId()); order.put("sellerId", o.getSellerId()); order.put("amount", o.getAmount()); order.put("createTime", o.getCreateTime()); order.put("updateTime", o.getUpdateTime()); order.put("snapshotAt", LocalDateTime.now());
            ProductDTO p = fetchProduct(o.getProductId());
            if (p != null) {
                product.put("productId", p.getId()); product.put("title", p.getTitle()); product.put("description", p.getDescription()); product.put("price", p.getPrice()); product.put("sellerId", p.getSellerId()); product.put("imageUrls", p.getImageUrls()); product.put("status", p.getStatus()); product.put("snapshotAt", LocalDateTime.now());
            }
            LocalDateTime start = o.getCreateTime() == null ? LocalDateTime.now().minusDays(7) : o.getCreateTime().minusDays(1);
            chat.put("scope", "ORDER_CONVERSATION_WINDOW"); chat.put("windowStart", start); chat.put("windowEnd", LocalDateTime.now()); chat.put("source", "MESSAGE_SERVICE_PENDING_INTEGRATION"); chat.put("note", "褰撳墠鐗堟湰浠呮彁渚涗細璇濊寖鍥村厓淇℃伅锛岃亰澶╂鏂囧皢鐢辨秷鎭湇鍔¤仛鍚堟帴鍙ｈˉ榻?);
        } else {
            order.put("snapshotAt", LocalDateTime.now()); order.put("note", "璁㈠崟蹇収鎷夊彇澶辫触鎴栬鍗曚笉瀛樺湪");
            chat.put("scope", "ORDER_CONVERSATION_WINDOW"); chat.put("source", "MESSAGE_SERVICE_PENDING_INTEGRATION"); chat.put("note", "缂哄皯璁㈠崟淇℃伅锛屾棤娉曡绠椾細璇濇椂闂寸獥");
        }
        v.setOrderSnapshot(order); v.setProductSnapshot(product); v.setChatContext(chat);
        return v;
    }

    private OrderDTO fetchOrder(Long orderId) {
        if (orderId == null) return null;
        try {
            Result<OrderDTO> r = tradeFeignClient.getOrderById(orderId);
            if (r != null && r.isSuccess()) return r.getData();
        } catch (Exception e) { log.warn("fetch order failed, orderId={}", orderId, e); }
        return null;
    }

    private ProductDTO fetchProduct(Long productId) {
        if (productId == null) return null;
        try {
            Result<ProductDTO> r = productFeignClient.getProductById(productId);
            if (r != null && r.isSuccess()) return r.getData();
        } catch (Exception e) { log.warn("fetch product failed, productId={}", productId, e); }
        return null;
    }

    private void log(Long arbitrationId, Long operatorId, String action, String remark) {
        ArbitrationLogEntity e = new ArbitrationLogEntity();
        e.setArbitrationId(arbitrationId); e.setOperatorId(operatorId); e.setAction(action); e.setRemark(remark); arbitrationLogService.save(e);
    }

    private double avgHandleDays() {
        List<ArbitrationEntity> list = this.list(new QueryWrapper<ArbitrationEntity>().eq("status", COMPLETED));
        if (list.isEmpty()) return 0.0;
        double total = 0.0; int valid = 0;
        for (ArbitrationEntity e : list) {
            if (e.getCreateTime() == null || e.getUpdateTime() == null) continue;
            long hours = ChronoUnit.HOURS.between(e.getCreateTime(), e.getUpdateTime());
            total += Math.max(0, hours) / 24.0; valid++;
        }
        return valid == 0 ? 0.0 : Math.round((total / valid) * 100.0) / 100.0;
    }

    private String toJson(List<String> arr) {
        List<String> v = arr == null ? Collections.emptyList() : arr.stream().filter(StringUtils::hasText).map(String::trim).collect(Collectors.toList());
        try { return om.writeValueAsString(v); } catch (Exception e) { return "[]"; }
    }

    private List<String> parseJson(String s) {
        if (!StringUtils.hasText(s)) return Collections.emptyList();
        try { return om.readValue(s, new TypeReference<List<String>>() {}); } catch (Exception e) { return Collections.singletonList(s); }
    }

    private String normalizeEvidenceJson(String evidence) { return StringUtils.hasText(evidence) ? toJson(parseJson(evidence)) : "[]"; }
}

```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/AdminArbitrationDetailVO.java
```java
package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AdminArbitrationDetailVO {

    private Long id;

    private String caseNumber;

    private Integer status;

    private String statusLabel;

    private String reason;

    private String reasonLabel;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long orderId;

    private String orderNo;

    private BigDecimal orderAmount;

    private Long productId;

    private String productName;

    private BigDecimal productPrice;

    private String productImage;

    private Long sourceDisputeId;

    private Long applicantId;

    private String applicantName;

    private Long respondentId;

    private String respondentName;

    private Long handlerId;

    private String handlerName;

    private String buyerClaim;

    private String sellerClaim;

    private String platformFocus;

    private String arbitrationRequest;

    private String negotiationSummary;

    private List<ArbitrationEvidenceVO> applicantEvidence = new ArrayList<>();

    private List<ArbitrationEvidenceVO> respondentEvidence = new ArrayList<>();

    private List<ArbitrationEvidenceVO> systemEvidence = new ArrayList<>();

    private List<ArbitrationChatSummaryVO> chatSummary = new ArrayList<>();

    private Map<String, Object> orderSnapshot = new LinkedHashMap<>();

    private Map<String, Object> productSnapshot = new LinkedHashMap<>();

    private List<ArbitrationTimelineVO> timeline = new ArrayList<>();

    private String decisionRemark;

    private String rejectReason;

    private Boolean canAccept;

    private Boolean canComplete;

    private Boolean canReject;

    private Boolean readonly;
}

```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/controller/DisputeController.java
```java
package org.shyu.marketservicearbitration.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicearbitration.dto.BuyerConfirmProposalDTO;
import org.shyu.marketservicearbitration.dto.DisputeCreateDTO;
import org.shyu.marketservicearbitration.dto.DisputeEscalateDTO;
import org.shyu.marketservicearbitration.dto.SellerDisputeResponseDTO;
import org.shyu.marketservicearbitration.service.IDisputeService;
import org.shyu.marketservicearbitration.vo.DisputeDetailVO;
import org.shyu.marketservicearbitration.vo.DisputeListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Api(tags = "浜夎鍗忓晢鏈嶅姟")
@Slf4j
@RestController
@RequestMapping("/dispute")
@Validated
public class DisputeController {

    @Autowired
    private IDisputeService disputeService;

    @ApiOperation("涔板鍙戣捣浜夎鐢宠")
    @PostMapping("/create")
    @SaCheckLogin
    public Result<?> createDispute(@Valid @RequestBody DisputeCreateDTO dto) {
        Long buyerId = StpUtil.getLoginIdAsLong();
        try {
            Long disputeId = disputeService.createDispute(dto, buyerId);
            return Result.success("浜夎鐢宠鍒涘缓鎴愬姛", disputeId);
        } catch (Exception e) {
            log.error("鍒涘缓浜夎鐢宠澶辫触", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("涔板鎴戠殑浜夎鍒楄〃")
    @GetMapping("/my")
    @SaCheckLogin
    public Result<?> getMyDisputes(@RequestParam(defaultValue = "1") Integer current,
                                   @RequestParam(defaultValue = "10") Integer size) {
        Long buyerId = StpUtil.getLoginIdAsLong();
        try {
            IPage<DisputeListItemVO> page = disputeService.getBuyerDisputeList(buyerId, current, size);
            return Result.success(page);
        } catch (Exception e) {
            log.error("鏌ヨ涔板浜夎鍒楄〃澶辫触", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("浜夎璇︽儏")
    @GetMapping("/detail/{id}")
    @SaCheckLogin
    public Result<?> getDisputeDetail(@PathVariable("id") Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        try {
            DisputeDetailVO detailVO = disputeService.getDisputeDetail(id, currentUserId);
            return Result.success(detailVO);
        } catch (Exception e) {
            log.error("鏌ヨ浜夎璇︽儏澶辫触", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("鍗囩骇浠茶")
    @PostMapping("/escalate")
    @SaCheckLogin
    public Result<?> escalateToArbitration(@Valid @RequestBody DisputeEscalateDTO dto) {
        Long buyerId = StpUtil.getLoginIdAsLong();
        try {
            Long arbitrationId = disputeService.escalateToArbitration(dto, buyerId);
            return Result.success("鍗囩骇浠茶鎴愬姛", arbitrationId);
        } catch (Exception e) {
            log.error("鍗囩骇浠茶澶辫触", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("鍗栧寰呭搷搴斾簤璁垪琛?)
    @GetMapping("/seller/pending")
    @SaCheckLogin
    public Result<?> getSellerPendingDisputes(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size) {
        Long sellerId = StpUtil.getLoginIdAsLong();
        try {
            IPage<DisputeListItemVO> page = disputeService.getSellerPendingDisputes(sellerId, current, size);
            return Result.success(page);
        } catch (Exception e) {
            log.error("鏌ヨ鍗栧寰呭搷搴斾簤璁け璐?, e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("鍗栧鍝嶅簲浜夎")
    @PostMapping("/seller/respond")
    @SaCheckLogin
    public Result<?> sellerRespond(@Valid @RequestBody SellerDisputeResponseDTO dto) {
        Long sellerId = StpUtil.getLoginIdAsLong();
        try {
            Boolean success = disputeService.sellerRespond(dto, sellerId);
            return success ? Result.success("鍗栧鍝嶅簲鎴愬姛", null) : Result.error("鍗栧鍝嶅簲澶辫触");
        } catch (Exception e) {
            log.error("鍗栧鍝嶅簲浜夎澶辫触", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("涔板纭鍗栧鏇夸唬鏂规")
    @PostMapping("/buyer/confirm-proposal")
    @SaCheckLogin
    public Result<?> buyerConfirmProposal(@Valid @RequestBody BuyerConfirmProposalDTO dto) {
        Long buyerId = StpUtil.getLoginIdAsLong();
        try {
            Boolean success = disputeService.buyerConfirmProposal(dto, buyerId);
            return success ? Result.success("涔板纭鎴愬姛", null) : Result.error("涔板纭澶辫触");
        } catch (Exception e) {
            log.error("涔板纭鍗栧鏂规澶辫触", e);
            return Result.error(e.getMessage());
        }
    }

    @ApiOperation("绯荤粺妫€鏌ヤ簤璁秴鏃?)
    @PostMapping("/system/check-timeout")
    @SaCheckLogin
    public Result<?> checkTimeout() {
        try {
            Integer changed = disputeService.checkAndMarkTimeout();
            return Result.success("瓒呮椂妫€鏌ュ畬鎴?, changed);
        } catch (Exception e) {
            log.error("浜夎瓒呮椂妫€鏌ュけ璐?, e);
            return Result.error(e.getMessage());
        }
    }
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/dto/BuyerConfirmProposalDTO.java
```java
package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("涔板纭鍗栧鏂规DTO")
public class BuyerConfirmProposalDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "disputeId涓嶈兘涓虹┖")
    @ApiModelProperty(value = "浜夎ID", required = true)
    private Long disputeId;

    @NotNull(message = "acceptProposal涓嶈兘涓虹┖")
    @ApiModelProperty(value = "鏄惁鎺ュ彈鍗栧鏂规", required = true)
    private Boolean acceptProposal;

    @ApiModelProperty("鎷掔粷鍘熷洜鎴栬ˉ鍏呰鏄?)
    private String remark;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/dto/DisputeCreateDTO.java
```java
package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("涔板鍙戣捣浜夎DTO")
public class DisputeCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "orderId涓嶈兘涓虹┖")
    @ApiModelProperty(value = "璁㈠崟ID", required = true)
    private Long orderId;

    @ApiModelProperty("鍟嗗搧ID锛屽彲涓嶄紶")
    private Long productId;

    @NotBlank(message = "reason涓嶈兘涓虹┖")
    @ApiModelProperty(value = "浜夎鍘熷洜", required = true)
    private String reason;

    @NotBlank(message = "factDescription涓嶈兘涓虹┖")
    @ApiModelProperty(value = "浜嬪疄璇存槑", required = true)
    private String factDescription;

    @NotBlank(message = "requestType涓嶈兘涓虹┖")
    @ApiModelProperty(value = "璇夋眰绫诲瀷", required = true)
    private String requestType;

    @NotBlank(message = "requestDescription涓嶈兘涓虹┖")
    @ApiModelProperty(value = "璇夋眰璇存槑", required = true)
    private String requestDescription;

    @DecimalMin(value = "0.00", message = "expectedAmount涓嶈兘灏忎簬0")
    @ApiModelProperty("鏈熸湜閲戦")
    private BigDecimal expectedAmount;

    @Valid
    @ApiModelProperty("璇佹嵁鍒楄〃")
    private List<EvidenceItemDTO> evidenceList = new ArrayList<>();

    @Data
    public static class EvidenceItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @ApiModelProperty("璇佹嵁绫诲瀷")
        private String evidenceType;

        @ApiModelProperty("鏍囬")
        private String title;

        @ApiModelProperty("璇存槑")
        private String description;

        @NotBlank(message = "fileUrl涓嶈兘涓虹┖")
        @ApiModelProperty(value = "鏂囦欢URL", required = true)
        private String fileUrl;

        @ApiModelProperty("缂╃暐鍥綰RL")
        private String thumbnailUrl;
    }
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/dto/DisputeEscalateDTO.java
```java
package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@ApiModel("鍗囩骇浠茶DTO")
public class DisputeEscalateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "disputeId涓嶈兘涓虹┖")
    @ApiModelProperty(value = "浜夎ID", required = true)
    private Long disputeId;

    @ApiModelProperty("鍗囩骇鍘熷洜")
    private String escalateReason;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/dto/SellerDisputeResponseDTO.java
```java
package org.shyu.marketservicearbitration.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ApiModel("鍗栧鍝嶅簲浜夎DTO")
public class SellerDisputeResponseDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "disputeId涓嶈兘涓虹┖")
    @ApiModelProperty(value = "浜夎ID", required = true)
    private Long disputeId;

    @NotBlank(message = "responseType涓嶈兘涓虹┖")
    @ApiModelProperty(value = "鍝嶅簲绫诲瀷: AGREE/REJECT/PROPOSE", required = true)
    private String responseType;

    @ApiModelProperty("鍝嶅簲璇存槑")
    private String responseDescription;

    @ApiModelProperty("鏇夸唬鏂规绫诲瀷")
    private String proposalType;

    @DecimalMin(value = "0.00", message = "proposalAmount涓嶈兘灏忎簬0")
    @ApiModelProperty("鏇夸唬鏂规閲戦")
    private BigDecimal proposalAmount;

    @ApiModelProperty("鏇夸唬鏂规璇存槑")
    private String proposalDescription;

    @ApiModelProperty("杩愯垂鎵挎媴鏂?)
    private String freightBearer;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/entity/DisputeEvidenceEntity.java
```java
package org.shyu.marketservicearbitration.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_dispute_evidence")
public class DisputeEvidenceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("biz_type")
    private String bizType;

    @TableField("biz_id")
    private Long bizId;

    @TableField("uploader_id")
    private Long uploaderId;

    @TableField("uploader_role")
    private String uploaderRole;

    @TableField("evidence_type")
    private String evidenceType;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("file_url")
    private String fileUrl;

    @TableField("thumbnail_url")
    private String thumbnailUrl;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/entity/DisputeNegotiationLogEntity.java
```java
package org.shyu.marketservicearbitration.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_dispute_negotiation_log")
public class DisputeNegotiationLogEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("dispute_id")
    private Long disputeId;

    @TableField("round_no")
    private Integer roundNo;

    @TableField("actor_id")
    private Long actorId;

    @TableField("actor_role")
    private String actorRole;

    @TableField("action_type")
    private String actionType;

    @TableField("content")
    private String content;

    @TableField("amount")
    private BigDecimal amount;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/entity/DisputeRequestEntity.java
```java
package org.shyu.marketservicearbitration.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_dispute_request")
public class DisputeRequestEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("product_id")
    private Long productId;

    @TableField("buyer_id")
    private Long buyerId;

    @TableField("seller_id")
    private Long sellerId;

    @TableField("reason")
    private String reason;

    @TableField("fact_description")
    private String factDescription;

    @TableField("request_type")
    private String requestType;

    @TableField("request_description")
    private String requestDescription;

    @TableField("expected_amount")
    private BigDecimal expectedAmount;

    @TableField("status")
    private String status;

    @TableField("current_round")
    private Integer currentRound;

    @TableField("seller_response_type")
    private String sellerResponseType;

    @TableField("seller_response_description")
    private String sellerResponseDescription;

    @TableField("seller_response_proposal_type")
    private String sellerResponseProposalType;

    @TableField("seller_response_amount")
    private BigDecimal sellerResponseAmount;

    @TableField("seller_response_freight_bearer")
    private String sellerResponseFreightBearer;

    @TableField("escalated_arbitration_id")
    private Long escalatedArbitrationId;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/enums/DisputeActorRoleEnum.java
```java
package org.shyu.marketservicearbitration.enums;

import lombok.Getter;

@Getter
public enum DisputeActorRoleEnum {
    BUYER("BUYER"),
    SELLER("SELLER"),
    SYSTEM("SYSTEM"),
    ADMIN("ADMIN");

    private final String code;

    DisputeActorRoleEnum(String code) {
        this.code = code;
    }
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/enums/DisputeNegotiationActionEnum.java
```java
package org.shyu.marketservicearbitration.enums;

import lombok.Getter;

@Getter
public enum DisputeNegotiationActionEnum {
    BUYER_CREATE("BUYER_CREATE", "涔板鍙戣捣浜夎"),
    SELLER_AGREE("SELLER_AGREE", "鍗栧鍚屾剰涔板璇夋眰"),
    SELLER_REJECT("SELLER_REJECT", "鍗栧鎷掔粷涔板璇夋眰"),
    SELLER_PROPOSE("SELLER_PROPOSE", "鍗栧鎻愬嚭鏇夸唬鏂规"),
    BUYER_ACCEPT_PROPOSAL("BUYER_ACCEPT_PROPOSAL", "涔板鎺ュ彈鍗栧鏂规"),
    BUYER_REJECT_PROPOSAL("BUYER_REJECT_PROPOSAL", "涔板鎷掔粷鍗栧鏂规"),
    SELLER_TIMEOUT("SELLER_TIMEOUT", "鍗栧瓒呮椂鏈搷搴?),
    BUYER_CONFIRM_TIMEOUT("BUYER_CONFIRM_TIMEOUT", "涔板纭瓒呮椂"),
    SELLER_LATE_RESPONSE("SELLER_LATE_RESPONSE", "鍗栧瓒呮椂鍚庤ˉ鍝嶅簲"),
    ESCALATE_TO_ARBITRATION("ESCALATE_TO_ARBITRATION", "鍗囩骇浠茶"),
    SYSTEM_AUTO_EXECUTE("SYSTEM_AUTO_EXECUTE", "绯荤粺鑷姩鎵ц鍗忓晢缁撴灉");

    private final String code;
    private final String label;

    DisputeNegotiationActionEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(String code) {
        for (DisputeNegotiationActionEnum value : values()) {
            if (value.code.equals(code)) {
                return value.label;
            }
        }
        return code;
    }
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/enums/DisputeStatusEnum.java
```java
package org.shyu.marketservicearbitration.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum DisputeStatusEnum {
    INIT("INIT", "宸插垱寤?),
    WAIT_SELLER_RESPONSE("WAIT_SELLER_RESPONSE", "寰呭崠瀹跺搷搴?),
    SELLER_PROPOSED("SELLER_PROPOSED", "鍗栧宸叉彁鍑烘柟妗?),
    WAIT_BUYER_CONFIRM("WAIT_BUYER_CONFIRM", "寰呬拱瀹剁‘璁?),
    NEGOTIATION_SUCCESS("NEGOTIATION_SUCCESS", "鍗忓晢鎴愬姛"),
    NEGOTIATION_FAILED("NEGOTIATION_FAILED", "鍗忓晢澶辫触"),
    SELLER_TIMEOUT("SELLER_TIMEOUT", "鍗栧瓒呮椂鏈搷搴?),
    ESCALATED_TO_ARBITRATION("ESCALATED_TO_ARBITRATION", "宸插崌绾т徊瑁?),
    CLOSED("CLOSED", "宸插叧闂?);

    private static final Set<String> SELLER_RESPONDABLE_STATUSES = new HashSet<>(Arrays.asList(
            WAIT_SELLER_RESPONSE.code,
            SELLER_TIMEOUT.code
    ));

    private static final Set<String> ESCALATABLE_STATUSES = new HashSet<>(Arrays.asList(
            NEGOTIATION_FAILED.code,
            SELLER_TIMEOUT.code,
            WAIT_BUYER_CONFIRM.code
    ));

    private final String code;
    private final String label;

    DisputeStatusEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean canSellerRespond(String status) {
        return SELLER_RESPONDABLE_STATUSES.contains(status);
    }

    public static boolean canEscalate(String status) {
        return ESCALATABLE_STATUSES.contains(status);
    }

    public static String getLabel(String code) {
        for (DisputeStatusEnum value : values()) {
            if (value.code.equals(code)) {
                return value.label;
            }
        }
        return code;
    }
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/enums/SellerResponseTypeEnum.java
```java
package org.shyu.marketservicearbitration.enums;

import lombok.Getter;

@Getter
public enum SellerResponseTypeEnum {
    AGREE("AGREE", "鍚屾剰涔板璇夋眰"),
    REJECT("REJECT", "鎷掔粷涔板璇夋眰"),
    PROPOSE("PROPOSE", "鎻愬嚭鏇夸唬鏂规");

    private final String code;
    private final String label;

    SellerResponseTypeEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean contains(String code) {
        for (SellerResponseTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/mapper/DisputeEvidenceMapper.java
```java
package org.shyu.marketservicearbitration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shyu.marketservicearbitration.entity.DisputeEvidenceEntity;

@Mapper
public interface DisputeEvidenceMapper extends BaseMapper<DisputeEvidenceEntity> {
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/mapper/DisputeNegotiationLogMapper.java
```java
package org.shyu.marketservicearbitration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shyu.marketservicearbitration.entity.DisputeNegotiationLogEntity;

@Mapper
public interface DisputeNegotiationLogMapper extends BaseMapper<DisputeNegotiationLogEntity> {
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/mapper/DisputeRequestMapper.java
```java
package org.shyu.marketservicearbitration.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.shyu.marketservicearbitration.entity.DisputeRequestEntity;

@Mapper
public interface DisputeRequestMapper extends BaseMapper<DisputeRequestEntity> {
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/service/IDisputeService.java
```java
package org.shyu.marketservicearbitration.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.shyu.marketservicearbitration.dto.BuyerConfirmProposalDTO;
import org.shyu.marketservicearbitration.dto.DisputeCreateDTO;
import org.shyu.marketservicearbitration.dto.DisputeEscalateDTO;
import org.shyu.marketservicearbitration.dto.SellerDisputeResponseDTO;
import org.shyu.marketservicearbitration.entity.DisputeRequestEntity;
import org.shyu.marketservicearbitration.vo.DisputeChatSummaryVO;
import org.shyu.marketservicearbitration.vo.DisputeDetailVO;
import org.shyu.marketservicearbitration.vo.DisputeListItemVO;

import java.time.LocalDateTime;
import java.util.List;

public interface IDisputeService extends IService<DisputeRequestEntity> {

    Long createDispute(DisputeCreateDTO dto, Long buyerId);

    IPage<DisputeListItemVO> getBuyerDisputeList(Long buyerId, Integer current, Integer size);

    IPage<DisputeListItemVO> getSellerPendingDisputes(Long sellerId, Integer current, Integer size);

    DisputeDetailVO getDisputeDetail(Long disputeId, Long currentUserId);

    Boolean sellerRespond(SellerDisputeResponseDTO dto, Long sellerId);

    Boolean buyerConfirmProposal(BuyerConfirmProposalDTO dto, Long buyerId);

    Long escalateToArbitration(DisputeEscalateDTO dto, Long buyerId);

    Integer checkAndMarkTimeout();

    String buildNegotiationSummary(Long disputeId);

    List<DisputeChatSummaryVO> buildChatSummaryByOrder(Long orderId, Long buyerId, Long sellerId,
                                                        LocalDateTime startTime, LocalDateTime endTime);
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/service/impl/DisputeServiceImpl.java
```java
package org.shyu.marketservicearbitration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapitrade.dto.OrderDTO;
import org.shyu.marketapitrade.feign.TradeFeignClient;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketcommon.result.Result;
import org.shyu.marketservicearbitration.dto.BuyerConfirmProposalDTO;
import org.shyu.marketservicearbitration.dto.DisputeCreateDTO;
import org.shyu.marketservicearbitration.dto.DisputeEscalateDTO;
import org.shyu.marketservicearbitration.dto.SellerDisputeResponseDTO;
import org.shyu.marketservicearbitration.entity.ArbitrationEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationEvidenceSubmissionEntity;
import org.shyu.marketservicearbitration.entity.ArbitrationLogEntity;
import org.shyu.marketservicearbitration.entity.DisputeEvidenceEntity;
import org.shyu.marketservicearbitration.entity.DisputeNegotiationLogEntity;
import org.shyu.marketservicearbitration.entity.DisputeRequestEntity;
import org.shyu.marketservicearbitration.enums.DisputeActorRoleEnum;
import org.shyu.marketservicearbitration.enums.DisputeNegotiationActionEnum;
import org.shyu.marketservicearbitration.enums.DisputeStatusEnum;
import org.shyu.marketservicearbitration.enums.SellerResponseTypeEnum;
import org.shyu.marketservicearbitration.mapper.ArbitrationMapper;
import org.shyu.marketservicearbitration.mapper.DisputeEvidenceMapper;
import org.shyu.marketservicearbitration.mapper.DisputeNegotiationLogMapper;
import org.shyu.marketservicearbitration.mapper.DisputeRequestMapper;
import org.shyu.marketservicearbitration.service.IArbitrationEvidenceSubmissionService;
import org.shyu.marketservicearbitration.service.IArbitrationLogService;
import org.shyu.marketservicearbitration.service.IDisputeService;
import org.shyu.marketservicearbitration.vo.DisputeChatSummaryVO;
import org.shyu.marketservicearbitration.vo.DisputeDetailVO;
import org.shyu.marketservicearbitration.vo.DisputeEvidenceVO;
import org.shyu.marketservicearbitration.vo.DisputeListItemVO;
import org.shyu.marketservicearbitration.vo.DisputeNegotiationLogVO;
import org.shyu.marketservicearbitration.vo.SellerProposalVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DisputeServiceImpl extends ServiceImpl<DisputeRequestMapper, DisputeRequestEntity>
        implements IDisputeService {

    private static final int ARBITRATION_PENDING = 0;
    private static final int SELLER_RESPONSE_TIMEOUT_HOURS = 24;
    private static final int BUYER_CONFIRM_TIMEOUT_HOURS = 24;
    private static final BigDecimal HIGH_AMOUNT_ESCALATE_THRESHOLD = new BigDecimal("5000.00");
    private static final String EVIDENCE_BIZ_DISPUTE = "DISPUTE";
    private static final String EVIDENCE_BIZ_ARBITRATION = "ARBITRATION";

    @Autowired
    private TradeFeignClient tradeFeignClient;

    @Autowired
    private DisputeEvidenceMapper disputeEvidenceMapper;

    @Autowired
    private DisputeNegotiationLogMapper disputeNegotiationLogMapper;

    @Autowired
    private ArbitrationMapper arbitrationMapper;

    @Autowired
    private IArbitrationLogService arbitrationLogService;

    @Autowired
    private IArbitrationEvidenceSubmissionService arbitrationEvidenceSubmissionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public Long createDispute(DisputeCreateDTO dto, Long buyerId) {
        checkAndMarkTimeout();
        OrderDTO orderDTO = fetchAndValidateOrder(dto.getOrderId(), buyerId);
        ensureNoActiveDispute(dto.getOrderId(), buyerId);

        DisputeRequestEntity dispute = new DisputeRequestEntity();
        dispute.setOrderId(orderDTO.getId());
        dispute.setProductId(dto.getProductId() != null ? dto.getProductId() : orderDTO.getProductId());
        dispute.setBuyerId(orderDTO.getBuyerId());
        dispute.setSellerId(orderDTO.getSellerId());
        dispute.setReason(dto.getReason().trim());
        dispute.setFactDescription(dto.getFactDescription().trim());
        dispute.setRequestType(dto.getRequestType().trim().toUpperCase(Locale.ROOT));
        dispute.setRequestDescription(dto.getRequestDescription().trim());
        dispute.setExpectedAmount(dto.getExpectedAmount() == null ? BigDecimal.ZERO : dto.getExpectedAmount());
        dispute.setStatus(DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode());
        dispute.setCurrentRound(1);
        dispute.setExpireTime(LocalDateTime.now().plusHours(SELLER_RESPONSE_TIMEOUT_HOURS));
        if (!save(dispute)) {
            throw new BusinessException("鍒涘缓浜夎鐢宠澶辫触");
        }

        saveDisputeEvidence(dispute.getId(), dto.getEvidenceList(), buyerId, DisputeActorRoleEnum.BUYER.getCode());
        recordLog(dispute.getId(), dispute.getCurrentRound(), buyerId, DisputeActorRoleEnum.BUYER.getCode(),
                DisputeNegotiationActionEnum.BUYER_CREATE.getCode(), "涔板鍙戣捣浜夎锛? + dispute.getFactDescription(), dispute.getExpectedAmount());

        if (hitHighRiskRule(dispute)) {
            dispute.setStatus(DisputeStatusEnum.NEGOTIATION_FAILED.getCode());
            dispute.setExpireTime(null);
            updateById(dispute);
            recordLog(dispute.getId(), dispute.getCurrentRound(), 0L, DisputeActorRoleEnum.SYSTEM.getCode(),
                    DisputeNegotiationActionEnum.SYSTEM_AUTO_EXECUTE.getCode(),
                    "鍛戒腑楂橀噾棰濊鍒欙紝寤鸿鐩存帴鍗囩骇浠茶", dispute.getExpectedAmount());
        }
        return dispute.getId();
    }

    @Override
    public IPage<DisputeListItemVO> getBuyerDisputeList(Long buyerId, Integer current, Integer size) {
        checkAndMarkTimeout();
        Page<DisputeRequestEntity> page = new Page<>(current, size);
        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", buyerId).orderByDesc("create_time");
        IPage<DisputeRequestEntity> entityPage = page(page, wrapper);
        return toListItemPage(entityPage);
    }

    @Override
    public IPage<DisputeListItemVO> getSellerPendingDisputes(Long sellerId, Integer current, Integer size) {
        checkAndMarkTimeout();
        Page<DisputeRequestEntity> page = new Page<>(current, size);
        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("seller_id", sellerId)
                .in("status", Arrays.asList(
                        DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode(),
                        DisputeStatusEnum.SELLER_TIMEOUT.getCode()))
                .orderByAsc("expire_time")
                .orderByDesc("create_time");
        IPage<DisputeRequestEntity> entityPage = page(page, wrapper);
        return toListItemPage(entityPage);
    }

    @Override
    public DisputeDetailVO getDisputeDetail(Long disputeId, Long currentUserId) {
        refreshSingleTimeout(disputeId);
        DisputeRequestEntity dispute = getById(disputeId);
        if (dispute == null) {
            throw new BusinessException("浜夎涓嶅瓨鍦?);
        }

        List<DisputeEvidenceEntity> evidenceEntities = listDisputeEvidence(disputeId);
        List<DisputeNegotiationLogEntity> logEntities = listNegotiationLog(disputeId);
        logEntities.sort(Comparator.comparing(DisputeNegotiationLogEntity::getCreateTime,
                Comparator.nullsLast(Comparator.naturalOrder())));

        DisputeDetailVO vo = new DisputeDetailVO();
        BeanUtils.copyProperties(dispute, vo);
        vo.setStatusLabel(DisputeStatusEnum.getLabel(dispute.getStatus()));
        vo.setCountdownSeconds(calcCountdownSeconds(dispute.getExpireTime()));
        vo.setEvidenceList(evidenceEntities.stream().map(this::toEvidenceVO).collect(Collectors.toList()));
        vo.setNegotiationLogs(logEntities.stream().map(this::toLogVO).collect(Collectors.toList()));
        vo.setNegotiationSummary(buildNegotiationSummary(disputeId));
        vo.setChatSummary(buildChatSummaryByOrder(dispute.getOrderId(), dispute.getBuyerId(), dispute.getSellerId(),
                dispute.getCreateTime(), LocalDateTime.now()));
        vo.setSellerProposal(buildSellerProposal(dispute));
        vo.setCanSellerRespond(DisputeStatusEnum.canSellerRespond(dispute.getStatus()));
        vo.setCanBuyerConfirm(DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode().equals(dispute.getStatus()));
        vo.setCanEscalate(DisputeStatusEnum.canEscalate(dispute.getStatus()));
        return vo;
    }

    @Override
    @Transactional
    public Boolean sellerRespond(SellerDisputeResponseDTO dto, Long sellerId) {
        checkAndMarkTimeout();
        DisputeRequestEntity dispute = getById(dto.getDisputeId());
        if (dispute == null) {
            throw new BusinessException("浜夎涓嶅瓨鍦?);
        }
        if (!Objects.equals(dispute.getSellerId(), sellerId)) {
            throw new BusinessException("鏃犳潈闄愬搷搴旇浜夎");
        }
        if (!DisputeStatusEnum.canSellerRespond(dispute.getStatus())) {
            throw new BusinessException("褰撳墠浜夎鐘舵€佷笉鍏佽鍗栧鍝嶅簲");
        }

        String responseType = normalizeUpper(dto.getResponseType());
        if (!SellerResponseTypeEnum.contains(responseType)) {
            throw new BusinessException("responseType浠呮敮鎸丄GREE/REJECT/PROPOSE");
        }

        Integer currentRound = dispute.getCurrentRound() == null ? 1 : dispute.getCurrentRound();
        boolean isLateResponse = DisputeStatusEnum.SELLER_TIMEOUT.getCode().equals(dispute.getStatus());
        dispute.setCurrentRound(currentRound + 1);
        dispute.setSellerResponseType(responseType);
        dispute.setSellerResponseDescription(trimToEmpty(dto.getResponseDescription()));
        dispute.setSellerResponseAmount(dto.getProposalAmount());
        dispute.setSellerResponseFreightBearer(trimToEmpty(dto.getFreightBearer()));
        dispute.setExpireTime(null);

        if (SellerResponseTypeEnum.AGREE.getCode().equals(responseType)) {
            dispute.setSellerResponseAmount(dispute.getExpectedAmount());
            dispute.setStatus(DisputeStatusEnum.NEGOTIATION_SUCCESS.getCode());
            if (StringUtils.hasText(dispute.getSellerResponseDescription())) {
                recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                        DisputeNegotiationActionEnum.SELLER_AGREE.getCode(),
                        dispute.getSellerResponseDescription(), dispute.getSellerResponseAmount());
            } else {
                recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                        DisputeNegotiationActionEnum.SELLER_AGREE.getCode(),
                        "鍗栧鍚屾剰涔板璇夋眰", dispute.getSellerResponseAmount());
            }
            executeNegotiationResult(dispute, dispute.getRequestType(), dispute.getExpectedAmount(), dispute.getRequestDescription());
        } else if (SellerResponseTypeEnum.REJECT.getCode().equals(responseType)) {
            dispute.setStatus(DisputeStatusEnum.NEGOTIATION_FAILED.getCode());
            recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                    DisputeNegotiationActionEnum.SELLER_REJECT.getCode(),
                    firstText(dto.getResponseDescription(), "鍗栧鎷掔粷涔板璇夋眰"), null);
        } else {
            if (!StringUtils.hasText(dto.getProposalType())) {
                throw new BusinessException("proposalType涓嶈兘涓虹┖");
            }
            if (!StringUtils.hasText(dto.getProposalDescription())) {
                throw new BusinessException("proposalDescription涓嶈兘涓虹┖");
            }
            dispute.setSellerResponseProposalType(normalizeUpper(dto.getProposalType()));
            dispute.setSellerResponseDescription(dto.getProposalDescription().trim());
            dispute.setStatus(DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode());
            dispute.setExpireTime(LocalDateTime.now().plusHours(BUYER_CONFIRM_TIMEOUT_HOURS));
            recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                    DisputeNegotiationActionEnum.SELLER_PROPOSE.getCode(), buildProposalText(dispute), dispute.getSellerResponseAmount());
        }

        if (!updateById(dispute)) {
            throw new BusinessException("鍗栧鍝嶅簲澶辫触");
        }
        if (isLateResponse) {
            recordLog(dispute.getId(), dispute.getCurrentRound(), sellerId, DisputeActorRoleEnum.SELLER.getCode(),
                    DisputeNegotiationActionEnum.SELLER_LATE_RESPONSE.getCode(), "鍗栧鍦ㄨ秴鏃跺悗琛ュ搷搴?, null);
        }
        return true;
    }

    @Override
    @Transactional
    public Boolean buyerConfirmProposal(BuyerConfirmProposalDTO dto, Long buyerId) {
        checkAndMarkTimeout();
        DisputeRequestEntity dispute = getById(dto.getDisputeId());
        if (dispute == null) {
            throw new BusinessException("浜夎涓嶅瓨鍦?);
        }
        if (!Objects.equals(dispute.getBuyerId(), buyerId)) {
            throw new BusinessException("鏃犳潈闄愮‘璁よ浜夎鏂规");
        }
        if (!DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode().equals(dispute.getStatus())) {
            throw new BusinessException("褰撳墠鐘舵€佷笉鍏佽涔板纭鏂规");
        }

        if (Boolean.TRUE.equals(dto.getAcceptProposal())) {
            dispute.setStatus(DisputeStatusEnum.NEGOTIATION_SUCCESS.getCode());
            dispute.setExpireTime(null);
            if (!updateById(dispute)) {
                throw new BusinessException("涔板纭澶辫触");
            }
            recordLog(dispute.getId(), dispute.getCurrentRound(), buyerId, DisputeActorRoleEnum.BUYER.getCode(),
                    DisputeNegotiationActionEnum.BUYER_ACCEPT_PROPOSAL.getCode(),
                    firstText(dto.getRemark(), "涔板鎺ュ彈鍗栧鏂规"), dispute.getSellerResponseAmount());
            executeNegotiationResult(dispute, dispute.getSellerResponseProposalType(),
                    dispute.getSellerResponseAmount(), dispute.getSellerResponseDescription());
            return true;
        }

        dispute.setStatus(DisputeStatusEnum.NEGOTIATION_FAILED.getCode());
        dispute.setExpireTime(null);
        if (!updateById(dispute)) {
            throw new BusinessException("涔板鎷掔粷鏂规澶辫触");
        }
        recordLog(dispute.getId(), dispute.getCurrentRound(), buyerId, DisputeActorRoleEnum.BUYER.getCode(),
                DisputeNegotiationActionEnum.BUYER_REJECT_PROPOSAL.getCode(),
                firstText(dto.getRemark(), "涔板鎷掔粷鍗栧鏂规骞跺崌绾т徊瑁?), null);
        doEscalateToArbitration(dispute, buyerId, firstText(dto.getRemark(), "涔板鎷掔粷鍗栧鏂规"), true);
        return true;
    }

    @Override
    @Transactional
    public Long escalateToArbitration(DisputeEscalateDTO dto, Long buyerId) {
        checkAndMarkTimeout();
        DisputeRequestEntity dispute = getById(dto.getDisputeId());
        if (dispute == null) {
            throw new BusinessException("浜夎涓嶅瓨鍦?);
        }
        if (!Objects.equals(dispute.getBuyerId(), buyerId)) {
            throw new BusinessException("浠呬拱瀹跺彲鍗囩骇浠茶");
        }
        if (dispute.getEscalatedArbitrationId() != null) {
            return dispute.getEscalatedArbitrationId();
        }
        if (!DisputeStatusEnum.canEscalate(dispute.getStatus())) {
            throw new BusinessException("褰撳墠鐘舵€佷笉鍏佽鍗囩骇浠茶");
        }
        return doEscalateToArbitration(dispute, buyerId, dto.getEscalateReason(), false);
    }

    @Override
    @Transactional
    public Integer checkAndMarkTimeout() {
        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.in("status", Arrays.asList(
                        DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode(),
                        DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode()))
                .isNotNull("expire_time")
                .lt("expire_time", LocalDateTime.now());
        List<DisputeRequestEntity> timeoutDisputes = list(wrapper);
        if (timeoutDisputes.isEmpty()) {
            return 0;
        }
        int changed = 0;
        for (DisputeRequestEntity dispute : timeoutDisputes) {
            if (DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode().equals(dispute.getStatus())) {
                dispute.setStatus(DisputeStatusEnum.SELLER_TIMEOUT.getCode());
                if (updateById(dispute)) {
                    changed++;
                    recordLog(dispute.getId(), dispute.getCurrentRound(), 0L, DisputeActorRoleEnum.SYSTEM.getCode(),
                            DisputeNegotiationActionEnum.SELLER_TIMEOUT.getCode(), "鍗栧鍦?4灏忔椂鍐呮湭鍝嶅簲", null);
                }
            } else if (DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode().equals(dispute.getStatus())) {
                dispute.setStatus(DisputeStatusEnum.NEGOTIATION_FAILED.getCode());
                if (updateById(dispute)) {
                    changed++;
                    recordLog(dispute.getId(), dispute.getCurrentRound(), 0L, DisputeActorRoleEnum.SYSTEM.getCode(),
                            DisputeNegotiationActionEnum.BUYER_CONFIRM_TIMEOUT.getCode(),
                            "涔板鍦?4灏忔椂鍐呮湭纭鍗栧鏂规锛屽崗鍟嗗垽瀹氬け璐ワ紝鍙崌绾т徊瑁?, null);
                }
            }
        }
        return changed;
    }

    @Override
    public String buildNegotiationSummary(Long disputeId) {
        DisputeRequestEntity dispute = getById(disputeId);
        if (dispute == null) {
            return "浜夎涓嶅瓨鍦?;
        }
        List<DisputeNegotiationLogEntity> logs = listNegotiationLog(disputeId);
        logs.sort(Comparator.comparing(DisputeNegotiationLogEntity::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())));
        StringBuilder builder = new StringBuilder();
        builder.append("浜夎ID=").append(disputeId)
                .append("锛屽綋鍓嶇姸鎬?").append(DisputeStatusEnum.getLabel(dispute.getStatus()))
                .append("锛岃疆娆?").append(dispute.getCurrentRound() == null ? 1 : dispute.getCurrentRound());
        if (!logs.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int count = 0;
            for (DisputeNegotiationLogEntity logEntity : logs) {
                if (count >= 12) {
                    builder.append("锛?..锛堝叾浣欒褰曠渷鐣ワ級");
                    break;
                }
                builder.append("锛沎").append(logEntity.getCreateTime() == null ? "-" : formatter.format(logEntity.getCreateTime()))
                        .append("] ").append(DisputeNegotiationActionEnum.labelOf(logEntity.getActionType()))
                        .append(" - ").append(firstText(logEntity.getContent(), "-"));
                count++;
            }
        }
        return builder.toString();
    }

    @Override
    public List<DisputeChatSummaryVO> buildChatSummaryByOrder(Long orderId, Long buyerId, Long sellerId,
                                                              LocalDateTime startTime, LocalDateTime endTime) {
        List<DisputeChatSummaryVO> result = new ArrayList<>();
        DisputeChatSummaryVO context = new DisputeChatSummaryVO();
        context.setSpeaker("绯荤粺");
        context.setRole(DisputeActorRoleEnum.SYSTEM.getCode());
        context.setTime(LocalDateTime.now());
        context.setSourceType("RULE_CONTEXT");
        context.setContent("鑱婂ぉ鎽樿鏃堕棿绐楋細" + firstText(String.valueOf(startTime), "-") + " ~ "
                + firstText(String.valueOf(endTime), "-")
                + "锛涘綋鍓嶇増鏈熀浜庡崗鍟嗘棩蹇楄鍒欐瀯寤猴紝娑堟伅鏈嶅姟鎺ュ叆棰勭暀鍦ㄨ鎺ュ彛");
        result.add(context);

        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId).eq("buyer_id", buyerId).eq("seller_id", sellerId);
        if (startTime != null) {
            wrapper.ge("create_time", startTime);
        }
        if (endTime != null) {
            wrapper.le("create_time", endTime);
        }
        List<DisputeRequestEntity> disputes = list(wrapper);
        if (disputes.isEmpty()) {
            return result;
        }

        List<Long> disputeIds = disputes.stream().map(DisputeRequestEntity::getId).collect(Collectors.toList());
        QueryWrapper<DisputeNegotiationLogEntity> logWrapper = new QueryWrapper<>();
        logWrapper.in("dispute_id", disputeIds).orderByAsc("create_time");
        List<DisputeNegotiationLogEntity> logs = disputeNegotiationLogMapper.selectList(logWrapper);
        for (DisputeNegotiationLogEntity logEntity : logs) {
            DisputeChatSummaryVO item = new DisputeChatSummaryVO();
            item.setSpeaker(resolveSpeaker(logEntity.getActorRole()));
            item.setRole(logEntity.getActorRole());
            item.setTime(logEntity.getCreateTime());
            item.setSourceType("NEGOTIATION_LOG");
            item.setContent(firstText(logEntity.getContent(), DisputeNegotiationActionEnum.labelOf(logEntity.getActionType())));
            result.add(item);
        }
        return result;
    }

    private void refreshSingleTimeout(Long disputeId) {
        DisputeRequestEntity dispute = getById(disputeId);
        if (dispute == null || dispute.getExpireTime() == null) {
            return;
        }
        if (LocalDateTime.now().isBefore(dispute.getExpireTime())) {
            return;
        }
        if (DisputeStatusEnum.WAIT_SELLER_RESPONSE.getCode().equals(dispute.getStatus())
                || DisputeStatusEnum.WAIT_BUYER_CONFIRM.getCode().equals(dispute.getStatus())) {
            checkAndMarkTimeout();
        }
    }

    private IPage<DisputeListItemVO> toListItemPage(IPage<DisputeRequestEntity> entityPage) {
        Page<DisputeListItemVO> voPage = new Page<>(entityPage.getCurrent(), entityPage.getSize(), entityPage.getTotal());
        List<DisputeListItemVO> records = entityPage.getRecords().stream().map(this::toListItemVO).collect(Collectors.toList());
        voPage.setRecords(records);
        return voPage;
    }

    private DisputeListItemVO toListItemVO(DisputeRequestEntity entity) {
        DisputeListItemVO vo = new DisputeListItemVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusLabel(DisputeStatusEnum.getLabel(entity.getStatus()));
        vo.setCanEscalate(DisputeStatusEnum.canEscalate(entity.getStatus()));
        return vo;
    }

    private DisputeEvidenceVO toEvidenceVO(DisputeEvidenceEntity entity) {
        DisputeEvidenceVO vo = new DisputeEvidenceVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private DisputeNegotiationLogVO toLogVO(DisputeNegotiationLogEntity entity) {
        DisputeNegotiationLogVO vo = new DisputeNegotiationLogVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setActionLabel(DisputeNegotiationActionEnum.labelOf(entity.getActionType()));
        return vo;
    }

    private SellerProposalVO buildSellerProposal(DisputeRequestEntity dispute) {
        if (!StringUtils.hasText(dispute.getSellerResponseType())) {
            return null;
        }
        SellerProposalVO vo = new SellerProposalVO();
        vo.setProposalType(firstText(dispute.getSellerResponseProposalType(), dispute.getRequestType()));
        vo.setProposalAmount(dispute.getSellerResponseAmount());
        vo.setProposalDescription(dispute.getSellerResponseDescription());
        vo.setFreightBearer(dispute.getSellerResponseFreightBearer());
        return vo;
    }

    private void ensureNoActiveDispute(Long orderId, Long buyerId) {
        QueryWrapper<DisputeRequestEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId)
                .eq("buyer_id", buyerId)
                .notIn("status", Arrays.asList(
                        DisputeStatusEnum.NEGOTIATION_SUCCESS.getCode(),
                        DisputeStatusEnum.CLOSED.getCode(),
                        DisputeStatusEnum.ESCALATED_TO_ARBITRATION.getCode()));
        if (count(wrapper) > 0) {
            throw new BusinessException("璇ヨ鍗曞凡鏈夎繘琛屼腑鐨勪簤璁紝璇峰厛澶勭悊褰撳墠浜夎");
        }
    }

    private OrderDTO fetchAndValidateOrder(Long orderId, Long buyerId) {
        Result<OrderDTO> result;
        try {
            result = tradeFeignClient.getOrderById(orderId);
        } catch (Exception e) {
            throw new BusinessException("鏌ヨ璁㈠崟澶辫触锛岃绋嶅悗閲嶈瘯");
        }
        if (result == null || !result.isSuccess() || result.getData() == null) {
            throw new BusinessException("璁㈠崟涓嶅瓨鍦ㄦ垨涓嶅彲鐢?);
        }
        OrderDTO orderDTO = result.getData();
        if (!Objects.equals(orderDTO.getBuyerId(), buyerId)) {
            throw new BusinessException("浠呬拱瀹跺彲鍙戣捣浜夎");
        }
        if (!Arrays.asList(1, 2, 5).contains(orderDTO.getStatus())) {
            throw new BusinessException("褰撳墠璁㈠崟鐘舵€佷笉鍏佽鍙戣捣浜夎");
        }
        return orderDTO;
    }

    private void saveDisputeEvidence(Long disputeId, List<DisputeCreateDTO.EvidenceItemDTO> evidenceList,
                                     Long uploaderId, String uploaderRole) {
        if (evidenceList == null || evidenceList.isEmpty()) {
            return;
        }
        for (DisputeCreateDTO.EvidenceItemDTO item : evidenceList) {
            if (item == null || !StringUtils.hasText(item.getFileUrl())) {
                continue;
            }
            DisputeEvidenceEntity evidenceEntity = new DisputeEvidenceEntity();
            evidenceEntity.setBizType(EVIDENCE_BIZ_DISPUTE);
            evidenceEntity.setBizId(disputeId);
            evidenceEntity.setUploaderId(uploaderId);
            evidenceEntity.setUploaderRole(uploaderRole);
            evidenceEntity.setEvidenceType(firstText(item.getEvidenceType(), "IMAGE"));
            evidenceEntity.setTitle(trimToEmpty(item.getTitle()));
            evidenceEntity.setDescription(trimToEmpty(item.getDescription()));
            evidenceEntity.setFileUrl(item.getFileUrl().trim());
            evidenceEntity.setThumbnailUrl(trimToEmpty(item.getThumbnailUrl()));
            disputeEvidenceMapper.insert(evidenceEntity);
        }
    }

    private List<DisputeEvidenceEntity> listDisputeEvidence(Long disputeId) {
        QueryWrapper<DisputeEvidenceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("biz_type", EVIDENCE_BIZ_DISPUTE)
                .eq("biz_id", disputeId)
                .orderByAsc("create_time");
        return disputeEvidenceMapper.selectList(wrapper);
    }

    private List<DisputeEvidenceEntity> listDisputeEvidenceByRole(Long disputeId, String role) {
        QueryWrapper<DisputeEvidenceEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("biz_type", EVIDENCE_BIZ_DISPUTE)
                .eq("biz_id", disputeId)
                .eq("uploader_role", role)
                .orderByAsc("create_time");
        return disputeEvidenceMapper.selectList(wrapper);
    }

    private List<DisputeNegotiationLogEntity> listNegotiationLog(Long disputeId) {
        QueryWrapper<DisputeNegotiationLogEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("dispute_id", disputeId).orderByAsc("create_time");
        return disputeNegotiationLogMapper.selectList(wrapper);
    }

    private void executeNegotiationResult(DisputeRequestEntity dispute, String proposalType,
                                          BigDecimal proposalAmount, String description) {
        String normalizedProposalType = normalizeUpper(firstText(proposalType, dispute.getRequestType()));
        String executeRemark;
        if ("FULL_REFUND".equals(normalizedProposalType)) {
            executeRemark = "绯荤粺杩涘叆鍏ㄩ閫€娆炬祦绋?;
        } else if ("PARTIAL_REFUND".equals(normalizedProposalType)) {
            executeRemark = "绯荤粺杩涘叆閮ㄥ垎閫€娆炬祦绋嬶紝閲戦锛? + (proposalAmount == null ? "0" : proposalAmount.toPlainString());
        } else if ("RETURN_AND_REFUND".equals(normalizedProposalType)) {
            executeRemark = "绯荤粺杩涘叆閫€璐ч€€娆炬祦绋嬶紙鍏堥€€璐у悗閫€娆撅級";
        } else if ("REPLACE".equals(normalizedProposalType) || "RESEND".equals(normalizedProposalType)) {
            executeRemark = "绯荤粺棰勭暀琛ュ彂/鎹㈣揣鎵ц鑳藉姏";
        } else {
            executeRemark = "绯荤粺杩涘叆鍗忓晢鎴愬姛鑷姩澶勭悊娴佺▼";
        }
        if (StringUtils.hasText(description)) {
            executeRemark = executeRemark + "锛涙柟妗堣鏄庯細" + description.trim();
        }
        recordLog(dispute.getId(), dispute.getCurrentRound(), 0L, DisputeActorRoleEnum.SYSTEM.getCode(),
                DisputeNegotiationActionEnum.SYSTEM_AUTO_EXECUTE.getCode(), executeRemark, proposalAmount);
    }

    private Long doEscalateToArbitration(DisputeRequestEntity dispute, Long operatorId, String escalateReason, boolean fromRejectFlow) {
        if (dispute.getEscalatedArbitrationId() != null) {
            return dispute.getEscalatedArbitrationId();
        }
        List<DisputeEvidenceEntity> buyerEvidence = listDisputeEvidenceByRole(dispute.getId(), DisputeActorRoleEnum.BUYER.getCode());
        List<String> buyerEvidenceUrls = buyerEvidence.stream()
                .map(DisputeEvidenceEntity::getFileUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        ArbitrationEntity arbitration = new ArbitrationEntity();
        arbitration.setOrderId(dispute.getOrderId());
        arbitration.setApplicantId(dispute.getBuyerId());
        arbitration.setRespondentId(dispute.getSellerId());
        arbitration.setReason(dispute.getReason());
        arbitration.setDescription(dispute.getRequestDescription());
        arbitration.setEvidence(toJson(buyerEvidenceUrls));
        arbitration.setStatus(ARBITRATION_PENDING);
        arbitration.setSourceDisputeId(dispute.getId());
        arbitration.setRequestType(dispute.getRequestType());
        arbitration.setRequestDescription(dispute.getRequestDescription());
        arbitration.setExpectedAmount(dispute.getExpectedAmount());
        arbitration.setBuyerClaim(dispute.getFactDescription());
        arbitration.setCreateTime(LocalDateTime.now());
        arbitration.setUpdateTime(LocalDateTime.now());
        arbitrationMapper.insert(arbitration);

        ArbitrationLogEntity arbitrationLog = new ArbitrationLogEntity();
        arbitrationLog.setArbitrationId(arbitration.getId());
        arbitrationLog.setOperatorId(operatorId);
        arbitrationLog.setAction("SUBMIT");
        arbitrationLog.setRemark("浜夎鍗囩骇浠茶锛宻ourceDisputeId=" + dispute.getId());
        arbitrationLogService.save(arbitrationLog);

        ArbitrationEvidenceSubmissionEntity buyerSubmission = new ArbitrationEvidenceSubmissionEntity();
        buyerSubmission.setArbitrationId(arbitration.getId());
        buyerSubmission.setSubmitterId(dispute.getBuyerId());
        buyerSubmission.setSubmitterRole(DisputeActorRoleEnum.BUYER.getCode());
        buyerSubmission.setClaimText(null);
        buyerSubmission.setFactText(dispute.getFactDescription());
        buyerSubmission.setEvidenceUrls(toJson(buyerEvidenceUrls));
        buyerSubmission.setNote("鐢变簤璁崗鍟嗛樁娈佃嚜鍔ㄧ户鎵?);
        buyerSubmission.setIsLate(0);
        arbitrationEvidenceSubmissionService.save(buyerSubmission);

        if (StringUtils.hasText(dispute.getSellerResponseDescription())) {
            ArbitrationEvidenceSubmissionEntity sellerSubmission = new ArbitrationEvidenceSubmissionEntity();
            sellerSubmission.setArbitrationId(arbitration.getId());
            sellerSubmission.setSubmitterId(dispute.getSellerId());
            sellerSubmission.setSubmitterRole(DisputeActorRoleEnum.SELLER.getCode());
            sellerSubmission.setClaimText(dispute.getSellerResponseDescription());
            sellerSubmission.setFactText(null);
            sellerSubmission.setEvidenceUrls("[]");
            sellerSubmission.setNote("鍗栧鍗忓晢闃舵鍝嶅簲鑷姩缁ф壙");
            sellerSubmission.setIsLate(0);
            arbitrationEvidenceSubmissionService.save(sellerSubmission);
        }

        ArbitrationEvidenceSubmissionEntity systemSubmission = new ArbitrationEvidenceSubmissionEntity();
        systemSubmission.setArbitrationId(arbitration.getId());
        systemSubmission.setSubmitterId(0L);
        systemSubmission.setSubmitterRole(DisputeActorRoleEnum.SYSTEM.getCode());
        systemSubmission.setClaimText(buildNegotiationSummary(dispute.getId()));
        systemSubmission.setFactText(firstText(escalateReason, fromRejectFlow ? "涔板鎷掔粷鍗栧鏂规鍚庡崌绾т徊瑁? : "浜夎鍗忓晢澶辫触锛屽崌绾т徊瑁?));
        systemSubmission.setEvidenceUrls("[]");
        systemSubmission.setNote("鍗忓晢鎽樿");
        systemSubmission.setIsLate(0);
        arbitrationEvidenceSubmissionService.save(systemSubmission);

        QueryWrapper<DisputeEvidenceEntity> evidenceWrapper = new QueryWrapper<>();
        evidenceWrapper.eq("biz_type", EVIDENCE_BIZ_DISPUTE).eq("biz_id", dispute.getId());
        List<DisputeEvidenceEntity> allEvidence = disputeEvidenceMapper.selectList(evidenceWrapper);
        for (DisputeEvidenceEntity evidenceEntity : allEvidence) {
            DisputeEvidenceEntity copy = new DisputeEvidenceEntity();
            BeanUtils.copyProperties(evidenceEntity, copy, "id", "createTime");
            copy.setBizType(EVIDENCE_BIZ_ARBITRATION);
            copy.setBizId(arbitration.getId());
            disputeEvidenceMapper.insert(copy);
        }

        dispute.setEscalatedArbitrationId(arbitration.getId());
        dispute.setStatus(DisputeStatusEnum.ESCALATED_TO_ARBITRATION.getCode());
        dispute.setExpireTime(null);
        updateById(dispute);

        recordLog(dispute.getId(), dispute.getCurrentRound(), operatorId, DisputeActorRoleEnum.BUYER.getCode(),
                DisputeNegotiationActionEnum.ESCALATE_TO_ARBITRATION.getCode(),
                firstText(escalateReason, "鍗忓晢澶辫触锛屽崌绾т徊瑁?), null);
        return arbitration.getId();
    }

    private void recordLog(Long disputeId, Integer roundNo, Long actorId, String actorRole,
                           String actionType, String content, BigDecimal amount) {
        DisputeNegotiationLogEntity logEntity = new DisputeNegotiationLogEntity();
        logEntity.setDisputeId(disputeId);
        logEntity.setRoundNo(roundNo == null ? 1 : roundNo);
        logEntity.setActorId(actorId);
        logEntity.setActorRole(actorRole);
        logEntity.setActionType(actionType);
        logEntity.setContent(content);
        logEntity.setAmount(amount);
        disputeNegotiationLogMapper.insert(logEntity);
    }

    private boolean hitHighRiskRule(DisputeRequestEntity dispute) {
        return dispute.getExpectedAmount() != null
                && dispute.getExpectedAmount().compareTo(HIGH_AMOUNT_ESCALATE_THRESHOLD) >= 0;
    }

    private Long calcCountdownSeconds(LocalDateTime expireTime) {
        if (expireTime == null) {
            return 0L;
        }
        long seconds = Duration.between(LocalDateTime.now(), expireTime).getSeconds();
        return Math.max(seconds, 0L);
    }

    private String buildProposalText(DisputeRequestEntity dispute) {
        StringBuilder builder = new StringBuilder();
        builder.append("proposalType=").append(firstText(dispute.getSellerResponseProposalType(), "-"));
        if (dispute.getSellerResponseAmount() != null) {
            builder.append("锛宲roposalAmount=").append(dispute.getSellerResponseAmount().toPlainString());
        }
        if (StringUtils.hasText(dispute.getSellerResponseDescription())) {
            builder.append("锛宲roposalDescription=").append(dispute.getSellerResponseDescription().trim());
        }
        if (StringUtils.hasText(dispute.getSellerResponseFreightBearer())) {
            builder.append("锛宖reightBearer=").append(dispute.getSellerResponseFreightBearer().trim());
        }
        return builder.toString();
    }

    private String resolveSpeaker(String role) {
        String normalized = normalizeUpper(role);
        if (DisputeActorRoleEnum.BUYER.getCode().equals(normalized)) {
            return "涔板";
        }
        if (DisputeActorRoleEnum.SELLER.getCode().equals(normalized)) {
            return "鍗栧";
        }
        if (DisputeActorRoleEnum.ADMIN.getCode().equals(normalized)) {
            return "绠＄悊鍛?;
        }
        return "绯荤粺";
    }

    private String toJson(List<String> urls) {
        List<String> safe = urls == null ? Collections.emptyList() : urls.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());
        try {
            return objectMapper.writeValueAsString(safe);
        } catch (Exception e) {
            log.warn("serialize urls failed", e);
            return "[]";
        }
    }

    private List<String> parseJsonArray(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.singletonList(json);
        }
    }

    private String normalizeUpper(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
    }

    private String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }

    private String firstText(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }
}

```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeChatSummaryVO.java
```java
package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DisputeChatSummaryVO {

    private String speaker;

    private String role;

    private String content;

    private LocalDateTime time;

    private String sourceType;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeDetailVO.java
```java
package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DisputeDetailVO {

    private Long id;

    private Long orderId;

    private Long productId;

    private Long buyerId;

    private Long sellerId;

    private String reason;

    private String factDescription;

    private String requestType;

    private String requestDescription;

    private BigDecimal expectedAmount;

    private String status;

    private String statusLabel;

    private Integer currentRound;

    private Long escalatedArbitrationId;

    private LocalDateTime expireTime;

    private Long countdownSeconds;

    private String negotiationSummary;

    private SellerProposalVO sellerProposal;

    private List<DisputeEvidenceVO> evidenceList = new ArrayList<>();

    private List<DisputeNegotiationLogVO> negotiationLogs = new ArrayList<>();

    private List<DisputeChatSummaryVO> chatSummary = new ArrayList<>();

    private Boolean canSellerRespond;

    private Boolean canBuyerConfirm;

    private Boolean canEscalate;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeEvidenceVO.java
```java
package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DisputeEvidenceVO {

    private Long id;

    private String evidenceType;

    private String title;

    private String description;

    private String fileUrl;

    private String thumbnailUrl;

    private String uploaderRole;

    private LocalDateTime createTime;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeListItemVO.java
```java
package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DisputeListItemVO {

    private Long id;

    private Long orderId;

    private Long productId;

    private String reason;

    private String requestType;

    private BigDecimal expectedAmount;

    private String status;

    private String statusLabel;

    private String sellerResponseType;

    private LocalDateTime expireTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Boolean canEscalate;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/DisputeNegotiationLogVO.java
```java
package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DisputeNegotiationLogVO {

    private Long id;

    private Integer roundNo;

    private Long actorId;

    private String actorRole;

    private String actionType;

    private String actionLabel;

    private String content;

    private BigDecimal amount;

    private LocalDateTime createTime;
}


```

## market-service/market-service-arbitration/src/main/java/org/shyu/marketservicearbitration/vo/SellerProposalVO.java
```java
package org.shyu.marketservicearbitration.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SellerProposalVO {

    private String proposalType;

    private BigDecimal proposalAmount;

    private String proposalDescription;

    private String freightBearer;
}


```

## market-service/market-service-arbitration/src/main/resources/mapper/DisputeRequestMapper.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="org.shyu.marketservicearbitration.mapper.DisputeRequestMapper">

    <resultMap id="BaseResultMap" type="org.shyu.marketservicearbitration.entity.DisputeRequestEntity">
        <id column="id" property="id"/>
        <result column="order_id" property="orderId"/>
        <result column="product_id" property="productId"/>
        <result column="buyer_id" property="buyerId"/>
        <result column="seller_id" property="sellerId"/>
        <result column="reason" property="reason"/>
        <result column="fact_description" property="factDescription"/>
        <result column="request_type" property="requestType"/>
        <result column="request_description" property="requestDescription"/>
        <result column="expected_amount" property="expectedAmount"/>
        <result column="status" property="status"/>
        <result column="current_round" property="currentRound"/>
        <result column="seller_response_type" property="sellerResponseType"/>
        <result column="seller_response_description" property="sellerResponseDescription"/>
        <result column="seller_response_proposal_type" property="sellerResponseProposalType"/>
        <result column="seller_response_amount" property="sellerResponseAmount"/>
        <result column="seller_response_freight_bearer" property="sellerResponseFreightBearer"/>
        <result column="escalated_arbitration_id" property="escalatedArbitrationId"/>
        <result column="expire_time" property="expireTime"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
    </resultMap>

</mapper>

```

## market-service/market-service-arbitration/src/main/resources/sql/market_dispute.sql
```sql
-- =========================
-- 浜夎鍗忓晢娴佺▼鏁版嵁搴撹剼鏈?-- =========================

CREATE TABLE IF NOT EXISTS `t_dispute_request` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `product_id` bigint DEFAULT NULL,
  `buyer_id` bigint NOT NULL,
  `seller_id` bigint NOT NULL,
  `reason` varchar(64) NOT NULL,
  `fact_description` varchar(2000) NOT NULL,
  `request_type` varchar(64) NOT NULL,
  `request_description` varchar(2000) NOT NULL,
  `expected_amount` decimal(12,2) DEFAULT NULL,
  `status` varchar(64) NOT NULL,
  `current_round` int NOT NULL DEFAULT 1,
  `seller_response_type` varchar(64) DEFAULT NULL,
  `seller_response_description` varchar(2000) DEFAULT NULL,
  `seller_response_proposal_type` varchar(64) DEFAULT NULL,
  `seller_response_amount` decimal(12,2) DEFAULT NULL,
  `seller_response_freight_bearer` varchar(32) DEFAULT NULL,
  `escalated_arbitration_id` bigint DEFAULT NULL,
  `expire_time` datetime DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dispute_order` (`order_id`),
  KEY `idx_dispute_buyer` (`buyer_id`),
  KEY `idx_dispute_seller` (`seller_id`),
  KEY `idx_dispute_status` (`status`),
  KEY `idx_dispute_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `t_dispute_evidence` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `biz_type` varchar(32) NOT NULL COMMENT 'DISPUTE / ARBITRATION',
  `biz_id` bigint NOT NULL,
  `uploader_id` bigint NOT NULL,
  `uploader_role` varchar(32) NOT NULL COMMENT 'BUYER / SELLER / SYSTEM / ADMIN',
  `evidence_type` varchar(32) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `description` varchar(2000) DEFAULT NULL,
  `file_url` varchar(1024) NOT NULL,
  `thumbnail_url` varchar(1024) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dispute_evidence_biz` (`biz_type`,`biz_id`),
  KEY `idx_dispute_evidence_uploader` (`uploader_id`,`uploader_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `t_dispute_negotiation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dispute_id` bigint NOT NULL,
  `round_no` int NOT NULL DEFAULT 1,
  `actor_id` bigint NOT NULL,
  `actor_role` varchar(32) NOT NULL,
  `action_type` varchar(64) NOT NULL,
  `content` varchar(4000) DEFAULT NULL,
  `amount` decimal(12,2) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_dispute_log_dispute` (`dispute_id`),
  KEY `idx_dispute_log_action` (`action_type`),
  KEY `idx_dispute_log_create` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================
-- 鎵╁睍浠茶琛ㄥ瓧娈?-- =========================
ALTER TABLE `t_arbitration`
  ADD COLUMN IF NOT EXISTS `source_dispute_id` bigint DEFAULT NULL COMMENT '鏉ユ簮浜夎ID',
  ADD COLUMN IF NOT EXISTS `request_type` varchar(64) DEFAULT NULL COMMENT '璇夋眰绫诲瀷',
  ADD COLUMN IF NOT EXISTS `request_description` varchar(2000) DEFAULT NULL COMMENT '璇夋眰璇存槑',
  ADD COLUMN IF NOT EXISTS `expected_amount` decimal(12,2) DEFAULT NULL COMMENT '鏈熸湜閲戦',
  ADD COLUMN IF NOT EXISTS `buyer_claim` varchar(2000) DEFAULT NULL COMMENT '涔板浜嬪疄涓诲紶',
  ADD COLUMN IF NOT EXISTS `decision_remark` varchar(2000) DEFAULT NULL COMMENT '瑁佸喅澶囨敞',
  ADD COLUMN IF NOT EXISTS `reject_reason` varchar(2000) DEFAULT NULL COMMENT '椹冲洖鍘熷洜';


```


