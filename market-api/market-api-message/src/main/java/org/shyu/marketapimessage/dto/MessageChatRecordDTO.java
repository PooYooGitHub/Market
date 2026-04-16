package org.shyu.marketapimessage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageChatRecordDTO {

    private Long id;

    private Long senderId;

    private Long receiverId;

    private String content;

    private Integer messageType;

    private Integer isRead;

    private Long productId;

    private LocalDateTime createTime;
}
