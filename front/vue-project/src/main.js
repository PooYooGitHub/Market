console.log('=== main.js 开始执行 ===')

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

// 全局样式
import './styles/global.css'

console.log('所有模块导入完成')

try {
  console.log('开始创建Vue应用...')

  const app = createApp(App)
  const pinia = createPinia()

  console.log('注册Element Plus图标...')
  // 注册Element Plus图标
  for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
  }

  console.log('使用插件...')
  // 使用插件
  app.use(router)
  app.use(pinia)
  app.use(ElementPlus, {
    locale: zhCn, // 中文语言包
  })

  console.log('挂载应用到#app...')
  // 挂载应用
  app.mount('#app')

  console.log('✅ Vue应用挂载成功！')

} catch (error) {
  console.error('❌ Vue应用启动失败：', error)

  // 如果Vue应用启动失败，显示错误信息
  setTimeout(() => {
    const appEl = document.getElementById('app')
    if (appEl) {
      appEl.innerHTML = `
        <div style="padding: 40px; text-align: center; font-family: Arial, sans-serif; background: #f5f5f5; min-height: 100vh;">
          <h1 style="color: #f56c6c; margin-bottom: 20px;">❌ 应用启动失败</h1>
          <p style="color: #666; font-size: 16px; margin-bottom: 20px;">错误信息：${error.message}</p>
          <div style="background: white; padding: 20px; border-radius: 8px; text-align: left; max-width: 600px; margin: 0 auto;">
            <pre style="color: #333; font-size: 12px; overflow: auto;">${error.stack}</pre>
          </div>
          <button onclick="location.reload()" style="background: #409EFF; color: white; padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; margin-top: 20px;">
            刷新页面
          </button>
        </div>
      `
    }
  }, 100)
}
