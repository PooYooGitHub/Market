package org.shyu.marketserviceproduct;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 数据初始化测试类
 * 用于批量导入产品数据
 */
@SpringBootTest
@ActiveProfiles("test")
public class DataInitializationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void initRichProductData() {
        try {
            // 读取SQL文件并执行
            String sqlFile = "doc/SQL/rich_product_data.sql";
            executeSqlFile(sqlFile);
            System.out.println("✅ 丰富产品数据导入成功！");
        } catch (Exception e) {
            System.err.println("❌ 数据导入失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeSqlFile(String sqlFile) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(sqlFile))) {

            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 跳过注释和空行
                if (line.isEmpty() || line.startsWith("--") || line.startsWith("/*")) {
                    continue;
                }

                sqlBuilder.append(line).append(" ");

                // 如果遇到分号，执行SQL语句
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();

                    // 跳过USE语句（数据源已经配置了数据库）
                    if (!sql.toUpperCase().startsWith("USE")) {
                        try {
                            statement.execute(sql);
                            System.out.println("✓ 执行SQL: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                        } catch (Exception e) {
                            System.err.println("✗ SQL执行失败: " + sql.substring(0, Math.min(50, sql.length())) + "...");
                            System.err.println("错误: " + e.getMessage());
                        }
                    }

                    sqlBuilder = new StringBuilder();
                }
            }

        } catch (IOException e) {
            throw new Exception("读取SQL文件失败: " + e.getMessage(), e);
        }
    }
}