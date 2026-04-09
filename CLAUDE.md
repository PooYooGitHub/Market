# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Campus Flea Market Platform (校园跳蚤市场平台)** - a microservices-based e-commerce system built with Spring Cloud Alibaba for campus second-hand trading. The system supports user authentication, product trading, real-time messaging, credit scoring, dispute arbitration, and file management.

## Architecture

### Microservice Structure
The project follows a multi-module Maven structure with clear service boundaries:

- **market-common**: Shared utilities, constants, exceptions, and response models
- **market-api**: Service interface definitions (Feign clients) 
- **market-gateway**: API Gateway (Spring Cloud Gateway, port 8100)
- **market-service**: Business service implementations
  - market-service-user: User management & authentication
  - market-service-product: Product catalog & search
  - market-service-trade: Order & payment processing
  - market-service-message: Real-time messaging
  - market-service-credit: Credit scoring system
  - market-service-arbitration (port 9907): Dispute resolution (Spring Boot)
  - market-service-file: File upload & storage
- **arbitration-backend**: Standalone Node.js arbitration API (port 8000)
- **front/vue-project**: Vue 3 frontend application

### Key Technology Stack
- **Spring Boot**: 2.7.18 (Java 8 compatible)
- **Spring Cloud**: 2021.0.8 + Spring Cloud Alibaba 2021.0.5.0
- **Service Discovery**: Nacos (port 8848, includes gRPC port 9849)
- **Database**: MySQL 8.0 (root password: 123456789) 
- **Authentication**: Sa-Token with Redis integration
- **Cache**: Redis 7.x (port 6379, password: 无密码)
- **Message Queue**: RocketMQ 5.x
- **Object Storage**: MinIO (port 9000, console 9001)
- **Search**: Elasticsearch 7.17.0 (port 9200) + Kibana (port 5601)
- **API Documentation**: Knife4j (accessible at /doc.html endpoints)
- **Frontend**: Vue 3 + Vite (separate frontend project in `front/vue-project/`)

### Database Architecture
Each microservice has its own database following DDD principles:
- `market_user` - User service data
- `market_product` - Product catalog data  
- `market_trade` - Order and transaction data
- `market_message` - Chat and messaging data
- `market_credit` - Credit scoring data
- `market_arbitration` - Dispute resolution data (Spring Boot service)
- `market_file` - File metadata

**Database Connection**: MySQL 8.0 (port 3306, root password: 123456)  
**Note**: The arbitration-backend uses SQLite instead of MySQL.

## Development Commands

### Environment Setup
基础设施需要通过Docker启动，包括Nacos、MySQL、Redis、MinIO、Elasticsearch等服务。

```bash
# Initialize application databases
mysql -u root -p < doc/SQL/init_schema.sql

# Import sample data (optional)
mysql -u root -p < doc/SQL/sample_data.sql

# Import rich product data (optional)  
mysql -u root -p < doc/SQL/rich_product_data.sql

# Import arbitration system data
mysql -u root -p < doc/SQL/market_arbitration_init.sql

# Import credit system data  
mysql -u root -p < doc/SQL/market_credit_init.sql
```

### Build Commands
```bash
# Build entire project
mvn clean compile -DskipTests

# Package for deployment
mvn clean package -DskipTests

# Run tests
mvn test

# Run tests for specific module
mvn test -pl market-service/market-service-user

# Build specific service
cd market-service/market-service-arbitration
mvn clean compile -DskipTests
```

### Service Startup

**重要提示**: 建议使用 IDEA 启动后端服务，避免端口冲突和关闭问题。

#### 推荐方式 - 使用 IDEA 启动:
1. 在 IDEA 中打开项目
2. 找到各个服务的主启动类：
   - `market-gateway`: `MarketGatewayApplication.java`
   - `market-service-user`: `MarketServiceUserApplication.java`
   - `market-service-product`: `MarketServiceProductApplication.java`
   - `market-service-trade`: `MarketServiceTradeApplication.java`
   - `market-service-message`: `MarketServiceMessageApplication.java`
   - `market-service-file`: `MarketServiceFileApplication.java`
   - `market-service-arbitration`: `MarketServiceArbitrationApplication.java` (port 9907)
   - `market-service-credit`: `MarketServiceCreditApplication.java`
3. 右键点击主类 → Run 或者使用 IDEA 的 Run Configuration

#### 启动仲裁后端(Node.js):
```bash
cd arbitration-backend
npm install
npm run dev  # 开发模式，port 8000
# 或
npm start    # 生产模式
```

#### 启动顺序建议:
1. 确保基础设施已启动（Nacos、MySQL、Redis等）
2. 先启动 Gateway (port 8100)
3. 启动各个业务服务
4. 如需仲裁功能，启动 arbitration-backend (port 8000)

#### 备选方式 - 命令行启动（不推荐）:
```bash
# Spring Boot服务
cd market-service/market-service-arbitration
mvn spring-boot:run

# 或使用java -jar (需先打包)
mvn clean package -DskipTests
java -jar target/market-service-arbitration-1.0-SNAPSHOT.jar
```

### Development Utilities
```bash
# Check service status with netstat
netstat -ano | findstr :8100  # Gateway
netstat -ano | findstr :8848  # Nacos
netstat -ano | findstr :9907  # Arbitration service
netstat -ano | findstr :8000  # Arbitration backend

# Check Nacos service registration
# Visit: http://localhost:8848/nacos (nacos/nacos)

# Check running Docker containers
docker ps
```

### Frontend Development
```bash
# Navigate to frontend directory
cd front/vue-project

# Install dependencies
npm install

# Start development server (port 3000)
npm run dev

# Build for production  
npm run build

# Start simplified admin interface (仲裁管理系统)
# 在项目根目录执行：
./start-admin-simplified.sh
# 或
powershell -ExecutionPolicy Bypass -File start-admin-simplified.ps1
```

## Key Development Patterns

### Service Communication
- **Internal calls**: Use Feign clients from `market-api-*` modules
- **Authentication**: All services use Sa-Token with Redis for session management
- **API Documentation**: Each service exposes Swagger/Knife4j at `/doc.html`
- **Error Handling**: Standardized through `market-common` exception classes

### Database Patterns
- **Soft Delete**: Use `status=0` as deletion marker in most entities
- **Audit Fields**: All tables have `create_time` and `update_time`
- **Multi-tenancy**: Services are isolated by database boundaries
- **ORM**: MyBatis-Plus for database operations

### Configuration Management
- **Service Discovery**: All services register with Nacos (port 8848)
- **Configuration**: Services use bootstrap.yml for Nacos config
- **Gateway Routing**: Configured in market-gateway/src/main/resources/bootstrap.yml
- **Port Allocation**: Gateway (8100), Arbitration Backend (8000), Spring Boot services (9907, etc.)
- **Database**: Services use individual databases with MySQL, except arbitration-backend (SQLite)

### Testing Strategy
- **Unit Tests**: Mock-based testing with `@MockBean`
- **Integration Tests**: Require running services and infrastructure  
- **API Testing**: Use Swagger UI at service endpoints
- **Feign Testing**: Separate test classes for inter-service communication

## Common Issues & Solutions

### Arbitration Service Network Errors
If you see `net::ERR_EMPTY_RESPONSE` for arbitration endpoints:

1. **Check which arbitration backend is needed**:
   - Spring Boot service (market-service-arbitration, port 9907) 
   - Node.js backend (arbitration-backend, port 8000)

2. **Gateway routing**: The gateway routes `/api/arbitration/**` to `lb://market-service-arbitration`
   - If using Node.js backend, you may need to update gateway routing or call port 8000 directly

3. **Start the correct backend**:
   ```bash
   # For Spring Boot arbitration service
   cd market-service/market-service-arbitration
   mvn spring-boot:run
   
   # For Node.js arbitration backend  
   cd arbitration-backend
   npm run dev
   ```

### Service Startup Issues
- Ensure Nacos is running before starting any microservice
- Check port conflicts with `netstat -ano | findstr :PORT`
- Verify database connections and schema initialization
- Services use bootstrap.yml for configuration, not application.yml

### Development Workflow
1. 确保基础设施已启动（Docker containers for Nacos, MySQL, Redis等）
2. Initialize databases: Run SQL scripts from `doc/SQL/`
3. Build project: `mvn clean compile -DskipTests`
4. **使用 IDEA 启动服务**：在 IDEA 中找到各服务的主启动类并运行
5. Access API docs at service endpoints with `/doc.html`

### Troubleshooting
- **端口占用问题**: 使用 IDEA 启动可以更好地管理服务生命周期
- Service logs: 在 IDEA 的 Run 窗口查看日志输出
- Gateway logs: Focus on routing configuration issues
- Database issues: Verify schema exists and credentials match
- Nacos issues: Check service registration and discovery

## Important Files
- Parent POM structure: Each module has its own pom.xml (market-service/pom.xml, market-api/pom.xml, etc.)
- `doc/SQL/init_schema.sql` - Database initialization
- `doc/SQL/sample_data.sql` - Sample data for development
- `doc/SQL/rich_product_data.sql` - Rich product sample data
- `doc/SQL/market_arbitration_init.sql` - Arbitration system initialization
- `doc/SQL/market_credit_init.sql` - Credit system initialization
- `market-gateway/src/main/resources/bootstrap.yml` - Gateway configuration & routing
- `market-service/*/src/main/resources/bootstrap.yml` - Service configurations
- `arbitration-backend/server.js` - Node.js arbitration API server
- `front/vue-project/` - Vue 3 frontend application
- `start-admin-simplified.ps1/.sh` - Simplified admin interface startup

## Access Points
- **Gateway**: http://localhost:8100
- **Nacos Console**: http://localhost:8848/nacos (username: nacos, password: nacos)
- **MinIO Console**: http://localhost:9001 (check Docker setup for credentials)
- **Service API Docs**: Each service exposes Swagger/Knife4j at `/doc.html`
  - Gateway routing makes them available as: http://localhost:8100/api/{service}/doc.html
- **Frontend Application**: http://localhost:5173 (Vite dev server)
  - 跳蚤市场主页: http://localhost:5173/
  - 商品列表: http://localhost:5173/products  
  - 用户登录: http://localhost:5173/login
  - 用户注册: http://localhost:5173/register
  - 管理员登录: http://localhost:5173/admin/login
  - 管理员工作台: http://localhost:5173/admin/dashboard
- **Arbitration Backend**: http://localhost:8000 (Node.js API)