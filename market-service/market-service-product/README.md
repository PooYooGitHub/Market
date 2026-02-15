# 商品服务（Product Service）- 快速开始指南

> **服务名称**: market-service-product  
> **端口**: 8102  
> **数据库**: market_product  
> **完成日期**: 2026-02-14

---

## 一、快速启动

### 1. 环境检查

确保以下服务正在运行：
- ✅ MySQL (端口 3306)
- ✅ Redis (端口 6379)
- ✅ Nacos (端口 8849)

### 2. 初始化数据库

```sql
-- 执行初始化脚本
mysql -u root -p < D:\program\Market\doc\SQL\init_product_data.sql
```

这将创建：
- 6个一级分类（数码产品、图书教材、生活用品、服装鞋包、运动健身、其他）
- 14个二级分类
- 5个测试商品

### 3. 启动服务

#### 方法1: 使用脚本（推荐）
```powershell
.\start-product.ps1
```

#### 方法2: 手动启动
```bash
cd market-service\market-service-product
mvn spring-boot:run
```

#### 方法3: IDEA运行
运行 `MarketServiceProductApplication.java`

### 4. 验证服务

```bash
# 检查服务注册
curl http://localhost:8849/nacos/v1/ns/instance/list?serviceName=market-service-product

# 测试分类接口
curl http://localhost:8102/category/list

# 测试商品列表
curl http://localhost:8102/product/list
```

---

## 二、核心接口示例

### 2.1 获取分类列表（公开）

```bash
curl http://localhost:9000/api/product/category/list
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "parentId": 0,
      "name": "数码产品",
      "sort": 1,
      "icon": "phone",
      "level": 1
    }
  ]
}
```

### 2.2 商品列表（公开）

```bash
# 基本查询
curl "http://localhost:9000/api/product/list?pageNum=1&pageSize=10"

# 关键词搜索
curl "http://localhost:9000/api/product/list?keyword=iPhone"

# 分类筛选
curl "http://localhost:9000/api/product/list?categoryId=1"

# 价格筛选
curl "http://localhost:9000/api/product/list?minPrice=100&maxPrice=1000"

# 排序（按价格升序）
curl "http://localhost:9000/api/product/list?sortField=price&sortOrder=asc"
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "二手iPhone 13 Pro 128G",
        "price": 4999.00,
        "originalPrice": 6999.00,
        "categoryId": 1,
        "categoryName": "数码产品",
        "status": 1,
        "viewCount": 128,
        "coverImage": "https://example.com/images/iphone13-1.jpg",
        "createTime": "2026-02-14T10:00:00"
      }
    ],
    "total": 5,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 2.3 商品详情（公开）

```bash
curl http://localhost:9000/api/product/detail/1
```

**响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "sellerId": 1,
    "title": "二手iPhone 13 Pro 128G",
    "description": "9成新，无磕碰，原装配件齐全...",
    "price": 4999.00,
    "originalPrice": 6999.00,
    "categoryId": 1,
    "categoryName": "数码产品",
    "status": 1,
    "viewCount": 129,
    "imageUrls": [
      "https://example.com/images/iphone13-1.jpg",
      "https://example.com/images/iphone13-2.jpg"
    ],
    "seller": null,
    "createTime": "2026-02-14T10:00:00"
  }
}
```

### 2.4 发布商品（需要登录）

```bash
curl -X POST http://localhost:9000/api/product/publish \
  -H "Content-Type: application/json" \
  -H "satoken: YOUR_TOKEN_HERE" \
  -d '{
    "title": "二手MacBook Pro",
    "description": "2020款，16G内存，512G硬盘，成色很新",
    "price": 8999.00,
    "originalPrice": 14999.00,
    "categoryId": 1,
    "imageUrls": [
      "http://example.com/img1.jpg",
      "http://example.com/img2.jpg"
    ]
  }'
```

**响应**:
```json
{
  "code": 200,
  "message": "发布成功",
  "data": 6
}
```

### 2.5 更新商品（需要登录）

```bash
curl -X PUT http://localhost:9000/api/product/update \
  -H "Content-Type: application/json" \
  -H "satoken: YOUR_TOKEN_HERE" \
  -d '{
    "id": 6,
    "title": "二手MacBook Pro（已降价）",
    "description": "2020款，16G内存，512G硬盘，成色很新",
    "price": 8500.00,
    "originalPrice": 14999.00,
    "categoryId": 1,
    "imageUrls": [
      "http://example.com/img1.jpg",
      "http://example.com/img2.jpg"
    ]
  }'
```

### 2.6 删除商品（需要登录）

```bash
curl -X DELETE http://localhost:9000/api/product/delete/6 \
  -H "satoken: YOUR_TOKEN_HERE"
```

### 2.7 我的商品（需要登录）

```bash
curl "http://localhost:9000/api/product/my?pageNum=1&pageSize=10" \
  -H "satoken: YOUR_TOKEN_HERE"
```

---

## 三、商品状态说明

| 状态值 | 说明 | 前端显示 |
|--------|------|----------|
| 0 | 草稿 | 不显示（暂未使用） |
| 1 | 已发布 | 正常显示，可购买 |
| 2 | 已售出 | 显示"已售出"标签 |
| 3 | 已下架 | 不显示 |

---

## 四、搜索和筛选功能

### 4.1 支持的查询参数

| 参数 | 类型 | 说明 | 示例 |
|------|------|------|------|
| keyword | String | 关键词（标题、描述） | iPhone |
| categoryId | Long | 分类ID | 1 |
| minPrice | BigDecimal | 最低价格 | 100 |
| maxPrice | BigDecimal | 最高价格 | 1000 |
| sortField | String | 排序字段 | price, view_count, create_time |
| sortOrder | String | 排序方式 | asc, desc |
| pageNum | Integer | 页码 | 1 |
| pageSize | Integer | 每页数量 | 10 |

### 4.2 排序示例

```bash
# 按价格升序
curl "http://localhost:9000/api/product/list?sortField=price&sortOrder=asc"

# 按浏览量降序（热门）
curl "http://localhost:9000/api/product/list?sortField=view_count&sortOrder=desc"

# 按时间降序（最新）
curl "http://localhost:9000/api/product/list?sortField=create_time&sortOrder=desc"
```

---

## 五、权限控制

### 5.1 公开接口（无需登录）

- ✅ GET `/product/list` - 商品列表
- ✅ GET `/product/detail/{id}` - 商品详情
- ✅ GET `/category/list` - 分类列表

### 5.2 需要登录的接口

- 🔒 POST `/product/publish` - 发布商品
- 🔒 PUT `/product/update` - 更新商品（仅自己的）
- 🔒 DELETE `/product/delete/{id}` - 删除商品（仅自己的）
- 🔒 GET `/product/my` - 我的商品

### 5.3 内部接口（Feign）

- 🔧 GET `/feign/product/{id}` - 供其他服务调用

---

## 六、常见问题

### Q1: 服务无法启动？

**检查**:
1. 端口8102是否被占用
2. MySQL是否运行
3. Redis是否运行
4. Nacos是否运行
5. 数据库market_product是否存在

### Q2: 商品列表为空？

**解决**:
```sql
-- 执行初始化脚本
mysql -u root -p < doc/SQL/init_product_data.sql
```

### Q3: 发布商品报错401？

**原因**: 未登录或Token无效

**解决**:
1. 先登录获取Token
2. 请求时携带Header: `satoken: YOUR_TOKEN`

### Q4: 更新/删除商品报错"无权操作"？

**原因**: 只能操作自己发布的商品

**解决**: 确保商品是当前登录用户发布的

---

## 七、下一步开发

### 7.1 待实现功能

- [ ] 图片上传功能
- [ ] 商品收藏功能
- [ ] 卖家信息获取（Feign调用User服务）
- [ ] 浏览量异步更新（Redis）
- [ ] 分类树形结构展示
- [ ] 商品搜索优化（Elasticsearch）

### 7.2 性能优化

- [ ] 商品列表Redis缓存
- [ ] 分类列表Redis缓存
- [ ] 热门商品推荐
- [ ] 图片CDN加速

---

## 八、项目结构

```
market-service-product/
├── src/main/java/org/shyu/marketserviceproduct/
│   ├── config/                     # 配置类
│   │   ├── SaTokenConfig.java      # Sa-Token配置
│   │   └── MyMetaObjectHandler.java # 自动填充
│   ├── controller/                 # 控制器
│   │   ├── ProductController.java
│   │   ├── CategoryController.java
│   │   └── ProductFeignController.java
│   ├── dto/                        # 数据传输对象
│   ├── entity/                     # 实体类
│   ├── mapper/                     # 数据访问层
│   ├── service/                    # 业务逻辑层
│   ├── vo/                         # 视图对象
│   └── handler/                    # 异常处理
└── src/main/resources/
    └── bootstrap.yml               # 配置文件
```

---

## 九、相关文档

- [Product服务开发完成报告](./Product服务开发完成报告.md)
- [项目开发计划v2.0](./项目开发计划_v2.md)
- [架构设计说明书](./架构设计说明书.md)

---

> **最后更新**: 2026-02-14  
> **维护者**: 开发组

