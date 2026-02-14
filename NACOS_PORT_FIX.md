# Nacos端口映射修复说明

## 问题描述
应用启动时出现错误：`Client not connected, current status:STARTING`

**根本原因**：Nacos 2.x使用gRPC进行客户端与服务端通信，但Docker容器没有映射gRPC端口。

## 端口说明

### Nacos 2.x 端口架构
- **8848**: HTTP API端口（容器内）
- **9848**: gRPC客户端请求端口（容器内，偏移量+1000）
- **9849**: gRPC服务端请求端口（容器内，偏移量+1001）

### 主机端口映射
- **8849 -> 8848**: HTTP API（Nacos控制台访问）
- **9849 -> 9848**: gRPC客户端连接端口
- **9850 -> 9849**: gRPC服务端连接端口（可选）

## 解决方案

### 1. 重启Nacos容器并添加端口映射

使用提供的脚本：
```powershell
.\restart-nacos.ps1
```

或手动执行：
```bash
# 停止并删除旧容器
docker stop nacos
docker rm nacos

# 启动新容器，添加完整端口映射
docker run -d \
  --name nacos \
  -p 8849:8848 \
  -p 9849:9848 \
  -p 9850:9849 \
  -e MODE=standalone \
  -e PREFER_HOST_MODE=ip \
  -e TIME_ZONE=Asia/Shanghai \
  -e JVM_XMS=512m \
  -e JVM_XMX=512m \
  -e JVM_XMN=256m \
  -e NACOS_AUTH_ENABLE=true \
  nacos/nacos-server:v2.2.3
```

### 2. 验证端口映射

检查端口是否正常监听：
```powershell
# 检查HTTP端口
netstat -ano | findstr "8849"

# 检查gRPC端口
netstat -ano | findstr "9849"
```

预期输出应该显示端口处于 `LISTENING` 状态。

### 3. 应用配置

应用的 `bootstrap.yml` 已配置为连接到 `127.0.0.1:8849`：

```yaml
spring:
  cloud:
    nacos:
      server-addr: 127.0.0.1:8849
      username: nacos
      password: nacos
```

客户端会自动计算gRPC端口：`8849 + 1000 = 9849`

## 访问地址

- **Nacos控制台**: http://localhost:8849/nacos
- **用户名**: nacos
- **密码**: nacos

## 验证步骤

1. 访问Nacos控制台，确认可以正常登录
2. 启动应用：`.\start-auth.ps1` 或在IDEA中运行
3. 检查应用日志，应该能看到成功注册到Nacos
4. 在Nacos控制台的"服务管理 -> 服务列表"中查看 `market-auth` 服务

## 内存优化

为了减少内存占用，已将Nacos的JVM参数调整为：
- `-Xms512m`: 初始堆内存512MB
- `-Xmx512m`: 最大堆内存512MB
- `-Xmn256m`: 新生代256MB

原默认值为1GB，现在减少了约50%的内存占用。

## 故障排查

### 如果仍然连接失败

1. **检查防火墙**：确保端口8849和9849没有被防火墙阻止
2. **查看Nacos日志**：`docker logs -f nacos`
3. **检查容器状态**：`docker ps | findstr nacos`
4. **重启容器**：`docker restart nacos`

### 常见错误

- **连接超时**：检查Nacos是否完全启动（需要约30秒）
- **认证失败**：确认用户名密码为 `nacos/nacos`
- **端口占用**：确保8849、9849、9850端口没有被其他程序占用

## 相关文档

- [Nacos官方文档 - 端口说明](https://nacos.io/zh-cn/docs/v2/guide/admin/cluster-mode-quick-start.html)
- [Spring Cloud Alibaba文档](https://spring-cloud-alibaba-group.github.io/)

---

**更新时间**: 2026-02-14
**状态**: ✅ 已修复

