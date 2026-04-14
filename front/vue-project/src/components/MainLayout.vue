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
