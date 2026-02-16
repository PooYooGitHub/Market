package org.shyu.marketservicemessage.service.impl;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketservicemessage.entity.ChatMessage;
import org.shyu.marketservicemessage.mapper.ChatMessageMapper;
import org.shyu.marketservicemessage.service.OfflineMessageService;
import org.shyu.marketservicemessage.vo.OfflineMessageVO;
import org.shyu.marketservicemessage.websocket.ChatWebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 离线消息服务实现
 *
 * @author shyu
 * @since 2026-02-16
 */
@Slf4j
@Service
@ConditionalOnBean(RedisTemplate.class)
public class OfflineMessageServiceImpl implements OfflineMessageService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    /**
     * Redis Key 前缀:离线消息列表
     */
    private static final String OFFLINE_MESSAGE_KEY = "offline:message:user:";

    @Override
    public List<OfflineMessageVO> getOfflineMessages(Long userId) {
        List<OfflineMessageVO> offlineMessages = new ArrayList<>();


        try {
            String key = OFFLINE_MESSAGE_KEY + userId;

            // 从Redis获取离线消息ID列表
            List<Object> messageIds = redisTemplate.opsForList().range(key, 0, -1);

            if (messageIds == null || messageIds.isEmpty()) {
                log.info("用户{}没有离线消息", userId);
                return offlineMessages;
            }

            // 查询消息详情
            for (Object messageIdObj : messageIds) {
                Long messageId = Long.valueOf(messageIdObj.toString());
                ChatMessage message = chatMessageMapper.selectById(messageId);

                if (message != null) {
                    OfflineMessageVO vo = convertToVO(message);
                    offlineMessages.add(vo);
                }
            }

            log.info("用户{}有{}条离线消息", userId, offlineMessages.size());

        } catch (Exception e) {
            log.error("获取离线消息失败: userId={}", userId, e);
        }

        return offlineMessages;
    }

    @Override
    public void clearOfflineMessages(Long userId) {
        // 如果Redis不可用，直接返回
        if (redisTemplate == null) {
            log.warn("Redis未配置，无法清除离线消息");
            return;
        }

        try {
            String key = OFFLINE_MESSAGE_KEY + userId;
            redisTemplate.delete(key);
            log.info("已清除用户{}的离线消息", userId);
        } catch (Exception e) {
            log.error("清除离线消息失败: userId={}", userId, e);
        }
    }

    @Override
    public int pushOfflineMessagesToUser(Long userId) {
        int pushedCount = 0;


        try {
            // 检查用户是否在线
            if (!ChatWebSocketServer.isUserOnline(userId)) {
                log.warn("用户{}不在线，无法推送离线消息", userId);
                return 0;
            }

            // 获取离线消息
            List<OfflineMessageVO> offlineMessages = getOfflineMessages(userId);

            if (offlineMessages.isEmpty()) {
                return 0;
            }

            // 推送每条离线消息
            for (OfflineMessageVO message : offlineMessages) {
                try {
                    Map<String, Object> wsMessage = new HashMap<>();
                    wsMessage.put("type", "offline_message");
                    wsMessage.put("messageId", message.getId());
                    wsMessage.put("senderId", message.getSenderId());
                    wsMessage.put("senderUsername", message.getSenderUsername());
                    wsMessage.put("senderAvatar", message.getSenderAvatar());
                    wsMessage.put("content", message.getContent());
                    wsMessage.put("messageType", message.getMessageType());
                    wsMessage.put("productId", message.getProductId());
                    wsMessage.put("createTime", message.getCreateTime());

                    ChatWebSocketServer.sendMessageToUser(userId, JSONUtil.toJsonStr(wsMessage));
                    pushedCount++;

                } catch (Exception e) {
                    log.error("推送离线消息失败: messageId={}", message.getId(), e);
                }
            }

            // 推送完成后清除离线消息
            if (pushedCount > 0) {
                clearOfflineMessages(userId);
                log.info("已推送{}条离线消息给用户{}", pushedCount, userId);
            }

        } catch (Exception e) {
            log.error("推送离线消息失败: userId={}", userId, e);
        }

        return pushedCount;
    }

    /**
     * 转换为VO
     */
    private OfflineMessageVO convertToVO(ChatMessage message) {
        OfflineMessageVO vo = new OfflineMessageVO();
        BeanUtils.copyProperties(message, vo);

        // 获取发送者信息
        try {
            UserDTO sender = userFeignClient.getUserById(message.getSenderId()).getData();
            if (sender != null) {
                vo.setSenderUsername(sender.getUsername());
                vo.setSenderAvatar(sender.getAvatar());
            }
        } catch (Exception e) {
            log.warn("获取发送者信息失败: senderId={}", message.getSenderId());
            vo.setSenderUsername("未知用户");
            vo.setSenderAvatar("");
        }

        return vo;
    }
}

