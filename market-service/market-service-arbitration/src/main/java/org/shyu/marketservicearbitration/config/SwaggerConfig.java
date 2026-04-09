package org.shyu.marketservicearbitration.config;

import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置（暂时禁用）
 * @author shyu
 * @since 2026-04-01
 */
@Configuration
//@EnableSwagger2
//@EnableKnife4j
public class SwaggerConfig {

    /*
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.shyu.marketservicearbitration.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("市场仲裁服务 API 文档")
                .description("提供仲裁申请、处理、统计等功能的 REST API")
                .version("1.0.0")
                .contact(new Contact("shyu", "", "shyu@example.com"))
                .build();
    }
    */
}