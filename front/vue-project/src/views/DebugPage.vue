<template>
  <div class="debug-page">
    <h1>Vue 应用调试页面</h1>

    <div class="status-section">
      <h2>✅ Vue 应用已正常加载</h2>
      <p>时间：{{ currentTime }}</p>
      <p>路由：{{ $route.path }}</p>
      <p>完整URL：{{ fullUrl }}</p>
    </div>

    <div class="route-test-section">
      <h3>路由测试：</h3>
      <div class="test-buttons">
        <el-button @click="testAdminLogin">测试管理员登录页</el-button>
        <el-button @click="testUserLogin">测试用户登录页</el-button>
        <el-button @click="testHome">测试首页</el-button>
        <el-button @click="clearStorage">清理存储</el-button>
      </div>
    </div>

    <div class="links-section">
      <h3>快速访问：</h3>
      <div class="link-grid">
        <router-link to="/admin/login" class="debug-link">管理员登录</router-link>
        <router-link to="/admin/arbitration/pending" class="debug-link">管理端仲裁页</router-link>
        <router-link to="/user" class="debug-link">用户端</router-link>
        <router-link to="/test" class="debug-link">测试页面</router-link>
      </div>
    </div>

    <div class="info-section">
      <h3>系统信息：</h3>
      <ul>
        <li>Vue版本：{{ vueVersion }}</li>
        <li>Element Plus：已加载</li>
        <li>路由模式：History</li>
        <li>API地址：http://localhost:8100</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { version } from 'vue'

const router = useRouter()
const route = useRoute()
const currentTime = ref(new Date().toLocaleString())
const vueVersion = ref(version)

const fullUrl = computed(() => {
  return window.location.href
})

// 路由测试方法
const testAdminLogin = () => {
  console.log('测试跳转到管理员登录页')
  router.push('/admin/login')
}

const testUserLogin = () => {
  console.log('测试跳转到用户登录页')
  router.push('/user/login')
}

const testHome = () => {
  console.log('测试跳转到首页')
  router.push('/')
}

const clearStorage = () => {
  localStorage.clear()
  sessionStorage.clear()
  console.log('已清理所有存储')
  alert('存储已清理')
}

onMounted(() => {
  console.log('Vue应用已加载，Element Plus图标：', Object.keys(window))
  setInterval(() => {
    currentTime.value = new Date().toLocaleString()
  }, 1000)
})
</script>

<style scoped>
.debug-page {
  padding: 40px;
  max-width: 800px;
  margin: 0 auto;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.status-section {
  background: #f0f9ff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #67c23a;
}

.links-section {
  margin-bottom: 30px;
}

.link-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.debug-link {
  display: block;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-decoration: none;
  padding: 15px 20px;
  border-radius: 8px;
  text-align: center;
  transition: all 0.3s ease;
}

.debug-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.route-test-section {
  background: #fff3cd;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #ffc107;
}

.test-buttons {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 15px;
}

.test-buttons .el-button {
  margin: 0;
}

.info-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
}

.info-section ul {
  list-style: none;
  padding: 0;
}

.info-section li {
  padding: 8px 0;
  border-bottom: 1px solid #e9ecef;
}

.info-section li:last-child {
  border-bottom: none;
}

h1 {
  color: #333;
  text-align: center;
  margin-bottom: 30px;
}

h2 {
  color: #67c23a;
  margin: 0;
}

h3 {
  color: #333;
  margin-bottom: 15px;
}
</style>
