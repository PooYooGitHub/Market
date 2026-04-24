# 用户收货地址功能说明（2026-04-20）

## 1. 目标与范围

本次改造为 Market 项目补全「用户多收货地址管理」能力，覆盖：

- 用户地址数据模型（1:N）
- 地址管理接口（新增/查询/修改/删除/设默认）
- 下单使用地址并保存订单地址快照
- 前端地址管理与结算地址选择
- SQL 迁移与回归验证清单

改造原则为：尽量少破坏现有功能、兼容历史数据与既有下单流程。

## 2. 数据模型设计

### 2.1 新增地址表

用户库 `market_user` 新增表 `t_user_address`，核心字段：

- `id`
- `user_id`
- `receiver_name`
- `receiver_phone`
- `province`
- `city`
- `district`
- `detail_address`
- `postal_code`
- `is_default`
- `create_time`
- `update_time`

关联关系：`User 1 : N Address`。

### 2.2 订单地址快照

交易库 `market_trade` 的 `t_order` 新增字段：

- `address_id`
- `receiver_name`
- `receiver_phone`
- `receiver_province`
- `receiver_city`
- `receiver_district`
- `receiver_detail_address`
- `receiver_postal_code`

说明：下单时写入地址快照，避免用户后续修改地址影响历史订单。

## 3. 默认地址策略

- 新用户新增第一条地址：自动设为默认地址。
- 用户手动设默认地址：同一用户其它地址 `is_default` 自动置为 `false`。
- 删除默认地址：自动将该用户最新一条地址设为默认（若还有地址）。
- 允许无默认地址状态：仅当用户没有任何地址时。

上述逻辑在服务层事务中执行，保证一致性。

## 4. 接口说明

## 4.1 用户地址接口（需登录）

路径前缀：`/api/user/address`

- `POST /` 新增地址
- `GET /list` 查询当前用户地址列表
- `GET /{addressId}` 查询地址详情
- `PUT /{addressId}` 更新地址
- `DELETE /{addressId}` 删除地址
- `PUT /{addressId}/default` 设为默认地址
- `GET /default` 查询默认地址

安全约束：只能操作当前登录用户自己的地址。

### 4.2 内部 Feign 接口（服务间调用）

路径前缀：`/feign/user/address`

- `GET /by-id?userId={userId}&addressId={addressId}`
- `GET /default?userId={userId}`

用于交易服务下单时做地址归属校验与默认地址获取。

## 5. 下单流程改造

下单请求 `CreateOrderRequest` 新增可选字段 `addressId`。

处理规则：

- 传 `addressId`：校验该地址必须属于当前登录用户。
- 不传 `addressId`：自动使用当前用户默认地址。
- 无默认地址：返回明确错误，提示先在个人中心新增并设置收货地址。

创建订单时，写入上述地址快照字段。

## 6. 前端改造说明

### 6.1 新增页面

- 路由：`/addresses`
- 页面：地址列表、新增、编辑、删除、设默认

### 6.2 入口调整

- 顶部用户下拉菜单新增「收货地址」入口
- 个人中心新增「管理收货地址」入口

### 6.3 结算与下单

- 购物车结算弹窗支持地址选择并传递 `addressId`
- 商品详情「立即购买」保持兼容，不传 `addressId` 时后端走默认地址逻辑
- 无地址场景增加引导跳转地址管理页

### 6.4 订单展示

- 订单详情展示收货信息（来自订单快照）

## 7. SQL 与迁移脚本

已提供脚本：

- `doc/SQL/migration_20260420_user_address.sql`
- `doc/SQL/migration_20260420_trade_order_address_snapshot.sql`

初始化 SQL 已同步：

- `doc/SQL/market_user.sql`
- `doc/SQL/market_trade.sql`

## 8. 验证与回归

### 8.1 编译构建

- 后端编译：
  `mvn -pl market-api/market-api-user,market-service/market-service-user,market-service/market-service-trade -am -DskipTests compile`
- 前端构建：
  `npm run build`

### 8.2 功能回归清单

见：

- `doc/testing/address_feature_test_checklist.md`

覆盖地址 CRUD、默认地址切换、下单地址归属校验、订单快照稳定性等关键场景。

