package org.shyu.marketservicemessage.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话VO
 *
 * @author shyu
 * @date 2026-02-15
 */
@Data
public class ConversationVO {

    /**
     * 会话ID
     */
    private Long id;

    /**
     * 对方用户ID
     */
    private Long otherUserId;

    /**
     * 对方用户名
     */
    private String otherUsername;

    /**
     * 对方头像
     */
    private String otherAvatar;

    /**
     * 最后一条消息
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 未读消息数
     */
    private Integer unreadCount;

}

