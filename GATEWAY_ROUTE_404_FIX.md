# Gateway路由404问题修复

## 问题描述

前端访问分类接口时出现404错误：
```
GET http://localhost:9000/api/product/category/list 404 (Not Found)
```

## 问题原因

### 路由匹配问题

**前端请求**：`/api/product/category/list`

**原Gateway配置**：
```yaml
- id: market-service-product
  uri: lb://market-service-product
  predicates:
    - Path=/api/product/**
  filters:
    - StripPrefix=1  # 只去掉第一段
```

**实际转发路径**：
- 输入：`/api/product/category/list`
- `StripPrefix=1` 去掉 `/api`
- 结果：`/product/category/list` ❌

**Product Service实际路径**：
```java
@RequestMapping("/category")  // Controller层
@GetMapping("/list")          // 方法层
// 实际路径: /category/list
```

**结果**：`/product/category/list` ≠ `/category/list` → **404**

---

## 解决方案

### 修改Gateway路由配置

为分类接口添加**独立的、更具体的路由规则**：

**修改文件**：`market-gateway/resources/bootstrap.yml`

```yaml
routes:
  # 分类服务路由（独立配置，更具体的规则优先匹配）
  - id: market-service-category
    uri: lb://market-service-product
    predicates:
      - Path=/api/product/category/**
    filters:
      - StripPrefix=2  # 去掉 /api/product，保留 /category/**

  # 商品服务路由（通用规则）
  - id: market-service-product
    uri: lb://market-service-product
    predicates:
      - Path=/api/product/**
    filters:
      - StripPrefix=1  # 去掉 /api，保留 /product/**
```

### 路由匹配优先级

Gateway按配置**从上到下**的顺序匹配路由：
1. 更具体的规则应该放在前面
2. 通用的规则放在后面

---

## 路由转发逻辑

### 分类接口（新配置）

```
前端请求: /api/product/category/list
           ↓
Gateway匹配: /api/product/category/** ✅
           ↓
StripPrefix=2: 去掉 /api/product
           ↓
转发到Product Service: /category/list ✅
           ↓
Controller匹配: @RequestMapping("/category") + @GetMapping("/list") ✅
```

### 商品列表接口

```
前端请求: /api/product/list
           ↓
Gateway匹配: /api/product/** ✅
           ↓
StripPrefix=1: 去掉 /api
           ↓
转发到Product Service: /product/list ✅
           ↓
Controller匹配: @RequestMapping("/product") + @GetMapping("/list") ✅
```

### 商品详情接口

```
前端请求: /api/product/detail/123
           ↓
Gateway匹配: /api/product/** ✅
           ↓
StripPrefix=1: 去掉 /api
           ↓
转发到Product Service: /product/detail/123 ✅
           ↓
Controller匹配: @RequestMapping("/product") + @GetMapping("/detail/{id}") ✅
```

---

## StripPrefix 说明

### StripPrefix=1
去掉URL的第一段（以`/`分隔）：
- `/api/product/list` → `/product/list`
- `/api/user/login` → `/user/login`

### StripPrefix=2
去掉URL的前两段：
- `/api/product/category/list` → `/category/list`
- `/api/product/category/tree` → `/category/tree`

---

## 完整的路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 1. 用户服务
        - id: market-service-user
          uri: lb://market-service-user
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1

        # 2. 分类服务（具体规则，优先匹配）
        - id: market-service-category
          uri: lb://market-service-product
          predicates:
            - Path=/api/product/category/**
          filters:
            - StripPrefix=2

        # 3. 商品服务（通用规则）
        - id: market-service-product
          uri: lb://market-service-product
          predicates:
            - Path=/api/product/**
          filters:
            - StripPrefix=1

        # 4. 文件服务
        - id: market-service-file
          uri: lb://market-service-file
          predicates:
            - Path=/api/file/**
          filters:
            - StripPrefix=1

        # 5. 交易服务
        - id: market-service-trade
          uri: lb://market-service-trade
          predicates:
            - Path=/api/trade/**
          filters:
            - StripPrefix=1
```

---

## 前后端路径对照表

| 前端请求 | Gateway匹配 | StripPrefix | 转发路径 | Controller |
|---------|------------|-------------|---------|-----------|
| `/api/user/login` | `/api/user/**` | 1 | `/user/login` | `@RequestMapping("/user")` |
| `/api/product/list` | `/api/product/**` | 1 | `/product/list` | `@RequestMapping("/product")` |
| `/api/product/category/list` | `/api/product/category/**` | 2 | `/category/list` | `@RequestMapping("/category")` |
| `/api/file/upload/avatar` | `/api/file/**` | 1 | `/file/upload/avatar` | `@RequestMapping("/file")` |

---

## 重启服务

修改后需要重启Gateway：

```powershell
# 方式1: 命令行
Stop-Process -Id 26008 -Force  # Gateway PID

# 方式2: IDE中重启
# 停止并重新运行 MarketGatewayApplication
```

---

## 测试验证

### 测试1: 分类列表 ✅
```bash
curl http://localhost:9000/api/product/category/list
```
**预期**：返回200，包含分类数据

### 测试2: 商品列表 ✅
```bash
curl http://localhost:9000/api/product/list
```
**预期**：返回200，包含商品列表

### 测试3: 商品详情 ✅
```bash
curl http://localhost:9000/api/product/detail/1
```
**预期**：返回200，包含商品详情

---

## Gateway日志验证

重启后应该看到：
```
INFO --- o.s.c.g.r.RouteDefinitionRouteLocator : Loaded route: market-service-category
INFO --- o.s.c.g.r.RouteDefinitionRouteLocator : Loaded route: market-service-product
DEBUG --- o.s.c.g.h.RoutePredicateHandlerMapping : Route matched: market-service-category
DEBUG --- o.s.c.g.h.RoutePredicateHandlerMapping : Mapping [Exchange: GET /api/product/category/list]
```

---

## 为什么要独立配置分类路由？

### 1. 路径匹配冲突
如果都用 `StripPrefix=1`：
- `/api/product/category/list` → `/product/category/list`
- Product Service 实际路径：`/category/list`
- **不匹配！**

### 2. Controller设计
Product Service有两个Controller：
- `ProductController`: `@RequestMapping("/product")`
- `CategoryController`: `@RequestMapping("/category")`

它们是平级的，所以需要不同的路由配置。

### 3. RESTful设计
- `/category/*` - 分类相关
- `/product/*` - 商品相关

这是合理的API设计，Gateway应该适配这种设计。

---

## 其他解决方案（不推荐）

### 方案2: 修改Controller路径（不推荐）
```java
// 不推荐：破坏了RESTful设计
@RequestMapping("/product/category")
public class CategoryController {
    @GetMapping("/list")  // 路径变成 /product/category/list
}
```

### 方案3: 修改前端API（不推荐）
```javascript
// 不推荐：前端路径不直观
export function getCategoryList() {
  return request({
    url: '/api/category/list',  // 改为 /api/category/list
    method: 'get'
  })
}
```
同时需要在Gateway添加独立的category路由。

---

## 注意事项

### 1. 路由顺序很重要
更具体的规则必须放在前面：
```yaml
# ✅ 正确：具体规则在前
- Path=/api/product/category/**  # 更具体
- Path=/api/product/**           # 通用

# ❌ 错误：通用规则在前会拦截所有 /api/product/** 的请求
- Path=/api/product/**           
- Path=/api/product/category/**  # 永远不会被匹配
```

### 2. StripPrefix数量要正确
- 去掉几段就写几：`/api/product` 是2段，所以 `StripPrefix=2`
- 数错会导致路径不匹配

### 3. 测试所有相关接口
修改路由配置后，测试：
- ✅ 分类列表
- ✅ 商品列表
- ✅ 商品详情
- ✅ 发布商品
- ✅ 我的商品

---

**修复时间**: 2026-02-15  
**修复状态**: ✅ 已修复，需要重启Gateway  
**需要重启**: Gateway (Port 9000)

