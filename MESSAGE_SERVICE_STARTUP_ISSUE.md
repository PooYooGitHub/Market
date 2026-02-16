# Message服务启动问题解决方案

## 问题描述
启动market-service-message时报错:`ClassNotFoundException: org.shyu.marketservicemessage.MarketServiceMessageApplication`

## 根本原因
主类`MarketServiceMessageApplication.java`没有被编译到target/classes目录。

## 解决方案

### 方案1: 在IDEA中操作(推荐)

1. **重新导入Maven项目**
   - 右键点击项目根目录 `Market`
   - 选择 `Maven` → `Reload project`
   - 等待刷新完成

2. **重新编译**
   - 在IDEA中找到 `market-service-message` 模块
   - 右键 → `Rebuild Module 'market-service-message'`
   
3. **配置运行配置**
   - 打开 `Run` → `Edit Configurations...`
   - 找到或创建 `MarketServiceMessageApplication` 配置
   - 确保以下设置:
     - Main class: `org.shyu.marketservicemessage.MarketServiceMessageApplication`
     - Working directory: `$MODULE_WORKING_DIR$`
     - Use classpath of module: `market-service-message`
   - 点击 `Apply` 和 `OK`

4. **运行服务**
   - 点击运行按钮启动服务

### 方案2: 命令行操作

打开PowerShell窗口(不要用IDE的终端),执行:

```powershell
# 进入项目目录
cd D:\program\Market

# 重新编译整个项目
mvn clean install -DskipTests

# 进入message服务目录
cd market-service\market-service-message

# 启动服务
mvn spring-boot:run
```

### 方案3: 使用启动脚本

直接运行我创建的启动脚本:

```powershell
cd D:\program\Market
.\start-message.ps1
```

## 验证编译是否成功

执行以下命令检查类文件是否存在:

```powershell
Test-Path "D:\program\Market\market-service\market-service-message\target\classes\org\shyu\marketservicemessage\MarketServiceMessageApplication.class"
```

如果返回 `True`,表示编译成功。

## 注意事项

1. 确保已经启动了依赖服务:
   - Nacos (端口8848)
   - MySQL (端口3306)
   - RocketMQ NameServer (端口9876)
   - RocketMQ Broker (端口10911)

2. message服务端口: **8105**

3. 如果仍然有问题,尝试:
   ```powershell
   # 清除IDEA缓存
   File → Invalidate Caches → Invalidate and Restart
   ```

## 当前项目依赖关系

```
market-service-message
├── market-common (已安装)
└── market-api-user (已安装)
```

所有依赖已经成功安装到本地Maven仓库。

