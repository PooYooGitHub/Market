# market-api-product 模块说明文档

> **模块名称**: market-api-product  
> **版本**: v1.0  
> **用途**: Product 服务的对外接口定义  
> **更新日期**: 2026-02-14

---

## 📚 模块概述

`market-api-product` 是 Product 服务的 **接口契约模块**，定义了其他服务调用 Product 服务时需要使用的：
- Feign 客户端接口
- 数据传输对象（DTO）
- 枚举常量

其他服务（如 Trade、Message）只需依赖此模块，即可远程调用 Product 服务。

---

## 📂 模块结构

```
market-api-product/
├── pom.xml                          # Maven 配置
└── src/main/java/.../marketapiproduct/
    ├── dto/                         # 数据传输对象
    │   ├── ProductDTO.java          # 商品信息 DTO
    │   └── CategoryDTO.java         # 分类信息 DTO
    ├── enums/                       # 枚举类
    │   └── ProductStatus.java       # 商品状态枚举
    └── feign/                       # Feign 客户端接口
        └── ProductFeignClient.java  # Product 服务远程调用接口
```

---

## 🔌 Feign 客户端接口

### ProductFeignClient

定义了 Product 服务提供给其他服务的远程调用接口。

```java
@FeignClient(name = "market-service-product", path = "/feign/product")
public interface ProductFeignClient {
    // 接口方法...
}
```

### 接口列表

| 方法 | 路径 | 说明 | 返回值 |
|------|------|------|--------|
| getProductById | GET /{id} | 根据ID查询商品详情 | Result<ProductDTO> |
| getProductsByIds | POST /batch | 批量查询商品信息 | Result<List<ProductDTO>> |
| checkProductAvailable | GET /check/{id} | 检查商品是否可用 | Result<Boolean> |
| updateProductStatus | POST /{id}/status/{status} | 更新商品状态 | Result<Void> |
| getCategoryById | GET /category/{id} | 查询分类信息 | Result<CategoryDTO> |

---

## 📦 数据传输对象（DTO）

### 1. ProductDTO

商品信息传输对象，用于服务间传输商品数据。

```java
@Data
public class ProductDTO implements Serializable {
    private Long id;                    // 商品ID
    private Long sellerId;              // 卖家ID
    private String title;               // 商品标题
    private String description;         // 商品描述
    private BigDecimal price;           // 当前价格
    private BigDecimal originalPrice;   // 原价
    private Long categoryId;            // 分类ID
    private String categoryName;        // 分类名称
    private Integer status;             // 状态
    private Integer viewCount;          // 浏览量
    private List<String> imageUrls;     // 商品图片列表
    private String coverImage;          // 封面图片
    private LocalDateTime createTime;   // 创建时间
    private LocalDateTime updateTime;   // 更新时间
}
```

### 2. CategoryDTO

分类信息传输对象。

```java
@Data
public class CategoryDTO implements Serializable {
    private Long id;            // 分类ID
    private Long parentId;      // 父分类ID
    private String name;        // 分类名称
    private Integer sort;       // 排序
    private String icon;        // 图标
    private Integer level;      // 层级
}
```

---

## 🎯 枚举类

### ProductStatus

商品状态枚举，定义了商品的各种状态。

```java
public enum ProductStatus {
    DRAFT(0, "草稿"),        // 草稿（暂未使用）
    PUBLISHED(1, "已发布"),   // 已发布（正常显示）
    SOLD(2, "已售出"),       // 已售出
    REMOVED(3, "已下架");    // 已下架（删除）
}
```

#### 工具方法

| 方法 | 说明 | 示例 |
|------|------|------|
| getByCode(Integer code) | 根据状态码获取枚举 | ProductStatus.getByCode(1) |
| isAvailable(Integer code) | 判断是否可售 | ProductStatus.isAvailable(1) |
| isSold(Integer code) | 判断是否已售出 | ProductStatus.isSold(2) |

---

## 🚀 使用示例

### 场景1: Trade服务创建订单时查询商品信息

```java
@Service
public class OrderServiceImpl {
    
    @Autowired
    private ProductFeignClient productFeignClient;
    
    public void createOrder(Long productId, Long buyerId) {
        // 1. 查询商品信息
        Result<ProductDTO> result = productFeignClient.getProductById(productId);
        if (result.getCode() != 200) {
            throw new BusinessException("商品不存在");
        }
        
        ProductDTO product = result.getData();
        
        // 2. 检查商品是否可售
        Result<Boolean> checkResult = productFeignClient.checkProductAvailable(productId);
        if (!checkResult.getData()) {
            throw new BusinessException("商品已下架或已售出");
        }
        
        // 3. 创建订单
        Order order = new Order();
        order.setProductId(product.getId());
        order.setProductTitle(product.getTitle());
        order.setProductPrice(product.getPrice());
        order.setSellerId(product.getSellerId());
        order.setBuyerId(buyerId);
        // ... 保存订单
        
        // 4. 更新商品状态为已售出
        productFeignClient.updateProductStatus(productId, ProductStatus.SOLD.getCode());
    }
}
```

### 场景2: Message服务发送商品相关通知

```java
@Service
public class NotificationService {
    
    @Autowired
    private ProductFeignClient productFeignClient;
    
    public void notifyProductRemoved(Long productId, Long userId) {
        // 获取商品信息
        Result<ProductDTO> result = productFeignClient.getProductById(productId);
        if (result.getCode() == 200) {
            ProductDTO product = result.getData();
            
            // 发送通知
            String message = String.format(
                "您的商品《%s》已被下架", 
                product.getTitle()
            );
            sendNotification(userId, message);
        }
    }
}
```

### 场景3: Trade服务批量查询商品信息

```java
@Service
public class CartServiceImpl {
    
    @Autowired
    private ProductFeignClient productFeignClient;
    
    public List<CartItemVO> getCartItems(List<Long> productIds) {
        // 批量查询商品信息
        Result<List<ProductDTO>> result = productFeignClient.getProductsByIds(productIds);
        
        if (result.getCode() != 200) {
            return Collections.emptyList();
        }
        
        // 组装购物车项
        return result.getData().stream()
            .map(product -> {
                CartItemVO item = new CartItemVO();
                item.setProductId(product.getId());
                item.setProductTitle(product.getTitle());
                item.setProductPrice(product.getPrice());
                item.setCoverImage(product.getCoverImage());
                item.setStatus(product.getStatus());
                return item;
            })
            .collect(Collectors.toList());
    }
}
```

---

## 🔧 依赖配置

### 1. 在其他服务的 pom.xml 中添加依赖

```xml
<dependency>
    <groupId>org.shyu</groupId>
    <artifactId>market-api-product</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 启用 Feign 客户端

在启动类上添加注解：

```java
@SpringBootApplication
@EnableFeignClients(basePackages = "org.shyu.marketapiproduct.feign")
public class MarketServiceTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(MarketServiceTradeApplication.class, args);
    }
}
```

### 3. 直接注入使用

```java
@Service
public class YourService {
    
    @Autowired
    private ProductFeignClient productFeignClient;
    
    // 使用 Feign 客户端...
}
```

---

## 📝 开发规范

### ✅ 应该在此模块中定义的内容

1. **Feign 客户端接口** - 定义服务间调用的方法
2. **DTO 对象** - 用于数据传输的对象
3. **枚举类** - 状态码、类型等枚举定义
4. **常量类** - 公共常量定义

### ❌ 不应该在此模块中定义的内容

1. ❌ Controller 实现
2. ❌ Service 实现
3. ❌ Mapper 接口
4. ❌ Entity 实体类
5. ❌ 配置类
6. ❌ 业务逻辑代码

---

## 🔄 接口实现

所有 Feign 接口都在 `market-service-product` 模块的 `ProductFeignController` 中实现：

```
market-service-product/
└── controller/
    └── ProductFeignController.java  # 实现 ProductFeignClient 的所有方法
```

---

## 📊 版本记录

### v1.0 (2026-02-14)

**新增**:
- ✅ ProductDTO（完整的商品信息传输对象）
- ✅ CategoryDTO（分类信息传输对象）
- ✅ ProductStatus（商品状态枚举）
- ✅ ProductFeignClient（完整的 Feign 接口）
  - 单个商品查询
  - 批量商品查询
  - 商品可用性检查
  - 商品状态更新
  - 分类信息查询

**优化**:
- ✅ ProductDTO 添加了更多字段（图片、分类名称等）
- ✅ 添加了详细的方法注释
- ✅ 统一使用 Result 包装返回值

---

## 🎯 使用场景总结

| 调用方服务 | 使用场景 | 调用方法 |
|-----------|---------|---------|
| Trade | 创建订单时验证商品 | getProductById, checkProductAvailable |
| Trade | 订单完成更新商品状态 | updateProductStatus |
| Trade | 购物车展示商品信息 | getProductsByIds |
| Message | 发送商品相关通知 | getProductById |
| Credit | 计算卖家信用分 | getProductsByIds |

---

## 📚 相关文档

- [Product 服务开发完成报告](../doc/Product服务开发完成报告.md)
- [项目开发计划 v2.0](../doc/项目开发计划_v2.md)
- [架构设计说明书](../doc/架构设计说明书.md)

---

> **最后更新**: 2026-02-14  
> **维护者**: 开发组  
> **状态**: ✅ 已完成

