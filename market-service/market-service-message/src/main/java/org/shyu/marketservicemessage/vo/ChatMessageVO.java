package org.shyu.marketservicemessage.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息VO
 *
 * @author shyu
 * @date 2026-02-15
 */
@Data
public class ChatMessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者用户ID
     */
    private Long senderId;

    /**
     * 发送者用户名
     */
    private String senderUsername;

    /**
     * 发送者头像
     */
    private String senderAvatar;

    /**
     * 接收者用户ID
     */
    private Long receiverId;

    /**
     * 接收者用户名
     */
    private String receiverUsername;

    /**
     * 接收者头像
     */
    private String receiverAvatar;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型: 1-文字 2-图片 3-系统通知
     */
    private Integer messageType;

    /**
     * 是否已读: 0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 关联商品ID
     */
    private Long productId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否是当前用户发送: true-发送 false-接收
     */
    private Boolean isSender;

}

