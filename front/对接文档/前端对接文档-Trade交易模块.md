# 前端对接文档 - Trade 交易模块

> **版本**: v1.0
> **更新时间**: 2026-03-04
> **适用前端**: Vue3 / React / 小程序等
> **API 网关地址**: `http://localhost:9901`

---

## 目录

- [1. 概述](#1-概述)
- [2. 通用说明](#2-通用说明)
- [3. 购物车接口](#3-购物车接口)
  - [3.1 添加商品到购物车](#31-添加商品到购物车)
  - [3.2 获取购物车列表](#32-获取购物车列表)
  - [3.3 修改购物车商品数量](#33-修改购物车商品数量)
  - [3.4 删除购物车商品](#34-删除购物车商品)
  - [3.5 批量删除购物车商品](#35-批量删除购物车商品)
  - [3.6 更新商品选中状态](#36-更新商品选中状态)
  - [3.7 全选/取消全选](#37-全选取消全选)
  - [3.8 清空购物车](#38-清空购物车)
  - [3.9 获取选中商品数量](#39-获取选中商品数量)
- [4. 订单接口](#4-订单接口)
  - [4.1 创建订单](#41-创建订单)
  - [4.2 取消订单](#42-取消订单)
  - [4.3 支付订单](#43-支付订单)
  - [4.4 确认发货（卖家）](#44-确认发货卖家)
  - [4.5 确认收货（买家）](#45-确认收货买家)
  - [4.6 获取订单详情](#46-获取订单详情)
  - [4.7 我的买入订单列表](#47-我的买入订单列表)
  - [4.8 我的卖出订单列表](#48-我的卖出订单列表)
- [5. 数据模型](#5-数据模型)
- [6. 枚举说明](#6-枚举说明)
- [7. 前端集成示例](#7-前端集成示例)
- [8. 页面路由说明](#8-页面路由说明)

---

## 1. 概述

### 1.1 模块说明

交易模块是校园跳蚤市场的核心业务模块，涵盖**购物车**和**订单**两大功能。

**购物车功能：**
- 添加/删除商品
- 修改购买数量
- 选中/取消选中
- 批量操作与清空

**订单功能：**
- 下单（从商品详情页或购物车发起）
- 订单状态流转：待支付 → 已支付 → 已发货 → 已收货/完成
- 异常状态：已取消、售后中
- 买家视角（我的购买）与卖家视角（我的销售）分离

**商品状态与订单联动（重要）：**

| 触发动作 | 商品状态变化 | 对前端的影响 |
|---------|------------|------------|
| **创建订单**（下单） | status: 1 → **2（已售出）** | 商品从公开列表消失，防止重复购买 |
| **取消订单** | status: 2 → **1（已发布）** | 商品重新出现在公开列表，可被他人购买 |
| **确认收货**（已有 status=2） | 无变化（已是已售出） | 商品继续不显示在列表中 |

### 1.2 服务地址

| 环境 | 地址 |
|------|------|
| 网关（前端唯一入口） | `http://localhost:9901` |
| 交易服务（内部，前端不直接访问） | `http://localhost:8104` |

### 1.3 认证机制

- **Token 类型**：Sa-Token
- **传递方式**：HTTP Header `satoken: <token值>`
- **所有交易接口均需登录**

---

## 2. 通用说明

### 2.1 请求头

```http
Content-Type: application/json
satoken: <登录后获取的token>
```

### 2.2 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200 = 成功，401 = 未登录，500 = 业务异常 |
| message | String | 提示信息 |
| data | Any | 业务数据，成功时返回，失败时为 null |

### 2.3 分页响应格式

订单列表接口返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "records": []
  }
}
```

### 2.4 前端 API 封装位置

```
src/api/trade.js
```

---

## 3. 购物车接口

### 3.1 添加商品到购物车

- **接口地址**：`POST /api/trade/cart/add`
- **是否需要登录**：是

#### 请求参数（Body JSON）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | Long | 是 | 商品ID |
| quantity | Integer | 是 | 购买数量，最小为 1 |

#### 请求示例

```json
{
  "productId": 10,
  "quantity": 1
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "userId": 7,
    "productId": 10,
    "productTitle": "九成新MacBook Pro 2021",
    "productImage": "http://minio/market/xxx.jpg",
    "productPrice": 6800.00,
    "productStatus": 1,
    "sellerId": 3,
    "sellerNickname": "小明",
    "quantity": 1,
    "selected": true,
    "createTime": "2026-03-04T20:00:00",
    "updateTime": "2026-03-04T20:00:00"
  }
}
```

#### 业务规则

- 不能添加自己发布的商品
- 商品状态必须为**已发布（status=1）**
- 同一商品重复添加时，数量累加

#### 前端调用

```javascript
import { addToCart } from '@/api/trade'

await addToCart({ productId: 10, quantity: 1 })
```

---

### 3.2 获取购物车列表

- **接口地址**：`GET /api/trade/cart/list`
- **是否需要登录**：是

#### 请求参数

无

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "userId": 7,
      "productId": 10,
      "productTitle": "九成新MacBook Pro 2021",
      "productImage": "http://minio/market/xxx.jpg",
      "productPrice": 6800.00,
      "productStatus": 1,
      "sellerId": 3,
      "sellerNickname": "小明",
      "quantity": 1,
      "selected": true,
      "createTime": "2026-03-04T20:00:00",
      "updateTime": "2026-03-04T20:00:00"
    }
  ]
}
```

#### 注意事项

- `productStatus !== 1` 时，前端应提示"商品已下架"或"商品已售出"
- 按添加时间倒序返回

#### 前端调用

```javascript
import { getCartList } from '@/api/trade'

const res = await getCartList()
const cartList = res.data
```

---

### 3.3 修改购物车商品数量

- **接口地址**：`PUT /api/trade/cart/quantity/{cartId}`
- **是否需要登录**：是

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| cartId | Long | 购物车记录ID |

#### 查询参数（Query）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| quantity | Integer | 是 | 新数量，必须 >= 1 |

#### 响应示例

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 前端调用

```javascript
import { updateCartQuantity } from '@/api/trade'

await updateCartQuantity(cartId, 2)
```

---

### 3.4 删除购物车商品

- **接口地址**：`DELETE /api/trade/cart/{cartId}`
- **是否需要登录**：是

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| cartId | Long | 购物车记录ID |

#### 响应示例

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 前端调用

```javascript
import { removeFromCart } from '@/api/trade'

await removeFromCart(cartId)
```

---

### 3.5 批量删除购物车商品

- **接口地址**：`DELETE /api/trade/cart/batch`
- **是否需要登录**：是

#### 请求参数（Body JSON Array）

```json
[1, 2, 3]
```

#### 响应示例

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 前端调用

```javascript
import { removeMultipleFromCart } from '@/api/trade'

const ids = selectedItems.map(i => i.id)
await removeMultipleFromCart(ids)
```

---

### 3.6 更新商品选中状态

- **接口地址**：`PUT /api/trade/cart/selected/{cartId}`
- **是否需要登录**：是

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| cartId | Long | 购物车记录ID |

#### 查询参数（Query）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| selected | Boolean | 是 | true=选中，false=取消选中 |

#### 前端调用

```javascript
import { updateCartSelected } from '@/api/trade'

await updateCartSelected(cartId, true)
```

---

### 3.7 全选/取消全选

- **接口地址**：`PUT /api/trade/cart/selectAll`
- **是否需要登录**：是

#### 查询参数（Query）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| selected | Boolean | 是 | true=全选，false=取消全选 |

#### 前端调用

```javascript
import { selectAllCart } from '@/api/trade'

await selectAllCart(true)
```

---

### 3.8 清空购物车

- **接口地址**：`DELETE /api/trade/cart/clear`
- **是否需要登录**：是

#### 响应示例

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 前端调用

```javascript
import { clearCart } from '@/api/trade'

await clearCart()
```

---

### 3.9 获取选中商品数量

- **接口地址**：`GET /api/trade/cart/selectedCount`
- **是否需要登录**：是

#### 响应示例

```json
{ "code": 200, "message": "操作成功", "data": 3 }
```

> 可用于导航栏购物车角标数量展示。

---

## 4. 订单接口

### 4.1 创建订单

- **接口地址**：`POST /api/trade/order/create`
- **是否需要登录**：是

#### 请求参数（Body JSON）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | Long | 是 | 商品ID |

#### 请求示例

```json
{
  "productId": 10
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "orderNo": "ORD20260304201916123456",
    "buyerId": 7,
    "buyerNickname": "买家A",
    "buyerAvatar": "http://minio/market/avatar.jpg",
    "sellerId": 3,
    "sellerNickname": "小明",
    "sellerAvatar": "http://minio/market/seller.jpg",
    "productId": 10,
    "productTitle": "九成新MacBook Pro 2021",
    "productImage": "http://minio/market/product.jpg",
    "productPrice": 6800.00,
    "totalAmount": 6800.00,
    "status": 0,
    "statusDesc": "待支付",
    "createTime": "2026-03-04T20:19:16",
    "payTime": null,
    "updateTime": "2026-03-04T20:19:16"
  }
}
```

#### 业务规则

- 不能购买自己发布的商品
- 商品状态必须为**已发布（status=1）**，已售出或下架商品无法购买
- 每个商品只能生成一笔订单（一对一交易模式）
- **下单成功后，商品状态立即变为已售出（status=2），从商品列表中消失，防止重复购买**

#### 失败响应示例

```json
{ "code": 500, "message": "不能购买自己的商品", "data": null }
{ "code": 500, "message": "商品已下架或不可售", "data": null }
{ "code": 500, "message": "商品锁定失败，请重试", "data": null }
```

#### 前端调用

```javascript
import { createOrder } from '@/api/trade'

const res = await createOrder({ productId: 10 })
const order = res.data
```

---

### 4.2 取消订单

- **接口地址**：`POST /api/trade/order/cancel/{orderId}`
- **是否需要登录**：是

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 订单ID |

#### 业务规则

- **仅买家或卖家**可操作
- **仅待支付（status=0）** 状态可取消
- **取消订单后，商品状态自动恢复为已发布（status=1），重新出现在商品列表中可被其他人购买**

#### 响应示例

```json
{ "code": 200, "message": "操作成功", "data": null }
```

#### 前端调用

```javascript
import { cancelOrder } from '@/api/trade'

await cancelOrder(orderId)
```

---

### 4.3 支付订单

- **接口地址**：`POST /api/trade/order/pay/{orderId}`
- **是否需要登录**：是

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 订单ID |

#### 业务规则

- **仅买家**可操作
- **仅待支付（status=0）** 状态可支付
- 支付成功后记录 `payTime`，状态变为**已支付（status=1）**

> 当前为模拟支付，直接修改状态，无需对接第三方支付。

#### 前端调用

```javascript
import { payOrder } from '@/api/trade'

await payOrder(orderId)
```

---

### 4.4 确认发货（卖家）

- **接口地址**：`POST /api/trade/order/ship/{orderId}`
- **是否需要登录**：是

#### 业务规则

- **仅卖家**可操作
- **仅已支付（status=1）** 状态可发货
- 发货后状态变为**已发货（status=2）**

#### 前端调用

```javascript
import { shipOrder } from '@/api/trade'

await shipOrder(orderId)
```

---

### 4.5 确认收货（买家）

- **接口地址**：`POST /api/trade/order/receive/{orderId}`
- **是否需要登录**：是

#### 业务规则

- **仅买家**可操作
- **仅已发货（status=2）** 状态可确认收货
- 确认收货后状态变为**已完成（status=3）**
- 商品状态此时已是已售出（status=2，下单时已锁定），无需重复操作

#### 前端调用

```javascript
import { receiveOrder } from '@/api/trade'

await receiveOrder(orderId)
```

---

### 4.6 获取订单详情

- **接口地址**：`GET /api/trade/order/{orderId}`
- **是否需要登录**：是

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 订单ID |

#### 业务规则

- 只有**买家或卖家**可查看

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "orderNo": "ORD20260304201916123456",
    "buyerId": 7,
    "buyerNickname": "买家A",
    "buyerAvatar": "http://minio/market/avatar.jpg",
    "sellerId": 3,
    "sellerNickname": "小明",
    "sellerAvatar": "http://minio/market/seller.jpg",
    "productId": 10,
    "productTitle": "九成新MacBook Pro 2021",
    "productImage": "http://minio/market/product.jpg",
    "productPrice": 6800.00,
    "totalAmount": 6800.00,
    "status": 1,
    "statusDesc": "已支付",
    "createTime": "2026-03-04T20:19:16",
    "payTime": "2026-03-04T20:25:00",
    "updateTime": "2026-03-04T20:25:00"
  }
}
```

#### 前端调用

```javascript
import { getOrderDetail } from '@/api/trade'

const res = await getOrderDetail(orderId)
const order = res.data
```

---

### 4.7 我的买入订单列表

- **接口地址**：`GET /api/trade/order/my`
- **是否需要登录**：是
- **角色**：买家视角

#### 查询参数（Query）

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 否 | 订单状态筛选，不传则查全部 |
| orderNo | String | 否 | 订单号模糊搜索 |
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页条数，默认 10 |

#### 响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 5,
    "records": [
      {
        "id": 1001,
        "orderNo": "ORD20260304201916123456",
        "buyerId": 7,
        "sellerId": 3,
        "sellerNickname": "小明",
        "sellerAvatar": "http://minio/market/seller.jpg",
        "productId": 10,
        "productTitle": "九成新MacBook Pro 2021",
        "productImage": "http://minio/market/product.jpg",
        "productPrice": 6800.00,
        "totalAmount": 6800.00,
        "status": 0,
        "statusDesc": "待支付",
        "createTime": "2026-03-04T20:19:16",
        "payTime": null,
        "updateTime": "2026-03-04T20:19:16"
      }
    ]
  }
}
```

#### 前端调用

```javascript
import { getMyOrders } from '@/api/trade'

const res = await getMyOrders({ status: 0, pageNum: 1, pageSize: 10 })
const { total, records } = res.data
```

---

### 4.8 我的卖出订单列表

- **接口地址**：`GET /api/trade/order/sales`
- **是否需要登录**：是
- **角色**：卖家视角

#### 查询参数（Query）

同 [4.7 我的买入订单列表](#47-我的买入订单列表)，返回结构相同，但 `records` 中包含买家信息（`buyerNickname`、`buyerAvatar`）。

#### 前端调用

```javascript
import { getMySalesOrders } from '@/api/trade'

const res = await getMySalesOrders({ pageNum: 1, pageSize: 10 })
const { total, records } = res.data
```

---

## 5. 数据模型

### 5.1 购物车项 CartVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 购物车记录ID |
| userId | Long | 用户ID |
| productId | Long | 商品ID |
| productTitle | String | 商品标题 |
| productImage | String | 商品封面图URL |
| productPrice | BigDecimal | 商品价格 |
| productStatus | Integer | 商品状态（见枚举） |
| sellerId | Long | 卖家ID |
| sellerNickname | String | 卖家昵称 |
| quantity | Integer | 购买数量 |
| selected | Boolean | 是否选中 |
| createTime | LocalDateTime | 添加时间 |
| updateTime | LocalDateTime | 更新时间 |

### 5.2 订单 OrderVO

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 订单ID |
| orderNo | String | 订单编号（ORD + 时间戳 + 随机数） |
| buyerId | Long | 买家ID |
| buyerNickname | String | 买家昵称 |
| buyerAvatar | String | 买家头像URL |
| sellerId | Long | 卖家ID |
| sellerNickname | String | 卖家昵称 |
| sellerAvatar | String | 卖家头像URL |
| productId | Long | 商品ID |
| productTitle | String | 商品标题（快照） |
| productImage | String | 商品图片URL（快照） |
| productPrice | BigDecimal | 商品单价（快照） |
| totalAmount | BigDecimal | 订单总金额 |
| status | Integer | 订单状态码（见枚举） |
| statusDesc | String | 订单状态描述文字 |
| createTime | LocalDateTime | 下单时间 |
| payTime | LocalDateTime | 支付时间，未支付为 null |
| updateTime | LocalDateTime | 最近更新时间 |

---

## 6. 枚举说明

### 6.1 订单状态（OrderStatus）

| code | 描述 | 买家可操作 | 卖家可操作 |
|------|------|-----------|-----------|
| 0 | 待支付 | 支付、取消 | 取消 |
| 1 | 已支付 | — | 确认发货 |
| 2 | 已发货 | 确认收货 | — |
| 3 | 已收货/完成 | — | — |
| 4 | 已取消 | — | — |
| 5 | 售后中 | — | — |

### 6.2 商品状态（ProductStatus，购物车中使用）

| code | 描述 | 购物车中处理 |
|------|------|------|
| 0 | 草稿 | 提示商品不可购买 |
| 1 | 已发布 | 正常显示 |
| 2 | 已售出 | 提示"该商品已售出" |
| 3 | 已下架 | 提示"该商品已下架" |

---

## 7. 前端集成示例

### 7.1 购物车页面完整流程

```javascript
// src/api/trade.js 已封装所有接口
import {
  getCartList,
  addToCart,
  updateCartQuantity,
  removeFromCart,
  updateCartSelected,
  selectAllCart,
  clearCart,
  createOrder
} from '@/api/trade'

// 1. 加载购物车
const res = await getCartList()
const cartList = res.data  // CartVO[]

// 2. 修改数量
await updateCartQuantity(cartId, 3)

// 3. 选中/取消选中
await updateCartSelected(cartId, true)

// 4. 全选
await selectAllCart(true)

// 5. 从购物车结算 - 逐个创建订单
for (const item of selectedItems) {
  await createOrder({ productId: item.productId })
}
```

### 7.2 从商品详情页直接购买

```javascript
import { createOrder } from '@/api/trade'
import { useRouter } from 'vue-router'

const router = useRouter()

const buyNow = async (productId) => {
  try {
    const res = await createOrder({ productId })
    const orderId = res.data.id
    router.push(`/order/${orderId}`)
  } catch (e) {
    alert('下单失败：' + e.message)
  }
}
```

### 7.3 订单状态流转操作

```javascript
import { payOrder, shipOrder, receiveOrder, cancelOrder } from '@/api/trade'

// 买家支付
await payOrder(orderId)

// 卖家发货
await shipOrder(orderId)

// 买家确认收货
await receiveOrder(orderId)

// 取消订单（买家或卖家，仅待支付状态）
await cancelOrder(orderId)
```

### 7.4 订���列表分页查询

```javascript
import { getMyOrders, getMySalesOrders } from '@/api/trade'

// 买家：查询待支付订单
const res = await getMyOrders({ status: 0, pageNum: 1, pageSize: 10 })
const { total, records } = res.data

// 卖家：查询全部销售订单
const res2 = await getMySalesOrders({ pageNum: 1, pageSize: 10 })
const { total: total2, records: salesList } = res2.data
```

### 7.5 计算购物车选中合计

```javascript
// 前端本地计算，无需额外接口
const totalPrice = computed(() =>
  cartList.value
    .filter(i => i.selected)
    .reduce((sum, i) => sum + Number(i.productPrice) * i.quantity, 0)
    .toFixed(2)
)
```

---

## 8. 页面路由说明

| 路由路径 | 页面文件 | 说明 | 是否需要登录 |
|---------|---------|------|------|
| `/cart` | `src/views/Cart.vue` | 购物车页面 | 是 |
| `/orders` | `src/views/OrderList.vue` | 订单列表（买入+卖出Tab） | 是 |
| `/order/:id` | `src/views/OrderDetail.vue` | 订单详情页 | 是 |

### 8.1 路由注册（router/index.js）

```javascript
import Cart from '@/views/Cart.vue'
import OrderList from '@/views/OrderList.vue'
import OrderDetail from '@/views/OrderDetail.vue'

const routes = [
  {
    path: '/cart',
    name: 'Cart',
    component: Cart,
    meta: { title: '购物车', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: OrderList,
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: OrderDetail,
    meta: { title: '订单详情', requiresAuth: true }
  }
]
```

### 8.2 导航栏入口

在 `Home.vue`、`ProductList.vue` 等公共导航中添加：

```html
<router-link to="/cart" class="nav-link">购物车</router-link>
<router-link to="/orders" class="nav-link">我的订单</router-link>
```

### 8.3 商品详情页添加"加入购物车"和"立即购买"按钮

```html
<button @click="addToCartHandler">加入购物车</button>
<button @click="buyNow">立即购买</button>
```

```javascript
import { addToCart, createOrder } from '@/api/trade'

const addToCartHandler = async () => {
  await addToCart({ productId: product.value.id, quantity: 1 })
  alert('已加入购物车')
}

const buyNow = async () => {
  const res = await createOrder({ productId: product.value.id })
  router.push(`/order/${res.data.id}`)
}
```

---

## 附录：错误码说明

| code | 场景 | 前端处理建议 |
|------|------|------|
| 200 | 操作成功 | 正常处理 data |
| 401 | 未登录或 Token 过期 | 跳转登录页 |
| 500 | 业务异常 | 弹出 message 提示 |

**常见业务错误 message：**

| message | 说明 |
|---------|------|
| 商品不存在 | productId 无效 |
| 商品已下架或不可售 | 商品 status != 1 |
| 不能购买自己的商品 | 买家 == 卖家 |
| 不能添加自己的商品到购物车 | 购物车同上 |
| 订单不存在 | orderId 无效 |
| 无权操作 | 非买家/卖家操作他人订单 |
| 订单状态不允许取消 | 只有待支付可取消 |
| 订单状态不允许支付 | 只有待支付可支付 |
| 订单状态不允许发货 | 只有已支付可发货 |
| 订单状态不允许确认收货 | 只有已发货可收货 |

