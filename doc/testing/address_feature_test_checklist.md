# 地址功能测试清单

## 1. 地址 CRUD

1. 登录普通用户，调用 `POST /api/user/address` 新增地址，断言返回 `code=200` 且返回地址ID。
2. 调用 `GET /api/user/address/list`，断言能查到新增地址。
3. 调用 `PUT /api/user/address/{id}` 修改地址字段，断言列表中字段更新成功。
4. 调用 `GET /api/user/address/{id}`，断言返回详情与更新后数据一致。
5. 调用 `DELETE /api/user/address/{id}`，断言地址被删除。

## 2. 默认地址逻辑

1. 新用户新增第一条地址，断言 `isDefault=true`。
2. 连续新增第二条地址（`isDefault=false`），断言第一条仍为默认。
3. 调用 `PUT /api/user/address/{id}/default` 切换默认地址，断言同用户仅一条默认地址。
4. 删除默认地址，断言系统自动将同用户最新一条地址设置为默认（若存在）。
5. 删除用户最后一条地址，断言地址列表为空且无默认地址。

## 3. 下单地址校验

1. 使用有效 `addressId` 下单：`POST /api/trade/order/create`，断言成功。
2. 使用他人地址ID下单，断言失败并返回“收货地址不存在或不属于当前用户”。
3. 不传 `addressId` 且存在默认地址，下单成功并使用默认地址。
4. 不传 `addressId` 且无默认地址，下单失败并返回“请先在个人中心新增并设置收货地址”。

## 4. 订单地址快照

1. 下单成功后，查询订单详情 `GET /api/trade/order/{id}`，断言包含：
   - `receiverName`
   - `receiverPhone`
   - `receiverProvince`
   - `receiverCity`
   - `receiverDistrict`
   - `receiverDetailAddress`
   - `receiverPostalCode`
2. 修改该用户对应地址后再次查询历史订单，断言订单快照字段不变。

## 5. 前端回归

1. 访问 `/addresses`，验证新增/编辑/删除/设默认可用。
2. 在购物车点击“结算”，验证可选择地址并携带 `addressId` 下单。
3. 无地址时结算弹窗显示“请先新增地址”提示并可跳转地址管理页。
4. 订单详情页可展示收货信息快照。
