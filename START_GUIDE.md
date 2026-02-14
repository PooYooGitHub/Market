# Market项目启动指南

## 当前状态

✅ **已完成的修复：**
1. 修正了market-auth的Feign客户端包扫描路径
2. 添加了Nacos失败容错配置
3. 配置了心跳和超时设置
4. 创建了application.yml日志配置
5. 编译所有必需模块成功
6. ✅ **已修复Nacos gRPC端口映射问题**（使用 restart-nacos.ps1）

## 前置步骤：启动Nacos

**⚠️ 重要：首次启动或遇到连接问题时，请先运行：**
```powershell
.\restart-nacos.ps1
```

该脚本会正确配置Nacos的gRPC端口映射。详细说明见 [NACOS_PORT_FIX.md](./NACOS_PORT_FIX.md)

**验证Nacos正常：**
- 访问：http://localhost:8849/nacos （用户名/密码：nacos/nacos）
- 端口检查：`netstat -ano | findstr "9849"` 应显示LISTENING状态

## 启动应用

### 方案A：在IntelliJ IDEA中运行（推荐）

1. **打开Run Configuration:**
   - 点击 Run → Edit Configurations
   - 选择 MarketAuthApplication

2. **添加VM Options:**
   ```
   -Dnacos.remote.client.grpc.enable=false
   ```

3. **设置Active profiles:**
   ```
   dev
   ```

4. **点击Run启动应用**

### 方案B：使用命令行启动

在`D:\program\Market`目录下运行：
```bat
start-auth.bat
```

或手动运行：
```cmd
cd D:\program\Market\market-auth
D:\program\JDK\bin\java.exe -Dnacos.remote.client.grpc.enable=false -Dspring.profiles.active=dev -jar target\market-auth-1.0-SNAPSHOT.jar
```

### 方案C：修复Nacos服务器gRPC端口（永久解决）

1. 找到Nacos的`application.properties`
2. 添加配置：
   ```properties
   server.port=8849
   nacos.server.grpc.port.offset=1000
   ```
3. 重启Nacos
4. 验证：`netstat -ano | findstr 9849`

## 启动顺序

1. **启动基础设施**（如果使用Docker）
   ```powershell
   .\start-infrastructure.ps1
   ```

2. **验证服务可用性**
   - Nacos: http://localhost:8849/nacos
   - MySQL: localhost:3306
   - Redis: localhost:6379

3. **启动market-auth**
   - 使用IDEA运行（方案A）
   - 或使用start-auth.bat（方案B）

## 验证启动成功

### 检查日志输出
应该看到：
```
Started MarketAuthApplication in X seconds
```

### 检查端口监听
```powershell
netstat -ano | Select-String "8081"
```

### 访问Actuator端点
```
http://localhost:8081/actuator/health
```

### 检查Nacos注册
访问Nacos控制台：http://localhost:8849/nacos
- 用户名/密码：nacos/nacos
- 查看服务列表，应该能看到`market-auth`

## 配置文件说明

### bootstrap.yml
- **server-addr**: Nacos地址（127.0.0.1:8849）
- **namespace**: dev环境
- **fail-fast**: false（允许启动失败）
- **配置了完整的心跳和超时参数**

### application.yml
- **日志级别**: 设置为DEBUG便于调试
- **profiles.active**: dev

## 常见问题

### Q1: 应用启动但无法注册到Nacos
**A:** 检查Nacos的dev命名空间是否存在
- 登录Nacos控制台
- 命名空间管理 → 新建命名空间
- 命名空间ID: dev

### Q2: Redis连接失败
**A:** 确保Redis已启动且端口正确
```powershell
Test-NetConnection -ComputerName 127.0.0.1 -Port 6379
```

### Q3: 找不到market-api-user包
**A:** 先编译依赖模块
```powershell
mvn clean install -Dmaven.test.skip=true -pl market-common,market-api/market-api-user -am
```

## 下一步

启动market-auth成功后，可以按以下顺序启动其他服务：
1. market-gateway（网关服务）
2. market-service-user（用户服务）
3. 其他业务服务

## 项目文件

- **启动脚本**: `start-auth.bat`
- **问题分析**: `NACOS_CONNECTION_FIX.md`
- **轻量启动**: `start-light.ps1`（精简版启动脚本）

## 技术支持

如果遇到问题，请检查：
1. Nacos是否正常运行（http://localhost:8849/nacos）
2. JDK版本（需要1.8）
3. Maven仓库依赖是否完整
4. 端口是否被占用

---
*最后更新: 2026-02-13*

