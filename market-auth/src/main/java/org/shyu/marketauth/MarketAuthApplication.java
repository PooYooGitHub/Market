package org.shyu.marketauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"org.shyu.marketapiuser.feign"})
public class MarketAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketAuthApplication.class, args);
    }

}
