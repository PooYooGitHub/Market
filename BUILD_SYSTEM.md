# 构建系统说明

## 当前构建工具

本项目使用 **Maven** 作为唯一的构建工具。

## 版本要求

- **JDK**: 1.8
- **Maven**: 3.6.3+
- **构建工具**: Maven（不使用 Gradle）

## 构建命令

```powershell
# 清理并编译
mvn clean compile

# 打包（跳过测试）
mvn clean package -DskipTests

# 完整构建（包含测试）
mvn clean install
```

## 历史说明

项目早期曾混合使用 Gradle 和 Maven，导致构建冲突。现已统一使用 Maven。

**已删除的 Gradle 配置：**
- ❌ `market-service-trade/gradle/` - 已删除
- ❌ `market-service-trade/build.gradle` - 已删除
- ❌ `market-service-trade/settings.gradle` - 已删除
- ❌ `market-common/gradle/` - 已删除

## 常见问题

### Q: 提示 "Gradle 版本不兼容" 错误

**A**: 项目已移除所有 Gradle 配置。如果 IDEA 仍提示 Gradle 错误，请：

1. 关闭 IDEA
2. 删除 `.idea/gradle.xml`（如果存在）
3. 重新打开 IDEA
4. 确保 IDEA 识别为 Maven 项目

### Q: JDK 1.8 与 Gradle 9 不兼容

**A**: 项目不使用 Gradle，无需关心 Gradle 版本。

## IDEA 配置

1. **文件 → 项目结构**
   - 项目 SDK: JDK 1.8
   - 语言级别: 8

2. **设置 → 构建工具 → Maven**
   - Maven 主目录: 选择 Maven 安装路径
   - User settings file: settings.xml

3. **Maven 面板** （右侧）
   - 使用此面板执行 Maven 生命周期命令

---

> 📅 创建时间: 2026-02-13  
> 🔧 构建工具: Maven Only  
> ☕ JDK 版本: 1.8

