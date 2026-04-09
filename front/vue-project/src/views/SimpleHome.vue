<template>
  <div class="simple-home">
    <div class="hero">
      <h1>欢迎来到校园跳蚤市场</h1>
      <p>让闲置物品流转起来，让资源得到更好的利用</p>
      <div class="hero-buttons">
        <el-button type="primary" @click="$router.push('/user/login')">立即登录</el-button>
        <el-button @click="$router.push('/user/register')">免费注册</el-button>
      </div>
    </div>

    <div class="features">
      <h2>平台特色</h2>
      <el-row :gutter="20">
        <el-col :span="8">
          <div class="feature-card">
            <h3>💱 便捷交易</h3>
            <p>简单快捷的交易流程</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-card">
            <h3>🔒 安全保障</h3>
            <p>多重安全验证机制</p>
          </div>
        </el-col>
        <el-col :span="8">
          <div class="feature-card">
            <h3>🌍 校园专属</h3>
            <p>专为校园师生设计</p>
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SimpleHome'
}
</script>

<style scoped>
.simple-home {
  padding: 20px;
}

.hero {
  text-align: center;
  padding: 60px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 12px;
  margin-bottom: 40px;
}

.hero h1 {
  font-size: 2.5rem;
  margin-bottom: 20px;
}

.hero p {
  font-size: 1.2rem;
  margin-bottom: 30px;
  opacity: 0.9;
}

.hero-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
}

.features {
  text-align: center;
  padding: 40px 0;
}

.features h2 {
  margin-bottom: 40px;
  color: #2c3e50;
}

.feature-card {
  padding: 30px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  height: 160px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.feature-card h3 {
  font-size: 1.3rem;
  margin-bottom: 15px;
  color: #667eea;
}

.feature-card p {
  color: #7f8c8d;
  line-height: 1.6;
}
</style>