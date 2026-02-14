<template>
  <div class="home-container">
    <!-- 导航栏 -->
    <header class="header">
      <div class="header-content">
        <h1 class="logo">🏫 校园跳蚤市场</h1>
        <nav class="nav">
          <template v-if="userStore.isLoggedIn">
            <span class="welcome">欢迎，{{ userStore.nickname || userStore.username }}</span>
            <router-link to="/profile" class="nav-link">个人中心</router-link>
            <button @click="handleLogout" class="nav-button">退出登录</button>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-link">登录</router-link>
            <router-link to="/register" class="nav-button">注册</router-link>
          </template>
        </nav>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="hero-section">
        <h2 class="hero-title">欢迎来到校园跳蚤市场</h2>
        <p class="hero-subtitle">让闲置物品流转起来，让资源得到更好的利用</p>
      </div>

      <div class="features">
        <div class="feature-card">
          <div class="feature-icon">📱</div>
          <h3>商品发布</h3>
          <p>轻松发布二手商品</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">🔍</div>
          <h3>智能搜索</h3>
          <p>快速找到心仪物品</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">💬</div>
          <h3>即时聊天</h3>
          <p>与卖家实时沟通</p>
        </div>
        <div class="feature-card">
          <div class="feature-icon">⭐</div>
          <h3>信用评价</h3>
          <p>建立可信交易环境</p>
        </div>
      </div>

      <div v-if="!userStore.isLoggedIn" class="cta-section">
        <h3>立即开始使用</h3>
        <div class="cta-buttons">
          <router-link to="/register" class="cta-button primary">注册账号</router-link>
          <router-link to="/login" class="cta-button secondary">立即登录</router-link>
        </div>
      </div>

      <div v-else class="user-section">
        <h3>您已登录</h3>
        <p>开始发布您的闲置物品，或浏览其他用户的商品吧！</p>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>&copy; 2026 校园跳蚤市场 | 基于 SpringCloud 微服务架构</p>
    </footer>
  </div>
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
  }
}
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

/* 头部导航 */
.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  margin: 0;
  font-size: 24px;
  color: #667eea;
  font-weight: 600;
}

.nav {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome {
  color: #666;
  font-size: 14px;
}

.nav-link {
  color: #667eea;
  text-decoration: none;
  padding: 8px 16px;
  border-radius: 6px;
  transition: all 0.3s;
}

.nav-link:hover {
  background: #f0f0f0;
}

.nav-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
}

.nav-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

/* 主内容区 */
.main-content {
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  width: 100%;
}

.hero-section {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  margin-bottom: 40px;
}

.hero-title {
  font-size: 42px;
  margin: 0 0 16px;
  font-weight: 700;
}

.hero-subtitle {
  font-size: 20px;
  margin: 0;
  opacity: 0.9;
}

.features {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 40px;
}

.feature-card {
  background: white;
  border-radius: 12px;
  padding: 32px 24px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.feature-card h3 {
  color: #333;
  margin: 0 0 8px;
  font-size: 20px;
}

.feature-card p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.cta-section {
  background: white;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.cta-section h3 {
  color: #333;
  font-size: 28px;
  margin: 0 0 24px;
}

.cta-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

.cta-button {
  padding: 14px 32px;
  border-radius: 8px;
  text-decoration: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s;
  display: inline-block;
}

.cta-button.primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.cta-button.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.cta-button.secondary {
  background: white;
  color: #667eea;
  border: 2px solid #667eea;
}

.cta-button.secondary:hover {
  background: #667eea;
  color: white;
}

.user-section {
  background: white;
  border-radius: 12px;
  padding: 40px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-section h3 {
  color: #333;
  font-size: 28px;
  margin: 0 0 16px;
}

.user-section p {
  color: #666;
  font-size: 16px;
  margin: 0;
}

/* 页脚 */
.footer {
  background: white;
  padding: 24px;
  text-align: center;
  border-top: 1px solid #eee;
  margin-top: auto;
}

.footer p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .hero-title {
    font-size: 32px;
  }
  
  .hero-subtitle {
    font-size: 16px;
  }
  
  .features {
    grid-template-columns: 1fr;
  }
  
  .header-content {
    flex-direction: column;
    gap: 16px;
  }
}
</style>
