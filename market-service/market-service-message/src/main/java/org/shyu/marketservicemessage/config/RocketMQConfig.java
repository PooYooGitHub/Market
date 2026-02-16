package org.shyu.marketservicemessage.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * RocketMQ 配置类
 * <p>
 * 说明：RocketMQ 是可选组件，主要用于：
 * 1. 离线消息推送
 * 2. 消息归档
 * 3. 消息审计
 * <p>
 * 如果 RocketMQ 未启动，系统会自动降级：
 * - 仍然通过 WebSocket 实时推送
 * - 消息正常保存到数据库
 * - 只是不会发送到 MQ（不影响核心功能）
 *
 * @author shyu
 * @since 2026-02-16
 */
@Slf4j
@Configuration
@ConditionalOnClass(RocketMQTemplate.class)
@ConditionalOnProperty(prefix = "rocketmq", name = "name-server")
public class RocketMQConfig {

    @PostConstruct
    public void init() {
        log.info("==========================================");
        log.info("RocketMQ 配置已加载");
        log.info("如果 RocketMQ 未启动，消息发送会自动降级");
        log.info("不影响 WebSocket 实时推送和数据库存储");
        log.info("==========================================");
    }
}

