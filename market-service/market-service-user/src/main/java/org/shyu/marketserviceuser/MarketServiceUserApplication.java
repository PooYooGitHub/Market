package org.shyu.marketserviceuser;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "org.shyu.marketapiuser.feign")
@MapperScan("org.shyu.marketserviceuser.mapper")
public class MarketServiceUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketServiceUserApplication.class, args);
    }

}
