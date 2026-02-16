# Message服务Swagger启动问题解决方案

## 问题描述
```
org.springframework.context.ApplicationContextException: Failed to start bean 'documentationPluginsBootstrapper'; 
nested exception is java.lang.NullPointerException
```

## 根本原因
Springfox 3.0.0与Spring Boot 2.6+版本的Spring MVC路径匹配策略不兼容。

Spring Boot 2.6+默认使用`PathPatternParser`,而Springfox期望使用`AntPathMatcher`。

## 已实施的解决方案

### 1. 修改 `application.yml`
在`spring`配置下添加:
```yaml
spring:
  # 修复Swagger与Spring Boot 2.6+兼容性问题
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
```

### 2. 修改 `SwaggerConfig.java`
```java
@Configuration
@EnableSwagger2
@ConditionalOnWebApplication  // 添加条件注解
public class SwaggerConfig implements WebMvcConfigurer {  // 实现WebMvcConfigurer
    // ...existing code...
}
```

## 启动方法

### 方法1: 使用IDEA
1. 在IDEA中找到`MarketServiceMessageApplication`
2. 右键 → Run 'MarketServiceMessageApplication'

### 方法2: 使用Maven命令
```powershell
cd D:\program\Market\market-service\market-service-message
mvn spring-boot:run
```

### 方法3: 使用启动脚本
```powershell
cd D:\program\Market
.\test-message-start.ps1
```

## 验证服务启动

### 1. 检查日志
看到以下信息表示启动成功:
```
Tomcat started on port(s): 8103 (http)
nacos registry, DEFAULT_GROUP market-service-message ...register finished
Started MarketServiceMessageApplication in ... seconds
```

### 2. 访问Swagger文档
浏览器打开: http://localhost:8103/swagger-ui/

### 3. 检查Nacos注册
访问Nacos控制台: http://localhost:8848/nacos
- 用户名: nacos
- 密码: nacos
- 检查服务列表中是否有`market-service-message`

## 依赖服务检查清单

启动message服务前,确保以下服务正在运行:

- [ ] **Nacos** (端口8848) - 服务注册中心
  ```powershell
  # 检查: 访问 http://localhost:8848/nacos
  ```

- [ ] **MySQL** (端口3306) - 数据库
  ```powershell
  # 检查数据库market_message是否存在
  ```

- [ ] **Redis** (端口6379) - 缓存
  ```powershell
  # 使用redis-cli或者RedisInsight连接测试
  ```

- [ ] **RocketMQ NameServer** (端口9876) - 消息队列名称服务器
  ```powershell
  docker ps | findstr rocketmq
  ```

- [ ] **RocketMQ Broker** (端口10911) - 消息队列broker
  ```powershell
  docker ps | findstr rocketmq
  ```

## 端口使用情况

| 服务 | 端口 | 说明 |
|------|------|------|
| market-gateway | 9901 | 网关服务 |
| market-service-user | 8101 | 用户服务 |
| market-service-product | 8102 | 商品服务 |
| market-service-message | 8103 | 消息服务 |
| market-service-file | 8106 | 文件服务 |
| Nacos | 8848 | 服务注册中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| RocketMQ NameServer | 9876 | MQ名称服务 |
| RocketMQ Broker | 10911 | MQ broker |
| MinIO | 9900 | 对象存储 |

## 常见问题

### Q1: 端口8103已被占用
```powershell
# 查找占用端口的进程
netstat -ano | findstr :8103
# 结束进程
taskkill /PID <进程ID> /F
```

### Q2: 无法连接到RocketMQ
检查Docker容器状态:
```powershell
docker ps
# 重启RocketMQ
docker restart rocketmq
```

### Q3: Nacos连接超时
确保Nacos服务正在运行:
```powershell
# 检查Nacos进程
netstat -ano | findstr :8848
```

## 数据库初始化

如果数据库`market_message`不存在,运行初始化脚本:
```powershell
# 脚本位置
D:\program\Market\market-service\market-service-message\src\main\resources\sql\market_message.sql
```

## 下一步

服务启动成功后,可以:
1. 通过Swagger测试API: http://localhost:8103/swagger-ui/
2. 通过Gateway访问: http://localhost:9901/api/message/...
3. 查看前端对接文档: `D:\program\Market\doc\前端对接文档\5-消息服务接口.md`

## 相关文档

- [架构设计文档](D:\program\Market\doc\架构设计.md)
- [消息服务设计](D:\program\Market\doc\消息服务设计.md)
- [RocketMQ使用指南](网上搜索)

---
**解决时间**: 2026-02-15
**状态**: ✅ 已解决

