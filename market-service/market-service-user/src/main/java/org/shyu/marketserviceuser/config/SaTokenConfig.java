package org.shyu.marketserviceuser.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 */
@Slf4j
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，使用自定义的认证逻辑
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 使用 SaHolder 获取当前请求对象
            String path = SaHolder.getRequest().getRequestPath();

            log.info("Sa-Token interceptor checking path: {}", path);

            // 白名单路径，直接放行
            if (isWhiteListPath(path)) {
                log.info("Path {} is in whitelist, skip authentication", path);
                return;
            }

            // 非白名单路径，需要登录验证
            log.info("Path {} requires authentication", path);
            StpUtil.checkLogin();
        })).addPathPatterns("/**");
    }

    /**
     * 判断是否是白名单路径
     * 注意：Gateway 配置了 StripPrefix=1，会移除 /api 前缀
     * 所以这里的路径不包含 /api
     */
    private boolean isWhiteListPath(String path) {
        // 认证相关接口（Gateway StripPrefix 后的路径）
        if (path.startsWith("/user/auth/")) {  // 对应前端的 /api/user/auth/*
            return true;
        }

        // 旧版注册登录接口（兼容）
        if (path.equals("/user/register") || path.equals("/user/login")) {
            return true;
        }

        // 管理员登录接口
        if (path.equals("/admin/login")) {
            return true;
        }

        // 微服务内部 Feign 调用接口
        if (path.startsWith("/feign/")) {
            return true;
        }

        // 仲裁管理接口（临时）
        if (path.startsWith("/arbitration/")) {
            return true;
        }

        // Feign 调用接口
        if (path.matches("/user/\\d+")) {  // 匹配 /user/1, /user/123 等
            return true;
        }
        if (path.equals("/user/username") || path.equals("/user/phone")) {
            return true;
        }

        // 统计接口（供其他服务调用）
        if (path.equals("/user/statistics")) {
            return true;
        }

        // 健康检查
        if (path.startsWith("/health")) {
            return true;
        }

        // 文档相关
        if (path.startsWith("/doc.html") ||
            path.startsWith("/swagger-resources") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/webjars")) {
            return true;
        }

        // 静态资源
        if (path.equals("/favicon.ico") || path.equals("/error")) {
            return true;
        }

        return false;
    }
}
