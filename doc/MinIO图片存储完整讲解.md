# MinIO文件存储配置说明

## 问题描述

文件上传时出现404错误：
```
桶操作失败: Non-XML response from server. Response code: 404, Content-Type: application/json
```

## 问题原因

MinIO的endpoint配置错误，指向了Gateway的端口而不是MinIO的实际端口：
- ❌ 错误配置：`http://localhost:9000` （Gateway端口）
- ✅ 正确配置：`http://localhost:9001` （MinIO端口）

## MinIO容器信息

### 容器配置
```bash
docker ps | grep minio
# 输出：0.0.0.0:9001->9000/tcp
# 说明：宿主机9001端口映射到容器内9000端口
```

### 访问地址
- **API访问（应用使用）**: http://localhost:9001
- **Web控制台**: http://localhost:9001
- **登录凭证**:
  - Access Key: `admin`
  - Secret Key: `3083446265@qq.com`

## 解决方案

### 1. 修改配置文件

**文件位置**: `market-service-file/src/main/resources/bootstrap.yml`

```yaml
minio:
  endpoint: http://localhost:9001  # 改为9001
  access-key: admin
  secret-key: 3083446265@qq.com
  bucket:
    product: market-product         # 商品图片桶
    avatar: market-avatar           # 用户头像桶
    arbitration: market-arbitration # 仲裁证据桶
```

### 2. 自动初始化Bucket

在`MinioConfig.java`中添加了`@PostConstruct`方法，应用启动时自动：
1. 检查bucket是否存在
2. 不存在则创建bucket
3. 设置bucket为公开读权限

```java
@PostConstruct
public void initBuckets() {
    try {
        MinioClient client = minioClient();
        log.info("开始初始化MinIO，端点: {}", endpoint);

        // 初始化所有bucket
        createBucketIfNotExists(client, bucket.getProduct());
        createBucketIfNotExists(client, bucket.getAvatar());
        createBucketIfNotExists(client, bucket.getArbitration());

        log.info("MinIO初始化完成");
    } catch (Exception e) {
        log.error("MinIO初始化失败: {}", e.getMessage(), e);
    }
}
```

## 端口规划

| 服务 | 端口 | 说明 |
|-----|------|-----|
| Gateway | 9000 | API网关，所有外部请求入口 |
| MinIO | 9001 | 对象存储服务 |
| User Service | 8101 | 用户服务 |
| File Service | 8106 | 文件服务 |
| Product Service | 8102 | 商品服务 |

## 文件上传流程

```
前端 → Gateway(9000) → File Service(8106) → MinIO(9001)
                              ↓
                        生成文件URL
                              ↓
                     http://localhost:9001/bucket-name/file-name
```

## 访问文件

上传成功后，文件URL格式：
```
http://localhost:9001/{bucket-name}/{file-name}
```

例如：
```
http://localhost:9001/market-avatar/abc123def456.jpg
```

## 验证步骤

### 1. 检查MinIO是否运行
```bash
docker ps | grep minio
```

### 2. 访问MinIO控制台
浏览器访问：http://localhost:9001
- 用户名：admin
- 密码：3083446265@qq.com

### 3. 重启File Service
停止并重新启动File Service，观察日志：
```
2026-02-15 10:30:00.123  INFO --- [main] o.s.m.config.MinioConfig : 开始初始化MinIO，端点: http://localhost:9001
2026-02-15 10:30:00.456  INFO --- [main] o.s.m.config.MinioConfig : 创建bucket成功: market-product
2026-02-15 10:30:00.789  INFO --- [main] o.s.m.config.MinioConfig : 创建bucket成功: market-avatar
2026-02-15 10:30:01.012  INFO --- [main] o.s.m.config.MinioConfig : 创建bucket成功: market-arbitration
2026-02-15 10:30:01.234  INFO --- [main] o.s.m.config.MinioConfig : MinIO初始化完成
```

### 4. 测试文件上传
- 访问前端：http://localhost:5173
- 登录用户
- 进入个人中心
- 点击上传头像
- 上传成功后，应该返回文件URL

## 常见问题

### Q1: 上传后无法访问图片
**原因**: Bucket没有设置公开读权限  
**解决**: 重启File Service，让初始化逻辑自动设置权限

### Q2: 连接超时
**原因**: MinIO服务未启动或端口配置错误  
**解决**: 
```bash
# 检查容器状态
docker ps -a | grep minio

# 启动容器
docker start minio
```

### Q3: 认证失败
**原因**: Access Key或Secret Key不正确  
**解决**: 检查配置文件中的凭证是否与MinIO实际配置一致

### Q4: Bucket已存在但无法访问
**原因**: Bucket权限未正确设置  
**解决**: 在MinIO控制台手动设置Bucket为公开读：
1. 登录MinIO控制台
2. 选择Bucket
3. 点击"Access Policy"
4. 选择"Public"或"Download"

## 生产环境建议

### 1. 使用独立域名
```yaml
minio:
  endpoint: https://minio.your-domain.com
```

### 2. 启用HTTPS
```bash
docker run -d \
  -p 9001:9000 \
  -v /path/to/certs:/root/.minio/certs \
  -e "MINIO_ROOT_USER=admin" \
  -e "MINIO_ROOT_PASSWORD=secure-password" \
  minio/minio server /data
```

### 3. 配置CDN加速
将MinIO的public bucket URL配置CDN加速，提升文件访问速度。

### 4. 设置备份策略
定期备份MinIO数据目录。

## 相关文件

- MinIO配置：`market-service-file/config/MinioConfig.java`
- 配置文件：`market-service-file/resources/bootstrap.yml`
- 文件服务：`market-service-file/service/FileService.java`
- 文件控制器：`market-service-file/controller/FileController.java`

## 更新日期

2026-02-15

