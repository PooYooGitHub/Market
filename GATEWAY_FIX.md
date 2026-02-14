# Gateway启动问题修复

## 问题描述
```
Spring MVC found on classpath, which is incompatible with Spring Cloud Gateway.
```

## 根本原因

**Spring Cloud Gateway使用的是WebFlux（响应式编程模型）**，但`market-common`模块依赖了`spring-boot-starter-web`（Spring MVC），导致冲突。

### 技术背景
- **Spring MVC**: 传统的同步、阻塞式Web框架
- **Spring WebFlux**: 响应式、非阻塞式Web框架
- **Spring Cloud Gateway**: 基于WebFlux构建，不能与Spring MVC共存

## 解决方案

### 1. ✅ 排除Spring MVC依赖

修改 `market-gateway/pom.xml`，在引入`market-common`时排除Spring MVC：

```xml
<dependency>
    <groupId>org.shyu</groupId>
    <artifactId>market-common</artifactId>
    <exclusions>
        <!-- Gateway使用WebFlux，排除Spring MVC -->
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 2. ✅ 明确指定Web应用类型

在 `application.yml` 中添加：

```yaml
spring:
  main:
    web-application-type: reactive
```

### 3. ✅ 更新配置

- **端口**: 改为9000（避免与market-auth的8080冲突）
- **命名空间**: 使用默认的public命名空间
- **认证**: 添加Nacos用户名密码

## 启动方式

### 方式1：使用脚本（推荐）
```powershell
.\start-gateway.ps1
```

### 方式2：在IDEA中运行
直接运行 `MarketGatewayApplication`

### 方式3：Maven命令
```powershell
cd market-gateway
mvn spring-boot:run
```

## 端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| Nacos | 8849 | 服务注册中心 |
| market-auth | 8080 | 认证服务 |
| **market-gateway** | **9000** | **API网关** |

## 验证

### 1. 检查Gateway启动日志
应该看到：
```
Tomcat started on port(s): 9000 (http)
nacos registry, DEFAULT_GROUP market-gateway 127.0.0.1:9000 register finished
```

### 2. 访问Nacos控制台
- URL: http://localhost:8849/nacos
- 在服务列表中应该能看到 `market-gateway`

### 3. 测试Gateway路由
```powershell
# 通过Gateway访问认证服务
curl http://localhost:9000/api/auth/test
```

## 架构说明

### 服务调用链路
```
客户端 -> Gateway(9000) -> 后端服务(8080等)
```

### Gateway路由配置
```yaml
routes:
  - id: market-auth
    uri: lb://market-auth        # 通过Nacos负载均衡
    predicates:
      - Path=/api/auth/**         # 匹配 /api/auth/** 的请求
    filters:
      - StripPrefix=1             # 去掉 /api 前缀
```

**示例**：
- 请求: `http://localhost:9000/api/auth/login`
- Gateway转发到: `http://market-auth/login`

## 注意事项

### ⚠️ Gateway不能使用Spring MVC注解

**错误示例**：
```java
@RestController  // ❌ 不能用
@RequestMapping  // ❌ 不能用
```

**正确做法**：
- Gateway只做路由转发
- 业务逻辑写在后端服务中

### ⚠️ 依赖冲突检查

如果遇到类似问题，使用Maven命令检查依赖树：
```powershell
mvn dependency:tree | Select-String "spring-boot-starter-web"
```

## 相关文件

### 配置文件
- `market-gateway/src/main/resources/bootstrap.yml` - Nacos和路由配置
- `market-gateway/src/main/resources/application.yml` - 应用配置

### 启动脚本
- `start-gateway.ps1` - Gateway启动脚本

### 依赖配置
- `market-gateway/pom.xml` - Maven依赖（已排除Spring MVC）

## 技术要点

### Spring Cloud Gateway特点
1. **异步非阻塞**: 基于Netty和Reactor
2. **高性能**: 比Zuul性能更好
3. **功能丰富**: 支持路由、过滤器、限流等
4. **Spring生态**: 与Spring Cloud无缝集成

### WebFlux vs MVC对比

| 特性 | WebFlux (Gateway) | MVC (其他服务) |
|------|------------------|----------------|
| 编程模型 | 响应式、异步 | 命令式、同步 |
| 线程模型 | 事件循环 | 每请求一线程 |
| 适用场景 | 网关、高并发 | 业务逻辑 |
| 依赖容器 | Netty | Tomcat |

## 故障排查

### 问题1：端口冲突
**错误**: `Port 9000 already in use`
**解决**: 修改`bootstrap.yml`中的`server.port`

### 问题2：找不到服务
**错误**: `503 Service Unavailable`
**解决**: 
1. 确认后端服务已启动并注册到Nacos
2. 检查路由配置的服务名是否正确

### 问题3：依然报Spring MVC冲突
**解决**:
```powershell
# 清理并重新编译
mvn clean compile -DskipTests
```

## 下一步

### ✅ 已完成
- [x] 排除Spring MVC依赖
- [x] 配置Gateway路由
- [x] 修改端口避免冲突
- [x] 添加Nacos认证

### 🔄 可以做的
- [ ] 启动Gateway服务
- [ ] 测试路由转发
- [ ] 添加自定义过滤器
- [ ] 配置限流策略
- [ ] 集成Sa-Token网关鉴权

---

**状态**: ✅ 问题已修复，可以启动
**修复时间**: 2026-02-14
**修复内容**: 排除Spring MVC依赖，配置响应式Web应用

