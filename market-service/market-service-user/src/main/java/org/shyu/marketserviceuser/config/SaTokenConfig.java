package org.shyu.marketserviceuser.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/user/auth/register",  // 注册接口
                        "/api/user/auth/login",     // 登录接口
                        "/doc.html",                // Knife4j文档
                        "/swagger-resources/**",    // Swagger资源
                        "/v3/api-docs/**",          // OpenAPI文档
                        "/webjars/**",              // 静态资源
                        "/favicon.ico",             // 图标
                        "/error"                    // 错误页面
                );
    }
}

