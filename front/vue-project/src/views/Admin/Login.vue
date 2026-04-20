<template>
  <div class="admin-login-page">
    <div class="login-background">
      <div class="bg-overlay"></div>
    </div>

    <div class="login-container">
      <div class="login-card">
        <!-- 头部 -->
        <div class="login-header">
          <div class="logo">
            <el-icon size="40" color="#409EFF">
              <Monitor />
            </el-icon>
            <h1>仲裁管理系统</h1>
          </div>
          <p class="subtitle">Administrator Login</p>
        </div>

        <!-- 登录表单 -->
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          size="large"
          @submit.prevent="handleLogin"
        >
          <!-- 添加测试账号提示 -->
          <el-alert
            title="测试账号"
            type="info"
            :closable="false"
            style="margin-bottom: 20px;"
          >
            <template #default>
              <div style="font-size: 13px;">
                <div><strong>管理员：</strong> admin / 123456</div>
              </div>
            </template>
          </el-alert>

          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入管理员账号"
              :prefix-icon="User"
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              show-password
              clearable
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <!-- 验证码已移除，简化登录流程 -->

          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="loginForm.remember">记住我</el-checkbox>
              <el-link type="primary" @click="showForgotPassword">
                忘记密码？
              </el-link>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="login-btn"
              size="large"
              :loading="loading"
              @click="handleLogin"
            >
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 底部信息 -->
        <div class="login-footer">
          <div class="security-tips">
            <el-icon><Warning /></el-icon>
            <span>管理员登录受到严格的安全保护</span>
          </div>
          <div class="copyright">
            © 2026 校园跳蚤市场仲裁管理系统
          </div>
        </div>
      </div>

      <!-- 系统信息侧边栏 -->
      <div class="system-info">
        <div class="info-card">
          <h3>系统特性</h3>
          <ul class="feature-list">
            <li>
              <el-icon><CircleCheck /></el-icon>
              高效的仲裁申请处理流程
            </li>
            <li>
              <el-icon><CircleCheck /></el-icon>
              智能化的数据统计分析
            </li>
            <li>
              <el-icon><CircleCheck /></el-icon>
              完善的权限管理体系
            </li>
            <li>
              <el-icon><CircleCheck /></el-icon>
              实时的消息通知系统
            </li>
          </ul>
        </div>

        <div class="info-card">
          <h3>安全提醒</h3>
          <ul class="security-list">
            <li>请使用授权的管理员账号登录</li>
            <li>定期更换登录密码</li>
            <li>不要在公共设备上保存登录状态</li>
            <li>发现异常情况请及时联系技术支持</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  Monitor, User, Lock, Warning, CircleCheck
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const loginFormRef = ref()

// 登录表单
const loginForm = reactive({
  username: 'admin',
  password: 'admin123',
  remember: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 3, max: 20, message: '账号长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) return

    loading.value = true

    console.log('尝试管理员登录，用户名:', loginForm.username)

    // 调用后端登录API
    const response = await request({
      url: '/api/user/login',
      method: 'post',
      data: {
        username: loginForm.username,
        password: loginForm.password
      }
    })

    console.log('后端登录响应:', response)

    if (response.code === 200 && response.data) {
      const loginData = response.data
      const userInfo = loginData.userInfo

      console.log('登录成功，用户信息:', userInfo)

      // 检查用户是否是管理员（通过用户名判断，后续可以改为角色判断）
      if (userInfo.username === 'admin') {
        // 保存管理员登录状态
        localStorage.setItem('adminToken', loginData.token)
        localStorage.setItem('adminInfo', JSON.stringify(userInfo))

        ElMessage.success('管理员登录成功')

        // 跳转到仲裁管理页
        try {
          await router.push('/admin/arbitration/pending')
          console.log('成功跳转到仲裁管理页')
        } catch (err) {
          console.error('路由跳转失败:', err)
          window.location.replace('/admin/arbitration/pending')
        }
      } else {
        ElMessage.error('当前账号没有管理员权限')
      }
    } else {
      ElMessage.error(response.message || '登录失败')
    }
  } catch (error) {
    console.error('登录错误：', error)
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.message || '登录失败')
    } else {
      ElMessage.error(error.message || '登录失败')
    }
  } finally {
    loading.value = false
  }
}

// 忘记密码
const showForgotPassword = () => {
  ElMessage.info('请联系系统管理员重置密码')
}

// 页面初始化
onMounted(() => {
  // 如果已经登录，直接跳转
  const adminToken = localStorage.getItem('adminToken')
  if (adminToken) {
    console.log('检测到管理员token，跳转到仲裁管理页')
    router.push('/admin/arbitration/pending').catch(err => {
      console.error('初始化跳转失败:', err)
      window.location.replace('/admin/arbitration/pending')
    })
  }
})
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

/* 背景 */
.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
}

/* 登录容器 */
.login-container {
  display: flex;
  max-width: 1200px;
  width: 90%;
  min-height: 600px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 登录卡片 */
.login-card {
  flex: 1;
  max-width: 500px;
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 16px;
}

.logo h1 {
  margin: 0;
  color: #303133;
  font-size: 28px;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
  font-weight: 300;
  letter-spacing: 1px;
}

/* 登录表单 */
.login-form {
  margin-bottom: 30px;
}

.login-form .el-form-item {
  margin-bottom: 24px;
}

.login-form .el-input {
  border-radius: 8px;
}


.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
}

/* 登录页脚 */
.login-footer {
  text-align: center;
}

.security-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #67c23a;
  font-size: 14px;
  margin-bottom: 16px;
}

.copyright {
  color: #909399;
  font-size: 12px;
}

/* 系统信息侧边栏 */
.system-info {
  flex: 1;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  padding: 60px 40px;
  color: white;
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.info-card h3 {
  margin: 0 0 20px 0;
  font-size: 20px;
  font-weight: 500;
}

.feature-list,
.security-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 14px;
  opacity: 0.9;
}

.security-list li {
  margin-bottom: 12px;
  font-size: 13px;
  opacity: 0.8;
  line-height: 1.5;
  padding-left: 16px;
  position: relative;
}

.security-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: rgba(255, 255, 255, 0.6);
}

/* 响应式适配 */
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
    width: 95%;
    min-height: auto;
    margin: 20px;
  }

  .login-card {
    padding: 40px 30px;
  }

  .system-info {
    display: none;
  }

  .logo h1 {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .login-card {
    padding: 30px 20px;
  }

  .login-container {
    margin: 10px;
    border-radius: 12px;
  }
}
</style>
