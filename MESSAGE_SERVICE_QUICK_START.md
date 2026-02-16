# 消息服务快速启动指南

## 🚀 立即启动（无需 Redis）

### 1. 启动服务

```powershell
cd D:\program\Market
.\start-message.ps1
```

### 2. 验证启动成功

**查看日志**，应该看到：
```
✅ Tomcat started on port(s): 8103 (http)
✅ market-service-message 注册到 Nacos 成功

⚠️ OfflineMessageService 不可用，离线消息功能已降级
```

### 3. 测试接口

```bash
# 获取在线用户数
curl http://localhost:8103/message/online/count

# 预期返回
{"code":200,"message":"操作成功","data":0}
```

---

## 📊 功能状态说明

| 功能 | 状态 | 说明 |
|------|-----|------|
| ✅ 实时聊天 | 正常 | WebSocket 连接正常 |
| ✅ 消息发送 | 正常 | 消息可以发送和接收 |
| ✅ 历史记录 | 正常 | 从数据库读取历史消息 |
| ✅ 会话列表 | 正常 | 显示所有聊天会话 |
| ✅ 在线状态 | 正常 | 实时显示用户在线状态 |
| ⚠️ 离线推送 | 降级 | 需要手动查看历史记录 |
| ⚠️ 消息提醒 | 降级 | 无红点提示，需主动刷新 |

---

## 🎯 核心功能测试

### 场景1: 实时聊天（双方在线）

```
1. 用户A 登录系统
2. 用户B 登录系统
3. 用户A 给 用户B 发消息："你好"
4. 验证: 用户B 立即收到消息 ✅
```

### 场景2: 离线消息（降级模式）

```
1. 用户A 在线，用户B 离线
2. 用户A 给 用户B 发消息："在吗？"
3. 用户B 上线
4. 验证:
   - 消息已保存到数据库 ✅
   - 用户B 打开聊天窗口可以看到消息 ✅
   - 没有弹窗提醒 ⚠️（降级，需要 Redis）
```

### 场景3: 查看历史记录

```
1. 打开任意会话
2. 滚动到顶部
3. 验证: 可以加载更多历史消息 ✅
```

---

## 🔧 如果需要完整功能

### 启动 Redis

```powershell
# 方式1: Docker（推荐）
docker run -d --name market-redis -p 6379:6379 redis

# 方式2: Docker Compose
cd D:\program\Market\docker
docker-compose up -d redis

# 验证
docker ps | findstr redis
```

### 重启消息服务

```powershell
# 停止当前服务（如果在运行）
# Ctrl+C

# 重新启动
cd D:\program\Market
.\start-message.ps1
```

### 验证完整功能

**查看日志**，应该**不再看到**降级提示：
```
✅ Tomcat started on port(s): 8103 (http)
✅ market-service-message 注册到 Nacos 成功
✅ Redis 连接成功
```

---

## ❗ 常见问题

### Q1: 服务启动后立即关闭？

**原因**: 可能是端口被占用

**解决**:
```powershell
# 检查 8103 端口
netstat -ano | findstr 8103

# 如果被占用，结束进程
taskkill /PID <进程ID> /F
```

### Q2: 看到 MySQL 连接错误？

**检查**:
```powershell
# 1. MySQL 是否运行
docker ps | findstr mysql

# 2. 数据库是否存在
mysql -u root -p -e "SHOW DATABASES LIKE 'market_message';"

# 3. 如果不存在，创建数据库
cd D:\program\Market
.\init-database.ps1
```

### Q3: 看到 Nacos 连接失败？

**检查**:
```powershell
# 1. Nacos 是否运行
docker ps | findstr nacos

# 2. 访问 Nacos 控制台
# http://localhost:8848/nacos
# 用户名: nacos
# 密码: nacos
```

### Q4: WebSocket 连接失败？

**检查前端配置**:
```javascript
// src/utils/websocket.js
const WS_URL = 'ws://localhost:8103/ws/chat/'

// 确保端口号是 8103
```

---

## 📝 启动日志参考

### 正常启动（无 Redis）

```
2026-02-16 12:30:00.123  INFO --- [main] o.s.m.MarketServiceMessageApplication    : Starting MarketServiceMessageApplication
2026-02-16 12:30:01.456  INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8103
2026-02-16 12:30:02.789  INFO --- [main] c.a.c.n.registry.NacosServiceRegistry    : nacos registry, market-service-message register finished
2026-02-16 12:30:03.012  INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8103 (http)
2026-02-16 12:30:03.234  WARN --- [main] o.s.m.controller.MessageController       : OfflineMessageService 不可用，离线消息功能已降级
2026-02-16 12:30:03.456  INFO --- [main] o.s.m.MarketServiceMessageApplication    : Started MarketServiceMessageApplication in 3.456 seconds
```

### 完整启动（有 Redis）

```
2026-02-16 12:30:00.123  INFO --- [main] o.s.m.MarketServiceMessageApplication    : Starting MarketServiceMessageApplication
2026-02-16 12:30:01.456  INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8103
2026-02-16 12:30:02.123  INFO --- [main] o.s.d.redis.core.RedisConnectionFactory  : Redis connection established
2026-02-16 12:30:02.789  INFO --- [main] c.a.c.n.registry.NacosServiceRegistry    : nacos registry, market-service-message register finished
2026-02-16 12:30:03.012  INFO --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8103 (http)
2026-02-16 12:30:03.456  INFO --- [main] o.s.m.MarketServiceMessageApplication    : Started MarketServiceMessageApplication in 3.456 seconds
```

---

## 🎉 成功标志

当你看到以下内容时，说明服务已经成功启动：

1. ✅ `Tomcat started on port(s): 8103`
2. ✅ `nacos registry ... register finished`
3. ✅ 可以访问接口：http://localhost:8103/message/online/count
4. ✅ 前端可以连接 WebSocket

---

## 📞 需要帮助？

如果遇到问题，请查看：
- [MESSAGE_SERVICE_FIX.md](./MESSAGE_SERVICE_FIX.md) - 详细修复文档
- [测试指南-消息模块.md](./front/vue-project/测试指南-消息模块.md) - 测试流程
- [前端对接文档-Message模块.md](./对接文档/前端对接文档-Message模块.md) - API 文档

---

**祝你好运！** 🚀

