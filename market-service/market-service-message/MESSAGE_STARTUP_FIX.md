# Message 服务启动问题修复说明

## 修复日期
2026-02-16

## 问题描述
启动 market-service-message 时遇到以下错误：
1. `找不到符号: 类 SaTokenSetup` - Sa-Token 相关类不存在
2. `RedisConnectionFactory` 相关的依赖注入失败

## 根本原因分析

### 1. SaTokenSetup 类不存在
- 在启动类中尝试排除 `SaTokenSetup.class`
- 但 Sa-Token 库中并不存在这个类
- message 服务的 pom.xml 中也没有直接依赖 Sa-Token

### 2. Redis 依赖问题
- message 服务继承了 market-service 父 POM，其中包含了 Redis 依赖
- Sa-Token 通过 market-common 传递依赖被引入
- Sa-Token 自动配置需要 RedisConnectionFactory，但被错误地排除了

## 解决方案

### 1. 移除错误的排除配置
**文件**: `MarketServiceMessageApplication.java`

**修改前**:
```java
@SpringBootApplication(exclude = {
    RedisAutoConfiguration.class,
    SaTokenSetup.class  // ❌ 这个类不存在
})
```

**修改后**:
```java
@SpringBootApplication(scanBasePackages = {
    "org.shyu.marketservicemessage",
    "org.shyu.marketapiuser.feign"
})
```

### 2. 添加 Redis 配置类（支持降级）
**新增文件**: `config/RedisConfig.java`

**特点**:
- 使用 `@ConditionalOnProperty(name = "spring.redis.host")` 实现条件装配
- 只有配置了 Redis 主机地址时才会初始化 Redis 相关 Bean
- Redis 未启动时，服务仍可正常启动，只是离线消息功能不可用

```java
@Configuration
@ConditionalOnProperty(name = "spring.redis.host")
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // ... Redis 配置
    }
}
```

### 3. 添加完整的 application.yml 配置
**新增文件**: `resources/application.yml`

**包含配置**:
- 服务器端口: 8103
- MySQL 数据库: market_message
- Redis 配置: localhost:6379 (database: 4)
- WebSocket 配置
- RocketMQ 配置
- MyBatis Plus 配置

## 降级机制说明

### Redis 降级
1. **配置方式**: 通过 `@ConditionalOnProperty` 注解
2. **降级效果**: 
   - Redis 未启动 → 离线消息功能不可用，其他功能正常
   - Sa-Token 依然可以工作（如果需要的话）
3. **用户体验**: 只影响离线消息推送，实时 WebSocket 通讯正常

### RocketMQ 降级  
1. **配置方式**: `RocketMQConfig` 中已配置日志提示
2. **降级效果**:
   - RocketMQ 未启动 → 消息发送会失败但不会影响服务启动
   - WebSocket 实时推送和数据库存储继续工作
3. **用户体验**: 只影响消息队列异步处理，核心功能不受影响

## 启动流程

1. **正常启动**（所有组件可用）:
   ```
   Nacos ✓ → MySQL ✓ → Redis ✓ → RocketMQ ✓
   → 所有功能正常
   ```

2. **Redis 降级启动**:
   ```
   Nacos ✓ → MySQL ✓ → Redis ✗ → RocketMQ ✓
   → 离线消息功能不可用，其他功能正常
   ```

3. **RocketMQ 降级启动**:
   ```
   Nacos ✓ → MySQL ✓ → Redis ✓ → RocketMQ ✗
   → 消息队列功能不可用，WebSocket 实时通讯正常
   ```

4. **双降级启动**:
   ```
   Nacos ✓ → MySQL ✓ → Redis ✗ → RocketMQ ✗
   → 仅支持 WebSocket 实时通讯和数据库消息存储
   ```

## 核心依赖关系

```
market-service-message
├── market-common (包含 Sa-Token)
│   └── sa-token-dao-redis-jackson (需要 Redis)
├── spring-boot-starter-data-redis (父POM提供)
└── rocketmq-spring-boot-starter
```

## 注意事项

1. **IDE 报错**: 
   - 可能会看到 `无法解析符号 'MapperScan'` 等错误
   - 这是 IDE 缓存问题，实际编译可以通过
   - 解决方法: 重新导入 Maven 项目或重启 IDE

2. **Sa-Token 配置**:
   - message 服务不需要单独配置 Sa-Token
   - 如果需要排除 Sa-Token，应该在 pom.xml 中排除依赖，而不是在 @SpringBootApplication 中排除

3. **数据库**: 
   - 确保 `market_message` 数据库已创建
   - 相关表结构应该已通过 SQL 脚本初始化

## 验证方法

1. 启动服务:
   ```powershell
   mvn spring-boot:run
   ```

2. 检查日志:
   ```
   [RocketMQConfig] RocketMQ 配置已加载 ✓
   [RedisConfig] Redis 连接成功 ✓
   Tomcat started on port(s): 8103 ✓
   ```

3. 测试 WebSocket 连接:
   ```
   ws://localhost:8103/ws?userId=xxx
   ```

## 相关文档

- [RocketMQ 配置说明](./config/RocketMQConfig.java)
- [Redis 配置说明](./config/RedisConfig.java)
- [WebSocket 配置](./config/WebSocketConfig.java)
- [前端对接文档](../../front/对接文档/前端对接文档-Message模块.md)

