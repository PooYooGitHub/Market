<template>
  <div class="route-test-page">
    <h1>路由测试页面</h1>

    <div class="current-info">
      <p><strong>当前路径:</strong> {{ $route.path }}</p>
      <p><strong>完整URL:</strong> {{ fullUrl }}</p>
      <p><strong>History State:</strong> {{ JSON.stringify(window.history.state) }}</p>
    </div>

    <div class="test-section">
      <h3>路由跳转测试</h3>
      <div class="button-group">
        <button @click="testPush('/admin/login')">Push to Admin Login</button>
        <button @click="testPush('/user/login')">Push to User Login</button>
        <button @click="testPush('/')">Push to Home</button>
        <button @click="testReplace('/admin/login')">Replace to Admin Login</button>
      </div>
    </div>

    <div class="test-section">
      <h3>原生跳转测试</h3>
      <div class="button-group">
        <button @click="testWindowLocation('/admin/login')">window.location to Admin</button>
        <button @click="testWindowHref('/admin/login')">window.location.href to Admin</button>
        <button @click="testWindowReplace('/admin/login')">window.location.replace to Admin</button>
      </div>
    </div>

    <div class="test-section">
      <h3>清理操作</h3>
      <div class="button-group">
        <button @click="clearStorage">清理localStorage</button>
        <button @click="clearHistory">清理浏览器历史</button>
        <button @click="reloadPage">重载页面</button>
      </div>
    </div>

    <div class="logs">
      <h3>操作日志</h3>
      <div class="log-container">
        <div v-for="log in logs" :key="log.id" class="log-item">
          {{ log.time }} - {{ log.message }}
        </div>
      </div>
      <button @click="clearLogs">清理日志</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const logs = ref([])

const fullUrl = computed(() => window.location.href)

const addLog = (message) => {
  logs.value.unshift({
    id: Date.now(),
    time: new Date().toLocaleTimeString(),
    message
  })
  console.log('[RouteTest]', message)
}

const testPush = async (path) => {
  try {
    addLog(`尝试 router.push('${path}')`)
    await router.push(path)
    addLog(`✅ router.push('${path}') 成功`)
  } catch (error) {
    addLog(`❌ router.push('${path}') 失败: ${error.message}`)
  }
}

const testReplace = async (path) => {
  try {
    addLog(`尝试 router.replace('${path}')`)
    await router.replace(path)
    addLog(`✅ router.replace('${path}') 成功`)
  } catch (error) {
    addLog(`❌ router.replace('${path}') 失败: ${error.message}`)
  }
}

const testWindowLocation = (path) => {
  addLog(`尝试 window.location = '${path}'`)
  window.location = path
}

const testWindowHref = (path) => {
  addLog(`尝试 window.location.href = '${path}'`)
  window.location.href = path
}

const testWindowReplace = (path) => {
  addLog(`尝试 window.location.replace('${path}')`)
  window.location.replace(path)
}

const clearStorage = () => {
  localStorage.clear()
  sessionStorage.clear()
  addLog('✅ 已清理所有存储')
}

const clearHistory = () => {
  // 这个操作会重载页面
  window.history.replaceState(null, '', '/')
  addLog('✅ 已重置浏览器历史')
}

const reloadPage = () => {
  addLog('🔄 重载页面...')
  window.location.reload()
}

const clearLogs = () => {
  logs.value = []
}

onMounted(() => {
  addLog('📱 路由测试页面已加载')
  addLog(`当前路径: ${route.path}`)
  addLog(`完整URL: ${fullUrl.value}`)
})
</script>

<style scoped>
.route-test-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.current-info {
  background: #f0f9ff;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #3b82f6;
}

.test-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.test-section h3 {
  margin-top: 0;
  color: #374151;
}

.button-group {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.button-group button {
  padding: 8px 16px;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s;
}

.button-group button:hover {
  background: #2563eb;
}

.logs {
  background: #f9fafb;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  background: white;
  padding: 10px;
  border-radius: 4px;
  margin: 10px 0;
  border: 1px solid #d1d5db;
}

.log-item {
  padding: 4px 0;
  border-bottom: 1px solid #f3f4f6;
  font-family: monospace;
  font-size: 12px;
}

.log-item:last-child {
  border-bottom: none;
}

h1 {
  color: #1f2937;
  text-align: center;
}
</style>