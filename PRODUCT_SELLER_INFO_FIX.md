# 商品详情页卖家信息显示问题 - 已解决

## 问题描述
前端商品详情页面显示"卖家信息加载中..."，卖家信息无法正常显示。

## 问题原因
**后端Product服务在返回商品详情时，没有调用User服务获取卖家信息，导致返回的VO中`seller`字段为null。**

具体原因：
1. `ProductServiceImpl.getProductDetail()`方法中有TODO注释，但没有实现获取卖家信息的逻辑
2. Product服务缺少`market-api-user`依赖
3. UserFeignClient的path配置错误（`/api/user`应该是`/user`）
4. Product服务启动类没有正确配置FeignClients扫描

## 解决方案

### 1. 修复UserFeignClient路径配置
**文件**: `market-api/market-api-user/src/main/java/org/shyu/marketapiuser/feign/UserFeignClient.java`

```java
// 修改前
@FeignClient(name = "market-service-user", path = "/api/user")

// 修改后（Feign调用是服务间直接调用，不经过Gateway）
@FeignClient(name = "market-service-user", path = "/user")
```

### 2. 添加market-api-user依赖
**文件**: `market-service/market-service-product/pom.xml`

```xml
<dependencies>
    <!-- 商品服务API -->
    <dependency>
        <groupId>org.shyu</groupId>
        <artifactId>market-api-product</artifactId>
    </dependency>

    <!-- 用户服务API - 用于Feign调用获取用户信息 -->
    <dependency>
        <groupId>org.shyu</groupId>
        <artifactId>market-api-user</artifactId>
    </dependency>
</dependencies>
```

### 3. 配置FeignClients扫描
**文件**: `market-service/market-service-product/src/main/java/org/shyu/marketserviceproduct/MarketServiceProductApplication.java`

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"org.shyu.marketapiuser.feign"})  // 指定扫描User API包
@MapperScan("org.shyu.marketserviceproduct.mapper")
public class MarketServiceProductApplication {
    // ...
}
```

### 4. 实现获取卖家信息逻辑
**文件**: `market-service/market-service-product/src/main/java/org/shyu/marketserviceproduct/service/impl/ProductServiceImpl.java`

```java
// 注入UserFeignClient
@Autowired
private UserFeignClient userFeignClient;

// 在getProductDetail方法中调用User服务
@Override
public ProductDetailVO getProductDetail(Long id) {
    // ...existing code...
    
    // 通过 Feign 调用 User 服务获取卖家信息
    try {
        Result<UserDTO> userResult = userFeignClient.getUserById(product.getSellerId());
        if (userResult != null && userResult.getCode() == 200 && userResult.getData() != null) {
            UserDTO userDTO = userResult.getData();
            ProductDetailVO.SellerInfo sellerInfo = new ProductDetailVO.SellerInfo();
            sellerInfo.setId(userDTO.getId());
            sellerInfo.setUsername(userDTO.getUsername());
            sellerInfo.setNickname(userDTO.getNickname());
            sellerInfo.setAvatar(userDTO.getAvatar());
            vo.setSeller(sellerInfo);
            log.debug("成功获取卖家信息: userId={}, username={}", userDTO.getId(), userDTO.getUsername());
        } else {
            log.warn("获取卖家信息失败: sellerId={}, result={}", product.getSellerId(), userResult);
        }
    } catch (Exception e) {
        log.error("调用User服务获取卖家信息失败: sellerId={}", product.getSellerId(), e);
        // 获取卖家信息失败不影响商品详情展示，继续返回
    }
    
    return vo;
}
```

## 重启服务步骤

### 1. 编译并安装依赖
```powershell
# 先安装market-api-user到本地仓库
cd D:\program\Market\market-api\market-api-user
mvn clean install

# 编译Product服务
cd D:\program\Market\market-service\market-service-product
mvn clean compile
```

### 2. 重启Product服务
```powershell
# 方式1: 在IDEA中重启
# 找到 MarketServiceProductApplication，点击重启按钮

# 方式2: 使用Maven命令
cd D:\program\Market\market-service\market-service-product
mvn spring-boot:run
```

## 验证方法

### 1. 检查日志
启动后查看Product服务日志，应该能看到：
```
成功获取卖家信息: userId=xxx, username=xxx
```

### 2. 测试接口
访问商品详情接口：
```
GET http://localhost:9901/api/product/detail/1
```

返回数据中应该包含seller字段：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "title": "iphone17",
    "seller": {
      "id": 6,
      "username": "user123",
      "nickname": "用户昵称",
      "avatar": "http://..."
    }
  }
}
```

### 3. 前端测试
刷新商品详情页面，卖家信息应该正常显示，不再显示"卖家信息加载中..."

## 架构说明

### Feign调用流程
```
Product Service (8102)
    ↓ (Feign调用)
User Service (8101)
    ↓ 
UserFeignController (/user/{id})
    ↓
返回用户信息
```

**注意事项：**
1. Feign调用是**服务间直接调用**，通过Nacos服务发现，**不经过Gateway**
2. 所以FeignClient的path应该是服务的实际Controller路径（`/user`），而不是Gateway路由路径（`/api/user`）
3. Gateway的`/api`前缀只是给前端调用用的，微服务之间通信不需要这个前缀

### 服务调用对比

| 调用方 | 路径 | 说明 |
|--------|------|------|
| 前端 → Gateway | `/api/user/info` | Gateway去掉/api后转发到User服务 |
| Gateway → User | `/user/info` | StripPrefix=1后的路径 |
| Product → User (Feign) | `/user/{id}` | 直接调用，不经过Gateway |

## 相关文件

- `market-api/market-api-user/src/main/java/org/shyu/marketapiuser/feign/UserFeignClient.java`
- `market-service/market-service-user/src/main/java/org/shyu/marketserviceuser/controller/UserFeignController.java`
- `market-service/market-service-product/src/main/java/org/shyu/marketserviceproduct/service/impl/ProductServiceImpl.java`
- `market-service/market-service-product/pom.xml`
- `market-service/market-service-product/src/main/java/org/shyu/marketserviceproduct/MarketServiceProductApplication.java`

---
**解决时间**: 2026-02-15
**状态**: ✅ 已解决

