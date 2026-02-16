# Message Service Redis 降级处理修复

## 问题描述

Message Service 启动失败，错误信息：
```
Field redisTemplate in org.shyu.marketservicemessage.listener.ChatMessageListener 
required a bean of type 'org.springframework.data.redis.core.RedisTemplate' 
that could not be found.
```

## 根本原因

虽然在 `MarketServiceMessageApplication` 中排除了 `RedisAutoConfiguration`：
```java
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
```

但是 `ChatMessageListener` 中仍然使用了 `@Autowired(required=true)` 强制要求注入 `RedisTemplate`，导致启动失败。

## 解决方案

### 1. 修改 ChatMessageListener.java

将 `redisTemplate` 的注入改为可选：

```java
@Autowired(required = false)
private RedisTemplate<String, Object> redisTemplate;
```

### 2. 添加空值检查

在 `storeOfflineMessage()` 方法中添加 Redis 可用性检查：

```java
private void storeOfflineMessage(ChatMessage message) {
    try {
        // Redis未配置时，跳过离线消息存储
        if (redisTemplate == null) {
            log.warn("Redis未配置，跳过离线消息存储: messageId={}, receiver={}", 
                    message.getId(), message.getReceiverId());
            return;
        }
        
        // ...原有的Redis存储逻辑
    } catch (Exception e) {
        log.error("存储离线消息失败: messageId={}", message.getId(), e);
    }
}
```

## 降级策略说明

### Redis 可用时
1. ✅ WebSocket 实时推送（用户在线）
2. ✅ Redis 离线消息存储（用户离线）
3. ✅ MySQL 消息持久化
4. ✅ RocketMQ 消息归档（可选）

### Redis 不可用时
1. ✅ WebSocket 实时推送（用户在线） - **正常工作**
2. ⚠️ Redis 离线消息存储（用户离线） - **降级：跳过存储，记录日志**
3. ✅ MySQL 消息持久化 - **正常工作**
4. ⚠️ RocketMQ 消息归档（可选） - **降级：连接超时但不影响主流程**

## 影响范围

### 不受影响的功能
- ✅ 在线用户之间的实时聊天（通过WebSocket）
- ✅ 消息数据库存储（MySQL）
- ✅ 会话列表查询
- ✅ 历史消息查询

### 受影响的功能
- ⚠️ 离线消息推送：用户离线期间的消息无法暂存到Redis，下次上线时需要主动查询数据库获取未读消息

## 使用建议

### 开发环境
- 可以不启动 Redis，不影响核心聊天功能
- 离线消息功能会自动降级
- 适合快速开发和测试

### 生产环境
- **建议启动 Redis**，确保离线消息推送功能完整可用
- Redis 提供更好的性能和用户体验
- 支持离线消息即时通知

## 验证步骤

1. **启动服务**
   ```bash
   cd D:\program\Market\market-service\market-service-message
   mvn spring-boot:run
   ```

2. **检查日志**
   - 如果 Redis 未启动，应该看到：
     ```
     RocketMQ 配置已加载
     如果 RocketMQ 未启动，消息发送会自动降级
     不影响 WebSocket 实时推送和数据库存储
     ```
   
   - 发送消息时，如果用户离线且 Redis 未配置，会看到：
     ```
     Redis未配置，跳过离线消息存储: messageId=xxx, receiver=xxx
     ```

3. **功能测试**
   - 两个用户同时在线 → 消息实时送达 ✅
   - 一个用户离线 → 消息存储到数据库，下次登录可查询 ✅
   - 查询历史消息 → 正常显示 ✅

## 总结

通过将 Redis 依赖改为可选，并添加空值检查，实现了消息服务的优雅降级：

- **核心功能不受影响**：实时聊天、消息存储、消息查询
- **可选功能自动降级**：离线消息推送（需要Redis支持）
- **灵活的部署选项**：开发环境可以不启动Redis，生产环境建议启动Redis

修复时间：2026-02-16
修复人：shyu

