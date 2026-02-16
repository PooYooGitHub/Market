package org.shyu.marketservicemessage.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 离线消息VO
 *
 * @author shyu
 * @date 2026-02-16
 */
@Data
public class OfflineMessageVO {
    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者ID
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
     * 消息内容
     */
    private String content;

    /**
     * 消息类型（1=文字 2=图片 3=文件）
     */
    private Integer messageType;

    /**
     * 关联商品ID
     */
    private Long productId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

