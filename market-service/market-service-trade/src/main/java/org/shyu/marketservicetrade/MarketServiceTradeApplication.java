package org.shyu.marketservicetrade;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 交易服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"org.shyu.marketapiuser.feign", "org.shyu.marketapiproduct.feign", "org.shyu.marketapitrade.feign", "org.shyu.marketapicredit.feign"})
@MapperScan("org.shyu.marketservicetrade.mapper")
@EnableScheduling
public class MarketServiceTradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketServiceTradeApplication.class, args);
    }

}

