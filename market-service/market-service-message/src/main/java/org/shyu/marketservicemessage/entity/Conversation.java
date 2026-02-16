package org.shyu.marketservicemessage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话实体
 *
 * @author shyu
 * @date 2026-02-15
 */
@Data
@TableName("t_conversation")
public class Conversation {

    /**
     * 会话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户1 ID
     */
    private Long user1Id;

    /**
     * 用户2 ID
     */
    private Long user2Id;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;

    /**
     * 用户1未读消息数
     */
    private Integer user1UnreadCount;

    /**
     * 用户2未读消息数
     */
    private Integer user2UnreadCount;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标记: 0-未删除 1-已删除
     */
    @TableLogic
    private Integer status;

}

