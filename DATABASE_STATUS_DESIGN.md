# 数据库设计说明 - status字段使用

## 设计原则

本项目使用单一的 `status` 字段来表示用户的状态，包括正常、禁用和删除状态，而不使用单独的 `deleted` 字段。

## 字段定义

### t_user 表的 status 字段

```sql
`status` tinyint(4) DEFAULT 1 COMMENT '状态 1:正常 0:禁用/删除'
```

## 状态值说明

| 状态值 | 含义 | 说明 |
|-------|------|------|
| 1 | 正常 | 用户可以正常登录和使用系统 |
| 0 | 禁用/删除 | 用户被禁用或删除，无法登录 |

## 使用场景

### 1. 用户注册
```java
user.setStatus(1); // 新注册用户默认为正常状态
```

### 2. 用户登录
```java
if (user.getStatus() == 0) {
    throw new BusinessException("账号已被禁用");
}
```

### 3. 禁用用户
```java
user.setStatus(0); // 将用户状态设置为禁用
userService.updateById(user);
```

### 4. 删除用户（软删除）
```java
user.setStatus(0); // 使用status=0实现软删除
userService.updateById(user);
```

### 5. 查询可用用户
```java
LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(UserEntity::getStatus, 1); // 只查询status=1的正常用户
List<UserEntity> users = userService.list(wrapper);
```

## 优势

1. **简化设计**: 不需要单独的deleted字段
2. **统一管理**: 禁用和删除使用同一个字段
3. **查询简单**: 只需要判断status是否为1
4. **兼容性好**: 符合传统数据库设计习惯

## 注意事项

1. **唯一索引问题**: 
   - 由于使用status=0表示删除，已删除的用户username和phone仍然占用唯一索引
   - 如果需要允许已删除用户的username和phone被重新使用，需要特殊处理
   - 建议方案：删除用户时在username和phone后面加上时间戳标记

2. **查询条件**:
   - 所有查询用户的地方都需要加上 `status=1` 的条件
   - 建议在Service层统一处理

3. **扩展性**:
   - 如果未来需要更多状态（如：待审核、冻结等），可以扩展status的值
   - 例如：0-删除, 1-正常, 2-待审核, 3-冻结

## 未来扩展建议

如果需要更细粒度的状态管理，可以考虑：

```sql
-- 方案1: 扩展status值
-- 0: 删除
-- 1: 正常  
-- 2: 待审核
-- 3: 冻结

-- 方案2: 分离status和deleted（如果确实需要）
`status` tinyint(4) DEFAULT 1 COMMENT '状态 1:正常 2:待审核 3:冻结'
`deleted` tinyint(4) DEFAULT 0 COMMENT '删除标记 0:未删除 1:已删除'
```

## 当前实现

当前代码已完全按照 `status` 字段设计实现：

✅ 数据库表无 `deleted` 字段  
✅ 实体类无 `deleted` 属性  
✅ MyBatis-Plus无逻辑删除配置  
✅ 业务代码使用 `status=0` 表示禁用/删除  
✅ 登录验证检查 `status=1`

---

**更新时间**: 2026年2月14日  
**适用项目**: 校园跳蚤市场系统

