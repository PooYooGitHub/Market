# 信用系统改造说明（2026-04-19）

## 1. 文档目的

本文档用于记录本次“芝麻分风格信用体系”改造内容，便于后续开发、排障、联调和运营规则迭代。

本次改造目标：
- 有效交易会提升信用分。
- 被评价人按评分增减信用分。
- 高分段增长放缓，避免分数过快冲高。
- 高信用用户展示信用背书（徽章/文案），用于交易场景可视化。

---

## 2. 改造范围

### 2.1 后端模块
- `market-api/market-api-credit`
- `market-service/market-service-credit`
- `market-service/market-service-trade`

### 2.2 前端模块
- `front/vue-project`

### 2.3 文档与SQL
- `market-service/market-service-credit/src/main/resources/sql/market_credit.sql`
- `market-service/market-service-credit/src/main/resources/sql/test_data.sql`
- `doc/SQL/market_credit.sql`

---

## 3. 新信用规则（核心）

## 3.1 分数区间
- 最低分：`350`
- 最高分：`950`
- 初始分：`550`

## 3.2 有效交易加分
- 触发时机：订单状态变为 `COMPLETED`（确认收货）
- 加分对象：买家 + 卖家
- 基础加分：`+4`

## 3.3 评价影响（仅被评价人）
- 5分：`+6`
- 4分：`+3`
- 3分：`0`
- 2分：`-4`
- 1分：`-8`

## 3.4 高分慢增长（仅正向分）
根据“当前分”对正向加分做衰减：
- `<=700`：1.0 倍（不衰减）
- `701~850`：0.5 倍
- `>850`：0.2 倍

说明：
- 负向分（扣分）不衰减，保持约束能力。
- 正向分衰减后最小仍至少生效 `+1`（防止正向事件完全无感）。

## 3.5 封顶封底
- 最终分数统一钳制在 `[350, 950]`。

---

## 4. 徽章与背书规则

按分数段计算徽章：
- `900~950`：`DIAMOND` / 钻石信誉
- `850~899`：`GOLD` / 金牌信誉
- `750~849`：`SILVER` / 银牌信誉
- `650~749`：`BRONZE` / 铜牌信誉
- `350~649`：`ROOKIE` / 新手认证

高可信标记：
- `score >= 850` 时 `highTrust = true`

---

## 5. 关键实现说明

## 5.1 统一规则入口
在 `market-service-credit` 中新增统一事件入口：
- `CreditScoreService.applyCreditEvent(...)`

该入口统一处理：
1. 幂等校验（`eventKey`）
2. 分值计算（原始分 -> 衰减后分）
3. 区间钳制
4. 等级/徽章/高可信刷新
5. 写入信用日志 `t_credit_log`

## 5.2 交易触发
交易服务 `receiveOrder` 完成后不再只初始化信用，而是调用：
- `creditFeignClient.onTradeCompleted(orderId, buyerId, sellerId)`

信用服务接收后调用：
- `CreditScoreService.handleTradeCompleted(...)`

## 5.3 评价触发
评价创建成功后，按评分映射出分值变化，调用统一入口 `applyCreditEvent`，并携带评价事件幂等键。

---

## 6. 接口变更

## 6.1 新增 Feign 内部接口
- `POST /feign/credit/transaction/complete`
- 参数：`orderId`, `buyerId`, `sellerId`

## 6.2 原有接口兼容
以下保持可用：
- `GET /credit/my`
- `GET /credit/{userId}`
- `POST /evaluation`
- `POST /feign/credit/update`

---

## 7. 数据结构变更

## 7.1 t_credit_score 新增字段
- `badge_code`
- `badge_name`
- `badge_color`
- `badge_desc`
- `high_trust`
- `valid_trade_count`
- `create_time`（兼容补齐）

并将默认值调整为新体系（如默认分 `550`）。

## 7.2 新增 t_credit_log
用于沉淀信用变更流水，核心字段：
- `change_type`
- `raw_score_change`
- `score_change`
- `before_score`
- `after_score`
- `event_key`（唯一索引，幂等）

---

## 8. 前端展示改造

## 8.1 CreditBadge 组件
支持优先展示后端返回：
- `badgeName`
- `badgeDesc`
- `badgeColor`

若后端无徽章字段，回退到 `level/levelColor`。

## 8.2 页面接入
已在以下页面接入新字段展示：
- 信用详情页（CreditInfo）
- 个人中心（Profile）
- 用户主页（UserProfile）

---

## 9. 编译与构建验证

本次改造后已验证：

### 9.1 后端编译通过
```bash
mvn -pl market-api/market-api-credit,market-service/market-service-credit,market-service/market-service-trade -am -DskipTests compile
```

### 9.2 前端构建通过
```bash
cd front/vue-project
npm run build
```

---

## 10. 上线步骤建议

1. 执行信用库DDL更新（优先使用服务内 `market_credit.sql`）。
2. 发布顺序建议：
   - `market-api-credit`
   - `market-service-credit`
   - `market-service-trade`
   - 前端
3. 重点观察：
   - 交易完成后买卖双方是否加分
   - 评价后被评价方是否变分
   - `t_credit_log` 是否按事件写入且无重复（幂等）
   - 前端是否展示徽章与背书文案

---

## 11. 兼容与注意事项

- 旧客户端仍可只使用 `score/level/levelColor`，不会被破坏。
- 新客户端建议优先读取 `badge*` 字段，展示信用背书。
- 如果历史数据没有新字段值，服务会在读取时自动补齐档位/徽章信息。

---

## 12. 后续可迭代建议

- 增加信用日志查询接口，支持用户查看“分数变化明细”。
- 对“交易有效性”增加更细规则（如售后/仲裁结果反向修正）。
- 将 `CreditPolicy` 参数外置到配置中心，支持运营动态调参。