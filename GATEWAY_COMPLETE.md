# Gateway 网关服务 - 完成说明文档

> **完成日期**: 2026-02-14  
> **版本**: v1.0  
> **状态**: ✅ 已完成

---

## 一、完成内容

### 1.1 新增文件

| 文件 | 说明 | 状态 |
|------|------|------|
| `filter/AuthGlobalFilter.java` | 全局认证过滤器 | ✅ |
| `config/WhiteListProperties.java` | 白名单配置类 | ✅ |
| `controller/HealthController.java` | 健康检查控制器 | ✅ |

### 1.2 修改文件

| 文件 | 修改内容 | 状态 |
|------|----------|------|
| `bootstrap.yml` | 更新路由配置、添加 Redis、白名单配置 | ✅ |

---

## 二、核心功能说明

### 2.1 认证过滤器（AuthGlobalFilter）

**职责**：
1. ✅ 检查白名单，白名单路径直接放行
2. ✅ 从 Header 获取 Token（支持 `satoken` 和 `Authorization`）
3. ✅ 从 Redis 验证 Token（本地验证，不调用服务）
4. ✅ 提取 userId 放入 `X-User-Id` Header
5. ✅ Token 无效返回 401 错误

**Token 获取方式**：
```bash
# 方式1：satoken Header
curl http://localhost:9000/api/user/info \
  -H "satoken: your-token-here"

# 方式2：Authorization Header
curl http://localhost:9000/api/user/info \
  -H "Authorization: Bearer your-token-here"
```

**Redis Key 格式**：
```
satoken:login:token:{tokenValue} -> userId
```

**认证流程**：
```
请求进入 Gateway
    ↓
检查是否白名单路径？
    ├─ 是 → 直接放行
    └─ 否 → 检查 Token
              ├─ 无 Token → 返回 401
              └─ 有 Token → 从 Redis 验证
                            ├─ 有效 → 提取 userId → 放入 Header → 转发
                            └─ 无效 → 返回 401
```

### 2.2 白名单配置（WhiteListProperties）

**默认白名单**：
```yaml
gateway:
  auth:
    white-list:
      - /api/user/login         # 用户登录
      - /api/user/register      # 用户注册
      - /api/product/list       # 商品列表
      - /api/product/detail     # 商品详情
      - /api/product/category   # 商品分类
      - /feign                  # Feign 内部调用
```

**添加新的白名单路径**：
在 `bootstrap.yml` 中添加：
```yaml
gateway:
  auth:
    white-list:
      - /api/your-new-path
```

### 2.3 路由配置

**更新后的路由**：
```yaml
routes:
  # 用户服务
  - id: market-service-user
    uri: lb://market-service-user
    predicates:
      - Path=/api/user/**
  
  # 商品服务
  - id: market-service-product
    uri: lb://market-service-product
    predicates:
      - Path=/api/product/**
  
  # 交易服务
  - id: market-service-trade
    uri: lb://market-service-trade
    predicates:
      - Path=/api/trade/**
```

**说明**：
- ✅ 移除了 Auth 服务路由
- ✅ 修正了 Product 和 Trade 服务的路由（不再指向 market-service-core）
- ✅ 使用 `StripPrefix=1` 去除 `/api` 前缀

---

## 三、使用示例

### 3.1 登录流程

```bash
# 1. 用户登录（无需 Token）
curl -X POST http://localhost:9000/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test",
    "password": "123456"
  }'

# 返回示例：
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "abc123xyz...",
    "userId": 1,
    "username": "test",
    "nickname": "测试用户"
  }
}
```

### 3.2 访问受保护接口

```bash
# 2. 访问需要登录的接口（带 Token）
curl http://localhost:9000/api/user/info \
  -H "satoken: abc123xyz..."

# Gateway 自动将 userId 放入 Header
# 后端服务收到的请求会包含：
# Header: X-User-Id: 1
```

### 3.3 访问白名单接口

```bash
# 3. 访问白名单接口（无需 Token）
curl http://localhost:9000/api/product/list

# 直接放行，不验证 Token
```

---

## 四、后端服务如何获取用户ID

### 4.1 Controller 层

```java
@RestController
@RequestMapping("/product")
public class ProductController {
    
    /**
     * 发布商品（需要登录）
     * Gateway 已验证 Token，这里直接从 Header 获取 userId
     */
    @PostMapping("/publish")
    public Result<Long> publish(
        @RequestHeader("X-User-Id") Long userId,  // ← 从 Gateway 传递
        @RequestBody ProductPublishRequest request
    ) {
        return Result.success(productService.publish(userId, request));
    }
    
    /**
     * 商品列表（无需登录）
     */
    @GetMapping("/list")
    public Result<List<ProductVO>> list() {
        // 白名单接口，不会有 X-User-Id Header
        return Result.success(productService.list());
    }
}
```

### 4.2 可选：创建工具类

```java
// UserContextHolder.java
public class UserContextHolder {
    
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    
    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }
    
    public static Long getUserId() {
        return USER_ID.get();
    }
    
    public static void clear() {
        USER_ID.remove();
    }
}

// UserContextInterceptor.java
@Component
public class UserContextInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userIdStr = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdStr)) {
            UserContextHolder.setUserId(Long.valueOf(userIdStr));
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContextHolder.clear();
    }
}
```

---

## 五、测试验证

### 5.1 健康检查

```bash
# 检查 Gateway 是否运行
curl http://localhost:9000/health

# 返回示例：
{
  "status": "UP",
  "service": "market-gateway",
  "timestamp": "2026-02-14T12:00:00",
  "redis": "UP"
}
```

### 5.2 测试白名单

```bash
# 1. 不带 Token 访问白名单（应该成功）
curl http://localhost:9000/api/product/list

# 2. 不带 Token 访问需要登录的接口（应该返回 401）
curl http://localhost:9000/api/user/info

# 返回：
{
  "code": 401,
  "message": "请先登录",
  "data": null
}
```

### 5.3 测试 Token 验证

```bash
# 1. 登录获取 Token
TOKEN=$(curl -X POST http://localhost:9000/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}' \
  | jq -r '.data.token')

# 2. 使用 Token 访问
curl http://localhost:9000/api/user/info \
  -H "satoken: $TOKEN"

# 3. 使用无效 Token（应该返回 401）
curl http://localhost:9000/api/user/info \
  -H "satoken: invalid-token"
```

---

## 六、配置说明

### 6.1 完整配置文件

```yaml
spring:
  application:
    name: market-gateway
  
  # Nacos 配置
  cloud:
    nacos:
      server-addr: 127.0.0.1:8849
      username: nacos
      password: nacos
      discovery:
        server-addr: 127.0.0.1:8849
    
    # Gateway 路由配置
    gateway:
      routes:
        - id: market-service-user
          uri: lb://market-service-user
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1
  
  # Redis 配置（必需，用于验证 Token）
  redis:
    host: 127.0.0.1
    port: 6379
    database: 0

server:
  port: 9000

# 白名单配置（可选，有默认值）
gateway:
  auth:
    white-list:
      - /api/user/login
      - /api/user/register
      - /api/product/list
```

### 6.2 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| Redis | 7.x | 必需，用于验证 Token |
| Nacos | 2.2.x | 必需，服务注册发现 |

---

## 七、常见问题

### Q1: 提示"请先登录"

**原因**：
- 未提供 Token
- Token 格式错误

**解决**：
```bash
# 确保 Header 名称正确
-H "satoken: your-token"
# 或
-H "Authorization: Bearer your-token"
```

### Q2: 提示"登录已过期"

**原因**：
- Token 已过期（默认 30 天）
- Redis 中没有该 Token

**解决**：
1. 重新登录获取新 Token
2. 检查 Redis 是否正常运行

### Q3: 白名单不生效

**原因**：
- 路径配置错误
- 路径匹配规则错误

**解决**：
```yaml
# 使用前缀匹配
white-list:
  - /api/product/list      # 只匹配 /api/product/list
  - /api/product/detail    # 匹配 /api/product/detail/123
```

### Q4: 后端服务获取不到 userId

**原因**：
- Gateway 未正常运行
- 认证过滤器未生效

**检查**：
```java
// 在后端服务打印 Header
String userId = request.getHeader("X-User-Id");
System.out.println("userId from gateway: " + userId);
```

---

## 八、性能优化

### 8.1 Redis 连接池

```yaml
spring:
  redis:
    lettuce:
      pool:
        max-active: 8    # 最大连接数
        max-idle: 8      # 最大空闲连接
        min-idle: 2      # 最小空闲连接
        max-wait: -1ms   # 获取连接最大等待时间
```

### 8.2 日志级别

```yaml
logging:
  level:
    org.shyu.marketgateway: INFO  # 生产环境使用 INFO
    org.springframework.cloud.gateway: WARN
```

---

## 九、下一步

### 9.1 已完成 ✅

- [x] Gateway 认证过滤器
- [x] 白名单配置
- [x] 路由配置
- [x] 健康检查

### 9.2 待开发 🔴

- [ ] Product 服务（商品管理）
- [ ] Trade 服务（交易管理）
- [ ] 前端开发

---

## 十、相关文档

| 文档 | 说明 |
|------|------|
| `doc/架构设计说明书.md` | 完整架构设计 |
| `doc/架构重构实施计划.md` | 重构步骤 |
| `doc/架构设计问题总结.md` | 常见问题 |

---

> **Gateway 网关服务开发完成！** 🎉  
> 可以开始启动测试了！

## 附录：启动命令

```powershell
# 1. 启动基础设施
# 确保 Redis、MySQL、Nacos 已运行

# 2. 启动 User 服务
cd D:\program\Market\market-service\market-service-user
mvn spring-boot:run

# 3. 启动 Gateway
cd D:\program\Market\market-gateway
mvn spring-boot:run

# 4. 测试
curl http://localhost:9000/health
```

