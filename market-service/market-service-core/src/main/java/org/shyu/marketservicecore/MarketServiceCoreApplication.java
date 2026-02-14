package org.shyu.marketservicecore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 核心业务服务启动类
 * 整合了用户、商品、交易三大核心功能
 * 减少微服务数量，降低内存占用
 */
@SpringBootApplication(scanBasePackages = {
    "org.shyu.marketservicecore",
    "org.shyu.marketserviceuser",
    "org.shyu.marketserviceproduct",
    "org.shyu.marketservicetrade"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
    "org.shyu.marketapiuser.feign",
    "org.shyu.marketapiproduct.feign",
    "org.shyu.marketapitrade.feign"
})
public class MarketServiceCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketServiceCoreApplication.class, args);
    }

}
