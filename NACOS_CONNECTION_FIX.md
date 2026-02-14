# Nacos连接问题解决方案

## 问题描述
market-auth启动时无法连接到Nacos服务器，错误信息：
```
Client not connected, current status:STARTING
com.alibaba.nacos.api.exception.NacosException: Client not connected
```

## 根本原因
1. Nacos服务器版本：2.2.3，运行在端口8849（HTTP）
2. Nacos客户端版本：2.2.0（通过Spring Cloud Alibaba 2021.0.5.0引入）
3. 客户端默认使用gRPC协议连接，但服务器的gRPC端口（9849）未启动
4. 从`netstat`输出可以看到8849端口监听，但9849端口不存在

## 解决方案

### 方案1：启用Nacos服务器的gRPC端口（推荐）

检查Nacos的配置文件并确保gRPC端口正确配置：

1. 找到Nacos的`application.properties`配置文件
2. 添加或修改以下配置：
```properties
# HTTP端口
server.port=8849
# gRPC端口（HTTP端口 + 1000）
nacos.server.grpc.port.offset=1000
```

3. 重启Nacos服务器
4. 验证端口：`netstat -ano | findstr "9849"`

### 方案2：配置客户端使用HTTP模式（临时方案）

在`bootstrap.yml`中已添加相关配置，但Nacos 2.x客户端强制使用gRPC。

需要在启动时添加JVM参数：
```bash
java -Dnacos.remote.client.grpc.enable=false -jar market-auth-1.0-SNAPSHOT.jar
```

### 方案3：降级Nacos客户端版本

修改父pom.xml，使用支持HTTP fallback的版本：
```xml
<spring-cloud-alibaba.version>2021.0.1.0</spring-cloud-alibaba.version>
```

## 验证步骤

1. 启动Nacos（确保gRPC端口开启）
2. 检查端口监听：
```powershell
netstat -ano | Select-String "8849"
netstat -ano | Select-String "9849"
```
3. 启动market-auth
4. 观察日志，应该看到成功注册信息

## 当前配置状态

bootstrap.yml已配置：
- ✅ fail-fast: false（允许启动失败后继续）
- ✅ 心跳和超时配置
- ✅ 命名空间：dev
- ✅ IP地址：127.0.0.1

## 下一步行动

**立即执行：检查并修复Nacos服务器的gRPC端口配置**

或者

**在IntelliJ IDEA中运行时添加VM参数：**
```
-Dnacos.remote.client.grpc.enable=false
```

位置：Run -> Edit Configurations -> market-auth -> VM options

