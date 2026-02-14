package org.shyu.marketgateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 白名单配置
 * 配置不需要认证的接口路径
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway.auth")
public class WhiteListProperties {

    /**
     * 白名单路径列表
     * 这些路径不需要登录即可访问
     */
    private List<String> whiteList = Arrays.asList(
        // 用户服务 - 登录注册
        "/api/user/login",
        "/api/user/register",

        // 商品服务 - 游客可浏览
        "/api/product/list",
        "/api/product/detail",
        "/api/product/category",

        // Feign 内部调用接口
        "/feign"
    );

    /**
     * 检查路径是否在白名单中
     */
    public boolean isWhitePath(String path) {
        return whiteList.stream().anyMatch(path::startsWith);
    }
}

