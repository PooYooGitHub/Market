/**
 * 前端诊断脚本
 * 用于检查代码语法和依赖问题
 */

console.log('=== 前端诊断工具 ===\n')

// 1. 检查 Node.js 版本
console.log('1. 检查 Node.js 版本:')
console.log(`   Node: ${process.version}`)
console.log(`   推荐: v18.x 或更高版本\n`)

// 2. 检查关键文件是否存在
const fs = require('fs')
const path = require('path')

console.log('2. 检查关键文件:')
const criticalFiles = [
  'package.json',
  'vite.config.js',
  'index.html',
  'src/main.js',
  'src/App.vue',
  'src/router/index.js',
  'src/stores/arbitration.js',
  'src/stores/user.js',
  'src/components/MainLayout.vue'
]

criticalFiles.forEach(file => {
  const exists = fs.existsSync(path.join(__dirname, file))
  console.log(`   ${exists ? '✓' : '✗'} ${file}`)
})
console.log()

// 3. 检查语法错误（简单检查）
console.log('3. 检查 arbitration store 语法:')
try {
  const storePath = path.join(__dirname, 'src/stores/arbitration.js')
  const content = fs.readFileSync(storePath, 'utf-8')
  
  // 检查基本语法
  const issues = []
  
  if (!content.includes('export const useArbitrationStore')) {
    issues.push('缺少 useArbitrationStore 导出')
  }
  
  if (!content.includes('defineStore(')) {
    issues.push('缺少 defineStore 调用')
  }
  
  // 检查括号匹配
  const openBraces = (content.match(/{/g) || []).length
  const closeBraces = (content.match(/}/g) || []).length
  if (openBraces !== closeBraces) {
    issues.push(`括号不匹配: { 数量=${openBraces}, } 数量=${closeBraces}`)
  }
  
  if (issues.length === 0) {
    console.log('   ✓ 基本语法检查通过')
  } else {
    console.log('   ✗ 发现问题:')
    issues.forEach(issue => console.log(`     - ${issue}`))
  }
} catch (error) {
  console.log(`   ✗ 检查失败: ${error.message}`)
}
console.log()

// 4. 检查 MainLayout 语法
console.log('4. 检查 MainLayout 组件:')
try {
  const layoutPath = path.join(__dirname, 'src/components/MainLayout.vue')
  const content = fs.readFileSync(layoutPath, 'utf-8')
  
  const issues = []
  
  if (!content.includes('useArbitrationStore')) {
    issues.push('未导入 useArbitrationStore')
  }
  
  if (!content.includes('onMounted')) {
    issues.push('未导入 onMounted')
  }
  
  if (!content.includes('startUserPolling')) {
    issues.push('未调用 startUserPolling')
  }
  
  if (issues.length === 0) {
    console.log('   ✓ 基本检查通过')
  } else {
    console.log('   ✗ 发现问题:')
    issues.forEach(issue => console.log(`     - ${issue}`))
  }
} catch (error) {
  console.log(`   ✗ 检查失败: ${error.message}`)
}
console.log()

console.log('=== 诊断完成 ===')
console.log('\n下一步操作建议:')
console.log('1. 运行: npm run dev')
console.log('2. 打开浏览器访问 http://localhost:5173')
console.log('3. 按 F12 打开开发者工具查看控制台错误')
console.log('4. 如有错误，请复制完整错误信息\n')
