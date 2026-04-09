package org.shyu.marketservicearbitration.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 * @author shyu
 * @since 2026-04-01
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验。
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 路由拦截
            SaRouter
                .match("/**")
                .notMatch("/health/**", "/swagger-ui/**", "/doc.html", "/swagger-resources/**", "/webjars/**", "/v2/**", "/favicon.ico")
                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/**");
    }

}