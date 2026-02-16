package org.shyu.marketservicemessage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.shyu.marketservicemessage.dto.SendMessageDTO;
import org.shyu.marketservicemessage.vo.ChatMessageVO;
import org.shyu.marketservicemessage.vo.ConversationVO;

import java.util.List;

/**
 * 消息服务接口
 *
 * @author shyu
 * @date 2026-02-15
 */
public interface MessageService {

    /**
     * 发送消息
     */
    void sendMessage(SendMessageDTO dto, Long senderId);

    /**
     * 获取会话列表
     */
    List<ConversationVO> getConversationList(Long userId);

    /**
     * 获取聊天记录(分页)
     */
    Page<ChatMessageVO> getChatHistory(Long userId, Long otherUserId, Integer pageNum, Integer pageSize);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long userId, Long otherUserId);

    /**
     * 获取未读消息总数
     */
    Integer getUnreadCount(Long userId);

    /**
     * 删除会话
     */
    void deleteConversation(Long userId, Long conversationId);

}

