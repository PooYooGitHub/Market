package org.shyu.marketservicemessage.listener;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketservicemessage.entity.ChatMessage;
import org.shyu.marketservicemessage.mapper.ChatMessageMapper;
import org.shyu.marketservicemessage.websocket.ChatWebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 聊天消息监听器
 * 用于消息归档、离线推送等场景
 *
 * @author shyu
 * @since 2026-02-15
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "chat-message-topic",
        consumerGroup = "chat-message-consumer"
)
public class ChatMessageListener implements RocketMQListener<ChatMessage> {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis Key 前缀：离线消息列表
     */
    private static final String OFFLINE_MESSAGE_KEY = "offline:message:user:";

    /**
     * 离线消息过期时间（7天）
     */
    private static final long OFFLINE_MESSAGE_EXPIRE_DAYS = 7;

    @Override
    public void onMessage(ChatMessage message) {
        log.info("RocketMQ收到消息: messageId={}, sender={}, receiver={}",
                message.getId(), message.getSenderId(), message.getReceiverId());

        try {
            // 1. 检查接收者是否在线
            Long receiverId = message.getReceiverId();
            boolean isOnline = ChatWebSocketServer.isUserOnline(receiverId);

            if (isOnline) {
                // 用户在线 - 通过WebSocket实时推送
                pushMessageToOnlineUser(message);
            } else {
                // 用户离线 - 存储到Redis离线消息队列
                storeOfflineMessage(message);
            }

            // 2. 消息归档和统计（可选功能）
            // archiveMessage(message);

        } catch (Exception e) {
            log.error("处理RocketMQ消息失败: messageId={}", message.getId(), e);
        }
    }

    /**
     * 推送消息给在线用户
     */
    private void pushMessageToOnlineUser(ChatMessage message) {
        try {
            // 获取发送者信息
            UserDTO sender = userFeignClient.getUserById(message.getSenderId()).getData();

            // 构建WebSocket消息
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "message");
            wsMessage.put("messageId", message.getId());
            wsMessage.put("senderId", message.getSenderId());
            wsMessage.put("senderUsername", sender != null ? sender.getUsername() : "未知用户");
            wsMessage.put("senderAvatar", sender != null ? sender.getAvatar() : "");
            wsMessage.put("content", message.getContent());
            wsMessage.put("messageType", message.getMessageType());
            wsMessage.put("productId", message.getProductId());
            wsMessage.put("createTime", message.getCreateTime());

            // 推送给接收者
            ChatWebSocketServer.sendMessageToUser(message.getReceiverId(), JSONUtil.toJsonStr(wsMessage));
            log.info("消息实时推送成功: messageId={}, receiver={}", message.getId(), message.getReceiverId());

        } catch (Exception e) {
            log.error("推送消息给在线用户失败: messageId={}", message.getId(), e);
            // 推送失败，存储为离线消息
            storeOfflineMessage(message);
        }
    }

    /**
     * 存储离线消息到Redis
     */
    private void storeOfflineMessage(ChatMessage message) {
        try {
            // Redis未配置时，跳过离线消息存储
            if (redisTemplate == null) {
                log.warn("Redis未配置，跳过离线消息存储: messageId={}, receiver={}",
                        message.getId(), message.getReceiverId());
                return;
            }

            String key = OFFLINE_MESSAGE_KEY + message.getReceiverId();

            // 将消息ID存入Redis列表
            redisTemplate.opsForList().rightPush(key, message.getId());

            // 设置过期时间
            redisTemplate.expire(key, OFFLINE_MESSAGE_EXPIRE_DAYS, TimeUnit.DAYS);

            log.info("离线消息已存储: messageId={}, receiver={}", message.getId(), message.getReceiverId());

        } catch (Exception e) {
            log.error("存储离线消息失败: messageId={}", message.getId(), e);
        }
    }

    /**
     * 消息归档（可选功能）
     */
    private void archiveMessage(ChatMessage message) {
        // TODO: 可以实现以下功能
        // 1. 同步到Elasticsearch用于全文搜索
        // 2. 消息敏感词过滤
        // 3. 消息统计分析
        // 4. 数据备份
        log.debug("消息归档: messageId={}", message.getId());
    }

}

