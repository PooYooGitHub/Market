# Market 项目启动指南

## 项目状态 ✅
- ✅ 项目结构完善
- ✅ POM依赖配置完成
- ✅ 配置文件创建完成
- ✅ 编译验证通过
- ⚠️ 业务代码待开发

## 快速开始

### 1. 验证编译
```powershell
# 快速构建验证
.\build.ps1

# 或使用Maven直接构建
mvn clean compile -DskipTests
```

### 2. 启动基础设施

#### 方式一：使用Docker Compose（推荐）
```powershell
# 启动MySQL、Redis、Nacos、RocketMQ
.\start-infrastructure.ps1

# 或直接使用docker-compose
docker-compose up -d
```

#### 方式二：手动启动
- **MySQL**: 端口 3306
- **Redis**: 端口 6379
- **Nacos**: 端口 8848
- **RocketMQ**: 端口 9876 (nameserver), 10911 (broker)

### 3. 初始化数据库

```powershell
# 初始化Nacos配置数据库
.\init-nacos-db.ps1

# 初始化业务数据库
# 在MySQL中执行以下SQL文件：
# - doc/SQL/init_schema.sql (创建数据库)
# - doc/SQL/mysql-schema.sql (创建表结构)
# - doc/SQL/sample_data.sql (插入测试数据)
```

### 4. 配置Nacos

1. 访问Nacos控制台: http://localhost:8849/nacos
2. 登录（默认用户名/密码: nacos/nacos）
3. 创建命名空间：
   - 命名空间ID: dev
   - 命名空间名: 开发环境

### 5. 打包项目

```powershell
# 打包所有服务
mvn clean package -DskipTests

# 打包成功后，每个服务的jar包位于对应的target目录
```

### 6. 启动服务

#### 启动顺序（重要！）

1. **先启动网关服务** (market-gateway)
```powershell
cd market-gateway\target
java -jar market-gateway-1.0-SNAPSHOT.jar
```

2. **再启动认证服务** (market-auth)
```powershell
cd market-auth\target
java -jar market-auth-1.0-SNAPSHOT.jar
```

3. **最后启动业务服务**（按需启动）

用户服务：
```powershell
cd market-service\market-service-user\target
java -jar market-service-user-1.0-SNAPSHOT.jar
```

商品服务：
```powershell
cd market-service\market-service-product\target
java -jar market-service-product-1.0-SNAPSHOT.jar
```

其他服务类似...

## 服务访问地址

| 服务 | 端口 | 访问地址 | 接口文档 |
|------|------|----------|----------|
| 网关 | 8080 | http://localhost:8080 | - |
| 认证服务 | 8081 | http://localhost:8081 | http://localhost:8081/doc.html |
| 用户服务 | 8101 | http://localhost:8101 | http://localhost:8101/doc.html |
| 商品服务 | 8102 | http://localhost:8102 | http://localhost:8102/doc.html |
| 交易服务 | 8103 | http://localhost:8103 | http://localhost:8103/doc.html |
| 消息服务 | 8104 | http://localhost:8104 | http://localhost:8104/doc.html |
| 信用服务 | 8105 | http://localhost:8105 | http://localhost:8105/doc.html |
| 仲裁服务 | 8106 | http://localhost:8106 | http://localhost:8106/doc.html |
| 文件服务 | 8107 | http://localhost:8107 | http://localhost:8107/doc.html |
| 管理服务 | 8108 | http://localhost:8108 | http://localhost:8108/doc.html |

## 管理控制台

- **Nacos**: http://localhost:8849/nacos (nacos/nacos)
- **RocketMQ**: http://localhost:9876
- **Knife4j**: 各服务的 /doc.html 路径

## 开发建议

### 1. 首先完成用户服务
用户服务是核心服务，建议先开发：
- 用户注册
- 用户登录
- 用户信息管理
- 用户认证授权

#### 创建实体类示例
```java
// market-service-user/src/main/java/org/shyu/marketserviceuser/entity/User.java
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
```

#### 创建Mapper接口
```java
// market-service-user/src/main/java/org/shyu/marketserviceuser/mapper/UserMapper.java
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

#### 创建Service
```java
// market-service-user/src/main/java/org/shyu/marketserviceuser/service/UserService.java
public interface UserService extends IService<User> {
}

// market-service-user/src/main/java/org/shyu/marketserviceuser/service/impl/UserServiceImpl.java
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
```

#### 创建Controller
```java
// market-service-user/src/main/java/org/shyu/marketserviceuser/controller/UserController.java
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    @ApiOperation("获取用户信息")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }
}
```

### 2. 定义Feign接口
在对应的API模块中定义Feign接口：

```java
// market-api-user/src/main/java/org/shyu/marketapiuser/feign/UserFeign.java
@FeignClient(name = "market-service-user", path = "/user")
public interface UserFeign {
    
    @GetMapping("/{id}")
    Result<UserDTO> getUser(@PathVariable("id") Long id);
}
```

### 3. 创建DTO
```java
// market-api-user/src/main/java/org/shyu/marketapiuser/dto/UserDTO.java
@Data
public class UserDTO implements Serializable {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
}
```

## 常见问题

### 1. 编译失败
- 检查Java版本：java -version（需要1.8+）
- 检查Maven版本：mvn -version
- 清理后重新编译：mvn clean compile

### 2. 服务启动失败
- 检查Nacos是否启动
- 检查MySQL是否启动
- 检查Redis是否启动
- 检查端口是否被占用
- 查看日志文件排查具体错误

### 3. 无法注册到Nacos
- 确认Nacos地址配置正确：127.0.0.1:8848
- 确认命名空间存在：dev
- 检查网络连接

### 4. 数据库连接失败
- 确认MySQL启动
- 确认数据库存在
- 确认用户名密码正确：root/root
- 确认数据库URL配置正确

## 项目技术栈

- **Spring Boot**: 2.7.18
- **Spring Cloud**: 2021.0.8
- **Spring Cloud Alibaba**: 2021.0.5.0
- **Nacos**: 服务注册与配置中心
- **Gateway**: API网关
- **MyBatis Plus**: ORM框架
- **Druid**: 数据库连接池
- **Redis**: 缓存
- **Sa-Token**: 认证授权
- **Knife4j**: 接口文档
- **RocketMQ**: 消息队列
- **MySQL**: 关系数据库

## 下一步计划

1. ✅ 项目结构完善 - **已完成**
2. ✅ 配置文件创建 - **已完成**
3. ✅ 公共模块完成 - **已完成**
4. ⏳ 用户服务开发 - **进行中**
5. ⏳ 商品服务开发 - **待开始**
6. ⏳ 交易服务开发 - **待开始**
7. ⏳ 其他服务开发 - **待开始**
8. ⏳ 前端开发 - **待开始**
9. ⏳ 集成测试 - **待开始**
10. ⏳ 部署上线 - **待开始**

## 参考文档

- [项目完善说明](PROJECT_IMPROVEMENT_SUMMARY.md)
- [项目开发计划](doc/项目开发计划.md)
- [数据库设计](doc/SQL/数据库设计.md)
- [README](README.md)

## 技术支持

如遇到问题：
1. 查看日志文件
2. 检查配置文件
3. 参考官方文档
4. 查看项目文档

---

**最后更新**: 2026-02-13
**项目状态**: ✅ 可编译通过，待开发业务功能

