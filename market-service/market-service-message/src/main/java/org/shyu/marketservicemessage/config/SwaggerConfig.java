package org.shyu.marketservicemessage.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Swagger 配置类
 *
 * @author shyu
 */
@Configuration
@EnableSwagger2
@ConditionalOnWebApplication
public class SwaggerConfig implements WebMvcConfigurer {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("org.shyu.marketservicemessage.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 解决Spring Boot 2.7.x与Swagger 3.0.0兼容性问题
     */
    @Bean
    public static BeanPostProcessor springfoxHandlerProviderBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping) {
                    try {
                        Field field = ReflectionUtils.findField(bean.getClass(), "mappingRegistry");
                        if (field != null) {
                            field.setAccessible(true);
                        }
                    } catch (Exception e) {
                        // 忽略异常
                    }
                }
                return bean;
            }
        };
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("消息服务API文档")
                .description("校园跳蚤市场-消息服务接口文档")
                .contact(new Contact("shyu", "", ""))
                .version("1.0")
                .build();
    }

}

