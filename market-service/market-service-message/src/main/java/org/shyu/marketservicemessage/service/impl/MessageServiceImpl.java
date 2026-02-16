package org.shyu.marketservicemessage.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.shyu.marketapiuser.feign.UserFeignClient;
import org.shyu.marketapiuser.dto.UserDTO;
import org.shyu.marketcommon.exception.BusinessException;
import org.shyu.marketservicemessage.dto.SendMessageDTO;
import org.shyu.marketservicemessage.entity.ChatMessage;
import org.shyu.marketservicemessage.entity.Conversation;
import org.shyu.marketservicemessage.mapper.ChatMessageMapper;
import org.shyu.marketservicemessage.mapper.ConversationMapper;
import org.shyu.marketservicemessage.service.MessageService;
import org.shyu.marketservicemessage.vo.ChatMessageVO;
import org.shyu.marketservicemessage.vo.ConversationVO;
import org.shyu.marketservicemessage.websocket.ChatWebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 消息服务实现
 *
 * @author shyu
 * @date 2026-02-15
 */
@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired(required = false)  // RocketMQ 是可选组件
    private RocketMQTemplate rocketMQTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(SendMessageDTO dto, Long senderId) {
        // 1. 验证接收者是否存在
        UserDTO receiver = userFeignClient.getUserById(dto.getReceiverId()).getData();
        if (receiver == null) {
            throw new BusinessException("接收者不存在");
        }

        // 2. 保存消息到数据库
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent());
        message.setMessageType(dto.getMessageType());
        message.setIsRead(0);
        message.setProductId(dto.getProductId());
        chatMessageMapper.insert(message);

        // 3. 更新或创建会话
        updateOrCreateConversation(senderId, dto.getReceiverId(), dto.getContent());

        // 4. 通过 WebSocket 实时推送消息
        if (ChatWebSocketServer.isUserOnline(dto.getReceiverId())) {
            UserDTO sender = userFeignClient.getUserById(senderId).getData();
            Map<String, Object> wsMessage = new HashMap<>();
            wsMessage.put("type", "message");
            wsMessage.put("messageId", message.getId());
            wsMessage.put("senderId", senderId);
            wsMessage.put("senderUsername", sender.getUsername());
            wsMessage.put("senderAvatar", sender.getAvatar());
            wsMessage.put("content", dto.getContent());
            wsMessage.put("messageType", dto.getMessageType());
            wsMessage.put("productId", dto.getProductId());
            wsMessage.put("createTime", LocalDateTime.now());

            ChatWebSocketServer.sendMessageToUser(dto.getReceiverId(), JSONUtil.toJsonStr(wsMessage));
        }

        // 5. 发送 RocketMQ 消息(用于离线推送、消息归档等) - 非核心功能，失败不影响消息发送
        // 注意：如果 RocketMQ 未启动，这里会降级处理
        // 使用异步方式发送，避免阻塞主流程
        if (rocketMQTemplate != null) {
            // 异步发送，不等待结果
            CompletableFuture.runAsync(() -> {
                try {
                    rocketMQTemplate.convertAndSend("chat-message-topic", message);
                    log.info("RocketMQ消息推送成功");
                } catch (Exception e) {
                    log.warn("RocketMQ消息推送失败(不影响消息发送): {}", e.getMessage());
                }
            });
        } else {
            log.warn("RocketMQ未初始化，跳过消息推送");
        }

        log.info("消息发送成功: {} -> {}", senderId, dto.getReceiverId());
    }

    @Override
    public List<ConversationVO> getConversationList(Long userId) {
        // 查询该用户的所有会话
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Conversation::getUser1Id, userId).or().eq(Conversation::getUser2Id, userId));
        wrapper.orderByDesc(Conversation::getLastMessageTime);
        List<Conversation> conversations = conversationMapper.selectList(wrapper);

        // 转换为 VO 并填充对方用户信息
        List<ConversationVO> voList = new ArrayList<>();
        for (Conversation conversation : conversations) {
            ConversationVO vo = new ConversationVO();
            vo.setId(conversation.getId());
            vo.setLastMessage(conversation.getLastMessage());
            vo.setLastMessageTime(conversation.getLastMessageTime());

            // 确定对方用户ID
            Long otherUserId = conversation.getUser1Id().equals(userId)
                    ? conversation.getUser2Id()
                    : conversation.getUser1Id();
            vo.setOtherUserId(otherUserId);

            // 获取对方用户信息
            UserDTO otherUser = userFeignClient.getUserById(otherUserId).getData();
            if (otherUser != null) {
                vo.setOtherUsername(otherUser.getUsername());
                vo.setOtherAvatar(otherUser.getAvatar());
            }

            // 设置未读消息数
            Integer unreadCount = conversation.getUser1Id().equals(userId)
                    ? conversation.getUser1UnreadCount()
                    : conversation.getUser2UnreadCount();
            vo.setUnreadCount(unreadCount);

            voList.add(vo);
        }

        return voList;
    }

    @Override
    public Page<ChatMessageVO> getChatHistory(Long userId, Long otherUserId, Integer pageNum, Integer pageSize) {
        // 分页查询聊天记录
        Page<ChatMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(w1 -> w1.eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, otherUserId))
                .or(w2 -> w2.eq(ChatMessage::getSenderId, otherUserId).eq(ChatMessage::getReceiverId, userId))
        );
        wrapper.orderByDesc(ChatMessage::getCreateTime);
        Page<ChatMessage> messagePage = chatMessageMapper.selectPage(page, wrapper);

        // 批量获取用户信息
        Map<Long, UserDTO> userMap = new HashMap<>();
        userMap.put(userId, userFeignClient.getUserById(userId).getData());
        userMap.put(otherUserId, userFeignClient.getUserById(otherUserId).getData());

        // 转换为 VO
        List<ChatMessageVO> voList = messagePage.getRecords().stream().map(message -> {
            ChatMessageVO vo = new ChatMessageVO();
            BeanUtils.copyProperties(message, vo);

            // 填充发送者信息
            UserDTO sender = userMap.get(message.getSenderId());
            if (sender != null) {
                vo.setSenderUsername(sender.getUsername());
                vo.setSenderAvatar(sender.getAvatar());
            }

            // 填充接收者信息
            UserDTO receiver = userMap.get(message.getReceiverId());
            if (receiver != null) {
                vo.setReceiverUsername(receiver.getUsername());
                vo.setReceiverAvatar(receiver.getAvatar());
            }

            // 标记是否是当前用户发送
            vo.setIsSender(message.getSenderId().equals(userId));

            return vo;
        }).collect(Collectors.toList());

        Page<ChatMessageVO> voPage = new Page<>(messagePage.getCurrent(), messagePage.getSize(), messagePage.getTotal());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long otherUserId) {
        // 1. 标记消息为已读
        LambdaUpdateWrapper<ChatMessage> messageWrapper = new LambdaUpdateWrapper<>();
        messageWrapper.eq(ChatMessage::getSenderId, otherUserId)
                .eq(ChatMessage::getReceiverId, userId)
                .eq(ChatMessage::getIsRead, 0)
                .set(ChatMessage::getIsRead, 1);
        chatMessageMapper.update(null, messageWrapper);

        // 2. 更新会话未读数
        LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.and(w -> w
                .and(w1 -> w1.eq(Conversation::getUser1Id, userId).eq(Conversation::getUser2Id, otherUserId))
                .or(w2 -> w2.eq(Conversation::getUser1Id, otherUserId).eq(Conversation::getUser2Id, userId))
        );
        Conversation conversation = conversationMapper.selectOne(queryWrapper);

        if (conversation != null) {
            if (conversation.getUser1Id().equals(userId)) {
                conversation.setUser1UnreadCount(0);
            } else {
                conversation.setUser2UnreadCount(0);
            }
            conversationMapper.updateById(conversation);
        }

        log.info("标记消息为已读: {} <- {}", userId, otherUserId);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Conversation::getUser1Id, userId).or().eq(Conversation::getUser2Id, userId));
        List<Conversation> conversations = conversationMapper.selectList(wrapper);

        int totalUnread = 0;
        for (Conversation conversation : conversations) {
            if (conversation.getUser1Id().equals(userId)) {
                totalUnread += conversation.getUser1UnreadCount();
            } else {
                totalUnread += conversation.getUser2UnreadCount();
            }
        }

        return totalUnread;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(Long userId, Long conversationId) {
        // 验证会话是否属于该用户
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException("会话不存在");
        }

        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new BusinessException("无权删除该会话");
        }

        // 删除会话
        conversationMapper.deleteById(conversationId);
        log.info("删除会话: userId={}, conversationId={}", userId, conversationId);
    }

    /**
     * 更新或创建会话
     */
    private void updateOrCreateConversation(Long user1Id, Long user2Id, String lastMessage) {
        // 查询是否已存在会话
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .and(w1 -> w1.eq(Conversation::getUser1Id, user1Id).eq(Conversation::getUser2Id, user2Id))
                .or(w2 -> w2.eq(Conversation::getUser1Id, user2Id).eq(Conversation::getUser2Id, user1Id))
        );
        Conversation conversation = conversationMapper.selectOne(wrapper);

        if (conversation == null) {
            // 创建新会话
            conversation = new Conversation();
            conversation.setUser1Id(user1Id);
            conversation.setUser2Id(user2Id);
            conversation.setLastMessage(lastMessage);
            conversation.setLastMessageTime(LocalDateTime.now());
            conversation.setUser1UnreadCount(0);
            conversation.setUser2UnreadCount(1);
            conversationMapper.insert(conversation);
        } else {
            // 更新已有会话
            conversation.setLastMessage(lastMessage);
            conversation.setLastMessageTime(LocalDateTime.now());

            // 增加接收方未读数
            if (conversation.getUser1Id().equals(user2Id)) {
                conversation.setUser1UnreadCount(conversation.getUser1UnreadCount() + 1);
            } else {
                conversation.setUser2UnreadCount(conversation.getUser2UnreadCount() + 1);
            }

            conversationMapper.updateById(conversation);
        }
    }

}

