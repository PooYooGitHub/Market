package org.shyu.marketgateway.filter;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketgateway.config.WhiteListProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway 全局认证过滤器
 *
 * 职责：
 * 1. 检查白名单，白名单路径直接放行
 * 2. 从 Header 获取 Token
 * 3. 从 Redis 验证 Token（本地验证，不调用服务）
 * 4. 提取 userId 放入 Header，传递给下游服务
 * 5. Token 无效或已过期，返回 401
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final StringRedisTemplate redisTemplate;
    private final WhiteListProperties whiteListProperties;

    /**
     * Token 在 Redis 中的 key 前缀
     * Sa-Token 存储格式：satoken:login:token:{tokenValue} -> userId
     */
    private static final String TOKEN_PREFIX = "satoken:login:token:";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.debug("Gateway 收到请求: {}", path);

        // 1. 白名单检查 - 白名单路径直接放行
        if (whiteListProperties.isWhitePath(path)) {
            log.debug("白名单路径，直接放行: {}", path);
            return chain.filter(exchange);
        }

        // 2. 获取 Token - 从 Header 中获取
        String token = extractToken(exchange);
        if (!StringUtils.hasText(token)) {
            log.warn("未提供 Token，路径: {}", path);
            return unauthorized(exchange, "请先登录");
        }

        // 3. 验证 Token - 从 Redis 读取验证（本地操作，不调用服务）
        try {
            String userId = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);

            if (!StringUtils.hasText(userId)) {
                log.warn("Token 无效或已过期: {}", token.substring(0, Math.min(10, token.length())) + "...");
                return unauthorized(exchange, "登录已过期，请重新登录");
            }

            log.debug("Token 验证通过，userId: {}", userId);

            // 4. 将 userId 和 token 放入 Header - 下游服务可直接使用
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("satoken", token)  // 传递原始token给下游服务，Sa-Token需要
                    .build();

            // 5. 继续执行过滤器链
            return chain.filter(exchange.mutate().request(request).build());

        } catch (Exception e) {
            log.error("Token 验证异常: {}", e.getMessage(), e);
            return unauthorized(exchange, "认证失败，请重新登录");
        }
    }

    /**
     * 从请求中提取 Token
     * 支持两种方式：
     * 1. Header: satoken: xxx
     * 2. Header: Authorization: xxx
     */
    private String extractToken(ServerWebExchange exchange) {
        // 优先从 satoken Header 获取
        String token = exchange.getRequest().getHeaders().getFirst("satoken");

        if (!StringUtils.hasText(token)) {
            // 尝试从 Authorization Header 获取
            token = exchange.getRequest().getHeaders().getFirst("Authorization");

            // 去除 Bearer 前缀
            if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
        }

        return token;
    }

    /**
     * 返回 401 未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建统一响应格式
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());

        String body = JSON.toJSONString(result);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 过滤器优先级
     * 数值越小优先级越高，这里设置为 -100 确保认证过滤器最先执行
     */
    @Override
    public int getOrder() {
        return -100;
    }
}

