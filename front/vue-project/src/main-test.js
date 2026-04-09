console.log('=== main-test.js 开始执行 ===')

// 先测试最基本的DOM操作
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM 已加载')
    const app = document.getElementById('app')
    if (app) {
        console.log('找到 #app 元素')
        app.innerHTML = `
            <div style="padding: 40px; text-align: center; font-family: Arial, sans-serif; background: #f5f5f5; min-height: 100vh;">
                <h1 style="color: #333; margin-bottom: 20px;">🎯 基础测试成功！</h1>
                <p style="color: #666; font-size: 16px;">时间: ${new Date().toLocaleString()}</p>
                <div style="margin: 30px 0;">
                    <button onclick="testVue()" style="background: #409EFF; color: white; padding: 12px 24px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px;">
                        测试Vue加载
                    </button>
                </div>
                <div id="vue-test" style="margin-top: 20px;"></div>
                <div style="background: white; padding: 20px; border-radius: 8px; margin: 20px 0; text-align: left;">
                    <h3>调试信息:</h3>
                    <ul>
                        <li>页面标题: ${document.title}</li>
                        <li>URL: ${window.location.href}</li>
                        <li>用户代理: ${navigator.userAgent.substring(0, 50)}...</li>
                    </ul>
                </div>
            </div>
        `
        console.log('页面内容已设置')
    } else {
        console.error('未找到 #app 元素')
    }
})

// 测试Vue功能
window.testVue = function() {
    console.log('开始测试Vue...')
    const testDiv = document.getElementById('vue-test')

    try {
        // 动态导入Vue
        import('vue').then(Vue => {
            console.log('Vue 导入成功:', Vue)
            testDiv.innerHTML = '<p style="color: green;">✅ Vue 导入成功！版本: 3.x</p>'

            // 尝试创建Vue应用
            const app = Vue.createApp({
                template: '<div>🎉 Vue应用创建成功！当前时间: {{ time }}</div>',
                data() {
                    return { time: new Date().toLocaleString() }
                }
            })

            // 创建一个新的div来挂载Vue
            const vueDiv = document.createElement('div')
            vueDiv.id = 'vue-mount'
            testDiv.appendChild(vueDiv)

            app.mount('#vue-mount')
            console.log('Vue应用挂载成功')

        }).catch(error => {
            console.error('Vue 导入失败:', error)
            testDiv.innerHTML = '<p style="color: red;">❌ Vue 导入失败: ' + error.message + '</p>'
        })
    } catch (error) {
        console.error('Vue测试错误:', error)
        testDiv.innerHTML = '<p style="color: red;">❌ Vue测试错误: ' + error.message + '</p>'
    }
}

console.log('=== main-test.js 执行完毕 ===')