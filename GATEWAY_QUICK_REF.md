# Gateway 快速参考

## 📋 核心功能

✅ **统一认证** - 从 Redis 验证 Token（不调用服务）  
✅ **白名单** - 登录/注册等接口无需认证  
✅ **自动传递** - 将 userId 放入 `X-User-Id` Header  
✅ **跨域处理** - 统一 CORS 配置  

---

## 🔑 Token 使用

### 前端发送请求
```javascript
// 方式1（推荐）
headers: {
  'satoken': 'your-token-here'
}

// 方式2
headers: {
  'Authorization': 'Bearer your-token-here'
}
```

### 后端接收 userId
```java
@GetMapping("/my-products")
public Result<List<Product>> myProducts(
    @RequestHeader("X-User-Id") Long userId
) {
    return productService.getByUserId(userId);
}
```

---

## 🎯 白名单路径

| 路径 | 说明 |
|------|------|
| `/api/user/login` | 登录 |
| `/api/user/register` | 注册 |
| `/api/product/list` | 商品列表 |
| `/api/product/detail/**` | 商品详情 |
| `/api/product/category/**` | 商品分类 |

---

## 🧪 测试命令

```bash
# 健康检查
curl http://localhost:9000/health

# 登录
curl -X POST http://localhost:9000/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 访问受保护接口
curl http://localhost:9000/api/user/info \
  -H "satoken: {token}"
```

---

## ⚙️ 配置要点

```yaml
# Redis 必需（用于验证 Token）
spring:
  redis:
    host: 127.0.0.1
    port: 6379

# 白名单配置（可选）
gateway:
  auth:
    white-list:
      - /api/your-path
```

---

## 📁 文件清单

- `filter/AuthGlobalFilter.java` - 认证过滤器 ⭐
- `config/WhiteListProperties.java` - 白名单配置
- `controller/HealthController.java` - 健康检查
- `bootstrap.yml` - 路由和配置

---

## 🚀 启动顺序

1. Redis (6379)
2. MySQL (3306)
3. Nacos (8849)
4. User 服务 (8101)
5. **Gateway (9000)** ← 最后启动

---

## ❓ 常见问题

**Q: 提示"请先登录"**  
A: 检查 Token 是否正确、Header 名称是否为 `satoken`

**Q: 后端获取不到 userId**  
A: 检查是否从 `@RequestHeader("X-User-Id")` 获取

**Q: 白名单不生效**  
A: 检查路径是否完全匹配，注意前缀

---

> 详细文档：`GATEWAY_COMPLETE.md`

