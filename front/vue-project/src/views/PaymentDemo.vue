<template>
  <div class="demo-container">
    <div class="demo-header">
      <h1>🎓 毕业设计支付系统演示</h1>
      <p class="demo-subtitle">完整的支付流程体验，适合毕业设计展示</p>
    </div>

    <div class="demo-content">
      <!-- 功能特性 -->
      <section class="feature-section">
        <h2>✨ 支付系统特性</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">💳</div>
            <h3>多种支付方式</h3>
            <p>支持支付宝、微信、余额、银行卡等多种支付方式</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🔒</div>
            <h3>安全支付环境</h3>
            <p>模拟真实支付界面，包含安全验证和加密传输</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📊</div>
            <h3>完整支付记录</h3>
            <p>详细的支付历史、状态跟踪和退款管理</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">⚡</div>
            <h3>流程简化</h3>
            <p>针对毕业设计优化，演示效果好且易于理解</p>
          </div>
        </div>
      </section>

      <!-- 演示流程 -->
      <section class="demo-section">
        <h2>🎯 演示流程</h2>
        <div class="demo-steps">
          <div class="demo-step">
            <div class="step-number">1</div>
            <div class="step-content">
              <h3>浏览商品</h3>
              <p>在商品列表中选择心仪的商品</p>
              <button class="btn btn-primary" @click="$router.push('/products')">
                前往商品列表
              </button>
            </div>
          </div>

          <div class="demo-step">
            <div class="step-number">2</div>
            <div class="step-content">
              <h3>加入购物车</h3>
              <p>将商品添加到购物车，可以批量选择</p>
              <button class="btn btn-primary" @click="$router.push('/cart')">
                查看购物车
              </button>
            </div>
          </div>

          <div class="demo-step">
            <div class="step-number">3</div>
            <div class="step-content">
              <h3>创建订单</h3>
              <p>从购物车结算，生成待支付订单</p>
              <button class="btn btn-primary" @click="$router.push('/orders')">
                我的订单
              </button>
            </div>
          </div>

          <div class="demo-step">
            <div class="step-number">4</div>
            <div class="step-content">
              <h3>完成支付</h3>
              <p>选择支付方式，体验完整支付流程</p>
              <button class="btn btn-primary" @click="$router.push('/payment-history')">
                支付记录
              </button>
            </div>
          </div>
        </div>
      </section>

      <!-- 技术架构 -->
      <section class="tech-section">
        <h2>🏗️ 技术架构</h2>
        <div class="tech-grid">
          <div class="tech-item">
            <h4>前端技术</h4>
            <ul>
              <li>Vue 3 + Composition API</li>
              <li>Element Plus UI组件库</li>
              <li>Vue Router 4</li>
              <li>Pinia 状态管理</li>
              <li>Axios HTTP客户端</li>
            </ul>
          </div>
          <div class="tech-item">
            <h4>后端架构</h4>
            <ul>
              <li>Spring Cloud微服务</li>
              <li>Spring Cloud Gateway</li>
              <li>Nacos服务注册与发现</li>
              <li>RocketMQ消息队列</li>
              <li>MySQL数据库</li>
            </ul>
          </div>
          <div class="tech-item">
            <h4>支付特性</h4>
            <ul>
              <li>模拟第三方支付界面</li>
              <li>支付状态实时更新</li>
              <li>支付记录完整追踪</li>
              <li>退款申请流程</li>
              <li>余额支付验证</li>
            </ul>
          </div>
        </div>
      </section>

      <!-- 演示数据 -->
      <section class="demo-data-section">
        <h2>🎮 演示数据说明</h2>
        <div class="demo-notes">
          <div class="note-card">
            <h4>💰 余额支付</h4>
            <p>系统会模拟用户余额，支付密码为任意6位数字</p>
          </div>
          <div class="note-card">
            <h4>🏦 银行卡支付</h4>
            <p>银行卡号、有效期、CVV等信息为演示用途，无需真实数据</p>
          </div>
          <div class="note-card">
            <h4>📱 扫码支付</h4>
            <p>支付宝、微信支付会显示模拟二维码，自动完成支付</p>
          </div>
          <div class="note-card">
            <h4>✅ 支付成功率</h4>
            <p>为了演示效果，支付成功率设置为80%，失败时可重试</p>
          </div>
        </div>
      </section>

      <!-- 快速体验 -->
      <section class="quick-demo-section">
        <h2>⚡ 快速体验</h2>
        <div class="quick-actions">
          <button class="quick-btn" @click="createTestData">
            <div class="quick-icon">🛍️</div>
            <div>创建测试订单</div>
          </button>
          <button class="quick-btn" @click="showPaymentDemo">
            <div class="quick-icon">💳</div>
            <div>支付流程演示</div>
          </button>
          <button class="quick-btn" @click="showFeatures">
            <div class="quick-icon">📋</div>
            <div>功能清单</div>
          </button>
        </div>
      </section>
    </div>

    <!-- 功能清单弹窗 -->
    <div v-if="showFeaturesModal" class="modal-overlay" @click.self="showFeaturesModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>📋 支付系统功能清单</h3>
          <button class="close-btn" @click="showFeaturesModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="feature-list">
            <div class="feature-group">
              <h4>🛒 购物车功能</h4>
              <ul>
                <li>✅ 添加商品到购物车</li>
                <li>✅ 购物车商品管理</li>
                <li>✅ 批量选择和结算</li>
                <li>✅ 购物车数量统计</li>
              </ul>
            </div>
            <div class="feature-group">
              <h4>💳 支付功能</h4>
              <ul>
                <li>✅ 多种支付方式选择</li>
                <li>✅ 支付界面模拟</li>
                <li>✅ 支付状态实时更新</li>
                <li>✅ 支付成功/失败处理</li>
                <li>✅ 余额支付验证</li>
              </ul>
            </div>
            <div class="feature-group">
              <h4>📊 记录管理</h4>
              <ul>
                <li>✅ 支付历史记录</li>
                <li>✅ 支付详情查看</li>
                <li>✅ 支付状态筛选</li>
                <li>✅ 退款申请功能</li>
                <li>✅ 支付统计信息</li>
              </ul>
            </div>
            <div class="feature-group">
              <h4>🔧 系统特性</h4>
              <ul>
                <li>✅ 响应式设计</li>
                <li>✅ 用户体验优化</li>
                <li>✅ 错误处理机制</li>
                <li>✅ 加载状态提示</li>
                <li>✅ 数据验证完整</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const showFeaturesModal = ref(false)

// 创建测试数据
const createTestData = () => {
  alert('💡 提示：\n\n' +
        '1. 请先在"商品列表"页面浏览商品\n' +
        '2. 将感兴趣的商品添加到购物车\n' +
        '3. 在购物车页面进行结算\n' +
        '4. 体验完整的支付流程')
  router.push('/products')
}

// 显示支付演示
const showPaymentDemo = () => {
  alert('💳 支付流程演示：\n\n' +
        '1. 支付宝 - 扫码支付演示\n' +
        '2. 微信支付 - 扫码支付演示\n' +
        '3. 余额支付 - 密码验证演示\n' +
        '4. 银行卡 - 卡片信息演示\n\n' +
        '点击确定后将跳转到支付记录页面查看历史记录')
  router.push('/payment-history')
}

// 显示功能清单
const showFeatures = () => {
  showFeaturesModal.value = true
}
</script>

<style scoped>
.demo-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
  background: #f8fafc;
  min-height: calc(100vh - 140px);
}

.demo-header {
  text-align: center;
  margin-bottom: 60px;
}

.demo-header h1 {
  font-size: 42px;
  font-weight: bold;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 16px;
}

.demo-subtitle {
  font-size: 18px;
  color: #64748b;
  margin: 0;
}

.demo-content {
  display: flex;
  flex-direction: column;
  gap: 60px;
}

/* 特性展示 */
.feature-section h2,
.demo-section h2,
.tech-section h2,
.demo-data-section h2,
.quick-demo-section h2 {
  font-size: 28px;
  font-weight: bold;
  color: #1e293b;
  margin: 0 0 32px;
  text-align: center;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.feature-card {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  text-align: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.feature-card h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px;
}

.feature-card p {
  font-size: 16px;
  color: #64748b;
  margin: 0;
  line-height: 1.6;
}

/* 演示流程 */
.demo-steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 32px;
}

.demo-step {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.step-number {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 20px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.step-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}

.step-content p {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 16px;
  line-height: 1.6;
}

/* 技术架构 */
.tech-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 24px;
}

.tech-item {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.tech-item h4 {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px;
}

.tech-item ul {
  margin: 0;
  padding-left: 20px;
  list-style-type: disc;
}

.tech-item li {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 8px;
  line-height: 1.5;
}

/* 演示说明 */
.demo-notes {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
}

.note-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.note-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}

.note-card p {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

/* 快速体验 */
.quick-actions {
  display: flex;
  justify-content: center;
  gap: 24px;
  flex-wrap: wrap;
}

.quick-btn {
  background: white;
  border: 2px solid #e2e8f0;
  border-radius: 16px;
  padding: 24px 32px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
  min-width: 140px;
}

.quick-btn:hover {
  border-color: #667eea;
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
}

.quick-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.quick-btn div:last-child {
  font-size: 14px;
  font-weight: 500;
  color: #1e293b;
}

/* 按钮样式 */
.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 功能清单弹窗 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  backdrop-filter: blur(4px);
}

.modal {
  background: white;
  border-radius: 16px;
  width: 700px;
  max-width: 95vw;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
}

.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f1f5f9;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  color: #64748b;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #e2e8f0;
  color: #1e293b;
}

.modal-body {
  padding: 32px;
  max-height: 60vh;
  overflow-y: auto;
}

.feature-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
}

.feature-group h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px;
}

.feature-group ul {
  margin: 0;
  padding-left: 20px;
}

.feature-group li {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 6px;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .demo-container {
    padding: 20px 16px;
  }

  .demo-header h1 {
    font-size: 32px;
  }

  .demo-subtitle {
    font-size: 16px;
  }

  .demo-content {
    gap: 40px;
  }

  .features-grid,
  .demo-steps,
  .tech-grid,
  .demo-notes {
    grid-template-columns: 1fr;
  }

  .demo-step {
    flex-direction: column;
    text-align: center;
  }

  .quick-actions {
    flex-direction: column;
    align-items: center;
  }

  .modal {
    width: 95vw;
  }

  .feature-list {
    grid-template-columns: 1fr;
  }
}
</style>