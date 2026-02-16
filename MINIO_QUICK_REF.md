# MinIO 数据持久化 - 快速参考

## 问题
MinIO 容器重启后数据丢失

## 原因
使用了 Linux 路径 `/usr/local/minio/data`，在 Windows 下被当作匿名卷

## 解决方案

### 一键修复
```powershell
.\restart-minio.ps1
```

### 验证测试
```powershell
.\test-minio-persistence.ps1
```

## 配置信息

| 项目 | 值 |
|------|---|
| API 端口 | 9001 |
| 控制台端口 | 9090 |
| 用户名 | minioadmin |
| 密码 | minioadmin123 |
| 数据目录 | docker\minio\data |

## 访问地址

- **API**: http://localhost:9001
- **控制台**: http://localhost:9090

## 后端配置

```yaml
minio:
  endpoint: http://localhost:9001
  access-key: minioadmin
  secret-key: minioadmin123
```

## 验证步骤

1. 访问控制台: http://localhost:9090
2. 登录: minioadmin / minioadmin123
3. 创建桶: market-avatar
4. 上传测试图片
5. 重启容器: `docker restart minio`
6. 确认图片还在

## 常用命令

```powershell
# 启动 MinIO
docker start minio

# 停止 MinIO
docker stop minio

# 重启 MinIO
docker restart minio

# 查看日志
docker logs minio

# 查看日志（实时）
docker logs -f minio

# 进入容器
docker exec -it minio sh

# 查看数据目录
ls docker\minio\data

# 备份数据
Copy-Item -Recurse docker\minio\data docker\minio\data_backup
```

## 故障排查

### MinIO 无法启动
```powershell
# 查看日志
docker logs minio

# 删除容器重新创建
docker stop minio
docker rm minio
.\restart-minio.ps1
```

### 端口冲突
- 修改端口映射为 9002:9000 和 9091:9090

### 无法访问控制台
- 检查防火墙
- 确认端口 9090 未被占用
- 重启容器

## 数据备份

### 备份
```powershell
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
Copy-Item -Recurse docker\minio\data "docker\minio\backup_$timestamp"
```

### 恢复
```powershell
# 停止容器
docker stop minio

# 恢复数据
Remove-Item -Recurse docker\minio\data
Copy-Item -Recurse docker\minio\backup_20260215_100000 docker\minio\data

# 启动容器
docker start minio
```

## 文档参考

详细文档: [MINIO_DATA_PERSISTENCE_FIX.md](./MINIO_DATA_PERSISTENCE_FIX.md)

