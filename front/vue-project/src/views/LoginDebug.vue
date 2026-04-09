<template>
  <div style="padding: 40px; font-family: monospace;">
    <h2>登录状态调试</h2>
    
    <div style="margin: 20px 0; padding: 20px; background: #f0f0f0; border-radius: 8px;">
      <h3>LocalStorage 信息:</h3>
      <pre>{{ localStorageInfo }}</pre>
    </div>

    <div style="margin: 20px 0; padding: 20px; background: #f0f0f0; border-radius: 8px;">
      <h3>UserStore 状态:</h3>
      <pre>{{ userStoreInfo }}</pre>
    </div>

    <div style="margin: 20px 0;">
      <el-button type="primary" @click="testLogin">测试登录</el-button>
      <el-button @click="clearStorage">清除存储</el-button>
      <el-button @click="goHome">跳转首页</el-button>
      <el-button @click="goLogin">跳转登录</el-button>
    </div>

    <div style="margin: 20px 0; padding: 20px; background: #ffe; border-radius: 8px;">
      <h3>测试日志:</h3>
      <div v-for="(log, index) in logs" :key="index" style="margin: 5px 0;">
        {{ log }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const logs = ref([])

const addLog = (msg) => {
  const timestamp = new Date().toLocaleTimeString()
  logs.value.push(`[${timestamp}] ${msg}`)
}

const localStorageInfo = computed(() => {
  return {
    token: localStorage.getItem('token'),
    userInfo: localStorage.getItem('userInfo'),
    adminToken: localStorage.getItem('adminToken')
  }
})

const userStoreInfo = computed(() => {
  return {
    token: userStore.token,
    isLoggedIn: userStore.isLoggedIn,
    username: userStore.username,
    nickname: userStore.nickname,
    userId: userStore.userId
  }
})

const testLogin = () => {
  addLog('测试设置登录状态...')
  
  const testToken = 'test-token-' + Date.now()
  const testUserInfo = {
    id: 123,
    username: 'testuser',
    nickname: '测试用户'
  }
  
  localStorage.setItem('token', testToken)
  localStorage.setItem('userInfo', JSON.stringify(testUserInfo))
  
  addLog('Token 已设置: ' + testToken)
  addLog('UserInfo 已设置: ' + JSON.stringify(testUserInfo))
  
  // 刷新 store
  userStore.initUserState()
  
  addLog('Store 已刷新')
}

const clearStorage = () => {
  addLog('清除所有存储...')
  localStorage.clear()
  userStore.logout()
  addLog('存储已清除')
}

const goHome = () => {
  addLog('跳转到首页...')
  router.push('/')
}

const goLogin = () => {
  addLog('跳转到登录页...')
  router.push('/login')
}

onMounted(() => {
  addLog('页面加载完成')
  addLog('当前路径: ' + router.currentRoute.value.path)
})
</script>
