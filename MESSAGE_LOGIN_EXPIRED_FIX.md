# ✅ 消息服务"登录过期"问题已修复

## 问题描述
**症状**: 点击"联系卖家"按钮后，跳转到消息页面时自动提示"登录已过期，请重新登录"

## 问题根本原因

Message服务配置了**SaToken拦截器**，要求所有请求都必须提供Token并通过SaToken验证。但是：

1. **Gateway已经完成了Token验证**，验证通过后会将`userId`通过`X-User-Id` Header传递给下游服务
2. **Gateway不会将Token传递给下游服务**（只传递userId）
3. **Message服务的SaToken拦截器尝试获取Token**，但找不到Token，因此返回401未登录错误
4. **前端收到401后**，request.js拦截器自动弹出"登录已过期"并跳转到登录页

## 解决方案

### 架构调整
**将Message服务的认证方式改为与Product、User服务一致**：
- ❌ 不使用SaToken拦截器进行本地Token验证
- ✅ 信任Gateway的认证结果，直接从`X-User-Id` Header获取用户ID

### 代码修改

#### 1. 修改SaTokenConfig
**文件**: `market-service-message/src/main/java/org/shyu/marketservicemessage/config/SaTokenConfig.java`

**修改前**:
```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new SaInterceptor(handle -> {
        SaRouter.match("/**")
                .notMatch("/ws/**", "/doc.html/**", ...)
                .check(r -> StpUtil.checkLogin());
    })).addPathPatterns("/**");
}
```

**修改后**:
```java
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    // 不需要添加SaToken拦截器
    // Gateway已经完成认证，会通过 X-User-Id Header 传递用户ID
}
```

#### 2. 修改MessageController
**文件**: `market-service-message/src/main/java/org/shyu/marketservicemessage/controller/MessageController.java`

**修改前**（所有方法）:
```java
@PostMapping("/send")
public Result<Void> sendMessage(@RequestBody SendMessageDTO dto) {
    Long userId = StpUtil.getLoginIdAsLong();  // ❌ 从SaToken获取
    // ...
}
```

**修改后**（所有方法）:
```java
@PostMapping("/send")
public Result<Void> sendMessage(@RequestHeader("X-User-Id") Long userId,  // ✅ 从Header获取
                                @RequestBody SendMessageDTO dto) {
    // 直接使用userId
    // ...
}
```

**修改的方法列表**:
- ✅ `sendMessage()` - 发送消息
- ✅ `getConversationList()` - 获取会话列表
- ✅ `getChatHistory()` - 获取聊天记录
- ✅ `markAsRead()` - 标记已读
- ✅ `getUnreadCount()` - 获取未读数
- ✅ `deleteConversation()` - 删除会话

## 🔴 重启Message服务

**修改完成后，必须重启Message服务！**

### 方式1 - 在IDEA中重启（推荐）
1. 停止当前运行的 `MarketServiceMessageApplication`
2. 重新启动

### 方式2 - 使用命令行
```powershell
cd D:\program\Market\market-service\market-service-message
mvn spring-boot:run
```

## ✅ 验证修复

### 1. 检查服务启动
查看日志，应该看到：
```
Tomcat started on port(s): 8103 (http)
nacos registry, DEFAULT_GROUP market-service-message ...register finished
```

### 2. 测试"联系卖家"功能
1. 打开浏览器 → http://localhost:5173
2. 登录用户账号
3. 进入任意商品详情页
4. 点击"联系卖家"按钮
5. **应该正常跳转到消息页面**，不再提示"登录已过期"

### 3. 测试消息功能
- ✅ 会话列表能正常加载
- ✅ 聊天记录能正常显示
- ✅ 能正常发送消息
- ✅ WebSocket连接正常

## 🔍 技术细节

### 微服务认证架构

```
前端 → Gateway (Token验证) → 微服务 (信任Gateway结果)
         ↓
      验证Token
      提取userId
         ↓
      Header: X-User-Id: 123
         ↓
      转发到微服务
```

### 统一认证方案

| 服务 | 认证方式 | 获取userId方式 |
|------|---------|---------------|
| Gateway | 验证Token（从Redis） | - |
| User Service | 信任Gateway | `@RequestHeader("X-User-Id")` |
| Product Service | 信任Gateway | `@RequestHeader("X-User-Id")` |
| Message Service | 信任Gateway | `@RequestHeader("X-User-Id")` ✅ |
| File Service | 信任Gateway | `@RequestHeader("X-User-Id")` |

### 为什么不在Message服务中使用SaToken？

1. **避免重复认证**：Gateway已经验证过Token，下游服务不需要再验证
2. **简化架构**：统一由Gateway负责认证，下游服务只负责业务逻辑
3. **性能更好**：避免每个服务都连接Redis验证Token
4. **更易维护**：认证逻辑集中在Gateway，便于统一管理

## 📋 相关服务

确保以下服务都在运行：
- ✅ Gateway (9901)
- ✅ User Service (8101)
- ✅ Product Service (8102)
- ✅ Message Service (8103) - **需要重启**
- ✅ Nacos (8848)
- ✅ MySQL (3306)
- ✅ Redis (6379)
- ✅ RocketMQ (9876, 10911)

## 🎯 注意事项

1. **WebSocket连接**：WebSocket连接（`/ws/**`）不经过Gateway，需要单独处理认证
2. **Feign调用**：如果Message服务需要调用其他服务，通过Feign调用时不需要Token
3. **直接访问**：如果绕过Gateway直接访问Message服务，将无法获取userId（不推荐）

---

## 🎉 完成！

重启Message服务后，"联系卖家"功能应该就能正常工作了！

如果还有问题，请检查：
1. Message服务是否成功重启
2. Gateway是否正常运行
3. Token是否真的有效（可以先访问其他需要登录的页面测试）
4. 浏览器控制台是否有其他错误信息

---
**修复时间**: 2026-02-15 18:30
**状态**: ✅ 代码修复完成，等待重启验证

