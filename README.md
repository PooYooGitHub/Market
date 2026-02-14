# 校园跳蚤市场平台 (Campus Flea Market Platform)

> 基于 Spring Cloud Alibaba 微服务架构的校园二手交易平台

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2022.0.0-blue.svg)](https://spring.io/projects/spring-cloud)
[![Spring Cloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2022.0.0.0-orange.svg)](https://github.com/alibaba/spring-cloud-alibaba)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 🚀 快速启动

### 前置要求
- JDK 8+
- Maven 3.6+
- Docker Desktop（用于运行基础设施）

### 一键启动
```powershell
# 1. 启动Nacos（包含gRPC端口映射）
.\restart-nacos.ps1

# 2. 启动认证服务
.\start-auth.ps1
```

📖 **详细说明**: [START_GUIDE.md](./START_GUIDE.md) | [FIX_SUMMARY.md](./FIX_SUMMARY.md)

## 📋 项目简介

校园跳蚤市场平台是一个专为校园师生打造的二手物品交易平台，采用先进的微服务架构，提供商品发布、在线交易、即时通讯、信用评价、纠纷仲裁等完整功能，致力于打造安全、便捷、可信的校园交易生态。

### ✨ 核心特性

- 🔐 **用户认证**：基于 Sa-Token 的统一认证授权体系
- 🛒 **商品交易**：完整的商品发布、浏览、购买流程
- 💬 **即时通讯**：WebSocket 实现的实时聊天功能
- ⭐ **信用体系**：完善的用户评价和信用分机制
- 🔍 **智能搜索**：基于 Elasticsearch 的全文搜索
- ⚖️ **纠纷仲裁**：公平的交易纠纷处理机制
- 📊 **数据看板**：管理后台数据统计与可视化

## 🏗️ 项目架构

### 模块结构

```
Market/
├── market-common/                  # 公共模块
│   └── 通用工具类、常量、异常、响应封装
├── market-api/                     # 服务接口模块
│   ├── market-api-user            # 用户服务接口
│   ├── market-api-product         # 商品服务接口
│   ├── market-api-trade           # 交易服务接口
│   ├── market-api-message         # 消息服务接口
│   ├── market-api-credit          # 信用服务接口
│   ├── market-api-arbitration     # 仲裁服务接口
│   └── market-api-file            # 文件服务接口
├── market-gateway/                 # API 网关
├── market-auth/                    # 认证授权服务
└── market-service/                 # 业务服务
    ├── market-service-user        # 用户服务
    ├── market-service-product     # 商品服务
    ├── market-service-trade       # 交易服务
    ├── market-service-message     # 消息服务
    ├── market-service-credit      # 信用服务
    ├── market-service-arbitration # 仲裁服务
    ├── market-service-file        # 文件服务
    └── market-service-admin       # 管理服务
```

### 技术架构图

```
┌─────────────────────────────────────────────────────────┐
│                      Frontend (Vue3)                     │
└───────────────────┬─────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────┐
│              Gateway (Spring Cloud Gateway)             │
│          ┌──────────────────────────────────┐          │
│          │  Authentication & Authorization  │          │
│          └──────────────────────────────────┘          │
└─────────────────┬───────────────────────────────────────┘
                  │
    ┌─────────────┼─────────────┐
    │             │             │
┌───▼───┐    ┌───▼───┐    ┌───▼───┐
│ User  │    │Product│    │ Trade │   ... (Other Services)
│Service│    │Service│    │Service│
└───┬───┘    └───┬───┘    └───┬───┘
    │            │            │
    └────────────┼────────────┘
                 │
    ┌────────────┼────────────┐
    │            │            │
┌───▼───┐    ┌──▼──┐    ┌───▼───┐
│ MySQL │    │Redis│    │  MQ   │
└───────┘    └─────┘    └───────┘
```

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|-----|------|-----|
| JDK | 8 | Java 开发环境 |
| Spring Boot | 3.0.2 | 基础框架 |
| Spring Cloud | 2022.0.0 | 微服务框架 |
| Spring Cloud Alibaba | 2022.0.0.0 | 阿里微服务组件 |
| Nacos | 2.2.x | 服务注册与配置中心 |
| Gateway | 4.0.x | API 网关 |
| Sentinel | 1.8.x | 流量控制与熔断 |
| Seata | 1.7.x | 分布式事务 |
| RocketMQ | 5.x | 消息队列 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7.x | 缓存 |
| Elasticsearch | 8.x | 搜索引擎 |
| MinIO | Latest | 对象存储 |
| MyBatis-Plus | 3.5.3.1 | ORM 框架 |
| Sa-Token | 1.37.0 | 权限认证 |
| Knife4j | 4.1.0 | 接口文档 |
| Lombok | 1.18.26 | 代码简化 |
| Hutool | 5.8.16 | 工具类库 |

### 前端技术

| 技术 | 版本 | 说明 |
|-----|------|-----|
| Vue | 3.x | 前端框架 |
| Vite | 5.x | 构建工具 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| Element Plus | 2.x | UI 组件库 |
| Axios | 1.x | HTTP 客户端 |
| Socket.io | 4.x | WebSocket |

## 🚀 快速开始

### 环境要求

- JDK 8+
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose
- MySQL 8.0+
- Redis 7.x

### 基础设施部署

使用 Docker Compose 一键部署基础设施：

```bash
# 启动基础设施（Nacos、MySQL、Redis、MinIO 等）
docker-compose up -d
```

### 后端服务启动

```bash
# 1. 克隆项目
git clone https://github.com/your-repo/market.git
cd market

# 2. 编译打包
mvn clean package -DskipTests

# 3. 启动网关
cd market-gateway
mvn spring-boot:run

# 4. 启动各个微服务
# 用户服务
cd market-service/market-service-user
mvn spring-boot:run

# 商品服务
cd market-service/market-service-product
mvn spring-boot:run

# ... 启动其他服务
```

### 前端项目启动

```bash
# 进入前端目录
cd market-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 📝 开发指南

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 统一使用 Lombok 减少样板代码
- RESTful API 设计规范
- 统一异常处理和响应格式

### Git 提交规范

```
[类型] 简短描述

类型：
- feat: 新功能
- fix: Bug 修复
- docs: 文档更新
- refactor: 重构
- test: 测试相关
- chore: 构建/工具链相关
```

### 分支策略

- `main`: 生产环境分支
- `develop`: 开发环境分支
- `feature-xxx`: 功能开发分支
- `hotfix-xxx`: 紧急修复分支

## 📚 文档

- [项目开发计划](doc/项目开发计划.md)
- [数据库设计](doc/SQL/数据库设计.md)
- [API 文档](http://localhost:8080/doc.html) - Knife4j
- [毕业设计](doc/毕业设计.md)

## 🗄️ 数据库

### 数据库列表

- `market_user` - 用户服务数据库
- `market_product` - 商品服务数据库
- `market_trade` - 交易服务数据库
- `market_message` - 消息服务数据库
- `market_credit` - 信用服务数据库
- `market_arbitration` - 仲裁服务数据库
- `market_file` - 文件服务数据库

### 初始化脚本

```bash
# 执行数据库初始化脚本
mysql -u root -p < doc/SQL/init_schema.sql

# 导入样例数据
mysql -u root -p < doc/SQL/sample_data.sql
```

## 🔌 服务端口

| 服务 | 端口 | 说明 |
|-----|------|-----|
| Gateway | 8080 | API 网关 |
| Auth | 8081 | 认证服务 |
| User Service | 8082 | 用户服务 |
| Product Service | 8083 | 商品服务 |
| Trade Service | 8084 | 交易服务 |
| Message Service | 8085 | 消息服务 |
| Credit Service | 8086 | 信用服务 |
| Arbitration Service | 8087 | 仲裁服务 |
| File Service | 8088 | 文件服务 |
| Admin Service | 8089 | 管理服务 |
| Nacos | 8848 | 注册中心 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Elasticsearch | 9200 | 搜索引擎 |
| MinIO | 9000 | 对象存储 |

## 🧪 测试

```bash
# 运行单元测试
mvn test

# 运行集成测试
mvn verify

# 生成测试报告
mvn surefire-report:report
```

## 📦 构建部署

### Docker 镜像构建

```bash
# 构建所有服务镜像
./build-docker.sh

# 或单独构建某个服务
cd market-service-user
docker build -t market-user:latest .
```

### 部署到服务器

```bash
# 使用 Docker Compose 部署
docker-compose -f docker-compose.prod.yml up -d
```

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m '[feat] Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件

## 👥 作者

- **项目作者** - [Your Name](https://github.com/your-username)

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

⭐ 如果这个项目对你有帮助，请给个 Star 支持一下！

