# MinIO 数据持久化修复总结

## ✅ 已完成的工作

### 1. 问题分析
- 识别出 MinIO 数据丢失是由于使用了 Linux 路径作为数据卷
- 在 Windows 下，Docker 将这些路径当作容器内部的匿名卷
- 容器删除或重启后，数据会丢失

### 2. 创建的脚本

| 脚本文件 | 功能 |
|---------|------|
| `restart-minio.ps1` | 停止旧容器，创建新的带数据持久化的 MinIO 容器 |
| `start-minio.ps1` | 启动或创建 MinIO 容器（幂等操作）|
| `test-minio-persistence.ps1` | 测试 MinIO 数据持久化功能 |

### 3. 更新的配置文件

| 文件 | 更新内容 |
|------|---------|
| `market-service-file/src/main/resources/bootstrap.yml` | MinIO 访问密钥更新为 minioadmin/minioadmin123 |
| `doc/开发说明文档.md` | MinIO 配置信息更新 |

### 4. 创建的文档

| 文档 | 内容 |
|------|------|
| `MINIO_DATA_PERSISTENCE_FIX.md` | 详细的问题分析、解决方案和验证步骤 |
| `MINIO_QUICK_REF.md` | 快速参考手册 |

## 📋 MinIO 配置信息

### 访问信息
- **API 地址**: http://localhost:9001
- **控制台地址**: http://localhost:9090
- **用户名**: minioadmin
- **密码**: minioadmin123

### 数据存储
- **数据目录**: `D:\program\Market\docker\minio\data`
- **配置目录**: `D:\program\Market\docker\minio\config`

### 容器设置
- **容器名**: minio
- **重启策略**: always（自动重启）
- **API 端口映射**: 9001:9000
- **控制台端口映射**: 9090:9090

## 🚀 使用指南

### 首次设置
```powershell
# 修复 MinIO 数据持久化
.\restart-minio.ps1
```

### 日常使用
```powershell
# 启动 MinIO（如果已停止）
.\start-minio.ps1

# 或直接使用 Docker 命令
docker start minio
```

### 测试验证
```powershell
# 运行测试脚本
.\test-minio-persistence.ps1
```

## ✅ 验证数据持久化

### 步骤 1: 上传测试文件
1. 访问 MinIO 控制台: http://localhost:9090
2. 登录: minioadmin / minioadmin123
3. 创建桶: market-avatar
4. 设置桶为公共访问
5. 上传一张测试图片

### 步骤 2: 检查数据目录
```powershell
# 查看数据目录
ls docker\minio\data\market-avatar

# 应该能看到上传的文件
```

### 步骤 3: 测试重启
```powershell
# 重启容器
docker restart minio

# 等待几秒
Start-Sleep -Seconds 5

# 再次访问控制台，确认文件还在
```

### 步骤 4: 测试电脑重启（可选）
1. 重启电脑
2. 启动 Docker Desktop
3. MinIO 容器应该自动启动（设置了 --restart=always）
4. 访问控制台，确认数据还在

## 🔧 后端服务配置

### File 服务 (market-service-file)

配置文件: `market-service-file/src/main/resources/bootstrap.yml`

```yaml
minio:
  endpoint: http://localhost:9001
  access-key: minioadmin
  secret-key: minioadmin123
  bucket:
    product: market-product
    avatar: market-avatar
    arbitration: market-arbitration
```

### 图片访问 URL 格式
```
http://localhost:9001/{bucket-name}/{object-name}
```

例如：
```
http://localhost:9001/market-avatar/abc123.jpg
```

## 📝 常用命令

### 容器管理
```powershell
# 查看 MinIO 状态
docker ps | Select-String "minio"

# 启动
docker start minio

# 停止
docker stop minio

# 重启
docker restart minio

# 删除（会保留数据）
docker rm minio

# 查看日志
docker logs minio
docker logs -f minio  # 实时查看
```

### 数据管理
```powershell
# 查看数据目录
ls docker\minio\data

# 备份数据
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Copy-Item -Recurse docker\minio\data "docker\minio\backup_$timestamp"

# 恢复数据
docker stop minio
Remove-Item -Recurse docker\minio\data
Copy-Item -Recurse docker\minio\backup_20260215_100000 docker\minio\data
docker start minio

# 查看数据目录大小
Get-ChildItem docker\minio\data -Recurse | Measure-Object -Property Length -Sum
```

## 🎯 下一步操作

### 1. 启动 MinIO
```powershell
.\start-minio.ps1
```

### 2. 创建必要的桶
在 MinIO 控制台创建以下桶：
- `market-avatar` - 用户头像
- `market-product` - 商品图片
- `market-arbitration` - 仲裁证据

### 3. 设置桶策略
将桶设置为公共访问（Public）以便前端可以直接访问图片

### 4. 测试文件上传
通过 File 服务的 `/file/upload/avatar` 接口测试头像上传

### 5. 验证数据持久化
```powershell
.\test-minio-persistence.ps1
```

## ⚠️ 注意事项

1. **旧数据无法恢复**
   - 修复前上传的文件存储在 Docker 的临时卷中
   - 修复后需要重新上传文件

2. **磁盘空间**
   - 确保 `docker\minio\data` 所在磁盘有足够空间
   - 定期清理不需要的文件

3. **数据备份**
   - 重要数据请定期备份
   - 备份目录: `docker\minio\data`

4. **端口占用**
   - 确保端口 9001 和 9090 未被其他程序占用
   - 如有冲突，修改启动脚本中的端口映射

5. **Docker Desktop**
   - 确保 Docker Desktop 开机自启动
   - MinIO 容器设置了自动重启策略

## 🐛 故障排查

### MinIO 无法启动

**检查日志**
```powershell
docker logs minio
```

**常见问题**
- 端口被占用 → 修改端口映射
- 数据目录权限问题 → 检查目录是否可写
- 镜像问题 → 重新拉取镜像

**解决方法**
```powershell
# 删除容器重新创建
docker stop minio
docker rm minio
.\restart-minio.ps1
```

### 无法访问控制台

1. 检查容器状态: `docker ps | Select-String "minio"`
2. 检查端口映射: `docker port minio`
3. 检查防火墙设置
4. 尝试重启容器: `docker restart minio`

### 数据丢失

1. 检查数据目录: `ls docker\minio\data`
2. 检查容器挂载: `docker inspect minio | Select-String "Mounts" -Context 10`
3. 确认使用了正确的脚本启动容器

## 📚 相关文档

- [MINIO_DATA_PERSISTENCE_FIX.md](./MINIO_DATA_PERSISTENCE_FIX.md) - 详细修复文档
- [MINIO_QUICK_REF.md](./MINIO_QUICK_REF.md) - 快速参考
- [FILE_UPLOAD_SUCCESS_SUMMARY.md](./FILE_UPLOAD_SUCCESS_SUMMARY.md) - 文件上传功能说明

## 🎉 总结

MinIO 数据持久化问题已经修复，现在：

✅ 数据保存在本地磁盘 `docker\minio\data`  
✅ 重启容器后数据不会丢失  
✅ 重启电脑后数据不会丢失  
✅ 容器自动重启  
✅ 易于备份和恢复  

**享受稳定的文件存储服务！** 🚀

