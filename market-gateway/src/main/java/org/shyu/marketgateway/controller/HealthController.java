package org.shyu.marketgateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关健康检查控制器
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Mono<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("service", "market-gateway");
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("message", "网关服务运行正常");
        return Mono.just(data);
    }
}

