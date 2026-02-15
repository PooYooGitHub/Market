package org.shyu.marketservicefile;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 文件服务启动类
 * File服务不需要数据库和API文档，排除相关自动配置
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DruidDataSourceAutoConfigure.class
})
@EnableDiscoveryClient
public class MarketServiceFileApplication {
    public static void main(String[] args) {
        // 禁用 Swagger
        System.setProperty("springfox.documentation.enabled", "false");
        System.setProperty("knife4j.enable", "false");
        SpringApplication.run(MarketServiceFileApplication.class, args);
    }
}

