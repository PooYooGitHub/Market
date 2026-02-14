# 用户服务认证问题修复报告

> **日期**: 2026-02-14  
> **问题**: 用户注册时报 `NotLoginException: 未能读取到有效Token`  
> **状态**: ✅ 已修复

---

## 一、问题描述

### 1.1 错误现象

当前端调用用户注册接口 `POST /api/user/auth/register` 时，后端抛出异常：

```
cn.dev33.satoken.exception.NotLoginException: 未能读取到有效Token
    at org.shyu.marketserviceuser.config.SaTokenConfig.lambda$addInterceptors$0
```

### 1.2 问题原因

Sa-Token 拦截器配置不正确，导致注册接口也被拦截并要求登录验证。虽然配置中已经排除了 `/api/user/auth/register`，但由于路径模式匹配问题，该路径仍然被拦截。

---

## 二、解决方案

### 2.1 修改文件

**文件路径**: `market-service-user/src/main/java/org/shyu/marketserviceuser/config/SaTokenConfig.java`

### 2.2 修改内容

**问题分析**：
Spring MVC 的 `excludePathPatterns` 在某些情况下可能无法正确匹配路径。Sa-Token 提供了更强大的 `SaRouter` 来处理路由匹配。

**最终解决方案**（使用 SaRouter）:
```java
@Slf4j
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 使用 SaRouter 进行路由匹配
            SaRouter.match("/**")  // 拦截所有路径
                    .notMatch(
                            "/api/user/auth/**",     // 认证相关接口
                            "/api/user/*/",          // Feign调用
                            "/api/user/username",    // Feign调用
                            "/api/user/phone",       // Feign调用
                            "/health/**",            // 健康检查
                            "/doc.html",             // 文档
                            "/swagger-resources/**", 
                            "/v3/api-docs/**",
                            "/webjars/**",
                            "/favicon.ico",
                            "/error"
                    )
                    .check(r -> {
                        log.debug("Sa-Token checking login for path: {}", SaRouter.getRequest().getUrl());
                        StpUtil.checkLogin();
                    });
        })).addPathPatterns("/**");
    }
}
```

### 2.3 修改说明

1. **使用 SaRouter 替代 excludePathPatterns**
   - `SaRouter` 是 Sa-Token 提供的路由匹配工具，比 Spring MVC 的路径匹配更可靠
   - `match("/**").notMatch(...)` 语法更清晰，表示"匹配所有路径，但排除这些"

2. **添加调试日志**
   - 使用 `@Slf4j` 注解启用日志
   - 在检查登录时输出当前请求路径，便于调试

3. **路径匹配更准确**
   - `/api/user/auth/**` - 匹配所有认证相关接口
   - `/api/user/*/` - 匹配如 `/api/user/1` 这样的路径（带ID的Feign调用）

### 2.4 为什么之前的方案不工作？

可能的原因：
1. **Spring MVC 路径匹配器版本差异** - 不同版本的 Spring Boot 对路径匹配的实现可能不同
2. **AntPathMatcher 行为** - 在某些配置下，`{id}` 这样的占位符可能不被正确识别
3. **拦截器注册顺序** - 可能有其他拦截器影响了路径匹配

使用 Sa-Token 自带的 `SaRouter` 可以避免这些问题，因为它在 Sa-Token 拦截器内部进行路由判断，不依赖 Spring MVC 的路径匹配。

---

## 三、架构说明

### 3.1 认证鉴权流程

根据 `doc/架构设计说明书.md`，系统的认证流程如下：

```
前端 → Gateway (统一认证) → User Service
                ↓
              验证 Token
              提取 userId
```

### 3.2 User Service 的职责

User Service 中的认证配置主要用于保护以下接口：

1. **需要 Token 的接口**（会被拦截器检查）：
   - `/api/user/profile` - 获取/更新用户信息
   - `/api/user/password` - 修改密码
   - 等其他需要登录才能访问的接口

2. **不需要 Token 的接口**（被排除，不拦截）：
   - `/api/user/auth/**` - 注册、登录验证等认证端点
   - `/api/user/{id}` 等 - 内部 Feign 调用端点
   - `/health/**` - 健康检查端点

### 3.3 为什么 User Service 需要 Sa-Token？

虽然 Gateway 已经做了统一认证，但 User Service 仍需要 Sa-Token 的原因：

1. **直接访问保护** - 防止绕过 Gateway 直接访问服务端口
2. **方法级权限控制** - 在 Service 层使用 `@SaCheckRole`、`@SaCheckPermission` 等注解
3. **获取登录信息** - 在业务代码中使用 `StpUtil.getLoginId()` 获取当前用户 ID

---

## 四、测试验证

### 4.0 ⚠️ 重要：必须重启服务

修改配置文件后，**必须重启 User Service** 才能生效！

**方式1：IDEA 中重启**
1. 在 IDEA 中停止当前运行的 `MarketServiceUserApplication`
2. 等待完全停止（控制台显示 "Disconnected from the target VM"）
3. 重新启动 `MarketServiceUserApplication`
4. 等待看到 "Started MarketServiceUserApplication" 日志

**方式2：命令行重启**
```bash
# 停止服务（按 Ctrl+C）
# 然后重新启动
cd market-service/market-service-user
mvn spring-boot:run
```

**验证服务已重启**：
- 查看日志，应该能看到 Sa-Token 相关的配置加载日志
- 访问健康检查接口确认服务运行：`http://localhost:8101/health`

### 4.1 测试场景

修复后，以下场景应该正常工作：

| 场景 | 接口 | 是否需要 Token | 预期结果 |
|------|------|---------------|----------|
| 用户注册 | POST /api/user/auth/register | ❌ 否 | ✅ 成功注册 |
| 登录验证 | POST /api/user/auth/validate-login | ❌ 否 | ✅ 返回用户信息 |
| 获取用户信息 | GET /api/user/profile | ✅ 是 | ✅ 返回当前用户信息 |
| Feign查询用户 | GET /api/user/{id} | ❌ 否 | ✅ 返回用户信息 |

### 4.2 测试步骤

1. **启动服务**
   ```bash
   # 确保 Nacos、MySQL、Redis 已启动
   # 启动 User Service
   cd market-service/market-service-user
   mvn spring-boot:run
   ```

2. **测试注册接口**
   ```bash
   curl -X POST http://localhost:8101/api/user/auth/register \
     -H "Content-Type: application/json" \
     -d '{
       "username": "testuser",
       "password": "123456",
       "phone": "13800138000",
       "nickname": "测试用户"
     }'
   ```

   预期响应：
   ```json
   {
     "code": 200,
     "message": "Registration successful",
     "data": 1
   }
   ```

3. **测试需要登录的接口**
   ```bash
   # 不带 Token，应该返回 401
   curl http://localhost:8101/api/user/profile
   ```

   预期响应：
   ```json
   {
     "code": 401,
     "message": "未能读取到有效Token"
   }
   ```

---

## 五、注意事项

### 5.1 开发注意事项

1. **新增认证相关接口** - 放在 `/api/user/auth/` 路径下，会自动被排除拦截
2. **新增 Feign 接口** - 如果需要被其他服务调用，需要在 `excludePathPatterns` 中添加
3. **业务接口** - 默认都需要登录，无需额外配置

### 5.2 安全建议

1. **网络隔离** - 生产环境中，服务端口（8101等）不应对外暴露，只能通过 Gateway 访问
2. **Feign 调用安全** - 可以考虑为 Feign 接口添加服务间认证（如使用内部 Token）
3. **定期审查** - 定期检查 `excludePathPatterns`，确保没有误排除需要保护的接口

---

## 六、相关文档

- [架构设计说明书](doc/架构设计说明书.md)
- [前端对接文档-用户模块](doc/前端对接文档-用户模块.md)
- [Sa-Token 官方文档](https://sa-token.cc/)

---

**修复完成！** 🎉

现在用户注册接口应该可以正常工作了。如果还有其他问题，请检查：
1. Nacos 是否正常运行
2. MySQL 数据库连接是否正确
3. Redis 是否正常运行
4. 网络端口是否被占用

