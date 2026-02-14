# 用户注册和登录功能说明

## 功能概述

用户服务(market-service-user)实现了完整的用户注册、登录、登出和用户信息管理功能。

## 技术栈

- **Spring Boot**: 微服务框架
- **MyBatis-Plus**: ORM框架，简化数据库操作
- **Sa-Token**: 轻量级权限认证框架，管理用户登录状态
- **Hutool**: Java工具库，提供BCrypt密码加密
- **Knife4j**: API文档生成工具
- **Nacos**: 服务注册与配置中心
- **MySQL**: 数据库
- **Redis**: 缓存（Sa-Token存储token）

## 项目结构

```
market-service-user/
├── src/main/java/org/shyu/marketserviceuser/
│   ├── config/                    # 配置类
│   │   ├── MyBatisPlusConfig.java    # MyBatis-Plus配置
│   │   └── SaTokenConfig.java        # Sa-Token拦截器配置
│   ├── controller/                # 控制器
│   │   ├── UserAuthController.java   # 认证相关接口
│   │   └── UserController.java       # 用户信息接口
│   ├── entity/                    # 实体类
│   │   └── UserEntity.java           # 用户实体（数据库映射）
│   ├── mapper/                    # Mapper接口
│   │   └── UserMapper.java           # 用户Mapper
│   ├── service/                   # 服务层
│   │   ├── UserService.java          # 用户服务接口
│   │   └── impl/
│   │       └── UserServiceImpl.java  # 用户服务实现
│   ├── handler/                   # 异常处理
│   │   └── GlobalExceptionHandler.java  # 全局异常处理器
│   └── MarketServiceUserApplication.java # 启动类
└── src/main/resources/
    └── bootstrap.yml              # 配置文件
```

## 核心功能

### 1. 用户注册 (`/api/user/auth/register`)

**接口说明**:
- 方法: POST
- 路径: `/api/user/auth/register`
- 是否需要登录: 否

**请求参数 (UserRegisterDTO)**:
```json
{
  "username": "testuser",        // 必填，4-20位字母、数字或下划线
  "password": "123456",          // 必填，6-20位
  "confirmPassword": "123456",   // 必填，需与password一致
  "nickname": "测试用户",         // 可选，默认使用username
  "phone": "13800138000",        // 可选，符合手机号格式
  "email": "test@example.com"    // 可选，符合邮箱格式
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "phone": "13800138000",
    "email": "test@example.com",
    "status": 1,
    "createTime": "2026-02-14T10:00:00"
  },
  "timestamp": 1707879600000
}
```

**业务逻辑**:
1. 验证两次密码是否一致
2. 检查用户名是否已存在
3. 检查手机号是否已被注册（如果提供）
4. 使用BCrypt加密密码
5. 保存用户信息到数据库
6. 返回用户信息（不含密码）

### 2. 用户登录 (`/api/user/auth/login`)

**接口说明**:
- 方法: POST
- 路径: `/api/user/auth/login`
- 是否需要登录: 否

**请求参数 (UserLoginDTO)**:
```json
{
  "username": "testuser",   // 必填
  "password": "123456"      // 必填
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
    "tokenType": "Bearer",
    "expiresIn": 2592000,
    "userInfo": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "phone": "13800138000",
      "email": "test@example.com",
      "status": 1,
      "createTime": "2026-02-14T10:00:00"
    }
  },
  "timestamp": 1707879600000
}
```

**业务逻辑**:
1. 根据用户名查询用户
2. 验证密码是否正确（BCrypt验证）
3. 检查用户状态是否正常
4. 生成Sa-Token令牌
5. 返回令牌和用户信息

### 3. 用户登出 (`/api/user/auth/logout`)

**接口说明**:
- 方法: POST
- 路径: `/api/user/auth/logout`
- 是否需要登录: 是

**响应示例**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1707879600000
}
```

### 4. 获取当前用户信息 (`/api/user/current`)

**接口说明**:
- 方法: GET
- 路径: `/api/user/current`
- 是否需要登录: 是

**响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "https://example.com/avatar.jpg",
    "phone": "13800138000",
    "email": "test@example.com",
    "status": 1,
    "createTime": "2026-02-14T10:00:00"
  },
  "timestamp": 1707879600000
}
```

### 5. 更新用户信息 (`/api/user/update`)

**接口说明**:
- 方法: PUT
- 路径: `/api/user/update`
- 是否需要登录: 是

**请求参数**:
```json
{
  "nickname": "新昵称",
  "avatar": "https://example.com/new-avatar.jpg",
  "email": "newemail@example.com"
}
```

## 数据库设计

### t_user 表结构

```sql
CREATE TABLE IF NOT EXISTS `t_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(加密)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态 1:正常 0:禁用',
  `deleted` tinyint(4) DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

## 配置说明

### bootstrap.yml 核心配置

```yaml
spring:
  application:
    name: market-service-user
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/market_user
    username: root
    password: root
  redis:
    host: 127.0.0.1
    port: 6379
    
server:
  port: 8101

mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

## 安全特性

1. **密码加密**: 使用BCrypt算法加密存储，不可逆
2. **令牌认证**: 使用Sa-Token管理登录状态，支持分布式
3. **参数校验**: 使用@Validated注解和JSR303规范校验输入
4. **异常处理**: 全局异常处理器统一处理错误
5. **逻辑删除**: 软删除用户数据，保留历史记录

## 部署步骤

### 1. 初始化数据库

```powershell
# 执行数据库初始化脚本
mysql -u root -p < doc/SQL/init_schema.sql
```

### 2. 启动依赖服务

```powershell
# 启动Nacos（服务注册中心）
cd nacos/bin
startup.cmd -m standalone

# 启动Redis（如果未运行）
redis-server
```

### 3. 编译项目

```powershell
# 在项目根目录执行
mvn clean package -DskipTests
```

### 4. 启动用户服务

```powershell
# 方式1: 使用启动脚本
.\start-user.ps1

# 方式2: 使用Maven
cd market-service/market-service-user
mvn spring-boot:run

# 方式3: 直接运行JAR
java -jar target/market-service-user-1.0-SNAPSHOT.jar
```

### 5. 验证服务

访问 API 文档: http://localhost:8101/doc.html

## 测试示例

### 使用 Postman 测试

#### 1. 注册用户
```
POST http://localhost:8101/api/user/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456",
  "confirmPassword": "123456",
  "nickname": "测试用户",
  "phone": "13800138000",
  "email": "test@example.com"
}
```

#### 2. 登录
```
POST http://localhost:8101/api/user/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "123456"
}
```

#### 3. 获取用户信息（需要先登录获取token）
```
GET http://localhost:8101/api/user/current
Authorization: Bearer {token}
```

### 使用 cURL 测试

```bash
# 注册
curl -X POST http://localhost:8101/api/user/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456","confirmPassword":"123456"}'

# 登录
curl -X POST http://localhost:8101/api/user/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"123456"}'

# 获取用户信息
curl -X GET http://localhost:8101/api/user/current \
  -H "Authorization: Bearer {your-token}"
```

## 常见问题

### 1. 数据库连接失败
- 检查MySQL是否启动
- 确认数据库名称、用户名、密码是否正确
- 检查防火墙设置

### 2. Redis连接失败
- 检查Redis是否启动
- 确认Redis端口是否正确
- Sa-Token需要Redis存储token

### 3. Nacos连接失败
- 检查Nacos是否启动
- 确认Nacos端口（8849）是否正确
- 检查namespace配置

### 4. 登录后访问接口仍提示未登录
- 确认请求头是否携带token
- token格式: `Authorization: Bearer {token}`
- 检查Sa-Token配置是否正确

## 后续扩展

1. **手机验证码登录**: 集成短信服务
2. **第三方登录**: 支持微信、QQ等OAuth登录
3. **找回密码**: 通过邮箱或手机重置密码
4. **实名认证**: 增加实名认证功能
5. **权限管理**: 基于角色的权限控制
6. **登录日志**: 记录登录历史和异常登录
7. **多端登录控制**: 限制同时登录设备数量

## 相关文档

- [Sa-Token官方文档](https://sa-token.cc/)
- [MyBatis-Plus官方文档](https://baomidou.com/)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Knife4j文档](https://doc.xiaominfo.com/)

## 开发者

Market Team - 2026年2月

