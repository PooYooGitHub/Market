<template>
  <div class="profile-container">
    <div class="profile-content">
      <!-- 用户信息卡片 -->
      <div class="info-card">
        <h3>基本信息</h3>
        
        <!-- 头像展示 -->
        <div class="avatar-display">
          <img 
            :src="userInfo?.avatar || defaultAvatar" 
            alt="用户头像"
            @error="handleAvatarError"
            class="avatar-image"
          />
        </div>
        
        <div class="info-item">
          <label>用户名：</label>
          <span>{{ userInfo?.username }}</span>
        </div>
        <div class="info-item">
          <label>昵称：</label>
          <span>{{ userInfo?.nickname }}</span>
        </div>
        <div class="info-item">
          <label>邮箱：</label>
          <span>{{ userInfo?.email || '未设置' }}</span>
        </div>
        <div class="info-item">
          <label>注册时间：</label>
          <span>{{ formatDate(userInfo?.createTime) }}</span>
        </div>
        <button @click="handleShowEditModal" class="btn btn-primary">
          编辑信息
        </button>
      </div>

      <!-- 信用信息卡片 -->
      <div class="info-card">
        <h3>信用信息</h3>
        <div v-if="creditInfo" class="credit-section">
          <div class="credit-display">
            <div class="credit-badge">
              <div class="credit-score" :style="{ color: creditInfo.levelColor }">
                {{ creditInfo.score }}
              </div>
              <div class="credit-level">{{ creditInfo.badgeName || creditInfo.level }}</div>
            </div>
            <div class="credit-stats">
              <div class="stat-item">
                <span class="stat-label">总评价</span>
                <span class="stat-value">{{ creditInfo.totalEvaluations }}</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">好评率</span>
                <span class="stat-value">{{ creditInfo.goodRate }}%</span>
              </div>
            </div>
          </div>
          <button @click="viewCreditDetail" class="btn btn-primary">
            查看详情
          </button>
        </div>
        <div v-else-if="creditInfoError" class="credit-error">
          <p>信用信息暂时无法加载，请稍后再试</p>
        </div>
        <div v-else class="credit-loading">
          <p>加载信用信息中...</p>
        </div>
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
            <label for="editAvatar">头像</label>
            <div class="avatar-upload-container">
              <div class="avatar-preview" @click="triggerAvatarInput">
                <img 
                  :src="avatarPreview || editForm.avatar || defaultAvatar" 
                  alt="头像预览"
                  @error="handleAvatarError"
                />
                <div class="avatar-upload-overlay">
                  <span v-if="!uploadingAvatar">点击上传</span>
                  <span v-else>上传中...</span>
                </div>
              </div>
              <input
                ref="avatarInput"
                type="file"
                accept="image/jpeg,image/jpg,image/png,image/gif,image/webp"
                style="display: none"
                @change="handleAvatarSelect"
              />
              <div class="avatar-upload-tip">
                支持 JPG、PNG、GIF、WEBP 格式，最大 2MB
              </div>
            </div>
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
import { uploadAvatar, validateImageFile } from '@/api/file'
import { getCreditInfo } from '@/api/credit'

const router = useRouter()
const userStore = useUserStore()

// 用户信息
const userInfo = computed(() => userStore.userInfo)

// 信用信息
const creditInfo = ref(null)
const creditInfoError = ref(false)

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

// 头像上传相关
const avatarInput = ref(null)
const avatarPreview = ref('')
const uploadingAvatar = ref(false)

// 默认头像（base64 编码的灰色头像占位图）
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KICA8cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2Y1ZjVmNSIvPgogIDxjaXJjbGUgY3g9IjUwIiBjeT0iNDAiIHI9IjE1IiBmaWxsPSIjZGRkIi8+CiAgPHBhdGggZD0iTSAzMCA3MCBRIDMwIDU1IDUwIDU1IFEgNzAgNTUgNzAgNzAgTCA3MCA4MCBRIDcwIDkwIDUwIDkwIFEgMzAgOTAgMzAgODAgWiIgZmlsbD0iI2RkZCIvPgo8L3N2Zz4='

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
  // 清空头像预览
  avatarPreview.value = ''
}

// 触发头像选择
const triggerAvatarInput = () => {
  if (!uploadingAvatar.value) {
    avatarInput.value?.click()
  }
}

// 头像选择处理
const handleAvatarSelect = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  
  // 验证文件
  const validation = validateImageFile(file, { maxSize: 2 })
  if (!validation.valid) {
    alert(validation.message)
    event.target.value = ''
    return
  }
  
  // 创建本地预览
  avatarPreview.value = URL.createObjectURL(file)
  
  // 上传头像
  uploadingAvatar.value = true
  
  try {
    const res = await uploadAvatar(file)
    
    if (res.code === 200) {
      // 上传成功，更新表单中的头像URL
      console.log('头像上传成功:', res.data)
      editForm.value.avatar = res.data
      // 清空本地预览，显示服务器上的图片
      URL.revokeObjectURL(avatarPreview.value)
      avatarPreview.value = ''
      alert('头像上传成功！请点击保存按钮完成更新')
    } else {
      throw new Error(res.message || '上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    // 根据错误类型给出更具体的提示
    if (error.response?.status === 500) {
      alert('文件上传服务暂时不可用，请稍后再试')
    } else if (error.response?.status === 413) {
      alert('头像文件太大，请选择小于2MB的图片')
    } else if (error.response?.status === 401) {
      alert('登录已过期，请重新登录')
      userStore.logout()
    } else {
      alert('头像上传失败，请检查网络连接或文件格式')
    }
    // 恢复原头像
    URL.revokeObjectURL(avatarPreview.value)
    avatarPreview.value = ''
  } finally {
    uploadingAvatar.value = false
    event.target.value = ''
  }
}

// 头像加载失败处理
const handleAvatarError = (e) => {
  // 防止无限循环：如果已经是默认头像，就移除 error 事件监听
  if (e.target.src === defaultAvatar || e.target.src.startsWith('data:image/svg')) {
    e.target.onerror = null
    return
  }
  // 设置为默认头像
  e.target.src = defaultAvatar
}

// 更新用户信息
const handleUpdateInfo = async () => {
  loading.value = true

  try {
    console.log('提交的表单数据:', editForm.value)
    const res = await updateUser(editForm.value)

    if (res.code === 200) {
      console.log('更新成功，后端返回:', res.data)
      alert('更新成功')
      // 重新获取最新的用户信息
      await userStore.getUserInfo()
      console.log('更新后的用户信息:', userStore.userInfo)
      // 清空预览
      if (avatarPreview.value) {
        URL.revokeObjectURL(avatarPreview.value)
        avatarPreview.value = ''
      }
      showEditModal.value = false
    } else {
      alert(res.message || '更新失败')
    }
  } catch (error) {
    console.error('更新失败:', error)
    // 根据错误类型给出更具体的提示
    if (error.response?.status === 500) {
      // 对于服务异常，提供更友好的处理
      const confirmLocalSave = confirm('用户服务暂时不可用，是否在本地暂存修改？\n（重新登录后会恢复为服务器上的信息）')
      if (confirmLocalSave) {
        // 本地更新用户信息显示
        userStore.updateUserInfo({
          nickname: editForm.value.nickname,
          email: editForm.value.email
        })
        alert('信息已在本地暂存，服务恢复后请重新保存')
        showEditModal.value = false
      }
    } else if (error.response?.status === 401) {
      alert('登录已过期，请重新登录')
      userStore.logout()
    } else {
      alert('网络连接异常，请检查网络后重试')
    }
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


// 查看信用详情
const viewCreditDetail = () => {
  router.push('/credit')
}

// 加载信用信息
const loadCreditInfo = async () => {
  try {
    creditInfoError.value = false
    const response = await getCreditInfo()
    if (response.success) {
      creditInfo.value = response.data
    }
  } catch (error) {
    creditInfoError.value = true
    // 静默处理信用服务错误，不在控制台输出详细错误信息
    if (error.response?.status === 500) {
      console.warn('信用服务暂时不可用，将显示友好提示')
    } else {
      console.error('加载信用信息失败:', error.message || error)
    }
    creditInfo.value = null
  }
}

// 页面加载时初始化
onMounted(() => {
  initEditForm()
  loadCreditInfo()
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

/* 头像展示 */
.avatar-display {
  text-align: center;
  margin: 20px 0;
}

.avatar-image {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid #667eea;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 头像上传 */
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.avatar-preview {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid #ddd;
  transition: all 0.3s;
}

.avatar-preview:hover {
  border-color: #667eea;
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 14px;
}

.avatar-preview:hover .avatar-upload-overlay {
  opacity: 1;
}

.avatar-upload-tip {
  font-size: 12px;
  color: #999;
  text-align: center;
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

/* 信用信息样式 */
.credit-section {
  padding: 20px 0;
}

.credit-display {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 20px;
}

.credit-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
  border-radius: 12px;
  border: 1px solid #dee2e6;
  min-width: 120px;
}

.credit-score {
  font-size: 36px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 5px;
}

.credit-level {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.credit-stats {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr;
  gap: 15px;
}

.credit-stats .stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #f8f9fa;
  border-radius: 8px;
  border: none;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

.stat-value {
  color: #303133;
  font-weight: 500;
  font-size: 16px;
}

.credit-loading {
  text-align: center;
  padding: 40px 0;
  color: #999;
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
