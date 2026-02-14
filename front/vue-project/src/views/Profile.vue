<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人中心</h2>
    </div>

    <div class="profile-content">
      <!-- 用户信息卡片 -->
      <div class="info-card">
        <h3>基本信息</h3>
        <div class="info-item">
          <label>用户名：</label>
          <span>{{ userInfo?.username }}</span>
        </div>
        <div class="info-item">
          <label>昵称：</label>
          <span>{{ userInfo?.nickname }}</span>
        </div>
        <div class="info-item">
          <label>手机号：</label>
          <span>{{ userInfo?.phone || '未设置' }}</span>
        </div>
        <div class="info-item">
          <label>邮箱：</label>
          <span>{{ userInfo?.email || '未设置' }}</span>
        </div>
        <div class="info-item">
          <label>注册时间：</label>
          <span>{{ formatDate(userInfo?.createTime) }}</span>
        </div>
        <button @click="showEditModal = true" class="btn btn-primary">
          编辑信息
        </button>
      </div>

      <!-- 修改密码卡片 -->
      <div class="info-card">
        <h3>修改密码</h3>
        <form @submit.prevent="handleChangePassword" class="password-form">
          <div class="form-group">
            <label for="oldPassword">旧密码</label>
            <input
              id="oldPassword"
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入旧密码"
              required
            />
          </div>
          <div class="form-group">
            <label for="newPassword">新密码</label>
            <input
              id="newPassword"
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="6-20位"
              required
            />
          </div>
          <div class="form-group">
            <label for="confirmPassword">确认新密码</label>
            <input
              id="confirmPassword"
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              required
            />
          </div>
          <button type="submit" class="btn btn-danger" :disabled="loading">
            {{ loading ? '修改中...' : '修改密码' }}
          </button>
        </form>
      </div>

      <!-- 退出登录 -->
      <div class="info-card">
        <button @click="handleLogout" class="btn btn-outline">
          退出登录
        </button>
      </div>
    </div>

    <!-- 编辑信息模态框 -->
    <div v-if="showEditModal" class="modal" @click.self="showEditModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑个人信息</h3>
          <button @click="showEditModal = false" class="close-btn">&times;</button>
        </div>
        <form @submit.prevent="handleUpdateInfo" class="modal-body">
          <div class="form-group">
            <label for="editNickname">昵称</label>
            <input
              id="editNickname"
              v-model="editForm.nickname"
              type="text"
              placeholder="请输入昵称"
            />
          </div>
          <div class="form-group">
            <label for="editEmail">邮箱</label>
            <input
              id="editEmail"
              v-model="editForm.email"
              type="email"
              placeholder="请输入邮箱"
            />
          </div>
          <div class="form-group">
            <label for="editAvatar">头像URL</label>
            <input
              id="editAvatar"
              v-model="editForm.avatar"
              type="text"
              placeholder="请输入头像URL"
            />
          </div>
          <div class="modal-footer">
            <button type="button" @click="showEditModal = false" class="btn btn-outline">
              取消
            </button>
            <button type="submit" class="btn btn-primary" :disabled="loading">
              {{ loading ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateUser, changePassword } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 编辑表单
const editForm = ref({
  nickname: '',
  email: '',
  avatar: ''
})

// 密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 模态框显示状态
const showEditModal = ref(false)
const loading = ref(false)

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

// 初始化编辑表单
const initEditForm = () => {
  editForm.value = {
    nickname: userInfo.value?.nickname || '',
    email: userInfo.value?.email || '',
    avatar: userInfo.value?.avatar || ''
  }
}

// 更新用户信息
const handleUpdateInfo = async () => {
  loading.value = true
  
  try {
    const res = await updateUser(editForm.value)
    
    if (res.code === 200) {
      alert('更新成功')
      // 更新本地用户信息
      userStore.updateUserInfo(res.data)
      showEditModal.value = false
    } else {
      alert(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    alert('更新失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 修改密码
const handleChangePassword = async () => {
  // 验证
  if (passwordForm.value.newPassword.length < 6 || passwordForm.value.newPassword.length > 20) {
    alert('新密码长度必须在6-20位之间')
    return
  }
  
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    alert('两次密码输入不一致')
    return
  }

  loading.value = true

  try {
    const res = await changePassword(passwordForm.value)
    
    if (res.code === 200) {
      alert('密码修改成功，请重新登录')
      // 登出
      userStore.logout()
    } else {
      alert(res.message || '修改失败')
    }
  } catch (error) {
    console.error('修改密码失败:', error)
    alert('修改密码失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 退出登录
const handleLogout = () => {
  if (confirm('确定要退出登录吗？')) {
    userStore.logout()
  }
}

// 页面加载时初始化
onMounted(() => {
  initEditForm()
  // 如果没有登录，跳转到登录页
  if (!userStore.isLoggedIn) {
    router.push('/login')
  }
})

// 监听编辑模态框打开，重新初始化表单
const handleShowEditModal = () => {
  initEditForm()
  showEditModal.value = true
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.profile-header {
  max-width: 800px;
  margin: 0 auto 30px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.profile-header h2 {
  margin: 0;
  color: #333;
}

.profile-content {
  max-width: 800px;
  margin: 0 auto;
}

.info-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.info-card h3 {
  margin: 0 0 20px;
  color: #333;
  font-size: 18px;
  border-bottom: 2px solid #667eea;
  padding-bottom: 10px;
}

.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-of-type {
  border-bottom: none;
}

.info-item label {
  min-width: 100px;
  color: #666;
  font-weight: 500;
}

.info-item span {
  color: #333;
}

.password-form .form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-danger {
  background: #e74c3c;
  color: white;
}

.btn-danger:hover:not(:disabled) {
  background: #c0392b;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
}

.btn-outline {
  background: white;
  color: #667eea;
  border: 1px solid #667eea;
}

.btn-outline:hover {
  background: #667eea;
  color: white;
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
}

.close-btn:hover {
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}
</style>
