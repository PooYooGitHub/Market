# Gateway 端口更新说明

## 📝 更新记录

- **更新日期**: 2026-02-15
- **更新内容**: Gateway 端口从 9000 更改为 9901
- **原因**: 避免与 MinIO (9900) 端口冲突，优化端口分配

---

## 🔧 已更新的文件

### 后端配置

1. ✅ `market-gateway/src/main/resources/bootstrap.yml`
   - 端口: `9000` → `9901`

### 前端对接文档

2. ✅ `front/对接文档/前端对接文档-用户模块.md`
   - 所有 API 地址: `http://localhost:9000` → `http://localhost:9901`
   - 更新时间: `2026-02-14` → `2026-02-15`

3. ✅ `front/对接文档/前端对接文档-商品模块.md`
   - 所有 API 地址: `http://localhost:9000` → `http://localhost:9901`
   - 更新时间: `2026-02-14` → `2026-02-15`

4. ✅ `front/对接文档/前端对接文档-文件上传模块.md`
   - 所有 API 地址: `http://localhost:9000` → `http://localhost:9901`

5. ✅ `front/对接文档/前端对接文档-Message模块.md`
   - 所有 API 地址: `http://localhost:9000` → `http://localhost:9901`

### 前端代码

- ✅ `front/vue-project/src/utils/request.js` - 已经是 9901，无需修改

---

## 🚀 使用说明

### 启动顺序

1. **启动基础设施**
   ```powershell
   # Nacos (8849)
   # MySQL (3306)
   # Redis (6379)
   # MinIO (9900)
   # RocketMQ (9876)
   ```

2. **启动 Gateway**
   ```powershell
   cd D:\program\Market\market-gateway
   mvn spring-boot:run
   # 访问: http://localhost:9901
   ```

3. **启动业务服务**
   ```powershell
   # User Service (8101)
   # Product Service (8102)
   # File Service (8106)
   # Message Service (8103)
   ```

### 访问地址

| 服务 | 端口 | 访问地址 |
|-----|------|---------|
| Gateway | 9901 | http://localhost:9901 |
| User Service | 8101 | http://localhost:8101 (内部) |
| Product Service | 8102 | http://localhost:8102 (内部) |
| File Service | 8106 | http://localhost:8106 (内部) |
| Message Service | 8103 | http://localhost:8103 (内部) |
| MinIO | 9900 | http://localhost:9900 |
| MinIO Console | 9090 | http://localhost:9090 |
| Nacos | 8849 | http://localhost:8849/nacos |

### API 调用示例

```javascript
// ✅ 正确 - 通过 Gateway 访问
const response = await axios.post('http://localhost:9901/api/user/login', {
  username: 'test',
  password: '123456'
})

// ❌ 错误 - 不要直接访问服务端口
const response = await axios.post('http://localhost:8101/user/login', {
  username: 'test',
  password: '123456'
})
```

### 前端配置

**Vue3 项目** (`src/utils/request.js`):
```javascript
const service = axios.create({
  baseURL: 'http://localhost:9901', // ✅ 已更新
  timeout: 10000
})
```

**环境变量** (`.env.development`):
```env
VITE_API_BASE_URL=http://localhost:9901
```

---

## ⚠️ 注意事项

### 1. 清除浏览器缓存

端口更新后，建议清除浏览器缓存和 localStorage：

```javascript
// 在浏览器控制台执行
localStorage.clear()
sessionStorage.clear()
location.reload()
```

### 2. 更新 Postman/Apifox

如果使用 API 测试工具，记得更新环境变量：

```
变量名: BASE_URL
旧值: http://localhost:9000
新值: http://localhost:9901
```

### 3. 跨域配置

Gateway 的跨域配置已自动应用到新端口，无需额外配置。

### 4. Nginx 反向代理

如果使用 Nginx，更新配置：

```nginx
upstream gateway {
    server localhost:9901;  # ✅ 已更新
}

server {
    listen 80;
    server_name api.market.com;
    
    location /api/ {
        proxy_pass http://gateway;
    }
}
```

---

## 🔍 验证步骤

### 1. 验证 Gateway 启动

```powershell
# 检查端口占用
netstat -ano | Select-String ":9901"

# 访问 Gateway 健康检查 (如果配置了)
curl http://localhost:9901/actuator/health
```

### 2. 验证前端连接

```javascript
// 在浏览器控制台测试
fetch('http://localhost:9901/api/user/info', {
  headers: {
    'satoken': localStorage.getItem('token')
  }
})
  .then(r => r.json())
  .then(console.log)
```

### 3. 检查 Nacos 注册

访问: http://localhost:8849/nacos

查看 `market-gateway` 服务是否注册成功，IP:PORT 应该显示为 `x.x.x.x:9901`

---

## 📚 相关文档

- [架构设计说明书](./doc/架构设计说明书.md)
- [Gateway 模块开发完成报告](./doc/Gateway模块开发完成报告.md)
- [前端对接文档-用户模块](./front/对接文档/前端对接文档-用户模块.md)
- [前端对接文档-商品模块](./front/对接文档/前端对接文档-商品模块.md)

---

## 🐛 故障排查

### 问题 1: Gateway 启动失败 - 端口被占用

**错误信息**: `Port 9901 was already in use`

**解决方案**:
```powershell
# 查找占用进程
netstat -ano | Select-String ":9901"

# 终止进程
taskkill /PID <进程ID> /F
```

### 问题 2: 前端无法访问 API

**可能原因**:
1. Gateway 未启动
2. 服务未注册到 Nacos
3. 浏览器缓存了旧地址

**解决方案**:
```powershell
# 1. 检查 Gateway 状态
docker ps | Select-String "gateway"

# 2. 查看 Gateway 日志
docker logs market-gateway --tail 50

# 3. 重启 Gateway
.\start-gateway.ps1
```

### 问题 3: Token 验证失败

**可能原因**: Token 存储在旧的域名/端口下

**解决方案**:
```javascript
// 清除旧 Token，重新登录
localStorage.removeItem('token')
// 访问登录页面重新登录
```

---

## ✅ 更新完成清单

- [x] 更新 Gateway 配置文件 (bootstrap.yml)
- [x] 更新前端对接文档 - 用户模块
- [x] 更新前端对接文档 - 商品模块
- [x] 更新前端对接文档 - 文件上传模块
- [x] 更新前端对接文档 - Message 模块
- [x] 验证前端代码配置
- [x] 创建更新说明文档

**所有更新已完成！** 🎉

