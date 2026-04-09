package org.shyu.marketserviceproduct.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置
 * 配置哪些接口需要登录，哪些不需要
 */
@Slf4j
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 白名单：不需要登录就可以访问的接口
            SaRouter.match("/**")
                    .notMatch("/product/list")           // 商品列表（公开）
                    .notMatch("/product/detail/**")      // 商品详情（公开）
                    .notMatch("/product/statistics")     // 平台统计（公开）
                    .notMatch("/category/list")          // 分类列表（公开）
                    .notMatch("/category/**")            // 分类相关接口（公开）
                    .notMatch("/feign/**")               // Feign 内部调用
                    .notMatch("/error")                  // 错误页面
                    .check(r -> StpUtil.checkLogin());   // 其他接口需要登录
        })).addPathPatterns("/**");

        log.info("Sa-Token拦截器注册成功 - Product Service");
        log.info("公开接口（无需登录）：/product/list, /product/detail/*, /product/statistics, /category/**");
        log.info("需要登录的接口：/product/publish, /product/update, /product/delete, /product/my");
    }
}

