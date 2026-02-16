<template>
  <router-view />
</template>

<script setup>
import { watch, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import websocketService from '@/utils/websocket'

const userStore = useUserStore()

// 监听用户登录状态
watch(
  () => userStore.isLoggedIn,
  (isLoggedIn) => {
    if (isLoggedIn && userStore.userInfo?.id) {
      // 用户登录，建立 WebSocket 连接
      websocketService.connect(userStore.userInfo.id)
    } else {
      // 用户登出，关闭 WebSocket 连接
      websocketService.close()
    }
  },
  { immediate: true }
)

// 组件卸载时关闭 WebSocket 连接
onUnmounted(() => {
  websocketService.close()
})
</script>

<style>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  min-height: 100vh;
}
</style>
