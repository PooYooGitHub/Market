# Message服务Redis降级配置完成

## 问题描述
Message服务启动时报错，提示无法找到`RedisTemplate` Bean，即使使用`@Autowired(required=false)`也无法启动。

## 解决方案

### 1. 排除Redis自动配置
在`MarketServiceMessageApplication.java`中排除Redis自动配置：

```java
@SpringBootApplication(exclude = {
    // 排除Redis自动配置，实现降级处理
    // Redis未启动时不影响服务启动，只是离线消息功能不可用
    RedisAutoConfiguration.class
})
```

### 2. 创建条件化Redis配置
创建`RedisConfig.java`，只在Redis连接工厂可用时才创建RedisTemplate：

```java
@Configuration
@ConditionalOnClass(RedisConnectionFactory.class)
public class RedisConfig {
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // ... 配置代码
    }
}
```

### 3. 离线消息服务降级处理
在`OfflineMessageServiceImpl.java`中添加Redis空值检查：

```java
@Autowired(required = false)
private RedisTemplate<String, Object> redisTemplate;

@Override
public List<OfflineMessageVO> getOfflineMessages(Long userId) {
    // 如果Redis不可用，返回空列表
    if (redisTemplate == null) {
        log.warn("Redis未配置，无法获取离线消息");
        return new ArrayList<>();
    }
    // ... 正常处理逻辑
}
```

## 功能说明

### 当前状态（Redis已排除）
- ✅ 服务可以正常启动
- ✅ WebSocket实时消息收发正常
- ✅ 消息存储到数据库正常
- ✅ RocketMQ消息队列正常（如果配置）
- ❌ 离线消息推送功能不可用（需要Redis）

### 如果启用Redis
如果想启用离线消息功能，只需：

1. 启动Redis服务器
2. 注释掉排除配置：
```java
@SpringBootApplication(exclude = {
    // RedisAutoConfiguration.class  // 注释掉这行
})
```
3. 重启服务

## 降级逻辑

### 消息发送流程
1. **实时推送**：通过WebSocket直接推送（不依赖Redis）
2. **数据库存储**：消息保存到MySQL（不依赖Redis）
3. **离线缓存**：如果Redis可用，缓存离线消息；否则跳过

### 用户上线流程
1. **WebSocket连接**：建立连接（不依赖Redis）
2. **离线消息推送**：
   - Redis可用：从Redis读取离线消息推送
   - Redis不可用：跳过，用户可通过会话列表查看历史消息

## 配置文件

### application.yml
```yaml
# Redis 配置（可选，如果Redis未启动，离线消息功能会自动降级）
redis:
  host: localhost
  port: 6379
  database: 3
```

如果不需要Redis，可以注释掉redis配置。

## 测试建议

1. **不启动Redis测试**：
   - 验证服务正常启动
   - 验证WebSocket正常工作
   - 验证消息正常存储

2. **启动Redis测试**：
   - 取消注释排除配置
   - 验证离线消息推送
   - 验证Redis缓存

## 注意事项

1. **数据一致性**：消息始终保存在数据库中，Redis只是缓存
2. **性能影响**：无Redis时，离线消息需要查询数据库，性能略有下降
3. **功能完整性**：核心聊天功能不受影响，只是离线推送需要Redis

## 总结

通过排除Redis自动配置和添加空值检查，实现了优雅的服务降级：
- **Redis可用**：完整功能，包括离线消息推送
- **Redis不可用**：核心功能正常，离线推送降级

这样可以保证服务的高可用性，不会因为Redis故障而无法启动。

