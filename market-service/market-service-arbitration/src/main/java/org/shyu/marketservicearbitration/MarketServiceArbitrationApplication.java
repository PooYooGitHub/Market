package org.shyu.marketservicearbitration;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 仲裁服务启动类
 * @author shyu
 * @since 2026-04-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
    "org.shyu.marketapiuser.feign",
    "org.shyu.marketapitrade.feign",
    "org.shyu.marketapiproduct.feign",
    "org.shyu.marketapicredit.feign"
})
@MapperScan("org.shyu.marketservicearbitration.mapper")
@EnableTransactionManagement
public class MarketServiceArbitrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketServiceArbitrationApplication.class, args);
        System.out.println("🎯 仲裁服务启动成功！");
        System.out.println("📋 Swagger文档地址: http://localhost:9907/doc.html");
    }

}
