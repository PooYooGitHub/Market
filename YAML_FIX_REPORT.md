# 启动问题修复报告

## 问题描述

应用启动时出现YAML配置错误：
```
org.yaml.snakeyaml.constructor.DuplicateKeyException: found duplicate key spring
```

## 问题原因

在 `bootstrap.yml` 配置文件中存在**重复的 `spring:` 顶级键**：
- 第1行有一个 `spring:` 配置块
- 第47行（service-core）/第56行（auth）又出现了另一个 `spring:` 配置块

YAML不允许在同一层级出现重复的键。

## 修复方案

### 1. market-service-core/src/main/resources/bootstrap.yml

**修复前：**
```yaml
spring:
  application:
    name: market-service-core
  cloud:
    ...
  datasource:
    ...
  redis:
    ...

server:
  port: 9001

# Spring 配置
spring:                              # ❌ 重复的键！
  main:
    allow-bean-definition-overriding: true
```

**修复后：**
```yaml
spring:
  application:
    name: market-service-core
  main:                              # ✓ 合并到第一个spring配置块
    allow-bean-definition-overriding: true
  cloud:
    ...
  datasource:
    ...
  redis:
    ...

server:
  port: 9001
```

### 2. market-auth/src/main/resources/bootstrap.yml

采用相同的方式修复，将 `spring.main.allow-bean-definition-overriding` 合并到第一个 `spring:` 配置块中。

## 已修复的文件

✅ `D:\program\Market\market-service\market-service-core\src\main\resources\bootstrap.yml`
✅ `D:\program\Market\market-auth\src\main\resources\bootstrap.yml`

## 验证方法

修复后，启动应用应该不再出现 `DuplicateKeyException` 错误。

### 启动顺序

1. **启动基础服务**
   ```powershell
   # 启动MySQL（如果未运行）
   # 启动Redis（如果未运行）
   # 启动Nacos
   docker start nacos
   ```

2. **启动应用服务**
   ```powershell
   # 方式1：使用提供的启动脚本
   .\start-all.ps1
   
   # 方式2：手动启动
   # 终端1：启动认证服务
   cd market-auth
   mvn spring-boot:run
   
   # 终端2：启动核心业务服务
   cd market-service/market-service-core
   mvn spring-boot:run
   ```

3. **验证服务启动**
   - Nacos控制台：http://localhost:8849/nacos
   - 检查服务列表中是否出现：
     - `market-auth`
     - `market-service-core`

## 其他注意事项

### YAML配置最佳实践

1. **同一层级不能有重复的键**
   ```yaml
   # ❌ 错误
   spring:
     application:
       name: demo
   spring:
     cloud:
       nacos: ...
   
   # ✓ 正确
   spring:
     application:
       name: demo
     cloud:
       nacos: ...
   ```

2. **使用YAML验证工具**
   - 在线工具：http://www.yamllint.com/
   - IDE插件：大多数IDE都内置YAML语法检查

3. **注意缩进**
   - YAML使用空格缩进（不要使用Tab）
   - 通常使用2个空格作为一级缩进

### 当前配置概览

#### market-service-core (端口: 9001)
- Nacos端口: 8849
- 命名空间: dev
- 数据库: market
- Redis: localhost:6379
- 整合了用户、商品、交易三大核心功能

#### market-auth (端口: 8080)
- Nacos端口: 8849
- 命名空间: public (默认)
- Redis: localhost:6379
- Sa-Token认证

## 下一步

修复完成后，可以：

1. ✅ 成功启动所有服务
2. 访问 Nacos 控制台查看服务注册情况
3. 访问 API 文档：http://localhost:9001/doc.html
4. 开始功能开发和测试

---
**修复完成时间：** 2026年2月14日 10:20  
**问题类型：** 配置文件语法错误  
**影响范围：** 应用启动失败  
**修复状态：** ✅ 已完成

