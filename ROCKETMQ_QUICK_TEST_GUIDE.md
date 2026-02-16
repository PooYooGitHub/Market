# 🚀 RocketMQ离线推送功能 - 快速测试指南

## 📝 测试目标

验证RocketMQ实现的离线消息推送功能是否正常工作。

---

## ⚙️ 前置准备

### 1. 检查服务状态

```powershell
# 检查MySQL
netstat -ano | findstr :3306

# 检查Redis
redis-cli ping
# 应该返回: PONG

# 检查Nacos
curl http://localhost:8848/nacos

# 检查RocketMQ (可选)
docker ps | findstr rocketmq
```

### 2. 启动必要服务

如果服务未启动：

```powershell
# 启动Redis
redis-server

# 启动Nacos
cd D:\program\nacos\bin
startup.cmd

# 启动RocketMQ (可选)
docker start rocketmq-namesrv
docker start rocketmq-broker
```

---

## 🎬 测试步骤

### Step 1: 启动后端服务

#### 1.1 启动Gateway (端口9901)
```powershell
cd D:\program\Market\market-gateway
mvn spring-boot:run
```

等待看到：
```
✅ Tomcat started on port(s): 9901 (http)
```

#### 1.2 启动User服务 (端口8101)
```powershell
cd D:\program\Market\market-service\market-service-user
mvn spring-boot:run
```

等待看到：
```
✅ Tomcat started on port(s): 8101 (http)
```

#### 1.3 启动Message服务 (端口8103)
```powershell
cd D:\program\Market\market-service\market-service-message
mvn spring-boot:run
```

等待看到：
```
✅ Tomcat started on port(s): 8103 (http)
✅ WebSocket started on /ws/chat/{userId}
```

### Step 2: 启动前端

```powershell
cd D:\program\Market\front\vue-project
npm run dev
```

访问: http://localhost:5173

---

## 🧪 测试场景1: 离线消息推送

### 2.1 准备测试用户

- **用户A**: ID=8, 用户名=test_sender
- **用户B**: ID=6, 用户名=test_receiver

### 2.2 发送离线消息

**Step 1**: 登录用户A
```
1. 打开浏览器1，访问 http://localhost:5173
2. 登录用户A (username=test_sender)
3. 进入消息页面
```

**Step 2**: 确保用户B未登录
```
重要：用户B此时不能在线！
```

**Step 3**: 发送3条消息
```
1. 用户A选择与用户B的会话
2. 发送消息1: "你好，这是第一条离线消息"
3. 发送消息2: "这是第二条"
4. 发送消息3: "这是第三条"
```

**Step 4**: 检查Redis存储
```powershell
redis-cli
SELECT 3
LRANGE offline:message:user:6 0 -1
```

**预期结果**:
```
1) "消息ID1"
2) "消息ID2"
3) "消息ID3"
```

### 2.3 接收离线消息

**Step 1**: 登录用户B
```
1. 打开浏览器2，访问 http://localhost:5173
2. 登录用户B (username=test_receiver)
```

**Step 2**: 观察前端控制台
```
应该看到：
📮 您有 3 条离线消息
WebSocket收到消息: {...}
WebSocket收到消息: {...}
WebSocket收到消息: {...}
```

**Step 3**: 进入与用户A的会话
```
1. 点击消息页面
2. 选择与用户A的会话
3. 应该看到3条离线消息
```

**Step 4**: 验证Redis已清除
```powershell
redis-cli
SELECT 3
LRANGE offline:message:user:6 0 -1
```

**预期结果**:
```
(empty list or set)
```

---

## 🧪 测试场景2: 在线消息

### 2.1 两个用户同时在线

**Step 1**: 用户A和用户B都保持登录状态

**Step 2**: 用户A发送消息
```
用户A: "这是一条在线消息"
```

**Step 3**: 用户B应该**立即**收到消息
```
✅ 实时显示在聊天窗口
✅ 不经过Redis存储
```

**Step 4**: 检查Redis
```powershell
redis-cli
SELECT 3
LRANGE offline:message:user:6 0 -1
```

**预期结果**:
```
(empty list or set)
因为用户B在线，消息不会存储到Redis
```

---

## 🧪 测试场景3: RocketMQ失败容错

### 3.1 停止RocketMQ

```powershell
# 如果使用Docker
docker stop rocketmq-broker
docker stop rocketmq-namesrv
```

### 3.2 发送消息

用户A发送消息给用户B

### 3.3 观察日志

Message服务日志应该显示：
```
⚠️ RocketMQ消息推送失败(不影响消息发送): sendDefaultImpl call timeout
✅ 消息发送成功: 8 -> 6
```

### 3.4 验证消息仍然正常

**预期结果**:
- ✅ 消息保存到MySQL
- ✅ WebSocket实时推送（如果在线）
- ❌ 离线推送功能不可用（因为RocketMQ停止）

**结论**: 即使RocketMQ失败，聊天功能仍然正常！

---

## 📊 测试检查清单

### 功能测试

- [ ] 离线消息能正确存储到Redis
- [ ] 用户上线自动推送离线消息
- [ ] 推送后Redis记录被清除
- [ ] 在线消息实时推送
- [ ] 前端显示离线消息数量通知
- [ ] RocketMQ失败不影响消息发送

### 性能测试

- [ ] 发送10条离线消息，上线后全部推送
- [ ] 多个用户同时在线，消息不串
- [ ] Redis存储的messageId数量正确

### 边界测试

- [ ] 用户B有100条离线消息，能正常推送
- [ ] 用户B上线又立即下线，消息不丢失
- [ ] 用户A连发10条消息，全部送达

---

## 🐛 常见问题排查

### 问题1: 离线消息没有推送

**可能原因**:
1. RocketMQ未启动或连接失败
2. Redis未正确存储

**排查步骤**:
```powershell
# 查看Message服务日志
tail -f logs/message.log | Select-String "RocketMQ"

# 查看Redis
redis-cli
SELECT 3
KEYS offline:*
```

**解决方案**:
- 启动RocketMQ
- 检查application.yml配置

### 问题2: WebSocket连接失败

**可能原因**:
1. Message服务未启动
2. 端口被占用

**排查步骤**:
```powershell
netstat -ano | findstr :8103
```

**解决方案**:
- 重启Message服务
- 修改端口配置

### 问题3: 消息重复推送

**可能原因**:
1. Redis未正确清除
2. WebSocket重连

**排查步骤**:
```powershell
redis-cli
SELECT 3
LLEN offline:message:user:6
```

**解决方案**:
- 手动清除Redis
- 检查clearOfflineMessages()逻辑

---

## 📈 测试结果记录

### 测试环境

- **日期**: ____________
- **测试人员**: ____________
- **版本**: 1.0.0

### 测试结果

| 测试项 | 状态 | 备注 |
|--------|------|------|
| 离线消息存储 | ⬜ Pass / ⬜ Fail | |
| 离线消息推送 | ⬜ Pass / ⬜ Fail | |
| Redis清除 | ⬜ Pass / ⬜ Fail | |
| 在线消息 | ⬜ Pass / ⬜ Fail | |
| RocketMQ容错 | ⬜ Pass / ⬜ Fail | |

### 性能指标

- 离线消息推送延迟: ______ms
- WebSocket连接时间: ______ms
- Redis读写时间: ______ms

---

## ✅ 测试通过标准

1. ✅ 所有功能测试通过
2. ✅ 无重复推送
3. ✅ 消息不丢失
4. ✅ Redis正确清除
5. ✅ RocketMQ失败不影响主流程

---

## 📞 问题反馈

如果测试失败，请记录：
1. 失败的测试步骤
2. 错误日志
3. 浏览器控制台截图
4. Redis数据截图

---

**Happy Testing! 🎉**

