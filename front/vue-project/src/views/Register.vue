<template>
  <div class="register-container">
    <div class="register-box">
      <h2 class="title">校园跳蚤市场</h2>
      <h3 class="subtitle">用户注册</h3>
      
      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-group">
          <label for="username">用户名 <span class="required">*</span></label>
          <input
            id="username"
            v-model="registerForm.username"
            type="text"
            placeholder="4-20位字母、数字或下划线"
            required
            autocomplete="username"
          />
        </div>

        <div class="form-group">
          <label for="password">密码 <span class="required">*</span></label>
          <input
            id="password"
            v-model="registerForm.password"
            type="password"
            placeholder="6-20位"
            required
            autocomplete="new-password"
          />
        </div>

        <div class="form-group">
          <label for="confirmPassword">确认密码 <span class="required">*</span></label>
          <input
            id="confirmPassword"
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            required
            autocomplete="new-password"
          />
        </div>

        <div class="form-group">
          <label for="nickname">昵称</label>
          <input
            id="nickname"
            v-model="registerForm.nickname"
            type="text"
            placeholder="选填，不填则默认为用户名"
          />
        </div>

        <div class="form-group">
          <label for="phone">手机号</label>
          <input
            id="phone"
            v-model="registerForm.phone"
            type="tel"
            placeholder="选填"
          />
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <input
            id="email"
            v-model="registerForm.email"
            type="email"
            placeholder="选填"
          />
        </div>

        <button type="submit" class="register-button" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <div class="link-group">
          <router-link to="/login" class="link">已有账号？立即登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'

const router = useRouter()

// 表单数据
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: ''
})

// 加载状态
const loading = ref(false)

// 前端表单验证
const validateForm = () => {
  const { username, password, confirmPassword, phone, email } = registerForm.value
  
  // 用户名验证
  if (!username.trim()) {
    alert('请输入用户名')
    return false
  }
  
  const usernameRegex = /^[a-zA-Z0-9_]{4,20}$/
  if (!usernameRegex.test(username)) {
    alert('用户名必须是4-20位字母、数字或下划线')
    return false
  }
  
  // 密码验证
  if (!password) {
    alert('请输入密码')
    return false
  }
  
  if (password.length < 6 || password.length > 20) {
    alert('密码长度必须在6-20位之间')
    return false
  }
  
  // 确认密码验证
  if (password !== confirmPassword) {
    alert('两次密码输入不一致')
    return false
  }
  
  // 手机号验证（如果填写）
  if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
    alert('手机号格式不正确')
    return false
  }
  
  // 邮箱验证（如果填写）
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    alert('邮箱格式不正确')
    return false
  }
  
  return true
}

// 处理注册
const handleRegister = async () => {
  // 前端验证
  if (!validateForm()) {
    return
  }

  loading.value = true

  try {
    const res = await register(registerForm.value)
    
    if (res.code === 200) {
      alert('注册成功，请登录')
      // 跳转到登录页
      router.push('/login')
    } else {
      alert(res.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    alert('注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-box {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
  padding: 40px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow-y: auto;
}

.title {
  text-align: center;
  color: #333;
  font-size: 28px;
  margin-bottom: 8px;
  font-weight: 600;
}

.subtitle {
  text-align: center;
  color: #666;
  font-size: 18px;
  margin-bottom: 30px;
  font-weight: 400;
}

.register-form {
  width: 100%;
}

.form-group {
  margin-bottom: 18px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.required {
  color: #e74c3c;
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

.register-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.register-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.link-group {
  margin-top: 20px;
  text-align: center;
}

.link {
  color: #667eea;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.link:hover {
  color: #764ba2;
  text-decoration: underline;
}
</style>
