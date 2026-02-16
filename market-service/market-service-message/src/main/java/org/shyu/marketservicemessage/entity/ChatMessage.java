package org.shyu.marketservicemessage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天消息实体
 *
 * @author shyu
 * @date 2026-02-15
 */
@Data
@TableName("t_chat_message")
public class ChatMessage {

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发送者用户ID
     */
    private Long senderId;

    /**
     * 接收者用户ID
     */
    private Long receiverId;

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
     * 关联的商品ID(可选)
     */
    private Long productId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 删除标记: 0-未删除 1-已删除
     */
    @TableLogic
    private Integer status;

}

