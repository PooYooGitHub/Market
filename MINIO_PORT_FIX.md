# MinIO端口配置错误修复

## ✅ 已完成的修改

### 1. 修复MinIO endpoint配置
**文件**: `market-service-file/src/main/resources/bootstrap.yml`

**修改内容**:
```yaml
# 从 http://localhost:9000 改为 http://localhost:9001
minio:
  endpoint: http://localhost:9001
```

**原因**: 
- 9000端口被Gateway占用
- MinIO实际运行在9001端口（Docker映射：`9001->9000`）

### 2. 添加自动初始化Bucket功能
**文件**: `market-service-file/src/main/java/org/shyu/marketservicefile/config/MinioConfig.java`

**新增功能**:
- 应用启动时自动检查并创建所需的bucket
- 自动设置bucket为公开读权限
- 完善的错误处理和日志输出

## 🔄 下一步操作

### 必须重启File Service

**方式1: 使用命令行停止**
```powershell
# 停止File Service进程 (PID: 7852)
Stop-Process -Id 7852 -Force
```

**方式2: 在IDE中重启**
- 在IDE中停止`MarketServiceFileApplication`
- 重新运行`MarketServiceFileApplication`

### 预期的启动日志

启动成功后，应该看到以下日志：
```
2026-02-15 10:30:00.123  INFO --- o.s.m.config.MinioConfig : 开始初始化MinIO，端点: http://localhost:9001
2026-02-15 10:30:00.456  INFO --- o.s.m.config.MinioConfig : 创建bucket成功: market-product
2026-02-15 10:30:00.456  INFO --- o.s.m.config.MinioConfig : 设置bucket公开读权限成功: market-product
2026-02-15 10:30:00.789  INFO --- o.s.m.config.MinioConfig : 创建bucket成功: market-avatar
2026-02-15 10:30:00.789  INFO --- o.s.m.config.MinioConfig : 设置bucket公开读权限成功: market-avatar
2026-02-15 10:30:01.012  INFO --- o.s.m.config.MinioConfig : 创建bucket成功: market-arbitration
2026-02-15 10:30:01.012  INFO --- o.s.m.config.MinioConfig : 设置bucket公开读权限成功: market-arbitration
2026-02-15 10:30:01.234  INFO --- o.s.m.config.MinioConfig : MinIO初始化完成
```

如果bucket已存在，会显示：
```
2026-02-15 10:30:00.456  INFO --- o.s.m.config.MinioConfig : Bucket已存在: market-avatar
```

## 🧪 测试步骤

### 1. 确认MinIO服务正常
```bash
docker ps | grep minio
# 应该看到容器在运行，端口映射为 9001->9000
```

### 2. 访问MinIO控制台（可选）
- 地址: http://localhost:9001
- 用户名: admin
- 密码: 3083446265@qq.com

### 3. 测试文件上传
1. 访问前端: http://localhost:5173
2. 登录用户账号
3. 进入个人中心/个人资料
4. 点击头像上传
5. 选择图片文件
6. 点击上传

### 4. 验证上传成功
**成功标志**:
- 前端显示上传成功
- 头像预览更新
- 浏览器Network标签显示200状态码
- 后端日志显示：`文件上传成功: http://localhost:9001/market-avatar/xxx.jpg`

**测试访问上传的图片**:
直接在浏览器地址栏访问返回的URL，应该能看到图片

## ⚠️ 如果仍然出错

### 错误1: 连接超时
```
ERROR: timeout
```
**解决**: 检查MinIO容器是否运行
```bash
docker start minio
```

### 错误2: 认证失败
```
ERROR: Access Denied
```
**解决**: 检查配置文件中的accessKey和secretKey

### 错误3: 无法访问上传的图片
**解决**: 
1. 登录MinIO控制台
2. 选择对应的bucket
3. 点击设置 → Access Policy
4. 设置为"Public"

### 错误4: 还是404
**解决**: 
1. 确认配置文件已保存
2. 确认已重启File Service
3. 查看启动日志，确认endpoint是9001而不是9000
4. 使用`curl`测试MinIO连接：
```bash
curl http://localhost:9001/minio/health/live
# 应该返回 200 OK
```

## 📊 端口对照表

| 服务 | 端口 | 说明 |
|-----|------|-----|
| Gateway | 9000 | API网关 |
| **MinIO** | **9001** | **对象存储（正确端口）** |
| User Service | 8101 | 用户服务 |
| File Service | 8106 | 文件服务 |
| Product Service | 8102 | 商品服务 |
| Nacos | 8849 | 服务注册中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

## 📝 相关文档

- MinIO详细配置: `doc/MinIO图片存储完整讲解.md`
- CORS问题解决: `doc/CORS跨域问题最终解决方案.md`
- 文件上传API: `front/对接文档/文件服务API.md`

---
**更新时间**: 2026-02-15 10:30  
**修复状态**: ✅ 配置已修复，等待重启服务验证

