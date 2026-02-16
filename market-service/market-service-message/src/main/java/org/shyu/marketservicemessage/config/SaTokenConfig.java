package org.shyu.marketservicemessage.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 *
 * 注意：Message服务通过Gateway访问，Gateway已经完成了Token验证
 * Gateway会将userId通过Header传递给Message服务
 * 所以Message服务不需要再次验证Token，直接信任Gateway的认证结果
 *
 * @author shyu
 * @date 2026-02-15
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    // 不需要添加SaToken拦截器
    // Gateway已经完成认证，会通过 X-User-Id Header 传递用户ID

}



