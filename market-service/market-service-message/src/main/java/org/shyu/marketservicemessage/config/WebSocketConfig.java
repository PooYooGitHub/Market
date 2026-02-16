package org.shyu.marketservicemessage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类
 *
 * @author shyu
 * @date 2026-02-15
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig {

    /**
     * 注入 ServerEndpointExporter
     * 自动注册使用 @ServerEndpoint 注解的 WebSocket endpoint
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

}

