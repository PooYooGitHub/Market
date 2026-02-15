# CORS跨域问题解决方案

## 问题描述

前端访问文件上传接口时出现CORS错误：
```
Access to XMLHttpRequest at 'http://localhost:9000/api/file/upload/avatar' from origin 'http://localhost:5173' has been blocked by CORS policy: The 'Access-Control-Allow-Origin' header contains multiple values 'http://localhost:5173, http://localhost:5173', but only one is allowed.
```

## 问题原因

在微服务架构中使用了Gateway网关，但**同时在Gateway和File Service中都配置了CORS**，导致响应头中出现重复的`Access-Control-Allow-Origin`值。

## 解决方案

### 1. **统一在Gateway配置CORS**

在微服务架构中，所有外部请求都经过Gateway，因此：
- ✅ **只在Gateway配置CORS**
- ❌ 不在各个微服务（user、product、file等）中配置CORS

### 2. 具体修改

#### 2.1 删除微服务中的CORS配置

已删除：
```
market-service-file/src/main/java/org/shyu/marketservicefile/config/CorsConfig.java
```

#### 2.2 统一Gateway的CORS配置

**文件位置**：`market-gateway/src/main/java/org/shyu/marketgateway/config/CorsConfig.java`

**配置内容**：
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的前端域名
        config.addAllowedOriginPattern("http://localhost:5173");
        config.addAllowedOriginPattern("http://127.0.0.1:5173");
        
        // 允许所有请求头和方法
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        
        // 允许携带认证信息
        config.setAllowCredentials(true);
        
        // 预检请求缓存时间
        config.setMaxAge(3600L);
        
        // 暴露的响应头
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Disposition");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}
```

#### 2.3 移除bootstrap.yml中的重复配置

已从`market-gateway/src/main/resources/bootstrap.yml`中移除：
```yaml
globalcors:
  cors-configurations:
    '[/**]':
      allowedOriginPatterns: "*"
      allowedMethods: "*"
      allowedHeaders: "*"
      allowCredentials: true
```

## 验证步骤

1. **重启Gateway服务**
   ```bash
   # 停止Gateway
   # 重新启动Gateway
   ```

2. **重启File服务**（如果正在运行）
   ```bash
   # 停止File服务
   # 重新启动File服务
   ```

3. **清除浏览器缓存**
   - 按F12打开开发者工具
   - 右键刷新按钮，选择"清空缓存并硬性重新加载"

4. **测试文件上传功能**
   - 访问前端页面
   - 尝试上传头像
   - 检查Network标签，确认：
     - OPTIONS预检请求返回200
     - POST请求成功，没有CORS错误
     - 响应头中只有一个`Access-Control-Allow-Origin`

## CORS配置最佳实践

### 微服务架构中的CORS处理

```
前端(5173) → Gateway(9000) → 后端服务(8101, 8102, ...)
              ↑
              只在这里配置CORS
```

**原因**：
1. Gateway是所有外部请求的统一入口
2. 后端服务之间使用Feign调用，不涉及跨域
3. 避免重复配置导致响应头冲突

### 生产环境建议

**开发环境**：
```java
config.addAllowedOriginPattern("http://localhost:5173");
```

**生产环境**：
```java
// 指定具体的前端域名
config.addAllowedOrigin("https://your-domain.com");
config.addAllowedOrigin("https://www.your-domain.com");

// 不要使用通配符
// ❌ config.addAllowedOriginPattern("*");
```

## 常见CORS错误

### 1. 多个Origin值
```
The 'Access-Control-Allow-Origin' header contains multiple values
```
**原因**：多处配置了CORS  
**解决**：只在Gateway配置

### 2. 不允许携带凭证
```
Response to preflight request doesn't pass access control check: 
The value of the 'Access-Control-Allow-Origin' header in the response 
must not be the wildcard '*' when the request's credentials mode is 'include'
```
**原因**：使用了通配符且`allowCredentials=true`  
**解决**：使用`addAllowedOriginPattern()`而不是`addAllowedOrigin("*")`

### 3. 预检请求失败
```
OPTIONS request fails with 401 or 403
```
**原因**：OPTIONS预检请求也被拦截器拦截  
**解决**：在拦截器中放行OPTIONS请求

## 相关文件

- Gateway CORS配置：`market-gateway/src/main/java/org/shyu/marketgateway/config/CorsConfig.java`
- Gateway路由配置：`market-gateway/src/main/resources/bootstrap.yml`
- 白名单配置：`gateway.auth.white-list`

## 更新日期

2026-02-15

