package org.shyu.marketgateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway 健康检查控制器
 */
@Slf4j
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 健康检查接口
     */
    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "market-gateway");
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // 检查 Redis 连接
        try {
            redisTemplate.opsForValue().get("health-check");
            result.put("redis", "UP");
        } catch (Exception e) {
            log.error("Redis 连接失败: {}", e.getMessage());
            result.put("redis", "DOWN");
            result.put("status", "DOWN");
        }

        return result;
    }
}

