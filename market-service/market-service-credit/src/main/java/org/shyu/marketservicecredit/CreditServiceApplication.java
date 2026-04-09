package org.shyu.marketservicecredit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 信用服务启动类
 *
 * @author Market Team
 * @since 2026-04-01
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"org.shyu.marketapiuser.feign", "org.shyu.marketapitrade.feign"})
@MapperScan("org.shyu.marketservicecredit.mapper")
public class CreditServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditServiceApplication.class, args);
    }
}