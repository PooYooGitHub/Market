package org.shyu.marketgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MarketGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketGatewayApplication.class, args);
    }

}
