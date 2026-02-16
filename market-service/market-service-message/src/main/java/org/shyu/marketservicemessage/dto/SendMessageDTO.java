package org.shyu.marketservicemessage.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发送消息DTO
 *
 * @author shyu
 * @date 2026-02-15
 */
@Data
public class SendMessageDTO {

    /**
     * 接收者用户ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 消息类型: 1-文字 2-图片
     */
    @NotNull(message = "消息类型不能为空")
    private Integer messageType;

    /**
     * 关联商品ID(可选)
     */
    private Long productId;

}

