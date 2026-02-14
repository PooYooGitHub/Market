# 🚨 注册接口报错快速修复指南

## 问题症状
```
cn.dev33.satoken.exception.NotLoginException: 未能读取到有效Token
```

## ✅ 解决步骤（5分钟搞定）

### 步骤1：检查配置是否已更新
打开文件：`market-service-user/src/main/java/org/shyu/marketserviceuser/config/SaTokenConfig.java`

确认代码如下（使用最新的直接判断方式）：
```java
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            HttpServletRequest request = (HttpServletRequest) handler;
            String path = request.getRequestURI();
            
            log.info("Sa-Token interceptor checking path: {}", path);
            
            // 白名单路径，直接放行
            if (isWhiteListPath(path)) {
                log.info("Path {} is in whitelist, skip authentication", path);
                return;
            }
            
            // 非白名单路径，需要登录验证
            log.info("Path {} requires authentication", path);
            StpUtil.checkLogin();
        })).addPathPatterns("/**");
    }
    
    private boolean isWhiteListPath(String path) {
        // 认证相关接口
        if (path.startsWith("/api/user/auth/")) {
            return true;
        }
        
        // Feign 调用接口
        if (path.matches("/api/user/\\d+")) {
            return true;
        }
        if (path.equals("/api/user/username") || path.equals("/api/user/phone")) {
            return true;
        }
        
        // 健康检查、文档、静态资源
        if (path.startsWith("/health") || 
            path.startsWith("/doc.html") || 
            path.startsWith("/swagger-resources") || 
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/webjars") ||
            path.equals("/favicon.ico") || 
            path.equals("/error")) {
            return true;
        }
        
        return false;
    }
}
```

**关键点**：
- ✅ 直接获取 `request.getRequestURI()` 进行判断
- ✅ 使用 `startsWith()` 匹配路径前缀
- ✅ 添加了详细的日志输出（`log.info`）
- ✅ 白名单路径直接 `return` 不执行登录检查
- ✅ 非白名单路径才执行 `StpUtil.checkLogin()`

### 步骤2：⚠️ 重启服务（必须！）

**在 IDEA 中：**
1. 点击红色停止按钮 ⏹️
2. 等待看到 "Disconnected from the target VM"
3. 重新点击绿色运行按钮 ▶️
4. 等待看到 "Started MarketServiceUserApplication"

**或者在命令行：**
```bash
# 按 Ctrl+C 停止
# 然后重新运行
cd D:\program\Market\market-service\market-service-user
mvn spring-boot:run
```

### 步骤3：测试注册接口

**使用 Postman 或 curl：**
```bash
curl -X POST http://localhost:8101/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser123",
    "password": "123456",
    "phone": "13900139000",
    "nickname": "测试用户"
  }'
```

**期望返回：**
```json
{
  "code": 200,
  "message": "Registration successful",
  "data": 1
}
```

## 🔍 如果还是失败，检查这些

### 检查点1：日志中的路径
启动后，尝试访问注册接口，查看日志输出：
```
Sa-Token checking login for path: /api/user/auth/register
```

如果看到这行日志，说明拦截器仍在检查该路径，配置没生效。

**解决方法：** 
- 确认配置文件已保存
- 确认已重启服务（不是热部署，要完全重启）

### 检查点2：导入的包
确认文件顶部有这些导入：
```java
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import javax.servlet.http.HttpServletRequest;  // 用于获取请求路径
```

**注意**：新版本不再使用 `SaRouter` 和 `SaHolder`，直接使用 `HttpServletRequest` 获取路径更可靠。

### 检查点3：pom.xml 依赖
确认 `market-service-user/pom.xml` 中有：
```xml
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot-starter</artifactId>
    <version>1.34.0</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

## 🎯 原理说明

### 为什么要用 SaRouter？
Spring MVC 的 `excludePathPatterns` 在某些版本下对 `/api/user/auth/**` 这样的路径模式匹配不准确。

Sa-Token 的 `SaRouter` 是专门为 Sa-Token 设计的路由匹配器，更可靠。

### 流程图
```
请求 /api/user/auth/register
    ↓
SaRouter.match("/**")          [✓ 匹配所有]
    ↓
.notMatch("/api/user/auth/**") [✓ 排除认证路径]
    ↓
不执行 StpUtil.checkLogin()   [✓ 不检查登录]
    ↓
直接进入 Controller           [✓ 成功！]
```

## 📞 还是不行？

如果按照以上步骤操作仍然失败，请提供：
1. 完整的启动日志（从 `Started MarketServiceUserApplication` 往上50行）
2. 请求时的完整错误日志
3. `SaTokenConfig.java` 文件的完整内容

---

**更新时间**: 2026-02-14  
**详细文档**: [USER_SERVICE_AUTH_FIX.md](./USER_SERVICE_AUTH_FIX.md)

