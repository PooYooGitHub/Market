<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-content">
        <div class="logo-section">
          <el-icon size="32" color="#667eea">
            <ShoppingBag />
          </el-icon>
          <h1 class="logo">校园跳蚤市场</h1>
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
            首页
          </el-menu-item>
          <el-menu-item index="/products">
            <el-icon><Goods /></el-icon>
            商品列表
          </el-menu-item>

          <template v-if="userStore.isLoggedIn">
            <el-menu-item index="/product/publish">
              <el-icon><Plus /></el-icon>
              发布商品
            </el-menu-item>
            <el-menu-item index="/my-products">
              <el-icon><Box /></el-icon>
              我的商品
            </el-menu-item>
            <el-menu-item index="/cart">
              <el-icon><ShoppingCart /></el-icon>
              购物车
            </el-menu-item>
            <el-menu-item index="/orders">
              <el-icon><Tickets /></el-icon>
              我的订单
            </el-menu-item>
            <el-menu-item index="/messages">
              <el-icon><ChatDotRound /></el-icon>
              消息
            </el-menu-item>
            <el-menu-item index="/credit">
              <el-icon><Medal /></el-icon>
              我的信用
            </el-menu-item>
            <el-menu-item index="/arbitration/list">
              <el-icon><DocumentChecked /></el-icon>
              我的仲裁
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
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button-group>
              <el-button @click="$router.push('/user/login')">登录</el-button>
              <el-button type="primary" @click="$router.push('/register')">注册</el-button>
            </el-button-group>
          </template>
        </div>
      </div>
    </el-header>

    <!-- 主内容区 -->
    <el-main class="main-content">
      <!-- Hero Section -->
      <div class="hero-section">
        <div class="hero-content">
          <h2 class="hero-title">欢迎来到校园跳蚤市场</h2>
          <p class="hero-subtitle">让闲置物品流转起来，让资源得到更好的利用</p>
          <div class="hero-stats">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-statistic title="注册用户" :value="1888" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="在售商品" :value="5692" />
              </el-col>
              <el-col :span="8">
                <el-statistic title="成功交易" :value="12580" />
              </el-col>
            </el-row>
          </div>
        </div>
      </div>

      <!-- 功能特性 -->
      <div class="features-section">
        <h3 class="section-title">平台特色</h3>
        <el-row :gutter="24" class="features">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="feature-card" shadow="hover">
              <div class="feature-icon">
                <el-icon size="48" color="#67C23A">
                  <DocumentAdd />
                </el-icon>
              </div>
              <h4>商品发布</h4>
              <p>轻松发布二手商品，支持多图上传</p>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="feature-card" shadow="hover">
              <div class="feature-icon">
                <el-icon size="48" color="#409EFF">
                  <Search />
                </el-icon>
              </div>
              <h4>智能搜索</h4>
              <p>快速找到心仪物品，多维度筛选</p>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="feature-card" shadow="hover">
              <div class="feature-icon">
                <el-icon size="48" color="#E6A23C">
                  <ChatDotRound />
                </el-icon>
              </div>
              <h4>即时聊天</h4>
              <p>与卖家实时沟通，交易更便捷</p>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="feature-card" shadow="hover">
              <div class="feature-icon">
                <el-icon size="48" color="#F56C6C">
                  <Star />
                </el-icon>
              </div>
              <h4>信用评价</h4>
              <p>建立可信交易环境，安全保障</p>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- CTA Section -->
      <div v-if="!userStore.isLoggedIn" class="cta-section">
        <el-card class="cta-card">
          <h3>立即开始使用</h3>
          <p>加入我们，开启您的校园交易之旅</p>
          <div class="cta-buttons">
            <el-button type="primary" size="large" @click="$router.push('/register')">
              <el-icon><UserFilled /></el-icon>
              立即注册
            </el-button>
            <el-button size="large" @click="$router.push('/user/login')">
              <el-icon><Right /></el-icon>
              快速登录
            </el-button>
          </div>
        </el-card>
      </div>

      <!-- 已登录用户区域 -->
      <div v-else class="user-dashboard">
        <el-card class="welcome-card">
          <h3>欢迎回来，{{ userStore.nickname || userStore.username }}！</h3>
          <p>开始发布您的闲置物品，或浏览其他用户的商品吧！</p>
          <div class="quick-actions">
            <el-button-group>
              <el-button type="primary" @click="$router.push('/product/publish')">
                <el-icon><Plus /></el-icon>
                发布商品
              </el-button>
              <el-button @click="$router.push('/products')">
                <el-icon><Search /></el-icon>
                浏览商品
              </el-button>
              <el-button @click="$router.push('/my-products')">
                <el-icon><Box /></el-icon>
                我的商品
              </el-button>
              <el-button @click="$router.push('/messages')">
                <el-icon><ChatDotRound /></el-icon>
                消息中心
              </el-button>
            </el-button-group>
          </div>
        </el-card>
      </div>

      <!-- 热门商品推荐 -->
      <div class="hot-products">
        <h3 class="section-title">热门商品</h3>
        <el-row :gutter="16">
          <el-col :xs="12" :sm="8" :md="6" v-for="product in hotProducts" :key="product.id">
            <el-card class="product-card" shadow="hover" @click="$router.push(`/product/${product.id}`)">
              <img :src="product.coverImage" class="product-image" />
              <div class="product-info">
                <h4 class="product-title">{{ product.title }}</h4>
                <div class="product-price">
                  <span class="current-price">¥{{ product.price }}</span>
                  <span v-if="product.originalPrice" class="original-price">¥{{ product.originalPrice }}</span>
                </div>
                <div class="product-meta">
                  <el-tag size="small" type="info">{{ product.categoryName }}</el-tag>
                  <span class="view-count">
                    <el-icon><View /></el-icon>
                    {{ product.viewCount }}
                  </span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-main>

    <!-- 页脚 -->
    <el-footer class="footer">
      <div class="footer-content">
        <div class="footer-info">
          <p>&copy; 2026 校园跳蚤市场</p>
          <p>基于 SpringCloud 微服务架构 | Vue 3 + Element Plus</p>
        </div>
        <div class="footer-links">
          <el-link href="#" :underline="false">关于我们</el-link>
          <el-divider direction="vertical" />
          <el-link href="#" :underline="false">帮助中心</el-link>
          <el-divider direction="vertical" />
          <el-link href="#" :underline="false">联系我们</el-link>
        </div>
      </div>
    </el-footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
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
  UserFilled,
  Right,
  DocumentAdd,
  DocumentChecked,
  Search,
  Star,
  View,
  ShoppingBag
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 热门商品数据
const hotProducts = ref([
  {
    id: 2,
    title: '乒乓球',
    price: 5.00,
    originalPrice: 10.00,
    categoryName: '运动健身',
    viewCount: 14,
    coverImage: 'http://localhost:9900/market-product/d6cdb061e6804a0198cf409964c52d10.png'
  },
  {
    id: 1,
    title: 'java教材',
    price: 100.00,
    originalPrice: 200.00,
    categoryName: '图书教材',
    viewCount: 2,
    coverImage: 'http://localhost:9900/market-product/6f2f0b7d63824ac3b9b138d53beab391.png'
  }
])

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
      } catch (error) {
        // 用户取消
      }
      break
  }
}

onMounted(() => {
  // 可以在这里加载热门商品数据
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
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
  gap: 12px;
}

.logo {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: #1f2937;
}

.nav-menu {
  flex: 1;
  margin: 0 40px;
  border-bottom: none;
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
}

.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 60px 40px;
  margin-bottom: 40px;
  color: white;
  text-align: center;
}

.hero-title {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 16px 0;
}

.hero-subtitle {
  font-size: 18px;
  margin: 0 0 40px 0;
  opacity: 0.9;
}

.hero-stats :deep(.el-statistic__head) {
  color: rgba(255, 255, 255, 0.8);
}

.hero-stats :deep(.el-statistic__content) {
  color: white;
}

.section-title {
  font-size: 28px;
  font-weight: 700;
  text-align: center;
  margin: 0 0 32px 0;
  color: #1f2937;
}

.features-section {
  margin-bottom: 60px;
}

.feature-card {
  text-align: center;
  border-radius: 16px;
  margin-bottom: 20px;
}

.feature-card :deep(.el-card__body) {
  padding: 32px 24px;
}

.feature-icon {
  margin-bottom: 16px;
}

.feature-card h4 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #1f2937;
}

.feature-card p {
  color: #6b7280;
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
}

.cta-section {
  margin-bottom: 60px;
}

.cta-card {
  text-align: center;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
}

.cta-card :deep(.el-card__body) {
  padding: 48px;
}

.cta-card h3 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #1f2937;
}

.cta-card p {
  color: #6b7280;
  margin: 0 0 32px 0;
  font-size: 16px;
}

.cta-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.user-dashboard {
  margin-bottom: 60px;
}

.welcome-card {
  text-align: center;
  border-radius: 16px;
}

.welcome-card :deep(.el-card__body) {
  padding: 48px;
}

.welcome-card h3 {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 16px 0;
  color: #1f2937;
}

.welcome-card p {
  color: #6b7280;
  margin: 0 0 32px 0;
  font-size: 16px;
}

.hot-products {
  margin-bottom: 40px;
}

.product-card {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}

.product-card :deep(.el-card__body) {
  padding: 0;
}

.product-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.product-info {
  padding: 16px;
}

.product-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 8px 0;
  color: #1f2937;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  margin-bottom: 8px;
}

.current-price {
  font-size: 16px;
  font-weight: 700;
  color: #ef4444;
  margin-right: 8px;
}

.original-price {
  font-size: 12px;
  color: #9ca3af;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #6b7280;
}

.footer {
  background: #f8fafc;
  border-top: 1px solid #e5e7eb;
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
  color: #6b7280;
  font-size: 14px;
  line-height: 1.6;
}

.footer-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    padding: 16px 20px;
  }

  .nav-menu {
    margin: 16px 0;
  }

  .hero-section {
    padding: 40px 24px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 16px;
  }

  .cta-buttons {
    flex-direction: column;
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