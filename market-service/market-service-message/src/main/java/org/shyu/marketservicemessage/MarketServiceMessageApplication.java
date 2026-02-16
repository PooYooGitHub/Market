package org.shyu.marketservicemessage;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 消息服务启动类
 *
 * @author shyu
 * @since 2026-02-15
 */
@SpringBootApplication(scanBasePackages = {
        "org.shyu.marketservicemessage",
        "org.shyu.marketapiuser.feign"
})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "org.shyu.marketapiuser.feign")
@MapperScan("org.shyu.marketservicemessage.mapper")
public class MarketServiceMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketServiceMessageApplication.class, args);
    }

}
