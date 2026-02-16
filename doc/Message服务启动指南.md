# Message服务启动指南

## 快速启动

### 方式1：直接启动（推荐 - 无需Redis）
```bash
# 进入message服务目录
cd D:\program\Market\market-service\market-service-message

# 编译
mvn clean compile -DskipTests

# 启动
mvn spring-boot:run
```

或者在IDEA中直接运行`MarketServiceMessageApplication`的main方法。

### 方式2：启用Redis后启动
如果需要离线消息推送功能：

1. 启动Redis服务器
```bash
docker start redis
# 或
redis-server
```

2. 修改`MarketServiceMessageApplication.java`，注释掉Redis排除：
```java
@SpringBootApplication(exclude = {
    // RedisAutoConfiguration.class  // 注释掉这行
})
```

3. 启动服务

## 验证服务状态

### 1. 检查服务启动日志
```
2026-02-16 12:36:24.811  INFO - ==========================================
2026-02-16 12:36:24.811  INFO - RocketMQ 配置已加载
2026-02-16 12:36:24.811  INFO - 如果 RocketMQ 未启动，消息发送会自动降级
2026-02-16 12:36:24.811  INFO - 不影响 WebSocket 实时推送和数据库存储
2026-02-16 12:36:24.811  INFO - ==========================================
```

### 2. 检查Nacos注册
访问 http://localhost:8848/nacos
查看`market-service-message`是否已注册

### 3. 测试WebSocket连接
```javascript
// 浏览器控制台测试
const ws = new WebSocket('ws://localhost:8103/websocket?userId=YOUR_USER_ID');
ws.onopen = () => console.log('Connected');
ws.onmessage = (e) => console.log('Message:', e.data);
```

## 功能验证

### 无Redis模式
- ✅ WebSocket连接成功
- ✅ 发送消息成功
- ✅ 接收消息成功  
- ✅ 消息存储到数据库
- ✅ 查询历史消息成功
- ❌ 离线消息推送（需要Redis）

### 有Redis模式
所有功能都可用，包括离线消息推送。

## 端口说明
- **8103**: HTTP服务端口
- **8103/websocket**: WebSocket连接端口

## 依赖服务
- **必需**:
  - MySQL (market_message数据库)
  - Nacos (服务注册)
  
- **可选**:
  - Redis (离线消息推送)
  - RocketMQ (消息队列，不启动会自动降级)

## 常见问题

### Q: 启动报错 "Field redisTemplate required a bean"
A: 确保`MarketServiceMessageApplication.java`中已排除Redis自动配置：
```java
@SpringBootApplication(exclude = { RedisAutoConfiguration.class })
```

### Q: WebSocket连接失败
A: 检查：
1. 服务是否启动成功
2. userId参数是否正确
3. 端口8103是否被占用

### Q: 消息发送失败
A: 检查：
1. MySQL数据库连接是否正常
2. market_message数据库是否存在
3. 查看后端日志错误信息

### Q: 想启用离线消息推送
A: 
1. 启动Redis
2. 注释掉Redis排除配置
3. 重启服务

## 开发建议

1. **本地开发**：不启动Redis，加快启动速度
2. **功能测试**：启动Redis，测试完整功能
3. **生产环境**：建议启动Redis，提供更好的用户体验

## 监控和日志

### 日志级别
默认INFO级别，可在application.yml修改：
```yaml
logging:
  level:
    org.shyu.marketservicemessage: debug
```

### 关键日志
- 消息发送：`INFO - 发送消息成功`
- 消息接收：`INFO - 收到消息`
- Redis状态：`WARN - Redis未配置`
- WebSocket连接：`INFO - WebSocket连接成功`

