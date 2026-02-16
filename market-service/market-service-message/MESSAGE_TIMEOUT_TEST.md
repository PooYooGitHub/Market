# 消息发送超时修复 - 测试指南

## 测试目标
验证消息发送超时问题已解决，前端不再报错

## 前置条件

1. ✅ 启动 Nacos (端口: 8848)
2. ✅ 启动 MySQL (market_message 数据库)
3. ✅ 启动 Gateway (端口: 9901)
4. ✅ 启动 User 服务 (端口: 8101)
5. ✅ 启动 Message 服务 (端口: 8103)
6. ⚠️ RocketMQ (可选，测试降级功能)

## 测试场景

### 场景 1: RocketMQ 正常运行

#### 步骤
1. 启动 RocketMQ
   ```powershell
   # 进入 RocketMQ docker 容器
   docker exec -it rocketmq bash
   ```

2. 登录前端，打开两个浏览器窗口（模拟两个用户）
   - 窗口 A: 登录用户 A
   - 窗口 B: 登录用户 B

3. 在窗口 A 中发送消息给用户 B

4. 观察控制台日志

#### 预期结果
- ✅ 前端：消息发送成功，无超时错误
- ✅ 窗口 B：实时收到消息（WebSocket）
- ✅ 后端日志：
  ```
  消息发送成功: userA -> userB
  RocketMQ消息推送成功
  ```
- ✅ 响应时间：< 1 秒

---

### 场景 2: RocketMQ 超时（模拟）

#### 步骤
1. 停止 RocketMQ Broker（保持 NameServer 运行）
   ```powershell
   # 在 Docker 中停止 Broker
   docker stop rocketmq-broker
   ```

2. 发送消息

#### 预期结果
- ✅ 前端：消息发送成功，无超时错误
- ✅ 消息正常送达
- ⚠️ 后端日志：
  ```
  消息发送成功: userA -> userB
  RocketMQ消息推送失败(不影响消息发送): sendDefaultImpl call timeout
  ```
- ✅ 响应时间：< 1 秒（不被 RocketMQ 阻塞）

---

### 场景 3: RocketMQ 完全未启动

#### 步骤
1. 停止 RocketMQ
   ```powershell
   docker stop rocketmq
   ```

2. 重启 Message 服务（会检测到 RocketMQ 不可用）

3. 发送消息

#### 预期结果
- ✅ 前端：消息发送成功，无超时错误
- ✅ 消息正常送达
- ℹ️ 后端日志：
  ```
  消息发送成功: userA -> userB
  RocketMQ未初始化，跳过消息推送
  ```
- ✅ 响应时间：< 1 秒

---

### 场景 4: 压力测试

#### 步骤
1. 快速连续发送 10 条消息

#### 预期结果
- ✅ 所有消息发送成功
- ✅ 无超时错误
- ✅ 消息顺序正确
- ✅ 平均响应时间 < 1 秒

---

## 性能对比

### 修复前
```
请求耗时: 12-15 秒
前端状态: AxiosError: timeout of 10000ms exceeded ❌
用户体验: 差 (看到错误提示)
```

### 修复后
```
请求耗时: 200-500 毫秒
前端状态: 发送成功 ✓
用户体验: 好 (流畅发送)
```

---

## 检查点

### 前端检查
- [ ] 发送消息后，输入框清空
- [ ] 消息立即显示在聊天窗口中
- [ ] 无 "timeout" 错误提示
- [ ] 无 "Network Error" 错误提示

### 后端检查
- [ ] Message 服务日志: "消息发送成功"
- [ ] 数据库: `t_chat_message` 表有新记录
- [ ] 数据库: `t_conversation` 表更新
- [ ] WebSocket: 对方实时收到消息

### RocketMQ 检查（如果启动）
- [ ] RocketMQ 日志: 收到消息（异步，可能稍晚）
- [ ] 或者: "RocketMQ消息推送失败(不影响消息发送)"

---

## 故障排查

### 问题 1: 前端仍然超时

**可能原因**:
- 前端代码未重新构建
- 浏览器缓存未清除

**解决方法**:
```powershell
# 重新启动前端
cd front/vue-project
npm run dev
```
- 清除浏览器缓存（Ctrl + Shift + Delete）
- 强制刷新页面（Ctrl + F5）

---

### 问题 2: 消息发送失败

**可能原因**:
- Gateway 未启动
- User 服务未启动
- Token 过期

**解决方法**:
- 检查所有服务是否正常运行
- 重新登录获取新 Token
- 查看后端日志确定具体错误

---

### 问题 3: WebSocket 连接失败

**可能原因**:
- Message 服务未启动
- 端口被占用

**解决方法**:
- 检查 Message 服务是否在 8103 端口运行
- 查看浏览器控制台 WebSocket 连接状态

---

## 日志示例

### 正常流程日志
```
2026-02-16 12:00:00.123 [http-nio-8103-exec-1] INFO  MessageServiceImpl - 消息发送成功: 8 -> 6
2026-02-16 12:00:00.124 [pool-1-thread-1] INFO  MessageServiceImpl - RocketMQ消息推送成功
```

### RocketMQ 超时日志（不影响功能）
```
2026-02-16 12:00:00.123 [http-nio-8103-exec-1] INFO  MessageServiceImpl - 消息发送成功: 8 -> 6
2026-02-16 12:00:03.456 [pool-1-thread-1] WARN  MessageServiceImpl - RocketMQ消息推送失败(不影响消息发送): sendDefaultImpl call timeout
```

### RocketMQ 未启动日志
```
2026-02-16 12:00:00.123 [http-nio-8103-exec-1] INFO  MessageServiceImpl - 消息发送成功: 8 -> 6
2026-02-16 12:00:00.123 [http-nio-8103-exec-1] WARN  MessageServiceImpl - RocketMQ未初始化，跳过消息推送
```

---

## 相关文档

- [消息发送超时修复说明](./MESSAGE_TIMEOUT_FIX.md)
- [Message 服务启动问题修复](./MESSAGE_STARTUP_FIX.md)
- [前端对接文档](../../front/对接文档/前端对接文档-Message模块.md)
- [RocketMQ 配置说明](./config/RocketMQConfig.java)

---

## 测试报告模板

```
测试日期: ______
测试人员: ______

场景 1 (RocketMQ 正常): [ ] 通过  [ ] 失败
场景 2 (RocketMQ 超时):  [ ] 通过  [ ] 失败
场景 3 (RocketMQ 未启动): [ ] 通过  [ ] 失败
场景 4 (压力测试):      [ ] 通过  [ ] 失败

前端检查: [ ] 全部通过
后端检查: [ ] 全部通过
RocketMQ 检查: [ ] 全部通过  [ ] N/A

平均响应时间: ______ ms
用户体验评分: ______ / 10

问题记录:
____________________________________
____________________________________

总结:
____________________________________
____________________________________
```

