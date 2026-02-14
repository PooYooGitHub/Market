# 校园跳蚤市场 - 用户模块前端

基于 Vue3 + Vite + Pinia + Vue Router 的校园跳蚤市场用户模块前端实现。

## 📁 项目结构

```
vue-project/
├── public/                  # 静态资源目录
├── src/
│   ├── api/                # API 接口封装
│   │   └── user.js        # 用户相关 API
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   ├── router/            # 路由配置
│   │   └── index.js      # 路由入口
│   ├── stores/            # Pinia 状态管理
│   │   ├── index.js      # Store 入口
│   │   └── user.js       # 用户状态管理
│   ├── utils/             # 工具函数
│   │   └── request.js    # Axios 配置
│   ├── views/             # 页面组件
│   │   ├── Home.vue      # 首页
│   │   ├── Login.vue     # 登录页
│   │   ├── Register.vue  # 注册页
│   │   └── Profile.vue   # 个人中心
│   ├── App.vue            # 根组件
│   └── main.js            # 入口文件
├── index.html              # HTML 模板
├── package.json            # 项目配置
├── vite.config.js         # Vite 配置
└── README-USER.md         # 本文档
```

## ✨ 已实现功能

### 用户认证
- ✅ 用户注册（含表单验证）
- ✅ 用户登录（JWT Token 认证）
- ✅ 自动保持登录状态
- ✅ 退出登录

### 用户信息管理
- ✅ 查看个人信息
- ✅ 编辑个人信息（昵称、邮箱、头像）
- ✅ 修改密码

### 路由与权限
- ✅ 路由守卫（登录拦截）
- ✅ 已登录用户自动跳转
- ✅ 页面标题动态设置

### 用户体验
- ✅ 响应式设计，支持移动端
- ✅ 美观的 UI 界面
- ✅ 友好的错误提示
- ✅ Loading 状态展示

## 🚀 快速开始

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

访问：http://localhost:5173

### 3. 构建生产版本

```bash
npm run build
```

## 🔧 技术栈

- **Vue 3.5+** - 渐进式 JavaScript 框架
- **Vite 7.3+** - 下一代前端构建工具
- **Vue Router 4** - 官方路由管理器
- **Pinia** - Vue 3 官方状态管理库
- **Axios** - HTTP 客户端

## 📡 API 接口

### 后端服务

- **API 网关**: http://localhost:9000
- **用户服务**: http://localhost:8101（通过网关访问）

### 已对接接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户注册 | POST | /api/user/register | 注册新用户 |
| 用户登录 | POST | /api/user/login | 用户登录获取 Token |
| 获取用户信息 | GET | /api/user/info | 获取当前用户信息 |
| 更新用户信息 | PUT | /api/user/update | 更新用户信息 |
| 修改密码 | PUT | /api/user/change-password | 修改密码 |

## 🔐 认证机制

### Token 管理

- Token 类型：Bearer Token (Sa-Token)
- 传递方式：HTTP Header `satoken: <token>`
- 有效期：30 天
- 存储位置：localStorage

### 请求拦截器

自动在请求头中添加 Token：

```javascript
config.headers['satoken'] = token
```

### 响应拦截器

统一处理错误：
- 401：Token 失效，跳转到登录页
- 403：无权限访问
- 500：服务器错误

## 📄 页面路由

| 路径 | 页面 | 权限 |
|------|------|------|
| / | 首页 | 公开 |
| /login | 登录页 | 仅未登录 |
| /register | 注册页 | 仅未登录 |
| /profile | 个人中心 | 需要登录 |

## 🎨 页面说明

### 首页 (Home.vue)

- 展示平台功能特性
- 导航栏显示登录状态
- 根据登录状态显示不同内容

### 登录页 (Login.vue)

- 用户名/密码登录
- 表单验证
- 登录成功后跳转首页
- 支持跳转到注册页

### 注册页 (Register.vue)

- 完整的注册表单
- 前端验证（用户名、密码、手机号、邮箱格式）
- 注册成功后跳转登录页

### 个人中心 (Profile.vue)

- 查看个人信息
- 编辑信息（模态框）
- 修改密码
- 退出登录

## 💾 状态管理 (Pinia)

### User Store

```javascript
// 状态
state: {
  token: string,      // 登录 Token
  userInfo: object    // 用户信息
}

// Getters
isLoggedIn          // 是否已登录
username            // 用户名
nickname            // 昵称
avatar              // 头像
userId              // 用户ID

// Actions
login()             // 登录
logout()            // 登出
getUserInfo()       // 获取用户信息
updateUserInfo()    // 更新本地用户信息
```

## 🔍 开发说明

### 添加新页面

1. 在 `src/views/` 创建页面组件
2. 在 `src/router/index.js` 添加路由配置
3. 设置 meta 信息（title, requiresAuth 等）

### 添加新 API

1. 在 `src/api/` 创建或编辑 API 文件
2. 使用 `request` 工具发起请求
3. 在组件中导入并使用

### 状态管理

1. 在 `src/stores/` 创建 store 文件
2. 使用 `defineStore` 定义 store
3. 在组件中通过 `useXxxStore()` 使用

## 🐛 调试技巧

### 查看请求

打开浏览器开发者工具 → Network（网络）标签

### 查看本地存储

开发者工具 → Application（应用）→ Local Storage

### 查看 Vuex 状态

安装 Vue DevTools 浏览器插件

## 📝 注意事项

### 跨域问题

后端网关已配置 CORS，前端无需配置代理。

### Token 失效

Token 有效期 30 天，失效后会自动跳转登录页。

### 密码安全

前端传输明文密码，后端负责加密存储。生产环境请使用 HTTPS。

### 表单验证

前端已实现基础验证，后端也会进行验证。

## 🚧 待优化

- [ ] 添加加载动画组件
- [ ] 完善错误提示（Toast 组件）
- [ ] 添加头像上传功能
- [ ] 实现"记住我"功能
- [ ] 添加找回密码功能
- [ ] 优化移动端体验
- [ ] 添加表单验证提示优化

## 📚 参考文档

- [前端对接文档 - 用户模块](../对接文档/前端对接文档-用户模块.md)
- [毕业设计说明](../对接文档/毕业设计.md)
- [Vue 3 文档](https://cn.vuejs.org/)
- [Vite 文档](https://cn.vitejs.dev/)
- [Vue Router 文档](https://router.vuejs.org/zh/)
- [Pinia 文档](https://pinia.vuejs.org/zh/)

## 📞 联系方式

如有问题，请联系开发团队。

---

**最后更新**: 2026-02-14
