package org.shyu.marketserviceuser.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger API Documentation Configuration
 * User Service API Documentation for Campus Flea Market Platform
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    /**
     * Create Swagger Docket Bean
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.shyu.marketserviceuser.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * API Information
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("User Service API")
                .description("Campus Flea Market - User Service API Documentation")
                .contact(new Contact("Market Team", "https://market.example.com", "market@example.com"))
                .version("1.0.0")
                .build();
    }
}

